package com.pokemon.game.player;

import com.pokemon.game.item.Item;

/**
 * Representa una ranura de inventario que almacena un tipo específico de ítem y su cantidad.
 * Permite gestionar el uso y consumo de ítems manteniendo un registro de las unidades disponibles.
 */
public class Ranura {
    private Item item;
    private int cantidad;

    /**
     * Crea una ranura con el ítem especificado y una cantidad inicial de una unidad.
     */
    public Ranura(Item item) {
        this.item = item;
        this.cantidad = 1;
    }

    /**
     * Crea una ranura con el ítem y la cantidad inicial especificados.
     */
    public Ranura(Item item, int cantidad) {
        this.item = item;
        this.cantidad = cantidad;
    }

    /**
     * Obtiene el ítem almacenado en esta ranura.
     */
    public Item getItem() { return item; }

    /**
     * Obtiene la cantidad actual del ítem en esta ranura.
     */
    public int getCantidad() { return cantidad; }

    /**
     * Establece una nueva cantidad para el ítem en esta ranura.
     * Reemplaza cualquier valor anterior sin validaciones adicionales.
     */
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    /**
     * Incrementa la cantidad del ítem en esta ranura.
     * Agrega la cantidad especificada al total actual.
     */
    public void incrementar(int cantidad) {
        this.cantidad += cantidad;
    }

    /**
     * Disminuye la cantidad del ítem en esta ranura.
     * Asegura que la cantidad nunca sea negativa, ajustando a cero si es necesario.
     */
    public void decrementar(int cantidad) {
        this.cantidad -= cantidad;
        if (this.cantidad < 0) this.cantidad = 0;
    }

    /**
     * Utiliza una unidad del ítem en esta ranura.
     * Ejecuta el efecto del ítem y reduce la cantidad en una unidad.
     * Muestra mensajes informativos sobre el estado restante.
     */
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

    /**
     * Utiliza una cantidad específica de unidades del ítem en esta ranura.
     * Diseñado para operaciones que requieren múltiples unidades, como crafteo.
     * No ejecuta el efecto del ítem, solo reduce la cantidad disponible.
     */
    public void usarCantidad(int cantidadUsar) {
        if (cantidad >= cantidadUsar) {
            decrementar(cantidadUsar);
        }
    }
}
