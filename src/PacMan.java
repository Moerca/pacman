import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;
import java.nio.file.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {

    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U = Up, D = Down, L = Left, R = Right
        int velocityX = 0;
        int velocityY = 0;

        boolean isEaten = false; // For ghosts

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y; 
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        /**
         * Only change direction if the move is possible (not blocked by wall).
         * Returns true if direction actually changed, false otherwise.
         */
        boolean updateDirection(char newDirection) {
            char prevDirection = this.direction;
            int prevX = this.x;
            int prevY = this.y;
            this.direction = newDirection;
            updateVelocity();

            // Try the move
            this.x += this.velocityX;
            this.y += this.velocityY;
            boolean blocked = false;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    blocked = true;
                    break;
                }
            }
            // If blocked, revert
            if (blocked) {
                this.x = prevX;
                this.y = prevY;
                this.direction = prevDirection;
                updateVelocity();
                return false;
            }
            // If not blocked, revert position (actual move will happen on next tick)
            this.x = prevX;
            this.y = prevY;
            return true;
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize / 4;
            } else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize / 4;
            } else if (this.direction == 'L') {
                this.velocityX = -tileSize / 4;
                this.velocityY = 0;
            } else if (this.direction == 'R') {
                this.velocityX = tileSize / 4;
                this.velocityY = 0;
            }
        }

        void reset(){
            this.x = this.startX;
            this.y = this.startY;
            this.isEaten = false;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    private Image cherryImage;
    private Image scaredGhostImage;

    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "A       bpo       A",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;
    private boolean paused = false;

    // Cherry & Powerup logic
    Block cherry = null;
    boolean cherryActive = false;
    Timer cherrySpawnTimer;
    Timer cherryRemoveTimer;
    Timer scaredTimer;
    boolean scaredMode = false;
    int scaredDuration = 8000; // ms
    int cherryAppearInterval = 20000; // ms
    int cherryStayDuration = 7000; // ms

    // Direction buffer for smooth controls
    char requestedDirection;

    // Highscore
    int highscore = 0;

    // Winning Screan
    boolean won = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        // Load images
        wallImage = new ImageIcon(getClass().getResource("./img/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./img/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./img/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./img/pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./img/redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./img/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./img/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./img/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./img/pacmanRight.png")).getImage();

        cherryImage = new ImageIcon(getClass().getResource("./img/cherry.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("./img/scaredGhost.png")).getImage();

        // Load highscore from file if available
        try {
            Path path = Paths.get("highscore.txt");
            if (Files.exists(path)) {
                String hs = Files.readAllLines(path).get(0);
                highscore = Integer.parseInt(hs.trim());
            }
        } catch (Exception e) {
            highscore = 0;
        }

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        gameLoop = new Timer(50, this); // 20 FPS
        gameLoop.start();

        // Start cherry spawning timer
        cherrySpawnTimer = new Timer(cherryAppearInterval, e -> spawnCherry());
        cherrySpawnTimer.setRepeats(true);
        cherrySpawnTimer.start();

        // Set initial requested direction
        requestedDirection = pacman.direction;
    }

    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char tileChar = tileMap[r].charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                switch (tileChar) {
                    case 'X':
                        walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                        break;
                    case 'b':
                        ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'o':
                        ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'p':
                        ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'r':
                        ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'P':
                        pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                        break;
                    case ' ':
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        for (Block ghost : ghosts) {
            if (!ghost.isEaten) {
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }
        }

        for (Block food : foods) {
            g.setColor(Color.white);
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        if (cherryActive && cherry != null) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }

        if (pacman != null) {
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        }

        // Always display score and highscore at the top
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.drawString("x" + lives + " Score: " + score, tileSize/2, tileSize/2);
        g.drawString("Highscore: " + highscore, boardWidth - 180, tileSize/2);

        if (paused && !gameOver) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, boardWidth, boardHeight);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Paused", boardWidth/2 - 70, boardHeight/2);
        }

        if (gameOver) {
            // Draw semi-transparent overlay
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, boardWidth, boardHeight);

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 54));
            g.drawString("GAME OVER", boardWidth/2 - 180, boardHeight/2 - 40);

            g.setFont(new Font("Arial", Font.PLAIN, 32));
            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, boardWidth/2 - 80, boardHeight/2 + 10);
            g.drawString("Highscore: " + highscore, boardWidth/2 - 100, boardHeight/2 + 50);

            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.setColor(Color.YELLOW);
            g.drawString("Press any key to restart", boardWidth/2 - 150, boardHeight/2 + 100);
        }

      if (won) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, boardWidth, boardHeight);

            g.setColor(Color.GREEN);    
            g.setFont(new Font("Arial", Font.BOLD, 54));
            g.drawString("YOU WIN!", boardWidth/2 - 140, boardHeight/2 - 40);

            g.setFont(new Font("Arial", Font.PLAIN, 32));
            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, boardWidth/2 - 80, boardHeight/2 + 10);

            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.setColor(Color.YELLOW);
            g.drawString("Press any key for next level", boardWidth/2 - 170, boardHeight/2 + 60);
        }  
    }

    public void spawnCherry() {
        if (cherryActive || foods.isEmpty()) return;
        Block[] foodArr = foods.toArray(new Block[0]);
        Block randomFood = foodArr[random.nextInt(foodArr.length)];
        cherry = new Block(cherryImage, randomFood.x - 10, randomFood.y - 10, 24, 24);
        cherryActive = true;

        // Remove Cherry after cherryStayDuration
        if (cherryRemoveTimer != null) cherryRemoveTimer.stop();
        cherryRemoveTimer = new Timer(cherryStayDuration, e -> {
            cherry = null;
            cherryActive = false;
        });
        cherryRemoveTimer.setRepeats(false);
        cherryRemoveTimer.start();
    }

    public void activateScaredMode() {
        scaredMode = true;
        for (Block ghost : ghosts) {
            ghost.image = scaredGhostImage;
            ghost.isEaten = false;
        }
        if (scaredTimer != null) scaredTimer.stop();
        scaredTimer = new Timer(scaredDuration, e -> {
            scaredMode = false;
            // Restore normal ghost images
            int idx = 0;
            for (Block ghost : ghosts) {
                if (idx == 0) ghost.image = blueGhostImage;
                else if (idx == 1) ghost.image = orangeGhostImage;
                else if (idx == 2) ghost.image = pinkGhostImage;
                else ghost.image = redGhostImage;
                idx++;
            }
        });
        scaredTimer.setRepeats(false);
        scaredTimer.start();
    }

    public void move() {
        // Buffered direction logic for Pac-Man: try to update to requestedDirection if possible
        if (pacman != null && requestedDirection != pacman.direction) {
            boolean changed = pacman.updateDirection(requestedDirection);
            if (changed) {
                // Change Pac-Man sprite
                if (requestedDirection == 'U') pacman.image = pacmanUpImage;
                else if (requestedDirection == 'D') pacman.image = pacmanDownImage;
                else if (requestedDirection == 'L') pacman.image = pacmanLeftImage;
                else if (requestedDirection == 'R') pacman.image = pacmanRightImage;
                // Stop requesting, since we succeeded
                requestedDirection = pacman.direction;
            }
        }

        if (pacman != null) {
            pacman.x += pacman.velocityX;
            pacman.y += pacman.velocityY;

            // Portal Logic for Pacman (tunnel row)
            if (pacman.y == 9 * tileSize) {
                if (pacman.x < 0) {
                    pacman.x = boardWidth - pacman.width;
                } else if (pacman.x + pacman.width > boardWidth ) {
                    pacman.x = 0;
                }
            }
        }

        // Ghost movement and portal logic
        int ghostIdx = 0;
        for (Block ghost : ghosts ) {
            if (ghost.isEaten) {
                ghostIdx++;
                continue; // don't move or draw eaten ghosts
            }
            // Ghost movement
            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            // Portal Logic for Ghosts
            if (ghost.y == 9 * tileSize) {
                if (ghost.x < 0) {
                    ghost.x = boardWidth - ghost.width;
                } else if (ghost.x + ghost.width > boardWidth) {
                    ghost.x = 0;
                }
            }

            for (Block wall : walls ) {
                if (collision(ghost, wall) || ghost.x < 0 || ghost.x + ghost.width > boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }

            // Ghost collision with Pacman
            if (!scaredMode && collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    // Highscore logic
                    if (score > highscore) {
                        highscore = score;
                        // Save highscore
                        try {
                            Files.write(Paths.get("highscore.txt"),
                                String.valueOf(highscore).getBytes());
                        } catch (Exception e) {}
                    }
                    return;
                }
                resetPositions();
                return;
            }
            if (scaredMode && collision(ghost, pacman) && !ghost.isEaten) {
                score += 200;
                ghost.isEaten = true;
                // Move offscreen
                ghost.x = -1000; ghost.y = -1000;
                // Respawn after 4 seconds at start position
                Timer respawn = new Timer(4000, e -> {
                    ghost.reset();
                    ghost.isEaten = false;
                });
                respawn.setRepeats(false);
                respawn.start();
            }
            ghostIdx++;
        }

        // Check wall collision for Pacman
        for (Block wall : walls) {
            if (collision(pacman, wall)){
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // check food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if(collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            won = true;
            loadMap();
            resetPositions();
        }

        // Cherry collision (Pacman eats cherry)
        if (cherryActive && cherry != null && collision(pacman, cherry)) {
            cherry = null;
            cherryActive = false;
            activateScaredMode();
        }

        
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
        a.x + a.width > b.x &&
        a.y < b.y + b.height && 
        a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        int idx = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            ghost.isEaten = false;
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
            idx++;
        }
        // Reset direction buffer
        requestedDirection = pacman.direction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver) {
            move();
        }
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            paused = false;
            gameLoop.start();
            return;
        }

        if (won) {
            won = false;
            // Switch to a new map!
            //selectNextMap(); // function to set tileMap and call loadMap()
            resetPositions();
            paused = false;
            gameLoop.start();
            return;
}

        // Toggle pause with 'P' or space bar
        if (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_SPACE) {
            paused = !paused;
            if (paused) {
                gameLoop.stop();
            } else {
                gameLoop.start();
            }
            repaint();
            return;
        }

        // Don't allow movement if paused
        if (paused) return;

        // Only set the requested direction here!
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            requestedDirection = 'U';
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            requestedDirection = 'D';
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            requestedDirection = 'L';
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            requestedDirection = 'R';
        }
        // Pac-Man's image will be updated automatically when the direction change is possible in move()
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pac-Man");
        PacMan pacmanGame = new PacMan();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(pacmanGame);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}