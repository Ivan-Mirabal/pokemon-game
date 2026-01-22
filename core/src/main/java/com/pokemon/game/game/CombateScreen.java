package com.pokemon.game.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Align;
import com.pokemon.game.*;
import com.pokemon.game.player.*;
import com.pokemon.game.pokedex.PokedexEntry;
import com.pokemon.game.pokemon.*;
import com.pokemon.game.item.*;
import java.util.*;
import com.badlogic.gdx.Input.Keys;

/**
 * Pantalla que gestiona toda la interfaz gráfica y lógica de combate Pokémon por turnos.
 * Maneja la visualización de sprites, menús de selección, barras de salud, mensajes
 * de combate y la interacción entre el jugador y el sistema de combate subyacente.
 * Implementa un sistema de estados complejo para controlar el flujo del combate.
 */
public class CombateScreen implements Screen {

    /** Referencia a la clase principal del juego para coordinar cambios de pantalla */
    private final PokemonGame game;

    /** Pantalla de juego principal a la que se retorna después del combate */
    private final GameScreen gameScreen;

    /** Motor de lógica de combate que gestiona turnos, daños y resultados */
    private final Combate combate;

    /** Jugador que participa en el combate para acceder a equipo e inventario */
    private final Player player;

    /** Lote de sprites utilizado para renderizar todos los elementos gráficos de la pantalla */
    private SpriteBatch batch;

    // Texturas y fuentes
    private Texture whitePixel;
    private BitmapFont font;
    private Texture fondoCombate;

    // Sprites de combate (96x96)
    private Texture spriteJugador;
    private Texture spriteRival;

    /**
     * Estados posibles del sistema de menús del combate que determinan qué interfaz
     * se muestra al jugador y qué tipo de entrada se procesa en cada momento.
     */
    enum EstadoMenu {
        /** Animación inicial de entrada cuando comienza el combate */
        ANIMACION_ENTRADA,

        /** Menú principal con las cuatro opciones básicas de acción */
        MENU_PRINCIPAL,

        /** Menú de selección de movimientos entre los cuatro disponibles */
        MENU_MOVIMIENTOS,

        /** Menú para cambiar al Pokémon activo por otro del equipo */
        MENU_POKEMON,

        /** Menú para usar objetos del inventario durante el combate */
        MENU_OBJETOS,

        /** Muestra mensajes de texto sobre eventos del combate */
        MOSTRANDO_MENSAJE,

        /** Estado forzado cuando el Pokémon actual es debilitado */
        CAMBIO_FORZADO,

        /** Pantalla final que muestra el resultado del combate */
        FIN_COMBATE
    }

    /** Estado actual del sistema de menús que controla el flujo de la interfaz */
    private EstadoMenu estadoActual;

    // Variables de selección
    private int seleccionPrincipal;
    private int seleccionMovimiento;
    private int seleccionPokemon;
    private int seleccionObjeto;

    // Animación
    private float tiempoAnimacion;

    private List<String> mensajes;
    private int indiceMensaje;
    private int ultimoIndiceHistorial;

    // Posiciones (ajustadas para 96x96)
    private final float JUGADOR_X = 200;
    private final float JUGADOR_Y = 120;
    private final float RIVAL_X = 505;
    private final float RIVAL_Y = 300;

    // Tamaño de sprites en combate
    private final int SPRITE_SIZE = 96;

    // Colores (consistentes con tu tema)
    private final Color COLOR_FONDO = new Color(0.1f, 0.1f, 0.15f, 1);
    private final Color COLOR_MENU = new Color(0.18f, 0.18f, 0.24f, 0.95f);
    private final Color COLOR_BORDE = new Color(0.4f, 0.4f, 0.6f, 1);
    private final Color COLOR_TEXTO = new Color(0.9f, 0.9f, 1.0f, 1);
    private final Color COLOR_SELECCION = new Color(0.3f, 0.4f, 0.8f, 0.8f);

    // Para mostrar Pokémon disponibles
    private List<PokemonJugador> equipoVisible;

    // InputProcessor para manejar entrada
    private InputProcessor inputProcessor;

    private boolean finalizando = false;
    private boolean destruido = false;

    private boolean modoRevivir = false;

    // Constantes para posiciones
    private final float MENU_Y = 50;
    private final float MENU_HEIGHT = 120;
    private final float OPTION_BOX_SIZE = 80;

    private float shakeTime = 0;
    private float shakeIntensity = 0;
    private float transicionAlpha = 0;

    /**
     * Construye una nueva pantalla de combate inicializando todos los sistemas
     * gráficos, cargando recursos y configurando el estado inicial del combate.
     *
     * @param game Referencia principal al juego para cambios de pantalla
     * @param gameScreen Pantalla de juego a la que retornar después del combate
     * @param combate Motor de lógica de combate ya inicializado
     * @param player Jugador que participa en el combate
     */
    public CombateScreen(PokemonGame game, GameScreen gameScreen, Combate combate, Player player) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.combate = combate;
        this.player = player;

        this.batch = game.batch;
        this.estadoActual = EstadoMenu.ANIMACION_ENTRADA;
        this.tiempoAnimacion = 0;
        this.seleccionPrincipal = 0;
        this.seleccionMovimiento = 0;
        this.seleccionPokemon = 0;
        this.seleccionObjeto = 0;
        this.mensajes = new ArrayList<>();
        this.indiceMensaje = 0;
        this.ultimoIndiceHistorial = 0;

        mensajes.add("¡" + combate.getPokemonRival().getNombre() + " salvaje apareció!");
        indiceMensaje = 0;

        inicializarRecursos();
        cargarSpritesCombate();

        // Inicializar con el historial actual
        actualizarMensajes();

        // Obtener equipo para menú de cambio
        equipoVisible = new ArrayList<>();
        for (PokemonJugador p : player.getEntrenador().getEquipo()) {
            if (!p.estaDebilitado()) {
                equipoVisible.add(p);
            }
        }

        // Configurar input processor
        configurarInput();

