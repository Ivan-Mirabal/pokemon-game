package com.pokemon.game.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Clase que gestiona la reproducción de bandas sonoras del juego, encargándose
 * de cargar, reproducir, detener y liberar los recursos de audio para diferentes
 * situaciones del juego como menús, mundo abierto, combates y pantallas de pausa.
 */
public class Musics {

    /** Música de fondo para la pantalla de título o menú principal */
    private Music backgroundmusic1;

    /** Música de fondo para la exploración en el mundo abierto del juego */
    private Music backgroundmusic2;

    /** Música de fondo para los combates contra entrenadores Pokémon */
    private Music backgroundmusic3;

    /** Música de fondo para situaciones especiales como pantallas de pausa */
    private Music backgroundmusic4;

    /**
     * Carga las cuatro pistas de música principales del juego desde los archivos
     * de recursos. Si ocurre un error durante la carga, se imprime la traza de
     * la excepción para facilitar la depuración.
     */
    public Musics() {
        try {
            backgroundmusic1 = Gdx.audio.newMusic(Gdx.files.internal("music/02 - Title Screen.mp3"));
            backgroundmusic2 = Gdx.audio.newMusic(Gdx.files.internal("music/15 - Pewter City.mp3"));
            backgroundmusic3 = Gdx.audio.newMusic(Gdx.files.internal("music/1-15. Battle (VS Trainer).mp3"));
            backgroundmusic4 = Gdx.audio.newMusic(Gdx.files.internal("music/1-38. Surf.mp3"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inicia la reproducción de la música del menú principal con configuración
     * específica de bucle continuo y volumen moderado.
     */
    public void startmenumusic() {
        backgroundmusic1.setLooping(true);
        backgroundmusic1.setVolume(0.5f);
        backgroundmusic1.play();
    }

    /**
     * Inicia la reproducción de la música del mundo abierto con configuración
     * específica de bucle continuo y volumen moderado.
     */
    public void startopenworldmusic() {
        backgroundmusic2.setLooping(true);
        backgroundmusic2.setVolume(0.5f);
        backgroundmusic2.play();
    }

    /**
     * Inicia la reproducción de la música de combate con configuración
     * específica de bucle continuo y volumen moderado.
     */
    public void startBattleMusic() {
        backgroundmusic3.setLooping(true);
        backgroundmusic3.setVolume(0.5f);
        backgroundmusic3.play();
    }

    /**
     * Inicia la reproducción de la música de pausa con configuración
     * específica de bucle continuo y volumen muy bajo para no interferir
     * con la experiencia del jugador durante pausas.
     */
    public void startpausemusic() {
        backgroundmusic4.setLooping(true);
        backgroundmusic4.setVolume(0.05f);
        backgroundmusic4.play();
    }

    /**
     * Detiene la reproducción de la música del menú principal.
     */
    public void stopmenumusic() {
        backgroundmusic1.stop();
    }

    /**
     * Detiene la reproducción de la música de pausa.
     */
    public void stoppausemusic() {
        backgroundmusic4.stop();
    }

    /**
     * Detiene la reproducción de la música del mundo abierto.
     */
    public void stopopenworldmusic() {
        backgroundmusic2.stop();
    }

    /**
     * Libera los recursos asociados a la música del menú principal.
     */
    public void disposemenumusic() {
        backgroundmusic1.dispose();
    }

    /**
     * Libera los recursos asociados a la música del mundo abierto.
     */
    public void disposeopenworldmusic() {
        backgroundmusic2.dispose();
    }

    /**
     * Detiene la reproducción de la música de combate.
     */
    public void stopBattleMusic() {
        backgroundmusic3.stop();
    }
}
