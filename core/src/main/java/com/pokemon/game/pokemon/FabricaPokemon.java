package com.pokemon.game.pokemon;

import com.pokemon.game.data.DataLoader;
import com.pokemon.game.data.DataLoader.SpeciesData;
import com.pokemon.game.data.DataLoader.MoveData;
import java.util.*;

/**
 * Fábrica centralizada para la creación de instancias de Pokémon y gestión de
 * recursos relacionados como especies, movimientos y encuentros aleatorios.
 * Utiliza un sistema de caché para optimizar el rendimiento y carga datos
 * desde archivos JSON mediante el DataLoader.
 */
public class FabricaPokemon {

    /** Almacenamiento en caché de todas las especies de Pokémon cargadas, indexadas por nombre */
    private static final Map<String, EspeciePokemon> especiesCache = new HashMap<>();

    /** Almacenamiento en caché de todos los movimientos cargados, indexados por nombre */
    private static final Map<String, Movimiento> movimientosCache = new HashMap<>();

    // Bloque de inicialización estática que carga todos los datos necesarios al iniciar la aplicación
    static {
        System.out.println("Inicializando fábrica de Pokémon...");
        cargarEspeciesDesdeJSON();
        cargarMovimientosDesdeJSON();
        System.out.println("Fábrica lista.");
    }

    /**
     * Carga todas las especies de Pokémon definidas en los archivos JSON de datos
     * y las convierte en objetos EspeciePokemon almacenados en la caché.
     * Maneja conversiones de tipos, habilidades y condiciones de evolución.
     */
    private static void cargarEspeciesDesdeJSON() {
        Map<String, SpeciesData> datos = DataLoader.getInstance().getAllSpeciesData();

        for (SpeciesData data : datos.values()) {
            try {
                // Convierte los strings de tipo a enumeraciones Tipo
                Tipo tipo1 = Tipo.valueOf(data.type1.toUpperCase());
                Tipo tipo2 = data.type2 != null ? Tipo.valueOf(data.type2.toUpperCase()) : null;

                Habilidad habilidad = Habilidad.valueOf(data.ability.toUpperCase());

                // Procesa información de evolución
                String evolucion = data.evolvesTo;
                int nivelEvolucion = data.evolutionLevel;
                String itemEvolucion = data.evolutionItem;

                // Crea la instancia de especie con todos los datos procesados
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

            } catch (Exception e) {
                System.err.println("Error creando especie " + data.name + ": " + e.getMessage());
            }
        }
    }

    /**
     * Inicializa el sistema de caché para movimientos. La carga real de datos
     * se realiza bajo demanda cuando se solicita un movimiento específico.
     */
    private static void cargarMovimientosDesdeJSON() {
        // La carga de movimientos se realiza bajo demanda en getMovimiento()
    }

    // ===== MÉTODOS PÚBLICOS PRINCIPALES =====

    /**
     * Crea un Pokémon controlable por el jugador con la especie, nivel y apodo especificados.
     * Asigna automáticamente movimientos apropiados basados en el tipo elemental del Pokémon.
     *
     * @param nombreEspecie Nombre de la especie del Pokémon a crear
     * @param nivel Nivel inicial del Pokémon (1-100)
     * @param apodo Nombre personalizado para el Pokémon (puede ser null o vacío para usar el nombre de especie)
     * @return Instancia de PokemonJugador completamente configurada
     * @throws IllegalArgumentException Si la especie especificada no existe en los datos cargados
     */
    public static PokemonJugador crearPokemonJugador(String nombreEspecie, int nivel, String apodo) {
        EspeciePokemon especie = getEspecie(nombreEspecie);
        if (especie == null) {
            throw new IllegalArgumentException("Especie no encontrada: " + nombreEspecie);
        }

        PokemonJugador pokemon = new PokemonJugador(especie, apodo, nivel);

        // Asigna movimientos basados en el tipo elemental y nivel del Pokémon
        asignarMovimientosPorTipo(pokemon, especie.getTipo1(), nivel);

        return pokemon;
    }

    /**
     * Crea un Pokémon salvaje con la especie y nivel especificados para encuentros
     * aleatorios o combates contra el jugador.
     *
     * @param nombreEspecie Nombre de la especie del Pokémon salvaje a crear
     * @param nivel Nivel del Pokémon salvaje
     * @return Instancia de PokemonSalvaje configurada para combate
     * @throws IllegalArgumentException Si la especie especificada no existe en los datos cargados
     */
    public static PokemonSalvaje crearPokemonSalvaje(String nombreEspecie, int nivel) {
        EspeciePokemon especie = getEspecie(nombreEspecie);
        if (especie == null) {
            throw new IllegalArgumentException("Especie no encontrada: " + nombreEspecie);
        }

        PokemonSalvaje pokemon = new PokemonSalvaje(especie, nivel);

        // Asigna movimientos basados en el tipo elemental y nivel del Pokémon
        asignarMovimientosPorTipo(pokemon, especie.getTipo1(), nivel);

        return pokemon;
    }

    // ===== MÉTODOS AUXILIARES PRIVADOS =====

    /**
     * Recupera una especie de Pokémon de la caché interna por su nombre.
     *
     * @param nombre Nombre de la especie a buscar
     * @return Instancia de EspeciePokemon o null si no existe
     */
    private static EspeciePokemon getEspecie(String nombre) {
        return especiesCache.get(nombre);
    }

