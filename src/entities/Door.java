package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import world.GameWorld;

public class Door implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient GameWorld gameWorld;

    public int gridX, gridY;
    public DoorColor color;
    public boolean isOpen = false;
    public boolean isLocked = true;
    public boolean isAlive = true;

    public enum DoorColor {
        RED("RedDoor.png", new Color(200, 50, 50), "Красная"), // <-- ДОБАВЛЕН displayName
        ORANGE("OrangeDoor.png", new Color(255, 150, 50), "Оранжевая"),
        YELLOW("YellowDoor.png", new Color(255, 215, 0), "Жёлтая"),
        GREEN("GreenDoor.png", new Color(50, 200, 50), "Зелёная"),
        BLUE("BlueDoor.png", new Color(50, 100, 255), "Синяя"),
        VIOLET("VioletDoor.png", new Color(150, 50, 200), "Фиолетовая");

        public final String fileName;
        public final Color color;
        public final String displayName; // <-- ДОБАВЛЕНО ПОЛЕ

        DoorColor(String fileName, Color color, String displayName) {
            this.fileName = fileName;
            this.color = color;
            this.displayName = displayName;
        }
    }

    public Door(int x, int y, DoorColor color) {
        this.gridX = x;
        this.gridY = y;
        this.color = color;
    }

    public void open() {
        if (!isOpen && isAlive) {
            // ===== ДАЖЕ ЕСЛИ ЗАБЛОКИРОВАНА, ОТКРЫВАЕМ (ключ уже использован) =====
            isOpen = true;
            isLocked = false;  // ← ВАЖНО: снимаем блокировку!
            System.out.println("🔓 Дверь открыта в [" + gridX + "," + gridY + "]");
            if (gameWorld != null) {
                gameWorld.onDoorStateChanged(this);
            }
        }
    }

    public void close() {
        if (isOpen && isAlive) {
            isOpen = false;
            System.out.println("🔒 Дверь закрыта в [" + gridX + "," + gridY + "]");
            // ===== УВЕДОМЛЯЕМ МИР ОБ ИЗМЕНЕНИИ =====
            if (gameWorld != null) {
                gameWorld.onDoorStateChanged(this);
            }
        }
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize, BufferedImage doorImage) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        if (doorImage != null) {
            g.drawImage(doorImage, x, y, cellSize, cellSize, null);
        } else {
            // Запасной вариант - цветной прямоугольник с символом замка
            g.setColor(color.color);
            g.fillRect(x, y, cellSize, cellSize);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, cellSize, cellSize);

            // Рисуем замок
            g.setColor(Color.YELLOW);
            g.fillOval(x + cellSize/2 - 4, y + cellSize/2 - 3, 8, 6);
            g.setColor(Color.BLACK);
            g.fillRect(x + cellSize/2 - 1, y + cellSize/2 - 5, 2, 3);
        }

        // Если дверь открыта - рисуем зелёную галочку
        if (isOpen) {
            g.setColor(new Color(0, 255, 0, 150));
            g.fillRect(x, y, cellSize, cellSize);
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("✓", x + cellSize/2 - 10, y + cellSize/2 + 8);
        }
    }

    // НОВЫЙ МЕТОД: попытка открыть дверь ключом
    public boolean tryOpenWithKey(DoorColor keyColor) {
        if (isOpen) return true;
        if (!isLocked) {
            open();
            return true;
        }
        if (keyColor == this.color) {
            open();
            this.isLocked = false;
            System.out.println("🔓 Дверь " + color.displayName + " открыта ключом!");
            return true;
        }
        System.out.println("❌ Неподходящий ключ для двери " + color.displayName);
        return false;
    }
}