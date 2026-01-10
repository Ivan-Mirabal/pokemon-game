package com.pokemon.game.pokedex;

import java.util.*;

public class PokedexManager {
    private Map<String, PokedexEntry> registros;
    private List<String> especiesCompletamenteInvestigadas;

    public PokedexManager() {
        this.registros = new HashMap<>();
        this.especiesCompletamenteInvestigadas = new ArrayList<>();
    }

    // Registrar avistamiento (al iniciar combate)
    public void registrarAvistamiento(String especie, String ubicacion) {
        PokedexEntry entrada = obtenerEntrada(especie);
        entrada.registrarAvistamiento(ubicacion);
        verificarInvestigacionCompleta(entrada);
    }

    // Registrar victoria en combate (+1 punto)
    public void registrarVictoriaCombate(String especie) {
        PokedexEntry entrada = obtenerEntrada(especie);
        entrada.registrarVictoriaCombate();
        verificarInvestigacionCompleta(entrada);
        System.out.println("[Pokédex] Victoria vs " + especie +
            " -> Nivel investigación: " + entrada.getNivelInvestigacion() + "/10");
    }

    // Registrar captura exitosa (+2 puntos)
    public void registrarCaptura(String especie, String ubicacion) {
        PokedexEntry entrada = obtenerEntrada(especie);
        entrada.registrarCaptura(ubicacion);
        verificarInvestigacionCompleta(entrada);
        System.out.println("[Pokédex] ¡Capturado " + especie +
            "! -> Nivel investigación: " + entrada.getNivelInvestigacion() + "/10");
    }

    // CONSULTAS
    public PokedexEntry getEntrada(String especie) {
        return registros.get(especie);
    }

    public boolean estaCompletamenteInvestigado(String especie) {
        PokedexEntry entrada = registros.get(especie);
        return entrada != null && entrada.estaCompletamenteInvestigado();
    }

    // Verifica si puede aparecer Pokémon legendario (5+ especies con nivel 10)
    public boolean puedeAparecerLegendario() {
        return getCantidadEspeciesCompletamenteInvestigadas() >= 5;
    }

    public int getCantidadEspeciesCompletamenteInvestigadas() {
        return especiesCompletamenteInvestigadas.size();
    }

    public int getTotalEspeciesVistas() {
        int count = 0;
        for (PokedexEntry entrada : registros.values()) {
            if (entrada.isVisto()) count++;
        }
        return count;
    }

    public int getTotalEspeciesCapturadas() {
        int count = 0;
        for (PokedexEntry entrada : registros.values()) {
            if (entrada.isCapturado()) count++;
        }
        return count;
    }

    // Para la UI: Obtener todas las entradas ordenadas
    public List<PokedexEntry> getEntradasOrdenadas() {
        List<PokedexEntry> lista = new ArrayList<>(registros.values());
        lista.sort((a, b) -> a.getEspecie().compareTo(b.getEspecie()));
        return lista;
    }

    // Estadísticas generales
    public String getResumen() {
        return String.format("Vistos: %d | Capturados: %d | Completados: %d/5",
            getTotalEspeciesVistas(),
            getTotalEspeciesCapturadas(),
            getCantidadEspeciesCompletamenteInvestigadas());
    }

    // MÉTODOS PRIVADOS
    private PokedexEntry obtenerEntrada(String especie) {
        PokedexEntry entrada = registros.get(especie);
        if (entrada == null) {
            entrada = new PokedexEntry(especie);
            registros.put(especie, entrada);
        }
        return entrada;
    }

    private void verificarInvestigacionCompleta(PokedexEntry entrada) {
        if (entrada.estaCompletamenteInvestigado() &&
            !especiesCompletamenteInvestigadas.contains(entrada.getEspecie())) {

            especiesCompletamenteInvestigadas.add(entrada.getEspecie());
            System.out.println("[Pokédex] ¡" + entrada.getEspecie() +
                " completamente investigado! (" +
                especiesCompletamenteInvestigadas.size() + "/5 para legendario)");

            if (puedeAparecerLegendario()) {
                System.out.println("[Pokédex] ¡Ya puedes encontrar Pokémon legendarios!");
            }
        }
    }

    // Para guardado/carga
    public Map<String, PokedexEntry> getRegistros() {
        return new HashMap<>(registros);
    }

    public void setRegistros(Map<String, PokedexEntry> registros) {
        this.registros = new HashMap<>(registros);
        especiesCompletamenteInvestigadas.clear();
        for (PokedexEntry entrada : registros.values()) {
            if (entrada.estaCompletamenteInvestigado()) {
                especiesCompletamenteInvestigadas.add(entrada.getEspecie());
            }
        }
    }
}
