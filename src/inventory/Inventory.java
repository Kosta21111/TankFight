package inventory;

import combat.Weapon;
import entities.PlayerTank;
import java.io.Serializable;
import java.util.*;
import entities.FriendlyUnit;
import javax.swing.JOptionPane;  // ← ДОБАВЬТЕ ЭТУ СТРОКУ

public class Inventory implements Serializable {

    private int weaponAccuracy;
    private double caliber;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 5;

    private Item equippedWeapon;
    public int currentAmmo;
    public int maxAmmo;
    public int reloadCost;

    public int burstSize;
    private transient Weapon weapon;
    private Item[][] slots;
    private int gold = 0;

    private boolean[][] occupied = new boolean[WIDTH][HEIGHT];
    private AmmoItem currentAmmoType;  // ← НОВОЕ ПОЛЕ - какой тип снарядов сейчас заряжен

    public Inventory() {
        slots = new Item[WIDTH][HEIGHT];
        occupied = new boolean[WIDTH][HEIGHT];
        weapon = new Weapon();
        this.maxAmmo = weapon.magazineSize;
        this.reloadCost = weapon.reloadCost;
        this.burstSize = weapon.burstSize;
        this.weaponAccuracy = weapon.accuracy;
        this.caliber = weapon.caliber;
        this.currentAmmo = maxAmmo;
        equippedWeapon = new Item(Item.ItemType.WEAPON, 1);

        // ===== ДОБАВЬТЕ ЭТУ СТРОКУ =====
        this.currentAmmoType = new AmmoItem(Caliber.CALIBER_20MM, maxAmmo, false); // базовые 20мм
    }

    // Добавьте этот конструктор для союзников (без оружия по умолчанию)
    public Inventory(boolean isForFriendly) {
        slots = new Item[WIDTH][HEIGHT];
        occupied = new boolean[WIDTH][HEIGHT];
        if (!isForFriendly) {
            // Только для игрока создаём оружие по умолчанию
            weapon = new Weapon();
            this.maxAmmo = weapon.magazineSize;
            this.reloadCost = weapon.reloadCost;
            this.burstSize = weapon.burstSize;
            this.weaponAccuracy = weapon.accuracy;
            this.caliber = weapon.caliber;
            this.currentAmmo = maxAmmo;
            equippedWeapon = new Item(Item.ItemType.WEAPON, 1);
        } else {
            // Для союзников - НЕ СОЗДАЁМ weapon, все поля в 0
            this.weapon = null;  // ← ВАЖНО!
            this.maxAmmo = 0;
            this.reloadCost = 0;
            this.burstSize = 0;
            this.weaponAccuracy = 0;
            this.caliber = 0;
            this.currentAmmo = 0;
            this.equippedWeapon = null;
        }
    }

    // Добавьте эти методы в класс Inventory (после конструкторов)

    public void setBurstSize(int burstSize) {
        this.burstSize = burstSize;
        System.out.println("  inventory.burstSize = " + this.burstSize);
    }

    public void setMaxAmmo(int maxAmmo) {
        this.maxAmmo = maxAmmo;
        System.out.println("  inventory.maxAmmo = " + this.maxAmmo);
    }

    public void setReloadCost(int reloadCost) {
        this.reloadCost = reloadCost;
        System.out.println("  inventory.reloadCost = " + this.reloadCost);
    }

    public void setWeaponAccuracy(int weaponAccuracy) {
        this.weaponAccuracy = weaponAccuracy;
        System.out.println("  inventory.weaponAccuracy = " + this.weaponAccuracy);
    }

    public void setCaliber(double caliber) {
        this.caliber = caliber;
        System.out.println("  inventory.caliber = " + this.caliber);
    }

