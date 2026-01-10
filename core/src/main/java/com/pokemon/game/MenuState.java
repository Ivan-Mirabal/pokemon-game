package com.pokemon.game;

public enum MenuState {
    NONE,           // Sin menú
    MAIN,           // Menú principal
    POKEMON_TEAM,   // Equipo Pokémon
    POKEMON_DETAIL, // Detalle de Pokémon individual
    INVENTORY,      // Inventario
    POKEDEX,        // Lista de Pokédex ← YA EXISTE
    POKEDEX_DETAIL, // Detalle de entrada Pokédex ← NUEVO
    CRAFTING,       // Crafteo
    SAVE,           // Guardar
    OPTIONS,        // Opciones
    COMBATE,
    ITEM_SELECTED,       // Cuando seleccionas un item para usar
    POKEMON_SELECT_FOR_ITEM, // Cuando estás seleccionando un Pokémon para usar un item
}
