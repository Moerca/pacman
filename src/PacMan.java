import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;


// Main PacMan game panel. Handles UI, input, game loop, and all timers.
public class PacMan extends JPanel implements ActionListener, KeyListener {
    private static final int TILE_SIZE = 32;
    private static final int ROWS = 21;
    private static final int COLS = 19;
    private static final int BOARD_WIDTH = COLS * TILE_SIZE;
    private static final int BOARD_HEIGHT = ROWS * TILE_SIZE;

    // TODO add new Maps
    private static final String[] TILE_MAP = {
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

    // Game objects
    private Set<Block> walls = new HashSet<>();
    private Set<Block> foods = new HashSet<>();
    private java.util.List<Ghost> ghosts = new ArrayList<>();
    private Player pacman;
    private Block cherry = null;
    private boolean cherryActive = false;

    // Score, state, assets
    private int score = 0, highscore = 0, lives = 3;
    private boolean won = false, gameOver = false, paused = false;
    private Image heartImage = ResourceManager.getHeartImage();
    private Random random = new Random();
    private char requestedDirection = 'R';

    // Timers
    private Timer gameLoop;
    private Timer cherrySpawnTimer;
    private Timer cherryRemoveTimer;
    private Timer scaredTimer;

    // Modes and durations
    private boolean scaredMode = false;
    private final char[] directions = {'U', 'D', 'L', 'R'};
    private final int scaredDuration = 8000;
    private final int cherryAppearInterval = 20000;
    private final int cherryStayDuration = 7000;

    public PacMan() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // Load Highscore from file
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
        gameLoop = new Timer(25, this);
        gameLoop.start();

        cherrySpawnTimer = new Timer(cherryAppearInterval, e -> spawnCherry());
        cherrySpawnTimer.setRepeats(true);
        cherrySpawnTimer.start();
    }

    private void loadMap() {
        walls.clear(); foods.clear(); ghosts.clear();
        pacman = null;

        for (int r = 0; r < TILE_MAP.length; r++) {
            for (int c = 0; c < TILE_MAP[0].length(); c++) {
                char ch = TILE_MAP[r].charAt(c);
                int x = c * TILE_SIZE;
                int y = r * TILE_SIZE;
                switch (ch) {
                    case 'X':
                        walls.add(new Block(ResourceManager.getWallImage(), x, y, TILE_SIZE, TILE_SIZE));
                        break;
                    case 'b':
                        ghosts.add(new Ghost(ResourceManager.getBlueGhostImage(), ResourceManager.getScaredGhostImage(), x, y, TILE_SIZE, 'U'));
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                    case 'o':
                        ghosts.add(new Ghost(ResourceManager.getOrangeGhostImage(), ResourceManager.getScaredGhostImage(), x, y, TILE_SIZE, 'U'));
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                    case 'p':
                        ghosts.add(new Ghost(ResourceManager.getPinkGhostImage(), ResourceManager.getScaredGhostImage(), x, y, TILE_SIZE, 'U'));
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                    case 'r':
                        ghosts.add(new Ghost(ResourceManager.getRedGhostImage(), ResourceManager.getScaredGhostImage(), x, y, TILE_SIZE, 'U'));
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                    case 'A':
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                    case 'P':
                        pacman = new Player(ResourceManager.getPacmanRightImage(),
                                           ResourceManager.getPacmanLeftImage(),
                                           ResourceManager.getPacmanUpImage(),
                                           ResourceManager.getPacmanDownImage(),
                                           x, y, TILE_SIZE);
                        requestedDirection = 'U';
                        break;
                    case ' ':
                        foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                    default:
                        break;
                }
            }
        }
        for (Ghost ghost : ghosts) {
            char newDir = directions[random.nextInt(4)];
            ghost.direction = newDir;
            ghost.updateVelocity(TILE_SIZE);
        }
        cherry = null;
        cherryActive = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Block wall : walls) wall.draw(g);
        for (Block food : foods) {
            g.setColor(Color.white);
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        for (Ghost ghost : ghosts)
            if (!ghost.isEaten) ghost.draw(g);
        if (cherryActive && cherry != null)
            cherry.draw(g);
        if (pacman != null) pacman.draw(g);
        drawHUD(g);
        if (paused && !gameOver) drawPause(g);
        if (gameOver) drawGameOver(g);
        if (won) drawWin(g);
    }

