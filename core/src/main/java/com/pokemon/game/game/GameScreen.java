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
import com.pokemon.game.player.Inventario;
import com.pokemon.game.player.Player;
import com.pokemon.game.player.Ranura;

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

    // Cooldown para evitar transiciones infinitas
    private float transitionCooldown = 0f;
    private static final float TRANSITION_COOLDOWN_TIME = 0.3f;

    // Variables para el Menú
    private BitmapFont font;
    private Texture whitePixel;

    public GameScreen(final PokemonGame game, String initialMap, float startX, float startY) {
        this.game = game;
        this.initialMap = initialMap;
        this.startX = startX;
        this.startY = startY;
    }

    @Override
    public void show() {
        loadMap(initialMap, startX, startY);

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

    @Override
    public void render(float delta) {
        // Actualizar cooldown
        if (transitionCooldown > 0) {
            transitionCooldown -= delta;
        }

        // 1. Manejo de teclas
        handleInput();

        // 2. Lógica del juego (solo si NO hay menú abierto)
        if (player.getMenuState() == MenuState.NONE) {
            player.update(delta);
            checkMapTransition();
            updateCamera();
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
        // Tecla I para abrir/cerrar menú principal
        if (Gdx.input.isKeyJustPressed(Keys.I)) {
            if (player.getMenuState() == MenuState.NONE) {
                player.setMenuState(MenuState.MAIN);
            } else {
                player.setMenuState(MenuState.NONE);
            }
        }

        // Tecla ESC para retroceder
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            player.goBack();
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

    // Método para dibujar el menú activo
    private void dibujarMenu() {
        // Usar proyección de pantalla para el menú
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.begin();

        // Fondo semitransparente
        spriteBatch.setColor(0, 0, 0, 0.7f);
        spriteBatch.draw(whitePixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.setColor(Color.WHITE);

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
            case POKEMON:
                dibujarPokemon(screenWidth, screenHeight);
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

        // Instrucciones generales
        dibujarInstrucciones(screenWidth, screenHeight);

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
    private void dibujarPokemon(int screenWidth, int screenHeight) {
        font.getData().setScale(2.0f);
        font.draw(spriteBatch, "POKÉMON", screenWidth / 2 - 60, screenHeight - 100);
        font.getData().setScale(1.0f);

        font.draw(spriteBatch, "Aquí iría tu equipo Pokémon", screenWidth / 2 - 120, screenHeight / 2);
        font.draw(spriteBatch, "(Implementación pendiente)", screenWidth / 2 - 100, screenHeight / 2 - 30);
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
        if (player != null) player.dispose();
        if (font != null) font.dispose();
        if (whitePixel != null) whitePixel.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
