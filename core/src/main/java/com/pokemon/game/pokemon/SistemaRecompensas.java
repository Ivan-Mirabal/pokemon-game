package com.pokemon.game.pokemon;

import com.pokemon.game.item.Recurso;
import java.util.Random;

public class SistemaRecompensas {

    private static final Random rand = new Random();

    public static class RecompensaCombate {
        public int experiencia;
        public String recursoGanado;
        public int cantidadRecurso;

        public RecompensaCombate(int exp, String recurso, int cantidad) {
            this.experiencia = exp;
            this.recursoGanado = recurso;
            this.cantidadRecurso = cantidad;
        }
    }

    public static RecompensaCombate calcularRecompensaVictoria(Pokemon vencedor, Pokemon perdedor) {
        int nivelPerdedor = perdedor.getNivel();

        // 1. Experiencia base: nivel * 10
        int expBase = nivelPerdedor * 10;

        // 2. Modificador por diferencia de nivel
        int diferenciaNivel = vencedor.getNivel() - nivelPerdedor;
        if (diferenciaNivel < -5) {
            expBase = (int)(expBase * 1.5); // Bonus por vencer Pokémon más fuerte
        } else if (diferenciaNivel > 5) {
            expBase = (int)(expBase * 0.5); // Penalización por Pokémon mucho más débil
        }

        // 3. Recurso aleatorio (SOLO los que ya tienes en recetas)
        String[] recursosDisponibles = {"Planta", "Guijarro", "Baya", "Metal"};
        String recursoGanado = recursosDisponibles[rand.nextInt(recursosDisponibles.length)];
        int cantidadRecurso = 1 + rand.nextInt(3); // 1-3 unidades

        return new RecompensaCombate(expBase, recursoGanado, cantidadRecurso);
    }
}
