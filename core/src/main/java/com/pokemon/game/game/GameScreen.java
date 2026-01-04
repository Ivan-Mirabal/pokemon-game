package com.pokemon.game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pokemon.game.*;
import com.pokemon.game.item.Crafteo;
import com.pokemon.game.item.Item;
import com.pokemon.game.item.Pokeball;
import com.pokemon.game.player.Inventario;
import com.pokemon.game.player.Player;
import com.pokemon.game.player.Ranura;
import com.pokemon.game.pokemon.*;

import java.util.HashMap;

import java.util.List;

public class GameScreen implements Screen {

    final PokemonGame game;

    // Variables
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer renderer;
    private String currentMapFile;

    // Cámara y Escalado
    private OrthographicCamera camara;
    private Viewport viewport;

    // Dimensiones del mapa actual
    private int mapWidth, mapHeight, tileWidth, tileHeight;
    private float worldWidthPx, worldHeightPx;

    // Variables del Jugador
    private SpriteBatch spriteBatch;
    private Player player;

    // Escala para zoom
    private float zoomScale = 2.0f;

    // Capa de colisiones
    private TiledMapTileLayer collisionLayer;

    // Límites de la cámara
    private float cameraMinX, cameraMaxX, cameraMinY, cameraMaxY;

    // Variables para carga inicial
    private String initialMap;
    private float startX, startY;

    private HashMap<String, Texture> pokemonSprites;

    // Cooldown para evitar transiciones infinitas
    private float transitionCooldown = 0f;
    private static final float TRANSITION_COOLDOWN_TIME = 0.3f;

    // Variables para el Menú
    private BitmapFont font;
    private Texture whitePixel;

    private Combate combateActivo;
    private boolean enCombate;
    private Pokemon pokemonSalvajeActual;

    public GameScreen(final PokemonGame game, String initialMap, float startX, float startY) {
        this.game = game;
        this.initialMap = initialMap;
        this.startX = startX;
        this.startY = startY;
    }

    @Override
    public void show() {
        loadMap(initialMap, startX, startY);
        game.musics.stopmenumusic();
        game.musics.startopenworldmusic();
        cargarSpritesPokemon();

        // Inicializar fuente
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(0.8f);

        // Crear textura blanca programáticamente
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();
    }

    private void loadMap(String mapFile, float playerX, float playerY) {
        if (mapa != null) mapa.dispose();
        if (renderer != null) renderer.dispose();

        currentMapFile = mapFile;
        mapa = MapManager.loadMap(mapFile);

        mapWidth = mapa.getProperties().get("width", Integer.class);
        mapHeight = mapa.getProperties().get("height", Integer.class);
        tileWidth = mapa.getProperties().get("tilewidth", Integer.class);
        tileHeight = mapa.getProperties().get("tileheight", Integer.class);

        worldWidthPx = mapWidth * tileWidth;
        worldHeightPx = mapHeight * tileHeight;

        renderer = new OrthogonalTiledMapRenderer(mapa, 1f);
        collisionLayer = (TiledMapTileLayer) mapa.getLayers().get("Colisiones");

        if (spriteBatch == null) {
            spriteBatch = new SpriteBatch();
        }

        if (player == null) {
            player = new Player("sprites/player.png", playerX, playerY, tileWidth, tileHeight, this);
        } else {
            player.x = playerX;
            player.y = playerY;
            transitionCooldown = TRANSITION_COOLDOWN_TIME;
        }

        if (camara == null) {
            camara = new OrthographicCamera();
        }

        if (viewport == null) {
            float viewportWidth = Gdx.graphics.getWidth() / zoomScale;
            float viewportHeight = Gdx.graphics.getHeight() / zoomScale;
            viewport = new FitViewport(viewportWidth, viewportHeight, camara);
        }

        calculateCameraBounds(viewport.getWorldWidth(), viewport.getWorldHeight());
        updateCamera();
    }

    private void checkMapTransition() {
        if (transitionCooldown > 0) return;

        MapManager.MapInfo currentMapInfo = MapManager.getMapInfo(currentMapFile);
        if (currentMapInfo == null) return;

        float margin = tileWidth * 0.5f;
        float playerLeft = player.x - player.width / 2;
        float playerRight = player.x + player.width / 2;
        float playerBottom = player.y - player.height / 2;
        float playerTop = player.y + player.height / 2;

        if (playerTop >= worldHeightPx - margin && currentMapInfo.northMap != null) {
            transitionToMap(currentMapInfo.northMap, player.x, tileHeight * 2);
            transitionCooldown = TRANSITION_COOLDOWN_TIME;
        } else if (playerBottom <= margin && currentMapInfo.southMap != null) {
            transitionToMap(currentMapInfo.southMap, player.x, worldHeightPx - tileHeight * 3);
            transitionCooldown = TRANSITION_COOLDOWN_TIME;
        } else if (playerRight >= worldWidthPx - margin && currentMapInfo.eastMap != null) {
            transitionToMap(currentMapInfo.eastMap, tileWidth * 2, player.y);
            transitionCooldown = TRANSITION_COOLDOWN_TIME;
        } else if (playerLeft <= margin && currentMapInfo.westMap != null) {
            transitionToMap(currentMapInfo.westMap, worldWidthPx - tileWidth * 3, player.y);
            transitionCooldown = TRANSITION_COOLDOWN_TIME;
        }
    }

    private void transitionToMap(String newMapFile, float newPlayerX, float newPlayerY) {
        loadMap(newMapFile, newPlayerX, newPlayerY);
    }

    private void calculateCameraBounds(float viewportWidth, float viewportHeight) {
        cameraMinX = viewportWidth / 2;
        cameraMaxX = worldWidthPx - viewportWidth / 2;
        cameraMinY = viewportHeight / 2;
        cameraMaxY = worldHeightPx - viewportHeight / 2;

        if (cameraMaxX < cameraMinX) {
            cameraMinX = cameraMaxX = worldWidthPx / 2;
        }
        if (cameraMaxY < cameraMinY) {
            cameraMinY = cameraMaxY = worldHeightPx / 2;
        }
    }

    private void updateCamera() {
        float cameraX = Math.max(cameraMinX, Math.min(cameraMaxX, player.x));
        float cameraY = Math.max(cameraMinY, Math.min(cameraMaxY, player.y));
        camara.position.set(cameraX, cameraY, 0);
        camara.update();
    }

    public boolean isCollision(float x, float y) {
        return isCollisionRect(x, y, 0, 0); // Para compatibilidad
    }

