package com.pokemon.game.item;

/**
 * Define los diferentes tipos de acciones que pueden realizar los ítems en el juego.
 * Cada tipo de ítem tiene un contexto de uso específico que determina cuándo y cómo puede ser utilizado.
 */
public enum ItemAction {
    /** El ítem puede usarse directamente sobre un Pokémon, como pociones o revivir. */
    USE_ON_POKEMON,

    /** El ítem solo puede usarse durante un combate, como las Pokéballs. */
    USE_IN_BATTLE,

    /** El ítem es un recurso utilizado para craftear otros objetos. */
    CRAFT,

    /** El ítem no tiene una acción directa de uso, puede ser decorativo o de colección. */
    NONE
}
