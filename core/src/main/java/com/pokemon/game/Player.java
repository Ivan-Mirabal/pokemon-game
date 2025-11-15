package com.pokemon.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

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

    // Animaciones separadas para cada dirección
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkRightAnimation;

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
    }

    private void setupAllAnimations() {
        // Animación para caminar hacia abajo
        Array<TextureRegion> downFrames = new Array<TextureRegion>();
        for (int col = 0; col < frameCols; col++) {
            downFrames.add(frames[DIR_DOWN][col]);
        }
        walkDownAnimation = new Animation<TextureRegion>(0.1f, downFrames, Animation.PlayMode.LOOP);

        // Animación para caminar hacia arriba
        Array<TextureRegion> upFrames = new Array<TextureRegion>();
        for (int col = 0; col < frameCols; col++) {
            upFrames.add(frames[DIR_UP][col]);
        }
        walkUpAnimation = new Animation<TextureRegion>(0.1f, upFrames, Animation.PlayMode.LOOP);

        // Animación para caminar hacia la izquierda
        Array<TextureRegion> leftFrames = new Array<TextureRegion>();
        for (int col = 0; col < frameCols; col++) {
            leftFrames.add(frames[DIR_LEFT][col]);
        }
        walkLeftAnimation = new Animation<TextureRegion>(0.1f, leftFrames, Animation.PlayMode.LOOP);

        // Animación para caminar hacia la derecha
        Array<TextureRegion> rightFrames = new Array<TextureRegion>();
        for (int col = 0; col < frameCols; col++) {
            rightFrames.add(frames[DIR_RIGHT][col]);
        }
        walkRightAnimation = new Animation<TextureRegion>(0.1f, rightFrames, Animation.PlayMode.LOOP);
    }

    public void update(float delta) {
        float movement = speed * delta;
        isMoving = false;

        // Guardar posición anterior para detectar movimiento
        float prevX = x;
        float prevY = y;

        // Magia bizarra para caminar en una sola direccion
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

        // 2. Verificar colisión
        if (gameScreen.isCollision(x, y)) {
            x = prevX;
            y = prevY;
            isMoving = false; // Detener animación si hay colisión
        }

        // 3. Actualizar estado de animación
        if (isMoving) {
            // Incrementar el tiempo solo si realmente nos estamos moviendo
            stateTime += delta;

            // Seleccionar la animación correcta según la dirección
            switch (currentDir) {
                case DIR_DOWN:
                    currentFrame = walkDownAnimation.getKeyFrame(stateTime, true);
                    break;
                case DIR_UP:
                    currentFrame = walkUpAnimation.getKeyFrame(stateTime, true);
                    break;
                case DIR_LEFT:
                    currentFrame = walkLeftAnimation.getKeyFrame(stateTime, true);
                    break;
                case DIR_RIGHT:
                    currentFrame = walkRightAnimation.getKeyFrame(stateTime, true);
                    break;
            }
        } else {
            // Cuando está quieto, mostrar el primer frame estático de la dirección actual
            currentFrame = frames[currentDir][0];
            // Reiniciar el stateTime para que la animación empiece desde el principio la próxima vez
            stateTime = 0;
        }
    }

    // XD
    public void dispose() {
        spriteSheet.dispose();
    }
}
