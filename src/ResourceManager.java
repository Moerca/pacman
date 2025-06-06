import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Loads and caches images for the game.
 */
public class ResourceManager {
    private static final String IMG_PATH = "src/img/";
    private static final HashMap<String, Image> imageCache = new HashMap<>();

    public static Image getImage(String filename) {
        if (imageCache.containsKey(filename)) return imageCache.get(filename);
        Image img = new ImageIcon(IMG_PATH + filename).getImage();
        if (img == null || img.getWidth(null) <= 0) {
            System.out.println("Could not load image: " + IMG_PATH + filename);
        }
        imageCache.put(filename, img);
        return img;
    }

    public static Image getWallImage()         { return getImage("wall.png"); }
    public static Image getBlueGhostImage()    { return getImage("blueGhost.png"); }
    public static Image getOrangeGhostImage()  { return getImage("orangeGhost.png"); }
    public static Image getPinkGhostImage()    { return getImage("pinkGhost.png"); }
    public static Image getRedGhostImage()     { return getImage("redGhost.png"); }
    public static Image getPacmanUpImage()     { return getImage("pacmanUp.png"); }
    public static Image getPacmanDownImage()   { return getImage("pacmanDown.png"); }
    public static Image getPacmanLeftImage()   { return getImage("pacmanLeft.png"); }
    public static Image getPacmanRightImage()  { return getImage("pacmanRight.png"); }
    public static Image getCherryImage()       { return getImage("cherry.png"); }
    public static Image getScaredGhostImage()  { return getImage("scaredGhost.png"); }
    public static Image getHeartImage()        { return getImage("heart.png"); }
}