package com.pokemon.game.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import java.util.HashMap;
import java.util.Map;

public class MapManager {

    public static class MapInfo {
        public String mapFile;
        public String northMap;
        public String southMap;
        public String eastMap;
        public String westMap;

        public MapInfo(String mapFile, String north, String south, String east, String west) {
            this.mapFile = mapFile;
            this.northMap = north;
            this.southMap = south;
            this.eastMap = east;
            this.westMap = west;
        }
    }

    // Usar HashMap para acceso más eficiente
    private static final Map<String, MapInfo> MAPAS = new HashMap<>();

    // Cargador estático
    private static final TmxMapLoader MAP_LOADER = new TmxMapLoader();

    static {
        // Inicializar mapa de mapas
        MAPAS.put("maps/mapa_centro.tmx",
            new MapInfo("maps/mapa_centro.tmx",
                "maps/mapa_norte.tmx",
                "maps/mapa_sur.tmx",
                "maps/mapa_este.tmx",
                "maps/mapa_oeste.tmx"));
        MAPAS.put("maps/mapa_norte.tmx",
            new MapInfo("maps/mapa_norte.tmx",
                null,
                "maps/mapa_centro.tmx",
                null,
                null));
        MAPAS.put("maps/mapa_sur.tmx",
            new MapInfo("maps/mapa_sur.tmx",
                "maps/mapa_centro.tmx",
                null,
                null,
                null));
        MAPAS.put("maps/mapa_este.tmx",
            new MapInfo("maps/mapa_este.tmx",
                null,
                null,
                null,
                "maps/mapa_centro.tmx"));
        MAPAS.put("maps/mapa_oeste.tmx",
            new MapInfo("maps/mapa_oeste.tmx",
                null,
                null,
                "maps/mapa_centro.tmx",
                null));
    }

    // Info del mapa
    public static MapInfo getMapInfo(String mapFile) {
        return MAPAS.get(mapFile);
    }

    // Cargar el mapa
    public static TiledMap loadMap(String mapFile) {
        return MAP_LOADER.load(mapFile);
    }

    // Método para agregar mapas dinámicamente (opcional)
    public static void registerMap(MapInfo mapInfo) {
        MAPAS.put(mapInfo.mapFile, mapInfo);
    }
}
