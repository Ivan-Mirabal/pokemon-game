package com.pokemon.game;

public enum MenuState {
    NONE,           // No hay menú visible
    MAIN,           // Menú principal
    INVENTORY,      // Inventario
    INVENTORY_ITEM_SELECTED, // Item seleccionado en inventario
    POKEMON,        // Pokémon
    POKEDEX,        // Pokédex
    CRAFTING,       // Crafteo
    SAVE,           // Guardar partida
    OPTIONS,        // Opciones
    OPTIONS_VOLUME, // Submenú de volumen
    OPTIONS_CONTROLS, // Submenú de controles
    OPTIONS_CREDITS  // Submenú de créditos
}
