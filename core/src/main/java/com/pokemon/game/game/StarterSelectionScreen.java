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

/**
 * Pantalla de selección de Pokémon inicial donde el jugador puede elegir entre tres
 * Pokémon diferentes para comenzar su aventura. La interfaz muestra los tres Pokémon
 * disponibles con sus respectivos sprites y tipos, permitiendo navegar entre ellos
 * y asignar un apodo personalizado al elegido.
 */
public class StarterSelectionScreen implements Screen {

    /** Referencia principal al juego para gestionar cambios de pantalla */
    private final PokemonGame game;

    /** Lote de sprites utilizado para renderizar todos los elementos gráficos de la pantalla */
    private SpriteBatch batch;

    /** Fuente de texto utilizada para mostrar títulos, nombres y descripciones */
    private BitmapFont font;

    /** Textura de un píxel blanco utilizada para dibujar cuadros de selección y fondos */
    private Texture whitePixel;

    /** Utilidad para calcular dimensiones del texto y centrarlo adecuadamente */
    private GlyphLayout layout;

    /** Nombres de los tres Pokémon iniciales disponibles para selección */
    private String[] starters = {"Snivy", "Charmander", "Wooper"};

    /** Tipos elementales correspondientes a cada Pokémon inicial */
    private String[] tipos = {"PLANTA", "FUEGO", "AGUA"};

    /** Colores representativos de cada tipo elemental para resaltar visualmente la selección */
    private Color[] coloresTipo = {Color.GREEN, Color.ORANGE, Color.BLUE};

    /** Sprites frontales de cada Pokémon inicial cargados desde el sistema de archivos */
    private Texture[] starterSprites;

    /** Índice que indica cuál de los tres Pokémon está actualmente seleccionado */
    private int selectionIndex = 1;

    /** Ancho de la ventana de visualización utilizado para cálculos de posicionamiento */
    private final int V_WIDTH = 800;

    /** Alto de la ventana de visualización utilizado para cálculos de posicionamiento */
    private final int V_HEIGHT = 600;

    /**
     * Inicializa la pantalla de selección cargando todos los recursos gráficos necesarios
     * y preparando la interfaz de usuario para la interacción.
     *
     * @param game Instancia principal del juego para coordinar cambios de pantalla
     */
    public StarterSelectionScreen(final PokemonGame game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        this.layout = new GlyphLayout();

        // Crear textura blanca básica para cuadros de selección y marcadores visuales
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.whitePixel = new Texture(pixmap);
        pixmap.dispose();

        cargarSprites();
    }

    /**
     * Carga los sprites frontales de cada Pokémon inicial desde el directorio de recursos.
     * Si algún sprite no se encuentra, utiliza la textura blanca como marcador de posición.
     */
    private void cargarSprites() {
        starterSprites = new Texture[3];
        for (int i = 0; i < 3; i++) {
            String path = "sprites/pokemon/" + starters[i].toLowerCase() + "/front.png";
            if (Gdx.files.internal(path).exists()) {
                starterSprites[i] = new Texture(Gdx.files.internal(path));
            } else {
                starterSprites[i] = whitePixel;
            }
        }
    }

