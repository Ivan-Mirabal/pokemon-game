package com.pokemon.game.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pokemon.game.data.SaveManager;

/**
 * Clase principal del juego. Gestiona pantallas, renderizado y recursos.
 */
public class PokemonGame extends Game {
    /** Lote de sprites compartido para renderizado */
    public SpriteBatch batch;

    /** Gestor de música del juego */
    public Musics musics;

    /**
     * Inicializa el juego: crea recursos y muestra la pantalla de inicio.
     */
    @Override
    public void create() {
        batch = new SpriteBatch();
        musics = new Musics();
        SaveManager.getInstance();
        setScreen(new SplashScreen(this));
    }

    /**
     * Cambia la pantalla actual, liberando la anterior si no es GameScreen.
     * @param screen Nueva pantalla a mostrar
     */
    @Override
    public void setScreen(Screen screen) {
        Screen oldScreen = getScreen();
        super.setScreen(screen);

        if (oldScreen != null && oldScreen != screen && !(oldScreen instanceof GameScreen)) {
            oldScreen.dispose();
        }
    }

    /**
     * Libera todos los recursos al cerrar el juego.
     */
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        musics.disposemenumusic();
        musics.disposeopenworldmusic();
        if (getScreen() != null) getScreen().dispose();
        super.dispose();
    }
}
