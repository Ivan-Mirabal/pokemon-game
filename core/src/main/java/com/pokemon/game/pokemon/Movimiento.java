package com.pokemon.game.pokemon;

public class Movimiento {
    private String nombre;
    private Tipo tipo;
    private int potencia;
    private int precision; // 1-100
    private int ppMax;
    private int ppActual;
    private boolean esFisico; // true = usa ataque/defensa, false = usa ataqueEsp/defensaEsp
    private String descripcion;

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

    public boolean puedeUsar() {
        return ppActual > 0;
    }

    public void usar() {
        if (ppActual > 0) {
            ppActual--;
        }
    }

    public void restaurarPP(int cantidad) {
        ppActual = Math.min(ppMax, ppActual + cantidad);
    }

    public void restaurarTodo() {
        ppActual = ppMax;
    }

    // Método principal para calcular daño
    public int calcularDaño(Pokemon atacante, Pokemon defensor) {
        // 1. Verificar precisión
        if (Math.random() * 100 > precision) {
            return 0; // Ataque fallado
        }

        // 2. Calcular stat ofensiva y defensiva
        double ataque = esFisico ? atacante.getAtaque() : atacante.getAtaqueEspecial();
        double defensa = esFisico ? defensor.getDefensa() : defensor.getDefensaEspecial();

        // 3. Fórmula básica de daño (simplificada)
        double dañoBase = ((2.0 * atacante.getNivel() / 5.0 + 2.0) * potencia * (ataque / defensa)) / 50.0 + 2.0;

        // 4. Aplicar efectividad de tipo
        double multiplicadorTipo = tipo.getMultiplicadorContra(defensor.getTipoPrimario());
        if (defensor.getTipoSecundario() != null) {
            multiplicadorTipo *= tipo.getMultiplicadorContra(defensor.getTipoSecundario());
        }
        dañoBase *= multiplicadorTipo;

        // 5. Variación aleatoria (85% a 100%)
        dañoBase *= (0.85 + Math.random() * 0.15);

        // 6. Crítico (6.25% de probabilidad)
        if (Math.random() < 0.0625) {
            dañoBase *= 1.5;
            System.out.println("¡Golpe crítico!");
        }

        return Math.max(1, (int)dañoBase); // Mínimo 1 de daño
    }

    // Getters
    public String getNombre() { return nombre; }
    public Tipo getTipo() { return tipo; }
    public int getPotencia() { return potencia; }
    public int getPrecision() { return precision; }
    public int getPpActual() { return ppActual; }
    public int getPpMax() { return ppMax; }
    public boolean isEsFisico() { return esFisico; }
    public String getDescripcion() { return descripcion; }
}
