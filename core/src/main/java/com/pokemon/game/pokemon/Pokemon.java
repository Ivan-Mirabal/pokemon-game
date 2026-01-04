package com.pokemon.game.pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import java.util.ArrayList;
import java.util.List;

public class Pokemon {
    // Referencia a la especie
    protected EspeciePokemon especie;

    // Datos individuales
    protected String apodo;
    protected int nivel;
    protected int experiencia;

    // Stats actuales
    protected int psActual;
    protected int psMaximos;
    protected int ataque;
    protected int defensa;
    protected int ataqueEspecial;
    protected int defensaEspecial;
    protected int velocidad;

    // Sistema de combate
    protected List<Movimiento> movimientos;
    protected boolean debilitado;
    protected Texture sprite;

    // Constructor principal
    public Pokemon(EspeciePokemon especie, String apodo, int nivel) {
        this.especie = especie;
        this.apodo = (apodo != null && !apodo.isEmpty()) ? apodo : especie.getNombre();
        this.nivel = nivel;
        this.movimientos = new ArrayList<>(4);
        this.debilitado = false;

        // Calcular stats basados en la especie y nivel
        calcularStats();
        this.psActual = psMaximos;
        this.experiencia = 0;

        // Cargar sprite
        cargarSprite();
    }

    // Calcular stats
    protected void calcularStats() {
        // Fórmula: ((2 * Base * Nivel) / 100) + 5
        psMaximos = (int)(((2.0 * especie.getPsBase() * nivel) / 100.0) + nivel + 10);
        ataque = calcularStat(especie.getAtaqueBase(), especie.getHabilidad().getModificadorAtaque());
        defensa = calcularStat(especie.getDefensaBase(), especie.getHabilidad().getModificadorDefensa());
        ataqueEspecial = calcularStat(especie.getAtaqueEspecialBase(), especie.getHabilidad().getModificadorAtaqueEspecial());
        defensaEspecial = calcularStat(especie.getDefensaEspecialBase(), especie.getHabilidad().getModificadorDefensaEspecial());
        velocidad = calcularStat(especie.getVelocidadBase(), especie.getHabilidad().getModificadorVelocidad());
    }

    private int calcularStat(int base, double modificadorHabilidad) {
        int stat = ((2 * base * nivel) / 100) + 5;
        return (int)(stat * modificadorHabilidad);
    }

    // Cargar sprite basado en la especie
    public void cargarSprite() {
        try {
            String path = "sprites/pokemon/" + especie.getNombre().toLowerCase() + ".png";
            sprite = new Texture(Gdx.files.internal(path));
        } catch (Exception e) {
            sprite = crearSpritePlaceholder();
        }
    }

    private Texture crearSpritePlaceholder() {
        Pixmap pixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);

        // Color según tipo primario
        Color colorFondo = getColorPorTipo(especie.getTipo1());

        // Cuerpo principal
        pixmap.setColor(colorFondo);
        pixmap.fillCircle(64, 64, 50);

        // Ojos
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(44, 74, 12);
        pixmap.fillCircle(84, 74, 12);

        // Pupilas
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle(44, 74, 6);
        pixmap.fillCircle(84, 74, 6);

        // Boca
        pixmap.setColor(Color.RED);
        pixmap.fillRectangle(54, 44, 20, 10);

        // Inicial del nombre
        pixmap.setColor(Color.WHITE);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Color getColorPorTipo(Tipo tipo) {
        if (tipo == null) return Color.GRAY;
        switch (tipo) {
            case FUEGO: return Color.ORANGE;
            case AGUA: return Color.BLUE;
            case PLANTA: return Color.GREEN;
            case ELECTRICO: return Color.YELLOW;
            case NORMAL: return Color.LIGHT_GRAY;
            case PSIQUICO: return Color.PURPLE;
            case LUCHA: return Color.BROWN;
            case VOLADOR: return Color.CYAN;
            case TIERRA: return new Color(0.65f, 0.3f, 0.1f, 1);
            case ROCA: return new Color(0.5f, 0.5f, 0.5f, 1);
            case BICHO: return new Color(0.4f, 0.7f, 0.2f, 1);
            case VENENO: return new Color(0.6f, 0.2f, 0.8f, 1);
            case HIELO: return new Color(0.6f, 0.8f, 1.0f, 1);
            case FANTASMA: return new Color(0.3f, 0.2f, 0.5f, 1);
            case DRAGON: return new Color(0.3f, 0.1f, 0.9f, 1);
            case ACERO: return new Color(0.7f, 0.7f, 0.8f, 1);
            case SINIESTRO: return Color.BLACK;
            default: return Color.GRAY;
        }
    }

    // Métodos de combate
    public void recibirDaño(int daño) {
        psActual = Math.max(0, psActual - daño);
        if (psActual == 0) {
            debilitado = true;
        }
    }

    public void curar(int cantidad) {
        if (debilitado && cantidad > 0) {
            debilitado = false;
        }
        psActual = Math.min(psMaximos, psActual + cantidad);
    }

    public void curarCompletamente() {
        psActual = psMaximos;
        debilitado = false;
        // También restaurar PP de movimientos
        for (Movimiento m : movimientos) {
            m.restaurarTodo();
        }
    }

    // Sistema de aprendizaje de movimientos
    public boolean aprenderMovimiento(Movimiento nuevoMovimiento) {
        if (movimientos.size() >= 4) {
            return false;
        }
        movimientos.add(nuevoMovimiento);
        return true;
    }

    public boolean olvidarMovimiento(int indice) {
        if (indice >= 0 && indice < movimientos.size()) {
            movimientos.remove(indice);
            return true;
        }
        return false;
    }

    // Getters y Setters
    public String getNombre() { return especie.getNombre(); }
    public String getApodo() { return apodo; }
    public void setApodo(String apodo) { this.apodo = apodo; }
    public EspeciePokemon getEspecie() { return especie; }
    public int getNivel() { return nivel; }
    public int getExperiencia() { return experiencia; }
    public int getPsActual() { return psActual; }
    public int getPsMaximos() { return psMaximos; }
    public int getAtaque() { return ataque; }
    public int getDefensa() { return defensa; }
    public int getAtaqueEspecial() { return ataqueEspecial; }
    public int getDefensaEspecial() { return defensaEspecial; }
    public int getVelocidad() { return velocidad; }
    public List<Movimiento> getMovimientos() { return new ArrayList<>(movimientos); }
    public Habilidad getHabilidad() { return especie.getHabilidad(); }
    public Tipo getTipoPrimario() { return especie.getTipo1(); }
    public Tipo getTipoSecundario() { return especie.getTipo2(); }
    public Texture getSprite() { return sprite; }
    public boolean estaDebilitado() { return debilitado; }

    public void dispose() {
        if (sprite != null) sprite.dispose();
    }

    @Override
    public String toString() {
        return apodo + " (" + especie.getNombre() + " Nv. " + nivel + ") PS: " + psActual + "/" + psMaximos;
    }
}
