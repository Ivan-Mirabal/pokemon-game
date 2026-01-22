package com.pokemon.game.pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase base que representa un Pokémon individual con todas sus características,
 * estadísticas, movimientos y funcionalidades de combate. Proporciona la estructura
 * fundamental tanto para Pokémon controlados por el jugador como para Pokémon salvajes.
 */
public class Pokemon {

    /** Referencia a la especie base que define las características inherentes de este Pokémon */
    protected EspeciePokemon especie;

    /** Nombre personalizado asignado por el entrenador, o el nombre de especie por defecto */
    protected String apodo;

    /** Nivel actual del Pokémon que influye en todas sus estadísticas y capacidades */
    protected int nivel;

    /** Puntos de experiencia acumulados que determinan cuándo el Pokémon sube de nivel */
    public int experiencia;

    /** Puntos de salud actuales del Pokémon durante el combate o exploración */
    public int psActual;

    /** Máximo de puntos de salud que el Pokémon puede tener en su estado óptimo */
    protected int psMaximos;

    /** Estadística actual de ataque físico que influye en el daño de movimientos físicos */
    protected int ataque;

    /** Estadística actual de defensa física que reduce el daño de movimientos físicos recibidos */
    protected int defensa;

    /** Estadística actual de ataque especial que influye en el daño de movimientos especiales */
    protected int ataqueEspecial;

    /** Estadística actual de defensa especial que reduce el daño de movimientos especiales recibidos */
    protected int defensaEspecial;

    /** Estadística actual de velocidad que determina el orden de turnos en combate */
    protected int velocidad;

    /** Lista de hasta cuatro movimientos que el Pokémon puede utilizar en combate */
    protected List<Movimiento> movimientos;

    /** Indica si el Pokémon ha sido derrotado en combate y no puede continuar luchando */
    public boolean debilitado;

    /** Textura gráfica que representa visualmente al Pokémon en la interfaz del juego */
    protected Texture sprite;

    /**
     * Construye una nueva instancia de Pokémon con la especie, apodo y nivel especificados.
     * Calcula automáticamente todas las estadísticas, inicializa los puntos de salud
     * al máximo y carga el sprite gráfico correspondiente.
     *
     * @param especie Especie base que define las características del Pokémon
     * @param apodo Nombre personalizado (usa el nombre de especie si es null o vacío)
     * @param nivel Nivel inicial del Pokémon (1-100)
     */
    public Pokemon(EspeciePokemon especie, String apodo, int nivel) {
        this.especie = especie;
        this.apodo = (apodo != null && !apodo.isEmpty()) ? apodo : especie.getNombre();
        this.nivel = nivel;
        this.movimientos = new ArrayList<>(4);
        this.debilitado = false;

        // Calcula las estadísticas basadas en la especie y nivel
        calcularStats();
        this.psActual = psMaximos;
        this.experiencia = 0;

        // Carga la textura gráfica del Pokémon desde los recursos
        cargarSprite();
    }

    /**
     * Calcula todas las estadísticas del Pokémon utilizando fórmulas basadas en
     * las estadísticas base de su especie, su nivel actual y los modificadores
     * de su habilidad especial.
     */
    protected void calcularStats() {
        // Fórmula para PS: ((2 * Base * Nivel) / 100) + Nivel + 10
        psMaximos = (int)(((2.0 * especie.getPsBase() * nivel) / 100.0) + nivel + 10);
        ataque = calcularStat(especie.getAtaqueBase(), especie.getHabilidad().getModificadorAtaque());
        defensa = calcularStat(especie.getDefensaBase(), especie.getHabilidad().getModificadorDefensa());
        ataqueEspecial = calcularStat(especie.getAtaqueEspecialBase(), especie.getHabilidad().getModificadorAtaqueEspecial());
        defensaEspecial = calcularStat(especie.getDefensaEspecialBase(), especie.getHabilidad().getModificadorDefensaEspecial());
        velocidad = calcularStat(especie.getVelocidadBase(), especie.getHabilidad().getModificadorVelocidad());
    }

