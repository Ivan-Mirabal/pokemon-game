package com.pokemon.game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pokemon.game.pokemon.FabricaPokemon;
import com.pokemon.game.pokemon.PokemonJugador;

public class StarterSelectionScreen implements Screen {

    final PokemonGame game;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture whitePixel;
    private GlyphLayout layout;

    private String[] starters = {"Snivy", "Charmander", "Wooper"};
    private String[] tipos = {"PLANTA", "FUEGO", "AGUA"};
    private Color[] coloresTipo = {Color.GREEN, Color.ORANGE, Color.BLUE};
    private Texture[] starterSprites;

    private int selectionIndex = 1;
    private final int V_WIDTH = 800;
    private final int V_HEIGHT = 600;

    public StarterSelectionScreen(final PokemonGame game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont(); // Usar fuente por defecto
        this.layout = new GlyphLayout();

        // Crear textura blanca para el selector
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.whitePixel = new Texture(pixmap);
        pixmap.dispose();

        cargarSprites();
    }

    private void cargarSprites() {
        starterSprites = new Texture[3];
        for (int i = 0; i < 3; i++) {
            String path = "sprites/pokemon/" + starters[i].toLowerCase() + "/front.png";
            if (Gdx.files.internal(path).exists()) {
                starterSprites[i] = new Texture(Gdx.files.internal(path));
            } else {
                starterSprites[i] = whitePixel; // Placeholder
            }
        }
    }

    @Override
    public void render(float delta) {
        // Fondo oscuro azulado
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1);

        // Input
        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            selectionIndex = (selectionIndex > 0) ? selectionIndex - 1 : 2;
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            selectionIndex = (selectionIndex < 2) ? selectionIndex + 1 : 0;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            confirmarSeleccion();
        }

        batch.begin();

        // 1. TÍTULO PRINCIPAL
        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
        drawCenteredText("ELIGE TU COMPAÑERO", V_HEIGHT - 80);

        // 2. DIBUJAR LOS 3 POKÉMON
        float startX = 150; // Margen izquierdo
        float spacing = 250; // Espacio entre centros
        float centerY = V_HEIGHT / 2;

        for (int i = 0; i < 3; i++) {
            float x = startX + (i * spacing);

            if (i == selectionIndex) {
                // CUADRO DE SELECCIÓN (Brilla con el color del tipo)
                batch.setColor(coloresTipo[i]);
                batch.draw(whitePixel, x - 70, centerY - 70, 140, 140);
                batch.setColor(Color.WHITE);

                // NOMBRE (Debajo del cuadro)
                font.getData().setScale(2.0f);
                font.setColor(Color.YELLOW);
                drawCenteredText(starters[i].toUpperCase(), centerY - 100, x);

                // TIPO
                font.getData().setScale(1.2f);
                font.setColor(Color.LIGHT_GRAY);
                drawCenteredText(tipos[i], centerY - 130, x);
            } else {
                // NO SELECCIONADO (Cuadro gris tenue)
                batch.setColor(0.2f, 0.2f, 0.2f, 0.5f);
                batch.draw(whitePixel, x - 60, centerY - 60, 120, 120);
                batch.setColor(Color.WHITE);
            }

            // DIBUJAR SPRITE (Escalado a 120x120 para que se vea bien)
            if (starterSprites[i] != null) {
                batch.draw(starterSprites[i], x - 60, centerY - 60, 120, 120);
            }
        }

        // 3. PIE DE PÁGINA / INSTRUCCIONES
        font.getData().setScale(1.2f);
        font.setColor(Color.GRAY);
        drawCenteredText("Usa las flechas para navegar y ENTER para elegir", 60);

        batch.end();
    }

    // Método para centrar texto en toda la pantalla
    private void drawCenteredText(String text, float y) {
        layout.setText(font, text);
        font.draw(batch, text, (V_WIDTH - layout.width) / 2, y);
    }

    // Método para centrar texto respecto a una coordenada X específica
    private void drawCenteredText(String text, float y, float centerX) {
        layout.setText(font, text);
        font.draw(batch, text, centerX - (layout.width / 2), y);
    }

    private void confirmarSeleccion() {
        final String especie = starters[selectionIndex];
        Gdx.input.getTextInput(new com.badlogic.gdx.Input.TextInputListener() {
            @Override
            public void input(String text) {
                String apodo = (text == null || text.trim().isEmpty()) ? especie : text.trim();
                iniciarJuego(especie, apodo);
            }
            @Override
            public void canceled() {
                iniciarJuego(especie, especie);
            }
        }, "Ponle un apodo a " + especie, especie, "");
    }

    private void iniciarJuego(String especie, String apodo) {
        Gdx.app.postRunnable(() -> {
            PokemonJugador inicial = FabricaPokemon.crearPokemonJugador(especie, 5, apodo);
            game.setScreen(new GameScreen(game, "maps/mapa_centro.tmx", 15 * 16, 10 * 16, inicial));
        });
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        whitePixel.dispose();
        for(Texture t : starterSprites) if(t != null) t.dispose();
    }
}
