package com.pokemon.game.pokemon;

public class PokemonSalvaje extends Pokemon {
    private int nivelAparente;

    public PokemonSalvaje(EspeciePokemon especie, int nivel) {
        super(especie, especie.getNombre(), nivel); // Apodo = nombre de especie
        this.nivelAparente = nivel;
    }

    // Sistema de captura simplificado
    public boolean intentarCaptura(float multiplicadorBall, double porcentajePs) {
        double probabilidad = (especie.getTasaCaptura() * multiplicadorBall) / 255.0;

        // Ajustar por HP bajo
        if (porcentajePs < 0.5) probabilidad *= 1.5;
        if (porcentajePs < 0.2) probabilidad *= 2.0;
        if (porcentajePs < 0.1) probabilidad *= 2.5;

        probabilidad = Math.min(probabilidad, 1.0);
        return Math.random() < probabilidad;
    }

    // Huida del Pokémon salvaje
    public boolean intentarHuir(Pokemon jugadorPokemon) {
        double probHuir = (double)velocidad / (velocidad + jugadorPokemon.getVelocidad());
        return Math.random() < probHuir;
    }

    // Convertir a PokemonJugador después de captura
    public PokemonJugador convertirAJugador() {
        PokemonJugador capturado = new PokemonJugador(especie, especie.getNombre(), nivel);

        // Copiar movimientos
        for (Movimiento m : movimientos) {
            capturado.aprenderMovimiento(m);
        }

        // Amistad inicial para Pokémon capturado
        capturado.aumentarAmistad(30);

        return capturado;
    }

    public int getNivelAparente() { return nivelAparente; }
}
