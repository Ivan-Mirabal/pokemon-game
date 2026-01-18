package com.pokemon.game.data;

import com.pokemon.game.pokedex.PokedexManager;
import java.util.ArrayList;
import java.util.List;

public class SaveData {
    // 1. POKÉDEX COMPLETO (ya es serializable)
    private PokedexManager pokedex;

    // 2. EQUIPO (solo datos esenciales)
    private List<PokemonSimple> equipo;

    // 3. INVENTARIO (solo nombres y cantidades)
    private List<ItemSlot> inventario;

    // Constructor vacío para JSON
    public SaveData() {
        this.equipo = new ArrayList<>();
        this.inventario = new ArrayList<>();
    }

    // ============ GETTERS Y SETTERS ============
    public PokedexManager getPokedex() { return pokedex; }
    public void setPokedex(PokedexManager pokedex) { this.pokedex = pokedex; }

    public List<PokemonSimple> getEquipo() { return equipo; }
    public void setEquipo(List<PokemonSimple> equipo) { this.equipo = equipo; }

    public List<ItemSlot> getInventario() { return inventario; }
    public void setInventario(List<ItemSlot> inventario) { this.inventario = inventario; }

    // ============ CLASES INTERNAS ============

    // Pokémon simplificado (solo datos para guardar)
    public static class PokemonSimple {
        private String especie;      // "Pikachu"
        private String apodo;        // "Pika"
        private int nivel;           // 12
        private int psActual;        // 42
        private int psMaximos;       // 45
        private int experiencia;     // 450

        // Constructor vacío para JSON
        public PokemonSimple() {}

        // Constructor completo
        public PokemonSimple(String especie, String apodo, int nivel,
                             int psActual, int psMaximos, int experiencia) {
            this.especie = especie;
            this.apodo = apodo;
            this.nivel = nivel;
            this.psActual = psActual;
            this.psMaximos = psMaximos;
            this.experiencia = experiencia;
        }

        // Getters y Setters
        public String getEspecie() { return especie; }
        public void setEspecie(String especie) { this.especie = especie; }

        public String getApodo() { return apodo; }
        public void setApodo(String apodo) { this.apodo = apodo; }

        public int getNivel() { return nivel; }
        public void setNivel(int nivel) { this.nivel = nivel; }

        public int getPsActual() { return psActual; }
        public void setPsActual(int psActual) { this.psActual = psActual; }

        public int getPsMaximos() { return psMaximos; }
        public void setPsMaximos(int psMaximos) { this.psMaximos = psMaximos; }

        public int getExperiencia() { return experiencia; }
        public void setExperiencia(int experiencia) { this.experiencia = experiencia; }
    }

    // Slot de ítem del inventario
    public static class ItemSlot {
        private String nombreItem;   // "Pokeball", "Poción"
        private int cantidad;        // 5

        // Constructor vacío para JSON
        public ItemSlot() {}

        // Constructor completo
        public ItemSlot(String nombreItem, int cantidad) {
            this.nombreItem = nombreItem;
            this.cantidad = cantidad;
        }

        // Getters y Setters
        public String getNombreItem() { return nombreItem; }
        public void setNombreItem(String nombreItem) { this.nombreItem = nombreItem; }

        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    }
}
