package com.pokemon.game;

public class Curacion extends Item {
    private int hpRestaurado;

    public Curacion(String nombre, int hpRestaurado) {
        super(nombre, "Restaura PS de Pokémon");
        this.hpRestaurado = hpRestaurado;
    }

    public Curacion(String nombre, int hpRestaurado, int cantidad) {
        super(nombre, "Restaura PS de Pokémon");
        this.hpRestaurado = hpRestaurado;
        this.cantidad = cantidad;
    }

    public int getHpRestaurado() { return hpRestaurado; }

    @Override
    public void usar() {
        if (cantidad > 0) {
            cantidad--;
            System.out.println("Usaste " + nombre + " y restauraste " + hpRestaurado + " PS");
        }
    }
}
