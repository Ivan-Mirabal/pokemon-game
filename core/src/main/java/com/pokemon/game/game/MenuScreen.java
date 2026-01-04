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

public class MenuScreen implements Screen {

    final PokemonGame game;
    private SpriteBatch batch;
    private Texture background;
    private Texture titleLogo;
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    private String[] menuItems = {"INICIAR JUEGO", "MULTIJUGADOR", "SALIR DEL JUEGO"};
    private int selectedItem = 0;
    private float blinkTimer = 0f;
    private boolean showText = true;

    // Bandera para evitar múltiples transiciones
    private boolean transitioning = false;

    // Variables para animación del logo
    private float logoScale = 1.0f;
    private float logoPulseSpeed = 1.5f;
    private float logoPulseTimer = 0f;
    private float maxLogoScale = 1.05f;
    private float minLogoScale = 0.95f;

    // Tamaño fijo para ventana de 800x600
    private final int SCREEN_WIDTH = 800;
    private final int SCREEN_HEIGHT = 600;

    public MenuScreen(final PokemonGame game) {
        this.game = game;
        batch = new SpriteBatch();

        // Cargar fondo
        try {
            background = new Texture(Gdx.files.internal("sprites/menu_background.png"));
        } catch (Exception e) {
            background = createFallbackTexture(Color.DARK_GRAY);
        }

        // Cargar logo del título
        try {
            titleLogo = new Texture(Gdx.files.internal("sprites/title_logo.png"));
        } catch (Exception e) {
            // Si no existe el logo, crear uno temporal
            titleLogo = createPlaceholderLogo();
        }

        // Fuente - tamaño ajustado para 800x600
        font = new BitmapFont();
        font.getData().setScale(1.8f);

        glyphLayout = new GlyphLayout();
    }

    private Texture createFallbackTexture(Color color) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(
            SCREEN_WIDTH, SCREEN_HEIGHT,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
        );
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createPlaceholderLogo() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(
            400, 240, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
        );
        pixmap.setColor(0.2f, 0.4f, 0.8f, 1.0f);
        pixmap.fill();
        pixmap.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        pixmap.fillCircle(200, 120, 80);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void render(float delta) {
        // Manejar entrada primero
        if (!transitioning) {
            handleInput();
        }

        // Actualizar animación del logo (pulsación suave)
        logoPulseTimer += delta * logoPulseSpeed;
        logoScale = minLogoScale + (maxLogoScale - minLogoScale) *
            (float)Math.abs(Math.sin(logoPulseTimer)) * 0.5f;

        // Solo renderizar si no estamos en transición
        if (transitioning) {
            return;
        }

        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        batch.begin();

        // Dibujar fondo
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // CALCULAR POSICIÓN DEL LOGO - BAJADO A 300px
        float logoWidth = titleLogo.getWidth() * logoScale;
        float logoHeight = titleLogo.getHeight() * logoScale;
        float logoX = (SCREEN_WIDTH - logoWidth) / 2;
        float logoY = 300; // BAJADO: 300px desde abajo (antes 350)

        // Dibujar logo con efecto de pulso
        batch.draw(titleLogo, logoX, logoY, logoWidth, logoHeight);

        // OPCIONES DEL MENÚ (también bajadas un poco para mantener proporción)
        float startY = 250; // BAJADO: 200px desde abajo (antes 220)
        float spacing = 50f;

        for (int i = 0; i < menuItems.length; i++) {
            String itemText = menuItems[i];
            String displayText = itemText;

            // Si está seleccionado y showText es true, agregar indicadores
            if (i == selectedItem && showText) {
                displayText = "> " + itemText + " <";
            }

            // Calcular posición
            glyphLayout.setText(font, displayText);
            float x = (SCREEN_WIDTH - glyphLayout.width) / 2;
            float y = startY - (i * spacing);

            // Determinar color de relleno
            Color fillColor;
            if (i == selectedItem) {
                fillColor = Color.GREEN;
            } else {
                fillColor = new Color(0.9f, 0.9f, 0.9f, 1);
            }

            // Dibujar con borde negro
            font.setColor(Color.BLACK);
            font.draw(batch, displayText, x - 1, y);
            font.draw(batch, displayText, x + 1, y);
            font.draw(batch, displayText, x, y - 1);
            font.draw(batch, displayText, x, y + 1);

            // Texto principal
            font.setColor(fillColor);
            font.draw(batch, displayText, x, y);
        }

        // INSTRUCCIONES (posición fija en la parte inferior)
        font.getData().setScale(1.2f);
        String instructions = "Flechas: Arriba/Abajo | ENTER: Seleccionar";
        glyphLayout.setText(font, instructions);
        float insX = (SCREEN_WIDTH - glyphLayout.width) / 2;
        float insY = 50; // 50px desde abajo

        // Instrucciones con borde
        font.setColor(Color.BLACK);
        font.draw(batch, instructions, insX - 1, insY);
        font.draw(batch, instructions, insX + 1, insY);
        font.draw(batch, instructions, insX, insY - 1);
        font.draw(batch, instructions, insX, insY + 1);

        font.setColor(new Color(0.8f, 0.8f, 0.8f, 1));
        font.draw(batch, instructions, insX, insY);

        // Restaurar tamaño de fuente para opciones
        font.getData().setScale(1.8f);

        // Actualizar parpadeo
        blinkTimer += delta;
        if (blinkTimer >= 0.3f) {
            blinkTimer = 0f;
            showText = !showText;
        }

        batch.end();
    }

    private void handleInput() {
        // Navegación del menú
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            selectedItem = (selectedItem - 1 + menuItems.length) % menuItems.length;
        }

        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            selectedItem = (selectedItem + 1) % menuItems.length;
        }

        // Seleccionar opción
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            transitioning = true;

            switch(selectedItem) {
                case 0: // INICIAR JUEGO
                    System.out.println("Iniciando juego...");
                    game.setScreen(new GameScreen(game, "maps/mapa_centro.tmx", 15 * 16, 10 * 16));
                    break;

                case 1: // MULTIJUGADOR
                    System.out.println("Modo multijugador - en desarrollo");
                    game.setScreen(new GameScreen(game, "maps/mapa_centro.tmx", 15 * 16, 10 * 16));
                    break;

                case 2: // SALIR DEL JUEGO
                    System.out.println("Saliendo del juego...");
                    Gdx.app.exit();
                    break;
            }
        }
    }

    @Override
    public void show() {
        transitioning = false;
        selectedItem = 0;
        logoScale = 1.0f;
        logoPulseTimer = 0f;
        game.musics.startmenumusic();
    }

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
        if (batch != null) batch.dispose();
        if (background != null) background.dispose();
        if (titleLogo != null) titleLogo.dispose();
        if (font != null) font.dispose();
    }
}
