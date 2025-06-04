import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {
    
        // Setting game window
        int rowCount = 21;
        int columnCount = 19;
        int tileSize =32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;

        JFrame frame = new JFrame("Pack Man");
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PacMan packmanGame = new PacMan();
        frame.add(packmanGame);
        frame.pack();
        packmanGame.requestFocus();
        frame.setVisible(true);
    }
}