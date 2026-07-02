package world;


import entities.*;
import combat.ExperienceSystem;
import java.io.*;
import java.util.*;
import entities.QuestNPC;
import entities.Tree;
import entities.Pavement;
import entities.StorageChest;
import entities.OakPlanks;
import entities.FriendlyUnit;
import inventory.Item;
import inventory.KeyItem;
import inventory.Caliber;
import java.awt.Point;

public class LevelLoader {

    // Формат файла уровня:
    // WALL x y health destructible (1/0)
    // Пример:
    // WALL 10 15 100 1
    // WALL 20 30 999 0 (неразрушаемая)

    public static List<Wall> loadLevel(String levelPath, int gridWidth, int gridHeight) {
        List<Wall> walls = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            System.err.println("Файл уровня не найден: " + levelPath);
            return generateDefaultWalls(gridWidth, gridHeight);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 5 && parts[0].equalsIgnoreCase("WALL")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int health = Integer.parseInt(parts[3]);
                    boolean destructible = Integer.parseInt(parts[4]) == 1;
                    String wallType = "Brick";  // по умолчанию

                    // ===== НОВОЕ: проверяем наличие типа стены =====
                    if (parts.length >= 6) {
                        wallType = parts[5];
                    }

                    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                        walls.add(new Wall(x, y, health, destructible, wallType));
                        System.out.println("Загружена стена: [" + x + "," + y +
                                "] прочность=" + health + ", разрушаемая=" + destructible +
                                ", тип=" + wallType);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки уровня: " + e.getMessage());
            return generateDefaultWalls(gridWidth, gridHeight);
        }

        return walls;
    }

    public static List<Tree> loadTrees(String levelPath, int gridWidth, int gridHeight) {
        List<Tree> trees = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            return trees;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                // Формат: TREE x y treeType
                if (parts.length >= 4 && parts[0].equalsIgnoreCase("TREE")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    String treeType = parts[3];

                    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                        trees.add(new Tree(x, y, treeType));
                        System.out.println("Загружено дерево: " + treeType + " в [" + x + "," + y + "]");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки деревьев: " + e.getMessage());
        }
        return trees;
    }

    // Добавьте этот метод в класс LevelLoader
    public static List<Pavement> loadPavements(String levelPath, int gridWidth, int gridHeight) {
        List<Pavement> pavements = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            return pavements;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                // Формат: PAVEMENT x y
                if (parts.length >= 3 && parts[0].equalsIgnoreCase("PAVEMENT")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                        pavements.add(new Pavement(x, y));
                        System.out.println("Загружен асфальт в [" + x + "," + y + "]");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки асфальта: " + e.getMessage());
        }
        return pavements;
    }

    public static List<OakPlanks> loadOakPlanks(String levelPath, int gridWidth, int gridHeight) {
        List<OakPlanks> oakPlanks = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            return oakPlanks;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                // Формат: OAKPLANKS x y
                if (parts.length >= 3 && parts[0].equalsIgnoreCase("OAKPLANKS")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                        oakPlanks.add(new OakPlanks(x, y));
                        System.out.println("Загружены дубовые доски в [" + x + "," + y + "]");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки дубовых досок: " + e.getMessage());
        }
        return oakPlanks;
    }

    public static List<IronFloor> loadIronFloors(String levelPath, int gridWidth, int gridHeight) {
        List<IronFloor> ironFloors = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            return ironFloors;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                // ===== ИСПРАВЛЕНИЕ: поддерживаем оба варианта =====
                if (parts.length >= 3 &&
                        (parts[0].equalsIgnoreCase("IRONFLOOR") ||   // ← единственное число
                                parts[0].equalsIgnoreCase("IRONFLOORS"))) {  // ← множественное число
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                        ironFloors.add(new IronFloor(x, y));
                        System.out.println("Загружены железные полы в [" + x + "," + y + "]");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки железных полов: " + e.getMessage());
        }

        System.out.println("📊 ИТОГО загружено железных полов: " + ironFloors.size());
        return ironFloors;
    }

    // Добавьте метод загрузки адской земли:
    public static List<InfernalLand> loadInfernalLands(String levelPath, int gridWidth, int gridHeight) {
        List<InfernalLand> infernalLands = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            return infernalLands;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                // Формат: INFERNALLAND x y
                if (parts.length >= 3 && parts[0].equalsIgnoreCase("INFERNALLAND")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                        infernalLands.add(new InfernalLand(x, y));
                        System.out.println("Загружена адская земля в [" + x + "," + y + "]");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки адской земли: " + e.getMessage());
        }
        return infernalLands;
    }

    // Формат файла: STORAGE x y [TYPE COUNT]...
