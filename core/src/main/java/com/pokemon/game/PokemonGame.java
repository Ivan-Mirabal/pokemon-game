package com.pokemon.game;

import com.badlogic.gdx.Game;

public class PokemonGame extends Game {

    @Override
    public void create() {
        //Crea el juego y muestra la pantalla de inicio
        setScreen(new TitleScreen(this));
    }

    @Override
    public void dispose() {
        //Cierra el juego y limpia todas las cosas necesarias
        super.dispose();
    }
}
