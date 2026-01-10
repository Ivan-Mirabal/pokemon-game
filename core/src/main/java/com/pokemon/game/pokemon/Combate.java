package com.pokemon.game.pokemon;

import com.pokemon.game.item.Pokeball;

import java.util.ArrayList;
import java.util.List;

public class Combate {
    private Pokemon pokemonJugador;
    private Pokemon pokemonRival;
    private boolean turnoJugador;
    private List<String> historial;
    private boolean combateTerminado;
    private String motivoFin; // "victoria", "derrota", "captura", "huida"

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
        this.motivoFin = null;

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
            // No terminar el combate aquí, dejar que CombateScreen maneje
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

                // Registrar victoria si el Pokémon jugador es un PokemonJugador
                if (pokemonJugador instanceof PokemonJugador) {
                    PokemonJugador pj = (PokemonJugador) pokemonJugador;
                    if (pj.getEntrenador() != null) {
                        pj.getEntrenador().registrarVictoriaContraPokemon(
                            pokemonRival.getEspecie().getNombre()
                        );
                    }
                }

                combateTerminado = true;
                motivoFin = "victoria";
                return ResultadoTurno.POKEMON_DEBILITADO;
            }
        }

        // Cambiar turno
        turnoJugador = false;

        return ResultadoTurno.EXITO;
    }

    // Turno del rival (IA simple)
    public void ejecutarTurnoRival() {
        if (pokemonJugador.estaDebilitado() || pokemonRival.estaDebilitado()) {
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

                    if (pokemonJugador.estaDebilitado()) {
                        registrarEvento("¡" + pokemonJugador.getNombre() + " fue debilitado!");
                        // No terminar el combate aquí - dejar que el jugador cambie
                    }
                }
                break;
            }
        }

        // Cambiar turno
        turnoJugador = true;
    }

    // Método para intentar captura durante el combate
    // Método para intentar captura durante el combate
    public boolean intentarCaptura(Entrenador entrenador, Pokeball ball) {
        // 1. Intentar gastar el ítem
        boolean gastado = entrenador.getInventario().removerItem(ball.getNombre(), 1);

        if (!gastado) {
            registrarEvento("¡No tienes más " + ball.getNombre() + "!");
            return false;
        }

        // 2. Calcular éxito
        double porcentajePs = (double)pokemonRival.getPsActual() / pokemonRival.getPsMaximos();
        double probabilidad = (ball.getTasaCaptura() * 0.25) / (porcentajePs + 0.1);
        boolean exito = Math.random() < probabilidad;

        if (exito) {
            registrarEvento("¡Atrapaste a " + pokemonRival.getNombre() + "!");

            // 3. Crear la instancia para el jugador y añadir al equipo
            PokemonJugador nuevo;

            if (pokemonRival instanceof PokemonSalvaje) {
                // ✅ USAR CONVERSIÓN QUE COPIA MOVIMIENTOS
                PokemonSalvaje salvaje = (PokemonSalvaje) pokemonRival;
                nuevo = salvaje.convertirAJugador();

                // Mantener nombre personalizado si tenía apodo
                String nombreRival = pokemonRival.getNombre();
                String nombreEspecie = pokemonRival.getEspecie().getNombre();
                if (!nombreRival.equals(nombreEspecie)) {
                    nuevo.setApodo(nombreRival);
                }

                System.out.println("✅ Movimientos copiados: " + nuevo.getMovimientos().size());
            } else {
                // Para compatibilidad con Pokémon de entrenadores
                nuevo = new PokemonJugador(
                    pokemonRival.getEspecie(),
                    pokemonRival.getNombre(),
                    pokemonRival.getNivel()
                );

                // Copiar movimientos manualmente
                for (Movimiento movimiento : pokemonRival.getMovimientos()) {
                    Movimiento copia = new Movimiento(
                        movimiento.getNombre(),
                        movimiento.getTipo(),
                        movimiento.getPotencia(),
                        movimiento.getPrecision(),
                        movimiento.getPpMax(),
                        movimiento.isEsFisico(),
                        movimiento.getDescripcion()
                    );
                    copia.restaurarTodo();
                    nuevo.aprenderMovimiento(copia);
                }
            }

            // 4. Añadir al equipo del entrenador
            boolean añadido = entrenador.agregarPokemon(nuevo);
            if (!añadido) {
                registrarEvento("¡Pero el equipo estaba lleno!");
            }

            // 5. Registrar captura en la Pokédex
            entrenador.registrarCapturaPokemon(
                pokemonRival.getEspecie().getNombre(),
                "En combate"
            );

            combateTerminado = true;
            motivoFin = "captura";
            return true;
        } else {
            registrarEvento(pokemonRival.getNombre() + " se liberó...");
            turnoJugador = false; // El turno pasa al rival por fallar
            return false;
        }
    }

    public ResultadoTurno cambiarPokemon(Pokemon nuevoPokemon) {
        if (combateTerminado) {
            return ResultadoTurno.COMBATE_TERMINADO;
        }

        if (!turnoJugador) {
            return ResultadoTurno.NO_ES_TU_TURNO;
        }

        if (nuevoPokemon.estaDebilitado()) {
            return ResultadoTurno.POKEMON_DEBILITADO;
        }

        if (nuevoPokemon == pokemonJugador) {
            registrarEvento("¡" + nuevoPokemon.getNombre() + " ya está en combate!");
            return ResultadoTurno.MOVIMIENTO_INVALIDO;
        }

        // Cambiar Pokémon
        pokemonJugador = nuevoPokemon;
        registrarEvento("¡Adelante " + pokemonJugador.getNombre() + "!");

        // Cambiar turno
        turnoJugador = false;

        return ResultadoTurno.EXITO;
    }

    // Método para intentar huir
    public boolean intentarHuir() {
        // Fórmula simple basada en velocidad
        double probabilidad = (double) pokemonJugador.getVelocidad() /
            (pokemonJugador.getVelocidad() + pokemonRival.getVelocidad());

        boolean exito = Math.random() < probabilidad;

        if (exito) {
            registrarEvento("¡Has huido con éxito!");
            combateTerminado = true;
            motivoFin = "huida";
        } else {
            registrarEvento("¡No has podido huir!");
            // Si fallas, el rival ataca
            turnoJugador = false;
            ejecutarTurnoRival();
        }

        return exito;
    }

    // Método para registrar derrota por equipo completo
    public void registrarDerrotaCompleta() {
        combateTerminado = true;
        motivoFin = "derrota";
    }

    // Getters
    public Pokemon getPokemonJugador() { return pokemonJugador; }
    public Pokemon getPokemonRival() { return pokemonRival; }
    public boolean isTurnoJugador() { return turnoJugador; }
    public boolean isCombateTerminado() { return combateTerminado; }
    public List<String> getHistorial() { return new ArrayList<>(historial); }
    public String getMotivoFin() { return motivoFin; }

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
