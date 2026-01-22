package com.pokemon.game;

/**
 * Representa todos los posibles estados del sistema de menús del juego,
 * controlando qué pantalla o interfaz se muestra al jugador en cada momento.
 * Cada estado corresponde a una pantalla específica con funcionalidades únicas.
 */
public enum MenuState {

    /** Estado por defecto cuando no hay menú activo, el jugador está en el mundo del juego */
    NONE,

    /** Menú principal que ofrece acceso a todas las opciones principales del juego */
    MAIN,

    /** Pantalla que muestra el equipo actual de Pokémon del jugador */
    POKEMON_TEAM,

    /** Pantalla detallada con información específica de un Pokémon individual */
    POKEMON_DETAIL,

    /** Pantalla que muestra todos los ítems en posesión del jugador organizados por categorías */
    INVENTORY,

    /** Pantalla que lista todas las especies de Pokémon registradas en el Pokédex */
    POKEDEX,

    /** Pantalla detallada con información completa de una entrada específica del Pokédex */
    POKEDEX_DETAIL,

    /** Pantalla del sistema de crafteo donde se combinan recursos para crear ítems */
    CRAFTING,

    /** Pantalla de guardado de partida con opciones para guardar o cargar progreso */
    SAVE,

    /** Pantalla de configuración con ajustes de juego, controles y preferencias */
    OPTIONS,

    /** Estado activo durante combates Pokémon con interfaz de selección de acciones */
    COMBATE,

    /** Estado cuando un ítem ha sido seleccionado del inventario para ser usado */
    ITEM_SELECTED,

    /** Estado cuando se está seleccionando un Pokémon específico como objetivo para un ítem */
    POKEMON_SELECT_FOR_ITEM,
}