    /**
     * Obtiene un movimiento por su nombre, cargándolo desde JSON si no está en caché.
     * Si el movimiento no existe o hay errores en la carga, se devuelve un movimiento por defecto.
     *
     * @param nombre Nombre del movimiento a obtener
     * @return Instancia de Movimiento correspondiente al nombre
     */
    private static Movimiento getMovimiento(String nombre) {
        // Verifica si el movimiento ya está en caché
        if (movimientosCache.containsKey(nombre)) {
            return movimientosCache.get(nombre);
        }

        // Carga los datos del movimiento desde JSON
        MoveData data = DataLoader.getInstance().getMoveData(nombre);
        if (data == null) {
            System.err.println("Movimiento no encontrado: " + nombre);
            return crearMovimientoPorDefecto();
        }

        try {
            // Convierte los datos del movimiento a objetos
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

            // Almacena en caché para futuras consultas
            movimientosCache.put(nombre, movimiento);
            return movimiento;

        } catch (Exception e) {
            System.err.println("Error creando movimiento " + nombre + ": " + e.getMessage());
            return crearMovimientoPorDefecto();
        }
    }

    /**
     * Crea un movimiento básico por defecto cuando no se puede cargar el movimiento solicitado.
     *
     * @return Movimiento "Placaje" como alternativa segura
     */
    private static Movimiento crearMovimientoPorDefecto() {
        return new Movimiento("Placaje", Tipo.NORMAL, 40, 100, 35, true, "Un ataque físico básico.");
    }

    /**
     * Asigna movimientos a un Pokémon basándose en su tipo elemental y nivel actual.
     * Los movimientos asignados varían según el tipo para proporcionar variedad estratégica.
     *
     * @param pokemon Pokémon al que se asignarán los movimientos
     * @param tipo Tipo elemental principal del Pokémon
     * @param nivel Nivel actual del Pokémon para determinar movimientos avanzados
     */
    private static void asignarMovimientosPorTipo(Pokemon pokemon, Tipo tipo, int nivel) {
        // Elimina cualquier movimiento existente para comenzar con una lista limpia
        while (pokemon.getMovimientos().size() > 0) {
            pokemon.olvidarMovimiento(0);
        }

        // Asigna movimientos específicos según el tipo elemental del Pokémon
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
                // Movimientos normales por defecto para tipos no especificados
                pokemon.aprenderMovimiento(getMovimiento("Placaje"));
                pokemon.aprenderMovimiento(getMovimiento("Arañazo"));
                pokemon.aprenderMovimiento(getMovimiento("Golpe Cabeza"));
                pokemon.aprenderMovimiento(getMovimiento("Ataque Rápido"));
                break;
        }

        // Añade un movimiento avanzado si el Pokémon tiene un nivel suficientemente alto
        if (nivel >= 20) {
            pokemon.aprenderMovimiento(getMovimiento("Contraataque"));
        }
    }

    // ===== MÉTODOS PARA OBTENER INFORMACIÓN =====

    /**
     * Obtiene la definición de especie de un Pokémon por su nombre.
     *
     * @param nombre Nombre de la especie a consultar
     * @return Objeto EspeciePokemon o null si no existe
     */
    public static EspeciePokemon getEspeciePokemon(String nombre) {
        return especiesCache.get(nombre);
    }

    /**
     * Devuelve una lista ordenada alfabéticamente de todos los nombres de especies cargadas.
     *
     * @return Lista de nombres de especies disponibles
     */
    public static List<String> getTodasEspecies() {
        List<String> especies = new ArrayList<>(especiesCache.keySet());
        Collections.sort(especies);
        return especies;
    }

    /**
     * Devuelve un arreglo con todos los nombres de especies cargadas, útil para interfaces de usuario.
     *
     * @return Arreglo de nombres de especies
     */
    public static String[] getNombresEspecies() {
        List<String> especies = getTodasEspecies();
        return especies.toArray(new String[0]);
    }

    /**
     * Genera un encuentro aleatorio con un Pokémon salvaje basado en la zona geográfica
     * y el nivel base del jugador. Utiliza tablas de encuentro definidas en JSON.
     *
     * @param zona Identificador de la zona donde ocurre el encuentro
     * @param nivelBase Nivel de referencia para determinar el nivel del Pokémon encontrado
     * @return Instancia de PokemonSalvaje para el encuentro o Pokémon por defecto si hay errores
     */
    public static PokemonSalvaje generarEncuentroAleatorio(String zona, int nivelBase) {
        List<DataLoader.EncounterData> encuentros = DataLoader.getInstance().getEncountersForZone(zona);

        if (encuentros == null) {
            System.out.println("❌ ENCUENTROS ES NULL para zona: " + zona);

            // Muestra todas las zonas cargadas para depuración
            System.out.println("Zonas cargadas en total:");
            // Necesitarías un método getter para ver todas las zonas
            return crearPokemonSalvaje("Pikachu", Math.max(5, nivelBase));
        }

        if (encuentros.isEmpty()) {
            System.out.println("⚠️ Lista de encuentros VACÍA para zona: " + zona);
            System.out.println("⚠️ Usando Pikachu por defecto");
            return crearPokemonSalvaje("Pikachu", Math.max(5, nivelBase));
        }

        // Calcula la probabilidad total para seleccionar un encuentro
        int totalProb = 0;
        for (DataLoader.EncounterData e : encuentros) {
            totalProb += e.probability;
        }

        // Selecciona aleatoriamente un encuentro basado en probabilidades
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

        // Genera un nivel aleatorio dentro del rango definido para el encuentro
        int nivel = seleccionado.minLevel + (int)(Math.random() * (seleccionado.maxLevel - seleccionado.minLevel + 1));

        return crearPokemonSalvaje(seleccionado.species, nivel);
    }
}
