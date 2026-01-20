package com.pokemon.game.player;

import com.pokemon.game.item.Item;

import java.util.ArrayList;
import java.util.List;

public class Inventario {
    private int capacidadMaxima;
    private List<Ranura> slots;
    private int cantidadTotal;

    public Inventario(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
        this.slots = new ArrayList<>();
        this.cantidadTotal = 0;
    }

    public boolean agregarItem(Item item) {
        return agregarItem(item, 1);
    }

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

    public Ranura buscarItem(String nombreItem) {
        if (nombreItem == null) return null;

        for (Ranura slot : slots) {
            if (slot.getItem().getNombre().equalsIgnoreCase(nombreItem)) {
                return slot;
            }
        }
        return null;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public List<Ranura> getRanuras() {
        return new ArrayList<>(slots);
    }

    // Devuelve TOTAL de ítems
    public int getCantidadTotal() {
        return cantidadTotal;
    }

    public void vaciarInventario() {
        this.slots.clear();
        this.cantidadTotal = 0;
    }
}