    private void drawHUD(Graphics g) {
        int heartSize = 20;
        for (int i = 0; i < lives; i++)
            g.drawImage(heartImage, 10 + i * (heartSize + 5), 10, heartSize, heartSize, null);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10 + 3 * (heartSize + 10), 25);
        g.drawString("Highscore: " + highscore, BOARD_WIDTH - 180, 25);
    }

    private void drawPause(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        drawCenteredString(g, "Paused", BOARD_HEIGHT / 2, g.getFont());
    }

    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setColor(Color.RED);
        drawCenteredString(g, "GAME OVER", BOARD_HEIGHT / 2 - 40, new Font("Arial", Font.BOLD, 54));
        g.setColor(Color.WHITE);
        drawCenteredString(g, "Score: " + score, BOARD_HEIGHT / 2 + 10, new Font("Arial", Font.PLAIN, 32));
        drawCenteredString(g, "Highscore: " + highscore, BOARD_HEIGHT / 2 + 50, new Font("Arial", Font.PLAIN, 32));
        g.setColor(Color.YELLOW);
        drawCenteredString(g, "Press any key to restart", BOARD_HEIGHT / 2 + 100, new Font("Arial", Font.BOLD, 24));
    }

    private void drawWin(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setColor(Color.GREEN);
        drawCenteredString(g, "YOU WIN!", BOARD_HEIGHT / 2 - 40, new Font("Arial", Font.BOLD, 54));
        g.setColor(Color.WHITE);
        drawCenteredString(g, "Score: " + score, BOARD_HEIGHT / 2 + 10, new Font("Arial", Font.PLAIN, 32));
        g.setColor(Color.YELLOW);
        drawCenteredString(g, "Press any key for next level", BOARD_HEIGHT / 2 + 60, new Font("Arial", Font.BOLD, 24));
    }

    private void drawCenteredString(Graphics g, String text, int y, Font font) {
        FontMetrics fm = g.getFontMetrics(font);
        int x = (BOARD_WIDTH - fm.stringWidth(text)) / 2;
        g.setFont(font);
        g.drawString(text, x, y);
    }

    private void spawnCherry() {
        if (cherryActive || foods.isEmpty()) return;
        Block[] foodArr = foods.toArray(new Block[0]);
        Block randomFood = foodArr[random.nextInt(foodArr.length)];
        cherry = new Block(ResourceManager.getCherryImage(), randomFood.x - 10, randomFood.y - 10, 24, 24);
        cherryActive = true;
        if (cherryRemoveTimer != null) cherryRemoveTimer.stop();
        cherryRemoveTimer = new Timer(cherryStayDuration, e -> {
            cherry = null;
            cherryActive = false;
        });
        cherryRemoveTimer.setRepeats(false);
        cherryRemoveTimer.start();
    }

    private void activateScaredMode() {
        scaredMode = true;
        for (Ghost ghost : ghosts) ghost.setScared(true);
        if (scaredTimer != null) scaredTimer.stop();
        scaredTimer = new Timer(scaredDuration, e -> {
            scaredMode = false;
            for (Ghost ghost : ghosts) ghost.setScared(false);
        });
        scaredTimer.setRepeats(false);
        scaredTimer.start();
    }

    private boolean updateDirectionIfPossible(Block block, char newDirection) {
        char prevDirection = block.direction;
        int prevX = block.x;
        int prevY = block.y;
        block.direction = newDirection;
        block.updateVelocity(TILE_SIZE);
        block.x += block.velocityX;
        block.y += block.velocityY;
        boolean blocked = false;
        for (Block wall : walls) {
            if (Block.collision(block, wall)) {
                blocked = true;
                break;
            }
        }
        block.x = prevX;
        block.y = prevY;
        if (blocked) {
            block.direction = prevDirection;
            block.updateVelocity(TILE_SIZE);
            return false;
        }
        return true;
    }

    private void moveGameObjects() {
        // Pac-Man direction buffering
        if (pacman != null && requestedDirection != pacman.direction) {
            boolean changed = updateDirectionIfPossible(pacman, requestedDirection);
            if (changed) {
                pacman.setDirection(requestedDirection, TILE_SIZE);
                requestedDirection = pacman.direction;
            }
        }
        if (pacman != null) {
            pacman.move();
            // Portal logic for Pac-Man
            if (pacman.y == 9 * TILE_SIZE) {
                if (pacman.x < 0) pacman.x = BOARD_WIDTH - pacman.width;
                else if (pacman.x + pacman.width > BOARD_WIDTH) pacman.x = 0;
            }
        }

        // Ghost movement and portal logic
        for (Ghost ghost : ghosts) {
            if (ghost.isEaten) continue;
            if (ghost.y == TILE_SIZE * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                updateDirectionIfPossible(ghost, 'U');
            }
            ghost.move();
            if (ghost.y == 9 * TILE_SIZE) {
                if (ghost.x < 0) ghost.x = BOARD_WIDTH - ghost.width;
                else if (ghost.x + ghost.width > BOARD_WIDTH) ghost.x = 0;
            }
            for (Block wall : walls) {
                if (Block.collision(ghost, wall) || ghost.x < 0 || ghost.x + ghost.width > BOARD_WIDTH) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    updateDirectionIfPossible(ghost, newDirection);
                }
            }
            // Ghost collision with Pac-Man
            if (!scaredMode && Block.collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    if (score > highscore) {
                        highscore = score;
                        try {
                            Files.write(Paths.get("highscore.txt"), String.valueOf(highscore).getBytes());
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
                    char newDirection = directions[random.nextInt(4)];
                    updateDirectionIfPossible(ghost, newDirection);
                });
                respawn.setRepeats(false);
                respawn.start();
            }
        }
        // Pac-Man wall collision handling
        for (Block wall : walls) {
            if (Block.collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }
        // Food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (Block.collision(pacman, food)) {
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
        // Cherry pickup
        if (cherryActive && cherry != null && Block.collision(pacman, cherry)) {
            cherry = null;
            cherryActive = false;
            activateScaredMode();
        }
    }

    private void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Ghost ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            updateDirectionIfPossible(ghost, newDirection);
        }
        requestedDirection = pacman.direction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver) moveGameObjects();
        repaint();
        if (gameOver) gameLoop.stop();
    }

    @Override
    public void keyPressed(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("PacMan");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            PacMan pacmanPanel = new PacMan();
            frame.setContentPane(pacmanPanel);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            pacmanPanel.requestFocusInWindow();
        });
    }
}