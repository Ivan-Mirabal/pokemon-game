package com.pokemon.game.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.pokemon.game.*;
import com.pokemon.game.data.SaveData;
import com.pokemon.game.game.GameScreen;
import com.pokemon.game.item.*;
import com.pokemon.game.pokedex.PokedexEntry;
import com.pokemon.game.pokedex.PokedexManager;
import com.pokemon.game.pokemon.Entrenador;
import com.pokemon.game.pokemon.FabricaPokemon;
import com.pokemon.game.pokemon.PokemonJugador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Player {

    public float x, y;
    public float width, height;
    public float speed;
    public TextureRegion currentFrame;

    // Variables de Animación
    private float stateTime;
    private boolean isMoving;

    private Texture spriteSheet;
    private TextureRegion[][] frames;

    // Animaciones en array para simplificar
    private Animation<TextureRegion>[] animations;

    private final int frameCols = 4;
    private final int frameRows = 4;

    // Mapeo de direcciones
    private final int DIR_DOWN = 0;
    private final int DIR_UP = 1;
    private final int DIR_LEFT = 2;
    private final int DIR_RIGHT = 3;
    private int currentDir = DIR_DOWN;

    private int tileWidth, tileHeight;
    private GameScreen gameScreen;

    // Variables del Inventario
    private Inventario inventario;

    private int paginaCrafteo = 0;
    private final int RECETAS_POR_PAGINA = 8;

    // Variables del Menú
    private MenuState menuState;
    private int menuSelection;

    // Variables para selección de inventario
    private Ranura itemSeleccionado = null;
    private int inventarioPage = 0;
    private final int ITEMS_PER_PAGE = 10;

    private Ranura selectedItemSlot = null;
    private ItemAction selectedItemAction = null;

    private Crafteo sistemaCrafteo;
    private int seleccionCrafteo;

    // Para navegación en equipo (2 columnas)
    private int pokemonTeamSelection = 0;
    private int pokemonDetailTab = 0; // 0: Estadísticas, 1: Movimientos, 2: Naturaleza, 3: Encontrado

    private int inventarioColumna = 0; // 0: Recursos, 1: Pociones, 2: Poké Balls
    private int inventarioIndice = 0;  // Índice dentro de la columna

    private Entrenador entrenador;

    private int pokedexSelection = 0;           // Índice seleccionado en lista
    private String pokedexSelectedSpecies = null; // Especie seleccionada
    private int pokedexPage = 0;                // Paginación
    public final int POKEDEX_ENTRIES_PER_PAGE = 6; // 10 por página

    public Player(String texturePath, float startX, float startY, int tileWidth, int tileHeight, GameScreen gameScreen) {
        this.x = startX;
        this.y = startY;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.gameScreen = gameScreen;

        this.speed = 4.0f * tileWidth;
        this.stateTime = 0f;

        spriteSheet = new Texture(Gdx.files.internal(texturePath));

        int spriteWidth = spriteSheet.getWidth() / frameCols;
        int spriteHeight = spriteSheet.getHeight() / frameRows;

        frames = TextureRegion.split(spriteSheet, spriteWidth, spriteHeight);

        this.width = spriteWidth;
        this.height = spriteHeight;

        // Configurar todas las animaciones al inicio
        setupAllAnimations();

        // Frame inicial estático mirando hacia abajo
        currentFrame = frames[DIR_DOWN][0];

        // INICIALIZAR INVENTARIO (AHORA 50 ÍTEMS TOTALES)
        this.inventario = new Inventario(50);

        this.entrenador = new Entrenador("Ash", inventario);

        // Dar Pokémon inicial (ejemplo: Pikachu nivel 5)
        PokemonJugador inicial = FabricaPokemon.crearPokemonJugador("Pikachu", 5, "Pika");
        entrenador.agregarPokemon(inicial);
        PokemonJugador charizard = FabricaPokemon.crearPokemonJugador("Charizard", 8, "Jorge");
        entrenador.agregarPokemon(charizard);

        // INICIALIZAR ESTADO DEL MENÚ
        this.menuState = MenuState.NONE;
        this.menuSelection = 0;

        // CORRECCIÓN: Usar nombres consistentes

        inventario.agregarItem(new Pokeball(), 5);
        inventario.agregarItem(new Curacion("Poción", 20), 3);
        inventario.agregarItem(new Recurso("Planta", "Planta"), 5);
        inventario.agregarItem(new Recurso("Guijarro", "Guijarro"), 8);
        inventario.agregarItem(new Recurso("Baya", "Baya"), 3);

        // AÑADIR METAL PARA PODER CRAFTEAR
        inventario.agregarItem(new Recurso("Metal", "Metal"), 5);

        this.sistemaCrafteo = new Crafteo(inventario);
        this.seleccionCrafteo = 0;

    }

    @SuppressWarnings("unchecked")
    private void setupAllAnimations() {
        animations = new Animation[4];

        for (int dir = 0; dir < 4; dir++) {
            Array<TextureRegion> dirFrames = new Array<>();
            for (int col = 0; col < frameCols; col++) {
                dirFrames.add(frames[dir][col]);
            }
            animations[dir] = new Animation<>(0.1f, dirFrames, Animation.PlayMode.LOOP);
        }
    }

    public void update(float delta) {
        // Si hay algún menú activo, no mover al jugador
        if (menuState != MenuState.NONE) {
            return;
        }

        float movement = speed * delta;
        isMoving = false;

        // Guardar posición anterior para detectar movimiento
        float prevX = x;
        float prevY = y;

        // Movimiento en una sola dirección
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            x -= movement;
            currentDir = DIR_LEFT;
            isMoving = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
            x += movement;
            currentDir = DIR_RIGHT;
            isMoving = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {
            y += movement;
            currentDir = DIR_UP;
            isMoving = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
            y -= movement;
            currentDir = DIR_DOWN;
            isMoving = true;
        }

        // Verificar colisión CON EL RECTÁNGULO COMPLETO DEL JUGADOR (no solo el centro)
        if (gameScreen.isCollisionRect(x, y, width, height)) {
            x = prevX;
            y = prevY;
            isMoving = false; // Detener animación si hay colisión
        }

        // Actualizar estado de animación
        if (isMoving) {
            // Incrementar el tiempo solo si realmente nos estamos moviendo
            stateTime += delta;
            currentFrame = animations[currentDir].getKeyFrame(stateTime, true);
        } else {
            // Cuando está quieto, mostrar el primer frame estático de la dirección actual
            currentFrame = frames[currentDir][0];
            // Reiniciar el stateTime para que la animación empiece desde el principio
            stateTime = 0;
        }
    }

    public Entrenador getEntrenador() {
        return entrenador;
    }

    // MÉTODOS DEL INVENTARIO
    public Inventario getInventario() {
        return entrenador.getInventario();
    }

    public boolean recolectarRecurso(Recurso recurso) {
        return inventario.agregarItem(recurso);
    }

    // CORRECCIÓN DEL BUG DE DOBLE DECREMENTO
    public boolean usarItem(String nombreItem) {
        Ranura slot = inventario.buscarItem(nombreItem);
        if (slot != null && slot.getCantidad() > 0) {
            slot.usar();  // ← ¡NUEVO! Usar desde la Ranura

            if (slot.getCantidad() <= 0) {
                inventario.removerItem(nombreItem, 0);  // Eliminar ranura vacía
            }
            return true;
        }
        return false;
    }

    public boolean tienePokeball(String tipoPokeball) {
        Ranura slot = inventario.buscarItem(tipoPokeball);
        return slot != null && slot.getCantidad() > 0;
    }

    public Pokeball obtenerPokeball(String tipoPokeball) {
        Ranura slot = inventario.buscarItem(tipoPokeball);
        if (slot != null && slot.getCantidad() > 0) {
            Item item = slot.getItem();
            if (item instanceof Pokeball) {
                return (Pokeball) item;
            }
        }
        return null;
    }

    // MÉTODOS DEL MENÚ
    public MenuState getMenuState() {
        return menuState;
    }

    public void setMenuState(MenuState state) {
        this.menuState = state;
        this.menuSelection = 0; // Resetear selección al cambiar estado

        // Ya NO usamos inSubMenu - lo eliminamos
        // this.inSubMenu = (state != MenuState.MAIN && state != MenuState.NONE);

        // Reset adicional para ciertos estados
        switch (state) {
            case INVENTORY:
                cancelarUsoItem();
                inventarioPage = 0;
                inventarioColumna = 0;
                inventarioIndice = 0;
                break;
            case POKEDEX:
                pokedexSelectedSpecies = null; // Resetear especie seleccionada
                pokedexSelection = 0;
                pokedexPage = 0;
                break;
            case POKEMON_TEAM:
                pokemonTeamSelection = 0; // Empezar en primer Pokémon
                break;
            case CRAFTING:
                seleccionCrafteo = 0; // Resetear selección de crafteo
                break;
            case OPTIONS:
                menuSelection = 0; // Empezar en primera opción
                break;
        }
    }

    public void toggleMenu() {
        if (menuState == MenuState.NONE) {
            setMenuState(MenuState.MAIN);
        } else {
            setMenuState(MenuState.NONE);
        }
    }

    public int getMenuSelection() {
        return menuSelection;
    }

    public void setMenuSelection(int selection) {
        this.menuSelection = selection;
    }

    public void moveMenuUp() {
        menuSelection--;
        if (menuSelection < 0) {
            menuSelection = getMaxMenuItems() - 1;
        }
    }

    public void moveMenuDown() {
        menuSelection++;
        if (menuSelection >= getMaxMenuItems()) {
            menuSelection = 0;
        }
    }

    public void selectMenuItem() {
        switch (menuState) {
            case MAIN:
                handleMainMenuSelection();
                break;
            case INVENTORY:
                handleInventorySelection();
                break;
            case OPTIONS:
                // ¡ESTO ES LO QUE ESTABA MAL! Ahora funciona:
                switch (menuSelection) {
                    case 0: // Volumen
                        System.out.println("Volumen ajustado");
                        // Aquí podrías cambiar el volumen real
                        break;
                    case 1: // Pantalla
                        System.out.println("Pantalla cambiada a modo ventana/completa");
                        // Aquí podrías cambiar entre ventana y pantalla completa
                        break;
                    case 2: // Controles
                        System.out.println("Mostrando controles...");
                        break;
                    case 3: // Créditos
                        System.out.println("Mostrando créditos...");
                        break;
                }
                break;
            // Otros casos se mantienen igual
        }
    }

    public boolean isSelectingPokemonForItem() {
        return menuState == MenuState.POKEMON_SELECT_FOR_ITEM;
    }

    public Item getSelectedItem() {
        return selectedItemSlot != null ? selectedItemSlot.getItem() : null;
    }

    public void goBack() {
        switch (menuState) {
            case NONE:
                // Ya no estamos en menú, no hacer nada
                break;

            case MAIN:
                // Desde menú principal, salir del menú completamente
                setMenuState(MenuState.NONE);
                break;

            case INVENTORY:
            case POKEDEX:
            case CRAFTING:
            case SAVE:
            case OPTIONS:
            case POKEMON_TEAM:
            case POKEMON_DETAIL:
                // Desde cualquier submenú, volver al menú principal
                setMenuState(MenuState.MAIN);
                break;

            case POKEMON_SELECT_FOR_ITEM:
                // Cancelar uso de item y volver al inventario
                cancelarUsoItem();
                setMenuState(MenuState.INVENTORY);
                break;

            case ITEM_SELECTED:
                // Cancelar selección de item (por si acaso)
                cancelarUsoItem();
                setMenuState(MenuState.INVENTORY);
                break;

            default:
                // Por defecto, volver al menú principal
                setMenuState(MenuState.MAIN);
                break;
        }
    }

    // En el método handleMainMenuSelection() de Player.java:
    private void handleMainMenuSelection() {
        switch (menuSelection) {
            case 0: // Pokémon
                setMenuState(MenuState.POKEMON_TEAM);
                break;
            case 1: // Pokédex
                setMenuState(MenuState.POKEDEX);
                // RESETEAR la Pokédex a vista de lista
                setPokedexSelectedSpecies(null);
                setPokedexPage(0);
                setPokedexSelection(0);
                break;
            case 2: // Inventario
                setMenuState(MenuState.INVENTORY);
                break;
            case 3: // Crafteo
                setMenuState(MenuState.CRAFTING);
                seleccionCrafteo = 0;
                break;
            case 4: // Guardar partida
                setMenuState(MenuState.SAVE);
                break;
            case 5: // Opciones
                setMenuState(MenuState.OPTIONS);
                menuSelection = 0;
                break;
        }
    }

    private void handleInventorySelection() {
        Inventario inv = getInventario();

        // Obtener items de la columna actual
        List<Ranura> itemsColumna = new ArrayList<>();
        switch (inventarioColumna) {
            case 0: // Recursos
                for (Ranura slot : inv.getRanuras()) {
                    Item item = slot.getItem();
                    if (!(item instanceof Curacion) && !(item instanceof Pokeball)) {
                        itemsColumna.add(slot);
                    }
                }
                break;
            case 1: // Pociones
                for (Ranura slot : inv.getRanuras()) {
                    if (slot.getItem() instanceof Curacion) {
                        itemsColumna.add(slot);
                    }
                }
                break;
            case 2: // Poké Balls
                for (Ranura slot : inv.getRanuras()) {
                    if (slot.getItem() instanceof Pokeball) {
                        itemsColumna.add(slot);
                    }
                }
                break;
        }

        // Verificar que el índice sea válido
        if (inventarioIndice >= itemsColumna.size()) {
            return;
        }

        Ranura slot = itemsColumna.get(inventarioIndice);
        Item item = slot.getItem();

        // SOLO las Pociones cambian de estado
        if (item instanceof Curacion) {
            selectedItemSlot = slot;
            selectedItemAction = ItemAction.USE_ON_POKEMON;
            setMenuState(MenuState.POKEMON_SELECT_FOR_ITEM);
            pokemonTeamSelection = 0;
        } else if (item instanceof Pokeball) {
            System.out.println("Las Poké Balls solo se pueden usar en combate.");
        } else {
            System.out.println(item.getNombre() + ": " + item.getDescripcion());
            System.out.println("Cantidad: " + slot.getCantidad());
        }
    }

    public boolean usarItemEnPokemon(PokemonJugador pokemon) {
        if (selectedItemSlot == null || selectedItemAction != ItemAction.USE_ON_POKEMON) {
            return false;
        }

        Item item = selectedItemSlot.getItem();
        if (!(item instanceof Curacion)) {
            return false;
        }

        Curacion pocion = (Curacion) item;

        // 1. Verificar si el Pokémon está debilitado
        if (pokemon.estaDebilitado()) {
            System.out.println(pokemon.getApodo() + " está debilitado. Necesitas un Revivir.");
            return false;
        }

        // 2. Verificar si ya tiene toda la salud
        if (pokemon.getPsActual() >= pokemon.getPsMaximos()) {
            System.out.println(pokemon.getApodo() + " ya tiene toda la salud.");
            return false;
        }

        // 3. Calcular curación
        int psAntes = pokemon.getPsActual();
        pokemon.curar(pocion.getHpRestaurado());
        int curacionReal = pokemon.getPsActual() - psAntes;

        // 4. Consumir item
        selectedItemSlot.decrementar(1);
        if (selectedItemSlot.getCantidad() <= 0) {
            inventario.removerItem(selectedItemSlot.getItem().getNombre(), 0);
        }

        System.out.println("¡Usaste " + item.getNombre() + " en " +
            pokemon.getApodo() + "! (+" + curacionReal + " PS)");

        // 5. Resetear
        selectedItemSlot = null;
        selectedItemAction = ItemAction.NONE;
        return true; // ¡IMPORTANTE! Devolver true para indicar éxito
    }

    public void cancelarUsoItem() {
        selectedItemSlot = null;
        selectedItemAction = ItemAction.NONE;
    }

    private int getMaxMenuItems() {
        switch (menuState) {
            case MAIN:
                return 6;
            case INVENTORY:
                return this.getInventario().getRanuras().size();
            case OPTIONS:
                return 4;
            case POKEDEX:
                // Para la Pokédex, el máximo es el número de entradas en la página actual
                List<PokedexEntry> entradas = getEntrenador().getPokedex().getEntradasOrdenadas();
                int inicio = pokedexPage * POKEDEX_ENTRIES_PER_PAGE;
                int fin = Math.min(inicio + POKEDEX_ENTRIES_PER_PAGE, entradas.size());
                return fin - inicio;
            default:
                return 0;
        }
    }

    // Métodos para manejar inventario
    public Ranura getItemSeleccionado() {
        return itemSeleccionado;
    }

    public void usarItemSeleccionado() {
        if (itemSeleccionado != null) {
            usarItem(itemSeleccionado.getItem().getNombre());
        }
    }

    public void tirarItemSeleccionado() {
        if (itemSeleccionado != null) {
            inventario.removerItem(itemSeleccionado.getItem().getNombre(), 1);
            System.out.println("Has tirado 1 " + itemSeleccionado.getItem().getNombre());
        }
    }

    public void cancelarSeleccionItem() {
        itemSeleccionado = null;
    }

    public void dispose() {
        spriteSheet.dispose();
    }

    public Crafteo getSistemaCrafteo() {
        return sistemaCrafteo;
    }

    public int getSeleccionCrafteo() {
        return seleccionCrafteo;
    }

    public void setSeleccionCrafteo(int seleccion) {
        this.seleccionCrafteo = seleccion;
    }

    public void moverSeleccionCrafteoArriba() {
        seleccionCrafteo--;
        if (seleccionCrafteo < 0) {
            seleccionCrafteo = sistemaCrafteo.getCantidadRecetas() - 1;
        }
    }

    public int getPaginaCrafteo() {
        return paginaCrafteo;
    }

    public void setPaginaCrafteo(int pagina) {
        this.paginaCrafteo = pagina;
    }

    public void siguientePaginaCrafteo() {
        int totalPaginas = (int) Math.ceil(sistemaCrafteo.getCantidadRecetas() / (float) RECETAS_POR_PAGINA);
        paginaCrafteo = (paginaCrafteo + 1) % totalPaginas;
        seleccionCrafteo = paginaCrafteo * RECETAS_POR_PAGINA;
    }

    public void anteriorPaginaCrafteo() {
        int totalPaginas = (int) Math.ceil(sistemaCrafteo.getCantidadRecetas() / (float) RECETAS_POR_PAGINA);
        paginaCrafteo = (paginaCrafteo - 1 + totalPaginas) % totalPaginas;
        seleccionCrafteo = paginaCrafteo * RECETAS_POR_PAGINA;
    }

    public void moverSeleccionCrafteoAbajo() {
        seleccionCrafteo++;
        if (seleccionCrafteo >= sistemaCrafteo.getCantidadRecetas()) {
            seleccionCrafteo = 0;
        }
    }

    public boolean intentarCraftear() {
        return sistemaCrafteo.crearItem(seleccionCrafteo + 1); // +1 porque IDs empiezan en 1
    }

    // Método para curar Pokémon seleccionado
    public boolean curarPokemonSeleccionado() {
        if (getEntrenador().getEquipo().isEmpty()) return false;

        // ❌ ANTES: Usaba pokemonMenuSelection
        // PokemonJugador pokemon = getEntrenador().getEquipo().get(pokemonMenuSelection);

        // ✅ AHORA: Usa el método getPokemonSeleccionado() que ya corregiste
        PokemonJugador pokemon = getPokemonSeleccionado();  // ¡Esto usa pokemonTeamSelection!

        if (pokemon == null) return false;

        // Verificar si tiene Pociones en inventario
        Ranura pociones = getInventario().buscarItem("Poción");
        if (pociones != null && pociones.getCantidad() > 0) {
            pokemon.curar(20); // Poción cura 20 PS
            pociones.usarCantidad(1);
            System.out.println("Has usado una Poción en " + pokemon.getApodo());
            return true;
        } else {
            System.out.println("¡No tienes Pociones!");
            return false;
        }
    }

    // Método para cambiar apodo
    public void cambiarApodoPokemon(String nuevoApodo) {
        if (getEntrenador().getEquipo().isEmpty()) return;

        // ❌ ANTES: Usaba pokemonMenuSelection
        // PokemonJugador pokemon = getEntrenador().getEquipo().get(pokemonMenuSelection);

        // ✅ AHORA: Usa el método getPokemonSeleccionado() que ya corregiste
        PokemonJugador pokemon = getPokemonSeleccionado();  // ¡Esto usa pokemonTeamSelection!

        if (pokemon == null) return;

        pokemon.setApodo(nuevoApodo);
        System.out.println("¡Ahora " + pokemon.getNombre() + " se llama " + nuevoApodo + "!");
    }

    // Método para seleccionar Pokémon en menú
    // Método para seleccionar Pokémon en menú - YA ESTÁ CORRECTO
    public PokemonJugador getPokemonSeleccionado() {
        List<PokemonJugador> equipo = getEntrenador().getEquipo();
        if (equipo.isEmpty()) return null;

        // Asegurarnos de que la selección esté dentro de los límites
        if (pokemonTeamSelection >= equipo.size()) {
            pokemonTeamSelection = 0;
        }

        return equipo.get(pokemonTeamSelection);
    }

    // Método auxiliar (privado)
    private boolean slotTienePokemon(int indice) {
        return indice < getEntrenador().getEquipo().size();
    }

    // Métodos de navegación mejorados
    public void movePokemonTeamUp() {
        int equipoSize = getEntrenador().getEquipo().size();
        if (equipoSize <= 1) return;

        int columna = pokemonTeamSelection % 2;
        int filaActual = pokemonTeamSelection / 2;

        // Buscar hacia arriba en la misma columna
        for (int f = filaActual - 1; f >= 0; f--) {
            int candidato = (f * 2) + columna;
            if (slotTienePokemon(candidato)) {
                pokemonTeamSelection = candidato;
                return;
            }
        }
    }

    public void movePokemonTeamDown() {
        int equipoSize = getEntrenador().getEquipo().size();
        if (equipoSize <= 1) return;

        int columna = pokemonTeamSelection % 2;
        int filaActual = pokemonTeamSelection / 2;

        // Buscar hacia abajo en la misma columna
        for (int f = filaActual + 1; f <= 2; f++) {
            int candidato = (f * 2) + columna;
            if (candidato < 6 && slotTienePokemon(candidato)) {
                pokemonTeamSelection = candidato;
                return;
            }
        }
    }

    public void movePokemonTeamLeft() {
        int equipoSize = getEntrenador().getEquipo().size();
        if (equipoSize <= 1) return;

        // Solo mover si está en columna derecha
        if (pokemonTeamSelection % 2 == 1) {
            int candidato = pokemonTeamSelection - 1;
            if (slotTienePokemon(candidato)) {
                pokemonTeamSelection = candidato;
            }
        }
    }

    public void movePokemonTeamRight() {
        int equipoSize = getEntrenador().getEquipo().size();
        if (equipoSize <= 1) return;

        // Solo mover si está en columna izquierda y no es el último slot
        if (pokemonTeamSelection % 2 == 0 && pokemonTeamSelection < 5) {
            int candidato = pokemonTeamSelection + 1;
            if (slotTienePokemon(candidato)) {
                pokemonTeamSelection = candidato;
            }
        }
    }

    // Para cambiar pestañas en vista detalle
    public void nextPokemonDetailTab() {
        pokemonDetailTab = (pokemonDetailTab + 1) % 4;
    }

    public void prevPokemonDetailTab() {
        pokemonDetailTab = (pokemonDetailTab - 1 + 4) % 4;
    }

    // Getters
    public int getPokemonTeamSelection() { return pokemonTeamSelection; }
    public int getPokemonDetailTab() { return pokemonDetailTab; }
    public void setPokemonTeamSelection(int sel) { pokemonTeamSelection = sel; }
    public void setPokemonDetailTab(int tab) { pokemonDetailTab = tab; }

    // ===== MÉTODOS PARA CONTROL DE POKÉDEX =====

    public int getPokedexSelection() {
        return pokedexSelection;
    }

    public void setPokedexSelection(int selection) {
        this.pokedexSelection = selection;
    }

    public String getPokedexSelectedSpecies() {
        return pokedexSelectedSpecies;
    }

    // AGREGAR este método:
    public void setPokedexSelectedSpecies(String species) {
        this.pokedexSelectedSpecies = species;
    }

    public int getPokedexPage() {
        return pokedexPage;
    }

    public void setPokedexPage(int page) {
        this.pokedexPage = page;
    }

    // REEMPLAZAR los métodos existentes por estos:

    public void movePokedexUp() {
        List<PokedexEntry> entradas = getEntrenador().getPokedex().getEntradasOrdenadas();
        int inicio = pokedexPage * POKEDEX_ENTRIES_PER_PAGE;
        int fin = Math.min(inicio + POKEDEX_ENTRIES_PER_PAGE, entradas.size());
        int entradasEnPagina = fin - inicio;

        if (entradasEnPagina == 0) return;

        pokedexSelection--;
        if (pokedexSelection < 0) {
            pokedexSelection = entradasEnPagina - 1;
        }
    }

    public void movePokedexDown() {
        List<PokedexEntry> entradas = getEntrenador().getPokedex().getEntradasOrdenadas();
        int inicio = pokedexPage * POKEDEX_ENTRIES_PER_PAGE;
        int fin = Math.min(inicio + POKEDEX_ENTRIES_PER_PAGE, entradas.size());
        int entradasEnPagina = fin - inicio;

        if (entradasEnPagina == 0) return;

        pokedexSelection++;
        if (pokedexSelection >= entradasEnPagina) {
            pokedexSelection = 0;
        }
    }

    public void nextPokedexPage() {
        List<PokedexEntry> entradas = getEntrenador().getPokedex().getEntradasOrdenadas();
        int totalPaginas = (int) Math.ceil(entradas.size() / (float) POKEDEX_ENTRIES_PER_PAGE);

        if (pokedexPage < totalPaginas - 1) {
            pokedexPage++;
            pokedexSelection = 0; // Resetear selección al cambiar página
        }
    }

    public void prevPokedexPage() {
        if (pokedexPage > 0) {
            pokedexPage--;
            pokedexSelection = 0; // Resetear selección al cambiar página
        }
    }

    public void moveInventoryLeft() {
        inventarioColumna = (inventarioColumna - 1 + 3) % 3;
        inventarioIndice = 0; // Resetear índice al cambiar de columna
    }

    public void moveInventoryRight() {
        inventarioColumna = (inventarioColumna + 1) % 3;
        inventarioIndice = 0;
    }

    public void moveInventoryUp() {
        inventarioIndice = Math.max(0, inventarioIndice - 1);
    }

    public void moveInventoryDown() {
        // El límite depende de la columna actual y los items
        inventarioIndice++; // El límite se valida en GameScreen
    }

    public boolean isMoving(){
        return isMoving;
    }

    public int getInventoryColumna() { return inventarioColumna; }
    public int getInventoryIndice() { return inventarioIndice; }
    public void setInventoryIndice(int indice) { this.inventarioIndice = indice; }

    // AÑADIR AL FINAL DE Player.java (antes del cierre de clase):

// ============ MÉTODOS PARA SISTEMA DE GUARDADO ============

    /**
     * Extrae los datos actuales del jugador para guardar
     */
    public SaveData extraerDatosParaGuardar() {
        SaveData datos = new SaveData();

        // 1. Extraer Pokédex COMPLETA
        if (this.getEntrenador() != null && this.getEntrenador().getPokedex() != null) {
            // Clonar la Pokédex para no modificar la original
            datos.setPokedex(clonarPokedex(this.getEntrenador().getPokedex()));
        }

        // 2. Extraer equipo Pokémon (simplificado)
        List<SaveData.PokemonSimple> equipoSimple = new ArrayList<>();
        if (this.getEntrenador() != null) {
            for (PokemonJugador pokemon : this.getEntrenador().getEquipo()) {
                SaveData.PokemonSimple simple = convertirPokemonASimple(pokemon);
                equipoSimple.add(simple);
            }
        }
        datos.setEquipo(equipoSimple);

        // 3. Extraer inventario
        List<SaveData.ItemSlot> inventarioSimple = new ArrayList<>();
        if (this.getInventario() != null) {
            for (Ranura ranura : this.getInventario().getRanuras()) {
                SaveData.ItemSlot slot = new SaveData.ItemSlot(
                    ranura.getItem().getNombre(),
                    ranura.getCantidad()
                );
                inventarioSimple.add(slot);
            }
        }
        datos.setInventario(inventarioSimple);

        return datos;
    }

    /**
     * Carga datos guardados en el jugador actual
     */
    public void cargarDatosGuardados(SaveData datos) {
        if (datos == null) {
            System.out.println("⚠️ No hay datos para cargar");
            return;
        }

        System.out.println("🔄 Cargando datos guardados...");

        // Player.java -> cargarDatosGuardados
        if (this.getEntrenador() != null) {
            this.getEntrenador().vaciarEquipo(); // Usa el nuevo método
            System.out.println("🧹 Equipo inicial limpiado.");
        }

        //Cargar pokedex
        if (datos.getPokedex() != null && this.getEntrenador() != null) {
            // Obtenemos los registros directamente del manager guardado
            java.util.Map<String, PokedexEntry> registrosGuardados = datos.getPokedex().getRegistros();

            if (registrosGuardados != null) {
                this.getEntrenador().getPokedex().setRegistros(registrosGuardados);
                System.out.println("✅ Pokédex cargada: " +
                    this.getEntrenador().getPokedex().getTotalEspeciesVistas() + " especies.");
            }
        }

        // 2. Cargar equipo Pokémon
        if (datos.getEquipo() != null && this.getEntrenador() != null) {
            // Limpiar equipo actual
            this.getEntrenador().getEquipo().clear();

            // Cargar cada Pokémon guardado
            int contador = 0;
            for (SaveData.PokemonSimple simple : datos.getEquipo()) {
                PokemonJugador pokemon = recrearPokemonDesdeSimple(simple);
                if (pokemon != null) {
                    this.getEntrenador().agregarPokemon(pokemon);
                    contador++;
                }
            }
            System.out.println("✅ Equipo cargado: " + contador + " Pokémon");
        }

        // 3. Cargar inventario
        if (datos.getInventario() != null && this.getInventario() != null) {
            // Crear nuevo inventario vacío
            this.getInventario().vaciarInventario();

            // Cargar cada ítem
            int totalItems = 0;
            for (SaveData.ItemSlot slot : datos.getInventario()) {
                Item item = crearItemPorNombre(slot.getNombreItem());
                if (item != null) {
                    this.getInventario().agregarItem(item, slot.getCantidad());
                    totalItems += slot.getCantidad();
                }
            }
            System.out.println("✅ Inventario cargado: " + totalItems + " ítems");
        }

        System.out.println("🎮 ¡Datos cargados exitosamente!");
    }

// ============ MÉTODOS PRIVADOS AUXILIARES ============

    /**
     * Convierte un PokemonJugador a su versión simple para guardar
     */
    private SaveData.PokemonSimple convertirPokemonASimple(PokemonJugador pokemon) {
        return new SaveData.PokemonSimple(
            pokemon.getEspecie().getNombre(),
            pokemon.getApodo(),
            pokemon.getNivel(),
            pokemon.getPsActual(),
            pokemon.getPsMaximos(),
            pokemon.getExperiencia()
        );
    }

    /**
     * Recrea un PokemonJugador desde datos simples
     */
    private PokemonJugador recrearPokemonDesdeSimple(SaveData.PokemonSimple simple) {
        try {
            // Usar la fábrica existente para crear el Pokémon
            PokemonJugador pokemon = FabricaPokemon.crearPokemonJugador(
                simple.getEspecie(),
                simple.getNivel(),
                simple.getApodo()
            );

            if (pokemon != null) {
                // Ajustar PS actuales (la fábrica los pone al máximo)
                int diferenciaPS = simple.getPsActual() - pokemon.getPsActual();
                if (diferenciaPS != 0) {
                    pokemon.curar(diferenciaPS);
                }

                // Ajustar experiencia (aproximado)
                // Nota: Esto es simplificado, en un sistema real necesitarías
                // calcular la experiencia basada en el nivel y experiencia guardada
            }

            return pokemon;
        } catch (Exception e) {
            System.err.println("❌ Error recreando Pokémon: " + simple.getEspecie());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Crea un ítem por su nombre
     */
    private Item crearItemPorNombre(String nombreItem) {
        if (nombreItem == null || nombreItem.isEmpty()) {
            return null;
        }

        String nombreLower = nombreItem.toLowerCase();

        // Sistema de ítems básicos
        switch (nombreLower) {
            case "pokeball":
            case "poké ball":
                return new Pokeball();

            case "poción":
                return new Curacion("Poción", 20);

            case "superpoción":
            case "super poción":
                return new Curacion("Superpoción", 50);

            case "metal":
                return new Recurso("Metal", "Material de crafteo");

            case "planta":
                return new Recurso("Planta", "Material de crafteo");

            case "guijarro":
                return new Recurso("Guijarro", "Material de crafteo");

            case "baya":
                return new Recurso("Baya", "Fruta curativa");

            default:
                System.out.println("⚠️ Item no reconocido: " + nombreItem);
                // Crear recurso genérico como fallback
                return new Recurso(nombreItem, "Ítem guardado");
        }
    }

    /**
     * Clona la Pokédex para evitar modificar la original
     */
    private PokedexManager clonarPokedex(PokedexManager original) {
        // Crear nueva instancia
        PokedexManager clon = new PokedexManager();

        // Copiar registros (asumiendo que PokedexEntry es serializable)
        // Nota: Esto es simplificado, necesitarías métodos de copia en PokedexManager
        try {
            // Por ahora, simplemente devolvemos la original
            // En una implementación real, necesitarías clonar profundamente
            return original;
        } catch (Exception e) {
            System.err.println("❌ Error clonando Pokédex");
            return new PokedexManager();
        }
    }
}
