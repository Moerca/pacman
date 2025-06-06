import java.awt.*;


// Represents Pac-Man

public class Player extends Block {
    public Image upImage, downImage, leftImage, rightImage;

    public Player(Image right, Image left, Image up, Image down, int x, int y, int size) {
        super(right, x, y, size, size);
        this.rightImage = right;
        this.leftImage = left;
        this.upImage = up;
        this.downImage = down;
        this.direction = 'S';
        updateVelocity(size);
    }

    public void setDirection(char dir, int tileSize) {
        this.direction = dir;
        switch (dir) {
            case 'U': image = upImage; break;
            case 'D': image = downImage; break;
            case 'L': image = leftImage; break;
            case 'R': image = rightImage; break;
        }
        updateVelocity(tileSize);
    }

    @Override
    public void reset() {
        super.reset();
        image = rightImage;
        direction = 'R';
    }
}