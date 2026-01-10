package com.pokemon.game.player;

import com.pokemon.game.item.Item;

import java.util.ArrayList;
import java.util.List;

public class Inventario {
    private int capacidadMaxima;  // Ahora = máximo de ÍTEMS totales (no slots)
    private List<Ranura> slots;
    private int cantidadTotal;    // Para llevar conteo rápido

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

        // 2. Buscar si ya existe el ítem para stackear
        for (Ranura slot : slots) {
            if (slot.getItem().getNombre().equals(item.getNombre())) {
                slot.incrementar(cantidad);
                cantidadTotal += cantidad;
                return true;
            }
        }

        // 3. Si no existe y hay espacio total, crear nueva ranura
        // NOTA: Aún necesitamos verificar que no excedamos slots infinitos
        // Pero como slots no tiene límite técnico, solo verificamos cantidadTotal
        slots.add(new Ranura(item, cantidad));
        cantidadTotal += cantidad;
        return true;
    }

    // Reemplaza o ajusta en Inventario.java
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
        for (Ranura slot : slots) {
            if (slot.getItem().getNombre().equals(nombreItem)) {
                return slot;
            }
        }
        return null;
    }

    // MÉTODO MEJORADO: Verificar espacio para X cantidad
    public boolean verificarEspacio() {
        return cantidadTotal < capacidadMaxima;
    }

    public boolean hayEspacioPara(int cantidad) {
        return (cantidadTotal + cantidad) <= capacidadMaxima;
    }

    // MÉTODO NUEVO: Espacio disponible
    public int getEspacioDisponible() {
        return capacidadMaxima - cantidadTotal;
    }

    // Para compatibilidad: mantengo nombre pero ahora cuenta slots
    public int getCantidadItems() {
        return slots.size();
    }

    // MÉTODO NUEVO: Obtener cantidad de un ítem específico
    public int getCantidadDeItem(String nombreItem) {
        Ranura slot = buscarItem(nombreItem);
        return slot != null ? slot.getCantidad() : 0;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public List<Ranura> getRanuras() {
        return new ArrayList<>(slots); // Copia para no modificar la original
    }

    // AHORA ESTE MÉTODO ES CLAVE: Devuelve TOTAL de ítems
    public int getCantidadTotal() {
        return cantidadTotal;
    }

    // Método auxiliar para verificar si tiene cierta cantidad de un ítem
    public boolean tieneItem(String nombreItem, int cantidadMinima) {
        int cantidad = getCantidadDeItem(nombreItem);
        return cantidad >= cantidadMinima;
    }
}
