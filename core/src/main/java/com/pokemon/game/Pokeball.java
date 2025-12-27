package com.pokemon.game;

public class Pokeball extends Item {
    private float tasaCaptura;

    public Pokeball() {
        super("Poké Ball", "Dispositivo para capturar Pokémon");
        this.tasaCaptura = 1.0f;
    }

    public Pokeball(int cantidad) {
        super("Poké Ball", "Dispositivo para capturar Pokémon");
        this.tasaCaptura = 1.0f;
        this.cantidad = cantidad;
    }

    public float getTasaCaptura() { return tasaCaptura; }

    @Override
    public void usar() {
        if (cantidad > 0) {
            cantidad--;
            System.out.println("Usaste una Poké Ball");
        }
    }
}
