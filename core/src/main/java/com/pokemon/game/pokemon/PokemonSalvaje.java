package com.pokemon.game.pokemon;

/**
 * Representa un Pokémon salvaje encontrado en la naturaleza, listo para ser enfrentado o capturado.
 * Hereda las propiedades base de un Pokémon y añade funcionalidades específicas para encuentros salvajes.
 */
public class PokemonSalvaje extends Pokemon {
    private int nivelAparente;

    /**
     * Crea un nuevo Pokémon salvaje con la especie y nivel especificados.
     * Utiliza el nombre de la especie como apodo por defecto.
     */
    public PokemonSalvaje(EspeciePokemon especie, int nivel) {
        super(especie, especie.getNombre(), nivel); // Apodo = nombre de especie
        this.nivelAparente = nivel;
    }

    /**
     * Intenta capturar este Pokémon salvaje utilizando una Pokéball con multiplicador específico.
     * La probabilidad de captura considera la salud actual del Pokémon y su especie.
     */
    public boolean intentarCaptura(float multiplicadorBall, double porcentajePs) {
        double probabilidad = (especie.getTasaCaptura() * multiplicadorBall) / 255.0;

        // Ajustar por HP bajo
        if (porcentajePs < 0.5) probabilidad *= 1.5;
        if (porcentajePs < 0.2) probabilidad *= 2.0;
        if (porcentajePs < 0.1) probabilidad *= 2.5;

        probabilidad = Math.min(probabilidad, 1.0);
        return Math.random() < probabilidad;
    }

    /**
     * Determina si el Pokémon salvaje logra huir del combate.
     * La probabilidad de huida depende de la velocidad relativa entre los Pokémon.
     */
    public boolean intentarHuir(Pokemon jugadorPokemon) {
        double probHuir = (double)velocidad / (velocidad + jugadorPokemon.getVelocidad());
        return Math.random() < probHuir;
    }

    /**
     * Convierte este Pokémon salvaje en un Pokémon del jugador tras una captura exitosa.
     * Preserva los movimientos conocidos y establece una amistad inicial.
     */
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

    /**
     * Obtiene el nivel aparente del Pokémon salvaje, que coincide con su nivel real.
     * Útil para mostrar información al jugador durante el encuentro.
     */
    public int getNivelAparente() { return nivelAparente; }
}
