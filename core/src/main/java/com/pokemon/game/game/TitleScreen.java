package com.pokemon.game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

public class TitleScreen implements Screen {

    final PokemonGame game;
    private SpriteBatch batch;
    private Texture titleTexture;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private float blinkTimer = 0f;
    private boolean showText = true;

    // Bandera para evitar múltiples transiciones
    private boolean transitioning = false;

    public TitleScreen(final PokemonGame game) {
        this.game = game;
        batch = new SpriteBatch();

        try {
            titleTexture = new Texture(Gdx.files.internal("sprites/title_screen.png"));
        } catch (Exception e) {
            // Fallback si no existe la textura
            titleTexture = createFallbackTexture();
        }

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        glyphLayout = new GlyphLayout();
    }

    private Texture createFallbackTexture() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(800, 600,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLUE);
        pixmap.fill();
        pixmap.setColor(Color.YELLOW);
        pixmap.fillCircle(400, 300, 100);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void render(float delta) {
        // Manejar entrada primero
        if (!transitioning && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            transitioning = true;
            game.setScreen(new MenuScreen(game));
            return; // Salir inmediatamente después de cambiar pantalla
        }

        // Solo renderizar si no estamos en transición
        if (transitioning) {
            return;
        }

        ScreenUtils.clear(0.0f, 0.2f, 0.4f, 1);

        batch.begin();
        batch.draw(titleTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        blinkTimer += delta;
        if (blinkTimer >= 0.5f) {
            blinkTimer = 0;
            showText = !showText;
        }

        if (showText) {
            String message = "Presiona ENTER para empezar";
            glyphLayout.setText(font, message);
            float x = (Gdx.graphics.getWidth() - glyphLayout.width) / 2;
            float y = Gdx.graphics.getHeight() * 0.2f;
            font.draw(batch, message, x, y);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        System.out.println("TitleScreen: Disposing resources");
        if (batch != null) {
            batch.dispose();
        }
        if (titleTexture != null) {
            titleTexture.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }

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
