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

/**
 * Gestor centralizado de operaciones de guardado y carga de partidas del juego.
 * Implementa el patrón Singleton para asegurar una única instancia en toda la aplicación
 * y utiliza serialización JSON para persistir el estado del juego en el sistema de archivos.
 */
public class SaveManager {

    /** Instancia única de la clase según el patrón Singleton */
    private static SaveManager instance;

    /** Motor de serialización/deserialización JSON configurado para formato legible */
    private Json json;

    /** Directorio relativo donde se almacenan los archivos de guardado */
    private static final String SAVE_DIR = "saves/";

    /** Nombre del archivo principal de guardado de partida */
    private static final String SAVE_FILE = "partida.json";

    /** Ruta completa relativa al archivo de guardado principal */
    private static final String SAVE_PATH = SAVE_DIR + SAVE_FILE;

    /**
     * Constructor privado que inicializa el motor JSON y configura el serializador
     * personalizado para la clase SaveData. Impide la instanciación directa desde
     * fuera de la clase.
     */
    private SaveManager() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setSerializer(SaveData.class, new SaveDataSerializer());
    }

    /**
     * Proporciona acceso a la instancia única de SaveManager, creándola si no existe.
     *
     * @return La instancia única de SaveManager
     */
    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }

    /**
     * Serializa y guarda el estado actual del juego en un archivo JSON en el sistema
     * de archivos. Crea automáticamente el directorio de guardados si no existe.
     *
     * @param datos Objeto SaveData que contiene todo el estado del juego a guardar
     * @return true si la operación de guardado fue exitosa, false en caso contrario
     */
    public boolean guardarPartida(SaveData datos) {
        try {
            // Crea el directorio de guardados si no existe
            FileHandle savesDir = getSavesDirectory();
            if (!savesDir.exists()) {
                savesDir.mkdirs();

                // Verifica si se creó correctamente
                if (!savesDir.exists()) {
                    System.err.println("❌ No se pudo crear el directorio de guardados");
                    return false;
                }
                System.out.println("📁 Directorio 'saves' creado exitosamente");
            }

            // Serializa los datos a formato JSON legible
            String jsonString = json.prettyPrint(datos);

            // Escribe el contenido JSON en el archivo
            FileHandle archivo = getSaveFile();
            archivo.writeString(jsonString, false);

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

    /**
     * Carga una partida previamente guardada deserializando el contenido del archivo
     * JSON y reconstruyendo el objeto SaveData con todo el estado del juego.
     *
     * @return Objeto SaveData con el estado cargado o null si no existe archivo o hay error
     */
    public SaveData cargarPartida() {
        try {
            FileHandle archivo = getSaveFile();

            // Verifica la existencia del archivo antes de intentar cargarlo
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

    /**
     * Verifica si existe un archivo de guardado válido en la ubicación esperada.
     *
     * @return true si existe el archivo de guardado, false en caso contrario
     */
    public boolean existePartida() {
        return getSaveFile().exists();
    }

    /**
     * Elimina permanentemente el archivo de guardado del sistema de archivos.
     *
     * @return true si se eliminó exitosamente o no existía, false si hubo error
     */
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

    /**
     * Obtiene el directorio de guardados como un objeto FileHandle absoluto
     * basado en el directorio de trabajo actual del proyecto.
     *
     * @return FileHandle que apunta al directorio de guardados
     */
    public FileHandle getSavesDirectory() {
        String basePath = System.getProperty("user.dir");
        FileHandle dir = Gdx.files.absolute(basePath + "/" + SAVE_DIR);
        return dir;
    }

    /**
     * Obtiene el archivo de guardado principal como un objeto FileHandle absoluto.
     *
     * @return FileHandle que apunta al archivo de guardado principal
     */
    public FileHandle getSaveFile() {
        String basePath = System.getProperty("user.dir");
        FileHandle file = Gdx.files.absolute(basePath + "/" + SAVE_PATH);
        return file;
    }

    /**
     * Serializador personalizado para la clase SaveData que controla específicamente
     * cómo se convierten los objetos SaveData a JSON y viceversa, incluyendo manejo
     * de tipos complejos como el Pokédex y el inventario.
     */
    private static class SaveDataSerializer implements Json.Serializer<SaveData> {

        /**
         * Convierte un objeto SaveData a su representación JSON estructurada,
         * incluyendo todos sus componentes: Pokédex, equipo de Pokémon e inventario.
         *
         * @param json Motor JSON que realiza la escritura
         * @param saveData Objeto SaveData a serializar
         * @param knownType Tipo conocido del objeto (SaveData.class)
         */
        @Override
        public void write(Json json, SaveData saveData, Class knownType) {
            json.writeObjectStart();

            // Serializa el estado del Pokédex si existe
            if (saveData.getPokedex() != null) {
                json.writeObjectStart("pokedex");
                json.writeValue("registros", saveData.getPokedex().getRegistros());
                json.writeObjectEnd();
            }

            // Serializa el equipo de Pokémon
            json.writeArrayStart("equipo");
            if (saveData.getEquipo() != null) {
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
            }
            json.writeArrayEnd();

            // Serializa el inventario con validación de nulos
            json.writeArrayStart("inventario");
            if (saveData.getInventario() != null) {
                for (SaveData.ItemSlot item : saveData.getInventario()) {
                    if (item != null && item.getNombreItem() != null) {
                        json.writeObjectStart();
                        json.writeValue("nombreItem", item.getNombreItem());
                        json.writeValue("cantidad", item.getCantidad());
                        json.writeObjectEnd();
                    }
                }
            }
            json.writeArrayEnd();

            json.writeObjectEnd();
        }

        /**
         * Reconstruye un objeto SaveData a partir de su representación JSON,
         * deserializando cada componente y manejando posibles errores de formato.
         *
         * @param json Motor JSON que realiza la lectura
         * @param jsonData Estructura de datos JSON a deserializar
         * @param type Tipo de objeto a crear (SaveData.class)
         * @return Objeto SaveData reconstruido con todos sus componentes
         */
        @Override
        public SaveData read(Json json, JsonValue jsonData, Class type) {
            SaveData saveData = new SaveData();

            try {
                // Deserializa el estado del Pokédex
                if (jsonData.has("pokedex")) {
                    JsonValue pokedexData = jsonData.get("pokedex");
                    if (pokedexData.has("registros")) {
                        JsonValue registrosData = pokedexData.get("registros");
                        PokedexManager pm = new PokedexManager();

                        HashMap<String, PokedexEntry> registros =
                            json.readValue(HashMap.class, PokedexEntry.class, registrosData);

                        if (registros != null) {
                            pm.setRegistros(registros);
                            saveData.setPokedex(pm);
                        }
                    }
                }

                // Deserializa el equipo de Pokémon
                if (jsonData.has("equipo")) {
                    JsonValue equipoData = jsonData.get("equipo");
                    for (JsonValue pokemonData = equipoData.child; pokemonData != null; pokemonData = pokemonData.next) {
                        try {
                            SaveData.PokemonSimple pokemon = new SaveData.PokemonSimple(
                                pokemonData.getString("especie"),
                                pokemonData.getString("apodo", ""),
                                pokemonData.getInt("nivel", 1),
                                pokemonData.getInt("psActual", 10),
                                pokemonData.getInt("psMaximos", 10),
                                pokemonData.getInt("experiencia", 0)
                            );
                            saveData.getEquipo().add(pokemon);
                        } catch (Exception e) {
                            System.err.println("Error leyendo Pokémon: " + e.getMessage());
                        }
                    }
                }

                // Deserializa el inventario con manejo de errores
                if (jsonData.has("inventario")) {
                    JsonValue inventarioData = jsonData.get("inventario");
                    for (JsonValue itemData = inventarioData.child; itemData != null; itemData = itemData.next) {
                        try {
                            String nombreItem = itemData.getString("nombreItem", "Ítem Desconocido");
                            int cantidad = itemData.getInt("cantidad", 1);

                            if (nombreItem != null && !nombreItem.trim().isEmpty() && cantidad > 0) {
                                SaveData.ItemSlot item = new SaveData.ItemSlot(nombreItem, cantidad);
                                saveData.getInventario().add(item);
                            }
                        } catch (Exception e) {
                            System.err.println("Error leyendo ítem: " + e.getMessage());
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("Error general al leer datos guardados: " + e.getMessage());
                e.printStackTrace();
            }

            return saveData;
        }
    }
}
