package com.pokemon.game.item;

/**
 * Representa un ítem base en el juego que puede ser utilizado o equipado.
 * Esta clase abstracta define las propiedades y comportamientos básicos que todos los ítems deben tener.
 */
public abstract class Item {
    protected String nombre;
    protected String descripcion;

    /**
     * Crea un nuevo ítem con el nombre y descripción especificados.
     *
     * @param nombre el nombre identificativo del ítem
     * @param descripcion la descripción detallada del ítem y su función
     */
    public Item(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el nombre del ítem.
     */
    public String getNombre() { return nombre; }

    /**
     * Obtiene la descripción detallada del ítem.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Establece un nuevo nombre para el ítem.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Ejecuta la acción principal del ítem.
     * Cada subclase debe implementar este método con el comportamiento específico del ítem.
     */
    public abstract void usar();
}
