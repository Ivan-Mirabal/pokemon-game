package com.pokemon.game.item;

public class Recurso extends Item {
    private String tipo; // "Planta", "Guijarro", "Baya"

    // CONSTRUCTOR SIMPLE - sin cantidad
    public Recurso(String nombre, String tipo) {
        super(nombre, "Recurso para craftear");
        this.tipo = tipo;
    }

    public String getTipo() { return tipo; }

    @Override
    public void usar() {

        System.out.println("Recurso '" + nombre + "' (tipo: " + tipo +
            "). Usado para craftear, no para usar directamente.");
    }
}
