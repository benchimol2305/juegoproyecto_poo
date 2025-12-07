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

    SnakeFrame parentFrame;
    boolean keyInput = false;

    // Variables para el sistema de puntuacion
    private int lowestScore;
    private ArrayList<Score> scoreList = new ArrayList<Score>();
    private boolean showJTextField = false;
    private String playerName = "";

    // Mensajes aleatorios para cuando termina el juego
    String[] gameOverMessages = {"suerte la proxima!", "Lo sentimos querido!", "Dale que se puede!",
            "GG WP", "Hora de dormir zzz", "cambia de teclado", "Ow :(",
            "Uhh que manco!", "mejor ve a jugar barbie"};
    String randomGameOverMessage = "";
    private Score actualScore;

    // Constructor del panel del juego
    GamePanel(JFrame frame){
        parentFrame = (SnakeFrame) frame;

        // El panel debe ser enfocable para recibir eventos de teclado
        this.setFocusable(true);
        this.requestFocus();
        this.addKeyListener(new MyKeyAdapter());
        this.setPreferredSize(new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT));
        this.setBackground(Color.black);
    }

    // Metodo para iniciar el juego
    public void startGame(){
        snakeSize = INITIAL_SNAKE_SIZE;
        applesEaten = 0;

        for(int i = 0; i < snakeSize; i++){
            snakeX[i] = 0;
            snakeY[i] = 0;
        }
        direction = 'R';
        timer.start();
        newApple();
        System.out.println("Initialized game panel startGame()");
        loadScoreList();
        loadLowestScore();
        randomGameOverMessage = gameOverMessages[random(gameOverMessages.length)];
    }

    // Metodo para cargar la lista de puntuaciones desde archivo
    public void loadScoreList(){
        try{
            scoreList.clear();
            BufferedReader buffer = new BufferedReader(new FileReader(new File("scores.data")));
            String line;
            String[] nameScore;
            Score aux;


            while((line = buffer.readLine()) != null){
                nameScore = line.split(",");
                aux = new Score(nameScore[0], Integer.parseInt(nameScore[1]));
                scoreList.add(aux);
            }
            System.out.println("ArrayList loaded successfully");
            System.out.println(scoreList);
        }catch(Exception ex){
            System.out.println("Error trying to read file");
        }
    }

    // Metodo para cargar la puntuacion mas baja del top 10
    public void loadLowestScore(){
        // Ordena la lista de mayor a menor
        scoreList.sort(Comparator.reverseOrder());
        lowestScore = scoreList.get(9).getScore(); // Obtiene la décima puntuación (la más baja del top 10)
        System.out.println("lowestScore: " + lowestScore);
    }


    // Metodo ejecutado por el temporizador en cada ciclo
    public void actionPerformed(ActionEvent ev){
        move(); // Mueve la serpiente
        checkCollision(); // Verifica colisiones
        eatApple(); // Verifica si se comió una manzana
        repaint(); // Vuelve a dibujar el panel
    }