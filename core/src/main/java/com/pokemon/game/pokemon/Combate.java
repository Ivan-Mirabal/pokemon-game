package com.pokemon.game.pokemon;

import com.pokemon.game.item.Pokeball;
import com.pokemon.game.item.Recurso;
import com.pokemon.game.pokedex.PokedexEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gestiona la lógica completa de un combate Pokémon entre un Pokémon del jugador
 * y un Pokémon rival, incluyendo turnos, movimientos, captura y recompensas.
 * Implementa un sistema de combate por turnos con mecánicas de velocidad,
 * captura de Pokémon salvajes y efectos especiales para Pokémon legendarios.
 */
public class Combate {

    /** Pokémon controlado por el jugador que actualmente está en combate */
    private Pokemon pokemonJugador;

    /** Pokémon oponente contra el que se está luchando */
    private Pokemon pokemonRival;

    /** Indica si es el turno del jugador (true) o del rival (false) */
    private boolean turnoJugador;

    /** Registro histórico de todos los eventos ocurridos durante el combate */
    private List<String> historial;

    /** Bandera que indica si el combate ha finalizado por cualquier motivo */
    private boolean combateTerminado;

    /** Motivo por el cual terminó el combate: "victoria", "derrota", "captura", "huida" */
    private String motivoFin;

    /**
     * Resultados posibles al intentar ejecutar un turno del jugador,
     * utilizados para comunicar el estado de la acción al sistema de interfaz.
     */
    public enum ResultadoTurno {
        /** La acción del turno se ejecutó correctamente */
        EXITO,
        /** Se intentó actuar cuando no era el turno del jugador */
        NO_ES_TU_TURNO,
        /** Se seleccionó un índice de movimiento no válido */
        MOVIMIENTO_INVALIDO,
        /** El movimiento seleccionado no tiene puntos de poder (PP) disponibles */
        SIN_PP,
        /** El Pokémon objetivo ya está debilitado */
        POKEMON_DEBILITADO,
        /** El combate ya ha terminado y no se pueden realizar más acciones */
        COMBATE_TERMINADO
    }

    /**
     * Inicializa un nuevo combate configurando los Pokémon participantes,
     * determinando el primer turno por velocidad y registrando el evento inicial.
     *
     * @param jugador Pokémon controlado por el jugador que inicia el combate
     * @param rival Pokémon oponente contra el que se va a combatir
     */
    public Combate(Pokemon jugador, Pokemon rival) {
        this.pokemonJugador = jugador;
        this.pokemonRival = rival;
        this.historial = new ArrayList<>();
        this.combateTerminado = false;
        this.motivoFin = null;

        // Determina qué Pokémon ataca primero comparando sus estadísticas de velocidad
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

    /**
     * Ejecuta el turno del jugador utilizando el movimiento seleccionado.
     * Realiza todas las validaciones necesarias antes de aplicar el daño
     * y cambia el turno al rival si la acción fue exitosa.
     *
     * @param indiceMovimiento Índice del movimiento a utilizar (0-3)
     * @return Resultado de la operación según el enum ResultadoTurno
     */
    public ResultadoTurno ejecutarTurnoJugador(int indiceMovimiento) {
        if (combateTerminado) {
            return ResultadoTurno.COMBATE_TERMINADO;
        }

        if (!turnoJugador) {
            return ResultadoTurno.NO_ES_TU_TURNO;
        }

        if (pokemonJugador.estaDebilitado() || pokemonRival.estaDebilitado()) {
            return ResultadoTurno.COMBATE_TERMINADO;
        }

        // Obtiene el movimiento seleccionado de la lista del Pokémon
        List<Movimiento> movimientos = pokemonJugador.getMovimientos();
        if (indiceMovimiento < 0 || indiceMovimiento >= movimientos.size()) {
            return ResultadoTurno.MOVIMIENTO_INVALIDO;
        }

        Movimiento movimiento = movimientos.get(indiceMovimiento);
        if (!movimiento.puedeUsar()) {
            return ResultadoTurno.SIN_PP;
        }

        // Utiliza el movimiento y calcula el daño infligido
        movimiento.usar();
        int daño = movimiento.calcularDaño(pokemonJugador, pokemonRival);

        if (daño == 0) {
            registrarEvento("¡El ataque de " + pokemonJugador.getNombre() + " falló!");
        } else {
            pokemonRival.recibirDaño(daño);
            registrarEvento(pokemonJugador.getNombre() + " usa " + movimiento.getNombre() +
                " y causa " + daño + " puntos de daño!");

            // Verifica si el Pokémon rival fue debilitado por el ataque
            if (pokemonRival.estaDebilitado()) {
                registrarEvento("¡" + pokemonRival.getNombre() + " fue debilitado!");

                combateTerminado = true;
                motivoFin = "victoria";
                return ResultadoTurno.POKEMON_DEBILITADO;
            }
        }

        // Transfiere el control al rival para su siguiente turno
        turnoJugador = false;

        return ResultadoTurno.EXITO;
    }

    /**
     * Ejecuta el turno del rival utilizando una IA simple que selecciona
     * aleatoriamente entre los movimientos disponibles con PP restantes.
     * El daño se calcula y aplica al Pokémon del jugador, finalizando el
     * combate si resulta debilitado.
     */
    public void ejecutarTurnoRival() {
        if (pokemonJugador.estaDebilitado() || pokemonRival.estaDebilitado()) {
            return;
        }

        // Filtra solo los movimientos que tienen puntos de poder disponibles
        List<Movimiento> movimientosDisponibles = new ArrayList<>();
        for (Movimiento movimiento : pokemonRival.getMovimientos()) {
            if (movimiento.puedeUsar()) {
                movimientosDisponibles.add(movimiento);
            }
        }

        // Si no hay movimientos disponibles, el rival no puede atacar
        if (movimientosDisponibles.isEmpty()) {
            registrarEvento("¡" + pokemonRival.getNombre() + " no tiene movimientos disponibles!");
            turnoJugador = true;
            return;
        }

        // Selecciona un movimiento aleatorio de los disponibles
        int indiceAleatorio = (int)(Math.random() * movimientosDisponibles.size());
        Movimiento movimiento = movimientosDisponibles.get(indiceAleatorio);

        // Usa el movimiento y calcula el daño infligido
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
            }
        }

