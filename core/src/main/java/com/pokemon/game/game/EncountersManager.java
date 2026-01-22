package com.pokemon.game.game;

import com.pokemon.game.pokemon.*;

/**
 * Gestiona los encuentros aleatorios con Pokémon salvajes durante la exploración
 * del mundo del juego, controlando la frecuencia y probabilidad de aparición
 * según el área y nivel del jugador.
 */
public class EncountersManager {

    /** Tiempo mínimo en segundos que debe transcurrir entre dos encuentros consecutivos */
    private static final float TIEMPO_MINIMO_ENTRE_ENCUENTROS = 15.0f;

    /** Probabilidad base por frame de que ocurra un encuentro cuando se cumplen las condiciones */
    private static final float PROBABILIDAD_BASE = 0.01f;

    /** Acumulador del tiempo transcurrido desde el último encuentro ocurrido */
    private float tiempoDesdeUltimoEncuentro;

    /** Bandera que indica si el sistema puede generar nuevos encuentros o está en enfriamiento */
    private boolean puedeEncontrarPokemon;

    /**
     * Inicializa el sistema de encuentros restableciendo el temporizador
     * y permitiendo encuentros desde el inicio.
     */
    public EncountersManager() {
        tiempoDesdeUltimoEncuentro = 0;
        puedeEncontrarPokemon = true;
    }

    /**
     * Actualiza el estado interno del administrador de encuentros, controlando
     * el período de enfriamiento entre posibles encuentros.
     *
     * @param deltaTime Tiempo transcurrido en segundos desde la última actualización
     */
    public void update(float deltaTime) {
        if (!puedeEncontrarPokemon) {
            tiempoDesdeUltimoEncuentro += deltaTime;

            if (tiempoDesdeUltimoEncuentro >= TIEMPO_MINIMO_ENTRE_ENCUENTROS) {
                puedeEncontrarPokemon = true;
                tiempoDesdeUltimoEncuentro = 0;
            }
        }
    }

    /**
     * Verifica si debe ocurrir un encuentro con Pokémon salvaje basándose en
     * el área actual, nivel del jugador, movimiento y condiciones de enfriamiento.
     *
     * @param zona Identificador del área actual para determinar Pokémon disponibles
     * @param nivelJugador Nivel actual del jugador para ajustar el nivel del Pokémon encontrado
     * @param isMoving Indica si el jugador se está moviendo (no ocurren encuentros estando quieto)
     * @return Instancia de Pokémon salvaje generada o null si no ocurre encuentro
     */
    public PokemonSalvaje checkEncounter(String zona, int nivelJugador, boolean isMoving) {
        if (!puedeEncontrarPokemon || !isMoving) {
            return null;
        }

        float random = (float) Math.random();
        if (random > PROBABILIDAD_BASE) {
            return null;
        }

        PokemonSalvaje encontrado = FabricaPokemon.generarEncuentroAleatorio(zona, nivelJugador);

        if (encontrado != null) {
            puedeEncontrarPokemon = false;
            tiempoDesdeUltimoEncuentro = 0;

            // Mensaje claro para el jugador
            System.out.println("¡" + encontrado.getNombre() + " salvaje apareció! (Nv. " + encontrado.getNivel() + ")");
        }

        return encontrado;
    }

    /**
     * Reinicia inmediatamente el período de enfriamiento, permitiendo que
     * ocurran nuevos encuentros sin esperar el tiempo mínimo establecido.
     */
    public void resetEncounterCooldown() {
        puedeEncontrarPokemon = true;
        tiempoDesdeUltimoEncuentro = 0;
    }

    /**
     * Indica si el sistema está actualmente en condiciones de generar encuentros.
     *
     * @return true si pueden ocurrir encuentros, false si está en período de enfriamiento
     */
    public boolean canEncounter() {
        return puedeEncontrarPokemon;
    }

    /**
     * Calcula el tiempo restante hasta que el sistema permita nuevos encuentros.
     *
     * @return Tiempo en segundos que falta para que termine el enfriamiento,
     *         0 si ya pueden ocurrir encuentros
     */
    public float getTimeUntilNextEncounter() {
        if (puedeEncontrarPokemon) {
            return 0;
        }
        return Math.max(0, TIEMPO_MINIMO_ENTRE_ENCUENTROS - tiempoDesdeUltimoEncuentro);
    }
}
