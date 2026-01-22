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
import com.pokemon.game.data.SaveData;
import com.pokemon.game.data.SaveManager;

/**
 * Pantalla del menú principal del juego. Muestra opciones para iniciar nueva partida, cargar partida y salir.
 */
public class MenuScreen implements Screen {
    private final PokemonGame game;
    private SpriteBatch batch;
    private Texture background;
    private Texture titleLogo;
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    private String[] menuItems = {"NUEVA PARTIDA", "CARGAR PARTIDA", "SALIR DEL JUEGO"};
    private int selectedItem = 0;
    private float blinkTimer = 0f;
    private boolean showText = true;

    // Control de transiciones
    private boolean transitioning = false;

    // Animación del logo
    private float logoScale = 1.0f;
    private float logoPulseSpeed = 1.5f;
    private float logoPulseTimer = 0f;
    private float maxLogoScale = 1.05f;
    private float minLogoScale = 0.95f;

    // Tamaño de pantalla fijo
    private final int SCREEN_WIDTH = 800;
    private final int SCREEN_HEIGHT = 600;

    /**
     * Constructor. Inicializa los recursos gráficos del menú.
     * @param game Instancia principal del juego
     */
    public MenuScreen(final PokemonGame game) {
        this.game = game;
        batch = new SpriteBatch();

        // Cargar fondos
        try {
            background = new Texture(Gdx.files.internal("sprites/menu_background.png"));
        } catch (Exception e) {
            background = createFallbackTexture(Color.DARK_GRAY);
        }

        try {
            titleLogo = new Texture(Gdx.files.internal("sprites/title_logo.png"));
        } catch (Exception e) {
            titleLogo = createPlaceholderLogo();
        }

        // Fuente
        font = new BitmapFont();
        font.getData().setScale(1.8f);
        glyphLayout = new GlyphLayout();
    }

    /**
     * Crea una textura de fallback cuando no se encuentra la imagen.
     * @param color Color de la textura
     * @return Textura generada
     */
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

    /**
     * Crea un logo temporal cuando no se encuentra la imagen original.
     * @return Logo temporal
     */
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

    /**
     * Renderiza la pantalla. Maneja entrada, animaciones y dibuja todos los elementos.
     * @param delta Tiempo desde el último frame
     */
    @Override
    public void render(float delta) {
        if (!transitioning) handleInput();

        // Animación del logo
        logoPulseTimer += delta * logoPulseSpeed;
        logoScale = minLogoScale + (maxLogoScale - minLogoScale) *
            (float)Math.abs(Math.sin(logoPulseTimer)) * 0.5f;

        if (transitioning) return;

        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        batch.begin();

        // Fondo
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Logo
        float logoWidth = titleLogo.getWidth() * logoScale;
        float logoHeight = titleLogo.getHeight() * logoScale;
        float logoX = (SCREEN_WIDTH - logoWidth) / 2;
        float logoY = 300;
        batch.draw(titleLogo, logoX, logoY, logoWidth, logoHeight);

        // Opciones del menú
        float startY = 250;
        float spacing = 50f;

        for (int i = 0; i < menuItems.length; i++) {
            String itemText = menuItems[i];
            String displayText = itemText;

            if (i == selectedItem && showText) {
                displayText = "> " + itemText + " <";
            }

            glyphLayout.setText(font, displayText);
            float x = (SCREEN_WIDTH - glyphLayout.width) / 2;
            float y = startY - (i * spacing);

            Color fillColor = (i == selectedItem) ? Color.GREEN : new Color(0.9f, 0.9f, 0.9f, 1);

            // Borde
            font.setColor(Color.BLACK);
            font.draw(batch, displayText, x - 1, y);
            font.draw(batch, displayText, x + 1, y);
            font.draw(batch, displayText, x, y - 1);
            font.draw(batch, displayText, x, y + 1);

            // Texto principal
            font.setColor(fillColor);
            font.draw(batch, displayText, x, y);
        }

        // Instrucciones
        font.getData().setScale(1.2f);
        String instructions = "Flechas: Arriba/Abajo | ENTER: Seleccionar";
        glyphLayout.setText(font, instructions);
        float insX = (SCREEN_WIDTH - glyphLayout.width) / 2;
        float insY = 50;

        font.setColor(Color.BLACK);
        font.draw(batch, instructions, insX - 1, insY);
        font.draw(batch, instructions, insX + 1, insY);
        font.draw(batch, instructions, insX, insY - 1);
        font.draw(batch, instructions, insX, insY + 1);

        font.setColor(new Color(0.8f, 0.8f, 0.8f, 1));
        font.draw(batch, instructions, insX, insY);

        font.getData().setScale(1.8f);

        // Parpadeo de selección
        blinkTimer += delta;
        if (blinkTimer >= 0.3f) {
            blinkTimer = 0f;
            showText = !showText;
        }

        batch.end();
    }

    /**
     * Maneja la entrada del teclado para navegar el menú.
     */
    private void handleInput() {
        if (transitioning) return;

        // Navegación
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            selectedItem = (selectedItem - 1 + menuItems.length) % menuItems.length;
        }

        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            selectedItem = (selectedItem + 1) % menuItems.length;
        }

        // Selección
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            transitioning = true;

            switch(selectedItem) {
                case 0: // Nueva partida
                    System.out.println("Iniciando nueva partida...");
                    game.setScreen(new StarterSelectionScreen(game));
                    break;

                case 1: // Cargar partida
                    cargarPartidaGuardada();
                    break;

                case 2: // Salir
                    System.out.println("Saliendo del juego...");
                    Gdx.app.exit();
                    break;
            }
        }
    }

    /**
     * Intenta cargar una partida guardada y transiciona a la pantalla de juego.
     */
    private void cargarPartidaGuardada() {
        try {
            if (!SaveManager.getInstance().existePartida()) return;

            SaveData datos = SaveManager.getInstance().cargarPartida();

            if (datos != null) {
                if (game.getScreen() instanceof GameScreen) {
                    GameScreen existingScreen = (GameScreen) game.getScreen();
                    existingScreen.cargarDatosJugador(datos);
                    game.setScreen(existingScreen);
                } else {
                    GameScreen gameScreen = new GameScreen(game, "maps/mapa_centro.tmx", 30 * 16, 20 * 16);
                    game.setScreen(gameScreen);
                    gameScreen.cargarDatosJugador(datos);
                }
                System.out.println("Datos cargados exitosamente");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
