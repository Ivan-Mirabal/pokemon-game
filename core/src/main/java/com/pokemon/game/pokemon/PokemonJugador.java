package com.pokemon.game.pokemon;

import com.pokemon.game.item.Item;

public class PokemonJugador extends Pokemon {
    private int amistad; // 0-255
    private Item objetoEquipado;
    private Entrenador entrenador; // ✅ NUEVO: Campo para el entrenador dueño

    public PokemonJugador(EspeciePokemon especie, String apodo, int nivel) {
        super(especie, apodo, nivel);
        this.amistad = 70;
        this.objetoEquipado = null;
        this.entrenador = null; // ✅ Inicialmente sin entrenador
    }

    // ✅ NUEVO: Métodos getter y setter para entrenador
    public Entrenador getEntrenador() {
        return entrenador;
    }

    public void setEntrenador(Entrenador entrenador) {
        this.entrenador = entrenador;
    }

    // Sistema de experiencia
    public void ganarExperiencia(int expGanada) {
        experiencia += expGanada;

        // 100 exp por nivel (simple)
        while (experiencia >= nivel * 100) {
            subirNivel();
        }
    }

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

    private void verificarEvolucion() {
        String itemEquipadoNombre = (objetoEquipado != null) ? objetoEquipado.getNombre() : null;
        if (especie.puedeEvolucionar(nivel, itemEquipadoNombre)) {
            System.out.println("¡" + apodo + " está listo para evolucionar a " + especie.getEvolucion() + "!");
            // Aquí podrías pedir confirmación al jugador
        }
    }

    // Método para evolucionar
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

    // Getters y Setters específicos
    public int getAmistad() { return amistad; }
    public Item getObjetoEquipado() { return objetoEquipado; }
    public void setObjetoEquipado(Item objeto) { this.objetoEquipado = objeto; }

    public void aumentarAmistad(int cantidad) {
        amistad = Math.min(255, amistad + cantidad);
    }

    public void disminuirAmistad(int cantidad) {
        amistad = Math.max(0, amistad - cantidad);
    }

    // Evolución forzada (para pruebas)
    public void evolucionarForzado() {
        if (especie.getEvolucion() != null) {
            // En un sistema real, buscarías la nueva especie en un registro
            // Por ahora, creamos una especie básica
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

            // Reemplazar este Pokémon con su evolución
            PokemonJugador evolucion = evolucionar(nuevaEspecie);

            // ✅ NUEVO: También deberías reemplazar en el equipo del entrenador
            // Esto es más complejo y debería hacerse fuera de esta clase
            // Por ahora, solo mostramos el mensaje
            System.out.println("Evolución completada!");
        }
    }
}
