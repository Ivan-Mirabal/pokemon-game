package com.pokemon.game.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SerializationException;
import com.pokemon.game.pokedex.PokedexEntry;
import com.pokemon.game.pokedex.PokedexManager;

import java.util.HashMap;

public class SaveManager {
    private static SaveManager instance;
    private Json json;

    // Ruta relativa desde el directorio raíz del proyecto
    private static final String SAVE_DIR = "saves/";
    private static final String SAVE_FILE = "partida.json";
    private static final String SAVE_PATH = SAVE_DIR + SAVE_FILE;

    private SaveManager() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setSerializer(SaveData.class, new SaveDataSerializer());
    }

    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }

    // Guardar partida
    public boolean guardarPartida(SaveData datos) {
        try {
            // Crear directorio saves si no existe
            FileHandle savesDir = getSavesDirectory();
            if (!savesDir.exists()) {
                savesDir.mkdirs(); // mkdirs() no retorna boolean en libGDX

                // Verificar si se creó correctamente
                if (!savesDir.exists()) {
                    System.err.println("❌ No se pudo crear el directorio de guardados");
                    return false;
                }
                System.out.println("📁 Directorio 'saves' creado exitosamente");
            }

            // Convertir a JSON
            String jsonString = json.prettyPrint(datos);

            // Guardar en archivo
            FileHandle archivo = getSaveFile();
            archivo.writeString(jsonString, false);

            System.out.println("✅ Partida guardada exitosamente en: " + archivo.path());
            System.out.println("📂 Ruta absoluta: " + archivo.file().getAbsolutePath());
            return true;

        } catch (SerializationException e) {
            System.err.println("❌ Error serializando datos: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error guardando partida: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Cargar partida
    public SaveData cargarPartida() {
        try {
            FileHandle archivo = getSaveFile();

            if (!archivo.exists()) {
                System.out.println("ℹ️ No existe archivo de guardado: " + SAVE_PATH);
                return null;
            }

            String jsonString = archivo.readString();
            SaveData datos = json.fromJson(SaveData.class, jsonString);

            System.out.println("✅ Partida cargada exitosamente desde: " + archivo.path());
            return datos;

        } catch (SerializationException e) {
            System.err.println("❌ Error deserializando datos: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error cargando partida: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Verificar si existe partida guardada
    public boolean existePartida() {
        return getSaveFile().exists();
    }

    // Eliminar partida guardada
    public boolean eliminarPartida() {
        try {
            FileHandle archivo = getSaveFile();
            if (archivo.exists()) {
                boolean deleted = archivo.delete();
                if (deleted) {
                    System.out.println("🗑️ Partida eliminada exitosamente");
                }
                return deleted;
            }
            System.out.println("ℹ️ No hay partida para eliminar");
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error eliminando partida: " + e.getMessage());
            return false;
        }
    }

    // Método para obtener el directorio de saves
    public FileHandle getSavesDirectory() {
        // Usamos getLocalStoragePath() para obtener la ruta base del proyecto
        String basePath = System.getProperty("user.dir");
        FileHandle dir = Gdx.files.absolute(basePath + "/" + SAVE_DIR);

        // Debug: mostrar la ruta
        System.out.println("📂 Ruta del directorio de saves: " + dir.path());
        System.out.println("📂 Existe: " + dir.exists());
        System.out.println("📂 Es directorio: " + (dir.exists() && dir.isDirectory()));

        return dir;
    }

    // Método para obtener el archivo de guardado
    public FileHandle getSaveFile() {
        String basePath = System.getProperty("user.dir");
        FileHandle file = Gdx.files.absolute(basePath + "/" + SAVE_PATH);

        // Debug: mostrar la ruta
        System.out.println("📄 Ruta del archivo de guardado: " + file.path());
        System.out.println("📄 Existe: " + file.exists());

        return file;
    }

    // Método para listar todos los archivos de guardado (útil para múltiples slots)
    public FileHandle[] listarPartidas() {
        FileHandle savesDir = getSavesDirectory();
        if (savesDir.exists() && savesDir.isDirectory()) {
            return savesDir.list(".json");
        }
        return new FileHandle[0];
    }

    // Método para guardar en slot específico
    public boolean guardarPartidaEnSlot(SaveData datos, int slot) {
        String slotFile = SAVE_DIR + "partida_" + slot + ".json";
        return guardarPartidaEnArchivo(datos, slotFile);
    }

    // Método para cargar desde slot específico
    public SaveData cargarPartidaDeSlot(int slot) {
        String slotFile = SAVE_DIR + "partida_" + slot + ".json";
        String basePath = System.getProperty("user.dir");
        FileHandle archivo = Gdx.files.absolute(basePath + "/" + slotFile);

        if (!archivo.exists()) {
            System.out.println("ℹ️ No existe partida en el slot " + slot);
            return null;
        }

        try {
            String jsonString = archivo.readString();
            SaveData datos = json.fromJson(SaveData.class, jsonString);
            System.out.println("✅ Partida cargada del slot " + slot);
            return datos;
        } catch (Exception e) {
            System.err.println("❌ Error cargando partida del slot " + slot + ": " + e.getMessage());
            return null;
        }
    }

    // Método privado para guardar en un archivo específico
    private boolean guardarPartidaEnArchivo(SaveData datos, String filePath) {
        try {
            String basePath = System.getProperty("user.dir");
            FileHandle archivo = Gdx.files.absolute(basePath + "/" + filePath);

            // Asegurar que el directorio padre existe
            FileHandle parentDir = archivo.parent();
            if (!parentDir.exists()) {
                parentDir.mkdirs();

                // Verificar si se creó
                if (!parentDir.exists()) {
                    System.err.println("❌ No se pudo crear el directorio: " + parentDir.path());
                    return false;
                }
            }

            String jsonString = json.prettyPrint(datos);
            archivo.writeString(jsonString, false);

            System.out.println("✅ Partida guardada en: " + archivo.path());
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error guardando partida: " + e.getMessage());
            return false;
        }
    }

    // Método para obtener información del sistema de archivos (útil para debug)
    public void mostrarInfoSistemaArchivos() {
        System.out.println("\n=== INFORMACIÓN DEL SISTEMA DE ARCHIVOS ===");
        System.out.println("Directorio de trabajo: " + System.getProperty("user.dir"));
        System.out.println("Sistema operativo: " + System.getProperty("os.name"));

        FileHandle savesDir = getSavesDirectory();
        System.out.println("Directorio de saves existe: " + savesDir.exists());
        System.out.println("Directorio de saves es directorio: " + (savesDir.exists() && savesDir.isDirectory()));

        FileHandle saveFile = getSaveFile();
        System.out.println("Archivo de guardado existe: " + saveFile.exists());

        // Listar archivos en el directorio de saves
        if (savesDir.exists() && savesDir.isDirectory()) {
            FileHandle[] archivos = savesDir.list();
            System.out.println("Archivos en saves/: " + archivos.length);
            for (FileHandle archivo : archivos) {
                System.out.println("  - " + archivo.name() + " (" + archivo.length() + " bytes)");
            }
        }
        System.out.println("========================================\n");
    }

    // Serializador personalizado para SaveData
    private static class SaveDataSerializer implements Json.Serializer<SaveData> {
        @Override
        public void write(Json json, SaveData saveData, Class knownType) {
            json.writeObjectStart();

            // Escribir Pokédex
            json.writeObjectStart("pokedex");
            json.writeValue("registros", saveData.getPokedex().getRegistros());
            json.writeObjectEnd();

            // Escribir equipo
            json.writeArrayStart("equipo");
            for (SaveData.PokemonSimple pokemon : saveData.getEquipo()) {
                json.writeObjectStart();
                json.writeValue("especie", pokemon.getEspecie());
                json.writeValue("apodo", pokemon.getApodo());
                json.writeValue("nivel", pokemon.getNivel());
                json.writeValue("psActual", pokemon.getPsActual());
                json.writeValue("psMaximos", pokemon.getPsMaximos());
                json.writeValue("experiencia", pokemon.getExperiencia());
                json.writeObjectEnd();
            }
            json.writeArrayEnd();

            // Escribir inventario
            json.writeArrayStart("inventario");
            for (SaveData.ItemSlot item : saveData.getInventario()) {
                json.writeObjectStart();
                json.writeValue("nombreItem", item.getNombreItem());
                json.writeValue("cantidad", item.getCantidad());
                json.writeObjectEnd();
            }
            json.writeArrayEnd();

            json.writeObjectEnd();
        }

        @Override
        public SaveData read(Json json, JsonValue jsonData, Class type) {
            SaveData saveData = new SaveData();

            // LEER POKÉDEX
            if (jsonData.has("pokedex")) {
                JsonValue registrosData = jsonData.get("pokedex").get("registros");
                PokedexManager pm = new PokedexManager();

                HashMap<String, PokedexEntry> registros = json.readValue(HashMap.class, PokedexEntry.class, registrosData);

                if (registros != null) {
                    pm.setRegistros(registros);
                    saveData.setPokedex(pm);
                }
            }

            // Leer equipo
            if (jsonData.has("equipo")) {
                JsonValue equipoData = jsonData.get("equipo");
                for (JsonValue pokemonData = equipoData.child; pokemonData != null; pokemonData = pokemonData.next) {
                    SaveData.PokemonSimple pokemon = new SaveData.PokemonSimple(
                        pokemonData.getString("especie"),
                        pokemonData.getString("apodo"),
                        pokemonData.getInt("nivel"),
                        pokemonData.getInt("psActual"),
                        pokemonData.getInt("psMaximos"),
                        pokemonData.getInt("experiencia")
                    );
                    saveData.getEquipo().add(pokemon);
                }
            }

            // Leer inventario
            if (jsonData.has("inventario")) {
                JsonValue inventarioData = jsonData.get("inventario");
                for (JsonValue itemData = inventarioData.child; itemData != null; itemData = itemData.next) {
                    SaveData.ItemSlot item = new SaveData.ItemSlot(
                        itemData.getString("nombreItem"),
                        itemData.getInt("cantidad")
                    );
                    saveData.getInventario().add(item);
                }
            }

            return saveData;
        }
    }
}
