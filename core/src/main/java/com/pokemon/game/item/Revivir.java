package com.pokemon.game.item;

public class Revivir extends Item {
    private int porcentajeRecuperacion; // Ejemplo: 50 para 50%

    public Revivir(String nombre, int porcentajeRecuperacion) {
        super(nombre, "Revive a un Pokémon");
        this.porcentajeRecuperacion = porcentajeRecuperacion;
    }

    public int getPorcentajeRecuperacion() {
        return porcentajeRecuperacion;
    }

    @Override
    public void usar() {
        System.out.println("Usando " + nombre + "...");
    }
}
