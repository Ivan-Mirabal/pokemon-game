package com.pokemon.game.pokemon;

public class EspeciePokemon {
    private final String nombre;
    private final Tipo tipo1;
    private final Tipo tipo2;
    private final int psBase;
    private final int ataqueBase;
    private final int defensaBase;
    private final int ataqueEspecialBase;
    private final int defensaEspecialBase;
    private final int velocidadBase;
    private final Habilidad habilidad;
    private final double tasaCaptura;
    private final String evolucion; // Nombre de la especie a la que evoluciona
    private final int nivelEvolucion; // Nivel requerido para evolucionar
    private final String itemEvolucion; // Item requerido (si aplica)

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

    // Getters
    public String getNombre() { return nombre; }
    public Tipo getTipo1() { return tipo1; }
    public Tipo getTipo2() { return tipo2; }
    public int getPsBase() { return psBase; }
    public int getAtaqueBase() { return ataqueBase; }
    public int getDefensaBase() { return defensaBase; }
    public int getAtaqueEspecialBase() { return ataqueEspecialBase; }
    public int getDefensaEspecialBase() { return defensaEspecialBase; }
    public int getVelocidadBase() { return velocidadBase; }
    public Habilidad getHabilidad() { return habilidad; }
    public double getTasaCaptura() { return tasaCaptura; }
    public String getEvolucion() { return evolucion; }
    public int getNivelEvolucion() { return nivelEvolucion; }
    public String getItemEvolucion() { return itemEvolucion; }

    public boolean puedeEvolucionar(int nivel, String itemEquipado) {
        if (evolucion == null) return false;

        boolean cumpleNivel = nivel >= nivelEvolucion;
        boolean cumpleItem = (itemEvolucion == null) ||
            (itemEquipado != null && itemEquipado.equals(itemEvolucion));

        return cumpleNivel && cumpleItem;
    }
}
