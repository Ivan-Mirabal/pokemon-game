package com.pokemon.game.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.pokemon.game.*;
import com.pokemon.game.game.GameScreen;
import com.pokemon.game.item.*;
import com.pokemon.game.pokemon.Entrenador;
import com.pokemon.game.pokemon.FabricaPokemon;
import com.pokemon.game.pokemon.PokemonJugador;

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
    private boolean inSubMenu;

    // Variables para selección de inventario
    private Ranura itemSeleccionado = null;
    private int inventarioPage = 0;
    private final int ITEMS_PER_PAGE = 10;

    private Crafteo sistemaCrafteo;
    private int seleccionCrafteo;

    // Para navegación en equipo (2 columnas)
    private int pokemonTeamSelection = 0;
    private int pokemonDetailTab = 0; // 0: Estadísticas, 1: Movimientos, 2: Naturaleza, 3: Encontrado

    private Entrenador entrenador;


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
        PokemonJugador bulbasaur = FabricaPokemon.crearPokemonJugador("Bulbasaur", 8, "Bulby");
        entrenador.agregarPokemon(bulbasaur);

        PokemonJugador squirtle = FabricaPokemon.crearPokemonJugador("Squirtle", 7, "Squirty");
        entrenador.agregarPokemon(squirtle);

        // INICIALIZAR ESTADO DEL MENÚ
        this.menuState = MenuState.NONE;
        this.menuSelection = 0;
        this.inSubMenu = false;

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
        this.inSubMenu = (state != MenuState.MAIN && state != MenuState.NONE);
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
                // Tu código existente para inventario
                List<Ranura> slots = inventario.getRanuras();
                if (!slots.isEmpty()) {
                    int indiceReal = menuSelection + (inventarioPage * ITEMS_PER_PAGE);
                    if (indiceReal < slots.size()) {
                        itemSeleccionado = slots.get(indiceReal);
                        System.out.println("=== ITEM SELECCIONADO ===");
                        System.out.println("Nombre: " + itemSeleccionado.getItem().getNombre());
                        System.out.println("Cantidad: " + itemSeleccionado.getCantidad());
                        System.out.println("Descripción: " + itemSeleccionado.getItem().getDescripcion());
                    }
                }
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

    public void goBack() {
        if (inSubMenu) {
            setMenuState(MenuState.MAIN);
        } else {
            setMenuState(MenuState.NONE);
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
                break;
            case 2: // Inventario
                setMenuState(MenuState.INVENTORY);
                break;
            case 3: // Crafteo
                setMenuState(MenuState.CRAFTING);
                seleccionCrafteo = 0; // Resetear selección de crafteo
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

    private int getMaxMenuItems() {
        switch (menuState) {
            case MAIN:
                return 6;
            case INVENTORY:
                return this.getInventario().getRanuras().size();
            case OPTIONS:
                return 4; // ← ¡CORRECTO! 4 opciones en el menú de opciones
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

        // Usar pokemonTeamSelection, que es la ÚNICA variable de selección
        int indice = pokemonTeamSelection;

        // Asegurar que el índice esté dentro del equipo
        if (indice >= equipo.size()) {
            // Si seleccionas un slot vacío, ir al último Pokémon disponible
            indice = equipo.size() - 1;
        }

        return equipo.get(indice);
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

}
