package com.pokemon.game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.pokemon.game.data.DataLoader;
import com.pokemon.game.data.SaveData;
import com.pokemon.game.data.SaveManager;
import com.pokemon.game.item.Crafteo;
import com.pokemon.game.item.Curacion;
import com.pokemon.game.item.Item;
import com.pokemon.game.item.Pokeball;
import com.pokemon.game.player.Inventario;
import com.pokemon.game.player.Player;
import com.pokemon.game.player.Ranura;
import com.pokemon.game.pokedex.PokedexScreen;
import com.pokemon.game.pokemon.*;
import com.pokemon.game.pokedex.PokedexEntry;

import java.util.ArrayList;
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
    private final BitmapFont font;
    private final Texture whitePixel;

    private PokedexScreen pokedexScreen;

    private EncountersManager encountersManager;

    private int selectedSaveOption = 0; // Para navegar en menú SAVE


    public GameScreen(final PokemonGame game, String initialMap, float startX, float startY) {
        this.game = game;
        this.initialMap = initialMap;
        this.startX = startX;
        this.startY = startY;

        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(0.8f);

        this.encountersManager = new EncountersManager();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.whitePixel = new Texture(pixmap);
        pixmap.dispose();

    }

    @Override
    public void show() {
        loadMap(initialMap, startX, startY);
        game.musics.stopmenumusic();
        game.musics.startopenworldmusic();
        cargarSpritesPokemon();

        pokedexScreen = new PokedexScreen(player);
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
        float left = centerX - collisionWidth / 2;
        float right = centerX + collisionWidth / 2;
        float bottom = centerY - collisionHeight / 2;
        float top = centerY + collisionHeight / 2;

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
        String[] nombresPokemon = {"pikachu", "charmander", "charizard"};

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

        // 2. Manejar entrada (SIEMPRE)
        handleInput();

        // 3. Lógica del juego (solo si NO hay menú abierto)
        if (player.getMenuState() == MenuState.NONE) {
            player.update(delta);
            checkMapTransition();
            updateCamera();
        }

        // 4. Limpiar pantalla
        ScreenUtils.clear(0, 0, 0, 1);

        // 5. Dibujar el mundo (SIEMPRE)
        renderer.setView(camara);
        renderer.render();

        // 6. Dibujar al jugador (SIEMPRE)
        spriteBatch.setProjectionMatrix(camara.combined);
        spriteBatch.begin();
        spriteBatch.draw(player.currentFrame,
            player.x - player.width / 2,
            player.y - player.height / 2,
            player.width,
            player.height);
        spriteBatch.end();

        // 7. Dibujar HUD (solo si no hay menú)
        if (player.getMenuState() == MenuState.NONE) {
            dibujarHUD();
        }

        // 8. Dibujar menú (si hay alguno activo)
        if (player.getMenuState() != MenuState.NONE) {
            dibujarMenu();
        }

        if (player.getMenuState() == MenuState.NONE) {
            encountersManager.update(delta);

            // Solo verificar encuentros si el jugador se está moviendo
            if (player.isMoving()) {
                String zonaActual = obtenerNombreMapaParaEncuentros(); // <-- CAMBIADO
                int nivelPromedio = calcularNivelPromedioEquipo();

                PokemonSalvaje encontrado = encountersManager.checkEncounter(
                    zonaActual, nivelPromedio, player.isMoving());

                if (encontrado != null) {
                    iniciarCombate(encontrado);
                }
            }
        }
    }

    public BitmapFont getFont() {
        return font;
    }

    public Texture getWhitePixel() {
        return whitePixel;
    }

    // Manejar entrada del teclado
    private void handleInput() {
        // NOTA IMPORTANTE: Ya NO hay manejo de combate aquí
        // El combate se maneja completamente en CombateScreen

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
            if (player.getMenuState() == MenuState.NONE) {
                game.musics.stoppausemusic();
                game.musics.startopenworldmusic();
            }
        }

        // Manejo para POKEMON_SELECT_FOR_ITEM (¡NUEVO!)
        if (player.getMenuState() == MenuState.POKEMON_SELECT_FOR_ITEM) {
            // Navegación en equipo (igual que en POKEMON_TEAM)
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

            // Enter para usar item en Pokémon seleccionado
            if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                PokemonJugador pokemon = player.getPokemonSeleccionado();
                if (pokemon != null) {
                    boolean usado = player.usarItemEnPokemon(pokemon);
                    if (usado) {
                        // Si se usó el item, volver al inventario
                        player.setMenuState(MenuState.INVENTORY);
                    }
                    // Si no se usó (pokémon con salud llena), se queda en esta pantalla
                }
            }

            // B para cancelar (además de ESC)
            if (Gdx.input.isKeyJustPressed(Keys.B)) {
                player.cancelarUsoItem();
                player.setMenuState(MenuState.INVENTORY);
            }

            // ¡IMPORTANTE! Salir del switch para que no entre en otros manejos
            return;
        }

        // Manejo de entrada según el estado del menú
        if (player.getMenuState() != MenuState.NONE) {
            // PRIMERO: Manejo específico para POKEDEX
            if (player.getMenuState() == MenuState.POKEDEX) {
                // Si no hay especie seleccionada = estamos en lista
                if (player.getPokedexSelectedSpecies() == null) {
                    if (Gdx.input.isKeyJustPressed(Keys.UP)) {
                        player.movePokedexUp();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
                        player.movePokedexDown();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
                        player.prevPokedexPage();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
                        player.nextPokedexPage();
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                        // Obtener la especie seleccionada
                        List<PokedexEntry> entradas = player.getEntrenador().getPokedex().getEntradasOrdenadas();
                        int inicio = player.getPokedexPage() * player.POKEDEX_ENTRIES_PER_PAGE;
                        int indice = inicio + player.getPokedexSelection();

                        // VERIFICAR que el índice sea válido
                        if (indice < entradas.size()) {
                            player.setPokedexSelectedSpecies(entradas.get(indice).getEspecie());
                        }
                    }
                    if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.B)) {
                        player.setMenuState(MenuState.MAIN);
                        player.setPokedexSelectedSpecies(null); // Resetear
                    }
                }
                // Si hay especie seleccionada = estamos en vista detalle
                else {
                    if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.B)) {
                        player.setPokedexSelectedSpecies(null); // Volver a lista
                    }
                }
            }

            // Manejo para CRAFTING (existente)
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

            // Manejo para POKEMON_TEAM (existente)
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
                        player.setPokemonDetailTab(0);
                    }
                }

                // B (o ESC) para volver al menú principal
                if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.B)) {
                    player.setMenuState(MenuState.MAIN);
                }
            }

            // Manejo para POKEMON_DETAIL (existente)
            if (player.getMenuState() == MenuState.POKEMON_DETAIL) {
                if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
                    player.prevPokemonDetailTab();
                }
                if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
                    player.nextPokemonDetailTab();
                }
                if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.B)) {
                    player.setMenuState(MenuState.POKEMON_TEAM);
                }
            }

            if (player.getMenuState() == MenuState.SAVE){
                if (Gdx.input.isKeyJustPressed(Keys.UP)) {
                    selectedSaveOption = (selectedSaveOption - 1 + 3) % 3;
                }
                if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
                    selectedSaveOption = (selectedSaveOption + 1) % 3;
                }

                // Selección
                if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                    switch(selectedSaveOption) {
                        case 0: // Guardar partida
                            guardarPartidaActual();
                            player.setMenuState(MenuState.MAIN);
                            break;

                        case 1: // Guardar y salir
                            guardarPartidaActual();
                            // Cambiar a MenuScreen
                            game.musics.stopopenworldmusic();
                            game.musics.startmenumusic();
                            game.setScreen(new MenuScreen(game));
                            break;

                        case 2: // Cancelar
                            player.setMenuState(MenuState.MAIN);
                            break;
                    }
                }

                // ESC para cancelar
                if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
                    player.setMenuState(MenuState.MAIN);
                }
            }

            // SEGUNDO: Manejo para otros menús (excepto POKEDEX)
            else if (player.getMenuState() != MenuState.POKEDEX) {
                // Flechas para navegar
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
            if (player.getMenuState() == MenuState.INVENTORY) {
                // Navegación por columnas
                if (Gdx.input.isKeyJustPressed(Keys.LEFT) || Gdx.input.isKeyJustPressed(Keys.A)) {
                    player.moveInventoryLeft();
                }
                if (Gdx.input.isKeyJustPressed(Keys.RIGHT) || Gdx.input.isKeyJustPressed(Keys.D)) {
                    player.moveInventoryRight();
                }
                if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W)) {
                    player.moveInventoryUp();
                }
                if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S)) {
                    player.moveInventoryDown();
                    // Validar límite basado en los items de la columna actual
                    validarLimiteInventario();
                }

                // Enter para seleccionar
                if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                    PokemonJugador pokemon = player.getPokemonSeleccionado();
                    if (pokemon != null) {
                        boolean usado = player.usarItemEnPokemon(pokemon);
                        if (usado) {
                            // ¡Vuelve al inventario!
                            player.setMenuState(MenuState.INVENTORY);
                        }
                    }
                }

                // ESC para volver
                if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
                    player.cancelarUsoItem();
                    player.setMenuState(MenuState.INVENTORY);
                }
            }
        }
    }

    private void guardarPartidaActual() {
        try {
            // Extraer datos del jugador
            SaveData datos = player.extraerDatosParaGuardar();

            // Guardar usando SaveManager
            boolean exito = SaveManager.getInstance().guardarPartida(datos);

            if (exito) {
                System.out.println("✅ ¡Partida guardada exitosamente!");
                // Podrías añadir un mensaje visual aquí
            } else {
                System.out.println("❌ Error al guardar la partida");
            }
        } catch (Exception e) {
            System.err.println("❌ Error en guardarPartidaActual: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cargarDatosJugador(SaveData datos) {
        // Si player es nulo, intentamos esperar un momento o forzar su creación
        if (this.player == null) {
            System.out.println("Wait... player era nulo, intentando inicializar...");
            // Aquí deberías llamar al método que instancia a tu jugador si no se ha hecho
        }

        if (this.player != null && datos != null) {
            this.player.cargarDatosGuardados(datos);
            System.out.println("✅ ¡Datos transferidos al jugador con éxito!");
        } else {
            System.out.println("❌ ERROR CRÍTICO: El objeto player sigue siendo nulo en GameScreen");
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
            case POKEMON_SELECT_FOR_ITEM:
                dibujarSeleccionPokemonParaItem(screenWidth, screenHeight);
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
            player.getMenuState() != MenuState.POKEMON_DETAIL &&
            player.getMenuState() != MenuState.POKEDEX) {
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
    private void dibujarInventario(int screenWidth, int screenHeight) {
        Inventario inv = player.getInventario();

        // Obtener items por categoría
        List<Ranura> recursos = new ArrayList<>();
        List<Ranura> pociones = new ArrayList<>();
        List<Ranura> pokeballs = new ArrayList<>();

        for (Ranura slot : inv.getRanuras()) {
            Item item = slot.getItem();
            if (item instanceof Curacion) {
                pociones.add(slot);
            } else if (item instanceof Pokeball) {
                pokeballs.add(slot);
            } else {
                recursos.add(slot);
            }
        }

        // 1. FONDO Y TÍTULO
        spriteBatch.setColor(new Color(0.1f, 0.1f, 0.15f, 0.95f));
        spriteBatch.draw(whitePixel, 0, 0, screenWidth, screenHeight);
        spriteBatch.setColor(Color.WHITE);

        font.getData().setScale(1.8f);
        font.setColor(new Color(0.9f, 0.9f, 1.0f, 1));
        font.draw(spriteBatch, "INVENTARIO", 350, screenHeight - 50);
        font.getData().setScale(1.0f);

        // 2. CAPACIDAD
        String capacidad = "Capacidad: " + inv.getCantidadTotal() + "/" + inv.getCapacidadMaxima();
        font.setColor(new Color(0.7f, 0.7f, 0.9f, 1));
        font.draw(spriteBatch, capacidad, 50, screenHeight - 100);

        // 3. DISEÑO DE 3 COLUMNAS CON SELECTOR
        float colRecursosX = 50;
        float colPocionesX = 300;
        float colPokeballsX = 550;
        float startY = screenHeight - 150;
        float espacio = 30;

        int columnaActual = player.getInventoryColumna();
        int indiceActual = player.getInventoryIndice();

        // Dibujar cada columna con el selector apropiado
        dibujarColumnaConSelector(spriteBatch, "RECURSOS", recursos, colRecursosX, startY, espacio,
            columnaActual == 0 ? indiceActual : -1);

        dibujarColumnaConSelector(spriteBatch, "POCIONES", pociones, colPocionesX, startY, espacio,
            columnaActual == 1 ? indiceActual : -1);

        dibujarColumnaConSelector(spriteBatch, "POKÉ BALLS", pokeballs, colPokeballsX, startY, espacio,
            columnaActual == 2 ? indiceActual : -1);

        // 4. OBTENER ITEM SELECCIONADO PARA MOSTRAR INFO
        Ranura itemSeleccionado = null;
        switch (columnaActual) {
            case 0:
                if (indiceActual < recursos.size()) {
                    itemSeleccionado = recursos.get(indiceActual);
                }
                break;
            case 1:
                if (indiceActual < pociones.size()) {
                    itemSeleccionado = pociones.get(indiceActual);
                }
                break;
            case 2:
                if (indiceActual < pokeballs.size()) {
                    itemSeleccionado = pokeballs.get(indiceActual);
                }
                break;
        }

        // 5. MOSTRAR INFORMACIÓN DEL ITEM SELECCIONADO
        if (itemSeleccionado != null) {
            dibujarInfoItem(itemSeleccionado, screenWidth, screenHeight);

            // Dibujar flecha indicadora de columna
            float flechaX = 0;
            switch (columnaActual) {
                case 0: flechaX = colRecursosX - 40; break;
                case 1: flechaX = colPocionesX - 40; break;
                case 2: flechaX = colPokeballsX - 40; break;
            }

            font.setColor(Color.YELLOW);
            font.draw(spriteBatch, "▶", flechaX, startY - (indiceActual * espacio));
        }

        // 6. INSTRUCCIONES
        font.setColor(new Color(0.6f, 0.6f, 0.8f, 1));
        String instrucciones = "←→: Cambiar columna  ↑↓: Navegar  Enter: Seleccionar  ESC: Volver";
        font.draw(spriteBatch, instrucciones, 200, 50);
    }

    // Método auxiliar para dibujar columna
    private void dibujarColumnaInventario(SpriteBatch batch, String titulo,
                                          List<Ranura> items, float x, float y,
                                          float espacio, int seleccionGlobal, int offset) {
        font.setColor(new Color(0.8f, 0.8f, 1.0f, 1));
        font.draw(batch, titulo, x, y);
        y -= 30;

        for (int i = 0; i < items.size(); i++) {
            Ranura slot = items.get(i);
            boolean seleccionado = (seleccionGlobal == offset + i);

            // Fondo para item seleccionado
            if (seleccionado) {
                batch.setColor(new Color(0.3f, 0.3f, 0.5f, 0.8f));
                batch.draw(whitePixel, x - 10, y - 20, 200, 25);
                batch.setColor(Color.WHITE);

                font.setColor(Color.YELLOW);
                font.draw(batch, "▶ " + slot.getItem().getNombre() + " x" + slot.getCantidad(), x, y);
                font.setColor(new Color(0.8f, 0.8f, 1.0f, 1));
            } else {
                font.draw(batch, "• " + slot.getItem().getNombre() + " x" + slot.getCantidad(), x, y);
            }
            y -= espacio;
        }
    }

    // Método para dibujar información del item seleccionado
    private void dibujarInfoItem(Ranura slot, int screenWidth, int screenHeight) {
        float panelX = 50;
        float panelY = 150;
        float panelAncho = screenWidth - 100;
        float panelAlto = 80;

        // Fondo del panel
        spriteBatch.setColor(new Color(0.15f, 0.15f, 0.2f, 0.9f));
        spriteBatch.draw(whitePixel, panelX, panelY, panelAncho, panelAlto);
        spriteBatch.setColor(Color.WHITE);

        // Borde
        spriteBatch.setColor(new Color(0.4f, 0.4f, 0.6f, 1));
        spriteBatch.draw(whitePixel, panelX, panelY, panelAncho, 2);
        spriteBatch.draw(whitePixel, panelX, panelY + panelAlto, panelAncho, 2);
        spriteBatch.setColor(Color.WHITE);

        // Información
        font.setColor(new Color(0.9f, 0.9f, 1.0f, 1));
        font.draw(spriteBatch, slot.getItem().getNombre(), panelX + 10, panelY + panelAlto - 20);

        font.setColor(new Color(0.7f, 0.7f, 0.9f, 1));
        font.draw(spriteBatch, "Cantidad: " + slot.getCantidad(), panelX + 10, panelY + panelAlto - 45);

        // Descripción con ajuste de línea
        String desc = slot.getItem().getDescripcion();
        if (desc.length() > 60) {
            // Dividir en líneas
            int splitIndex = 60;
            for (int i = 60; i >= 0; i--) {
                if (desc.charAt(i) == ' ') {
                    splitIndex = i;
                    break;
                }
            }

            String linea1 = desc.substring(0, splitIndex);
            String linea2 = desc.substring(splitIndex + 1);

            font.draw(spriteBatch, linea1, panelX + 10, panelY + panelAlto - 70);
            font.draw(spriteBatch, linea2, panelX + 10, panelY + panelAlto - 95);
        } else {
            font.draw(spriteBatch, desc, panelX + 10, panelY + panelAlto - 70);
        }

        // Uso específico según tipo
        font.setColor(new Color(0.5f, 0.8f, 1.0f, 1));
        if (slot.getItem() instanceof Curacion) {
            Curacion pocion = (Curacion) slot.getItem();
            font.draw(spriteBatch, "Cura: " + pocion.getHpRestaurado() + " PS",
                panelX + 200, panelY + panelAlto - 45);
            font.draw(spriteBatch, "Enter: Usar en Pokémon",
                panelX + 200, panelY + panelAlto - 70);
        } else if (slot.getItem() instanceof Pokeball) {
            Pokeball ball = (Pokeball) slot.getItem();
            font.draw(spriteBatch, "Captura: " + ball.getTasaCaptura() + "x",
                panelX + 200, panelY + panelAlto - 45);
            font.draw(spriteBatch, "Solo en combate",
                panelX + 200, panelY + panelAlto - 70);
        } else {
            font.draw(spriteBatch, "Para crafteo",
                panelX + 200, panelY + panelAlto - 45);
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
                float porcentajePS = (float) p.getPsActual() / p.getPsMaximos();
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
        float porcentajePS = (float) p.getPsActual() / p.getPsMaximos();

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
            float porcentajeStat = Math.min(1.0f, (float) stats[i] / maxStat);
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
            float ppPorcentaje = (float) m.getPpActual() / m.getPpMax();
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
        font.draw(spriteBatch, "SERIO", x + ancho / 2 - 30, y);
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
        font.draw(spriteBatch, "Datos de captura", x + ancho / 2 - 50, y - 20);
        font.getData().setScale(1.0f);
    }

    // Método auxiliar para colores de tipo
    private Color getColorPorTipo(Tipo tipo) {
        switch (tipo) {
            case FUEGO:
                return new Color(0.9f, 0.3f, 0.1f, 1);
            case AGUA:
                return new Color(0.1f, 0.5f, 0.9f, 1);
            case PLANTA:
                return new Color(0.2f, 0.7f, 0.2f, 1);
            case ELECTRICO:
                return new Color(0.9f, 0.9f, 0.1f, 1);
            case NORMAL:
                return new Color(0.7f, 0.7f, 0.6f, 1);
            default:
                return new Color(0.5f, 0.5f, 0.5f, 1);
        }
    }

    private void dibujarSeleccionPokemonParaItem(int screenWidth, int screenHeight) {
        // Usar el mismo dibujo del equipo pero con instrucciones diferentes
        dibujarEquipoPokemon(screenWidth, screenHeight);

        // Fondo semitransparente en la parte superior
        spriteBatch.setColor(0, 0, 0, 0.7f);
        spriteBatch.draw(whitePixel, 0, screenHeight - 100, screenWidth, 100);
        spriteBatch.setColor(Color.WHITE);

        // Información del item
        Item item = player.getSelectedItem();
        if (item != null) {
            font.setColor(Color.YELLOW);
            font.getData().setScale(1.2f);
            font.draw(spriteBatch, "USAR: " + item.getNombre(), 50, screenHeight - 40);
            font.getData().setScale(1.0f);

            font.setColor(new Color(0.8f, 0.8f, 1.0f, 1));
            font.draw(spriteBatch, item.getDescripcion(), 50, screenHeight - 70);

            // Info específica si es curación
            if (item instanceof Curacion) {
                Curacion pocion = (Curacion) item;
                font.draw(spriteBatch, "Cura " + pocion.getHpRestaurado() + " PS",
                    screenWidth - 200, screenHeight - 40);
            }
        }

        // Instrucciones
        font.setColor(new Color(0.6f, 0.6f, 0.8f, 1));
        String instrucciones = "Selecciona un Pokémon  Enter: Curar  ESC: Cancelar";
        font.draw(spriteBatch, instrucciones, 250, 50);

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

            switch (recetaSeleccionada.id) {
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
    // Pokédex - REEMPLAZAR COMPLETAMENTE
    private void dibujarPokedex(int screenWidth, int screenHeight) {
        pokedexScreen.dibujar(spriteBatch, screenWidth, screenHeight);
    }

    // Guardar (placeholder)
    private void dibujarGuardar(int screenWidth, int screenHeight) {
        // Título
        font.getData().setScale(2.0f);
        font.setColor(new Color(0.9f, 0.9f, 1.0f, 1));
        String titulo = "GUARDAR PARTIDA";
        float tituloWidth = font.getData().scaleX * titulo.length() * 10;
        font.draw(spriteBatch, titulo, (screenWidth - tituloWidth) / 2, screenHeight - 100);
        font.getData().setScale(1.0f);

        // Opciones
        String[] opciones = {
            "Guardar partida",
            "Guardar y salir al menú",
            "Cancelar"
        };

        float startX = screenWidth / 2 - 100;
        float startY = screenHeight / 2 + 80;
        float espacio = 50;

        for (int i = 0; i < opciones.length; i++) {
            if (i == selectedSaveOption) {
                // Fondo para opción seleccionada
                spriteBatch.setColor(0.3f, 0.3f, 0.5f, 0.8f);
                spriteBatch.draw(whitePixel, startX - 20, startY - i * espacio - 20, 240, 35);
                spriteBatch.setColor(Color.WHITE);

                // Texto seleccionado
                font.setColor(Color.YELLOW);
                font.draw(spriteBatch, "> " + opciones[i], startX, startY - i * espacio);
                font.setColor(Color.WHITE);
            } else {
                font.draw(spriteBatch, "  " + opciones[i], startX, startY - i * espacio);
            }
        }

        // Información adicional
        font.setColor(new Color(0.7f, 0.7f, 0.9f, 1));
        font.getData().setScale(0.9f);

        // Mostrar estadísticas actuales
        String info = "Pokémon en equipo: " + player.getEntrenador().getEquipo().size() +
            " | Ítems en inventario: " + player.getInventario().getCantidadTotal();
        float infoWidth = font.getData().scaleX * info.length() * 6;
        font.draw(spriteBatch, info, (screenWidth - infoWidth) / 2, startY - opciones.length * espacio - 20);

        // Instrucciones
        String instrucciones = "Flechas: Navegar | ENTER: Seleccionar | ESC: Volver";
        float insWidth = font.getData().scaleX * instrucciones.length() * 6;
        font.draw(spriteBatch, instrucciones, (screenWidth - insWidth) / 2, 50);

        font.getData().setScale(1.0f);
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
            // NO agregar case para POKEDEX - dejamos que la Pokédex maneje sus propias instrucciones
            default:
                instrucciones = "Flechas: Navegar | Enter: Seleccionar | ESC: Volver";
                break;
        }

        // Solo dibujar si hay instrucciones
        if (!instrucciones.isEmpty()) {
            float instruccionWidth = font.getData().scaleX * instrucciones.length() * 6;
            font.draw(spriteBatch, instrucciones, (screenWidth - instruccionWidth) / 2, 50);
        }

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

        if (pokedexScreen != null) {
            pokedexScreen.dispose();
        }

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

        // ❌ NO hay combateActivo que disponer
    }

    private String obtenerNombreMapaActual() {
        if (currentMapFile == null) return "Ubicación desconocida";

        // Extraer nombre del archivo sin extensión ni path
        String nombreArchivo = currentMapFile.substring(currentMapFile.lastIndexOf('/') + 1);
        nombreArchivo = nombreArchivo.replace(".tmx", "");

        // Reemplazar guiones bajos por espacios
        nombreArchivo = nombreArchivo.replace("_", " ");

        // Capitalizar primera letra de cada palabra
        String[] palabras = nombreArchivo.split(" ");
        StringBuilder nombreFormateado = new StringBuilder();

        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                nombreFormateado.append(Character.toUpperCase(palabra.charAt(0)))
                    .append(palabra.substring(1).toLowerCase())
                    .append(" ");
            }
        }

        return nombreFormateado.toString().trim();
    }

    private String obtenerNombreMapaParaEncuentros() {
        if (currentMapFile == null) return "mapa_centro";

        // Extraer solo el nombre del archivo sin extensión
        String nombreArchivo = currentMapFile.substring(
            currentMapFile.lastIndexOf('/') + 1,
            currentMapFile.lastIndexOf('.')
        );

        // Convertir a minúsculas
        return nombreArchivo.toLowerCase();
    }

    public void iniciarCombate(PokemonSalvaje pokemonSalvaje) {
        // Detener música del mundo abierto
        if (game.musics != null) {
            game.musics.stopopenworldmusic();
        }

        // Restablecer cooldown de encuentros
        encountersManager.resetEncounterCooldown();

        // Crear combate con el Pokémon actual del jugador
        Pokemon pokemonJugador = player.getEntrenador().getPokemonActual();
        if (pokemonJugador == null || pokemonJugador.estaDebilitado()) {
            // Buscar primer Pokémon no debilitado
            for (PokemonJugador p : player.getEntrenador().getEquipo()) {
                if (!p.estaDebilitado()) {
                    pokemonJugador = p;
                    player.getEntrenador().setPokemonActual(p);
                    break;
                }
            }
        }

        // Si no hay Pokémon disponibles, no se puede combatir
        if (pokemonJugador == null || pokemonJugador.estaDebilitado()) {
            System.out.println("¡Todos tus Pokémon están debilitados!");
            if (game.musics != null) {
                game.musics.startopenworldmusic();
            }
            return;
        }

        // Obtener ubicación actual formateada
        String ubicacion = obtenerNombreMapaActual();

        // Registrar avistamiento en Pokédex
        player.getEntrenador().registrarAvistamientoPokemon(
            pokemonSalvaje.getEspecie().getNombre(),
            ubicacion
        );

        // Crear combate
        Combate combate = new Combate(pokemonJugador, pokemonSalvaje);

        // Cambiar a pantalla de combate PASANDO LA UBICACIÓN
        game.setScreen(new CombateScreen(game, this, combate, player));
    }

    // Este método se llama desde CombateScreen para reanudar el juego
    public void reanudarDespuesCombate() {
        // Restaurar música del mundo
        if (game.musics != null) {
            game.musics.startopenworldmusic();
        }

        // Asegurar que el jugador no esté en menú
        player.setMenuState(MenuState.NONE);
    }

    private void validarLimiteInventario() {
        Inventario inv = player.getInventario();
        List<Ranura> itemsColumna = obtenerItemsPorColumna(player.getInventoryColumna(), inv);

        if (player.getInventoryIndice() >= itemsColumna.size()) {
            player.setInventoryIndice(Math.max(0, itemsColumna.size() - 1));
        }
    }

    private void dibujarColumnaConSelector(SpriteBatch batch, String titulo,
                                           List<Ranura> items, float x, float y,
                                           float espacio, int indiceSeleccionado) {
        font.setColor(new Color(0.8f, 0.8f, 1.0f, 1));
        font.draw(batch, titulo, x, y);
        y -= 30;

        for (int i = 0; i < items.size(); i++) {
            Ranura slot = items.get(i);
            boolean seleccionado = (i == indiceSeleccionado);

            // Color según tipo de item
            if (slot.getItem() instanceof Curacion) {
                font.setColor(seleccionado ? Color.YELLOW : new Color(0.5f, 1.0f, 0.5f, 1));
            } else if (slot.getItem() instanceof Pokeball) {
                font.setColor(seleccionado ? Color.YELLOW : new Color(1.0f, 0.5f, 0.5f, 1));
            } else {
                font.setColor(seleccionado ? Color.YELLOW : new Color(0.8f, 0.8f, 1.0f, 1));
            }

            // Fondo para item seleccionado
            if (seleccionado) {
                batch.setColor(new Color(0.3f, 0.3f, 0.5f, 0.8f));
                batch.draw(whitePixel, x - 10, y - 20, 200, 25);
                batch.setColor(Color.WHITE);
            }

            font.draw(batch, slot.getItem().getNombre() + " x" + slot.getCantidad(), x, y);
            y -= espacio;
        }
    }

    // Método auxiliar para obtener items por columna
    private List<Ranura> obtenerItemsPorColumna(int columna, Inventario inv) {
        List<Ranura> resultado = new ArrayList<>();

        for (Ranura slot : inv.getRanuras()) {
            Item item = slot.getItem();

            switch (columna) {
                case 0: // Recursos
                    if (!(item instanceof Curacion) && !(item instanceof Pokeball)) {
                        resultado.add(slot);
                    }
                    break;
                case 1: // Pociones
                    if (item instanceof Curacion) {
                        resultado.add(slot);
                    }
                    break;
                case 2: // Poké Balls
                    if (item instanceof Pokeball) {
                        resultado.add(slot);
                    }
                    break;
            }
        }

        return resultado;
    }

    private int calcularNivelPromedioEquipo() {
        List<PokemonJugador> equipo = player.getEntrenador().getEquipo();
        if (equipo.isEmpty()) return 5;

        int total = 0;
        for (PokemonJugador p : equipo) {
            total += p.getNivel();
        }
        return total / equipo.size();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
