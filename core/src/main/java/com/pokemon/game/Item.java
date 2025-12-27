package com.pokemon.game;

public abstract class Item {
    protected String nombre;
    protected String descripcion;
    protected int cantidad;

    public Item(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidad = 1;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    // Ahora es abstracto, pero sin parámetro Jugador
    public abstract void usar();
}
