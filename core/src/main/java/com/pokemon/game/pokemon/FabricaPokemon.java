package com.pokemon.game.pokemon;

import java.util.HashMap;
import java.util.Map;

public class FabricaPokemon {
    // Mapa de especies
    private static final Map<String, EspeciePokemon> especies = new HashMap<>();

    static {
        // Inicializar especies con datos de evolución
        especies.put("Pikachu", new EspeciePokemon(
            "Pikachu", Tipo.ELECTRICO, null,
            35, 55, 40, 50, 50, 90,
            Habilidad.ELECTRICIDAD_ESTATICA, 190,
            "Raichu",  // Evoluciona a Raichu
            30,        // Nivel 30
            "Piedratrueno"  // Con Piedratrueno
        ));

        especies.put("Raichu", new EspeciePokemon(
            "Raichu", Tipo.ELECTRICO, null,
            60, 90, 55, 90, 80, 110,
            Habilidad.ELECTRICIDAD_ESTATICA, 75,
            null,      // No evoluciona más
            0,         // Sin nivel de evolución
            null       // Sin item de evolución
        ));

        especies.put("Charmander", new EspeciePokemon(
            "Charmander", Tipo.FUEGO, null,
            39, 52, 43, 60, 50, 65,
            Habilidad.FUEGO_INTERIOR, 45,
            "Charmeleon",  // Evoluciona a Charmeleon
            16,            // Nivel 16
            null
        ));

        especies.put("Charmeleon", new EspeciePokemon(
            "Charmeleon", Tipo.FUEGO, null,
            58, 64, 58, 80, 65, 80,
            Habilidad.FUEGO_INTERIOR, 45,
            "Charizard",   // Evoluciona a Charizard
            36,            // Nivel 36
            null
        ));

        especies.put("Charizard", new EspeciePokemon(
            "Charizard", Tipo.FUEGO, Tipo.VOLADOR,
            78, 84, 78, 109, 85, 100,
            Habilidad.FUEGO_INTERIOR, 45,
            null,      // No evoluciona más
            0,         // Sin nivel de evolución
            null       // Sin item de evolución
        ));

        especies.put("Bulbasaur", new EspeciePokemon(
            "Bulbasaur", Tipo.PLANTA, Tipo.VENENO,
            45, 49, 49, 65, 65, 45,
            Habilidad.ESPESURA, 45,
            "Ivysaur",   // Evoluciona a Ivysaur
            16,          // Nivel 16
            null
        ));

        especies.put("Squirtle", new EspeciePokemon(
            "Squirtle", Tipo.AGUA, null,
            44, 48, 65, 50, 64, 43,
            Habilidad.REGENERACION, 45,
            "Wartortle",  // Evoluciona a Wartortle
            16,           // Nivel 16
            null
        ));

        especies.put("Gengar", new EspeciePokemon(
            "Gengar", Tipo.FANTASMA, Tipo.VENENO,
            60, 65, 60, 130, 75, 110,
            Habilidad.VISTA_LINCE, 45,
            null,      // No evoluciona más
            0,         // Sin nivel de evolución
            null
        ));
    }

    // Crear Pokémon para el jugador
    public static PokemonJugador crearPokemonJugador(String nombreEspecie, int nivel, String apodo) {
        EspeciePokemon especie = especies.get(nombreEspecie);
        if (especie == null) {
            throw new IllegalArgumentException("Especie no encontrada: " + nombreEspecie);
        }

        PokemonJugador pokemon = new PokemonJugador(especie, apodo, nivel);

        // Añadir movimientos según tipo
        agregarMovimientosPorTipo(pokemon, especie.getTipo1());

        return pokemon;
    }

