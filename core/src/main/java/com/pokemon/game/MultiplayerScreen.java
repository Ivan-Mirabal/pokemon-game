package com.pokemon.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

public class MultiplayerScreen implements Screen {

    final PokemonGame game;
    SpriteBatch batch;
    BitmapFont font;

    public MultiplayerScreen(final PokemonGame game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.0f);
        font.setColor(Color.WHITE);
    }

    @Override
    public void render(float delta) {
        handleInput();

        ScreenUtils.clear(0.2f, 0.2f, 0.3f, 1);

        batch.begin();

        font.draw(batch, "MODO MULTIJUGADOR",
            Gdx.graphics.getWidth()/2 - 150,
            Gdx.graphics.getHeight()/2 + 50);

        font.getData().setScale(1.5f);
        font.draw(batch, "(Implementación pendiente)",
            Gdx.graphics.getWidth()/2 - 120,
            Gdx.graphics.getHeight()/2);

        font.getData().setScale(1.0f);
        font.draw(batch, "Presiona ESC para volver al menú",
            Gdx.graphics.getWidth()/2 - 140,
            100);

        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
