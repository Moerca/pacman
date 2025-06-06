import java.awt.*;

/**
 * Represents a Ghost enemy.
 */
public class Ghost extends Block {
    public Image scaredImage, normalImage;
    public char initialDirection;

    public Ghost(Image normal, Image scared, int x, int y, int size, char startDir) {
        super(normal, x, y, size, size);
        this.normalImage = normal;
        this.scaredImage = scared;
        this.initialDirection = startDir;
        this.direction = startDir;
        updateVelocity(size);
    }

    public void setScared(boolean scared) {
        if (scared) {
            image = scaredImage;
        } else {
            image = normalImage;
        }
    }

    @Override
    public void reset() {
        super.reset();
        image = normalImage;
        direction = initialDirection;
        updateVelocity(width); // width == height == tile size
        isEaten = false;
    }
}