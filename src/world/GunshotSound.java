package world;

import java.awt.Point;

public class GunshotSound {
    public int x, y;           // Координаты выстрела
    public int radius;         // Радиус слышимости
    public int turnHeard;      // В каком ходу был произведён выстрел
    public double caliber;     // Калибр оружия

    public GunshotSound(int x, int y, double caliber, int currentTurn) {
        this.x = x;
        this.y = y;
        this.caliber = caliber;
        // Радиус слышимости: 10 + 100 * калибр (в метрах)
        // Например: 20мм (0.02) -> 10 + 100*0.02 = 12 клеток
        // 128мм (0.128) -> 10 + 100*0.128 = 22.8 ≈ 23 клетки
        this.radius = (int)(10 + 100 * caliber);
        this.turnHeard = currentTurn;
    }

    public boolean isInRange(int targetX, int targetY) {
        int dx = Math.abs(x - targetX);
        int dy = Math.abs(y - targetY);
        int distance = (int)Math.sqrt(dx*dx + dy*dy);
        return distance <= radius;
    }
}