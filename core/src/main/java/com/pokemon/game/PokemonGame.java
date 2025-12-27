package com.pokemon.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class PokemonGame extends Game {

    @Override
    public void create() {

        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        // Comenzar con la pantalla de inicio
        setScreen(new SplashScreen(this));

        // O si quieres ir directamente al menú principal:
        // setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