    public void rebuildOccupied() {
        // Сбрасываем occupied
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                occupied[x][y] = false;
            }
        }

        // Помечаем все клетки с предметами
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Item item = slots[x][y];
                if (item != null && item.getCount() > 0) {
                    // Помечаем все клетки, которые занимает предмет
                    for (int dy = 0; dy < item.getHeight(); dy++) {
                        for (int dx = 0; dx < item.getWidth(); dx++) {
                            int targetX = x + dx;
                            int targetY = y + dy;
                            if (targetX < WIDTH && targetY < HEIGHT) {
                                occupied[targetX][targetY] = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public void clearWeapon() {
        this.weapon = null;
        this.maxAmmo = 0;
        this.reloadCost = 0;
        this.burstSize = 0;
        this.weaponAccuracy = 0;
        this.caliber = 0;
        this.currentAmmo = 0;
        this.equippedWeapon = null;
        System.out.println("🗑 Оружие по умолчанию удалено");
    }

    // Стартовые предметы для игрока
    public void addStarterItems() {
        addItem(Item.ItemType.AMMO, 60);
        addItem(Item.ItemType.MEDKIT, 2);
        addItem(Item.ItemType.REPAIR_KIT, 1);
        addItem(Item.ItemType.FIRE_EXTINGUISHER, 1);
    }

    public void clearInventory() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                slots[x][y] = null;
            }
        }
        gold = 0;
        System.out.println("🗑 Инвентарь очищен");
    }

    public void updateWeaponStatsForM53(int burstSize, int weaponAccuracy, double caliber, int maxAmmo, int reloadCost) {
        this.burstSize = burstSize;
        this.weaponAccuracy = weaponAccuracy;
        this.caliber = caliber;
        this.maxAmmo = maxAmmo;
        this.reloadCost = reloadCost;  // ← ЭТА СТРОКА КРИТИЧНА!
        this.currentAmmo = maxAmmo;
        System.out.println("🔫 M53 вооружён: калибр " + caliber + " м, обойма " + maxAmmo +
                ", перезарядка " + reloadCost + " о.х.");
    }

    public int getWeaponAccuracy() { return weaponAccuracy; }
    public double getCaliber() { return caliber; }
    public Item getEquippedWeapon() { return equippedWeapon; }
    public AmmoItem getCurrentAmmo() {
        return currentAmmoType;
    }
    public int getMaxAmmo() { return maxAmmo; }
    public int getReloadCost() { return reloadCost; }
    public int getBurstSize() { return burstSize; }
    public boolean isWeaponEmpty() { return currentAmmo == 0; }
    public boolean isWeaponFull() { return currentAmmo >= maxAmmo; }

    public boolean useAmmoFromWeapon(int amount) {
        System.out.println("=== useAmmoFromWeapon ===");
        System.out.println("  amount: " + amount);
        System.out.println("  currentAmmo: " + currentAmmo);
        System.out.println("  burstSize: " + burstSize);

        if (currentAmmo >= amount) {
            currentAmmo -= amount;
            System.out.println("  После выстрела осталось: " + currentAmmo + "/" + maxAmmo);
            return true;
        } else {
            System.out.println("  ❌ Недостаточно снарядов!");
            return false;
        }
    }



    // === МЕТОДЫ ДЛЯ СНАРЯДОВ С КАЛИБРОМ ===

    public int getAmmoCount(Caliber caliber) {
        int total = 0;
        System.out.println("=== getAmmoCount for " + caliber.name() + " ===");
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Item item = getItem(x, y);
                if (item != null && item.getType() == Item.ItemType.AMMO) {
                    if (item instanceof AmmoItem) {
                        AmmoItem ammoItem = (AmmoItem) item;
                        if (ammoItem.getCaliber() == caliber) {
                            total += item.getCount();
                            System.out.println("  Found " + item.getCount() + " of " + caliber.name() + " at [" + x + "," + y + "]");
                        }
                    }
                }
            }
        }
        System.out.println("  Total: " + total);
        return total;
    }

    public int reloadWeapon(Caliber requiredCaliber) {
        int neededAmmo = maxAmmo - currentAmmo;
        if (neededAmmo <= 0) {
            System.out.println("Оружие уже полностью заряжено!");
            return 0;
        }

        // ИЩЕМ СНАРЯДЫ В ИНВЕНТАРЕ
        List<AmmoItem> availableAmmo = new ArrayList<>();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Item item = getItem(x, y);
                if (item instanceof AmmoItem) {
                    AmmoItem ammo = (AmmoItem) item;
                    if (ammo.getCaliber() == requiredCaliber && ammo.getCount() > 0) {
                        availableAmmo.add(ammo);
                    }
                }
            }
        }

        if (availableAmmo.isEmpty()) {
            System.out.println("Нет снарядов калибра " + requiredCaliber.name + "!");
            return 0;
        }

        AmmoItem selectedAmmo = null;

        // Если только один тип - заряжаем его
        if (availableAmmo.size() == 1) {
            selectedAmmo = availableAmmo.get(0);
        } else {
            // Показываем диалог выбора
            String[] options = new String[availableAmmo.size()];
            for (int i = 0; i < availableAmmo.size(); i++) {
                AmmoItem ammo = availableAmmo.get(i);
                options[i] = (ammo.isImproved() ? "✨ УЛУЧШЕННЫЕ" : "📦 БАЗОВЫЕ") +
                        " " + ammo.getCaliber().name +
                        " (x" + ammo.getCount() + ")";
            }
            int choice = JOptionPane.showOptionDialog(null,
                    "Выберите тип снарядов для перезарядки:",
                    "Перезарядка",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            if (choice < 0) return 0; // Отмена
            selectedAmmo = availableAmmo.get(choice);
        }

        int ammoToReload = Math.min(neededAmmo, selectedAmmo.getCount());

        // ===== ВАЖНО: Удаляем ТОЛЬКО ВЫБРАННЫЙ ТИП снарядов! =====
        if (removeSpecificAmmoItem(selectedAmmo, ammoToReload)) {
            currentAmmo += ammoToReload;
            setCurrentAmmoType(selectedAmmo);
            String ammoType = selectedAmmo.isImproved() ? "УЛУЧШЕННЫЕ" : "БАЗОВЫЕ";
            System.out.println("🔄 Перезаряжено " + ammoToReload + " " + ammoType +
                    " снарядов калибра " + requiredCaliber.name);
            return ammoToReload;
        }
        return 0;
    }

    // НОВЫЙ МЕТОД: удаляет конкретный объект снаряда из инвентаря
    private boolean removeSpecificAmmoItem(AmmoItem targetAmmo, int amount) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Item item = getItem(x, y);
                if (item == targetAmmo) {
                    // Нашли нужный объект!
                    if (removeItem(x, y, amount)) {
                        System.out.println("  Удалено " + amount + " снарядов из выбранного стака");
                        return true;
                    }
                }
            }
        }
        System.err.println("❌ Не удалось найти выбранный стак снарядов!");
        return false;
    }

    public void setWeaponForFriendly(FriendlyUnit.WeaponData weapon, int maxAmmo, boolean preserveAmmo) {
        this.burstSize = weapon.burstSize;           // ← ВАЖНО: обновляем burstSize
        this.weaponAccuracy = weapon.weaponAccuracy;
        this.caliber = weapon.weaponCaliber;
        this.maxAmmo = maxAmmo;                      // ← ВАЖНО: обновляем maxAmmo
        this.reloadCost = weapon.reloadCost;

        if (!preserveAmmo) {
            this.currentAmmo = maxAmmo;              // ← ВАЖНО: обновляем currentAmmo
        }

        System.out.println("🔫 Обновлён инвентарь для оружия " + weapon.name +
                ": burstSize=" + this.burstSize +
                ", maxAmmo=" + this.maxAmmo +
                ", currentAmmo=" + this.currentAmmo);
    }

    public void setWeaponForFriendly(FriendlyUnit.WeaponData weapon, int maxAmmo) {
        setWeaponForFriendly(weapon, maxAmmo, false);
    }

    public void setCurrentAmmo(int ammo) {
        this.currentAmmo = Math.min(ammo, maxAmmo);
        System.out.println("🔫 Восстановлено снарядов: " + currentAmmo + "/" + maxAmmo);
    }

    private int getMaxAmmoForCaliber(Caliber caliber) {
        return caliber.getMaxStackSize();
    }

    public boolean removeAmmoByCaliber(Caliber caliber, int amount) {
        int remaining = amount;

        // Проходим по всем слотам инвентаря
        for (int y = 0; y < HEIGHT && remaining > 0; y++) {
            for (int x = 0; x < WIDTH && remaining > 0; x++) {
                Item item = getItem(x, y);
                if (item != null && item.getType() == Item.ItemType.AMMO) {
                    // Проверяем калибр
                    boolean caliberMatches = false;
                    if (item instanceof AmmoItem) {
                        if (((AmmoItem) item).getCaliber() == caliber) {
                            caliberMatches = true;
                        }
                    } else {
                        // Старые снаряды без калибра - считаем их 20мм
                        if (caliber == Caliber.CALIBER_20MM) {
                            caliberMatches = true;
                        }
                    }

                    if (caliberMatches) {
                        int toRemove = Math.min(remaining, item.getCount());
                        if (removeItem(x, y, toRemove)) {
                            remaining -= toRemove;
                            System.out.println("  Удалено " + toRemove + " снарядов калибра " + caliber.name +
                                    " из слота [" + x + "," + y + "], осталось удалить: " + remaining);
                        }
                    }
                }
            }
        }

        if (remaining > 0) {
            System.err.println("❌ Не удалось удалить " + remaining + " снарядов калибра " + caliber.name);
            return false;
        }

        System.out.println("✅ Успешно удалено " + amount + " снарядов калибра " + caliber.name);
        return true;
    }

    public void removeAllAmmo() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Item item = getItem(x, y);
                if (item != null && item.getType() == Item.ItemType.AMMO) {
                    removeItem(x, y, item.getCount());
                }
            }
        }
    }

    public void addAmmoItem(AmmoItem ammoItem) {
        if (ammoItem == null || ammoItem.getCount() <= 0) return;

        Caliber caliber = ammoItem.getCaliber();
        boolean isImproved = ammoItem.isImproved();
        int remainingCount = ammoItem.getCount();
        int maxStack = caliber.getMaxStackSize();

        System.out.println("📦 Добавление снарядов: калибр=" + caliber.name +
                ", улучшенные=" + isImproved +
                ", количество=" + remainingCount + ", макс. в стаке=" + maxStack);

        // Сначала пытаемся добавить в существующие стаки (только если они не полны)
        for (int y = 0; y < HEIGHT && remainingCount > 0; y++) {
            for (int x = 0; x < WIDTH && remainingCount > 0; x++) {
                Item item = getItem(x, y);
                if (item instanceof AmmoItem) {
                    AmmoItem existing = (AmmoItem) item;
                    if (existing.getCaliber() == caliber &&
                            existing.isImproved() == isImproved &&
                            !existing.isFull()) {
                        int canAdd = Math.min(remainingCount, existing.getMaxStackSize() - existing.getCount());
                        if (canAdd > 0) {
                            existing.addCount(canAdd);
                            remainingCount -= canAdd;
                            System.out.println("  Добавлено " + canAdd + " в существующий стак в [" + x + "," + y + "], осталось: " + remainingCount);
                        }
                    }
                }
            }
        }

        // Создаём новые стаки для оставшихся
        while (remainingCount > 0) {
            int[] emptySlot = getFirstFreeSlot();
            if (emptySlot == null) {
                System.err.println("❌ Нет места для " + remainingCount + " снарядов " + caliber.name);
                break;
            }

            int toAdd = Math.min(remainingCount, maxStack);
            AmmoItem newStack = new AmmoItem(caliber, toAdd, isImproved);
            slots[emptySlot[0]][emptySlot[1]] = newStack;
            remainingCount -= toAdd;
            System.out.println("  Создан новый стак с " + toAdd + " снарядами" +
                    (isImproved ? " (улучшенными)" : "") + " в слоте [" +
                    emptySlot[0] + "," + emptySlot[1] + "], осталось: " + remainingCount);
        }

        rebuildOccupied();
        System.out.println("✅ Добавление снарядов завершено. Не добавлено: " + remainingCount);
    }

    // Добавьте этот метод в класс Inventory
    // В классе Inventory.java добавьте метод:
    public double getTotalWeight() {
        double totalWeight = 0.0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Item item = slots[x][y];
                if (item != null && item.getCount() > 0) {
                    totalWeight += item.getTotalWeight();
                }
            }
        }
        return totalWeight;
    }

    // Добавьте метод для проверки, можно ли добавить предмет по весу
    public boolean canAddItemByWeight(Item item, double maxWeight) {
        double currentWeight = getTotalWeight();
        double newWeight = currentWeight + item.getTotalWeight();
        return newWeight <= maxWeight;
    }

    // === ОСНОВНЫЕ МЕТОДЫ ИНВЕНТАРЯ ===

    private void removeAmmo(Caliber caliber, int amount) {
        int remaining = amount;
        System.out.println("removeAmmo: нужно удалить " + amount + " снарядов калибра " + caliber.name);

        for (int y = 0; y < HEIGHT && remaining > 0; y++) {
            for (int x = 0; x < WIDTH && remaining > 0; x++) {
                Item item = getItem(x, y);
                if (item != null && item.getType() == Item.ItemType.AMMO) {
                    if (item instanceof AmmoItem) {
                        AmmoItem ammoItem = (AmmoItem) item;
                        if (ammoItem.getCaliber() == caliber) {
                            int toRemove = Math.min(remaining, ammoItem.getCount());
                            if (removeItem(x, y, toRemove)) {
                                remaining -= toRemove;
                                System.out.println("  Удалено " + toRemove + " из слота [" + x + "," + y +
                                        "], осталось удалить: " + remaining);
                            }
                        }
                    }
                    // Старые снаряды ИГНОРИРУЕМ
                }
            }
        }

        if (remaining > 0) {
            System.err.println("❌ Не хватает снарядов! Не удалось удалить " + remaining + " снарядов калибра " + caliber.name);
        } else {
            System.out.println("✅ Успешно удалено " + amount + " снарядов калибра " + caliber.name);
        }
    }

    public void convertOldAmmo() {
        boolean converted = false;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Item item = getItem(x, y);
                if (item != null && item.getType() == Item.ItemType.AMMO && !(item instanceof AmmoItem)) {
                    // Конвертируем старый снаряд в 20мм
                    int count = item.getCount();
                    removeItem(x, y, count);
                    AmmoItem newAmmo = new AmmoItem(Caliber.CALIBER_20MM, count);
                    addAmmoItem(newAmmo);
                    converted = true;
                    System.out.println("🔄 Конвертировано " + count + " старых снарядов в 20мм");
                }
            }
        }
        if (!converted) {
            System.out.println("Старых снарядов не найдено");
        }
    }

    public boolean canShoot() {
        return currentAmmo >= burstSize;
    }

    public boolean canPlaceItem(Item.ItemType type, int x, int y) {
        int width = type.width;
        int height = type.height;

        if (x < 0 || y < 0 || x + width > WIDTH || y + height > HEIGHT) return false;

        for (int dy = 0; dy < height; dy++) {
            for (int dx = 0; dx < width; dx++) {
                int checkX = x + dx;
                int checkY = y + dy;
                if (occupied[checkX][checkY]) {
                    return false;
                }
            }
        }
        return true;
    }


    public void updateWeaponStatsForMS1(int burstSize, int weaponAccuracy, double caliber, int maxAmmo, int reloadCost) {
        this.burstSize = burstSize;
        this.weaponAccuracy = weaponAccuracy;
        this.caliber = caliber;
        this.maxAmmo = maxAmmo;
        this.reloadCost = reloadCost;
        this.currentAmmo = maxAmmo;
        // НЕ трогайте this.weapon - он должен остаться null!
        System.out.println("🔫 MS-1 вооружён: калибр " + caliber + " м, обойма " + maxAmmo +
                ", перезарядка " + reloadCost + " о.х.");
    }


    public boolean placeItem(Item item, int x, int y) {
        // Для предметов размером 1x1 можно использовать упрощённую логику
        if (item.getWidth() == 1 && item.getHeight() == 1) {
            if (slots[x][y] != null) {
                System.err.println("  ❌ Слот [" + x + "," + y + "] уже занят!");
                return false;
            }
            slots[x][y] = item;
            occupied[x][y] = true;
            System.out.println("  ✅ Предмет размещён в слоте [" + x + "," + y + "]");
            return true;
        }

        // Для больших предметов используем полную проверку
        if (!canPlaceItem(item.getType(), x, y)) return false;

        for (int dy = 0; dy < item.getHeight(); dy++) {
            for (int dx = 0; dx < item.getWidth(); dx++) {
                int targetX = x + dx;
                int targetY = y + dy;
                if (dx == 0 && dy == 0) {
                    slots[targetX][targetY] = item;
                } else {
                    slots[targetX][targetY] = new Item(item.getType(), 0);
                }
            }
        }
        rebuildOccupied();
        return true;
    }

    public Item getItem(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) return null;
        return slots[x][y];  // Просто возвращаем то, что есть (может быть null)
    }

    private int[] findRootItem(int x, int y) {
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                int checkX = x + dx;
                int checkY = y + dy;
                if (checkX >= 0 && checkX < WIDTH && checkY >= 0 && checkY < HEIGHT) {
                    Item item = slots[checkX][checkY];
                    if (item != null && item.getCount() > 0) {
                        if (checkX <= x && x < checkX + item.getWidth() && checkY <= y && y < checkY + item.getHeight()) {
                            return new int[]{checkX, checkY};
                        }
                    }
                }
            }
        }
        return null;
    }

    // Добавьте этот метод в Inventory.java

    public void setPlayerWeapon(PlayerTank.WeaponData weapon, int maxAmmo) {
        this.burstSize = weapon.burstSize;
        this.weaponAccuracy = weapon.weaponAccuracy;
        this.caliber = weapon.weaponCaliber;
        this.maxAmmo = maxAmmo;
        this.reloadCost = weapon.reloadCost;
        this.currentAmmo = maxAmmo;
        System.out.println("🔫 Игрок экипировал " + weapon.name +
                ": maxAmmo=" + maxAmmo + ", reloadCost=" + reloadCost +
                ", caliber=" + caliber);
    }

    public boolean removeItem(int x, int y, int amount) {
        int[] root = findRootItem(x, y);
        if (root != null) {
            x = root[0];
            y = root[1];
        }
        Item item = slots[x][y];
        if (item == null || item.getCount() == 0) return false;

        // ===== ДЛЯ MedicalItem: проверяем, не истощён ли =====
        if (item instanceof MedicalItem && ((MedicalItem) item).isDepleted()) {
            // Очищаем все клетки, которые занимал предмет
            int width = item.getWidth();
            int height = item.getHeight();
            for (int dy = 0; dy < height; dy++) {
                for (int dx = 0; dx < width; dx++) {
                    int targetX = x + dx;
                    int targetY = y + dy;
                    if (targetX < WIDTH && targetY < HEIGHT) {
                        slots[targetX][targetY] = null;
                        occupied[targetX][targetY] = false;
                    }
                }
            }
            rebuildOccupied();
            System.out.println("  🗑 Удалён истощённый MedicalItem из [" + x + "," + y + "]");
            return true;
        }

        if (item.removeCount(amount)) {
            if (item.isEmpty()) {
                int width = item.getWidth();
                int height = item.getHeight();
                for (int dy = 0; dy < height; dy++) {
                    for (int dx = 0; dx < width; dx++) {
                        int targetX = x + dx;
                        int targetY = y + dy;
                        if (targetX < WIDTH && targetY < HEIGHT) {
                            slots[targetX][targetY] = null;
                            occupied[targetX][targetY] = false;
                        }
                    }
                }
                rebuildOccupied();
                System.out.println("  🗑 Предмет удалён из [" + x + "," + y + "] (освобождено " + width + "x" + height + " клеток)");
                return true;
            }
            return true;
        }
        return false;
    }

    public void clearSlot(int x, int y) {
        Item item = slots[x][y];
        if (item != null) {
            int width = item.getWidth();
            int height = item.getHeight();
            for (int dy = 0; dy < height; dy++) {
                for (int dx = 0; dx < width; dx++) {
                    int targetX = x + dx;
                    int targetY = y + dy;
                    if (targetX < WIDTH && targetY < HEIGHT) {
                        slots[targetX][targetY] = null;
                        occupied[targetX][targetY] = false;
                    }
                }
            }
        }
    }

    public void forceClearSlot(int x, int y) {
        Item item = slots[x][y];
        if (item != null) {
            int width = item.getWidth();
            int height = item.getHeight();
            for (int dy = 0; dy < height; dy++) {
                for (int dx = 0; dx < width; dx++) {
                    int targetX = x + dx;
                    int targetY = y + dy;
                    if (targetX < WIDTH && targetY < HEIGHT) {
                        slots[targetX][targetY] = null;
                        occupied[targetX][targetY] = false;
                    }
                }
            }
            rebuildOccupied();
            System.out.println("  🗑 Принудительно очищен слот [" + x + "," + y + "]");
        }
    }

    public boolean moveItem(int fromX, int fromY, int toX, int toY) {
        int[] fromRoot = findRootItem(fromX, fromY);
        if (fromRoot == null) return false;
        int realFromX = fromRoot[0];
        int realFromY = fromRoot[1];
        Item fromItem = slots[realFromX][realFromY];
        if (fromItem == null || fromItem.getCount() == 0) return false;
        if (!canPlaceItem(fromItem.getType(), toX, toY)) return false;

        Item.ItemType type = fromItem.getType();
        int count = fromItem.getCount();
        removeItem(realFromX, realFromY, count);

        Item newItem;
        if (fromItem instanceof KeyItem) {
            KeyItem oldKey = (KeyItem) fromItem;
            newItem = new KeyItem(oldKey.getColor(), count);
        } else if (fromItem instanceof AmmoItem) {
            AmmoItem oldAmmo = (AmmoItem) fromItem;
            newItem = new AmmoItem(oldAmmo.getCaliber(), count, oldAmmo.isImproved());
        } else {
            newItem = new Item(type, count);
        }

        boolean placed = placeItem(newItem, toX, toY);
        if (!placed) {
            // Возвращаем обратно
            if (fromItem instanceof KeyItem) {
                KeyItem oldKey = (KeyItem) fromItem;
                placeItem(new KeyItem(oldKey.getColor(), count), realFromX, realFromY);
            } else if (fromItem instanceof AmmoItem) {
                AmmoItem oldAmmo = (AmmoItem) fromItem;
                placeItem(new AmmoItem(oldAmmo.getCaliber(), count, oldAmmo.isImproved()), realFromX, realFromY);
            } else {
                placeItem(new Item(type, count), realFromX, realFromY);
            }
            return false;
        }
        return true;
    }

    public void addItem(Item.ItemType type, int amount) {
        System.out.println("=== addItem ===");
        System.out.println("  Добавляем тип: " + type.name());
        System.out.println("  Это WEAPON? " + (type == Item.ItemType.WEAPON));
        System.out.println("  Это WEAPON_8MM? " + (type == Item.ItemType.WEAPON_8MM));
        System.out.println("  Это WEAPON_30MM? " + (type == Item.ItemType.WEAPON_30MM));
        System.out.println("  Это WEAPON_13MM (JAPAN)? " + (type == Item.ItemType.WEAPON_13MM_JAPAN));
        System.out.println("  Это WEAPON_13MM (FRENCH)? " + (type == Item.ItemType.WEAPON_13MM_FRENCH));
        System.out.println("  Это WEAPON_25MM? " + (type == Item.ItemType.WEAPON_25MM));
        System.out.println("  Это WEAPON_45MM? " + (type == Item.ItemType.WEAPON_45MM));
        System.out.println("  Это WEAPON_37MM (ITALIAN)? " + (type == Item.ItemType.WEAPON_37MM_ITALIAN));
        System.out.println("  Это WEAPON_37MM (AMERICAN)? " + (type == Item.ItemType.WEAPON_37MM_AMERICAN));
        System.out.println("  Это WEAPON_37MM (SWEDEN)? " + (type == Item.ItemType.WEAPON_37MM_SWEDEN));
        System.out.println("  Это WEAPON_47MM (FRENCH)? " + (type == Item.ItemType.WEAPON_47MM_FRENCH));
        System.out.println("  Это WEAPON_47MM (ITALIAN)? " + (type == Item.ItemType.WEAPON_47MM_ITALIAN));
        System.out.println("  Это WEAPON_76MM? " + (type == Item.ItemType.WEAPON_76MM));
        System.out.println("  Это WEAPON_105MM? " + (type == Item.ItemType.WEAPON_105MM));
        System.out.println("  Это WEAPON_128MM? " + (type == Item.ItemType.WEAPON_128MM));
        System.out.println("  Это WEAPON_203MM? " + (type == Item.ItemType.WEAPON_203MM));
        Item newItem = new Item(type, amount);
        addItemToInventory(newItem);
    }

    public void updateWeaponStatsForFriendly(FriendlyUnit.WeaponData weapon) {
        this.burstSize = weapon.burstSize;
        this.weaponAccuracy = weapon.weaponAccuracy;
        this.caliber = weapon.weaponCaliber;
        this.maxAmmo = weapon.caliber == Caliber.CALIBER_203MM ? 1 : 12;
        this.reloadCost = weapon.reloadCost;
        this.currentAmmo = this.maxAmmo;
    }

    public double getItemWeight(Item item) {
        if (item == null) return 0;
        return item.getTotalWeight();
    }

    // Также метод для проверки, может ли юнит подобрать предмет по весу и ОХ
    public boolean canPickupItem(Item item, int currentMovePoints, double maxCarryWeight, double currentWeight) {
        double itemWeight = item.getTotalWeight();

        // Проверка по грузоподъёмности
        if (currentWeight + itemWeight > maxCarryWeight) {
            return false;
        }

        // Проверка по очкам хода (1 кг = 1 ОХ)
        int requiredPoints = (int)Math.ceil(itemWeight);
        return currentMovePoints >= requiredPoints;
    }

    // В классе Inventory.java добавьте:



    public boolean addItemToInventory(Item item) {



        System.out.println("=== addItemToInventory ===");
        System.out.println("  Добавляем: " + item.getType().name() + " x" + item.getCount());
        System.out.println("  Размер предмета: " + item.getWidth() + "x" + item.getHeight());

        if (item == null || item.getCount() <= 0) {
            System.out.println("  ❌ Предмет пустой или count=0!");
            return false;
        }

        // ===== КЛЮЧИ - не стакаются, но могут быть несколько в одном слоте =====
        if (item instanceof KeyItem) {
            KeyItem newKey = (KeyItem) item;
            // Ищем существующий ключ такого же цвета
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    Item existing = getItem(x, y);
                    if (existing instanceof KeyItem) {
                        KeyItem existingKey = (KeyItem) existing;
                        if (existingKey.getColor() == newKey.getColor() && !existingKey.isFull()) {
                            int canAdd = Math.min(newKey.getCount(),
                                    existingKey.getMaxStackSize() - existingKey.getCount());
                            if (canAdd > 0) {
                                existingKey.addCount(canAdd);
                                newKey.removeCount(canAdd);
                                if (newKey.getCount() == 0) return true;
                            }
                        }
                    }
                }
            }
        }

        // ===== ДЛЯ НЕ-СТАКАЮЩИХСЯ ПРЕДМЕТОВ (лекарства, гранаты и т.д.) =====
        if (!item.canStack()) {
            System.out.println("  Предмет НЕ стакается, ищем пустой слот...");
            int[] emptySlot = findEmptySlotForType(item.getType());
            if (emptySlot != null) {
                boolean result = placeItem(item, emptySlot[0], emptySlot[1]);
                rebuildOccupied();
                return result;
            }
            return false;
        }

        // Если это AmmoItem, пытаемся сложить с существующими AmmoItem того же калибра
        if (item instanceof AmmoItem) {
            AmmoItem newAmmo = (AmmoItem) item;
            Caliber caliber = newAmmo.getCaliber();
            boolean isImproved = newAmmo.isImproved();

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    Item existing = getItem(x, y);
                    if (existing instanceof AmmoItem) {
                        AmmoItem existingAmmo = (AmmoItem) existing;
                        if (existingAmmo.getCaliber() == caliber && !existingAmmo.isFull() && existingAmmo.isImproved() == isImproved) {
                            int canAdd = Math.min(newAmmo.getCount(),
                                    existingAmmo.getMaxStackSize() - existingAmmo.getCount());
                            if (canAdd > 0) {
                                existingAmmo.addCount(canAdd);
                                newAmmo.removeCount(canAdd);
                                if (newAmmo.getCount() == 0) return true;
                            }
                        }
                    }
                }
            }
        } else if (item.getType().canStack) {
            // Для обычных стакающихся предметов (например, ENERGY_DRINK)
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    Item existing = getItem(x, y);
                    if (existing != null && existing.getType() == item.getType() && !existing.isFull()) {
                        int canAdd = Math.min(item.getCount(), existing.getMaxStackSize() - existing.getCount());
                        if (canAdd > 0) {
                            existing.addCount(canAdd);
                            item.removeCount(canAdd);
                            if (item.getCount() == 0) return true;
                        }
                    }
                }
            }
        }

        // Если не удалось сложить, ищем пустой слот
        int[] emptySlot = findEmptySlotForType(item.getType());
        if (emptySlot != null) {
            System.out.println("  Найден пустой слот: [" + emptySlot[0] + "," + emptySlot[1] + "]");
            boolean result = placeItem(item, emptySlot[0], emptySlot[1]);
            rebuildOccupied();
            return result;
        } else {
            System.out.println("  НЕТ СВОБОДНОГО МЕСТА ДЛЯ ПРЕДМЕТА РАЗМЕРОМ " +
                    item.getWidth() + "x" + item.getHeight());
            return false;
        }
    }

    // При добавлении оружия, ищем свободное место 2x2
    public int[] findEmptySlotForType(Item.ItemType type) {
        int width = type.width;   // для AMMO это 1
        int height = type.height; // для AMMO это 1

        System.out.println("=== Поиск места для " + type.name() + " (" + width + "x" + height + ") ===");

        // Показываем текущую карту занятости
        for (int y = 0; y < HEIGHT; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < WIDTH; x++) {
                if (slots[x][y] != null) {
                    row.append("█ ");
                } else {
                    row.append(". ");
                }
            }
            System.out.println("  Строка " + y + ": " + row);
        }

        for (int y = 0; y <= HEIGHT - height; y++) {
            for (int x = 0; x <= WIDTH - width; x++) {
                boolean allEmpty = true;
                for (int dy = 0; dy < height; dy++) {
                    for (int dx = 0; dx < width; dx++) {
                        int checkX = x + dx;
                        int checkY = y + dy;
                        if (slots[checkX][checkY] != null) {
                            allEmpty = false;
                            break;
                        }
                    }
                    if (!allEmpty) break;
                }
                if (allEmpty) {
                    System.out.println("  ✅ Найдено свободное место: [" + x + "," + y + "]");
                    return new int[]{x, y};
                }
            }
        }
        System.out.println("  ❌ Нет свободного места для " + type.name());
        return null;
    }



    public int[] getFirstFreeSlot() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (slots[x][y] == null) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    public boolean useItem(Item.ItemType type) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Item item = getItem(x, y);
                if (item != null && item.getType() == type && item.getCount() > 0) {
                    return removeItem(x, y, 1);
                }
            }
        }
        return false;
    }

    public int getItemCount(Item.ItemType type) {
        int total = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Item item = getItem(x, y);
                if (item != null && item.getType() == type) {
                    total += item.getCount();
                }
            }
        }
        return total;
    }

    public int getGold() { return gold; }
    public void addGold(int amount) { gold += amount; }
    public boolean removeGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    public void restoreAmmo(int currentAmmo, int maxAmmo, int reloadCost, int burstSize) {
        this.currentAmmo = currentAmmo;
        this.maxAmmo = maxAmmo;
        this.reloadCost = reloadCost;
        this.burstSize = burstSize;

        // ===== ВАЖНО: При загрузке сохранения, тип снарядов нужно восстановить =====
        // currentAmmoType будет восстановлен отдельно через setCurrentAmmoType()
    }

    public boolean useMedkit() { return useItem(Item.ItemType.MEDKIT); }
    public boolean useRepairKit() { return useItem(Item.ItemType.REPAIR_KIT); }
    public boolean useFireExtinguisher() { return useItem(Item.ItemType.FIRE_EXTINGUISHER); }

    public boolean useAmmo(int amount) {
        for (int i = 0; i < amount; i++) {
            if (!useItem(Item.ItemType.AMMO)) return false;
        }
        return true;
    }

    public void addLegacyItem(String oldType, int amount) {
        if (oldType.equals("ammo")) addItem(Item.ItemType.AMMO, amount);
        else if (oldType.equals("medkit")) addItem(Item.ItemType.MEDKIT, amount);
        else if (oldType.equals("repair_kit")) addItem(Item.ItemType.REPAIR_KIT, amount);
        else if (oldType.equals("fire_extinguisher")) addItem(Item.ItemType.FIRE_EXTINGUISHER, amount);
    }

    public void updateUnitStats(PlayerTank player) {
        this.currentAmmo = player.getInventory().getCurrentAmmoCount();
        this.maxAmmo = player.getInventory().getMaxAmmo();
    }

    public AmmoItem getAmmoItemByCaliber(Caliber caliber) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Item item = getItem(x, y);
                if (item instanceof AmmoItem) {
                    AmmoItem ammo = (AmmoItem) item;
                    if (ammo.getCaliber() == caliber && ammo.getCount() > 0) {
                        return ammo;
                    }
                }
            }
        }
        return null;
    }



    // Добавьте геттер и сеттер:
    public void setCurrentAmmoType(AmmoItem ammo) { this.currentAmmoType = ammo; }
    public int getCurrentAmmoCount() {
        return currentAmmo;  // текущее количество снарядов в оружии
    }
}