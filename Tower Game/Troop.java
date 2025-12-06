public class Troop {

    public enum Type {
        NORMAL, TANK, FAST, SNIPER
    }

    private Type type;
    private boolean enemy;   // false = player, true = enemy
    private int lane;        // 1, 2 or 3
    private double x;        // top-left X
    private int y;           // top-left Y
    private double speed;

    private int width;
    private int height;

    // Combat stats
    private int maxHp;
    private int hp;
    private int attack;
    private double range;         // in pixels (distance along lane)
    private int cooldownTime;     // frames between attacks
    private int cooldownTimer;    // frames remaining until next shot

    public Troop(Type type, boolean enemy, int lane, int laneCenterY, double startX) {
        this.type = type;
        this.enemy = enemy;
        this.lane = lane;

        this.width = getWidthForType(type);
        this.height = getHeightForType(type);
        this.x = startX;
        this.y = laneCenterY - height / 2;

        setStatsForType(type);
        this.hp = maxHp;
        this.cooldownTimer = 0;
    }

    private int getWidthForType(Type t) {
        switch (t) {
            case NORMAL: return 20;
            case TANK:   return 28;
            case FAST:   return 18;
            case SNIPER: return 20;
            default:     return 20;
        }
    }

    private int getHeightForType(Type t) {
        switch (t) {
            case NORMAL: return 20;
            case TANK:   return 28;
            case FAST:   return 18;
            case SNIPER: return 20;
            default:     return 20;
        }
    }

    private void setStatsForType(Type t) {
        // Values based on our design
        switch (t) {
            case NORMAL:
                maxHp = 80;
                attack = 10;
                speed = 2.0;
                range = 40.0;
                cooldownTime = 30;   // about 0.5s at ~60 FPS
                break;
            case TANK:
                maxHp = 150;
                attack = 10;
                speed = 1.0;
                range = 40.0;
                cooldownTime = 30;
                break;
            case FAST:
                maxHp = 80;
                attack = 15;
                speed = 3.0;
                range = 40.0;
                cooldownTime = 30;
                break;
            case SNIPER:
                maxHp = 40;
                attack = 50;
                speed = 1.2;
                range = 150.0;
                cooldownTime = 60;  // slower fire rate
                break;
            default:
                maxHp = 80;
                attack = 10;
                speed = 2.0;
                range = 40.0;
                cooldownTime = 30;
                break;
        }
    }

    // Movement (only when not attacking)
    public void move() {
        if (enemy) {
            x -= speed;
        } else {
            x += speed;
        }
    }

    public void tickCooldown() {
        if (cooldownTimer > 0) {
            cooldownTimer--;
        }
    }

    public boolean isReadyToAttack() {
        return cooldownTimer <= 0;
    }

    public void resetCooldown() {
        cooldownTimer = cooldownTime;
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) {
            hp = 0;
        }
    }

    public boolean isDead() {
        return hp <= 0;
    }

    // Front X (the side facing the enemy base)
    public int getFrontX() {
        if (enemy) {
            return (int)x;           // moving left, front is left edge
        } else {
            return (int)x + width;   // moving right, front is right edge
        }
    }

    // Center X for distance calculations
    public double getCenterX() {
        return x + width / 2.0;
    }

    // Getters
    public int getX() { return (int)x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Type getType() { return type; }
    public boolean isEnemy() { return enemy; }
    public int getLane() { return lane; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public double getRange() { return range; }
}
