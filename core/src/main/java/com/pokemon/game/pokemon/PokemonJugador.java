package com.pokemon.game.pokemon;

import com.pokemon.game.item.Item;

/**
 * Representa un Pokémon que pertenece al jugador, con características adicionales como amistad y objetos equipados.
 * Hereda todas las propiedades base de un Pokémon y añade funcionalidades específicas para Pokémon entrenados.
 */
public class PokemonJugador extends Pokemon {
    private int amistad; // 0-255
    private Item objetoEquipado;
    private Entrenador entrenador; // ✅ NUEVO: Campo para el entrenador dueño

    /**
     * Crea un nuevo Pokémon del jugador con especie, apodo y nivel especificados.
     * Inicializa la amistad en un valor base y sin objeto equipado.
     */
    public PokemonJugador(EspeciePokemon especie, String apodo, int nivel) {
        super(especie, apodo, nivel);
        this.amistad = 70;
        this.objetoEquipado = null;
        this.entrenador = null; // ✅ Inicialmente sin entrenador
    }

    /**
     * Obtiene el entrenador dueño de este Pokémon.
     */
    public Entrenador getEntrenador() {
        return entrenador;
    }

    /**
     * Establece el entrenador dueño de este Pokémon.
     */
    public void setEntrenador(Entrenador entrenador) {
        this.entrenador = entrenador;
    }

    /**
     * Agrega experiencia al Pokémon y verifica si sube de nivel.
     * El sistema calcula automáticamente los aumentos de nivel múltiples si se gana suficiente experiencia.
     */
    public void ganarExperiencia(int expGanada) {
        experiencia += expGanada;

        // 100 exp por nivel (simple)
        while (experiencia >= nivel * 70) {
            subirNivel();
        }
    }

    /**
     * Incrementa el nivel del Pokémon y recalcula todas sus estadísticas.
     * Mantiene el porcentaje actual de PS al subir de nivel y aumenta la amistad.
     */
    private void subirNivel() {
        nivel++;

        // Recalcular stats
        int psAntes = psMaximos;
        calcularStats();

        // Curar PS proporcionalmente
        double porcentajePs = (double)psActual / psAntes;
        psActual = (int)(psMaximos * porcentajePs);

        // Aumentar amistad
        amistad = Math.min(255, amistad + 5);

        System.out.println("¡" + apodo + " subió al nivel " + nivel + "!");

        // Verificar si puede evolucionar
        verificarEvolucion();
    }

    /**
     * Verifica si el Pokémon cumple las condiciones para evolucionar.
     * Considera tanto el nivel como los objetos equipados que puedan desencadenar la evolución.
     */
    private void verificarEvolucion() {
        String itemEquipadoNombre = (objetoEquipado != null) ? objetoEquipado.getNombre() : null;
        if (especie.puedeEvolucionar(nivel, itemEquipadoNombre)) {
            System.out.println("¡" + apodo + " está listo para evolucionar a " + especie.getEvolucion() + "!");
        }
    }

    /**
     * Evoluciona este Pokémon a una nueva especie especificada.
     * Preserva el apodo, nivel, experiencia, movimientos y relación con el entrenador.
     */
    public PokemonJugador evolucionar(EspeciePokemon nuevaEspecie) {
        System.out.println("¡" + apodo + " está evolucionando!");

        // Crear nuevo Pokémon con la misma experiencia/nivel pero nueva especie
        PokemonJugador evolucion = new PokemonJugador(nuevaEspecie, apodo, nivel);

        // Copiar movimientos (simplificado - en realidad algunos movimientos pueden cambiar)
        for (Movimiento m : movimientos) {
            evolucion.aprenderMovimiento(m);
        }

        // Copiar PS proporcionalmente
        double porcentajePs = (double)psActual / psMaximos;
        evolucion.psActual = (int)(evolucion.psMaximos * porcentajePs);

        // Copiar experiencia
        evolucion.experiencia = experiencia;

        // ✅ NUEVO: También copiar el entrenador
        evolucion.setEntrenador(this.entrenador);

        System.out.println("¡" + apodo + " ha evolucionado a " + nuevaEspecie.getNombre() + "!");
        return evolucion;
    }

    /**
     * Obtiene el nivel actual de amistad del Pokémon (0-255).
     */
    public int getAmistad() { return amistad; }

    /**
     * Obtiene el objeto actualmente equipado en este Pokémon.
     */
    public Item getObjetoEquipado() { return objetoEquipado; }

    /**
     * Equipa o desequipa un objeto en este Pokémon.
     * Reemplaza cualquier objeto previamente equipado.
     */
    public void setObjetoEquipado(Item objeto) { this.objetoEquipado = objeto; }

    /**
     * Aumenta la amistad del Pokémon, sin exceder el valor máximo de 255.
     */
    public void aumentarAmistad(int cantidad) {
        amistad = Math.min(255, amistad + cantidad);
    }

    /**
     * Disminuye la amistad del Pokémon, sin caer por debajo del valor mínimo de 0.
     */
    public void disminuirAmistad(int cantidad) {
        amistad = Math.max(0, amistad - cantidad);
    }

    /**
     * Fuerza la evolución del Pokémon si es posible según su especie.
     * Método diseñado principalmente para pruebas de desarrollo.
     */
    public void evolucionarForzado() {
        if (especie.getEvolucion() != null) {
            EspeciePokemon nuevaEspecie = new EspeciePokemon(
                especie.getEvolucion(),
                especie.getTipo1(),
                especie.getTipo2(),
                especie.getPsBase() + 20,
                especie.getAtaqueBase() + 10,
                especie.getDefensaBase() + 10,
                especie.getAtaqueEspecialBase() + 10,
                especie.getDefensaEspecialBase() + 10,
                especie.getVelocidadBase() + 10,
                especie.getHabilidad(),
                especie.getTasaCaptura(),
                null, // No más evoluciones
                0,
                null
            );

            PokemonJugador evolucion = evolucionar(nuevaEspecie);

            System.out.println("Evolución completada!");
        }
    }
}
