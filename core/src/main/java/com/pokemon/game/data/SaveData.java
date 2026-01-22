package com.pokemon.game.data;

import com.pokemon.game.pokedex.PokedexManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Contiene todos los datos del estado actual del juego que deben ser guardados
 * y cargados para permitir la persistencia entre sesiones. Incluye el progreso
 * del Pokédex, el equipo de Pokémon del jugador y los ítems del inventario.
 */
public class SaveData {

    /** Gestor del Pokédex que registra todas las especies encontradas y su nivel de investigación */
    private PokedexManager pokedex;

    /** Lista de los Pokémon que actualmente forman parte del equipo activo del jugador */
    private List<PokemonSimple> equipo;

    /** Lista de los ítems que el jugador ha recolectado y almacenado en su inventario */
    private List<ItemSlot> inventario;

    /**
     * Constructor por defecto que inicializa las estructuras de datos vacías.
     * Utilizado principalmente por el sistema de serialización JSON.
     */
    public SaveData() {
        this.equipo = new ArrayList<>();
        this.inventario = new ArrayList<>();
    }

    // ============ MÉTODOS DE ACCESO ============

    /**
     * Obtiene el gestor del Pokédex que contiene todos los registros de especies.
     *
     * @return Instancia de PokedexManager con el estado actual del Pokédex
     */
    public PokedexManager getPokedex() { return pokedex; }

    /**
     * Establece un nuevo gestor de Pokédex para los datos de guardado.
     *
     * @param pokedex Nuevo gestor de Pokédex a asociar con esta partida
     */
    public void setPokedex(PokedexManager pokedex) { this.pokedex = pokedex; }

    /**
     * Obtiene la lista de Pokémon simplificados que representan el equipo actual.
     *
     * @return Lista de objetos PokemonSimple en el equipo del jugador
     */
    public List<PokemonSimple> getEquipo() { return equipo; }

    /**
     * Reemplaza completamente la composición del equipo del jugador.
     *
     * @param equipo Nueva lista de Pokémon que formarán el equipo
     */
    public void setEquipo(List<PokemonSimple> equipo) { this.equipo = equipo; }

    /**
     * Obtiene la lista de ítems almacenados en el inventario del jugador.
     *
     * @return Lista de objetos ItemSlot representando el inventario
     */
    public List<ItemSlot> getInventario() { return inventario; }

    /**
     * Reemplaza completamente el contenido del inventario del jugador.
     *
     * @param inventario Nueva lista de ítems para el inventario
     */
    public void setInventario(List<ItemSlot> inventario) { this.inventario = inventario; }

    // ============ CLASES INTERNAS ============

    /**
     * Representación simplificada de un Pokémon para propósitos de guardado,
     * conteniendo solo los datos esenciales necesarios para reconstruir el
     * estado del Pokémon al cargar la partida.
     */
    public static class PokemonSimple {

        /** Nombre de la especie del Pokémon (ej: "Pikachu") */
        private String especie;

        /** Apodo personalizado asignado por el jugador (puede estar vacío) */
        private String apodo;

        /** Nivel actual del Pokémon (entre 1 y 100) */
        private int nivel;

        /** Puntos de salud actuales del Pokémon en el momento del guardado */
        private int psActual;

        /** Puntos de salud máximos que el Pokémon puede tener en su nivel actual */
        private int psMaximos;

        /** Puntos de experiencia acumulados hacia el siguiente nivel */
        private int experiencia;

        /**
         * Constructor por defecto requerido para la deserialización JSON.
         */
        public PokemonSimple() {}

        /**
         * Construye un Pokémon simplificado con todos sus atributos definidos.
         *
         * @param especie Nombre de la especie del Pokémon
         * @param apodo Apodo personalizado del Pokémon
         * @param nivel Nivel actual del Pokémon
         * @param psActual Puntos de salud actuales
         * @param psMaximos Puntos de salud máximos
         * @param experiencia Experiencia acumulada
         */
        public PokemonSimple(String especie, String apodo, int nivel,
                             int psActual, int psMaximos, int experiencia) {
            this.especie = especie;
            this.apodo = apodo;
            this.nivel = nivel;
            this.psActual = psActual;
            this.psMaximos = psMaximos;
            this.experiencia = experiencia;
        }

