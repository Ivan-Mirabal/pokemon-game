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

    public SplashScreen(final PokemonGame game) {
        this.game = game;
        batch = new SpriteBatch();
        splashTexture = new Texture(Gdx.files.internal("sprites/splash_logo.png")); // Cambia por tu logo
    }

    @Override
    public void render(float delta) {
        timeElapsed += delta;

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
        batch.draw(splashTexture,
            Gdx.graphics.getWidth()/2 - splashTexture.getWidth()/2,
            Gdx.graphics.getHeight()/2 - splashTexture.getHeight()/2);
        batch.end();

        // Cambiar a menú principal después del tiempo
        if (timeElapsed >= splashDuration) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        splashTexture.dispose();
    }

    // Métodos de Screen sin implementar
    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
