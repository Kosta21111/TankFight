package save;

import entities.*;
import ui.GamePanel;
import inventory.*;
import combat.*;
import inventory.Item;
import world.GameWorld;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;        // ← ДОБАВИТЬ
import java.util.Map;         // ← ДОБАВИТЬ
import java.util.HashMap;     // ← ДОБАВИТЬ

public class GameSave implements Serializable {
    private static final long serialVersionUID = 1L;
    private int silver;  // Общее серебро команды
    private int parts;

    private java.util.List<StorageChestSaveData> storageChests;
    private java.util.List<GarbageContainerSaveData> garbageContainers;
    private java.util.List<IronFloorSaveData> ironFloors;

    private transient GamePanel gamePanel;  // transient - чтобы не сериализовать
    private static final String SAVE_DIR = "src/saves/";
    private static final String SCREENSHOT_DIR = SAVE_DIR + "screenshots/";
    public static final int MAX_SAVES = 9;
    public static final int THUMBNAIL_WIDTH = 120;
    public static final int THUMBNAIL_HEIGHT = 90;

    // Данные игрока
    private int playerX, playerY;
    private int playerHealth;
    private int playerStrength;
    private int playerAgility;
    private int playerMovePoints;
    private int playerMaxMovePoints;
    private boolean playerOnHill;
    private int playerHillBonus;

    // В начале класса GameSave, после других полей, добавьте:
    private int playerMaxHealth;
    private int playerArmor;
    private int playerCritBonus;
    private int playerVision;
    private int playerViewRadius;
    private int playerReloadBonus;
    private int playerNimble;
    private double playerDodgeChance;

    // Характеристики оружия
    private int playerWeaponAccuracy;
    private int playerWeaponDamage;
    private double playerCritChance;
    private int playerShotCost;
    private int playerAimedShotCost;
    private double playerWeaponCaliber;
    private int playerBurstSize;
    private int playerReloadCost;
    private int playerCurrentAmmo;
    private int playerMaxAmmo;

    // Данные врагов
    private java.util.List<EnemySaveData> enemies;

    // Данные стен
    private java.util.List<WallSaveData> walls;

    // Данные деревьев
    private java.util.List<TreeSaveData> trees;

    // Данные NPC
    private java.util.List<NPCSaveData> npcs;

    // Данные асфальта
    private java.util.List<PavementSaveData> pavements;

    // Данные досок
    private java.util.List<OakPlanksSaveData> oakPlanks;

    private java.util.List<WaterSaveData> waters;
    private java.util.List<DoorSaveData> doors;


    // Данные союзников
    private java.util.List<FriendlySaveData> friendlies;

    // ===== ДОБАВЛЯЕМ ДАННЫЕ ДРОПА =====
    private java.util.List<LootDropSaveData> lootDrops;
    private java.util.List<TraderSaveData> traders;

    // Инвентарь
    private InventorySaveData inventory;

    // Прогресс квеста
    private int totalEnemiesKilled;

    // Статистика
    private Date saveDate;
    private String saveName;
    private int slotNumber;


    private int level;
    private int experience;

    private int playerLevel;
    private int playerExperience;

    private int playerUpgradeLevel;
    private String playerUpgradeClass;

    private String currentLevelPath;


    // В начале класса GameSave, после других полей:
    private int playerBaseAccuracy;
    private int playerWeaponWeight;
    private int playerOriginalReloadCost;
    private String playerEquippedWeaponId;

    public GameSave() {
        this.saveDate = new Date();
    }

