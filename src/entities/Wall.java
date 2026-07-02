package entities;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Wall {
    public int gridX, gridY;
    public int health;
    public int maxHealth;
    public boolean isDestructible;
    public String wallType;  // ← ДОБАВИТЬ: "Brick" или "IronBlock"

    // Конструктор для обратной совместимости (по умолчанию Brick)
    public Wall(int x, int y, int health, boolean isDestructible) {
        this(x, y, health, isDestructible, "Brick");
    }

    // НОВЫЙ КОНСТРУКТОР с типом стены
    public Wall(int x, int y, int health, boolean isDestructible, String wallType) {
        this.gridX = x;
        this.gridY = y;
        this.health = health;
        this.maxHealth = health;
        this.isDestructible = isDestructible;
        this.wallType = wallType != null ? wallType : "Brick";
    }

    public void takeDamage(int damage) {
        if (!isDestructible) return;
        health -= damage;
        if (health < 0) health = 0;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize,
                     BufferedImage brickImage, BufferedImage ironBlockImage) {  // ← ДОБАВИЛИ ПАРАМЕТР
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        // Выбираем изображение в зависимости от типа
        BufferedImage imageToDraw = null;
        if ("IronBlock".equals(wallType)) {
            imageToDraw = ironBlockImage;
        } else {
            imageToDraw = brickImage;  // Brick по умолчанию
        }

        if (imageToDraw != null) {
            g.drawImage(imageToDraw, x, y, cellSize, cellSize, null);
        } else {
            // Запасной вариант - цветной прямоугольник
            if ("IronBlock".equals(wallType)) {
                g.setColor(new Color(100, 100, 110));  // Серый металлик
            } else {
                g.setColor(new Color(139, 69, 19));    // Коричневый кирпич
            }
            g.fillRect(x, y, cellSize, cellSize);
            g.setColor(new Color(101, 67, 33));
            g.drawRect(x, y, cellSize, cellSize);
        }

        // Полоска прочности для разрушаемых стен
        if (isDestructible && health < maxHealth) {
            int healthBarWidth = (int)(cellSize * ((float)health / maxHealth));
            g.setColor(new Color(200, 0, 0, 180));
            g.fillRect(x, y - 5, healthBarWidth, 3);
        }
    }
}