// Пример: STORAGE 10 15 AMMO_20MM 30 AMMO_203MM 5 MEDKIT 2

    public static List<StorageChest> loadStorageChests(String levelPath, int gridWidth, int gridHeight) {
        List<StorageChest> chests = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            return chests;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                if (parts.length >= 3 && parts[0].equalsIgnoreCase("STORAGE")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                        StorageChest chest = new StorageChest(x, y);

                        for (int i = 3; i + 1 < parts.length; i += 2) {
                            String typeStr = parts[i];
                            int count = Integer.parseInt(parts[i + 1]);

                            System.out.println("Загрузка предмета: " + typeStr + " x" + count);

                            // ===== НОВЫЙ ФОРМАТ СНАРЯДОВ =====
                            if (typeStr.startsWith("AMMO_CALIBER_")) {
                                String caliberName = typeStr.substring("AMMO_CALIBER_".length());
                                try {
                                    Caliber caliber = Caliber.valueOf("CALIBER_" + caliberName);
                                    chest.addAmmo(caliber, count);
                                    System.out.println("  ✅ Загружены снаряды: " + caliberName + " x" + count);
                                } catch (IllegalArgumentException e) {
                                    System.err.println("❌ Неизвестный калибр: " + caliberName);
                                }
                            }

                            else if (typeStr.startsWith("KEY_")) {
                                String colorStr = typeStr.substring(4);
                                try {
                                    Door.DoorColor color = Door.DoorColor.valueOf(colorStr);
                                    KeyItem key = new KeyItem(color, count);
                                    chest.addKey(key);
                                    System.out.println("  ✅ Загружен ключ: " + colorStr + " x" + count);
                                    System.out.println("  📊 Всего ключей в тумбочке: " + chest.getKeys().size());
                                } catch (IllegalArgumentException e) {
                                    System.err.println("❌ Неизвестный цвет ключа: " + colorStr);
                                }
                            }

                            // ===== СТАРЫЙ ФОРМАТ ДЛЯ СОВМЕСТИМОСТИ =====
                            else if (typeStr.startsWith("AMMO_") && !typeStr.startsWith("AMMO_CALIBER_")) {
                                String caliberStr = typeStr.substring(5);
                                if ("20MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_20MM, count);
                                } else if ("8MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_8MM, count);
                                } else if ("13MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_13MM, count);
                                } else if ("25MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_25MM, count);
                                } else if ("45MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_45MM, count);
                                } else if ("47MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_47MM, count);
                                } else if ("203MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_203MM, count);
                                } else if ("30MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_30MM, count);
                                } else if ("37MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_37MM, count);
                                } else if ("76MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_76MM, count);
                                } else if ("105MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_105MM, count);
                                } else if ("128MM".equals(caliberStr)) {
                                    chest.addAmmo(Caliber.CALIBER_128MM, count);
                                }
                            }
                            else if (typeStr.equals("WEAPON_8MM")) {
                                chest.addItem(Item.ItemType.WEAPON_8MM, count);
                            } else if (typeStr.equals("WEAPON_45MM")) {
                                chest.addItem(Item.ItemType.WEAPON_45MM, count);
                            } else if (typeStr.equals("WEAPON_25MM")) {
                                chest.addItem(Item.ItemType.WEAPON_25MM, count);
                            } else if (typeStr.equals("WEAPON_47MM_FRENCH")) {
                                System.out.println("  ✅ ЗАГРУЖАЮ ФРАНЦУЗСКОЕ орудие!");
                                chest.addItem(Item.ItemType.WEAPON_47MM_FRENCH, count);
                            } else if (typeStr.equals("WEAPON_47MM_ITALIAN")) {
                                System.out.println("  ✅ ЗАГРУЖАЮ ИТАЛЬЯНСКОЕ орудие!");
                                chest.addItem(Item.ItemType.WEAPON_47MM_ITALIAN, count);
                            } else if (typeStr.equals("WEAPON_76MM")) {
                                chest.addItem(Item.ItemType.WEAPON_76MM, count);
                            } else if (typeStr.equals("WEAPON_105MM")) {
                                chest.addItem(Item.ItemType.WEAPON_105MM, count);
                            } else if (typeStr.equals("WEAPON_128MM")) {
                                chest.addItem(Item.ItemType.WEAPON_128MM, count);
                            } else if (typeStr.equals("WEAPON_203MM")) {
                                chest.addItem(Item.ItemType.WEAPON_203MM, count);
                            } else if (typeStr.equals("WEAPON")) {
                                chest.addItem(Item.ItemType.WEAPON, count);
                            } else if (typeStr.equals("WEAPON_30MM")) {
                                chest.addItem(Item.ItemType.WEAPON_30MM, count);
                            } else if (typeStr.equals("WEAPON_37MM_ITALIAN")) {
                                chest.addItem(Item.ItemType.WEAPON_37MM_ITALIAN, count);
                            } else if (typeStr.equals("WEAPON_37MM_AMERICAN")) {
                                chest.addItem(Item.ItemType.WEAPON_37MM_AMERICAN, count);
                            } else if (typeStr.equals("WEAPON_37MM_SWEDEN")) {
                                chest.addItem(Item.ItemType.WEAPON_37MM_SWEDEN, count);
                            } else if (typeStr.equals("WEAPON_13MM_JAPAN")) {
                                chest.addItem(Item.ItemType.WEAPON_13MM_JAPAN, count);
                            } else if (typeStr.equals("WEAPON_13MM_FRENCH")) {
                                chest.addItem(Item.ItemType.WEAPON_13MM_FRENCH, count);
                            }

                            else {
                                try {
                                    Item.ItemType type = Item.ItemType.valueOf(typeStr);
                                    chest.addItem(type, count);
                                } catch (IllegalArgumentException ignored) {
                                    System.err.println("⚠️ Неизвестный тип: " + typeStr);
                                }
                            }
                        }

                        chests.add(chest);
                        System.out.println("Загружена тумбочка в [" + x + "," + y + "]");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки тумбочек: " + e.getMessage());
        }
        return chests;
    }

    public static Point loadStartPosition(String levelPath, int gridWidth, int gridHeight) {
        File levelFile = new File(levelPath);
        if (!levelFile.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 3 && parts[0].equalsIgnoreCase("START")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    // Проверяем, что позиция валидна и не выходит за границы
                    if (x >= 0 && x + 5 <= gridWidth && y >= 0 && y + 5 <= gridHeight) {
                        System.out.println("Загружена стартовая позиция: [" + x + "," + y + "] (размер 5x5)");
                        return new Point(x, y);  // ← Теперь Point доступен
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки стартовой позиции: " + e.getMessage());
        }
        return null;
    }

    // Дефолтные стены для тестирования
    private static List<Wall> generateDefaultWalls(int gridWidth, int gridHeight) {
        List<Wall> walls = new ArrayList<>();
        Random rand = new Random(123);

        // Добавляем несколько случайных стен для примера
        int wallCount = 20 + rand.nextInt(30);
        for (int i = 0; i < wallCount; i++) {
            int x = rand.nextInt(gridWidth);
            int y = rand.nextInt(gridHeight);
            int health = 50 + rand.nextInt(150);
            boolean destructible = rand.nextBoolean();
            walls.add(new Wall(x, y, health, destructible));
        }

        System.out.println("Сгенерировано " + walls.size() + " дефолтных стен");
        return walls;
    }

    // В LevelLoader.java
    public static List<QuestNPC> loadNPCs(String filePath, int gridWidth, int gridHeight) {
        List<QuestNPC> npcs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                if (parts.length >= 5 && parts[0].equals("NPC")) {
                    try {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);

                        String name = "";
                        String npcType = "";
                        int questTarget = 0;

                        // ===== ИСПРАВЛЕННЫЙ ПАРСИНГ =====
                        // Проверяем, есть ли кавычки в имени
                        boolean hasQuotes = false;

                        // Сначала проверяем, не является ли parts[3] именем с кавычками
                        if (parts[3].startsWith("\"") && parts[3].endsWith("\"")) {
                            // Имя в кавычках - это одна часть
                            name = parts[3].substring(1, parts[3].length() - 1);
                            // Тогда тип - это parts[4], а цель - parts[5]
                            if (parts.length >= 6) {
                                npcType = parts[4];
                                questTarget = Integer.parseInt(parts[5]);
                            }
                            System.out.println("  Парсинг (кавычки): name='" + name + "', type='" + npcType + "', target=" + questTarget);
                        } else {
                            // Имя без кавычек - может состоять из нескольких частей
                            // Ищем тип среди частей
                            int typeStartIndex = -1;

                            // Проверяем, есть ли среди частей "T18"
                            for (int i = 3; i < parts.length; i++) {
                                if (parts[i].equals("T18")) {
                                    typeStartIndex = i;
                                    break;
                                }
                            }

                            // Если не нашли T18, проверяем "Sav m/43" (две части) или "Sav_m/43"
                            if (typeStartIndex == -1) {
                                for (int i = 3; i < parts.length - 1; i++) {
                                    if (parts[i].equals("Sav") && parts[i+1].equals("m/43")) {
                                        typeStartIndex = i;
                                        break;
                                    }
                                }
                            }
                            if (typeStartIndex == -1) {
                                for (int i = 3; i < parts.length; i++) {
                                    if (parts[i].equals("Sav_m/43")) {
                                        typeStartIndex = i;
                                        break;
                                    }
                                }
                            }

                            if (typeStartIndex != -1) {
                                // Собираем имя из частей до типа
                                StringBuilder nameBuilder = new StringBuilder();
                                for (int i = 3; i < typeStartIndex; i++) {
                                    if (i > 3) nameBuilder.append(" ");
                                    nameBuilder.append(parts[i]);
                                }
                                name = nameBuilder.toString().trim();

                                // Если имя пустое - используем тип как имя
                                if (name.isEmpty()) {
                                    // Определяем, какой это NPC
                                    if (parts[typeStartIndex].equals("Sav") || parts[typeStartIndex].equals("Sav_m/43")) {
                                        name = "Sav m/43";
                                    } else {
                                        name = parts[typeStartIndex];
                                    }
                                }

                                // Собираем тип
                                StringBuilder typeBuilder = new StringBuilder();
                                for (int i = typeStartIndex; i < parts.length - 1; i++) {
                                    if (i > typeStartIndex) typeBuilder.append(" ");
                                    typeBuilder.append(parts[i]);
                                }
                                npcType = typeBuilder.toString().trim();
                                questTarget = Integer.parseInt(parts[parts.length - 1]);
                            } else {
                                // Если тип не найден, предполагаем, что имя - это parts[3], тип - parts[4]
                                name = parts[3];
                                npcType = parts[4];
                                questTarget = Integer.parseInt(parts[5]);
                            }
                        }

                        System.out.println("Парсинг NPC: name='" + name + "', type='" + npcType + "', target=" + questTarget);

                        QuestNPC npc;
                        // ===== ПРОВЕРЯЕМ ПРАВИЛЬНО =====
                        boolean isSavM43 =
                                "Sav m/43".equals(name) ||
                                        "Sav_m/43".equals(name) ||
                                        "Sav m/43".equals(npcType) ||
                                        "Sav_m/43".equals(npcType) ||
                                        (name.contains("Sav") && npcType.contains("m/43"));

                        if (isSavM43) {
                            npc = QuestNPC.createSavM43(x, y);
                            System.out.println("  ✅ Создан Sav m/43 с именем: '" + npc.name + "'");
                        } else {
                            // Если имя пустое - используем "T18"
                            if (name == null || name.trim().isEmpty()) {
                                name = "T18";
                            }
                            npc = new QuestNPC(x, y, name);
                            System.out.println("  ✅ Создан T18 с именем: '" + npc.name + "'");
                        }

                        npc.questTarget = questTarget;
                        npc.questText = "Уничтожь " + questTarget + " врагов!";
                        npc.questDescription = "Уничтожь " + questTarget + " противников на поле боя.";
                        npcs.add(npc);

                        System.out.println("✅ Загружен NPC: " + npc.name + " (тип: " + npcType +
                                ", цель: " + questTarget + ") в [" + x + "," + y + "]");

                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка парсинга NPC: " + line + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки NPC: " + e.getMessage());
        }

        return npcs;
    }

    // Добавьте метод загрузки:
    public static List<FriendlyUnit> loadFriendlyUnits(String levelPath, int gridWidth, int gridHeight) {
        List<FriendlyUnit> friendlies = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            return friendlies;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                // Разбиваем строку, но учитываем возможные кавычки в имени
                String[] parts = line.split("\\s+");

                if (parts.length >= 5 && parts[0].equalsIgnoreCase("FRIENDLY")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    String name = parts[3];
                    String type = parts[4];

                    // Убираем кавычки из имени, если они есть
                    if (name.startsWith("\"") && name.endsWith("\"")) {
                        name = name.substring(1, name.length() - 1);
                    }

                    int upgradeLevel = 1;
                    String upgradeClass = null;
                    int expLevel = 1;
                    int expPoints = 0;

                    // Индекс для чтения следующих параметров
                    int idx = 5;

                    // upgradeLevel
                    if (idx < parts.length) {
                        try {
                            upgradeLevel = Integer.parseInt(parts[idx]);
                            idx++;
                        } catch (NumberFormatException e) {
                            // Если не число, значит это имя или тип
                            System.err.println("  Предупреждение: не удалось распарсить upgradeLevel: " + parts[idx]);
                            // Пропускаем этот элемент и продолжаем
                            idx++;
                        }
                    }

                    // upgradeClass
                    if (idx < parts.length) {
                        String cls = parts[idx];
                        if (!"NONE".equals(cls) && !"null".equals(cls)) {
                            upgradeClass = cls;
                        }
                        idx++;
                    }

                    // expLevel
                    if (idx < parts.length) {
                        try {
                            expLevel = Integer.parseInt(parts[idx]);
                            idx++;
                        } catch (NumberFormatException e) {
                            System.err.println("  Предупреждение: не удалось распарсить expLevel: " + parts[idx]);
                            idx++;
                        }
                    }

                    // expPoints
                    if (idx < parts.length) {
                        try {
                            expPoints = Integer.parseInt(parts[idx]);
                            idx++;
                        } catch (NumberFormatException e) {
                            System.err.println("  Предупреждение: не удалось распарсить expPoints: " + parts[idx]);
                        }
                    }

                    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                        // Создаём союзника с БАЗОВЫМИ характеристиками
                        FriendlyUnit friendly = new FriendlyUnit(x, y, name, type);

                        // ===== СОХРАНЯЕМ значения модернизации, но НЕ ПРИМЕНЯЕМ =====
                        friendly.setUpgradeLevel(upgradeLevel);
                        friendly.setUpgradeClass(upgradeClass);

                        // ===== СОХРАНЯЕМ значения опыта, но НЕ ПРИМЕНЯЕМ =====
                        if (expLevel > 1 || expPoints > 0) {
                            ExperienceSystem expSys = new ExperienceSystem(type);
                            for (int i = 1; i < expLevel; i++) {
                                expSys.addExperience(expSys.getExperienceForNextLevel());
                            }
                            if (expPoints > 0) {
                                expSys.addExperience(expPoints);
                            }
                            friendly.setExperienceSystem(expSys);
                        }

                        friendlies.add(friendly);
                        System.out.println("Загружен союзник: " + name + " (тип: " + type +
                                ") в [" + x + "," + y + "]");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки союзников: " + e.getMessage());
        }
        return friendlies;
    }

    // В LevelLoader.java добавьте, если отсутствует:
    public static List<Enemy> loadEnemies(String levelPath, int gridWidth, int gridHeight, GameWorld gameWorld) {
        List<Enemy> enemies = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) return enemies;

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                // НОВЫЙ ФОРМАТ С ФРАКЦИЕЙ
                if (parts.length >= 12 && parts[0].equalsIgnoreCase("ENEMY")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    String enemyType = parts[3];
                    String factionStr = parts[4];                    // ← Фракция
                    int health = Integer.parseInt(parts[5]);
                    Enemy.Direction dir = Enemy.Direction.valueOf(parts[6]);
                    int strength = Integer.parseInt(parts[7]);
                    int agility = Integer.parseInt(parts[8]);
                    int armor = Integer.parseInt(parts[9]);
                    int nimble = Integer.parseInt(parts[10]);
                    int behavior = Integer.parseInt(parts[11]);

                    Enemy enemy = new Enemy(x, y, dir);
                    enemy.setType(enemyType);

                    // Устанавливаем фракцию
                    try {
                        Faction faction = Faction.valueOf(factionStr);
                        enemy.setFaction(faction);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Неизвестная фракция: " + factionStr + ", используем NEUTRAL");
                        enemy.setFaction(Faction.NEUTRAL);
                    }

                    // Если это R_Otsu - применяем специальные статы
                    if ("R_Otsu".equals(enemyType)) {
                        enemy.setR_OtsuStats();
                    } else if ("M14_41".equals(enemyType)) {
                        enemy.setM14_41Stats();
                    } else if ("H35".equals(enemyType)) {
                        enemy.setH35Stats();
                    } else if ("FT".equals(enemyType)) {  // ← ДОБАВИТЬ
                        enemy.setFTStats();                // ← ДОБАВИТЬ
                    } else if ("Fiat3000".equals(enemyType)) {  // ← ДОБАВИТЬ
                        enemy.setFiat3000Stats();                // ← ДОБАВИТЬ
                    }

                    enemy.health = health;
                    enemy.maxHealth = health;
                    enemy.strength = strength;
                    enemy.agility = agility;
                    enemy.armor = armor;
                    enemy.nimble = nimble;
                    enemy.calculateDodgeChance();
                    enemy.calculateMovePoints();

                    switch(behavior) {
                        case 0: enemy.setBehaviorType(Enemy.BehaviorType.AGGRESSIVE); break;
                        case 1: enemy.setBehaviorType(Enemy.BehaviorType.TACTICAL); break;
                        case 2: enemy.setBehaviorType(Enemy.BehaviorType.SNIPER); break;
                    }

                    enemy.setGameWorld(gameWorld);
                    enemies.add(enemy);
                    System.out.println("Загружен враг: " + enemyType + ", фракция: " + factionStr);
                }
                // СТАРЫЙ ФОРМАТ: ENEMY x y type health direction strength agility armor nimble behavior
                else if (parts.length >= 11 && parts[0].equalsIgnoreCase("ENEMY")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    String enemyType = parts[3];                    // ← тип: "Leichttraktor" или "R_Otsu"
                    int health = Integer.parseInt(parts[4]);
                    Enemy.Direction dir = Enemy.Direction.valueOf(parts[5]);
                    int strength = Integer.parseInt(parts[6]);
                    int agility = Integer.parseInt(parts[7]);
                    int armor = Integer.parseInt(parts[8]);
                    int nimble = Integer.parseInt(parts[9]);
                    int behavior = Integer.parseInt(parts[10]);

                    Enemy enemy = new Enemy(x, y, dir);
                    enemy.setType(enemyType);                       // ← УСТАНАВЛИВАЕМ ТИП!

                    // Если это R_Otsu - применяем специальные статы
                    if ("R_Otsu".equals(enemyType)) {
                        enemy.setR_OtsuStats();
                    }

                    enemy.health = health;
                    enemy.maxHealth = health;
                    enemy.strength = strength;
                    enemy.agility = agility;
                    enemy.armor = armor;
                    enemy.nimble = nimble;
                    enemy.setGameWorld(gameWorld);
                    enemy.calculateDodgeChance();
                    enemy.calculateMovePoints();

                    switch(behavior) {
                        case 0: enemy.setBehaviorType(Enemy.BehaviorType.AGGRESSIVE); break;
                        case 1: enemy.setBehaviorType(Enemy.BehaviorType.TACTICAL); break;
                        case 2: enemy.setBehaviorType(Enemy.BehaviorType.SNIPER); break;
                    }

                    enemy.setGameWorld(gameWorld);
                    enemies.add(enemy);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки врагов: " + e.getMessage());
        }
        return enemies;
    }

    // Загрузка мусорных контейнеров
    public static List<GarbageContainer> loadGarbageContainers(String levelPath, int gridWidth, int gridHeight) {
        List<GarbageContainer> containers = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            return containers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                // Формат: GARBAGE x y
                if (parts.length >= 3 && parts[0].equalsIgnoreCase("GARBAGE")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    // Проверка границ для размера 2x1
                    if (x >= 0 && x + 2 <= gridWidth && y >= 0 && y + 1 <= gridHeight) {
                        GarbageContainer container = new GarbageContainer(x, y);

                        // Загружаем содержимое (если есть)
                        for (int i = 3; i + 1 < parts.length; i += 2) {
                            String typeStr = parts[i];
                            int count = Integer.parseInt(parts[i + 1]);

                            if (typeStr.startsWith("AMMO_")) {
                                String caliberStr = typeStr.substring(5);
                                if ("20MM".equals(caliberStr)) {
                                    container.addAmmo(Caliber.CALIBER_20MM, count);
                                } else if ("203MM".equals(caliberStr)) {
                                    container.addAmmo(Caliber.CALIBER_203MM, count);
                                } else if ("8MM".equals(caliberStr)) {  // ← ДОБАВИТЬ
                                    container.addAmmo(Caliber.CALIBER_8MM, count);
                                } else if ("13MM".equals(caliberStr)) {  // ← ДОБАВИТЬ
                                    container.addAmmo(Caliber.CALIBER_13MM, count);
                                } else if ("25MM".equals(caliberStr)) {  // ← ДОБАВИТЬ
                                    container.addAmmo(Caliber.CALIBER_25MM, count);
                                } else if ("30MM".equals(caliberStr)) {  // ← ДОБАВИТЬ
                                    container.addAmmo(Caliber.CALIBER_30MM, count);
                                } else if ("37MM".equals(caliberStr)) {  // ← ДОБАВИТЬ
                                    container.addAmmo(Caliber.CALIBER_37MM, count);
                                } else if ("45MM".equals(caliberStr)) {  // ← ДОБАВИТЬ
                                    container.addAmmo(Caliber.CALIBER_45MM, count);
                                } else if ("47MM".equals(caliberStr)) {  // ← ДОБАВИТЬ
                                    container.addAmmo(Caliber.CALIBER_47MM, count);
                                } else if ("76MM".equals(caliberStr)) {  // ← ДОБАВИТЬ
                                    container.addAmmo(Caliber.CALIBER_76MM, count);
                                } else if ("105MM".equals(caliberStr)) {  // ← ДОБАВИТЬ
                                    container.addAmmo(Caliber.CALIBER_105MM, count);
                                } else if ("128MM".equals(caliberStr)) {  // ← ДОБАВИТЬ
                                    container.addAmmo(Caliber.CALIBER_128MM, count);
                                } else if ("203MM".equals(caliberStr)) {  // ← ДОБАВИТЬ
                                    container.addAmmo(Caliber.CALIBER_203MM, count);
                                }
                            } else {
                                try {
                                    Item.ItemType type = Item.ItemType.valueOf(typeStr);
                                    container.addItem(type, count);
                                } catch (IllegalArgumentException ignored) {}
                            }
                        }

                        containers.add(container);
                        System.out.println("Загружен мусорный контейнер в [" + x + "," + y + "] (размер 2x1)");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки мусорных контейнеров: " + e.getMessage());
        }
        return containers;
    }

    // Метод для сохранения уровня (удобно для редактора)
    // Добавьте параметр startPosition в saveLevel или создайте отдельный метод
    public static void saveLevel(String levelPath, List<Wall> walls) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(levelPath))) {
            writer.println("// Формат: WALL x y health destructible(1/0)");
            writer.println("// Примеры:");
            writer.println("// WALL 10 15 100 1  - разрушаемая стена");
            writer.println("// WALL 20 30 999 0  - неразрушаемая стена\n");

            for (Wall wall : walls) {
                writer.printf("WALL %d %d %d %d %s\n",
                        wall.gridX, wall.gridY, wall.health,
                        wall.isDestructible ? 1 : 0,
                        wall.wallType != null ? wall.wallType : "Brick");
            }
            System.out.println("Стены сохранены: " + walls.size());
        } catch (IOException e) {
            System.err.println("Ошибка сохранения стен: " + e.getMessage());
        }
    }

    public static void saveWaters(String levelPath, List<Water> waters) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(levelPath, true))) {
            writer.println("\n// ===== WATERS =====");
            for (Water water : waters) {
                writer.printf("WATER %d %d\n", water.gridX, water.gridY);
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения воды: " + e.getMessage());
        }
    }

    // Сохранение тумбочек
    public static void saveStorageChests(String levelPath, List<StorageChest> chests) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(levelPath, true))) {
            writer.println();
            writer.println("// ===== STORAGE CHESTS =====");
            for (StorageChest chest : chests) {
                writer.printf("STORAGE %d %d", chest.gridX, chest.gridY);
                for (Map.Entry<Item.ItemType, Integer> entry : chest.getItems().entrySet()) {
                    writer.printf(" %s %d", entry.getKey().name(), entry.getValue());
                }
                writer.println();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения тумбочек: " + e.getMessage());
        }
    }

    public static void saveIronFloors(String levelPath, List<IronFloor> ironFloors) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(levelPath, true))) {
            writer.println("\n// ===== IRON FLOOR =====");
            for (IronFloor floor : ironFloors) {
                writer.printf("IRONFLOOR %d %d\n", floor.gridX, floor.gridY);
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения железных полов: " + e.getMessage());
        }
    }

    public static List<Trader> loadTraders(String levelPath, int gridWidth, int gridHeight) {
        List<Trader> traders = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            return traders;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                // Формат: TRADER x y name
                if (parts.length >= 4 && parts[0].equalsIgnoreCase("TRADER")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    String name = parts[3];

                    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                        traders.add(new Trader(x, y, name));
                        System.out.println("Загружен торговец: " + name + " в [" + x + "," + y + "]");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки торговцев: " + e.getMessage());
        }
        return traders;
    }

    public static List<Water> loadWaters(String levelPath, int gridWidth, int gridHeight) {
        List<Water> waters = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            return waters;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                // Формат: WATER x y
                if (parts.length >= 3 && parts[0].equalsIgnoreCase("WATER")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                        waters.add(new Water(x, y));
                        System.out.println("Загружена вода в [" + x + "," + y + "]");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки воды: " + e.getMessage());
        }
        return waters;
    }

    // Добавьте метод загрузки дверей:
    public static List<Door> loadDoors(String levelPath, int gridWidth, int gridHeight) {
        List<Door> doors = new ArrayList<>();
        File levelFile = new File(levelPath);

        if (!levelFile.exists()) {
            return doors;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");

                // Формат: DOOR x y COLOR OPEN/CLOSED
                if (parts.length >= 4 && parts[0].equalsIgnoreCase("DOOR")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    Door.DoorColor color = Door.DoorColor.valueOf(parts[3]);
                    boolean isOpen = parts.length >= 5 && parts[4].equalsIgnoreCase("OPEN");

                    if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                        Door door = new Door(x, y, color);
                        if (isOpen) {
                            door.open();
                        }
                        doors.add(door);
                        System.out.println("Загружена дверь: " + color + " в [" + x + "," + y +
                                "], открыта: " + isOpen);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки дверей: " + e.getMessage());
        }
        return doors;
    }

    // В LevelLoader.java добавьте метод загрузки победной зоны
    public static Point loadWinZone(String levelPath, int gridWidth, int gridHeight) {
        File levelFile = new File(levelPath);
        if (!levelFile.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(levelFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 3 && parts[0].equalsIgnoreCase("WINZONE")) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    // Проверяем, что позиция валидна и не выходит за границы
                    if (x >= 0 && x + 5 <= gridWidth && y >= 0 && y + 5 <= gridHeight) {
                        System.out.println("Загружена победная зона: [" + x + "," + y + "] (размер 5x5)");
                        return new Point(x, y);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки победной зоны: " + e.getMessage());
        }
        return null;
    }
}