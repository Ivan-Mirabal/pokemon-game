package com.pokemon.game.pokedex;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    // Cuando ves un Pokémon
    public void registrarAvistamiento(String ubicacion) {
        if (!visto) {
            visto = true;
            primerAvistamientoUbicacion = ubicacion;
            primerAvistamientoFecha = obtenerFechaActual();
        }
        vecesVisto++;
    }

    // Victoria en combate (+1 punto)
    public void registrarVictoriaCombate() {
        incrementarNivelInvestigacion(1);
    }

    // Captura exitosa (+2 puntos)
    public void registrarCaptura(String ubicacion) {
        if (!capturado) {
            capturado = true;
            primeraCapturaUbicacion = ubicacion;
            primeraCapturaFecha = obtenerFechaActual();
        }
        vecesCapturado++;
        incrementarNivelInvestigacion(2);
    }

    private void incrementarNivelInvestigacion(int puntos) {
        nivelInvestigacion = Math.min(10, nivelInvestigacion + puntos);
    }

    private String obtenerFechaActual() {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return ahora.format(formatter);
    }

    // GETTERS
    public String getEspecie() { return especie; }
    public boolean isVisto() { return visto; }
    public boolean isCapturado() { return capturado; }
    public int getNivelInvestigacion() { return nivelInvestigacion; }
    public int getVecesVisto() { return vecesVisto; }
    public int getVecesCapturado() { return vecesCapturado; }
    public String getPrimerAvistamientoUbicacion() { return primerAvistamientoUbicacion; }
    public String getPrimerAvistamientoFecha() { return primerAvistamientoFecha; }
    public String getPrimeraCapturaUbicacion() { return primeraCapturaUbicacion; }
    public String getPrimeraCapturaFecha() { return primeraCapturaFecha; }

    public boolean estaCompletamenteInvestigado() {
        return nivelInvestigacion >= 10;
    }

    public float getProgresoInvestigacion() {
        return nivelInvestigacion / 10.0f;
    }

    @Override
    public String toString() {
        return String.format("%s - Nivel: %d/10 %s %s",
            especie, nivelInvestigacion,
            visto ? "(Visto)" : "",
            capturado ? "(Capturado)" : "");
    }
}