        // Devuelve el control al jugador para el siguiente turno
        turnoJugador = true;
    }

    /**
     * Intenta capturar al Pokémon rival utilizando una Pokéball del inventario.
     * Implementa fórmulas de captura diferenciadas para Pokémon comunes y legendarios,
     * otorgando recompensas especiales y completando la investigación del Pokédex
     * en caso de captura exitosa de un legendario.
     *
     * @param entrenador Entrenador que está intentando la captura
     * @param ball Pokéball a utilizar para la captura
     * @return true si la captura fue exitosa, false en caso contrario
     */
    public boolean intentarCaptura(Entrenador entrenador, Pokeball ball) {
        // Verifica que el entrenador tenga la Pokéball en su inventario
        boolean gastado = entrenador.getInventario().removerItem(ball.getNombre(), 1);

        if (!gastado) {
            registrarEvento("¡No tienes más " + ball.getNombre() + "!");
            return false;
        }

        // Calcula la probabilidad de captura basada en PS restantes y tipo de Pokémon
        double porcentajePs = (double)pokemonRival.getPsActual() / pokemonRival.getPsMaximos();
        double probabilidad;

        boolean esLegendario = esPokemonLegendario(pokemonRival.getEspecie().getNombre());

        if (esLegendario) {
            // Fórmula más restrictiva para Pokémon legendarios
            probabilidad = (ball.getTasaCaptura() * 0.1) / (porcentajePs * 2.0 + 0.05);

            if (porcentajePs > 0.5) {
                probabilidad *= 0.5;
            }

            probabilidad = Math.max(0.01, Math.min(0.5, probabilidad));

        } else {
            // Fórmula estándar para Pokémon comunes
            probabilidad = (ball.getTasaCaptura() * 0.25) / (porcentajePs + 0.1);
        }

        boolean exito = Math.random() < probabilidad;

        if (exito) {
            registrarEvento("¡Atrapaste a " + pokemonRival.getNombre() + "!");

            // Otorga recompensas especiales por capturar un Pokémon legendario
            if (esLegendario) {
                registrarEvento("¡¡¡HAS CAPTURADO UN POKÉMON LEGENDARIO!!!");
                registrarEvento("¡Logro máximo completado!");

                PokedexEntry entrada = entrenador.getPokedex().getEntrada(
                    pokemonRival.getEspecie().getNombre()
                );

                if (entrada != null) {
                    int intentosNecesarios = 10 - entrada.getNivelInvestigacion();
                    for (int i = 0; i < intentosNecesarios; i++) {
                        entrada.registrarVictoriaCombate();
                    }
                    registrarEvento(pokemonRival.getEspecie().getNombre() +
                        " ha sido registrado como COMPLETAMENTE INVESTIGADO (Nivel 10)");
                }

                entrenador.ganarDinero(10000);
                registrarEvento("¡Has obtenido $10,000 como recompensa!");
                registrarEvento("¡OBJETIVO PRINCIPAL COMPLETADO!");
            }

            // Convierte el Pokémon rival a una instancia controlable por el jugador
            PokemonJugador nuevo;

            if (pokemonRival instanceof PokemonSalvaje) {
                PokemonSalvaje salvaje = (PokemonSalvaje) pokemonRival;
                nuevo = salvaje.convertirAJugador();

                String nombreRival = pokemonRival.getNombre();
                String nombreEspecie = pokemonRival.getEspecie().getNombre();
                if (!nombreRival.equals(nombreEspecie)) {
                    nuevo.setApodo(nombreRival);
                }

            } else {
                nuevo = new PokemonJugador(
                    pokemonRival.getEspecie(),
                    pokemonRival.getNombre(),
                    pokemonRival.getNivel()
                );

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

            // Añade el Pokémon capturado al equipo del entrenador
            boolean añadido = entrenador.agregarPokemon(nuevo);
            if (!añadido) {
                registrarEvento("¡Pero el equipo estaba lleno!");
            }

            // Registra la captura en el Pokédex del entrenador
            entrenador.registrarCapturaPokemon(
                pokemonRival.getEspecie().getNombre(),
                "En combate"
            );

            combateTerminado = true;
            motivoFin = "captura";
            return true;

        } else {
            registrarEvento(pokemonRival.getNombre() + " se liberó...");

            if (esLegendario) {
                registrarEvento("¡El poder legendario es demasiado fuerte!");
            }

            turnoJugador = false;
            return false;
        }
    }

    /**
     * Determina si un Pokémon pertenece a la categoría de legendarios
     * según una lista predefinida de nombres.
     *
     * @param nombrePokemon Nombre de la especie del Pokémon a verificar
     * @return true si el Pokémon es considerado legendario, false en caso contrario
     */
    private boolean esPokemonLegendario(String nombrePokemon) {
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

    /**
     * Permite al jugador cambiar su Pokémon activo por otro de su equipo
     * durante el combate, siempre que no esté debilitado.
     *
     * @param nuevoPokemon Pokémon que tomará el lugar en combate
     * @return Resultado de la operación según el enum ResultadoTurno
     */
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

        pokemonJugador = nuevoPokemon;
        registrarEvento("¡Adelante " + pokemonJugador.getNombre() + "!");

        turnoJugador = false;

        return ResultadoTurno.EXITO;
    }

    /**
     * Intenta que el jugador huya del combate con una probabilidad basada
     * en la comparación de velocidad entre los Pokémon. Si falla, el rival
     * ataca inmediatamente.
     *
     * @return true si la huida fue exitosa, false en caso contrario
     */
    public boolean intentarHuir() {
        double probabilidad = (double) pokemonJugador.getVelocidad() /
            (pokemonJugador.getVelocidad() + pokemonRival.getVelocidad());

        boolean exito = Math.random() < probabilidad;

        if (exito) {
            registrarEvento("¡Has huido con éxito!");
            combateTerminado = true;
            motivoFin = "huida";
        } else {
            registrarEvento("¡No has podido huir!");
            turnoJugador = false;
            ejecutarTurnoRival();
        }

        return exito;
    }

    /**
     * Calcula y otorga las recompensas por victoria en combate, incluyendo
     * experiencia para el Pokémon, recursos y registro en el Pokédex.
     *
     * @param entrenador Entrenador que recibe las recompensas
     */
    public void otorgarRecompensasVictoria(Entrenador entrenador) {
        if (motivoFin == null || !motivoFin.equals("victoria")) {
            return;
        }

        SistemaRecompensas.RecompensaCombate recompensa =
            SistemaRecompensas.calcularRecompensaVictoria(pokemonJugador, pokemonRival);

        if (pokemonJugador instanceof PokemonJugador) {
            PokemonJugador pj = (PokemonJugador) pokemonJugador;
            pj.ganarExperiencia(recompensa.experiencia * 2);
            registrarEvento(pj.getApodo() + " ganó " + recompensa.experiencia + " puntos de experiencia!");
        }

        if (recompensa.recursoGanado != null && recompensa.cantidadRecurso > 0) {
            Recurso recurso = new Recurso(recompensa.recursoGanado, recompensa.recursoGanado);
            boolean agregado = entrenador.getInventario().agregarItem(recurso, recompensa.cantidadRecurso);
            if (agregado) {
                registrarEvento("¡Obtuviste " + recompensa.cantidadRecurso + "x " + recompensa.recursoGanado + "!");
            }
        }

        entrenador.registrarVictoriaContraPokemon(pokemonRival.getEspecie().getNombre());
    }

    /**
     * Aplica penalizaciones por derrota en combate, incluyendo la posible
     * pérdida de recursos y el debilitamiento de todo el equipo.
     *
     * @param entrenador Entrenador que sufre las penalizaciones
     */
    public void aplicarPenalizacionDerrota(Entrenador entrenador) {
        Random rand = new Random();
        if (rand.nextDouble() < 0.3) {
            String[] recursosDisponibles = {"Planta", "Guijarro", "Baya", "Metal"};
            String recursoPerdido = recursosDisponibles[rand.nextInt(recursosDisponibles.length)];

            if (entrenador.getInventario().removerItem(recursoPerdido, 1)) {
                registrarEvento("Perdiste 1 " + recursoPerdido + " del inventario...");
            }
        }

        for (PokemonJugador p : entrenador.getEquipo()) {
            if (!p.estaDebilitado()) {
                p.recibirDaño(p.getPsActual());
            }
        }
        registrarEvento("Todos tus Pokémon quedaron debilitados...");
    }

    /**
     * Finaliza el combate por derrota completa cuando todos los Pokémon
     * del equipo del jugador han sido debilitados.
     *
     * @param entrenador Entrenador que ha sido derrotado
     */
    public void registrarDerrotaCompleta(Entrenador entrenador) {
        combateTerminado = true;
        motivoFin = "derrota";
        registrarEvento("¡Todos tus Pokémon fueron derrotados!");

        aplicarPenalizacionDerrota(entrenador);
    }

    /**
     * Añade un evento al historial del combate y lo imprime en la consola
     * para propósitos de depuración.
     *
     * @param evento Descripción textual del evento ocurrido
     */
    private void registrarEvento(String evento) {
        historial.add(evento);
        System.out.println("[Combate] " + evento);
    }

    // Métodos de acceso para obtener información del estado actual del combate

    /** @return Pokémon del jugador actualmente en combate */
    public Pokemon getPokemonJugador() { return pokemonJugador; }

    /** @return Pokémon rival actualmente en combate */
    public Pokemon getPokemonRival() { return pokemonRival; }

    /** @return true si es el turno del jugador, false si es del rival */
    public boolean isTurnoJugador() { return turnoJugador; }

    /** @return true si el combate ha finalizado, false en caso contrario */
    public boolean isCombateTerminado() { return combateTerminado; }

    /** @return Copia del historial de eventos del combate */
    public List<String> getHistorial() { return new ArrayList<>(historial); }

    /** @return Motivo por el cual terminó el combate o null si no ha terminado */
    public String getMotivoFin() { return motivoFin; }

    /**
     * Determina qué Pokémon resultó ganador del combate basándose en
     * qué Pokémon quedó debilitado.
     *
     * @return Pokémon ganador o null si el combate no ha terminado o terminó sin debilitación
     */
    public Pokemon getGanador() {
        if (!combateTerminado) return null;
        if (pokemonJugador.estaDebilitado()) return pokemonRival;
        if (pokemonRival.estaDebilitado()) return pokemonJugador;
        return null;
    }

    /**
     * Determina qué Pokémon resultó perdedor del combate basándose en
     * qué Pokémon quedó debilitado.
     *
     * @return Pokémon perdedor o null si el combate no ha terminado o terminó sin debilitación
     */
    public Pokemon getPerdedor() {
        if (!combateTerminado) return null;
        if (pokemonJugador.estaDebilitado()) return pokemonJugador;
        if (pokemonRival.estaDebilitado()) return pokemonRival;
        return null;
    }
}
