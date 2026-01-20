package com.pokemon.game.pokemon;

import com.pokemon.game.item.Pokeball;
import com.pokemon.game.item.Recurso;
import com.pokemon.game.pokedex.PokedexEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    // Reemplaza el método ejecutarTurnoRival() con esta versión mejorada:

    public void ejecutarTurnoRival() {
        if (pokemonJugador.estaDebilitado() || pokemonRival.estaDebilitado()) {
            return;
        }

        // 1. Obtener todos los movimientos disponibles (con PP)
        List<Movimiento> movimientosDisponibles = new ArrayList<>();
        for (Movimiento movimiento : pokemonRival.getMovimientos()) {
            if (movimiento.puedeUsar()) {
                movimientosDisponibles.add(movimiento);
            }
        }

        // 2. Si no hay movimientos disponibles, no hace nada
        if (movimientosDisponibles.isEmpty()) {
            registrarEvento("¡" + pokemonRival.getNombre() + " no tiene movimientos disponibles!");
            turnoJugador = true;
            return;
        }

        // 3. Seleccionar un movimiento ALEATORIO
        int indiceAleatorio = (int)(Math.random() * movimientosDisponibles.size());
        Movimiento movimiento = movimientosDisponibles.get(indiceAleatorio);

        // 4. Usar el movimiento
        movimiento.usar();
        int daño = movimiento.calcularDaño(pokemonRival, pokemonJugador);

        // 5. Registrar resultado
        if (daño == 0) {
            registrarEvento("¡El ataque de " + pokemonRival.getNombre() + " falló!");
        } else {
            pokemonJugador.recibirDaño(daño);
            registrarEvento(pokemonRival.getNombre() + " usa " + movimiento.getNombre() +
                " y causa " + daño + " puntos de daño!");

            if (pokemonJugador.estaDebilitado()) {
                registrarEvento("¡" + pokemonJugador.getNombre() + " fue debilitado!");
            }
        }

        // 6. Cambiar turno
        turnoJugador = true;
    }

    // Método para intentar captura durante el combate
    public boolean intentarCaptura(Entrenador entrenador, Pokeball ball) {
        // 1. Intentar gastar el ítem
        boolean gastado = entrenador.getInventario().removerItem(ball.getNombre(), 1);

        if (!gastado) {
            registrarEvento("¡No tienes más " + ball.getNombre() + "!");
            return false;
        }

        // 2. Calcular éxito (fórmula especial para legendarios)
        double porcentajePs = (double)pokemonRival.getPsActual() / pokemonRival.getPsMaximos();
        double probabilidad;

        // Verificar si es Pokémon legendario
        boolean esLegendario = esPokemonLegendario(pokemonRival.getEspecie().getNombre());

        if (esLegendario) {
            // FÓRMULA MÁS DIFÍCIL PARA LEGENDARIOS
            // Base más baja (0.1 vs 0.25) y penalización mayor por PS altos
            probabilidad = (ball.getTasaCaptura() * 0.1) / (porcentajePs * 2.0 + 0.05);

            // Penalización adicional si no está debilitado
            if (porcentajePs > 0.5) {
                probabilidad *= 0.5; // 50% menos de probabilidad
            }

            // Asegurar límites razonables para legendarios
            probabilidad = Math.max(0.01, Math.min(0.5, probabilidad));

        } else {
            // Fórmula normal para Pokémon comunes
            probabilidad = (ball.getTasaCaptura() * 0.25) / (porcentajePs + 0.1);
        }

        boolean exito = Math.random() < probabilidad;

        if (exito) {
            registrarEvento("¡Atrapaste a " + pokemonRival.getNombre() + "!");

            // 3. Si es legendario, mensaje especial
            if (esLegendario) {
                registrarEvento("¡¡¡HAS CAPTURADO UN POKÉMON LEGENDARIO!!!");
                registrarEvento("¡Logro máximo completado!");

                // AUTO-COMPLETAR INVESTIGACIÓN (Nivel 10 inmediato)
                PokedexEntry entrada = entrenador.getPokedex().getEntrada(
                    pokemonRival.getEspecie().getNombre()
                );

                if (entrada != null) {
                    // Forzar nivel 10 de investigación
                    int intentosNecesarios = 10 - entrada.getNivelInvestigacion();
                    for (int i = 0; i < intentosNecesarios; i++) {
                        entrada.registrarVictoriaCombate();
                    }
                    registrarEvento(pokemonRival.getEspecie().getNombre() +
                        " ha sido registrado como COMPLETAMENTE INVESTIGADO (Nivel 10)");
                }

                // Otorgar recompensas especiales
                entrenador.ganarDinero(10000); // 10,000 de dinero
                registrarEvento("¡Has obtenido $10,000 como recompensa!");

                // Finalizar el objetivo central de la simulación
                registrarEvento("¡OBJETIVO PRINCIPAL COMPLETADO!");
            }

            // 4. Crear la instancia para el jugador y añadir al equipo
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

            // 5. Añadir al equipo del entrenador
            boolean añadido = entrenador.agregarPokemon(nuevo);
            if (!añadido) {
                registrarEvento("¡Pero el equipo estaba lleno!");
            }

            // 6. Registrar captura en la Pokédex
            entrenador.registrarCapturaPokemon(
                pokemonRival.getEspecie().getNombre(),
                "En combate"
            );

            combateTerminado = true;
            motivoFin = "captura";
            return true;

        } else {
            registrarEvento(pokemonRival.getNombre() + " se liberó...");

            // Mensaje especial para legendarios fallidos
            if (esLegendario) {
                registrarEvento("¡El poder legendario es demasiado fuerte!");
            }

            turnoJugador = false; // El turno pasa al rival por fallar
            return false;
        }
    }

    // Método auxiliar para detectar Pokémon legendarios
    private boolean esPokemonLegendario(String nombrePokemon) {
        // Lista de Pokémon legendarios
        String[] legendarios = {
            "Arceus", "Mewtwo", "Rayquaza", "Groudon", "Kyogre",
            "Dialga", "Palkia", "Giratina", "Lugia", "Ho-Oh",
            "Zapdos", "Moltres", "Articuno", "Mew", "Celebi",
            "Jirachi", "Deoxys", "Darkrai", "Shaymin", "Arceus"
        };

        for (String legendario : legendarios) {
            if (nombrePokemon.equalsIgnoreCase(legendario)) {
                return true;
            }
        }
        return false;
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

    public void otorgarRecompensasVictoria(Entrenador entrenador) {
        if (motivoFin == null || !motivoFin.equals("victoria")) {
            return;
        }

        // Calcular recompensas
        SistemaRecompensas.RecompensaCombate recompensa =
            SistemaRecompensas.calcularRecompensaVictoria(pokemonJugador, pokemonRival);

        // 1. Dar experiencia al Pokémon que luchó
        if (pokemonJugador instanceof PokemonJugador) {
            PokemonJugador pj = (PokemonJugador) pokemonJugador;
            pj.ganarExperiencia(recompensa.experiencia);
            registrarEvento(pj.getApodo() + " ganó " + recompensa.experiencia + " puntos de experiencia!");
        }

        // 2. Dar recurso
        if (recompensa.recursoGanado != null && recompensa.cantidadRecurso > 0) {
            Recurso recurso = new Recurso(recompensa.recursoGanado, recompensa.recursoGanado);
            boolean agregado = entrenador.getInventario().agregarItem(recurso, recompensa.cantidadRecurso);
            if (agregado) {
                registrarEvento("¡Obtuviste " + recompensa.cantidadRecurso + "x " + recompensa.recursoGanado + "!");
            }
        }

        // 3. Registrar victoria en Pokédex
        entrenador.registrarVictoriaContraPokemon(pokemonRival.getEspecie().getNombre());
    }

    public void aplicarPenalizacionDerrota(Entrenador entrenador) {
        // 30% de chance de perder un recurso aleatorio
        Random rand = new Random();
        if (rand.nextDouble() < 0.3) {
            String[] recursosDisponibles = {"Planta", "Guijarro", "Baya", "Metal"};
            String recursoPerdido = recursosDisponibles[rand.nextInt(recursosDisponibles.length)];

            if (entrenador.getInventario().removerItem(recursoPerdido, 1)) {
                registrarEvento("Perdiste 1 " + recursoPerdido + " del inventario...");
            }
        }

        // Todos los Pokémon del equipo quedan debilitados
        for (PokemonJugador p : entrenador.getEquipo()) {
            if (!p.estaDebilitado()) {
                p.recibirDaño(p.getPsActual());
            }
        }
        registrarEvento("Todos tus Pokémon quedaron debilitados...");
    }

    public void registrarDerrotaCompleta(Entrenador entrenador) {
        combateTerminado = true;
        motivoFin = "derrota";
        registrarEvento("¡Todos tus Pokémon fueron derrotados!");

        aplicarPenalizacionDerrota(entrenador);
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
