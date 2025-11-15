package com.pokemon.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class MapManager {

    public static class MapInfo {
        public String mapFile;
        public String northMap; // Mapa al norte
        public String southMap; // Mapa al sur
        public String eastMap;  // Mapa al este
        public String westMap;  // Mapa al oeste

        public MapInfo(String mapFile, String north, String south, String east, String west) {
            this.mapFile = mapFile;
            this.northMap = north;
            this.southMap = south;
            this.eastMap = east;
            this.westMap = west;
        }
    }

    // Registro de mapas disponibles (propenso a mucho cambio)
    public static MapInfo[] MAPAS = {
        new MapInfo("maps/mapa_centro.tmx", "maps/mapa_norte.tmx", "maps/mapa_sur.tmx", "maps/mapa_este.tmx", "maps/mapa_oeste.tmx"),
        new MapInfo("maps/mapa_norte.tmx", null, "maps/mapa_centro.tmx", null, null),
        new MapInfo("maps/mapa_sur.tmx", "maps/mapa_centro.tmx", null, null, null),
        new MapInfo("maps/mapa_este.tmx", null, null, null, "maps/mapa_centro.tmx"),
        new MapInfo("maps/mapa_oeste.tmx", null, null, "maps/mapa_centro.tmx", null)
    };

    // Info del mapa
    public static MapInfo getMapInfo(String mapFile) {
        for (MapInfo info : MAPAS) {
            if (info.mapFile.equals(mapFile)) {
                return info;
            }
        }
        return null;
    }

    // Cargar el mapa
    public static TiledMap loadMap(String mapFile) {
        return new TmxMapLoader().load(mapFile);
    }
}
