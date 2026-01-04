package com.pokemon.game.item;

public class Pokeball extends Item {
    private float tasaCaptura;

    // Constructor básico
    public Pokeball() {
        super("Poké Ball", "Dispositivo para capturar Pokémon");
        this.tasaCaptura = 1.0f;
    }

    // Constructor con nombre personalizado y tasa
    public Pokeball(String nombre, float tasaCaptura) {
        super(nombre, "Dispositivo para capturar Pokémon");
        this.tasaCaptura = tasaCaptura;
    }

    // GETTER para la tasa (CORRECCIÓN: nombre correcto)
    public float getTasaCaptura() {
        return tasaCaptura;
    }

    // SETTERS
    public void setTasaCaptura(float tasa) {
        this.tasaCaptura = tasa;
    }

    // También necesitamos poder cambiar el nombre
    @Override
    public void setNombre(String nuevoNombre) {
        this.nombre = nuevoNombre;
    }

    @Override
    public void usar() {
        System.out.println("¡Lanzaste una " + nombre + " hacia el Pokémon salvaje!");
    }

    // Método para intentar captura (para compatibilidad)
    public boolean intentarCapturar() {
        double probabilidad = tasaCaptura * 0.3;
        return Math.random() < probabilidad;
    }
}
