package com.pokemon.game.pokemon;

/**
 * Enumeración que representa las habilidades especiales que un Pokémon puede poseer.
 * Cada habilidad proporciona modificadores estadísticos y efectos únicos que afectan
 * el rendimiento del Pokémon en combate, otorgando ventajas tácticas específicas.
 */
public enum Habilidad {

    /**
     * Reduce el ataque del Pokémon rival cuando este Pokémon entra en combate.
     * Modificadores: Ataque 1.0, Defensa 1.0, AtaqueEspecial 1.0, DefensaEspecial 1.0, Velocidad 1.0
     */
    INTIMIDACION("Intimidación", "Baja el ataque del rival al entrar", 1.0, 1.1, 1.0, 1.0, 1.0),

    /**
     * Aumenta la defensa física del Pokémon en un 10% al entrar en combate.
     * Modificadores: Ataque 1.0, Defensa 1.1, AtaqueEspecial 1.0, DefensaEspecial 1.0, Velocidad 1.0
     */
    ARMADURA_ACORAZADA("Armadura Acorazada", "Aumenta defensa un 10%", 1.0, 1.1, 1.0, 1.0, 1.0),

    /**
     * Incrementa la precisión de todos los movimientos del Pokémon en un 5%.
     * Modificadores: Ataque 1.0, Defensa 1.0, AtaqueEspecial 1.0, DefensaEspecial 1.0, Velocidad 1.05
     */
    VISTA_LINCE("Vista Lince", "Aumenta precisión de movimientos", 1.0, 1.0, 1.0, 1.0, 1.05),

    /**
     * Aumenta el ataque físico del Pokémon en un 15% durante todo el combate.
     * Modificadores: Ataque 1.15, Defensa 1.0, AtaqueEspecial 1.0, DefensaEspecial 1.0, Velocidad 1.0
     */
    POTENCIA("Potencia", "Aumenta ataque un 15%", 1.15, 1.0, 1.0, 1.0, 1.0),

    /**
     * El Pokémon recupera el 5% de sus PS máximos cada 3 turnos de combate.
     * Modificadores: Ataque 1.0, Defensa 1.0, AtaqueEspecial 1.0, DefensaEspecial 1.0, Velocidad 1.0
     */
    REGENERACION("Regeneración", "Recupera 5% PS cada 3 turnos", 1.0, 1.0, 1.0, 1.0, 1.0),

    /**
     * Tiene una probabilidad de paralizar al Pokémon rival cuando este realiza
     * un movimiento de contacto físico contra el portador de la habilidad.
     * Modificadores: Ataque 1.0, Defensa 1.0, AtaqueEspecial 1.05, DefensaEspecial 1.0, Velocidad 1.0
     */
    ELECTRICIDAD_ESTATICA("Electricidad Estática", "Puede paralizar al contacto", 1.0, 1.0, 1.05, 1.0, 1.0),

    /**
     * Aumenta el ataque especial del Pokémon en un 10% cuando sus PS están por debajo del 33%.
     * Modificadores: Ataque 1.0, Defensa 1.0, AtaqueEspecial 1.1, DefensaEspecial 1.0, Velocidad 1.0
     */
    FUEGO_INTERIOR("Fuego Interior", "Aumenta ataque especial con PS bajos", 1.0, 1.0, 1.1, 1.0, 1.0),

    /**
     * Incrementa el poder de los movimientos de tipo PLANTA en un 10%.
     * Modificadores: Ataque 1.0, Defensa 1.0, AtaqueEspecial 1.1, DefensaEspecial 1.0, Velocidad 1.0
     */
    ESPESURA("Espesura", "Aumenta poder de movimientos planta", 1.0, 1.0, 1.1, 1.0, 1.0);

    /** Nombre legible de la habilidad para mostrar en interfaces de usuario */
    private final String nombre;

    /** Descripción detallada del efecto de la habilidad */
    private final String descripcion;

    /** Multiplicador aplicado a la estadística de ataque físico cuando la habilidad está activa */
    private final double modAtaque;

    /** Multiplicador aplicado a la estadística de defensa física cuando la habilidad está activa */
    private final double modDefensa;

    /** Multiplicador aplicado a la estadística de ataque especial cuando la habilidad está activa */
    private final double modAtaqueEsp;

    /** Multiplicador aplicado a la estadística de defensa especial cuando la habilidad está activa */
    private final double modDefensaEsp;

    /** Multiplicador aplicado a la estadística de velocidad cuando la habilidad está activa */
    private final double modVelocidad;

    /**
     * Constructor privado de la enumeración que inicializa todos los atributos
     * de una habilidad, incluyendo sus modificadores estadísticos y descripción.
     *
     * @param nombre Nombre legible de la habilidad
     * @param desc Descripción textual del efecto de la habilidad
     * @param atk Multiplicador para la estadística de ataque físico (1.0 = sin cambio)
     * @param def Multiplicador para la estadística de defensa física (1.0 = sin cambio)
     * @param atkEsp Multiplicador para la estadística de ataque especial (1.0 = sin cambio)
     * @param defEsp Multiplicador para la estadística de defensa especial (1.0 = sin cambio)
     * @param vel Multiplicador para la estadística de velocidad (1.0 = sin cambio)
     */
    Habilidad(String nombre, String desc, double atk, double def, double atkEsp, double defEsp, double vel) {
        this.nombre = nombre;
        this.descripcion = desc;
        this.modAtaque = atk;
        this.modDefensa = def;
        this.modAtaqueEsp = atkEsp;
        this.modDefensaEsp = defEsp;
        this.modVelocidad = vel;
    }

    /**
     * Obtiene el multiplicador que esta habilidad aplica a la estadística de ataque físico.
     *
     * @return Valor multiplicador (1.0 = efecto neutro)
     */
    public double getModificadorAtaque() { return modAtaque; }

    /**
     * Obtiene el multiplicador que esta habilidad aplica a la estadística de defensa física.
     *
     * @return Valor multiplicador (1.0 = efecto neutro)
     */
    public double getModificadorDefensa() { return modDefensa; }

    /**
     * Obtiene el multiplicador que esta habilidad aplica a la estadística de ataque especial.
     *
     * @return Valor multiplicador (1.0 = efecto neutro)
     */
    public double getModificadorAtaqueEspecial() { return modAtaqueEsp; }

    /**
     * Obtiene el multiplicador que esta habilidad aplica a la estadística de defensa especial.
     *
     * @return Valor multiplicador (1.0 = efecto neutro)
     */
    public double getModificadorDefensaEspecial() { return modDefensaEsp; }

    /**
     * Obtiene el multiplicador que esta habilidad aplica a la estadística de velocidad.
     *
     * @return Valor multiplicador (1.0 = efecto neutro)
     */
    public double getModificadorVelocidad() { return modVelocidad; }

    /**
     * Obtiene el nombre legible de esta habilidad para mostrar en interfaces.
     *
     * @return Nombre de la habilidad
     */
    public String getNombre() { return nombre; }

    /**
     * Obtiene la descripción detallada del efecto de esta habilidad.
     *
     * @return Descripción de la habilidad
     */
    public String getDescripcion() { return descripcion; }
}
