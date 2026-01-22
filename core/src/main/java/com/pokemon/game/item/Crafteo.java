package com.pokemon.game.item;

import com.pokemon.game.player.Inventario;
import com.pokemon.game.player.Ranura;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona el sistema de crafteo del juego que permite a los jugadores combinar
 * recursos recolectados para crear ítems útiles como Pokéballs, pociones y objetos
 * de recuperación. Contiene un catálogo de recetas predefinidas que especifican
 * los ingredientes necesarios y los productos resultantes.
 */
public class Crafteo {

    /** Referencia al inventario del jugador para verificar y consumir ingredientes */
    private Inventario inventory;

    /** Lista de todas las recetas de crafteo disponibles para el jugador */
    private List<Receta> recetasDisponibles;

    /**
     * Representa una receta de crafteo completa que define los ingredientes
     * requeridos y el ítem resultante que se puede crear mediante el sistema.
     */
    public static class Receta {

        /** Identificador único de la receta para referencia interna */
        public int id;

        /** Nombre legible del ítem resultante de esta receta */
        public String nombre;

        /** Lista de ingredientes necesarios para completar el crafteo */
        public List<Ingrediente> ingredientes;

        /** Descripción textual del ítem resultante y sus efectos */
        public String descripcion;

        /**
         * Construye una nueva receta de crafteo con identificador, nombre y descripción.
         *
         * @param id Identificador numérico único para la receta
         * @param nombre Nombre del ítem que produce esta receta
         * @param descripcion Descripción detallada del ítem resultante
         */
        public Receta(int id, String nombre, String descripcion) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.ingredientes = new ArrayList<>();
        }