    /**
     * Renderiza cada frame de la pantalla procesando entrada del usuario, actualizando
     * la selección visual y dibujando todos los elementos gráficos.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        // Establece un fondo azul oscuro para la pantalla
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1);

        // Procesa entrada del teclado para navegar entre opciones
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

        // Renderiza el título principal centrado en la parte superior
        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
        drawCenteredText("ELIGE TU COMPAÑERO", V_HEIGHT - 80);

        // Renderiza los tres Pokémon disponibles en posición horizontal
        float startX = 150;
        float spacing = 250;
        float centerY = V_HEIGHT / 2;

        for (int i = 0; i < 3; i++) {
            float x = startX + (i * spacing);

            if (i == selectionIndex) {
                // Dibuja un cuadro resaltado con el color del tipo alrededor del Pokémon seleccionado
                batch.setColor(coloresTipo[i]);
                batch.draw(whitePixel, x - 70, centerY - 70, 140, 140);
                batch.setColor(Color.WHITE);

                // Muestra el nombre en mayúsculas y color amarillo debajo del cuadro
                font.getData().setScale(2.0f);
                font.setColor(Color.YELLOW);
                drawCenteredText(starters[i].toUpperCase(), centerY - 100, x);

                // Muestra el tipo elemental en color gris claro
                font.getData().setScale(1.2f);
                font.setColor(Color.LIGHT_GRAY);
                drawCenteredText(tipos[i], centerY - 130, x);
            } else {
                // Dibuja cuadros tenues para los Pokémon no seleccionados
                batch.setColor(0.2f, 0.2f, 0.2f, 0.5f);
                batch.draw(whitePixel, x - 60, centerY - 60, 120, 120);
                batch.setColor(Color.WHITE);
            }

            // Dibuja el sprite del Pokémon escalado a 120x120 píxeles
            if (starterSprites[i] != null) {
                batch.draw(starterSprites[i], x - 60, centerY - 60, 120, 120);
            }
        }

        // Renderiza las instrucciones de control en la parte inferior
        font.getData().setScale(1.2f);
        font.setColor(Color.GRAY);
        drawCenteredText("Usa las flechas para navegar y ENTER para elegir", 60);

        batch.end();
    }

    /**
     * Dibuja texto centrado horizontalmente en la pantalla en la posición Y especificada.
     *
     * @param text Texto a renderizar
     * @param y Posición vertical donde se dibujará la línea base del texto
     */
    private void drawCenteredText(String text, float y) {
        layout.setText(font, text);
        font.draw(batch, text, (V_WIDTH - layout.width) / 2, y);
    }

    /**
     * Dibuja texto centrado horizontalmente alrededor de una coordenada X específica.
     *
     * @param text Texto a renderizar
     * @param y Posición vertical donde se dibujará la línea base del texto
     * @param centerX Coordenada horizontal alrededor de la cual se centrará el texto
     */
    private void drawCenteredText(String text, float y, float centerX) {
        layout.setText(font, text);
        font.draw(batch, text, centerX - (layout.width / 2), y);
    }

    /**
     * Inicia el proceso de confirmación de selección solicitando al jugador que
     * ingrese un apodo personalizado para el Pokémon elegido mediante un cuadro
     * de diálogo de entrada de texto.
     */
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

    /**
     * Crea el Pokémon inicial seleccionado con el apodo proporcionado y cambia
     * a la pantalla principal del juego para comenzar la aventura.
     *
     * @param especie Nombre de la especie del Pokémon seleccionado
     * @param apodo Nombre personalizado asignado por el jugador
     */
    private void iniciarJuego(String especie, String apodo) {
        Gdx.app.postRunnable(() -> {
            PokemonJugador inicial = FabricaPokemon.crearPokemonJugador(especie, 5, apodo);
            game.setScreen(new GameScreen(game, "maps/mapa_centro.tmx", 30 * 16, 20 * 16, inicial));
        });
    }

    /** Se ejecuta cuando esta pantalla se convierte en la pantalla activa del juego */
    @Override
    public void show() {}

    /** Maneja el redimensionamiento de la ventana de visualización */
    @Override
    public void resize(int width, int height) {}

    /** Se ejecuta cuando el juego entra en estado de pausa */
    @Override
    public void pause() {}

    /** Se ejecuta cuando el juego se reanuda después de una pausa */
    @Override
    public void resume() {}

    /** Se ejecuta cuando esta pantalla deja de ser la pantalla activa */
    @Override
    public void hide() {}

    /**
     * Libera todos los recursos gráficos utilizados por esta pantalla para
     * prevenir fugas de memoria cuando la pantalla ya no es necesaria.
     */
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        whitePixel.dispose();
        for (Texture t : starterSprites) {
            if (t != null) {
                t.dispose();
            }
        }
    }
}
