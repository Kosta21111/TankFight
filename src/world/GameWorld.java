package world;

import audio.SoundManager;
import entities.*;
import ui.GamePanel;
import inventory.Inventory;
import inventory.Item;
import combat.Projectile;
import inventory.AmmoItem;
import inventory.Caliber;
import inventory.WeaponLibrary;  // ← ДОБАВЬТЕ ЭТУ СТРОКУ

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.Queue;
import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.io.*;  // ← ДОБАВЬТЕ ЭТУ СТРОКУ (для Serializable)

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class GameWorld {

    private List<GunshotSound> gunshotSounds = new ArrayList<>();
    private int currentTurn = 0;  // Счётчик ходов

    // В начале класса GameWorld, после других полей:
    private Point winZone = null;  // Победная зона (5x5)
    private boolean levelCompleted = false;

    private boolean fullMapRevealed = false;  // Флаг для чита видимости всей карты
    private List<Wall> walls = new ArrayList<>();
    private List<Trader> traders = new ArrayList<>();
    private List<OakPlanks> oakPlanks = new ArrayList<>();
    private List<InfernalLand> infernalLands = new ArrayList<>();
    private List<FriendlyUnit> friendlyUnits = new ArrayList<>();
    private List<Door> doors = new ArrayList<>();
    // В начале класса GameWorld, после других полей, добавьте:
    private List<FriendlyUnit> controllableUnits = new ArrayList<>();  // Список управляемых юнитов
    private Map<FriendlyUnit, boolean[][]> friendlyVisibilityCache = new HashMap<>();

    public static final int FIELD_SIZE = 4000;
    public static final int CELL_SIZE = 50;
    public static final int TANK_SIZE = 40;
    public static final int VIEW_RADIUS = 10;
    public static final int ENEMY_VIEW_RADIUS = 8; // Враги видят чуть хуже

    private List<LootDrop> lootDrops = new ArrayList<>();
    private PlayerTank player;

    private GamePanel gamePanel;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Hill> hills = new ArrayList<>();
    private List<Tree> trees = new ArrayList<>();  // ← ДОБАВИТЬ
    private List<Water> waters = new ArrayList<>();
    private List<Pavement> pavements = new ArrayList<>();  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
    private List<IronFloor> ironFloors = new ArrayList<>();  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
    private List<StorageChest> storageChests = new ArrayList<>();

    private Random random = new Random();
    private boolean[][] visibilityMap;
    private boolean[][] combinedVisibilityMap;
    private boolean[][] friendlyVisibilityMap;
    private BufferedImage fieldCache;

    private BufferedImage m53Image;

    private SoundManager soundManager;  // ← ДОБАВЛЕНО

    // Флаг для хода врагов
    private boolean isEnemyTurn = false;
    private int currentEnemyIndex = 0;

    private List<QuestNPC> questNPCs = new ArrayList<>();
    private List<GarbageContainer> garbageContainers = new ArrayList<>();
    private int totalEnemiesKilled = 0;
    private String currentLevelPath;

    // В GameWorld.java, добавьте поле
    private Enemy currentActiveEnemy = null;

    public void setCurrentActiveEnemy(Enemy enemy) {
        this.currentActiveEnemy = enemy;
    }

    public Enemy getCurrentActiveEnemy() {
        return currentActiveEnemy;
    }

    // Добавьте getter для стен:
    public List<Wall> getWalls() { return walls; }
    public List<Door> getDoors() { return doors; }

    public GameWorld(PlayerTank player) {
        this.player = player;
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        visibilityMap = new boolean[gridWidth][gridHeight];
        combinedVisibilityMap = new boolean[gridWidth][gridHeight];
        friendlyVisibilityMap = new boolean[gridWidth][gridHeight];
    }

    private boolean isCellVisibleToPlayer(int gridX, int gridY) {
        if (gridX < 0 || gridX >= visibilityMap.length || gridY < 0 || gridY >= visibilityMap[0].length) {
            return false;
        }
        return visibilityMap[gridX][gridY];
    }

    // Добавьте метод загрузки NPC:
    public void loadNPCs(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        questNPCs = LevelLoader.loadNPCs(levelPath, gridWidth, gridHeight);

        // ===== ПЕРЕДАЁМ SoundManager ВСЕМ NPC =====
        for (QuestNPC npc : questNPCs) {
            npc.setSoundManager(soundManager);
            if ("Sav m/43".equals(npc.name)) {
                System.out.println("✅ Sav m/43 получил SoundManager, звуки: Received=" +
                        (npc.getQuestReceivedSound() != null) + ", Done=" +
                        (npc.getQuestDoneSound() != null));
            }
        }
    }

    // Добавьте метод загрузки мусорных контейнеров в GameWorld
    public void loadGarbageContainers(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        List<GarbageContainer> loaded = LevelLoader.loadGarbageContainers(levelPath, gridWidth, gridHeight);
        if (!loaded.isEmpty()) {
            garbageContainers.clear();
            garbageContainers.addAll(loaded);
            System.out.println("Загружено мусорных контейнеров: " + garbageContainers.size());
        }
    }

    public boolean canMoveToForFriendly(FriendlyUnit friendly, int gridX, int gridY) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        if (gridX < 0 || gridX >= gridWidth || gridY < 0 || gridY >= gridHeight) return false;
        if (gridX == player.gridX && gridY == player.gridY) return false;

        for (Enemy e : enemies) {
            if (e.isAlive && e.gridX == gridX && e.gridY == gridY) return false;
        }
        for (Wall wall : walls) {
            if (wall.isAlive() && wall.gridX == gridX && wall.gridY == gridY) return false;
        }
        for (Tree tree : trees) {
            if (tree.isAlive && tree.gridX == gridX && tree.gridY == gridY) return false;
        }
        for (FriendlyUnit f : friendlyUnits) {
            if (f != friendly && f.isAlive && f.gridX == gridX && f.gridY == gridY) return false;
        }

        // ===== ВОДА НЕПРОХОДИМА! =====
        if (isWaterAt(gridX, gridY)) return false;

        // ===== ЗАКРЫТЫЕ ДВЕРИ НЕПРОХОДИМЫ ДЛЯ СОЮЗНИКОВ! =====
        if (!canMoveThroughDoor(gridX, gridY)) {
            System.out.println("  🚪 " + friendly.name + " не может пройти через закрытую дверь в [" + gridX + "," + gridY + "]");
            return false;
        }

        return true;
    }


    public void loadTrees(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        trees = LevelLoader.loadTrees(levelPath, gridWidth, gridHeight);
        System.out.println("Загружено деревьев: " + trees.size());
    }

    public void loadOakPlanks(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        oakPlanks = LevelLoader.loadOakPlanks(levelPath, gridWidth, gridHeight);
        System.out.println("Загружено дубовых досок: " + oakPlanks.size());
    }

    public void loadIronFloors(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        ironFloors = LevelLoader.loadIronFloors(levelPath, gridWidth, gridHeight);
        System.out.println("Загружено железных полов: " + ironFloors.size());  // ← Добавьте эту строку
    }

    public void loadStorageChests(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        storageChests = LevelLoader.loadStorageChests(levelPath, gridWidth, gridHeight);
        System.out.println("Загружено тумбочек: " + storageChests.size());
    }

    public boolean isTreeAt(int gridX, int gridY) {
        for (Tree tree : trees) {
            if (tree.isAlive && tree.gridX == gridX && tree.gridY == gridY) {
                return true;
            }
        }
        return false;
    }

    public boolean isInfernalLandAt(int gridX, int gridY) {
        for (InfernalLand land : infernalLands) {
            if (land.isAlive && land.gridX == gridX && land.gridY == gridY) {
                return true;
            }
        }
        return false;
    }

    public Tree getTreeAt(int gridX, int gridY) {
        for (Tree tree : trees) {
            if (tree.isAlive && tree.gridX == gridX && tree.gridY == gridY) {
                return tree;
            }
        }
        return null;
    }

    public List<Water> getWaters() { return waters; }

    public boolean isWaterAt(int gridX, int gridY) {
        for (Water water : waters) {
            if (water.isAlive && water.gridX == gridX && water.gridY == gridY) {
                return true;
            }
        }
        return false;
    }

    public void damageTree(int gridX, int gridY, int damage) {
        Tree tree = getTreeAt(gridX, gridY);
        if (tree != null) {
            tree.takeDamage(damage);
            if (!tree.isAlive) {
                System.out.println("Дерево срублено! [" + gridX + "," + gridY + "]");
                trees.remove(tree);
            }
        }
    }

    // Добавьте метод для обновления прогресса квестов при убийстве врага:
    public void updateQuestProgress() {
        totalEnemiesKilled++;

        // Шанс выпадения предметов
        Random rand = new Random();
        if (rand.nextDouble() < 0.3) { // 30% шанс
            int ammoDrop = 5 + rand.nextInt(11); // 5-15 снарядов
            player.getInventory().addItem(Item.ItemType.AMMO, ammoDrop);
            System.out.println("📦 Получено снарядов: " + ammoDrop);
        }

        if (rand.nextDouble() < 0.15) { // 15% шанс
            player.getInventory().addItem(Item.ItemType.MEDKIT, 1);
            System.out.println("💊 Найдена аптечка!");
        }

        for (QuestNPC npc : questNPCs) {
            if (!npc.isQuestCompleted && npc.hasReceivedQuest() &&
                    npc.questType == QuestNPC.QuestType.KILL_ENEMIES) {
                npc.updateProgress(totalEnemiesKilled);
                System.out.println("Квест обновлён: " + npc.getStatusText());
            }
        }
    }

    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    public void setGamePanel(GamePanel panel) {
        this.gamePanel = panel;
    }

    // В GameWorld.java
    public void debugPrintFactionCounts() {
        System.out.println("\n=== СТАТИСТИКА ФРАКЦИЙ ===");

        int germany = 0;
        int japan = 0;
        int france = 0;
        int italy = 0;
        int neutral = 0;
        int total = 0;

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive) continue;
            total++;
            switch (enemy.getFaction()) {
                case GERMANY: germany++; break;
                case JAPAN: japan++; break;
                case FRANCE: france++; break;
                case ITALY: italy++; break;
                case NEUTRAL: neutral++; break;
            }
        }

        System.out.println("  🇩🇪 Германия: " + germany);
        System.out.println("  🇯🇵 Япония: " + japan);
        System.out.println("  🇫🇷 Франция: " + france);
        System.out.println("  🇮🇹 Италия: " + italy);
        System.out.println("  ⚪ Нейтральные: " + neutral);
        System.out.println("  📊 Всего живых врагов: " + total);

        // Проверка для квеста Дедушки-Шведа
        if (france == 0 && italy == 0) {
            System.out.println("✅ КВЕСТ ДЕДУШКИ-ШВЕДА: ВСЕ ВРАГИ ФРАНЦИИ И ИТАЛИИ УНИЧТОЖЕНЫ!");
        } else {
            System.out.println("⚠ КВЕСТ ДЕДУШКИ-ШВЕДА: ОСТАЛИСЬ ВРАГИ:");
            if (france > 0) System.out.println("  🇫🇷 Франция: " + france + " шт.");
            if (italy > 0) System.out.println("  🇮🇹 Италия: " + italy + " шт.");
        }
        System.out.println("============================\n");
    }

    public void checkAllQuestsAfterLoad() {
        System.out.println("=== ПРОВЕРКА ВСЕХ КВЕСТОВ ПОСЛЕ ЗАГРУЗКИ ===");

        for (QuestNPC npc : questNPCs) {
            if ("Sav m/43".equals(npc.name) && !npc.isQuestFinished) {
                // Принудительно проверяем квест
                boolean completed = npc.isQuestCompletedForFactions(enemies);
                if (completed && !npc.isQuestCompleted) {
                    npc.isQuestCompleted = true;
                    npc.isQuestFinished = true;
                    System.out.println("✅ Квест Sav m/43 выполнен при загрузке!");

                    // Можно сразу дать награду, если хотите
                    // Но лучше оставить для ручного получения
                }
            }
        }
    }

    // НОВЫЙ МЕТОД: Запуск хода врагов
    public void startEnemyTurn() {

        gunshotSounds.removeIf(s -> currentTurn - s.turnHeard > 1);  // храним только 1 ход
        currentTurn++;  // Увеличиваем счётчик ходов
        isEnemyTurn = true;
        currentEnemyIndex = 0;

        for (Enemy enemy : enemies) {
            if (enemy.isAlive) {
                enemy.startTurn();
                System.out.println("Враг [" + enemy.gridX + "," + enemy.gridY +
                        "] получил " + enemy.movePoints + " очков хода, стоимость шага: " + enemy.moveCost);
            }
        }

        processNextEnemy();
    }

    // В GameWorld.java, добавьте метод:
    public void updateCombinedVisibilityMap() {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        // Сбрасываем комбинированную карту
        for (int i = 0; i < gridWidth; i++) {
            Arrays.fill(combinedVisibilityMap[i], false);
        }

        // Добавляем видимость от игрока
        addVisibilityFromPosition(player.gridX, player.gridY, combinedVisibilityMap);

        // Добавляем видимость от всех нанятых союзников
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                addVisibilityFromPosition(friendly.gridX, friendly.gridY, combinedVisibilityMap);
            }
        }
    }

    // В GameWorld.java, добавьте метод для обновления кэша после движения союзника:
    public void invalidateFriendlyVisibilityCache(FriendlyUnit friendly) {
        friendlyVisibilityCache.remove(friendly);
        // При следующем вызове isEnemyVisibleFromFriendly карта будет пересоздана
    }

    public void updateAllFriendlyVisibilityMaps() {
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isRecruited && friendly.isAlive) {
                updateFriendlyVisibilityMap(friendly);
            }
        }
    }

    // В GameWorld.java, добавьте метод:
    // В GameWorld.java, проверьте метод updateFriendlyVisibilityMap:

    // В GameWorld.java, замените метод updateFriendlyVisibilityMap:
    public void updateFriendlyVisibilityMap(FriendlyUnit friendly) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        // Создаём новую карту для этого союзника
        boolean[][] map = new boolean[gridWidth][gridHeight];

        int[][] hillHeight = new int[gridWidth][gridHeight];
        for (Hill hill : hills) {
            hillHeight[hill.gridX][hill.gridY] = hill.height;
        }

        int observerHeight = 0;
        for (Hill hill : hills) {
            if (hill.gridX == friendly.gridX && hill.gridY == friendly.gridY) {
                observerHeight = hill.height;
                break;
            }
        }

        boolean isM53 = "M53".equals(friendly.type);

        for (int dy = -VIEW_RADIUS; dy <= VIEW_RADIUS; dy++) {
            for (int dx = -VIEW_RADIUS; dx <= VIEW_RADIUS; dx++) {
                int targetX = friendly.gridX + dx;
                int targetY = friendly.gridY + dy;
                if (targetX < 0 || targetX >= gridWidth || targetY < 0 || targetY >= gridHeight) continue;
                if (dx*dx + dy*dy > VIEW_RADIUS * VIEW_RADIUS) continue;

                if (isM53) {
                    map[targetX][targetY] = true;
                } else {
                    if (isLineOfSightClearWithWalls(friendly.gridX, friendly.gridY, targetX, targetY, observerHeight, hillHeight)) {
                        map[targetX][targetY] = true;
                    }
                }
            }
        }
        map[friendly.gridX][friendly.gridY] = true;

        // Сохраняем в кэш
        friendlyVisibilityCache.put(friendly, map);

        // Также обновляем глобальную карту для совместимости со старым кодом
        if (friendlyVisibilityMap == null || friendlyVisibilityMap.length != gridWidth) {
            friendlyVisibilityMap = new boolean[gridWidth][gridHeight];
        }
        for (int i = 0; i < gridWidth; i++) {
            System.arraycopy(map[i], 0, friendlyVisibilityMap[i], 0, gridHeight);
        }

        System.out.println("updateFriendlyVisibilityMap для " + friendly.name +
                " на позиции [" + friendly.gridX + "," + friendly.gridY + "], M53=" + isM53);
    }

    // В GameWorld.java, добавьте метод:
    // В GameWorld.java, добавьте или проверьте, что метод isEnemyVisibleFromFriendly использует правильную карту:

    public boolean isEnemyVisibleFromFriendly(FriendlyUnit friendly, Enemy enemy) {
        if (!enemy.isAlive) return false;

        // Обновляем карту видимости для этого союзника
        updateFriendlyVisibilityMap(friendly);

        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        if (enemy.gridX < 0 || enemy.gridX >= gridWidth || enemy.gridY < 0 || enemy.gridY >= gridHeight) {
            return false;
        }

        return friendlyVisibilityMap[enemy.gridX][enemy.gridY];
    }

    // Добавляем видимость от конкретной позиции
    private void addVisibilityFromPosition(int observerX, int observerY, boolean[][] targetMap) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        int[][] hillHeight = new int[gridWidth][gridHeight];
        for (Hill hill : hills) {
            hillHeight[hill.gridX][hill.gridY] = hill.height;
        }

        int observerHeight = 0;
        for (Hill hill : hills) {
            if (hill.gridX == observerX && hill.gridY == observerY) {
                observerHeight = hill.height;
                break;
            }
        }

        for (int dy = -VIEW_RADIUS; dy <= VIEW_RADIUS; dy++) {
            for (int dx = -VIEW_RADIUS; dx <= VIEW_RADIUS; dx++) {
                int targetX = observerX + dx;
                int targetY = observerY + dy;
                if (targetX < 0 || targetX >= gridWidth || targetY < 0 || targetY >= gridHeight) continue;
                if (dx*dx + dy*dy > VIEW_RADIUS * VIEW_RADIUS) continue;

                if (isLineOfSightClearWithWalls(observerX, observerY, targetX, targetY, observerHeight, hillHeight)) {
                    targetMap[targetX][targetY] = true;
                }
            }
        }
        targetMap[observerX][observerY] = true;
    }

    // Добавьте метод для загрузки стен:
    public void loadWalls(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        walls = LevelLoader.loadLevel(levelPath, gridWidth, gridHeight);
    }

    public void loadPavements(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        pavements = LevelLoader.loadPavements(levelPath, gridWidth, gridHeight);
        System.out.println("Загружено асфальта: " + pavements.size());
    }

    public void loadInfernalLands(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        infernalLands = LevelLoader.loadInfernalLands(levelPath, gridWidth, gridHeight);
        System.out.println("Загружено адской земли: " + infernalLands.size());
    }

    public void loadWaters(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        waters = LevelLoader.loadWaters(levelPath, gridWidth, gridHeight);
        System.out.println("Загружено воды: " + waters.size());
    }

    public void loadDoors(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        doors = LevelLoader.loadDoors(levelPath, gridWidth, gridHeight);
        System.out.println("Загружено дверей: " + doors.size());
    }

    // НОВЫЙ МЕТОД: Обработка следующего врага
    // НОВЫЙ МЕТОД: Обработка следующего врага
    public void processNextEnemy() {

        // ===== ПРОВЕРКА СМЕРТИ ИГРОКА В САМОМ НАЧАЛЕ =====
        if (player.health <= 0 && gamePanel != null) {
            System.out.println("💀 Leichttraktor уничтожен! Игра окончена.");
            SwingUtilities.invokeLater(() -> gamePanel.showGameOverDialog());
            return;
        }

        while (currentEnemyIndex < enemies.size()) {
            Enemy enemy = enemies.get(currentEnemyIndex);
            if (enemy.isAlive) {
                // ===== УСТАНАВЛИВАЕМ АКТИВНОГО ВРАГА ДЛЯ КАМЕРЫ ТОЛЬКО ЕСЛИ ОН ВИДИМ =====
                if (isEnemyVisibleByAnyone(enemy)) {
                    setCurrentActiveEnemy(enemy);
                    if (gamePanel != null) {
                        gamePanel.onEnemyTurnStart(enemy);
                    }
                } else {
                    // Для невидимых врагов сбрасываем текущего активного
                    setCurrentActiveEnemy(null);
                    if (gamePanel != null) {
                        gamePanel.onEnemyTurnEnd();
                    }
                }

                processEnemyTurn(enemy);
                return;
            }
            currentEnemyIndex++;
        }

        // Все враги закончили ход
        isEnemyTurn = false;

        // ===== ДОБАВЬТЕ УДАЛЕНИЕ МЁРТВЫХ СОЮЗНИКОВ =====
        removeDeadControllableUnits();
        if (gamePanel != null) {
            gamePanel.syncRecruitedUnits();
        }

        // ===== ПРОВЕРКА СМЕРТИ ИГРОКА =====
        if (player.health <= 0 && gamePanel != null) {
            System.out.println("💀 Leichttraktor уничтожен! Игра окончена.");
            gamePanel.showGameOverDialog();  // ← ТОЛЬКО ЭТОТ ВЫЗОВ
            return;
        }

        player.startNewTurn();

        // ===== ДОБАВЬТЕ: СБРОС ФЛАГА ОБНАРУЖЕНИЯ В ИГРОВОЙ ПАНЕЛИ =====
        if (gamePanel != null) {
            gamePanel.enemyDetectedThisTurn = false;  // ← ИСПРАВИТЬ НА public или setter
            gamePanel.updateBackgroundMusic();
            System.out.println("🔄 Новый ход игрока! Флаг обнаружения сброшен.");
        }

        // Воспроизводим звук отравления для игрока
        if (player.isPoisoned() && soundManager != null) {
            soundManager.playPoisonedSound();
        }


        // ===== НОВЫЙ КОД: Восстанавливаем очки хода для всех нанятых союзников =====
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                // Проверяем отравление ДО начала хода
                // При отравлении VK 100.01 P:
                if (friendly.isPoisoned() && soundManager != null && !friendly.poisonedSoundPlayed) {
                    if ("M53".equals(friendly.type)) {
                        soundManager.playM53PoisonedSound();
                    } else if ("MS-1".equals(friendly.type)) {
                        soundManager.playMS1PoisonedSound();
                    } else if ("VK10001P".equals(friendly.type)) {
                        soundManager.playVK10001PPoisonedSound();  // ← ДОБАВИТЬ
                    } else if ("T1".equals(friendly.type)) {
                        soundManager.playT1PoisonedSound();  // ← ДОБАВИТЬ
                    }else {
                        soundManager.playPoisonedSound();
                    }
                    friendly.poisonedSoundPlayed = true;
                }

                friendly.startTurn();  // Восстанавливаем очки хода

                System.out.println("🔄 " + friendly.name + " восстановил очки хода: " +
                        friendly.movePoints + "/" + friendly.maxMovePoints);
            }
        }
        // ===== КОНЕЦ НОВОГО КОДА =====

        System.out.println("=== ХОД ВРАГОВ ЗАВЕРШЁН ===");

        // В конце processNextEnemy(), после восстановления очков хода союзников, вызовите:
        updateBreadDebuffCounters();  // ← должно быть updateBreadDebuffCounters, а не updateBreadDebuffs

        checkSavM43Quest();
        checkVictory();

        if (!isEnemyTurn) {
            if (player.health <= 0) {
                System.out.println("ИГРА ОКОНЧЕНА! Игрок повержен.");
                // Здесь можно показать сообщение и остановить игру
            }
        }

        // ===== ДОБАВЬТЕ - оповещаем GamePanel обновить обнаружение =====
        if (gamePanel != null) {
            gamePanel.checkEnemyDetection();
            gamePanel.updateBackgroundMusic();
        }
