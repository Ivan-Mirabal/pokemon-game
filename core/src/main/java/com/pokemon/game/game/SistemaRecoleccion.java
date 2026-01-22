package com.pokemon.game.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.pokemon.game.player.Player;
import com.pokemon.game.item.Recurso;
import java.util.Random;

/**
 * Sistema de recolección periódica de recursos en el mundo del juego que otorga
 * oportunidades temporales al jugador para recolectar materiales mientras explora.
 * Controla intervalos de tiempo, validación de posiciones y visualización de interfaz.
 */
public class SistemaRecoleccion {

    /** Referencia al jugador que realizará las recolecciones */
    private Player player;

    /** Generador de números aleatorios para determinar recursos y tiempos */
    private Random random;

    /** Indica si actualmente hay una oportunidad activa de recolección */
    private boolean oportunidadDisponible = false;

    /** Tiempo acumulado desde que terminó la última oportunidad de recolección */
    private float tiempoDesdeUltimaOportunidad = 0f;

    /** Intervalo fijo en segundos entre oportunidades de recolección consecutivas */
    private final float INTERVALO_OPORTUNIDAD = 45f;

    /** Duración máxima en segundos que una oportunidad de recolección permanece activa */
    private final float DURACION_OPORTUNIDAD = 10f;

    /** Tiempo transcurrido desde que comenzó la oportunidad de recolección actual */
    private float tiempoOportunidadActual = 0f;

    /** Controla si la posición actual del jugador permite realizar recolección */
    private boolean puedeRecolectarEnPosicion = false;

    /**
     * Inicializa el sistema de recolección asociándolo con un jugador específico
     * y configurando un tiempo inicial aleatorio para la primera oportunidad.
     *
     * @param player Instancia del jugador que utilizará el sistema
     */
    public SistemaRecoleccion(Player player) {
        this.player = player;
        this.random = new Random();
        // Establece un tiempo inicial aleatorio para variar el primer intervalo
        tiempoDesdeUltimaOportunidad = random.nextFloat() * 20f;
    }

    /**
     * Actualiza el estado interno del sistema controlando los tiempos de oportunidad
     * y validando si la posición actual del jugador permite recolección.
     *
     * @param delta Tiempo transcurrido en segundos desde la última actualización
     * @param estaColisionando Indica si el jugador está colisionando con obstáculos
     */
    public void actualizar(float delta, boolean estaColisionando) {
        tiempoDesdeUltimaOportunidad += delta;

        // Genera nueva oportunidad si ha pasado el intervalo y no hay una activa
        if (!oportunidadDisponible && tiempoDesdeUltimaOportunidad >= INTERVALO_OPORTUNIDAD) {
            oportunidadDisponible = true;
            tiempoOportunidadActual = 0f;
            tiempoDesdeUltimaOportunidad = 0f;
        }

        // Controla el tiempo de duración de la oportunidad actual
        if (oportunidadDisponible) {
            tiempoOportunidadActual += delta;

            // Expira la oportunidad si supera el tiempo máximo
            if (tiempoOportunidadActual >= DURACION_OPORTUNIDAD) {
                oportunidadDisponible = false;
                tiempoDesdeUltimaOportunidad = 0f;
            }
        }

        // Solo permite recolección si hay oportunidad activa y no hay colisiones
        puedeRecolectarEnPosicion = oportunidadDisponible && !estaColisionando;
    }

    /**
     * Intenta recolectar un recurso aleatorio si se cumplen todas las condiciones
     * necesarias. El recurso obtenido se añade al inventario del jugador si hay espacio.
     */
    public void intentarRecolectar() {
        if (!puedeRecolectarEnPosicion) {
            return;
        }

        // Selecciona aleatoriamente entre los tipos de recursos disponibles
        String[] recursos = {"Planta", "Guijarro", "Baya", "Metal"};
        String recursoNombre = recursos[random.nextInt(recursos.length)];

        // Crea una instancia del recurso seleccionado
        Recurso recurso = new Recurso(recursoNombre, recursoNombre);

        // Intenta añadir el recurso al inventario del jugador
        boolean exito = player.recolectarRecurso(recurso);

        if (exito) {
            System.out.println("[RECOLECCIÓN] ¡Has encontrado " + recursoNombre + "! (x1)");

            // Reinicia el sistema para una nueva oportunidad
            oportunidadDisponible = false;
            tiempoDesdeUltimaOportunidad = 0f;
        } else {
            System.out.println("[RECOLECCIÓN] No se pudo recolectar " + recursoNombre + " (inventario lleno)");
        }
    }

    /**
     * Renderiza la interfaz de usuario del sistema de recolección mostrando indicadores
     * visuales del estado actual, tiempo restante y controles disponibles.
     *
     * @param batch Lote de sprites para dibujar elementos gráficos
     * @param font Fuente de texto para mostrar información
     * @param screenWidth Ancho total de la pantalla en píxeles
     * @param screenHeight Alto total de la pantalla en píxeles
     */
    public void dibujarInterfaz(SpriteBatch batch, BitmapFont font, float screenWidth, float screenHeight) {
        if (batch == null || font == null || player == null) return;

        // Solo muestra interfaz cuando hay oportunidad activa
        if (!oportunidadDisponible) return;

        float botonX = screenWidth - 220;
        float botonY = 30;

        // Guarda los estados originales de color para restauración posterior
        Color batchColorOriginal = batch.getColor();
        Color fontColorOriginal = font.getColor();

        try {
            if (puedeRecolectarEnPosicion) {
                // Botón activo (color verde) cuando se puede recolectar
                batch.setColor(0.1f, 0.7f, 0.2f, 0.9f);
                batch.draw(player.getWhitePixel(), botonX, botonY, 200, 35);
                batch.setColor(Color.WHITE);

                font.setColor(Color.WHITE);
                font.getData().setScale(0.9f);
                font.draw(batch, "E - RECOLECTAR", botonX + 10, botonY + 22);
                font.getData().setScale(1.0f);
            } else {
                // Botón inactivo (color rojo) cuando hay obstáculos
                batch.setColor(0.7f, 0.1f, 0.1f, 0.9f);
                batch.draw(player.getWhitePixel(), botonX, botonY, 200, 35);
                batch.setColor(Color.WHITE);

                font.setColor(Color.WHITE);
                font.getData().setScale(0.9f);
                font.draw(batch, "VE A ZONA DESPEJADA", botonX + 10, botonY + 22);
                font.getData().setScale(1.0f);
            }

            // Muestra el tiempo restante de la oportunidad actual
            float tiempoRestante = DURACION_OPORTUNIDAD - tiempoOportunidadActual;
            font.setColor(Color.YELLOW);
            font.getData().setScale(0.7f);
            font.draw(batch, String.format("%.0fs", tiempoRestante), botonX + 180, botonY + 10);
            font.getData().setScale(1.0f);

        } finally {
            // Restaura los colores originales para evitar efectos no deseados
            batch.setColor(batchColorOriginal);
            font.setColor(fontColorOriginal);
        }
    }

    /**
     * Indica si actualmente existe una oportunidad activa de recolección.
     *
     * @return true si hay oportunidad disponible, false en caso contrario
     */
    public boolean isOportunidadDisponible() {
        return oportunidadDisponible;
    }

    /**
     * Indica si el jugador está en una posición válida para realizar recolección
     * durante una oportunidad activa.
     *
     * @return true si puede recolectar en la posición actual, false en caso contrario
     */
    public boolean isPuedeRecolectarEnPosicion() {
        return puedeRecolectarEnPosicion;
    }
}
