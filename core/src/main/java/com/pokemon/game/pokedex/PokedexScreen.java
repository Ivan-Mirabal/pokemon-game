package com.pokemon.game.pokedex;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.pokemon.game.player.Player;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class PokedexScreen {
    private Player player;
    private PokedexManager pokedex;
    private BitmapFont font;
    private Texture whitePixel;
    private Map<String, Texture> spriteCache;

    // Constantes para tamaños
    private static final int POKEDEX_SPRITE_SIZE = 40; // 40x40 sprites
    private static final int PROGRESS_BAR_WIDTH = 120;
    private static final int PROGRESS_BAR_HEIGHT = 8;

    // Colores consistentes con el tema del juego
    private final Color COLOR_FONDO = new Color(0.12f, 0.12f, 0.18f, 1);
    private final Color COLOR_PANEL = new Color(0.18f, 0.18f, 0.24f, 0.95f);
    private final Color COLOR_BORDE = new Color(0.4f, 0.4f, 0.6f, 1);
    private final Color COLOR_TEXTO_PRINCIPAL = new Color(0.9f, 0.9f, 1.0f, 1);
    private final Color COLOR_TEXTO_SECUNDARIO = new Color(0.7f, 0.7f, 0.9f, 1);
    private final Color COLOR_SELECCION = new Color(0.3f, 0.4f, 0.8f, 0.8f);

    // Colores para barras de progreso
    private final Color COLOR_PROGRESS_BG = new Color(0.3f, 0.3f, 0.4f, 1);
    private final Color COLOR_PROGRESS_LOW = new Color(1.0f, 0.2f, 0.2f, 1);    // Rojo (0-33%)
    private final Color COLOR_PROGRESS_MED = new Color(1.0f, 0.8f, 0.2f, 1);    // Amarillo (34-66%)
    private final Color COLOR_PROGRESS_HIGH = new Color(0.2f, 0.8f, 0.2f, 1);   // Verde (67-99%)
    private final Color COLOR_PROGRESS_MAX = new Color(0.0f, 0.6f, 1.0f, 1);    // Azul (100%)

    // Para calcular anchos de texto
    private GlyphLayout layout;

    public PokedexScreen(Player player) {
        this.player = player;
        this.pokedex = player.getEntrenador().getPokedex();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.0f);
        this.layout = new GlyphLayout();
        this.whitePixel = crearTexturaBlanca();

        // Cache con límite de 20 sprites para evitar memory leaks
        this.spriteCache = new HashMap<String, Texture>() {
            protected boolean removeEldestEntry(Map.Entry<String, Texture> eldest) {
                if (size() > 20) {
                    eldest.getValue().dispose();
                    return true;
                }
                return false;
            }
        };
    }

    private Texture crearTexturaBlanca() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void dibujar(SpriteBatch batch, int screenWidth, int screenHeight) {
        String especieSeleccionada = player.getPokedexSelectedSpecies();

        if (especieSeleccionada == null) {
            dibujarListaPokedex(batch, screenWidth, screenHeight);
        } else {
            dibujarDetallePokedex(batch, screenWidth, screenHeight, especieSeleccionada);
        }
    }

    private void dibujarListaPokedex(SpriteBatch batch, int screenWidth, int screenHeight) {
        List<PokedexEntry> entradas = pokedex.getEntradasOrdenadas();
        int pagina = player.getPokedexPage();
        int inicio = pagina * player.POKEDEX_ENTRIES_PER_PAGE;
        int fin = Math.min(inicio + player.POKEDEX_ENTRIES_PER_PAGE, entradas.size());

        // Fondo
        batch.setColor(0.1f, 0.1f, 0.15f, 1);
        batch.draw(whitePixel, 0, 0, screenWidth, screenHeight);
        batch.setColor(Color.WHITE);

        // Título
        font.getData().setScale(2.0f);
        font.setColor(new Color(0.9f, 0.9f, 1.0f, 1));
        String titulo = "POKÉDEX";
        layout.setText(font, titulo);
        font.draw(batch, titulo, (screenWidth - layout.width) / 2, screenHeight - 50);
        font.getData().setScale(1.0f);

        // Estadísticas centradas
        font.setColor(new Color(0.7f, 0.7f, 0.9f, 1));
        String resumen = pokedex.getResumen();
        layout.setText(font, resumen);
        font.draw(batch, resumen, (screenWidth - layout.width) / 2, screenHeight - 100);

        // Progreso para legendario
        int completadas = pokedex.getCantidadEspeciesCompletamenteInvestigadas();
        String textoBarra = "Progreso para legendario: " + completadas + "/5";
        layout.setText(font, textoBarra);
        font.draw(batch, textoBarra, (screenWidth - layout.width) / 2, screenHeight - 130);

        font.setColor(Color.WHITE);

        // ===== RECUADRO DE LA LISTA DE POKÉMON =====
        float panelX = 50;
        float panelY = screenHeight - 200;
        float panelAncho = screenWidth - 100;
        float panelAlto = screenHeight - 300;

        // Fondo del panel
        batch.setColor(new Color(0.15f, 0.15f, 0.2f, 0.9f));
        batch.draw(whitePixel, panelX, panelY - panelAlto + 50, panelAncho, panelAlto);

        // Borde del panel
        batch.setColor(new Color(0.4f, 0.4f, 0.6f, 1));
        batch.draw(whitePixel, panelX, panelY - panelAlto + 50, panelAncho, 2);
        batch.draw(whitePixel, panelX, panelY + 50, panelAncho, 2);
        batch.draw(whitePixel, panelX, panelY - panelAlto + 50, 2, panelAlto);
        batch.draw(whitePixel, panelX + panelAncho, panelY - panelAlto + 50, 2, panelAlto);
        batch.setColor(Color.WHITE);

        // Lista de Pokémon dentro del recuadro
        float startX = panelX + 20;
        float startY = panelY;
        float espacio = 45; // Más espacio para incluir barra

        for (int i = inicio; i < fin; i++) {
            PokedexEntry entrada = entradas.get(i);
            float y = startY - ((i - inicio) * espacio);
            boolean seleccionado = (i == inicio + player.getPokedexSelection());

            // Resaltar seleccionado
            if (seleccionado) {
                batch.setColor(new Color(0.3f, 0.3f, 0.5f, 0.8f));
                batch.draw(whitePixel, startX - 10, y - 25, panelAncho - 20, 40);
                batch.setColor(Color.WHITE);
            }

            // 1. SPRITE POKÉDEX (40x40) - A LA IZQUIERDA
            float spriteX = startX;
            float spriteY = y - 25; // Centrado verticalmente

            Texture spritePokedex = cargarSpritePokedex(entrada.getEspecie());
            if (spritePokedex != null) {
                batch.draw(spritePokedex, spriteX, spriteY,
                    POKEDEX_SPRITE_SIZE, POKEDEX_SPRITE_SIZE);
            } else {
                // Placeholder cuadrado
                batch.setColor(new Color(0.3f, 0.3f, 0.4f, 1));
                batch.draw(whitePixel, spriteX, spriteY,
                    POKEDEX_SPRITE_SIZE, POKEDEX_SPRITE_SIZE);
                batch.setColor(Color.WHITE);

                // Inicial del nombre como fallback
                font.setColor(new Color(0.6f, 0.6f, 0.8f, 1));
                if (!entrada.getEspecie().isEmpty()) {
                    String inicial = entrada.getEspecie().substring(0, 1);
                    font.draw(batch, inicial, spriteX + 15, spriteY + 25);
                }
                font.setColor(Color.WHITE);
            }

            // 2. NOMBRE DEL POKÉMON (al lado del sprite)
            float nombreX = spriteX + POKEDEX_SPRITE_SIZE + 15;
            float nombreY = y;

            if (seleccionado) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "▶ " + entrada.getEspecie(), nombreX, nombreY);
            } else {
                // Color según estado
                if (entrada.isCapturado()) {
                    font.setColor(Color.GREEN);
                } else if (entrada.isVisto()) {
                    font.setColor(Color.YELLOW);
                } else {
                    font.setColor(Color.GRAY);
                }
                font.draw(batch, entrada.getEspecie(), nombreX, nombreY);
            }

            // 3. BARRA DE PROGRESO DE INVESTIGACIÓN (a la derecha del nombre)
            float barraX = nombreX + 180; // Espacio después del nombre más largo
            float barraY = y - 10;

            if (entrada.isVisto()) {
                // Solo mostrar barra si se ha visto al menos una vez
                dibujarBarraProgreso(batch, entrada, barraX, barraY);
            } else {
                // Mostrar "???" si no se ha visto
                font.setColor(new Color(0.5f, 0.5f, 0.7f, 1));
                font.draw(batch, "???", barraX, nombreY);
            }

            font.setColor(Color.WHITE);
        }

        // Página actual
        font.setColor(new Color(0.5f, 0.5f, 0.7f, 1));
        int totalPaginas = (int) Math.ceil(entradas.size() / (float) player.POKEDEX_ENTRIES_PER_PAGE);
        String paginaInfo = "Página " + (pagina + 1) + "/" + totalPaginas;
        layout.setText(font, paginaInfo);
        font.draw(batch, paginaInfo, screenWidth - layout.width - 50, 50);

        // INSTRUCCIONES
        font.setColor(new Color(0.6f, 0.6f, 0.8f, 1));
        String instrucciones = "↑↓: Navegar  Enter: Ver detalles  ESC: Volver";
        layout.setText(font, instrucciones);
        font.draw(batch, instrucciones, (screenWidth - layout.width) / 2, 50);

        font.setColor(Color.WHITE);
    }

    private void dibujarBarraProgreso(SpriteBatch batch, PokedexEntry entrada,
                                      float x, float y) {
        float progreso = entrada.getProgresoInvestigacion();

        // Fondo de la barra
        batch.setColor(COLOR_PROGRESS_BG);
        batch.draw(whitePixel, x, y, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);

        // Color según progreso
        Color colorBarra;
        if (progreso >= 1.0f) {
            colorBarra = COLOR_PROGRESS_MAX; // 100% - Azul
        } else if (progreso >= 0.67f) {
            colorBarra = COLOR_PROGRESS_HIGH; // 67-99% - Verde
        } else if (progreso >= 0.34f) {
            colorBarra = COLOR_PROGRESS_MED; // 34-66% - Amarillo
        } else {
            colorBarra = COLOR_PROGRESS_LOW; // 0-33% - Rojo
        }

        // Barra de progreso
        batch.setColor(colorBarra);
        batch.draw(whitePixel, x, y, PROGRESS_BAR_WIDTH * progreso, PROGRESS_BAR_HEIGHT);
        batch.setColor(Color.WHITE);

        // Texto de progreso (ej: "7/10")
        font.setColor(Color.WHITE);
        String textoProgreso = entrada.getNivelInvestigacion() + "/10";
        font.draw(batch, textoProgreso,
            x + PROGRESS_BAR_WIDTH + 10,
            y + PROGRESS_BAR_HEIGHT/2 + 4);
    }

    private void dibujarDetallePokedex(SpriteBatch batch, int screenWidth, int screenHeight,
                                       String especie) {
        // [Mantener el código existente del detalle, pero puedes mejorarlo también]
        PokedexEntry entrada = pokedex.getEntrada(especie);
        if (entrada == null) return;

        // Fondo
        batch.setColor(0.08f, 0.08f, 0.12f, 1);
        batch.draw(whitePixel, 0, 0, screenWidth, screenHeight);
        batch.setColor(Color.WHITE);

        // ===== PANEL IZQUIERDO: SPRITE Y DATOS BÁSICOS =====
        float panelIzqX = 50;
        float panelIzqY = 100;
        float panelIzqAncho = screenWidth * 0.4f;
        float panelIzqAlto = screenHeight - 150;

        // Panel izquierdo fondo
        batch.setColor(new Color(0.15f, 0.15f, 0.2f, 0.9f));
        batch.draw(whitePixel, panelIzqX, panelIzqY, panelIzqAncho, panelIzqAlto);
        batch.setColor(new Color(0.4f, 0.4f, 0.6f, 1));
        // Bordes
        batch.draw(whitePixel, panelIzqX, panelIzqY, panelIzqAncho, 2);
        batch.draw(whitePixel, panelIzqX, panelIzqY + panelIzqAlto, panelIzqAncho, 2);
        batch.draw(whitePixel, panelIzqX, panelIzqY, 2, panelIzqAlto);
        batch.draw(whitePixel, panelIzqX + panelIzqAncho, panelIzqY, 2, panelIzqAlto);
        batch.setColor(Color.WHITE);

        // Nombre del Pokémon
        font.getData().setScale(2.0f);
        font.setColor(new Color(1.0f, 1.0f, 0.8f, 1));
        layout.setText(font, especie);
        font.draw(batch, especie, panelIzqX + 20, panelIzqY + panelIzqAlto - 30);
        font.getData().setScale(1.0f);

        // Estado
        font.setColor(entrada.isCapturado() ? Color.GREEN :
            entrada.isVisto() ? Color.YELLOW : Color.GRAY);
        String estado = entrada.isCapturado() ? "● CAPTURADO" :
            entrada.isVisto() ? "○ VISTO" : "? DESCONOCIDO";
        font.draw(batch, estado, panelIzqX + 20, panelIzqY + panelIzqAlto - 60);

        // Sprite grande del Pokémon (128x128)
        Texture sprite = cargarSpritePokemon(especie, "front"); // front para vista detalle
        if (sprite != null) {
            float spriteX = panelIzqX + (panelIzqAncho / 2) - 64;
            float spriteY = panelIzqY + (panelIzqAlto / 2) - 32;
            batch.draw(sprite, spriteX, spriteY, 128, 128);
        } else {
            // Placeholder si no hay sprite
            batch.setColor(new Color(0.3f, 0.3f, 0.4f, 1));
            batch.draw(whitePixel, panelIzqX + (panelIzqAncho / 2) - 64,
                panelIzqY + (panelIzqAlto / 2) - 32, 128, 128);
            batch.setColor(Color.WHITE);

            font.setColor(new Color(0.6f, 0.6f, 0.8f, 1));
            font.draw(batch, "Sprite no disponible",
                panelIzqX + (panelIzqAncho / 2) - 50,
                panelIzqY + (panelIzqAlto / 2) + 10);
        }

        // ===== PANEL DERECHO: INFORMACIÓN DETALLADA =====
        float panelDerX = panelIzqX + panelIzqAncho + 20;
        float panelDerAncho = screenWidth - panelDerX - 50;

        // Panel derecho fondo
        batch.setColor(new Color(0.15f, 0.15f, 0.2f, 0.9f));
        batch.draw(whitePixel, panelDerX, panelIzqY, panelDerAncho, panelIzqAlto);
        batch.setColor(new Color(0.4f, 0.4f, 0.6f, 1));
        // Bordes
        batch.draw(whitePixel, panelDerX, panelIzqY, panelDerAncho, 2);
        batch.draw(whitePixel, panelDerX, panelIzqY + panelIzqAlto, panelDerAncho, 2);
        batch.draw(whitePixel, panelDerX, panelIzqY, 2, panelIzqAlto);
        batch.draw(whitePixel, panelDerX + panelDerAncho, panelIzqY, 2, panelIzqAlto);
        batch.setColor(Color.WHITE);

        float y = panelIzqY + panelIzqAlto - 40;

        // NIVEL DE INVESTIGACIÓN con barra mejorada
        font.setColor(new Color(0.8f, 0.8f, 1.0f, 1));
        font.draw(batch, "NIVEL DE INVESTIGACIÓN", panelDerX + 20, y);
        y -= 30;

        // Barra de investigación grande en detalle
        float progreso = entrada.getProgresoInvestigacion();
        float barraAncho = panelDerAncho - 100;
        float barraY = y - 15;

        // Llamar al método mejorado de dibujo de barra
        dibujarBarraProgreso(batch, entrada, panelDerX + 20, barraY);

        // Texto del progreso ya está incluido en dibujarBarraProgreso
        y -= 50;

        // REGISTRO DE ENCUENTROS (mantener igual)
        font.setColor(new Color(0.8f, 0.8f, 1.0f, 1));
        font.draw(batch, "REGISTRO", panelDerX + 20, y);
        y -= 25;

        font.setColor(new Color(0.7f, 0.7f, 0.9f, 1));
        font.draw(batch, "Veces visto: " + entrada.getVecesVisto(),
            panelDerX + 20, y);
        y -= 20;

        font.draw(batch, "Veces capturado: " + entrada.getVecesCapturado(),
            panelDerX + 20, y);
        y -= 30;

        if (entrada.getPrimerAvistamientoUbicacion() != null) {
            font.setColor(new Color(0.5f, 0.8f, 1.0f, 1));
            font.draw(batch, "Primer avistamiento:", panelDerX + 20, y);
            y -= 20;
            font.setColor(Color.WHITE);
            font.draw(batch, entrada.getPrimerAvistamientoUbicacion(),
                panelDerX + 40, y);
            y -= 15;
            font.draw(batch, entrada.getPrimerAvistamientoFecha(),
                panelDerX + 40, y);
            y -= 25;
        }

        if (entrada.getPrimeraCapturaUbicacion() != null) {
            font.setColor(new Color(0.5f, 1.0f, 0.5f, 1));
            font.draw(batch, "Primera captura:", panelDerX + 20, y);
            y -= 20;
            font.setColor(Color.WHITE);
            font.draw(batch, entrada.getPrimeraCapturaUbicacion(),
                panelDerX + 40, y);
            y -= 15;
            font.draw(batch, entrada.getPrimeraCapturaFecha(),
                panelDerX + 40, y);
        }

        // ===== INSTRUCCIÓN PARA VOLVER =====
        font.getData().setScale(0.9f);
        font.setColor(new Color(0.6f, 0.6f, 0.8f, 1));
        String instrucciones = "ESC: Volver al menu  B: Volver a la pokedex";
        layout.setText(font, instrucciones);
        font.draw(batch, instrucciones, (screenWidth - layout.width) / 2, 50);
        font.getData().setScale(1.0f);
    }

    /**
     * Carga sprite específico para Pokédex (40x40)
     */
    private Texture cargarSpritePokedex(String especie) {
        String cacheKey = especie.toLowerCase() + "_pokedex";

        if (spriteCache.containsKey(cacheKey)) {
            return spriteCache.get(cacheKey);
        }

        try {
            // Primero intentar con pokedex.png (40x40)
            String ruta = "sprites/pokemon/" + especie.toLowerCase() + "/pokedex.png";
            Texture sprite = new Texture(Gdx.files.internal(ruta));
            spriteCache.put(cacheKey, sprite);
            return sprite;
        } catch (Exception e1) {
            try {
                // Si no existe, intentar con front.png y escalaremos
                String ruta = "sprites/pokemon/" + especie.toLowerCase() + "/front.png";
                Texture sprite = new Texture(Gdx.files.internal(ruta));
                spriteCache.put(cacheKey, sprite);
                return sprite;
            } catch (Exception e2) {
                // Si tampoco existe, cachear null para no seguir intentando
                spriteCache.put(cacheKey, null);
                return null;
            }
        }
    }

    /**
     * Carga sprite para vista detalle (front/back)
     */
    private Texture cargarSpritePokemon(String especie, String tipo) {
        String cacheKey = especie.toLowerCase() + "_" + tipo;

        if (spriteCache.containsKey(cacheKey)) {
            return spriteCache.get(cacheKey);
        }

        try {
            String ruta = "sprites/pokemon/" + especie.toLowerCase() + "/" + tipo + ".png";
            Texture sprite = new Texture(Gdx.files.internal(ruta));
            spriteCache.put(cacheKey, sprite);
            return sprite;
        } catch (Exception e) {
            spriteCache.put(cacheKey, null);
            return null;
        }
    }

    public void dispose() {
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
        if (font != null) {
            font.dispose();
            font = null;
        }

        // Liberar sprites de la cache
        for (Texture texture : spriteCache.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        spriteCache.clear();
    }
}
