package com.pokemon.game;

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
        float playerLeft = player.x - player.width/2;
        float playerRight = player.x + player.width/2;
        float playerBottom = player.y - player.height/2;
        float playerTop = player.y + player.height/2;

        if (playerTop >= worldHeightPx - margin && currentMapInfo.northMap != null) {
            transitionToMap(currentMapInfo.northMap, player.x, tileHeight * 2);
            transitionCooldown = TRANSITION_COOLDOWN_TIME;
        }
        else if (playerBottom <= margin && currentMapInfo.southMap != null) {
            transitionToMap(currentMapInfo.southMap, player.x, worldHeightPx - tileHeight * 3);
            transitionCooldown = TRANSITION_COOLDOWN_TIME;
        }
        else if (playerRight >= worldWidthPx - margin && currentMapInfo.eastMap != null) {
            transitionToMap(currentMapInfo.eastMap, tileWidth * 2, player.y);
            transitionCooldown = TRANSITION_COOLDOWN_TIME;
        }
        else if (playerLeft <= margin && currentMapInfo.westMap != null) {
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
        if (x < 0 || x >= worldWidthPx || y < 0 || y >= worldHeightPx) {
            return true;
        }

        int tileX = (int) (x / tileWidth);
        int tileY = (int) (y / tileHeight);

        if (tileX < 0 || tileX >= mapWidth || tileY < 0 || tileY >= mapHeight) {
            return true;
        }

        if (collisionLayer == null) {
            return false;
        }

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null;
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
            player.x - player.width/2,
            player.y - player.height/2,
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

        // Solo procesar navegación si hay un menú activo
        if (player.getMenuState() != MenuState.NONE) {
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
    private void dibujarInventario(int screenWidth, int screenHeight) {
        font.getData().setScale(1.8f);
        font.draw(spriteBatch, "INVENTARIO", screenWidth / 2 - 80, screenHeight - 80);
        font.getData().setScale(1.0f);

        font.draw(spriteBatch, "Capacidad: " + player.getInventario().getCantidadItems() +
                "/" + player.getInventario().getCapacidadMaxima(),
            screenWidth / 2 - 100, screenHeight - 120);

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

        // Lista de todos los items
        float startX = screenWidth * 0.2f;
        float startY = y - 50;
        float espacio = 25;

        List<Ranura> slots = player.getInventario().getRanuras();
        for (int i = 0; i < slots.size(); i++) {
            Ranura slot = slots.get(i);
            float itemY = startY - i * espacio;

            if (itemY < 100) {
                startX += 200;
                startY = y - 50;
                i--;
                continue;
            }

            String itemText = "• " + slot.getItem().getNombre() + " x" + slot.getCantidad();
            font.draw(spriteBatch, itemText, startX, itemY);
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

    // Pokédex (placeholder)
    private void dibujarPokedex(int screenWidth, int screenHeight) {
        font.getData().setScale(2.0f);
        font.draw(spriteBatch, "POKÉDEX", screenWidth / 2 - 60, screenHeight - 100);
        font.getData().setScale(1.0f);

        font.draw(spriteBatch, "Registro de Pokémon investigados", screenWidth / 2 - 140, screenHeight / 2);
        font.draw(spriteBatch, "(Implementación pendiente)", screenWidth / 2 - 100, screenHeight / 2 - 30);
    }

    // Crafteo (placeholder)
    private void dibujarCrafteo(int screenWidth, int screenHeight) {
        font.getData().setScale(2.0f);
        font.draw(spriteBatch, "CRAFTEO", screenWidth / 2 - 60, screenHeight - 100);
        font.getData().setScale(1.0f);

        font.draw(spriteBatch, "Crear objetos a partir de recursos", screenWidth / 2 - 150, screenHeight / 2);
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
    private void dibujarOpciones(int screenWidth, int screenHeight) {
        font.getData().setScale(2.0f);
        font.draw(spriteBatch, "OPCIONES", screenWidth / 2 - 60, screenHeight - 100);
        font.getData().setScale(1.0f);

        String[] opciones = {
            "Volumen: ███████░░░",
            "Pantalla: Ventana",
            "Controles",
            "Créditos"
        };

        float startX = screenWidth / 2 - 150;
        float startY = screenHeight / 2 + 50;
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

    // Instrucciones del menú
    private void dibujarInstrucciones(int screenWidth, int screenHeight) {
        font.getData().setScale(0.9f);
        String instrucciones = "";

        switch (player.getMenuState()) {
            case MAIN:
                instrucciones = "Flechas: Navegar | Enter: Seleccionar | ESC/I: Salir";
                break;
            case INVENTORY:
                instrucciones = "Flechas: Navegar | Enter: Usar item | ESC: Volver";
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