    // NUEVO MÉTODO: Verifica colisión con el rectángulo completo del jugador
    public boolean isCollisionRect(float centerX, float centerY, float width, float height) {
        // Usar un rectángulo más pequeño (70% del tamaño original)
        float collisionWidth = width * 0.7f;
        float collisionHeight = height * 0.7f;

        // Calcular los bordes del rectángulo de colisión
        float left = centerX - collisionWidth/2;
        float right = centerX + collisionWidth/2;
        float bottom = centerY - collisionHeight/2;
        float top = centerY + collisionHeight/2;

        // Convertir a celdas de tiles
        int leftTile = (int) (left / tileWidth);
        int rightTile = (int) (right / tileWidth);
        int bottomTile = (int) (bottom / tileHeight);
        int topTile = (int) (top / tileHeight);

        // Verificar todas las celdas dentro del área del jugador
        for (int tileX = leftTile; tileX <= rightTile; tileX++) {
            for (int tileY = bottomTile; tileY <= topTile; tileY++) {
                if (tileX < 0 || tileX >= mapWidth || tileY < 0 || tileY >= mapHeight) {
                    return true;
                }

                if (collisionLayer != null) {
                    TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
                    if (cell != null && cell.getTile() != null) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void cargarSpritesPokemon() {
        pokemonSprites = new HashMap<>();
        String[] nombresPokemon = {"pikachu", "bulbasaur", "squirtle", "charmander", "charizard"};

        for (String nombre : nombresPokemon) {
            try {
                Texture sprite = new Texture(Gdx.files.internal("sprites/pokemon/" + nombre + ".png"));
                pokemonSprites.put(nombre, sprite);
                System.out.println("Sprite precargado: " + nombre);
            } catch (Exception e) {
                System.out.println("No se pudo cargar sprite para: " + nombre);
                // El placeholder se creará en PokemonJugador
            }
        }
    }

    @Override
    public void render(float delta) {
        // 1. Actualizar cooldown
        if (transitionCooldown > 0) {
            transitionCooldown -= delta;
        }

        // 2. Si hay combate activo, manejar combate
        if (enCombate && combateActivo != null) {
            manejarCombate(delta);
        } else {
            // 3. Si NO hay combate, manejar entrada normal
            handleInput();

            // 4. Lógica del juego (solo si NO hay menú abierto)
            if (player.getMenuState() == MenuState.NONE) {
                player.update(delta);
                checkMapTransition();
                updateCamera();
            }
        }

        // 3. Limpiar pantalla
        ScreenUtils.clear(0, 0, 0, 1);

        // 4. Dibujar el mundo (SIEMPRE)
        renderer.setView(camara);
        renderer.render();

        // 5. Dibujar al jugador (SIEMPRE)
        spriteBatch.setProjectionMatrix(camara.combined);
        spriteBatch.begin();
        spriteBatch.draw(player.currentFrame,
            player.x - player.width / 2,
            player.y - player.height / 2,
            player.width,
            player.height);
        spriteBatch.end();

        // 6. Dibujar HUD (solo si no hay menú)
        if (player.getMenuState() == MenuState.NONE) {
            dibujarHUD();
        }

        // 7. Dibujar menú (si hay alguno activo)
        if (player.getMenuState() != MenuState.NONE) {
            dibujarMenu();
        }
    }

    // Manejar entrada del teclado
    private void handleInput() {

        if (enCombate && combateActivo != null) {
            manejarEntradaCombate();
        } else {
            // Tecla I para abrir/cerrar menú principal
            if (Gdx.input.isKeyJustPressed(Keys.I)) {
                if (player.getMenuState() == MenuState.NONE) {
                    game.musics.stopopenworldmusic();
                    game.musics.startpausemusic();
                    player.setMenuState(MenuState.MAIN);
                } else {
                    player.setMenuState(MenuState.NONE);
                    game.musics.stoppausemusic();
                    game.musics.startopenworldmusic();
                }
            }

            // Tecla ESC para retroceder
            if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
                player.goBack();
                if(player.getMenuState()== MenuState.NONE){
                    game.musics.stoppausemusic();
                    game.musics.startopenworldmusic();
                }
            }



            // Manejo de entrada según el estado del menú
            if (player.getMenuState() != MenuState.NONE) {
                // PRIMERO: Manejo específico para CRAFTING
                if (player.getMenuState() == MenuState.CRAFTING) {
                    if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W)) {
                        player.moverSeleccionCrafteoArriba();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S)) {
                        player.moverSeleccionCrafteoAbajo();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                        boolean exito = player.intentarCraftear();
                        if (exito) {
                            System.out.println("¡Ítem crafteado con éxito!");
                        } else {
                            System.out.println("No tienes los materiales necesarios.");
                        }
                    }
                }
                if (player.getMenuState() == MenuState.POKEMON_TEAM) {
                    // Navegación en equipo (2 columnas)
                    if (Gdx.input.isKeyJustPressed(Keys.UP)) {
                        player.movePokemonTeamUp();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
                        player.movePokemonTeamDown();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
                        player.movePokemonTeamLeft();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
                        player.movePokemonTeamRight();
                    }

                    // A (o ENTER) para ver detalles
                    if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.A)) {
                        if (player.getPokemonSeleccionado() != null) {
                            player.setMenuState(MenuState.POKEMON_DETAIL);
                            player.setPokemonDetailTab(0); // Empezar en primera pestaña
                        }
                    }

                    // B (o ESC) para volver al menú principal
                    if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.B)) {
                        player.setMenuState(MenuState.MAIN);
                    }
                }

                if (player.getMenuState() == MenuState.POKEMON_DETAIL) {
                    // ←→ para cambiar pestañas
                    if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
                        player.prevPokemonDetailTab();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
                        player.nextPokemonDetailTab();
                    }

                    // B (o ESC) para volver al equipo
                    if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.B)) {
                        player.setMenuState(MenuState.POKEMON_TEAM);
                    }
                }
                // SEGUNDO: Manejo para otros menús
                else {
                    // Flechas para navegar (para todos los menús excepto CRAFTING)
                    if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W)) {
                        player.moveMenuUp();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S)) {
                        player.moveMenuDown();
                    }

                    // Enter para seleccionar
                    if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                        player.selectMenuItem();
                    }
                }

                // TERCERO: Manejo específico para INVENTORY con item seleccionado
                // (esto es independiente, puede coexistir con la navegación)
                if (player.getMenuState() == MenuState.INVENTORY && player.getItemSeleccionado() != null) {
                    if (Gdx.input.isKeyJustPressed(Keys.NUM_1)) {
                        player.usarItemSeleccionado();
                        player.cancelarSeleccionItem();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.NUM_2)) {
                        player.tirarItemSeleccionado();
                        player.cancelarSeleccionItem();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.NUM_3)) {
                        player.cancelarSeleccionItem();
                        System.out.println("Selección cancelada");
                    }
                }
            }
        }
    }

    // Método para dibujar el menú activo
    private void dibujarMenu() {
        // Usar proyección de pantalla para el menú
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.begin();

        // Fondo semitransparente SOLO si NO estamos en equipo/detalle Pokémon
        if (player.getMenuState() != MenuState.POKEMON_TEAM &&
            player.getMenuState() != MenuState.POKEMON_DETAIL) {
            spriteBatch.setColor(0, 0, 0, 0.7f);
            spriteBatch.draw(whitePixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            spriteBatch.setColor(Color.WHITE);
        }

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Dibujar según el estado del menú
        switch (player.getMenuState()) {
            case MAIN:
                dibujarMenuPrincipal(screenWidth, screenHeight);
                break;
            case INVENTORY:
                dibujarInventario(screenWidth, screenHeight);
                break;
            case POKEMON_TEAM:
                dibujarEquipoPokemon(screenWidth, screenHeight);
                // NO dibujar instrucciones generales aquí
                break;
            case POKEMON_DETAIL:
                dibujarDetallePokemon(screenWidth, screenHeight);
                // NO dibujar instrucciones generales aquí
                break;
            case POKEDEX:
                dibujarPokedex(screenWidth, screenHeight);
                break;
            case CRAFTING:
                dibujarCrafteo(screenWidth, screenHeight);
                break;
            case SAVE:
                dibujarGuardar(screenWidth, screenHeight);
                break;
            case OPTIONS:
                dibujarOpciones(screenWidth, screenHeight);
                break;
        }

        // Instrucciones generales SOLO para algunos menús
        // NO para Pokémon Team/Detail
        if (player.getMenuState() != MenuState.POKEMON_TEAM &&
            player.getMenuState() != MenuState.POKEMON_DETAIL) {
            dibujarInstrucciones(screenWidth, screenHeight);
        }

        spriteBatch.end();

        // Restaurar proyección de la cámara
        spriteBatch.setProjectionMatrix(camara.combined);
    }

    // Menú Principal
    private void dibujarMenuPrincipal(int screenWidth, int screenHeight) {
        font.getData().setScale(2.0f);
        String titulo = "MENÚ PRINCIPAL";
        float tituloWidth = font.getData().scaleX * titulo.length() * 10;
        font.draw(spriteBatch, titulo, (screenWidth - tituloWidth) / 2, screenHeight - 100);
        font.getData().setScale(1.0f);

        String[] opciones = {
            "Pokémon",
            "Pokédex",
            "Inventario",
            "Crafteo",
            "Guardar partida",
            "Opciones"
        };

        float startX = screenWidth / 2 - 100;
        float startY = screenHeight / 2 + 100;
        float espacio = 40;

        for (int i = 0; i < opciones.length; i++) {
            if (i == player.getMenuSelection()) {
                font.setColor(Color.YELLOW);
                font.draw(spriteBatch, "> " + opciones[i], startX - 20, startY - i * espacio);
                font.setColor(Color.WHITE);
            } else {
                font.draw(spriteBatch, opciones[i], startX, startY - i * espacio);
            }
        }
    }

    // Inventario
    // Solo muestro la parte modificada del método dibujarInventario()
    // Inventario - CON SELECCIÓN VISUAL
    private void dibujarInventario(int screenWidth, int screenHeight) {
        font.getData().setScale(1.8f);
        font.draw(spriteBatch, "INVENTARIO", screenWidth / 2 - 80, screenHeight - 80);
        font.getData().setScale(1.0f);

        Inventario inv = player.getInventario();
        String capacidad = "Capacidad: " + inv.getCantidadTotal() +
            "/" + inv.getCapacidadMaxima() + " ítems";
        String slotsInfo = "(" + inv.getCantidadItems() + " tipos diferentes)";

        font.draw(spriteBatch, capacidad, screenWidth / 2 - 100, screenHeight - 120);
        font.draw(spriteBatch, slotsInfo, screenWidth / 2 - 100, screenHeight - 140);

        // Mostrar Poké Balls y Pociones destacados
        Ranura pokeballs = player.getInventario().buscarItem("Poké Ball");
        Ranura pociones = player.getInventario().buscarItem("Poción");

        float y = screenHeight - 170;
        if (pokeballs != null) {
            font.draw(spriteBatch, "Poké Balls: " + pokeballs.getCantidad(), screenWidth * 0.2f, y);
        }
        if (pociones != null) {
            font.draw(spriteBatch, "Pociones: " + pociones.getCantidad(), screenWidth * 0.6f, y);
        }

        // Lista de todos los items CON SELECCIÓN
        float startX = screenWidth * 0.2f;
        float startY = y - 50;
        float espacio = 25;

        List<Ranura> slots = player.getInventario().getRanuras();

        // Si hay un item seleccionado, mostrarlo destacado
        Ranura itemSeleccionado = player.getItemSeleccionado();
        int indiceSeleccionado = -1;
        if (itemSeleccionado != null) {
            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i).getItem().getNombre().equals(itemSeleccionado.getItem().getNombre())) {
                    indiceSeleccionado = i;
                    break;
                }
            }
        }

        for (int i = 0; i < slots.size(); i++) {
            Ranura slot = slots.get(i);
            float itemY = startY - i * espacio;

            if (itemY < 100) {
                startX += 200;
                startY = y - 50;
                i--;
                continue;
            }

            // Resaltar item seleccionado
            if (i == indiceSeleccionado) {
                font.setColor(Color.YELLOW);
                font.draw(spriteBatch, "> " + slot.getItem().getNombre() + " x" + slot.getCantidad(), startX, itemY);
                font.setColor(Color.WHITE);
            }
            // Resaltar item en el que está el cursor del menú
            else if (i == player.getMenuSelection()) {
                font.setColor(Color.CYAN);
                font.draw(spriteBatch, "• " + slot.getItem().getNombre() + " x" + slot.getCantidad(), startX, itemY);
                font.setColor(Color.WHITE);
            } else {
                font.draw(spriteBatch, "• " + slot.getItem().getNombre() + " x" + slot.getCantidad(), startX, itemY);
            }
        }

        // Mostrar instrucciones si hay item seleccionado
        if (itemSeleccionado != null) {
            font.draw(spriteBatch, "ITEM SELECCIONADO: " + itemSeleccionado.getItem().getNombre(),
                screenWidth * 0.6f, screenHeight - 200);
            font.draw(spriteBatch, "1: Usar | 2: Tirar 1 | 3: Cancelar",
                screenWidth * 0.6f, screenHeight - 230);
        }
    }

    // Pokémon (placeholder)
    // ===============================
