package com.pokemon.game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Pantalla de presentación inicial que muestra un logo antes de dirigir al usuario
 * al menú principal del juego. Implementa efectos de fundido de entrada y salida
 * durante un tiempo determinado antes de realizar la transición automática.
 */
public class SplashScreen implements Screen {

    /** Referencia a la clase principal del juego para manejar cambios de pantalla */
    private final PokemonGame game;

    /** Lote de sprites utilizado para renderizar la textura del splash */
    private SpriteBatch batch;

    /** Textura del logo que se muestra durante la pantalla de presentación */
    private Texture splashTexture;

    /** Tiempo acumulado desde que se mostró la pantalla, en segundos */
    private float timeElapsed = 0;

    /** Duración total que la pantalla de splash permanece visible, en segundos */
    private final float splashDuration = 2.0f;

    /** Controla si ya se inició la transición para evitar cambios múltiples */
    private boolean transitioning = false;

    /**
     * Construye una nueva pantalla de presentación inicializando los recursos gráficos.
     * Si no encuentra la textura del logo, genera una de reemplazo automáticamente.
     *
     * @param game Instancia principal del juego para coordinar cambios de pantalla
     */
    public SplashScreen(final PokemonGame game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // Intenta cargar la textura del logo, genera una alternativa si falla
        try {
            splashTexture = new Texture(Gdx.files.internal("sprites/splash_logo.png"));
        } catch (Exception e) {
            splashTexture = createPlaceholderTexture();
        }
    }

    /**
     * Genera una textura de reemplazo simple cuando no se encuentra el archivo original.
     * Crea un círculo azul sobre fondo blanco como imagen de respaldo.
     *
     * @return Textura generada como alternativa visual
     */
    private Texture createPlaceholderTexture() {
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

    /**
     * Procesa la lógica de renderizado de la pantalla, incluyendo el cálculo de tiempo,
     * efectos visuales de fundido y la transición automática al menú principal.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        timeElapsed += delta;

        // Evita procesamiento adicional si ya se inició la transición
        if (transitioning) {
            return;
        }

        // Calcula transparencia para efectos de fundido
        float alpha = 0;
        if (timeElapsed < 1.0f) {
            alpha = timeElapsed; // Fundido de entrada durante el primer segundo
        } else if (timeElapsed > splashDuration - 1.0f) {
            alpha = splashDuration - timeElapsed; // Fundido de salida en el último segundo
        } else {
            alpha = 1.0f; // Visibilidad completa durante el tiempo intermedio
        }

        // Limpia la pantalla con color negro
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Renderiza la textura con el nivel de transparencia calculado
        batch.begin();
        batch.setColor(1, 1, 1, alpha);
        float x = Gdx.graphics.getWidth() / 2 - splashTexture.getWidth() / 2;
        float y = Gdx.graphics.getHeight() / 2 - splashTexture.getHeight() / 2;
        batch.draw(splashTexture, x, y);
        batch.setColor(1, 1, 1, 1); // Restaura la opacidad completa
        batch.end();

        // Inicia la transición al menú principal cuando se cumple el tiempo
        if (timeElapsed >= splashDuration && !transitioning) {
            transitioning = true;
            game.setScreen(new MenuScreen(game));
        }
    }

    /**
     * Libera todos los recursos gráficos utilizados por esta pantalla.
     * Este método debe llamarse cuando la pantalla ya no se necesita.
     */
    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (splashTexture != null) {
            splashTexture.dispose();
            splashTexture = null;
        }
    }

    /**
     * Se ejecuta cuando esta pantalla se activa como pantalla actual.
     * Reinicia el estado de transición para permitir nuevas visualizaciones.
     */
    @Override
    public void show() {
        transitioning = false;
    }

    /**
     * Maneja el redimensionamiento de la ventana de visualización.
     *
     * @param width Nuevo ancho de la ventana en píxeles
     * @param height Nuevo alto de la ventana en píxeles
     */
    @Override
    public void resize(int width, int height) {
        // La implementación actual no requiere acciones específicas al redimensionar
    }

    /**
     * Se ejecuta cuando el juego entra en estado de pausa.
     */
    @Override
    public void pause() {
        // La implementación actual no requiere acciones específicas al pausar
    }

    /**
     * Se ejecuta cuando el juego se reanuda después de una pausa.
     */
    @Override
    public void resume() {
        // La implementación actual no requiere acciones específicas al reanudar
    }

    /**
     * Se ejecuta cuando esta pantalla deja de ser la pantalla actual activa.
     */
    @Override
    public void hide() {
        // La implementación actual no requiere acciones específicas al ocultar
    }
}
