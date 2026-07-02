package entities;

import java.awt.*;
import java.awt.image.BufferedImage;

public class OakPlanks {
    public int gridX, gridY;
    public boolean isAlive = true;

    public OakPlanks(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize, BufferedImage oakPlanksImage) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        if (oakPlanksImage != null) {
            g.drawImage(oakPlanksImage, x, y, cellSize, cellSize, null);
        } else {
            // Запасной вариант - коричневый прямоугольник с текстурой дерева
            g.setColor(new Color(160, 120, 80));
            g.fillRect(x, y, cellSize, cellSize);
            g.setColor(new Color(120, 80, 40));
            // Рисуем линии, имитирующие доски
            for (int i = 0; i < 3; i++) {
                g.drawLine(x + 5, y + 10 + i * 15, x + cellSize - 5, y + 10 + i * 15);
            }
            g.drawRect(x, y, cellSize, cellSize);
        }
    }
}