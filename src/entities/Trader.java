package entities;

import inventory.*;
import entities.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class Trader {
    public int gridX, gridY;
    public String name;
    public BufferedImage portrait;
    public boolean isTalkedTo = false;

    private List<TradeOffer> offers = new ArrayList<>();

    public static class TradeOffer implements Serializable {
        public String itemName;
        public Item.ItemType itemType;
        public Caliber caliber;
        public int pricePerStack;
        public int stackSize;
        public int quantity;
        public int maxQuantity;
        public boolean isImproved;  // ← ДОБАВИТЬ

        public TradeOffer(String itemName, Item.ItemType itemType, Caliber caliber,
                          int pricePerStack, int stackSize, int quantity, boolean isImproved) {
            this.itemName = itemName;
            this.itemType = itemType;
            this.caliber = caliber;
            this.pricePerStack = pricePerStack;
            this.stackSize = stackSize;
            this.quantity = quantity;
            this.maxQuantity = quantity;
            this.isImproved = isImproved;
        }

        // Для не-снарядов (оружие, аптечки)
        public TradeOffer(String itemName, Item.ItemType itemType, Caliber caliber,
                          int pricePerStack, int stackSize, int quantity) {
            this(itemName, itemType, caliber, pricePerStack, stackSize, quantity, false);
        }

        public String getDisplayName() {
            if (itemType == Item.ItemType.AMMO && caliber != null) {
                String improvedText = isImproved ? " улучшенные" : "";
                return caliber.name + " снаряды" + improvedText + " (стак " + stackSize + " шт.)";
            }
            return itemName;
        }

        public int getTotalPrice(int stacks) {
            return pricePerStack * stacks;
        }

        public int getTotalAmmoCount(int stacks) {
            return stackSize * stacks;
        }
    }

    public Trader(int x, int y, String name) {
        this.gridX = x;
        this.gridY = y;
        this.name = name;
        loadOffers();
    }

    private void loadOffers() {
        String offersPath = "src/traders/T34/OffersT34.txt";
        File file = new File(offersPath);

        if (!file.exists()) {
            System.err.println("Файл предложений не найден: " + offersPath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    String itemName = parts[0].trim();
                    int price = Integer.parseInt(parts[1].trim());
                    int quantity = Integer.parseInt(parts[2].trim()); // количество стаков

                    TradeOffer offer = parseOffer(itemName, price, quantity);
                    if (offer != null) {
                        offers.add(offer);
                        System.out.println("Загружено предложение: " + itemName +
                                " - " + price + " серебра за стак (в наличии " + quantity + " стаков)");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки предложений: " + e.getMessage());
        }
    }

    private int getStackSizeForCaliber(Caliber caliber) {
        if (caliber == null) return 1;
        return caliber.getMaxStackSize();
    }

    private TradeOffer parseOffer(String itemName, int price, int quantity) {
        // Снаряды 20mm (базовые)
        if (itemName.equals("20мм снаряды (базовые)")) {
            int stackSize = getStackSizeForCaliber(Caliber.CALIBER_20MM);
            return new TradeOffer(itemName, Item.ItemType.AMMO, Caliber.CALIBER_20MM,
                    price, stackSize, quantity, false);  // ← isImproved = false
        }
        // Снаряды 20mm (улучшенные)
        else if (itemName.equals("20мм снаряды (улучшенные)")) {
            int stackSize = getStackSizeForCaliber(Caliber.CALIBER_20MM);
            return new TradeOffer(itemName, Item.ItemType.AMMO, Caliber.CALIBER_20MM,
                    price, stackSize, quantity, true);   // ← isImproved = true
        }

        // В parseOffer(), добавьте:
        else if (itemName.equals("37мм снаряды (базовые)")) {
            int stackSize = getStackSizeForCaliber(Caliber.CALIBER_37MM);
            return new TradeOffer(itemName, Item.ItemType.AMMO, Caliber.CALIBER_37MM,
                    price, stackSize, quantity, false);
        }
        else if (itemName.equals("37мм снаряды (улучшенные)")) {
            int stackSize = getStackSizeForCaliber(Caliber.CALIBER_37MM);
            return new TradeOffer(itemName, Item.ItemType.AMMO, Caliber.CALIBER_37MM,
                    price, stackSize, quantity, true);
        }
        // 76 мм Л-10С (орудие)
        else if (itemName.equals("76 мм Л-10С")) {
            return new TradeOffer(itemName, Item.ItemType.WEAPON_76MM, null,
                    price, 1, quantity);
        }
        // 76мм снаряды (базовые)
        else if (itemName.equals("76мм снаряды (базовые)")) {
            int stackSize = getStackSizeForCaliber(Caliber.CALIBER_76MM);
            return new TradeOffer(itemName, Item.ItemType.AMMO, Caliber.CALIBER_76MM,
                    price, stackSize, quantity);
        }
        // 3 cm M.K. 103A
        else if (itemName.equals("3 cm M.K. 103A")) {
            return new TradeOffer(itemName, Item.ItemType.WEAPON_30MM, null,
                    price, 1, quantity);
        }
        // 30мм снаряды (базовые)
        else if (itemName.equals("30мм снаряды (базовые)")) {
            int stackSize = getStackSizeForCaliber(Caliber.CALIBER_30MM);
            return new TradeOffer(itemName, Item.ItemType.AMMO, Caliber.CALIBER_30MM,
                    price, stackSize, quantity);
        }
        // Аптечка
        else if (itemName.equals("Аптечка")) {
            return new TradeOffer(itemName, Item.ItemType.MEDKIT, null,
                    price, 1, quantity);
        }
        // Бинт
        else if (itemName.equals("Бинт")) {
            return new TradeOffer(itemName, Item.ItemType.BANDAGE, null,
                    price, 1, quantity);
        }
        // Ремкомплект
        else if (itemName.equals("Ремкомплект")) {
            return new TradeOffer(itemName, Item.ItemType.REPAIR_KIT, null,
                    price, 1, quantity);
        }

        System.err.println("Неизвестный предмет: " + itemName);
        return null;
    }

    public List<TradeOffer> getOffers() {
        return new ArrayList<>(offers);
    }

    // Добавьте метод для получения количества товара (если нужно)
    public TradeOffer getOfferByItemName(String itemName) {
        for (TradeOffer offer : offers) {
            if (offer.itemName.equals(itemName)) {
                return offer;
            }
        }
        return null;
    }

    public boolean buyItem(TradeOffer offer, int stacksAmount, PlayerTank teamSilver, Object buyer) {
        if (stacksAmount <= 0 || stacksAmount > offer.quantity) {
            return false;
        }

        int totalCost = offer.getTotalPrice(stacksAmount);
        if (!teamSilver.removeSilver(totalCost)) {
            return false;
        }

        // ПРОВЕРКА ВЕСА ПЕРЕД ПОКУПКОЙ
        Inventory targetInventory = null;
        if (buyer instanceof PlayerTank) {
            targetInventory = ((PlayerTank) buyer).getInventory();
        } else if (buyer instanceof FriendlyUnit) {
            targetInventory = ((FriendlyUnit) buyer).getInventory();
        }

        if (targetInventory != null) {
            double currentWeight = targetInventory.getTotalWeight();
            double additionalWeight = 0;

            if (offer.itemType == Item.ItemType.AMMO && offer.caliber != null) {
                int totalAmmo = offer.getTotalAmmoCount(stacksAmount);
                additionalWeight = totalAmmo * 0.1; // Примерный вес одного снаряда
            } else {
                // Для не-снарядов: создаём ВРЕМЕННЫЙ предмет для расчёта веса
                Item tempItem = new Item(offer.itemType, stacksAmount);
                additionalWeight = tempItem.getTotalWeight();
            }

            double maxWeight = (buyer instanceof PlayerTank) ?
                    ((PlayerTank) buyer).maxCarryWeight : ((FriendlyUnit) buyer).maxCarryWeight;

            if (currentWeight + additionalWeight > maxWeight) {
                System.out.println("❌ Недостаточно грузоподъёмности! Нужно освободить " +
                        String.format("%.1f", currentWeight + additionalWeight - maxWeight) + " кг");
                // Возвращаем серебро
                teamSilver.addSilver(totalCost);
                return false;
            }
        }

        // ===== ИСПРАВЛЕНИЕ: Добавляем КАЖДЫЙ стак отдельно =====
        if (offer.itemType == Item.ItemType.AMMO && offer.caliber != null) {
            int totalAmmo = offer.getTotalAmmoCount(stacksAmount);
            AmmoItem ammo = new AmmoItem(offer.caliber, totalAmmo, offer.isImproved);
            targetInventory.addAmmoItem(ammo);
            System.out.println("✅ " + getBuyerName(buyer) + " купил " + stacksAmount + " стаков (" + totalAmmo + " шт.) "
                    + (offer.isImproved ? "улучшенных " : "") + "снарядов " + offer.caliber.name);
        } else {
            // Для не-снарядов (оружие, аптечки и т.д.) - добавляем КАЖДЫЙ предмет по одному
            for (int i = 0; i < stacksAmount; i++) {
                Item itemToAdd = new Item(offer.itemType, 1);
                if (!targetInventory.addItemToInventory(itemToAdd)) {
                    // Если не хватает места, возвращаем серебро за все непоместившиеся предметы
                    System.err.println("❌ Не хватает места для предмета! Возврат серебра...");
                    // Возвращаем серебро за все предметы, которые не поместились
                    int refund = totalCost - (i * offer.pricePerStack);
                    teamSilver.addSilver(refund);
                    return i > 0; // Хотя бы несколько предметов добавилось
                }
            }
            System.out.println("✅ " + getBuyerName(buyer) + " купил " + stacksAmount + "x " + offer.getDisplayName());
        }

        offer.quantity -= stacksAmount;
        return true;
    }

    private String getBuyerName(Object buyer) {
        if (buyer instanceof PlayerTank) {
            return "Leichttraktor";
        } else if (buyer instanceof FriendlyUnit) {
            return ((FriendlyUnit) buyer).name;
        }
        return "Unknown";
    }

    public boolean sellItem(Object seller, Item.ItemType type, int amount, int price, PlayerTank player) {
        if (amount <= 0) return false;

        Inventory inv = null;
        if (seller instanceof PlayerTank) {
            inv = ((PlayerTank) seller).getInventory();
        } else if (seller instanceof FriendlyUnit) {
            inv = ((FriendlyUnit) seller).getInventory();
        } else {
            return false;
        }

        // Проверяем, есть ли нужное количество
        if (inv.getItemCount(type) < amount) return false;

        // Удаляем предметы
        for (int i = 0; i < amount; i++) {
            if (!inv.useItem(type)) return false;
        }

        // Добавляем серебро игроку
        player.addSilver(price * amount);

        return true;
    }

    public boolean sellAmmo(Object seller, Caliber caliber, int amount, int price, PlayerTank player) {
        if (amount <= 0) return false;

        Inventory inv = null;
        if (seller instanceof PlayerTank) {
            inv = ((PlayerTank) seller).getInventory();
        } else if (seller instanceof FriendlyUnit) {
            inv = ((FriendlyUnit) seller).getInventory();
        } else {
            return false;
        }

        // Проверяем, есть ли нужное количество снарядов
        int available = inv.getAmmoCount(caliber);
        if (available < amount) return false;

        // Удаляем снаряды
        inv.removeAmmoByCaliber(caliber, amount);

        // Добавляем серебро игроку
        player.addSilver(price * amount);

        return true;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize, int tankSize,
                     BufferedImage traderImage, Object activeUnit) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        if (traderImage != null) {
            g.drawImage(traderImage, x, y, tankSize, tankSize, null);
        } else {
            // Запасной вариант
            g.setColor(new Color(100, 150, 50));
            g.fillRoundRect(x, y, tankSize, tankSize, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("💰", x + tankSize/2 - 8, y + tankSize/2 + 5);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 9));
        g.drawString(name, x + 5, y - 8);
    }

    public boolean sellFullAmmoStack(Object seller, Caliber caliber, int stacksToSell, int stackSize, int pricePerStack, PlayerTank player) {
        if (stacksToSell <= 0) return false;

        Inventory inv = null;
        if (seller instanceof PlayerTank) {
            inv = ((PlayerTank) seller).getInventory();
        } else if (seller instanceof FriendlyUnit) {
            inv = ((FriendlyUnit) seller).getInventory();
        } else {
            return false;
        }

        int totalAmmoToRemove = stacksToSell * stackSize;
        int availableAmmo = inv.getAmmoCount(caliber);

        // Проверяем, есть ли нужное количество полных стаков
        if (availableAmmo < totalAmmoToRemove) return false;

        // Удаляем снаряды
        if (!inv.removeAmmoByCaliber(caliber, totalAmmoToRemove)) return false;

        // Добавляем серебро
        player.addSilver(pricePerStack * stacksToSell);

        System.out.println("💰 Продано " + stacksToSell + " стаков (" + totalAmmoToRemove + " шт.) снарядов " + caliber.name);
        return true;
    }
}