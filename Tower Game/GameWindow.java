import javax.swing.JFrame;

public class GameWindow extends JFrame {

    private MenuPanel menuPanel;
    private GamePanel gamePanel;

    public GameWindow() {
        setTitle("Lane Clash Tower Defense");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 520);
        setResizable(false);

        showMenu();

        setLocationRelativeTo(null); // center on screen
        setVisible(true);
    }

    public void showMenu() {
        if (gamePanel != null) {
            gamePanel.stopGame();
        }
        getContentPane().removeAll();

        menuPanel = new MenuPanel(this);
        add(menuPanel);

        revalidate();
        repaint();
        menuPanel.requestFocusInWindow();
    }

    public void startGame(GameMode mode) {
        getContentPane().removeAll();

        gamePanel = new GamePanel(mode);
        add(gamePanel);

        revalidate();
        repaint();
        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        new GameWindow();
    }
}
