package com.pokemon.game.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.*;

public class DataLoader {
    private static DataLoader instance;

    // Mapas de datos
    private Map<String, SpeciesData> speciesData;
    private Map<String, MoveData> moveData;
    private Map<String, List<EncounterData>> encounterData;

    // Cache de sprites para evitar cargas múltiples
    private Map<String, String> spritePaths;

    private DataLoader() {
        speciesData = new HashMap<>();
        moveData = new HashMap<>();
        encounterData = new HashMap<>();
        spritePaths = new HashMap<>();

        loadAllData();
    }

    public static DataLoader getInstance() {
        if (instance == null) {
            instance = new DataLoader();
        }
        return instance;
    }

    private void loadAllData() {
        System.out.println("Cargando datos de Pokémon...");
        loadSpecies();
        loadMoves();
        loadEncounters();
        System.out.println("Datos cargados.");
    }

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

    private void loadMoves() {
        try {
            FileHandle file = Gdx.files.internal("data/moves.json");
            Json json = new Json();

            MoveList list = json.fromJson(MoveList.class, file);

            for (MoveData data : list.moves) {
                moveData.put(data.name.toUpperCase(), data);
            }

            System.out.println("Movimientos cargados: " + moveData.size());
        } catch (Exception e) {
            System.err.println("Error cargando moves.json: " + e.getMessage());
        }
    }

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

            System.out.println("Encuentros: " + zonasCargadas + " zonas, " + encuentrosTotales + " Pokémon");

        } catch (Exception e) {
            System.err.println("ERROR cargando encounters.json: " + e.getMessage());
            cargarEncuentrosPorDefecto();
        }
    }

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

    private EncounterData crearEncounterPorDefecto(String especie, int probabilidad, int minNivel, int maxNivel) {
        EncounterData data = new EncounterData();
        data.species = especie;
        data.probability = probabilidad;
        data.minLevel = minNivel;
        data.maxLevel = maxNivel;
        return data;
    }

    private void loadSpritePaths() {
        // Pre-cargar rutas de sprites basadas en nombres de especies
        String[] especies = {"Pikachu", "Charizard", "Charmander", "Lucario", "Gardevoir", "Gyarados", "Vaporeon"};

        for (String especie : especies) {
            String lowerName = especie.toLowerCase();
            spritePaths.put(especie.toUpperCase(), "assets/sprites/pokemon/" + lowerName + "/");
        }
    }

    // ===== GETTERS PÚBLICOS =====

    public SpeciesData getSpeciesData(String name) {
        return speciesData.get(name.toUpperCase());
    }

    public MoveData getMoveData(String name) {
        return moveData.get(name.toUpperCase());
    }

    public List<EncounterData> getEncountersForZone(String zone) {
        String key = zone.toLowerCase();
        List<EncounterData> result = encounterData.get(key);

        if (result == null) {
            System.out.println("[DataLoader] No se encontró zona: '" + key + "'");
            System.out.println("[DataLoader] Zonas disponibles: " + encounterData.keySet());
        } else {
            System.out.println("[DataLoader] Zona encontrada: '" + key + "' con " + result.size() + " encuentros");
        }

        return result;
    }

    public Map<String, SpeciesData> getAllSpeciesData() {
        return new HashMap<>(speciesData);
    }

    public List<String> getAllSpeciesNames() {
        List<String> names = new ArrayList<>(speciesData.keySet());
        Collections.sort(names);
        return names;
    }

    public String getSpritePath(String speciesName, String spriteType) {
        String basePath = spritePaths.get(speciesName.toUpperCase());
        if (basePath == null) {
            return "assets/sprites/pokemon/placeholder/";
        }
        return basePath + spriteType + ".png";
    }

    // ===== CLASES INTERNAS PARA PARSING JSON =====

    public static class SpeciesList {
        public SpeciesData[] species;
    }

    public static class MoveList {
        public MoveData[] moves;
    }

    // Ya no usamos EncounterDataList porque parseamos manualmente

    // ===== CLASES DE DATOS =====

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

        @Override
        public String toString() {
            return name + " (" + type1 + (type2 != null ? "/" + type2 : "") + ")";
        }
    }

    public static class MoveData {
        public String name;
        public String type;
        public int power;
        public int accuracy;
        public int pp;
        public String category; // "PHYSICAL" o "SPECIAL"
        public String description;

        @Override
        public String toString() {
            return name + " [" + type + "] " + power + " PWR";
        }
    }

    public static class EncounterData {
        public String species;
        public int probability;
        public int minLevel;
        public int maxLevel;

        @Override
        public String toString() {
            return species + " (" + minLevel + "-" + maxLevel + ") " + probability + "%";
        }
    }

    public void debugAllZones() {
        System.out.println("=== DEBUG: TODAS LAS ZONAS CARGADAS ===");
        for (Map.Entry<String, List<EncounterData>> entry : encounterData.entrySet()) {
            System.out.println("Zona: '" + entry.getKey() + "' -> " + entry.getValue().size() + " encuentros");
        }
    }



}
