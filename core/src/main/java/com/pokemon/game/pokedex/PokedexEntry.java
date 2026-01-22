package com.pokemon.game.pokedex;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa una entrada individual en la Pokédex para una especie específica de Pokémon.
 * Registra información detallada sobre avistamientos, capturas y progreso de investigación.
 */
public class PokedexEntry {
    private String especie;
    private boolean visto;
    private boolean capturado;
    private int nivelInvestigacion; // 0-10
    private int vecesVisto;
    private int vecesCapturado;
    private String primerAvistamientoUbicacion;
    private String primerAvistamientoFecha;
    private String primeraCapturaUbicacion;
    private String primeraCapturaFecha;

    /**
     * Crea una entrada vacía de Pokédex sin datos iniciales.
     */
    public PokedexEntry() {
    }

    /**
     * Crea una nueva entrada de Pokédex para una especie específica.
     * Inicializa todos los registros como vacíos y el nivel de investigación en cero.
     */
    public PokedexEntry(String especie) {
        this.especie = especie;
        this.visto = false;
        this.capturado = false;
        this.nivelInvestigacion = 0;
        this.vecesVisto = 0;
        this.vecesCapturado = 0;
        this.primerAvistamientoUbicacion = null;
        this.primerAvistamientoFecha = null;
        this.primeraCapturaUbicacion = null;
        this.primeraCapturaFecha = null;
    }

    /**
     * Registra el Pokémon inicial recibido al comenzar la aventura.
     * Marca como visto y capturado, pero no incrementa el nivel de investigación.
     */
    public void registrarInicial() {
        if (!visto) {
            visto = true;
            primerAvistamientoUbicacion = "Regalo Inicial";
            primerAvistamientoFecha = obtenerFechaActual();
            vecesVisto = 1;
        }
        if (!capturado) {
            capturado = true;
            primeraCapturaUbicacion = "Regalo Inicial";
            primeraCapturaFecha = obtenerFechaActual();
            vecesCapturado = 1;
        }
        // IMPORTANTE: No llamamos a incrementarNivelInvestigacion
        // El nivel se mantiene en 0
    }

    /**
     * Registra un avistamiento del Pokémon en la ubicación especificada.
     * Actualiza los contadores y establece la primera vez que fue visto.
     */
    public void registrarAvistamiento(String ubicacion) {
        if (!visto) {
            visto = true;
            primerAvistamientoUbicacion = ubicacion;
            primerAvistamientoFecha = obtenerFechaActual();
        }
        vecesVisto++;
    }

    /**
     * Registra una victoria en combate contra este Pokémon.
     * Incrementa el nivel de investigación, excepto para Arceus que se completa automáticamente.
     */
    public void registrarVictoriaCombate() {
        // SI ES ARCEUS, NIVEL 10 INMEDIATO (incluso si solo ganas)
        if (this.especie.equalsIgnoreCase("Arceus")) {
            nivelInvestigacion = 10;
            System.out.println("¡Has derrotado a ARCEUS! Registro completo.");
        } else {
            incrementarNivelInvestigacion(1);
        }
    }

    /**
     * Registra una captura exitosa del Pokémon en la ubicación especificada.
     * Actualiza los contadores y aumenta significativamente el nivel de investigación.
     */
    public void registrarCaptura(String ubicacion) {
        if (!capturado) {
            capturado = true;
            primeraCapturaUbicacion = ubicacion;
            primeraCapturaFecha = obtenerFechaActual();
        }
        vecesCapturado++;

        // SI ES ARCEUS, NIVEL 10 INMEDIATO
        if (this.especie.equalsIgnoreCase("Arceus")) {
            nivelInvestigacion = 10;
            System.out.println("¡ARCEUS registrado completamente en la Pokédex!");
        } else {
            incrementarNivelInvestigacion(2);
        }
    }

    /**
     * Incrementa el nivel de investigación asegurando que no exceda el máximo de 10.
     */
    private void incrementarNivelInvestigacion(int puntos) {
        nivelInvestigacion = Math.min(10, nivelInvestigacion + puntos);
    }

    /**
     * Genera la fecha y hora actual en formato legible.
     */
    private String obtenerFechaActual() {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return ahora.format(formatter);
    }

    /**
     * Obtiene el nombre de la especie de Pokémon de esta entrada.
     */
    public String getEspecie() { return especie; }

    /**
     * Indica si el Pokémon ha sido visto al menos una vez.
     */
    public boolean isVisto() { return visto; }

    /**
     * Indica si el Pokémon ha sido capturado al menos una vez.
     */
    public boolean isCapturado() { return capturado; }

    /**
     * Obtiene el nivel de investigación actual (0-10).
     */
    public int getNivelInvestigacion() { return nivelInvestigacion; }

    /**
     * Obtiene el número total de veces que se ha visto este Pokémon.
     */
    public int getVecesVisto() { return vecesVisto; }

    /**
     * Obtiene el número total de veces que se ha capturado este Pokémon.
     */
    public int getVecesCapturado() { return vecesCapturado; }

    /**
     * Obtiene la ubicación del primer avistamiento registrado.
     */
    public String getPrimerAvistamientoUbicacion() { return primerAvistamientoUbicacion; }

    /**
     * Obtiene la fecha y hora del primer avistamiento registrado.
     */
    public String getPrimerAvistamientoFecha() { return primerAvistamientoFecha; }

    /**
     * Obtiene la ubicación de la primera captura registrada.
     */
    public String getPrimeraCapturaUbicacion() { return primeraCapturaUbicacion; }

    /**
     * Obtiene la fecha y hora de la primera captura registrada.
     */
    public String getPrimeraCapturaFecha() { return primeraCapturaFecha; }

    /**
     * Indica si el Pokémon ha sido completamente investigado (nivel 10).
     */
    public boolean estaCompletamenteInvestigado() {
        return nivelInvestigacion >= 10;
    }

    /**
     * Calcula el progreso de investigación como valor entre 0.0 y 1.0.
     */
    public float getProgresoInvestigacion() {
        return nivelInvestigacion / 10.0f;
    }

    /**
     * Devuelve una representación resumida de la entrada de la Pokédex.
     * Incluye especie, nivel de investigación y estado de visto/capturado.
     */
    @Override
    public String toString() {
        return String.format("%s - Nivel: %d/10 %s %s",
            especie, nivelInvestigacion,
            visto ? "(Visto)" : "",
            capturado ? "(Capturado)" : "");
    }
}
