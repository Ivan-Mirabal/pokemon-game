package com.pokemon.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {

    final PokemonGame game;

    // Variables
    private TiledMap mapa; //El mapa
    private OrthogonalTiledMapRenderer renderer; // Renderizado para hacer el mapa
    private String currentMapFile; // El mapa actual (para lo del cambio de escenario)

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

    public GameScreen(final PokemonGame game, String initialMap, float startX, float startY) {
        this.game = game;
        this.currentMapFile = initialMap;
        this.playerStartX = startX;
        this.playerStartY = startY;
    }

    @Override
    public void show() {
        loadMap(currentMapFile, playerStartX, playerStartY);
    }

    /**
     * Carga un nuevo mapa y posiciona al jugador
     */
    private void loadMap(String mapFile, float playerX, float playerY) {
        // Limpiar recursos del mapa anterior
        if (mapa != null) {
            mapa.dispose();
        }
        if (renderer != null) {
            renderer.dispose();
        }

        // Carga el nuevo mapa
        currentMapFile = mapFile;
        mapa = MapManager.loadMap(mapFile);

        // Propiedades del mapa
        mapWidth = mapa.getProperties().get("width", Integer.class);
        mapHeight = mapa.getProperties().get("height", Integer.class);
        tileWidth = mapa.getProperties().get("tilewidth", Integer.class);
        tileHeight = mapa.getProperties().get("tileheight", Integer.class);

        // Calcular dimensiones del mundo
        worldWidthPx = mapWidth * tileWidth;
        worldHeightPx = mapHeight * tileHeight;

        // Inicializar el renderizador
        renderer = new OrthogonalTiledMapRenderer(mapa, 1f);

        // Cargar la capa de colisiones
        collisionLayer = (TiledMapTileLayer) mapa.getLayers().get("Colisiones");

        // Inicializar SpriteBatch
        if (spriteBatch == null) {
            spriteBatch = new SpriteBatch();
        }

        // Crear o reposicionar al jugador
        if (player == null) {
            player = new Player("sprites/player.png", playerX, playerY, tileWidth, tileHeight, this);
        } else {
            player.x = playerX;
            player.y = playerY;
        }

        // Configurar la cámara
        if (camara == null) {
            camara = new OrthographicCamera();
        }
        if (viewport == null) {
            float viewportWidth = (Gdx.graphics.getWidth() / zoomScale);
            float viewportHeight = (Gdx.graphics.getHeight() / zoomScale);
            viewport = new FitViewport(viewportWidth, viewportHeight, camara);
        }

        // Calcular límites de la cámara
        calculateCameraBounds(viewport.getWorldWidth(), viewport.getWorldHeight());

        // Centrar la cámara
        updateCamera();

        Gdx.app.log("MapManager", "Mapa cargado: " + mapFile);
    }

    /**
     * Verifica si el jugador está intentando cambiar de mapa
     */
    private void checkMapTransition() {
        MapManager.MapInfo currentMapInfo = MapManager.getMapInfo(currentMapFile);
        if (currentMapInfo == null) return;

        float transitionMargin = 5; // Margen en píxeles para detectar transición

        // Transición pal norte
        if (player.y >= worldHeightPx - transitionMargin && currentMapInfo.northMap != null) {
            transitionToMap(currentMapInfo.northMap, player.x, transitionMargin);
        }
        // Transición pal sur
        else if (player.y <= transitionMargin && currentMapInfo.southMap != null) {
            transitionToMap(currentMapInfo.southMap, player.x, worldHeightPx - transitionMargin);
        }
        // Transición pal este
        else if (player.x >= worldWidthPx - transitionMargin && currentMapInfo.eastMap != null) {
            transitionToMap(currentMapInfo.eastMap, transitionMargin, player.y);
        }
        // Transición pal oeste
        else if (player.x <= transitionMargin && currentMapInfo.westMap != null) {
            transitionToMap(currentMapInfo.westMap, worldWidthPx - transitionMargin, player.y);
        }
    }

    /**
     * Realiza la transición a otro mapa
     */
    private void transitionToMap(String newMapFile, float newPlayerX, float newPlayerY) {
        Gdx.app.log("MapManager", "Transición a: " + newMapFile);
        loadMap(newMapFile, newPlayerX, newPlayerY);
    }

    // Para que la camara no se salga de los limite
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

    // Posicion de la camara sin salirse del limite
    private void updateCamera() {
        float cameraX = Math.max(cameraMinX, Math.min(cameraMaxX, player.x));
        float cameraY = Math.max(cameraMinY, Math.min(cameraMaxY, player.y));

        camara.position.set(cameraX, cameraY, 0);
        camara.update();
    }

    // Verifica colision
    public boolean isCollision(float x, float y) {
        int tileX = (int) (x / tileWidth);
        int tileY = (int) (y / tileHeight);

        if (tileX < 0 || tileX >= mapWidth || tileY < 0 || tileY >= mapHeight) {
            return true;
        }

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null;
    }

    @Override
    public void render(float delta) {
        // 1. Lógica de movimiento
        player.update(delta);

        // 2. Verificar transiciones de mapa
        checkMapTransition();

        // 3. Renderizado
        ScreenUtils.clear(0, 0, 0, 1);

        // 4. Actualizar cámara
        updateCamera();

        // 5. Dibujar mapa
        renderer.setView(camara);
        renderer.render();

        // 6. Dibujar jugador
        spriteBatch.setProjectionMatrix(camara.combined);
        spriteBatch.begin();
        spriteBatch.draw(player.currentFrame,
            player.x - player.width/2,
            player.y - player.height/2,
            player.width,
            player.height);
        spriteBatch.end();
    }

    // más metodos y sobreescrituras
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
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    // Variables temporales para el constructor
    private float playerStartX, playerStartY;
}
