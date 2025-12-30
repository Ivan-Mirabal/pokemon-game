package com.pokemon.game.item;

public class Curacion extends Item {
    private int hpRestaurado;

    public Curacion(String nombre, int hpRestaurado) {
        super(nombre, "Restaura PS de Pokémon");
        this.hpRestaurado = hpRestaurado;
    }

    @Override
    public void usar() {
        System.out.println("Usaste " + nombre + " y restauraste " + hpRestaurado + " PS");
    }

    // AÑADIR GETTER
    public int getHpRestaurado() {
        return hpRestaurado;
    }
}
