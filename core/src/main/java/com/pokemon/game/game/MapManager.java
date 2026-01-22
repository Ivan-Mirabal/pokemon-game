package com.pokemon.game.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor centralizado de mapas del juego que administra la carga, almacenamiento
 * y relaciones de conectividad entre los diferentes mapas del mundo.
 * Utiliza un sistema de registros estáticos para acceso eficiente y consistencia.
 */
public class MapManager {

    /**
     * Contiene toda la información de conectividad y ubicación de un mapa específico,
     * incluyendo referencias a los mapas adyacentes en las cuatro direcciones cardinales.
     */
    public static class MapInfo {
        /** Ruta del archivo TMX del mapa actual */
        public String mapFile;

        /** Ruta del mapa ubicado al norte del actual (null si no hay conexión) */
        public String northMap;

        /** Ruta del mapa ubicado al sur del actual (null si no hay conexión) */
        public String southMap;

        /** Ruta del mapa ubicado al este del actual (null si no hay conexión) */
        public String eastMap;

        /** Ruta del mapa ubicado al oeste del actual (null si no hay conexión) */
        public String westMap;

        /**
         * Construye un registro completo de información para un mapa específico.
         *
         * @param mapFile Ruta del archivo TMX del mapa
         * @param north Ruta del mapa al norte o null si no hay conexión
         * @param south Ruta del mapa al sur o null si no hay conexión
         * @param east Ruta del mapa al este o null si no hay conexión
         * @param west Ruta del mapa al oeste o null si no hay conexión
         */
        public MapInfo(String mapFile, String north, String south, String east, String west) {
            this.mapFile = mapFile;
            this.northMap = north;
            this.southMap = south;
            this.eastMap = east;
            this.westMap = west;
        }
    }

    /**
     * Almacenamiento eficiente que asocia rutas de archivo de mapas con su información
     * de conectividad para acceso rápido mediante claves.
     */
    private static final Map<String, MapInfo> MAPAS = new HashMap<>();

    /**
     * Cargador compartido de mapas TMX que evita la creación múltiple de instancias
     * y optimiza el uso de memoria.
     */
    private static final TmxMapLoader MAP_LOADER = new TmxMapLoader();

    // Inicialización estática del registro de mapas disponibles en el juego
    static {
        // Centro - mapa principal conectado a las cuatro direcciones
        MAPAS.put("maps/mapa_centro.tmx",
            new MapInfo("maps/mapa_centro.tmx",
                "maps/mapa_norte.tmx",
                "maps/mapa_sur.tmx",
                "maps/mapa_este.tmx",
                "maps/mapa_oeste.tmx"));

        // Arceus - mapa especial conectado solo al sur
        MAPAS.put("maps/mapa_arceus.tmx",
            new MapInfo("maps/mapa_arceus.tmx",
                null,
                "maps/mapa_norte.tmx",
                null,
                null));

        // Norte - conectado al sur con el centro y al norte con Arceus
        MAPAS.put("maps/mapa_norte.tmx",
            new MapInfo("maps/mapa_norte.tmx",
                "maps/mapa_arceus.tmx",
                "maps/mapa_centro.tmx",
                null,
                null));

        // Sur - conectado solo al norte con el centro
        MAPAS.put("maps/mapa_sur.tmx",
            new MapInfo("maps/mapa_sur.tmx",
                "maps/mapa_centro.tmx",
                null,
                null,
                null));

        // Este - conectado solo al oeste con el centro
        MAPAS.put("maps/mapa_este.tmx",
            new MapInfo("maps/mapa_este.tmx",
                null,
                null,
                null,
                "maps/mapa_centro.tmx"));

        // Oeste - conectado solo al este con el centro
        MAPAS.put("maps/mapa_oeste.tmx",
            new MapInfo("maps/mapa_oeste.tmx",
                null,
                null,
                "maps/mapa_centro.tmx",
                null));

        // Tienda - edificio interior conectado al sur con el centro
        MAPAS.put("maps/mapa_tienda.tmx",
            new MapInfo("maps/mapa_tienda.tmx",
                null,
                "maps/mapa_centro.tmx",
                null,
                null));
    }

    /**
     * Recupera la información de conectividad de un mapa específico basándose
     * en la ruta de su archivo TMX.
     *
     * @param mapFile Ruta completa del archivo TMX del mapa consultado
     * @return Objeto MapInfo con toda la información de conexiones o null si no existe
     */
    public static MapInfo getMapInfo(String mapFile) {
        return MAPAS.get(mapFile);
    }

    /**
     * Carga un mapa desde el sistema de archivos utilizando el cargador compartido.
     *
     * @param mapFile Ruta completa del archivo TMX a cargar
     * @return Instancia de TiledMap lista para su uso en renderizado
     */
    public static TiledMap loadMap(String mapFile) {
        return MAP_LOADER.load(mapFile);
    }

    /**
     * Registra dinámicamente un nuevo mapa en el sistema, permitiendo la expansión
     * del mundo del juego sin modificar el código de inicialización estática.
     *
     * @param mapInfo Información completa del mapa a registrar
     */
    public static void registerMap(MapInfo mapInfo) {
        MAPAS.put(mapInfo.mapFile, mapInfo);
    }
}
