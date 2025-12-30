package com.pokemon.game.item;

public class Recurso extends Item {
    private String tipo; // "Planta", "Guijarro", "Baya"

    // CONSTRUCTOR SIMPLE - sin cantidad
    public Recurso(String nombre, String tipo) {
        super(nombre, "Recurso para craftear");
        this.tipo = tipo;
        // Nota: La cantidad se maneja en Ranura, NO aquí
    }

    // ELIMINAR el constructor que recibe cantidad
    // Porque Item ya NO tiene variable cantidad
    // Si necesitas crear un Recurso con cantidad, usa:
    // Ranura ranura = new Ranura(new Recurso("Planta", "Planta"), 5);

    public String getTipo() { return tipo; }

    @Override
    public void usar() {
        // Los recursos no se usan directamente
        // Si alguien intenta usar un recurso, muestra mensaje
        System.out.println("Recurso '" + nombre + "' (tipo: " + tipo +
            "). Usado para craftear, no para usar directamente.");
    }
}
