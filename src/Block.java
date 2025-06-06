import java.awt.Image;


// Set Objects (Wall, Ghost, PacMan, ...)

public class Block {
    public int x, y, width, height;
    public Image image;

    public int startX, startY;
    public char direction = 'U'; // U = Up, D = Down, L = Left, R = Right
    public int velocityX = 0, velocityY = 0;
    public boolean isEaten = false; // eaten Ghosts

    public Block(Image image, int x, int y, int width, int height) {
        this.image = image;
        this.x = x;
        this.y = y; 
        this.width = width;
        this.height = height;
        this.startX = x;
        this.startY = y;
    }

    
    public void updateVelocity(int tileSize) {
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

    
    // Reset to Start Position
    public void reset() {
        this.x = this.startX;
        this.y = this.startY;
        this.isEaten = false;
    }

    
    // Rectangular collision detection
    public static boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height && 
               a.y + a.height > b.y;
    }
}