// ===== КОНЕЦ ДОБАВЛЕНИЯ =====
    }

    private void updateBreadDebuffCounters() {
        // Обновляем дебафф игрока
        if (player.breadDebuffRemainingTurns > 0) {
            player.breadDebuffRemainingTurns--;
            if (player.breadDebuffRemainingTurns == 0) {
                player.movePoints = player.maxMovePoints;
                player.breadDebuffTurns = 0;
                player.poisonedSoundPlayed = false;  // ← СБРАСЫВАЕМ ФЛАГ
                System.out.println("🍞 Отравление хлебом прошло! Очки хода восстановлены до " + player.movePoints);
            } else {
                reducePlayerMovePoints(player);
            }
        }

        // Обновляем дебафф союзников
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isRecruited && friendly.breadDebuffRemainingTurns > 0) {
                friendly.breadDebuffRemainingTurns--;
                if (friendly.breadDebuffRemainingTurns == 0) {
                    friendly.movePoints = friendly.maxMovePoints;
                    friendly.breadDebuffTurns = 0;
                    friendly.poisonedSoundPlayed = false;  // ← СБРАСЫВАЕМ ФЛАГ
                    System.out.println("🍞 Отравление хлебом у " + friendly.name + " прошло!");
                } else {
                    reduceFriendlyMovePoints(friendly);
                }
            }
        }
    }

    private void reducePlayerMovePoints(PlayerTank player) {
        if (player.breadDebuffRemainingTurns <= 0) return;

        int penaltyPercent;
        if (player.breadDebuffRemainingTurns == 3) penaltyPercent = 80;
        else if (player.breadDebuffRemainingTurns == 2) penaltyPercent = 50;
        else penaltyPercent = 30;

        // Уменьшаем ТЕКУЩИЕ очки хода на процент от МАКСИМУМА
        int reduction = player.maxMovePoints * penaltyPercent / 100;
        player.movePoints = Math.max(0, player.maxMovePoints - reduction);

        System.out.println("🍞 Штраф хлеба: -" + penaltyPercent + "%, текущих очков: " + player.movePoints + "/" + player.maxMovePoints);
    }

    private void reduceFriendlyMovePoints(FriendlyUnit friendly) {
        if (friendly.breadDebuffRemainingTurns <= 0) return;

        int penaltyPercent;
        if (friendly.breadDebuffRemainingTurns == 3) penaltyPercent = 80;
        else if (friendly.breadDebuffRemainingTurns == 2) penaltyPercent = 50;
        else penaltyPercent = 30;

        int reduction = friendly.maxMovePoints * penaltyPercent / 100;
        friendly.movePoints = Math.max(0, friendly.maxMovePoints - reduction);

        System.out.println("🍞 Штраф хлеба у " + friendly.name + ": -" + penaltyPercent + "%, осталось: " + friendly.movePoints);
    }

    // НОВЫЙ МЕТОД: Логика ИИ для одного врага
    // НОВЫЙ МЕТОД: Логика ИИ для одного врага
    // В GameWorld.java, полностью замените метод processEnemyTurn:

    // В GameWorld.java, полностью перепишите processEnemyTurn:

    public void updateVisibilityFromPosition(int observerX, int observerY, boolean isM53) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        // Сбрасываем карту видимости
        for (int i = 0; i < gridWidth; i++) {
            Arrays.fill(visibilityMap[i], false);
        }

        int[][] hillHeight = new int[gridWidth][gridHeight];
        for (Hill hill : hills) {
            hillHeight[hill.gridX][hill.gridY] = hill.height;
        }

        int observerHeight = 0;
        for (Hill hill : hills) {
            if (hill.gridX == observerX && hill.gridY == observerY) {
                observerHeight = hill.height;
                break;
            }
        }

        // Если это M53 - игнорируем стены и деревья
        for (int dy = -VIEW_RADIUS; dy <= VIEW_RADIUS; dy++) {
            for (int dx = -VIEW_RADIUS; dx <= VIEW_RADIUS; dx++) {
                int targetX = observerX + dx;
                int targetY = observerY + dy;
                if (targetX < 0 || targetX >= gridWidth || targetY < 0 || targetY >= gridHeight) continue;
                if (dx*dx + dy*dy > VIEW_RADIUS * VIEW_RADIUS) continue;

                if (isM53) {
                    // M53 видит всех в радиусе без препятствий
                    visibilityMap[targetX][targetY] = true;
                } else {
                    if (isLineOfSightClearWithWalls(observerX, observerY, targetX, targetY, observerHeight, hillHeight)) {
                        visibilityMap[targetX][targetY] = true;
                    }
                }
            }
        }
        visibilityMap[observerX][observerY] = true;
    }

    public void updateVisibilityForEnemy(Enemy enemy) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        int[][] hillHeight = new int[gridWidth][gridHeight];
        for (Hill hill : hills) {
            hillHeight[hill.gridX][hill.gridY] = hill.height;
        }

        int observerHeight = player.isOnHill ? (1 + player.currentHillBonus) : 0;

        // ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД С ПРОВЕРКОЙ СТЕН
        if (isLineOfSightClearWithWalls(player.gridX, player.gridY, enemy.gridX, enemy.gridY, observerHeight, hillHeight)) {
            visibilityMap[enemy.gridX][enemy.gridY] = true;
        }
    }



    private void processEnemyTurn(Enemy enemy) {
        System.out.println("\n--- ХОД ВРАГА [" + enemy.gridX + "," + enemy.gridY +
                "] Тип: " + enemy.getBehaviorType() + " ---");

        setCurrentActiveEnemy(enemy);
        if (gamePanel != null) {
            gamePanel.onEnemyTurnStart(enemy);
            System.out.println(">>> onEnemyTurnStart вызван для врага [" + enemy.gridX + "," + enemy.gridY +
                    "], виден: " + isEnemyVisibleByAnyone(enemy));
        }

        final Enemy currentEnemy = enemy;
        final int[] stepCount = {0};
        final int[] shotsFired = {0};
        final boolean[] isWaitingAfterShot = {false};  // Флаг ожидания после выстрела
        final javax.swing.Timer actionTimer = new javax.swing.Timer(50, null); // Создаём сначала без ActionListener

        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Если сейчас ожидание после выстрела - ничего не делаем
                if (isWaitingAfterShot[0]) {
                    return;
                }

                if (!currentEnemy.isAlive) {
                    actionTimer.stop();
                    finishEnemyTurn(currentEnemy);
                    return;
                }

                if (currentEnemy.isMoving()) {
                    return;
                }

                // Оценка ситуации
                evaluateSituationForEnemy(currentEnemy);
                checkEnemyHearGunshots(currentEnemy);

                // Проверка видимости
                boolean seesPlayer = isPlayerVisibleToEnemy(currentEnemy);
                boolean seesFriendly = isAnyFriendlyVisibleToEnemy(currentEnemy);

                // ===== ПРОВЕРКА ШТЫКОВОЙ АТАКИ =====
                if (!seesPlayer && !seesFriendly && currentEnemy.canAssault() &&
                        currentEnemy.getAssaultPoint() == null) {

                    Point targetPos = null;
                    if (currentEnemy.getRememberedEnemyPositions().size() > 0) {
                        targetPos = currentEnemy.getRememberedEnemyPositions().get(0);
                    } else if (currentEnemy.lastSeenPlayerX != -1) {
                        targetPos = new Point(currentEnemy.lastSeenPlayerX, currentEnemy.lastSeenPlayerY);
                    }

                    if (targetPos != null) {
                        System.out.println("  ⚔️ ВКЛЮЧАЮ ШТЫКОВУЮ АТАКУ! Цель: [" + targetPos.x + "," + targetPos.y + "]");
                        currentEnemy.generateAssaultPoint(targetPos.x, targetPos.y, GameWorld.this);
                        currentEnemy.setAssaultMode(true);
                        currentEnemy.resetAssaultCooldown();
                    }
                }

                // ===== ПРОВЕРКА ТОЧКИ ШТУРМА =====
                if (currentEnemy.isAssaultMode() && currentEnemy.getAssaultPoint() != null) {
                    Point assaultPoint = currentEnemy.getAssaultPoint();
                    List<Point> testPath = findPathForEnemy(currentEnemy.gridX, currentEnemy.gridY,
                            assaultPoint.x, assaultPoint.y);
                    if (testPath.isEmpty()) {
                        System.out.println("  ❌ ТОЧКА ШТУРМА НЕДОСТИЖИМА! СБРАСЫВАЮ!");
                        currentEnemy.clearAssaultPoint();
                        currentEnemy.setAssaultMode(false);
                    } else {
                        System.out.println("  🏁 ДОСТИГНУТА ТОЧКА ШТУРМА!");
                        currentEnemy.clearAssaultPoint();
                        currentEnemy.setAssaultMode(false);
                    }
                }

                boolean canSeeTarget = seesPlayer || seesFriendly;

                // Если цель не видна, но враг ЗНАЕТ, где она находится
                if (!canSeeTarget && (currentEnemy.getRememberedEnemyPositions().size() > 0 ||
                        (currentEnemy.lastSeenPlayerX != -1 && currentEnemy.memoryTurns > 0))) {

                    Point lastKnownPos;
                    if (currentEnemy.getRememberedEnemyPositions().size() > 0) {
                        lastKnownPos = currentEnemy.getRememberedEnemyPositions().get(0);
                    } else {
                        lastKnownPos = new Point(currentEnemy.lastSeenPlayerX, currentEnemy.lastSeenPlayerY);
                    }

                    boolean hasWallOnPath = hasWallOnLineOfFire(currentEnemy.gridX, currentEnemy.gridY,
                            lastKnownPos.x, lastKnownPos.y);

                    if (hasWallOnPath) {
                        System.out.println("  🧱 СТЕНА НА ПУТИ К ЦЕЛИ! ШТУРМУЮ ЗДАНИЕ!");
                        Point assaultTarget = findEntranceToBuilding(currentEnemy, lastKnownPos.x, lastKnownPos.y);
                        if (assaultTarget != null) {
                            boolean moved = moveTowardsPositionWithPathfinding(currentEnemy, assaultTarget.x, assaultTarget.y);
                            if (moved) {
                                stepCount[0]++;
                                if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                            }
                        } else {
                            boolean moved = moveAlongWallToTarget(currentEnemy, lastKnownPos.x, lastKnownPos.y);
                            if (moved) {
                                stepCount[0]++;
                                if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                            }
                        }
                    }
                }

                int attackRange = (currentEnemy.getBehaviorType() == Enemy.BehaviorType.SNIPER) ? 12 : 8;
                int distanceToPlayer = Math.abs(currentEnemy.gridX - player.gridX) +
                        Math.abs(currentEnemy.gridY - player.gridY);

                boolean canAttackPlayer = seesPlayer && distanceToPlayer <= attackRange &&
                        currentEnemy.movePoints >= currentEnemy.shotCost &&
                        isLineOfSightClearForShot(currentEnemy.gridX, currentEnemy.gridY, player.gridX, player.gridY);  // ← ДОБАВИТЬ

                boolean canAttackFriendly = false;
                FriendlyUnit targetFriendly = null;
                if (seesFriendly && currentEnemy.movePoints >= currentEnemy.shotCost) {
                    targetFriendly = getClosestVisibleFriendly(currentEnemy);
                    if (targetFriendly != null) {
                        int distToFriendly = Math.abs(currentEnemy.gridX - targetFriendly.gridX) +
                                Math.abs(currentEnemy.gridY - targetFriendly.gridY);
                        canAttackFriendly = distToFriendly <= attackRange &&
                                isLineOfSightClearForShot(currentEnemy.gridX, currentEnemy.gridY,
                                        targetFriendly.gridX, targetFriendly.gridY);  // ← ДОБАВИТЬ
                    }
                }

                // ===== НОВОЕ: атака врагов из других фракций =====
                boolean canAttackOtherEnemy = false;
                Enemy targetEnemy = null;
                if (currentEnemy.movePoints >= currentEnemy.shotCost) {
                    targetEnemy = findBestEnemyTarget(currentEnemy);
                    canAttackOtherEnemy = (targetEnemy != null);
                    if (canAttackOtherEnemy) {
                        int distToTarget = Math.abs(currentEnemy.gridX - targetEnemy.gridX) +
                                Math.abs(currentEnemy.gridY - targetEnemy.gridY);
                        canAttackOtherEnemy = distToTarget <= attackRange;
                        if (canAttackOtherEnemy) {
                            System.out.println("  🎯 Враг " + currentEnemy.type + " (" + currentEnemy.getFaction().displayName +
                                    ") атакует врага " + targetEnemy.type + " (" + targetEnemy.getFaction().displayName + ")!");
                        }
                    }
                }

                // ===== ТАКТИЧЕСКОЕ ПОВЕДЕНИЕ =====
                if (currentEnemy.getBehaviorType() == Enemy.BehaviorType.TACTICAL) {
                    int enemiesCount = currentEnemy.getEnemiesCount();
                    int alliesCount = currentEnemy.getAlliesCount();

                    handleTacticalBehavior(currentEnemy);

                    if (!seesPlayer && !seesFriendly && currentEnemy.getRememberedEnemyPositions().isEmpty()) {
                        currentEnemy.stuckInPatrolCounter++;
                        if (currentEnemy.stuckInPatrolCounter > 3) {
                            System.out.println("  🚨 ТАКТИЧЕСКИЙ ВРАГ ЗАСТРЯЛ! МЕНЯЮ СТРАТЕГИЮ НА АГРЕССИВНУЮ!");
                            currentEnemy.setBehaviorType(Enemy.BehaviorType.AGGRESSIVE);
                            currentEnemy.stuckInPatrolCounter = 0;
                            if (currentEnemy.lastSeenPlayerX != -1) {
                                currentEnemy.generateAssaultPoint(currentEnemy.lastSeenPlayerX,
                                        currentEnemy.lastSeenPlayerY, GameWorld.this);
                                currentEnemy.setAssaultMode(true);
                            }
                        }
                    } else {
                        currentEnemy.stuckInPatrolCounter = 0;
                    }

                    if (enemiesCount > alliesCount) {
                        System.out.println("  [ТАКТИЧЕСКИЙ] Врагов больше! ОТСТУПАЮ!");
                        if (!currentEnemy.isRetreating()) {
                            currentEnemy.setRetreating(true);
                            currentEnemy.setRetreatTurns(4);
                        }
                        boolean retreated = performTacticalRetreatWithMemory(currentEnemy);
                        if (retreated) {
                            stepCount[0]++;
                            if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                        }
                        actionTimer.stop();
                        finishEnemyTurn(currentEnemy);
                        return;
                    } else {
                        if (currentEnemy.isRetreating()) {
                            boolean retreated = simpleRetreatMove(currentEnemy);
                            if (retreated) {
                                stepCount[0]++;
                                if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                            }
                        }
                    }
                }

                // ===== СТРЕЛЬБА =====
                if (!currentEnemy.isRetreating() && !currentEnemy.isAssaultMode() &&
                        (canAttackPlayer || canAttackFriendly || canAttackOtherEnemy) &&
                        currentEnemy.movePoints >= currentEnemy.shotCost) {

                    if (canAttackPlayer) {
                        attackPlayer(currentEnemy, gamePanel);
                    } else if (canAttackFriendly && targetFriendly != null) {
                        attackFriendly(currentEnemy, targetFriendly, gamePanel);
                    } else if (canAttackOtherEnemy && targetEnemy != null) {
                        attackEnemy(currentEnemy, targetEnemy, gamePanel);
                    }

                    currentEnemy.movePoints -= currentEnemy.shotCost;
                    shotsFired[0]++;

                    // ===== ЗАДЕРЖКА 750 мс ПОСЛЕ ВЫСТРЕЛА =====
                    isWaitingAfterShot[0] = true;
                    actionTimer.stop();

                    javax.swing.Timer shotDelayTimer = new javax.swing.Timer(750, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ev) {
                            ((Timer) ev.getSource()).stop();
                            isWaitingAfterShot[0] = false;

                            // Проверяем, остались ли очки хода для следующего выстрела
                            if (currentEnemy.movePoints >= currentEnemy.shotCost) {
                                actionTimer.start();
                            } else {
                                // Если очков хода не хватает - завершаем ход
                                finishEnemyTurn(currentEnemy);
                            }
                        }
                    });
                    shotDelayTimer.setRepeats(false);
                    shotDelayTimer.start();
                    return;
                } else {
                    // ===== НОВЫЙ БЛОК: если не может стрелять, но слышал звук =====
                    if (currentEnemy.isInvestigatingSound() && currentEnemy.getLastHeardSoundPosition() != null) {
                        Point soundPos = currentEnemy.getLastHeardSoundPosition();
                        int distanceToSound = Math.abs(currentEnemy.gridX - soundPos.x) +
                                Math.abs(currentEnemy.gridY - soundPos.y);

                        // Если враг ещё не достиг места звука, двигаемся к нему
                        if (distanceToSound > 2 && currentEnemy.movePoints >= currentEnemy.moveCost) {
                            System.out.println("  🔊 Двигаюсь к месту выстрела, чтобы найти позицию!");
                            boolean moved = moveTowardsPositionWithPathfinding(currentEnemy, soundPos.x, soundPos.y);
                            if (moved) {
                                stepCount[0]++;
                                if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                            }
                        }
                    }
                }



                // ===== ПРОВЕРКА ДОЛИ ПОПАДАНИЙ =====
                if (currentEnemy.shouldAssault()) {
                    System.out.println("  ⚔️ НИЗКАЯ ДОЛЯ ПОПАДАНИЙ (<=20%)! ПЕРЕХОЖУ В ШТЫКОВУЮ АТАКУ!");
                    Point targetPos;
                    if (player.health > 0 && isPlayerVisibleToEnemy(currentEnemy)) {
                        targetPos = new Point(player.gridX, player.gridY);
                    } else {
                        FriendlyUnit visibleFriendly = getClosestVisibleFriendly(currentEnemy);
                        if (visibleFriendly != null) {
                            targetPos = new Point(visibleFriendly.gridX, visibleFriendly.gridY);
                        } else {
                            targetPos = new Point(currentEnemy.lastSeenPlayerX, currentEnemy.lastSeenPlayerY);
                        }
                    }
                    currentEnemy.generateAssaultPoint(targetPos.x, targetPos.y, GameWorld.this);
                    currentEnemy.setAssaultMode(true);
                    currentEnemy.resetAssaultCooldown();
                    if (currentEnemy.getAssaultPoint() != null) {
                        Point assaultPoint = currentEnemy.getAssaultPoint();
                        boolean moved = moveTowardsPositionWithPathfinding(currentEnemy, assaultPoint.x, assaultPoint.y);
                        if (moved) {
                            stepCount[0]++;
                            if (Math.abs(currentEnemy.gridX - assaultPoint.x) <= 1 &&
                                    Math.abs(currentEnemy.gridY - assaultPoint.y) <= 1) {
                                System.out.println("  🏁 ДОСТИГНУТА ТОЧКА ШТУРМА!");
                                currentEnemy.clearAssaultPoint();
                            }
                            if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                        }
                    }
                }

                // ===== ПРОВЕРКА: ЕСЛИ ТАКТИЧЕСКИЙ ВРАГ НЕ МОЖЕТ НАЙТИ ПУТЬ =====
                if (currentEnemy.getBehaviorType() == Enemy.BehaviorType.TACTICAL &&
                        !seesPlayer && !seesFriendly &&
                        currentEnemy.getLongTermTarget() != null) {

                    List<Point> testPath = findPathForEnemy(currentEnemy.gridX, currentEnemy.gridY,
                            currentEnemy.getLongTermTarget().x,
                            currentEnemy.getLongTermTarget().y);
                    if (testPath.isEmpty()) {
                        System.out.println("  🚫 ТАКТИЧЕСКИЙ ВРАГ НЕ МОЖЕТ ДОСТИЧЬ ЦЕЛИ! ГЕНЕРИРУЮ НОВУЮ ЦЕЛЬ!");
                        Point newTarget = currentEnemy.getNextPatrolPoint();
                        currentEnemy.setLongTermTarget(newTarget);
                    }
                }

                // ===== ДВИЖЕНИЕ =====
                boolean moved = false;

                if (!currentEnemy.isRetreating() && currentEnemy.movePoints >= currentEnemy.moveCost) {
                    boolean seesTarget = seesPlayer || seesFriendly;
                    if (seesTarget) {
                        currentEnemy.noTargetTurns = 0;
                        currentEnemy.setPatrolling(false);
                        if (distanceToPlayer > attackRange) {
                            System.out.println("  Цель видна, но далеко! Двигаюсь ближе!");
                            moved = moveTowardsPlayerWithPathfinding(currentEnemy);
                            if (moved) {
                                stepCount[0]++;
                                if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                            }
                        } else {
                            // Цель в радиусе атаки, но может быть препятствие
                            // Проверяем, может ли враг выстрелить с текущей позиции
                            boolean canShoot = isLineOfSightClearForShot(currentEnemy.gridX, currentEnemy.gridY,
                                    player.gridX, player.gridY);

                            if (!canShoot) {
                                System.out.println("  🧱 Препятствие на линии огня! Ищу лучшую позицию!");
                                boolean foundBetterPos = findBetterShootingPosition(currentEnemy, player.gridX, player.gridY);
                                if (foundBetterPos) {
                                    moved = true;
                                    stepCount[0]++;
                                    if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                                } else {
                                    System.out.println("  ⚠ Не могу найти позицию для выстрела! Жду...");
                                }
                            } else {
                                System.out.println("  Цель в радиусе атаки! Жду выстрела.");
                            }
                        }
                    } else if (seesFriendly) {
                        FriendlyUnit target = getClosestVisibleFriendly(currentEnemy);
                        if (target != null) {
                            int distToFriendly = Math.abs(currentEnemy.gridX - target.gridX) +
                                    Math.abs(currentEnemy.gridY - target.gridY);
                            if (distToFriendly > attackRange) {
                                System.out.println("  Союзник виден, но далеко! Двигаюсь ближе!");
                                moved = moveTowardsPositionWithPathfinding(currentEnemy, target.gridX, target.gridY);
                                if (moved) {
                                    stepCount[0]++;
                                    if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                                }
                            } else {
                                // Проверяем, может ли враг выстрелить с текущей позиции
                                boolean canShoot = isLineOfSightClearForShot(currentEnemy.gridX, currentEnemy.gridY,
                                        target.gridX, target.gridY);

                                if (!canShoot) {
                                    System.out.println("  🧱 Препятствие на линии огня к союзнику! Ищу лучшую позицию!");
                                    boolean foundBetterPos = findBetterShootingPosition(currentEnemy, target.gridX, target.gridY);
                                    if (foundBetterPos) {
                                        moved = true;
                                        stepCount[0]++;
                                        if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                                    }
                                }
                            }
                        }
                    } else {
                        if (currentEnemy.isInvestigatingSound() && currentEnemy.getSoundMemoryTurns() <= 0) {
                            System.out.println("  🔄 Звук забыт, возвращаюсь к патрулированию!");
                            currentEnemy.setInvestigatingSound(false);
                            currentEnemy.setPatrolling(true);
                            Point newTarget = currentEnemy.getNextPatrolPoint();
                            currentEnemy.setLongTermTarget(newTarget);
                            currentEnemy.setTargetPriority(1);
                        }

                        if (currentEnemy.getSoundMemoryTurns() > 0 && currentEnemy.getLastHeardSoundPosition() != null) {
                            Point soundPos = currentEnemy.getLastHeardSoundPosition();
                            boolean reachedSound = Math.abs(currentEnemy.gridX - soundPos.x) <= 1 &&
                                    Math.abs(currentEnemy.gridY - soundPos.y) <= 1;
                            if (reachedSound) {
                                System.out.println("  🎯 Достиг места выстрела! Никого не нашёл.");
                                gunshotSounds.removeIf(sound -> sound.x == soundPos.x && sound.y == soundPos.y);
                                currentEnemy.setSoundMemoryTurns(0);
                                currentEnemy.setLastHeardSoundPosition(null);
                                currentEnemy.setInvestigatingSound(false);

                                // Проверяем, есть ли враги рядом (игрок или союзники)
                                boolean enemyNearby = isPlayerVisibleToEnemy(currentEnemy) || isAnyFriendlyVisibleToEnemy(currentEnemy);
                                if (enemyNearby) {
                                    System.out.println("  👀 Враг рядом! Не ухожу на патрулирование!");
                                    currentEnemy.setPatrolling(false);
                                    // Пытаемся найти позицию для стрельбы
                                    findBetterShootingPosition(currentEnemy, player.gridX, player.gridY);
                                } else {
                                    currentEnemy.setPatrolling(true);
                                    Point newTarget = currentEnemy.getNextPatrolPoint();
                                    currentEnemy.setLongTermTarget(newTarget);
                                    currentEnemy.setTargetPriority(1);
                                }
                            } else {
                                currentEnemy.setSoundMemoryTurns(currentEnemy.getSoundMemoryTurns() - 1);
                                currentEnemy.setInvestigatingSound(true);
                                System.out.println("  🔍 Двигаюсь к месту выстрела!");
                                moved = moveTowardsPositionWithPathfinding(currentEnemy, soundPos.x, soundPos.y);
                                if (moved) {
                                    stepCount[0]++;
                                    if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                                }
                            }
                        }

                        if (!moved && !currentEnemy.isInvestigatingSound()) {
                            // СНАЧАЛА ПРОВЕРЯЕМ, ВИДИТ ЛИ ВРАГ ЦЕЛЬ
                            if (isPlayerVisibleToEnemy(currentEnemy) || isAnyFriendlyVisibleToEnemy(currentEnemy)) {
                                System.out.println("  👀 ВИЖУ ВРАГА! Переключаюсь в режим атаки!");
                                currentEnemy.setPatrolling(false);

                                // ===== ЛОГИКА АТАКИ =====
                                if (isPlayerVisibleToEnemy(currentEnemy)) {
                                    int distToPlayer = Math.abs(currentEnemy.gridX - player.gridX) +
                                            Math.abs(currentEnemy.gridY - player.gridY);
                                    attackRange = (currentEnemy.getBehaviorType() == Enemy.BehaviorType.SNIPER) ? 12 : 8;

                                    if (distToPlayer <= attackRange && currentEnemy.movePoints >= currentEnemy.shotCost &&
                                            isLineOfSightClearForShot(currentEnemy.gridX, currentEnemy.gridY, player.gridX, player.gridY)) {
                                        // Стреляем в игрока
                                        attackPlayer(currentEnemy, gamePanel);
                                        currentEnemy.movePoints -= currentEnemy.shotCost;
                                        shotsFired[0]++;
                                        // Ждём после выстрела
                                        isWaitingAfterShot[0] = true;
                                        actionTimer.stop();
                                        javax.swing.Timer shotDelayTimer = new javax.swing.Timer(750, new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent ev) {
                                                ((Timer) ev.getSource()).stop();
                                                isWaitingAfterShot[0] = false;
                                                finishEnemyTurn(currentEnemy);
                                            }
                                        });
                                        shotDelayTimer.setRepeats(false);
                                        shotDelayTimer.start();
                                        return;
                                    } else {
                                        // Не может стрелять - двигается к цели
                                        moveTowardsPlayerWithPathfinding(currentEnemy);
                                        stepCount[0]++;
                                        if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                                    }
                                } else if (isAnyFriendlyVisibleToEnemy(currentEnemy)) {
                                    FriendlyUnit target = getClosestVisibleFriendly(currentEnemy);
                                    if (target != null) {
                                        int distToFriendly = Math.abs(currentEnemy.gridX - target.gridX) +
                                                Math.abs(currentEnemy.gridY - target.gridY);
                                        attackRange = (currentEnemy.getBehaviorType() == Enemy.BehaviorType.SNIPER) ? 12 : 8;

                                        if (distToFriendly <= attackRange && currentEnemy.movePoints >= currentEnemy.shotCost &&
                                                isLineOfSightClearForShot(currentEnemy.gridX, currentEnemy.gridY, target.gridX, target.gridY)) {
                                            attackFriendly(currentEnemy, target, gamePanel);
                                            currentEnemy.movePoints -= currentEnemy.shotCost;
                                            shotsFired[0]++;
                                            isWaitingAfterShot[0] = true;
                                            actionTimer.stop();
                                            javax.swing.Timer shotDelayTimer = new javax.swing.Timer(750, new ActionListener() {
                                                @Override
                                                public void actionPerformed(ActionEvent ev) {
                                                    ((Timer) ev.getSource()).stop();
                                                    isWaitingAfterShot[0] = false;
                                                    finishEnemyTurn(currentEnemy);
                                                }
                                            });
                                            shotDelayTimer.setRepeats(false);
                                            shotDelayTimer.start();
                                            return;
                                        } else {
                                            moveTowardsPositionWithPathfinding(currentEnemy, target.gridX, target.gridY);
                                            stepCount[0]++;
                                            if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                                        }
                                    }
                                }
                                // ===== КОНЕЦ ЛОГИКИ АТАКИ =====

                                // ВАЖНО: НЕ СТАВИТЬ continue, а продолжать выполнение или завершать ход
                                // Если атака не удалась, возможно, стоит просто завершить ход
                                if (currentEnemy.movePoints < currentEnemy.moveCost) {
                                    System.out.println("  Ход завершён (шагов: " + stepCount[0] + ", выстрелов: " + shotsFired[0] + ")");
                                    actionTimer.stop();
                                    finishEnemyTurn(currentEnemy);
                                    return;
                                }
                            }

                            System.out.println("  Режим: " + (currentEnemy.isPatrolling() ? "ПАТРУЛИРОВАНИЕ" : "ИССЛЕДОВАНИЕ"));
                            Point longTermTarget = currentEnemy.getLongTermTarget();
                            if (longTermTarget == null || currentEnemy.getTargetPriority() == 0) {
                                if (currentEnemy.isPatrolling()) {
                                    longTermTarget = currentEnemy.getNextPatrolPoint();
                                    currentEnemy.setTargetPriority(1);
                                } else {
                                    longTermTarget = currentEnemy.getExplorationTarget();
                                    currentEnemy.setTargetPriority(2);
                                }
                                currentEnemy.setLongTermTarget(longTermTarget);
                            }
                            if (longTermTarget != null &&
                                    Math.abs(currentEnemy.gridX - longTermTarget.x) <= 2 &&
                                    Math.abs(currentEnemy.gridY - longTermTarget.y) <= 2) {
                                System.out.println("  🎯 Достигнута цель!");
                                if (currentEnemy.isPatrolling()) {
                                    longTermTarget = currentEnemy.getNextPatrolPoint();
                                } else {
                                    currentEnemy.regenerateExplorationTarget();
                                    longTermTarget = currentEnemy.getExplorationTarget();
                                }
                                currentEnemy.setLongTermTarget(longTermTarget);
                            }
                            if (longTermTarget != null) {
                                moved = moveTowardsPositionWithPathfinding(currentEnemy, longTermTarget.x, longTermTarget.y);
                                if (moved) {
                                    stepCount[0]++;
                                    if (currentEnemy.movePoints >= currentEnemy.moveCost) return;
                                }
                            }
                        }
                    }
                }

                // ===== ПРИНУДИТЕЛЬНАЯ ПРОВЕРКА ВЫСТРЕЛА ПОСЛЕ ДВИЖЕНИЯ =====
                if (!currentEnemy.isRetreating() && !currentEnemy.isAssaultMode() &&
                        !currentEnemy.hasAttackedThisTurn && currentEnemy.movePoints >= currentEnemy.shotCost) {

                    // Проверяем игрока
                    if (isPlayerVisibleToEnemy(currentEnemy)) {
                        int distToPlayer = Math.abs(currentEnemy.gridX - player.gridX) +
                                Math.abs(currentEnemy.gridY - player.gridY);
                        attackRange = (currentEnemy.getBehaviorType() == Enemy.BehaviorType.SNIPER) ? 12 : 8;
                        if (distToPlayer <= attackRange &&
                                isLineOfSightClearForShot(currentEnemy.gridX, currentEnemy.gridY, player.gridX, player.gridY)) {
                            System.out.println("  🔫 Принудительный выстрел по игроку!");
                            attackPlayer(currentEnemy, gamePanel);
                            currentEnemy.movePoints -= currentEnemy.shotCost;
                            shotsFired[0]++;
                            // Ждём после выстрела
                            isWaitingAfterShot[0] = true;
                            actionTimer.stop();
                            javax.swing.Timer shotDelayTimer = new javax.swing.Timer(750, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent ev) {
                                    ((Timer) ev.getSource()).stop();
                                    isWaitingAfterShot[0] = false;
                                    finishEnemyTurn(currentEnemy);
                                }
                            });
                            shotDelayTimer.setRepeats(false);
                            shotDelayTimer.start();
                            return;
                        }
                    }

                    // Проверяем союзников
                    targetFriendly = getClosestVisibleFriendly(currentEnemy);
                    if (targetFriendly != null) {
                        int distToFriendly = Math.abs(currentEnemy.gridX - targetFriendly.gridX) +
                                Math.abs(currentEnemy.gridY - targetFriendly.gridY);
                        attackRange = (currentEnemy.getBehaviorType() == Enemy.BehaviorType.SNIPER) ? 12 : 8;
                        if (distToFriendly <= attackRange &&
                                isLineOfSightClearForShot(currentEnemy.gridX, currentEnemy.gridY,
                                        targetFriendly.gridX, targetFriendly.gridY)) {
                            System.out.println("  🔫 Принудительный выстрел по союзнику " + targetFriendly.name + "!");
                            attackFriendly(currentEnemy, targetFriendly, gamePanel);
                            currentEnemy.movePoints -= currentEnemy.shotCost;
                            shotsFired[0]++;
                            isWaitingAfterShot[0] = true;
                            actionTimer.stop();
                            javax.swing.Timer shotDelayTimer = new javax.swing.Timer(750, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent ev) {
                                    ((Timer) ev.getSource()).stop();
                                    isWaitingAfterShot[0] = false;
                                    finishEnemyTurn(currentEnemy);
                                }
                            });
                            shotDelayTimer.setRepeats(false);
                            shotDelayTimer.start();
                            return;
                        }
                    }
                }

                System.out.println("  Ход завершён (шагов: " + stepCount[0] + ", выстрелов: " + shotsFired[0] + ")");
                actionTimer.stop();
                finishEnemyTurn(currentEnemy);
            }
        };

        actionTimer.addActionListener(action);
        actionTimer.setRepeats(true);
        actionTimer.start();
    }

    // Добавьте этот метод в GameWorld.java
    private boolean moveTowardsBetterShootingPosition(Enemy enemy, int targetX, int targetY) {
        if (enemy.isMoving()) return false;
        if (enemy.movePoints < enemy.moveCost) return false;

        // Проверяем клетки вокруг цели (радиус 3)
        int searchRadius = 4;
        Point bestPosition = null;
        int bestDistance = Integer.MAX_VALUE;
        int currentX = enemy.gridX;
        int currentY = enemy.gridY;

        // Перебираем позиции вокруг цели
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -searchRadius; dy <= searchRadius; dy++) {
                // Пропускаем слишком далёкие
                if (dx*dx + dy*dy > searchRadius*searchRadius) continue;

                int checkX = targetX + dx;
                int checkY = targetY + dy;

                // Проверяем, можно ли встать на эту клетку
                if (!canMoveToForEnemy(enemy, checkX, checkY)) continue;

                // Проверяем, есть ли прямая видимость с этой клетки до цели
                if (isLineOfSightClearForShot(checkX, checkY, targetX, targetY)) {
                    // Расстояние от врага до этой позиции
                    int distFromEnemy = Math.abs(enemy.gridX - checkX) + Math.abs(enemy.gridY - checkY);
                    // Расстояние от позиции до цели
                    int distToTarget = Math.abs(checkX - targetX) + Math.abs(checkY - targetY);

                    // Если позиция рядом с целью (но не вплотную) и не слишком далеко от врага
                    if (distToTarget >= 2 && distFromEnemy < bestDistance) {
                        bestDistance = distFromEnemy;
                        bestPosition = new Point(checkX, checkY);
                    }
                }
            }
        }

        // Если нашли позицию вокруг цели - двигаемся к ней
        if (bestPosition != null) {
            System.out.println("  🎯 Двигаюсь к позиции вокруг цели: [" + bestPosition.x + "," + bestPosition.y + "]");
            return moveTowardsPositionWithPathfinding(enemy, bestPosition.x, bestPosition.y);
        }

        // Если не нашли, просто двигаемся к цели
        System.out.println("  🚶 Двигаюсь к цели, чтобы найти позицию!");
        return moveTowardsPositionWithPathfinding(enemy, targetX, targetY);
    }

    // Атака другого врага (межфракционные бои)
    // Атака другого врага (межфракционные бои)
    private void attackEnemy(Enemy attacker, Enemy target, GamePanel panel) {
        // ===== ПРОВЕРКА ВИДИМОСТИ (Line of Sight) =====
        if (!isLineOfSightClearForShot(attacker.gridX, attacker.gridY, target.gridX, target.gridY)) {
            System.out.println("🧱 СТЕНА/ПРЕПЯТСТВИЕ НА ЛИНИИ ОГНЯ! " + attacker.type +
                    " не может стрелять в " + target.type + "!");

            // Если не может стрелять - пытается подойти ближе
            if (attacker.movePoints >= attacker.moveCost) {
                System.out.println("  🔄 Пытаюсь подойти ближе для выстрела!");
                moveTowardsPositionWithPathfinding(attacker, target.gridX, target.gridY);
            }
            return;
        }

        int dx = Math.abs(attacker.gridX - target.gridX);
        int dy = Math.abs(attacker.gridY - target.gridY);
        double distance = Math.sqrt(dx*dx + dy*dy);

        double hitChance = 0.7 * (1 - Math.min(0.5, distance / 20.0));
        hitChance = hitChance * (attacker.weaponAccuracy / 50.0);
        hitChance = Math.min(0.9, Math.max(0.1, hitChance));

        System.out.println("=== МЕЖФРАКЦИОННАЯ АТАКА! ===");
        System.out.println(attacker.type + " (" + attacker.getFaction().displayName +
                ") атакует " + target.type + " (" + target.getFaction().displayName + ")");
        System.out.println("Дистанция: " + distance);
        System.out.println("Шанс попадания: " + (int)(hitChance * 100) + "%");
        System.out.println("Количество снарядов: " + attacker.burstSize);

        if (soundManager != null) {
            soundManager.playEnemyShootSound(attacker.weaponCaliber);
        }

        if (panel != null && isEnemyVisibleByAnyone(attacker)) {
            panel.centerCameraOnEnemy(attacker);
        }

        double startX = attacker.gridX * CELL_SIZE + CELL_SIZE / 2.0;
        double startY = attacker.gridY * CELL_SIZE + CELL_SIZE / 2.0;

        attacker.registerShot();

        for (int i = 0; i < attacker.burstSize; i++) {
            boolean isCrit = random.nextDouble() < attacker.critChance;
            int damage = attacker.weaponDamage;
            if (isCrit) {
                damage *= (1.5 + random.nextDouble() * 1.5);
                System.out.println("  КРИТ! Урон: " + damage);
            }

            double spread = (random.nextDouble() - 0.5) * 0.1;
            double actualHitChance = Math.min(0.95, Math.max(0.05, hitChance + spread));

            Projectile projectile = new Projectile(startX, startY, target, damage, actualHitChance, this, attacker);

            if (panel != null) {
                panel.addProjectile(projectile);
            }
        }
    }

    // Добавьте этот метод в класс GameWorld (после метода isLineOfSightClearWithWalls)
    /**
     * Проверяет, есть ли препятствие на линии огня между двумя точками.
     * Отличается от isLineOfSightClearWithWalls тем, что проверяет ВСЕ клетки,
     * через которые проходит прямая, а также учитывает, что препятствием считаются
     * стены, деревья, закрытые двери и т.д.
     */
    public boolean isLineOfSightClearForShot(int x0, int y0, int x1, int y1) {
        if (x0 == x1 && y0 == y1) return true;

        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        // ===== ПРОВЕРКА: ЦЕЛЬ ЗА ЗАКРЫТОЙ ДВЕРЬЮ =====
        Door targetDoor = getDoorAt(x1, y1);
        if (targetDoor != null && !targetDoor.isOpen) {
            System.out.println("  🚪 Цель за закрытой дверью! Выстрел невозможен!");
            return false;
        }

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int x = x0, y = y0;

        while (!(x == x1 && y == y1)) {
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }

            if (x == x0 && y == y0) continue;
            if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
                return false;
            }

            // Стены
            if (isWallAt(x, y)) {
                return false;
            }

            // Деревья
            if (isTreeAt(x, y)) {
                return false;
            }

            // ===== ЗАКРЫТЫЕ ДВЕРИ БЛОКИРУЮТ ВЫСТРЕЛ =====
            Door door = getDoorAt(x, y);
            if (door != null && !door.isOpen) {
                System.out.println("  🚪 Закрытая дверь на линии огня в [" + x + "," + y + "]");
                return false;
            }

            // Тумбочки
            if (getStorageChestAt(x, y) != null) {
                return false;
            }

            // Мусорные контейнеры
            if (getGarbageContainerAt(x, y) != null) {
                return false;
            }

            if (sx != 0 && sy != 0) {
                int checkX1 = x - sx;
                int checkY1 = y;
                int checkX2 = x;
                int checkY2 = y - sy;

                if (isWallAt(checkX1, checkY1) || isWallAt(checkX2, checkY2)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void onDoorStateChanged(Door door) {
        System.out.println("🔄 Обновление видимости после изменения двери в [" + door.gridX + "," + door.gridY + "]");

        // Обновляем карту видимости для игрока
        updateVisibilityMap();

        // Обновляем карты видимости для всех союзников
        updateAllFriendlyVisibilityMaps();

        // Обновляем комбинированную карту
        updateCombinedVisibilityMap();

        // Перерисовываем
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }

    private boolean findBetterShootingPosition(Enemy enemy, int targetX, int targetY) {
        if (enemy.isMoving()) return false;
        if (enemy.movePoints < enemy.moveCost) return false;

        // Радиус поиска вокруг текущей позиции
        int searchRadius = 3;
        Point bestPosition = null;
        double bestScore = -Double.MAX_VALUE;

        // Получаем текущую позицию
        int currentX = enemy.gridX;
        int currentY = enemy.gridY;

        // Проверяем, есть ли прямая видимость с текущей позиции
        boolean hasDirectShot = isLineOfSightClearForShot(currentX, currentY, targetX, targetY);

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -searchRadius; dy <= searchRadius; dy++) {
                // Пропускаем слишком далёкие клетки
                if (dx*dx + dy*dy > searchRadius*searchRadius) continue;

                int checkX = currentX + dx;
                int checkY = currentY + dy;

                // Проверяем, можно ли встать на эту клетку
                if (!canMoveToForEnemy(enemy, checkX, checkY)) continue;

                // Проверяем, есть ли прямая видимость с этой клетки до цели
                boolean hasLineOfSight = isLineOfSightClearForShot(checkX, checkY, targetX, targetY);

                if (hasLineOfSight) {
                    // Оцениваем позицию
                    double score = 0;

                    // Чем ближе к цели - тем лучше (но не слишком близко)
                    double distance = Math.hypot(checkX - targetX, checkY - targetY);
                    if (distance > 2) { // Не стоит вплотную к цели
                        score += 10 - distance * 0.5;
                    }

                    // Если на текущей позиции нет прямой видимости, а на новой есть - большой бонус
                    if (!hasDirectShot && hasLineOfSight) {
                        score += 50;
                    }

                    // Штраф за расстояние от текущей позиции
                    score -= Math.hypot(dx, dy) * 0.3;

                    // Проверяем, есть ли укрытие на этой позиции (бонус)
                    if (hasCoverNearby(checkX, checkY)) {
                        score += 5;
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        bestPosition = new Point(checkX, checkY);
                    }
                }
            }
        }

        // Если нашли лучшую позицию и она отличается от текущей
        if (bestPosition != null && !(bestPosition.x == currentX && bestPosition.y == currentY)) {
            System.out.println("  🎯 Найдена лучшая позиция для стрельбы: [" + bestPosition.x + "," + bestPosition.y + "]");
            return moveTowardsPositionWithPathfinding(enemy, bestPosition.x, bestPosition.y);
        }

        // Если не нашли позицию с прямой видимостью, но есть стена - пытаемся обойти её
        if (!hasDirectShot) {
            Point wallPos = findWallBlockingShot(enemy, targetX, targetY);
            if (wallPos != null) {
                // Пытаемся обойти стену
                Point flankPos = findFlankPosition(enemy, wallPos.x, wallPos.y, targetX, targetY);
                if (flankPos != null && canMoveToForEnemy(enemy, flankPos.x, flankPos.y)) {
                    System.out.println("  🧱 Обхожу стену в [" + wallPos.x + "," + wallPos.y +
                            "], цель: [" + flankPos.x + "," + flankPos.y + "]");
                    return moveTowardsPositionWithPathfinding(enemy, flankPos.x, flankPos.y);
                }
            }
        }

        // ===== НОВОЕ: если не нашли позицию - двигаемся к цели =====
        System.out.println("  🔄 Не могу найти позицию для стрельбы! Двигаюсь к цели!");
        return moveTowardsPositionWithPathfinding(enemy, targetX, targetY);
    }

    private Point findWallBlockingShot(Enemy enemy, int targetX, int targetY) {
        int x0 = enemy.gridX;
        int y0 = enemy.gridY;
        int x1 = targetX;
        int y1 = targetY;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int x = x0, y = y0;

        while (!(x == x1 && y == y1)) {
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }

            if (x == x0 && y == y0) continue;
            if (x == x1 && y == y1) break;

            // Если нашли стену на пути
            if (isWallAt(x, y) || isTreeAt(x, y)) {
                return new Point(x, y);
            }
        }
        return null;
    }

    private Point findFlankPosition(Enemy enemy, int wallX, int wallY, int targetX, int targetY) {
        // Определяем, с какой стороны стены находится цель
        int dx = targetX - wallX;
        int dy = targetY - wallY;

        // Пробуем обойти стену с разных сторон
        int[][] offsets = {
                {0, 1}, {0, -1}, {1, 0}, {-1, 0},
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
        };

        Point bestPos = null;
        int bestDist = Integer.MAX_VALUE;

        for (int[] off : offsets) {
            int checkX = wallX + off[0];
            int checkY = wallY + off[1];

            // Проверяем, можно ли встать на эту клетку
            if (!canMoveToForEnemy(enemy, checkX, checkY)) continue;

            // Проверяем, есть ли прямая видимость с этой клетки до цели
            if (isLineOfSightClearForShot(checkX, checkY, targetX, targetY)) {
                int dist = Math.abs(enemy.gridX - checkX) + Math.abs(enemy.gridY - checkY);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestPos = new Point(checkX, checkY);
                }
            }
        }

        return bestPos;
    }

    // Поиск входа в здание (клетки, с которой есть прямой доступ к цели)
    private Point findEntranceToBuilding(Enemy enemy, int targetX, int targetY) {
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        Point bestEntrance = null;
        int bestDist = Integer.MAX_VALUE;

        for (int[] dir : dirs) {
            int entranceX = targetX + dir[0];
            int entranceY = targetY + dir[1];

            if (entranceX < 0 || entranceX >= 80 || entranceY < 0 || entranceY >= 80) continue;

            // Проверяем, можно ли встать на эту клетку
            if (!canMoveToForEnemy(enemy, entranceX, entranceY)) continue;

            List<Point> path = findPathForEnemy(enemy.gridX, enemy.gridY, entranceX, entranceY);
            if (!path.isEmpty()) {
                // Проверяем, есть ли прямая видимость от входа к цели (нет стены)
                if (!hasWallOnLineOfFire(entranceX, entranceY, targetX, targetY)) {
                    int dist = path.size();
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestEntrance = new Point(entranceX, entranceY);
                    }
                }
            }
        }

        // Если не нашли вход - пробуем подойти максимально близко к стене
        if (bestEntrance == null) {
            System.out.println("  ⚠ Не найден прямой вход, пытаюсь подойти к стене!");
            bestEntrance = findClosestWallCell(enemy, targetX, targetY);
        }

        return bestEntrance;
    }

    // Проверка, видит ли один враг другого (с учётом стен)
    private boolean isEnemyVisibleToEnemy(Enemy attacker, Enemy target) {
        if (attacker == null || target == null) return false;
        if (!attacker.isAlive || !target.isAlive) return false;

        int dx = Math.abs(attacker.gridX - target.gridX);
        int dy = Math.abs(attacker.gridY - target.gridY);
        int distSq = dx*dx + dy*dy;

        // Враги видят на расстоянии ENEMY_VIEW_RADIUS (8 клеток)
        if (distSq > ENEMY_VIEW_RADIUS * ENEMY_VIEW_RADIUS) {
            return false;
        }

        // Получаем высоты холмов
        int[][] hillHeight = new int[FIELD_SIZE / CELL_SIZE][FIELD_SIZE / CELL_SIZE];
        for (Hill hill : hills) {
            hillHeight[hill.gridX][hill.gridY] = hill.height;
        }

        int attackerHeight = 0;
        for (Hill hill : hills) {
            if (hill.gridX == attacker.gridX && hill.gridY == attacker.gridY) {
                attackerHeight = hill.height;
                break;
            }
        }

        // Используем существующий метод проверки Line of Sight с учётом стен
        return isLineOfSightClearWithWalls(attacker.gridX, attacker.gridY,
                target.gridX, target.gridY,
                attackerHeight, hillHeight);
    }

    private Point findClosestWallCell(Enemy enemy, int targetX, int targetY) {
        int radius = 5;
        Point bestCell = null;
        int bestDist = Integer.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int checkX = targetX + dx;
                int checkY = targetY + dy;

                if (checkX < 0 || checkX >= 80 || checkY < 0 || checkY >= 80) continue;
                if (!canMoveToForEnemy(enemy, checkX, checkY)) continue;

                // Проверяем, есть ли стена рядом с этой клеткой (со стороны цели)
                boolean hasWallNearby = false;
                for (int[] dir : new int[][]{{0,-1},{0,1},{-1,0},{1,0}}) {
                    int wallX = checkX + dir[0];
                    int wallY = checkY + dir[1];
                    if (isWallAt(wallX, wallY)) {
                        hasWallNearby = true;
                        break;
                    }
                }

                if (hasWallNearby) {
                    int dist = Math.abs(enemy.gridX - checkX) + Math.abs(enemy.gridY - checkY);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestCell = new Point(checkX, checkY);
                    }
                }
            }
        }

        return bestCell;
    }

    // Движение вдоль стены к цели (обходим препятствие)
    private boolean moveAlongWallToTarget(Enemy enemy, int targetX, int targetY) {
        // Определяем направление к цели
        int dx = targetX - enemy.gridX;
        int dy = targetY - enemy.gridY;

        // Пробуем двигаться в перпендикулярном направлении к стене
        Enemy.Direction[] attempts;

        if (Math.abs(dx) > Math.abs(dy)) {
            // Нужно двигаться по горизонтали, но стена мешает - пробуем вверх/вниз
            attempts = new Enemy.Direction[]{Enemy.Direction.UP, Enemy.Direction.DOWN};
        } else {
            // Нужно двигаться по вертикали, но стена мешает - пробуем влево/вправо
            attempts = new Enemy.Direction[]{Enemy.Direction.LEFT, Enemy.Direction.RIGHT};
        }

        for (Enemy.Direction dir : attempts) {
            int newX = enemy.gridX;
            int newY = enemy.gridY;
            switch(dir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            if (canMoveToForEnemy(enemy, newX, newY)) {
                System.out.println("  🚪 ОБХОЖУ СТЕНУ: " + dir);
                boolean isVisible = isEnemyVisibleByAnyone(enemy);
                enemy.consumeMovePoints();
                enemy.startAnimatedMove(newX, newY, dir, CELL_SIZE, TANK_SIZE, isVisible);
                if (isVisible && soundManager != null) {
                    soundManager.playEnemyMoveSound();
                }
                return true;
            }
        }

        return false;
    }

    // Завершение хода врага
    // В finishEnemyTurn, добавьте:
    private void finishEnemyTurn(Enemy enemy) {
        System.out.println("=== Враг [" + enemy.gridX + "," + enemy.gridY + "] закончил ход ===");

        enemy.setCurrentMovementTarget(null);
        enemy.resetStuckCounter();

        // ===== СБРАСЫВАЕМ ТОЛЬКО ЕСЛИ ЭТОТ ВРАГ БЫЛ АКТИВНЫМ =====
        if (currentActiveEnemy == enemy) {
            setCurrentActiveEnemy(null);
            if (gamePanel != null) {
                gamePanel.onEnemyTurnEnd();
            }
        }

        javax.swing.Timer delayTimer = new javax.swing.Timer(150, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentEnemyIndex++;
                processNextEnemy();
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    // В GameWorld.java, добавьте метод:

    private boolean performTacticalRetreatWithMemory(Enemy enemy) {
        System.out.println("  ⚠⚠⚠ ОТСТУПЛЕНИЕ с использованием памяти! enemies=" +
                enemy.getMaxEnemiesSeenThisTurn() + ", allies=" + enemy.getAlliesCount());

        // 1. Используем запомненные позиции врагов для определения направления отступления
        List<Point> rememberedEnemies = enemy.getRememberedEnemyPositions();

        if (!rememberedEnemies.isEmpty()) {
            // Вычисляем среднюю позицию врагов (центр угрозы)
            int avgX = 0, avgY = 0;
            for (Point p : rememberedEnemies) {
                avgX += p.x;
                avgY += p.y;
            }
            avgX /= rememberedEnemies.size();
            avgY /= rememberedEnemies.size();

            // Отступаем в противоположную сторону от центра угрозы
            int dirX = enemy.gridX - avgX;
            int dirY = enemy.gridY - avgY;

            // Нормализуем направление и пробуем двигаться
            if (Math.abs(dirX) > Math.abs(dirY)) {
                if (dirX > 0) {
                    if (tryMoveInDirection(enemy, enemy.gridX + 1, enemy.gridY, Enemy.Direction.RIGHT)) return true;
                } else if (dirX < 0) {
                    if (tryMoveInDirection(enemy, enemy.gridX - 1, enemy.gridY, Enemy.Direction.LEFT)) return true;
                }
            } else {
                if (dirY > 0) {
                    if (tryMoveInDirection(enemy, enemy.gridX, enemy.gridY + 1, Enemy.Direction.DOWN)) return true;
                } else if (dirY < 0) {
                    if (tryMoveInDirection(enemy, enemy.gridX, enemy.gridY - 1, Enemy.Direction.UP)) return true;
                }
            }
        }

        // 2. Если не удалось отступить от запомненных позиций - ищем укрытие
        Point coverSpot = findBestRetreatSpot(enemy);
        if (coverSpot != null) {
            System.out.println("  [ОТСТУПЛЕНИЕ] Двигаюсь в укрытие [" + coverSpot.x + "," + coverSpot.y + "]");
            boolean moved = moveTowardsPositionWithPathfinding(enemy, coverSpot.x, coverSpot.y);
            if (moved) return true;
        }

        // 3. Просто убегаем от ближайшего врага
        boolean fled = fleeFromNearestEnemy(enemy);
        if (fled) return true;

        // 4. Случайное движение
        System.out.println("  [ОТСТУПЛЕНИЕ] Нет вариантов! Случайное движение...");
        return randomMoveOneStep(enemy);
    }

    // Движение вперёд (агрессивное наступление)
    private boolean advanceTowardsEnemy(Enemy enemy) {
        if (enemy.isMoving()) return false;

        System.out.println("  [НАСТУПЛЕНИЕ] Двигаюсь к врагу!");

        // Приоритет: стрелять, если можем
        int distanceToPlayer = Math.abs(enemy.gridX - player.gridX) + Math.abs(enemy.gridY - player.gridY);
        int attackRange = (enemy.getBehaviorType() == Enemy.BehaviorType.SNIPER) ? 12 : 8;

        // Если уже в зоне атаки - не двигаемся, ждём выстрела
        if (distanceToPlayer <= attackRange && isPlayerVisibleToEnemy(enemy)) {
            System.out.println("  [НАСТУПЛЕНИЕ] Уже в зоне атаки, жду команды на выстрел!");
            return false;
        }

        // Иначе двигаемся к игроку
        return moveTowardsPlayerWithPathfinding(enemy);
    }

    // Завершение хода врага с задержкой
    private void finishEnemyTurnWithDelay(Enemy enemy) {
        javax.swing.Timer delayTimer = new javax.swing.Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentEnemyIndex++;
                processNextEnemy();
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    // Ожидание завершения анимации движения врага
    private void waitForEnemyMoveComplete(Enemy enemy) {
        javax.swing.Timer waitTimer = new javax.swing.Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!enemy.isMoving()) {
                    // Анимация завершена - переходим к следующему врагу
                    ((Timer) e.getSource()).stop();
                    currentEnemyIndex++;
                    processNextEnemy();
                }
            }
        });
        waitTimer.setRepeats(true);
        waitTimer.start();
    }

    // Движение к игроку с использованием поиска пути
    private boolean moveTowardsPlayerWithPathfinding(Enemy enemy) {
        if (enemy.isMoving()) return false;

        System.out.println("  Поиск пути к игроку [" + player.gridX + "," + player.gridY + "]");

        List<Point> path = findPathForEnemy(enemy.gridX, enemy.gridY, player.gridX, player.gridY);

        // Пробуем использовать pathfinding
        if (!path.isEmpty()) {
            Point nextStep = path.get(0);
            Enemy.Direction dir = getDirectionForEnemy(enemy.gridX, enemy.gridY, nextStep.x, nextStep.y);

            if (dir != null && canMoveToForEnemy(enemy, nextStep.x, nextStep.y)) {
                boolean willBeVisible = isEnemyVisibleByAnyoneAfterMove(enemy, nextStep.x, nextStep.y);
                boolean isCurrentlyVisible = isEnemyVisibleByAnyone(enemy);
                boolean shouldAnimate = isCurrentlyVisible || willBeVisible;

                System.out.println("    ✅ Путь найден! Шаг: " + dir + " -> [" + nextStep.x + "," + nextStep.y + "]");
                enemy.consumeMovePoints();
                enemy.startAnimatedMove(nextStep.x, nextStep.y, dir, CELL_SIZE, TANK_SIZE, shouldAnimate);
                if (shouldAnimate && soundManager != null) {
                    soundManager.playEnemyMoveSound();
                }
                return true;
            }
        }

        // ===== НОВОЕ: Если путь не найден, пробуем двигаться напрямую к игроку (эвристика) =====
        System.out.println("    ⚠ Путь не найден, пробую двигаться напрямую!");

        Enemy.Direction bestDir = null;
        int bestDist = Math.abs(enemy.gridX - player.gridX) + Math.abs(enemy.gridY - player.gridY);

        for (Enemy.Direction dir : Enemy.Direction.values()) {
            int newX = enemy.gridX, newY = enemy.gridY;
            switch(dir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            if (canMoveToForEnemy(enemy, newX, newY)) {
                int newDist = Math.abs(newX - player.gridX) + Math.abs(newY - player.gridY);
                if (newDist < bestDist) {
                    bestDist = newDist;
                    bestDir = dir;
                }
            }
        }

        if (bestDir != null) {
            int newX = enemy.gridX, newY = enemy.gridY;
            switch(bestDir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            boolean willBeVisible = isEnemyVisibleByAnyoneAfterMove(enemy, newX, newY);
            boolean isCurrentlyVisible = isEnemyVisibleByAnyone(enemy);
            boolean shouldAnimate = isCurrentlyVisible || willBeVisible;

            System.out.println("    ✅ Движение напрямую: " + bestDir + " -> [" + newX + "," + newY + "]");
            enemy.consumeMovePoints();
            enemy.startAnimatedMove(newX, newY, bestDir, CELL_SIZE, TANK_SIZE, shouldAnimate);
            if (shouldAnimate && soundManager != null) {
                soundManager.playEnemyMoveSound();
            }
            return true;
        }

        System.out.println("    ❌ Нет доступных направлений для движения!");
        return false;
    }

    // Движение к запомненной позиции с поиском пути (ИСПРАВЛЕННАЯ ВЕРСИЯ)
    private boolean moveTowardsPositionWithPathfinding(Enemy enemy, int targetX, int targetY) {
        if (enemy.isMoving()) return false;

        // Если цель на той же горизонтали или вертикали - двигаемся прямо
        if (enemy.gridX == targetX || enemy.gridY == targetY) {
            Enemy.Direction dir;
            if (enemy.gridX == targetX) {
                dir = targetY > enemy.gridY ? Enemy.Direction.DOWN : Enemy.Direction.UP;
            } else {
                dir = targetX > enemy.gridX ? Enemy.Direction.RIGHT : Enemy.Direction.LEFT;
            }

            int newX = enemy.gridX, newY = enemy.gridY;
            switch(dir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            if (canMoveToForEnemy(enemy, newX, newY)) {
                System.out.println("  🏃 ПРЯМОЕ ДВИЖЕНИЕ К ЦЕЛИ: " + dir);
                enemy.consumeMovePoints();
                boolean isVisible = isEnemyVisibleByAnyone(enemy);
                enemy.startAnimatedMove(newX, newY, dir, CELL_SIZE, TANK_SIZE, isVisible);
                return true;
            }
        }

        // Проверка "уже рядом" - для штурмового режима ИГНОРИРУЕМ
        if (!enemy.isAssaultMode()) {
            if (Math.abs(enemy.gridX - targetX) + Math.abs(enemy.gridY - targetY) == 1) {
                System.out.println("  Уже рядом с целью (соседняя клетка)!");
                return false;
            }
        } else {
            // В штурмовом режиме: если достигли ТОЧНО точки штурма
            if (enemy.gridX == targetX && enemy.gridY == targetY) {
                System.out.println("  🏁 ДОСТИГНУТА ТОЧКА ШТУРМА!");
                enemy.clearAssaultPoint();
                enemy.setAssaultMode(false);
                return false;
            }
        }

        // В moveTowardsPositionWithPathfinding, замените проверку:
        if (Math.abs(enemy.gridX - targetX) + Math.abs(enemy.gridY - targetY) == 1) {
            System.out.println("  Уже рядом с целью (соседняя клетка)!");
            return false;
        }

        System.out.println("  Поиск пути к [" + targetX + "," + targetY + "]");

        List<Point> path = findPathForEnemy(enemy.gridX, enemy.gridY, targetX, targetY);

        if (!path.isEmpty()) {
            Point nextStep = path.get(0);
            Enemy.Direction dir = getDirectionForEnemy(enemy.gridX, enemy.gridY, nextStep.x, nextStep.y);

            if (dir != null && canMoveToForEnemy(enemy, nextStep.x, nextStep.y)) {
                // Проверяем, виден ли враг кому-то из команды
                boolean isVisible = isEnemyVisibleByAnyone(enemy);

                System.out.println("    ✅ Путь найден! Шаг: " + dir + " -> [" + nextStep.x + "," + nextStep.y + "]");
                enemy.consumeMovePoints();

                if (isVisible) {
                    // Видимый враг - плавная анимация
                    enemy.startAnimatedMove(nextStep.x, nextStep.y, dir, CELL_SIZE, TANK_SIZE, true);
                    if (soundManager != null) {
                        soundManager.playEnemyMoveSound();
                    }
                } else {
                    // Невидимый враг - мгновенная телепортация
                    enemy.teleportMove(nextStep.x, nextStep.y, dir);
                }
                return true;
            }
        }

        // Прямое движение (эвристика) - если pathfinding не сработал
        System.out.println("    ⚠ Путь не найден, пробую двигаться напрямую!");

        Enemy.Direction bestDir = null;
        int bestDist = Math.abs(enemy.gridX - targetX) + Math.abs(enemy.gridY - targetY);

        for (Enemy.Direction dir : Enemy.Direction.values()) {
            int newX = enemy.gridX, newY = enemy.gridY;
            switch(dir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            if (canMoveToForEnemy(enemy, newX, newY)) {
                int newDist = Math.abs(newX - targetX) + Math.abs(newY - targetY);
                if (newDist < bestDist) {
                    bestDist = newDist;
                    bestDir = dir;
                }
            }
        }

        if (bestDir != null) {
            int newX = enemy.gridX, newY = enemy.gridY;
            switch(bestDir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            boolean isVisible = isEnemyVisibleByAnyone(enemy);

            System.out.println("    ✅ Движение напрямую: " + bestDir + " -> [" + newX + "," + newY + "]");
            enemy.consumeMovePoints();

            if (isVisible) {
                enemy.startAnimatedMove(newX, newY, bestDir, CELL_SIZE, TANK_SIZE, true);
                if (soundManager != null) {
                    soundManager.playEnemyMoveSound();
                }
            } else {
                enemy.teleportMove(newX, newY, bestDir);
            }
            return true;
        }

        // Если путь не найден - пробуем найти ЛЮБУЮ доступную клетку рядом
        if (path.isEmpty()) {
            System.out.println("    ⚠ Путь не найден, ищу любую доступную клетку!");

            Enemy.Direction[] dirs = {Enemy.Direction.UP, Enemy.Direction.DOWN,
                    Enemy.Direction.LEFT, Enemy.Direction.RIGHT};
            for (Enemy.Direction dir : dirs) {
                int newX = enemy.gridX, newY = enemy.gridY;
                switch(dir) {
                    case UP: newY--; break;
                    case DOWN: newY++; break;
                    case LEFT: newX--; break;
                    case RIGHT: newX++; break;
                }
                if (canMoveToForEnemy(enemy, newX, newY)) {
                    boolean isVisible = isEnemyVisibleByAnyone(enemy);
                    enemy.consumeMovePoints();
                    enemy.startAnimatedMove(newX, newY, dir, CELL_SIZE, TANK_SIZE, isVisible);
                    System.out.println("    ✅ Найдена доступная клетка: " + dir + " -> [" + newX + "," + newY + "]");
                    return true;
                }
            }

            System.out.println("    ❌ Нет доступных клеток для движения!");
            return false;
        }

        System.out.println("    ❌ Нет доступных направлений!");
        return false;
    }



    private void finishEnemyTurn(Enemy enemy, int stepsTaken, javax.swing.Timer timer) {
        timer.stop();
        System.out.println("=== Враг закончил ход ===");
        System.out.println("Сделано шагов: " + stepsTaken);
        System.out.println("Осталось очков хода: " + enemy.movePoints);
        System.out.println("Атаковал: " + (enemy.hasAttackedThisTurn ? "ДА" : "НЕТ"));

        javax.swing.Timer delayTimer = new javax.swing.Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentEnemyIndex++;
                processNextEnemy();
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    public boolean isAnyEnemyMoving() {
        for (Enemy enemy : enemies) {
            if (enemy.isMoving()) return true;
        }
        return false;
    }

    // НОВЫЙ МЕТОД: Проверка, видит ли враг игрока
    private boolean isPlayerVisibleToEnemy(Enemy enemy) {
        int dx = Math.abs(enemy.gridX - player.gridX);
        int dy = Math.abs(enemy.gridY - player.gridY);
        int distSq = dx*dx + dy*dy;

        if (distSq > ENEMY_VIEW_RADIUS * ENEMY_VIEW_RADIUS) {
            return false;
        }

        int[][] hillHeight = new int[FIELD_SIZE / CELL_SIZE][FIELD_SIZE / CELL_SIZE];
        for (Hill hill : hills) {
            hillHeight[hill.gridX][hill.gridY] = hill.height;
        }

        int enemyHeight = 0;
        for (Hill hill : hills) {
            if (hill.gridX == enemy.gridX && hill.gridY == enemy.gridY) {
                enemyHeight = hill.height;
                break;
            }
        }

        // ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД С ПРОВЕРКОЙ СТЕН
        return isLineOfSightClearWithWalls(enemy.gridX, enemy.gridY, player.gridX, player.gridY, enemyHeight, hillHeight);
    }

    // Проверка, видит ли враг какого-либо союзника
    private boolean isAnyFriendlyVisibleToEnemy(Enemy enemy) {
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                int dx = Math.abs(enemy.gridX - friendly.gridX);
                int dy = Math.abs(enemy.gridY - friendly.gridY);
                int distSq = dx*dx + dy*dy;

                if (distSq <= ENEMY_VIEW_RADIUS * ENEMY_VIEW_RADIUS) {
                    int[][] hillHeight = new int[FIELD_SIZE / CELL_SIZE][FIELD_SIZE / CELL_SIZE];
                    for (Hill hill : hills) {
                        hillHeight[hill.gridX][hill.gridY] = hill.height;
                    }

                    int enemyHeight = 0;
                    for (Hill hill : hills) {
                        if (hill.gridX == enemy.gridX && hill.gridY == enemy.gridY) {
                            enemyHeight = hill.height;
                            break;
                        }
                    }

                    int friendlyHeight = 0;
                    for (Hill hill : hills) {
                        if (hill.gridX == friendly.gridX && hill.gridY == friendly.gridY) {
                            friendlyHeight = hill.height;
                            break;
                        }
                    }

                    if (isLineOfSightClearWithWalls(enemy.gridX, enemy.gridY, friendly.gridX, friendly.gridY, enemyHeight, hillHeight)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Получить ближайшего видимого союзника
    private FriendlyUnit getClosestVisibleFriendly(Enemy enemy) {
        FriendlyUnit closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited && isFriendlyVisibleToEnemy(enemy, friendly)) {
                int distance = Math.abs(enemy.gridX - friendly.gridX) + Math.abs(enemy.gridY - friendly.gridY);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = friendly;
                }
            }
        }
        return closest;
    }

    // Проверка, видит ли враг конкретного союзника
    private boolean isFriendlyVisibleToEnemy(Enemy enemy, FriendlyUnit friendly) {
        if (!friendly.isAlive || !friendly.isRecruited) return false;

        int dx = Math.abs(enemy.gridX - friendly.gridX);
        int dy = Math.abs(enemy.gridY - friendly.gridY);
        int distSq = dx*dx + dy*dy;

        if (distSq > ENEMY_VIEW_RADIUS * ENEMY_VIEW_RADIUS) return false;

        int[][] hillHeight = new int[FIELD_SIZE / CELL_SIZE][FIELD_SIZE / CELL_SIZE];
        for (Hill hill : hills) {
            hillHeight[hill.gridX][hill.gridY] = hill.height;
        }

        int enemyHeight = 0;
        for (Hill hill : hills) {
            if (hill.gridX == enemy.gridX && hill.gridY == enemy.gridY) {
                enemyHeight = hill.height;
                break;
            }
        }

        return isLineOfSightClearWithWalls(enemy.gridX, enemy.gridY, friendly.gridX, friendly.gridY, enemyHeight, hillHeight);
    }

    // НОВЫЙ МЕТОД: Движение к игроку
    // Движение к игроку (один шаг)
    // Полностью перепишите метод moveTowardsPlayerOneStep:
    // В GameWorld.java, исправьте метод moveTowardsPlayerOneStep:

    private boolean moveTowardsPlayerOneStep(Enemy enemy) {
        if (enemy.isMoving()) {
            return false;
        }

        System.out.println("\n  [ВРАГ " + enemy.gridX + "," + enemy.gridY + "] Ищет путь к игроку [" + player.gridX + "," + player.gridY + "]");

        // Пытаемся найти полный путь к игроку
        List<Point> fullPath = findPathForEnemy(enemy.gridX, enemy.gridY, player.gridX, player.gridY);

        if (!fullPath.isEmpty()) {
            // Берем только первый шаг
            Point nextStep = fullPath.get(0);
            Enemy.Direction dir = getDirectionForEnemy(enemy.gridX, enemy.gridY, nextStep.x, nextStep.y);

            if (dir != null && canMoveToForEnemy(enemy, nextStep.x, nextStep.y)) {
                boolean willBeVisible = isCellVisibleToPlayer(nextStep.x, nextStep.y);
                boolean isCurrentlyVisible = isEnemyVisibleToPlayer(enemy);
                boolean shouldAnimate = isCurrentlyVisible || willBeVisible;

                System.out.println("    ✅ Движение ПО ПУТИ: " + dir + " -> [" + nextStep.x + "," + nextStep.y + "]");

                enemy.consumeMovePoints();
                enemy.startAnimatedMove(nextStep.x, nextStep.y, dir, CELL_SIZE, TANK_SIZE, shouldAnimate);

                if (shouldAnimate && soundManager != null) {
                    soundManager.playEnemyMoveSound();
                }
                return true;
            }
        }

        // Если путь не найден, пробуем простую эвристику (только если видим игрока)
        if (isPlayerVisibleToEnemy(enemy)) {
            System.out.println("    ⚠ Путь не найден, пробуем двигаться к игроку напрямую");

            // Пробуем все 4 направления, выбираем лучшее
            Enemy.Direction[] dirs = {Enemy.Direction.UP, Enemy.Direction.DOWN, Enemy.Direction.LEFT, Enemy.Direction.RIGHT};
            Enemy.Direction bestDir = null;
            int bestDist = Math.abs(enemy.gridX - player.gridX) + Math.abs(enemy.gridY - player.gridY);

            for (Enemy.Direction dir : dirs) {
                int newX = enemy.gridX;
                int newY = enemy.gridY;
                switch(dir) {
                    case UP: newY--; break;
                    case DOWN: newY++; break;
                    case LEFT: newX--; break;
                    case RIGHT: newX++; break;
                }

                if (canMoveToForEnemy(enemy, newX, newY)) {
                    int newDist = Math.abs(newX - player.gridX) + Math.abs(newY - player.gridY);
                    if (newDist < bestDist) {
                        bestDist = newDist;
                        bestDir = dir;
                    }
                }
            }

            if (bestDir != null) {
                int newX = enemy.gridX;
                int newY = enemy.gridY;
                switch(bestDir) {
                    case UP: newY--; break;
                    case DOWN: newY++; break;
                    case LEFT: newX--; break;
                    case RIGHT: newX++; break;
                }

                boolean willBeVisible = isCellVisibleToPlayer(newX, newY);
                boolean isCurrentlyVisible = isEnemyVisibleToPlayer(enemy);
                boolean shouldAnimate = isCurrentlyVisible || willBeVisible;

                System.out.println("    ✅ Движение ПО ПРЯМОЙ: " + bestDir + " -> [" + newX + "," + newY + "]");

                enemy.consumeMovePoints();
                enemy.startAnimatedMove(newX, newY, bestDir, CELL_SIZE, TANK_SIZE, shouldAnimate);

                if (shouldAnimate && soundManager != null) {
                    soundManager.playEnemyMoveSound();
                }
                return true;
            }
        }

        System.out.println("    ❌ НЕТ ДОСТУПНЫХ НАПРАВЛЕНИЙ ДЛЯ ДВИЖЕНИЯ!");
        return false;
    }

    // Движение к запомненной позиции (один шаг)
    private boolean moveTowardsPositionOneStep(Enemy enemy, int targetX, int targetY) {
        if (enemy.isMoving()) return false;

        System.out.println("  moveTowardsPositionOneStep: от [" + enemy.gridX + "," + enemy.gridY +
                "] к [" + targetX + "," + targetY + "]");

        // СНАЧАЛА ПЫТАЕМСЯ НАЙТИ ПУТЬ
        List<Point> path = findPathForEnemy(enemy.gridX, enemy.gridY, targetX, targetY);

        if (!path.isEmpty()) {
            Point nextStep = path.get(0);
            Enemy.Direction dir = getDirectionForEnemy(enemy.gridX, enemy.gridY, nextStep.x, nextStep.y);

            if (dir != null && canMoveToForEnemy(enemy, nextStep.x, nextStep.y)) {
                boolean willBeVisible = isCellVisibleToPlayer(nextStep.x, nextStep.y);
                boolean isCurrentlyVisible = isEnemyVisibleToPlayer(enemy);
                boolean shouldAnimate = isCurrentlyVisible || willBeVisible;

                System.out.println("    moveTowardsPositionOneStep (по пути): двигаемся " + dir +
                        " на [" + nextStep.x + "," + nextStep.y + "], анимация=" + shouldAnimate);

                enemy.consumeMovePoints();
                enemy.startAnimatedMove(nextStep.x, nextStep.y, dir, CELL_SIZE, TANK_SIZE, shouldAnimate);

                if (shouldAnimate && soundManager != null) {
                    soundManager.playEnemyMoveSound();
                }
                return true;
            }
        }

        // ЕСЛИ ПУТЬ НЕ НАЙДЕН, ПЫТАЕМСЯ ПРОСТО ПРИБЛИЗИТЬСЯ
        int currentDist = Math.abs(enemy.gridX - targetX) + Math.abs(enemy.gridY - targetY);

        Enemy.Direction[] dirs = {Enemy.Direction.UP, Enemy.Direction.DOWN, Enemy.Direction.LEFT, Enemy.Direction.RIGHT};
        Enemy.Direction bestDir = null;
        int bestDist = currentDist;

        // Приоритезируем направления
        List<Enemy.Direction> prioritizedDirs = new ArrayList<>();
        if (targetX > enemy.gridX) prioritizedDirs.add(Enemy.Direction.RIGHT);
        if (targetX < enemy.gridX) prioritizedDirs.add(Enemy.Direction.LEFT);
        if (targetY > enemy.gridY) prioritizedDirs.add(Enemy.Direction.DOWN);
        if (targetY < enemy.gridY) prioritizedDirs.add(Enemy.Direction.UP);

        for (Enemy.Direction dir : prioritizedDirs) {
            int newX = enemy.gridX;
            int newY = enemy.gridY;
            switch(dir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            if (canMoveToForEnemy(enemy, newX, newY)) {
                int newDist = Math.abs(newX - targetX) + Math.abs(newY - targetY);
                if (newDist < bestDist) {
                    bestDist = newDist;
                    bestDir = dir;
                }
            }
        }

        if (bestDir != null) {
            int newX = enemy.gridX;
            int newY = enemy.gridY;
            switch(bestDir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            boolean willBeVisible = isCellVisibleToPlayer(newX, newY);
            boolean isCurrentlyVisible = isEnemyVisibleToPlayer(enemy);
            boolean shouldAnimate = isCurrentlyVisible || willBeVisible;

            System.out.println("    moveTowardsPositionOneStep (прямая): двигаемся " + bestDir +
                    " на [" + newX + "," + newY + "], анимация=" + shouldAnimate);

            enemy.consumeMovePoints();
            enemy.startAnimatedMove(newX, newY, bestDir, CELL_SIZE, TANK_SIZE, shouldAnimate);

            if (shouldAnimate && soundManager != null) {
                soundManager.playEnemyMoveSound();
            }
            return true;
        }

        System.out.println("    moveTowardsPositionOneStep: НЕТ доступных направлений!");
        return false;
    }

    // Случайное движение (один шаг)
    // В GameWorld.java, замените метод randomMoveOneStep на этот:

    private boolean randomMoveOneStep(Enemy enemy) {
        if (enemy.isMoving()) return false;

        System.out.println("\n  [ВРАГ " + enemy.gridX + "," + enemy.gridY + "] Случайное движение");

        List<Enemy.Direction> possibleMoves = new ArrayList<>();
        Enemy.Direction[] allDirs = Enemy.Direction.values();

        for (Enemy.Direction dir : allDirs) {
            int newX = enemy.gridX;
            int newY = enemy.gridY;
            switch(dir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            if (canMoveToForEnemy(enemy, newX, newY)) {
                possibleMoves.add(dir);
            }
        }

        if (!possibleMoves.isEmpty()) {
            Enemy.Direction chosenDir = possibleMoves.get(random.nextInt(possibleMoves.size()));
            int newX = enemy.gridX;
            int newY = enemy.gridY;

            switch(chosenDir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            // ===== ИСПРАВЛЕНИЕ: проверяем видимость ОТ ВСЕЙ КОМАНДЫ =====
            boolean willBeVisible = isEnemyVisibleByAnyoneAfterMove(enemy, newX, newY);
            boolean isCurrentlyVisible = isEnemyVisibleByAnyone(enemy);
            boolean shouldAnimate = isCurrentlyVisible || willBeVisible;

            System.out.println("    ✅ Случайное движение: " + chosenDir + " -> [" + newX + "," + newY + "]");
            System.out.println("    shouldAnimate=" + shouldAnimate);

            enemy.consumeMovePoints();
            enemy.startAnimatedMove(newX, newY, chosenDir, CELL_SIZE, TANK_SIZE, shouldAnimate);

            if (shouldAnimate && soundManager != null) {
                soundManager.playEnemyMoveSound();
            }
            return true;
        }

        System.out.println("    ❌ НЕТ ДОСТУПНЫХ НАПРАВЛЕНИЙ!");
        return false;
    }

    // ДОБАВЬТЕ МЕТОД для проверки, видит ли игрок врага:
    private boolean isEnemyVisibleToPlayer(Enemy enemy) {
        if (!enemy.isAlive) return false;
        int dx = Math.abs(enemy.gridX - player.gridX);
        int dy = Math.abs(enemy.gridY - player.gridY);
        if (dx*dx + dy*dy > VIEW_RADIUS * VIEW_RADIUS) return false;
        return visibilityMap[enemy.gridX][enemy.gridY];
    }

    // НОВЫЙ МЕТОД: Атака игрока
    private void attackPlayer(Enemy enemy, GamePanel panel) {

        // ===== ПРОВЕРКА ПРЯМОЙ ВИДИМОСТИ =====
        if (!isLineOfSightClearForShot(enemy.gridX, enemy.gridY, player.gridX, player.gridY)) {
            System.out.println("🧱 СТЕНА НА ЛИНИИ ОГНЯ! Враг не может выстрелить!");
            // Пытаемся подойти ближе
            if (enemy.movePoints >= enemy.moveCost) {
                moveTowardsPlayerWithPathfinding(enemy);
            }
            return;
        }

        // ===== ДОБАВЛЯЕМ ПРОВЕРКУ ПРЯМОЙ ВИДИМОСТИ ПЕРЕД ВЫСТРЕЛОМ =====
        if (!isLineOfSightClearForShot(enemy.gridX, enemy.gridY, player.gridX, player.gridY)) {
            System.out.println("🧱 СТЕНА/ПРЕПЯТСТВИЕ НА ЛИНИИ ОГНЯ! Враг не может выстрелить в игрока!");

            // Если не может стрелять - пытается подойти ближе
            if (enemy.movePoints >= enemy.moveCost) {
                System.out.println("  🔄 Пытаюсь подойти ближе для выстрела!");
                moveTowardsPlayerWithPathfinding(enemy);
            }



            return;
        }

        int dx = Math.abs(enemy.gridX - player.gridX);
        int dy = Math.abs(enemy.gridY - player.gridY);
        double distance = Math.sqrt(dx*dx + dy*dy);

        double hitChance = 0.7 * (1 - Math.min(0.5, distance / 20.0));
        hitChance = hitChance * (enemy.weaponAccuracy / 50.0);
        hitChance = Math.min(0.9, Math.max(0.1, hitChance));

        System.out.println("=== ВРАГ АТАКУЕТ! ===");
        System.out.println("Дистанция: " + distance);
        System.out.println("Шанс попадания: " + (int)(hitChance * 100) + "%");
        System.out.println("Количество снарядов: " + enemy.burstSize);

        if (soundManager != null) {
            soundManager.playEnemyShootSound(enemy.weaponCaliber);
        }

        if (panel != null && isEnemyVisibleByAnyone(enemy)) {
            panel.centerCameraOnEnemy(enemy);
        }

        double startX = enemy.gridX * CELL_SIZE + CELL_SIZE / 2.0;
        double startY = enemy.gridY * CELL_SIZE + CELL_SIZE / 2.0;

        enemy.registerShot();

        for (int i = 0; i < enemy.burstSize; i++) {
            boolean isCrit = random.nextDouble() < enemy.critChance;
            int damage = enemy.weaponDamage;
            if (isCrit) {
                damage *= (1.5 + random.nextDouble() * 1.5);
                System.out.println("  КРИТ! Урон: " + damage);
            }

            double spread = (random.nextDouble() - 0.5) * 0.1;
            double actualHitChance = Math.min(0.95, Math.max(0.05, hitChance + spread));

            Projectile projectile = new Projectile(startX, startY, player, damage, actualHitChance, this, enemy);

            if (panel != null) {
                panel.addProjectile(projectile);
            }
        }
    }

    private void attackFriendly(Enemy enemy, FriendlyUnit friendly, GamePanel panel) {
        // ===== ДОБАВЛЯЕМ ПРОВЕРКУ ПРЯМОЙ ВИДИМОСТИ ПЕРЕД ВЫСТРЕЛОМ =====
        if (!isLineOfSightClearForShot(enemy.gridX, enemy.gridY, friendly.gridX, friendly.gridY)) {
            System.out.println("🧱 СТЕНА/ПРЕПЯТСТВИЕ НА ЛИНИИ ОГНЯ! Враг не может выстрелить в " + friendly.name + "!");

            // Если не может стрелять - пытается подойти ближе
            if (enemy.movePoints >= enemy.moveCost) {
                System.out.println("  🔄 Пытаюсь подойти ближе для выстрела!");
                moveTowardsPositionWithPathfinding(enemy, friendly.gridX, friendly.gridY);
            }
            return;
        }

        int dx = Math.abs(enemy.gridX - friendly.gridX);
        int dy = Math.abs(enemy.gridY - friendly.gridY);
        double distance = Math.sqrt(dx*dx + dy*dy);

        double hitChance = 0.7 * (1 - Math.min(0.5, distance / 20.0));
        hitChance = hitChance * (enemy.weaponAccuracy / 50.0);
        hitChance = Math.min(0.9, Math.max(0.1, hitChance));

        System.out.println("=== ВРАГ АТАКУЕТ СОЮЗНИКА " + friendly.name + "! ===");
        System.out.println("Дистанция: " + distance);
        System.out.println("Шанс попадания: " + (int)(hitChance * 100) + "%");
        System.out.println("Количество снарядов: " + enemy.burstSize);

        if (soundManager != null) {
            soundManager.playEnemyShootSound(enemy.weaponCaliber);
        }

        double startX = enemy.gridX * CELL_SIZE + CELL_SIZE / 2.0;
        double startY = enemy.gridY * CELL_SIZE + CELL_SIZE / 2.0;

        for (int i = 0; i < enemy.burstSize; i++) {
            boolean isCrit = random.nextDouble() < enemy.critChance;
            int damage = enemy.weaponDamage;
            if (isCrit) {
                damage *= (1.5 + random.nextDouble() * 1.5);
                System.out.println("  КРИТ! Урон: " + damage);
            }

            double spread = (random.nextDouble() - 0.5) * 0.1;
            double actualHitChance = Math.min(0.95, Math.max(0.05, hitChance + spread));

            Projectile projectile = new Projectile(startX, startY, friendly, damage, actualHitChance, this);

            if (panel != null) {
                panel.addProjectile(projectile);
            }
        }
    }



    // НОВЫЙ МЕТОД: Поиск пути для врага
    public List<Point> findPathForEnemy(int startX, int startY, int targetX, int targetY) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        // Выход за границы
        if (startX < 0 || startX >= gridWidth || startY < 0 || startY >= gridHeight ||
                targetX < 0 || targetX >= gridWidth || targetY < 0 || targetY >= gridHeight) {
            return new ArrayList<>();
        }

        // Если старт = цель
        if (startX == targetX && startY == targetY) {
            return new ArrayList<>();
        }

        // BFS для поиска пути
        boolean[][] visited = new boolean[gridWidth][gridHeight];
        Point[][] previous = new Point[gridWidth][gridHeight];
        Queue<Point> queue = new LinkedList<>();

        queue.add(new Point(startX, startY));
        visited[startX][startY] = true;

        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        int maxIterations = gridWidth * gridHeight;
        int iterations = 0;

        while (!queue.isEmpty() && iterations < maxIterations) {
            iterations++;
            Point current = queue.poll();

            if (current.x == targetX && current.y == targetY) {
                // Путь найден!
                List<Point> path = new ArrayList<>();
                Point step = new Point(targetX, targetY);
                while (!(step.x == startX && step.y == startY)) {
                    path.add(0, step);
                    step = previous[step.x][step.y];
                    if (step == null) break;
                }
                System.out.println("  Путь найден! Длина: " + path.size());
                return path;
            }

            for (int i = 0; i < 4; i++) {
                int newX = current.x + dx[i];
                int newY = current.y + dy[i];

                if (newX >= 0 && newX < gridWidth && newY >= 0 && newY < gridHeight && !visited[newX][newY]) {
                    // ВАЖНО: Проверяем, можно ли пройти через эту клетку
                    // Для целевой клетки делаем исключение (она может быть занята врагом или игроком)
                    boolean isTarget = (newX == targetX && newY == targetY);

                    if (isTarget || canMoveToForEnemy(null, newX, newY)) {
                        visited[newX][newY] = true;
                        previous[newX][newY] = current;
                        queue.add(new Point(newX, newY));
                    }
                }
            }
        }

        System.out.println("  Путь НЕ НАЙДЕН! Попыток: " + iterations);
        // Включаем отладку для понимания проблемы
        debugPrintPathfindingMap(startX, startY, targetX, targetY);
        return new ArrayList<>();
    }

    // НОВЫЙ МЕТОД: Проверка возможности движения для врага
    public boolean canMoveToForEnemy(Enemy enemy, int gridX, int gridY) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        if (gridX < 0 || gridX >= gridWidth || gridY < 0 || gridY >= gridHeight) {
            return false;
        }

        if (gridX == player.gridX && gridY == player.gridY) {
            return false;
        }

        for (Enemy e : enemies) {
            if (e.isAlive && e != enemy && e.gridX == gridX && e.gridY == gridY) {
                return false;
            }
        }

        for (Wall wall : walls) {
            if (wall.isAlive() && wall.gridX == gridX && wall.gridY == gridY) {
                return false;
            }
        }

        for (Tree tree : trees) {
            if (tree.isAlive && tree.gridX == gridX && tree.gridY == gridY) {
                return false;
            }
        }

        // Проверка на двери
        if (!canMoveThroughDoor(gridX, gridY)) return false;

        // Добавьте проверку на тумбочки
        for (StorageChest chest : storageChests) {
            if (chest.containsCell(gridX, gridY)) {
                return false;
            }
        }

        // Добавьте проверку на мусорные контейнеры
        for (GarbageContainer container : garbageContainers) {
            if (container.containsCell(gridX, gridY)) {
                return false;
            }
        }

        // ВОДА НЕПРОХОДИМА!
        if (isWaterAt(gridX, gridY)) return false;

        return true;
    }

    // ВРЕМЕННЫЙ МЕТОД ДЛЯ ОТЛАДКИ - ПОКАЗЫВАЕТ КАРТУ ПРЕПЯТСТВИЙ
    private void debugPrintPathfindingMap(int startX, int startY, int targetX, int targetY) {
        System.out.println("\n=== ОТЛАДКА ПОИСКА ПУТИ ===");
        System.out.println("Старт: [" + startX + "," + startY + "] -> Цель: [" + targetX + "," + targetY + "]");

        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        // Показываем область вокруг цели (10x10)
        for (int y = Math.max(0, targetY - 5); y <= Math.min(gridHeight - 1, targetY + 5); y++) {
            StringBuilder line = new StringBuilder();
            for (int x = Math.max(0, targetX - 5); x <= Math.min(gridWidth - 1, targetX + 5); x++) {
                if (x == startX && y == startY) {
                    line.append("S ");
                } else if (x == targetX && y == targetY) {
                    line.append("T ");
                } else if (isWallAt(x, y)) {
                    line.append("█ ");
                } else if (!canMoveToForEnemy(null, x, y)) {
                    line.append("X ");
                } else {
                    line.append(". ");
                }
            }
            System.out.println(line);
        }
        System.out.println("========================\n");
    }

    // Добавьте метод для проверки попадания в стену:
    public boolean checkWallHit(int gridX, int gridY, int damage) {
        for (Wall wall : walls) {
            if (wall.isAlive() && wall.gridX == gridX && wall.gridY == gridY) {
                wall.takeDamage(damage);
                if (!wall.isAlive()) {
                    System.out.println("Стена разрушена! [" + gridX + "," + gridY + "]");
                } else {
                    System.out.println("Стена повреждена! Прочность: " + wall.health + "/" + wall.maxHealth);
                }
                return true;
            }
        }
        return false;
    }

    // НОВЫЙ МЕТОД: Определение направления для врага
    private Enemy.Direction getDirectionForEnemy(int fromX, int fromY, int toX, int toY) {
        if (toX > fromX) return Enemy.Direction.RIGHT;
        if (toX < fromX) return Enemy.Direction.LEFT;
        if (toY > fromY) return Enemy.Direction.DOWN;
        if (toY < fromY) return Enemy.Direction.UP;
        return null;
    }



    public void setFieldCache(BufferedImage cache) {
        this.fieldCache = cache;
    }

    public void generateHills() {
        Random rand = new Random(42);
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        int numHills = 50 + rand.nextInt(31);

        for (int i = 0; i < numHills * 2 && hills.size() < numHills; i++) {
            int x = rand.nextInt(gridWidth);
            int y = rand.nextInt(gridHeight);
            if (Math.abs(x - player.gridX) < 5 && Math.abs(y - player.gridY) < 5) continue;

            boolean hillExists = hills.stream().anyMatch(h -> h.gridX == x && h.gridY == y);
            if (hillExists) continue;

            hills.add(new Hill(x, y, 1 + rand.nextInt(3)));
        }
        System.out.println("Сгенерировано холмов: " + hills.size());
    }

    public void onEnemyKilled() {
        if (soundManager != null) {
            soundManager.playEnemyDestroyedSound();
        }
    }

    public void loadEnemies(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        List<Enemy> loadedEnemies = LevelLoader.loadEnemies(levelPath, gridWidth, gridHeight, this);
        if (!loadedEnemies.isEmpty()) {
            enemies.clear();
            for (Enemy enemy : loadedEnemies) {
                // Проверяем, не находится ли враг в препятствии
                if (isValidEnemyPosition(enemy.gridX, enemy.gridY)) {
                    enemies.add(enemy);
                    System.out.println("  Загружен враг в [" + enemy.gridX + "," + enemy.gridY + "]");
                } else {
                    System.out.println("  ⚠ Враг в [" + enemy.gridX + "," + enemy.gridY +
                            "] находится в препятствии! Пропускаем.");
                }
            }
            System.out.println("Загружено врагов: " + enemies.size());
        }
    }

    // Вспомогательный метод проверки валидной позиции для врага
    private boolean isValidEnemyPosition(int x, int y) {
        // Проверка границ
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) return false;

        // Проверка на позицию игрока
        if (x == player.gridX && y == player.gridY) return false;

        // Проверка на стены
        for (Wall wall : walls) {
            if (wall.isAlive() && wall.gridX == x && wall.gridY == y) return false;
        }

        // Проверка на деревья
        for (Tree tree : trees) {
            if (tree.isAlive && tree.gridX == x && tree.gridY == y) return false;
        }

        // Проверка на тумбочки
        for (StorageChest chest : storageChests) {
            if (chest.containsCell(x, y)) return false;
        }

        // Проверка на мусорные контейнеры
        for (GarbageContainer container : garbageContainers) {
            if (container.containsCell(x, y)) return false;
        }

        // Проверка на союзников
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.gridX == x && friendly.gridY == y) return false;
        }

        return true;
    }

    public void generateEnemies(int count) {
        if (!enemies.isEmpty()) {
            System.out.println("Враги уже загружены из файла: " + enemies.size());
            return;
        }

        enemies.clear();
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        Enemy.Direction[] directions = Enemy.Direction.values();
        Enemy.BehaviorType[] behaviorTypes = Enemy.BehaviorType.values();

        for (int attempt = 0; attempt < count * 20 && enemies.size() < count; attempt++) {
            int x = random.nextInt(gridWidth);
            int y = random.nextInt(gridHeight);

            // Проверка валидности позиции
            if (!isValidEnemyPosition(x, y)) continue;

            boolean enemyExists = enemies.stream().anyMatch(e -> e.gridX == x && e.gridY == y);
            if (!enemyExists) {
                Enemy.Direction dir = directions[random.nextInt(directions.length)];
                Enemy enemy = new Enemy(x, y, dir);

                // Случайное распределение типов (можно изменить)
                int typeRoll = random.nextInt(100);
                if (typeRoll < 60) {
                    enemy.setBehaviorType(Enemy.BehaviorType.AGGRESSIVE);
                } else if (typeRoll < 85) {
                    enemy.setBehaviorType(Enemy.BehaviorType.TACTICAL);
                } else {
                    enemy.setBehaviorType(Enemy.BehaviorType.SNIPER);
                }

                enemies.add(enemy);
            }
        }
        System.out.println("Сгенерировано врагов: " + enemies.size());
    }

    // Добавьте метод загрузки:
    public void loadFriendlyUnits(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        friendlyUnits = LevelLoader.loadFriendlyUnits(levelPath, gridWidth, gridHeight);
        System.out.println("Загружено союзников: " + friendlyUnits.size());

        for (FriendlyUnit friendly : friendlyUnits) {
            friendly.setSoundManager(soundManager);
            // По умолчанию isRecruited = false, это правильно!
            System.out.println("Загружен союзник: " + friendly.name +
                    ", нанят: " + friendly.isRecruited);  // Должно быть false
        }
    }

    // Добавьте метод найма:
    // В GameWorld.java, метод recruitFriendly()
    public void recruitFriendly(FriendlyUnit unit) {
        System.out.println("!!! ВНИМАНИЕ: recruitFriendly() вызван для " + unit.name +
                " (isRecruited=" + unit.isRecruited + ")");

        if (unit.isUnavailable) {
            System.out.println(unit.name + " больше не может быть нанят!");
            return;
        }

        if (unit != null && !unit.isRecruited) {
            unit.isRecruited = true;

            if ("MS-1".equals(unit.type)) {
                unit.equipDefaultWeapon();
            }

            unit.calculateMovePoints();
            unit.movePoints = unit.maxMovePoints;

            addControllableUnit(unit);

            // ===== ДОБАВЬТЕ ЭТОТ БЛОК =====
            // Обновляем текстуры и портрет союзника
            if (gamePanel != null) {
                gamePanel.loadFriendlyTextures(unit);
                gamePanel.updateFriendlyPortrait(unit);
                gamePanel.syncRecruitedUnits();
                gamePanel.repaint();
            }
            // ================================

            System.out.println("🏆 " + unit.name + " присоединился к команде!");
            System.out.println("  ❤️ Здоровье: " + unit.health + "/" + unit.maxHealth);
            System.out.println("  ⭐ Очки хода: " + unit.movePoints + "/" + unit.maxMovePoints);
            System.out.println("  ⚖️ Вес инвентаря: " + unit.getInventory().getTotalWeight());
        }
    }

// Добавьте этот метод в GameWorld.java

    // В GameWorld.java, измените модификатор с private на public:
    // В GameWorld.java, метод dropLootFromEnemy:

    public void dropLootFromEnemy(Enemy enemy) {
        Random rand = new Random();

        System.out.println("\n=== ВЫПАДЕНИЕ ДРОПА С ВРАГА ===");
        System.out.println("Позиция: [" + enemy.gridX + "," + enemy.gridY + "]");
        System.out.println("Тип врага: " + enemy.type);

        LootDrop drop = new LootDrop(enemy.gridX, enemy.gridY);

        // ===== ПРОВЕРКА: ЯПОНСКИЙ ВРАГ R_OTSU =====
        if ("R_Otsu".equals(enemy.type)) {
            // === 13mm снаряды (100% выпадение) ===
            int ammoDrop = 0;
            int roll = rand.nextInt(100);

            if (roll < 60) {
                ammoDrop = 15;
            } else if (roll < 80) {
                ammoDrop = 30;
            } else if (roll < 92) {
                ammoDrop = 45;
            } else {
                ammoDrop = 60;
            }

            if (ammoDrop > 0) {
                drop.addAmmo(Caliber.CALIBER_13MM, ammoDrop);
                System.out.println("📦 Выпало снарядов 13mm: " + ammoDrop);
            }

            // === 13 mm Autocannon Type Ho (10% вероятность) ===
            if (rand.nextDouble() < 0.10) {
                drop.addItem(Item.ItemType.WEAPON_13MM_JAPAN, 1);
                System.out.println("🔫 Выпало орудие 13 mm Autocannon Type Ho!");
            }

            // === Аптечка (20% вероятность) ===
            if (rand.nextDouble() < 0.20) {
                drop.addItem(Item.ItemType.MEDKIT, 1);
                System.out.println("💊 Выпала аптечка!");
            }

            // === Бинт (50% вероятность) ===
            if (rand.nextDouble() < 0.50) {
                drop.addItem(Item.ItemType.BANDAGE, 1);
                System.out.println("🩹 Выпал бинт!");
            }

            // === Детали (от 5 до 10) ===
            int partsAmount = 5 + rand.nextInt(6);
            player.addParts(partsAmount);
            System.out.println("⚙️ Выпало деталей: " + partsAmount);
        } else if ("M14_41".equals(enemy.type)) {
            // === 47mm снаряды (100% выпадение) ===
            int ammoDrop = 0;
            int roll = random.nextInt(100);

            if (roll < 60) {
                ammoDrop = 10;
            } else if (roll < 80) {
                ammoDrop = 20;
            } else if (roll < 92) {
                ammoDrop = 30;
            } else {
                ammoDrop = 40;
            }

            if (ammoDrop > 0) {
                drop.addAmmo(Caliber.CALIBER_47MM, ammoDrop);
                System.out.println("📦 Выпало снарядов 47mm: " + ammoDrop);
            }

            // === Cannone da 47-32 (10% вероятность) ===
            if (random.nextDouble() < 0.10) {
                drop.addItem(Item.ItemType.WEAPON_47MM_ITALIAN, 1);
                System.out.println("🔫 Выпало орудие Cannone da 47-32!");
            }

            // === Аптечка (20% вероятность) ===
            if (random.nextDouble() < 0.20) {
                drop.addItem(Item.ItemType.MEDKIT, 1);
                System.out.println("💊 Выпала аптечка!");
            }

            // === Бинт (50% вероятность) ===
            if (random.nextDouble() < 0.50) {
                drop.addItem(Item.ItemType.BANDAGE, 1);
                System.out.println("🩹 Выпал бинт!");
            }

            // === Детали (от 7 до 14) ===
            int partsAmount = 7 + random.nextInt(8);
            player.addParts(partsAmount);
            System.out.println("⚙️ Выпало деталей: " + partsAmount);
        }
        else if ("H35".equals(enemy.type)) {
            // === 47mm снаряды (100% выпадение) ===
            int ammoDrop = 0;
            int roll = random.nextInt(100);

            if (roll < 60) {
                ammoDrop = 15;
            } else if (roll < 80) {
                ammoDrop = 30;
            } else if (roll < 92) {
                ammoDrop = 45;
            } else {
                ammoDrop = 60;
            }

            if (ammoDrop > 0) {
                drop.addAmmo(Caliber.CALIBER_25MM, ammoDrop);
                System.out.println("📦 Выпало снарядов 25mm: " + ammoDrop);
            }

            // === Cannone da 47-32 (10% вероятность) ===
            if (random.nextDouble() < 0.10) {
                drop.addItem(Item.ItemType.WEAPON_25MM, 1);
                System.out.println("🔫 Выпало орудие 25 mm Canon Raccourci mle. 1934!");
            }

            // === Аптечка (20% вероятность) ===
            if (random.nextDouble() < 0.20) {
                drop.addItem(Item.ItemType.MEDKIT, 1);
                System.out.println("💊 Выпала аптечка!");
            }

            // === Бинт (50% вероятность) ===
            if (random.nextDouble() < 0.50) {
                drop.addItem(Item.ItemType.BANDAGE, 1);
                System.out.println("🩹 Выпал бинт!");
            }

            // === Детали (от 7 до 14) ===
            int partsAmount = 8 + random.nextInt(9);
            player.addParts(partsAmount);
            System.out.println("⚙️ Выпало деталей: " + partsAmount);
        } // В методе dropLootFromEnemy(), после блока для H35, добавьте:
        else if ("FT".equals(enemy.type)) {
            // === 13mm снаряды (100% выпадение) ===
            int ammoDrop = 0;
            int roll = rand.nextInt(100);

            if (roll < 60) {
                ammoDrop = 15;
            } else if (roll < 80) {
                ammoDrop = 30;
            } else if (roll < 92) {
                ammoDrop = 45;
            } else {
                ammoDrop = 60;
            }

            if (ammoDrop > 0) {
                drop.addAmmo(Caliber.CALIBER_13MM, ammoDrop);
                System.out.println("📦 Выпало снарядов 13mm: " + ammoDrop);
            }

            // === 13,2 mm Hotchkiss mle. 1930 (10% вероятность) ===
            if (rand.nextDouble() < 0.10) {
                drop.addItem(Item.ItemType.WEAPON_13MM_FRENCH, 1);
                System.out.println("🔫 Выпало орудие 13,2 mm Hotchkiss mle. 1930!");
            }

            // === Аптечка (20% вероятность) ===
            if (rand.nextDouble() < 0.20) {
                drop.addItem(Item.ItemType.MEDKIT, 1);
                System.out.println("💊 Выпала аптечка!");
            }

            // === Бинт (50% вероятность) ===
            if (rand.nextDouble() < 0.50) {
                drop.addItem(Item.ItemType.BANDAGE, 1);
                System.out.println("🩹 Выпал бинт!");
            }

            // === Детали (от 3 до 7) ===
            int partsAmount = 3 + rand.nextInt(5);
            player.addParts(partsAmount);
            System.out.println("⚙️ Выпало деталей: " + partsAmount);
        } else if ("Fiat3000".equals(enemy.type)) {
            // === 37mm снаряды (100% выпадение) ===
            int ammoDrop = 0;
            int roll = rand.nextInt(100);

            if (roll < 60) {
                ammoDrop = 10;
            } else if (roll < 80) {
                ammoDrop = 20;
            } else if (roll < 92) {
                ammoDrop = 30;
            } else {
                ammoDrop = 40;
            }

            if (ammoDrop > 0) {
                drop.addAmmo(Caliber.CALIBER_37MM, ammoDrop);
                System.out.println("📦 Выпало снарядов 37mm: " + ammoDrop);
            }

            // === Cannone da 37-40 (8% вероятность) ===
            if (rand.nextDouble() < 0.08) {
                drop.addItem(Item.ItemType.WEAPON_37MM_ITALIAN, 1);
                System.out.println("🔫 Выпало орудие Cannone da 37-40!");
            }

            // === Аптечка (20% вероятность) ===
            if (rand.nextDouble() < 0.20) {
                drop.addItem(Item.ItemType.MEDKIT, 1);
                System.out.println("💊 Выпала аптечка!");
            }

            // === Бинт (50% вероятность) ===
            if (rand.nextDouble() < 0.50) {
                drop.addItem(Item.ItemType.BANDAGE, 1);
                System.out.println("🩹 Выпал бинт!");
            }

            // === Детали (от 4 до 8) ===
            int partsAmount = 4 + rand.nextInt(5);
            player.addParts(partsAmount);
            System.out.println("⚙️ Выпало деталей: " + partsAmount);
        } else {
            // ===== ОБЫЧНЫЙ ДРОП ДЛЯ LEICHTTRAKTOR (немецкий) =====

            // === 20mm снаряды (100% выпадение) ===
            int ammoDrop = 0;
            int roll = rand.nextInt(100);

            if (roll < 60) {
                ammoDrop = 15;
            } else if (roll < 80) {
                ammoDrop = 30;
            } else if (roll < 92) {
                ammoDrop = 45;
            } else {
                ammoDrop = 60;
            }

            if (ammoDrop > 0) {
                drop.addAmmo(Caliber.CALIBER_20MM, ammoDrop);
                System.out.println("📦 Выпало снарядов 20mm: " + ammoDrop);
            }

            // === 2 cm Breda (I) (10% вероятность) ===
            if (rand.nextDouble() < 0.10) {
                drop.addItem(Item.ItemType.WEAPON, 1);
                System.out.println("🔫 Выпало орудие 2 cm Breda (I)!");
            }

            // === Аптечка (20% вероятность) ===
            if (rand.nextDouble() < 0.20) {
                drop.addItem(Item.ItemType.MEDKIT, 1);
                System.out.println("💊 Выпала аптечка!");
            }

            // === Бинт (50% вероятность) ===
            if (rand.nextDouble() < 0.50) {
                drop.addItem(Item.ItemType.BANDAGE, 1);
                System.out.println("🩹 Выпал бинт!");
            }

            // === Детали (от 5 до 10) ===
            int partsAmount = 5 + rand.nextInt(6);
            player.addParts(partsAmount);
            System.out.println("⚙️ Выпало деталей: " + partsAmount);
        }

        // Добавляем дроп на карту
        createLootDrop(enemy.gridX, enemy.gridY, drop);
        System.out.println("=== ДРОП СОЗДАН НА КАРТЕ ===\n");
    }

    // Проверка, являются ли два врага союзниками (не атакуют друг друга)
    public boolean areEnemiesAllied(Enemy enemy1, Enemy enemy2) {
        if (enemy1 == null || enemy2 == null) return false;
        if (enemy1 == enemy2) return true;
        return enemy1.getFaction() == enemy2.getFaction();
    }

    // Проверка, должен ли враг атаковать другого врага
    public boolean shouldEnemyAttack(Enemy attacker, Enemy target) {
        if (attacker == null || target == null) return false;
        if (attacker == target) return false;
        if (!attacker.isAlive || !target.isAlive) return false;

        // Нейтралы никого не атакуют
        if (attacker.getFaction() == Faction.NEUTRAL) return false;

        // Атакуем только врагов из других фракций
        return attacker.getFaction() != target.getFaction();
    }
    public void updateVisibilityMap() {

        if (fullMapRevealed) {
            // Если чит включён, делаем всю карту видимой
            int gridWidth = FIELD_SIZE / CELL_SIZE;
            int gridHeight = FIELD_SIZE / CELL_SIZE;
            for (int i = 0; i < gridWidth; i++) {
                Arrays.fill(visibilityMap[i], true);
            }
            return;
        }

        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        for (int i = 0; i < gridWidth; i++) {
            Arrays.fill(visibilityMap[i], false);
        }

        int[][] hillHeight = new int[gridWidth][gridHeight];
        for (Hill hill : hills) {
            hillHeight[hill.gridX][hill.gridY] = hill.height;
        }

        int observerHeight = player.isOnHill ? (1 + player.currentHillBonus) : 0;

        for (int dy = -VIEW_RADIUS; dy <= VIEW_RADIUS; dy++) {
            for (int dx = -VIEW_RADIUS; dx <= VIEW_RADIUS; dx++) {
                int targetX = player.gridX + dx;
                int targetY = player.gridY + dy;
                if (targetX < 0 || targetX >= gridWidth || targetY < 0 || targetY >= gridHeight) continue;
                if (dx*dx + dy*dy > VIEW_RADIUS * VIEW_RADIUS) continue;

                // ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД С ПРОВЕРКОЙ СТЕН
                if (isLineOfSightClearWithWalls(player.gridX, player.gridY, targetX, targetY, observerHeight, hillHeight)) {
                    visibilityMap[targetX][targetY] = true;
                }
            }
        }
        visibilityMap[player.gridX][player.gridY] = true;
    }

    // Существующий метод isLineOfSightClear (скопируйте из предыдущей версии)
    private boolean isLineOfSightClear(int x0, int y0, int x1, int y1, int observerHeight, int[][] hillHeight) {
        if (x0 == x1 && y0 == y1) return true;

        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int x = x0, y = y0;

        while (!(x == x1 && y == y1)) {
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
            if (x == x1 && y == y1) break;
            if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) return false;
            if (hillHeight[x][y] > observerHeight) return false;
        }
        return true;
    }

    // Добавьте этот метод - проверка видимости с учётом стен
    // Убедитесь, что метод isLineOfSightClearWithWalls есть и он корректный:
    private boolean isLineOfSightClearWithWalls(int x0, int y0, int x1, int y1, int observerHeight, int[][] hillHeight) {
        if (x0 == x1 && y0 == y1) return true;

        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int x = x0, y = y0;

        int steps = 0;
        int maxSteps = Math.max(dx, dy) + 5;

        while (!(x == x1 && y == y1) && steps < maxSteps) {
            steps++;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }

            // Пропускаем начальную и конечную клетки
            if ((x == x0 && y == y0) || (x == x1 && y == y1)) {
                continue;
            }

            if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) return false;

            // ===== СТЕНЫ БЛОКИРУЮТ ВИДИМОСТЬ =====
            if (isWallAt(x, y)) return false;

            // ===== ЗАКРЫТЫЕ ДВЕРИ БЛОКИРУЮТ ВИДИМОСТЬ! =====
            Door door = getDoorAt(x, y);
            if (door != null && !door.isOpen) {
                System.out.println("  🚪 Закрытая дверь блокирует обзор в [" + x + "," + y + "]");
                return false;
            }

            // Холмы блокируют видимость
            if (hillHeight[x][y] > observerHeight) return false;
        }

        // ===== ДОПОЛНИТЕЛЬНАЯ ПРОВЕРКА: цель за закрытой дверью =====
        Door targetDoor = getDoorAt(x1, y1);
        if (targetDoor != null && !targetDoor.isOpen) {
            System.out.println("  🚪 Цель за закрытой дверью в [" + x1 + "," + y1 + "]");
            return false;
        }

        return true;
    }

    // В GameWorld.java, добавьте этот метод:
    public boolean isEnemyVisibleByTeam(Enemy enemy) {
        if (fullMapRevealed) return enemy.isAlive;
        if (!enemy.isAlive) return false;

        // Проверяем игрока
        if (isEnemyVisibleFromPosition(player.gridX, player.gridY, enemy)) {
            return true;
        }

        // Проверяем всех нанятых союзников
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                if (isEnemyVisibleFromPosition(friendly.gridX, friendly.gridY, enemy)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Добавьте вспомогательный метод проверки стены
    public boolean isWallAt(int gridX, int gridY) {
        for (Wall wall : walls) {
            if (wall.isAlive() && wall.gridX == gridX && wall.gridY == gridY) {
                return true;
            }
        }
        return false;
    }

    public boolean isDoorAt(int gridX, int gridY) {
        for (Door door : doors) {
            if (door.isAlive && door.gridX == gridX && door.gridY == gridY) {
                return true;
            }
        }
        return false;
    }

    // Получение двери в клетке:
    public Door getDoorAt(int gridX, int gridY) {
        for (Door door : doors) {
            if (door.isAlive && door.gridX == gridX && door.gridY == gridY) {
                return door;
            }
        }
        return null;
    }

    public boolean canMoveThroughDoor(int gridX, int gridY) {
        Door door = getDoorAt(gridX, gridY);
        if (door != null && !door.isOpen) {
            return false; // Закрытая дверь непроходима
        }
        return true;
    }

    // Повреждение стены
    public void damageWall(int gridX, int gridY, int damage) {
        for (Wall wall : walls) {
            if (wall.isAlive() && wall.gridX == gridX && wall.gridY == gridY) {
                wall.takeDamage(damage);
                if (!wall.isAlive()) {
                    System.out.println("Стена разрушена! [" + gridX + "," + gridY + "]");
                } else {
                    System.out.println("Стена повреждена! Прочность: " + wall.health + "/" + wall.maxHealth);
                }
                break;
            }
        }
    }

    // Получить стену в клетке (если есть)
    public Wall getWallAt(int gridX, int gridY) {
        for (Wall wall : walls) {
            if (wall.isAlive() && wall.gridX == gridX && wall.gridY == gridY) {
                return wall;
            }
        }
        return null;
    }

    // Проверка, есть ли тумбочка в клетке
    public StorageChest getStorageChestAt(int gridX, int gridY) {
        for (StorageChest chest : storageChests) {
            if (chest.containsCell(gridX, gridY)) {
                return chest;
            }
        }
        return null;
    }

    public GarbageContainer getGarbageContainerAt(int gridX, int gridY) {
        for (GarbageContainer container : garbageContainers) {
            if (container.containsCell(gridX, gridY)) {
                System.out.println("Найден контейнер в клетке [" + gridX + "," + gridY +
                        "], позиция контейнера: [" + container.gridX + "," + container.gridY + "]");
                return container;
            }
        }
        return null;
    }


    // Добавить тумбочку
    public void addStorageChest(StorageChest chest) {
        storageChests.add(chest);
    }

    // Удалить тумбочку
    public void removeStorageChest(StorageChest chest) {
        storageChests.remove(chest);
    }

    // Добавьте метод добавления контейнера
    public void addGarbageContainer(GarbageContainer container) {
        garbageContainers.add(container);
    }

    // Добавьте метод удаления контейнера
    public void removeGarbageContainer(GarbageContainer container) {
        garbageContainers.remove(container);
    }

    // Проверка, можно ли разместить тумбочку
    public boolean canPlaceStorageChest(int x, int y) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        // Проверка границ
        if (x < 0 || x + 4 > gridWidth || y < 0 || y + 5 > gridHeight) {
            return false;
        }

        // Проверка всех клеток тумбочки
        for (int dx = 0; dx < 4; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                int checkX = x + dx;
                int checkY = y + dy;

                // Нельзя ставить на стены
                for (Wall wall : walls) {
                    if (wall.isAlive() && wall.gridX == checkX && wall.gridY == checkY) {
                        return false;
                    }
                }

                // Нельзя ставить на деревья
                for (Tree tree : trees) {
                    if (tree.isAlive && tree.gridX == checkX && tree.gridY == checkY) {
                        return false;
                    }
                }

                // Нельзя ставить на другие тумбочки
                for (StorageChest chest : storageChests) {
                    if (chest.containsCell(checkX, checkY)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Добавьте метод проверки возможности размещения
    public boolean canPlaceGarbageContainer(int x, int y) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        int width = 2;   // ← ширина 2 клетки
        int height = 1;  // ← высота 1 клетка

        if (x < 0 || x + width > gridWidth || y < 0 || y + height > gridHeight) {
            return false;
        }

        // Проверка на пересечение со стенами
        for (Wall wall : walls) {
            for (int dx = 0; dx < width; dx++) {
                for (int dy = 0; dy < height; dy++) {
                    if (wall.isAlive() && wall.gridX == x + dx && wall.gridY == y + dy) {
                        return false;
                    }
                }
            }
        }

        // Проверка на пересечение с другими контейнерами
        for (GarbageContainer container : garbageContainers) {
            for (int dx = 0; dx < width; dx++) {
                for (int dy = 0; dy < height; dy++) {
                    if (container.containsCell(x + dx, y + dy)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean isEnemyVisible(Enemy enemy) {
        if (fullMapRevealed) return enemy.isAlive;  // ← Добавить эту строку
        if (!enemy.isAlive) return false;

        // Проверяем видимость от игрока
        if (isEnemyVisibleFromPosition(player.gridX, player.gridY, enemy)) {
            return true;
        }

        // Проверяем видимость от всех нанятых союзников
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                if (isEnemyVisibleFromPosition(friendly.gridX, friendly.gridY, enemy)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Включение полной видимости карты
    public void revealFullMap() {
        fullMapRevealed = true;

        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        // Заполняем все карты видимости true
        for (int i = 0; i < gridWidth; i++) {
            Arrays.fill(visibilityMap[i], true);
            Arrays.fill(combinedVisibilityMap[i], true);
        }

        System.out.println("👁️ FULL MAP REVEAL активирован - вся карта видна!");
    }

    // Отключение полной видимости
    public void disableFullMapReveal() {
        fullMapRevealed = false;
        System.out.println("👁️ FULL MAP REVEAL отключён");
    }

    // Проверка, включён ли чит
    public boolean isFullMapRevealed() {
        return fullMapRevealed;
    }

    public void checkHillOccupation() {
        player.isOnHill = false;
        player.currentHillBonus = 0;
        for (Hill hill : hills) {
            if (hill.gridX == player.gridX && hill.gridY == player.gridY) {
                player.isOnHill = true;
                player.currentHillBonus = hill.height;
                hill.isOccupied = true;
                System.out.println("Вы заняли холм высотой " + hill.height + "!");
            } else {
                hill.isOccupied = false;
            }
        }
        updateVisibilityMap();
    }

    public boolean canMoveTo(int gridX, int gridY) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        if (gridX < 0 || gridX >= gridWidth || gridY < 0 || gridY >= gridHeight) return false;

        // ВОДА НЕПРОХОДИМА!
        if (isWaterAt(gridX, gridY)) return false;

        // ДВЕРИ
        if (!canMoveThroughDoor(gridX, gridY)) return false;

        for (Enemy e : enemies) {
            if (e.isAlive && e.gridX == gridX && e.gridY == gridY) return false;
        }

        for (Wall wall : walls) {
            if (wall.isAlive() && wall.gridX == gridX && wall.gridY == gridY) return false;
        }

        for (Tree tree : trees) {
            if (tree.isAlive && tree.gridX == gridX && tree.gridY == gridY) return false;
        }

        return true;
    }

    // Getters
    public PlayerTank getPlayer() { return player; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Hill> getHills() { return hills; }
    public boolean[][] getVisibilityMap() { return visibilityMap; }
    public List<QuestNPC> getQuestNPCs() { return questNPCs; }
    public int getTotalEnemiesKilled() { return totalEnemiesKilled; }
    public List<Tree> getTrees() { return trees; }
    public List<Pavement> getPavements() { return pavements; }
    public List<OakPlanks> getOakPlanks() {
        return oakPlanks;
    }

    public List<IronFloor> getIronFloors() { return ironFloors; }
    public List<InfernalLand> getInfernalLands() { return infernalLands; }
    public List<FriendlyUnit> getFriendlyUnits() { return friendlyUnits; }
    public List<StorageChest> getStorageChests() { return storageChests; }

    public List<GarbageContainer> getGarbageContainers() { return garbageContainers; }

    // Добавьте после методов getFriendlyUnits() и т.д.
    public List<FriendlyUnit> getControllableUnits() {
        return controllableUnits;
    }

    public void addControllableUnit(FriendlyUnit unit) {
        if (unit != null && !controllableUnits.contains(unit)) {
            controllableUnits.add(unit);
            // ❌ УДАЛИТЕ ИЛИ ЗАКОММЕНТИРУЙТЕ ЭТУ СТРОКУ:
            // unit.startTurn();

            System.out.println("✅ " + unit.name + " добавлен в список управляемых юнитов");
            System.out.println("   Очки хода: " + unit.movePoints + "/" + unit.maxMovePoints);
        }
    }

    public void checkHillOccupationForFriendly(FriendlyUnit unit) {
        for (Hill hill : hills) {
            if (hill.gridX == unit.gridX && hill.gridY == unit.gridY) {
                // Союзник на холме - можно добавить бонусы
                System.out.println(unit.name + " занял холм высотой " + hill.height + "!");
                break;
            }
        }
    }

    // В GameWorld.java добавьте этот метод
    public void updateVisibilityForFriendly(FriendlyUnit friendly) {
        invalidateFriendlyVisibilityCache(friendly);
        updateFriendlyVisibilityMap(friendly);
    }

    // В методе processNextEnemy(), после завершения хода врагов, добавьте:


// Вызывайте updateBreadDebuffs() в конце processNextEnemy(), после восстановления очков хода союзников

    // Проверка, виден ли враг с определённой позиции
    // Этот метод уже есть в вашем GameWorld, убедитесь:
    public boolean isEnemyVisibleFromPosition(int observerX, int observerY, Enemy enemy) {
        if (fullMapRevealed) return enemy.isAlive;
        if (!enemy.isAlive) return false;

        int dx = Math.abs(enemy.gridX - observerX);
        int dy = Math.abs(enemy.gridY - observerY);
        int distSq = dx*dx + dy*dy;

        if (distSq > VIEW_RADIUS * VIEW_RADIUS) {
            return false;
        }

        // ===== ПРОВЕРКА: ЕСЛИ ВРАГ ЗА ЗАКРЫТОЙ ДВЕРЬЮ — НЕ ВИДЕН! =====
        Door enemyDoor = getDoorAt(enemy.gridX, enemy.gridY);
        if (enemyDoor != null && !enemyDoor.isOpen) {
            System.out.println("  🚪 Враг за закрытой дверью в [" + enemy.gridX + "," + enemy.gridY + "] — НЕ ВИДЕН!");
            return false;
        }

        int[][] hillHeight = new int[FIELD_SIZE / CELL_SIZE][FIELD_SIZE / CELL_SIZE];
        for (Hill hill : hills) {
            hillHeight[hill.gridX][hill.gridY] = hill.height;
        }

        int observerHeight = 0;
        for (Hill hill : hills) {
            if (hill.gridX == observerX && hill.gridY == observerY) {
                observerHeight = hill.height;
                break;
            }
        }

        // ===== ЭТОТ МЕТОД ТЕПЕРЬ УЧИТЫВАЕТ ДВЕРИ =====
        return isLineOfSightClearWithWalls(observerX, observerY, enemy.gridX, enemy.gridY, observerHeight, hillHeight);
    }

    public boolean isM53InTeam() {
        for (FriendlyUnit unit : friendlyUnits) {
            if (unit.isAlive && unit.isRecruited && "M53".equals(unit.type)) {
                return true;
            }
        }
        return false;
    }

    public boolean isM53Active() {
        // Этот метод будет вызываться из GamePanel
        // Нужно передавать текущего активного юнита
        return false; // Временно
    }

    // Добавьте метод для получения дропа в клетке:
    public LootDrop getLootDropAt(int gridX, int gridY) {
        for (LootDrop drop : lootDrops) {
            if (drop.isAlive && drop.gridX == gridX && drop.gridY == gridY) {
                return drop;
            }
        }
        return null;
    }

    // Добавьте метод для создания дропа:
    public void createLootDrop(int gridX, int gridY, LootDrop drop) {
        // Удаляем старый дроп в этой клетке, если есть
        lootDrops.removeIf(d -> d.gridX == gridX && d.gridY == gridY);
        lootDrops.add(drop);
    }

    public List<LootDrop> getLootDrops() { return lootDrops; }
    public int getTeamSilver() {
        return player.getSilver();
    }

    // Создайте метод для распределения опыта:
    public void distributeExperience(Enemy enemy, PlayerTank player, FriendlyUnit killer) {
        // Базовая сумма опыта за врага
        int baseExp = 50;

        // Кто убил?
        if (killer == null) {
            // Убил игрок
            player.addExperience(baseExp);
            System.out.println("✨ Leichttraktor получил " + baseExp + " опыта!");
        } else {
            // Убил союзник
            killer.addExperience(baseExp);
            System.out.println("✨ " + killer.name + " получил " + baseExp + " опыта!");
        }
    }

    // В GameWorld.java добавьте:
    public List<Object> getAllTeamMembers() {
        List<Object> team = new ArrayList<>();
        if (player.health > 0) {
            team.add(player);
        }
        for (FriendlyUnit unit : friendlyUnits) {
            if (unit.isAlive && unit.isRecruited) {
                team.add(unit);
            }
        }
        return team;
    }

    public void loadTraders(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        traders = LevelLoader.loadTraders(levelPath, gridWidth, gridHeight);
        System.out.println("Загружено торговцев: " + traders.size());
    }

    public List<Trader> getTraders() { return traders; }

    // Агрессивное поведение
    // Агрессивное поведение - возвращает true если движение начато
    private boolean handleAggressiveBehavior(Enemy enemy) {
        System.out.println("  [АГРЕССИВНЫЙ] Атакую без раздумий!");

        if (isPlayerVisibleToEnemy(enemy)) {
            return moveTowardsPlayerWithPathfinding(enemy);
        } else if (isAnyFriendlyVisibleToEnemy(enemy)) {
            FriendlyUnit target = getClosestVisibleFriendly(enemy);
            if (target != null) {
                return moveTowardsPositionWithPathfinding(enemy, target.gridX, target.gridY);
            }
        } else if (enemy.memoryTurns > 0) {
            return moveTowardsPositionWithPathfinding(enemy, enemy.lastSeenPlayerX, enemy.lastSeenPlayerY);
        }

        return randomMoveOneStep(enemy);
    }

    // Тактическое поведение
    // Тактическое поведение (обновлённое)
    // Тактическое поведение (обновлённое)
    private void handleTacticalBehavior(Enemy enemy) {
        evaluateSituationForEnemy(enemy);

        int enemiesCount = enemy.getEnemiesCount();
        int alliesCount = enemy.getAlliesCount();

        System.out.println("  [ТАКТИЧЕСКИЙ] enemies=" + enemiesCount + ", allies=" + alliesCount + ", retreatTurns=" + enemy.getRetreatTurns());

        // ===== ИСПРАВЛЕНИЕ: Отступаем только если врагов БОЛЬШЕ на 2+ =====
        if (enemiesCount > alliesCount + 1 && !enemy.isRetreating()) {
            System.out.println("  [ТАКТИЧЕСКИЙ] Врагов значительно больше! Начинаю отступление!");
            enemy.setRetreating(true);
            enemy.setRetreatTurns(2);
            enemy.setHasShotThisTurn(false);
        } else if (enemiesCount <= alliesCount) {
            // Если врагов не больше - отменяем отступление
            if (enemy.isRetreating()) {
                System.out.println("  [ТАКТИЧЕСКИЙ] Угроза миновала, отступаю!");
                enemy.setRetreating(false);
                enemy.setRetreatTurns(0);
            }
        }

        if (enemy.isRetreating()) {
            enemy.setHasShotThisTurn(false);
        }
    }

    private boolean simpleRetreatMove(Enemy enemy) {
        if (enemy.isMoving()) return false;

        // Пытаемся двигаться в текущем направлении
        int newX = enemy.gridX;
        int newY = enemy.gridY;
        Enemy.Direction dir = enemy.direction;

        switch(dir) {
            case UP: newY--; break;
            case DOWN: newY++; break;
            case LEFT: newX--; break;
            case RIGHT: newX++; break;
        }

        // Если можем двигаться вперёд - двигаемся
        if (canMoveToForEnemy(enemy, newX, newY)) {
            boolean willBeVisible = isEnemyVisible(enemy);
            enemy.consumeMovePoints();
            enemy.startAnimatedMove(newX, newY, dir, CELL_SIZE, TANK_SIZE, willBeVisible);
            if (willBeVisible && soundManager != null) {
                soundManager.playEnemyMoveSound();
            }
            return true;
        }

        // Если не можем - пробуем другие направления
        List<Enemy.Direction> directions = Arrays.asList(Enemy.Direction.values());
        for (Enemy.Direction d : directions) {
            int checkX = enemy.gridX;
            int checkY = enemy.gridY;
            switch(d) {
                case UP: checkY--; break;
                case DOWN: checkY++; break;
                case LEFT: checkX--; break;
                case RIGHT: checkX++; break;
            }
            if (canMoveToForEnemy(enemy, checkX, checkY)) {
                boolean willBeVisible = isEnemyVisible(enemy);
                enemy.consumeMovePoints();
                enemy.startAnimatedMove(checkX, checkY, d, CELL_SIZE, TANK_SIZE, willBeVisible);
                if (willBeVisible && soundManager != null) {
                    soundManager.playEnemyMoveSound();
                }
                return true;
            }
        }

        return false;
    }

    // Получение позиций всех противников (игрок + союзники)
    private List<Point> getEnemyPositions() {
        List<Point> positions = new ArrayList<>();
        if (player.health > 0) {
            positions.add(new Point(player.gridX, player.gridY));
        }
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                positions.add(new Point(friendly.gridX, friendly.gridY));
            }
        }
        return positions;
    }

    // Проверка, может ли враг атаковать цель
    private boolean canEnemyAttackTarget(Enemy enemy, int targetX, int targetY) {
        int dx = Math.abs(enemy.gridX - targetX);
        int dy = Math.abs(enemy.gridY - targetY);
        int distance = dx + dy;
        int attackRange = (enemy.getBehaviorType() == Enemy.BehaviorType.SNIPER) ? 12 : 8;
        return distance <= attackRange && enemy.movePoints >= enemy.shotCost;
    }

    // Тактическое отступление к ближайшему союзнику
    // Выполнение тактического отступления - возвращает true если удалось отступить
    private boolean performTacticalRetreat(Enemy enemy) {
        return performTacticalRetreatWithMemory(enemy);
    }

    // В GameWorld.java, добавьте метод:

    private boolean hasReachedTarget(Enemy enemy, Point target) {
        if (target == null) return false;
        // Если враг в радиусе 2 клеток от цели - считаем, что достиг
        return Math.abs(enemy.gridX - target.x) <= 2 && Math.abs(enemy.gridY - target.y) <= 2;
    }

    // Поиск лучшего места для отступления (укрытие, подальше от врагов)
    private Point findBestRetreatSpot(Enemy enemy) {
        int radius = 8;
        Point bestSpot = null;
        double bestScore = -Double.MAX_VALUE;

        // Получаем список врагов (игрок + союзники)
        List<Point> threats = new ArrayList<>();
        if (player.health > 0) threats.add(new Point(player.gridX, player.gridY));
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                threats.add(new Point(friendly.gridX, friendly.gridY));
            }
        }

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int checkX = enemy.gridX + dx;
                int checkY = enemy.gridY + dy;

                if (!canMoveToForEnemy(enemy, checkX, checkY)) continue;

                double score = 0;

                // Чем дальше от врагов - тем лучше
                double minThreatDist = Double.MAX_VALUE;
                for (Point threat : threats) {
                    double dist = Math.hypot(checkX - threat.x, checkY - threat.y);
                    minThreatDist = Math.min(minThreatDist, dist);
                }
                score += minThreatDist * 3;

                // Наличие укрытия даёт бонус
                if (hasCoverNearby(checkX, checkY)) {
                    score += 10;
                }

                // Чем ближе к союзникам - тем лучше
                double minAllyDist = Double.MAX_VALUE;
                for (Enemy other : enemies) {
                    if (other != enemy && other.isAlive) {
                        double dist = Math.hypot(checkX - other.gridX, checkY - other.gridY);
                        minAllyDist = Math.min(minAllyDist, dist);
                    }
                }
                if (minAllyDist < Double.MAX_VALUE) {
                    score += (15 - minAllyDist);
                }

                // Небольшой штраф за расстояние
                score -= Math.hypot(dx, dy);

                if (score > bestScore) {
                    bestScore = score;
                    bestSpot = new Point(checkX, checkY);
                }
            }
        }

        return bestSpot;
    }

    // Проверка наличия укрытия рядом с клеткой
    private boolean hasCoverNearby(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int checkX = x + dx;
                int checkY = y + dy;
                if (isWallAt(checkX, checkY) || isTreeAt(checkX, checkY)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Убегаем от ближайшего врага
    private boolean fleeFromNearestEnemy(Enemy enemy) {
        // Находим ближайшего врага
        int nearestX = -1, nearestY = -1;
        double minDist = Double.MAX_VALUE;

        if (player.health > 0) {
            double dist = Math.hypot(enemy.gridX - player.gridX, enemy.gridY - player.gridY);
            if (dist < minDist) {
                minDist = dist;
                nearestX = player.gridX;
                nearestY = player.gridY;
            }
        }

        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                double dist = Math.hypot(enemy.gridX - friendly.gridX, enemy.gridY - friendly.gridY);
                if (dist < minDist) {
                    minDist = dist;
                    nearestX = friendly.gridX;
                    nearestY = friendly.gridY;
                }
            }
        }

        if (nearestX != -1) {
            // Вычисляем направление ОТ врага
            int dirX = enemy.gridX - nearestX;
            int dirY = enemy.gridY - nearestY;

            // Нормализуем направление (двигаемся в противоположную сторону)
            if (Math.abs(dirX) > Math.abs(dirY)) {
                if (dirX > 0) {
                    return tryMoveInDirection(enemy, enemy.gridX + 1, enemy.gridY, Enemy.Direction.RIGHT);
                } else if (dirX < 0) {
                    return tryMoveInDirection(enemy, enemy.gridX - 1, enemy.gridY, Enemy.Direction.LEFT);
                }
            } else {
                if (dirY > 0) {
                    return tryMoveInDirection(enemy, enemy.gridX, enemy.gridY + 1, Enemy.Direction.DOWN);
                } else if (dirY < 0) {
                    return tryMoveInDirection(enemy, enemy.gridX, enemy.gridY - 1, Enemy.Direction.UP);
                }
            }
        }

        return false;
    }

    private boolean tryMoveInDirection(Enemy enemy, int newX, int newY, Enemy.Direction dir) {
        if (canMoveToForEnemy(enemy, newX, newY)) {
            System.out.println("  [ОТСТУПЛЕНИЕ] Убегаю: " + dir + " -> [" + newX + "," + newY + "]");
            boolean willBeVisible = isEnemyVisible(enemy);
            enemy.consumeMovePoints();
            enemy.startAnimatedMove(newX, newY, dir, CELL_SIZE, TANK_SIZE, willBeVisible);
            if (willBeVisible && soundManager != null) {
                soundManager.playEnemyMoveSound();
            }
            return true;
        }
        return false;
    }

    // Проверка, не отрезан ли путь к союзнику врагами (игроком/союзниками)
    private boolean isPathToAllyBlocked(Enemy enemy, Enemy ally) {
        // Простая проверка: есть ли враг между текущей позицией и союзником
        int minX = Math.min(enemy.gridX, ally.gridX);
        int maxX = Math.max(enemy.gridX, ally.gridX);
        int minY = Math.min(enemy.gridY, ally.gridY);
        int maxY = Math.max(enemy.gridY, ally.gridY);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                // Проверяем, стоит ли на пути игрок
                if (player.gridX == x && player.gridY == y && player.health > 0) {
                    return true;
                }
                // Проверяем союзников игрока
                for (FriendlyUnit friendly : friendlyUnits) {
                    if (friendly.isAlive && friendly.isRecruited &&
                            friendly.gridX == x && friendly.gridY == y) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Поиск укрытия (стены, деревья, углы)
    private Point findCoverSpot(Enemy enemy) {
        int radius = 5;
        Point bestSpot = null;
        double bestSafety = -1;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int checkX = enemy.gridX + dx;
                int checkY = enemy.gridY + dy;

                if (!canMoveToForEnemy(enemy, checkX, checkY)) continue;

                // Оценка безопасности укрытия
                double safety = evaluateCoverSafety(checkX, checkY);

                // Учитываем расстояние
                double distance = Math.hypot(dx, dy);
                safety = safety - distance * 0.5;

                if (safety > bestSafety) {
                    bestSafety = safety;
                    bestSpot = new Point(checkX, checkY);
                }
            }
        }

        return bestSpot;
    }

    public void checkSavM43Quest() {
        for (QuestNPC npc : questNPCs) {
            if ("Sav m/43".equals(npc.name) && !npc.isQuestFinished) {
                boolean completed = npc.isQuestCompletedForFactions(enemies);
                if (completed && !npc.isQuestCompleted) {
                    npc.isQuestCompleted = true;
                    System.out.println("✅ Квест Sav m/43 выполнен! Все французские и итальянские враги уничтожены!");
                }
            }
        }
    }

    public void revalidateAllQuestNPCs() {
        for (QuestNPC npc : questNPCs) {
            if ("Sav m/43".equals(npc.name)) {
                npc.revalidateQuestStatus(enemies);
            }
        }
    }

    // Оценка безопасности клетки (наличие укрытий рядом)
    private double evaluateCoverSafety(int x, int y) {
        double safety = 0;

        // Проверяем соседние клетки на наличие стен или деревьев
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int checkX = x + dx;
                int checkY = y + dy;

                if (isWallAt(checkX, checkY)) {
                    safety += 2.0;  // Стена - отличное укрытие
                } else if (isTreeAt(checkX, checkY)) {
                    safety += 1.5;  // Дерево - хорошее укрытие
                }
            }
        }

        return safety;
    }

    // Осторожное продвижение к цели (не в лоб, а с использованием укрытий)
    private void cautiousAdvance(Enemy enemy) {
        Point coverSpot = findCoverSpotNearTarget(enemy, player.gridX, player.gridY);
        if (coverSpot != null && !(coverSpot.x == enemy.gridX && coverSpot.y == enemy.gridY)) {
            System.out.println("  [ОСТОРОЖНО] Продвигаюсь к укрытию рядом с целью");
            moveTowardsPositionWithPathfinding(enemy, coverSpot.x, coverSpot.y);
        } else {
            moveTowardsPlayerWithPathfinding(enemy);
        }
    }

    private void cautiousAdvanceToTarget(Enemy enemy, int targetX, int targetY) {
        Point coverSpot = findCoverSpotNearTarget(enemy, targetX, targetY);
        if (coverSpot != null && !(coverSpot.x == enemy.gridX && coverSpot.y == enemy.gridY)) {
            System.out.println("  [ОСТОРОЖНО] Продвигаюсь к укрытию");
            moveTowardsPositionWithPathfinding(enemy, coverSpot.x, coverSpot.y);
        } else {
            moveTowardsPositionWithPathfinding(enemy, targetX, targetY);
        }
    }

    // Поиск укрытия рядом с целью
    private Point findCoverSpotNearTarget(Enemy enemy, int targetX, int targetY) {
        int radius = 4;
        Point bestSpot = null;
        double bestScore = -1;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int checkX = targetX + dx;
                int checkY = targetY + dy;

                if (!canMoveToForEnemy(enemy, checkX, checkY)) continue;

                // Проверяем, есть ли укрытие на этой клетке
                boolean hasCover = isWallAt(checkX, checkY) || isTreeAt(checkX, checkY);

                // Расстояние от врага
                double distFromEnemy = Math.hypot(checkX - enemy.gridX, checkY - enemy.gridY);

                // Расстояние до цели
                double distToTarget = Math.hypot(checkX - targetX, checkY - targetY);

                double score = (hasCover ? 10 : 0) - distFromEnemy * 0.5 + (10 - distToTarget);

                if (score > bestScore) {
                    bestScore = score;
                    bestSpot = new Point(checkX, checkY);
                }
            }
        }

        return bestSpot;
    }

    // Отчаянное сопротивление (когда некуда бежать)
    private void desperateFight(Enemy enemy) {
        System.out.println("  [ОТЧАЯНИЕ] НЕКУДА БЕЖАТЬ! ОТСТРЕЛИВАЮСЬ ДО ПОСЛЕДНЕГО!");

        // Проверяем, можно ли выстрелить
        boolean canAttackPlayer = canEnemyAttackTarget(enemy, player.gridX, player.gridY);
        boolean canAttackFriendly = false;
        FriendlyUnit targetFriendly = getClosestVisibleFriendly(enemy);
        if (targetFriendly != null) {
            canAttackFriendly = canEnemyAttackTarget(enemy, targetFriendly.gridX, targetFriendly.gridY);
        }

        if (canAttackPlayer) {
            System.out.println("  [ОТЧАЯНИЕ] ПОСЛЕДНИЙ ВЫСТРЕЛ ПО ИГРОКУ!");
            attackPlayer(enemy, gamePanel);
            enemy.movePoints -= enemy.shotCost;
        } else if (canAttackFriendly && targetFriendly != null) {
            System.out.println("  [ОТЧАЯНИЕ] ПОСЛЕДНИЙ ВЫСТРЕЛ ПО СОЮЗНИКУ " + targetFriendly.name + "!");
            attackFriendly(enemy, targetFriendly, gamePanel);
            enemy.movePoints -= enemy.shotCost;
        } else if (enemy.movePoints >= enemy.moveCost) {
            // Если не может стрелять - хотя бы двигается в случайном направлении
            randomMoveOneStep(enemy);
        }
    }

    // Получение позиций врагов (игрок + союзники)
    private void setEnemyPositionsForRetreat(Enemy enemy) {
        // Запоминаем позиции всех противников для планирования отступления
        java.util.List<Point> enemyPositions = new ArrayList<>();
        if (player.health > 0) {
            enemyPositions.add(new Point(player.gridX, player.gridY));
        }
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                enemyPositions.add(new Point(friendly.gridX, friendly.gridY));
            }
        }
        // Сохраняем в поле enemy (нужно добавить поле в Enemy.java)
        enemy.setLastSeenEnemyPositions(enemyPositions);
    }

    // Снайперское поведение
    private boolean handleSniperBehavior(Enemy enemy) {
        System.out.println("  [СНАЙПЕР] Режим: " + (enemy.isSnipingMode() ? "СТРЕЛЬБА" : "ПОИСК ПРИКРЫТИЯ"));

        boolean seesPlayer = isPlayerVisibleToEnemy(enemy);
        boolean seesFriendly = isAnyFriendlyVisibleToEnemy(enemy);

        // В режиме снайпера и видит цель - не двигаемся
        if (enemy.isSnipingMode() && (seesPlayer || seesFriendly)) {
            System.out.println("  [СНАЙПЕР] На позиции, жду команды на выстрел!");
            return false;  // не двигаемся
        }

        if (enemy.isSnipingMode() && !seesPlayer && !seesFriendly) {
            System.out.println("  [СНАЙПЕР] Никого не вижу, ищу прикрытие!");
            enemy.setSnipingMode(false);
        }

        if (!enemy.isSnipingMode()) {
            // Ищем ближайшего союзника врага
            Enemy nearestAlly = null;
            double minDist = Double.MAX_VALUE;

            for (Enemy other : enemies) {
                if (other != enemy && other.isAlive) {
                    double dist = Math.hypot(enemy.gridX - other.gridX, enemy.gridY - other.gridY);
                    if (dist < minDist) {
                        minDist = dist;
                        nearestAlly = other;
                    }
                }
            }

            if (nearestAlly != null && minDist < 15) {
                System.out.println("  [СНАЙПЕР] Двигаюсь к союзнику для прикрытия!");
                return moveTowardsPositionWithPathfinding(enemy, nearestAlly.gridX, nearestAlly.gridY);
            } else if (seesPlayer || seesFriendly) {
                System.out.println("  [СНАЙПЕР] Возвращаюсь в режим стрельбы!");
                enemy.setSnipingMode(true);
                return false;
            }
        }

        return randomMoveOneStep(enemy);
    }

    // Оценка ситуации для врага
    // Оценка ситуации для врага
    private void evaluateSituationForEnemy(Enemy enemy) {
        int allies = 1;  // Сам враг
        int enemiesCount = 0;
        int detectionRadius = 12;

        // Получаем высоты холмов для проверки Line of Sight
        int[][] hillHeight = new int[FIELD_SIZE / CELL_SIZE][FIELD_SIZE / CELL_SIZE];
        for (Hill hill : hills) {
            hillHeight[hill.gridX][hill.gridY] = hill.height;
        }

        int enemyHeight = 0;
        for (Hill hill : hills) {
            if (hill.gridX == enemy.gridX && hill.gridY == enemy.gridY) {
                enemyHeight = hill.height;
                break;
            }
        }

        // 1. Считаем союзников (танки из ТОЙ ЖЕ ФРАКЦИИ)
        for (Enemy other : enemies) {
            if (other != enemy && other.isAlive) {
                int dx = Math.abs(enemy.gridX - other.gridX);
                int dy = Math.abs(enemy.gridY - other.gridY);
                if (dx*dx + dy*dy <= detectionRadius * detectionRadius) {
                    // Проверяем фракцию
                    if (enemy.getFaction() == other.getFaction()) {
                        allies++;
                    }
                }
            }
        }

        // 2. Считаем ПРОТИВНИКОВ (игрок + союзники игрока + враги других фракций)
        // Проверяем игрока
        if (player.health > 0) {
            int dx = Math.abs(enemy.gridX - player.gridX);
            int dy = Math.abs(enemy.gridY - player.gridY);
            if (dx*dx + dy*dy <= detectionRadius * detectionRadius) {
                if (isLineOfSightClearWithWalls(enemy.gridX, enemy.gridY, player.gridX, player.gridY, enemyHeight, hillHeight)) {
                    enemiesCount++;
                }
            }
        }

        // Проверяем врагов из ДРУГИХ фракций
        for (Enemy other : enemies) {
            if (other != enemy && other.isAlive && enemy.getFaction() != other.getFaction()) {
                int dx = Math.abs(enemy.gridX - other.gridX);
                int dy = Math.abs(enemy.gridY - other.gridY);
                if (dx*dx + dy*dy <= detectionRadius * detectionRadius) {
                    if (isLineOfSightClearWithWalls(enemy.gridX, enemy.gridY, other.gridX, other.gridY, enemyHeight, hillHeight)) {
                        enemiesCount++;
                    }
                }
            }
        }

        // 3. Считаем союзников игрока (они всегда враги для всех, кроме нейтралов)
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                int dx = Math.abs(enemy.gridX - friendly.gridX);
                int dy = Math.abs(enemy.gridY - friendly.gridY);
                if (dx*dx + dy*dy <= detectionRadius * detectionRadius) {
                    // Проверяем Line of Sight к союзнику
                    int friendlyHeight = 0;
                    for (Hill hill : hills) {
                        if (hill.gridX == friendly.gridX && hill.gridY == friendly.gridY) {
                            friendlyHeight = hill.height;
                            break;
                        }
                    }
                    // Используем максимальную высоту для проверки
                    int checkHeight = Math.max(enemyHeight, friendlyHeight);
                    if (isLineOfSightClearWithWalls(enemy.gridX, enemy.gridY, friendly.gridX, friendly.gridY, checkHeight, hillHeight)) {
                        // Нейтралы не считают союзников игрока врагами
                        if (enemy.getFaction() != Faction.NEUTRAL) {
                            enemiesCount++;
                        }
                    }
                }
            }
        }

        enemy.setAlliesCount(allies);
        enemy.setEnemiesCount(enemiesCount);

        System.out.println("  [ОЦЕНКА] Фракция: " + enemy.getFaction().displayName +
                ", союзников: " + allies + ", врагов: " + enemiesCount);
    }

    // Поиск цели для атаки (приоритет: враги из других фракций > игрок > союзники игрока)
    // Поиск цели для атаки (приоритет: враги из других фракций > игрок > союзники игрока)
    private Enemy findBestEnemyTarget(Enemy attacker) {
        Enemy bestTarget = null;
        int bestDistance = Integer.MAX_VALUE;

        int attackRange = (attacker.getBehaviorType() == Enemy.BehaviorType.SNIPER) ? 12 : 8;

        for (Enemy target : enemies) {
            if (target != attacker && target.isAlive) {
                // Атакуем только врагов из ДРУГИХ фракций
                if (shouldEnemyAttack(attacker, target)) {
                    int distance = Math.abs(attacker.gridX - target.gridX) +
                            Math.abs(attacker.gridY - target.gridY);

                    // ===== ДОБАВЛЯЕМ ПРОВЕРКУ ВИДИМОСТИ =====
                    if (distance <= attackRange && isEnemyVisibleToEnemy(attacker, target)) {
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            bestTarget = target;
                        }
                    }
                }
            }
        }
        return bestTarget;
    }

    // Проверка, есть ли стена на линии огня
    private boolean hasWallOnLineOfFire(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int x = x0;
        int y = y0;

        while (true) {
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }

            if (x == x0 && y == y0) continue;
            if (x == x1 && y == y1) break;

            if (isWallAt(x, y)) {
                return true;
            }
            if (isTreeAt(x, y)) {
                return true;
            }
        }
        return false;
    }

    // Проверка, находится ли союзник в опасности (в бою с игроком или его союзниками)
    private boolean isAllyInDanger(Enemy ally) {
        // Проверяем, есть ли игрок рядом
        int dxToPlayer = Math.abs(ally.gridX - player.gridX);
        int dyToPlayer = Math.abs(ally.gridY - player.gridY);
        if (dxToPlayer + dyToPlayer <= 8) {
            return true; // Игрок рядом
        }

        // Проверяем, есть ли союзники игрока рядом
        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                int dx = Math.abs(ally.gridX - friendly.gridX);
                int dy = Math.abs(ally.gridY - friendly.gridY);
                if (dx + dy <= 8) {
                    return true; // Союзник игрока рядом
                }
            }
        }

        return false;
    }

    // Отступление от врагов
    private boolean retreatFromEnemies(Enemy enemy) {
        int nearestX = -1, nearestY = -1;
        double minDist = Double.MAX_VALUE;

        if (player.health > 0) {
            double dist = Math.hypot(enemy.gridX - player.gridX, enemy.gridY - player.gridY);
            if (dist < minDist) {
                minDist = dist;
                nearestX = player.gridX;
                nearestY = player.gridY;
            }
        }

        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                double dist = Math.hypot(enemy.gridX - friendly.gridX, enemy.gridY - friendly.gridY);
                if (dist < minDist) {
                    minDist = dist;
                    nearestX = friendly.gridX;
                    nearestY = friendly.gridY;
                }
            }
        }

        if (nearestX != -1) {
            int dirX = enemy.gridX - nearestX;
            int dirY = enemy.gridY - nearestY;

            if (Math.abs(dirX) > Math.abs(dirY)) {
                if (dirX > 0 && canMoveToForEnemy(enemy, enemy.gridX + 1, enemy.gridY)) {
                    startRetreatMove(enemy, enemy.gridX + 1, enemy.gridY, Enemy.Direction.RIGHT);
                    enemy.setRetreatTurns(enemy.getRetreatTurns() - 1);
                    return true;
                } else if (dirX < 0 && canMoveToForEnemy(enemy, enemy.gridX - 1, enemy.gridY)) {
                    startRetreatMove(enemy, enemy.gridX - 1, enemy.gridY, Enemy.Direction.LEFT);
                    enemy.setRetreatTurns(enemy.getRetreatTurns() - 1);
                    return true;
                }
            } else {
                if (dirY > 0 && canMoveToForEnemy(enemy, enemy.gridX, enemy.gridY + 1)) {
                    startRetreatMove(enemy, enemy.gridX, enemy.gridY + 1, Enemy.Direction.DOWN);
                    enemy.setRetreatTurns(enemy.getRetreatTurns() - 1);
                    return true;
                } else if (dirY < 0 && canMoveToForEnemy(enemy, enemy.gridX, enemy.gridY - 1)) {
                    startRetreatMove(enemy, enemy.gridX, enemy.gridY - 1, Enemy.Direction.UP);
                    enemy.setRetreatTurns(enemy.getRetreatTurns() - 1);
                    return true;
                }
            }
        }

        return randomMoveOneStep(enemy);
    }

    private void startRetreatMove(Enemy enemy, int newX, int newY, Enemy.Direction dir) {
        if (enemy.isMoving()) return;

        boolean willBeVisible = isEnemyVisible(enemy);
        boolean shouldAnimate = willBeVisible;

        enemy.consumeMovePoints();
        enemy.startAnimatedMove(newX, newY, dir, CELL_SIZE, TANK_SIZE, shouldAnimate);

        if (shouldAnimate && soundManager != null) {
            soundManager.playEnemyMoveSound();
        }
    }

    // Проверка, видит ли кто-то из команды этого врага
    // Проверка, видит ли кто-то из команды этого врага СЕЙЧАС
    // Этот метод уже есть и он правильный - оставьте как есть
    public boolean isEnemyVisibleByAnyone(Enemy enemy) {
        if (fullMapRevealed) return enemy.isAlive;  // ← Добавить эту строку
        if (!enemy.isAlive) return false;

        if (isEnemyVisibleFromPosition(player.gridX, player.gridY, enemy)) {
            return true;
        }

        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                if (isEnemyVisibleFromPosition(friendly.gridX, friendly.gridY, enemy)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Проверка, будет ли враг виден ПОСЛЕ перемещения на новую клетку
    public boolean isEnemyVisibleByAnyoneAfterMove(Enemy enemy, int newX, int newY) {
        // Сохраняем старые координаты
        int oldX = enemy.gridX;
        int oldY = enemy.gridY;

        // Временно перемещаем врага для проверки
        enemy.gridX = newX;
        enemy.gridY = newY;

        boolean visible = isEnemyVisibleByAnyone(enemy);

        // Возвращаем на место
        enemy.gridX = oldX;
        enemy.gridY = oldY;

        return visible;
    }

    // Получить позицию наблюдателя, который видит врага (для центрирования камеры)
    public Point getObserverPositionForEnemy(Enemy enemy) {
        if (isEnemyVisibleFromPosition(player.gridX, player.gridY, enemy)) {
            return new Point(player.gridX, player.gridY);
        }

        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                if (isEnemyVisibleFromPosition(friendly.gridX, friendly.gridY, enemy)) {
                    return new Point(friendly.gridX, friendly.gridY);
                }
            }
        }
        return null;
    }

    // В GameWorld.java, добавьте метод:
    public void addGunshotSound(int x, int y, double caliber) {
        GunshotSound sound = new GunshotSound(x, y, caliber, currentTurn);
        gunshotSounds.add(sound);
        System.out.println("🔊 ВЫСТРЕЛ! Координаты: [" + x + "," + y +
                "], калибр: " + caliber + "м, радиус слышимости: " + sound.radius);

        // Очищаем старые звуки (старше 2 ходов)
        gunshotSounds.removeIf(s -> currentTurn - s.turnHeard > 2);
    }

    // В GameWorld.java, добавьте метод:
    private GunshotSound checkEnemyHearGunshots(Enemy enemy) {
        // Ищем САМЫЙ ГРОМКИЙ/БЛИЖАЙШИЙ звук для этого врага
        GunshotSound closestSound = null;
        int minDistance = Integer.MAX_VALUE;

        for (GunshotSound sound : gunshotSounds) {
            if (currentTurn - sound.turnHeard <= 1 && sound.isInRange(enemy.gridX, enemy.gridY)) {
                int distance = Math.abs(enemy.gridX - sound.x) + Math.abs(enemy.gridY - sound.y);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestSound = sound;
                }
            }
        }

        if (closestSound != null) {
            System.out.println("👂 Враг [" + enemy.gridX + "," + enemy.gridY +
                    "] услышал выстрел в [" + closestSound.x + "," + closestSound.y +
                    "]! Радиус: " + closestSound.radius + ", дистанция: " + minDistance);

            enemy.setLastHeardSoundPosition(new Point(closestSound.x, closestSound.y));
            enemy.setSoundMemoryTurns(2);
            enemy.setInvestigatingSound(true);
            enemy.setPatrolling(false);
            enemy.setLongTermTarget(new Point(closestSound.x, closestSound.y));
            enemy.setTargetPriority(3);
        }

        return closestSound;  // ← возвращаем звук, который услышали
    }

    public void loadWinZone(String levelPath) {
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;
        winZone = LevelLoader.loadWinZone(levelPath, gridWidth, gridHeight);
    }

    // Метод проверки победы (вызывать после каждого хода игрока/союзника)
    // Метод проверки победы (вызывать только в конце хода)
    public void checkVictory() {
        if (levelCompleted) return;
        if (winZone == null) return;

        // Проверяем, находится ли игрок в победной зоне
        boolean playerInZone = isPositionInWinZone(player.gridX, player.gridY);

        // Проверяем всех нанятых союзников
        boolean allAlliesInZone = true;
        boolean hasAnyAlly = false;

        for (FriendlyUnit friendly : friendlyUnits) {
            if (friendly.isAlive && friendly.isRecruited) {
                hasAnyAlly = true;
                if (!isPositionInWinZone(friendly.gridX, friendly.gridY)) {
                    allAlliesInZone = false;
                    break;
                }
            }
        }

        if (playerInZone && (allAlliesInZone || !hasAnyAlly)) {
            levelCompleted = true;
            System.out.println("🏆 ПОБЕДА! Все юниты достигли победной зоны!");

            if (gamePanel != null) {
                // Вместо showVictoryDialog вызываем переход на следующий уровень
                gamePanel.loadNextLevel();
            }
        }
    }

    // Проверка, находится ли позиция в победной зоне
    private boolean isPositionInWinZone(int gridX, int gridY) {
        if (winZone == null) return false;
        return (gridX >= winZone.x && gridX < winZone.x + 5 &&
                gridY >= winZone.y && gridY < winZone.y + 5);
    }

    // Визуальная проверка, видит ли игрок победную зону (для отрисовки на миникарте)
    public boolean isWinZoneVisible() {
        if (winZone == null) return false;
        // Проверяем видимость любого угла зоны
        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                int x = winZone.x + dx;
                int y = winZone.y + dy;
                if (isCellVisibleToPlayer(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean[][] getCombinedVisibilityMap() { return combinedVisibilityMap; }
    public List<GunshotSound> getGunshotSounds() { return gunshotSounds; }

    // Геттер:
    public Point getWinZone() { return winZone; }
    public boolean isLevelCompleted() { return levelCompleted; }
    public boolean isEnemyTurn() {
        return isEnemyTurn;
    }

    // В GameWorld.java добавьте метод:

    // В GameWorld.java, замените метод dropItemToAdjacentCell на этот:

    public boolean dropItemOnCurrentCell(int sourceX, int sourceY, Item item) {
        // Проверяем, есть ли уже дроп на текущей клетке
        LootDrop existingDrop = getLootDropAt(sourceX, sourceY);

        if (existingDrop != null) {
            // Дроп уже существует - добавляем предметы в него
            if (item instanceof AmmoItem) {
                AmmoItem ammo = (AmmoItem) item;
                existingDrop.addAmmo(ammo.getCaliber(), ammo.getCount());
            } else {
                existingDrop.addItem(item.getType(), item.getCount());
            }
            System.out.println("📦 Предмет добавлен к существующему дропу в клетке [" + sourceX + "," + sourceY + "]");
            return true;
        } else {
            // Создаём новый дроп на текущей клетке
            LootDrop drop = new LootDrop(sourceX, sourceY);

            if (item instanceof AmmoItem) {
                AmmoItem ammo = (AmmoItem) item;
                drop.addAmmo(ammo.getCaliber(), ammo.getCount());
            } else {
                drop.addItem(item.getType(), item.getCount());
            }

            lootDrops.add(drop);
            System.out.println("📦 Создан новый дроп в клетке [" + sourceX + "," + sourceY + "]");
            return true;
        }
    }

    // В GameWorld.java добавьте эти методы:

    public void loadNextLevel(String nextLevelPath, GamePanel gamePanel) {
        System.out.println("=== ЗАГРУЗКА СЛЕДУЮЩЕГО УРОВНЯ: " + nextLevelPath + " ===");

        // ===== ПРОВЕРКА: M53 в команде =====
        FriendlyUnit m53 = getM53Unit();
        if (m53 != null) {
            System.out.println("👋 M53 покидает команду при переходе на новый уровень.");
            System.out.println("   Его история завершена, он остаётся с отцом T18.");

            // Удаляем из управляемых юнитов
            controllableUnits.remove(m53);

            // Отмечаем как недоступного
            m53.isRecruited = false;
            m53.isUnavailable = true;

            // Показываем сообщение (если есть GamePanel)
            if (gamePanel != null) {
                JOptionPane.showMessageDialog(gamePanel,
                        "👨‍👦 " + m53.name + " остаётся здесь, чтобы помочь отцу T18.\n\n" +
                                "Его приключение с вами закончено.\n" +
                                "Он навсегда запомнит вашу помощь!",
                        "Прощание с M53",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }

        // Сохраняем важные данные текущей команды
        List<FriendlyUnit> recruitedUnits = new ArrayList<>();
        PlayerTank.PlayerSaveData playerData = player.saveData();

        for (FriendlyUnit unit : friendlyUnits) {
            if (unit.isRecruited && unit.isAlive) {
                recruitedUnits.add(unit);
            }
        }

        // Очищаем текущий мир
        clearWorld();

        // Загружаем новый уровень
        loadLevel(nextLevelPath, gamePanel);

        // Восстанавливаем команду
        restoreTeam(recruitedUnits, playerData);

        // ===== ПОКАЗЫВАЕМ СООБЩЕНИЕ О ВОССТАНОВЛЕНИИ =====
        if (gamePanel != null) {
            int healedCount = 0;
            // Считаем, сколько юнитов было ранено (но не убито)
            for (FriendlyUnit unit : friendlyUnits) {
                if (unit.isRecruited && unit.isAlive && unit.health < unit.maxHealth) {
                    healedCount++;
                }
            }
            if (player.health < player.maxHealth) healedCount++;

            if (healedCount > 0) {
                JOptionPane.showMessageDialog(gamePanel,
                        "🏥 ВСЯ КОМАНДА ИСЦЕЛЕНА!\n\n" +
                                healedCount + " членов команды восстановили здоровье до максимума.\n" +
                                "Готовьтесь к новым испытаниям!",
                        "Исцеление",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }

        System.out.println("=== УРОВЕНЬ ЗАГРУЖЕН, КОМАНДА ВОССТАНОВЛЕНА ===");
    }

    // В GameWorld.java, добавьте эти методы:

    // ===== ДОБАВЬТЕ ЭТОТ МЕТОД =====
    public FriendlyUnit getM53Unit() {
        for (FriendlyUnit unit : friendlyUnits) {
            if (unit.isAlive && unit.isRecruited && "M53".equals(unit.type)) {
                return unit;
            }
        }
        return null;
    }

    private void clearWorld() {
        walls.clear();
        enemies.clear();
        trees.clear();
        pavements.clear();
        oakPlanks.clear();
        ironFloors.clear();
        infernalLands.clear();
        friendlyUnits.clear();
        controllableUnits.clear();
        storageChests.clear();
        garbageContainers.clear();
        questNPCs.clear();
        traders.clear();
        lootDrops.clear();
        hills.clear();
        winZone = null;
        levelCompleted = false;
    }

    private void loadLevel(String levelPath, GamePanel gamePanel) {

        this.currentLevelPath = levelPath;
        int gridWidth = FIELD_SIZE / CELL_SIZE;
        int gridHeight = FIELD_SIZE / CELL_SIZE;

        // Загружаем стены
        walls = LevelLoader.loadLevel(levelPath, gridWidth, gridHeight);

        // Загружаем NPC
        questNPCs = LevelLoader.loadNPCs(levelPath, gridWidth, gridHeight);

        // Загружаем деревья
        trees = LevelLoader.loadTrees(levelPath, gridWidth, gridHeight);

        // Загружаем асфальт
        pavements = LevelLoader.loadPavements(levelPath, gridWidth, gridHeight);

        // Загружаем дубовые доски
        oakPlanks = LevelLoader.loadOakPlanks(levelPath, gridWidth, gridHeight);
        ironFloors = LevelLoader.loadIronFloors(levelPath, gridWidth, gridHeight);

        infernalLands = LevelLoader.loadInfernalLands(levelPath, gridWidth, gridHeight);

        waters = LevelLoader.loadWaters(levelPath, gridWidth, gridHeight);
        doors = LevelLoader.loadDoors(levelPath, gridWidth, gridHeight);

        // Загружаем союзников (ненаймлённых)
        List<FriendlyUnit> loadedFriendlies = LevelLoader.loadFriendlyUnits(levelPath, gridWidth, gridHeight);
        for (FriendlyUnit f : loadedFriendlies) {
            f.setSoundManager(soundManager);
            friendlyUnits.add(f);
        }

        // Загружаем тумбочки
        storageChests = LevelLoader.loadStorageChests(levelPath, gridWidth, gridHeight);

        // Загружаем врагов
        enemies = LevelLoader.loadEnemies(levelPath, gridWidth, gridHeight, this);

        // Загружаем мусорные контейнеры
        garbageContainers = LevelLoader.loadGarbageContainers(levelPath, gridWidth, gridHeight);

        // Загружаем торговцев
        traders = LevelLoader.loadTraders(levelPath, gridWidth, gridHeight);

        // Загружаем победную зону
        winZone = LevelLoader.loadWinZone(levelPath, gridWidth, gridHeight);

        // Загружаем стартовую позицию игрока
        Point startPos = LevelLoader.loadStartPosition(levelPath, gridWidth, gridHeight);
        if (startPos != null) {
            player.gridX = startPos.x + 2;  // Центр зоны 5x5
            player.gridY = startPos.y + 2;
        } else {
            player.gridX = gridWidth / 2;
            player.gridY = gridHeight / 2;
        }

        // Генерируем холмы
        generateHills();

        // Обновляем видимость
        updateVisibilityMap();

        // Обновляем карту кэша
        if (gamePanel != null) {
            gamePanel.cacheField();
        }

        System.out.println("Загружено: стен=" + walls.size() + ", врагов=" + enemies.size() +
                ", союзников=" + friendlyUnits.size());
    }

    public void reloadWinZoneFromFile() {
        System.out.println("=== reloadWinZoneFromFile() ===");
        if (gamePanel == null) {
            System.out.println("⚠ gamePanel is null!");
            return;
        }
        String path = gamePanel.getCurrentLevelPath();
        System.out.println("  currentLevelPath = " + path);
        if (path != null) {
            int gridWidth = FIELD_SIZE / CELL_SIZE;
            int gridHeight = FIELD_SIZE / CELL_SIZE;
            Point loadedWinZone = LevelLoader.loadWinZone(path, gridWidth, gridHeight);
            if (loadedWinZone != null) {
                this.winZone = loadedWinZone;
                System.out.println("✅ Победная зона перезагружена из файла: [" + winZone.x + "," + winZone.y + "]");
            } else {
                System.out.println("⚠ Победная зона не найдена в файле: " + path);
            }
        } else {
            System.out.println("⚠ Не могу перезагрузить победную зону: путь к уровню null");
        }
    }

    private void restoreTeam(List<FriendlyUnit> recruitedUnits, PlayerTank.PlayerSaveData playerData) {
        // Восстанавливаем игрока
        player.restoreData(playerData);

        // ===== ВОССТАНАВЛИВАЕМ ЗДОРОВЬЕ ИГРОКА ДО МАКСИМУМА =====
        player.health = player.maxHealth;
        System.out.println("❤️ Leichttraktor полностью исцелён при переходе на новый уровень!");

        // Восстанавливаем нанятых союзников
        for (FriendlyUnit savedUnit : recruitedUnits) {

            // ===== ПРОПУСКАЕМ M53 =====
            if ("M53".equals(savedUnit.type)) {
                System.out.println("⏭️ Пропускаем M53 при загрузке (он должен остаться с отцом)");
                continue;
            }
            // Ищем соответствующего союзника на новой карте (по имени и типу)
            FriendlyUnit targetUnit = null;
            for (FriendlyUnit unit : friendlyUnits) {
                if (unit.name.equals(savedUnit.name) && unit.type.equals(savedUnit.type)) {
                    targetUnit = unit;
                    break;
                }
            }

            if (targetUnit != null) {
                // Восстанавливаем все характеристики, КРОМЕ здоровья
                targetUnit.isRecruited = true;
                // ===== НЕ ВОССТАНАВЛИВАЕМ ЗДОРОВЬЕ ИЗ СОХРАНЕНИЯ! =====
                // targetUnit.health = savedUnit.health;  // ← УДАЛИТЬ ЭТУ СТРОКУ
                targetUnit.maxHealth = savedUnit.maxHealth;

                // ===== ВОССТАНАВЛИВАЕМ ЗДОРОВЬЕ СОЮЗНИКА ДО МАКСИМУМА =====
                targetUnit.health = targetUnit.maxHealth;
                System.out.println("❤️ " + targetUnit.name + " полностью исцелён при переходе на новый уровень!");

                targetUnit.strength = savedUnit.strength;
                targetUnit.agility = savedUnit.agility;
                targetUnit.weaponAccuracy = savedUnit.weaponAccuracy;
                targetUnit.armor = savedUnit.armor;
                targetUnit.critBonus = savedUnit.critBonus;
                targetUnit.vision = savedUnit.vision;
                targetUnit.viewRadius = savedUnit.viewRadius;
                targetUnit.reloadBonus = savedUnit.reloadBonus;
                targetUnit.nimble = savedUnit.nimble;
                targetUnit.dodgeChance = savedUnit.dodgeChance;

                // Восстанавливаем опыт
                if (savedUnit.getExperienceSystem() != null) {
                    targetUnit.setExperienceSystem(savedUnit.getExperienceSystem());
                }

                // Восстанавливаем инвентарь
                targetUnit.getInventory().clearInventory();
                copyInventory(savedUnit.getInventory(), targetUnit.getInventory());

                // Восстанавливаем экипированное оружие
                if (savedUnit.getEquippedWeaponId() != null) {
                    FriendlyUnit.WeaponData weapon = WeaponLibrary.getWeaponByWeaponId(savedUnit.getEquippedWeaponId());
                    if (weapon != null) {
                        targetUnit.setEquippedWeapon(savedUnit.getEquippedWeaponId(), weapon);
                    }
                }

                addControllableUnit(targetUnit);
                System.out.println("✅ Восстановлен союзник: " + targetUnit.name);
            } else {
                // Если союзника нет на новой карте, создаём его на стартовой позиции
                targetUnit = savedUnit;
                targetUnit.gridX = player.gridX;
                targetUnit.gridY = player.gridY;
                // Находим свободную клетку рядом
                int[][] offsets = {{0,1},{1,0},{0,-1},{-1,0},{1,1},{-1,-1},{1,-1},{-1,1}};
                for (int[] off : offsets) {
                    int newX = player.gridX + off[0];
                    int newY = player.gridY + off[1];
                    if (canMoveToForFriendly(targetUnit, newX, newY)) {
                        targetUnit.gridX = newX;
                        targetUnit.gridY = newY;
                        break;
                    }
                }

                // ===== ВОССТАНАВЛИВАЕМ ЗДОРОВЬЕ ДО МАКСИМУМА =====
                targetUnit.health = targetUnit.maxHealth;
                targetUnit.isRecruited = true;

                friendlyUnits.add(targetUnit);
                addControllableUnit(targetUnit);
                System.out.println("✅ Союзник " + targetUnit.name + " добавлен на новую карту у игрока (исцелён)");
            }
        }

        // Обновляем список управляемых юнитов в GamePanel
        if (gamePanel != null) {
            gamePanel.syncRecruitedUnits();
        }

        if (gamePanel != null) {
            String levelPath = gamePanel.getCurrentLevelPath();
            if (levelPath != null) {
                int gridWidth = FIELD_SIZE / CELL_SIZE;
                int gridHeight = FIELD_SIZE / CELL_SIZE;
                winZone = LevelLoader.loadWinZone(levelPath, gridWidth, gridHeight);
                System.out.println("✅ Победная зона перезагружена из файла: " + winZone);
            }
        }

        checkSavM43Quest();
        revalidateAllQuestNPCs();
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    private void copyInventory(Inventory from, Inventory to) {
        // Копируем предметы из одного инвентаря в другой
        for (int y = 0; y < Inventory.HEIGHT; y++) {
            for (int x = 0; x < Inventory.WIDTH; x++) {
                Item item = from.getItem(x, y);
                if (item != null && item.getCount() > 0) {
                    if (item instanceof AmmoItem) {
                        AmmoItem ammo = (AmmoItem) item;
                        to.addAmmoItem(new AmmoItem(ammo.getCaliber(), ammo.getCount(), ammo.isImproved()));
                    } else {
                        to.addItemToInventory(new Item(item.getType(), item.getCount()));
                    }
                }
            }
        }
    }

    public void removeDeadControllableUnits() {
        controllableUnits.removeIf(unit -> !unit.isAlive);
        System.out.println("Удалено мёртвых союзников из управляемых, осталось: " + controllableUnits.size());
    }
}