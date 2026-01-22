package com.pokemon.game.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.*;

/**
 * Carga y gestiona todos los datos del juego desde archivos JSON.
 * Implementa el patrón Singleton para asegurar una única instancia global.
 * Incluye caché para mejorar el rendimiento en accesos repetidos a los datos.
 */
public class DataLoader {
    private static DataLoader instance;

    // Mapas de datos
    private Map<String, SpeciesData> speciesData;
    private Map<String, MoveData> moveData;
    private Map<String, List<EncounterData>> encounterData;

    // Cache de sprites para evitar cargas múltiples
    private Map<String, String> spritePaths;

    /**
     * Constructor privado para implementar el patrón Singleton.
     * Inicializa todas las estructuras de datos y carga la información del juego.
     */
    private DataLoader() {
        speciesData = new HashMap<>();
        moveData = new HashMap<>();
        encounterData = new HashMap<>();
        spritePaths = new HashMap<>();

        loadAllData();
    }

    /**
     * Obtiene la instancia única del cargador de datos.
     * Crea una nueva instancia si no existe previamente.
     */
    public static DataLoader getInstance() {
        if (instance == null) {
            instance = new DataLoader();
        }
        return instance;
    }

    /**
     * Coordina la carga de todos los tipos de datos del juego.
     * Llama a los métodos específicos para cargar especies, movimientos y encuentros.
     */
    private void loadAllData() {
        System.out.println("Cargando datos de Pokémon...");
        loadSpecies();
        loadMoves();
        loadEncounters();
    }

