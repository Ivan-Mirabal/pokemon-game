package com.pokemon.game.pokemon;

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

    // Tabla de efectividad simplificada (sólo algunos tipos para ejemplo)
    // 2.0 = súper efectivo, 0.5 = no muy efectivo, 0.0 = no afecta
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
