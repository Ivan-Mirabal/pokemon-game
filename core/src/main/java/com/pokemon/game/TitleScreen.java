package com.pokemon.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

public class TitleScreen implements Screen {

    final PokemonGame game;
    SpriteBatch batch;
    Texture titleTexture;

    // Nuevas variables para el texto
    BitmapFont font;
    float blinkTimer = 0f;
    boolean showText = true;

    public TitleScreen(final PokemonGame game) {
        this.game = game;
        batch = new SpriteBatch();
        titleTexture = new Texture(Gdx.files.internal("sprites/title_screen.png"));

        // Inicializar la fuente
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f); // Tamaño de fuente más grande
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.0f, 0.2f, 0.4f, 1);

        batch.begin();
        // Dibuja la imagen de título
        batch.draw(titleTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Lógica de parpadeo del texto
        blinkTimer += delta;
        if (blinkTimer >= 0.5f) { // Cambia cada 0.5 segundos
            blinkTimer = 0;
            showText = !showText;
        }

        // Dibujar texto parpadeante
        if (showText) {
            String message = "Presiona ENTER para empezar";

            // Calcular posición centrada en X y en la parte inferior en Y
            float textWidth = font.getXHeight() * message.length() * 1.2f; // Aproximación del ancho
            float x = (Gdx.graphics.getWidth() - textWidth) / 2;
            float y = Gdx.graphics.getHeight() * 0.2f; // 20% desde abajo

            font.draw(batch, message, x, y);
        }

        batch.end();

        // Lógica de transición al presionar ENTER
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            game.setScreen(new GameScreen(game, "maps/mapa_centro.tmx", 15 * 16, 10 * 16));
            dispose();
        }
    }

    @Override
    public void show() {}
    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        titleTexture.dispose();
        font.dispose(); // ¡IMPORTANTE! Liberar la fuente
    }
}
