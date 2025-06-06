import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;
import java.nio.file.*;


// Main 
public class PacMan extends JPanel implements ActionListener, KeyListener {
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
    int scaredDuration = 8000;
    int cherryAppearInterval = 20000;
    int cherryStayDuration = 7000;

    // Direction buffer
    char requestedDirection;

    int highscore = 0;
    boolean won = false;

    public PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        // Load Images
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

        // set Highscore 
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
            updateDirectionIfPossible(ghost, newDirection); // <- Wichtig: Richtungslogik verwenden!
        }
        gameLoop = new Timer(50, this);
        gameLoop.start();

        cherrySpawnTimer = new Timer(cherryAppearInterval, e -> spawnCherry());
        cherrySpawnTimer.setRepeats(true);
        cherrySpawnTimer.start();

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
        for (Block wall : walls)
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        for (Block ghost : ghosts)
            if (!ghost.isEaten)
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        for (Block food : foods) {
            g.setColor(Color.white);
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        if (cherryActive && cherry != null)
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        if (pacman != null)
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
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
        // Buffered direction logic für Pac-Man
        if (pacman != null && requestedDirection != pacman.direction) {
            boolean changed = updateDirectionIfPossible(pacman, requestedDirection);
            if (changed) {
                if (requestedDirection == 'U') pacman.image = pacmanUpImage;
                else if (requestedDirection == 'D') pacman.image = pacmanDownImage;
                else if (requestedDirection == 'L') pacman.image = pacmanLeftImage;
                else if (requestedDirection == 'R') pacman.image = pacmanRightImage;
                requestedDirection = pacman.direction;
            }
        }
        if (pacman != null) {
            pacman.x += pacman.velocityX;
            pacman.y += pacman.velocityY;
            if (pacman.y == 9 * tileSize) {
                if (pacman.x < 0) {
                    pacman.x = boardWidth - pacman.width;
                } else if (pacman.x + pacman.width > boardWidth ) {
                    pacman.x = 0;
                }
            }
        }
        // Ghost movement und Portal-Logik
        for (Block ghost : ghosts ) {
            if (ghost.isEaten) continue;
            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                updateDirectionIfPossible(ghost, 'U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            if (ghost.y == 9 * tileSize) {
                if (ghost.x < 0) {
                    ghost.x = boardWidth - ghost.width;
                } else if (ghost.x + ghost.width > boardWidth) {
                    ghost.x = 0;
                }
            }
            for (Block wall : walls ) {
                if (Block.collision(ghost, wall) || ghost.x < 0 || ghost.x + ghost.width > boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    updateDirectionIfPossible(ghost, newDirection);
                }
            }
            // Ghost collision mit Pacman
            if (!scaredMode && Block.collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    if (score > highscore) {
                        highscore = score;
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
            if (scaredMode && Block.collision(ghost, pacman) && !ghost.isEaten) {
                score += 200;
                ghost.isEaten = true;
                ghost.x = -1000; ghost.y = -1000;
                Timer respawn = new Timer(4000, e -> {
                    ghost.reset();
                    ghost.isEaten = false;
                    char newDirection = directions[random.nextInt(4)];
                    updateDirectionIfPossible(ghost, newDirection);
                });
                respawn.setRepeats(false);
                respawn.start();
            }
        }
        // Pacman-Wall-Collision
        for (Block wall : walls) {
            if (Block.collision(pacman, wall)){
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }
        // Food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if(Block.collision(pacman, food)) {
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

        if (cherryActive && cherry != null && Block.collision(pacman, cherry)) {
            cherry = null;
            cherryActive = false;
            activateScaredMode();
        }
    }

    // to not get stuck
    private boolean updateDirectionIfPossible(Block block, char newDirection) {
        char prevDirection = block.direction;
        int prevX = block.x;
        int prevY = block.y;
        block.direction = newDirection;
        block.updateVelocity(tileSize);

        block.x += block.velocityX;
        block.y += block.velocityY;
        boolean blocked = false;
        for (Block wall : walls) {
            if (Block.collision(block, wall)) {
                blocked = true;
                break;
            }
        }
        if (blocked) {
            block.x = prevX;
            block.y = prevY;
            block.direction = prevDirection;
            block.updateVelocity(tileSize);
            return false;
        }
        block.x = prevX;
        block.y = prevY;
        return true;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            ghost.isEaten = false;
            char newDirection = directions[random.nextInt(4)];
            updateDirectionIfPossible(ghost, newDirection);
        }
        requestedDirection = pacman.direction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver) move();
        repaint();
        if (gameOver) gameLoop.stop();
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
            resetPositions();
            paused = false;
            gameLoop.start();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_SPACE) {
            paused = !paused;
            if (paused) gameLoop.stop();
            else gameLoop.start();
            repaint();
            return;
        }
        if (paused) return;
        if (e.getKeyCode() == KeyEvent.VK_UP) requestedDirection = 'U';
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) requestedDirection = 'D';
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) requestedDirection = 'L';
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) requestedDirection = 'R';
    }
}