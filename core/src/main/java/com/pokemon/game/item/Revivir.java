package com.pokemon.game.item;

/**
 * Representa un objeto de revivir que puede resucitar a un Pokémon debilitado
 * durante el combate o fuera de él, restaurando un porcentaje específico de
 * sus puntos de salud máximos para permitirle continuar luchando.
 */
public class Revivir extends Item {

    /** Porcentaje de los puntos de salud máximos que se recuperan al usar este objeto */
    private int porcentajeRecuperacion;

    /**
     * Construye un nuevo objeto de revivir con el nombre y porcentaje de recuperación especificados.
     *
     * @param nombre Nombre identificativo del objeto (ej: "Revivir", "Revivir Máximo")
     * @param porcentajeRecuperacion Porcentaje de PS máximos a restaurar (1-100)
     */
    public Revivir(String nombre, int porcentajeRecuperacion) {
        super(nombre, "Revive a un Pokémon");
        this.porcentajeRecuperacion = porcentajeRecuperacion;
    }

    /**
     * Obtiene el porcentaje de puntos de salud máximos que este objeto de revivir
     * puede restaurar a un Pokémon debilitado.
     *
     * @return Porcentaje de recuperación de PS (1-100)
     */
    public int getPorcentajeRecuperacion() {
        return porcentajeRecuperacion;
    }

    /**
     * Simula el uso del objeto de revivir, mostrando un mensaje en la consola.
     * En una implementación completa, este método estaría integrado con el sistema
     * de combate para aplicar el efecto de revivir a un Pokémon específico.
     */
    @Override
    public void usar() {
        System.out.println("Usando " + nombre + "...");
    }
}
