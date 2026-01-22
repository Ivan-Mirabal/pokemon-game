package com.pokemon.game.pokemon;

/**
 * Representa un movimiento o ataque que un Pokémon puede utilizar durante el combate.
 * Contiene toda la información necesaria para calcular el daño infligido, incluyendo
 * tipo elemental, potencia, precisión y consumo de puntos de poder (PP).
 */
public class Movimiento {

    /** Nombre identificativo del movimiento para mostrar en interfaces */
    private String nombre;

    /** Tipo elemental del movimiento que afecta a la efectividad contra otros tipos */
    private Tipo tipo;

    /** Potencia base del movimiento que influye directamente en el daño infligido */
    private int potencia;

    /** Precisión del movimiento expresada como porcentaje entre 1 y 100 */
    private int precision;

    /** Puntos de poder máximos que determina cuántas veces puede usarse el movimiento */
    private int ppMax;

    /** Puntos de poder actuales disponibles para usar el movimiento */
    private int ppActual;

    /** Indica si el movimiento es físico (true) o especial (false), afectando qué estadísticas se usan */
    private boolean esFisico;

    /** Descripción textual del movimiento para informar al jugador sobre sus efectos */
    private String descripcion;

    /**
     * Construye un nuevo movimiento con todas sus propiedades definidas.
     *
     * @param nombre Nombre identificativo del movimiento
     * @param tipo Tipo elemental del movimiento
     * @param potencia Potencia base del movimiento (mayor valor = más daño)
     * @param precision Precisión como porcentaje (1-100)
     * @param pp Puntos de poder máximos iniciales
     * @param esFisico true si es movimiento físico, false si es especial
     * @param descripcion Descripción textual del movimiento
     */
    public Movimiento(String nombre, Tipo tipo, int potencia, int precision, int pp, boolean esFisico, String descripcion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.potencia = potencia;
        this.precision = precision;
        this.ppMax = pp;
        this.ppActual = pp;
        this.esFisico = esFisico;
        this.descripcion = descripcion;
    }

    /**
     * Determina si el movimiento puede ser usado comprobando si quedan puntos de poder.
     *
     * @return true si quedan PP disponibles, false en caso contrario
     */
    public boolean puedeUsar() {
        return ppActual > 0;
    }

    /**
     * Consume un punto de poder del movimiento al ser utilizado en combate.
     * Solo reduce los PP si hay disponibles.
     */
    public void usar() {
        if (ppActual > 0) {
            ppActual--;
        }
    }

    /**
     * Restaura una cantidad específica de puntos de poder al movimiento,
     * sin exceder el máximo establecido.
     *
     * @param cantidad Número de PP a restaurar
     */
    public void restaurarPP(int cantidad) {
        ppActual = Math.min(ppMax, ppActual + cantidad);
    }

    /**
     * Restaura completamente todos los puntos de poder del movimiento
     * hasta su valor máximo original.
     */
    public void restaurarTodo() {
        ppActual = ppMax;
    }

    /**
     * Calcula el daño infligido por este movimiento cuando es usado por un Pokémon
     * atacante contra un Pokémon defensor. Considera múltiples factores como
     * precisión, estadísticas, efectividad de tipos, variación aleatoria y golpes críticos.
     *
     * @param atacante Pokémon que ejecuta el movimiento
     * @param defensor Pokémon que recibe el daño
     * @return Cantidad de daño infligido (mínimo 1) o 0 si el ataque falla por precisión
     */
    public int calcularDaño(Pokemon atacante, Pokemon defensor) {
        // Verifica si el movimiento impacta según su precisión
        if (Math.random() * 100 > precision) {
            return 0;
        }

        // Determina qué estadísticas usar según si el movimiento es físico o especial
        double ataque = esFisico ? atacante.getAtaque() : atacante.getAtaqueEspecial();
        double defensa = esFisico ? defensor.getDefensa() : defensor.getDefensaEspecial();

        // Fórmula base de daño simplificada inspirada en los juegos Pokémon
        double dañoBase = ((2.0 * atacante.getNivel() / 5.0 + 2.0) * potencia * (ataque / defensa)) / 50.0 + 2.0;

        // Aplica multiplicadores por efectividad de tipo contra los tipos del defensor
        double multiplicadorTipo = tipo.getMultiplicadorContra(defensor.getTipoPrimario());
        if (defensor.getTipoSecundario() != null) {
            multiplicadorTipo *= tipo.getMultiplicadorContra(defensor.getTipoSecundario());
        }
        dañoBase *= multiplicadorTipo;

        // Aplica variación aleatoria típica de los juegos (85%-100% del daño calculado)
        dañoBase *= (0.85 + Math.random() * 0.15);

        // Posibilidad de golpe crítico con probabilidad estándar de 6.25%
        if (Math.random() < 0.0625) {
            dañoBase *= 1.5;
            System.out.println("¡Golpe crítico!");
        }

        // Garantiza un daño mínimo de 1 punto si el movimiento impacta
        return Math.max(1, (int)dañoBase);
    }

    // Métodos de acceso para obtener información sobre el movimiento

    /**
     * @return Nombre identificativo del movimiento
     */
    public String getNombre() { return nombre; }

    /**
     * @return Tipo elemental del movimiento
     */
    public Tipo getTipo() { return tipo; }

    /**
     * @return Potencia base del movimiento
     */
    public int getPotencia() { return potencia; }

    /**
     * @return Precisión del movimiento como porcentaje (1-100)
     */
    public int getPrecision() { return precision; }

    /**
     * @return Puntos de poder actualmente disponibles
     */
    public int getPpActual() { return ppActual; }

    /**
     * @return Puntos de poder máximos del movimiento
     */
    public int getPpMax() { return ppMax; }

    /**
     * @return true si el movimiento es físico, false si es especial
     */
    public boolean isEsFisico() { return esFisico; }

    /**
     * @return Descripción textual del movimiento
     */
    public String getDescripcion() { return descripcion; }
}
