package com.pokemon.game.item;

/**
 * Representa un recurso material del juego que puede ser recolectado durante la
 * exploración y utilizado como ingrediente en el sistema de crafteo para crear
 * ítems útiles como Pokéballs, pociones y objetos de recuperación.
 */
public class Recurso extends Item {

    /** Categoría del recurso que determina su uso en recetas específicas */
    private String tipo; // Valores posibles: "Planta", "Guijarro", "Baya", etc.

    /**
     * Construye un nuevo recurso con un nombre y tipo específicos, heredando
     * la funcionalidad base de la clase Item. Los recursos no tienen una cantidad
     * intrínseca, ya que su gestión de cantidad se maneja externamente en el
     * inventario del jugador.
     *
     * @param nombre Nombre identificativo del recurso (ej: "Hoja Verde", "Piedra Lisa")
     * @param tipo Categoría del recurso que define su uso en recetas de crafteo
     */
    public Recurso(String nombre, String tipo) {
        super(nombre, "Recurso para craftear");
        this.tipo = tipo;
    }

    /**
     * Obtiene la categoría o tipo de este recurso, lo que determina para qué
     * recetas de crafteo puede ser utilizado como ingrediente.
     *
     * @return Cadena que identifica el tipo del recurso
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Implementación del método de uso del recurso. Los recursos no están diseñados
     * para ser utilizados directamente, sino que deben combinarse mediante el
     * sistema de crafteo. Este método informa al jugador sobre el propósito
     * correcto del recurso.
     */
    @Override
    public void usar() {
        System.out.println("Recurso '" + nombre + "' (tipo: " + tipo +
            "). Usado para craftear, no para usar directamente.");
    }
}