    // Crear Pokémon salvaje
    public static PokemonSalvaje crearPokemonSalvaje(String nombreEspecie, int nivel) {
        EspeciePokemon especie = especies.get(nombreEspecie);
        if (especie == null) {
            throw new IllegalArgumentException("Especie no encontrada: " + nombreEspecie);
        }

        PokemonSalvaje pokemon = new PokemonSalvaje(especie, nivel);

        // Añadir movimientos según tipo
        agregarMovimientosPorTipo(pokemon, especie.getTipo1());

        return pokemon;
    }

    // Obtener evolución de una especie
    public static EspeciePokemon getEvolucion(String nombreEspecie) {
        EspeciePokemon especie = especies.get(nombreEspecie);
        if (especie == null || especie.getEvolucion() == null) {
            return null;
        }
        return especies.get(especie.getEvolucion());
    }

    // Obtener especie por nombre
    public static EspeciePokemon getEspecie(String nombreEspecie) {
        return especies.get(nombreEspecie);
    }

    // Agregar movimientos según tipo
    private static void agregarMovimientosPorTipo(Pokemon pokemon, Tipo tipo) {
        switch (tipo) {
            case FUEGO:
                pokemon.aprenderMovimiento(new Movimiento("Ascuas", Tipo.FUEGO, 40, 100, 25, false, "Quema ligeramente al rival"));
                pokemon.aprenderMovimiento(new Movimiento("Lanzallamas", Tipo.FUEGO, 90, 85, 15, false, "Puede quemar al rival"));
                pokemon.aprenderMovimiento(new Movimiento("Giro Fuego", Tipo.FUEGO, 60, 100, 25, true, "Golpea con fuego giratorio"));
                break;
            case AGUA:
                pokemon.aprenderMovimiento(new Movimiento("Pistola Agua", Tipo.AGUA, 40, 100, 25, false, "Dispara agua a presión"));
                pokemon.aprenderMovimiento(new Movimiento("Hidrobomba", Tipo.AGUA, 110, 80, 5, false, "Potente chorro de agua"));
                pokemon.aprenderMovimiento(new Movimiento("Placaje", Tipo.NORMAL, 40, 100, 35, true, "Golpea con el cuerpo"));
                break;
            case PLANTA:
                pokemon.aprenderMovimiento(new Movimiento("Latigo Cepa", Tipo.PLANTA, 45, 100, 25, true, "Golpea con látigos de plantas"));
                pokemon.aprenderMovimiento(new Movimiento("Rayo Solar", Tipo.PLANTA, 120, 100, 10, false, "Absorbe luz solar para atacar"));
                pokemon.aprenderMovimiento(new Movimiento("Drenadoras", Tipo.PLANTA, 20, 100, 10, false, "Drena PS del rival"));
                break;
            case ELECTRICO:
                pokemon.aprenderMovimiento(new Movimiento("Impactrueno", Tipo.ELECTRICO, 40, 100, 30, false, "Puede paralizar al rival"));
                pokemon.aprenderMovimiento(new Movimiento("Rayo", Tipo.ELECTRICO, 90, 100, 15, false, "Puede paralizar al rival"));
                pokemon.aprenderMovimiento(new Movimiento("Ataque Rápido", Tipo.NORMAL, 40, 100, 30, true, "Ataca primero siempre"));
                break;
            default:
                // Movimientos normales por defecto
                pokemon.aprenderMovimiento(new Movimiento("Placaje", Tipo.NORMAL, 40, 100, 35, true, "Golpea con el cuerpo"));
                pokemon.aprenderMovimiento(new Movimiento("Arañazo", Tipo.NORMAL, 40, 100, 35, true, "Araña al rival"));
                pokemon.aprenderMovimiento(new Movimiento("Golpe Cabeza", Tipo.NORMAL, 70, 100, 15, true, "Puede hacer retroceder"));
                break;
        }

        // Añadir un cuarto movimiento de cobertura
        pokemon.aprenderMovimiento(new Movimiento("Contraataque", Tipo.LUCHA, 70, 100, 20, true, "Contraataca después de recibir daño"));
    }
}
