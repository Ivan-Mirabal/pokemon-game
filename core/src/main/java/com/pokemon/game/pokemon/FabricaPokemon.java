package com.pokemon.game.pokemon;

import com.pokemon.game.data.DataLoader;
import com.pokemon.game.data.DataLoader.SpeciesData;
import com.pokemon.game.data.DataLoader.MoveData;
import java.util.*;

public class FabricaPokemon {
    private static final Map<String, EspeciePokemon> especiesCache = new HashMap<>();
    private static final Map<String, Movimiento> movimientosCache = new HashMap<>();

    static {
        System.out.println("Inicializando fábrica de Pokémon...");
        cargarEspeciesDesdeJSON();
        cargarMovimientosDesdeJSON();
        System.out.println("Fábrica lista.");
    }

    private static void cargarEspeciesDesdeJSON() {
        Map<String, SpeciesData> datos = DataLoader.getInstance().getAllSpeciesData();

        for (SpeciesData data : datos.values()) {
            try {
                // Convertir strings a enums
                Tipo tipo1 = Tipo.valueOf(data.type1.toUpperCase());
                Tipo tipo2 = data.type2 != null ? Tipo.valueOf(data.type2.toUpperCase()) : null;

                Habilidad habilidad = Habilidad.valueOf(data.ability.toUpperCase());

                // Convertir evolución
                String evolucion = data.evolvesTo;
                int nivelEvolucion = data.evolutionLevel;
                String itemEvolucion = data.evolutionItem;

                // Crear la especie
                EspeciePokemon especie = new EspeciePokemon(
                    data.name,
                    tipo1,
                    tipo2,
                    data.baseHP,
                    data.baseAttack,
                    data.baseDefense,
                    data.baseSpecialAttack,
                    data.baseSpecialDefense,
                    data.baseSpeed,
                    habilidad,
                    data.catchRate,
                    evolucion,
                    nivelEvolucion,
                    itemEvolucion
                );

                especiesCache.put(data.name, especie);
                System.out.println("✓ Especie creada: " + data.name);

            } catch (Exception e) {
                System.err.println("Error creando especie " + data.name + ": " + e.getMessage());
            }
        }
    }

    private static void cargarMovimientosDesdeJSON() {
        // Los movimientos se cargan bajo demanda en getMovimiento()
    }

    // ===== MÉTODOS PÚBLICOS PRINCIPALES =====

    public static PokemonJugador crearPokemonJugador(String nombreEspecie, int nivel, String apodo) {
        EspeciePokemon especie = getEspecie(nombreEspecie);
        if (especie == null) {
            throw new IllegalArgumentException("Especie no encontrada: " + nombreEspecie);
        }

        PokemonJugador pokemon = new PokemonJugador(especie, apodo, nivel);

        // Asignar movimientos según tipo y nivel
        asignarMovimientosPorTipo(pokemon, especie.getTipo1(), nivel);

        return pokemon;
    }

    public static PokemonSalvaje crearPokemonSalvaje(String nombreEspecie, int nivel) {
        EspeciePokemon especie = getEspecie(nombreEspecie);
        if (especie == null) {
            throw new IllegalArgumentException("Especie no encontrada: " + nombreEspecie);
        }

        PokemonSalvaje pokemon = new PokemonSalvaje(especie, nivel);

        // Asignar movimientos según tipo y nivel
        asignarMovimientosPorTipo(pokemon, especie.getTipo1(), nivel);

        return pokemon;
    }

    // ===== MÉTODOS AUXILIARES =====

    private static EspeciePokemon getEspecie(String nombre) {
        return especiesCache.get(nombre);
    }

    private static Movimiento getMovimiento(String nombre) {
        // Cache de movimientos
        if (movimientosCache.containsKey(nombre)) {
            return movimientosCache.get(nombre);
        }

        // Cargar desde JSON
        MoveData data = DataLoader.getInstance().getMoveData(nombre);
        if (data == null) {
            System.err.println("Movimiento no encontrado: " + nombre);
            return crearMovimientoPorDefecto();
        }

        try {
            Tipo tipo = Tipo.valueOf(data.type.toUpperCase());
            boolean esFisico = data.category.equalsIgnoreCase("PHYSICAL");

            Movimiento movimiento = new Movimiento(
                data.name,
                tipo,
                data.power,
                data.accuracy,
                data.pp,
                esFisico,
                data.description
            );

            movimientosCache.put(nombre, movimiento);
            return movimiento;

        } catch (Exception e) {
            System.err.println("Error creando movimiento " + nombre + ": " + e.getMessage());
            return crearMovimientoPorDefecto();
        }
    }

    private static Movimiento crearMovimientoPorDefecto() {
        return new Movimiento("Placaje", Tipo.NORMAL, 40, 100, 35, true, "Un ataque físico básico.");
    }