        // Iniciar música de combate si existe
        if (game.musics != null) {
            try {
                game.musics.startBattleMusic();
            } catch (Exception e) {
                System.out.println("Música de combate no disponible");
            }
        }
    }

    /**
     * Inicializa todos los recursos gráficos básicos necesarios para renderizar
     * la pantalla de combate, incluyendo texturas, fuentes y fondos.
     */
    private void inicializarRecursos() {
        // Textura blanca para dibujar formas
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        // Fuente
        font = new BitmapFont();
        font.getData().setScale(1.0f);

        // Intentar cargar fondo
        try {
            fondoCombate = new Texture(Gdx.files.internal("sprites/combate/fondo.png"));
        } catch (Exception e) {
            fondoCombate = null;
        }
    }

    /**
     * Configura el procesador de entrada personalizado que captura las teclas
     * presionadas y las delega a los métodos de manejo según el estado actual.
     */
    private void configurarInput() {
        inputProcessor = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                return procesarTecla(keycode);
            }
        };
    }

    /**
     * Procesa una tecla presionada delegando al método correspondiente según
     * el estado actual del sistema de menús del combate.
     *
     * @param keycode Código de la tecla presionada según la clase Keys de LibGDX
     * @return true si la tecla fue procesada, false si se ignoró
     */
    private boolean procesarTecla(int keycode) {
        switch (estadoActual) {
            case MENU_PRINCIPAL:
                return manejarMenuPrincipal(keycode);
            case MENU_MOVIMIENTOS:
                return manejarMenuMovimientos(keycode);
            case MENU_POKEMON:
            case CAMBIO_FORZADO:
                return manejarMenuPokemon(keycode);
            case MENU_OBJETOS:
                return manejarMenuObjetos(keycode);
            case MOSTRANDO_MENSAJE:
                if (keycode == Keys.ENTER || keycode == Keys.SPACE) {
                    avanzarMensaje();
                    return true;
                }
                break;
            case FIN_COMBATE:
                if (keycode == Keys.SPACE || keycode == Keys.ENTER) {
                    terminarCombate();
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * Maneja la entrada del teclado cuando el menú principal está activo,
     * permitiendo navegar entre las cuatro opciones y seleccionar una.
     *
     * @param keycode Código de la tecla presionada
     * @return true si la tecla fue procesada, false si se ignoró
     */
    private boolean manejarMenuPrincipal(int keycode) {
        switch (keycode) {
            case Keys.UP:
                if (seleccionPrincipal == 0 || seleccionPrincipal == 1) {
                    seleccionPrincipal += 2;
                } else {
                    seleccionPrincipal -= 2;
                }
                return true;
            case Keys.DOWN:
                if (seleccionPrincipal == 2 || seleccionPrincipal == 3) {
                    seleccionPrincipal -= 2;
                } else {
                    seleccionPrincipal += 2;
                }
                return true;
            case Keys.LEFT:
                if (seleccionPrincipal == 0 || seleccionPrincipal == 2) {
                    seleccionPrincipal += 1;
                } else {
                    seleccionPrincipal -= 1;
                }
                return true;
            case Keys.RIGHT:
                if (seleccionPrincipal == 1 || seleccionPrincipal == 3) {
                    seleccionPrincipal -= 1;
                } else {
                    seleccionPrincipal += 1;
                }
                return true;
            case Keys.ENTER:
            case Keys.SPACE:
                ejecutarAccionMenuPrincipal();
                return true;
            case Keys.NUM_1:
            case Keys.NUMPAD_1:
                seleccionPrincipal = 0;
                ejecutarAccionMenuPrincipal();
                return true;
            case Keys.NUM_2:
            case Keys.NUMPAD_2:
                seleccionPrincipal = 1;
                ejecutarAccionMenuPrincipal();
                return true;
            case Keys.NUM_3:
            case Keys.NUMPAD_3:
                seleccionPrincipal = 2;
                ejecutarAccionMenuPrincipal();
                return true;
            case Keys.NUM_4:
            case Keys.NUMPAD_4:
                seleccionPrincipal = 3;
                ejecutarAccionMenuPrincipal();
                return true;
            case Keys.W:
                if (seleccionPrincipal == 0 || seleccionPrincipal == 1) {
                    seleccionPrincipal += 2;
                } else {
                    seleccionPrincipal -= 2;
                }
                return true;
            case Keys.S:
                if (seleccionPrincipal == 2 || seleccionPrincipal == 3) {
                    seleccionPrincipal -= 2;
                } else {
                    seleccionPrincipal += 2;
                }
                return true;
            case Keys.A:
                if (seleccionPrincipal == 0 || seleccionPrincipal == 2) {
                    seleccionPrincipal += 1;
                } else {
                    seleccionPrincipal -= 1;
                }
                return true;
            case Keys.D:
                if (seleccionPrincipal == 1 || seleccionPrincipal == 3) {
                    seleccionPrincipal -= 1;
                } else {
                    seleccionPrincipal += 1;
                }
                return true;
        }
        return false;
    }

    /**
     * Ejecuta la acción correspondiente a la opción seleccionada en el menú
     * principal, cambiando al estado de menú apropiado para continuar.
     */
    private void ejecutarAccionMenuPrincipal() {
        switch (seleccionPrincipal) {
            case 0:
                estadoActual = EstadoMenu.MENU_MOVIMIENTOS;
                seleccionMovimiento = 0;
                break;
            case 1:
                estadoActual = EstadoMenu.MENU_OBJETOS;
                seleccionObjeto = 0;
                break;
            case 2:
                modoRevivir = false;
                actualizarEquipoVisible();
                estadoActual = EstadoMenu.MENU_POKEMON;
                seleccionPokemon = 0;
                break;
            case 3:
                intentarHuir();
                break;
        }
    }

    /**
     * Maneja la entrada del teclado cuando el menú de movimientos está activo,
     * permitiendo navegar entre los movimientos disponibles y seleccionar uno.
     *
     * @param keycode Código de la tecla presionada
     * @return true si la tecla fue procesada, false si se ignoró
     */
    private boolean manejarMenuMovimientos(int keycode) {
        List<Movimiento> movimientos = combate.getPokemonJugador().getMovimientos();
        int totalMovimientos = movimientos.size();

        switch (keycode) {
            case Keys.UP:
                if (seleccionMovimiento >= 2) {
                    int nuevaSeleccion = seleccionMovimiento - 2;
                    if (nuevaSeleccion < totalMovimientos) {
                        seleccionMovimiento = nuevaSeleccion;
                    }
                }
                return true;
            case Keys.DOWN:
                if (seleccionMovimiento < 2) {
                    int nuevaSeleccion = seleccionMovimiento + 2;
                    if (nuevaSeleccion < totalMovimientos) {
                        seleccionMovimiento = nuevaSeleccion;
                    }
                }
                return true;
            case Keys.LEFT:
                if (seleccionMovimiento % 2 == 1) {
                    int nuevaSeleccion = seleccionMovimiento - 1;
                    if (nuevaSeleccion < totalMovimientos) {
                        seleccionMovimiento = nuevaSeleccion;
                    }
                }
                return true;
            case Keys.RIGHT:
                if (seleccionMovimiento % 2 == 0) {
                    int nuevaSeleccion = seleccionMovimiento + 1;
                    if (nuevaSeleccion < totalMovimientos) {
                        seleccionMovimiento = nuevaSeleccion;
                    }
                }
                return true;
            case Keys.NUM_1:
            case Keys.NUMPAD_1:
                if (0 < totalMovimientos) {
                    seleccionMovimiento = 0;
                    ejecutarMovimiento();
                }
                return true;
            case Keys.NUM_2:
            case Keys.NUMPAD_2:
                if (1 < totalMovimientos) {
                    seleccionMovimiento = 1;
                    ejecutarMovimiento();
                }
                return true;
            case Keys.NUM_3:
            case Keys.NUMPAD_3:
                if (2 < totalMovimientos) {
                    seleccionMovimiento = 2;
                    ejecutarMovimiento();
                }
                return true;
            case Keys.NUM_4:
            case Keys.NUMPAD_4:
                if (3 < totalMovimientos) {
                    seleccionMovimiento = 3;
                    ejecutarMovimiento();
                }
                return true;
            case Keys.ENTER:
            case Keys.SPACE:
                if (seleccionMovimiento < totalMovimientos) {
                    ejecutarMovimiento();
                }
                return true;
            case Keys.B:
            case Keys.ESCAPE:
                estadoActual = EstadoMenu.MENU_PRINCIPAL;
                return true;
        }
        return false;
    }

    /**
     * Maneja la entrada del teclado cuando el menú de Pokémon está activo,
     * permitiendo navegar entre los Pokémon disponibles y seleccionar uno.
     *
     * @param keycode Código de la tecla presionada
     * @return true si la tecla fue procesada, false si se ignoró
     */
    private boolean manejarMenuPokemon(int keycode) {
        switch (keycode) {
            case Keys.UP:
                if (seleccionPokemon >= 2) seleccionPokemon -= 2;
                return true;
            case Keys.DOWN:
                if (seleccionPokemon < 4 && seleccionPokemon + 2 < equipoVisible.size()) {
                    seleccionPokemon += 2;
                }
                return true;
            case Keys.LEFT:
                if (seleccionPokemon % 2 == 1) seleccionPokemon--;
                return true;
            case Keys.RIGHT:
                if (seleccionPokemon % 2 == 0 && seleccionPokemon + 1 < equipoVisible.size()) {
                    seleccionPokemon++;
                }
                return true;

            case Keys.ENTER:
            case Keys.SPACE:
                if (modoRevivir) {
                    ejecutarAccionRevivir();
                }
                else if (estadoActual == EstadoMenu.CAMBIO_FORZADO) {
                    cambiarPokemonForzado();
                } else {
                    cambiarPokemon();
                }
                return true;

            case Keys.B:
            case Keys.ESCAPE:
                if (modoRevivir) {
                    modoRevivir = false;
                    actualizarEquipoVisible();
                    estadoActual = EstadoMenu.MENU_OBJETOS;
                } else if (estadoActual != EstadoMenu.CAMBIO_FORZADO) {
                    estadoActual = EstadoMenu.MENU_PRINCIPAL;
                }
                return true;
        }
        return false;
    }

    /**
     * Maneja la entrada del teclado cuando el menú de objetos está activo,
     * permitiendo navegar entre los objetos disponibles y seleccionar uno.
     *
     * @param keycode Código de la tecla presionada
     * @return true si la tecla fue procesada, false si se ignoró
     */
    private boolean manejarMenuObjetos(int keycode) {
        List<Ranura> objetosCombate = new ArrayList<>();
        for (Ranura ranura : player.getInventario().getRanuras()) {
            Item item = ranura.getItem();
            if (item instanceof Pokeball || item instanceof Curacion || item instanceof Revivir) {
                objetosCombate.add(ranura);
            }
        }

        int totalObjetos = Math.min(objetosCombate.size(), 6);

        switch (keycode) {
            case Keys.UP:
                if (seleccionObjeto >= 2) {
                    int nuevaSeleccion = seleccionObjeto - 2;
                    if (nuevaSeleccion < totalObjetos) {
                        seleccionObjeto = nuevaSeleccion;
                    }
                }
                return true;
            case Keys.DOWN:
                if (seleccionObjeto < 4 && seleccionObjeto + 2 < totalObjetos) {
                    seleccionObjeto += 2;
                }
                return true;
            case Keys.LEFT:
                if (seleccionObjeto % 2 == 1 && seleccionObjeto - 1 < totalObjetos) {
                    seleccionObjeto--;
                }
                return true;
            case Keys.RIGHT:
                if (seleccionObjeto % 2 == 0 && seleccionObjeto + 1 < totalObjetos) {
                    seleccionObjeto++;
                }
                return true;
            case Keys.NUM_1:
            case Keys.NUMPAD_1:
                if (0 < totalObjetos) {
                    seleccionObjeto = 0;
                    usarObjeto();
                }
                return true;
            case Keys.NUM_2:
            case Keys.NUMPAD_2:
                if (1 < totalObjetos) {
                    seleccionObjeto = 1;
                    usarObjeto();
                }
                return true;
            case Keys.NUM_3:
            case Keys.NUMPAD_3:
                if (2 < totalObjetos) {
                    seleccionObjeto = 2;
                    usarObjeto();
                }
                return true;
            case Keys.NUM_4:
            case Keys.NUMPAD_4:
                if (3 < totalObjetos) {
                    seleccionObjeto = 3;
                    usarObjeto();
                }
                return true;
            case Keys.NUM_5:
            case Keys.NUMPAD_5:
                if (4 < totalObjetos) {
                    seleccionObjeto = 4;
                    usarObjeto();
                }
                return true;
            case Keys.NUM_6:
            case Keys.NUMPAD_6:
                if (5 < totalObjetos) {
                    seleccionObjeto = 5;
                    usarObjeto();
                }
                return true;
            case Keys.ENTER:
            case Keys.SPACE:
                if (seleccionObjeto < totalObjetos) {
                    usarObjeto();
                }
                return true;
            case Keys.B:
            case Keys.ESCAPE:
                estadoActual = EstadoMenu.MENU_PRINCIPAL;
                return true;
        }
        return false;
    }

    /**
     * Carga los sprites gráficos de los Pokémon participantes en el combate
     * desde el sistema de archivos, creando placeholders si no se encuentran.
     */
    private void cargarSpritesCombate() {
        String nombreJugador = combate.getPokemonJugador().getNombre().toLowerCase();
        String nombreRival = combate.getPokemonRival().getNombre().toLowerCase();

        // Cargar sprite del jugador (back)
        try {
            spriteJugador = new Texture(Gdx.files.internal(
                "sprites/pokemon/" + nombreJugador + "/back.png"
            ));
        } catch (Exception e) {
            spriteJugador = crearSpritePlaceholder(Color.BLUE);
        }

        // Cargar sprite del rival (front)
        try {
            spriteRival = new Texture(Gdx.files.internal(
                "sprites/pokemon/" + nombreRival + "/front.png"
            ));
        } catch (Exception e) {
            spriteRival = crearSpritePlaceholder(Color.RED);
        }
    }

    /**
     * Crea una textura de placeholder para un Pokémon cuando no se encuentra
     * su sprite original, utilizando un círculo de color con características
     * faciales básicas.
     *
     * @param color Color base para el sprite de placeholder
     * @return Textura generada como marcador de posición
     */
    private Texture crearSpritePlaceholder(Color color) {
        Pixmap pixmap = new Pixmap(SPRITE_SIZE, SPRITE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillCircle(SPRITE_SIZE/2, SPRITE_SIZE/2, SPRITE_SIZE/2 - 5);

        // Ojos
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(SPRITE_SIZE/3, 2*SPRITE_SIZE/3, 10);
        pixmap.fillCircle(2*SPRITE_SIZE/3, 2*SPRITE_SIZE/3, 10);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Renderiza cada frame de la pantalla de combate, actualizando el estado
     * interno y dibujando todos los elementos gráficos según el estado actual.
     *
     * @param delta Tiempo transcurrido en segundos desde el último frame
     */
    @Override
    public void render(float delta) {
        if (destruido) return;
        actualizar(delta);
        dibujar();
    }

    /**
     * Activa un efecto de temblor en pantalla para enfatizar impactos fuertes
     * o eventos importantes durante el combate.
     *
     * @param intensidad Fuerza del efecto de temblor en píxeles
     * @param duracion Duración del efecto en segundos
     */
    public void activarShake(float intensidad, float duracion) {
        shakeTime = duracion;
        shakeIntensity = intensidad;
    }

    /**
     * Actualiza el estado interno de la pantalla de combate, incluyendo
     * temporizadores de animación y transiciones entre estados.
     *
     * @param delta Tiempo transcurrido en segundos desde el último frame
     */
    private void actualizar(float delta) {
        switch (estadoActual) {
            case ANIMACION_ENTRADA:
                tiempoAnimacion += delta;
                if (tiempoAnimacion >= 1.0f) {
                    estadoActual = EstadoMenu.MENU_PRINCIPAL;
                }
                break;
        }
    }

    /**
     * Dibuja todos los elementos gráficos de la pantalla de combate según
     * el estado actual, incluyendo Pokémon, barras de salud y menús.
     */
    private void dibujar() {
        ScreenUtils.clear(COLOR_FONDO);
        batch.begin();

        if (transicionAlpha > 0) {
            batch.setColor(1, 1, 1, transicionAlpha);
            batch.draw(whitePixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(Color.WHITE);
            transicionAlpha -= Gdx.graphics.getDeltaTime() * 2;
        }

        // Fondo
        if (fondoCombate != null) {
            batch.draw(fondoCombate, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        // Efecto de shake
        float shakeX = 0, shakeY = 0;
        if (shakeTime > 0) {
            shakeX = (float) (Math.random() - 0.5) * shakeIntensity;
            shakeY = (float) (Math.random() - 0.5) * shakeIntensity;
            shakeTime -= Gdx.graphics.getDeltaTime();
        }

        // Dibujar Pokémon rival
        if (spriteRival != null) {
            batch.draw(spriteRival, RIVAL_X + shakeX, RIVAL_Y + shakeY, SPRITE_SIZE, SPRITE_SIZE);
        }

        // Dibujar Pokémon jugador
        if (spriteJugador != null) {
            float offsetY = (float) Math.sin(Gdx.graphics.getFrameId() * 0.1f) * 2;
            batch.draw(spriteJugador, JUGADOR_X + shakeX, JUGADOR_Y + offsetY + shakeY,
                SPRITE_SIZE, SPRITE_SIZE);
        }

        // Dibujar barras de salud
        dibujarBarraSalud(combate.getPokemonJugador(), 575, 180, true);
        dibujarBarraSalud(combate.getPokemonRival(), 50, 450, false);

        // Dibujar interfaz según estado
        switch (estadoActual) {
            case ANIMACION_ENTRADA:
                dibujarAnimacionEntrada();
                break;
            case MENU_PRINCIPAL:
                dibujarMenuPrincipal();
                break;
            case MENU_MOVIMIENTOS:
                dibujarMenuMovimientos();
                break;
            case MENU_POKEMON:
            case CAMBIO_FORZADO:
                dibujarMenuPokemon();
                break;
            case MENU_OBJETOS:
                dibujarMenuObjetos();
                break;
            case MOSTRANDO_MENSAJE:
                dibujarMensaje();
                break;
            case FIN_COMBATE:
                dibujarFinCombate();
                break;
        }

        batch.end();
    }

    /**
     * Dibuja una barra de salud visual para un Pokémon específico en la
     * posición indicada, con colores que varían según el porcentaje de salud.
     *
     * @param pokemon Pokémon cuya barra de salud se va a dibujar
     * @param x Posición horizontal en píxeles donde dibujar la barra
     * @param y Posición vertical en píxeles donde dibujar la barra
     * @param esJugador true si es el Pokémon del jugador, false si es rival
     */
    private void dibujarBarraSalud(Pokemon pokemon, float x, float y, boolean esJugador) {
        float anchoBarra = 180;
        float altoBarra = 12;

        // Fondo de la barra
        batch.setColor(new Color(0.3f, 0.3f, 0.3f, 1));
        batch.draw(whitePixel, x, y, anchoBarra, altoBarra);

        // Calcular porcentaje de salud
        float porcentaje = (float) pokemon.getPsActual() / pokemon.getPsMaximos();

        // Color de la barra según salud
        Color colorBarra;
        if (porcentaje > 0.5) {
            colorBarra = new Color(0.4f, 0.8f, 0.2f, 1);
        } else if (porcentaje > 0.2) {
            colorBarra = new Color(1f, 0.8f, 0.2f, 1);
        } else {
            colorBarra = new Color(0.9f, 0.2f, 0.2f, 1);
        }

        // Barra de salud
        batch.setColor(colorBarra);
        batch.draw(whitePixel, x + 1, y + 1, (anchoBarra - 2) * porcentaje, altoBarra - 2);

        // Borde de la barra
        batch.setColor(new Color(0.1f, 0.1f, 0.1f, 1));
        batch.draw(whitePixel, x, y, anchoBarra, 1);
        batch.draw(whitePixel, x, y + altoBarra, anchoBarra, 1);
        batch.draw(whitePixel, x, y, 1, altoBarra);
        batch.draw(whitePixel, x + anchoBarra, y, 1, altoBarra);

        // Información del Pokémon
        font.setColor(Color.WHITE);
        String nombre = esJugador ? pokemon.getApodo() : pokemon.getNombre();
        font.draw(batch, nombre, x, y + altoBarra + 15);

        // PS: actual/máximo
        font.setColor(new Color(0.8f, 0.8f, 0.9f, 1));
        font.draw(batch, "PS: " + pokemon.getPsActual() + "/" + pokemon.getPsMaximos(),
            x, y + altoBarra + 30);

        // Nivel
        font.setColor(new Color(0.9f, 0.9f, 0.5f, 1));
        font.draw(batch, "Nv." + pokemon.getNivel(), x + anchoBarra - 40, y + altoBarra + 15);
    }

    /**
     * Dibuja el menú principal de combate con las cuatro opciones básicas
     * organizadas en una cuadrícula 2x2 con resaltado de selección.
     */
    private void dibujarMenuPrincipal() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Panel inferior completo
        float panelY = MENU_Y - 50;
        float panelAlto = MENU_HEIGHT;

        // Fondo del panel
        batch.setColor(new Color(0f, 0f, 0f, 0.9f));
        batch.draw(whitePixel, 0, panelY, screenWidth, panelAlto);

        // Bordes decorativos
        batch.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
        batch.draw(whitePixel, 0, panelY, screenWidth, 4);
        batch.draw(whitePixel, 0, panelY + panelAlto - 4, screenWidth, 4);
        batch.setColor(Color.WHITE);

        // Configurar cuadrícula 2x2
        float cellWidth = screenWidth / 2;
        float cellHeight = panelAlto / 2;

        // Posiciones de las 4 opciones en cuadrícula 2x2
        float[][] posiciones = {
            {0, panelY + cellHeight},
            {cellWidth, panelY + cellHeight},
            {0, panelY},
            {cellWidth, panelY}
        };

        // Dibujar líneas divisorias
        batch.setColor(new Color(0.3f, 0.3f, 0.4f, 1));
        batch.draw(whitePixel, cellWidth - 2, panelY, 4, panelAlto);
        batch.draw(whitePixel, 0, panelY + cellHeight - 2, screenWidth, 4);
        batch.setColor(Color.WHITE);

        // Opciones del menú
        String[] opciones = {"LUCHAR", "BOLSA", "POKéMON", "HUIR"};

        // Fuente más grande para mejor visibilidad
        font.getData().setScale(1.4f);

        for (int i = 0; i < 4; i++) {
            float x = posiciones[i][0];
            float y = posiciones[i][1];
            boolean esSeleccionada = (i == seleccionPrincipal);

            // Fondo de la celda si está seleccionada
            if (esSeleccionada) {
                batch.setColor(new Color(0.3f, 0.4f, 0.8f, 0.5f));
                batch.draw(whitePixel, x, y, cellWidth, cellHeight);

                // Borde de selección
                batch.setColor(new Color(0.4f, 0.6f, 1.0f, 1));
                batch.draw(whitePixel, x, y, cellWidth, 3);
                batch.draw(whitePixel, x, y + cellHeight, cellWidth, 3);
                batch.draw(whitePixel, x, y, 3, cellHeight);
                batch.draw(whitePixel, x + cellWidth, y, 3, cellHeight);
                batch.setColor(Color.WHITE);
            }

            // Dibujar opción
            font.setColor(esSeleccionada ? Color.YELLOW : new Color(0.9f, 0.9f, 1f, 1));

            String opcion = opciones[i];
            GlyphLayout layout = new GlyphLayout(font, opcion);
            float textoX = x + (cellWidth - layout.width) / 2;
            float textoY = y + (cellHeight + layout.height) / 2;

            font.draw(batch, opcion, textoX, textoY);

            // Número de tecla rápida
            font.getData().setScale(1.0f);
            font.setColor(new Color(0.5f, 0.5f, 0.7f, 0.6f));
            font.draw(batch, String.valueOf(i + 1), x + 10, y + cellHeight - 10);
            font.getData().setScale(1.4f);
        }

        // Restaurar tamaño de fuente
        font.getData().setScale(1.0f);
    }

    /**
     * Dibuja el menú de movimientos mostrando los cuatro movimientos del
     * Pokémon activo con información detallada de cada uno.
     */
    private void dibujarMenuMovimientos() {
        List<Movimiento> movimientos = combate.getPokemonJugador().getMovimientos();

        // Usar todo el panel inferior
        float panelY = MENU_Y - 50;
        float panelAlto = MENU_HEIGHT;
        float panelAncho = Gdx.graphics.getWidth();

        // Fondo del panel
        batch.setColor(new Color(0f, 0f, 0f, 0.8f));
        batch.draw(whitePixel, 0, panelY, panelAncho, panelAlto);

        // Borde decorativo
        batch.setColor(new Color(0.3f, 0.3f, 0.5f, 1));
        batch.draw(whitePixel, 0, panelY, panelAncho, 4);
        batch.draw(whitePixel, 0, panelY + panelAlto - 4, panelAncho, 4);
        batch.setColor(Color.WHITE);

        // Configurar la cuadrícula 2x2
        float cellWidth = panelAncho / 2;
        float cellHeight = panelAlto / 2;

        // Posiciones de las 4 ranuras
        float[][] posiciones = {
            {0, panelY + cellHeight},
            {cellWidth, panelY + cellHeight},
            {0, panelY},
            {cellWidth, panelY}
        };

        // Divisorias
        batch.setColor(new Color(0.3f, 0.3f, 0.4f, 1));
        batch.draw(whitePixel, cellWidth - 1, panelY, 2, panelAlto);
        batch.draw(whitePixel, 0, panelY + cellHeight - 1, panelAncho, 2);
        batch.setColor(Color.WHITE);

        // Fuente más pequeña
        font.getData().setScale(0.9f);

        // Dibujar las 4 ranuras
        for (int i = 0; i < 4; i++) {
            float x = posiciones[i][0];
            float y = posiciones[i][1];

            boolean tieneMovimiento = i < movimientos.size();
            boolean esSeleccionada = (i == seleccionMovimiento);

            // Resaltar seleccionada
            if (esSeleccionada && tieneMovimiento) {
                batch.setColor(new Color(0.3f, 0.4f, 0.8f, 0.3f));
                batch.draw(whitePixel, x, y, cellWidth, cellHeight);
                batch.setColor(Color.WHITE);
            }

            if (tieneMovimiento) {
                Movimiento m = movimientos.get(i);

                float nombreY = y + cellHeight - 30;
                float infoY = y + 20;

                // Nombre del ataque
                font.setColor(esSeleccionada ? Color.YELLOW : COLOR_TEXTO);

                String nombre = m.getNombre();
                if (nombre.length() > 15) {
                    nombre = nombre.substring(0, 15) + "...";
                }

                GlyphLayout nombreLayout = new GlyphLayout(font, nombre);
                float nombreX = x + (cellWidth - nombreLayout.width) / 2;
                font.draw(batch, nombre, nombreX, nombreY);

                // Tipo
                font.setColor(getColorPorTipo(m.getTipo()));
                font.draw(batch, m.getTipo().toString(), x + 10, infoY);

                // PP
                String ppTexto = "PP: " + m.getPpActual() + "/" + m.getPpMax();
                float ppPorcentaje = (float) m.getPpActual() / m.getPpMax();
                if (ppPorcentaje <= 0.2f) {
                    font.setColor(Color.RED);
                } else if (ppPorcentaje <= 0.5f) {
                    font.setColor(Color.YELLOW);
                } else {
                    font.setColor(new Color(0.8f, 0.8f, 0.9f, 1));
                }

                GlyphLayout ppLayout = new GlyphLayout(font, ppTexto);
                float ppX = x + cellWidth - ppLayout.width - 10;
                font.draw(batch, ppTexto, ppX, infoY);

            } else {
                // Ranura vacía
                font.setColor(new Color(0.5f, 0.5f, 0.7f, 1));
                String textoVacio = "--- VACÍO ---";
                GlyphLayout vacioLayout = new GlyphLayout(font, textoVacio);
                float vacioX = x + (cellWidth - vacioLayout.width) / 2;
                float vacioY = y + cellHeight / 2;
                font.draw(batch, textoVacio, vacioX, vacioY);
            }

            // Número de la ranura
            font.getData().setScale(0.7f);
            font.setColor(new Color(0.4f, 0.4f, 0.6f, 0.6f));
            font.draw(batch, String.valueOf(i + 1), x + 5, y + cellHeight - 8);
            font.getData().setScale(0.9f);
        }

        // Restaurar tamaño de fuente
        font.getData().setScale(1.0f);
    }

    /**
     * Dibuja el menú de cambio de Pokémon mostrando todos los Pokémon no
     * debilitados del equipo con información detallada de cada uno.
     */
    private void dibujarMenuPokemon() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Fondo semi-transparente oscuro
        batch.setColor(new Color(0f, 0f, 0f, 0.85f));
        batch.draw(whitePixel, 0, 0, screenWidth, screenHeight);
        batch.setColor(Color.WHITE);

        // Cabecera con título
        float headerY = screenHeight - 60;

        // Fondo de cabecera
        batch.setColor(new Color(0.1f, 0.1f, 0.2f, 0.9f));
        batch.draw(whitePixel, 0, headerY - 40, screenWidth, 70);
        batch.setColor(Color.WHITE);

        // Título según estado
        String titulo;
        Color colorTitulo;

        if (estadoActual == EstadoMenu.CAMBIO_FORZADO) {
            titulo = "¡POKÉMON DEBILITADO!";
            colorTitulo = new Color(0.9f, 0.2f, 0.2f, 1);
        } else {
            titulo = "CAMBIO DE POKÉMON";
            colorTitulo = new Color(0.4f, 0.7f, 1.0f, 1);
        }

        // Título principal
        font.getData().setScale(2.0f);
        font.setColor(colorTitulo);

        GlyphLayout tituloLayout = new GlyphLayout(font, titulo);
        float tituloX = (screenWidth - tituloLayout.width) / 2;
        float tituloY = headerY;
        font.draw(batch, titulo, tituloX, tituloY);
        font.getData().setScale(1.0f);

        // Subtítulo
        String subtitulo;
        if (estadoActual == EstadoMenu.CAMBIO_FORZADO) {
            subtitulo = "SELECCIONA OTRO POKÉMON PARA CONTINUAR";
        } else {
            subtitulo = "¿A QUÉ POKÉMON QUIERES CAMBIAR?";
        }

        font.setColor(new Color(0.8f, 0.8f, 1.0f, 1));
        GlyphLayout subtituloLayout = new GlyphLayout(font, subtitulo);
        float subtituloX = (screenWidth - subtituloLayout.width) / 2;
        float subtituloY = headerY - 25;
        font.draw(batch, subtitulo, subtituloX, subtituloY);

        // Información del Pokémon actual
        Pokemon actual = combate.getPokemonJugador();
        font.setColor(new Color(0.9f, 0.9f, 0.5f, 1));
        String actualTexto = "Actual: " + actual.getApodo() + " (Nv. " + actual.getNivel() + ")";
        GlyphLayout actualLayout = new GlyphLayout(font, actualTexto);
        float actualX = (screenWidth - actualLayout.width) / 2;
        float actualY = headerY - 45;
        font.draw(batch, actualTexto, actualX, actualY);

        // Diseño de cuadrícula 2x3
        float startY = headerY - 175;

        float boxWidth = 320;
        float boxHeight = 100;
        float horizontalSpacing = 40;
        float verticalSpacing = 25;

        // Centrar horizontalmente toda la cuadrícula
        float totalWidth = (2 * boxWidth) + horizontalSpacing;
        float gridStartX = (screenWidth - totalWidth) / 2;

        // Posiciones para 2 columnas, 3 filas
        float[][] positions = {
            {gridStartX, startY},
            {gridStartX + boxWidth + horizontalSpacing, startY},
            {gridStartX, startY - boxHeight - verticalSpacing},
            {gridStartX + boxWidth + horizontalSpacing, startY - boxHeight - verticalSpacing},
            {gridStartX, startY - 2 * (boxHeight + verticalSpacing)},
            {gridStartX + boxWidth + horizontalSpacing, startY - 2 * (boxHeight + verticalSpacing)}
        };

        // Dibujar los 6 slots
        for (int slotIndex = 0; slotIndex < 6; slotIndex++) {
            float x = positions[slotIndex][0];
            float y = positions[slotIndex][1];

            boolean tienePokemon = slotIndex < equipoVisible.size();
            boolean esSeleccionado = (slotIndex == seleccionPokemon);
            boolean esActual = tienePokemon && (equipoVisible.get(slotIndex) == actual);

            // Dibujar cuadro del slot
            Color colorFondo;
            Color colorBorde;

            if (esActual) {
                colorFondo = new Color(0.3f, 0.3f, 0.1f, 0.8f);
                colorBorde = new Color(0.9f, 0.9f, 0.3f, 1);
            } else if (esSeleccionado && tienePokemon) {
                colorFondo = new Color(0.2f, 0.4f, 0.8f, 0.8f);
                colorBorde = new Color(0.4f, 0.6f, 1.0f, 1);
            } else {
                colorFondo = new Color(0.15f, 0.15f, 0.2f, 0.8f);
                colorBorde = new Color(0.4f, 0.4f, 0.5f, 1);
            }

            // Fondo del cuadro
            batch.setColor(colorFondo);
            batch.draw(whitePixel, x, y, boxWidth, boxHeight);

            // Borde del cuadro
            batch.setColor(colorBorde);
            batch.draw(whitePixel, x, y, boxWidth, 3);
            batch.draw(whitePixel, x, y + boxHeight, boxWidth, 3);
            batch.draw(whitePixel, x, y, 3, boxHeight);
            batch.draw(whitePixel, x + boxWidth, y, 3, boxHeight);
            batch.setColor(Color.WHITE);

            // Dibujar Pokémon si existe
            if (tienePokemon) {
                PokemonJugador p = equipoVisible.get(slotIndex);

                // SPRITE (56x56)
                float spriteX = x + 10;
                float spriteY = y + (boxHeight - 56) / 2;

                if (p.getSprite() != null) {
                    batch.draw(p.getSprite(), spriteX, spriteY, 56, 56);
                } else {
                    batch.setColor(getColorPorTipo(p.getTipoPrimario()));
                    batch.draw(whitePixel, spriteX, spriteY, 56, 56);
                    batch.setColor(Color.WHITE);

                    font.setColor(Color.WHITE);
                    String inicial = p.getNombre().substring(0, 1).toUpperCase();
                    font.draw(batch, inicial, spriteX + 18, spriteY + 35);
                }

                // Información a la derecha del sprite
                float infoX = x + 80;
                float infoTopY = y + boxHeight - 15;

                // NOMBRE
                String nombreTexto = p.getApodo();
                if (esActual) {
                    nombreTexto += " (ACTUAL)";
                    font.setColor(new Color(1f, 1f, 0.5f, 1));
                } else if (esSeleccionado) {
                    font.setColor(new Color(0.7f, 0.9f, 1.0f, 1));
                } else {
                    font.setColor(new Color(0.9f, 0.9f, 1.0f, 1));
                }
                font.draw(batch, nombreTexto, infoX, infoTopY);

                // NIVEL
                font.setColor(new Color(0.8f, 0.8f, 0.9f, 1));
                font.draw(batch, "Nv." + p.getNivel(), x + boxWidth - 60, infoTopY);

                // TIPO(S)
                float tipoY = y + boxHeight - 40;
                font.setColor(getColorPorTipo(p.getTipoPrimario()));
                String tipoTexto = p.getTipoPrimario().toString();
                if (p.getTipoSecundario() != null) {
                    tipoTexto += "/" + p.getTipoSecundario().toString();
                }
                font.draw(batch, tipoTexto, infoX, tipoY);

                // BARRA DE PS
                float porcentajePS = (float) p.getPsActual() / p.getPsMaximos();
                float barraX = infoX;
                float barraY = y + 20;
                float barraAncho = 180;
                float barraAlto = 14;

                // Fondo barra
                batch.setColor(new Color(0.3f, 0.3f, 0.4f, 1));
                batch.draw(whitePixel, barraX, barraY, barraAncho, barraAlto);

                // Color según salud
                Color colorBarra;
                if (p.estaDebilitado()) {
                    colorBarra = new Color(0.5f, 0.5f, 0.5f, 1);
                } else if (porcentajePS > 0.5) {
                    colorBarra = new Color(0.4f, 0.8f, 0.2f, 1);
                } else if (porcentajePS > 0.2) {
                    colorBarra = new Color(1f, 0.8f, 0.2f, 1);
                } else {
                    colorBarra = new Color(0.9f, 0.2f, 0.2f, 1);
                }

                // Barra de salud
                batch.setColor(colorBarra);
                batch.draw(whitePixel, barraX + 2, barraY + 2, (barraAncho - 4) * porcentajePS, barraAlto - 4);
                batch.setColor(Color.WHITE);

                // Texto PS
                font.setColor(new Color(0.7f, 0.7f, 0.9f, 1));
                String psTexto = p.estaDebilitado() ? "DEBILITADO" : p.getPsActual() + "/" + p.getPsMaximos();
                font.draw(batch, psTexto, barraX + barraAncho + 10, barraY + barraAlto/2 + 4);

            } else {
                // SLOT VACÍO
                font.setColor(new Color(0.5f, 0.5f, 0.7f, 1));
                String textoVacio = "--- VACÍO ---";
                float textoWidth = textoVacio.length() * 10;
                float textoX = x + (boxWidth - textoWidth) / 2;
                float textoY = y + boxHeight / 2;
                font.draw(batch, textoVacio, textoX, textoY);
            }
        }

        // Instrucciones
        float instruccionesY = 60;

        // Fondo para instrucciones
        batch.setColor(new Color(0f, 0f, 0f, 0.7f));
        batch.draw(whitePixel, 0, instruccionesY - 20, screenWidth, 50);
        batch.setColor(Color.WHITE);

        font.setColor(new Color(0.7f, 0.7f, 0.9f, 1));
        String instrucciones;

        if (estadoActual == EstadoMenu.CAMBIO_FORZADO) {
            instrucciones = "FLECHAS: Navegar  ENTER: Cambiar Pokémon (OBLIGATORIO)";
        } else {
            instrucciones = "FLECHAS: Navegar  ENTER: Seleccionar  B/ESC: Cancelar";
        }

        GlyphLayout instruccionesLayout = new GlyphLayout(font, instrucciones);
        float instruccionesX = (screenWidth - instruccionesLayout.width) / 2;
        font.draw(batch, instrucciones, instruccionesX, instruccionesY);
    }

    /**
     * Dibuja el menú de objetos mostrando los objetos de combate disponibles
     * en el inventario del jugador organizados en una cuadrícula 2x3.
     */
    private void dibujarMenuObjetos() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Fondo semi-transparente oscuro
        batch.setColor(new Color(0f, 0f, 0f, 0.85f));
        batch.draw(whitePixel, 0, 0, screenWidth, screenHeight);
        batch.setColor(Color.WHITE);

        // Cabecera con título
        float headerY = screenHeight - 60;

        // Fondo de cabecera
        batch.setColor(new Color(0.1f, 0.1f, 0.2f, 0.9f));
        batch.draw(whitePixel, 0, headerY - 40, screenWidth, 70);
        batch.setColor(Color.WHITE);

        // Título
        font.getData().setScale(2.0f);
        font.setColor(new Color(0.4f, 0.8f, 1.0f, 1));

        String titulo = "MOCHILA DE COMBATE";
        GlyphLayout tituloLayout = new GlyphLayout(font, titulo);
        float tituloX = (screenWidth - tituloLayout.width) / 2;
        float tituloY = headerY;
        font.draw(batch, titulo, tituloX, tituloY);
        font.getData().setScale(1.0f);

        // Subtítulo
        font.setColor(new Color(0.8f, 0.8f, 1.0f, 1));
        String subtitulo = "SELECCIONA UN OBJETO PARA USAR";
        GlyphLayout subtituloLayout = new GlyphLayout(font, subtitulo);
        float subtituloX = (screenWidth - subtituloLayout.width) / 2;
        float subtituloY = headerY - 25;
        font.draw(batch, subtitulo, subtituloX, subtituloY);

        // Información del Pokémon actual
        Pokemon actual = combate.getPokemonJugador();
        font.setColor(new Color(0.9f, 0.9f, 0.5f, 1));
        String actualTexto = "Actual: " + actual.getApodo() + " (Nv. " + actual.getNivel() + ")";
        GlyphLayout actualLayout = new GlyphLayout(font, actualTexto);
        float actualX = (screenWidth - actualLayout.width) / 2;
        float actualY = headerY - 45;
        font.draw(batch, actualTexto, actualX, actualY);

        // Diseño de cuadrícula 2x3
        float startY = headerY - 175;

        float boxWidth = 320;
        float boxHeight = 100;
        float horizontalSpacing = 40;
        float verticalSpacing = 25;

        // Centrar horizontalmente toda la cuadrícula
        float totalWidth = (2 * boxWidth) + horizontalSpacing;
        float gridStartX = (screenWidth - totalWidth) / 2;

        // Posiciones para 2 columnas, 3 filas
        float[][] positions = {
            {gridStartX, startY},
            {gridStartX + boxWidth + horizontalSpacing, startY},
            {gridStartX, startY - boxHeight - verticalSpacing},
            {gridStartX + boxWidth + horizontalSpacing, startY - boxHeight - verticalSpacing},
            {gridStartX, startY - 2 * (boxHeight + verticalSpacing)},
            {gridStartX + boxWidth + horizontalSpacing, startY - 2 * (boxHeight + verticalSpacing)}
        };

        // Obtener objetos de combate del inventario
        Inventario inventario = player.getInventario();
        List<Ranura> todasLasRanuras = inventario.getRanuras();
        List<Ranura> objetosCombate = new ArrayList<>();

        for (Ranura ranura : todasLasRanuras) {
            Item item = ranura.getItem();
            if (item instanceof Pokeball || item instanceof Curacion || item instanceof Revivir) {
                objetosCombate.add(ranura);
            }
        }

        // Dibujar los 6 slots
        for (int slotIndex = 0; slotIndex < 6; slotIndex++) {
            float x = positions[slotIndex][0];
            float y = positions[slotIndex][1];

            boolean tieneObjeto = slotIndex < objetosCombate.size();
            boolean esSeleccionado = (slotIndex == seleccionObjeto);

            // Dibujar cuadro del slot
            Color colorFondo;
            Color colorBorde;

            if (esSeleccionado && tieneObjeto) {
                colorFondo = new Color(0.2f, 0.4f, 0.8f, 0.8f);
                colorBorde = new Color(0.4f, 0.6f, 1.0f, 1);
            } else {
                colorFondo = new Color(0.15f, 0.15f, 0.2f, 0.8f);
                colorBorde = new Color(0.4f, 0.4f, 0.5f, 1);
            }

            // Fondo del cuadro
            batch.setColor(colorFondo);
            batch.draw(whitePixel, x, y, boxWidth, boxHeight);

            // Borde del cuadro
            batch.setColor(colorBorde);
            batch.draw(whitePixel, x, y, boxWidth, 3);
            batch.draw(whitePixel, x, y + boxHeight, boxWidth, 3);
            batch.draw(whitePixel, x, y, 3, boxHeight);
            batch.draw(whitePixel, x + boxWidth, y, 3, boxHeight);
            batch.setColor(Color.WHITE);

            // Dibujar objeto si existe
            if (tieneObjeto) {
                Ranura ranura = objetosCombate.get(slotIndex);
                Item item = ranura.getItem();

                // ICONO según tipo de objeto (56x56)
                float iconoX = x + 10;
                float iconoY = y + (boxHeight - 56) / 2;
                float iconoSize = 56;

                // Color del icono según tipo
                Color colorIcono;
                String textoIcono = "";

                if (item instanceof Pokeball) {
                    colorIcono = new Color(1f, 0.3f, 0.3f, 1);
                    textoIcono = "POKÉ";
                } else if (item instanceof Revivir) {
                    colorIcono = new Color(1f, 1f, 0.2f, 1);
                    textoIcono = "REVIVE";
                } else if (item instanceof Curacion) {
                    colorIcono = new Color(0.3f, 1f, 0.3f, 1);
                    textoIcono = "CURAR";
                } else {
                    colorIcono = new Color(0.8f, 0.8f, 0.3f, 1);
                    textoIcono = "ITEM";
                }

                // Dibujar icono circular
                batch.setColor(colorIcono);
                batch.draw(whitePixel, iconoX, iconoY, iconoSize, iconoSize);

                // Borde del icono
                batch.setColor(new Color(1f, 1f, 1f, 0.3f));
                batch.draw(whitePixel, iconoX, iconoY, iconoSize, 1);
                batch.draw(whitePixel, iconoX, iconoY + iconoSize, iconoSize, 1);
                batch.draw(whitePixel, iconoX, iconoY, 1, iconoSize);
                batch.draw(whitePixel, iconoX + iconoSize, iconoY, 1, iconoSize);

                // Texto del icono
                font.getData().setScale(0.8f);
                font.setColor(Color.WHITE);
                GlyphLayout iconoLayout = new GlyphLayout(font, textoIcono);
                float iconoTextoX = iconoX + (iconoSize - iconoLayout.width) / 2;
                float iconoTextoY = iconoY + (iconoSize + iconoLayout.height) / 2;
                font.draw(batch, textoIcono, iconoTextoX, iconoTextoY);
                font.getData().setScale(1.0f);
                batch.setColor(Color.WHITE);

                // Información a la derecha del icono
                float infoX = x + 80;
                float infoTopY = y + boxHeight - 15;

                // NOMBRE DEL OBJETO
                font.setColor(esSeleccionado ? new Color(1f, 1f, 0.5f, 1) : new Color(0.9f, 0.9f, 1f, 1));
                String nombreItem = item.getNombre();
                if (nombreItem.length() > 15) {
                    nombreItem = nombreItem.substring(0, 13) + "...";
                }
                font.draw(batch, nombreItem, infoX, infoTopY);

                // CANTIDAD
                font.setColor(new Color(0.8f, 0.8f, 0.9f, 1));
                String cantidadTexto = "x" + ranura.getCantidad();
                font.draw(batch, cantidadTexto, x + boxWidth - 60, infoTopY);

                // EFECTO
                float efectoY = y + boxHeight - 40;
                font.setColor(colorIcono);

                String efectoTexto = "";
                if (item instanceof Revivir) {
                    Revivir rev = (Revivir) item;
                    efectoTexto = "REVIVE: " + rev.getPorcentajeRecuperacion() + "% PS";
                } else if (item instanceof Pokeball) {
                    Pokeball pokeball = (Pokeball) item;
                    efectoTexto = "CAPTURA: " + pokeball.getTasaCaptura();
                } else if (item instanceof Curacion) {
                    Curacion curacion = (Curacion) item;
                    efectoTexto = "CURA: " + curacion.getHpRestaurado() + " PS";
                }

                font.draw(batch, efectoTexto, infoX, efectoY);

            } else {
                // SLOT VACÍO
                font.setColor(new Color(0.5f, 0.5f, 0.7f, 1));
                String textoVacio = "--- VACÍO ---";
                float textoWidth = textoVacio.length() * 10;
                float textoX = x + (boxWidth - textoWidth) / 2;
                float textoY = y + boxHeight / 2;
                font.draw(batch, textoVacio, textoX, textoY);
            }
        }

        // Instrucciones
        float instruccionesY = 30;

        // Fondo para instrucciones
        batch.setColor(new Color(0f, 0f, 0f, 0.7f));
        batch.draw(whitePixel, 0, instruccionesY - 15, screenWidth, 40);
        batch.setColor(Color.WHITE);

        font.setColor(new Color(0.7f, 0.7f, 0.9f, 1));
        String instrucciones = "FLECHAS: Navegar  1-6: Selección rápida  ENTER: Usar  B/ESC: Volver";

        // Centrar instrucciones
        GlyphLayout instruccionesLayout = new GlyphLayout(font, instrucciones);
        float instruccionesX = (screenWidth - instruccionesLayout.width) / 2;
        font.draw(batch, instrucciones, instruccionesX, instruccionesY + 10);
    }

    /**
     * Dibuja el panel de mensajes que muestra eventos y resultados del combate,
     * permitiendo al jugador avanzar a través de múltiples mensajes secuenciales.
     */
    private void dibujarMensaje() {
        // Panel de mensaje
        batch.setColor(COLOR_MENU);
        batch.draw(whitePixel, 0, 50, Gdx.graphics.getWidth(), 100);

        // Borde
        batch.setColor(COLOR_BORDE);
        batch.draw(whitePixel, 0, 50, Gdx.graphics.getWidth(), 2);
        batch.draw(whitePixel, 0, 150, Gdx.graphics.getWidth(), 2);

        // Mensaje actual
        if (indiceMensaje < mensajes.size()) {
            String mensaje = mensajes.get(indiceMensaje);

            GlyphLayout layout = new GlyphLayout();
            font.setColor(COLOR_TEXTO);
            layout.setText(font, mensaje, Color.WHITE, Gdx.graphics.getWidth() - 100, Align.left, true);
            font.draw(batch, layout, 50, 130);
        }

        // Indicador de continuar
        font.setColor(new Color(0.6f, 0.6f, 0.8f, 1));
        font.draw(batch, "Presiona Enter para continuar...", 50, 70);
    }

    /**
     * Actualiza la lista de mensajes mostrados en pantalla con los eventos
     * recientes del historial de combate, añadiendo solo los mensajes nuevos.
     */
    private void actualizarMensajes() {
        List<String> historialCompleto = combate.getHistorial();
        if (mensajes.size() < historialCompleto.size()) {
            for (int i = mensajes.size(); i < historialCompleto.size(); i++) {
                mensajes.add(historialCompleto.get(i));
            }
        }
    }

    /**
     * Dibuja la animación inicial de entrada al combate con efecto de fundido
     * que gradualmente revela la escena de combate completa.
     */
    private void dibujarAnimacionEntrada() {
        // Efecto de fade in
        float alpha = Math.min(1.0f, tiempoAnimacion);
        batch.setColor(1, 1, 1, 1 - alpha);
        batch.draw(whitePixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);
    }

    /**
     * Dibuja la pantalla final del combate que muestra el resultado (victoria,
     * derrota, captura o huida) y permite al jugador continuar.
     */
    private void dibujarFinCombate() {
        // Panel de resultado
        batch.setColor(COLOR_MENU);
        batch.draw(whitePixel, 200, 200, 400, 200);

        // Borde
        batch.setColor(COLOR_BORDE);
        batch.draw(whitePixel, 200, 200, 400, 2);
        batch.draw(whitePixel, 200, 400, 400, 2);
        batch.draw(whitePixel, 200, 200, 2, 200);
        batch.draw(whitePixel, 600, 200, 2, 200);

        // Resultado según motivo
        String resultado = "";
        Color colorResultado = Color.WHITE;

        if (combate.getMotivoFin() != null) {
            switch (combate.getMotivoFin()) {
                case "victoria":
                    resultado = "¡HAS GANADO!";
                    colorResultado = Color.GREEN;
                    break;
                case "derrota":
                    resultado = "HAS PERDIDO...";
                    colorResultado = Color.RED;
                    break;
                case "captura":
                    resultado = "¡CAPTURA EXITOSA!";
                    colorResultado = Color.BLUE;
                    break;
                case "huida":
                    resultado = "HUÍDA EXITOSA";
                    colorResultado = Color.YELLOW;
                    break;
                default:
                    // Por defecto, usar ganador
                    Pokemon ganador = combate.getGanador();
                    if (ganador == combate.getPokemonJugador()) {
                        resultado = "¡HAS GANADO!";
                        colorResultado = Color.GREEN;
                    } else {
                        resultado = "HAS PERDIDO...";
                        colorResultado = Color.RED;
                    }
            }
        } else {
            // Por defecto, usar ganador
            Pokemon ganador = combate.getGanador();
            if (ganador == combate.getPokemonJugador()) {
                resultado = "¡HAS GANADO!";
                colorResultado = Color.GREEN;
            } else {
                resultado = "HAS PERDIDO...";
                colorResultado = Color.RED;
            }
        }

        font.getData().setScale(1.5f);
        font.setColor(colorResultado);
        font.draw(batch, resultado, 300, 350);
        font.getData().setScale(1.0f);

        // Instrucciones
        font.setColor(new Color(0.7f, 0.7f, 0.9f, 1));
        font.draw(batch, "Presiona ESPACIO para continuar", 250, 250);
    }

    /**
     * Obtiene un color representativo para un tipo elemental específico de
     * Pokémon, utilizado para resaltar visualmente movimientos y tipos.
     *
     * @param tipo Tipo elemental del Pokémon o movimiento
     * @return Color asociado al tipo especificado
     */
    private Color getColorPorTipo(Tipo tipo) {
        switch (tipo) {
            case FUEGO: return Color.ORANGE;
            case AGUA: return Color.BLUE;
            case PLANTA: return Color.GREEN;
            case ELECTRICO: return Color.YELLOW;
            case NORMAL: return Color.LIGHT_GRAY;
            default: return Color.WHITE;
        }
    }

    /**
     * Ejecuta el movimiento seleccionado por el jugador en el combate actual,
     * validando que sea posible y actualizando el estado del combate.
     */
    private void ejecutarMovimiento() {
        List<Movimiento> movimientos = combate.getPokemonJugador().getMovimientos();

        // Validar que la ranura seleccionada tenga un movimiento
        if (seleccionMovimiento >= movimientos.size()) {
            mensajes.clear();
            mensajes.add("¡Esta ranura de movimiento está vacía!");
            indiceMensaje = 0;
            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
            return;
        }

        if (!combate.isTurnoJugador()) {
            mensajes.clear();
            mensajes.add("¡No es tu turno! El rival ataca...");
            indiceMensaje = 0;
            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
            return;
        }

        // Guardar el tamaño del historial ANTES de la acción
        int historialAntes = combate.getHistorial().size();

        // Ejecutar el turno
        Combate.ResultadoTurno resultado = combate.ejecutarTurnoJugador(seleccionMovimiento);

        // Preparar mensajes para mostrar
        prepararMensajesParaAccion();

        // Si no hay mensajes nuevos, agregar uno por defecto
        if (mensajes.isEmpty()) {
            mensajes.add("¡" + combate.getPokemonJugador().getNombre() + " atacó!");
        }

        estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
    }

    /**
     * Cambia el Pokémon activo en combate por otro del equipo del jugador,
     * validando que el nuevo Pokémon esté disponible y no debilitado.
     */
    private void cambiarPokemon() {
        if (seleccionPokemon >= equipoVisible.size()) return;

        PokemonJugador nuevo = equipoVisible.get(seleccionPokemon);

        // Validaciones
        if (nuevo.estaDebilitado()) {
            mensajes.clear();
            mensajes.add("¡" + nuevo.getApodo() + " está debilitado!");
            indiceMensaje = 0;
            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
            return;
        }

        if (nuevo == combate.getPokemonJugador()) {
            mensajes.clear();
            mensajes.add("¡" + nuevo.getApodo() + " ya está en combate!");
            indiceMensaje = 0;
            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
            return;
        }

        // Guardar historial antes
        int historialAntes = combate.getHistorial().size();

        // Cambiar Pokémon
        Combate.ResultadoTurno resultado = combate.cambiarPokemon(nuevo);

        if (resultado == Combate.ResultadoTurno.EXITO) {
            // Actualizar sprite del jugador
            cargarSpritesCombate();

            // Preparar mensajes
            prepararMensajesParaAccion();

            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
        } else {
            // Manejar errores
            mensajes.clear();
            switch (resultado) {
                case NO_ES_TU_TURNO:
                    mensajes.add("¡No es tu turno!");
                    break;
                case POKEMON_DEBILITADO:
                    mensajes.add("¡Ese Pokémon está debilitado!");
                    break;
                default:
                    mensajes.add("No se pudo cambiar el Pokémon");
            }
            indiceMensaje = 0;
            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
        }
    }

    /**
     * Maneja el cambio forzado de Pokémon cuando el actual ha sido debilitado,
     * obligando al jugador a seleccionar un Pokémon alternativo para continuar.
     */
    private void cambiarPokemonForzado() {
        if (seleccionPokemon >= equipoVisible.size()) return;

        PokemonJugador nuevo = equipoVisible.get(seleccionPokemon);

        // Validaciones específicas para cambio forzado
        if (nuevo.estaDebilitado()) {
            mensajes.clear();
            mensajes.add("¡" + nuevo.getApodo() + " está debilitado!");
            mensajes.add("Selecciona un Pokémon que pueda luchar.");
            indiceMensaje = 0;
            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
            return;
        }

        // Cambiar Pokémon
        Combate.ResultadoTurno resultado = combate.cambiarPokemon(nuevo);

        if (resultado == Combate.ResultadoTurno.EXITO) {
            // Actualizar sprite
            cargarSpritesCombate();

            // Actualizar mensajes
            actualizarMensajes();
            indiceMensaje = mensajes.size() - 1;

            // Volver a estado normal
            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
        }
    }

    /**
     * Usa el objeto seleccionado del inventario durante el combate, aplicando
     * su efecto correspondiente (captura, curación o revivir).
     */
    private void usarObjeto() {
        // Obtenemos las ranuras de items de combate
        List<Ranura> itemsCombate = new ArrayList<>();
        for (Ranura ranura : player.getInventario().getRanuras()) {
            Item item = ranura.getItem();
            if (item instanceof Pokeball || item instanceof Curacion || item instanceof Revivir) {
                itemsCombate.add(ranura);
            }
        }

        if (itemsCombate.isEmpty()) {
            mensajes.clear();
            mensajes.add("¡No tienes objetos!");
            indiceMensaje = 0;
            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
            return;
        }

        if (seleccionObjeto >= itemsCombate.size()) {
            seleccionObjeto = 0;
        }

        Ranura ranura = itemsCombate.get(seleccionObjeto);
        Item item = ranura.getItem();

        // Guardar historial antes
        int historialAntes = combate.getHistorial().size();

        if (item instanceof Revivir) {
            modoRevivir = true;
            actualizarEquipoVisible();
            estadoActual = EstadoMenu.MENU_POKEMON;
            seleccionPokemon = 0;
            return;
        }

        if (item instanceof Pokeball) {
            // Intentar captura
            boolean capturado = combate.intentarCaptura(player.getEntrenador(), (Pokeball)item);

            // Preparar mensajes
            prepararMensajesParaAccion();

            if (capturado) {
                estadoActual = EstadoMenu.FIN_COMBATE;
            } else {
                estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
            }
        }
        else if (item instanceof Curacion) {
            Curacion pocion = (Curacion)item;

            // Curar al Pokémon actual
            int hpAntes = combate.getPokemonJugador().getPsActual();
            combate.getPokemonJugador().curar(pocion.getHpRestaurado());
            int curado = combate.getPokemonJugador().getPsActual() - hpAntes;

            // Gastar del inventario
            player.getEntrenador().getInventario().removerItem(pocion.getNombre(), 1);

            // Mostrar mensajes de curación
            mensajes.clear();
            mensajes.add("Usaste " + pocion.getNombre() + ".");
            mensajes.add("¡Recuperaste " + curado + " PS!");
            indiceMensaje = 0;

            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
        }
    }

    /**
     * Ejecuta la acción de revivir un Pokémon debilitado utilizando un objeto
     * Revivir del inventario, restaurando un porcentaje de su salud máxima.
     */
    private void ejecutarAccionRevivir() {
        PokemonJugador seleccionado = equipoVisible.get(seleccionPokemon);

        if (seleccionado.estaDebilitado()) {
            // Aplicar efecto
            seleccionado.revivir(50);

            // Consumir el item del inventario
            player.getInventario().removerItem("Revivir", 1);

            // Configurar mensajes
            mensajes.clear();
            mensajes.add("¡" + seleccionado.getApodo() + " ha revivido!");

            // Finalizar turno y limpiar bandera
            modoRevivir = false;
            combate.ejecutarTurnoRival();
            prepararMensajesParaAccion();
            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
        } else {
            // Error si intentas revivir a alguien sano
            mensajes.clear();
            mensajes.add("¡" + seleccionado.getApodo() + " no necesita eso!");
            indiceMensaje = 0;
            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
        }
    }

    /**
     * Intenta que el jugador huya del combate con una probabilidad basada en
     * la comparación de velocidad entre los Pokémon combatientes.
     */
    private void intentarHuir() {
        // Guardar historial antes
        int historialAntes = combate.getHistorial().size();

        // Usar el método del combate
        boolean exito = combate.intentarHuir();

        // Preparar mensajes
        prepararMensajesParaAccion();

        if (exito) {
            // Si huyó con éxito, terminar
            estadoActual = EstadoMenu.FIN_COMBATE;
        } else {
            // Si falló, mostrar mensajes
            estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
        }
    }

    /**
     * Avanza al siguiente mensaje en la secuencia o, si no hay más mensajes,
     * determina el siguiente estado del combate basándose en las condiciones
     * actuales (turno rival, cambio forzado, fin del combate, etc.).
     */
    private void avanzarMensaje() {
        indiceMensaje++;

        if (indiceMensaje >= mensajes.size()) {
            if (combate.isCombateTerminado()) {
                estadoActual = EstadoMenu.FIN_COMBATE;
            } else if (combate.getPokemonJugador().estaDebilitado()) {
                // Verificar si hay más Pokémon vivos en el equipo
                boolean tienePokemonVivos = false;
                for (PokemonJugador p : player.getEntrenador().getEquipo()) {
                    if (!p.estaDebilitado() && p != combate.getPokemonJugador()) {
                        tienePokemonVivos = true;
                        break;
                    }
                }

                if (tienePokemonVivos) {
                    // Forzar cambio de Pokémon
                    estadoActual = EstadoMenu.CAMBIO_FORZADO;
                    mensajes.clear();
                    mensajes.add("¡" + combate.getPokemonJugador().getNombre() + " fue debilitado!");
                    mensajes.add("Selecciona otro Pokémon para continuar.");
                    indiceMensaje = 0;

                    // Actualizar lista de equipo visible
                    actualizarEquipoVisible();
                } else {
                    // No hay más Pokémon vivos - derrota
                    combate.registrarDerrotaCompleta(player.getEntrenador());
                    estadoActual = EstadoMenu.FIN_COMBATE;
                }
            } else if (!combate.isTurnoJugador()) {
                // Es turno del rival - ejecutar su acción
                int historialAntes = combate.getHistorial().size();
                combate.ejecutarTurnoRival();

                // Preparar mensajes del ataque rival
                prepararMensajesParaAccion();

                if (mensajes.isEmpty()) {
                    mensajes.add(combate.getPokemonRival().getNombre() + " atacó!");
                }

                indiceMensaje = 0;
                estadoActual = EstadoMenu.MOSTRANDO_MENSAJE;
            } else {
                // Es turno del jugador
                estadoActual = EstadoMenu.MENU_PRINCIPAL;
            }
        }
    }

    /**
     * Actualiza la lista de Pokémon visibles en los menús de selección,
     * filtrando según el modo actual (normal o modo revivir).
     */
    private void actualizarEquipoVisible() {
        equipoVisible.clear();
        for (PokemonJugador p : player.getEntrenador().getEquipo()) {
            if (modoRevivir || !p.estaDebilitado()) {
                equipoVisible.add(p);
            }
        }
    }

    /**
     * Finaliza el combate limpiando recursos, otorgando recompensas si corresponde,
     * restaurando la música del mundo y retornando a la pantalla de juego principal.
     */
    private void terminarCombate() {
        if (finalizando) return;
        finalizando = true;

        // Obtener motivo del fin
        String motivo = combate.getMotivoFin();

        // SOLO otorgar recompensas si es victoria
        if (motivo != null && motivo.equals("victoria")) {
            combate.otorgarRecompensasVictoria(player.getEntrenador());
        }

        // Restaurar música del mundo
        if (game.musics != null) {
            try {
                game.musics.stopBattleMusic();
                game.musics.startopenworldmusic();
            } catch (Exception e) {
                System.out.println("Música de combate no disponible");
            }
        }

        PokedexEntry arceusEntry = player.getEntrenador().getPokedex().getEntrada("Arceus");
        if (arceusEntry != null && arceusEntry.estaCompletamenteInvestigado()) {
            System.out.println("==========================================");
            System.out.println("¡OBJETIVO DE LA SIMULACIÓN COMPLETADO!");
            System.out.println("Has investigado completamente a ARCEUS");
            System.out.println("==========================================");

            game.setScreen(gameScreen);
        }

        // Volver a GameScreen
        game.setScreen(gameScreen);
    }

    /**
     * Actualiza la lista de mensajes mostrados desde el historial del combate,
     * manteniendo un registro del último índice procesado para evitar duplicados.
     */
    private void actualizarMensajesDesdeHistorial() {
        List<String> historial = combate.getHistorial();

        for (int i = ultimoIndiceHistorial; i < historial.size(); i++) {
            mensajes.add(historial.get(i));
        }

        ultimoIndiceHistorial = historial.size();
    }

    /**
     * Prepara la lista de mensajes para mostrar después de una acción de combate,
     * limpiando mensajes anteriores y cargando los nuevos del historial.
     */
    private void prepararMensajesParaAccion() {
        // Limpiar mensajes anteriores de la pantalla
        mensajes.clear();
        indiceMensaje = 0;

        // Obtener mensajes nuevos del historial
        actualizarMensajesDesdeHistorial();
    }

    /**
     * Se ejecuta cuando esta pantalla se convierte en la pantalla activa del juego,
     * configurando el procesador de entrada para capturar las teclas del jugador.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputProcessor);
    }

    /** Maneja el redimensionamiento de la ventana de visualización */
    @Override
    public void resize(int width, int height) {}

    /** Se ejecuta cuando el juego entra en estado de pausa */
    @Override
    public void pause() {}

    /** Se ejecuta cuando el juego se reanuda después de una pausa */
    @Override
    public void resume() {}

    /**
     * Se ejecuta cuando esta pantalla deja de ser la pantalla activa,
     * limpiando el procesador de entrada para evitar conflictos.
     */
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    /**
     * Libera todos los recursos gráficos utilizados por esta pantalla para
     * prevenir fugas de memoria cuando ya no es necesaria.
     */
    @Override
    public void dispose() {
        if (destruido) return;
        destruido = true;

        Gdx.app.postRunnable(() -> {
            if (font != null) {
                font.dispose();
                font = null;
            }
            if (whitePixel != null) { whitePixel.dispose(); whitePixel = null; }
            if (fondoCombate != null) { fondoCombate.dispose(); fondoCombate = null; }
            if (spriteJugador != null) { spriteJugador.dispose(); spriteJugador = null; }
            if (spriteRival != null) { spriteRival.dispose(); spriteRival = null; }
        });
    }
}
