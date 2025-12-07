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

    // Metodo para dibujar componentes graficos
    public void paintComponent(Graphics g){
        super.paintComponent(g); // Llama al metodo de la superclase

        // Dibujar manzana en appleX y appleY
        g.setColor(Color.red);
        g.fillOval(appleX , appleY, UNIT_SIZE, UNIT_SIZE);

        // Dibujar cabeza de la serpiente
        g.setColor(Color.green);
        g.fillRect(snakeX[0], snakeY[0], UNIT_SIZE, UNIT_SIZE);
        // Dibujar cuerpo de la serpiente
        for(int i = 1; i < snakeSize; i++){
            g.fillRect(snakeX[i], snakeY[i], UNIT_SIZE, UNIT_SIZE);
        }

        // Dibujar cadena de puntuacion
        g.setColor(Color.white);
        g.setFont(new Font("MS Gothic", Font.PLAIN, 25));
        FontMetrics fontSize = g.getFontMetrics();
        int fontX = SCREEN_WIDTH - fontSize.stringWidth("Puntaje: " + applesEaten) - 10;
        int fontY = fontSize.getHeight();
        g.drawString("Puntaje: " + applesEaten, fontX, fontY);


        if(!timer.isRunning()){
            // Imprimir pantalla de fin de juego
            g.setColor(Color.white);
            g.setFont(new Font("MS Gothic", Font.PLAIN, 58));
            String message = randomGameOverMessage;
            fontSize = g.getFontMetrics();
            fontX = (SCREEN_WIDTH - fontSize.stringWidth(message)) / 2 ;
            fontY = (SCREEN_HEIGHT - fontSize.getHeight()) /2;
            g.drawString(message, fontX, fontY);

            g.setFont(new Font("MS Gothic", Font.PLAIN, 24));
            message = "Presiona F2 para reiniciar";
            fontSize = g.getFontMetrics();
            fontX = (SCREEN_WIDTH - fontSize.stringWidth(message)) / 2 ;
            fontY = fontY + fontSize.getHeight() + 20;
            g.drawString(message, fontX, fontY);


            if(showJTextField){
                drawJTextField(g);
                drawPlayerName(g);
            }
        }
    }

    // Metodo para dibujar indicación de ingreso de nombre
    public void drawJTextField(Graphics g){
        g.setFont(new Font("MS Gothic", Font.PLAIN, 24));
        String message = "Ingresa tu nombre:";
        FontMetrics fontSize = g.getFontMetrics();
        // Centrar horizontalmente
        int fontX = (SCREEN_WIDTH - fontSize.stringWidth(message)) / 2 ;
        g.drawString(message, fontX, 350);
    }

    // Metodo para dibujar el nombre del jugador
    public void drawPlayerName(Graphics g){
        g.setFont(new Font("MS Gothic", Font.PLAIN, 24));
        FontMetrics fontSize = g.getFontMetrics();
        // Centrar horizontalmente
        int fontX = (SCREEN_WIDTH - fontSize.stringWidth(playerName)) / 2 ;
        g.drawString(playerName, fontX, 400);
    }



    // Metodo para generar una nueva manzana en posicion aleatoria
    public void newApple(){
        // Número aleatorio entre 0 y 23 * unit size
        int x = random(HORIZONTAL_UNITS) * UNIT_SIZE;
        int y = random(VERTICAL_UNITS) * UNIT_SIZE;
        Point provisional = new Point(x,y);
        Point snakePos = new Point();
        boolean newApplePermission = true;

        // Verifica que la manzana no aparezca sobre la serpiente
        for(int i = 0; i < snakeSize; i++){
            snakePos.setLocation(snakeX[i], snakeY[i]);
            if(provisional.equals(snakePos)){
                newApplePermission = false;
            }
        }

        if(newApplePermission){
            appleX = x;
            appleY = y;
        }else{
            newApple();
        }
    }

    // Metodo para verificar colisiones
    public void checkCollision(){
        // Colisión con los bordes de la pantalla
        if(snakeX[0] >= (SCREEN_WIDTH) || snakeX[0] < 0 || snakeY[0] >= (SCREEN_HEIGHT) || snakeY[0] < 0){
            gameOver();
        }

        // Colisión con el propio cuerpo
        for(int i = 1; i < snakeSize; i++){
            if((snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])){
                gameOver();
            }
        }
    }

    // Metodo para verificar si la serpiente comio una manzana
    public void eatApple(){
        if(snakeX[0] == appleX && snakeY[0] == appleY){
            snakeSize++;
            applesEaten++;
            newApple();
        }
    }

    // Metodo para mover la serpiente
    public void move(){
        // Este método se ejecuta cada vez que el temporizador lo permite
        // Hay que recorrer la serpiente de atrás hacia adelante
        for(int i = snakeSize; i > 0; i--){
            snakeX[i] = snakeX[i-1];
            snakeY[i] = snakeY[i-1];
        }

        // Mueve la cabeza según la dirección actual
        switch(direction){
            case 'R':
                snakeX[0] += UNIT_SIZE;
                break;
            case 'L':
                snakeX[0] -= UNIT_SIZE;
                break;
            case 'U':
                snakeY[0] -= UNIT_SIZE;
                break;
            case 'D':
                snakeY[0] += UNIT_SIZE;
                break;
        }

        keyInput = false; // Permite nueva entrada de teclado
    }

    // Método para finalizar el juego
    public void gameOver(){
        timer.stop(); // Detiene el temporizador

        // Si la puntuación es mayor que la más baja del top 10
        if(applesEaten > lowestScore){
            showJTextField = true; // Muestra campo para ingresar nombre
        }
    }


    // Metodo para generar numero aleatorio
    public int random(int range){
        // Retorna un entero entre 0 y range-1
        return (int)(Math.random() * range);
    }

    // Clase interna para manejar eventos de teclado
    class MyKeyAdapter extends KeyAdapter{
        public void keyPressed(KeyEvent k){

            switch(k.getKeyCode()){
                case (KeyEvent.VK_DOWN):
                    if(direction != 'U' && keyInput == false){
                        direction = 'D';
                        keyInput = true;
                    }
                    break;
                case (KeyEvent.VK_UP):
                    if(direction != 'D' && !keyInput){
                        direction = 'U';
                        keyInput = true;
                    }
                    break;
                case (KeyEvent.VK_LEFT):
                    if(direction != 'R' && keyInput == false){
                        direction = 'L';
                        keyInput = true;
                    }
                    break;
                case (KeyEvent.VK_RIGHT):
                    if(direction != 'L' && keyInput == false){
                        direction = 'R';
                        keyInput = true;
                    }
                    break;
                case (KeyEvent.VK_F2):
                    if(!timer.isRunning()){
                        startGame();
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    parentFrame.switchToLobbyPanel();
                    break;
            }

            // Manejo de entrada de texto para nombre del jugador
            if(showJTextField){
                if(k.getKeyCode() == KeyEvent.VK_ENTER){
                    // Guarda la puntuacion y regresa al lobby
                    actualScore = new Score(playerName, applesEaten);
                    scoreList.add(actualScore);
                    playerName = "";
                    sortAndSave(); // Ordena y guarda puntuaciones
                    showJTextField = false;
                    parentFrame.switchToLobbyPanel();
                }else if(k.getKeyCode() == KeyEvent.VK_BACK_SPACE && playerName.length() > 0){
                    // Elimina ultimo caracter
                    StringBuilder sb = new StringBuilder(playerName);
                    sb.deleteCharAt(sb.length() - 1);
                    playerName = sb.toString();
                }
                else{
                    if(!k.isActionKey() && k.getKeyCode() != KeyEvent.VK_SHIFT && k.getKeyCode() != KeyEvent.VK_BACK_SPACE){
                        playerName = playerName + k.getKeyChar();
                    }
                }

                repaint();
            }
        }
    }

    // Método para ordenar y guardar las puntuaciones en archivo
    public void sortAndSave(){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("scores.data")));
            scoreList.sort(Comparator.reverseOrder()); // Ordena de mayor a menor

            // Guarda solo las top 10 puntuaciones
            for(int i = 0; i < 10; i++){
                Score element = scoreList.get(i);
                bw.write(element.name + "," + String.valueOf(element.score) + "\n");
            }
            bw.flush(); // Asegura que los datos se escriban
        }catch(IOException ex){
            System.out.println("Error writing file");
        }
    }


    public void sleep(int millis){
        try{
            Thread.sleep(millis);
            System.out.println("Slept");
        }catch(Exception ex){
            System.out.println("Fatal Error in sleep() method");
        }
    }

}