        /**
         * Añade un ingrediente requerido a esta receta especificando el recurso
         * necesario y la cantidad exacta que se debe consumir.
         *
         * @param nombre Nombre del recurso requerido como ingrediente
         * @param cantidad Cantidad de unidades necesarias de este recurso
         */
        public void agregarIngrediente(String nombre, int cantidad) {
            ingredientes.add(new Ingrediente(nombre, cantidad));
        }
    }

    /**
     * Representa un ingrediente individual requerido para una receta de crafteo,
     * especificando el tipo de recurso y la cantidad necesaria.
     */
    public static class Ingrediente {

        /** Nombre del recurso que actúa como ingrediente */
        public String nombre;

        /** Cantidad de unidades de este recurso necesarias para la receta */
        public int cantidad;

        /**
         * Construye un nuevo ingrediente con el recurso y cantidad especificados.
         *
         * @param nombre Nombre del recurso necesario
         * @param cantidad Cantidad de unidades requeridas
         */
        public Ingrediente(String nombre, int cantidad) {
            this.nombre = nombre;
            this.cantidad = cantidad;
        }
    }

    /**
     * Inicializa el sistema de crafteo asociándolo con un inventario específico
     * y cargando todas las recetas disponibles definidas en el juego.
     *
     * @param inventario Inventario del jugador que se utilizará para verificar
     *                   ingredientes y almacenar ítems crafteados
     */
    public Crafteo(Inventario inventario) {
        this.inventory = inventario;
        this.recetasDisponibles = new ArrayList<>();
        inicializarRecetas();
    }

    /**
     * Carga y configura todas las recetas de crafteo disponibles en el juego,
     * definiendo los ingredientes necesarios y los ítems resultantes para cada una.
     */
    private void inicializarRecetas() {
        // Receta 1: Poké Ball básica para capturas estándar
        Receta receta1 = new Receta(1, "Poké Ball", "Poké Ball básica para capturar Pokémon");
        receta1.agregarIngrediente("Metal", 1);
        receta1.agregarIngrediente("Guijarro", 1);
        recetasDisponibles.add(receta1);

        // Receta 2: Super Poké Ball con mayor efectividad de captura
        Receta receta2 = new Receta(2, "Super Poké Ball", "Poké Ball con mayor tasa de captura (x2)");
        receta2.agregarIngrediente("Metal", 2);
        receta2.agregarIngrediente("Guijarro", 3);
        receta2.agregarIngrediente("Baya", 1);
        recetasDisponibles.add(receta2);

        // Receta 3: Poción pequeña para recuperación moderada de PS
        Receta receta3 = new Receta(3, "Poción", "Restaura 20 PS de un Pokémon");
        receta3.agregarIngrediente("Planta", 1);
        receta3.agregarIngrediente("Guijarro", 1);
        recetasDisponibles.add(receta3);

        // Receta 4: Poción grande para recuperación significativa de PS
        Receta receta4 = new Receta(4, "Poción Grande", "Restaura 50 PS de un Pokémon");
        receta4.agregarIngrediente("Planta", 2);
        receta4.agregarIngrediente("Baya", 1);
        receta4.agregarIngrediente("Guijarro", 3);
        recetasDisponibles.add(receta4);

        // Receta 5: Revivir para resucitar Pokémon debilitados
        Receta receta5 = new Receta(5,"Revivir", "Revive al Pokémon y restaura la mitad de sus PS");
        receta5.agregarIngrediente("Metal",3);
        receta5.agregarIngrediente("Guijarro",3);
        receta5.agregarIngrediente("Baya", 2);
        receta5.agregarIngrediente("Planta",4);
        recetasDisponibles.add(receta5);
    }

    /**
     * Obtiene la lista completa de todas las recetas de crafteo disponibles
     * para que el jugador pueda consultar los requisitos y resultados.
     *
     * @return Lista inmutable de todas las recetas configuradas en el sistema
     */
    public List<Receta> getRecetasDisponibles() {
        return recetasDisponibles;
    }

    /**
     * Verifica si el jugador posee todos los ingredientes necesarios en las
     * cantidades requeridas para craftear un ítem específico.
     *
     * @param idReceta Identificador de la receta que se desea verificar
     * @return true si el inventario contiene todos los ingredientes necesarios,
     *         false en caso contrario o si la receta no existe
     */
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

    /**
     * Ejecuta el proceso de crafteo consumiendo los ingredientes necesarios
     * del inventario y añadiendo el ítem resultante si la operación es exitosa.
     *
     * @param idReceta Identificador de la receta que se desea ejecutar
     * @return true si el crafteo se completó exitosamente y el ítem fue añadido
     *         al inventario, false en caso de error o falta de ingredientes
     */
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

        // Elimina los ingredientes necesarios del inventario
        for (Ingrediente ingrediente : receta.ingredientes) {
            boolean removido = inventory.removerItem(ingrediente.nombre, ingrediente.cantidad);
            if (!removido) {
                System.out.println("Error al remover ingrediente: " + ingrediente.nombre);
                return false;
            }
        }

        // Crea la instancia del ítem correspondiente según la receta
        Item itemCrafteado = null;

        switch(idReceta) {
            case 1: // Poké Ball estándar
                itemCrafteado = new Pokeball("Poké Ball", 1.0f);
                System.out.println("¡Has crafteado una Poké Ball!");
                break;

            case 2: // Super Poké Ball mejorada
                itemCrafteado = new Pokeball("Super Poké Ball", 1.5f);
                System.out.println("¡Has crafteado una Super Poké Ball!");
                break;

            case 3: // Poción de recuperación básica
                itemCrafteado = new Curacion("Poción", 20);
                System.out.println("¡Has crafteado una Poción!");
                break;

            case 4: // Poción grande de recuperación amplia
                itemCrafteado = new Curacion("Poción Grande", 50);
                System.out.println("¡Has crafteado una Poción Grande!");
                break;

            case 5: // Revivir para Pokémon debilitados
                itemCrafteado = new Revivir("Revivir", 50);
                System.out.println("¡Has crafteado un Revivir!");
                break;

            default:
                System.out.println("ID de receta desconocido: " + idReceta);
                return false;
        }

        // Añade el ítem creado al inventario del jugador
        if (itemCrafteado != null) {
            boolean agregado = inventory.agregarItem(itemCrafteado, 1);
            if (agregado) {
                return true;
            } else {
                System.out.println("No hay espacio en el inventario");
                return false;
            }
        }

        return false;
    }

    /**
     * Busca una receta específica en la lista de recetas disponibles utilizando
     * su identificador único como criterio de búsqueda.
     *
     * @param id Identificador numérico de la receta a localizar
     * @return Objeto Receta correspondiente al ID o null si no se encuentra
     */
    private Receta buscarReceta(int id) {
        for (Receta receta : recetasDisponibles) {
            if (receta.id == id) {
                return receta;
            }
        }
        return null;
    }

    /**
     * Obtiene una receta específica de la lista por su posición de índice.
     *
     * @param indice Posición de la receta en la lista (0-based)
     * @return Objeto Receta en la posición especificada o null si el índice
     *         está fuera de los límites de la lista
     */
    public Receta getReceta(int indice) {
        if (indice >= 0 && indice < recetasDisponibles.size()) {
            return recetasDisponibles.get(indice);
        }
        return null;
    }

    /**
     * Obtiene el número total de recetas de crafteo disponibles en el sistema.
     *
     * @return Cantidad de recetas configuradas y cargadas
     */
    public int getCantidadRecetas() {
        return recetasDisponibles.size();
    }
}
