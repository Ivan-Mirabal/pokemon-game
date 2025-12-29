package com.pokemon.game;

// Curacion.java
public class Curacion extends Item {
    private int hpRestaurado;

    public Curacion(String nombre, int hpRestaurado) {
        super(nombre, "Restaura PS de Pokémon");
        this.hpRestaurado = hpRestaurado;
    }

    @Override
    public void usar() {
        System.out.println("Usaste " + nombre + " y restauraste " + hpRestaurado + " PS");
        // Lógica de curación, NO manejo de cantidad
    }
}
