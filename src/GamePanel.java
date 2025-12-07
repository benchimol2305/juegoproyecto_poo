import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

// Clase principal que maneja el panel del juego
class GamePanel extends JPanel implements ActionListener{
    // Constantes de dimensiones de la ventana
    public static final int SCREEN_WIDTH = 600;
    public static final int SCREEN_HEIGHT = 600;
    public static final int UNIT_SIZE = 25;
    // La ventana debe ser cuadrada para que funcione correctamente
    public static final int GAME_UNITS = (int) (SCREEN_WIDTH/UNIT_SIZE) * (SCREEN_HEIGHT/UNIT_SIZE);
    public static final int HORIZONTAL_UNITS = SCREEN_WIDTH/UNIT_SIZE;
    public static final int VERTICAL_UNITS = SCREEN_HEIGHT/UNIT_SIZE;
    public static final int DELAY = 100; // Retardo del temporizador en milisegundos
    public static final int INITIAL_SNAKE_SIZE = 6; // tamanp inicial de la serpiente

    // Variables de estado del juego
    private boolean running = false;
    private int appleX;
    private int appleY;
    private Timer timer = new Timer(DELAY, this);
    private char direction;
    private int[] snakeX = new int[GAME_UNITS];
    private int[] snakeY = new int[GAME_UNITS];
    private int snakeSize;
    private int applesEaten;