    private static void asignarMovimientosPorTipo(Pokemon pokemon, Tipo tipo, int nivel) {
        // Limpiar movimientos existentes
        while (pokemon.getMovimientos().size() > 0) {
            pokemon.olvidarMovimiento(0);
        }

        // Asignar movimientos basados en tipo
        switch (tipo) {
            case FUEGO:
                pokemon.aprenderMovimiento(getMovimiento("Ascuas"));
                pokemon.aprenderMovimiento(getMovimiento("Lanzallamas"));
                pokemon.aprenderMovimiento(getMovimiento("Giro Fuego"));
                break;
            case AGUA:
                pokemon.aprenderMovimiento(getMovimiento("Pistola Agua"));
                pokemon.aprenderMovimiento(getMovimiento("Hidrobomba"));
                pokemon.aprenderMovimiento(getMovimiento("Placaje"));
                break;
            case PLANTA:
                pokemon.aprenderMovimiento(getMovimiento("Latigo Cepa"));
                pokemon.aprenderMovimiento(getMovimiento("Rayo Solar"));
                pokemon.aprenderMovimiento(getMovimiento("Drenadoras"));
                break;
            case ELECTRICO:
                pokemon.aprenderMovimiento(getMovimiento("Impactrueno"));
                pokemon.aprenderMovimiento(getMovimiento("Rayo"));
                pokemon.aprenderMovimiento(getMovimiento("Ataque Rápido"));
                break;
            case PSIQUICO:
                pokemon.aprenderMovimiento(getMovimiento("Psicoonda"));
                pokemon.aprenderMovimiento(getMovimiento("Golpe Cabeza"));
                pokemon.aprenderMovimiento(getMovimiento("Ataque Rápido"));
                break;
            case LUCHA:
                pokemon.aprenderMovimiento(getMovimiento("Puño Dinamico"));
                pokemon.aprenderMovimiento(getMovimiento("Contraataque"));
                pokemon.aprenderMovimiento(getMovimiento("Golpe Cabeza"));
                break;
            default:
                // Movimientos normales por defecto
                pokemon.aprenderMovimiento(getMovimiento("Placaje"));
                pokemon.aprenderMovimiento(getMovimiento("Arañazo"));
                pokemon.aprenderMovimiento(getMovimiento("Golpe Cabeza"));
                pokemon.aprenderMovimiento(getMovimiento("Ataque Rápido"));
                break;
        }

        // Si el nivel es alto, añadir un movimiento poderoso
        if (nivel >= 20) {
            pokemon.aprenderMovimiento(getMovimiento("Contraataque"));
        }
    }

    // ===== MÉTODOS PARA OBTENER INFORMACIÓN =====

    public static EspeciePokemon getEspeciePokemon(String nombre) {
        return especiesCache.get(nombre);
    }

    public static List<String> getTodasEspecies() {
        List<String> especies = new ArrayList<>(especiesCache.keySet());
        Collections.sort(especies);
        return especies;
    }

    public static String[] getNombresEspecies() {
        List<String> especies = getTodasEspecies();
        return especies.toArray(new String[0]);
    }

    public static PokemonSalvaje generarEncuentroAleatorio(String zona, int nivelBase) {
        System.out.println("=== GENERANDO ENCUENTRO PARA ZONA: " + zona + " ===");

        // Debug: mostrar todas las zonas cargadas
        System.out.println("Zonas disponibles en datos: " +
            DataLoader.getInstance().getEncountersForZone(zona));

        List<DataLoader.EncounterData> encuentros = DataLoader.getInstance().getEncountersForZone(zona);

        if (encuentros == null) {
            System.out.println("❌ ENCUENTROS ES NULL para zona: " + zona);

            // Mostrar todas las zonas cargadas
            System.out.println("Zonas cargadas en total:");
            // Necesitarías un método getter para ver todas las zonas
            return crearPokemonSalvaje("Pikachu", Math.max(5, nivelBase));
        }

        if (encuentros.isEmpty()) {
            System.out.println("⚠️ Lista de encuentros VACÍA para zona: " + zona);
            System.out.println("⚠️ Usando Pikachu por defecto");
            return crearPokemonSalvaje("Pikachu", Math.max(5, nivelBase));
        }

        // Mostrar encuentros disponibles
        System.out.println("Encuentros disponibles en " + zona + ":");
        int totalProb = 0;
        for (DataLoader.EncounterData e : encuentros) {
            System.out.println("  - " + e.species + " (" + e.probability + "%) Nv." + e.minLevel + "-" + e.maxLevel);
            totalProb += e.probability;
        }
        System.out.println("Probabilidad total: " + totalProb + "%");

        // Calcular probabilidad total
        int random = (int)(Math.random() * totalProb);
        int acumulado = 0;
        DataLoader.EncounterData seleccionado = null;

        for (DataLoader.EncounterData e : encuentros) {
            acumulado += e.probability;
            if (random < acumulado) {
                seleccionado = e;
                break;
            }
        }

        if (seleccionado == null) {
            seleccionado = encuentros.get(0);
        }

        // Generar nivel aleatorio dentro del rango
        int nivel = seleccionado.minLevel + (int)(Math.random() * (seleccionado.maxLevel - seleccionado.minLevel + 1));

        System.out.println("✅ Encuentro seleccionado: " + seleccionado.species + " Nv." + nivel);

        return crearPokemonSalvaje(seleccionado.species, nivel);
    }
}
