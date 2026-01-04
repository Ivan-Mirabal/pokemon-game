package com.pokemon.game.pokemon;

public enum Habilidad {
    // Nombre, Descripción, Modificadores: Ataque, Defensa, AtaqueEsp, DefensaEsp, Velocidad
    INTIMIDACION("Intimidación", "Baja el ataque del rival al entrar", 1.0, 1.1, 1.0, 1.0, 1.0),
    ARMADURA_ACORAZADA("Armadura Acorazada", "Aumenta defensa un 10%", 1.0, 1.1, 1.0, 1.0, 1.0),
    VISTA_LINCE("Vista Lince", "Aumenta precisión de movimientos", 1.0, 1.0, 1.0, 1.0, 1.05),
    POTENCIA("Potencia", "Aumenta ataque un 15%", 1.15, 1.0, 1.0, 1.0, 1.0),
    REGENERACION("Regeneración", "Recupera 5% PS cada 3 turnos", 1.0, 1.0, 1.0, 1.0, 1.0),
    ELECTRICIDAD_ESTATICA("Electricidad Estática", "Puede paralizar al contacto", 1.0, 1.0, 1.05, 1.0, 1.0),
    FUEGO_INTERIOR("Fuego Interior", "Aumenta ataque especial con PS bajos", 1.0, 1.0, 1.1, 1.0, 1.0),
    ESPESURA("Espesura", "Aumenta poder de movimientos planta", 1.0, 1.0, 1.1, 1.0, 1.0);

    private final String nombre;
    private final String descripcion;
    private final double modAtaque;
    private final double modDefensa;
    private final double modAtaqueEsp;
    private final double modDefensaEsp;
    private final double modVelocidad;

    Habilidad(String nombre, String desc, double atk, double def, double atkEsp, double defEsp, double vel) {
        this.nombre = nombre;
        this.descripcion = desc;
        this.modAtaque = atk;
        this.modDefensa = def;
        this.modAtaqueEsp = atkEsp;
        this.modDefensaEsp = defEsp;
        this.modVelocidad = vel;
    }

    public double getModificadorAtaque() { return modAtaque; }
    public double getModificadorDefensa() { return modDefensa; }
    public double getModificadorAtaqueEspecial() { return modAtaqueEsp; }
    public double getModificadorDefensaEspecial() { return modDefensaEsp; }
    public double getModificadorVelocidad() { return modVelocidad; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}