// VISTA DE EQUIPO (2 COLUMNAS)
// ===============================
    private void dibujarEquipoPokemon(int screenWidth, int screenHeight) {
        List<PokemonJugador> equipo = player.getEntrenador().getEquipo();

        // 1. FONDO
        spriteBatch.setColor(0.1f, 0.1f, 0.15f, 1);
        spriteBatch.draw(whitePixel, 0, 0, 800, 600);
        spriteBatch.setColor(Color.WHITE);

        // 2. TÍTULO
        font.getData().setScale(1.8f);
        font.setColor(new Color(0.9f, 0.9f, 1.0f, 1));
        font.draw(spriteBatch, "EQUIPO POKÉMON", 250, 560);

        // 3. LÍNEA DIVISORIA
        spriteBatch.setColor(new Color(0.5f, 0.5f, 0.6f, 1));
        spriteBatch.draw(whitePixel, 30, 530, 740, 2);
        spriteBatch.setColor(Color.WHITE);

        // 4. POSICIONES FIJAS PARA 6 SLOTS (2x3) - ORGANIZADAS POR FILAS
        float[][] posiciones = {
            // Fila 1 (arriba)
            {50, 400},   // Slot 0: Izquierda - Pokémon 1
            {410, 400},  // Slot 1: Derecha - Pokémon 2

            // Fila 2 (medio)
            {50, 270},   // Slot 2: Izquierda - Pokémon 3
            {410, 270},  // Slot 3: Derecha - Pokémon 4

            // Fila 3 (abajo)
            {50, 140},   // Slot 4: Izquierda - Pokémon 5
            {410, 140}   // Slot 5: Derecha - Pokémon 6
        };

        // Tamaño fijo de cada slot
        float slotAncho = 340;
        float slotAlto = 100;

        // 5. DIBUJAR LOS 6 SLOTS
        for (int i = 0; i < 6; i++) {
            float x = posiciones[i][0];
            float y = posiciones[i][1];

            // Fondo del slot
            if (i == player.getPokemonTeamSelection()) {
                // Seleccionado - azul
                spriteBatch.setColor(new Color(0.4f, 0.5f, 0.9f, 0.9f));
            } else {
                // No seleccionado - gris claro
                spriteBatch.setColor(new Color(0.2f, 0.2f, 0.25f, 0.9f));
            }
            spriteBatch.draw(whitePixel, x, y, slotAncho, slotAlto);
            spriteBatch.setColor(Color.WHITE);

            // Borde del slot
            spriteBatch.setColor(new Color(0.8f, 0.8f, 0.85f, 1));
            spriteBatch.draw(whitePixel, x, y, slotAncho, 2); // Superior
            spriteBatch.draw(whitePixel, x, y + slotAlto, slotAncho, 2); // Inferior
            spriteBatch.draw(whitePixel, x, y, 2, slotAlto); // Izquierdo
            spriteBatch.draw(whitePixel, x + slotAncho, y, 2, slotAlto); // Derecho
            spriteBatch.setColor(Color.WHITE);

            // 6. SI HAY POKÉMON EN ESTE SLOT
            if (i < equipo.size()) {
                PokemonJugador p = equipo.get(i);

                // SPRITE DEL POKÉMON (NUEVO)
                float spriteX = x + 10;
                float spriteY = y + 10;
                float spriteWidth = 80;
                float spriteHeight = 80;

                if (p.getSprite() != null) {
                    spriteBatch.draw(p.getSprite(), spriteX, spriteY, spriteWidth, spriteHeight);
                } else {
                    // Placeholder si no hay sprite
                    spriteBatch.setColor(getColorPorTipo(p.getTipoPrimario()));
                    spriteBatch.draw(whitePixel, spriteX, spriteY, spriteWidth, spriteHeight);
                    spriteBatch.setColor(Color.WHITE);
                }

                // Nombre (ajustado si es muy largo) - movido a la derecha del sprite
                font.getData().setScale(1.2f);
                if (i == player.getPokemonTeamSelection()) {
                    font.setColor(new Color(0.0f, 0.2f, 0.6f, 1)); // Azul oscuro
                } else {
                    font.setColor(new Color(0.2f, 0.2f, 0.4f, 1)); // Gris azulado
                }

                String nombre = p.getApodo();
                if (nombre.length() > 8) {
                    nombre = nombre.substring(0, 8) + "...";
                }
                // Posición ajustada por el sprite
                float nombreX = x + 100;
                font.draw(spriteBatch, nombre, nombreX, y + 80);

                // Nivel (derecha)
                font.getData().setScale(1.0f);
                font.draw(spriteBatch, "Nv. " + p.getNivel(), x + slotAncho - 70, y + 80);

                // Barra de PS - ajustada por el sprite
                float porcentajePS = (float)p.getPsActual() / p.getPsMaximos();
                float barraAncho = 220;
                float barraY = y + 55;

                // Fondo barra
                spriteBatch.setColor(new Color(0.85f, 0.85f, 0.9f, 1));
                spriteBatch.draw(whitePixel, x + 100, barraY, barraAncho, 10);

                // Color barra según PS
                if (porcentajePS > 0.5) {
                    spriteBatch.setColor(new Color(0.0f, 0.8f, 0.2f, 1));
                } else if (porcentajePS > 0.2) {
                    spriteBatch.setColor(new Color(1.0f, 0.8f, 0.0f, 1));
                } else {
                    spriteBatch.setColor(Color.RED);
                }
                spriteBatch.draw(whitePixel, x + 100, barraY, barraAncho * porcentajePS, 10);

                // Texto PS (debajo de la barra)
                spriteBatch.setColor(Color.WHITE);
                font.getData().setScale(0.9f);
                font.setColor(new Color(0.4f, 0.4f, 0.6f, 1));
                String psTexto = p.getPsActual() + "/" + p.getPsMaximos();
                font.draw(spriteBatch, psTexto, x + 100, barraY - 12);

                // Estado debilitado
                if (p.estaDebilitado()) {
                    font.setColor(Color.RED);
                    font.getData().setScale(0.8f);
                    font.draw(spriteBatch, "DEBILITADO", x + 100, y + 20);
                }

                font.getData().setScale(1.0f);
                spriteBatch.setColor(Color.WHITE);
            } else {
                // SLOT VACÍO
                font.setColor(new Color(0.6f, 0.6f, 0.7f, 1));
                font.draw(spriteBatch, "--- VACÍO ---", x + 120, y + 50);
            }
        }

        // 7. INSTRUCCIONES
        font.getData().setScale(0.9f);
        font.setColor(new Color(0.4f, 0.4f, 0.6f, 1));
        String instrucciones = "↑↓←→: Navegar  Enter: Seleccionar  ESC: Volver";
        font.draw(spriteBatch, instrucciones, 250, 50);

        // 8. RESTAURAR
        font.getData().setScale(1.0f);
        font.setColor(Color.WHITE);
    }

    // ===============================