    /**
     * Calcula una estadística individual aplicando la fórmula base de Pokémon
     * y multiplicando por el modificador proporcionado por la habilidad.
     *
     * @param base Valor base de la estadística según la especie
     * @param modificadorHabilidad Multiplicador aplicado por la habilidad del Pokémon
     * @return Valor final de la estadística calculada
     */
    private int calcularStat(int base, double modificadorHabilidad) {
        int stat = ((2 * base * nivel) / 100) + 5;
        return (int)(stat * modificadorHabilidad);
    }

    /**
     * Intenta cargar la textura gráfica del Pokémon desde el sistema de archivos
     * basándose en el nombre de su especie. Si no encuentra el archivo, genera
     * un sprite de marcador de posición con colores representativos del tipo.
     */
    public void cargarSprite() {
        try {
            String path = "sprites/pokemon/" + especie.getNombre().toLowerCase() + "/normal.png";
            sprite = new Texture(Gdx.files.internal(path));
        } catch (Exception e) {
            sprite = crearSpritePlaceholder();
        }
    }

    /**
     * Crea una textura de marcador de posición cuando no se encuentra el sprite
     * original. Genera un círculo con colores según el tipo primario del Pokémon
     * y características faciales básicas para identificación visual.
     *
     * @return Textura generada como marcador de posición
     */
    private Texture crearSpritePlaceholder() {
        Pixmap pixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);

        // Determina el color de fondo según el tipo primario
        Color colorFondo = getColorPorTipo(especie.getTipo1());

        // Dibuja el cuerpo principal como un círculo
        pixmap.setColor(colorFondo);
        pixmap.fillCircle(64, 64, 50);

        // Dibuja los ojos blancos
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(44, 74, 12);
        pixmap.fillCircle(84, 74, 12);

        // Dibuja las pupilas negras
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle(44, 74, 6);
        pixmap.fillCircle(84, 74, 6);

        // Dibuja la boca roja
        pixmap.setColor(Color.RED);
        pixmap.fillRectangle(54, 44, 20, 10);

        // No se dibuja la inicial del nombre para mantener el placeholder simple

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Asigna un color representativo a cada tipo elemental de Pokémon para
     * utilizarlo en sprites de marcador de posición y efectos visuales.
     *
     * @param tipo Tipo elemental del Pokémon
     * @return Color asociado al tipo especificado
     */
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

    /**
     * Reduce los puntos de salud actuales del Pokémon según el daño recibido
     * y actualiza su estado de debilitación si los PS llegan a cero.
     *
     * @param daño Cantidad de puntos de salud a restar
     */
    public void recibirDaño(int daño) {
        psActual = Math.max(0, psActual - daño);
        verificarEstadoDebilitado();
    }

    /**
     * Restaura una cantidad específica de puntos de salud al Pokémon sin
     * exceder su máximo. Si se cura por encima de cero, el Pokémon deja de
     * estar debilitado.
     *
     * @param cantidad Puntos de salud a recuperar
     */
    public void curar(int cantidad) {
        this.psActual += cantidad;
        if (this.psActual > this.psMaximos) {
            this.psActual = this.psMaximos;
        }
        if (this.psActual > 0) {
            this.debilitado = false;
        }
    }

    /**
     * Revive a un Pokémon debilitado restaurando un porcentaje de sus puntos
     * de salud máximos. Solo tiene efecto si el Pokémon está actualmente debilitado.
     *
     * @param porcentaje Porcentaje de PS máximos a recuperar (1-100)
     */
    public void revivir(int porcentaje) {
        if (!this.debilitado) return;

        this.debilitado = false;
        int psRecuperados = (int) (this.psMaximos * (porcentaje / 100.0f));

        // Garantiza al menos 1 PS recuperado
        if (psRecuperados < 1) psRecuperados = 1;

        this.psActual = psRecuperados;

        // Asegura que no exceda el máximo
        if (this.psActual > this.psMaximos) {
            this.psActual = this.psMaximos;
        }
        System.out.println(apodo + " ha revivido con " + psActual + " PS.");
    }

    /**
     * Restaura completamente la salud del Pokémon a su máximo y reestablece
     * todos los puntos de poder de sus movimientos. También lo saca del estado
     * de debilitación si estaba derrotado.
     */
    public void curarCompletamente() {
        psActual = psMaximos;
        debilitado = false;
        // Restaura todos los PP de los movimientos
        for (Movimiento m : movimientos) {
            m.restaurarTodo();
        }
    }

