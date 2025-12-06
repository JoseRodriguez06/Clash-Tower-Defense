    import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {

    private List<Troop> troops = new ArrayList<>();
    private Timer timer;

    private GameMode mode;

    // Lane layout
    private int laneHeight = 120;
    private int lane1Y = 50;
    private int lane2Y = 200;
    private int lane3Y = 350;

    // Bases: 3 per side, each lane has its own HP
    private int[] playerBaseHP = {300, 300, 300};
    private int[] enemyBaseHP  = {300, 300, 300};

    private int baseWidth = 40;
    private int baseHeight = 60;
    private int hpBarWidth = 100; // base HP bar width

    // Economy: separate coins for P1 and P2 (PvP)
    private int coinsP1 = 200;
    private int coinsP2 = 200;
    private int incomeTimer = 0; // frames until next passive income

    // Enemy waves (Survival mode only)
    private Random rng = new Random();
    private int enemySpawnTimer = 0;      // counts frames
    private int enemySpawnInterval = 180; // frames between waves (~3s at 60FPS)
    private int waveNumber = 1;

    // Game over state
    private boolean gameOver = false;
    private String gameResult = ""; // win/lose/draw message

    // Match timer (PvP only): 1 minute = 60s * 60 FPS
    private int timeLeftFrames = 60 * 60;

    // Help overlay toggle
    private boolean showHelp = false;

    public GamePanel(GameMode mode) {
        this.mode = mode;

        setBackground(Color.BLACK);

        // Enable keyboard input
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        spawnInitialTroops();

        // Game loop: ~60 FPS
        timer = new Timer(16, e -> {
            updateGame();
            repaint();
        });
        timer.start();
    }

    // ---------- INPUT: SPAWN TROOPS & HELP ----------

    private void handleKeyPress(KeyEvent e) {
        if (gameOver) {
            return; // ignore spawning input after game ends
        }

        int code = e.getKeyCode();

        // Toggle help overlay
        if (code == KeyEvent.VK_H) {
            showHelp = !showHelp;
            return;
        }

        // ---- Player 1 (left side) controls (both modes) ----
        // NORMAL: 1,2,3 → lanes 1,2,3
        if (code == KeyEvent.VK_1) {
            spawnPlayerTroop(Troop.Type.NORMAL, 1);
        } else if (code == KeyEvent.VK_2) {
            spawnPlayerTroop(Troop.Type.NORMAL, 2);
        } else if (code == KeyEvent.VK_3) {
            spawnPlayerTroop(Troop.Type.NORMAL, 3);
        }

        // TANK: Q,W,E → lanes 1,2,3
        else if (code == KeyEvent.VK_Q) {
            spawnPlayerTroop(Troop.Type.TANK, 1);
        } else if (code == KeyEvent.VK_W) {
            spawnPlayerTroop(Troop.Type.TANK, 2);
        } else if (code == KeyEvent.VK_E) {
            spawnPlayerTroop(Troop.Type.TANK, 3);
        }

        // FAST: A,S,D → lanes 1,2,3
        else if (code == KeyEvent.VK_A) {
            spawnPlayerTroop(Troop.Type.FAST, 1);
        } else if (code == KeyEvent.VK_S) {
            spawnPlayerTroop(Troop.Type.FAST, 2);
        } else if (code == KeyEvent.VK_D) {
            spawnPlayerTroop(Troop.Type.FAST, 3);
        }

        // SNIPER: Z,X,C → lanes 1,2,3
        else if (code == KeyEvent.VK_Z) {
            spawnPlayerTroop(Troop.Type.SNIPER, 1);
        } else if (code == KeyEvent.VK_X) {
            spawnPlayerTroop(Troop.Type.SNIPER, 2);
        } else if (code == KeyEvent.VK_C) {
            spawnPlayerTroop(Troop.Type.SNIPER, 3);
        }

        // ---- Player 2 (right side) controls (PvP ONLY) ----
        if (mode == GameMode.PVP) {
            // NORMAL: T,Y,U
            if (code == KeyEvent.VK_T) {
                spawnEnemyPlayerTroop(Troop.Type.NORMAL, 1);
            } else if (code == KeyEvent.VK_Y) {
                spawnEnemyPlayerTroop(Troop.Type.NORMAL, 2);
            } else if (code == KeyEvent.VK_U) {
                spawnEnemyPlayerTroop(Troop.Type.NORMAL, 3);
            }
            // TANK: G,H,J
            else if (code == KeyEvent.VK_G) {
                spawnEnemyPlayerTroop(Troop.Type.TANK, 1);
            } else if (code == KeyEvent.VK_H) {
                spawnEnemyPlayerTroop(Troop.Type.TANK, 2);
            } else if (code == KeyEvent.VK_J) {
                spawnEnemyPlayerTroop(Troop.Type.TANK, 3);
            }
            // FAST: B,N,M
            else if (code == KeyEvent.VK_B) {
                spawnEnemyPlayerTroop(Troop.Type.FAST, 1);
            } else if (code == KeyEvent.VK_N) {
                spawnEnemyPlayerTroop(Troop.Type.FAST, 2);
            } else if (code == KeyEvent.VK_M) {
                spawnEnemyPlayerTroop(Troop.Type.FAST, 3);
            }
            // SNIPER: I,O,P
            else if (code == KeyEvent.VK_I) {
                spawnEnemyPlayerTroop(Troop.Type.SNIPER, 1);
            } else if (code == KeyEvent.VK_O) {
                spawnEnemyPlayerTroop(Troop.Type.SNIPER, 2);
            } else if (code == KeyEvent.VK_P) {
                spawnEnemyPlayerTroop(Troop.Type.SNIPER, 3);
            }
        }
    }

    private void spawnPlayerTroop(Troop.Type type, int lane) {
        int cost = getCostForType(type);
        if (coinsP1 < cost) {
            return;
        }
        coinsP1 -= cost;
        int laneCenter = getLaneCenterY(lane);
        int startX = getPlayerBaseFrontX() + 10;
        troops.add(new Troop(type, false, lane, laneCenter, startX));
    }

    // PvP: Player 2 spawns enemy-side troops with its own coins
    private void spawnEnemyPlayerTroop(Troop.Type type, int lane) {
        if (mode != GameMode.PVP) return;

        int cost = getCostForType(type);
        if (coinsP2 < cost) {
            return;
        }
        coinsP2 -= cost;
        int laneCenter = getLaneCenterY(lane);
        int startX = getEnemyBaseFrontX() - 10; // near enemy base
        troops.add(new Troop(type, true, lane, laneCenter, startX));
    }

    private int getCostForType(Troop.Type type) {
        switch (type) {
            case NORMAL: return 25;
            case TANK:   return 50;
            case FAST:   return 40;
            case SNIPER: return 75;
            default:     return 30;
        }
    }

    private int getLaneCenterY(int lane) {
        switch (lane) {
            case 1: return lane1Y + laneHeight / 2;
            case 2: return lane2Y + laneHeight / 2;
            case 3: return lane3Y + laneHeight / 2;
            default: return lane1Y + laneHeight / 2;
        }
    }

    private void spawnInitialTroops() {
        // Start empty in both modes.
    }

    // ---------- ENEMY WAVE LOGIC (SURVIVAL ONLY) ----------

    private void spawnEnemyWave() {
        int width = getWidth();
        if (width <= 0) {
            width = 900;
        }

        int startX = width - 120; // enemy spawn X

        for (int lane = 1; lane <= 3; lane++) {
            if (rng.nextDouble() < 0.6) {
                Troop.Type type = getRandomEnemyType();
                int laneCenter = getLaneCenterY(lane);
                troops.add(new Troop(type, true, lane, laneCenter, startX));
            }
        }
    }

    private Troop.Type getRandomEnemyType() {
        double r = rng.nextDouble();

        if (waveNumber < 3) {
            if (r < 0.5) return Troop.Type.NORMAL;
            if (r < 0.8) return Troop.Type.FAST;
            return Troop.Type.TANK;
        } else if (waveNumber < 6) {
            if (r < 0.3) return Troop.Type.NORMAL;
            if (r < 0.6) return Troop.Type.FAST;
            if (r < 0.85) return Troop.Type.TANK;
            return Troop.Type.SNIPER;
        } else {
            if (r < 0.2) return Troop.Type.NORMAL;
            if (r < 0.45) return Troop.Type.FAST;
            if (r < 0.8) return Troop.Type.TANK;
            return Troop.Type.SNIPER;
        }
    }

    // ---------- GAME LOGIC UPDATE ----------

    private void updateGame() {
        if (gameOver) {
            return;
        }

        int width = getWidth();

        // ---- Match timer (PvP only) ----
        if (mode == GameMode.PVP && timeLeftFrames > 0) {
            timeLeftFrames--;
            if (timeLeftFrames == 0) {
                decideWinnerByHP();
                return;
            }
        }

        // Passive income: both players get coins
        incomeTimer++;
        if (incomeTimer >= 60) {
            coinsP1 += 5;
            if (mode == GameMode.PVP) {
                coinsP2 += 5;
            }
            incomeTimer = 0;
        }

        // Enemy waves in SURVIVAL mode only
        if (mode == GameMode.SURVIVAL) {
            enemySpawnTimer++;
            if (enemySpawnTimer >= enemySpawnInterval) {
                spawnEnemyWave();
                enemySpawnTimer = 0;
                waveNumber++;

                if (enemySpawnInterval > 60) {
                    enemySpawnInterval -= 10;
                }
            }
        }

        // 1) Tick cooldowns
        for (Troop t : troops) {
            t.tickCooldown();
        }

        // 2) Movement + attacking
        for (Troop t : troops) {
            if (t.isDead()) continue;

            boolean attackedThisFrame = false;

            Troop target = findTargetTroop(t);

            if (target != null) {
                double dist = Math.abs(target.getCenterX() - t.getCenterX());
                if (dist <= t.getRange()) {
                    if (t.isReadyToAttack()) {
                        target.takeDamage(t.getAttack());
                        t.resetCooldown();
                    }
                    attackedThisFrame = true;
                }
            } else {
                int laneIdx = t.getLane() - 1;

                if (!t.isEnemy()) {
                    // Player troop attacks enemy base only in PvP
                    if (mode == GameMode.PVP) {
                        int baseX = getEnemyBaseFrontX();
                        double dist = Math.abs(baseX - t.getFrontX());
                        if (dist <= t.getRange()) {
                            if (t.isReadyToAttack() && enemyBaseHP[laneIdx] > 0) {
                                enemyBaseHP[laneIdx] -= t.getAttack();
                                if (enemyBaseHP[laneIdx] < 0) enemyBaseHP[laneIdx] = 0;
                                t.resetCooldown();
                            }
                            attackedThisFrame = true;
                        }
                    }
                } else {
                    // Enemy troop (AI or Player 2) attacks player base in BOTH modes
                    int baseX = getPlayerBaseFrontX();
                    double dist = Math.abs(t.getFrontX() - baseX);
                    if (dist <= t.getRange()) {
                        if (t.isReadyToAttack() && playerBaseHP[laneIdx] > 0) {
                            playerBaseHP[laneIdx] -= t.getAttack();
                            if (playerBaseHP[laneIdx] < 0) playerBaseHP[laneIdx] = 0;
                            t.resetCooldown();
                        }
                        attackedThisFrame = true;
                    }
                }
            }

            if (!attackedThisFrame) {
                t.move();
            }
        }

        // 3) Remove dead or off-screen troops
        for (int i = troops.size() - 1; i >= 0; i--) {
            Troop t = troops.get(i);
            if (t.isDead() || t.getX() < -200 || t.getX() > width + 200) {
                troops.remove(i);
            }
        }

        // 4) Check win/lose conditions
        checkWinLose();
    }

    private void checkWinLose() {
        boolean allPlayerDead = true;
        boolean allEnemyDead = true;

        for (int hp : playerBaseHP) {
            if (hp > 0) {
                allPlayerDead = false;
                break;
            }
        }

        for (int hp : enemyBaseHP) {
            if (hp > 0) {
                allEnemyDead = false;
                break;
            }
        }

        if (mode == GameMode.SURVIVAL) {
            // Only you have bases (enemy bases are ignored)
            if (allPlayerDead) {
                gameOver = true;
                int wavesSurvived = waveNumber - 1;
                if (wavesSurvived < 0) wavesSurvived = 0;
                gameResult = "GAME OVER - Waves survived: " + wavesSurvived;
                if (timer != null) timer.stop();
            }
        } else {
            // PvP: both sides have bases
            if (allPlayerDead || allEnemyDead) {
                gameOver = true;

                if (allEnemyDead && !allPlayerDead) {
                    gameResult = "YOU WIN";
                } else if (allPlayerDead && !allEnemyDead) {
                    gameResult = "YOU LOSE";
                } else {
                    gameResult = "DRAW";
                }

                if (timer != null) {
                    timer.stop();
                }
            }
        }
    }

    // PvP: time runs out and no one has fully died
    private void decideWinnerByHP() {
        if (gameOver || mode != GameMode.PVP) return;

        int playerSum = 0;
        int enemySum = 0;

        for (int hp : playerBaseHP) playerSum += hp;
        for (int hp : enemyBaseHP) enemySum += hp;

        gameOver = true;
        if (playerSum > enemySum) {
            gameResult = "TIME UP - YOU WIN";
        } else if (enemySum > playerSum) {
            gameResult = "TIME UP - YOU LOSE";
        } else {
            gameResult = "TIME UP - DRAW";
        }

        if (timer != null) {
            timer.stop();
        }
    }

    // Troops attack closest enemy in their lane (any direction)
    private Troop findTargetTroop(Troop attacker) {
        Troop best = null;
        double bestDist = Double.MAX_VALUE;

        for (Troop other : troops) {
            if (other == attacker) continue;
            if (other.isDead()) continue;
            if (other.isEnemy() == attacker.isEnemy()) continue;
            if (other.getLane() != attacker.getLane()) continue;

            double dist = Math.abs(other.getCenterX() - attacker.getCenterX());
            if (dist <= attacker.getRange() && dist < bestDist) {
                best = other;
                bestDist = dist;
            }
        }
        return best;
    }

    private int getPlayerBaseFrontX() {
        return 20 + baseWidth;
    }

    private int getEnemyBaseFrontX() {
        int width = getWidth();
        return width - baseWidth - 20;
    }

    // ---------- RENDERING ----------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int width = getWidth();

        // Lanes
        g2.setColor(new Color(40, 40, 40));
        g2.fillRect(0, lane1Y, width, laneHeight);
        g2.fillRect(0, lane2Y, width, laneHeight);
        g2.fillRect(0, lane3Y, width, laneHeight);

        int playerBaseX = 20;
        int enemyBaseX = width - baseWidth - 20;

        // Player bases
        g2.setColor(Color.YELLOW);
        g2.fillRect(playerBaseX, lane1Y + laneHeight/2 - baseHeight/2, baseWidth, baseHeight);
        g2.fillRect(playerBaseX, lane2Y + laneHeight/2 - baseHeight/2, baseWidth, baseHeight);
        g2.fillRect(playerBaseX, lane3Y + laneHeight/2 - baseHeight/2, baseWidth, baseHeight);

        // Enemy bases (PvP only)
        if (mode == GameMode.PVP) {
            g2.setColor(new Color(150, 0, 0));
            g2.fillRect(enemyBaseX, lane1Y + laneHeight/2 - baseHeight/2, baseWidth, baseHeight);
            g2.fillRect(enemyBaseX, lane2Y + laneHeight/2 - baseHeight/2, baseWidth, baseHeight);
            g2.fillRect(enemyBaseX, lane3Y + laneHeight/2 - baseHeight/2, baseWidth, baseHeight);
        }

        // Base HP bars
        int[] laneYs = {lane1Y, lane2Y, lane3Y};
        for (int i = 0; i < 3; i++) {
            int barY = laneYs[i] + laneHeight/2 - baseHeight/2 - 12;

            // Player bar
            drawBaseHPBar(
                g2,
                playerBaseX + (baseWidth / 2) - (hpBarWidth / 2),
                barY,
                playerBaseHP[i]
            );

            // Enemy bar only in PvP
            if (mode == GameMode.PVP) {
                drawBaseHPBar(
                    g2,
                    enemyBaseX + (baseWidth / 2) - (hpBarWidth / 2),
                    barY,
                    enemyBaseHP[i]
                );
            }
        }

        // Troops
        for (Troop t : troops) {
            drawTroop(g2, t);
            drawTroopHPBar(g2, t);
        }

        // UI
        g2.setColor(Color.WHITE);

        if (mode == GameMode.SURVIVAL) {
            g2.drawString("Mode: Survival", 10, 20);
            g2.drawString("Wave: " + waveNumber, 10, 35);
            g2.drawString("P1 Coins: " + coinsP1, 10, 50);
        } else {
            g2.drawString("Mode: PvP", 10, 20);
            g2.drawString("Time: " + getTimeString(), 10, 35);
            g2.drawString("P1 Coins: " + coinsP1, 10, 50);
            g2.drawString("P2 Coins: " + coinsP2, 10, 65);
        }

        g2.drawString("Press H for controls & troop info", 10, 90);

        // Game over overlay
        if (gameOver) {
            drawGameOverOverlay(g2);
        }

        // Help overlay (draw on top of everything else)
        if (showHelp) {
            drawHelpOverlay(g2);
        }
    }

    private String getTimeString() {
        int totalSeconds = timeLeftFrames / 60;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        String minStr = (minutes < 10 ? "0" : "") + minutes;
        String secStr = (seconds < 10 ? "0" : "") + seconds;

        return minStr + ":" + secStr;
    }

    private void drawTroop(Graphics2D g2, Troop t) {
        int x = t.getX();
        int y = t.getY();
        int w = t.getWidth();
        int h = t.getHeight();

        if (!t.isEnemy()) {
            switch (t.getType()) {
                case NORMAL: g2.setColor(Color.BLUE); break;
                case TANK:   g2.setColor(new Color(0, 0, 150)); break;
                case FAST:   g2.setColor(Color.CYAN); break;
                case SNIPER: g2.setColor(new Color(180, 0, 180)); break;
            }
        } else {
            switch (t.getType()) {
                case NORMAL: g2.setColor(Color.RED); break;
                case TANK:   g2.setColor(new Color(120, 0, 0)); break;
                case FAST:   g2.setColor(Color.PINK); break;
                case SNIPER: g2.setColor(Color.MAGENTA); break;
            }
        }

        g2.fillRect(x, y, w, h);

        if (t.getType() == Troop.Type.SNIPER) {
            g2.setColor(Color.WHITE);
            if (!t.isEnemy()) {
                g2.drawLine(x + w, y + h/2, x + w + 30, y + h/2);
            } else {
                g2.drawLine(x - 30, y + h/2, x, y + h/2);
            }
        }
    }

    private void drawBaseHPBar(Graphics2D g2, int x, int y, int hp) {
        int maxHp = 300;
        int barWidth = hpBarWidth;
        int barHeight = 6;

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x, y, barWidth, barHeight);

        double ratio = hp / (double)maxHp;
        int filled = (int)(barWidth * ratio);

        g2.setColor(Color.GREEN);
        g2.fillRect(x, y, filled, barHeight);

        g2.setColor(Color.WHITE);
        g2.drawRect(x, y, barWidth, barHeight);
    }

    private void drawTroopHPBar(Graphics2D g2, Troop t) {
        int x = t.getX();
        int y = t.getY() - 6;
        int w = t.getWidth();
        int barHeight = 4;

        double ratio = t.getHp() / (double)t.getMaxHp();
        int filled = (int)(w * ratio);

        g2.setColor(Color.RED);
        g2.fillRect(x, y, w, barHeight);

        g2.setColor(Color.GREEN);
        g2.fillRect(x, y, filled, barHeight);

        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, w, barHeight);
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        int width = getWidth();
        int height = getHeight();

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, width, height);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));

        int textWidth = g2.getFontMetrics().stringWidth(gameResult);
        int textX = (width - textWidth) / 2;
        int textY = height / 2;

        g2.drawString(gameResult, textX, textY);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        String hint = "Close the window to restart from the menu in BlueJ.";
        int hintWidth = g2.getFontMetrics().stringWidth(hint);
        int hintX = (width - hintWidth) / 2;
        int hintY = textY + 40;
        g2.drawString(hint, hintX, hintY);
    }

    private void drawHelpOverlay(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, w, h);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.drawString("HELP / CONTROLS", w / 2 - 120, 60);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        int y = 110;

        // Player 1 controls
        g2.drawString("Player 1 (Left side):", 40, y); 
        y += 25;
        g2.drawString("NORMAL: 1=L1, 2=L2, 3=L3", 40, y); y += 20;
        g2.drawString("TANK:   Q=L1, W=L2, E=L3", 40, y); y += 20;
        g2.drawString("FAST:   A=L1, S=L2, D=L3", 40, y); y += 20;
        g2.drawString("SNIPER: Z=L1, X=L2, C=L3", 40, y); y += 30;

        // Player 2 controls (PvP only)
        if (mode == GameMode.PVP) {
            g2.drawString("Player 2 (Right side):", 40, y); 
            y += 25;
            g2.drawString("NORMAL: T=L1, Y=L2, U=L3", 40, y); y += 20;
            g2.drawString("TANK:   G=L1, H=L2, J=L3", 40, y); y += 20;
            g2.drawString("FAST:   B=L1, N=L2, M=L3", 40, y); y += 20;
            g2.drawString("SNIPER: I=L1, O=L2, P=L3", 40, y); y += 30;
        }

        // Troop info
        y += 10;
        g2.drawString("Troop Types (Cubes):", 40, y); 
        y += 25;
        g2.drawString("BLUE = Player 1 units", 60, y); y += 20;
        g2.drawString("RED  = Enemy / Player 2 units", 60, y); y += 30;

        g2.drawString("NORMAL: low cost, normal speed, low range, low HP", 40, y); y += 20;
        g2.drawString("TANK:   high cost, very high HP, slow, low range", 40, y); y += 20;
        g2.drawString("FAST:   normal cost, high speed, normal attack", 40, y); y += 20;
        g2.drawString("SNIPER: high cost, high damage, long range, slow fire rate", 40, y); y += 40;

        g2.drawString("Press H again to close this help screen.", w / 2 - 150, h - 40);
    }

    // Allow GameWindow to stop the timer when switching screens
    public void stopGame() {
        if (timer != null) {
            timer.stop();
        }
    }
}
