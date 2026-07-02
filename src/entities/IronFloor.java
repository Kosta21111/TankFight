package entities;

import java.awt.*;
import java.awt.image.BufferedImage;

public class IronFloor {
    public int gridX, gridY;
    public boolean isAlive = true;

    public IronFloor(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize, BufferedImage ironFloorImage) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        if (ironFloorImage != null) {
            g.drawImage(ironFloorImage, x, y, cellSize, cellSize, null);
        } else {
            System.err.println("❌ ironFloorImage = null в клетке [" + gridX + "," + gridY + "]");
            // Запасной вариант
            g.setColor(new Color(120, 120, 130));
            g.fillRect(x, y, cellSize, cellSize);
            g.setColor(new Color(100, 100, 110));
            g.drawRect(x, y, cellSize, cellSize);
        }
    }
}