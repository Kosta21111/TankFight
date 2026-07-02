package entities;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Pavement {
    public int gridX, gridY;
    public boolean isAlive = true;

    public Pavement(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize, BufferedImage pavementImage) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        if (pavementImage != null) {
            g.drawImage(pavementImage, x, y, cellSize, cellSize, null);
        } else {
            // Запасной вариант - серый прямоугольник
            g.setColor(new Color(80, 80, 80));
            g.fillRect(x, y, cellSize, cellSize);
            g.setColor(new Color(60, 60, 60));
            g.drawRect(x, y, cellSize, cellSize);
        }
    }
}