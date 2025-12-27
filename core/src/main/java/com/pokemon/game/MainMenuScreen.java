package com.pokemon.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen implements Screen {

    final PokemonGame game;
    SpriteBatch batch;
    Texture background;
    Texture titleLogo;
    BitmapFont menuFont;
    GlyphLayout glyphLayout;

    String[] menuItems = {"INICIAR JUEGO", "MULTIJUGADOR", "SALIR"};
    int selectedItem = 0;

    float menuYStart;
    float menuSpacing = 50f;

    // Variables para posición del logo
    float logoX, logoY, logoWidth, logoHeight;

    public MainMenuScreen(final PokemonGame game) {
        this.game = game;
        batch = new SpriteBatch();

        // Cargar imágenes
        background = new Texture(Gdx.files.internal("sprites/menu_background.png"));
        titleLogo = new Texture(Gdx.files.internal("sprites/title_logo.png"));

        // Tamaño del logo (500x200 como dijiste)
        logoWidth = 500;
        logoHeight = 200;

        // Centrar horizontalmente
        float screenWidth = Gdx.graphics.getWidth();   // 800px
        float screenHeight = Gdx.graphics.getHeight(); // 600px

        logoX = (screenWidth - logoWidth) / 2; // (800-500)/2 = 150px

        // POSICIÓN VERTICAL MÁS BAJA - AJUSTADA
        // Antes: 0.72f (432px desde abajo) → Ahora: 0.60f (360px desde abajo)
        logoY = screenHeight * 0.60f; // 60% desde arriba = 240px desde abajo

        // Fuente para el menú
        menuFont = new BitmapFont();
        menuFont.getData().setScale(1.8f);
        menuFont.setColor(Color.WHITE);

        glyphLayout = new GlyphLayout();

        // Menú también más bajo
        // 120px debajo del logo (antes era 80px)
        menuYStart = logoY - 120;

        // Asegurar que el menú no quede demasiado abajo
        if (menuYStart < 100) {
            menuYStart = 100;
        }

        Gdx.app.log("MENU", "Logo en: " + logoX + ", " + logoY);
        Gdx.app.log("MENU", "Menú comienza en: " + menuYStart);
    }

    @Override
    public void render(float delta) {
        handleInput();

        // Limpiar pantalla
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        batch.begin();

        // Dibujar fondo
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Dibujar logo en 500x200
        batch.draw(titleLogo,
            logoX,           // Posición X centrada
            logoY,           // Posición Y ajustada (más baja)
            logoWidth,       // 500px
            logoHeight);     // 200px

        // Dibujar opciones del menú (más bajas también)
        for (int i = 0; i < menuItems.length; i++) {
            String item = menuItems[i];

            // Calcular posición centrada
            glyphLayout.setText(menuFont, item);
            float x = (Gdx.graphics.getWidth() - glyphLayout.width) / 2;
            float y = menuYStart - (i * menuSpacing);

            // Resaltar opción seleccionada
            if (i == selectedItem) {
                menuFont.setColor(Color.YELLOW);
                menuFont.draw(batch, ">", x - 25, y); // Selector
                menuFont.draw(batch, item, x, y);
                menuFont.setColor(Color.WHITE);
            } else {
                menuFont.draw(batch, item, x, y);
            }
        }

        // Instrucciones también un poco más bajas
        menuFont.setColor(Color.LIGHT_GRAY);
        menuFont.getData().setScale(1.0f);
        String instructions = "Usa ARRIBA/ABAJO para navegar, ENTER para seleccionar";
        glyphLayout.setText(menuFont, instructions);
        float instX = (Gdx.graphics.getWidth() - glyphLayout.width) / 2;
        // Bajar instrucciones de 50 a 60
        menuFont.draw(batch, instructions, instX, 60);
        menuFont.getData().setScale(1.8f);

        batch.end();
    }

    private void handleInput() {
        // Navegación del menú
        if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W)) {
            selectedItem--;
            if (selectedItem < 0) {
                selectedItem = menuItems.length - 1;
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S)) {
            selectedItem++;
            if (selectedItem >= menuItems.length) {
                selectedItem = 0;
            }
        }

        // Seleccionar opción
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            selectMenuItem();
        }

        // Salir con ESC
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private void selectMenuItem() {
        switch (selectedItem) {
            case 0: // INICIAR JUEGO
                game.setScreen(new GameScreen(game, "maps/mapa_centro.tmx", 15 * 16, 10 * 16));
                dispose();
                break;

            case 1: // MULTIJUGADOR
                System.out.println("Modo multijugador - Implementación pendiente");
                break;

            case 2: // SALIR
                Gdx.app.exit();
                break;
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (background != null) background.dispose();
        if (titleLogo != null) titleLogo.dispose();
        if (menuFont != null) menuFont.dispose();
    }

    @Override
    public void show() {}

    @Override
    public void resize(int width, int height) {
        // Recalcular posiciones si la ventana cambia de tamaño
        logoX = (width - logoWidth) / 2;
        logoY = height * 0.60f;
        menuYStart = logoY - 120;
        if (menuYStart < 100) menuYStart = 100;
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