    // Сохранение в конкретный слот со скриншотом
    public static void saveToSlot(GameWorld world, PlayerTank player, int slotNumber,
                                  BufferedImage screenshot, String levelPath) {
        String slotName = "slot_" + slotNumber;

        GameSave save = new GameSave();
        save.saveName = slotName;
        save.slotNumber = slotNumber;
        save.saveDate = new Date();

        save.playerX = player.gridX;
        save.playerY = player.gridY;
        save.playerHealth = player.health;
        save.playerMaxHealth = player.maxHealth;        // ← ДОБАВИТЬ
        save.playerStrength = player.strength;
        save.playerAgility = player.agility;
        save.playerArmor = player.armor;                // ← ДОБАВИТЬ
        save.playerCritBonus = player.critBonus;        // ← ДОБАВИТЬ
        save.playerVision = player.vision;              // ← ДОБАВИТЬ
        save.playerViewRadius = player.viewRadius;      // ← ДОБАВИТЬ
        save.playerReloadBonus = player.reloadBonus;    // ← ДОБАВИТЬ
        save.playerNimble = player.nimble;              // ← ДОБАВИТЬ
        save.playerDodgeChance = player.dodgeChance;    // ← ДОБАВИТЬ
        save.playerMovePoints = player.movePoints;
        save.playerMaxMovePoints = player.maxMovePoints;
        save.playerOnHill = player.isOnHill;
        save.playerHillBonus = player.currentHillBonus;
        save.silver = player.getSilver();
        save.parts = player.getParts();

        save.playerUpgradeLevel = player.getUpgradeLevel();
        save.playerUpgradeClass = player.getUpgradeClass();

        // ===== ДОБАВЬТЕ ХАРАКТЕРИСТИКИ ОРУЖИЯ =====
        save.playerWeaponAccuracy = player.weaponAccuracy;
        save.playerWeaponDamage = player.weaponDamage;
        save.playerCritChance = player.critChance;
        save.playerShotCost = player.shotCost;
        save.playerAimedShotCost = player.aimedShotCost;
        save.playerWeaponCaliber = player.weaponCaliber;
        save.playerBurstSize = player.burstSize;
        save.playerReloadCost = player.reloadCost;
        save.playerCurrentAmmo = player.getInventory().getCurrentAmmoCount();
        save.playerMaxAmmo = player.getInventory().getMaxAmmo();

        // ===== ДОБАВЬТЕ ЭТИ СТРОКИ =====
        save.playerEquippedWeaponId = player.getEquippedWeaponId();
        save.playerBaseAccuracy = player.baseAccuracy;
        save.playerWeaponWeight = player.weaponWeight;
        save.playerOriginalReloadCost = player.getOriginalReloadCost();

        if (player.getExperienceSystem() != null) {
            save.playerLevel = player.getExperienceSystem().getLevel();
            save.playerExperience = player.getExperienceSystem().getExperience();
        } else {
            save.playerLevel = 1;
            save.playerExperience = 0;
        }

        save.inventory = new InventorySaveData(player.getInventory(), player.getSilver());

        // Сохраняем врагов
        save.enemies = new ArrayList<>();
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isAlive) {
                save.enemies.add(new EnemySaveData(enemy));
            }
        }

        // Сохраняем стены
        save.walls = new ArrayList<>();
        for (Wall wall : world.getWalls()) {
            if (wall.isAlive()) {
                save.walls.add(new WallSaveData(wall));
            }
        }

        // Сохраняем деревья
        save.trees = new ArrayList<>();
        for (Tree tree : world.getTrees()) {
            if (tree.isAlive) {
                save.trees.add(new TreeSaveData(tree));
            }
        }

        // Сохраняем NPC
        save.npcs = new ArrayList<>();
        for (QuestNPC npc : world.getQuestNPCs()) {
            save.npcs.add(new NPCSaveData(npc));
        }

        // Сохраняем асфальт
        save.pavements = new ArrayList<>();
        for (Pavement pavement : world.getPavements()) {
            save.pavements.add(new PavementSaveData(pavement));
        }

        // Сохраняем дубовые доски
        save.oakPlanks = new ArrayList<>();
        for (OakPlanks plank : world.getOakPlanks()) {
            save.oakPlanks.add(new OakPlanksSaveData(plank));
        }

        save.ironFloors = new ArrayList<>();
        for (IronFloor floor : world.getIronFloors()) {
            save.ironFloors.add(new IronFloorSaveData(floor));
        }

        save.waters = new ArrayList<>();
        for (Water water : world.getWaters()) {
            if (water.isAlive) {
                save.waters.add(new WaterSaveData(water));
            }
        }

        save.doors = new ArrayList<>();
        for (Door door : world.getDoors()) {
            if (door.isAlive) {
                save.doors.add(new DoorSaveData(door));
            }
        }
        System.out.println("Сохранено дверей: " + save.doors.size());

        // Сохраняем союзников
        save.friendlies = new ArrayList<>();
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isAlive) {
                save.friendlies.add(new FriendlySaveData(friendly));
            }
        }

        save.storageChests = new ArrayList<>();
        for (StorageChest chest : world.getStorageChests()) {
            if (chest.wasModified() || chest.isEmpty()) {
                // Сохраняем изменённые или пустые сундуки
                save.storageChests.add(new StorageChestSaveData(chest));
            }
        }

        save.garbageContainers = new ArrayList<>();
        for (GarbageContainer container : world.getGarbageContainers()) {
            if (container.wasModified() || container.isLooted || container.isEmpty()) {
                save.garbageContainers.add(new GarbageContainerSaveData(container));
            }
        }

        // ===== СОХРАНЯЕМ ТОРГОВЦЕВ =====
        save.traders = new ArrayList<>();
        for (Trader trader : world.getTraders()) {
            // Сохраняем ВСЕХ торговцев, чтобы они не исчезали при загрузке
            save.traders.add(new TraderSaveData(trader));
            System.out.println("✅ Сохранён торговец: " + trader.name +
                    " с " + trader.getOffers().size() + " товарами");
        }

        // ===== СОХРАНЯЕМ ДРОП =====
        save.lootDrops = new ArrayList<>();
        for (LootDrop drop : world.getLootDrops()) {
            if (drop.isAlive && !drop.isEmpty()) {
                save.lootDrops.add(new LootDropSaveData(drop));
            }
        }

        save.currentLevelPath = levelPath;
        System.out.println("✅ Сохранён путь к уровню: " + save.currentLevelPath);

        save.inventory = new InventorySaveData(player.getInventory(), player.getSilver());
        save.totalEnemiesKilled = world.getTotalEnemiesKilled();

        try {
            File dir = new File(SAVE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Сохраняем данные игры
            String fileName = SAVE_DIR + slotName + ".sav";
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(save);
            oos.close();
            fos.close();

            // Сохраняем скриншот
            if (screenshot != null) {
                saveScreenshot(slotNumber, screenshot);
            }

            System.out.println("Игра сохранена в слот " + slotNumber + ": " + fileName);
            System.out.println("  Сохранено дропов: " + save.lootDrops.size());
        } catch (IOException e) {
            System.err.println("Ошибка сохранения игры: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class StorageChestSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;
        private Map<String, Integer> items;
        private Map<String, Integer> ammo;
        private Map<String, Integer> keys;

        StorageChestSaveData(StorageChest chest) {
            this.x = chest.gridX;
            this.y = chest.gridY;
            this.items = new HashMap<>();
            this.ammo = new HashMap<>();
            this.keys = new HashMap<>();  // ← ДОБАВЬТЕ ЭТУ СТРОКУ!

            for (Map.Entry<Item.ItemType, Integer> entry : chest.getItems().entrySet()) {
                items.put(entry.getKey().name(), entry.getValue());
            }
            for (Map.Entry<Caliber, Integer> entry : chest.getAmmo().entrySet()) {
                String caliberName = entry.getKey().name().replace("CALIBER_", "");
                ammo.put(caliberName, entry.getValue());
            }
            for (Map.Entry<Door.DoorColor, Integer> entry : chest.getKeys().entrySet()) {
                keys.put(entry.getKey().name(), entry.getValue());
            }
        }

        StorageChest toStorageChest() {
            StorageChest chest = new StorageChest(x, y);
            for (Map.Entry<String, Integer> entry : items.entrySet()) {
                try {
                    Item.ItemType type = Item.ItemType.valueOf(entry.getKey());
                    chest.addItem(type, entry.getValue());
                } catch (IllegalArgumentException e) {
                    System.err.println("Неизвестный тип: " + entry.getKey());
                }
            }
            for (Map.Entry<String, Integer> entry : ammo.entrySet()) {
                try {
                    Caliber caliber = Caliber.valueOf("CALIBER_" + entry.getKey());
                    chest.addAmmo(caliber, entry.getValue());
                } catch (IllegalArgumentException e) {
                    System.err.println("Неизвестный калибр: " + entry.getKey());
                }
            }

            // ===== ВОССТАНАВЛИВАЕМ КЛЮЧИ =====
            if (keys != null) {  // ← ДОБАВЬТЕ ЭТУ ПРОВЕРКУ!
                for (Map.Entry<String, Integer> entry : keys.entrySet()) {
                    try {
                        Door.DoorColor color = Door.DoorColor.valueOf(entry.getKey());
                        chest.addKey(color, entry.getValue());
                        System.out.println("  ✅ Восстановлен ключ: " + entry.getKey() + " x" + entry.getValue());
                    } catch (IllegalArgumentException e) {
                        System.err.println("Неизвестный цвет ключа: " + entry.getKey());
                    }
                }
            }
            return chest;
        }
    }

    static class GarbageContainerSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;
        private boolean isLooted;
        private Map<String, Integer> items;
        private Map<String, Integer> ammo;

        GarbageContainerSaveData(GarbageContainer container) {
            this.x = container.gridX;
            this.y = container.gridY;
            this.isLooted = container.isLooted;
            this.items = new HashMap<>();
            this.ammo = new HashMap<>();

            for (Map.Entry<Item.ItemType, Integer> entry : container.getItems().entrySet()) {
                items.put(entry.getKey().name(), entry.getValue());
            }
            for (Map.Entry<Caliber, Integer> entry : container.getAmmo().entrySet()) {
                String caliberName = entry.getKey().name().replace("CALIBER_", "");
                ammo.put(caliberName, entry.getValue());
            }
        }

        GarbageContainer toGarbageContainer() {
            GarbageContainer container = new GarbageContainer(x, y);
            container.isLooted = isLooted;
            for (Map.Entry<String, Integer> entry : items.entrySet()) {
                try {
                    Item.ItemType type = Item.ItemType.valueOf(entry.getKey());
                    container.addItem(type, entry.getValue());
                } catch (IllegalArgumentException e) {
                    System.err.println("Неизвестный тип: " + entry.getKey());
                }
            }
            for (Map.Entry<String, Integer> entry : ammo.entrySet()) {
                try {
                    Caliber caliber = Caliber.valueOf("CALIBER_" + entry.getKey());
                    container.addAmmo(caliber, entry.getValue());
                } catch (IllegalArgumentException e) {
                    System.err.println("Неизвестный калибр: " + entry.getKey());
                }
            }
            return container;
        }
    }

    static class TraderSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;
        private String name;
        private List<TradeOfferSaveData> offers;

        TraderSaveData(Trader trader) {
            this.x = trader.gridX;
            this.y = trader.gridY;
            this.name = trader.name;
            this.offers = new ArrayList<>();
            for (Trader.TradeOffer offer : trader.getOffers()) {
                offers.add(new TradeOfferSaveData(offer));
            }
        }

        Trader toTrader() {
            Trader trader = new Trader(x, y, name);

            // Восстанавливаем оставшееся количество товаров
            for (TradeOfferSaveData savedOffer : offers) {
                for (Trader.TradeOffer currentOffer : trader.getOffers()) {
                    if (savedOffer.itemName.equals(currentOffer.itemName)) {
                        currentOffer.quantity = savedOffer.quantity;
                        System.out.println("  Восстановлен товар: " + savedOffer.itemName +
                                ", осталось: " + savedOffer.quantity + " стаков");
                        break;
                    }
                }
            }

            return trader;
        }
    }

    static class TradeOfferSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String itemName;
        private Item.ItemType itemType;
        private String caliberName;  // для снарядов
        private int pricePerStack;
        private int stackSize;
        private int quantity;
        private boolean isImproved;

        TradeOfferSaveData(Trader.TradeOffer offer) {
            this.itemName = offer.itemName;
            this.itemType = offer.itemType;
            if (offer.caliber != null) {
                this.caliberName = offer.caliber.name().replace("CALIBER_", "");
            }
            this.pricePerStack = offer.pricePerStack;
            this.stackSize = offer.stackSize;
            this.quantity = offer.quantity;
            this.isImproved = offer.isImproved;
        }
    }

    // Сохранение скриншота
    private static void saveScreenshot(int slotNumber, BufferedImage screenshot) {
        try {
            File screenshotDir = new File(SCREENSHOT_DIR);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            // Создаём миниатюру
            BufferedImage thumbnail = new BufferedImage(
                    THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = thumbnail.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(screenshot, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, null);
            g.dispose();

            // Сохраняем миниатюру
            String screenshotPath = SCREENSHOT_DIR + "slot_" + slotNumber + ".png";
            ImageIO.write(thumbnail, "png", new File(screenshotPath));

            System.out.println("Скриншот сохранён: " + screenshotPath);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения скриншота: " + e.getMessage());
        }
    }

    // Загрузка скриншота для слота
    public static BufferedImage loadScreenshot(int slotNumber) {
        String screenshotPath = SCREENSHOT_DIR + "slot_" + slotNumber + ".png";
        File screenshotFile = new File(screenshotPath);

        if (screenshotFile.exists()) {
            try {
                return ImageIO.read(screenshotFile);
            } catch (IOException e) {
                System.err.println("Ошибка загрузки скриншота: " + e.getMessage());
            }
        }
        return null;
    }

    // Загрузка из слота
    public static GameSave loadFromSlot(int slotNumber) {
        String slotName = "slot_" + slotNumber;
        String fileName = SAVE_DIR + slotName + ".sav";
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            GameSave save = (GameSave) ois.readObject();
            ois.close();
            fis.close();
            return save;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка загрузки игры из слота " + slotNumber + ": " + e.getMessage());
            return null;
        }
    }

    // Проверка существования слота
    public static boolean isSlotUsed(int slotNumber) {
        String fileName = SAVE_DIR + "slot_" + slotNumber + ".sav";
        File file = new File(fileName);
        return file.exists();
    }

    // Получение информации о слоте
    public static SaveInfo getSlotInfo(int slotNumber) {
        String fileName = SAVE_DIR + "slot_" + slotNumber + ".sav";
        File file = new File(fileName);
        if (!file.exists()) return null;

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            GameSave save = (GameSave) ois.readObject();
            BufferedImage thumbnail = loadScreenshot(slotNumber);
            return new SaveInfo("Слот " + slotNumber, save.saveDate,
                    save.totalEnemiesKilled, save.playerHealth, thumbnail);
        } catch (Exception e) {
            BufferedImage thumbnail = loadScreenshot(slotNumber);
            return new SaveInfo("Слот " + slotNumber, new Date(file.lastModified()), 0, 0, thumbnail);
        }
    }

    // Получение списка всех слотов
    public static java.util.List<SaveSlotInfo> getAllSlots() {
        java.util.List<SaveSlotInfo> slots = new ArrayList<>();
        for (int i = 1; i <= MAX_SAVES; i++) {
            SaveInfo info = getSlotInfo(i);
            slots.add(new SaveSlotInfo(i, "Слот " + i, info));
        }
        return slots;
    }

    // Удаление слота
    public static boolean deleteSlot(int slotNumber) {
        String fileName = SAVE_DIR + "slot_" + slotNumber + ".sav";
        File file = new File(fileName);
        boolean deleted = false;
        if (file.exists()) {
            deleted = file.delete();
        }

        // Удаляем скриншот
        String screenshotPath = SCREENSHOT_DIR + "slot_" + slotNumber + ".png";
        File screenshotFile = new File(screenshotPath);
        if (screenshotFile.exists()) {
            screenshotFile.delete();
        }

        return deleted;
    }

    // Создание скриншота текущего вида
    public static BufferedImage createScreenshot(GamePanel gamePanel) {
        BufferedImage screenshot = new BufferedImage(
                gamePanel.getWidth(), gamePanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = screenshot.createGraphics();
        gamePanel.paint(g);
        g.dispose();
        return screenshot;
    }

    public void restoreToWorld(GameWorld world, PlayerTank player) {
        // ===== ПРЯМОЕ ВОССТАНОВЛЕНИЕ ВСЕХ ХАРАКТЕРИСТИК =====
        player.gridX = this.playerX;
        player.gridY = this.playerY;
        player.health = this.playerHealth;
        player.maxHealth = this.playerMaxHealth;
        player.strength = this.playerStrength;
        player.agility = this.playerAgility;
        player.armor = this.playerArmor;
        player.critBonus = this.playerCritBonus;
        player.vision = this.playerVision;
        player.viewRadius = this.playerViewRadius;
        player.reloadBonus = this.playerReloadBonus;
        player.nimble = this.playerNimble;
        player.dodgeChance = this.playerDodgeChance;
        player.movePoints = this.playerMovePoints;
        player.maxMovePoints = this.playerMaxMovePoints;
        player.isOnHill = this.playerOnHill;
        player.currentHillBonus = this.playerHillBonus;
        player.setSilver(this.silver);
        player.setParts(this.parts);
        player.upgradeLevel = this.playerUpgradeLevel;
        player.upgradeClass = this.playerUpgradeClass;

        // ===== ДОБАВЬТЕ ВОССТАНОВЛЕНИЕ ЭКИПИРОВАННОГО ОРУЖИЯ =====
        player.baseAccuracy = this.playerBaseAccuracy;
        player.weaponWeight = this.playerWeaponWeight;
        player.originalReloadCost = this.playerOriginalReloadCost;

        // Восстанавливаем оружие
        if (this.playerEquippedWeaponId != null && !this.playerEquippedWeaponId.isEmpty()) {
            player.restoreEquippedWeapon(this.playerEquippedWeaponId,
                    this.playerCurrentAmmo, this.playerMaxAmmo,
                    this.playerReloadCost, this.playerBurstSize);
        } else {
            // Если нет сохранённого оружия - используем Breda
            player.setEquippedWeapon("breda", player.createPlayerWeaponFromId("breda"));
        }


        // СРАЗУ ПОСЛЕ НИХ добавьте:
        if (this.gamePanel != null) {
            this.gamePanel.updatePlayerTextures();
        }

        // Восстанавливаем характеристики оружия
        player.weaponAccuracy = this.playerWeaponAccuracy;
        player.weaponDamage = this.playerWeaponDamage;
        player.critChance = this.playerCritChance;
        player.shotCost = this.playerShotCost;
        player.aimedShotCost = this.playerAimedShotCost;
        player.weaponCaliber = this.playerWeaponCaliber;
        player.burstSize = this.playerBurstSize;
        player.reloadCost = this.playerReloadCost;

        // Восстанавливаем снаряды в оружии
        player.getInventory().restoreAmmo(this.playerCurrentAmmo, this.playerMaxAmmo,
                this.playerReloadCost, this.playerBurstSize);

        // ===== ВСЕГДА СОЗДАЁМ НОВУЮ СИСТЕМУ ОПЫТА =====
        ExperienceSystem expSys = new ExperienceSystem("Leichttraktor");

        // Восстанавливаем опыт ИЗ СОХРАНЕНИЯ, если он был
        if (playerLevel > 1 || playerExperience > 0) {
            System.out.println("Восстанавливаю опыт из сохранения: level=" + playerLevel + ", exp=" + playerExperience);
            for (int i = 1; i < playerLevel; i++) {
                expSys.addExperience(expSys.getExperienceForNextLevel());
            }
            if (playerExperience > 0) {
                expSys.addExperience(playerExperience);
            }
            //expSys.setExperienceForNextLevel(playerExperienceForNextLevel);  // ← теперь работает
        } else {
            System.out.println("Опыт в сохранении = 0, создаю новую систему с 0 опыта");
            // expSys уже создан с 0 опыта
        }

        // ===== ВСЕГДА ПРИСВАИВАЕМ =====
        player.setExperienceSystem(expSys);

        world.getEnemies().clear();
        world.getWalls().clear();
        world.getTrees().clear();
        world.getQuestNPCs().clear();
        world.getFriendlyUnits().clear();
        world.getLootDrops().clear();
        world.getStorageChests().clear();
        world.getGarbageContainers().clear();
        world.getPavements().clear();
        world.getOakPlanks().clear();
        world.getIronFloors().clear();
        world.getWaters().clear();
        world.getDoors().clear();


        // Восстанавливаем врагов
        for (EnemySaveData data : enemies) {
            world.getEnemies().add(data.toEnemy());
        }

        // Восстанавливаем стены
        for (WallSaveData data : walls) {
            world.getWalls().add(data.toWall());
        }

        // Восстанавливаем деревья
        for (TreeSaveData data : trees) {
            world.getTrees().add(data.toTree());
        }

        // Восстанавливаем NPC
        // В GameSave.java, в методе restoreToWorld(), после восстановления NPC:

// Восстанавливаем NPC
        // Восстанавливаем NPC
        // В GameSave.java, в restoreToWorld(), после восстановления NPC:

// Восстанавливаем NPC
        // Восстанавливаем NPC
        // Восстанавливаем NPC
        if (npcs != null) {
            for (NPCSaveData data : npcs) {
                QuestNPC npc = data.toNPC();
                world.getQuestNPCs().add(npc);

                // ===== ИСПРАВЛЕНИЕ ДЛЯ SAV M/43 =====
                if ("Sav m/43".equals(npc.name)) {
                    // ===== ВАЖНО: проверяем, был ли получен квест =====
                    if (npc.hasReceivedQuest()) {
                        // Проверяем, все ли враги Франции и Италии уничтожены
                        boolean completed = npc.isQuestCompletedForFactions(world.getEnemies());

                        if (completed && !npc.isQuestFinished) {
                            // Если все враги уничтожены - квест выполнен!
                            npc.isQuestCompleted = true;
                            System.out.println("✅ Sav m/43: квест восстановлен как ВЫПОЛНЕННЫЙ!");
                        } else if (!completed) {
                            // Если враги остались - квест НЕ выполнен
                            npc.isQuestCompleted = false;
                            System.out.println("⚠️ Sav m/43: квест НЕ выполнен (остались враги)");
                        }
                    } else {
                        // ===== КВЕСТ НЕ БЫЛ ПОЛУЧЕН =====
                        System.out.println("ℹ️ Sav m/43: квест ещё не получен игроком");
                        npc.isQuestCompleted = false;
                        npc.isQuestFinished = false;
                    }

                    // Если квест уже был завершён ранее - помечаем как законченный
                    if (npc.isQuestCompleted && !npc.isQuestFinished) {
                        // Проверяем ещё раз, все ли враги мертвы
                        boolean allDead = true;
                        for (Enemy enemy : world.getEnemies()) {
                            if (enemy.isAlive && (enemy.getFaction() == Faction.FRANCE ||
                                    enemy.getFaction() == Faction.ITALY)) {
                                allDead = false;
                                break;
                            }
                        }
                        if (allDead) {
                            npc.isQuestFinished = true;
                            npc.isQuestCompleted = false; // Сбрасываем, чтобы не показывать диалог повторно
                            System.out.println("✅ Sav m/43: квест помечен как ПОЛНОСТЬЮ ЗАВЕРШЁННЫЙ!");
                        }
                    }

                    // ===== УБЕЖДАЕМСЯ, ЧТО NPC ПРАВИЛЬНО НАСТРОЕН =====
                    npc.disableSound();
                }
            }
        }

        // Восстанавливаем асфальт
        for (PavementSaveData data : pavements) {
            world.getPavements().add(data.toPavement());
        }

        // Восстанавливаем дубовые доски
        for (OakPlanksSaveData data : oakPlanks) {
            world.getOakPlanks().add(data.toOakPlanks());
        }

        if (waters != null) {
            for (WaterSaveData data : waters) {
                world.getWaters().add(data.toWater());
            }
        }

        if (ironFloors != null) {
            for (IronFloorSaveData data : ironFloors) {
                world.getIronFloors().add(data.toIronFloor());
            }
        }

        if (doors != null) {
            for (DoorSaveData data : doors) {
                world.getDoors().add(data.toDoor());
            }
            System.out.println("Восстановлено дверей: " + world.getDoors().size());
        }

        // Восстанавливаем союзников
        // В цикле восстановления союзников:
        // В GameSave.java, в restoreToWorld(), в цикле восстановления союзников:

        for (FriendlySaveData data : friendlies) {
            FriendlyUnit friendly = data.toFriendly();
            world.getFriendlyUnits().add(friendly);

            if (friendly.isRecruited) {
                world.addControllableUnit(friendly);
                // ===== ДОБАВЬТЕ ЭТОТ БЛОК =====
                if (this.gamePanel != null) {
                    this.gamePanel.loadFriendlyTextures(friendly);
                    this.gamePanel.updateFriendlyPortrait(friendly);
                }
                // ==============================
            }
        }

        if (storageChests != null) {
            for (StorageChestSaveData data : storageChests) {
                world.getStorageChests().add(data.toStorageChest());
            }
        }

        if (garbageContainers != null) {
            for (GarbageContainerSaveData data : garbageContainers) {
                world.getGarbageContainers().add(data.toGarbageContainer());
            }
        }

        world.getTraders().clear();  // ← ДОБАВЬТЕ ЭТУ СТРОКУ, если её нет

        if (traders != null) {
            for (TraderSaveData data : traders) {
                world.getTraders().add(data.toTrader());
            }
            System.out.println("✅ Восстановлено торговцев: " + world.getTraders().size());
        }

        // ===== ВАЖНО: НЕ вызываем startTurn() и не перезаписываем movePoints! =====
        // Просто добавляем в список управляемых
        world.getControllableUnits().clear();
        for (FriendlyUnit unit : world.getFriendlyUnits()) {
            if (unit.isRecruited && unit.isAlive) {
                world.addControllableUnit(unit);
                // ❌ УДАЛИТЕ ЭТУ СТРОКУ, если она есть:
                // unit.startTurn();
            }
        }

        // ===== ВОССТАНАВЛИВАЕМ ДРОП =====
        if (lootDrops != null) {
            System.out.println("Восстановление дропов: " + lootDrops.size());
            for (LootDropSaveData data : lootDrops) {
                LootDrop drop = data.toLootDrop();
                if (!drop.isEmpty()) {
                    world.getLootDrops().add(drop);
                    System.out.println("  Добавлен дроп в [" + drop.gridX + "," + drop.gridY +
                            "] с предметами: " + drop.items.size() + ", снарядами: " + drop.ammo.size());
                }
            }
        }

        if (this.currentLevelPath != null && this.gamePanel != null) {
            this.gamePanel.setCurrentLevelPath(this.currentLevelPath);
            System.out.println("✅ Восстановлен путь к уровню: " + this.currentLevelPath);
        }
        world.reloadWinZoneFromFile();

        // ===== ВАЖНО: обновляем recruitedUnits в GamePanel =====
        if (this.gamePanel != null) {
            this.gamePanel.syncRecruitedUnits();
        }

        // Пересчитываем очки хода для всех союзников
        for (FriendlyUnit unit : world.getFriendlyUnits()) {
            if (unit.isRecruited && unit.isAlive) {
                unit.calculateMovePoints();
                if (unit.movePoints > unit.maxMovePoints) {
                    unit.movePoints = unit.maxMovePoints;
                }
                System.out.println(unit.name + ": вес инвентаря=" + unit.getInventory().getTotalWeight() +
                        "/" + unit.maxCarryWeight + ", ОХ=" + unit.movePoints + "/" + unit.maxMovePoints);
            }
        }

        inventory.restoreToInventory(player.getInventory(), player);

        // ===== ВАЖНО: просто устанавливаем значения, НЕ вызываем applyUpgradeBonuses =====
        player.setUpgradeLevel(playerUpgradeLevel);
        player.setUpgradeClass(playerUpgradeClass);
        // player.applyUpgradeBonuses();  // ← НЕ ВЫЗЫВАТЬ!



        // ===== ОБНОВЛЯЕМ ТЕКСТУРЫ И ПОРТРЕТ =====
        if (this.gamePanel != null) {
            this.gamePanel.updatePlayerTextures();
            this.gamePanel.updateHeroPortrait();  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
        }

        player.maxCarryWeight = player.strength * 0.5;

        // Пересчитываем очки хода без применения бонусов
        player.calculateMovePoints();

        world.updateVisibilityMap();
        world.checkHillOccupation();
        world.checkSavM43Quest();
        world.revalidateAllQuestNPCs();

        gamePanel.repaint();
    }


    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public Date getSaveDate() { return saveDate; }
    public int getTotalEnemiesKilled() { return totalEnemiesKilled; }
    public String getSaveName() { return saveName; }
    public int getSlotNumber() { return slotNumber; }

    // ===== ВСПОМОГАТЕЛЬНЫЙ КЛАСС ДЛЯ ДРОПА =====
    static class LootDropSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;
        private Map<String, Integer> items;  // тип предмета -> количество
        private Map<String, Integer> ammo;   // калибр -> количество
        private Map<String, Integer> keys;  // ← ДОБАВЛЯЕМ ПОЛЕ ДЛЯ КЛЮЧЕЙ

        LootDropSaveData(LootDrop drop) {
            this.x = drop.gridX;
            this.y = drop.gridY;
            this.items = new HashMap<>();
            this.ammo = new HashMap<>();

            for (Map.Entry<Item.ItemType, Integer> entry : drop.items.entrySet()) {
                items.put(entry.getKey().name(), entry.getValue());
                System.out.println("  Сохранён предмет: " + entry.getKey().name() + " x" + entry.getValue());
            }

            for (Map.Entry<Caliber, Integer> entry : drop.ammo.entrySet()) {
                // Убираем префикс "CALIBER_" при сохранении
                String caliberName = entry.getKey().name().replace("CALIBER_", "");
                ammo.put(caliberName, entry.getValue());
                System.out.println("  Сохранены снаряды: " + caliberName + " x" + entry.getValue());
            }
        }

        LootDrop toLootDrop() {
            LootDrop drop = new LootDrop(x, y);

            // Восстанавливаем предметы
            for (Map.Entry<String, Integer> entry : items.entrySet()) {
                try {
                    Item.ItemType type = Item.ItemType.valueOf(entry.getKey());
                    drop.addItem(type, entry.getValue());
                } catch (IllegalArgumentException e) {
                    System.err.println("Неизвестный тип предмета: " + entry.getKey());
                }
            }

            // ===== ИСПРАВЛЕНИЕ: ВОССТАНАВЛИВАЕМ СНАРЯДЫ =====
            for (Map.Entry<String, Integer> entry : ammo.entrySet()) {
                String caliberName = entry.getKey();
                int count = entry.getValue();

                try {
                    // Добавляем префикс "CALIBER_" для получения enum
                    Caliber caliber = Caliber.valueOf("CALIBER_" + caliberName);
                    drop.addAmmo(caliber, count);
                    System.out.println("  Восстановлены снаряды: " + caliberName + " x" + count);
                } catch (IllegalArgumentException e) {
                    System.err.println("Неизвестный калибр: " + caliberName);
                }
            }

            return drop;
        }
    }

    // Вспомогательные классы для сериализации
    static class EnemySaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;
        private int health;
        private int maxHealth;
        private String direction;
        private int movePoints;
        private int lastSeenX, lastSeenY;
        private int memoryTurns;
        private boolean isAlive;

        // ===== НОВЫЕ ПОЛЯ =====
        private String type;           // Тип врага (Leichttraktor, R_Otsu, M14_41, H35, FT, Fiat3000)
        private String faction;        // Фракция (GERMANY, JAPAN, FRANCE, ITALY, NEUTRAL)
        private int strength;
        private int agility;
        private int armor;
        private int nimble;
        private int critBonus;
        private int vision;
        private int behaviorType;      // 0=AGGRESSIVE, 1=TACTICAL, 2=SNIPER
        // ====================

        EnemySaveData(Enemy enemy) {
            this.x = enemy.gridX;
            this.y = enemy.gridY;
            this.health = enemy.health;
            this.direction = enemy.direction.name();
            this.movePoints = enemy.movePoints;
            this.lastSeenX = enemy.lastSeenPlayerX;
            this.lastSeenY = enemy.lastSeenPlayerY;
            this.memoryTurns = enemy.memoryTurns;
            this.maxHealth = enemy.maxHealth;  // ← ЭТА СТРОКА ДОЛЖНА БЫТЬ!
            this.isAlive = enemy.isAlive;      // ← И ЭТА!

            // ===== НОВЫЕ ПОЛЯ =====
            this.type = enemy.type;
            this.faction = enemy.getFaction().name();
            this.strength = enemy.strength;
            this.agility = enemy.agility;
            this.armor = enemy.armor;
            this.nimble = enemy.nimble;
            this.critBonus = enemy.critBonus;
            this.vision = enemy.vision;
            this.behaviorType = enemy.getBehaviorType().ordinal();
            // ====================
        }

        Enemy toEnemy() {
            Enemy.Direction dir = Enemy.Direction.valueOf(direction);

            // Создаём врага с временными параметрами
            Enemy enemy = new Enemy(x, y, dir);

            // ===== ВАЖНО: устанавливаем ТИП, НО НЕ ВЫЗЫВАЕМ setType() ДВАЖДЫ! =====
            // Сначала устанавливаем тип и применяем базовые статы
            enemy.type = this.type;
            enemy.setType(this.type);  // Это установит базовые статы (maxHealth, health и т.д.)

            // ===== ЗАТЕМ ПЕРЕЗАПИСЫВАЕМ ЗДОРОВЬЕ ИЗ СОХРАНЕНИЯ =====
            enemy.health = this.health;
            enemy.maxHealth = this.maxHealth;
            enemy.isAlive = this.isAlive;

            // Восстанавливаем остальные характеристики (они уже установлены setType,
            // но перезаписываем из сохранения, чтобы быть уверенными)
            enemy.strength = this.strength;
            enemy.agility = this.agility;
            enemy.armor = this.armor;
            enemy.nimble = this.nimble;
            enemy.critBonus = this.critBonus;
            enemy.vision = this.vision;
            enemy.viewRadius = 10 + this.vision / 5;

            // Восстанавливаем поведение
            switch(this.behaviorType) {
                case 0: enemy.setBehaviorType(Enemy.BehaviorType.AGGRESSIVE); break;
                case 1: enemy.setBehaviorType(Enemy.BehaviorType.TACTICAL); break;
                case 2: enemy.setBehaviorType(Enemy.BehaviorType.SNIPER); break;
            }

            // ===== ВАЖНО: не вызываем setType() больше! =====
            // Убираем дублирующий вызов enemy.setType(this.type);

            // Восстанавливаем фракцию
            try {
                Faction factionEnum = Faction.valueOf(this.faction);
                enemy.setFaction(factionEnum);
            } catch (IllegalArgumentException e) {
                enemy.setFaction(Faction.NEUTRAL);
            }

            // Пересчитываем производные
            enemy.calculateDodgeChance();
            enemy.calculateMovePoints();

            // Если враг был мёртв в сохранении
            if (!this.isAlive) {
                enemy.health = 0;
                enemy.isAlive = false;
            }

            return enemy;
        }
    }

    static class WallSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;
        private int health;
        private boolean destructible;
        private String wallType;  // ← ДОБАВИТЬ

        WallSaveData(Wall wall) {
            this.x = wall.gridX;
            this.y = wall.gridY;
            this.health = wall.health;
            this.destructible = wall.isDestructible;
            this.wallType = wall.wallType != null ? wall.wallType : "Brick";  // ← ДОБАВИТЬ
        }

        Wall toWall() {
            return new Wall(x, y, health, destructible, wallType);  // ← ИСПРАВИТЬ
        }
    }

    static class TreeSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;
        private String treeType;
        private int health;

        TreeSaveData(Tree tree) {
            this.x = tree.gridX;
            this.y = tree.gridY;
            this.treeType = tree.treeType;
            this.health = tree.health;
        }

        Tree toTree() {
            Tree tree = new Tree(x, y, treeType);
            tree.health = health;
            tree.isAlive = health > 0;
            return tree;
        }
    }

    static class NPCSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;
        private String name;
        private boolean hasReceivedQuest;
        private boolean isQuestCompleted;
        private boolean isQuestFinished;  // ← ДОБАВЬТЕ ЭТО ПОЛЕ
        private int questProgress;
        private int questTarget;

        NPCSaveData(QuestNPC npc) {
            this.x = npc.gridX;
            this.y = npc.gridY;
            this.name = npc.name;
            this.hasReceivedQuest = npc.hasReceivedQuest();  // ← Сохраняем реальное значение
            this.isQuestCompleted = npc.isQuestCompleted;
            this.isQuestFinished = npc.isQuestFinished;
            this.questProgress = npc.questProgress;
            this.questTarget = npc.questTarget;
        }

        QuestNPC toNPC() {
            QuestNPC npc;

            // ===== ПРАВИЛЬНОЕ СОЗДАНИЕ NPC =====
            if ("Sav m/43".equals(name)) {
                npc = QuestNPC.createSavM43(x, y);
                // ===== ВАЖНО: восстанавливаем РЕАЛЬНОЕ состояние =====
                npc.hasReceivedQuest = hasReceivedQuest;  // ← Используем сохранённое значение!
                npc.isQuestCompleted = isQuestCompleted;
                npc.isQuestFinished = isQuestFinished;
                npc.questProgress = questProgress;
                npc.questTarget = questTarget;
                npc.hasTalked = hasReceivedQuest;
                npc.disableSound();  // Отключаем звуки
            } else {
                npc = new QuestNPC(x, y, name);
                if (hasReceivedQuest) {
                    npc.setQuestReceived();
                }
                npc.isQuestCompleted = isQuestCompleted;
                npc.isQuestFinished = isQuestFinished;
                npc.questProgress = questProgress;
                npc.questTarget = questTarget;
                npc.hasTalked = hasReceivedQuest;
            }

            return npc;
        }
    }

    static class PavementSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;

        PavementSaveData(Pavement pavement) {
            this.x = pavement.gridX;
            this.y = pavement.gridY;
        }

        Pavement toPavement() {
            return new Pavement(x, y);
        }
    }

    static class OakPlanksSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;

        OakPlanksSaveData(OakPlanks planks) {
            this.x = planks.gridX;
            this.y = planks.gridY;
        }

        OakPlanks toOakPlanks() {
            return new OakPlanks(x, y);
        }
    }

    static class IronFloorSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;

        IronFloorSaveData(IronFloor floor) {
            this.x = floor.gridX;
            this.y = floor.gridY;
        }

        IronFloor toIronFloor() {
            return new IronFloor(x, y);
        }
    }

    // В GameSave.java, после других внутренних классов (например, после OakPlanksSaveData)
    static class WaterSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;

        WaterSaveData(Water water) {
            this.x = water.gridX;
            this.y = water.gridY;
        }

        Water toWater() {
            return new Water(x, y);
        }
    }

    public static class KeySaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String color;
        private int count;

        KeySaveData(String color, int count) {
            this.color = color;
            this.count = count;
        }
    }

    // Внутри класса GameSave, после других внутренних классов, добавьте:
    static class DoorSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;
        private String color;
        private boolean isOpen;
        private boolean isLocked;
        private boolean isAlive;

        DoorSaveData(Door door) {
            this.x = door.gridX;
            this.y = door.gridY;
            this.color = door.color.name();
            this.isOpen = door.isOpen;
            this.isLocked = door.isLocked;
            this.isAlive = door.isAlive;
        }

        Door toDoor() {
            Door.DoorColor doorColor = Door.DoorColor.valueOf(color);
            Door door = new Door(x, y, doorColor);
            if (isOpen) door.open();
            door.isLocked = isLocked;
            door.isAlive = isAlive;
            return door;
        }
    }

    static class FriendlySaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x, y;
        private String name;
        private String type;
        private boolean isRecruited;
        private boolean isUnavailable;  // ← ДОБАВЬТЕ ЭТО ПОЛЕ
        private int health;
        private int maxHealth;
        private String equippedWeaponId;
        private java.util.List<String> selectedBonuses;  // ← ДОБАВИТЬ

        // Состояние
        private int level;
        private int experience;
        private int experienceForNextLevel;  // Опыт до следующего уровня
        private int upgradeLevel;
        private String upgradeClass;
        private int movePoints;
        private int maxMovePoints;
        private String direction;
        private int currentAmmo;
        private int maxAmmo;

        // ===== НОВЫЕ ПОЛЯ ДЛЯ ХАРАКТЕРИСТИК =====
        private int strength;
        private int agility;
        private int weaponAccuracy;      // ← ЭТО ВАЖНО!
        private int armor;
        private int critBonus;
        private int vision;
        private int viewRadius;
        private int reloadBonus;
        private int nimble;
        private double dodgeChance;

        private InventorySaveData inventoryData;

        // ===== ДОБАВЬТЕ ЭТИ ПОЛЯ =====
        public int baseAccuracy;
        public int weaponWeight;
        public int originalReloadCost;
        public int baseWeaponAccuracy;


        FriendlySaveData(FriendlyUnit friendly) {
            this.x = friendly.gridX;
            this.y = friendly.gridY;
            this.name = friendly.name;
            this.type = friendly.type;
            this.isRecruited = friendly.isRecruited;
            this.isUnavailable = friendly.isUnavailable;  // ← СОХРАНЯЕМ
            this.health = friendly.health;
            this.maxHealth = friendly.maxHealth;
            this.equippedWeaponId = friendly.getEquippedWeaponId();

            this.upgradeLevel = friendly.getUpgradeLevel();
            this.upgradeClass = friendly.getUpgradeClass();

            this.movePoints = friendly.movePoints;
            this.maxMovePoints = friendly.maxMovePoints;
            this.direction = friendly.currentDirection.name();
            this.currentAmmo = friendly.getInventory().getCurrentAmmoCount();
            this.maxAmmo = friendly.getInventory().getMaxAmmo();

            // Сохраняем текущие характеристики
            this.strength = friendly.strength;
            this.agility = friendly.agility;
            this.weaponAccuracy = friendly.weaponAccuracy;
            this.armor = friendly.armor;
            this.critBonus = friendly.critBonus;
            this.vision = friendly.vision;
            this.viewRadius = friendly.viewRadius;
            this.reloadBonus = friendly.reloadBonus;
            this.nimble = friendly.nimble;
            this.dodgeChance = friendly.dodgeChance;

            this.inventoryData = new InventorySaveData(friendly.getInventory(), 0);

            if (friendly.getExperienceSystem() != null) {
                this.level = friendly.getExperienceSystem().getLevel();
                this.experience = friendly.getExperienceSystem().getExperience();
                this.experienceForNextLevel = friendly.getExperienceSystem().getExperienceForNextLevel();

                LevelUpBonus bonus = friendly.getExperienceSystem().getPendingLevelUpBonus();
                if (bonus != null && bonus.getSelectedBonuses() != null) {
                    this.selectedBonuses = new java.util.ArrayList<>();  // ← полное имя
                    for (LevelUpBonus.BonusType bt : bonus.getSelectedBonuses()) {
                        this.selectedBonuses.add(bt.name());
                    }
                }
            } else {
                this.level = 1;
                this.experience = 0;
            }

            if (friendly.getExperienceSystem() != null) {
                this.level = friendly.getExperienceSystem().getLevel();
                this.experience = friendly.getExperienceSystem().getExperience();

                // ===== ДОБАВЬТЕ ЭТОТ БЛОК =====
                LevelUpBonus bonus = friendly.getExperienceSystem().getPendingLevelUpBonus();
                if (bonus != null && bonus.getSelectedBonuses() != null) {
                    this.selectedBonuses = new java.util.ArrayList<>();
                    for (LevelUpBonus.BonusType bt : bonus.getSelectedBonuses()) {
                        this.selectedBonuses.add(bt.name());
                    }
                }
                // ===============================
            }

            this.baseAccuracy = friendly.baseAccuracy;
            this.weaponWeight = friendly.weaponWeight;
            this.originalReloadCost = friendly.getOriginalReloadCost();
            this.baseWeaponAccuracy = friendly.baseWeaponAccuracy;
        }

        FriendlyUnit toFriendly() {
            FriendlyUnit friendly = new FriendlyUnit(x, y, name, type, true);

            friendly.isRecruited = isRecruited;
            friendly.isUnavailable = isUnavailable;  // ← ВОССТАНАВЛИВАЕМ
            friendly.isAlive = health > 0;

            // ===== ВОССТАНАВЛИВАЕМ БАЗОВЫЕ ХАРАКТЕРИСТИКИ =====
            friendly.health = this.health;
            friendly.maxHealth = this.maxHealth;
            friendly.strength = this.strength;
            friendly.agility = this.agility;
            friendly.weaponAccuracy = this.weaponAccuracy;
            friendly.baseAccuracy = this.baseAccuracy;
            friendly.armor = this.armor;
            friendly.critBonus = this.critBonus;
            friendly.vision = this.vision;
            friendly.viewRadius = this.viewRadius;
            friendly.reloadBonus = this.reloadBonus;
            friendly.nimble = this.nimble;
            friendly.dodgeChance = this.dodgeChance;

            // ===== ВОССТАНАВЛИВАЕМ ГРУЗОПОДЪЁМНОСТЬ =====
            friendly.maxCarryWeight = friendly.strength * 0.5;

            // Восстанавливаем очки хода и направление
            friendly.movePoints = movePoints;
            friendly.maxMovePoints = maxMovePoints;
            friendly.currentDirection = FriendlyUnit.Direction.valueOf(direction);

            // ===== ВОССТАНАВЛИВАЕМ ИНВЕНТАРЬ =====
            Inventory inv = friendly.getInventory();
            // Очищаем инвентарь
            for (int y = 0; y < Inventory.HEIGHT; y++) {
                for (int x = 0; x < Inventory.WIDTH; x++) {
                    inv.clearSlot(x, y);
                }
            }

            // Восстанавливаем обычные предметы
            if (inventoryData != null) {
                for (ItemSaveData data : inventoryData.getItems()) {
                    inv.addItemToInventory(new Item(data.type, data.count));
                }

                // Восстанавливаем снаряды
                for (AmmoSaveData data : inventoryData.getAmmoItems()) {
                    try {
                        Caliber caliber = Caliber.valueOf("CALIBER_" + data.caliber.toUpperCase());
                        AmmoItem ammoItem = new AmmoItem(caliber, data.count);
                        inv.addAmmoItem(ammoItem);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Неизвестный калибр: " + data.caliber);
                    }
                }
            }

            // ===== ВОССТАНАВЛИВАЕМ ЭКИПИРОВАННОЕ ОРУЖИЕ =====
            if (equippedWeaponId != null && !equippedWeaponId.isEmpty()) {
                FriendlyUnit.WeaponData weapon = WeaponLibrary.getWeaponByWeaponId(equippedWeaponId);
                if (weapon != null) {
                    friendly.setEquippedWeapon(equippedWeaponId, weapon);
                    // Восстанавливаем снаряды в оружии
                    if (currentAmmo > 0 || maxAmmo > 0) {
                        friendly.getInventory().restoreAmmo(currentAmmo, maxAmmo,
                                friendly.getReloadCost(), friendly.getBurstSize());
                    }
                    System.out.println("🔫 Восстановлено оружие для " + friendly.name + ": " + weapon.name);
                } else {
                    System.err.println("❌ Оружие не найдено: " + equippedWeaponId);
                }
            }

            if (friendly.getEquippedWeaponData() != null) {
                friendly.weaponAccuracy = friendly.baseAccuracy + friendly.getEquippedWeaponData().weaponAccuracy;
                System.out.println("🎯 Пересчитана точность для " + friendly.name +
                        ": baseAccuracy=" + friendly.baseAccuracy +
                        ", weaponAccuracy=" + friendly.getEquippedWeaponData().weaponAccuracy +
                        ", итого=" + friendly.weaponAccuracy);
            } else {
                friendly.weaponAccuracy = friendly.baseAccuracy;
            }

            // ===== ВОССТАНАВЛИВАЕМ СИСТЕМУ ОПЫТА =====
            if (level > 1 || experience > 0) {
                ExperienceSystem exp = new ExperienceSystem(type);
                // Сначала устанавливаем уровень
                for (int i = 1; i < level; i++) {
                    exp.addExperience(exp.getExperienceForNextLevel());
                }
                // Затем добавляем опыт в текущем уровне
                if (experience > 0) {
                    exp.addExperience(experience);
                }

                exp.setExperienceForNextLevel(experienceForNextLevel);

                friendly.setExperienceSystem(exp);

                // Восстанавливаем выбранные бонусы
                if (selectedBonuses != null) {
                    for (String bonusName : selectedBonuses) {
                        try {
                            LevelUpBonus.BonusType bonus = LevelUpBonus.BonusType.valueOf(bonusName);
                            // friendly.applyBonusWithoutConsuming(bonus);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Неизвестный бонус: " + bonusName);
                        }
                    }
                }
            }

            // ===== НЕ ВЫЗЫВАЙТЕ applyUpgradeBonuses() ЗДЕСЬ =====
            friendly.setUpgradeLevel(upgradeLevel);
            friendly.setUpgradeClass(upgradeClass);
            // friendly.applyUpgradeBonuses();  // ← УДАЛИТЬ ИЛИ ЗАКОММЕНТИРОВАТЬ

            // Пересчитываем очки хода
            friendly.calculateMovePoints();
            if (friendly.movePoints > friendly.maxMovePoints) {
                friendly.movePoints = friendly.maxMovePoints;
            }

            System.out.println("✅ Восстановлен союзник: " + name +
                    " (здоровье: " + friendly.health + "/" + friendly.maxHealth +
                    ", очки хода: " + friendly.movePoints + "/" + friendly.maxMovePoints +
                    ", грузоподъёмность: " + friendly.maxCarryWeight + ")");

            // ===== ДОБАВЬТЕ ВОССТАНОВЛЕНИЕ ВЫБРАННЫХ БОНУСОВ =====
            if (selectedBonuses != null) {
                for (String bonusName : selectedBonuses) {
                    try {
                        LevelUpBonus.BonusType bonus = LevelUpBonus.BonusType.valueOf(bonusName);
                        //friendly.applyBonusWithoutConsuming(bonus);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Неизвестный бонус: " + bonusName);
                    }
                }
            }
            // =================================================

            friendly.baseAccuracy = this.baseAccuracy;
            friendly.weaponWeight = this.weaponWeight;
            friendly.baseWeaponAccuracy = this.baseWeaponAccuracy;

            return friendly;
        }
    }



    static class InventorySaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int silver;
        public java.util.List<ItemSaveData> items;
        public java.util.List<AmmoSaveData> ammoItems;
        public java.util.List<KeySaveData> keyItems;  // ← НОВОЕ ПОЛЕ
        private int currentAmmo;
        private int maxAmmo;
        private int reloadCost;
        private int burstSize;
        private String currentAmmoCaliber;
        private boolean currentAmmoImproved;

        public java.util.List<ItemSaveData> getItems() { return items; }
        public java.util.List<AmmoSaveData> getAmmoItems() { return ammoItems; }
        public java.util.List<KeySaveData> getKeyItems() { return keyItems; }  // ← НОВЫЙ ГЕТТЕР

        InventorySaveData(Inventory inventory, int teamSilver) {
            this.silver = teamSilver;
            this.currentAmmo = inventory.getCurrentAmmoCount();
            this.maxAmmo = inventory.getMaxAmmo();
            this.reloadCost = inventory.getReloadCost();
            this.burstSize = inventory.getBurstSize();
            this.items = new java.util.ArrayList<>();
            this.ammoItems = new java.util.ArrayList<>();
            this.keyItems = new java.util.ArrayList<>();  // ← ИНИЦИАЛИЗАЦИЯ

            // Сохраняем ТИП текущих снарядов
            AmmoItem current = inventory.getCurrentAmmo();
            if (current != null) {
                this.currentAmmoCaliber = current.getCaliber().name();
                this.currentAmmoImproved = current.isImproved();
            }

            // Сохраняем ВСЕ предметы
            for (int y = 0; y < Inventory.HEIGHT; y++) {
                for (int x = 0; x < Inventory.WIDTH; x++) {
                    Item item = inventory.getItem(x, y);
                    if (item != null && item.getCount() > 0) {
                        if (item instanceof AmmoItem) {
                            AmmoItem ammoItem = (AmmoItem) item;
                            String caliberShort = ammoItem.getCaliber().name().replace("CALIBER_", "");
                            ammoItems.add(new AmmoSaveData(caliberShort, ammoItem.getCount()));
                        } else if (item instanceof KeyItem) {
                            KeyItem keyItem = (KeyItem) item;
                            keyItems.add(new KeySaveData(keyItem.getColor().name(), keyItem.getCount()));
                        } else {
                            items.add(new ItemSaveData(item.getType(), item.getCount()));
                        }
                    }
                }
            }
        }

        void restoreToInventory(Inventory inventory, PlayerTank player) {
            if (player != null) {
                player.setSilver(silver);
            }

            // Очищаем инвентарь
            for (int y = 0; y < Inventory.HEIGHT; y++) {
                for (int x = 0; x < Inventory.WIDTH; x++) {
                    inventory.clearSlot(x, y);
                }
            }

            // Восстанавливаем обычные предметы
            for (ItemSaveData data : items) {
                inventory.addItemToInventory(new Item(data.type, data.count));
            }

            // Восстанавливаем снаряды
            for (AmmoSaveData data : ammoItems) {
                try {
                    Caliber caliber = Caliber.valueOf("CALIBER_" + data.caliber.toUpperCase());
                    AmmoItem ammoItem = new AmmoItem(caliber, data.count);
                    inventory.addAmmoItem(ammoItem);
                } catch (IllegalArgumentException e) {
                    System.err.println("Неизвестный калибр: " + data.caliber);
                }
            }

            // ===== ВОССТАНАВЛИВАЕМ КЛЮЧИ =====
            if (keyItems != null) {
                for (KeySaveData data : keyItems) {
                    try {
                        Door.DoorColor color = Door.DoorColor.valueOf(data.color);
                        KeyItem keyItem = new KeyItem(color, data.count);
                        inventory.addItemToInventory(keyItem);
                        System.out.println("✅ Восстановлен ключ: " + data.color + " x" + data.count);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Неизвестный цвет ключа: " + data.color);
                    }
                }
            }

            if (currentAmmoCaliber != null) {
                try {
                    Caliber caliber = Caliber.valueOf(currentAmmoCaliber);
                    inventory.setCurrentAmmoType(new AmmoItem(caliber, 0, currentAmmoImproved));
                } catch (Exception e) {
                    System.err.println("Не удалось восстановить тип снарядов: " + e.getMessage());
                }
            }

            inventory.restoreAmmo(currentAmmo, maxAmmo, reloadCost, burstSize);
        }
    }

    public static class AmmoSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private String caliber;
        private int count;

        AmmoSaveData(String caliber, int count) {
            this.caliber = caliber;
            this.count = count;
        }
    }

    public static class ItemSaveData implements Serializable {
        private static final long serialVersionUID = 1L;
        private Item.ItemType type;
        private int count;

        ItemSaveData(Item.ItemType type, int count) {
            this.type = type;
            this.count = count;
        }
    }
}