package com.pokemon.game.item;

import com.pokemon.game.player.Inventario;
import com.pokemon.game.player.Ranura;

import java.util.ArrayList;
import java.util.List;

public class Crafteo {
    private Inventario inventory;
    private List<Receta> recetasDisponibles;

    public static class Receta {
        public int id;
        public String nombre;
        public List<Ingrediente> ingredientes;
        public String descripcion;

        public Receta(int id, String nombre, String descripcion) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.ingredientes = new ArrayList<>();
        }

        public void agregarIngrediente(String nombre, int cantidad) {
            ingredientes.add(new Ingrediente(nombre, cantidad));
        }
    }

    public static class Ingrediente {
        public String nombre;
        public int cantidad;

        public Ingrediente(String nombre, int cantidad) {
            this.nombre = nombre;
            this.cantidad = cantidad;
        }
    }

    public Crafteo(Inventario inventario) {
        this.inventory = inventario;
        this.recetasDisponibles = new ArrayList<>();
        inicializarRecetas();
    }

    private void inicializarRecetas() {
        // Receta 1: Poké Ball normal
        Receta receta1 = new Receta(1, "Poké Ball", "Poké Ball básica para capturar Pokémon");
        receta1.agregarIngrediente("Metal", 1);
        receta1.agregarIngrediente("Guijarro", 1);
        recetasDisponibles.add(receta1);

        // Receta 2: Super Poké Ball (usa setter para modificar)
        Receta receta2 = new Receta(2, "Super Poké Ball", "Poké Ball con mayor tasa de captura (x2)");
        receta2.agregarIngrediente("Metal", 2);
        receta2.agregarIngrediente("Guijarro", 3);
        receta2.agregarIngrediente("Baya", 1);
        recetasDisponibles.add(receta2);

        // Receta 3: Poción pequeña
        Receta receta3 = new Receta(3, "Poción", "Restaura 20 PS de un Pokémon");
        receta3.agregarIngrediente("Planta", 1);
        receta3.agregarIngrediente("Guijarro", 1);
        recetasDisponibles.add(receta3);

        // Receta 4: Poción grande
        Receta receta4 = new Receta(4, "Poción Grande", "Restaura 50 PS de un Pokémon");
        receta4.agregarIngrediente("Planta", 2);
        receta4.agregarIngrediente("Baya", 1);
        receta4.agregarIngrediente("Guijarro", 3);
        recetasDisponibles.add(receta4);
    }

    public List<Receta> getRecetasDisponibles() {
        return recetasDisponibles;
    }

    public boolean puedeCraftear(int idReceta) {
        Receta receta = buscarReceta(idReceta);
        if (receta == null) return false;

        for (Ingrediente ingrediente : receta.ingredientes) {
            Ranura slot = inventory.buscarItem(ingrediente.nombre);
            if (slot == null || slot.getCantidad() < ingrediente.cantidad) {
                return false;
            }
        }
        return true;
    }

    public boolean crearItem(int idReceta) {
        if (!puedeCraftear(idReceta)) {
            System.out.println("No puedes craftear este ítem");
            return false;
        }

        Receta receta = buscarReceta(idReceta);
        if (receta == null) {
            System.out.println("Receta no encontrada");
            return false;
        }

        // Remover ingredientes
        for (Ingrediente ingrediente : receta.ingredientes) {
            boolean removido = inventory.removerItem(ingrediente.nombre, ingrediente.cantidad);
            if (!removido) {
                System.out.println("Error al remover ingrediente: " + ingrediente.nombre);
                return false;
            }
        }

        // Crear el ítem resultante - VERSIÓN MEJORADA
        Item itemCrafteado = null;

        switch(idReceta) {
            case 1: // Poké Ball normal
                itemCrafteado = new Pokeball("Poké Ball", 1.0f);
                System.out.println("¡Has crafteado una Poké Ball!");
                break;

            case 2: // Super Poké Ball
                itemCrafteado = new Pokeball("Super Poké Ball", 1.5f);
                System.out.println("¡Has crafteado una Super Poké Ball!");
                break;

            case 3: // Poción pequeña
                itemCrafteado = new Curacion("Poción", 20);
                System.out.println("¡Has crafteado una Poción!");
                break;

            case 4: // Poción grande
                itemCrafteado = new Curacion("Poción Grande", 50);
                System.out.println("¡Has crafteado una Poción Grande!");
                break;

            default:
                System.out.println("ID de receta desconocido: " + idReceta);
                return false;
        }

        // Agregar al inventario
        if (itemCrafteado != null) {
            boolean agregado = inventory.agregarItem(itemCrafteado, 1);
            if (agregado) {
                System.out.println("Ítem agregado al inventario: " + itemCrafteado.getNombre());
                return true;
            } else {
                System.out.println("No hay espacio en el inventario");
                return false;
            }
        }

        return false;
    }

    private Receta buscarReceta(int id) {
        for (Receta receta : recetasDisponibles) {
            if (receta.id == id) {
                return receta;
            }
        }
        return null;
    }

    public Receta getReceta(int indice) {
        if (indice >= 0 && indice < recetasDisponibles.size()) {
            return recetasDisponibles.get(indice);
        }
        return null;
    }

    public int getCantidadRecetas() {
        return recetasDisponibles.size();
    }
}
