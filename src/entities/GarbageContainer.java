package entities;

import inventory.Item;
import inventory.Caliber;
import inventory.AmmoItem;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.*;

public class GarbageContainer implements Serializable {
    public int gridX, gridY;
    public int width = 2;
    public int height = 1;
    public boolean isAlive = true;
    public boolean isLooted = false;

    private boolean wasModified = false;

    private Map<Item.ItemType, Integer> items = new HashMap<>();
    private Map<Caliber, Integer> ammo = new HashMap<>();

    public GarbageContainer(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }

    public void addItem(Item.ItemType type, int amount) {
        wasModified = true;
        int current = items.getOrDefault(type, 0);
        items.put(type, current + amount);
    }

    public void addAmmo(Caliber caliber, int amount) {
        wasModified = true;
        int current = ammo.getOrDefault(caliber, 0);
        ammo.put(caliber, current + amount);
    }

    public Map<Caliber, Integer> getAmmo() {
        return new HashMap<>(ammo);
    }

    public int takeAmmo(Caliber caliber, int amount) {
        wasModified = true;
        if (!ammo.containsKey(caliber)) return 0;
        int current = ammo.get(caliber);
        int taken = Math.min(amount, current);
        if (taken > 0) {
            ammo.put(caliber, current - taken);
            if (ammo.get(caliber) == 0) {
                ammo.remove(caliber);
            }
            System.out.println("Взято " + taken + " снарядов калибра " + caliber.name);
        }
        return taken;
    }

    public int getAmmoCount(Caliber caliber) {
        return ammo.getOrDefault(caliber, 0);
    }

    public int takeItem(Item.ItemType type, int amount) {
        wasModified = true;
        int current = items.getOrDefault(type, 0);
        int taken = Math.min(amount, current);
        if (taken > 0) {
            items.put(type, current - taken);
            if (items.get(type) == 0) {
                items.remove(type);
            }
        }
        return taken;
    }

    public boolean wasModified() {
        return wasModified;
    }

    public int getItemCount(Item.ItemType type) {
        return items.getOrDefault(type, 0);
    }

    public Map<Item.ItemType, Integer> getItems() {
        return new HashMap<>(items);
    }

    public boolean isEmpty() {
        return items.isEmpty() && ammo.isEmpty();
    }

    public boolean containsCell(int x, int y) {
        boolean result = (x >= this.gridX && x < this.gridX + width &&
                y >= this.gridY && y < this.gridY + height);
        return result;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize, BufferedImage image) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;
        int objWidth = width * cellSize;
        int objHeight = height * cellSize;

        if (image != null) {
            // Рисуем изображение, масштабируя под размер 2x1 клетки
            g.drawImage(image, x, y, objWidth, objHeight, null);
        } else {
            // Запасной вариант - если изображение не загружено
            g.setColor(new Color(80, 90, 70));
            g.fillRect(x, y, objWidth, objHeight);
            g.setColor(new Color(60, 70, 50));
            g.drawRect(x, y, objWidth, objHeight);
            g.setColor(new Color(50, 60, 40));
            g.fillRect(x + 5, y - 5, objWidth - 10, 8);
            g.setColor(new Color(100, 110, 90));
            g.fillRoundRect(x + objWidth/2 - 15, y + objHeight/2 - 5, 30, 10, 5, 5);
        }

        // Индикатор, что контейнер не пуст
        if (!isLooted && !isEmpty()) {
            g.setColor(new Color(255, 200, 0, 200));
            g.fillOval(x + objWidth - 20, y + 5, 16, 16);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("📦", x + objWidth - 17, y + 14);
        }
    }
}