    /**
     * Carga las especies de Pokémon desde el archivo JSON correspondiente.
     * Almacena los datos en un mapa clave-valor para acceso rápido.
     */
    private void loadSpecies() {
        try {
            FileHandle file = Gdx.files.internal("data/species.json");
            Json json = new Json();
            json.setIgnoreUnknownFields(true);

            SpeciesList list = json.fromJson(SpeciesList.class, file);

            int contador = 0;
            for (SpeciesData data : list.species) {
                speciesData.put(data.name.toUpperCase(), data);
                contador++;
            }

            System.out.println("Especies: " + contador + " cargadas");
            System.out.println("Todas las especies cargadas");

        } catch (Exception e) {
            System.err.println("Error cargando species.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga los movimientos de Pokémon desde el archivo JSON correspondiente.
     * Incluye información sobre tipo, poder, precisión y categoría de cada movimiento.
     */
    private void loadMoves() {
        try {
            FileHandle file = Gdx.files.internal("data/moves.json");
            Json json = new Json();

            MoveList list = json.fromJson(MoveList.class, file);

            for (MoveData data : list.moves) {
                moveData.put(data.name.toUpperCase(), data);
            }

        } catch (Exception e) {
            System.err.println("Error cargando moves.json: " + e.getMessage());
        }
    }

    /**
     * Carga las tablas de encuentros de Pokémon para diferentes zonas del juego.
     * Maneja errores de archivo faltante cargando datos por defecto como respaldo.
     */
    private void loadEncounters() {
        try {
            FileHandle file = Gdx.files.internal("data/encounters.json");

            if (!file.exists()) {
                System.err.println("ERROR: Archivo encounters.json no encontrado");
                cargarEncuentrosPorDefecto();
                return;
            }

            JsonReader jsonReader = new JsonReader();
            JsonValue root = jsonReader.parse(file);
            JsonValue encountersObj = root.get("encounters");

            if (encountersObj == null) {
                System.err.println("ERROR: No se encontró 'encounters' en JSON");
                cargarEncuentrosPorDefecto();
                return;
            }

            int zonasCargadas = 0;
            int encuentrosTotales = 0;

            for (JsonValue zona = encountersObj.child; zona != null; zona = zona.next) {
                String nombreZona = zona.name;
                List<EncounterData> encuentrosZona = new ArrayList<>();

                for (JsonValue encuentro = zona.child; encuentro != null; encuentro = encuentro.next) {
                    EncounterData data = new EncounterData();
                    data.species = encuentro.getString("species", "Pikachu");
                    data.probability = encuentro.getInt("probability", 10);
                    data.minLevel = encuentro.getInt("minLevel", 5);
                    data.maxLevel = encuentro.getInt("maxLevel", 10);
                    encuentrosZona.add(data);
                    encuentrosTotales++;
                }

                encounterData.put(nombreZona.toLowerCase(), encuentrosZona);
                zonasCargadas++;
            }

        } catch (Exception e) {
            System.err.println("ERROR cargando encounters.json: " + e.getMessage());
            cargarEncuentrosPorDefecto();
        }
    }

    /**
     * Carga una configuración de encuentros por defecto cuando el archivo principal no está disponible.
     * Proporciona datos básicos para permitir que el juego funcione sin el archivo JSON.
     */
    private void cargarEncuentrosPorDefecto() {
        System.out.println("⚠️ Cargando encuentros por defecto...");

        // Datos por defecto para mapa_centro
        List<EncounterData> mapaCentro = new ArrayList<>();
        mapaCentro.add(crearEncounterPorDefecto("Pikachu", 10, 3, 5));
        mapaCentro.add(crearEncounterPorDefecto("Snivy", 15, 5, 8));
        mapaCentro.add(crearEncounterPorDefecto("Farfetch'd", 15, 7, 10));
        mapaCentro.add(crearEncounterPorDefecto("Ralts", 10, 5, 7));
        mapaCentro.add(crearEncounterPorDefecto("Zorua", 10, 6, 9));

        encounterData.put("mapa_centro", mapaCentro);
        System.out.println("✅ Encuentros por defecto cargados para mapa_centro");
    }

    /**
     * Crea un registro de encuentro por defecto con los parámetros especificados.
     */
    private EncounterData crearEncounterPorDefecto(String especie, int probabilidad, int minNivel, int maxNivel) {
        EncounterData data = new EncounterData();
        data.species = especie;
        data.probability = probabilidad;
        data.minLevel = minNivel;
        data.maxLevel = maxNivel;
        return data;
    }

    // ===== GETTERS PÚBLICOS =====

    /**
     * Obtiene los datos de una especie específica de Pokémon.
     *
     * @param name nombre de la especie (no sensible a mayúsculas/minúsculas)
     * @return datos de la especie o null si no se encuentra
     */
    public SpeciesData getSpeciesData(String name) {
        return speciesData.get(name.toUpperCase());
    }

    /**
     * Obtiene los datos de un movimiento específico.
     *
     * @param name nombre del movimiento (no sensible a mayúsculas/minúsculas)
     * @return datos del movimiento o null si no se encuentra
     */
    public MoveData getMoveData(String name) {
        return moveData.get(name.toUpperCase());
    }

    /**
     * Obtiene la lista de encuentros disponibles en una zona específica del juego.
     *
     * @param zone nombre de la zona (no sensible a mayúsculas/minúsculas)
     * @return lista de encuentros para la zona o null si no hay datos
     */
    public List<EncounterData> getEncountersForZone(String zone) {
        String key = zone.toLowerCase();
        List<EncounterData> result = encounterData.get(key);

        return result;
    }

    /**
     * Obtiene un mapa con todas las especies cargadas en el sistema.
     * Devuelve una copia para proteger la integridad de los datos internos.
     */
    public Map<String, SpeciesData> getAllSpeciesData() {
        return new HashMap<>(speciesData);
    }

    /**
     * Obtiene una lista ordenada alfabéticamente con los nombres de todas las especies.
     */
    public List<String> getAllSpeciesNames() {
        List<String> names = new ArrayList<>(speciesData.keySet());
        Collections.sort(names);
        return names;
    }

    // ===== CLASES INTERNAS PARA PARSING JSON =====

    /**
     * Clase contenedora para la lista de especies en el archivo JSON.
     */
    public static class SpeciesList {
        public SpeciesData[] species;
    }

    /**
     * Clase contenedora para la lista de movimientos en el archivo JSON.
     */
    public static class MoveList {
        public MoveData[] moves;
    }

    // ===== CLASES DE DATOS =====

    /**
     * Representa los datos completos de una especie de Pokémon.
     * Incluye estadísticas base, tipos, habilidad e información de evolución.
     */
    public static class SpeciesData {
        public String name;
        public String type1;
        public String type2;
        public int baseHP;
        public int baseAttack;
        public int baseDefense;
        public int baseSpecialAttack;
        public int baseSpecialDefense;
        public int baseSpeed;
        public String ability;
        public double catchRate;
        public String evolvesTo;
        public int evolutionLevel;
        public String evolutionItem;

        /**
         * Devuelve una representación legible de la especie con sus tipos.
         */
        @Override
        public String toString() {
            return name + " (" + type1 + (type2 != null ? "/" + type2 : "") + ")";
        }
    }

    /**
     * Representa los datos completos de un movimiento de Pokémon.
     * Incluye propiedades de combate como poder, precisión y tipo de daño.
     */
    public static class MoveData {
        public String name;
        public String type;
        public int power;
        public int accuracy;
        public int pp;
        public String category; // "PHYSICAL" o "SPECIAL"
        public String description;

        /**
         * Devuelve una representación resumida del movimiento.
         */
        @Override
        public String toString() {
            return name + " [" + type + "] " + power + " PWR";
        }
    }

    /**
     * Representa un posible encuentro de Pokémon en una zona específica.
     * Define la especie, rango de niveles y probabilidad de aparición.
     */
    public static class EncounterData {
        public String species;
        public int probability;
        public int minLevel;
        public int maxLevel;

        /**
         * Devuelve una representación legible del encuentro.
         */
        @Override
        public String toString() {
            return species + " (" + minLevel + "-" + maxLevel + ") " + probability + "%";
        }
    }
}
