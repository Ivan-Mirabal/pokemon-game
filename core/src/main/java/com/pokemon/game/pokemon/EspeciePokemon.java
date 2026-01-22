package com.pokemon.game.pokemon;

/**
 * Representa las características base y atributos inherentes a una especie
 * específica de Pokémon, incluyendo sus estadísticas base, tipos, habilidades
 * y condiciones de evolución. Esta clase actúa como plantilla para crear
 * instancias individuales de Pokémon de la misma especie.
 */
public class EspeciePokemon {

    /** Nombre oficial de la especie del Pokémon */
    private final String nombre;

    /** Tipo elemental primario del Pokémon */
    private final Tipo tipo1;

    /** Tipo elemental secundario del Pokémon (puede ser null si solo tiene un tipo) */
    private final Tipo tipo2;

    /** Puntos de salud base que determinan la salud máxima del Pokémon */
    private final int psBase;

    /** Estadística base de ataque físico que influye en daño de movimientos físicos */
    private final int ataqueBase;

    /** Estadística base de defensa física que reduce el daño de movimientos físicos */
    private final int defensaBase;

    /** Estadística base de ataque especial que influye en daño de movimientos especiales */
    private final int ataqueEspecialBase;

    /** Estadística base de defensa especial que reduce el daño de movimientos especiales */
    private final int defensaEspecialBase;

    /** Estadística base de velocidad que determina el orden de turnos en combate */
    private final int velocidadBase;

    /** Habilidad especial que este Pokémon puede tener (efectos en combate o fuera) */
    private final Habilidad habilidad;

    /** Probabilidad base de captura expresada como valor entre 0.0 y 1.0 */
    private final double tasaCaptura;

    /** Nombre de la especie a la que evoluciona este Pokémon, null si no evoluciona */
    private final String evolucion;

    /** Nivel mínimo requerido para que este Pokémon evolucione */
    private final int nivelEvolucion;

    /** Nombre del ítem necesario para la evolución, null si no requiere ítem */
    private final String itemEvolucion;

    /**
     * Construye una nueva especie de Pokémon con todas sus características
     * base y condiciones de evolución definidas.
     *
     * @param nombre Nombre oficial de la especie del Pokémon
     * @param tipo1 Tipo elemental primario del Pokémon
     * @param tipo2 Tipo elemental secundario (puede ser null)
     * @param psBase Puntos de salud base de la especie
     * @param ataqueBase Estadística base de ataque físico
     * @param defensaBase Estadística base de defensa física
     * @param ataqueEspecialBase Estadística base de ataque especial
     * @param defensaEspecialBase Estadística base de defensa especial
     * @param velocidadBase Estadística base de velocidad
     * @param habilidad Habilidad especial asociada a la especie
     * @param tasaCaptura Probabilidad base de captura (0.0-1.0)
     * @param evolucion Nombre de la especie evolucionada (null si no evoluciona)
     * @param nivelEvolucion Nivel mínimo requerido para evolucionar
     * @param itemEvolucion Ítem necesario para la evolución (null si no requiere)
     */
    public EspeciePokemon(String nombre, Tipo tipo1, Tipo tipo2,
                          int psBase, int ataqueBase, int defensaBase,
                          int ataqueEspecialBase, int defensaEspecialBase, int velocidadBase,
                          Habilidad habilidad, double tasaCaptura,
                          String evolucion, int nivelEvolucion, String itemEvolucion) {
        this.nombre = nombre;
        this.tipo1 = tipo1;
        this.tipo2 = tipo2;
        this.psBase = psBase;
        this.ataqueBase = ataqueBase;
        this.defensaBase = defensaBase;
        this.ataqueEspecialBase = ataqueEspecialBase;
        this.defensaEspecialBase = defensaEspecialBase;
        this.velocidadBase = velocidadBase;
        this.habilidad = habilidad;
        this.tasaCaptura = tasaCaptura;
        this.evolucion = evolucion;
        this.nivelEvolucion = nivelEvolucion;
        this.itemEvolucion = itemEvolucion;
    }

    /**
     * Obtiene el nombre oficial de la especie del Pokémon.
     *
     * @return Nombre de la especie
     */
    public String getNombre() { return nombre; }

    /**
     * Obtiene el tipo elemental primario del Pokémon.
     *
     * @return Tipo primario del Pokémon
     */
    public Tipo getTipo1() { return tipo1; }

    /**
     * Obtiene el tipo elemental secundario del Pokémon.
     *
     * @return Tipo secundario del Pokémon, puede ser null
     */
    public Tipo getTipo2() { return tipo2; }

    /**
     * Obtiene los puntos de salud base de la especie.
     *
     * @return Estadística base de puntos de salud
     */
    public int getPsBase() { return psBase; }

    /**
     * Obtiene la estadística base de ataque físico de la especie.
     *
     * @return Estadística base de ataque físico
     */
    public int getAtaqueBase() { return ataqueBase; }

    /**
     * Obtiene la estadística base de defensa física de la especie.
     *
     * @return Estadística base de defensa física
     */
    public int getDefensaBase() { return defensaBase; }

    /**
     * Obtiene la estadística base de ataque especial de la especie.
     *
     * @return Estadística base de ataque especial
     */
    public int getAtaqueEspecialBase() { return ataqueEspecialBase; }

    /**
     * Obtiene la estadística base de defensa especial de la especie.
     *
     * @return Estadística base de defensa especial
     */
    public int getDefensaEspecialBase() { return defensaEspecialBase; }

    /**
     * Obtiene la estadística base de velocidad de la especie.
     *
     * @return Estadística base de velocidad
     */
    public int getVelocidadBase() { return velocidadBase; }

    /**
     * Obtiene la habilidad especial asociada a la especie.
     *
     * @return Habilidad del Pokémon
     */
    public Habilidad getHabilidad() { return habilidad; }

    /**
     * Obtiene la probabilidad base de captura de la especie.
     *
     * @return Tasa de captura entre 0.0 y 1.0
     */
    public double getTasaCaptura() { return tasaCaptura; }

    /**
     * Obtiene el nombre de la especie a la que puede evolucionar.
     *
     * @return Nombre de la especie evolucionada o null si no evoluciona
     */
    public String getEvolucion() { return evolucion; }

    /**
     * Obtiene el nivel mínimo requerido para que evolucione la especie.
     *
     * @return Nivel de evolución o 0 si no evoluciona
     */
    public int getNivelEvolucion() { return nivelEvolucion; }

    /**
     * Obtiene el nombre del ítem necesario para la evolución.
     *
     * @return Nombre del ítem de evolución o null si no requiere ítem
     */
    public String getItemEvolucion() { return itemEvolucion; }

    /**
     * Determina si un Pokémon de esta especie puede evolucionar dadas las
     * condiciones actuales de nivel e ítem equipado.
     *
     * @param nivel Nivel actual del Pokémon
     * @param itemEquipado Nombre del ítem actualmente equipado (puede ser null)
     * @return true si cumple todas las condiciones para evolucionar, false en caso contrario
     */
    public boolean puedeEvolucionar(int nivel, String itemEquipado) {
        if (evolucion == null) return false;

        boolean cumpleNivel = nivel >= nivelEvolucion;
        boolean cumpleItem = (itemEvolucion == null) ||
            (itemEquipado != null && itemEquipado.equals(itemEvolucion));

        return cumpleNivel && cumpleItem;
    }
}
