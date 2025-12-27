package com.pokemon.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;

public class Player {

    public float x, y;
    public float width, height;
    public float speed;
    public TextureRegion currentFrame;

    // Variables de Animación
    private float stateTime;
    private boolean isMoving;

    private Texture spriteSheet;
    private TextureRegion[][] frames;

    // Animaciones en array para simplificar
    private Animation<TextureRegion>[] animations;

    private final int frameCols = 4;
    private final int frameRows = 4;

    // Mapeo de direcciones
    private final int DIR_DOWN = 0;
    private final int DIR_UP = 1;
    private final int DIR_LEFT = 2;
    private final int DIR_RIGHT = 3;
    private int currentDir = DIR_DOWN;

    private int tileWidth, tileHeight;
    private GameScreen gameScreen;

    // Variables del Inventario
    private Inventario inventario;

    // Variables del Menú
    private MenuState menuState;
    private int menuSelection;
    private boolean inSubMenu;

    public Player(String texturePath, float startX, float startY, int tileWidth, int tileHeight, GameScreen gameScreen) {
        this.x = startX;
        this.y = startY;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.gameScreen = gameScreen;

        this.speed = 4.0f * tileWidth;
        this.stateTime = 0f;

        spriteSheet = new Texture(Gdx.files.internal(texturePath));

        int spriteWidth = spriteSheet.getWidth() / frameCols;
        int spriteHeight = spriteSheet.getHeight() / frameRows;

        frames = TextureRegion.split(spriteSheet, spriteWidth, spriteHeight);

        this.width = spriteWidth;
        this.height = spriteHeight;

        // Configurar todas las animaciones al inicio
        setupAllAnimations();

        // Frame inicial estático mirando hacia abajo
        currentFrame = frames[DIR_DOWN][0];

        // INICIALIZAR INVENTARIO
        this.inventario = new Inventario(20); // Capacidad de 20 slots

        // INICIALIZAR ESTADO DEL MENÚ
        this.menuState = MenuState.NONE;
        this.menuSelection = 0;
        this.inSubMenu = false;

        // Agregar algunos items iniciales para pruebas
        inventario.agregarItem(new Pokeball(), 5);
        inventario.agregarItem(new Curacion("Poción", 20), 3);
        inventario.agregarItem(new Recurso("Planta", "Planta"), 5);
        inventario.agregarItem(new Recurso("Guijarro", "Guijarro"), 8);
        inventario.agregarItem(new Recurso("Baya", "Baya"), 3);
    }

    @SuppressWarnings("unchecked")
    private void setupAllAnimations() {
        animations = new Animation[4];

        for (int dir = 0; dir < 4; dir++) {
            Array<TextureRegion> dirFrames = new Array<>();
            for (int col = 0; col < frameCols; col++) {
                dirFrames.add(frames[dir][col]);
            }
            animations[dir] = new Animation<>(0.1f, dirFrames, Animation.PlayMode.LOOP);
        }
    }

    public void update(float delta) {
        // Si hay algún menú activo, no mover al jugador
        if (menuState != MenuState.NONE) {
            return;
        }

        float movement = speed * delta;
        isMoving = false;

        // Guardar posición anterior para detectar movimiento
        float prevX = x;
        float prevY = y;

        // Movimiento en una sola dirección
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            x -= movement;
            currentDir = DIR_LEFT;
            isMoving = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
            x += movement;
            currentDir = DIR_RIGHT;
            isMoving = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {
            y += movement;
            currentDir = DIR_UP;
            isMoving = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
            y -= movement;
            currentDir = DIR_DOWN;
            isMoving = true;
        }

        // Verificar colisión
        if (gameScreen.isCollision(x, y)) {
            x = prevX;
            y = prevY;
            isMoving = false; // Detener animación si hay colisión
        }

        // Actualizar estado de animación
        if (isMoving) {
            // Incrementar el tiempo solo si realmente nos estamos moviendo
            stateTime += delta;
            currentFrame = animations[currentDir].getKeyFrame(stateTime, true);
        } else {
            // Cuando está quieto, mostrar el primer frame estático de la dirección actual
            currentFrame = frames[currentDir][0];
            // Reiniciar el stateTime para que la animación empiece desde el principio
            stateTime = 0;
        }
    }

    // MÉTODOS DEL INVENTARIO
    public Inventario getInventario() {
        return inventario;
    }

    public boolean recolectarRecurso(Recurso recurso) {
        return inventario.agregarItem(recurso);
    }

    public boolean usarItem(String nombreItem) {
        Ranura slot = inventario.buscarItem(nombreItem);
        if (slot != null && slot.getCantidad() > 0) {
            slot.getItem().usar();
            slot.decrementar(1);

            if (slot.getCantidad() <= 0) {
                inventario.removerItem(nombreItem, 0);
            }
            return true;
        }
        return false;
    }

    // MÉTODOS DEL MENÚ
    public MenuState getMenuState() {
        return menuState;
    }

    public void setMenuState(MenuState state) {
        this.menuState = state;
        this.menuSelection = 0; // Resetear selección al cambiar estado
        this.inSubMenu = (state != MenuState.MAIN && state != MenuState.NONE);
    }

    public void toggleMenu() {
        if (menuState == MenuState.NONE) {
            setMenuState(MenuState.MAIN);
        } else {
            setMenuState(MenuState.NONE);
        }
    }

    public int getMenuSelection() {
        return menuSelection;
    }

    public void setMenuSelection(int selection) {
        this.menuSelection = selection;
    }

    public void moveMenuUp() {
        menuSelection--;
        if (menuSelection < 0) {
            menuSelection = getMaxMenuItems() - 1;
        }
    }

    public void moveMenuDown() {
        menuSelection++;
        if (menuSelection >= getMaxMenuItems()) {
            menuSelection = 0;
        }
    }

    public void selectMenuItem() {
        switch (menuState) {
            case MAIN:
                handleMainMenuSelection();
                break;
            case INVENTORY:
                // Aquí iría la lógica para seleccionar un item del inventario
                break;
            // Otros casos para otros menús
        }
    }

    public void goBack() {
        if (inSubMenu) {
            setMenuState(MenuState.MAIN);
        } else {
            setMenuState(MenuState.NONE);
        }
    }

    private void handleMainMenuSelection() {
        switch (menuSelection) {
            case 0: // Pokémon
                setMenuState(MenuState.POKEMON);
                break;
            case 1: // Pokédex
                setMenuState(MenuState.POKEDEX);
                break;
            case 2: // Inventario
                setMenuState(MenuState.INVENTORY);
                break;
            case 3: // Crafteo
                setMenuState(MenuState.CRAFTING);
                break;
            case 4: // Guardar partida
                setMenuState(MenuState.SAVE);
                break;
            case 5: // Opciones
                setMenuState(MenuState.OPTIONS);
                break;
        }
    }

    private int getMaxMenuItems() {
        switch (menuState) {
            case MAIN:
                return 6; // 6 opciones en el menú principal
            case INVENTORY:
                // CORRECCIÓN: Usar this.getInventario() o inventario directamente
                return this.getInventario().getRanuras().size();
            // Otros casos
            default:
                return 0;
        }
    }

    public void dispose() {
        spriteSheet.dispose();
    }
}
