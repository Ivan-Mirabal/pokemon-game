package com.pokemon.game.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class PokemonGame extends Game {

    @Override
    public void create() {
        // Iniciar con la SplashScreen
        setScreen(new SplashScreen(this));
    }

    @Override
    public void setScreen(Screen screen) {
        Screen oldScreen = getScreen();
        if (oldScreen != null) {
            oldScreen.dispose();
        }
        super.setScreen(screen);
    }

    @Override
    public void dispose() {
        Screen current = getScreen();
        if (current != null) {
            current.dispose();
        }
        super.dispose();
    }
}
