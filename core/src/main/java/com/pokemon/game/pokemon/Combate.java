package com.pokemon.game.pokemon;

import java.util.ArrayList;
import java.util.List;

public class Combate {
    private Pokemon pokemonJugador;
    private Pokemon pokemonRival;
    private boolean turnoJugador;
    private List<String> historial;
    private boolean combateTerminado;

    public enum ResultadoTurno {
        EXITO,
        NO_ES_TU_TURNO,
        MOVIMIENTO_INVALIDO,
        SIN_PP,
        POKEMON_DEBILITADO,
        COMBATE_TERMINADO
    }

    public Combate(Pokemon jugador, Pokemon rival) {
        this.pokemonJugador = jugador;
        this.pokemonRival = rival;
        this.historial = new ArrayList<>();
        this.combateTerminado = false;

        // Determinar quién va primero por velocidad
        turnoJugador = jugador.getVelocidad() >= rival.getVelocidad();

        registrarEvento("¡Comienza el combate!");
        registrarEvento(jugador.getNombre() + " (PS: " + jugador.getPsActual() + ") vs " +
            rival.getNombre() + " (PS: " + rival.getPsActual() + ")");

        if (turnoJugador) {
            registrarEvento("¡Tú atacas primero!");
        } else {
            registrarEvento("¡El rival ataca primero!");
        }
    }

    // Turno del jugador
    public ResultadoTurno ejecutarTurnoJugador(int indiceMovimiento) {
        if (combateTerminado) {
            return ResultadoTurno.COMBATE_TERMINADO;
        }

        if (!turnoJugador) {
            return ResultadoTurno.NO_ES_TU_TURNO;
        }

        if (pokemonJugador.estaDebilitado() || pokemonRival.estaDebilitado()) {
            combateTerminado = true;
            return ResultadoTurno.COMBATE_TERMINADO;
        }

        // Obtener movimiento
        List<Movimiento> movimientos = pokemonJugador.getMovimientos();
        if (indiceMovimiento < 0 || indiceMovimiento >= movimientos.size()) {
            return ResultadoTurno.MOVIMIENTO_INVALIDO;
        }

        Movimiento movimiento = movimientos.get(indiceMovimiento);
        if (!movimiento.puedeUsar()) {
            return ResultadoTurno.SIN_PP;
        }

        // Usar movimiento
        movimiento.usar();
        int daño = movimiento.calcularDaño(pokemonJugador, pokemonRival);

        if (daño == 0) {
            registrarEvento("¡El ataque de " + pokemonJugador.getNombre() + " falló!");
        } else {
            pokemonRival.recibirDaño(daño);
            registrarEvento(pokemonJugador.getNombre() + " usa " + movimiento.getNombre() +
                " y causa " + daño + " puntos de daño!");

            // Verificar si el rival fue debilitado
            if (pokemonRival.estaDebilitado()) {
                registrarEvento("¡" + pokemonRival.getNombre() + " fue debilitado!");
                combateTerminado = true;
                return ResultadoTurno.POKEMON_DEBILITADO;
            }
        }

        // Cambiar turno y ejecutar turno rival
        turnoJugador = false;

        // Si el rival sigue vivo, ejecutar su turno
        if (!pokemonRival.estaDebilitado()) {
            ejecutarTurnoRival();
        }

        return ResultadoTurno.EXITO;
    }

    // Turno del rival (IA simple)
    private void ejecutarTurnoRival() {
        if (pokemonJugador.estaDebilitado() || pokemonRival.estaDebilitado()) {
            combateTerminado = true;
            return;
        }

        // IA simple: usa el primer movimiento disponible
        for (Movimiento movimiento : pokemonRival.getMovimientos()) {
            if (movimiento.puedeUsar()) {
                movimiento.usar();
                int daño = movimiento.calcularDaño(pokemonRival, pokemonJugador);

                if (daño == 0) {
                    registrarEvento("¡El ataque de " + pokemonRival.getNombre() + " falló!");
                } else {
                    pokemonJugador.recibirDaño(daño);
                    registrarEvento(pokemonRival.getNombre() + " usa " + movimiento.getNombre() +
                        " y causa " + daño + " puntos de daño!");

                    // Verificar si el jugador fue debilitado
                    if (pokemonJugador.estaDebilitado()) {
                        registrarEvento("¡" + pokemonJugador.getNombre() + " fue debilitado!");
                        combateTerminado = true;
                    }
                }
                break;
            }
        }

        // Cambiar turno
        turnoJugador = true;
    }

    // Método para intentar captura durante el combate
    public boolean intentarCaptura(float multiplicadorBall) {
        if (!(pokemonRival instanceof PokemonSalvaje)) {
            registrarEvento("¡No puedes capturar este Pokémon!");
            return false;
        }

        PokemonSalvaje salvaje = (PokemonSalvaje) pokemonRival;
        double porcentajePs = (double)pokemonRival.getPsActual() / pokemonRival.getPsMaximos();
        boolean exito = salvaje.intentarCaptura(multiplicadorBall, porcentajePs);

        if (exito) {
            registrarEvento("¡Enhorabuena! Has capturado a " + pokemonRival.getNombre() + "!");
            combateTerminado = true;
        } else {
            registrarEvento("¡Oh no! " + pokemonRival.getNombre() + " ha escapado de la Poké Ball.");
            // El combate continúa
        }

        return exito;
    }

    // Getters
    public Pokemon getPokemonJugador() { return pokemonJugador; }
    public Pokemon getPokemonRival() { return pokemonRival; }
    public boolean isTurnoJugador() { return turnoJugador; }
    public boolean isCombateTerminado() { return combateTerminado; }
    public List<String> getHistorial() { return new ArrayList<>(historial); }

    public Pokemon getGanador() {
        if (!combateTerminado) return null;
        if (pokemonJugador.estaDebilitado()) return pokemonRival;
        if (pokemonRival.estaDebilitado()) return pokemonJugador;
        return null;
    }

    public Pokemon getPerdedor() {
        if (!combateTerminado) return null;
        if (pokemonJugador.estaDebilitado()) return pokemonJugador;
        if (pokemonRival.estaDebilitado()) return pokemonRival;
        return null;
    }

    private void registrarEvento(String evento) {
        historial.add(evento);
        System.out.println("[Combate] " + evento);
    }
}
