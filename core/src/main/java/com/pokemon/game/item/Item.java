package com.pokemon.game.item;

public abstract class Item {
    protected String nombre;
    protected String descripcion;

    public Item(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }

    // AÑADIR SETTER PARA NOMBRE
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public abstract void usar();
}
