package com.pokemon.game.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pokemon.game.data.SaveManager;

public class PokemonGame extends Game {
    public SpriteBatch batch; // Añade esto
    public Musics musics;

    @Override
    public void create() {
        batch = new SpriteBatch(); // Créalo una sola vez
        musics = new Musics();
        SaveManager.getInstance();
        setScreen(new SplashScreen(this));
    }

    @Override
    public void setScreen(Screen screen) {
        Screen oldScreen = getScreen();

        // Solo cambiar la pantalla
        super.setScreen(screen);

        // Opcional: si quieres disponer la pantalla anterior solo en ciertos casos
        if (oldScreen != null && oldScreen != screen && !(oldScreen instanceof GameScreen)) {
            oldScreen.dispose();
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        musics.disposemenumusic();
        musics.disposeopenworldmusic();
        Screen current = getScreen();
        if (current != null) {
            current.dispose();
        }
        super.dispose();
    }
}
