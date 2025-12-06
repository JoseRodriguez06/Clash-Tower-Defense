import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MenuPanel extends JPanel {

    private GameWindow window;

    public MenuPanel(GameWindow window) {
        this.window = window;
        setBackground(Color.BLACK);

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_1) {
                    // 1 Player Survival
                    window.startGame(GameMode.SURVIVAL);
                } else if (code == KeyEvent.VK_2) {
                    // PvP mode
                    window.startGame(GameMode.PVP);
                } else if (code == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        // Background
        g2.setColor(new Color(20, 20, 20));
        g2.fillRect(0, 0, width, height);

        // Title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String title = "Lane Clash Tower Defense";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (width - titleWidth) / 2, height / 4);

        // Mode options
        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        String mode1 = "1 - Survival Mode (1 Player vs Waves)";
        String mode2 = "2 - PvP Mode (Player vs Player)";
        int m1Width = g2.getFontMetrics().stringWidth(mode1);
        int m2Width = g2.getFontMetrics().stringWidth(mode2);
        g2.drawString(mode1, (width - m1Width) / 2, height / 2);
        g2.drawString(mode2, (width - m2Width) / 2, height / 2 + 30);

        // Short hint about help screen
        String helpMsg = "Press H in game to view controls and troop info";
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        int helpWidth = g2.getFontMetrics().stringWidth(helpMsg);
        g2.drawString(helpMsg, (width - helpWidth) / 2, height / 2 + 70);

        // Footer
        String exitMsg = "Press ESC to quit";
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        int exitWidth = g2.getFontMetrics().stringWidth(exitMsg);
        g2.drawString(exitMsg, (width - exitWidth) / 2, height - 40);
    }
}
