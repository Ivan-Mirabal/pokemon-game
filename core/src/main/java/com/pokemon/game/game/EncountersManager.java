package com.pokemon.game.game;

import com.pokemon.game.pokemon.*;

public class EncountersManager {
    // En EncountersManager.java
    private static final float TIEMPO_MINIMO_ENTRE_ENCUENTROS = 3.0f; // 3 segundos es suficiente
    private static final float PROBABILIDAD_BASE = 0.3f; // 30% es razonable

    private float tiempoDesdeUltimoEncuentro;
    private boolean puedeEncontrarPokemon;

    public EncountersManager() {
        tiempoDesdeUltimoEncuentro = 0;
        puedeEncontrarPokemon = true;
    }

    public void update(float deltaTime) {
        if (!puedeEncontrarPokemon) {
            tiempoDesdeUltimoEncuentro += deltaTime;
            System.out.println("[EncountersManager] Cooldown: " + tiempoDesdeUltimoEncuentro +
                "/" + TIEMPO_MINIMO_ENTRE_ENCUENTROS);

            if (tiempoDesdeUltimoEncuentro >= TIEMPO_MINIMO_ENTRE_ENCUENTROS) {
                puedeEncontrarPokemon = true;
                tiempoDesdeUltimoEncuentro = 0;
                System.out.println("[EncountersManager] Cooldown terminado, listo para nuevo encuentro");
            }
        }
    }

    public PokemonSalvaje checkEncounter(String zona, int nivelJugador, boolean isMoving) {
        if (!puedeEncontrarPokemon || !isMoving) {
            return null;
        }

        float random = (float) Math.random();
        if (random > PROBABILIDAD_BASE) {
            return null;
        }

        // Solo este mensaje es útil
        System.out.println("[Encounters] ¡Encuentro generado!");

        PokemonSalvaje encontrado = FabricaPokemon.generarEncuentroAleatorio(zona, nivelJugador);

        if (encontrado != null) {
            puedeEncontrarPokemon = false;
            tiempoDesdeUltimoEncuentro = 0;

            // Mensaje claro para el jugador
            System.out.println("¡" + encontrado.getNombre() + " salvaje apareció! (Nv. " + encontrado.getNivel() + ")");
        }

        return encontrado;
    }

    public void resetEncounterCooldown() {
        puedeEncontrarPokemon = true;
        tiempoDesdeUltimoEncuentro = 0;
    }

    public boolean canEncounter() {
        return puedeEncontrarPokemon;
    }

    public float getTimeUntilNextEncounter() {
        if (puedeEncontrarPokemon) {
            return 0;
        }
        return Math.max(0, TIEMPO_MINIMO_ENTRE_ENCUENTROS - tiempoDesdeUltimoEncuentro);
    }
}
