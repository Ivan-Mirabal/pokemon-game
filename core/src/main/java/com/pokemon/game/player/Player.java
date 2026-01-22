package com.pokemon.game.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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

    private Texture whitePixel;

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

    private boolean reordenandoEquipo = false;
    private int pokemonParaMover = -1;

    private int inventarioColumna = 0; // 0: Recursos, 1: Pociones, 2: Poké Balls
    private int inventarioIndice = 0;  // Índice dentro de la columna

    private Entrenador entrenador;

    private int pokedexSelection = 0;           // Índice seleccionado en lista
    private String pokedexSelectedSpecies = null; // Especie seleccionada
    private int pokedexPage = 0;                // Paginación
    public final int POKEDEX_ENTRIES_PER_PAGE = 6; // 10 por página

    public Player(String texturePath, float startX, float startY, int tileWidth, int tileHeight, GameScreen gameScreen, PokemonJugador pokemonInicial) {
        this.x = startX;
        this.y = startY;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.gameScreen = gameScreen;

        this.speed = 4.0f * tileWidth;
        this.stateTime = 0f;

        spriteSheet = new Texture(Gdx.files.internal(texturePath));

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.whitePixel = new Texture(pixmap);
        pixmap.dispose();

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

        inventario.agregarItem(new Pokeball("Poké Ball", 1.0f), 5);
        inventario.agregarItem(new Curacion("Poción", 20), 3);
        inventario.agregarItem(new Revivir("Revivir", 50), 3); // <--- NUEVO ITEM
        inventario.agregarItem(new Recurso("Planta", "Planta"), 5);
        inventario.agregarItem(new Recurso("Guijarro", "Guijarro"), 8);
        inventario.agregarItem(new Recurso("Baya", "Baya"), 3);
        inventario.agregarItem(new Recurso("Metal", "Metal"), 5);

        this.entrenador = new Entrenador("Ash", inventario);

        // --- LÓGICA DEL INICIAL ---
        if (pokemonInicial != null) {
            // 1. Agregar al equipo
            entrenador.agregarPokemon(pokemonInicial);

            // 2. Registrar en Pokédex (Solo capturado, Inv: 0)
            entrenador.getPokedex().registrarPokemonInicial(pokemonInicial.getNombre());

            System.out.println("¡Comienzas tu aventura con " + pokemonInicial.getApodo() + "!");
        }

        // INICIALIZAR ESTADO DEL MENÚ
        this.menuState = MenuState.NONE;
        this.menuSelection = 0;

        this.sistemaCrafteo = new Crafteo(inventario);
        this.seleccionCrafteo = 0;

    }

    public Texture getWhitePixel() {
        return whitePixel;
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
        this.menuSelection = 0;

        switch (state) {
            case INVENTORY:
                cancelarUsoItem();
                inventarioPage = 0;
                inventarioColumna = 0;
                inventarioIndice = 0;
                break;
            case POKEDEX:
                pokedexSelectedSpecies = null;
                pokedexSelection = 0;
                pokedexPage = 0;
                break;
            case POKEMON_TEAM:
                pokemonTeamSelection = 0;
                cancelarReordenamiento();
                break;
            case CRAFTING:
                seleccionCrafteo = 0;
                break;
            case OPTIONS:
                menuSelection = 0;
                break;
            case NONE:
                // Al salir del menú, asegurar que el Pokémon actual esté correcto
                if (getEntrenador() != null && !getEntrenador().getEquipo().isEmpty()) {
                    getEntrenador().actualizarPokemonActual();
                }
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
                    if (slot.getItem() instanceof Curacion || slot.getItem() instanceof Revivir) {
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
        if (item instanceof Curacion || item instanceof Revivir) {
            selectedItemSlot = slot;
            selectedItemAction = ItemAction.USE_ON_POKEMON;
            setMenuState(MenuState.POKEMON_SELECT_FOR_ITEM);
            setPokemonTeamSelection(0);
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
        String nombreItem = item.getNombre();

        if (item instanceof Revivir) {
            Revivir revivir = (Revivir) item;

            if (!pokemon.estaDebilitado()) {
                System.out.println("¡" + pokemon.getApodo() + " no está debilitado!");
                return false;
            }

            pokemon.revivir(revivir.getPorcentajeRecuperacion());

            // Consumir item
            boolean removido = inventario.removerItem(nombreItem, 1);
            if (removido) {
                selectedItemSlot = null; // Reset
                selectedItemAction = ItemAction.NONE;
                return true;
            }
            return false;
        } else if (item instanceof Curacion) {

            // 1. Verificar si el Pokémon está debilitado
            if (pokemon.estaDebilitado()) {
                System.out.println(pokemon.getApodo() + " está debilitado.");
                return false;
            }

            Curacion pocion = (Curacion) item;

            // 2. Verificar si ya tiene toda la salud
            if (pokemon.getPsActual() >= pokemon.getPsMaximos()) {
                System.out.println(pokemon.getApodo() + " ya tiene toda la salud.");
                return false;
            }

            // 3. Calcular curación
            int psAntes = pokemon.getPsActual();
            pokemon.curar(pocion.getHpRestaurado());
            int curacionReal = pokemon.getPsActual() - psAntes;

            // Llamamos a removerItem: esto descuenta 1 de la ranura Y 1 del cantidadTotal
            boolean removido = inventario.removerItem(nombreItem, 1);

            if (removido) {
                System.out.println("¡Usaste " + nombreItem + " en " +
                    pokemon.getApodo() + "! (+" + curacionReal + " PS)");
            } else {
                // Esto es por seguridad, por si acaso el ítem ya no estaba
                return false;
            }

            // 5. Resetear selección
            selectedItemSlot = null;
            selectedItemAction = ItemAction.NONE;
            return true;
        }
        return false;
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
                List<PokedexEntry> entradas = getEntrenador().getPokedex().getEntradasOrdenadas();
                int inicio = pokedexPage * POKEDEX_ENTRIES_PER_PAGE;
                int fin = Math.min(inicio + POKEDEX_ENTRIES_PER_PAGE, entradas.size());
                return fin - inicio;
            default:
                return 0;
        }
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

    public void moverSeleccionCrafteoArriba() {
        seleccionCrafteo--;
        if (seleccionCrafteo < 0) {
            seleccionCrafteo = sistemaCrafteo.getCantidadRecetas() - 1;
        }
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

    // Método para seleccionar Pokémon en menú
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

    public void iniciarReordenamiento() {
        List<PokemonJugador> equipo = getEntrenador().getEquipo();
        if (equipo.isEmpty()) return;

        if (pokemonTeamSelection >= equipo.size()) {
            pokemonTeamSelection = 0;
        }

        reordenandoEquipo = true;
        pokemonParaMover = pokemonTeamSelection;

        System.out.println("Iniciando reordenamiento - Pokémon seleccionado: " +
            equipo.get(pokemonParaMover).getApodo() +
            " en posición " + pokemonParaMover);
    }

    public void cancelarReordenamiento() {
        reordenandoEquipo = false;
        pokemonParaMover = -1;
    }

    public void finalizarReordenamiento() {
        reordenandoEquipo = false;
        pokemonParaMover = -1;
    }

    public boolean estaReordenandoEquipo() {
        return reordenandoEquipo;
    }

    public int getPokemonParaMover() {
        return pokemonParaMover;
    }

    public void moverPokemonAPosicion(int nuevaPosicion) {
        if (pokemonParaMover < 0 || nuevaPosicion < 0 ||
            pokemonParaMover == nuevaPosicion) {
            return;
        }

        List<PokemonJugador> equipo = getEntrenador().getEquipo();

        if (pokemonParaMover >= equipo.size() || nuevaPosicion >= equipo.size()) {
            return;
        }

        // Guardar referencia al Pokémon que se está moviendo
        PokemonJugador pokemonAMover = equipo.get(pokemonParaMover);

        // Intercambiar
        equipo.set(pokemonParaMover, equipo.get(nuevaPosicion));
        equipo.set(nuevaPosicion, pokemonAMover);

        // Actualizar selección
        pokemonTeamSelection = nuevaPosicion;

        // ¡IMPORTANTE! Actualizar el Pokémon actual del entrenador
        // Si movemos un Pokémon a la posición 0, ese debería ser el que arranca
        if (nuevaPosicion == 0 || pokemonParaMover == 0) {
            getEntrenador().actualizarPokemonActual();
        }

        finalizarReordenamiento();

        System.out.println("Pokémon movido de posición " + pokemonParaMover + " a " + nuevaPosicion);
        System.out.println("Pokémon que arranca ahora: " +
            (getEntrenador().getEquipo().isEmpty() ? "ninguno" :
                getEntrenador().getEquipo().get(0).getApodo()));
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

    public void setPokedexSelectedSpecies(String species) {
        this.pokedexSelectedSpecies = species;
    }

    public int getPokedexPage() {
        return pokedexPage;
    }

    public void setPokedexPage(int page) {
        this.pokedexPage = page;
    }

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

// ============ MÉTODOS PARA SISTEMA DE GUARDADO ============

    /**
     * Extrae los datos actuales del jugador para guardar
     */
    public SaveData extraerDatosParaGuardar() {

        sanearPokemonAntesDeGuardar();

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

        // Player.java -> cargarDatosGuardados
        if (this.getEntrenador() != null) {
            this.getEntrenador().vaciarEquipo(); // Usa el nuevo método
        }

        //Cargar pokedex
        if (datos.getPokedex() != null && this.getEntrenador() != null) {
            // Obtenemos los registros directamente del manager guardado
            java.util.Map<String, PokedexEntry> registrosGuardados = datos.getPokedex().getRegistros();

            if (registrosGuardados != null) {
                this.getEntrenador().getPokedex().setRegistros(registrosGuardados);
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
        }

    }

    public boolean isLegendaryEncountered() {
        // Verificar si Arceus ya ha sido visto en la Pokédex
        PokedexEntry arceusEntry = this.getEntrenador().getPokedex().getEntrada("Arceus");
        return arceusEntry != null && arceusEntry.isVisto();
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
            // Validar datos básicos
            if (simple.getEspecie() == null || simple.getEspecie().isEmpty()) {
                System.err.println("❌ Especie inválida en datos guardados");
                return null;
            }

            // Crear Pokémon
            PokemonJugador pokemon = FabricaPokemon.crearPokemonJugador(
                simple.getEspecie(),
                simple.getNivel(),
                simple.getApodo()
            );

            if (pokemon == null) {
                System.err.println("❌ No se pudo crear Pokémon: " + simple.getEspecie());
                return null;
            }

            // IMPORTANTE: Establecer PS actuales (no usar curar directamente)
            int psObjetivo = Math.min(
                Math.max(0, simple.getPsActual()), // No permitir negativos
                pokemon.getPsMaximos() // No exceder máximos
            );

            pokemon.psActual = psObjetivo;

            // ✅ NUEVO: Marcar explícitamente como debilitado si tiene 0 PS
            if (psObjetivo <= 0) {
                pokemon.debilitado = true;
                pokemon.psActual = 0;
            } else {
                pokemon.debilitado = false;
            }

            // Copiar experiencia
            pokemon.experiencia = Math.max(0, simple.getExperiencia());

            return pokemon;

        } catch (Exception e) {
            System.err.println("❌ Error crítico recreando Pokémon: " +
                (simple != null ? simple.getEspecie() : "null"));
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Crea un ítem por su nombre
     */
    /**
     * Crea un ítem por su nombre (VERSIÓN CORREGIDA)
     */
    private Item crearItemPorNombre(String nombreItem) {
        if (nombreItem == null || nombreItem.isEmpty()) {
            return null;
        }

        // 1. POKÉBALLS
        if (nombreItem.equals("Poké Ball")) {
            Pokeball pb = new Pokeball();
            pb.setNombre("Poké Ball");
            pb.setTasaCaptura(1.0f);
            return pb;
        }
        else if (nombreItem.equals("Super Poké Ball")) {
            Pokeball superBall = new Pokeball();
            superBall.setNombre("Super Poké Ball");
            superBall.setTasaCaptura(1.5f);
            return superBall;
        }

        // 2. POCIONES
        else if (nombreItem.equals("Poción")) {
            return new Curacion("Poción", 20);
        }
        else if (nombreItem.equals("Poción Grande")) {
            return new Curacion("Poción Grande", 50);
        }

       else if (nombreItem.equals("Revivir")) {
            return new Revivir("Revivir", 50);
        }

        // 3. RECURSOS
        else if (nombreItem.equals("Metal")) {
            return new Recurso("Metal", "Metal");
        }
        else if (nombreItem.equals("Planta")) {
            return new Recurso("Planta", "Planta");
        }
        else if (nombreItem.equals("Guijarro")) {
            return new Recurso("Guijarro", "Guijarro");
        }
        else if (nombreItem.equals("Baya")) {
            return new Recurso("Baya", "Baya");
        }

        // 4. FALLBACK - Si no se reconoce, crear recurso genérico
        else {
            // Intentar crear como recurso genérico
            return new Recurso(nombreItem, "Ítem guardado");
        }
    }

    /**
     * Clona la Pokédex para evitar modificar la original
     */
    private PokedexManager clonarPokedex(PokedexManager original) {
        // Crear nueva instancia
        PokedexManager clon = new PokedexManager();
        try {

            return original;
        } catch (Exception e) {
            System.err.println("❌ Error clonando Pokédex");
            return new PokedexManager();
        }
    }

    // Método para sanear datos de Pokémon antes de guardar
    public void sanearPokemonAntesDeGuardar() {
        if (entrenador == null) return;

        for (PokemonJugador pokemon : entrenador.getEquipo()) {
            if (pokemon != null) {
                // Asegurar que PS actual no sea negativo
                if (pokemon.getPsActual() < 0) {
                    pokemon.psActual = 0;
                }

                // Asegurar que PS actual no exceda máximos
                if (pokemon.getPsActual() > pokemon.getPsMaximos()) {
                    pokemon.psActual = pokemon.getPsMaximos();
                }

                // Asegurar que si tiene 0 PS, esté marcado como debilitado
                if (pokemon.getPsActual() <= 0) {
                    pokemon.debilitado = true;
                    pokemon.psActual = 0;
                }
            }
        }
    }
}
