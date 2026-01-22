package com.pokemon.game.pokedex;

import java.util.*;

/**
 * Gestiona todas las entradas de la Pokédex y coordina las operaciones de registro.
 * Controla el progreso global del jugador y los requisitos para encuentros especiales.
 */
public class PokedexManager {
    private Map<String, PokedexEntry> registros;
    private List<String> especiesCompletamenteInvestigadas;

    /**
     * Crea un gestor de Pokédex vacío sin registros previos.
     * Inicializa todas las estructuras de datos internas.
     */
    public PokedexManager() {
        this.registros = new HashMap<>();
        this.especiesCompletamenteInvestigadas = new ArrayList<>();
    }

    /**
     * Registra un Pokémon inicial recibido al comenzar la aventura.
     * Establece los registros básicos sin incrementar el nivel de investigación.
     */
    public void registrarPokemonInicial(String especie) {
        PokedexEntry entrada = obtenerEntrada(especie);
        entrada.registrarInicial();
        System.out.println("[Pokédex] Inicial registrado: " + especie + " (Inv: 0/10)");
    }

    /**
     * Registra un avistamiento de Pokémon al iniciar un combate.
     * Actualiza la ubicación y fecha del primer avistamiento si corresponde.
     */
    public void registrarAvistamiento(String especie, String ubicacion) {
        PokedexEntry entrada = obtenerEntrada(especie);
        entrada.registrarAvistamiento(ubicacion);
        verificarInvestigacionCompleta(entrada);
    }

    /**
     * Registra una victoria en combate contra un Pokémon.
     * Incrementa el nivel de investigación y verifica si se completa la entrada.
     */
    public void registrarVictoriaCombate(String especie) {
        PokedexEntry entrada = obtenerEntrada(especie);
        entrada.registrarVictoriaCombate();
        verificarInvestigacionCompleta(entrada);
        System.out.println("[Pokédex] Victoria vs " + especie +
            " -> Nivel investigación: " + entrada.getNivelInvestigacion() + "/10");
    }

    /**
     * Registra una captura exitosa de un Pokémon.
     * Establece los registros de primera captura e incrementa significativamente la investigación.
     */
    public void registrarCaptura(String especie, String ubicacion) {
        PokedexEntry entrada = obtenerEntrada(especie);
        entrada.registrarCaptura(ubicacion);
        verificarInvestigacionCompleta(entrada);
        System.out.println("[Pokédex] ¡Capturado " + especie +
            "! -> Nivel investigación: " + entrada.getNivelInvestigacion() + "/10");
    }

    /**
     * Obtiene la entrada completa de la Pokédex para una especie específica.
     * Devuelve null si la especie no ha sido registrada de ninguna forma.
     */
    public PokedexEntry getEntrada(String especie) {
        return registros.get(especie);
    }

    /**
     * Verifica si una especie específica ha sido completamente investigada (nivel 10).
     */
    public boolean estaCompletamenteInvestigado(String especie) {
        PokedexEntry entrada = registros.get(especie);
        return entrada != null && entrada.estaCompletamenteInvestigado();
    }

    /**
     * Determina si ya se cumplen los requisitos para encontrar Pokémon legendarios.
     * Requiere al menos 5 especies completamente investigadas.
     */
    public boolean puedeAparecerLegendario() {
        return getCantidadEspeciesCompletamenteInvestigadas() >= 5;
    }

    /**
     * Obtiene el número total de especies completamente investigadas.
     */
    public int getCantidadEspeciesCompletamenteInvestigadas() {
        return especiesCompletamenteInvestigadas.size();
    }

    /**
     * Calcula el número total de especies diferentes que han sido avistadas al menos una vez.
     */
    public int getTotalEspeciesVistas() {
        int count = 0;
        for (PokedexEntry entrada : registros.values()) {
            if (entrada.isVisto()) count++;
        }
        return count;
    }

    /**
     * Calcula el número total de especies diferentes que han sido capturadas al menos una vez.
     */
    public int getTotalEspeciesCapturadas() {
        int count = 0;
        for (PokedexEntry entrada : registros.values()) {
            if (entrada.isCapturado()) count++;
        }
        return count;
    }

    /**
     * Obtiene todas las entradas de la Pokédex ordenadas alfabéticamente por especie.
     * Útil para interfaces de usuario que muestran la lista completa.
     */
    public List<PokedexEntry> getEntradasOrdenadas() {
        List<PokedexEntry> lista = new ArrayList<>(registros.values());
        lista.sort((a, b) -> a.getEspecie().compareTo(b.getEspecie()));
        return lista;
    }

    /**
     * Genera un resumen estadístico del progreso global en la Pokédex.
     * Incluye conteos de especies vistas, capturadas y completamente investigadas.
     */
    public String getResumen() {
        return String.format("Vistos: %d | Capturados: %d | Completados: %d/5",
            getTotalEspeciesVistas(),
            getTotalEspeciesCapturadas(),
            getCantidadEspeciesCompletamenteInvestigadas());
    }

    /**
     * Obtiene o crea una entrada de Pokédex para la especie especificada.
     * Si no existe previamente, crea una nueva entrada con valores iniciales.
     */
    private PokedexEntry obtenerEntrada(String especie) {
        PokedexEntry entrada = registros.get(especie);
        if (entrada == null) {
            entrada = new PokedexEntry(especie);
            registros.put(especie, entrada);
        }
        return entrada;
    }

    /**
     * Verifica si una entrada alcanzó el nivel de investigación completo.
     * Actualiza la lista de especies completadas y notifica al jugador.
     */
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

    /**
     * Obtiene una copia de todos los registros actuales de la Pokédex.
     * Útil para sistemas de guardado y carga de partidas.
     */
    public Map<String, PokedexEntry> getRegistros() {
        return new HashMap<>(registros);
    }

    /**
     * Restaura los registros de la Pokédex desde datos guardados.
     * Reconstruye automáticamente la lista de especies completamente investigadas.
     */
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
