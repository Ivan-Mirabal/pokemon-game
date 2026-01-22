package com.pokemon.game.item;

/**
 * Representa una Pokéball, un dispositivo utilizado para capturar Pokémon salvajes.
 * La efectividad de captura se determina por una tasa de captura multiplicadora.
 */
public class Pokeball extends Item {
    private float tasaCaptura;

    /**
     * Crea una Pokéball con nombre vacío y tasa de captura predeterminada de 1.0.
     * Útil para instancias genéricas que serán configuradas posteriormente.
     */
    public Pokeball() {
        super("", "Dispositivo para capturar"); // Nombre vacío
        this.tasaCaptura = 1.0f; // Valor por defecto
    }

    /**
     * Crea una Pokéball con el nombre y tasa de captura especificados.
     *
     * @param nombre el nombre de la Pokéball (ej: "Pokéball", "Ultraball")
     * @param tasaCaptura el multiplicador de captura que aplica esta Pokéball
     */
    public Pokeball(String nombre, float tasaCaptura) {
        super(nombre, "Dispositivo para capturar");
        this.tasaCaptura = tasaCaptura;
    }

    /**
     * Obtiene la tasa de captura actual de esta Pokéball.
     */
    public float getTasaCaptura() {
        return tasaCaptura;
    }

    /**
     * Establece una nueva tasa de captura para esta Pokéball.
     */
    public void setTasaCaptura(float tasa) {
        this.tasaCaptura = tasa;
    }

    /**
     * Cambia el nombre de esta Pokéball.
     */
    @Override
    public void setNombre(String nuevoNombre) {
        this.nombre = nuevoNombre;
    }

    /**
     * Simula el lanzamiento de la Pokéball hacia un Pokémon salvaje.
     * Muestra un mensaje indicando que se ha lanzado la Pokéball.
     */
    @Override
    public void usar() {
        System.out.println("¡Lanzaste una " + nombre + " hacia el Pokémon salvaje!");
    }
}
