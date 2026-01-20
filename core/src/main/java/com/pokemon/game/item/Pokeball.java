package com.pokemon.game.item;

public class Pokeball extends Item {
    private float tasaCaptura;

    // Constructor por defecto - SIN nombre fijo
    public Pokeball() {
        super("", "Dispositivo para capturar"); // Nombre vacío
        this.tasaCaptura = 1.0f; // Valor por defecto
    }

    // Constructor con nombre y tasa
    public Pokeball(String nombre, float tasaCaptura) {
        super(nombre, "Dispositivo para capturar");
        this.tasaCaptura = tasaCaptura;
    }

    // GETTERS Y SETTERS
    public float getTasaCaptura() {
        return tasaCaptura;
    }

    public void setTasaCaptura(float tasa) {
        this.tasaCaptura = tasa;
    }

    @Override
    public void setNombre(String nuevoNombre) {
        this.nombre = nuevoNombre;
    }

    @Override
    public void usar() {
        System.out.println("¡Lanzaste una " + nombre + " hacia el Pokémon salvaje!");
    }
}
