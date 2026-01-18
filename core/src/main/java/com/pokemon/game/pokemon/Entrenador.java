package com.pokemon.game.pokemon;

import com.pokemon.game.pokedex.PokedexManager; // NUEVO IMPORT
import com.pokemon.game.player.Inventario;
import java.util.ArrayList;
import java.util.List;

public class Entrenador {
    private String nombre;
    private List<PokemonJugador> equipo; // Máximo 6
    private Pokemon pokemonActual;
    private Inventario inventario;
    private int dinero;
    private int victorias;
    private int derrotas;
    private PokedexManager pokedex; // NUEVO

    public Entrenador(String nombre, Inventario inventario) {
        this.nombre = nombre;
        this.equipo = new ArrayList<>();
        this.inventario = inventario;
        this.dinero = 3000; // Dinero inicial
        this.victorias = 0;
        this.derrotas = 0;
        this.pokemonActual = null;
        this.pokedex = new PokedexManager(); // NUEVO
    }

    // Gestionar equipo
    public boolean agregarPokemon(PokemonJugador pokemon) {
        if (equipo.size() >= 6) {
            System.out.println("El equipo está lleno. Máximo 6 Pokémon.");
            return false;
        }

        // ✅ Asegúrate de que esta línea esté presente:
        pokemon.setEntrenador(this); // Asignar este entrenador como dueño

        equipo.add(pokemon);
        if (pokemonActual == null) {
            pokemonActual = pokemon;
        }
        System.out.println("¡" + pokemon.getNombre() + " se ha unido al equipo!");
        return true;
    }

    public boolean retirarPokemon(int indice) {
        if (indice < 0 || indice >= equipo.size()) {
            return false;
        }

        Pokemon removido = equipo.remove(indice);
        if (pokemonActual == removido) {
            pokemonActual = equipo.isEmpty() ? null : equipo.get(0);
        }
        return true;
    }

    public boolean cambiarPokemon(int indice) {
        if (indice < 0 || indice >= equipo.size()) {
            return false;
        }

        Pokemon nuevo = equipo.get(indice);
        if (nuevo.estaDebilitado()) {
            System.out.println("¡" + nuevo.getNombre() + " está debilitado!");
            return false;
        }

        pokemonActual = nuevo;
        System.out.println("¡Adelante, " + pokemonActual.getNombre() + "!");
        return true;
    }

    public boolean tienePokemonVivos() {
        for (Pokemon p : equipo) {
            if (!p.estaDebilitado()) {
                return true;
            }
        }
        return false;
    }

    public void curarEquipo() {
        for (PokemonJugador p : equipo) {
            p.curarCompletamente();
        }
        System.out.println("¡El equipo ha sido curado completamente!");
    }

    public void restaurarPPEquipo() {
        for (PokemonJugador p : equipo) {
            for (Movimiento m : p.getMovimientos()) {
                m.restaurarTodo();
            }
        }
        System.out.println("¡Los PP de los movimientos han sido restaurados!");
    }

    // Métodos de combate
    public boolean prepararParaCombate() {
        if (!tienePokemonVivos()) {
            System.out.println("¡Todos tus Pokémon están debilitados!");
            return false;
        }

        if (pokemonActual == null || pokemonActual.estaDebilitado()) {
            for (Pokemon p : equipo) {
                if (!p.estaDebilitado()) {
                    pokemonActual = p;
                    break;
                }
            }
        }

        return pokemonActual != null;
    }

    // ===== MÉTODOS DE POKÉDEX =====

    public PokedexManager getPokedex() {
        return pokedex;
    }

    public void registrarAvistamientoPokemon(String especie, String ubicacion) {
        pokedex.registrarAvistamiento(especie, ubicacion);
    }

    public void registrarVictoriaContraPokemon(String especie) {
        pokedex.registrarVictoriaCombate(especie);
        victorias++; // ✅ CORRECTO - incrementar directamente
    }

    public void registrarCapturaPokemon(String especie, String ubicacion) {
        pokedex.registrarCaptura(especie, ubicacion);
    }

    public boolean puedeEncontrarLegendario() {
        return pokedex.puedeAparecerLegendario();
    }

    public int getEspeciesCompletamenteInvestigadas() {
        return pokedex.getCantidadEspeciesCompletamenteInvestigadas();
    }

    public void mostrarEstadoPokedex() {
        System.out.println("=== POKÉDEX ===");
        System.out.println(pokedex.getResumen());
        System.out.println("Especies completas: " +
            pokedex.getCantidadEspeciesCompletamenteInvestigadas() + "/5");

        if (puedeEncontrarLegendario()) {
            System.out.println("¡LISTO para encontrar Pokémon legendarios!");
        }
    }

    // Getters
    public String getNombre() { return nombre; }
    public List<PokemonJugador> getEquipo() { return new ArrayList<>(equipo); }
    public Pokemon getPokemonActual() { return pokemonActual; }
    public Inventario getInventario() { return inventario; }
    public int getDinero() { return dinero; }
    public int getVictorias() { return victorias; }
    public int getDerrotas() { return derrotas; }
    public int getCantidadPokemon() { return equipo.size(); }

    // Setters
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPokemonActual(Pokemon pokemon) { this.pokemonActual = pokemon; }

    // Dinero y estadísticas
    public void ganarDinero(int cantidad) { dinero += cantidad; }
    public void gastarDinero(int cantidad) { dinero = Math.max(0, dinero - cantidad); }
    public void incrementarVictorias() { victorias++; }
    public void incrementarDerrotas() { derrotas++; }

    public void vaciarEquipo() {
        this.equipo.clear();
        this.pokemonActual = null;
    }

    @Override
    public String toString() {
        return nombre + " - Pokémon: " + equipo.size() + "/6 - Dinero: $" + dinero;
    }
}