// VISTA DE DETALLE INDIVIDUAL
// ===============================
    private void dibujarDetallePokemon(int screenWidth, int screenHeight) {
        PokemonJugador p = player.getPokemonSeleccionado();
        if (p == null) return;

        // 1. FONDO GENERAL (igual que equipo Pokémon)
        spriteBatch.setColor(0.1f, 0.1f, 0.15f, 1);
        spriteBatch.draw(whitePixel, 0, 0, screenWidth, screenHeight);
        spriteBatch.setColor(Color.WHITE);

        // ===== LADO IZQUIERDO: SPRITE Y NIVEL =====
        float izquierdaAncho = screenWidth * 0.4f;

        // Marco para el sprite - ESTILO OSCURO
        float marcoX = 50;
        float marcoY = 120;
        float marcoAncho = izquierdaAncho - 100;
        float marcoAlto = screenHeight - 240;

        // Fondo del marco (oscuro)
        spriteBatch.setColor(new Color(0.2f, 0.2f, 0.25f, 0.9f));
        spriteBatch.draw(whitePixel, marcoX, marcoY, marcoAncho, marcoAlto);

        // Borde del marco (gris claro)
        spriteBatch.setColor(new Color(0.5f, 0.5f, 0.6f, 1));
        // Bordes más gruesos (3px)
        spriteBatch.draw(whitePixel, marcoX, marcoY, marcoAncho, 3);
        spriteBatch.draw(whitePixel, marcoX, marcoY + marcoAlto - 3, marcoAncho, 3);
        spriteBatch.draw(whitePixel, marcoX, marcoY, 3, marcoAlto);
        spriteBatch.draw(whitePixel, marcoX + marcoAncho - 3, marcoY, 3, marcoAlto);
        spriteBatch.setColor(Color.WHITE);

        // Nivel (arriba)
        font.getData().setScale(2.0f);
        font.setColor(new Color(0.9f, 0.9f, 1.0f, 1)); // Texto claro
        float nivelY = marcoY + marcoAlto - 40;
        font.draw(spriteBatch, "Nv. " + p.getNivel(), marcoX + 20, nivelY);

        // Sprite del Pokémon (centrado en el marco)
        float spriteX = marcoX + (marcoAncho / 2) - 64;
        float spriteY = marcoY + (marcoAlto / 2) - 64;

        // Placeholder para sprite (color por tipo)
        spriteBatch.setColor(getColorPorTipo(p.getTipoPrimario()));
        spriteBatch.draw(whitePixel, spriteX, spriteY, 128, 128);

        // DIBUJAR SPRITE REAL DEL POKÉMON (MODIFICADO)
        if (p.getSprite() != null) {
            spriteBatch.draw(p.getSprite(), spriteX, spriteY, 128, 128);
        } else {
            // Placeholder si no hay sprite
            spriteBatch.setColor(getColorPorTipo(p.getTipoPrimario()));
            spriteBatch.draw(whitePixel, spriteX, spriteY, 128, 128);
            spriteBatch.setColor(Color.WHITE);

            // Letra inicial (placeholder)
            font.getData().setScale(3.0f);
            font.setColor(Color.WHITE);
            String inicial = p.getNombre().substring(0, 1);
            font.draw(spriteBatch, inicial, spriteX + 50, spriteY + 70);
            font.getData().setScale(1.0f);
        }

        // Nombre (abajo)
        font.getData().setScale(1.5f);
        font.setColor(new Color(0.9f, 0.9f, 1.0f, 1)); // Texto claro
        float nombreY = marcoY + 40;
        font.draw(spriteBatch, p.getApodo(), marcoX + 20, nombreY);
        font.getData().setScale(1.0f);
        font.setColor(new Color(0.7f, 0.7f, 0.9f, 1)); // Texto secundario
        font.draw(spriteBatch, "(" + p.getNombre() + ")", marcoX + 20, nombreY - 25);

        // ===== LADO DERECHO: 4 PESTAÑAS =====
        float derechaX = izquierdaAncho + 20;
        float derechaAncho = screenWidth - derechaX - 30;

        // Títulos de pestañas
        String[] titulosPestanas = {"ESTADÍSTICAS", "MOVIMIENTOS", "NATURALEZA", "ENCONTRADO"};

        // Espacio para pestañas
        float tabAlto = 45;
        float tabsY = screenHeight - 80;

        for (int i = 0; i < 4; i++) {
            float tabX = derechaX + (i * (derechaAncho / 4));
            float tabAncho = derechaAncho / 4;

            if (i == player.getPokemonDetailTab()) {
                // Pestaña activa (estilo oscuro)
                spriteBatch.setColor(new Color(0.3f, 0.3f, 0.4f, 0.9f));
                spriteBatch.draw(whitePixel, tabX, tabsY - tabAlto, tabAncho, tabAlto);

                // Borde superior azul (más grueso)
                spriteBatch.setColor(new Color(0.4f, 0.6f, 1.0f, 1));
                spriteBatch.draw(whitePixel, tabX, tabsY, tabAncho, 4);

                // Bordes laterales sutiles
                spriteBatch.setColor(new Color(0.5f, 0.5f, 0.7f, 1));
                spriteBatch.draw(whitePixel, tabX, tabsY - tabAlto, 1, tabAlto);
                spriteBatch.draw(whitePixel, tabX + tabAncho - 1, tabsY - tabAlto, 1, tabAlto);
            } else {
                // Pestaña inactiva (más oscura)
                spriteBatch.setColor(new Color(0.15f, 0.15f, 0.2f, 0.9f));
                spriteBatch.draw(whitePixel, tabX, tabsY - tabAlto, tabAncho, tabAlto);

                // Borde superior gris
                spriteBatch.setColor(new Color(0.4f, 0.4f, 0.5f, 1));
                spriteBatch.draw(whitePixel, tabX, tabsY, tabAncho, 1);
            }

            // Texto de pestaña (centrado)
            if (i == player.getPokemonDetailTab()) {
                font.setColor(new Color(0.9f, 0.9f, 1.0f, 1)); // Texto claro activo
            } else {
                font.setColor(new Color(0.6f, 0.6f, 0.8f, 1)); // Texto gris claro inactivo
            }
            font.getData().setScale(0.9f);

            // Calcular ancho del texto para centrar
            String tabTexto = titulosPestanas[i];
            float textoAncho = tabTexto.length() * 7;
            float textoX = tabX + (tabAncho - textoAncho) / 2;

            font.draw(spriteBatch, tabTexto, textoX, tabsY - 25);

            spriteBatch.setColor(Color.WHITE);
        }

        // ===== CONTENIDO DE LA PESTAÑA =====
        float contenidoY = tabsY - tabAlto - 20;
        float contenidoAlto = contenidoY - 120;

        // Fondo del contenido (oscuro)
        float contenidoX = derechaX;
        float contenidoAncho = derechaAncho;

        spriteBatch.setColor(new Color(0.15f, 0.15f, 0.2f, 0.9f));
        spriteBatch.draw(whitePixel, contenidoX, 120, contenidoAncho, contenidoAlto);

        // Borde del contenido (gris)
        spriteBatch.setColor(new Color(0.5f, 0.5f, 0.6f, 1));
        spriteBatch.draw(whitePixel, contenidoX, 120, contenidoAncho, 2);
        spriteBatch.draw(whitePixel, contenidoX, 120 + contenidoAlto, contenidoAncho, 2);
        spriteBatch.draw(whitePixel, contenidoX, 120, 2, contenidoAlto);
        spriteBatch.draw(whitePixel, contenidoX + contenidoAncho, 120, 2, contenidoAlto);
        spriteBatch.setColor(Color.WHITE);

        // Mostrar contenido según pestaña
        float margenInternoX = 20;
        float margenInternoY = 20;

        switch (player.getPokemonDetailTab()) {
            case 0:
                dibujarEstadisticasPokemon(p, contenidoX + margenInternoX,
                    contenidoY - margenInternoY,
                    contenidoAncho - margenInternoX * 2);
                break;
            case 1:
                dibujarMovimientosPokemon(p, contenidoX + margenInternoX,
                    contenidoY - margenInternoY,
                    contenidoAncho - margenInternoX * 2);
                break;
            case 2:
                dibujarNaturalezaPokemon(p, contenidoX + margenInternoX,
                    contenidoY - margenInternoY,
                    contenidoAncho - margenInternoX * 2);
                break;
            case 3:
                dibujarEncontradoPokemon(p, contenidoX + margenInternoX,
                    contenidoY - margenInternoY,
                    contenidoAncho - margenInternoX * 2);
                break;
        }

        // Instrucciones ESPECÍFICAS
        font.getData().setScale(0.9f);
        font.setColor(new Color(0.7f, 0.7f, 0.9f, 1));
        String instrucciones = "←→: Cambiar pestaña  B: Volver al equipo  ESC: Volver al menu";
        float instruccionWidth = font.getData().scaleX * instrucciones.length() * 6;
        font.draw(spriteBatch, instrucciones, (screenWidth - instruccionWidth) / 2, 50);

        font.getData().setScale(1.0f);
        font.setColor(Color.WHITE);
    }

    // ===============================
