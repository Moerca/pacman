import java.awt.*;

/**
 * Represents any rectangular object on the game board.
 * Used as base for walls, food, PacMan, ghosts, etc.
 */
public class Block {
    public int x, y, width, height;
    public Image image;
    public boolean isEaten = false;
    public char direction = 'R'; // Only used if moving entity
    public int velocityX = 0, velocityY = 0;
    public int startX, startY;

    public Block(Image img, int x, int y, int w, int h) {
        this.image = img;
        this.x = this.startX = x;
        this.y = this.startY = y;
        this.width = w;
        this.height = h;
    }

    public void draw(Graphics g) {
        if (image != null && image.getWidth(null) > 0) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            // Fallback: draw colored rectangles for debugging
            String className = getClass().getSimpleName();
            if (className.equals("Player")) g.setColor(Color.YELLOW);
            else if (className.equals("Ghost")) g.setColor(Color.CYAN);
            else g.setColor(Color.WHITE);
            g.fillRect(x, y, width, height);
        }
    }

    public void updateVelocity(int tileSize) {
        velocityX = velocityY = 0;
        switch (direction) {
            case 'U': velocityY = -tileSize / 8; break;
            case 'D': velocityY = tileSize / 8; break;
            case 'L': velocityX = -tileSize / 8; break;
            case 'R': velocityX = tileSize / 8; break;
        }
    }

    public void move() {
        x += velocityX;
        y += velocityY;
    }

    public void reset() {
        x = startX;
        y = startY;
        velocityX = velocityY = 0;
        direction = 'R';
        isEaten = false;
    }

    public static boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x &&
               a.y < b.y + b.height && a.y + a.height > b.y;
    }
}