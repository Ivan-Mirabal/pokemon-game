package com.pokemon.game.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.pokemon.game.player.Player;
import com.pokemon.game.item.Recurso;
import java.util.Random;

public class SistemaRecoleccion {

    private Player player;
    private Random random;
    private boolean oportunidadDisponible = false;
    private float tiempoDesdeUltimaOportunidad = 0f;
    private final float INTERVALO_OPORTUNIDAD = 45f; // 45 segundos
    private final float DURACION_OPORTUNIDAD = 10f; // 10 segundos para recolectar
    private float tiempoOportunidadActual = 0f;

    // Para evitar recolectar en colisiones
    private boolean puedeRecolectarEnPosicion = false;

    public SistemaRecoleccion(Player player) {
        this.player = player;
        this.random = new Random();
        // Empezar con un intervalo aleatorio para que no todos sean iguales
        tiempoDesdeUltimaOportunidad = random.nextFloat() * 20f; // 0-20 segundos inicial
    }

    public void actualizar(float delta, boolean estaColisionando) {
        // Siempre actualizar tiempo
        tiempoDesdeUltimaOportunidad += delta;

        // Si NO hay oportunidad disponible y ha pasado el intervalo → crear nueva oportunidad
        if (!oportunidadDisponible && tiempoDesdeUltimaOportunidad >= INTERVALO_OPORTUNIDAD) {
            oportunidadDisponible = true;
            tiempoOportunidadActual = 0f;
            tiempoDesdeUltimaOportunidad = 0f;
        }

        // Si hay oportunidad disponible, actualizar su tiempo
        if (oportunidadDisponible) {
            tiempoOportunidadActual += delta;

            // Verificar si la oportunidad expiró
            if (tiempoOportunidadActual >= DURACION_OPORTUNIDAD) {
                oportunidadDisponible = false;
                tiempoDesdeUltimaOportunidad = 0f;
            }
        }

        // Solo se puede recolectar si hay oportunidad y no está colisionando
        puedeRecolectarEnPosicion = oportunidadDisponible && !estaColisionando;
    }

    public void intentarRecolectar() {
        if (!puedeRecolectarEnPosicion) {
            return;
        }

        // Obtener recurso aleatorio
        String[] recursos = {"Planta", "Guijarro", "Baya", "Metal"};
        String recursoNombre = recursos[random.nextInt(recursos.length)];

        // Crear el recurso
        Recurso recurso = new Recurso(recursoNombre, recursoNombre);

        // Intentar agregar al inventario
        boolean exito = player.recolectarRecurso(recurso);

        if (exito) {
            System.out.println("[RECOLECCIÓN] ¡Has encontrado " + recursoNombre + "! (x1)");

            // Resetear para nueva oportunidad
            oportunidadDisponible = false;
            tiempoDesdeUltimaOportunidad = 0f;
        } else {
            System.out.println("[RECOLECCIÓN] No se pudo recolectar " + recursoNombre + " (inventario lleno)");
        }
    }

    public void dibujarInterfaz(SpriteBatch batch, BitmapFont font, float screenWidth, float screenHeight) {
        if (batch == null || font == null || player == null) return;

        // Solo mostrar si hay oportunidad disponible
        if (!oportunidadDisponible) return;

        float botonX = screenWidth - 220;
        float botonY = 30;

        // Guardar colores originales
        Color batchColorOriginal = batch.getColor();
        Color fontColorOriginal = font.getColor();

        try {
            if (puedeRecolectarEnPosicion) {
                // Botón activo (verde)
                batch.setColor(0.1f, 0.7f, 0.2f, 0.9f);
                batch.draw(player.getWhitePixel(), botonX, botonY, 200, 35);
                batch.setColor(Color.WHITE);

                font.setColor(Color.WHITE);
                font.getData().setScale(0.9f);
                font.draw(batch, "E - RECOLECTAR", botonX + 10, botonY + 22);
                font.getData().setScale(1.0f);
            } else {
                // Oportunidad disponible pero no en posición válida (rojo)
                batch.setColor(0.7f, 0.1f, 0.1f, 0.9f);
                batch.draw(player.getWhitePixel(), botonX, botonY, 200, 35);
                batch.setColor(Color.WHITE);

                font.setColor(Color.WHITE);
                font.getData().setScale(0.9f);
                font.draw(batch, "VE A ZONA DESPEJADA", botonX + 10, botonY + 22);
                font.getData().setScale(1.0f);
            }

            // Mostrar tiempo restante de oportunidad
            float tiempoRestante = DURACION_OPORTUNIDAD - tiempoOportunidadActual;
            font.setColor(Color.YELLOW);
            font.getData().setScale(0.7f);
            font.draw(batch, String.format("%.0fs", tiempoRestante), botonX + 180, botonY + 10);
            font.getData().setScale(1.0f);

        } finally {
            // Restaurar colores
            batch.setColor(batchColorOriginal);
            font.setColor(fontColorOriginal);
        }
    }

    public boolean isOportunidadDisponible() {
        return oportunidadDisponible;
    }

    public boolean isPuedeRecolectarEnPosicion() {
        return puedeRecolectarEnPosicion;
    }
}
