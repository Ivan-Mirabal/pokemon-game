package com.pokemon.game.data;

import java.util.List;
import com.pokemon.game.pokemon.FabricaPokemon;

public class PokemonData {
    public static String[] getAvailableSpecies() {
        return FabricaPokemon.getNombresEspecies();
    }

    public static List<String> getAllSpecies() {
        return FabricaPokemon.getTodasEspecies();
    }

    public static boolean speciesExists(String name) {
        return FabricaPokemon.getEspeciePokemon(name) != null;
    }

    public static String getTypeColor(String type) {
        if (type == null) return "#A8A878"; // Gris para normal

        switch (type.toUpperCase()) {
            case "FUEGO": return "#F08030";
            case "AGUA": return "#6890F0";
            case "PLANTA": return "#78C850";
            case "ELECTRIC": return "#F8D030";
            case "PSIQUICO": return "#F85888";
            case "LUCHA": return "#C03028";
            case "VOLADOR": return "#A890F0";
            case "TIERRA": return "#E0C068";
            case "ROCA": return "#B8A038";
            case "BICHO": return "#A8B820";
            case "VENENO": return "#A040A0";
            case "HIELO": return "#98D8D8";
            case "FANTASMA": return "#705898";
            case "DRAGON": return "#7038F8";
            case "ACERO": return "#B8B8D0";
            case "SINIESTRO": return "#705848";
            case "HADA": return "#EE99AC";
            default: return "#A8A878"; // Normal
        }
    }
}