    /**
     * Intenta añadir un nuevo movimiento a la lista del Pokémon. El Pokémon
     * puede aprender como máximo cuatro movimientos simultáneamente.
     *
     * @param nuevoMovimiento Movimiento a aprender
     * @return true si se aprendió exitosamente, false si ya tiene cuatro movimientos
     */
    public boolean aprenderMovimiento(Movimiento nuevoMovimiento) {
        if (movimientos.size() >= 4) {
            return false;
        }
        movimientos.add(nuevoMovimiento);
        return true;
    }

    /**
     * Elimina un movimiento específico de la lista del Pokémon según su índice.
     *
     * @param indice Posición del movimiento a olvidar (0-3)
     * @return true si se eliminó exitosamente, false si el índice no es válido
     */
    public boolean olvidarMovimiento(int indice) {
        if (indice >= 0 && indice < movimientos.size()) {
            movimientos.remove(indice);
            return true;
        }
        return false;
    }

    // Métodos de acceso para obtener información sobre el Pokémon

    /** @return Nombre de la especie del Pokémon */
    public String getNombre() { return especie.getNombre(); }

    /** @return Apodo personalizado del Pokémon */
    public String getApodo() { return apodo; }

    /**
     * Cambia el apodo personalizado del Pokémon.
     * @param apodo Nuevo apodo a asignar
     */
    public void setApodo(String apodo) { this.apodo = apodo; }

    /** @return Especie base del Pokémon */
    public EspeciePokemon getEspecie() { return especie; }

    /** @return Nivel actual del Pokémon */
    public int getNivel() { return nivel; }

    /** @return Experiencia acumulada del Pokémon */
    public int getExperiencia() { return experiencia; }

    /** @return Puntos de salud actuales */
    public int getPsActual() { return psActual; }

    /** @return Puntos de salud máximos */
    public int getPsMaximos() { return psMaximos; }

    /** @return Estadística de ataque físico actual */
    public int getAtaque() { return ataque; }

    /** @return Estadística de defensa física actual */
    public int getDefensa() { return defensa; }

    /** @return Estadística de ataque especial actual */
    public int getAtaqueEspecial() { return ataqueEspecial; }

    /** @return Estadística de defensa especial actual */
    public int getDefensaEspecial() { return defensaEspecial; }

    /** @return Estadística de velocidad actual */
    public int getVelocidad() { return velocidad; }

    /** @return Copia de la lista de movimientos del Pokémon */
    public List<Movimiento> getMovimientos() { return new ArrayList<>(movimientos); }

    /** @return Habilidad especial del Pokémon */
    public Habilidad getHabilidad() { return especie.getHabilidad(); }

    /** @return Tipo elemental primario */
    public Tipo getTipoPrimario() { return especie.getTipo1(); }

    /** @return Tipo elemental secundario o null si no tiene */
    public Tipo getTipoSecundario() { return especie.getTipo2(); }

    /** @return Textura gráfica del Pokémon */
    public Texture getSprite() { return sprite; }

    /** @return true si el Pokémon está debilitado, false en caso contrario */
    public boolean estaDebilitado() { return debilitado; }

    /**
     * Verifica y actualiza el estado de debilitación del Pokémon basándose
     * en sus puntos de salud actuales. Si tiene 0 PS, se marca como debilitado.
     */
    public void verificarEstadoDebilitado() {
        if (psActual <= 0) {
            this.debilitado = true;
            this.psActual = 0;
        } else {
            this.debilitado = false;
        }
    }

    /**
     * Libera los recursos gráficos utilizados por el sprite del Pokémon
     * para prevenir fugas de memoria cuando el objeto ya no es necesario.
     */
    public void dispose() {
        if (sprite != null) sprite.dispose();
    }

    /**
     * Genera una representación textual del Pokémon que incluye su apodo,
     * especie, nivel y estado de salud actual.
     *
     * @return Cadena descriptiva del Pokémon
     */
    @Override
    public String toString() {
        return apodo + " (" + especie.getNombre() + " Nv. " + nivel + ") PS: " + psActual + "/" + psMaximos;
    }
}
