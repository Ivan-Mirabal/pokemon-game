package com.pokemon.game;

public class Recurso extends Item {
    private String tipo; // "Planta", "Guijarro", "Baya"

    public Recurso(String nombre, String tipo) {
        super(nombre, "Recurso para craftear");
        this.tipo = tipo;
    }

    public Recurso(String nombre, String tipo, int cantidad) {
        super(nombre, "Recurso para craftear");
        this.tipo = tipo;
        this.cantidad = cantidad;
    }

    public String getTipo() { return tipo; }

    @Override
    public void usar() {
        // Los recursos no se usan directamente
        System.out.println("Este es un recurso para craftear");
    }
}
