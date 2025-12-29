package com.pokemon.game;

public class Pokeball extends Item {
    private float tasaCaptura;

    // SOLO constructor sin cantidad
    public Pokeball() {
        super("Poké Ball", "Dispositivo para capturar Pokémon");
        this.tasaCaptura = 1.0f;
    }

    // ELIMINAR constructor con cantidad
    // public Pokeball(int cantidad) {
    //     super("Poké Ball", "Dispositivo para capturar Pokémon");
    //     this.tasaCaptura = 1.0f;
    //     this.cantidad = cantidad; // ¡ERROR! Item no tiene cantidad
    // }

    public float getTasaCaptura() { return tasaCaptura; }

    @Override
    public void usar() {
        // SOLO lógica, NO decrementar cantidad
        System.out.println("¡Lanzaste una Poké Ball hacia el Pokémon salvaje!");
        // Aquí iría la lógica de captura:
        // 1. Calcular probabilidad de captura
        // 2. Intentar capturar
        // 3. Devolver éxito/fracaso
    }

    // MÉTODO NUEVO: Para la lógica de captura
    public boolean intentarCapturar() {
        // Lógica de captura (simplificada)
        double probabilidad = tasaCaptura * 0.3; // Ejemplo simple
        return Math.random() < probabilidad;
    }
}