        // Métodos de acceso para todos los atributos

        /**
         * @return Nombre de la especie del Pokémon
         */
        public String getEspecie() { return especie; }

        /**
         * @param especie Nuevo nombre de especie para el Pokémon
         */
        public void setEspecie(String especie) { this.especie = especie; }

        /**
         * @return Apodo personalizado del Pokémon
         */
        public String getApodo() { return apodo; }

        /**
         * @param apodo Nuevo apodo para el Pokémon
         */
        public void setApodo(String apodo) { this.apodo = apodo; }

        /**
         * @return Nivel actual del Pokémon
         */
        public int getNivel() { return nivel; }

        /**
         * @param nivel Nuevo nivel para el Pokémon
         */
        public void setNivel(int nivel) { this.nivel = nivel; }

        /**
         * @return Puntos de salud actuales
         */
        public int getPsActual() { return psActual; }

        /**
         * @param psActual Nuevos puntos de salud actuales
         */
        public void setPsActual(int psActual) { this.psActual = psActual; }

        /**
         * @return Puntos de salud máximos
         */
        public int getPsMaximos() { return psMaximos; }

        /**
         * @param psMaximos Nuevos puntos de salud máximos
         */
        public void setPsMaximos(int psMaximos) { this.psMaximos = psMaximos; }

        /**
         * @return Experiencia acumulada
         */
        public int getExperiencia() { return experiencia; }

        /**
         * @param experiencia Nueva cantidad de experiencia acumulada
         */
        public void setExperiencia(int experiencia) { this.experiencia = experiencia; }

        /**
         * Valida y corrige los datos del Pokémon para asegurar consistencia
         * y prevenir estados inválidos como PS negativos o niveles fuera de rango.
         */
        public void validarDatos() {
            // Garantiza que los PS no sean negativos
            if (psActual < 0) psActual = 0;
            if (psMaximos < 1) psMaximos = 1;

            // Asegura que los PS actuales no excedan el máximo
            if (psActual > psMaximos) psActual = psMaximos;

            // Mantiene el nivel dentro de los límites válidos
            if (nivel < 1) nivel = 1;
            if (nivel > 100) nivel = 100;

            // Previene experiencia negativa
            if (experiencia < 0) experiencia = 0;
        }
    }

    /**
     * Representa un espacio individual en el inventario que contiene un tipo
     * de ítem específico y la cantidad acumulada de ese ítem.
     */
    public static class ItemSlot {

        /** Nombre identificativo del ítem (ej: "Pokeball", "Poción", "Revivir") */
        private String nombreItem;

        /** Cantidad de unidades de este ítem que posee el jugador */
        private int cantidad;

        /**
         * Constructor por defecto requerido para la deserialización JSON.
         */
        public ItemSlot() {}

        /**
         * Construye un espacio de inventario con el ítem y cantidad especificados.
         *
         * @param nombreItem Nombre del ítem almacenado
         * @param cantidad Cantidad de unidades del ítem
         */
        public ItemSlot(String nombreItem, int cantidad) {
            this.nombreItem = nombreItem;
            this.cantidad = cantidad;
        }

        // Métodos de acceso para los atributos del ítem

        /**
         * @return Nombre del ítem almacenado en este espacio
         */
        public String getNombreItem() { return nombreItem; }

        /**
         * @param nombreItem Nuevo nombre para el ítem en este espacio
         */
        public void setNombreItem(String nombreItem) { this.nombreItem = nombreItem; }

        /**
         * @return Cantidad de unidades del ítem
         */
        public int getCantidad() { return cantidad; }

        /**
         * @param cantidad Nueva cantidad de unidades para este ítem
         */
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    }
}
