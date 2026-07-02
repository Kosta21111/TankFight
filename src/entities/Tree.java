package entities;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Tree {
    public int gridX, gridY;
    public String treeType; // "Tree1" или "Tree2"
    public boolean isAlive = true;
    public int health = 50;
    public int maxHealth = 50;

    public Tree(int x, int y, String treeType) {
        this.gridX = x;
        this.gridY = y;
        this.treeType = treeType;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            isAlive = false;
        }
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize,
                     BufferedImage tree1Image, BufferedImage tree2Image) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        BufferedImage image = treeType.equals("Tree1") ? tree1Image : tree2Image;

        if (image != null) {
            g.drawImage(image, x, y, cellSize, cellSize, null);
        } else {
            // Запасной вариант - зелёный прямоугольник
            g.setColor(new Color(34, 139, 34));
            g.fillRect(x, y, cellSize, cellSize);
            g.setColor(new Color(0, 100, 0));
            g.drawRect(x, y, cellSize, cellSize);
        }

        // Полоска здоровья
        if (health < maxHealth) {
            int healthBarWidth = (int)(cellSize * ((float)health / maxHealth));
            g.setColor(new Color(200, 0, 0, 180));
            g.fillRect(x, y - 5, healthBarWidth, 3);
        }
    }
}