package entities;

import inventory.Item;
import inventory.Caliber;
import inventory.AmmoItem;
import inventory.MedicalItem;
import inventory.KeyItem; // <-- ЭТУ СТРОКУ НУЖНО ДОБАВИТЬ
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.*;

public class StorageChest implements Serializable {
    public int gridX, gridY;
    public int width = 1;
    public int height = 1;
    public boolean isAlive = true;

    private Map<Item.ItemType, Integer> items = new HashMap<>();
    private Map<Door.DoorColor, Integer> keys = new HashMap<>();
    private Map<Caliber, Integer> ammo = new HashMap<>();

    public StorageChest(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }

    private boolean wasModified = false;  // ← НОВОЕ ПОЛЕ

    public void addItem(Item.ItemType type, int amount) {
        wasModified = true;
        System.out.println("  StorageChest.addItem: type=" + type.name() + ", amount=" + amount);
        int current = items.getOrDefault(type, 0);
        items.put(type, current + amount);
    }

    // StorageChest.java - новый метод
    public void addAmmo(Caliber caliber, int amount) {
        wasModified = true;
        int current = ammo.getOrDefault(caliber, 0);
        ammo.put(caliber, current + amount);
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
        }
        return taken;
    }

    // Методы:
    public void addKey(KeyItem key) {
        wasModified = true;
        int current = keys.getOrDefault(key.getColor(), 0);
        keys.put(key.getColor(), current + key.getCount());
    }

    public void addKey(Door.DoorColor color, int count) {
        keys.put(color, keys.getOrDefault(color, 0) + count);
    }

    public int getKeyCount(Door.DoorColor color) {
        return keys.getOrDefault(color, 0);
    }

    public int takeKey(Door.DoorColor color, int amount) {
        wasModified = true;
        if (!keys.containsKey(color)) return 0;
        int current = keys.get(color);
        int taken = Math.min(amount, current);
        if (taken > 0) {
            keys.put(color, current - taken);
            if (keys.get(color) == 0) {
                keys.remove(color);
            }
        }
        return taken;
    }

    public Map<Door.DoorColor, Integer> getKeys() {
        return new HashMap<>(keys);
    }

// А предметы AMMO хранить как обычно, но при взятии создавать AmmoItem

    // Также добавьте метод для получения количества аммуниции по калибру
    public int getAmmoCount(Caliber caliber) {
        return ammo.getOrDefault(caliber, 0);
    }

    public int takeItem(Item.ItemType type, int amount) {
        wasModified = true;
        if (!items.containsKey(type)) return 0;
        int current = items.get(type);
        int taken = Math.min(amount, current);
        if (taken > 0) {
            items.put(type, current - taken);
            if (items.get(type) == 0) {
                items.remove(type);
            }
        }
        System.out.println("takeItem: type=" + type.name() + ", requested=" + amount +
                ", available=" + current + ", taken=" + taken);
        return taken;
    }

    public boolean wasModified() {
        return wasModified;
    }

    public Map<Caliber, Integer> getAmmo() {
        return new HashMap<>(ammo);
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
        return this.gridX == x && this.gridY == y;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize, BufferedImage image) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        if (image != null) {
            g.drawImage(image, x, y, cellSize, cellSize, null);
        } else {
            g.setColor(new Color(139, 69, 19));
            g.fillRect(x, y, cellSize, cellSize);
            g.setColor(new Color(101, 67, 33));
            g.drawRect(x, y, cellSize, cellSize);

            g.setColor(new Color(255, 215, 0));
            g.fillOval(x + cellSize / 2 - 5, y + cellSize / 2 - 3, 10, 6);
        }

        if (!items.isEmpty() || !ammo.isEmpty()) {
            g.setColor(new Color(255, 200, 0, 200));
            g.fillOval(x + cellSize - 15, y + 5, 12, 12);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("📦", x + cellSize - 14, y + 14);
        }
    }
}