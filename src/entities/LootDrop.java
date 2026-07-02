package entities;

import inventory.AmmoItem;
import inventory.Caliber;
import inventory.Item;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.*;

public class LootDrop implements Serializable {
    public int gridX, gridY;
    public Map<Item.ItemType, Integer> items = new HashMap<>();
    public Map<Caliber, Integer> ammo = new HashMap<>();
    public boolean isAlive = true;

    public LootDrop(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }

    public void addItem(Item.ItemType type, int amount) {
        int current = items.getOrDefault(type, 0);
        items.put(type, current + amount);
    }

    public void addAmmo(Caliber caliber, int amount) {
        int current = ammo.getOrDefault(caliber, 0);
        ammo.put(caliber, current + amount);
    }

    public int takeItem(Item.ItemType type, int amount) {
        if (!items.containsKey(type)) return 0;
        int current = items.get(type);
        int taken = Math.min(amount, current);
        if (taken > 0) {
            items.put(type, current - taken);
            if (items.get(type) == 0) {
                items.remove(type);
            }
        }
        return taken;
    }

    public int takeAmmo(Caliber caliber, int amount) {
        if (!ammo.containsKey(caliber)) return 0;
        int current = ammo.get(caliber);
        int taken = Math.min(amount, current);
        if (taken > 0) {
            ammo.put(caliber, current - taken);
            if (ammo.get(caliber) == 0) {
                ammo.remove(caliber);
            }
        }
        return taken;
    }

    public int getItemCount(Item.ItemType type) {
        return items.getOrDefault(type, 0);
    }

    public int getAmmoCount(Caliber caliber) {
        return ammo.getOrDefault(caliber, 0);
    }

    public boolean isEmpty() {
        return items.isEmpty() && ammo.isEmpty();
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        // Подсчитываем общее количество предметов для визуального отображения
        int totalItems = 0;
        for (Integer count : items.values()) {
            totalItems += count;
        }
        for (Integer count : ammo.values()) {
            totalItems += count;
        }

        // Рисуем мешок с дропом (размер зависит от количества)
        int size = cellSize / 2;
        int offset = (cellSize - size) / 2;

        if (totalItems > 50) {
            g.setColor(new Color(255, 200, 100, 220));
            g.fillOval(x + offset - 3, y + offset - 3, size + 6, size + 6);
            g.setColor(new Color(200, 150, 50));
            g.fillOval(x + offset, y + offset, size, size);
        } else if (totalItems > 20) {
            g.setColor(new Color(255, 200, 100, 220));
            g.fillOval(x + offset, y + offset, size, size);
            g.setColor(new Color(200, 150, 50));
            g.fillOval(x + offset + 2, y + offset + 2, size - 4, size - 4);
        } else {
            g.setColor(new Color(255, 200, 100, 220));
            g.fillOval(x + offset + 3, y + offset + 3, size - 6, size - 6);
            g.setColor(new Color(200, 150, 50));
            g.fillOval(x + offset + 5, y + offset + 5, size - 10, size - 10);
        }

        // Индикатор, что дроп не пуст
        g.setColor(new Color(255, 200, 0, 200));
        g.fillOval(x + cellSize - 15, y + 5, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString(totalItems > 99 ? "99+" : String.valueOf(totalItems), x + cellSize - 13, y + 14);
    }
}