package com.pokemon.game;

public abstract class Item {
    protected String nombre;
    protected String descripcion;
    // ELIMINADO: protected int cantidad;

    public Item(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        // ELIMINADO: this.cantidad = 1;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }

    public abstract void usar();  // Solo lógica de uso, NO manejo de cantidad
}
