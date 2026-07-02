package entities;

import java.awt.*;
import java.awt.image.BufferedImage;

public class InfernalLand {
    public int gridX, gridY;
    public boolean isAlive = true;

    public InfernalLand(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize, BufferedImage infernalLandImage) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        if (infernalLandImage != null) {
            g.drawImage(infernalLandImage, x, y, cellSize, cellSize, null);
        } else {
            // Запасной вариант - тёмно-красный прямоугольник с текстурой лавы
            g.setColor(new Color(80, 30, 20));
            g.fillRect(x, y, cellSize, cellSize);
            g.setColor(new Color(180, 60, 30));
            // Рисуем трещины, имитирующие адскую землю
            for (int i = 0; i < 3; i++) {
                g.drawLine(x + 5 + i * 15, y + 10, x + 15 + i * 15, y + 30);
                g.drawLine(x + 10 + i * 15, y + 30, x + 20 + i * 15, y + 45);
            }
            g.drawRect(x, y, cellSize, cellSize);
        }
    }
}