package com.pokemon.game.item;

/**
 * Representa un objeto de curación que restaura puntos de salud a un Pokémon.
 * Hereda las propiedades básicas de un ítem y añade capacidad de restauración de PS.
 */
public class Curacion extends Item {
    private int hpRestaurado;

    /**
     * Crea un nuevo objeto de curación con el nombre y poder de restauración especificados.
     *
     * @param nombre nombre identificativo del objeto de curación
     * @param hpRestaurado cantidad de puntos de salud que restaura al usarse
     */
    public Curacion(String nombre, int hpRestaurado) {
        super(nombre, "Restaura PS de Pokémon");
        this.hpRestaurado = hpRestaurado;
    }

    /**
     * Ejecuta la acción de usar el objeto de curación.
     * Muestra un mensaje indicando la cantidad de PS restaurados.
     */
    @Override
    public void usar() {
        System.out.println("Usaste " + nombre + " y restauraste " + hpRestaurado + " PS");
    }

    /**
     * Obtiene la cantidad de puntos de salud que este objeto restaura.
     */
    public int getHpRestaurado() {
        return hpRestaurado;
    }
}
