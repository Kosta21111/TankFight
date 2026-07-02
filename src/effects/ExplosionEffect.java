package effects;

import java.awt.*;

public class ExplosionEffect {
    private double x, y;
    private int progress = 0;
    private int duration = 10; // кадров

    public ExplosionEffect(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean update() {
        progress++;
        return progress >= duration;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY) {
        int screenX = (int)x - cameraX;
        int screenY = (int)y - cameraY;
        int size = 10 + progress * 2;
        int alpha = 255 - (progress * 25);

        g.setColor(new Color(255, 100, 0, Math.min(255, alpha)));
        g.fillOval(screenX - size/2, screenY - size/2, size, size);

        g.setColor(new Color(255, 200, 0, Math.min(200, alpha)));
        g.fillOval(screenX - size/4, screenY - size/4, size/2, size/2);
    }
}