package com.pokemon.game;

public class Ranura {
    private Item item;
    private int cantidad;

    public Ranura(Item item) {
        this.item = item;
        this.cantidad = 1;
    }

    public Ranura(Item item, int cantidad) {
        this.item = item;
        this.cantidad = cantidad;
    }

    public Item getItem() { return item; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public void incrementar(int cantidad) {
        this.cantidad += cantidad;
    }

    public void decrementar(int cantidad) {
        this.cantidad -= cantidad;
        if (this.cantidad < 0) this.cantidad = 0;
    }
}
