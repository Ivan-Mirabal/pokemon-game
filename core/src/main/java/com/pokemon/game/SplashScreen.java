package com.pokemon.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class SplashScreen implements Screen {

    final PokemonGame game;
    SpriteBatch batch;
    Texture splashTexture;
    float timeElapsed = 0;
    float splashDuration = 2.0f; // 2 segundos

    // Bandera para evitar múltiples transiciones
    private boolean transitioning = false;

    public SplashScreen(final PokemonGame game) {
        this.game = game;
        batch = new SpriteBatch();

        // Cargar la textura del splash, con manejo de error
        try {
            splashTexture = new Texture(Gdx.files.internal("sprites/splash_logo.png"));
        } catch (Exception e) {
            // Si no existe, crear una textura de relleno
            splashTexture = createPlaceholderTexture();
        }
    }

    private Texture createPlaceholderTexture() {
        // Crear una textura de relleno simple (un cuadrado blanco)
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(200, 200,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        pixmap.setColor(0.2f, 0.2f, 0.8f, 1);
        pixmap.fillCircle(100, 100, 80);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void render(float delta) {
        timeElapsed += delta;

        // Solo procesar si no estamos en transición
        if (transitioning) {
            return;
        }

        // Fade in/out effect
        float alpha = 0;
        if (timeElapsed < 1.0f) {
            alpha = timeElapsed; // Fade in
        } else if (timeElapsed > splashDuration - 1.0f) {
            alpha = splashDuration - timeElapsed; // Fade out
        } else {
            alpha = 1.0f; // Mantener visible
        }

        // Limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.setColor(1, 1, 1, alpha);
        // Dibujar centrado
        float x = Gdx.graphics.getWidth()/2 - splashTexture.getWidth()/2;
        float y = Gdx.graphics.getHeight()/2 - splashTexture.getHeight()/2;
        batch.draw(splashTexture, x, y);
        batch.setColor(1, 1, 1, 1); // Restaurar
        batch.end();

        // Cambiar a menú principal después del tiempo
        if (timeElapsed >= splashDuration && !transitioning) {
            transitioning = true;
            game.setScreen(new MenuScreen(game)); // Cambiar a MenuScreen
        }
    }

    @Override
    public void dispose() {
        System.out.println("SplashScreen: Disposing resources");
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (splashTexture != null) {
            splashTexture.dispose();
            splashTexture = null;
        }
    }

    // Métodos de Screen
    @Override
    public void show() {
        transitioning = false;
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}
}
