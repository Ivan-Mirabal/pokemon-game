package com.pokemon.game.pokemon;

/**
 * Representa los tipos elementales de Pokémon y sus relaciones de efectividad en combate.
 * Cada tipo tiene ventajas y desventajas contra otros tipos, afectando el daño de los movimientos.
 */
public enum Tipo {
    NORMAL,
    FUEGO,
    AGUA,
    PLANTA,
    ELECTRICO,
    PSIQUICO,
    LUCHA,
    VOLADOR,
    TIERRA,
    ROCA,
    BICHO,
    VENENO,
    HIELO,
    FANTASMA,
    DRAGON,
    ACERO,
    HADA,
    SINIESTRO;

    /**
     * Calcula el multiplicador de daño de este tipo contra el tipo defensor.
     * La efectividad se basa en las fortalezas y debilidades tradicionales de Pokémon.
     *
     * @param defensor el tipo del Pokémon que recibe el ataque
     * @return 2.0 si es súper efectivo, 0.5 si no es muy efectivo, 0.0 si no afecta, 1.0 si es normal
     */
    public double getMultiplicadorContra(Tipo defensor) {
        // Fuego
        if (this == FUEGO) {
            if (defensor == PLANTA || defensor == BICHO || defensor == HIELO) return 2.0;
            if (defensor == FUEGO || defensor == AGUA || defensor == ROCA || defensor == DRAGON) return 0.5;
            return 1.0;
        }
        // Agua
        if (this == AGUA) {
            if (defensor == FUEGO || defensor == ROCA || defensor == TIERRA) return 2.0;
            if (defensor == AGUA || defensor == PLANTA || defensor == DRAGON) return 0.5;
            return 1.0;
        }
        // Planta
        if (this == PLANTA) {
            if (defensor == AGUA || defensor == ROCA || defensor == TIERRA) return 2.0;
            if (defensor == FUEGO || defensor == PLANTA || defensor == VENENO ||
                defensor == VOLADOR || defensor == BICHO || defensor == DRAGON) return 0.5;
            return 1.0;
        }
        // Eléctrico
        if (this == ELECTRICO) {
            if (defensor == AGUA || defensor == VOLADOR) return 2.0;
            if (defensor == PLANTA || defensor == ELECTRICO || defensor == DRAGON) return 0.5;
            if (defensor == TIERRA) return 0.0;
            return 1.0;
        }
        // Normal
        if (this == NORMAL) {
            if (defensor == ROCA) return 0.5;
            if (defensor == FANTASMA) return 0.0;
            return 1.0;
        }
        // Por defecto
        return 1.0;
    }
}
