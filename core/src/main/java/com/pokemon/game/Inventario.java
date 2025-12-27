package com.pokemon.game;

import java.util.ArrayList;
import java.util.List;

public class Inventario {
    private int capacidadMaxima;
    private List<Ranura> slots;

    public Inventario(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
        this.slots = new ArrayList<>();
    }

    public boolean agregarItem(Item item) {
        return agregarItem(item, 1);
    }

    public boolean agregarItem(Item item, int cantidad) {
        // Buscar si ya existe el item
        for (Ranura slot : slots) {
            if (slot.getItem().getNombre().equals(item.getNombre())) {
                slot.incrementar(cantidad);
                return true;
            }
        }

        // Si no existe y hay espacio
        if (slots.size() < capacidadMaxima) {
            slots.add(new Ranura(item, cantidad));
            return true;
        }

        return false; // No hay espacio
    }

    public boolean removerItem(String nombreItem, int cantidad) {
        for (int i = 0; i < slots.size(); i++) {
            Ranura slot = slots.get(i);
            if (slot.getItem().getNombre().equals(nombreItem)) {
                slot.decrementar(cantidad);
                if (slot.getCantidad() <= 0) {
                    slots.remove(i);
                }
                return true;
            }
        }
        return false;
    }

    public Ranura buscarItem(String nombreItem) {
        for (Ranura slot : slots) {
            if (slot.getItem().getNombre().equals(nombreItem)) {
                return slot;
            }
        }
        return null;
    }

    public boolean verificarEspacio() {
        return slots.size() < capacidadMaxima;
    }

    public int getCantidadItems() {
        return slots.size();
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public List<Ranura> getRanuras() {
        return new ArrayList<>(slots); // Copia para no modificar la original
    }

    public int getCantidadTotal() {
        int total = 0;
        for (Ranura slot : slots) {
            total += slot.getCantidad();
        }
        return total;
    }
}