// CONTENIDOS DE LAS PESTAÑAS
// ===============================
    private void dibujarEstadisticasPokemon(PokemonJugador p, float x, float y, float ancho) {
        // MÁRGEN SUPERIOR
        y -= 15; // 15px de margen desde el borde superior del contenedor

        font.setColor(new Color(0.2f, 0.2f, 0.4f, 1));

        // ===== BARRA DE PS CON MEJOR ESPACIADO =====
        font.getData().setScale(1.3f); // Un poco más grande
        font.draw(spriteBatch, "PS", x, y);

        float psAncho = ancho * 0.7f; // Más angosta para dejar espacio
        float porcentajePS = (float)p.getPsActual() / p.getPsMaximos();

        // Posicionar barra DEBAJO del texto, no al mismo nivel
        float barraY = y - 25; // 25px debajo del texto "PS"

        // Fondo barra PS
        spriteBatch.setColor(new Color(0.85f, 0.85f, 0.9f, 1));
        spriteBatch.draw(whitePixel, x + 80, barraY, psAncho, 20); // Más alta (20px)

        // Barra PS actual
        if (porcentajePS > 0.5) {
            spriteBatch.setColor(new Color(0.0f, 0.8f, 0.2f, 1));
        } else if (porcentajePS > 0.2) {
            spriteBatch.setColor(new Color(1.0f, 0.8f, 0.0f, 1));
        } else {
            spriteBatch.setColor(Color.RED);
        }
        spriteBatch.draw(whitePixel, x + 80, barraY, psAncho * porcentajePS, 20);

        // Texto PS (al lado de la barra, no encima)
        spriteBatch.setColor(Color.WHITE);
        font.setColor(Color.BLACK);
        font.getData().setScale(1.1f);
        // Posicionar texto alineado verticalmente con la barra
        font.draw(spriteBatch, p.getPsActual() + "/" + p.getPsMaximos(),
            x + psAncho + 90, barraY + 5);

        // ===== ESTADÍSTICAS (con más espacio) =====
        y = barraY - 40; // 40px debajo de la barra de PS

        String[] statNombres = {"Ataque", "Defensa", "Ataque Esp.", "Defensa Esp.", "Velocidad"};
        int[] stats = {
            p.getAtaque(),
            p.getDefensa(),
            p.getAtaqueEspecial(),
            p.getDefensaEspecial(),
            p.getVelocidad()
        };

        font.getData().setScale(1.0f);
        for (int i = 0; i < statNombres.length; i++) {
            font.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
            font.draw(spriteBatch, statNombres[i] + ":", x, y);

            // Barra de stat
            float maxStat = 200;
            float porcentajeStat = Math.min(1.0f, (float)stats[i] / maxStat);
            float statAncho = ancho * 0.5f; // Más angosta

            spriteBatch.setColor(new Color(0.85f, 0.85f, 0.9f, 1));
            spriteBatch.draw(whitePixel, x + 100, y - 8, statAncho, 12); // Más alta y con margen

            // Color según tipo de stat
            if (i == 0 || i == 2) {
                spriteBatch.setColor(new Color(0.9f, 0.3f, 0.3f, 1));
            } else if (i == 1 || i == 3) {
                spriteBatch.setColor(new Color(0.3f, 0.3f, 0.9f, 1));
            } else {
                spriteBatch.setColor(new Color(0.9f, 0.9f, 0.3f, 1));
            }

            spriteBatch.draw(whitePixel, x + 100, y - 8, statAncho * porcentajeStat, 12);

            // Valor (alineado a la derecha de la barra)
            font.setColor(Color.BLACK);
            font.draw(spriteBatch, String.valueOf(stats[i]), x + statAncho + 110, y);

            y -= 28; // Más espacio entre stats
        }

        // ===== HABILIDAD =====
        y -= 15;
        font.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
        font.draw(spriteBatch, "Habilidad:", x, y);
        font.setColor(Color.BLACK);
        font.draw(spriteBatch, p.getHabilidad().getNombre(), x + 80, y);

        // Descripción (con ajuste de línea mejorado)
        y -= 25;
        font.getData().setScale(0.85f);
        font.setColor(new Color(0.5f, 0.5f, 0.7f, 1));

        String descripcion = p.getHabilidad().getDescripcion();
        if (descripcion.length() > 50) {
            // Encontrar espacio para dividir
            int splitIndex = 50;
            for (int i = 50; i >= 0; i--) {
                if (descripcion.charAt(i) == ' ') {
                    splitIndex = i;
                    break;
                }
            }

            String primeraLinea = descripcion.substring(0, splitIndex);
            font.draw(spriteBatch, primeraLinea, x, y);

            y -= 20;
            String segundaLinea = descripcion.substring(splitIndex + 1);
            if (segundaLinea.length() > 50) {
                segundaLinea = segundaLinea.substring(0, 47) + "...";
            }
            font.draw(spriteBatch, segundaLinea, x, y);
        } else {
            font.draw(spriteBatch, descripcion, x, y);
        }

        font.getData().setScale(1.0f);
    }

    private void dibujarMovimientosPokemon(PokemonJugador p, float x, float y, float ancho) {
        List<Movimiento> movimientos = p.getMovimientos();

        font.setColor(new Color(0.2f, 0.2f, 0.4f, 1));
        font.getData().setScale(1.2f);
        font.draw(spriteBatch, "MOVIMIENTOS", x, y);
        font.getData().setScale(1.0f);

        y -= 40;

        for (int i = 0; i < movimientos.size(); i++) {
            Movimiento m = movimientos.get(i);

            // Fondo alternado para mejor legibilidad
            if (i % 2 == 0) {
                spriteBatch.setColor(new Color(0.97f, 0.97f, 1.0f, 1));
            } else {
                spriteBatch.setColor(new Color(0.93f, 0.93f, 0.98f, 1));
            }
            spriteBatch.draw(whitePixel, x, y - 15, ancho, 35);
            spriteBatch.setColor(Color.WHITE);

            // Nombre del movimiento
            font.setColor(Color.BLACK);
            font.draw(spriteBatch, m.getNombre(), x + 10, y);

            // Tipo
            Color colorTipo = getColorPorTipo(m.getTipo());
            font.setColor(colorTipo);
            font.draw(spriteBatch, m.getTipo().toString(), x + 120, y);

            // PP - MOVIDO MÁS A LA DERECHA (x + ancho - 80)
            font.setColor(Color.BLACK);
            String pp = m.getPpActual() + "/" + m.getPpMax();

            // Color del texto de PP según la cantidad
            float ppPorcentaje = (float)m.getPpActual() / m.getPpMax();
            if (ppPorcentaje > 0.5) {
                font.setColor(new Color(0.0f, 0.5f, 0.0f, 1)); // Verde oscuro
            } else if (ppPorcentaje > 0.2) {
                font.setColor(new Color(0.8f, 0.5f, 0.0f, 1)); // Naranja
            } else {
                font.setColor(Color.RED);
            }

            font.draw(spriteBatch, "PP: " + pp, x + ancho - 80, y); // 30px más a la derecha
            font.setColor(Color.BLACK); // Restaurar color

            // ELIMINADO: Código de la barra de PP
            // float ppPorcentaje = (float)m.getPpActual() / m.getPpMax();
            // float ppAncho = 60;
            // ... resto del código de la barra ...

            y -= 40;
        }
    }

    private void dibujarNaturalezaPokemon(PokemonJugador p, float x, float y, float ancho) {
        // Por ahora, naturaleza placeholder
        font.setColor(new Color(0.2f, 0.2f, 0.4f, 1));

        font.getData().setScale(1.5f);
        font.draw(spriteBatch, "NATURALEZA", x, y);
        font.getData().setScale(1.0f);

        y -= 50;

        // Placeholder - puedes implementar sistema de naturalezas después
        font.setColor(new Color(0.8f, 0.4f, 0.2f, 1)); // Color naranja
        font.getData().setScale(2.0f);
        font.draw(spriteBatch, "SERIO", x + ancho/2 - 30, y);
        font.getData().setScale(1.0f);

        y -= 50;

        font.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
        font.draw(spriteBatch, "Esta naturaleza no altera", x, y);
        y -= 25;
        font.draw(spriteBatch, "ninguna estadística.", x, y);

        y -= 40;

        // Placeholder para efectos de naturaleza
        String[] stats = {"Ataque", "Defensa", "Ataque Esp.", "Defensa Esp.", "Velocidad"};
        String[] efectos = {"→", "→", "→", "→", "→"}; // Neutral

        for (int i = 0; i < stats.length; i++) {
            font.setColor(new Color(0.4f, 0.4f, 0.6f, 1));
            font.draw(spriteBatch, stats[i], x + 50, y);

            if (efectos[i].equals("↑")) {
                font.setColor(Color.GREEN);
            } else if (efectos[i].equals("↓")) {
                font.setColor(Color.RED);
            } else {
                font.setColor(new Color(0.6f, 0.6f, 0.6f, 1));
            }

            font.draw(spriteBatch, efectos[i], x + 150, y);
            y -= 25;
        }
    }

    private void dibujarEncontradoPokemon(PokemonJugador p, float x, float y, float ancho) {
        font.setColor(new Color(0.2f, 0.2f, 0.4f, 1));

        font.getData().setScale(1.5f);
        font.draw(spriteBatch, "REGISTRO", x, y);
        font.getData().setScale(1.0f);

        y -= 50;

        // Información placeholder (puedes conectarlo con sistema de guardado después)
        font.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
        font.draw(spriteBatch, "Lugar encontrado:", x, y);
        font.setColor(Color.BLACK);
        font.draw(spriteBatch, "Ruta 201", x + 150, y);
        y -= 30;

        font.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
        font.draw(spriteBatch, "Nivel encontrado:", x, y);
        font.setColor(Color.BLACK);
        font.draw(spriteBatch, String.valueOf(p.getNivel()), x + 150, y);
        y -= 30;

        font.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
        font.draw(spriteBatch, "Fecha:", x, y);
        font.setColor(Color.BLACK);
        font.draw(spriteBatch, "15/03/2024", x + 150, y);
        y -= 30;

        font.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
        font.draw(spriteBatch, "Hora:", x, y);
        font.setColor(Color.BLACK);
        font.draw(spriteBatch, "14:30", x + 150, y);
        y -= 30;

        font.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
        font.draw(spriteBatch, "Método:", x, y);
        font.setColor(Color.BLACK);
        font.draw(spriteBatch, "Captura salvaje", x + 150, y);
        y -= 40;

        // ELIMINADO: Línea divisoria y sección "OTROS DATOS"
        // spriteBatch.setColor(new Color(0.8f, 0.8f, 0.85f, 1));
        // spriteBatch.draw(whitePixel, x, y, ancho, 1);
        // spriteBatch.setColor(Color.WHITE);
        // y -= 30;

        // ELIMINADO: "OTROS DATOS" y sus campos
        // font.setColor(new Color(0.2f, 0.2f, 0.4f, 1));
        // font.getData().setScale(1.2f);
        // font.draw(spriteBatch, "OTROS DATOS", x, y);
        // font.getData().setScale(1.0f);
        // y -= 40;
        //
        // font.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
        // font.draw(spriteBatch, "ID del Entrenador:", x, y);
        // font.setColor(Color.BLACK);
        // font.draw(spriteBatch, "00001", x + 150, y);
        // y -= 25;
        //
        // font.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
        // font.draw(spriteBatch, "Nombre OT:", x, y);
        // font.setColor(Color.BLACK);
        // font.draw(spriteBatch, player.getEntrenador().getNombre(), x + 150, y);

        // En su lugar, podemos agregar un mensaje o dejar espacio
        font.setColor(new Color(0.4f, 0.4f, 0.6f, 1));
        font.getData().setScale(0.9f);
        font.draw(spriteBatch, "Datos de captura", x + ancho/2 - 50, y - 20);
        font.getData().setScale(1.0f);
    }

    // Método auxiliar para colores de tipo
    private Color getColorPorTipo(Tipo tipo) {
        switch (tipo) {
            case FUEGO: return new Color(0.9f, 0.3f, 0.1f, 1);
            case AGUA: return new Color(0.1f, 0.5f, 0.9f, 1);
            case PLANTA: return new Color(0.2f, 0.7f, 0.2f, 1);
            case ELECTRICO: return new Color(0.9f, 0.9f, 0.1f, 1);
            case NORMAL: return new Color(0.7f, 0.7f, 0.6f, 1);
            default: return new Color(0.5f, 0.5f, 0.5f, 1);
        }
    }

    private void dibujarCrafteo(int screenWidth, int screenHeight) {
        font.getData().setScale(2.0f);
        String titulo = "SISTEMA DE CRAFTEO";
        float tituloWidth = font.getData().scaleX * titulo.length() * 10;
        font.draw(spriteBatch, titulo, (screenWidth - tituloWidth) / 2, screenHeight - 40);
        font.getData().setScale(1.0f);

        // Obtener recetas disponibles
        List<Crafteo.Receta> recetas = player.getSistemaCrafteo().getRecetasDisponibles();
        int seleccion = player.getSeleccionCrafteo();

        // Dimensiones del panel principal - CENTRADO
        float panelAncho = screenWidth * 0.75f;  // Más angosto para mejor centrado
        float panelAlto = screenHeight * 0.7f;   // Reducido en altura
        float panelX = (screenWidth - panelAncho) / 2;  // Centrado horizontal
        float panelY = screenHeight * 0.15f;  // Más centrado verticalmente (25% desde arriba)

        // Fondo negro para todo el panel - REDUCIDO BORDE SUPERIOR
        spriteBatch.setColor(0.0f, 0.0f, 0.0f, 0.85f);
        spriteBatch.draw(whitePixel, panelX, panelY, panelAncho, panelAlto);
        spriteBatch.setColor(Color.WHITE);

        // Bordes del panel - AJUSTADOS
        spriteBatch.setColor(0.5f, 0.5f, 0.5f, 1.0f);
        // Bordes horizontales
        spriteBatch.draw(whitePixel, panelX, panelY, panelAncho, 2);
        spriteBatch.draw(whitePixel, panelX, panelY + panelAlto, panelAncho, 2);
        // Bordes verticales
        spriteBatch.draw(whitePixel, panelX, panelY, 2, panelAlto);
        spriteBatch.draw(whitePixel, panelX + panelAncho - 2, panelY, 2, panelAlto);
        spriteBatch.setColor(Color.WHITE);

        // COLUMNAS dentro del panel
        float columnaDetallesX = panelX + 20;  // Izquierda: detalles de la receta
        float columnaRecetasX = panelX + panelAncho * 0.55f;  // Derecha: lista de recetas

        // CONTADOR DE RECETAS - ESQUINA SUPERIOR DERECHA DEL CUADRO NEGRO
        font.setColor(Color.YELLOW);
        font.getData().setScale(0.9f);
        String contador = String.format("Recetas: %d/%d", seleccion + 1, recetas.size());
        float contadorX = panelX + panelAncho - 20;  // 20px desde el borde derecho
        float contadorY = panelY + panelAlto - 25;   // 25px desde el borde superior del cuadro
        font.draw(spriteBatch, contador, contadorX - (contador.length() * 6), contadorY);
        font.getData().setScale(1.0f);

        // Título "RECETAS DISPONIBLES" - dentro del cuadro negro, columna derecha
        float recetaY = panelY + panelAlto - 50;  // Bajado para dejar espacio al contador
        font.setColor(Color.CYAN);
        font.draw(spriteBatch, "RECETAS DISPONIBLES:", columnaRecetasX, recetaY);
        recetaY -= 25;

        // Dibujar lista de recetas (máximo 6 para evitar superposición)
        int maxRecetasPorPantalla = 6;
        int inicioRecetas = 0;

        if (recetas.size() > maxRecetasPorPantalla && seleccion >= maxRecetasPorPantalla) {
            inicioRecetas = seleccion - maxRecetasPorPantalla + 1;
        }

        float espacioRecetas = 30;

        for (int i = inicioRecetas; i < Math.min(inicioRecetas + maxRecetasPorPantalla, recetas.size()); i++) {
            Crafteo.Receta receta = recetas.get(i);
            float y = recetaY - (i - inicioRecetas) * espacioRecetas;

            if (y < panelY + 20) break; // No dibujar muy abajo

            boolean puedeCraftear = player.getSistemaCrafteo().puedeCraftear(receta.id);
            boolean esSeleccionada = (i == seleccion);

            if (esSeleccionada) {
                // Fondo para la receta seleccionada
                spriteBatch.setColor(0.4f, 0.4f, 0.1f, 0.8f);
                spriteBatch.draw(whitePixel, columnaRecetasX - 10, y - 20, panelAncho * 0.4f, 25);
                spriteBatch.setColor(Color.WHITE);

                // Borde para la receta seleccionada
                spriteBatch.setColor(0.8f, 0.8f, 0.0f, 1.0f);
                spriteBatch.draw(whitePixel, columnaRecetasX - 11, y - 21, panelAncho * 0.4f + 1, 1);
                spriteBatch.draw(whitePixel, columnaRecetasX - 11, y + 4, panelAncho * 0.4f + 1, 1);
                spriteBatch.draw(whitePixel, columnaRecetasX - 11, y - 21, 1, 26);
                spriteBatch.draw(whitePixel, columnaRecetasX + panelAncho * 0.4f - 10, y - 21, 1, 26);
                spriteBatch.setColor(Color.WHITE);

                font.setColor(puedeCraftear ? Color.YELLOW : Color.ORANGE);
                font.draw(spriteBatch, "▶ " + receta.nombre, columnaRecetasX, y);
            } else {
                font.setColor(puedeCraftear ? Color.WHITE : Color.GRAY);
                font.draw(spriteBatch, "• " + receta.nombre, columnaRecetasX, y);
            }
        }

        // Dibujar detalles de la receta seleccionada (columna izquierda)
        if (seleccion >= 0 && seleccion < recetas.size()) {
            Crafteo.Receta recetaSeleccionada = recetas.get(seleccion);
            boolean puedeCraftear = player.getSistemaCrafteo().puedeCraftear(recetaSeleccionada.id);

            float detallesY = panelY + panelAlto - 50;

            // Título de la receta
            font.setColor(Color.YELLOW);
            font.getData().setScale(1.2f);
            font.draw(spriteBatch, recetaSeleccionada.nombre, columnaDetallesX, detallesY);
            font.getData().setScale(1.0f);
            detallesY -= 30;

            // Descripción
            font.setColor(Color.LIGHT_GRAY);
            font.draw(spriteBatch, "Descripción:", columnaDetallesX, detallesY);
            detallesY -= 20;

            // Descripción en múltiples líneas
            String[] palabras = recetaSeleccionada.descripcion.split(" ");
            StringBuilder linea = new StringBuilder();
            float lineaY = detallesY;

            for (String palabra : palabras) {
                if (linea.length() + palabra.length() > 35) {
                    font.draw(spriteBatch, linea.toString(), columnaDetallesX, lineaY);
                    linea = new StringBuilder(palabra + " ");
                    lineaY -= 20;
                } else {
                    linea.append(palabra).append(" ");
                }
            }
            if (linea.length() > 0) {
                font.draw(spriteBatch, linea.toString(), columnaDetallesX, lineaY);
            }
            detallesY = lineaY - 30;

            // Ingredientes necesarios
            font.setColor(Color.CYAN);
            font.draw(spriteBatch, "INGREDIENTES REQUERIDOS:", columnaDetallesX, detallesY);
            detallesY -= 25;

            // Lista de ingredientes
            for (Crafteo.Ingrediente ingrediente : recetaSeleccionada.ingredientes) {
                Ranura slot = player.getInventario().buscarItem(ingrediente.nombre);
                int cantidadDisponible = (slot != null) ? slot.getCantidad() : 0;
                boolean tieneSuficiente = cantidadDisponible >= ingrediente.cantidad;

                font.setColor(tieneSuficiente ? Color.GREEN : Color.RED);
                String textoIngrediente = String.format("  %s: %d/%d",
                    ingrediente.nombre, cantidadDisponible, ingrediente.cantidad);

                font.draw(spriteBatch, textoIngrediente, columnaDetallesX, detallesY);
                detallesY -= 22;
            }

            // Estado de crafteo
            detallesY -= 20;
            font.getData().setScale(1.1f);
            if (puedeCraftear) {
                font.setColor(Color.GREEN);
                font.draw(spriteBatch, "✓ LISTO PARA CRAFTEAR", columnaDetallesX, detallesY);
            } else {
                font.setColor(Color.RED);
                font.draw(spriteBatch, "✗ MATERIALES INSUFICIENTES", columnaDetallesX, detallesY);
            }
            font.getData().setScale(1.0f);

            // Resultado del crafteo
            detallesY -= 35;
            font.setColor(Color.YELLOW);
            font.draw(spriteBatch, "RESULTADO:", columnaDetallesX, detallesY);
            detallesY -= 20;
            font.setColor(Color.WHITE);

            switch(recetaSeleccionada.id) {
                case 1:
                    font.draw(spriteBatch, "• 1x Poké Ball", columnaDetallesX, detallesY);
                    font.draw(spriteBatch, "  (Tasa captura: 1.0x)", columnaDetallesX + 10, detallesY - 18);
                    break;
                case 2:
                    font.draw(spriteBatch, "• 1x Super Poké Ball", columnaDetallesX, detallesY);
                    font.draw(spriteBatch, "  (Tasa captura: 2.0x)", columnaDetallesX + 10, detallesY - 18);
                    break;
                case 3:
                    font.draw(spriteBatch, "• 1x Poción", columnaDetallesX, detallesY);
                    font.draw(spriteBatch, "  (Cura 20 PS)", columnaDetallesX + 10, detallesY - 18);
                    break;
                case 4:
                    font.draw(spriteBatch, "• 1x Poción Grande", columnaDetallesX, detallesY);
                    font.draw(spriteBatch, "  (Cura 50 PS)", columnaDetallesX + 10, detallesY - 18);
                    break;
            }
        }
    }

    // Pokédex (placeholder)
    private void dibujarPokedex(int screenWidth, int screenHeight) {
        font.getData().setScale(2.0f);
        font.draw(spriteBatch, "POKÉDEX", screenWidth / 2 - 60, screenHeight - 100);
        font.getData().setScale(1.0f);

        font.draw(spriteBatch, "Registro de Pokémon investigados", screenWidth / 2 - 140, screenHeight / 2);
        font.draw(spriteBatch, "(Implementación pendiente)", screenWidth / 2 - 100, screenHeight / 2 - 30);
    }

    // Guardar (placeholder)
    private void dibujarGuardar(int screenWidth, int screenHeight) {
        font.getData().setScale(2.0f);
        font.draw(spriteBatch, "GUARDAR PARTIDA", screenWidth / 2 - 120, screenHeight - 100);
        font.getData().setScale(1.0f);

        font.draw(spriteBatch, "Guardar progreso actual", screenWidth / 2 - 100, screenHeight / 2);
        font.draw(spriteBatch, "(Implementación pendiente)", screenWidth / 2 - 100, screenHeight / 2 - 30);
    }

    // Opciones (placeholder)
    // Menú de Opciones - ARREGLADO
    private void dibujarOpciones(int screenWidth, int screenHeight) {
        // Título
        font.getData().setScale(2.0f);
        font.draw(spriteBatch, "OPCIONES", screenWidth / 2 - 70, screenHeight - 100);
        font.getData().setScale(1.0f);

        // Las 4 opciones funcionan correctamente
        String[] opciones = {
            "Ajustar Volumen",
            "Pantalla Completa",
            "Ver Controles",
            "Ver Créditos"
        };

        float startX = screenWidth / 2 - 150;
        float startY = screenHeight / 2 + 80;
        float espacio = 50;

        for (int i = 0; i < opciones.length; i++) {
            // Resaltar la opción seleccionada
            if (i == player.getMenuSelection()) {
                // Fondo para la opción seleccionada
                spriteBatch.setColor(0.2f, 0.2f, 0.5f, 0.8f);
                spriteBatch.draw(whitePixel, startX - 20, startY - i * espacio - 20, 340, 35);
                spriteBatch.setColor(Color.WHITE);

                // Texto de la opción seleccionada
                font.setColor(Color.YELLOW);
                font.draw(spriteBatch, "➤ " + opciones[i], startX, startY - i * espacio);
                font.setColor(Color.WHITE);
            } else {
                // Texto normal
                font.draw(spriteBatch, "  " + opciones[i], startX, startY - i * espacio);
            }
        }

        // Información adicional sobre la opción seleccionada
        float infoY = startY - (opciones.length * espacio) - 40;
        String info = "";

        switch (player.getMenuSelection()) {
            case 0:
                info = "Ajusta el volumen de efectos y música";
                break;
            case 1:
                info = "Alterna entre ventana y pantalla completa";
                break;
            case 2:
                info = "Muestra los controles del juego";
                break;
            case 3:
                info = "Información sobre los desarrolladores";
                break;
        }

        if (!info.isEmpty()) {
            font.setColor(Color.LIGHT_GRAY);
            font.draw(spriteBatch, info, screenWidth / 2 - 200, infoY);
            font.setColor(Color.WHITE);
        }
    }

    // Instrucciones del menú
    private void dibujarInstrucciones(int screenWidth, int screenHeight) {
        font.getData().setScale(0.9f);
        String instrucciones = "";

        switch (player.getMenuState()) {
            case MAIN:
                instrucciones = "Flechas: Navegar | Enter: Seleccionar | ESC/I: Salir";
                break;
            case POKEMON_TEAM:
                instrucciones = "Flechas ↑↓: Navegar Pokémon | ←→: Seleccionar acción | 1-4: Acciones rápidas | Enter: Ejecutar | ESC: Volver";
                break;
            case INVENTORY:
                instrucciones = "Flechas: Navegar | Enter: Seleccionar item | 1/2/3: Acciones | ESC: Volver";
                break;
            case CRAFTING:
                instrucciones = "Flechas ↑↓: Navegar recetas | Enter: Craftear | ESC: Volver al menú principal";
                break;
            case OPTIONS:
                instrucciones = "Flechas: Navegar opciones | Enter: Seleccionar | ESC: Volver al menú principal";
                break;
            default:
                instrucciones = "Flechas: Navegar | Enter: Seleccionar | ESC: Volver";
                break;
        }

        float instruccionWidth = font.getData().scaleX * instrucciones.length() * 6;
        font.draw(spriteBatch, instrucciones, (screenWidth - instruccionWidth) / 2, 50);
        font.getData().setScale(1.0f);
    }

    // HUD durante el juego
    private void dibujarHUD() {
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.begin();

        float hudX = 10;
        float hudY = Gdx.graphics.getHeight() - 30;

        font.draw(spriteBatch, "I - Menú", hudX, hudY);

        spriteBatch.end();
        spriteBatch.setProjectionMatrix(camara.combined);
    }

    // AGREGAR este método para manejar combate:
    private void manejarCombate(float delta) {
        // Aquí irá la lógica de la interfaz de combate
        // Mostrará los Pokémon, PS, movimientos, etc.

        // Por ahora, solo dibujamos
        if (combateActivo != null && combateActivo.isCombateTerminado()) {
            // Combate terminado
            terminarCombate();
        }
    }


    // AGREGAR método para manejar entrada en combate:
    // EN GameScreen.java, en el método manejarEntradaCombate():
    private void manejarEntradaCombate() {
        if (combateActivo.isTurnoJugador()) {
            // Teclas 1-4 para usar movimientos
            if (Gdx.input.isKeyJustPressed(Keys.NUM_1)) {
                combateActivo.ejecutarTurnoJugador(0);
            }
            if (Gdx.input.isKeyJustPressed(Keys.NUM_2)) {
                combateActivo.ejecutarTurnoJugador(1);
            }
            if (Gdx.input.isKeyJustPressed(Keys.NUM_3)) {
                combateActivo.ejecutarTurnoJugador(2);
            }
            if (Gdx.input.isKeyJustPressed(Keys.NUM_4)) {
                combateActivo.ejecutarTurnoJugador(3);
            }

            // Tecla B para usar Poké Ball (CORREGIDO)
            if (Gdx.input.isKeyJustPressed(Keys.B)) {
                if (pokemonSalvajeActual instanceof PokemonSalvaje) {
                    // Verificar si hay Poké Balls en inventario
                    Ranura pokeballsSlot = player.getInventario().buscarItem("Poké Ball");
                    if (pokeballsSlot != null && pokeballsSlot.getCantidad() > 0) {
                        // Obtener multiplicador de la Poké Ball
                        Item itemPokeball = pokeballsSlot.getItem();
                        float multiplicador = 1.0f; // Default

                        // Determinar multiplicador según tipo de Poké Ball
                        if (itemPokeball instanceof Pokeball) {
                            Pokeball pokeball = (Pokeball) itemPokeball;
                            multiplicador = pokeball.getTasaCaptura();
                        }

                        // Usar Poké Ball
                        pokeballsSlot.usarCantidad(1);
                        boolean exitoCaptura = combateActivo.intentarCaptura(multiplicador);

                        if (exitoCaptura) {
                            // Convertir a Pokémon jugador y agregar al equipo
                            PokemonJugador pokemonCapturado = ((PokemonSalvaje)pokemonSalvajeActual).convertirAJugador();
                            boolean agregado = player.getEntrenador().agregarPokemon(pokemonCapturado);

                            if (agregado) {
                                System.out.println("¡Has capturado a " + pokemonCapturado.getNombre() + "!");
                            } else {
                                System.out.println("¡Equipo lleno! No puedes llevar más Pokémon.");
                            }
                        }
                    } else {
                        System.out.println("¡No tienes Poké Balls!");
                    }
                }
            }

            // Tecla C para cambiar Pokémon
            if (Gdx.input.isKeyJustPressed(Keys.C)) {
                player.setMenuState(MenuState.CAMBIO_POKEMON);
            }
        }
    }

    // AGREGAR método para terminar combate:
    private void terminarCombate() {
        if (combateActivo == null) return;

        Pokemon ganador = combateActivo.getGanador();
        Pokemon perdedor = combateActivo.getPerdedor();

        if (ganador == player.getEntrenador().getPokemonActual()) {
            System.out.println("¡Has ganado el combate!");
            // Dar experiencia al Pokémon
            int expGanada = pokemonSalvajeActual.getNivel() * 10;
            if (player.getEntrenador().getPokemonActual() instanceof PokemonJugador) {
                ((PokemonJugador)player.getEntrenador().getPokemonActual()).ganarExperiencia(expGanada);
            }
        } else {
            System.out.println("¡Has perdido el combate!");
        }

        // Limpiar
        combateActivo = null;
        pokemonSalvajeActual = null;
        enCombate = false;
        player.setMenuState(MenuState.NONE);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        calculateCameraBounds(viewport.getWorldWidth(), viewport.getWorldHeight());
    }

    @Override
    public void dispose() {
        if (mapa != null) mapa.dispose();
        if (renderer != null) renderer.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (player != null) {
            // Liberar sprites de los Pokémon del jugador
            Entrenador entrenador = player.getEntrenador();
            if (entrenador != null) {
                for (PokemonJugador pokemon : entrenador.getEquipo()) {
                    if (pokemon != null) {
                        pokemon.dispose();
                    }
                }
            }
            player.dispose();
        }
        if (font != null) font.dispose();
        if (whitePixel != null) whitePixel.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
