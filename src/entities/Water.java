package entities;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Water {
    public int gridX, gridY;
    public boolean isAlive = true;

    public Water(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize, BufferedImage waterImage) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        if (waterImage != null) {
            g.drawImage(waterImage, x, y, cellSize, cellSize, null);
        } else {
            // Запасной вариант - синий прямоугольник с волнами
            g.setColor(new Color(30, 100, 180));
            g.fillRect(x, y, cellSize, cellSize);
            g.setColor(new Color(50, 150, 220, 150));
            // Рисуем "волны"
            for (int i = 0; i < 3; i++) {
                g.drawArc(x + 5 + i * 15, y + cellSize - 15, 10, 8, 0, 180);
            }
            g.setColor(new Color(20, 80, 150));
            g.drawRect(x, y, cellSize, cellSize);
        }
    }
}