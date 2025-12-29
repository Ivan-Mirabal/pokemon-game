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

    // MÉTODO CORREGIDO: ¡Ahora SÍ descuenta!
    public void usar() {
        if (cantidad > 0) {
            item.usar();            // Muestra mensaje
            decrementar(1);         // ¡ESTO ES LO IMPORTANTE!
            System.out.println("Quedan: " + cantidad);

            // Si queda 0, avisar
            if (cantidad == 0) {
                System.out.println("¡Te quedaste sin " + item.getNombre() + "!");
            }
        } else {
            System.out.println("No puedes usar " + item.getNombre() + ", no tienes.");
        }
    }

    // MÉTODO NUEVO: Usar cantidad específica (para crafteo)
    public boolean usarCantidad(int cantidadUsar) {
        if (cantidad >= cantidadUsar) {
            decrementar(cantidadUsar);
            return true;
        }
        return false;
    }
}
