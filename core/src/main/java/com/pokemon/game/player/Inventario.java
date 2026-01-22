package com.pokemon.game.player;

import com.pokemon.game.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona el almacenamiento de ítems del jugador con capacidad limitada.
 * Permite agregar, remover y buscar ítems organizados en ranuras apilables.
 */
public class Inventario {
    private int capacidadMaxima;
    private List<Ranura> slots;
    private int cantidadTotal;

    /**
     * Crea un inventario con la capacidad máxima especificada.
     * Inicializa todas las estructuras internas como vacías.
     */
    public Inventario(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
        this.slots = new ArrayList<>();
        this.cantidadTotal = 0;
    }

    /**
     * Agrega una unidad del ítem especificado al inventario.
     * Busca ranuras existentes para apilar antes de crear una nueva.
     */
    public boolean agregarItem(Item item) {
        return agregarItem(item, 1);
    }

    /**
     * Agrega múltiples unidades del mismo ítem al inventario.
     * Valida que haya espacio suficiente antes de realizar la operación.
     */
    public boolean agregarItem(Item item, int cantidad) {
        // 1. Verificar si hay espacio TOTAL
        if (cantidadTotal + cantidad > capacidadMaxima) {
            return false; // Inventario lleno
        }

        // 2. Buscar si ya existe el ítem para stackear (usando equalsIgnoreCase)
        for (Ranura slot : slots) {
            if (slot.getItem().getNombre().equalsIgnoreCase(item.getNombre())) {
                slot.incrementar(cantidad);
                cantidadTotal += cantidad;
                return true;
            }
        }

        // 3. Si no existe, crear nueva ranura
        slots.add(new Ranura(item, cantidad));
        cantidadTotal += cantidad;
        return true;
    }

    /**
     * Elimina una cantidad específica de ítems del inventario por nombre.
     * Remueve completamente las ranuras que quedan vacías después de la operación.
     */
    public boolean removerItem(String nombreItem, int cantidad) {
        Ranura slot = buscarItem(nombreItem);
        if (slot != null && slot.getCantidad() >= cantidad) {
            slot.decrementar(cantidad);
            this.cantidadTotal -= cantidad;

            // Si no quedan unidades, eliminamos la ranura de la lista
            if (slot.getCantidad() <= 0) {
                slots.remove(slot);
            }
            return true;
        }
        return false;
    }

    /**
     * Busca una ranura que contenga el ítem especificado por nombre.
     * La búsqueda no distingue entre mayúsculas y minúsculas.
     */
    public Ranura buscarItem(String nombreItem) {
        if (nombreItem == null) return null;

        for (Ranura slot : slots) {
            if (slot.getItem().getNombre().equalsIgnoreCase(nombreItem)) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Obtiene la cantidad máxima de ítems que el inventario puede almacenar.
     */
    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    /**
     * Devuelve una lista de todas las ranuras que contienen ítems.
     * La lista es una copia para proteger la integridad interna del inventario.
     */
    public List<Ranura> getRanuras() {
        return new ArrayList<>(slots);
    }

    /**
     * Obtiene el número total de ítems almacenados en todas las ranuras.
     */
    public int getCantidadTotal() {
        return cantidadTotal;
    }

    /**
     * Elimina todos los ítems del inventario y restablece el contador total.
     */
    public void vaciarInventario() {
        this.slots.clear();
        this.cantidadTotal = 0;
    }
}
