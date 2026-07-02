package ui;

import audio.SoundManager;
import combat.*;
import entities.*;
import inventory.*;
import world.*;
import world.GameWorld;
import world.PathFinder;
import entities.Wall;
import audio.*;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.awt.event.MouseMotionListener;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {

    private MusicManager musicManager;

    public boolean enemyDetectedThisTurn = false;  // ← НОВОЕ ПОЛЕ
    private int currentTurnNumber = 0;  // ← НОМЕР ТЕКУЩЕГО ХОДА

    private boolean debugMode = true;
    private boolean isCtrlPressed = false;
    private boolean isShiftPressed = false;
    private boolean cheatModeEnabled = false;
    private boolean shiftPressed = false;
    private boolean ctrlPressed = false;

    private long lastM53SpottedTime = 0;
    private int lastVisibleEnemyCount = 0;
    private GameWorld world;
    private PlayerTank player;
    private Weapon weapon;
    private SoundManager soundManager;
    private PathFinder pathFinder;

    // Добавьте поле:
    private BufferedImage wallImage;
    private BufferedImage ironBlockImage;
    private BufferedImage bedsideTableImage;
    private Map<Door.DoorColor, BufferedImage> doorImages;

    private List<IronFloor> ironFloors = new ArrayList<>();  // ← ДОБАВИТЬ
    private BufferedImage m53Image;
    private BufferedImage m53Portrait;  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
    private BufferedImage m53Right, m53Left, m53Up, m53Down;  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
    private BufferedImage ms1Right, ms1Left, ms1Up, ms1Down;  // ← ДОБАВИТЬ
    private BufferedImage ms1Portrait;

    // После других полей для изображений:
    private BufferedImage vk10001pRight, vk10001pLeft, vk10001pUp, vk10001pDown;
    private BufferedImage vk10001pPortrait;
    // После других полей для изображений, добавьте:
    private BufferedImage amx40Right, amx40Left, amx40Up, amx40Down;
    private BufferedImage amx40Portrait;
    private BufferedImage t1Right, t1Left, t1Up, t1Down;
    private BufferedImage t1Portrait;
    private BufferedImage traderImage;

    private BufferedImage rOtsuRight, rOtsuLeft, rOtsuUp, rOtsuDown;
    private BufferedImage rOtsuEnemyRight, rOtsuEnemyLeft, rOtsuEnemyUp, rOtsuEnemyDown;

    private BufferedImage m14_41Right, m14_41Left, m14_41Up, m14_41Down;
    private BufferedImage m14_41EnemyRight, m14_41EnemyLeft, m14_41EnemyUp, m14_41EnemyDown;

    private BufferedImage h35Right, h35Left, h35Up, h35Down;
    private BufferedImage h35EnemyRight, h35EnemyLeft, h35EnemyUp, h35EnemyDown;
    // В начале класса GamePanel, после других полей для изображений, добавьте:
    private BufferedImage ftRight, ftLeft, ftUp, ftDown;
    private BufferedImage ftEnemyRight, ftEnemyLeft, ftEnemyUp, ftEnemyDown;
    private BufferedImage fiat3000Right, fiat3000Left, fiat3000Up, fiat3000Down;
    private BufferedImage fiat3000EnemyRight, fiat3000EnemyLeft, fiat3000EnemyUp, fiat3000EnemyDown;
    private BufferedImage savM43Image;


    private List<combat.Projectile> activeProjectiles = new ArrayList<>();
    // Список нанятых союзников для отображения в HUD
    private List<FriendlyUnit> recruitedUnits = new ArrayList<>();
    private Random random = new Random();

    private BufferedImage tankRight, tankLeft, tankUp, tankDown, currentTankImage;
    private BufferedImage enemyRight, enemyLeft, enemyUp, enemyDown;
    private BufferedImage hillImage, heroPortrait;
    private BufferedImage fieldCache;
    private BufferedImage tree1Image, tree2Image;
    private BufferedImage pavementImage;
    private BufferedImage ironFloorImage;
    private BufferedImage oakPlanksImage;
    private BufferedImage waterImage;

    private BufferedImage doorImage;
    private BufferedImage infernalLandImage;
    private BufferedImage friendlyImage;
    private BufferedImage dumpsterImage;

    private int cameraX = 0, cameraY = 0;

    // В начале класса GamePanel, после других полей
    private Enemy currentActiveEnemy = null;  // Текущий враг, за которым следит камера
    private javax.swing.Timer timer;

    // Система путей и движения (ВОССТАНОВЛЕНО)
    private int targetGridX = -1;
    private int targetGridY = -1;
    private java.util.List<Point> currentPath = new ArrayList<>();
    private int currentPathIndex = 0;
    private boolean isAutoMoving = false;

    // Для анимации движения (ВОССТАНОВЛЕНО)
    private boolean isMoving = false;
    private int moveProgress = 0;
    private int moveFromX, moveFromY;
    private int moveToX, moveToY;
    private PlayerTank.Direction moveDirection;

    // Режим стрельбы
    private boolean isAimingMode = false;
    private int targetShotX = -1, targetShotY = -1;

    private boolean hillImageLoaded = false;
    private boolean portraitLoaded = false;

    private int hoveredEnemyX = -1;      // Клетка врага под мышью
    private int hoveredEnemyY = -1;
    private Enemy hoveredEnemy = null;
    private boolean gameOver = false;

    // Цвета для интерфейса
    private Color aimingModeColor = new Color(255, 100, 0, 220);
    private Color burstModeColor = new Color(0, 150, 200, 220);



    // Добавьте поля:
    private BufferedImage npcImage;
    private QuestNPC currentNPC = null;
    private boolean showQuestDialog = false;

    private boolean wasPlayerVisibleEnemyBeforeMove = false;
    private boolean wasFriendlyVisibleEnemyBeforeMove = false;
    private boolean isFullMapRevealed = false;  // Флаг для чита видимости всей карты
    // В начале класса GamePanel, после других полей:
    private int visibleEnemyCountBeforeMove = 0;
    private Map<FriendlyUnit, Integer> friendlyVisibleEnemyCountBeforeMove = new HashMap<>();


    private String currentLevelPath;

    private GameFrame parentFrame;  // Добавьте это поле

    // В начале класса GamePanel, после других полей, добавьте:
    private int currentUnitIndex = 0;  // Индекс текущего активного юнита (0 = игрок)


    private List<Point> friendlyPath = new ArrayList<>();
    private int friendlyPathIndex = 0;
    private boolean isFriendlyAutoMoving = false;
    private FriendlyUnit movingFriendly = null;

    // Для анимации движения M53
    private boolean isFriendlyMoving = false;
    private int friendlyMoveProgress = 0;
    private int friendlyMoveFromX, friendlyMoveFromY;
    private int friendlyMoveToX, friendlyMoveToY;
    private FriendlyUnit.Direction friendlyMoveDirection;
    private FriendlyUnit movingFriendlyForAnimation = null;

    private Point hoverTargetGrid = null;      // Цель при наведении мыши
    private List<Point> hoverPath = new ArrayList<>();  // Путь при наведении мыши
    private boolean isTeleportMode = false;


    // В начале класса GamePanel, после других полей
    private List<Enemy> visibleEnemies = new ArrayList<>();
    private int currentEnemyIndex = -1;  // -1 означает, что камера не на враге
    private boolean isCameraOnEnemy = false;

    private boolean needsDetectionCheck = false;



    // В начале класса GamePanel добавьте:
    private String[] levelPaths = {
            "src/levels/level1.txt",
            "src/levels/level2.txt",
            "src/levels/level3.txt",
            // ... добавьте остальные уровни
    };
    private int currentLevelIndex = 0;

    public GamePanel(GameFrame frame) {
        this.parentFrame = frame;
        //this.musicManager = soundManager.getMusicManager();
        this.currentLevelPath = "src/levels/level1.txt";


        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        soundManager = new SoundManager();
        this.musicManager = soundManager.getMusicManager(); // ← ПОТОМ ПОЛУЧАЕМ MusicManager

        // ===== УСТАНАВЛИВАЕМ ГРОМКОСТЬ =====
        if (musicManager != null) {
            musicManager.setVolume(0.6f); // 60% громкости
        }

        Point loadedStartPos = LevelLoader.loadStartPosition(
                "src/levels/level1.txt",
                GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE,
                GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE
        );

        int startX, startY;
        if (loadedStartPos != null) {
            // Центр области 5x5
            startX = loadedStartPos.x + 2;
            startY = loadedStartPos.y + 2;
            System.out.println("Используем стартовую позицию из файла: [" + startX + "," + startY + "]");
        } else {
            startX = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE / 2;
            startY = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE / 2;
            System.out.println("Стартовая позиция не найдена, используем центр карты");
        }

        player = new PlayerTank(startX, startY);
        player.setSoundManager(soundManager);

        loadTankImages();       // ← ДОБАВИТЬ ЭТУ СТРОКУ

        player.addParts(0);  // ← временно для тестирования модернизации
        System.out.println("⚙️ Для тестирования добавлено 8000 деталей");

        // В GamePanel, после создания player:
        player.getInventory().convertOldAmmo();
        world = new GameWorld(player);


        // ПЕРЕДАЁМ SoundManager В GameWorld
        world.setSoundManager(soundManager);
        world.setGamePanel(this);

        weapon = new Weapon();
        soundManager.updateWeaponCaliber(weapon.caliber);
        pathFinder = new PathFinder(world);

        loadImages();
        soundManager.loadMoveSound();
        soundManager.loadEnemyMoveSound();
        soundManager.loadVoiceSound();
        soundManager.loadSpottingSounds();
        soundManager.loadEnemyDestroyedSounds();  // ← Добавить эту строку
        soundManager.loadM53Sounds();  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
        soundManager.loadMS1Sounds();  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
        soundManager.loadVK10001PSounds();
        soundManager.loadT1Sounds();
        soundManager.loadPoisonedSounds();
        soundManager.preloadEnemyShootSounds();  // ← ПРЕДЗАГРУЗКА ЗВУКОВ ВРАГОВ

        cacheField();
        world.setFieldCache(fieldCache);
        world.generateHills();
        world.loadWalls("src/levels/level1.txt");
        world.loadNPCs("src/levels/level1.txt");
        world.loadTrees("src/levels/level1.txt");
        world.loadPavements("src/levels/level1.txt");
        world.loadOakPlanks("src/levels/level1.txt");
        world.loadIronFloors("src/levels/level1.txt");
        world.loadWaters("src/levels/level1.txt");
        world.loadDoors("src/levels/level1.txt");
        world.loadFriendlyUnits("src/levels/level1.txt");
        world.loadStorageChests("src/levels/level1.txt");
        world.loadEnemies("src/levels/level1.txt");    // ← СНАЧАЛА ЗАГРУЖАЕМ ИЗ ФАЙЛА
        world.loadGarbageContainers("src/levels/level1.txt");
        world.loadTraders("src/levels/level1.txt");
        world.loadWinZone("src/levels/level1.txt");

        // ===== ДОБАВЬТЕ ЭТОТ БЛОК =====
        // Обновляем текстуры для уже нанятых союзников
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isRecruited && friendly.isAlive) {
                loadFriendlyTextures(friendly);
                updateFriendlyPortrait(friendly);
                System.out.println("  Загружены текстуры для нанятого союзника: " + friendly.name);
            }
        }
        // ==============================

        world.updateVisibilityMap();
        updateBackgroundMusic();  // Запускаем музыку при старте игры

        // А затем, ЕСЛИ врагов всё ещё нет - генерируем случайных
        if (world.getEnemies().isEmpty()) {
            world.generateEnemies(10);
        }

        // ДОБАВЬТЕ MouseMotionListener
        addMouseMotionListener(this);

        timer = new javax.swing.Timer(16, this);
        timer.start();

        setFocusable(true);
        requestFocusInWindow();

        // В конце конструктора GamePanel добавьте:
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "switchUnit");
        actionMap.put("switchUnit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("TAB через ActionMap!");
                switchActiveUnit();
            }
        });
    }

    private void convertOldAmmoToNew() {
        Inventory inv = player.getInventory();
        int oldAmmoCount = inv.getItemCount(Item.ItemType.AMMO);

        if (oldAmmoCount > 0) {
            inv.removeAllAmmo();
            // ИСПОЛЬЗУЙТЕ ПОЛНЫЕ ИМЕНА С ПАКЕТОМ
            inventory.AmmoItem newAmmo = new inventory.AmmoItem(inventory.Caliber.CALIBER_20MM, oldAmmoCount);
            inv.addAmmoItem(newAmmo);
            System.out.println("🔄 Конвертировано " + oldAmmoCount + " старых снарядов в 20mm");
        }
    }

    // Установка цели для союзника
    // Установка цели для союзника
    private void setFriendlyTarget(FriendlyUnit friendly, int targetGridX, int targetGridY) {
        if (player.turnEnded) {
            System.out.println("Ход завершен! Нажмите E для нового хода.");
            return;
        }

        // Всегда проверяем, можно ли физически пройти (без учёта очков хода)
        if (!world.canMoveToForFriendly(friendly, targetGridX, targetGridY) &&
                !(friendly.gridX == targetGridX && friendly.gridY == targetGridY)) {
            System.out.println("Нельзя выбрать эту клетку для " + friendly.name + "!");
            return;
        }

        // Проверяем, кликнули ли по той же клетке, что и текущий путь
        boolean isSameTarget = false;
        if (!friendlyPath.isEmpty() && movingFriendly == friendly) {
            Point lastTarget = friendlyPath.get(friendlyPath.size() - 1);
            if (lastTarget.x == targetGridX && lastTarget.y == targetGridY) {
                isSameTarget = true;
            }
        }

        // Если кликнули по той же цели - начинаем движение С ПРОВЕРКОЙ ОЧКОВ ХОДА
        // В GamePanel.java, в методе setFriendlyTarget, замените блок проверки очков хода:

// Если кликнули по той же цели - начинаем движение С ПРОВЕРКОЙ ОЧКОВ ХОДА
        if (isSameTarget && !isFriendlyAutoMoving) {
            // Проверяем, хватает ли очков хода для всего пути
            int totalCost = friendlyPath.size() * friendly.moveCost;
            if (friendly.movePoints < totalCost) {
                // ===== НОВОЕ: обрезаем путь до максимально возможного =====
                int maxSteps = friendly.movePoints / friendly.moveCost;
                if (maxSteps > 0) {
                    System.out.println("⚠ Недостаточно очков хода для всего пути!");
                    System.out.println("   Нужно: " + totalCost + ", есть: " + friendly.movePoints);
                    System.out.println("   Будет пройдено максимально возможное расстояние (" + maxSteps + " шагов)");

                    // Обрезаем путь
                    friendlyPath = friendlyPath.subList(0, maxSteps);

                    // Запускаем движение по обрезанному пути
                    isFriendlyAutoMoving = true;
                    friendlyPathIndex = 0;
                    moveFriendlyToNextCell();
                    repaint();
                } else {
                    System.out.println("⚠ Недостаточно очков хода даже для одного шага!");
                    JOptionPane.showMessageDialog(this,
                            "⚠ " + friendly.name + " не может пройти этот путь!\n\n" +
                                    "Нужно очков хода: " + totalCost + "\n" +
                                    "Осталось: " + friendly.movePoints,
                            "Недостаточно очков хода",
                            JOptionPane.WARNING_MESSAGE);
                }
                return;
            }

            System.out.println("Запуск движения по сохранённому пути для " + friendly.name);
            isFriendlyAutoMoving = true;
            friendlyPathIndex = 0;
            moveFriendlyToNextCell();
            repaint();
            return;
        }

        // Запоминаем КОЛИЧЕСТВО видимых врагов ДО начала движения
        int visibleCount = 0;
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isAlive && world.isEnemyVisibleFromPosition(friendly.gridX, friendly.gridY, enemy)) {
                visibleCount++;
            }
        }
        friendlyVisibleEnemyCountBeforeMove.put(friendly, visibleCount);
        System.out.println("📊 " + friendly.name + " до движения видит врагов: " + visibleCount);

        // ВСЕГДА вычисляем путь, даже если не хватает очков хода
        List<Point> newPath = findPathForFriendly(friendly, targetGridX, targetGridY);

        if (newPath.isEmpty()) {
            System.out.println("Путь не найден для " + friendly.name + "!");
            friendlyPath.clear();
            movingFriendly = null;
            hoverPath.clear();
            hoverTargetGrid = null;
        } else {
            System.out.println("Путь найден для " + friendly.name + "! Шагов: " + newPath.size());

            // Сохраняем путь для отрисовки, НО НЕ НАЧИНАЕМ ДВИЖЕНИЕ
            friendlyPath = newPath;
            movingFriendly = friendly;
            isFriendlyAutoMoving = false;  // Не двигаемся автоматически

            // Очищаем hoverPath, чтобы не мешал
            hoverPath.clear();
            hoverTargetGrid = null;
        }
        repaint();
    }

    public void updateBackgroundMusic() {
        if (musicManager == null) {
            System.err.println("⚠ musicManager is NULL!");
            return;
        }

        // ===== ЕСЛИ В ЭТОМ ХОДУ УЖЕ БЫЛ ОБНАРУЖЕН ВРАГ - НЕ МЕНЯЕМ МУЗЫКУ =====
        if (enemyDetectedThisTurn) {
            System.out.println("🎵 В этом ходу был обнаружен враг! Музыка НЕ меняется.");
            return;
        }

        boolean hasVisibleEnemy = false;
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isAlive && world.isEnemyVisibleByTeam(enemy)) {
                hasVisibleEnemy = true;
                break;
            }
        }

        System.out.println("updateBackgroundMusic: visible enemy = " + hasVisibleEnemy +
                ", enemyDetectedThisTurn = " + enemyDetectedThisTurn);
        musicManager.updateMusic(hasVisibleEnemy);
    }

    public void disposeMusic() {
        if (musicManager != null) {
            musicManager.dispose();
        }
    }

    // Поиск пути для союзника
    // Поиск пути для союзника (без проверки очков хода)
    private List<Point> findPathForFriendly(FriendlyUnit friendly, int targetX, int targetY) {
        int gridWidth = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE;
        int gridHeight = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE;

        if (targetX < 0 || targetX >= gridWidth || targetY < 0 || targetY >= gridHeight) {
            return new ArrayList<>();
        }

        boolean[][] visited = new boolean[gridWidth][gridHeight];
        Point[][] previous = new Point[gridWidth][gridHeight];
        Queue<Point> queue = new LinkedList<>();

        queue.add(new Point(friendly.gridX, friendly.gridY));
        visited[friendly.gridX][friendly.gridY] = true;

        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.x == targetX && current.y == targetY) {
                List<Point> path = new ArrayList<>();
                Point step = new Point(targetX, targetY);
                while (!(step.x == friendly.gridX && step.y == friendly.gridY)) {
                    path.add(0, step);
                    step = previous[step.x][step.y];
                    if (step == null) break;
                }
                return path;
            }

            for (int i = 0; i < 4; i++) {
                int newX = current.x + dx[i];
                int newY = current.y + dy[i];
                if (newX >= 0 && newX < gridWidth && newY >= 0 && newY < gridHeight && !visited[newX][newY]) {
                    // Разрешаем проходить через любые клетки, кроме врагов и стен
                    if (world.canMoveToForFriendly(friendly, newX, newY) || (newX == targetX && newY == targetY)) {
                        visited[newX][newY] = true;
                        previous[newX][newY] = current;
                        queue.add(new Point(newX, newY));
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    // Движение союзника к следующей клетке по пути
    // Движение союзника к следующей клетке по пути
    private void moveFriendlyToNextCell() {

        if (checkAndInterruptMoveForFriendly()) {
            return;
        }

        if (movingFriendly == null) {
            isFriendlyAutoMoving = false;
            return;
        }

        // ===== ДОБАВИТЬ ЭТУ ПРОВЕРКУ =====
        if (movingFriendly.isOverweight()) {
            System.out.println("⚠ ПЕРЕГРУЗ! " + movingFriendly.name + " не может двигаться!");
            isFriendlyAutoMoving = false;
            soundManager.stopMoveSound();
            movingFriendly = null;
            friendlyPath.clear();
            JOptionPane.showMessageDialog(this,
                    "⚠ " + movingFriendly.name + " ПЕРЕГРУЖЕН!\n" +
                            "Выбросьте лишние предметы через инвентарь.",
                    "Невозможно двигаться", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ===== ДОБАВИТЬ ПРОВЕРКУ ОЧКОВ ХОДА ПЕРЕД ШАГОМ =====
        if (!movingFriendly.canMove()) {
            System.out.println("⚠ Недостаточно очков хода для следующего шага!");
            System.out.println("   Остановка на максимально возможной позиции: [" + movingFriendly.gridX + "," + movingFriendly.gridY + "]");
            isFriendlyAutoMoving = false;
            friendlyPath.clear();
            friendlyVisibleEnemyCountBeforeMove.remove(movingFriendly);
            movingFriendly = null;
            soundManager.stopMoveSound();
            return;
        }

        if (checkAndInterruptMoveForFriendly()) {
            return;
        }

        if (friendlyPathIndex >= friendlyPath.size()) {
            // Путь завершён - очищаем
            friendlyPath.clear();
            friendlyVisibleEnemyCountBeforeMove.remove(movingFriendly);  // ← ИСПРАВЛЕНО
            movingFriendly = null;
            isFriendlyAutoMoving = false;
            System.out.println("Движение завершено");
            return;
        }

        if (movingFriendly == null) {
            isFriendlyAutoMoving = false;
            return;
        }

        if (player.turnEnded) {
            System.out.println("Ход завершён! Движение прервано.");
            isFriendlyAutoMoving = false;
            soundManager.stopMoveSound();
            movingFriendly = null;
            friendlyPath.clear();
            return;
        }

        Point nextCell = friendlyPath.get(friendlyPathIndex);

        // Убедитесь, что следующий шаг валиден
        if (!world.canMoveToForFriendly(movingFriendly, nextCell.x, nextCell.y)) {
            System.out.println("Путь заблокирован! Движение прервано.");
            isFriendlyAutoMoving = false;
            friendlyVisibleEnemyCountBeforeMove.remove(movingFriendly);  // ← ИСПРАВЛЕНО
            movingFriendly = null;
            friendlyPath.clear();
            return;
        }

        // Определяем направление
        FriendlyUnit.Direction dir = null;
        if (nextCell.x > movingFriendly.gridX) dir = FriendlyUnit.Direction.RIGHT;
        else if (nextCell.x < movingFriendly.gridX) dir = FriendlyUnit.Direction.LEFT;
        else if (nextCell.y > movingFriendly.gridY) dir = FriendlyUnit.Direction.DOWN;
        else if (nextCell.y < movingFriendly.gridY) dir = FriendlyUnit.Direction.UP;

        if (dir != null && movingFriendly.canMove()) {
            // Тратим очки хода
            movingFriendly.consumeMovePoints();

            // ЗАПУСКАЕМ АНИМАЦИЮ
            startFriendlyMove(nextCell.x, nextCell.y, dir);

            // friendlyPathIndex увеличится после завершения анимации в updateFriendlyMove
        } else {
            System.out.println("Движение невозможно! Очков хода: " + movingFriendly.movePoints + ", нужно: " + movingFriendly.moveCost);
            isFriendlyAutoMoving = false;
            soundManager.stopMoveSound();
            friendlyVisibleEnemyCountBeforeMove.remove(movingFriendly);  // ← ИСПРАВЛЕНО
            movingFriendly = null;
            friendlyPath.clear();
        }
    }

    private boolean checkAndInterruptMoveForFriendly() {
        if (movingFriendly == null) return false;

        // Получаем сохранённое количество видимых врагов ДО движения
        Integer savedCount = friendlyVisibleEnemyCountBeforeMove.get(movingFriendly);
        if (savedCount == null) return false;

        // Считаем текущее количество видимых врагов от позиции союзника
        int currentVisibleCount = 0;
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isAlive && world.isEnemyVisibleFromPosition(movingFriendly.gridX, movingFriendly.gridY, enemy)) {
                currentVisibleCount++;
            }
        }

        // ===== ПРЕРЫВАЕМ, ЕСЛИ КОЛИЧЕСТВО ВИДИМЫХ ВРАГОВ УВЕЛИЧИЛОСЬ =====
        if (currentVisibleCount > savedCount) {
            System.out.println("⚠ " + movingFriendly.name + " обнаружил нового противника! (было: " +
                    savedCount + ", стало: " + currentVisibleCount + ") Движение прервано!");
            isFriendlyAutoMoving = false;
            friendlyPath.clear();
            friendlyVisibleEnemyCountBeforeMove.remove(movingFriendly);
            movingFriendly = null;
            soundManager.stopMoveSound();

            return true;
        }

        return false;
    }

    // Запуск анимированного движения союзника
    private void startFriendlyMove(int newGridX, int newGridY, FriendlyUnit.Direction dir) {
        if (movingFriendly == null) return;

        isFriendlyMoving = true;
        friendlyMoveProgress = 0;

        // Вычисляем пиксельные координаты
        friendlyMoveFromX = movingFriendly.gridX * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
        friendlyMoveFromY = movingFriendly.gridY * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
        friendlyMoveToX = newGridX * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
        friendlyMoveToY = newGridY * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
        friendlyMoveDirection = dir;
        movingFriendlyForAnimation = movingFriendly;

        // Обновляем направление
        movingFriendly.currentDirection = dir;

        // Звук движения (опционально)
        soundManager.playMoveSound();

        // Обновляем позицию мгновенно (логически)
        movingFriendly.gridX = newGridX;
        movingFriendly.gridY = newGridY;

        // ===== ОБНОВЛЯЕМ ВИДИМОСТЬ =====
        world.updateVisibilityForFriendly(movingFriendly);
        checkEnemyDetection();
        // =====
    }

    // Обновление анимации движения союзника
    // Обновление анимации движения союзника
    private void updateFriendlyMove() {
        if (!isFriendlyMoving) return;

        friendlyMoveProgress += 16;
        int moveDuration = 200;

        if (friendlyMoveProgress >= moveDuration) {
            if (movingFriendlyForAnimation != null) {
                movingFriendlyForAnimation.gridX = friendlyMoveToX / GameWorld.CELL_SIZE;
                movingFriendlyForAnimation.gridY = friendlyMoveToY / GameWorld.CELL_SIZE;

                if (!isPlayerActive() && getActiveFriendly() == movingFriendlyForAnimation) {
                    centerCameraOnUnit(movingFriendlyForAnimation.gridX, movingFriendlyForAnimation.gridY);
                }
            }

            isFriendlyMoving = false;
            movingFriendlyForAnimation = null;

            soundManager.stopMoveSound();

            // Переходим к следующему шагу пути
            friendlyPathIndex++;

            // Продолжаем движение по пути, если есть ещё шаги и авто-движение включено
            if (isFriendlyAutoMoving && movingFriendly != null && friendlyPathIndex < friendlyPath.size()) {
                SwingUtilities.invokeLater(() -> moveFriendlyToNextCell());
            } else if (friendlyPathIndex >= friendlyPath.size()) {
                // Путь завершён
                friendlyPath.clear();
                movingFriendly = null;
                isFriendlyAutoMoving = false;
                System.out.println("Путь завершён!");
            }
        }

        if (friendlyPathIndex >= friendlyPath.size()) {
            needsDetectionCheck = true;
        }
    }

    // Получение анимированной X-координаты союзника
    // Методы для анимации союзника (добавьте в GamePanel)
    public int getFriendlyAnimatedX(FriendlyUnit friendly) {
        if (isFriendlyMoving && movingFriendlyForAnimation == friendly) {
            float t = (float)friendlyMoveProgress / 200f;
            return (int)(friendlyMoveFromX + (friendlyMoveToX - friendlyMoveFromX) * t);
        }
        return friendly.gridX * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
    }

    public int getFriendlyAnimatedY(FriendlyUnit friendly) {
        if (isFriendlyMoving && movingFriendlyForAnimation == friendly) {
            float t = (float)friendlyMoveProgress / 200f;
            return (int)(friendlyMoveFromY + (friendlyMoveToY - friendlyMoveFromY) * t);
        }
        return friendly.gridY * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
    }

    // Вызывается в начале хода врага
    // Вызывается в начале хода врага
    public void onEnemyTurnStart(Enemy enemy) {
        this.currentActiveEnemy = enemy;
        System.out.println("=== onEnemyTurnStart ===");
        System.out.println("  enemy: [" + enemy.gridX + "," + enemy.gridY + "]");
        System.out.println("  isAlive: " + enemy.isAlive);
        System.out.println("  isVisible: " + world.isEnemyVisibleByAnyone(enemy));
    }

    // Вызывается в конце хода врага
    public void onEnemyTurnEnd() {
        System.out.println("=== onEnemyTurnEnd ===");
        this.currentActiveEnemy = null;
    }

    public void updateCameraToCurrentEnemy() {
        if (currentActiveEnemy != null && world.isEnemyVisibleByAnyone(currentActiveEnemy)) {
            centerCameraOnEnemy(currentActiveEnemy);
        }
    }

    // Центрирование камеры на враге (с проверкой видимости)
    public void centerCameraOnEnemy(Enemy enemy) {
        if (enemy == null || !enemy.isAlive) return;

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        if (viewWidth <= 0 || viewHeight <= 0) return;

        // ===== ИСПОЛЬЗУЕМ ЛОГИЧЕСКИЕ КООРДИНАТЫ, А НЕ АНИМИРОВАННЫЕ! =====
        int enemyScreenX = enemy.gridX * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE / 2;
        int enemyScreenY = enemy.gridY * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE / 2;

        int newCameraX = enemyScreenX - viewWidth / 2;
        int newCameraY = enemyScreenY - viewHeight / 2;

        int maxX = GameWorld.FIELD_SIZE - viewWidth;
        int maxY = GameWorld.FIELD_SIZE - viewHeight;
        newCameraX = Math.max(0, Math.min(maxX, newCameraX));
        newCameraY = Math.max(0, Math.min(maxY, newCameraY));

        if (cameraX != newCameraX || cameraY != newCameraY) {
            cameraX = newCameraX;
            cameraY = newCameraY;
            System.out.println("  centerCameraOnEnemy: враг [" + enemy.gridX + "," + enemy.gridY +
                    "], центр=" + enemyScreenX + "," + enemyScreenY +
                    ", новая камера=(" + cameraX + "," + cameraY + ")");
        }
    }

    private void drawImprovedHUD(Graphics2D g) {
        int panelWidth = 230;
        int panelHeight = 70;
        int margin = 10;

        // Полупрозрачный фон
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(margin, margin, panelWidth, panelHeight, 15, 15);

        // Рамка в зависимости от режима стрельбы
        if (isAimingMode) {
            g.setColor(aimingModeColor);
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(margin, margin, panelWidth, panelHeight, 15, 15);
        } else {
            g.setColor(burstModeColor);
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(margin, margin, panelWidth, panelHeight, 15, 15);
        }

        // === ПОРТРЕТ ===
        if (heroPortrait != null) {
            Image scaledPortrait = heroPortrait.getScaledInstance(55, 55, Image.SCALE_SMOOTH);
            g.drawImage(scaledPortrait, margin + 7, margin + 8, 55, 55, null);
        } else {
            g.setColor(new Color(100, 100, 150));
            g.fillRoundRect(margin + 7, margin + 8, 55, 55, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("", margin + 27, margin + 45);
        }

        // === ИМЯ ===
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(new Color(255, 215, 0));
        g.drawString("Leichttraktor", margin + 70, margin + 18);

        // === ЗДОРОВЬЕ ===
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(Color.WHITE);
        g.drawString("", margin + 70, margin + 35);
        g.setFont(new Font("Arial", Font.PLAIN, 9));
        g.drawString("ЗДОРОВЬЕ", margin + 82, margin + 35);

        // Полоска здоровья
        int healthBarWidth = 100;
        int healthBarHeight = 8;
        g.setColor(new Color(60, 60, 60));
        g.fillRect(margin + 70, margin + 38, healthBarWidth, healthBarHeight);

        if (player.breadDebuffRemainingTurns > 0) {
            g.setColor(new Color(150, 100, 50, 220));
            g.fillRoundRect(margin + 70, margin + 70, 100, 18, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 8));
            g.drawString("🍞 Отравление", margin + 72, margin + 79);
            g.drawString(player.breadDebuffRemainingTurns + "/3 ходов", margin + 72, margin + 86);
        }

        float healthPercent = (float)player.health / player.maxHealth;
        if (healthPercent > 0.6f) g.setColor(new Color(0, 200, 0));
        else if (healthPercent > 0.3f) g.setColor(new Color(255, 200, 0));
        else g.setColor(new Color(200, 0, 0));
        g.fillRect(margin + 70, margin + 38, (int)(healthBarWidth * healthPercent), healthBarHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 8));
        g.drawString(player.health + "/" + player.maxHealth, margin + 70 + healthBarWidth - 38, margin + 47);

        // === ОЧКИ ХОДА ===
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(Color.WHITE);
        g.drawString("", margin + 70, margin + 55);
        g.setFont(new Font("Arial", Font.PLAIN, 9));
        g.drawString("ОЧКИ ХОДА", margin + 82, margin + 55);

        // Полоска очков хода
        int movePointsWidth = 100;
        int movePointsHeight = 8;
        g.setColor(new Color(60, 60, 60));
        g.fillRect(margin + 70, margin + 58, movePointsWidth, movePointsHeight);

        float movePercent = (float)player.movePoints / player.maxMovePoints;
        g.setColor(new Color(50, 100, 200));
        g.fillRect(margin + 70, margin + 58, (int)(movePointsWidth * movePercent), movePointsHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 8));
        g.drawString(player.movePoints + "/" + player.maxMovePoints, margin + 70 + movePointsWidth - 42, margin + 67);

        int partsY = margin + 88;
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.setColor(new Color(255, 215, 0));
        g.drawString("💰 " + player.getSilver(), margin + 70, partsY);
        g.setColor(new Color(200, 200, 255));
        g.drawString("⚙️ " + player.getParts(), margin + 70, partsY + 12);

        // ===== ПЕРЕГРУЗ - ТЕПЕРЬ СПРАВА ОТ ПОРТРЕТА =====
        if (player.isOverweight()) {
            // Прямоугольник справа от портрета (после имени и полосок)
            int overloadX = margin + 70 + 105; // X = начало имени + ширина полосок + отступ
            int overloadY = margin + 15;       // Y на уровне имени
            int overloadWidth = 50;
            int overloadHeight = 70;

            g.setColor(new Color(255, 50, 50, 230));
            g.fillRoundRect(overloadX, overloadY, overloadWidth, overloadHeight, 8, 8);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("⚠️", overloadX + 20, overloadY + 20);
            g.setFont(new Font("Arial", Font.BOLD, 8));
            g.drawString("ПЕРЕ", overloadX + 12, overloadY + 38);
            g.drawString("ГРУЗ", overloadX + 12, overloadY + 50);

            // Вес рядом с пиктограммой перегруза
            g.setFont(new Font("Arial", Font.PLAIN, 7));
            g.drawString(String.format("%.1f", player.getInventory().getTotalWeight()) + "/" +
                    String.format("%.1f", player.maxCarryWeight), overloadX + 6, overloadY + 65);
        }

        // === ПОДСКАЗКИ ПО УПРАВЛЕНИЮ ===
        int bottomY = getHeight() - 40;
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(margin, bottomY, getWidth() - margin * 2, 30, 10, 10);
        g.setFont(new Font("Arial", Font.PLAIN, 9));
        g.setColor(Color.LIGHT_GRAY);
        String controls = "WASD - движение | ЛКМ - цель | ПРОБЕЛ - выстрел | F - режим | R - сброс | E - ход | I - инвентарь | TAB - смена";
        int textWidth = g.getFontMetrics().stringWidth(controls);
        g.drawString(controls, (getWidth() - textWidth) / 2, bottomY + 19);
    }

    // Отрисовка панелей всех нанятых союзников (ПОЛНАЯ КОПИЯ СТИЛЯ drawImprovedHUD)
    // Отрисовка панелей всех нанятых союзников (ТОЧНАЯ КОПИЯ drawImprovedHUD)
    // Отрисовка панелей всех нанятых союзников (ТОЧНАЯ КОПИЯ СТИЛЯ drawImprovedHUD)
    // Отрисовка панелей всех нанятых союзников (ТОЧНАЯ КОПИЯ drawImprovedHUD)
    // Отрисовка панелей всех нанятых союзников (ТОЧНАЯ КОПИЯ drawImprovedHUD)
    private void drawAlliedUnitsPanels(Graphics2D g) {
        if (recruitedUnits.isEmpty()) {
            return;
        }

        int panelWidth = 230;
        int panelHeight = 70;
        int margin = 10;
        int startY = margin + 70;

        for (int i = 0; i < recruitedUnits.size(); i++) {
            FriendlyUnit unit = recruitedUnits.get(i);
            if (!unit.isAlive) continue;

            int panelY = startY + (i * (panelHeight + 5));

            // Полупрозрачный фон
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRoundRect(margin, panelY, panelWidth, panelHeight, 15, 15);

            // Рамка
            if (isAimingMode) {
                g.setColor(aimingModeColor);
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(margin, panelY, panelWidth, panelHeight, 15, 15);
            } else {
                g.setColor(burstModeColor);
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(margin, panelY, panelWidth, panelHeight, 15, 15);
            }

            // === ПОРТРЕТ ===
            BufferedImage portrait = null;
            if ("M53".equals(unit.type)) portrait = m53Portrait;
            else if ("MS-1".equals(unit.type)) portrait = ms1Portrait;
            else if ("VK10001P".equals(unit.type)) portrait = vk10001pPortrait;
            else if ("AMX40".equals(unit.type)) portrait = amx40Portrait;
            else if ("T1".equals(unit.type)) portrait = t1Portrait;

            if (portrait != null) {
                Image scaledPortrait = portrait.getScaledInstance(55, 55, Image.SCALE_SMOOTH);
                g.drawImage(scaledPortrait, margin + 7, panelY + 8, 55, 55, null);
            } else {
                g.setColor(new Color(100, 100, 150));
                g.fillRoundRect(margin + 7, panelY + 8, 55, 55, 10, 10);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString(unit.type.equals("M53") ? "M" : "M", margin + 27, panelY + 45);
            }

            // === ИМЯ ===
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(new Color(255, 215, 0));
            g.drawString(unit.name, margin + 70, panelY + 18);

            // === ЗДОРОВЬЕ ===
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.setColor(Color.WHITE);
            g.drawString("", margin + 70, panelY + 35);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.drawString("ЗДОРОВЬЕ", margin + 82, panelY + 35);

            // Полоска здоровья
            int healthBarWidth = 100;
            int healthBarHeight = 8;
            g.setColor(new Color(60, 60, 60));
            g.fillRect(margin + 70, panelY + 38, healthBarWidth, healthBarHeight);

            float healthPercent = (float)unit.health / unit.maxHealth;
            if (healthPercent > 0.6f) g.setColor(new Color(0, 200, 0));
            else if (healthPercent > 0.3f) g.setColor(new Color(255, 200, 0));
            else g.setColor(new Color(200, 0, 0));
            g.fillRect(margin + 70, panelY + 38, (int)(healthBarWidth * healthPercent), healthBarHeight);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 8));
            g.drawString(unit.health + "/" + unit.maxHealth, margin + 70 + healthBarWidth - 38, panelY + 47);

            // === ОЧКИ ХОДА ===
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.setColor(Color.WHITE);
            g.drawString("", margin + 70, panelY + 55);
            g.setFont(new Font("Arial", Font.PLAIN, 9));
            g.drawString("ОЧКИ ХОДА", margin + 82, panelY + 55);

            // Полоска очков хода
            int movePointsWidth = 100;
            int movePointsHeight = 8;
            g.setColor(new Color(60, 60, 60));
            g.fillRect(margin + 70, panelY + 58, movePointsWidth, movePointsHeight);

            float movePercent = (float)unit.movePoints / unit.maxMovePoints;
            g.setColor(new Color(50, 100, 200));
            g.fillRect(margin + 70, panelY + 58, (int)(movePointsWidth * movePercent), movePointsHeight);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 8));
            g.drawString(unit.movePoints + "/" + unit.maxMovePoints, margin + 70 + movePointsWidth - 42, panelY + 67);

            // ===== ПЕРЕГРУЗ ДЛЯ СОЮЗНИКА - СПРАВА ОТ ПОРТРЕТА =====
            if (unit.isOverweight()) {
                int overloadX = margin + 70 + 105;
                int overloadY = panelY + 15;
                int overloadWidth = 50;
                int overloadHeight = 55;

                g.setColor(new Color(255, 50, 50, 230));
                g.fillRoundRect(overloadX, overloadY, overloadWidth, overloadHeight, 8, 8);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString("⚠️", overloadX + 20, overloadY + 18);
                g.setFont(new Font("Arial", Font.BOLD, 8));
                g.drawString("ПЕРЕ", overloadX + 12, overloadY + 33);
                g.drawString("ГРУЗ", overloadX + 12, overloadY + 45);

                // Вес
                g.setFont(new Font("Arial", Font.PLAIN, 7));
                g.drawString(String.format("%.1f", unit.getInventory().getTotalWeight()) + "/" +
                        String.format("%.1f", unit.maxCarryWeight), overloadX + 6, overloadY + 53);
            }

            // === ПОДСВЕТКА АКТИВНОГО ЮНИТА ===
            if (!isPlayerActive() && getActiveFriendly() == unit) {
                g.setColor(new Color(100, 200, 255, 60));
                g.fillRoundRect(margin, panelY, panelWidth, panelHeight, 15, 15);
            }
        }
    }

    // Обновление списка видимых врагов от текущего активного юнита
    // В GamePanel.java, замените метод updateVisibleEnemies:

    // В GamePanel.java, добавьте отладочный вывод в updateVisibleEnemies:
    // В GamePanel.java, замените метод updateVisibleEnemies:
    private void updateVisibleEnemies() {
        visibleEnemies.clear();

        if (isPlayerActive()) {
            for (Enemy enemy : world.getEnemies()) {
                if (enemy.isAlive && world.isEnemyVisible(enemy)) {
                    visibleEnemies.add(enemy);
                }
            }
            System.out.println("updateVisibleEnemies (Player): найдено " + visibleEnemies.size() + " врагов");
        } else {
            FriendlyUnit active = getActiveFriendly();
            if (active != null) {
                System.out.println("updateVisibleEnemies для " + active.name +
                        " - используем ОБЩУЮ видимость команды");

                // ===== ИСПРАВЛЕНИЕ: используем видимость от ВСЕЙ КОМАНДЫ =====
                for (Enemy enemy : world.getEnemies()) {
                    if (enemy.isAlive && world.isEnemyVisibleByTeam(enemy)) {
                        visibleEnemies.add(enemy);
                        System.out.println("  Виден враг: " + enemy.type + " [" + enemy.gridX + "," + enemy.gridY + "]");
                    }
                }
                System.out.println("  ИТОГО видимых врагов (команда): " + visibleEnemies.size());
            }
        }

        if (visibleEnemies.isEmpty()) {
            isCameraOnEnemy = false;
            currentEnemyIndex = -1;
        } else if (currentEnemyIndex >= visibleEnemies.size()) {
            currentEnemyIndex = 0;
        }
    }

    // Переключение камеры на следующего видимого врага
    // В GamePanel.java, замените метод cycleToNextVisibleEnemy:

    private void cycleToNextVisibleEnemy() {
        updateVisibleEnemies();

        if (visibleEnemies.isEmpty()) {
            String message;
            if (isPlayerActive()) {
                message = "Нет видимых врагов!";
            } else {
                FriendlyUnit active = getActiveFriendly();
                message = active != null ?
                        active.name + " не видит ни одного врага!" :
                        "Нет активного юнита!";
            }
            JOptionPane.showMessageDialog(this,
                    message,
                    "Информация",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Переключаемся на следующего врага
        currentEnemyIndex = (currentEnemyIndex + 1) % visibleEnemies.size();
        isCameraOnEnemy = true;

        Enemy targetEnemy = visibleEnemies.get(currentEnemyIndex);
        centerCameraOnUnit(targetEnemy.gridX, targetEnemy.gridY);

        System.out.println("📷 Камера переключена на врага: " + targetEnemy.type +
                " [" + targetEnemy.gridX + "," + targetEnemy.gridY + "]");

        // Показываем всплывающую подсказку
        showEnemyInfoTooltip(targetEnemy);
    }

    // Показ информации о враге
    private void showEnemyInfoTooltip(Enemy enemy) {
        // Создаём временную метку для отображения информации
        JLabel infoLabel = new JLabel("👾 " + enemy.type + " | ❤️ " + enemy.health + "/" + enemy.maxHealth);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        infoLabel.setForeground(Color.RED);
        infoLabel.setBackground(new Color(0, 0, 0, 200));
        infoLabel.setOpaque(true);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Позиционируем в правом верхнем углу
        infoLabel.setBounds(getWidth() - 250, 10, 240, 30);
        add(infoLabel);

        // Удаляем через 2 секунды
        javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
            remove(infoLabel);
            repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    // Возврат камеры к активному юниту
    private void returnCameraToActiveUnit() {
        isCameraOnEnemy = false;
        currentEnemyIndex = -1;

        if (isPlayerActive()) {
            centerCameraOnUnit(player.gridX, player.gridY);
            System.out.println("📷 Камера возвращена к игроку");
        } else {
            FriendlyUnit active = getActiveFriendly();
            if (active != null) {
                centerCameraOnUnit(active.gridX, active.gridY);
                System.out.println("📷 Камера возвращена к " + active.name);
            }
        }
    }

    // В методе updateQuestProgress (или где обрабатывается смерть врага):
    private void dropLootFromEnemy(Enemy enemy) {
        Random rand = new Random();
        Inventory inv = player.getInventory();

        // Снаряды (80% шанс)
        if (rand.nextDouble() < 0.8) {
            int ammoDrop = 5 + rand.nextInt(11);
            inv.addItem(Item.ItemType.AMMO, ammoDrop);
            System.out.println("📦 Получено снарядов: " + ammoDrop);
        }

        // Аптечка (15% шанс)
        if (rand.nextDouble() < 0.15) {
            inv.addItem(Item.ItemType.MEDKIT, 1);
            System.out.println("💊 Найдена аптечка!");
        }

        // Бинт (25% шанс)
        if (rand.nextDouble() < 0.25) {
            inv.addItem(Item.ItemType.BANDAGE, 1);
            System.out.println("🩹 Найден бинт!");
        }

        // Энергетик (10% шанс)
        if (rand.nextDouble() < 0.1) {
            inv.addItem(Item.ItemType.ENERGY_DRINK, 1);
            System.out.println("⚡ Найден энергетик!");
        }

        // Огнетушитель (5% шанс)
        if (rand.nextDouble() < 0.05) {
            inv.addItem(Item.ItemType.FIRE_EXTINGUISHER, 1);
            System.out.println("🧯 Найден огнетушитель!");
        }
    }

    // В GamePanel.java, в drawEnemyHoverHighlight, замените:
    private void drawEnemyHoverHighlight(Graphics2D g) {
        if (hoveredEnemy != null && hoveredEnemy.isAlive) {

            // ===== ПРОВЕРКА ВИДИМОСТИ ОТ КОМАНДЫ =====
            boolean isVisible = world.isEnemyVisibleByTeam(hoveredEnemy);
            if (!isVisible) return;

            int x = hoveredEnemy.gridX * GameWorld.CELL_SIZE - cameraX;
            int y = hoveredEnemy.gridY * GameWorld.CELL_SIZE - cameraY;

            // Подсветка врага
            if (isAimingMode) {
                g.setColor(new Color(255, 100, 0, 100));
            } else {
                g.setColor(new Color(0, 150, 200, 100));
            }
            g.fillRect(x, y, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);

            // Рамка
            g.setColor(isAimingMode ? new Color(255, 100, 0, 200) : new Color(0, 150, 200, 200));
            g.setStroke(new BasicStroke(3));
            g.drawRect(x, y, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);

            // Информационная панель над врагом
            int infoX = x + GameWorld.CELL_SIZE / 2;
            int infoY = y - 30;

            g.setColor(new Color(0, 0, 0, 200));
            g.fillRoundRect(infoX - 60, infoY - 5, 120, 25, 8, 8);

            // Расчёт шанса и стоимости
            double hitChance;
            int shotCost;

            if (isPlayerActive()) {
                hitChance = calculateHitProbability(hoveredEnemy.gridX, hoveredEnemy.gridY);
                shotCost = isAimingMode ? weapon.aimedShotCost : weapon.burstShotCost;
            } else {
                FriendlyUnit active = getActiveFriendly();
                if (active == null) return;
                hitChance = calculateHitProbabilityForFriendly(active, hoveredEnemy.gridX, hoveredEnemy.gridY);
                shotCost = isAimingMode ? active.aimedShotCost : active.shotCost;
            }

            int hitPercent = (int)(hitChance * 100);

            g.setFont(new Font("Arial", Font.BOLD, 11));
            if (hitPercent > 70) g.setColor(new Color(0, 255, 0));
            else if (hitPercent > 40) g.setColor(new Color(255, 200, 0));
            else g.setColor(new Color(255, 50, 50));
            g.drawString("Шанс: " + hitPercent + "%", infoX - 35, infoY + 10);

            g.setColor(Color.CYAN);
            g.drawString("Стоимость: " + shotCost + " о.х.", infoX + 15, infoY + 10);
        }
    }

    // В GamePanel.java, добавьте метод drawEnemyCounter:

    private void drawEnemyCounter(Graphics2D g) {
        // Получаем количество видимых врагов от всей команды
        int visibleEnemiesCount = 0;
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isAlive && world.isEnemyVisibleByTeam(enemy)) {
                visibleEnemiesCount++;
            }
        }

        if (visibleEnemiesCount == 0) return;

        // Позиция в нижнем правом углу
        int size = 60;
        int x = getWidth() - size - 15;
        int y = getHeight() - size - 15;

        // Красный квадрат с закруглёнными углами
        g.setColor(new Color(180, 0, 0, 220));
        g.fillRoundRect(x, y, size, size, 15, 15);

        // Жёлтая рамка
        g.setColor(new Color(255, 215, 0));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(x, y, size, size, 15, 15);

        // Белая тень для цифры
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String countText = String.valueOf(visibleEnemiesCount);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(countText);
        int textHeight = fm.getHeight();

        // Рисуем цифру жёлтым цветом
        g.setColor(new Color(255, 215, 0));
        g.drawString(countText, x + (size - textWidth) / 2, y + (size + textHeight) / 2 - 5);

        // Иконка врага сверху
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("👾", x + size - 22, y + 18);
    }

    // ДОБАВЬТЕ МЕТОД для отрисовки прицела на враге (если выбран)
    private void drawTargetingCrosshair(Graphics2D g) {
        if (targetShotX != -1 && targetShotY != -1) {
            int targetX = targetShotX * GameWorld.CELL_SIZE - cameraX;
            int targetY = targetShotY * GameWorld.CELL_SIZE - cameraY;
            int centerX = targetX + GameWorld.CELL_SIZE / 2;
            int centerY = targetY + GameWorld.CELL_SIZE / 2;

            // Внешнее свечение
            g.setColor(isAimingMode ? new Color(255, 100, 0, 100) : new Color(0, 150, 200, 100));
            g.fillOval(centerX - 25, centerY - 25, 50, 50);

            // Прицел
            g.setColor(isAimingMode ? new Color(255, 100, 0, 255) : new Color(0, 150, 200, 255));
            g.setStroke(new BasicStroke(2));

            // Крест
            g.drawLine(centerX - 20, centerY, centerX - 8, centerY);
            g.drawLine(centerX + 8, centerY, centerX + 20, centerY);
            g.drawLine(centerX, centerY - 20, centerX, centerY - 8);
            g.drawLine(centerX, centerY + 8, centerX, centerY + 20);

            // Круг
            g.drawOval(centerX - 12, centerY - 12, 24, 24);

            // Ромб в центре
            int[] xPoints = {centerX, centerX + 4, centerX, centerX - 4};
            int[] yPoints = {centerY - 4, centerY, centerY + 4, centerY};
            g.fillPolygon(xPoints, yPoints, 4);

            // Текст с информацией - тоже нужно исправить!
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.setColor(Color.WHITE);
            g.drawString(isAimingMode ? "ПРИЦЕЛЬНЫЙ" : "БЕГЛЫЙ", centerX - 30, centerY - 28);

            // Стоимость выстрела
            // Стоимость выстрела - зависит от активного юнита!
            int shotCost;
            if (isPlayerActive()) {
                shotCost = isAimingMode ? weapon.aimedShotCost : weapon.burstShotCost;
            } else {
                FriendlyUnit active = getActiveFriendly();
                shotCost = (active != null) ? (isAimingMode ? active.aimedShotCost : active.shotCost) : 0;
            }

            g.setColor(Color.YELLOW);
            g.drawString("Цена: " + shotCost + " о.х.", centerX - 25, centerY + 32);
        }
    }

    // В drawFriendlyPath() обновите отображение информации:

    private void drawFriendlyPath(Graphics2D g) {
        if (friendlyPath.isEmpty() || movingFriendly == null) return;

        if (isPlayerActive() || getActiveFriendly() != movingFriendly) return;

        g.setColor(new Color(0, 200, 255, 150));
        g.setStroke(new BasicStroke(4));

        Point prev = null;
        for (Point cell : friendlyPath) {
            int cellX = cell.x * GameWorld.CELL_SIZE - cameraX;
            int cellY = cell.y * GameWorld.CELL_SIZE - cameraY;

            g.setColor(new Color(0, 200, 255, 60));
            g.fillRect(cellX, cellY, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);

            if (prev != null) {
                g.setColor(new Color(0, 200, 255, 200));
                int fromX = prev.x * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE/2 - cameraX;
                int fromY = prev.y * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE/2 - cameraY;
                int toX = cell.x * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE/2 - cameraX;
                int toY = cell.y * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE/2 - cameraY;
                g.drawLine(fromX, fromY, toX, toY);
                drawArrow(g, fromX, fromY, toX, toY);
            }
            prev = cell;
        }

        if (!friendlyPath.isEmpty()) {
            Point target = friendlyPath.get(friendlyPath.size() - 1);
            int targetX = target.x * GameWorld.CELL_SIZE - cameraX;
            int targetY = target.y * GameWorld.CELL_SIZE - cameraY;

            g.setColor(new Color(0, 200, 255, 100));
            g.fillRect(targetX, targetY, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);
            g.setColor(new Color(0, 150, 200));
            g.setStroke(new BasicStroke(3));
            g.drawRect(targetX, targetY, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);
        }

        int totalCost = friendlyPath.size() * movingFriendly.moveCost;
        int remainingPoints = movingFriendly.movePoints;

        if (!friendlyPath.isEmpty()) {
            Point firstCell = friendlyPath.get(0);
            int firstX = firstCell.x * GameWorld.CELL_SIZE - cameraX;
            int firstY = firstCell.y * GameWorld.CELL_SIZE - cameraY;

            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(firstX, firstY - 25, 160, 22, 8, 8);

            g.setColor(Color.CYAN);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("Стоимость: " + totalCost + " ⭐", firstX + 5, firstY - 10);

            if (remainingPoints < totalCost) {
                g.setColor(Color.RED);
                int maxSteps = remainingPoints / movingFriendly.moveCost;
                g.drawString(" (хватит на " + maxSteps + " из " + friendlyPath.size() + " шагов)",
                        firstX + 85, firstY - 10);
            } else {
                g.setColor(Color.GREEN);
                g.drawString(" ✅ хватит", firstX + 85, firstY - 10);
            }
        }
    }

    // ДОБАВЬТЕ ИНДИКАТОР РЕЖИМА СТРЕЛЬБЫ В УГЛУ ЭКРАНА
    private void drawFireModeIndicator(Graphics2D g) {
        int x = getWidth() - 210;
        int y = 10;

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, 200, 50, 10, 10);

        if (isAimingMode) {
            g.setColor(aimingModeColor);
            g.fillRoundRect(x + 5, y + 5, 90, 40, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("🎯 ПРИЦЕЛ", x + 15, y + 30);

            g.setColor(new Color(80, 80, 80));
            g.fillRoundRect(x + 100, y + 5, 95, 40, 8, 8);
            g.setColor(Color.GRAY);
            g.drawString("⚡ БЕГЛЫЙ", x + 112, y + 30);
        } else {
            g.setColor(new Color(80, 80, 80));
            g.fillRoundRect(x + 5, y + 5, 90, 40, 8, 8);
            g.setColor(Color.GRAY);
            g.drawString("🎯 ПРИЦЕЛ", x + 15, y + 30);

            g.setColor(burstModeColor);
            g.fillRoundRect(x + 100, y + 5, 95, 40, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("⚡ БЕГЛЫЙ", x + 112, y + 30);
        }

        // Подсказка по смене режима
        g.setFont(new Font("Arial", Font.PLAIN, 9));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Нажми F для смены", x + 55, y + 48);
    }



    // Добавьте новый метод для проверки обнаружения врагов
    // Обновите метод checkEnemyDetection - теперь он должен вызываться и во время хода врагов
    // Временно убираем проверку на isEnemyTurn()
    // В GamePanel.java, в методе checkEnemyDetection, замените блок для союзников:

    // В GamePanel.java, в методе checkEnemyDetection, замените блок для союзников:

    // В GamePanel.java, в checkEnemyDetection, измените:
    public void checkEnemyDetection() {
        int visibleCount = 0;

        if (!isPlayerActive()) {
            FriendlyUnit active = getActiveFriendly();
            if (active != null) {
                // ===== ИСПРАВЛЕНИЕ: считаем врагов, видимых ВСЕЙ КОМАНДОЙ =====
                for (Enemy enemy : world.getEnemies()) {
                    if (enemy.isAlive && world.isEnemyVisibleByTeam(enemy)) {
                        visibleCount++;
                    }
                }

                if (visibleCount != lastVisibleEnemyCount && visibleCount > 0) {
                    long now = System.currentTimeMillis();
                    if (now - lastM53SpottedTime > 5000) {
                        if ("M53".equals(active.type)) {
                            soundManager.playM53SpottedSound(visibleCount);
                        } else if ("MS-1".equals(active.type)) {
                            soundManager.playMS1SpottedSound(visibleCount);
                        } else if ("VK10001P".equals(active.type)) {
                            soundManager.playVK10001PSpottedSound(visibleCount);
                        } else if ("T1".equals(active.type)) {
                            soundManager.playT1SpottedSound(visibleCount);  // ← ДОБАВИТЬ
                        }
                        lastM53SpottedTime = now;
                    }
                }
                lastVisibleEnemyCount = visibleCount;
            }
        } else {
            // Для игрока
            for (Enemy enemy : world.getEnemies()) {
                if (enemy.isAlive && world.isEnemyVisible(enemy)) {
                    visibleCount++;
                }
            }

            if (visibleCount != lastVisibleEnemyCount && visibleCount > 0) {
                soundManager.playEnemySpottedSound(visibleCount);
                lastVisibleEnemyCount = visibleCount;
            }
        }

        if (!isCameraOnEnemy) {
            updateVisibleEnemies();
        }

        // ===== НОВАЯ ЛОГИКА: ЕСЛИ ВИДИМЫХ ВРАГОВ > 0, ПОМЕЧАЕМ ОБНАРУЖЕНИЕ =====
        if (visibleCount > 0) {
            enemyDetectedThisTurn = true;
            System.out.println("👁️ Враги обнаружены в этом ходу! Флаг = true");
        }

        if (!isCameraOnEnemy) {
            updateVisibleEnemies();
        }

        // ===== ОБНОВЛЯЕМ МУЗЫКУ ТОЛЬКО ЕСЛИ НЕ БЫЛО ОБНАРУЖЕНИЯ В ЭТОМ ХОДУ =====

        updateBackgroundMusic();
    }

    private BufferedImage getDoorImage(Door.DoorColor color) {
        if (doorImages == null) return null;
        return doorImages.get(color);
    }

    private void loadImages() {

        try {
            // Загрузка изображений игрока
            String basePath = "src/PositiveHeroes/Leichttraktor/";
            tankRight = ImageIO.read(new File(basePath + "Leichttraktor (вправо).png"));
            tankLeft = ImageIO.read(new File(basePath + "Leichttraktor (влево).png"));
            tankDown = ImageIO.read(new File(basePath + "Leichttraktor (вниз).png"));
            tankUp = ImageIO.read(new File(basePath + "Leichttraktor (вверх).png"));

            // ЗАГРУЗКА ИЗОБРАЖЕНИЙ ВРАГА ИЗ НОВОЙ ПАПКИ
            String enemyPath = "src/Enemies/Leichttraktor/Movement/";

            // Сначала пробуем загрузить обычные изображения
            BufferedImage enemyRightOriginal = null;
            BufferedImage enemyLeftOriginal = null;
            BufferedImage enemyUpOriginal = null;
            BufferedImage enemyDownOriginal = null;

            try {
                enemyRightOriginal = ImageIO.read(new File(enemyPath + "Leichttraktor (вправо).png"));
                enemyLeftOriginal = ImageIO.read(new File(enemyPath + "Leichttraktor (влево).png"));
                enemyDownOriginal = ImageIO.read(new File(enemyPath + "Leichttraktor (вниз).png"));
                enemyUpOriginal = ImageIO.read(new File(enemyPath + "Leichttraktor (вверх).png"));
                System.out.println("Изображения врага загружены из папки Movement");
            } catch (IOException e) {
                System.err.println("Не удалось загрузить изображения врага из Movement, используются tint-версии");
                // Если не загрузились, используем tint-версии танка игрока
                enemyRightOriginal = tankRight;
                enemyLeftOriginal = tankLeft;
                enemyUpOriginal = tankUp;
                enemyDownOriginal = tankDown;
            }

// В методе loadImages() добавьте:
            try {
                String rOtsuPath = "src/Enemies/R_Otsu/Movement/";
                rOtsuRight = ImageIO.read(new File(rOtsuPath + "R_Otsu (вправо).png"));
                rOtsuLeft = ImageIO.read(new File(rOtsuPath + "R_Otsu (влево).png"));
                rOtsuUp = ImageIO.read(new File(rOtsuPath + "R_Otsu (вверх).png"));
                rOtsuDown = ImageIO.read(new File(rOtsuPath + "R_Otsu (вниз).png"));

                // ===== СОЗДАЁМ КРАСНЫЕ ВЕРСИИ ДЛЯ ВРАГОВ =====
                rOtsuEnemyRight = tintImage(rOtsuRight, new Color(200, 0, 0, 100));
                rOtsuEnemyLeft = tintImage(rOtsuLeft, new Color(200, 0, 0, 100));
                rOtsuEnemyUp = tintImage(rOtsuUp, new Color(200, 0, 0, 100));
                rOtsuEnemyDown = tintImage(rOtsuDown, new Color(200, 0, 0, 100));

                System.out.println("✅ Изображения R.Otsu загружены (включая вражеские версии)");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки R.Otsu: " + e.getMessage());
                // Создаём заглушки
                rOtsuRight = createPlaceholderTank("R→");
                rOtsuLeft = createPlaceholderTank("R←");
                rOtsuUp = createPlaceholderTank("R↑");
                rOtsuDown = createPlaceholderTank("R↓");
                rOtsuEnemyRight = createPlaceholderTank("R→");
                rOtsuEnemyLeft = createPlaceholderTank("R←");
                rOtsuEnemyUp = createPlaceholderTank("R↑");
                rOtsuEnemyDown = createPlaceholderTank("R↓");
            }

            // Загрузка изображений для M14_41
            try {
                String m14_41Path = "src/Enemies/M14_41/Movement/";
                m14_41Right = ImageIO.read(new File(m14_41Path + "M14_41 (вправо).png"));
                m14_41Left = ImageIO.read(new File(m14_41Path + "M14_41 (влево).png"));
                m14_41Up = ImageIO.read(new File(m14_41Path + "M14_41 (вверх).png"));
                m14_41Down = ImageIO.read(new File(m14_41Path + "M14_41 (вниз).png"));

                // Создаём tint-версии для врагов (красный оттенок)
                m14_41EnemyRight = tintImage(m14_41Right, new Color(200, 0, 0, 100));
                m14_41EnemyLeft = tintImage(m14_41Left, new Color(200, 0, 0, 100));
                m14_41EnemyUp = tintImage(m14_41Up, new Color(200, 0, 0, 100));
                m14_41EnemyDown = tintImage(m14_41Down, new Color(200, 0, 0, 100));

                System.out.println("✅ Изображения M14_41 загружены");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки M14_41: " + e.getMessage());
                // Создаём заглушки
                m14_41Right = createPlaceholderTank("M→");
                m14_41Left = createPlaceholderTank("M←");
                m14_41Up = createPlaceholderTank("M↑");
                m14_41Down = createPlaceholderTank("M↓");
                m14_41EnemyRight = createPlaceholderTank("M→");
                m14_41EnemyLeft = createPlaceholderTank("M←");
                m14_41EnemyUp = createPlaceholderTank("M↑");
                m14_41EnemyDown = createPlaceholderTank("M↓");
            }

            try {
                String h35Path = "src/Enemies/H35/Movement/";
                h35Right = ImageIO.read(new File(h35Path + "H35 (вправо).png"));
                h35Left = ImageIO.read(new File(h35Path + "H35 (влево).png"));
                h35Up = ImageIO.read(new File(h35Path + "H35 (вверх).png"));
                h35Down = ImageIO.read(new File(h35Path + "H35 (вниз).png"));

                // Создаём tint-версии для врагов (красный оттенок)
                h35EnemyRight = tintImage(h35Right, new Color(200, 0, 0, 100));
                h35EnemyLeft = tintImage(h35Left, new Color(200, 0, 0, 100));
                h35EnemyUp = tintImage(h35Up, new Color(200, 0, 0, 100));
                h35EnemyDown = tintImage(h35Down, new Color(200, 0, 0, 100));

                System.out.println("✅ Изображения H35 загружены");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки H35: " + e.getMessage());
                // Создаём заглушки
                h35Right = createPlaceholderTank("H→");
                h35Left = createPlaceholderTank("H←");
                h35Up = createPlaceholderTank("H↑");
                h35Down = createPlaceholderTank("H↓");
                h35EnemyRight = createPlaceholderTank("H→");
                h35EnemyLeft = createPlaceholderTank("H←");
                h35EnemyUp = createPlaceholderTank("H↑");
                h35EnemyDown = createPlaceholderTank("H↓");
            }

            // В методе loadImages(), после загрузки H35, добавьте:
            try {
                String ftPath = "src/Enemies/FT/Movement/";
                BufferedImage ftRight = ImageIO.read(new File(ftPath + "FT (вправо).png"));
                BufferedImage ftLeft = ImageIO.read(new File(ftPath + "FT (влево).png"));
                BufferedImage ftUp = ImageIO.read(new File(ftPath + "FT (вверх).png"));
                BufferedImage ftDown = ImageIO.read(new File(ftPath + "FT (вниз).png"));

                // Создаём tint-версии для врагов
                ftEnemyRight = tintImage(ftRight, new Color(200, 0, 0, 100));
                ftEnemyLeft = tintImage(ftLeft, new Color(200, 0, 0, 100));
                ftEnemyUp = tintImage(ftUp, new Color(200, 0, 0, 100));
                ftEnemyDown = tintImage(ftDown, new Color(200, 0, 0, 100));

                System.out.println("✅ Изображения FT загружены");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки FT: " + e.getMessage());
                // Создаём заглушки
                ftRight = createPlaceholderTank("F→");
                ftLeft = createPlaceholderTank("F←");
                ftUp = createPlaceholderTank("F↑");
                ftDown = createPlaceholderTank("F↓");
                ftEnemyRight = createPlaceholderTank("F→");
                ftEnemyLeft = createPlaceholderTank("F←");
                ftEnemyUp = createPlaceholderTank("F↑");
                ftEnemyDown = createPlaceholderTank("F↓");
            }

            try {
                String fiat3000Path = "src/Enemies/Fiat3000/Movement/";
                BufferedImage fiat3000Right = ImageIO.read(new File(fiat3000Path + "Fiat 3000 (вправо).png"));
                BufferedImage fiat3000Left = ImageIO.read(new File(fiat3000Path + "Fiat 3000 (влево).png"));
                BufferedImage fiat3000Up = ImageIO.read(new File(fiat3000Path + "Fiat 3000 (вверх).png"));
                BufferedImage fiat3000Down = ImageIO.read(new File(fiat3000Path + "Fiat 3000 (вниз).png"));

                // Создаём tint-версии для врагов (красный оттенок)
                fiat3000EnemyRight = tintImage(fiat3000Right, new Color(200, 0, 0, 100));
                fiat3000EnemyLeft = tintImage(fiat3000Left, new Color(200, 0, 0, 100));
                fiat3000EnemyUp = tintImage(fiat3000Up, new Color(200, 0, 0, 100));
                fiat3000EnemyDown = tintImage(fiat3000Down, new Color(200, 0, 0, 100));

                System.out.println("✅ Изображения Fiat3000 загружены");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки Fiat3000: " + e.getMessage());
                // Создаём заглушки
                fiat3000Right = createPlaceholderTank("F→");
                fiat3000Left = createPlaceholderTank("F←");
                fiat3000Up = createPlaceholderTank("F↑");
                fiat3000Down = createPlaceholderTank("F↓");
                fiat3000EnemyRight = createPlaceholderTank("F→");
                fiat3000EnemyLeft = createPlaceholderTank("F←");
                fiat3000EnemyUp = createPlaceholderTank("F↑");
                fiat3000EnemyDown = createPlaceholderTank("F↓");
            }

            try {
                String wallPath = "src/Obstacles/Brick_wall.png";
                File wallFile = new File(wallPath);
                if (wallFile.exists()) {
                    wallImage = ImageIO.read(wallFile);
                    System.out.println("Изображение стены загружено");
                } else {
                    System.err.println("Файл стены не найден: " + wallPath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки стены: " + e.getMessage());
            }

            // ===== ЗАГРУЗКА IRON BLOCK =====
            try {
                String ironPath = "src/Obstacles/IronBlock.png";
                File ironFile = new File(ironPath);
                if (ironFile.exists()) {
                    ironBlockImage = ImageIO.read(ironFile);
                    System.out.println("✅ Изображение IronBlock загружено");
                } else {
                    System.err.println("❌ Файл IronBlock не найден: " + ironPath);
                    ironBlockImage = null;
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки IronBlock: " + e.getMessage());
                ironBlockImage = null;
            }

            // Загрузка изображений деревьев
            try {
                String treePath = "src/Obstacles/";
                tree1Image = ImageIO.read(new File(treePath + "Tree_1.png"));
                tree2Image = ImageIO.read(new File(treePath + "Tree_2.png"));
                System.out.println("Изображения деревьев загружены");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки деревьев: " + e.getMessage());
                tree1Image = null;
                tree2Image = null;
            }

            try {
                String pavementPath = "src/Obstacles/pavement.png";
                pavementImage = ImageIO.read(new File(pavementPath));
                System.out.println("Изображение асфальта загружено");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки асфальта: " + e.getMessage());
                pavementImage = null;
            }

            try {
                String oakPlanksPath = "src/Obstacles/Oak_planks.png";
                oakPlanksImage = ImageIO.read(new File(oakPlanksPath));
                System.out.println("Изображение дубовых досок загружено");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки дубовых досок: " + e.getMessage());
                oakPlanksImage = null;
            }

            // ===== ЗАГРУЗКА IRON FLOOR =====
            try {
                String ironFloorPath = "src/Obstacles/IronFloor.png";
                File ironFloorFile = new File(ironFloorPath);
                if (ironFloorFile.exists()) {
                    ironFloorImage = ImageIO.read(ironFloorFile);
                    System.out.println("✅ Изображение IronFloor загружено, размер: " +
                            ironFloorImage.getWidth() + "x" + ironFloorImage.getHeight());  // ← ДОБАВИТЬ
                } else {
                    System.err.println("❌ Файл IronFloor не найден: " + ironFloorPath);
                    ironFloorImage = null;
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки IronFloor: " + e.getMessage());
                ironFloorImage = null;
            }

            try {
                String infernalLandPath = "src/Obstacles/InfernalLand.png";
                File infernalLandFile = new File(infernalLandPath);
                if (infernalLandFile.exists()) {
                    infernalLandImage = ImageIO.read(infernalLandFile);
                    System.out.println("Изображение адской земли загружено");
                } else {
                    System.err.println("Файл адской земли не найден: " + infernalLandPath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки адской земли: " + e.getMessage());
            }
            // Для воды
            try {
                String waterPath = "src/Obstacles/Water.png";
                File waterFile = new File(waterPath);
                if (waterFile.exists()) {
                    waterImage = ImageIO.read(waterFile);
                    System.out.println("✅ Изображение воды загружено");
                } else {
                    System.err.println("❌ Файл воды не найден: " + waterPath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки воды: " + e.getMessage());
            }

            // Загрузка изображений дверей
            doorImages = new HashMap<>();
            String doorPath = "src/Obstacles/";
            for (Door.DoorColor color : Door.DoorColor.values()) {
                try {
                    File doorFile = new File(doorPath + color.fileName);
                    if (doorFile.exists()) {
                        doorImages.put(color, ImageIO.read(doorFile));
                        System.out.println("✅ Загружена дверь: " + color.fileName);
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка загрузки двери " + color.fileName + ": " + e.getMessage());
                }
            }

            // В loadImages():
            try {
                String chestPath = "src/Obstacles/BedsideTable.png";
                bedsideTableImage = ImageIO.read(new File(chestPath));
                System.out.println("Изображение тумбочки загружено");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки тумбочки: " + e.getMessage());
                bedsideTableImage = null;
            }

            try {
                String dumpsterPath = "src/Obstacles/Dumpster.png";
                File dumpsterFile = new File(dumpsterPath);
                if (dumpsterFile.exists()) {
                    dumpsterImage = ImageIO.read(dumpsterFile);
                    System.out.println("✅ Изображение мусорного контейнера загружено");
                } else {
                    System.err.println("❌ Файл мусорного контейнера не найден: " + dumpsterPath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки мусорного контейнера: " + e.getMessage());
            }

            try {
                String npcPath = "src/QuestPersons/T18/Hero/T18.png";
                File npcFile = new File(npcPath);
                if (npcFile.exists()) {
                    npcImage = ImageIO.read(npcFile);
                    System.out.println("Изображение T18 загружено");
                } else {
                    System.err.println("Файл NPC не найден: " + npcPath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки NPC: " + e.getMessage());
            }

            // В loadImages():
            try {
                String savPath = "src/QuestPersons/Дедушка-Швед/Hero/Дедушка-Швед.png";
                File savFile = new File(savPath);
                if (savFile.exists()) {
                    savM43Image = ImageIO.read(savFile);
                    System.out.println("✅ Изображение Дедушки-Шведа загружено");
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки Дедушки-Шведа: " + e.getMessage());
            }

            // В loadImages() добавьте загрузку:
            try {
                String friendlyPath = "src/FriendlyPersons/MS-1/Photo/MS-1.png";
                friendlyImage = ImageIO.read(new File(friendlyPath));
                System.out.println("Изображение MS-1 загружено");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки MS-1: " + e.getMessage());
                friendlyImage = null;
            }

            // Загрузка спрайта M53 (для карты)
            try {
                String m53SpritePath = "src/FriendlyPersons/M53/Photo/M53.png";
                File m53SpriteFile = new File(m53SpritePath);
                if (m53SpriteFile.exists()) {
                    m53Image = ImageIO.read(m53SpriteFile);
                    System.out.println("✅ Спрайт M53 загружен");
                } else {
                    System.err.println("❌ Спрайт M53 не найден: " + m53SpritePath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки спрайта M53: " + e.getMessage());
            }

            // Загрузка портрета M53 (для диалога)
            try {
                String m53PortraitPath = "src/FriendlyPersons/M53/Photo/M53.png";
                File m53PortraitFile = new File(m53PortraitPath);
                if (m53PortraitFile.exists()) {
                    m53Portrait = ImageIO.read(m53PortraitFile);
                    System.out.println("✅ Портрет M53 загружен");
                } else {
                    System.err.println("❌ Портрет M53 не найден: " + m53PortraitPath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки портрета M53: " + e.getMessage());
            }

            // Загрузка изображений M53 (добавьте после загрузки портрета)
            try {
                String m53SpritePath = "src/PositiveHeroes/M53/";
                m53Right = ImageIO.read(new File(m53SpritePath + "M53 (вправо).png"));
                m53Left = ImageIO.read(new File(m53SpritePath + "M53 (влево).png"));
                m53Up = ImageIO.read(new File(m53SpritePath + "M53 (вверх).png"));
                m53Down = ImageIO.read(new File(m53SpritePath + "M53 (вниз).png"));
                System.out.println("✅ Изображения M53 загружены");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки M53: " + e.getMessage());
                // Создаём заглушки
                m53Right = createPlaceholderTank("M→");
                m53Left = createPlaceholderTank("M←");
                m53Up = createPlaceholderTank("M↑");
                m53Down = createPlaceholderTank("M↓");
            }

            // Загрузка изображений MS-1 (добавьте после загрузки M53)
            try {
                String ms1SpritePath = "src/PositiveHeroes/MS-1/";

                // Сначала пробуем загрузить с кириллическими названиями
                File rightFile = new File(ms1SpritePath + "MS-1 (вправо).png");
                File leftFile = new File(ms1SpritePath + "MS-1 (влево).png");
                File downFile = new File(ms1SpritePath + "MS-1 (вниз).png");
                File upFile = new File(ms1SpritePath + "MS-1 (вверх).png");

                ms1Right = ImageIO.read(rightFile);
                ms1Left = ImageIO.read(leftFile);
                ms1Down = ImageIO.read(downFile);
                ms1Up = ImageIO.read(upFile);
                System.out.println("✅ Изображения MS-1 загружены");

            } catch (IOException e) {
                System.err.println("Ошибка загрузки MS-1: " + e.getMessage());
                ms1Right = createPlaceholderTank("M→");
                ms1Left = createPlaceholderTank("M←");
                ms1Up = createPlaceholderTank("M↑");
                ms1Down = createPlaceholderTank("M↓");
            }

            // Загрузка спрайтов VK 100.01 P
            try {
                String vk10001pPath = "src/PositiveHeroes/VK_100_01_P (Модернизация ТТ-4)/";
                vk10001pRight = ImageIO.read(new File(vk10001pPath + "VK_100_01_P (вправо).png"));
                vk10001pLeft = ImageIO.read(new File(vk10001pPath + "VK_100_01_P (влево).png"));
                vk10001pUp = ImageIO.read(new File(vk10001pPath + "VK_100_01_P (вверх).png"));
                vk10001pDown = ImageIO.read(new File(vk10001pPath + "VK_100_01_P (вниз).png"));
                System.out.println("✅ Изображения VK 100.01 P загружены");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки VK 100.01 P: " + e.getMessage());
                vk10001pRight = createPlaceholderTank("V→");
                vk10001pLeft = createPlaceholderTank("V←");
                vk10001pUp = createPlaceholderTank("V↑");
                vk10001pDown = createPlaceholderTank("V↓");
            }

// Загрузка портрета VK 100.01 P
            try {
                String vk10001pPortraitPath = "src/PositiveHeroes/ImageOfHeroes/VK_100_01_P.png";
                File vkPortraitFile = new File(vk10001pPortraitPath);
                if (vkPortraitFile.exists()) {
                    vk10001pPortrait = ImageIO.read(vkPortraitFile);
                    System.out.println("✅ Портрет VK 100.01 P загружен");
                } else {
                    System.err.println("❌ Портрет VK 100.01 P не найден: " + vk10001pPortraitPath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки портрета VK 100.01 P: " + e.getMessage());
            }

            // Загрузка изображений AMX 40
            try {
                String amx40SpritePath = "src/PositiveHeroes/AMX_40 (Модернизация ТТ-2)/";
                amx40Right = ImageIO.read(new File(amx40SpritePath + "AMX_40 (вправо).png"));
                amx40Left = ImageIO.read(new File(amx40SpritePath + "AMX_40 (влево).png"));
                amx40Up = ImageIO.read(new File(amx40SpritePath + "AMX_40 (вверх).png"));
                amx40Down = ImageIO.read(new File(amx40SpritePath + "AMX_40 (вниз).png"));
                System.out.println("✅ Изображения AMX 40 загружены");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки AMX 40: " + e.getMessage());
                amx40Right = createPlaceholderTank("A→");
                amx40Left = createPlaceholderTank("A←");
                amx40Up = createPlaceholderTank("A↑");
                amx40Down = createPlaceholderTank("A↓");
            }

            // Загрузка портрета AMX 40
            try {
                String amx40PortraitPath = "src/PositiveHeroes/ImageOfHeroes/AMX_40 (Модернизация ТТ-2).png";
                File amx40PortraitFile = new File(amx40PortraitPath);
                if (amx40PortraitFile.exists()) {
                    amx40Portrait = ImageIO.read(amx40PortraitFile);
                    System.out.println("✅ Портрет AMX 40 загружен");
                } else {
                    System.err.println("❌ Портрет AMX 40 не найден: " + amx40PortraitPath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки портрета AMX 40: " + e.getMessage());
            }

            try {
                String t1Path = "src/PositiveHeroes/T1/";
                t1Right = ImageIO.read(new File(t1Path + "T1 (вправо).png"));
                t1Left = ImageIO.read(new File(t1Path + "T1 (влево).png"));
                t1Up = ImageIO.read(new File(t1Path + "T1 (вверх).png"));
                t1Down = ImageIO.read(new File(t1Path + "T1 (вниз).png"));
                System.out.println("✅ Изображения T1 загружены");
            } catch (IOException e) {
                System.err.println("Ошибка загрузки T1: " + e.getMessage());
                t1Right = createPlaceholderTank("T→");
                t1Left = createPlaceholderTank("T←");
                t1Up = createPlaceholderTank("T↑");
                t1Down = createPlaceholderTank("T↓");
            }

// Загрузка портрета T1
            try {
                String t1PortraitPath = "src/PositiveHeroes/ImageOfHeroes/T1.png";
                File t1PortraitFile = new File(t1PortraitPath);
                if (t1PortraitFile.exists()) {
                    t1Portrait = ImageIO.read(t1PortraitFile);
                    System.out.println("✅ Портрет T1 загружен");
                } else {
                    System.err.println("❌ Портрет T1 не найден: " + t1PortraitPath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки портрета T1: " + e.getMessage());
            }

            // Добавьте в конец метода loadImages()
            try {
                String m53PortraitPath = "src/FriendlyPersons/M53/Photo/M53.png";
                File m53PortraitFile = new File(m53PortraitPath);
                if (m53PortraitFile.exists()) {
                    m53Portrait = ImageIO.read(m53PortraitFile);
                    System.out.println("✅ Портрет M53 загружен");
                }

                String ms1PortraitPath = "src/FriendlyPersons/MS-1/Photo/MS-1.png";
                File ms1PortraitFile = new File(ms1PortraitPath);
                if (ms1PortraitFile.exists()) {
                    ms1Portrait = ImageIO.read(ms1PortraitFile);
                    System.out.println("✅ Портрет MS-1 загружен");
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки портретов союзников: " + e.getMessage());
            }

            try {
                String traderPath = "src/traders/T34/T34.png";
                File traderFile = new File(traderPath);
                if (traderFile.exists()) {
                    traderImage = ImageIO.read(traderFile);
                    System.out.println("✅ Изображение торговца T34 загружено");
                } else {
                    System.err.println("❌ Файл торговца не найден: " + traderPath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки изображения торговца: " + e.getMessage());
            }

            enemyRight = tintImage(tankRight, new Color(200, 0, 0, 100));
            enemyLeft = tintImage(tankLeft, new Color(200, 0, 0, 100));
            enemyUp = tintImage(tankUp, new Color(200, 0, 0, 100));
            enemyDown = tintImage(tankDown, new Color(200, 0, 0, 100));

            currentTankImage = tankRight;

            String hillPath = "src/Obstacles/hill.png";
            File hillFile = new File(hillPath);
            if (hillFile.exists()) {
                hillImage = ImageIO.read(hillFile);
                hillImageLoaded = true;
            }

            /*
            String portraitPath = "src/PositiveHeroes/ImageOfHeroes/Leichttraktor.png";
            File portraitFile = new File(portraitPath);
            if (portraitFile.exists()) {
                heroPortrait = ImageIO.read(portraitFile);
                portraitLoaded = true;
            }
             */

            loadHeroPortrait();


            // ===== ДОБАВЬТЕ ЗАГРУЗКУ ПОРТРЕТА MS-1 =====
            String ms1PortraitPath = "src/PositiveHeroes/ImageOfHeroes/MS-1.png";
            File ms1PortraitFile = new File(ms1PortraitPath);
            if (ms1PortraitFile.exists()) {
                ms1Portrait = ImageIO.read(ms1PortraitFile);
                System.out.println("✅ Портрет MS-1 загружен");
            } else {
                System.err.println("❌ Портрет MS-1 не найден: " + ms1PortraitPath);
            }

// Для ненаймлённых союзников - загружаем только базовые текстуры
            loadBasicFriendlyTextures("MS-1");
            loadBasicFriendlyTextures("M53");
            loadBasicFriendlyTextures("VK10001P");
            loadBasicFriendlyTextures("AMX40");
            loadBasicFriendlyTextures("T1");

            // Загрузка модернизированных текстур для уже нанятых союзников
            for (FriendlyUnit friendly : world.getFriendlyUnits()) {
                if (friendly.isRecruited && friendly.isAlive && friendly.getUpgradeLevel() > 1) {
                    loadFriendlyTextures(friendly);
                    updateFriendlyPortrait(friendly);
                }
            }


        } catch (IOException e) {
            createPlaceholderImages();
        }

        debugFileStructure();
    }

    // Загрузка базовых текстур для типа (без модернизации)
    private void loadBasicFriendlyTextures(String type) {
        String basePath = "src/PositiveHeroes/";
        String fullPath = basePath + type + "/";

        System.out.println("Загрузка базовых текстур для типа " + type + " из папки: " + fullPath);

        try {
            // Используем type как имя файла (MS-1, M53, VK10001P, AMX40)
            BufferedImage right = ImageIO.read(new File(fullPath + type + " (вправо).png"));
            BufferedImage left = ImageIO.read(new File(fullPath + type + " (влево).png"));
            BufferedImage up = ImageIO.read(new File(fullPath + type + " (вверх).png"));
            BufferedImage down = ImageIO.read(new File(fullPath + type + " (вниз).png"));

            if ("MS-1".equals(type)) {
                ms1Right = right;
                ms1Left = left;
                ms1Up = up;
                ms1Down = down;
                loadBasicFriendlyPortrait(type);
            } else if ("M53".equals(type)) {
                m53Right = right;
                m53Left = left;
                m53Up = up;
                m53Down = down;
                loadBasicFriendlyPortrait(type);
            } else if ("VK10001P".equals(type)) {
                vk10001pRight = right;
                vk10001pLeft = left;
                vk10001pUp = up;
                vk10001pDown = down;
                loadBasicFriendlyPortrait(type);
            } else if ("AMX40".equals(type)) {
                amx40Right = right;
                amx40Left = left;
                amx40Up = up;
                amx40Down = down;
                loadBasicFriendlyPortrait(type);
            } else if ("T1".equals(type)) {
                t1Right = right;
                t1Left = left;
                t1Up = up;
                t1Down = down;
                loadBasicFriendlyPortrait(type);
            }

            System.out.println("✅ Базовые текстуры загружены для типа " + type);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки базовых текстур для типа " + type + ": " + e.getMessage());
            System.err.println("  Путь: " + fullPath + type + " (вправо).png");
            setDefaultFriendlyTexturesByType(type);
        }
    }

    private void debugFileStructure() {
        System.out.println("\n=== ДИАГНОСТИКА ФАЙЛОВОЙ СТРУКТУРЫ ===");

        String[] types = {"MS-1", "M53", "VK10001P", "AMX40", "T1"};
        String[] folders = {
                "src/PositiveHeroes/MS-1/",
                "src/PositiveHeroes/M53/",
                "src/PositiveHeroes/VK10001P/",
                "src/PositiveHeroes/AMX40/",
                "src/PositiveHeroes/T1/"
        };

        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            String path = folders[i];
            File folder = new File(path);

            System.out.println("\n📁 " + type + ": " + path);

            if (!folder.exists()) {
                System.err.println("  ❌ ПАПКА НЕ СУЩЕСТВУЕТ!");
                continue;
            }

            if (!folder.isDirectory()) {
                System.err.println("  ❌ ЭТО НЕ ПАПКА!");
                continue;
            }

            File[] files = folder.listFiles();
            if (files == null || files.length == 0) {
                System.err.println("  ❌ ПАПКА ПУСТА!");
                continue;
            }

            System.out.println("  ✅ Найдено файлов: " + files.length);
            for (File f : files) {
                String name = f.getName();
                // Проверяем, содержит ли имя "вправо", "влево", "вверх", "вниз"
                boolean isRight = name.contains("вправо");
                boolean isLeft = name.contains("влево");
                boolean isUp = name.contains("вверх");
                boolean isDown = name.contains("вниз");
                boolean isPng = name.toLowerCase().endsWith(".png");

                if (isRight || isLeft || isUp || isDown) {
                    System.out.println("    " + (isPng ? "✅" : "⚠️") + " " + name +
                            (isPng ? "" : " (НЕ PNG!)"));
                }
            }

            // Проверяем конкретные файлы
            String[] directions = {"вправо", "влево", "вверх", "вниз"};
            for (String dir : directions) {
                File testFile = new File(path + type + " (" + dir + ").png");
                if (testFile.exists()) {
                    System.out.println("    ✅ НАЙДЕН: " + type + " (" + dir + ").png");
                } else {
                    // Проверяем с альтернативным именем
                    String altName = type;
                    if ("VK10001P".equals(type)) altName = "VK_100_01_P";
                    if ("AMX40".equals(type)) altName = "AMX_40";

                    File altFile = new File(path + altName + " (" + dir + ").png");
                    if (altFile.exists()) {
                        System.out.println("    ✅ НАЙДЕН (ALT): " + altName + " (" + dir + ").png");
                    } else {
                        System.out.println("    ❌ НЕ НАЙДЕН: " + type + " (" + dir + ").png");
                    }
                }
            }
        }
        System.out.println("=== КОНЕЦ ДИАГНОСТИКИ ===\n");
    }

    private void loadBasicFriendlyPortrait(String type) {
        String portraitPath = "src/PositiveHeroes/ImageOfHeroes/" + type + ".png";
        try {
            File portraitFile = new File(portraitPath);
            if (portraitFile.exists()) {
                BufferedImage portrait = ImageIO.read(portraitFile);
                if ("MS-1".equals(type)) {
                    ms1Portrait = portrait;
                } else if ("M53".equals(type)) {
                    m53Portrait = portrait;
                } else if ("VK10001P".equals(type)) {
                    vk10001pPortrait = portrait;
                } else if ("AMX40".equals(type)) {
                    amx40Portrait = portrait;
                } else if ("T1".equals(type)) {
                    t1Portrait = portrait;
                }
                System.out.println("✅ Базовый портрет загружен для типа " + type);
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки базового портрета для типа " + type);
        }
    }

    private void setDefaultFriendlyTexturesByType(String type) {
        if ("MS-1".equals(type)) {
            if (ms1Right == null) ms1Right = createPlaceholderTank("M→");
            if (ms1Left == null) ms1Left = createPlaceholderTank("M←");
            if (ms1Up == null) ms1Up = createPlaceholderTank("M↑");
            if (ms1Down == null) ms1Down = createPlaceholderTank("M↓");
        } else if ("M53".equals(type)) {
            if (m53Right == null) m53Right = createPlaceholderTank("M→");
            if (m53Left == null) m53Left = createPlaceholderTank("M←");
            if (m53Up == null) m53Up = createPlaceholderTank("M↑");
            if (m53Down == null) m53Down = createPlaceholderTank("M↓");
        } else if ("VK10001P".equals(type)) {
            if (vk10001pRight == null) vk10001pRight = createPlaceholderTank("V→");
            if (vk10001pLeft == null) vk10001pLeft = createPlaceholderTank("V←");
            if (vk10001pUp == null) vk10001pUp = createPlaceholderTank("V↑");
            if (vk10001pDown == null) vk10001pDown = createPlaceholderTank("V↓");
        } else if ("AMX40".equals(type)) {
            if (amx40Right == null) amx40Right = createPlaceholderTank("A→");
            if (amx40Left == null) amx40Left = createPlaceholderTank("A←");
            if (amx40Up == null) amx40Up = createPlaceholderTank("A↑");
            if (amx40Down == null) amx40Down = createPlaceholderTank("A↓");
        } else if ("T1".equals(type)) {
            if (t1Right == null) t1Right = createPlaceholderTank("A→");
            if (t1Left == null) t1Left = createPlaceholderTank("A←");
            if (t1Up == null) t1Up = createPlaceholderTank("A↑");
            if (t1Down == null) t1Down = createPlaceholderTank("A↓");
        }
    }

    // В GamePanel.java, полностью перепишите метод loadFriendlyTextures:

    public void loadFriendlyTextures(FriendlyUnit friendly) {
        if (friendly == null) return;

        String basePath = "src/PositiveHeroes/";
        String folder = friendly.getTextureFolder();
        String fullPath = basePath + folder + "/";

        System.out.println("Загрузка текстур для " + friendly.name + " из папки: " + fullPath);

        try {
            // ИСПРАВЛЕНИЕ: используем имя файла "MS-1 (вправо).png" БЕЗ изменения названия!
            // Пробуем загрузить файлы с разными вариантами имени
            BufferedImage right = null;
            BufferedImage left = null;
            BufferedImage up = null;
            BufferedImage down = null;

            // Вариант 1: файл называется как тип юнита (MS-1, M53, VK10001P, AMX40)
            String fileNameBase = friendly.type;
            File rightFile = new File(fullPath + fileNameBase + " (вправо).png");
            File leftFile = new File(fullPath + fileNameBase + " (влево).png");
            File upFile = new File(fullPath + fileNameBase + " (вверх).png");
            File downFile = new File(fullPath + fileNameBase + " (вниз).png");

            if (rightFile.exists()) {
                right = ImageIO.read(rightFile);
                left = ImageIO.read(leftFile);
                up = ImageIO.read(upFile);
                down = ImageIO.read(downFile);
                System.out.println("✅ Загружены текстуры с именем: " + fileNameBase);
            } else {
                // Вариант 2: для VK 100.01 P и AMX 40 у них могут быть имена папки с пробелами
                // Например: "VK_100_01_P" вместо "VK10001P"
                String altName = friendly.type;
                if ("VK10001P".equals(friendly.type)) {
                    altName = "VK10001P";
                } else if ("AMX40".equals(friendly.type)) {
                    altName = "AMX40";
                }

                File altRightFile = new File(fullPath + altName + " (вправо).png");
                if (altRightFile.exists()) {
                    right = ImageIO.read(altRightFile);
                    left = ImageIO.read(new File(fullPath + altName + " (влево).png"));
                    up = ImageIO.read(new File(fullPath + altName + " (вверх).png"));
                    down = ImageIO.read(new File(fullPath + altName + " (вниз).png"));
                    System.out.println("✅ Загружены текстуры с альтернативным именем: " + altName);
                }
            }

            // Если удалось загрузить хоть одно изображение, сохраняем
            if (right != null) {
                if ("MS-1".equals(friendly.type)) {
                    ms1Right = right;
                    ms1Left = left;
                    ms1Up = up;
                    ms1Down = down;
                } else if ("M53".equals(friendly.type)) {
                    m53Right = right;
                    m53Left = left;
                    m53Up = up;
                    m53Down = down;
                } else if ("VK10001P".equals(friendly.type)) {
                    vk10001pRight = right;
                    vk10001pLeft = left;
                    vk10001pUp = up;
                    vk10001pDown = down;
                } else if ("AMX40".equals(friendly.type)) {
                    amx40Right = right;
                    amx40Left = left;
                    amx40Up = up;
                    amx40Down = down;
                } else if ("T1".equals(friendly.type)) {
                    t1Right = right;
                    t1Left = left;
                    t1Up = up;
                    t1Down = down;
                }
                System.out.println("✅ Текстуры загружены для " + friendly.name + " из: " + folder);
                updateFriendlyPortrait(friendly);
                return;
            }

            // Если не удалось загрузить модернизированные текстуры, пробуем базовые
            System.out.println("⚠️ Модернизированные текстуры не найдены, загружаем базовые для " + friendly.type);
            loadBasicFriendlyTextures(friendly.type);

        } catch (IOException e) {
            System.err.println("Ошибка загрузки текстур для " + friendly.name + ": " + e.getMessage());
            loadBasicFriendlyTextures(friendly.type);
        }
    }

    // В GamePanel.java добавьте этот метод после updateFriendlyTextures()

    public void refreshAllFriendlyTextures() {
        System.out.println("=== refreshAllFriendlyTextures ===");
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isRecruited && friendly.isAlive) {
                loadFriendlyTextures(friendly);
                updateFriendlyPortrait(friendly);
                System.out.println("  Загружены текстуры для " + friendly.name +
                        " (уровень " + friendly.getUpgradeLevel() + ")");
            }
        }
        repaint();
    }

    private void loadFriendlyTexturesForType(String type) {
        String basePath = "src/PositiveHeroes/";
        String folder = type; // По умолчанию без модернизации

        // Проверяем наличие модернизированных текстур для уже нанятых союзников
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.type.equals(type) && friendly.isRecruited && friendly.getUpgradeLevel() > 1) {
                folder = friendly.getTextureFolder();
                break;
            }
        }

        String fullPath = basePath + folder + "/";
        System.out.println("Загрузка текстур для типа " + type + " из папки: " + fullPath);

        try {
            BufferedImage right = ImageIO.read(new File(fullPath + type + " (вправо).png"));
            BufferedImage left = ImageIO.read(new File(fullPath + type + " (влево).png"));
            BufferedImage up = ImageIO.read(new File(fullPath + type + " (вверх).png"));
            BufferedImage down = ImageIO.read(new File(fullPath + type + " (вниз).png"));

            // Сохраняем в соответствующие поля
            if ("MS-1".equals(type)) {
                ms1Right = right;
                ms1Left = left;
                ms1Up = up;
                ms1Down = down;
                loadFriendlyPortraitForType(type);
            } else if ("M53".equals(type)) {
                m53Right = right;
                m53Left = left;
                m53Up = up;
                m53Down = down;
                loadFriendlyPortraitForType(type);
            } else if ("VK10001P".equals(type)) {
                vk10001pRight = right;
                vk10001pLeft = left;
                vk10001pUp = up;
                vk10001pDown = down;
                loadFriendlyPortraitForType(type);
            } else if ("AMX40".equals(type)) {
                amx40Right = right;
                amx40Left = left;
                amx40Up = up;
                amx40Down = down;
                loadFriendlyPortraitForType(type);
            } else if ("t1".equals(type)) {
                t1Right = right;
                t1Left = left;
                t1Up = up;
                t1Down = down;
                loadFriendlyPortraitForType(type);
            }

            System.out.println("✅ Текстуры загружены для типа " + type);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки текстур для типа " + type + ": " + e.getMessage());
        }
    }

    private void loadFriendlyPortraitForType(String type) {
        String basePath = "src/PositiveHeroes/ImageOfHeroes/";
        String folder = type;

        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.type.equals(type) && friendly.isRecruited && friendly.getUpgradeLevel() > 1) {
                folder = friendly.type + " (Модернизация " + friendly.getUpgradeClass() + "-" + friendly.getUpgradeLevel() + ")";
                break;
            }
        }

        String portraitPath = basePath + folder + ".png";
        try {
            File portraitFile = new File(portraitPath);
            if (portraitFile.exists()) {
                BufferedImage portrait = ImageIO.read(portraitFile);
                if ("MS-1".equals(type)) {
                    ms1Portrait = portrait;
                } else if ("M53".equals(type)) {
                    m53Portrait = portrait;
                } else if ("VK10001P".equals(type)) {
                    vk10001pPortrait = portrait;
                } else if ("AMX40".equals(type)) {
                    amx40Portrait = portrait;
                } else if ("T1".equals(type)) {
                    t1Portrait = portrait;
                }
                System.out.println("✅ Портрет загружен для типа " + type + ": " + portraitPath);
            } else {
                // Используем стандартный портрет
                String defaultPath = basePath + type + ".png";
                File defaultFile = new File(defaultPath);
                if (defaultFile.exists()) {
                    BufferedImage portrait = ImageIO.read(defaultFile);
                    if ("MS-1".equals(type)) {
                        ms1Portrait = portrait;
                    } else if ("M53".equals(type)) {
                        m53Portrait = portrait;
                    } else if ("VK10001P".equals(type)) {
                        vk10001pPortrait = portrait;
                    } else if ("AMX40".equals(type)) {
                        amx40Portrait = portrait;
                    } else if ("T1".equals(type)) {
                        t1Portrait = portrait;
                    }
                    System.out.println("✅ Загружен стандартный портрет для типа " + type);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки портрета для типа " + type + ": " + e.getMessage());
        }
    }

    public void updateFriendlyTextures(FriendlyUnit friendly) {
        if (friendly == null) return;

        System.out.println("Обновление текстур для " + friendly.name +
                " (уровень " + friendly.getUpgradeLevel() + ", класс " + friendly.getUpgradeClass() + ")");

        String basePath = "src/PositiveHeroes/";
        String folder = friendly.getTextureFolder();
        String fullPath = basePath + folder + "/";

        try {
            BufferedImage right = ImageIO.read(new File(fullPath + friendly.type + " (вправо).png"));
            BufferedImage left = ImageIO.read(new File(fullPath + friendly.type + " (влево).png"));
            BufferedImage up = ImageIO.read(new File(fullPath + friendly.type + " (вверх).png"));
            BufferedImage down = ImageIO.read(new File(fullPath + friendly.type + " (вниз).png"));

            if ("MS-1".equals(friendly.type)) {
                ms1Right = right;
                ms1Left = left;
                ms1Up = up;
                ms1Down = down;
            } else if ("M53".equals(friendly.type)) {
                m53Right = right;
                m53Left = left;
                m53Up = up;
                m53Down = down;
            } else if ("VK10001P".equals(friendly.type)) {
                vk10001pRight = right;
                vk10001pLeft = left;
                vk10001pUp = up;
                vk10001pDown = down;
            } else if ("AMX40".equals(friendly.type)) {
                amx40Right = right;
                amx40Left = left;
                amx40Up = up;
                amx40Down = down;
            } else if ("T1".equals(friendly.type)) {
                t1Right = right;
                t1Left = left;
                t1Up = up;
                t1Down = down;
            }

            // Обновляем портрет
            updateFriendlyPortrait(friendly);

            repaint();
        } catch (IOException e) {
            System.err.println("Ошибка обновления текстур для " + friendly.name + ": " + e.getMessage());
        }
    }

    private void loadTankImages() {
        String basePath = "src/PositiveHeroes/";
        String folder = player.getTextureFolder();
        String fullPath = basePath + folder + "/";

        System.out.println("Загрузка текстур из папки: " + fullPath);

        try {
            tankRight = ImageIO.read(new File(fullPath + "Leichttraktor (вправо).png"));
            tankLeft = ImageIO.read(new File(fullPath + "Leichttraktor (влево).png"));
            tankDown = ImageIO.read(new File(fullPath + "Leichttraktor (вниз).png"));
            tankUp = ImageIO.read(new File(fullPath + "Leichttraktor (вверх).png"));
            System.out.println("Загружены текстуры из: " + folder);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки текстур из папки " + folder + ": " + e.getMessage());
            createPlaceholderImages();
        }
        currentTankImage = tankRight;
    }

    public void updatePlayerTextures() {
        String basePath = "src/PositiveHeroes/";
        String folder = player.getTextureFolder();
        String fullPath = basePath + folder + "/";

        System.out.println("Обновление текстур из папки: " + fullPath);

        try {
            tankRight = ImageIO.read(new File(fullPath + "Leichttraktor (вправо).png"));
            tankLeft = ImageIO.read(new File(fullPath + "Leichttraktor (влево).png"));
            tankDown = ImageIO.read(new File(fullPath + "Leichttraktor (вниз).png"));
            tankUp = ImageIO.read(new File(fullPath + "Leichttraktor (вверх).png"));
            System.out.println("✅ Текстуры загружены из: " + folder);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки текстур из папки " + folder + ": " + e.getMessage());
            createPlaceholderImages();
        }

        // ===== ДОБАВЬТЕ ОБНОВЛЕНИЕ ПОРТРЕТА =====
        updateHeroPortrait();

        // Обновляем текущее изображение
        if (moveDirection != null) {
            switch(moveDirection) {
                case UP: currentTankImage = tankUp; break;
                case DOWN: currentTankImage = tankDown; break;
                case LEFT: currentTankImage = tankLeft; break;
                case RIGHT: currentTankImage = tankRight; break;
            }
        } else {
            currentTankImage = tankRight;
        }

        repaint();
    }

    private void endPlayerTurnAndStartEnemyTurn() {

        friendlyPath.clear();
        movingFriendly = null;
        isFriendlyAutoMoving = false;

        if (world.isAnyEnemyMoving()) {
            System.out.println("Подождите, враги ещё двигаются...");
            return;
        }

        player.turnEnded = true;

        // ===== ПРИ ЗАВЕРШЕНИИ ХОДА ИГРОКА ОБНОВЛЯЕМ МУЗЫКУ =====
        // Сбрасываем флаг, чтобы при старте следующего хода была правильная музыка

        // Сбрасываем флаг turnEnded для всех союзников, чтобы они могли начать новый ход
        for (FriendlyUnit unit : world.getControllableUnits()) {
            if (unit.isAlive && unit.isRecruited) {
                unit.turnEnded = false;
                System.out.println(unit.name + " готов к новому ходу");
            }
        }

        // Сбрасываем индекс активного юнита на игрока
        currentUnitIndex = 0;

        // Центрируем камеру на игроке
        centerCameraOnUnit(player.gridX, player.gridY);

        System.out.println("Ход игрока завершён!");
        System.out.println("=== НАЧАЛО ХОДА ВРАГОВ ===");
        world.startEnemyTurn();
        repaint();
    }

    // ДОБАВЬТЕ МЕТОД для обновления анимаций всех врагов
    // В GamePanel.java, обновите метод:
    private void updateEnemyAnimations() {
        boolean anyVisibleEnemyMoving = false;

        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isMoving) {
                boolean completed = enemy.updateAnimation(GameWorld.CELL_SIZE, GameWorld.TANK_SIZE);
                if (completed) {
                    soundManager.stopEnemyMoveSound();
                }
                anyVisibleEnemyMoving = true;
            }
        }

        // УБИРАЕМ центрирование отсюда! Оно будет в actionPerformed

        if (anyVisibleEnemyMoving) {
            repaint();
        }
    }

    private BufferedImage tintImage(BufferedImage src, Color tint) {
        if (src == null) return createPlaceholderTank("E");
        BufferedImage tinted = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tinted.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setColor(tint);
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.dispose();
        return tinted;
    }

    public void cacheField() {
        int gridWidth = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE;
        int gridHeight = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE;
        fieldCache = new BufferedImage(GameWorld.FIELD_SIZE, GameWorld.FIELD_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = fieldCache.createGraphics();
        Random rand = new Random(42);

        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                int green = 100 + ((x + y) % 3) * 20;
                g.setColor(new Color(34, green, 34));
                g.fillRect(x * GameWorld.CELL_SIZE, y * GameWorld.CELL_SIZE, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);
            }
        }

        // Сетка
        g.setColor(new Color(0, 0, 0, 100));
        for (int x = 0; x <= gridWidth; x++) {
            g.drawLine(x * GameWorld.CELL_SIZE, 0, x * GameWorld.CELL_SIZE, GameWorld.FIELD_SIZE);
        }
        for (int y = 0; y <= gridHeight; y++) {
            g.drawLine(0, y * GameWorld.CELL_SIZE, GameWorld.FIELD_SIZE, y * GameWorld.CELL_SIZE);
        }

        if (world != null) {
            world.setFieldCache(fieldCache);
        }

        g.dispose();
    }

    private void createPlaceholderImages() {
        tankRight = createPlaceholderTank("→");
        tankLeft = createPlaceholderTank("←");
        tankUp = createPlaceholderTank("↑");
        tankDown = createPlaceholderTank("↓");
        currentTankImage = tankRight;
    }

    // В GamePanel.java, замените метод syncRecruitedUnits:
    // В GamePanel.java, замените метод syncRecruitedUnits:
    public void syncRecruitedUnits() {
        System.out.println("=== syncRecruitedUnits ВЫЗВАН ===");
        recruitedUnits.clear();
        for (FriendlyUnit unit : world.getFriendlyUnits()) {
            if (unit.isRecruited && unit.isAlive) {
                recruitedUnits.add(unit);
                // ===== ДОБАВЬТЕ ЭТУ СТРОКУ =====
                // Загружаем/обновляем текстуры для каждого нанятого союзника
                loadFriendlyTextures(unit);
                updateFriendlyPortrait(unit);
                // ==============================
            }
        }
        world.getControllableUnits().removeIf(unit -> !unit.isAlive);
        repaint();
    }

    private BufferedImage createPlaceholderTank(String symbol) {
        BufferedImage img = new BufferedImage(GameWorld.TANK_SIZE, GameWorld.TANK_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(70, 70, 70));
        g.fillRoundRect(0, 0, GameWorld.TANK_SIZE, GameWorld.TANK_SIZE, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(symbol, GameWorld.TANK_SIZE/2 - 5, GameWorld.TANK_SIZE/2 + 5);
        g.dispose();
        return img;
    }

    // В performShot(), для стрельбы союзника, измените создание снаряда:

    private void performShot(int targetX, int targetY) {
        Enemy targetEnemy = null;

        for (Enemy e : world.getEnemies()) {
            if (e.gridX == targetX && e.gridY == targetY && e.isAlive) {
                targetEnemy = e;
                break;
            }
        }

        if (targetEnemy == null) {
            System.out.println("В выбранной клетке нет живого врага!");
            return;
        }

        // ===== ПРОВЕРКА ВИДИМОСТИ ОТ КОМАНДЫ ДЛЯ ВСЕХ =====
        boolean isVisibleByTeam = world.isEnemyVisibleByTeam(targetEnemy);

        if (!isVisibleByTeam) {
            System.out.println("Враг не виден команде! Нельзя стрелять по невидимой цели!");
            JOptionPane.showMessageDialog(this,
                    "❌ Враг не виден!\n\n" +
                            "Вы не можете стрелять по цели, которую никто не видит.\n" +
                            "Подойдите ближе или используйте другие юниты для разведки.",
                    "Цель не видна",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (isPlayerActive()) {
            // ===== СТРЕЛЬБА ИГРОКА =====
            PlayerTank player = this.player;

            if (player.isOverweight()) {
                JOptionPane.showMessageDialog(this,
                        "⚠ ПЕРЕГРУЗ! ДВИЖЕНИЕ НЕВОЗМОЖНО!\n\n" +
                                "Текущий вес: " + String.format("%.1f", player.getInventory().getTotalWeight()) + "/" +
                                String.format("%.1f", player.maxCarryWeight) + "\n" +
                                "Сбросьте лишний груз, чтобы восстановить подвижность.",
                        "Невозможно двигаться", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (player.getInventory().isWeaponEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "⚠ ОРУЖИЕ РАЗРЯЖЕНО!\n\n" +
                                "Нажмите R (перезарядка) в инвентаре, чтобы зарядить оружие.",
                        "Нет снарядов!", JOptionPane.WARNING_MESSAGE);
                return;
            }

            AmmoItem currentAmmo = player.getCurrentAmmo();

            int shotCost = isAimingMode ? player.aimedShotCost : player.shotCost;
            if (player.movePoints < shotCost) {
                System.out.println("Недостаточно очков хода!");
                return;
            }

            player.movePoints -= shotCost;
            player.getInventory().useAmmoFromWeapon(player.getBurstSize());
            soundManager.updateWeaponCaliber(player.weaponCaliber);

            int baseDamage = currentAmmo != null ? currentAmmo.getDamage() : player.weaponDamage;
            double critChance = currentAmmo != null ? currentAmmo.getCritChance() : player.critChance;

            double hitChance = calculateHitProbability(targetX, targetY);
            soundManager.playShootSound();

            double startX = getAnimatedX() + GameWorld.TANK_SIZE / 2.0;
            double startY = getAnimatedY() + GameWorld.TANK_SIZE / 2.0;

            for (int i = 0; i < player.getBurstSize(); i++) {
                boolean isCrit = random.nextDouble() < (player.critChance + (player.critBonus / 100.0));
                int damage = player.weaponDamage;
                if (isCrit) damage *= (1.5 + random.nextDouble() * 1.5);

                Projectile projectile = new Projectile(startX, startY, targetEnemy, damage, hitChance, world);
                activeProjectiles.add(projectile);
            }

            System.out.println("Leichttraktor выпустил " + player.getBurstSize() + " снарядов по цели [" + targetX + "," + targetY + "]!");
        }
        else {
            // ===== СТРЕЛЬБА СОЮЗНИКА (ИСПРАВЛЕНА) =====
            FriendlyUnit active = getActiveFriendly();
            if (active == null) return;

            if (active.isOverweight()) {
                JOptionPane.showMessageDialog(this,
                        "⚠ ПЕРЕГРУЗ! ДВИЖЕНИЕ НЕВОЗМОЖНО!\n\n" +
                                "Текущий вес: " + String.format("%.1f", active.getInventory().getTotalWeight()) + "/" +
                                String.format("%.1f", active.maxCarryWeight) + "\n" +
                                "Сбросьте лишний груз, чтобы восстановить подвижность.",
                        "Невозможно двигаться", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Inventory inv = active.getInventory();

            if (inv.isWeaponEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "⚠ ОРУЖИЕ " + active.name.toUpperCase() + " РАЗРЯЖЕНО!\n\n" +
                                "Нажмите R (перезарядка) в инвентаре, чтобы зарядить оружие.",
                        "Нет снарядов!", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int shotCost = isAimingMode ? active.aimedShotCost : active.shotCost;
            if (active.movePoints < shotCost) {
                System.out.println("Недостаточно очков хода! Нужно: " + shotCost + ", есть: " + active.movePoints);
                return;
            }

            active.movePoints -= shotCost;
            inv.useAmmoFromWeapon(active.burstSize);

            soundManager.updateWeaponCaliber(active.weaponCaliber);
            soundManager.playShootSound();

            // ===== ВАЖНО: используем тот же hitChance, что и для игрока =====
            // Но нужно добавить метод calculateHitProbabilityForFriendlyWithTeamVision
            double hitChance = calculateHitProbabilityForFriendly(active, targetX, targetY);

            System.out.println("🎯 " + active.name + " стреляет по [" + targetX + "," + targetY +
                    "] с позиции [" + active.gridX + "," + active.gridY +
                    "], шанс: " + (int)(hitChance * 100) + "%, цель видна командой: " + isVisibleByTeam);

            double startX = active.gridX * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE / 2.0;
            double startY = active.gridY * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE / 2.0;

            for (int i = 0; i < active.burstSize; i++) {
                boolean isCrit = random.nextDouble() < active.critChance;
                int damage = active.weaponDamage;
                if (isCrit) damage *= (1.5 + random.nextDouble() * 1.5);

                Projectile projectile = new Projectile(startX, startY, targetEnemy, damage, hitChance, world, active);
                activeProjectiles.add(projectile);
            }

            System.out.println(active.name + " выпустил " + active.burstSize + " снарядов!");
            soundManager.updateWeaponCaliber(weapon.caliber);
        }

        double caliber = isPlayerActive() ? player.weaponCaliber : getActiveFriendly().weaponCaliber;
        world.addGunshotSound(isPlayerActive() ? player.gridX : getActiveFriendly().gridX,
                isPlayerActive() ? player.gridY : getActiveFriendly().gridY,
                caliber);

        isAimingMode = false;
        targetShotX = targetShotY = -1;
        repaint();
    }

    private AmmoItem getFriendlyAmmoForCaliber(FriendlyUnit friendly, Caliber caliber) {
        Inventory inv = friendly.getInventory();
        for (int y = 0; y < Inventory.HEIGHT; y++) {
            for (int x = 0; x < Inventory.WIDTH; x++) {
                Item item = inv.getItem(x, y);
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

    // Измените модификатор с private на public
    // Измените модификатор с private на public
    public double calculateHitProbability(int targetX, int targetY) {
        int dx = Math.abs(player.gridX - targetX);
        int dy = Math.abs(player.gridY - targetY);
        double distance = Math.sqrt(dx*dx + dy*dy);

        int agility = player.agility;
        int weaponAccuracy = player.weaponAccuracy;

        // ===== ПОЛУЧАЕМ ТЕКУЩИЙ СНАРЯД =====
        AmmoItem currentAmmo = player.getCurrentAmmo();
        System.out.println("=== ОТЛАДКА СНАРЯДОВ ===");
        System.out.println("currentAmmo is null? " + (currentAmmo == null));
        if (currentAmmo != null) {
            System.out.println("  Калибр: " + currentAmmo.getCaliber().name);
            System.out.println("  Улучшенные? " + currentAmmo.isImproved());
            System.out.println("  Множитель точности: " + currentAmmo.getAccuracyMultiplier());
            System.out.println("  Количество в оружии: " + player.getInventory().getCurrentAmmoCount());
        }
        double accuracyMultiplier = (currentAmmo != null) ? currentAmmo.getAccuracyMultiplier() : 1.0;

        // Базовая точность в упор
        double baseHitChance = 0.30 + (weaponAccuracy / 100.0) * 0.65;
        baseHitChance = Math.min(0.95, baseHitChance);

        // Штраф за расстояние
        double distancePenalty = Math.exp(-distance / 25.0);

        // Бонус ловкости
        double agilityBonus = 0.05 + (agility / 500.0);
        agilityBonus = Math.min(0.15, agilityBonus);

        // ===== ПРИМЕНЯЕМ МНОЖИТЕЛЬ ТОЧНОСТИ ОТ СНАРЯДА =====
        double hitChance = baseHitChance * distancePenalty * accuracyMultiplier + agilityBonus;

        // Режим прицельного огня
        if (isAimingMode) {
            hitChance = hitChance * 1.2;
        }

        // Бонус холма
        if (player.isOnHill) {
            hitChance *= (1.0 + player.currentHillBonus * 0.1);
        }

        hitChance = Math.min(0.95, Math.max(0.05, hitChance));

        System.out.println("=== РАСЧЁТ ШАНСА ПОПАДАНИЯ ===");
        System.out.println("  Дистанция: " + distance);
        System.out.println("  Точность оружия: " + weaponAccuracy);
        System.out.println("  Множитель снаряда: " + accuracyMultiplier + " (" +
                (currentAmmo != null ? (currentAmmo.isImproved() ? "улучшенный" : "базовый") : "нет снаряда") + ")");
        System.out.println("  ИТОГОВЫЙ ШАНС: " + String.format("%.2f", hitChance * 100) + "%");

        return hitChance;
    }

    // ВОССТАНОВЛЕН МЕТОД setTarget
    private void setTarget(int gridX, int gridY) {
        if (!world.canMoveTo(gridX, gridY)) {
            System.out.println("Нельзя выбрать эту клетку!");
            return;
        }

        if (player.turnEnded) {
            System.out.println("Ход завершен! Нажмите E для нового хода.");
            return;
        }

        if (targetGridX == gridX && targetGridY == gridY && !currentPath.isEmpty()) {
            startAutoMove();
            return;
        }

        targetGridX = gridX;
        targetGridY = gridY;

        currentPath = pathFinder.findPath(player.gridX, player.gridY, targetGridX, targetGridY);
        currentPathIndex = 0;

        if (currentPath.isEmpty()) {
            System.out.println("Путь не найден!");
            targetGridX = -1;
            targetGridY = -1;
        } else {
            System.out.println("Путь найден! Количество шагов: " + currentPath.size());
            System.out.println("Для движения потребуется: " + (currentPath.size() * player.moveCost) + " очков хода");
        }

        repaint();
    }

    // ВОССТАНОВЛЕН МЕТОД startAutoMove
    // В GamePanel.java, замените метод startAutoMove:

    private void startAutoMove() {
        if (currentPath.isEmpty() || isAutoMoving) return;

        if (player.turnEnded) {
            System.out.println("Ход завершен! Нажмите E для нового хода.");
            return;
        }

        // ===== ДОБАВИТЬ ЭТУ ПРОВЕРКУ =====
        if (player.isOverweight()) {
            JOptionPane.showMessageDialog(this,
                    "⚠ ПЕРЕГРУЗ! Автоматическое движение невозможно!\n" +
                            "Текущий вес: " + String.format("%.1f", player.getInventory().getTotalWeight()) + "/" +
                            String.format("%.1f", player.maxCarryWeight) + "\n" +
                            "Выбросьте лишние предметы через инвентарь (I).",
                    "Невозможно двигаться", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentPath.isEmpty() || isAutoMoving) return;

        if (player.turnEnded) {
            System.out.println("Ход завершен! Нажмите E для нового хода.");
            return;
        }

        // ===== ОБРЕЗАЕМ ПУТЬ, ЕСЛИ НЕ ХВАТАЕТ ОЧКОВ ХОДА =====
        int maxSteps = player.movePoints / player.moveCost;
        if (currentPath.size() > maxSteps) {
            System.out.println("⚠ Не хватает очков хода для всего пути!");
            System.out.println("   Нужно шагов: " + currentPath.size() + ", хватит только на " + maxSteps);
            System.out.println("   Будет пройдено максимально возможное расстояние.");

            // Обрезаем путь до максимально возможного количества шагов
            currentPath = currentPath.subList(0, maxSteps);

            if (currentPath.isEmpty()) {
                System.out.println("   Не хватает даже на один шаг!");
                return;
            }
        }

        // ===== ЗАПОМИНАЕМ КОЛИЧЕСТВО ВИДИМЫХ ВРАГОВ =====
        visibleEnemyCountBeforeMove = 0;
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isAlive && world.isEnemyVisible(enemy)) {
                visibleEnemyCountBeforeMove++;
            }
        }
        System.out.println("📊 До движения видно врагов: " + visibleEnemyCountBeforeMove);

        isAutoMoving = true;
        currentPathIndex = 0;
        moveToNextCell();
    }

    // ВОССТАНОВЛЕН МЕТОД moveToNextCell
    // В GamePanel.java, замените метод moveToNextCell:

    private void moveToNextCell() {
        // ===== ПРОВЕРКА ПЕРЕД КАЖДЫМ ШАГОМ =====
        if (checkAndInterruptMoveForPlayer()) {
            return;
        }

        if (currentPathIndex >= currentPath.size()) {
            isAutoMoving = false;
            targetGridX = -1;
            targetGridY = -1;
            currentPath.clear();
            System.out.println("Цель достигнута!");
            System.out.println("Осталось очков хода: " + player.movePoints);
            soundManager.stopMoveSound();
            return;
        }

        Point nextCell = currentPath.get(currentPathIndex);

        // ===== ПРОВЕРКА, ХВАТАЕТ ЛИ ОЧКОВ ХОДА ДЛЯ ЭТОГО ШАГА =====
        if (!player.canMove()) {
            // Не хватает очков хода для следующего шага
            System.out.println("⚠ Недостаточно очков хода для следующего шага!");
            System.out.println("   Остановка на максимально возможной позиции: [" + player.gridX + "," + player.gridY + "]");
            isAutoMoving = false;
            currentPath.clear();
            targetGridX = -1;
            targetGridY = -1;
            soundManager.stopMoveSound();
            return;
        }

        PlayerTank.Direction direction = getDirection(player.gridX, player.gridY, nextCell.x, nextCell.y);

        if (direction != null && player.canMove()) {
            startMove(nextCell.x, nextCell.y, direction, true);
        } else if (!player.canMove()) {
            System.out.println("Недостаточно очков хода для следующего шага!");
            isAutoMoving = false;
            currentPath.clear();
            soundManager.stopMoveSound();
        }

        currentPathIndex++;
    }

    private boolean checkAndInterruptMoveForPlayer() {
        // Считаем текущее количество видимых врагов
        int currentVisibleCount = 0;
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isAlive && world.isEnemyVisible(enemy)) {
                currentVisibleCount++;
            }
        }

        // ===== ПРЕРЫВАЕМ, ЕСЛИ КОЛИЧЕСТВО ВИДИМЫХ ВРАГОВ УВЕЛИЧИЛОСЬ =====
        if (currentVisibleCount > visibleEnemyCountBeforeMove) {
            System.out.println("⚠ ОБНАРУЖЕН НОВЫЙ ПРОТИВНИК! (было: " + visibleEnemyCountBeforeMove +
                    ", стало: " + currentVisibleCount + ") Движение прервано!");

            // ===== УСТАНАВЛИВАЕМ ФЛАГ ОБНАРУЖЕНИЯ =====
            enemyDetectedThisTurn = true;
            System.out.println("👁️ Флаг обнаружения установлен (обнаружен враг при движении)");

            isAutoMoving = false;
            currentPath.clear();
            targetGridX = -1;
            targetGridY = -1;
            soundManager.stopMoveSound();

            // Обновляем счётчик для следующего движения
            visibleEnemyCountBeforeMove = currentVisibleCount;
            return true;
        }

        return false;
    }

    private PlayerTank.Direction getDirection(int fromX, int fromY, int toX, int toY) {
        if (toX > fromX) return PlayerTank.Direction.RIGHT;
        if (toX < fromX) return PlayerTank.Direction.LEFT;
        if (toY > fromY) return PlayerTank.Direction.DOWN;
        if (toY < fromY) return PlayerTank.Direction.UP;
        return null;
    }

    // ВОССТАНОВЛЕН МЕТОД startMove (с поддержкой анимации)
    private void startMove(int newGridX, int newGridY, PlayerTank.Direction direction, boolean isAuto) {

        if (!world.canMoveTo(newGridX, newGridY)) {
            if (isAuto) {
                isAutoMoving = false;
                currentPath.clear();
            }
            return;
        }

        if (!player.canMove()) {
            System.out.println("Недостаточно очков хода!");
            return;
        }

        isMoving = true;
        moveProgress = 0;

        // Вычисляем пиксельные координаты
        moveFromX = player.gridX * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
        moveFromY = player.gridY * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
        moveToX = newGridX * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
        moveToY = newGridY * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
        moveDirection = direction;

        // Тратим очки хода
        player.consumeMovePoints();

        switch(direction) {
            case UP: currentTankImage = tankUp; break;
            case DOWN: currentTankImage = tankDown; break;
            case LEFT: currentTankImage = tankLeft; break;
            case RIGHT: currentTankImage = tankRight; break;
        }

        soundManager.playMoveSound();
    }

    // ВОССТАНОВЛЕН МЕТОД updateMove (плавная анимация)
    private void updateMove() {
        if (!isMoving) return;

        moveProgress += 16;
        int moveDuration = 200;

        if (moveProgress >= moveDuration) {
            player.gridX = moveToX / GameWorld.CELL_SIZE;
            player.gridY = moveToY / GameWorld.CELL_SIZE;
            isMoving = false;

            // Если активен игрок, центрируем камеру
            if (isPlayerActive()) {
                centerCameraOnUnit(player.gridX, player.gridY);
            }

            world.checkHillOccupation();
            checkEnemyDetection();

            if (isAutoMoving) {
                moveToNextCell();
            } else {
                soundManager.stopMoveSound();
            }

        }
    }

    // Метод для получения текущей позиции отрисовки с учётом анимации
    private int getAnimatedX() {
        if (isMoving) {
            float t = (float)moveProgress / 200f;
            return (int)(moveFromX + (moveToX - moveFromX) * t);
        }
        return player.gridX * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
    }

    private int getAnimatedY() {
        if (isMoving) {
            float t = (float)moveProgress / 200f;
            return (int)(moveFromY + (moveToY - moveFromY) * t);
        }
        return player.gridY * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2;
    }

    private void updateCamera(int viewWidth, int viewHeight) {
        // Если размеры окна ещё не известны, ничего не делаем
        if (viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        // ===== НЕ ПЕРЕСЧИТЫВАЕМ КАМЕРУ ВО ВРЕМЯ ХОДА ВРАГОВ =====
        if (world.isEnemyTurn()) {
            // Просто ограничиваем камеру границами, не меняем её позицию
            int maxX = GameWorld.FIELD_SIZE - viewWidth;
            int maxY = GameWorld.FIELD_SIZE - viewHeight;
            cameraX = Math.max(0, Math.min(maxX, cameraX));
            cameraY = Math.max(0, Math.min(maxY, cameraY));
            return;
        }

        // Если камера на враге, не пересчитываем её автоматически
        if (isCameraOnEnemy) {
            // Просто ограничиваем камеру границами
            int maxX = GameWorld.FIELD_SIZE - viewWidth;
            int maxY = GameWorld.FIELD_SIZE - viewHeight;
            cameraX = Math.max(0, Math.min(maxX, cameraX));
            cameraY = Math.max(0, Math.min(maxY, cameraY));
            return;
        }

        // Если активен не игрок, не пересчитываем камеру (она уже установлена centerCameraOnUnit)
        if (!isPlayerActive()) {
            int maxX = GameWorld.FIELD_SIZE - viewWidth;
            int maxY = GameWorld.FIELD_SIZE - viewHeight;
            cameraX = Math.max(0, Math.min(maxX, cameraX));
            cameraY = Math.max(0, Math.min(maxY, cameraY));
            return;
        }

        // Для игрока вычисляем камеру как обычно
        int tankScreenX = getAnimatedX();
        int tankScreenY = getAnimatedY();
        cameraX = tankScreenX + GameWorld.TANK_SIZE/2 - viewWidth / 2;
        cameraY = tankScreenY + GameWorld.TANK_SIZE/2 - viewHeight / 2;

        int maxX = GameWorld.FIELD_SIZE - viewWidth;
        int maxY = GameWorld.FIELD_SIZE - viewHeight;
        cameraX = Math.max(0, Math.min(maxX, cameraX));
        cameraY = Math.max(0, Math.min(maxY, cameraY));
    }

    // ВОССТАНОВЛЕН МЕТОД drawTargetAndPath
    private void drawTargetAndPath(Graphics2D g) {
        // Отрисовка прицела для стрельбы
        if (targetShotX != -1 && targetShotY != -1) {
            int targetX = targetShotX * GameWorld.CELL_SIZE - cameraX;
            int targetY = targetShotY * GameWorld.CELL_SIZE - cameraY;

            g.setColor(new Color(255, 0, 0, 200));
            g.setStroke(new BasicStroke(3));
            g.drawLine(targetX + GameWorld.CELL_SIZE/2 - 15, targetY + GameWorld.CELL_SIZE/2,
                    targetX + GameWorld.CELL_SIZE/2 + 15, targetY + GameWorld.CELL_SIZE/2);
            g.drawLine(targetX + GameWorld.CELL_SIZE/2, targetY + GameWorld.CELL_SIZE/2 - 15,
                    targetX + GameWorld.CELL_SIZE/2, targetY + GameWorld.CELL_SIZE/2 + 15);
            g.drawOval(targetX + GameWorld.CELL_SIZE/2 - 10, targetY + GameWorld.CELL_SIZE/2 - 10, 20, 20);

            g.setColor(isAimingMode ? Color.ORANGE : Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(isAimingMode ? "ПРИЦЕЛЬНЫЙ" : "БЕГЛЫЙ",
                    targetX + GameWorld.CELL_SIZE/2 - 25, targetY - 5);
        }

        // Отрисовка пути
        // В drawTargetAndPath(), обновите блок отрисовки пути:

// Отрисовка пути
        if (!currentPath.isEmpty()) {
            g.setColor(new Color(255, 255, 0, 150));
            g.setStroke(new BasicStroke(4));

            Point prev = null;
            for (Point cell : currentPath) {
                int cellX = cell.x * GameWorld.CELL_SIZE - cameraX;
                int cellY = cell.y * GameWorld.CELL_SIZE - cameraY;

                g.setColor(new Color(255, 255, 0, 80));
                g.fillRect(cellX, cellY, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);

                if (prev != null) {
                    g.setColor(new Color(255, 255, 0, 200));
                    int fromX = prev.x * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE/2 - cameraX;
                    int fromY = prev.y * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE/2 - cameraY;
                    int toX = cell.x * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE/2 - cameraX;
                    int toY = cell.y * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE/2 - cameraY;
                    g.drawLine(fromX, fromY, toX, toY);
                    drawArrow(g, fromX, fromY, toX, toY);
                }
                prev = cell;
            }

            // Добавляем информацию о стоимости пути
            if (!currentPath.isEmpty()) {
                int totalCost = currentPath.size() * player.moveCost;
                Point firstCell = currentPath.get(0);
                int firstX = firstCell.x * GameWorld.CELL_SIZE - cameraX;
                int firstY = firstCell.y * GameWorld.CELL_SIZE - cameraY;

                g.setColor(new Color(0, 0, 0, 180));
                g.fillRoundRect(firstX, firstY - 25, 160, 22, 8, 8);

                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString("Стоимость: " + totalCost + " ⭐", firstX + 5, firstY - 10);

                if (player.movePoints < totalCost) {
                    g.setColor(Color.RED);
                    int maxSteps = player.movePoints / player.moveCost;
                    g.drawString(" (хватит на " + maxSteps + " из " + currentPath.size() + " шагов)",
                            firstX + 85, firstY - 10);
                } else {
                    g.setColor(Color.GREEN);
                    g.drawString(" ✅ хватит", firstX + 85, firstY - 10);
                }
            }
        }

        // Отрисовка целевой клетки
        if (targetGridX != -1 && targetGridY != -1) {
            int targetX = targetGridX * GameWorld.CELL_SIZE - cameraX;
            int targetY = targetGridY * GameWorld.CELL_SIZE - cameraY;

            g.setColor(new Color(255, 0, 0, 100));
            g.fillRect(targetX, targetY, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(3));
            g.drawRect(targetX, targetY, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);
        }
    }

    private void drawArrow(Graphics2D g, int fromX, int fromY, int toX, int toY) {
        int arrowSize = 8;
        double angle = Math.atan2(toY - fromY, toX - fromX);
        int arrowX = (int)(toX - Math.cos(angle) * GameWorld.CELL_SIZE/3);
        int arrowY = (int)(toY - Math.sin(angle) * GameWorld.CELL_SIZE/3);
        int x1 = (int)(arrowX - arrowSize * Math.cos(angle - Math.PI / 6));
        int y1 = (int)(arrowY - arrowSize * Math.sin(angle - Math.PI / 6));
        int x2 = (int)(arrowX - arrowSize * Math.cos(angle + Math.PI / 6));
        int y2 = (int)(arrowY - arrowSize * Math.sin(angle + Math.PI / 6));
        g.drawLine(arrowX, arrowY, x1, y1);
        g.drawLine(arrowX, arrowY, x2, y2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int viewWidth = getWidth(), viewHeight = getHeight();
        updateCamera(viewWidth, viewHeight);

        g2d.drawImage(fieldCache, 0, 0, viewWidth, viewHeight, cameraX, cameraY, cameraX + viewWidth, cameraY + viewHeight, null);

        // Рисуем холмы
        for (entities.Hill hill : world.getHills()) {
            int x = hill.gridX * GameWorld.CELL_SIZE - cameraX;
            int y = hill.gridY * GameWorld.CELL_SIZE - cameraY;
            if (x + GameWorld.CELL_SIZE > 0 && x < viewWidth && y + GameWorld.CELL_SIZE > 0 && y < viewHeight) {
                drawHill(g2d, hill.gridX, hill.gridY, hill.height);
            }
        }

        drawWinZone(g2d);

        // Отрисовка асфальта (под всеми объектами)
        for (Pavement pavement : world.getPavements()) {
            pavement.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, pavementImage);
        }

        for (OakPlanks oakPlank : world.getOakPlanks()) {
            oakPlank.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, oakPlanksImage);
        }

        for (IronFloor ironFloor : world.getIronFloors()) {
            ironFloor.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, ironFloorImage);
        }

        for (InfernalLand land : world.getInfernalLands()) {
            land.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, infernalLandImage);
        }

        for (Water water : world.getWaters()) {
            water.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, waterImage);
        }

        for (Door door : world.getDoors()) {
            BufferedImage doorImage = getDoorImage(door.color);
            door.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, doorImage);
        }

        // После отрисовки асфальта, но до стен
        for (StorageChest chest : world.getStorageChests()) {
            chest.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, bedsideTableImage);
        }

        for (LootDrop drop : world.getLootDrops()) {
            if (drop.isAlive && !drop.isEmpty()) {
                drop.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE);
            }
        }

        // Вместо текущей отрисовки контейнеров, используйте:
        for (GarbageContainer container : world.getGarbageContainers()) {
            container.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, dumpsterImage);
        }

        for (Wall wall : world.getWalls()) {
            if (wall.isAlive()) {
                wall.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, wallImage, ironBlockImage);
            }
        }

        for (Tree tree : world.getTrees()) {
            if (tree.isAlive) {
                tree.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, tree1Image, tree2Image);
            }
        }

        for (Water water : world.getWaters()) {
            water.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, waterImage);
        }

        // ПОДСВЕТКА ВРАГА ПОД МЫШЬЮ
        drawEnemyHoverHighlight(g2d);

        // ПРИЦЕЛ НА ВЫБРАННОЙ ЦЕЛИ
        drawTargetingCrosshair(g2d);

        // Рисуем путь и цель
        drawTargetAndPath(g2d);
        drawFriendlyPath(g2d);

        // Рисуем видимых врагов
        // В paintComponent, найдите блок отрисовки врагов и замените его:

// Рисуем видимых врагов
        for (Enemy enemy : world.getEnemies()) {
            // ===== ПРОВЕРКА ВИДИМОСТИ ОТ КОМАНДЫ =====
            boolean isVisible = world.isEnemyVisibleByTeam(enemy);

            if (!enemy.isAlive || !isVisible) continue;

            int x = enemy.getAnimatedX(GameWorld.CELL_SIZE, GameWorld.TANK_SIZE) - cameraX;
            int y = enemy.getAnimatedY(GameWorld.CELL_SIZE, GameWorld.TANK_SIZE) - cameraY;

            if (x + GameWorld.TANK_SIZE > 0 && x < viewWidth && y + GameWorld.TANK_SIZE > 0 && y < viewHeight) {
                BufferedImage img = null;

                if ("R_Otsu".equals(enemy.type)) {
                    switch(enemy.direction) {
                        case UP: img = rOtsuEnemyUp; break;
                        case DOWN: img = rOtsuEnemyDown; break;
                        case LEFT: img = rOtsuEnemyLeft; break;
                        case RIGHT: img = rOtsuEnemyRight; break;
                    }
                } else if ("M14_41".equals(enemy.type)) {
                    switch(enemy.direction) {
                        case UP: img = m14_41EnemyUp; break;
                        case DOWN: img = m14_41EnemyDown; break;
                        case LEFT: img = m14_41EnemyLeft; break;
                        case RIGHT: img = m14_41EnemyRight; break;
                    }
                } else if ("H35".equals(enemy.type)) {
                    switch(enemy.direction) {
                        case UP: img = h35EnemyUp; break;
                        case DOWN: img = h35EnemyDown; break;
                        case LEFT: img = h35EnemyLeft; break;
                        case RIGHT: img = h35EnemyRight; break;
                    }
                } else if ("FT".equals(enemy.type)) {
                    switch(enemy.direction) {
                        case UP: img = ftEnemyUp; break;
                        case DOWN: img = ftEnemyDown; break;
                        case LEFT: img = ftEnemyLeft; break;
                        case RIGHT: img = ftEnemyRight; break;
                    }
                } else if ("Fiat3000".equals(enemy.type)) {
                    switch (enemy.direction) {
                        case UP:
                            img = fiat3000EnemyUp;
                            break;
                        case DOWN:
                            img = fiat3000EnemyDown;
                            break;
                        case LEFT:
                            img = fiat3000EnemyLeft;
                            break;
                        case RIGHT:
                            img = fiat3000EnemyRight;
                            break;
                    }
                } else {
                    switch (enemy.direction) {
                        case UP: img = enemyUp; break;
                        case DOWN: img = enemyDown; break;
                        case LEFT: img = enemyLeft; break;
                        case RIGHT: img = enemyRight; break;
                    }
                }

                if (img != null) {
                    g2d.drawImage(img, x, y, GameWorld.TANK_SIZE, GameWorld.TANK_SIZE, null);
                }

                // Полоска здоровья
                float healthPercent = (float)enemy.health / enemy.maxHealth;
                int healthBarWidth = (int)(GameWorld.TANK_SIZE * healthPercent);
                g2d.setColor(new Color(60, 60, 60));
                g2d.fillRect(x, y - 8, GameWorld.TANK_SIZE, 5);
                g2d.setColor(healthPercent > 0.6f ? new Color(0, 200, 0) : (healthPercent > 0.3f ? new Color(255, 200, 0) : new Color(200, 0, 0)));
                g2d.fillRect(x, y - 8, healthBarWidth, 5);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 9));
                g2d.drawString(enemy.health + "/" + enemy.maxHealth, x + 5, y - 10);
            }
        }

        // В paintComponent, после отрисовки врагов, добавьте (только для отладки):

        for (QuestNPC npc : world.getQuestNPCs()) {
            // Определяем, какое изображение использовать
            BufferedImage npcImg;
            if ("Sav m/43".equals(npc.name)) {
                npcImg = savM43Image;
            } else {
                npcImg = npcImage;  // T18
            }
            npc.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, GameWorld.TANK_SIZE, npcImg, player);
        }

        for (Trader trader : world.getTraders()) {
            trader.draw(g2d, cameraX, cameraY, GameWorld.CELL_SIZE, GameWorld.TANK_SIZE,
                    traderImage, isPlayerActive() ? player : getActiveFriendly());
        }

        // В paintComponent, после отрисовки NPC, добавьте отрисовку союзников:
        // Отрисовка союзников
        // Отрисовка союзников
        // Отрисовка союзников
        // В paintComponent, после отрисовки NPC, добавьте отрисовку союзников:
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isAlive) {
                BufferedImage imgToUse = null;

                int drawX, drawY;
                if (isFriendlyMoving && movingFriendlyForAnimation == friendly) {
                    float t = (float)friendlyMoveProgress / 200f;
                    drawX = (int)(friendlyMoveFromX + (friendlyMoveToX - friendlyMoveFromX) * t) - cameraX;
                    drawY = (int)(friendlyMoveFromY + (friendlyMoveToY - friendlyMoveFromY) * t) - cameraY;
                } else {
                    drawX = friendly.gridX * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2 - cameraX;
                    drawY = friendly.gridY * GameWorld.CELL_SIZE + (GameWorld.CELL_SIZE - GameWorld.TANK_SIZE) / 2 - cameraY;
                }

                if ("M53".equals(friendly.type)) {
                    if (friendly.isRecruited) {
                        switch(friendly.currentDirection) {
                            case UP: imgToUse = m53Up; break;
                            case DOWN: imgToUse = m53Down; break;
                            case LEFT: imgToUse = m53Left; break;
                            case RIGHT: imgToUse = m53Right; break;
                            default: imgToUse = m53Right;
                        }
                    } else {
                        imgToUse = m53Image;
                    }
                }
                else if ("VK10001P".equals(friendly.type)) {  // ← ДОБАВЬТЕ ЭТУ ВЕТКУ ПЕРВОЙ (ДО MS-1)
                    if (friendly.isRecruited) {
                        switch(friendly.currentDirection) {
                            case UP: imgToUse = vk10001pUp; break;
                            case DOWN: imgToUse = vk10001pDown; break;
                            case LEFT: imgToUse = vk10001pLeft; break;
                            case RIGHT: imgToUse = vk10001pRight; break;
                            default: imgToUse = vk10001pRight;
                        }
                    } else {
                        // Для ненаймлённого VK 100.01 P используем его же изображение (или статичное)
                        imgToUse = vk10001pRight;
                    }
                }
                else if ("MS-1".equals(friendly.type)) {
                    if (friendly.isRecruited) {
                        switch(friendly.currentDirection) {
                            case UP: imgToUse = ms1Up; break;
                            case DOWN: imgToUse = ms1Down; break;
                            case LEFT: imgToUse = ms1Left; break;
                            case RIGHT: imgToUse = ms1Right; break;
                            default: imgToUse = ms1Right;
                        }
                    } else {
                        imgToUse = friendlyImage;
                    }
                }
                else if ("AMX40".equals(friendly.type)) {
                    if (friendly.isRecruited) {
                        switch(friendly.currentDirection) {
                            case UP: imgToUse = amx40Up; break;
                            case DOWN: imgToUse = amx40Down; break;
                            case LEFT: imgToUse = amx40Left; break;
                            case RIGHT: imgToUse = amx40Right; break;
                            default: imgToUse = amx40Right;
                        }
                    } else {
                        imgToUse = amx40Right;
                    }
                } else if ("T1".equals(friendly.type)) {
                    if (friendly.isRecruited) {
                        switch(friendly.currentDirection) {
                            case UP: imgToUse = t1Up; break;
                            case DOWN: imgToUse = t1Down; break;
                            case LEFT: imgToUse = t1Left; break;
                            case RIGHT: imgToUse = t1Right; break;
                            default: imgToUse = t1Right;
                        }
                    } else {
                        imgToUse = t1Right;
                    }
                } else {
                    imgToUse = friendlyImage;
                }

                if (imgToUse != null) {
                    g2d.drawImage(imgToUse, drawX, drawY, GameWorld.TANK_SIZE, GameWorld.TANK_SIZE, null);
                } else {
                    g2d.setColor(new Color(0, 100, 200));
                    g2d.fillRoundRect(drawX, drawY, GameWorld.TANK_SIZE, GameWorld.TANK_SIZE, 10, 10);
                }
            }
        }

        drawVisionRadius(g2d);

        // Рисуем танк с учётом анимации
        drawTank(g2d, getAnimatedX() - cameraX, getAnimatedY() - cameraY);

        for (combat.Projectile p : activeProjectiles) {
            p.draw(g2d, cameraX, cameraY);
        }

        // НОВЫЙ УЛУЧШЕННЫЙ HUD
        drawImprovedHUD(g2d);
        // Отрисовка панелей союзников
        drawAlliedUnitsPanels(g2d);

        // ИНДИКАТОР РЕЖИМА СТРЕЛЬБЫ
        drawFireModeIndicator(g2d);

        // Дополнительная информация о выбранной цели
        // Дополнительная информация о выбранной цели
        if (targetShotX != -1 && targetShotY != -1) {
            double hitChance;
            if (isPlayerActive()) {
                hitChance = calculateHitProbability(targetShotX, targetShotY);
            } else {
                FriendlyUnit active = getActiveFriendly();
                if (active != null) {
                    hitChance = calculateHitProbabilityForFriendly(active, targetShotX, targetShotY);
                } else {
                    hitChance = 0;
                }
            }

            g.setColor(new Color(0, 0, 0, 200));
            g.fillRoundRect(getWidth() - 220, getHeight() - 80, 210, 70, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 11));
            g.drawString("ВЫБРАНА ЦЕЛЬ: [" + targetShotX + "," + targetShotY + "]", getWidth() - 210, getHeight() - 60);
            g.drawString("Шанс попадания: " + (int)(hitChance * 100) + "%", getWidth() - 210, getHeight() - 45);
            g.drawString("Нажми ПРОБЕЛ для выстрела", getWidth() - 210, getHeight() - 30);
            g.setColor(Color.YELLOW);
            g.drawString("R - сбросить цель", getWidth() - 210, getHeight() - 15);
        }

        // Счётчик обнаруженных врагов
        drawEnemyCounter(g2d);

        drawEnemyTurnOverlay(g2d);

        drawEnemyTurnOverlay(g2d);

        if (debugMode && musicManager != null) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(getWidth() - 200, getHeight() - 30, 190, 22);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            String status = musicManager.isFightMusicPlaying() ? "🎵 БОЕВАЯ" : "🎵 ТИХАЯ";
            g2d.drawString("Музыка: " + status, getWidth() - 190, getHeight() - 14);
        }
    }

    // Конвертер направления из PlayerTank.Direction в FriendlyUnit.Direction
    private FriendlyUnit.Direction convertDirection(PlayerTank.Direction dir) {
        switch(dir) {
            case UP: return FriendlyUnit.Direction.UP;
            case DOWN: return FriendlyUnit.Direction.DOWN;
            case LEFT: return FriendlyUnit.Direction.LEFT;
            case RIGHT: return FriendlyUnit.Direction.RIGHT;
            default: return FriendlyUnit.Direction.RIGHT;
        }
    }

    private void drawEnemyTurnOverlay(Graphics2D g) {
        if (world.isEnemyTurn()) {
            // Полупрозрачное затемнение
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Текст "ХОД ВРАГА"
            g.setColor(new Color(255, 100, 100));
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String text = "ХОД ВРАГА";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            g.drawString(text, (getWidth() - textWidth) / 2, (getHeight() - textHeight) / 2);

            // Маленькая анимация точек
            long time = System.currentTimeMillis();
            int dots = (int)((time / 500) % 4);
            String dotsText = ".".repeat(dots);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString(dotsText, (getWidth() + textWidth) / 2 + 10, (getHeight() - textHeight) / 2);
        }
    }

    private void setDefaultFriendlyTextures(FriendlyUnit friendly) {
        if ("MS-1".equals(friendly.type)) {
            if (ms1Right == null) ms1Right = createPlaceholderTank("M→");
            if (ms1Left == null) ms1Left = createPlaceholderTank("M←");
            if (ms1Up == null) ms1Up = createPlaceholderTank("M↑");
            if (ms1Down == null) ms1Down = createPlaceholderTank("M↓");
        } else if ("M53".equals(friendly.type)) {
            if (m53Right == null) m53Right = createPlaceholderTank("M→");
            if (m53Left == null) m53Left = createPlaceholderTank("M←");
            if (m53Up == null) m53Up = createPlaceholderTank("M↑");
            if (m53Down == null) m53Down = createPlaceholderTank("M↓");
        } else if ("VK10001P".equals(friendly.type)) {
            if (vk10001pRight == null) vk10001pRight = createPlaceholderTank("V→");
            if (vk10001pLeft == null) vk10001pLeft = createPlaceholderTank("V←");
            if (vk10001pUp == null) vk10001pUp = createPlaceholderTank("V↑");
            if (vk10001pDown == null) vk10001pDown = createPlaceholderTank("V↓");
        } else if ("AMX40".equals(friendly.type)) {
            if (amx40Right == null) amx40Right = createPlaceholderTank("A→");
            if (amx40Left == null) amx40Left = createPlaceholderTank("A←");
            if (amx40Up == null) amx40Up = createPlaceholderTank("A↑");
            if (amx40Down == null) amx40Down = createPlaceholderTank("A↓");
        } else if ("T1".equals(friendly.type)) {
            if (t1Right == null) t1Right = createPlaceholderTank("A→");
            if (t1Left == null) t1Left = createPlaceholderTank("A←");
            if (t1Up == null) t1Up = createPlaceholderTank("A↑");
            if (t1Down == null) t1Down = createPlaceholderTank("A↓");
        }
    }

    public void updateFriendlyPortrait(FriendlyUnit friendly) {
        if (friendly == null) return;

        String portraitPath = friendly.getPortraitPath();
        File portraitFile = new File(portraitPath);

        try {
            if (portraitFile.exists()) {
                BufferedImage portrait = ImageIO.read(portraitFile);
                if ("MS-1".equals(friendly.type)) {
                    ms1Portrait = portrait;
                } else if ("M53".equals(friendly.type)) {
                    m53Portrait = portrait;
                } else if ("VK10001P".equals(friendly.type)) {
                    vk10001pPortrait = portrait;
                } else if ("AMX40".equals(friendly.type)) {
                    amx40Portrait = portrait;
                } else if ("T1".equals(friendly.type)) {
                    t1Portrait = portrait;
                }
                System.out.println("✅ Портрет обновлён для " + friendly.name + ": " + portraitFile.getName());
            } else {
                // Используем портрет по умолчанию
                loadBasicFriendlyPortrait(friendly.type);
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки портрета для " + friendly.name + ": " + e.getMessage());
            loadBasicFriendlyPortrait(friendly.type);
        }
    }

    private void loadDefaultFriendlyPortrait(FriendlyUnit friendly) {
        String defaultPath = "src/PositiveHeroes/ImageOfHeroes/" + friendly.type + ".png";
        try {
            File defaultFile = new File(defaultPath);
            if (defaultFile.exists()) {
                BufferedImage portrait = ImageIO.read(defaultFile);
                if ("MS-1".equals(friendly.type)) {
                    ms1Portrait = portrait;
                } else if ("M53".equals(friendly.type)) {
                    m53Portrait = portrait;
                } else if ("VK10001P".equals(friendly.type)) {
                    vk10001pPortrait = portrait;
                } else if ("AMX40".equals(friendly.type)) {
                    amx40Portrait = portrait;
                } else if ("T1".equals(friendly.type)) {
                    t1Portrait = portrait;
                }
                System.out.println("✅ Загружен стандартный портрет для " + friendly.name);
            }
        } catch (IOException e) {
            System.err.println("Не удалось загрузить стандартный портрет для " + friendly.name);
        }
    }



    // Шанс попадания для союзника
    // Шанс попадания для союзника (теперь такой же, как у игрока)
    private double calculateHitProbabilityForFriendly(FriendlyUnit friendly, int targetX, int targetY) {
        int dx = Math.abs(friendly.gridX - targetX);
        int dy = Math.abs(friendly.gridY - targetY);
        double distance = Math.sqrt(dx*dx + dy*dy);

        int agility = friendly.agility;
        int weaponAccuracy = friendly.weaponAccuracy;

        AmmoItem currentAmmo = friendly.getCurrentAmmo();
        double accuracyMultiplier = (currentAmmo != null) ? currentAmmo.getAccuracyMultiplier() : 1.0;

        double baseHitChance = 0.30 + (weaponAccuracy / 100.0) * 0.65;
        baseHitChance = Math.min(0.95, baseHitChance);
        double distancePenalty = Math.exp(-distance / 25.0);
        double agilityBonus = 0.05 + (agility / 500.0);
        agilityBonus = Math.min(0.15, agilityBonus);

        double hitChance = baseHitChance * distancePenalty * accuracyMultiplier + agilityBonus;

        if (isAimingMode) {
            hitChance = hitChance * 1.2;
        }

        // Бонус холма для союзника
        for (Hill hill : world.getHills()) {
            if (hill.gridX == friendly.gridX && hill.gridY == friendly.gridY) {
                hitChance *= (1.0 + hill.height * 0.1);
                break;
            }
        }

        hitChance = Math.min(0.95, Math.max(0.05, hitChance));
        return hitChance;
    }

    // ДОБАВЬТЕ МЕТОДЫ MouseMotionListener
    @Override
    public void mouseMoved(MouseEvent e) {
        int mouseGridX = (e.getX() + cameraX) / GameWorld.CELL_SIZE;
        int mouseGridY = (e.getY() + cameraY) / GameWorld.CELL_SIZE;

        // ===== ДОБАВЬТЕ ПРОВЕРКУ ОБНАРУЖЕНИЯ ПРИ НАВЕДЕНИИ =====
        if (!isPlayerActive()) {
            FriendlyUnit active = getActiveFriendly();
            if (active != null && "M53".equals(active.type)) {
                // Обновляем видимость от позиции M53
                //world.updateVisibilityFromPosition(active.gridX, active.gridY, true);
                //checkEnemyDetection();
            }
        }

        // Проверяем врагов
        hoveredEnemy = null;
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isAlive && enemy.gridX == mouseGridX && enemy.gridY == mouseGridY) {
                // ===== ИСПРАВЛЕНИЕ: используем видимость от команды =====
                boolean isVisible = world.isEnemyVisibleByTeam(enemy);
                if (isVisible) {
                    hoveredEnemy = enemy;
                    break;
                }
            }
        }

        // Проверяем NPC
        boolean isNearNPC = false;
        for (QuestNPC npc : world.getQuestNPCs()) {
            if (npc.gridX == mouseGridX && npc.gridY == mouseGridY) {
                // ===== ПРОВЕРЯЕМ РАССТОЯНИЕ ОТ АКТИВНОГО ЮНИТА =====
                int unitX, unitY;
                if (isPlayerActive()) {
                    unitX = player.gridX;
                    unitY = player.gridY;
                } else {
                    FriendlyUnit active = getActiveFriendly();
                    if (active == null) continue;
                    unitX = active.gridX;
                    unitY = active.gridY;
                }

                int dx = Math.abs(unitX - npc.gridX);
                int dy = Math.abs(unitY - npc.gridY);
                boolean isAdjacent = (dx == 1 && dy == 0) || (dx == 0 && dy == 1);

                if (isAdjacent) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                } else {
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
            }
        }

        // Обновляем курсор
        if (hoveredEnemy != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }

        repaint();
    }

    private void drawHill(Graphics2D g, int gridX, int gridY, int height) {
        int drawX = gridX * GameWorld.CELL_SIZE - cameraX;
        int drawY = gridY * GameWorld.CELL_SIZE - cameraY;
        if (hillImageLoaded && hillImage != null) {
            g.drawImage(hillImage, drawX, drawY, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE, null);
        } else {
            g.setColor(new Color(101, 67, 33));
            g.fillOval(drawX + 5, drawY + GameWorld.CELL_SIZE - 15, GameWorld.CELL_SIZE - 10, 15);
            g.setColor(new Color(139, 90, 43));
            int hillHeight = 10 + height * 8;
            int[] xPoints = {drawX + GameWorld.CELL_SIZE/2, drawX + GameWorld.CELL_SIZE/2 - 12 - height * 2, drawX + GameWorld.CELL_SIZE/2 + 12 + height * 2};
            int[] yPoints = {drawY + GameWorld.CELL_SIZE - 15 - hillHeight, drawY + GameWorld.CELL_SIZE - 5, drawY + GameWorld.CELL_SIZE - 5};
            g.fillPolygon(xPoints, yPoints, 3);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("H" + height, drawX + GameWorld.CELL_SIZE - 15, drawY + 15);
    }

    private void drawVisionRadius(Graphics2D g) {

        // Если чит на полную видимость включён - не рисуем радиус обзора
        if (world.isFullMapRevealed()) {
            return;
        }

        int centerX, centerY;
        int radius = GameWorld.VIEW_RADIUS * GameWorld.CELL_SIZE;

        // Определяем, для кого рисовать поле зрения
        if (isPlayerActive()) {
            // Рисуем для игрока
            centerX = getAnimatedX() + GameWorld.TANK_SIZE/2 - cameraX;
            centerY = getAnimatedY() + GameWorld.TANK_SIZE/2 - cameraY;
        } else {
            // Рисуем для активного союзника
            FriendlyUnit active = getActiveFriendly();
            if (active != null && active.isAlive) {
                // Получаем координаты союзника с учётом анимации
                int drawX = getFriendlyAnimatedX(active);
                int drawY = getFriendlyAnimatedY(active);
                centerX = drawX + GameWorld.TANK_SIZE/2 - cameraX;
                centerY = drawY + GameWorld.TANK_SIZE/2 - cameraY;
            } else {
                return;
            }
        }

        // Рисуем свечение
        g.setColor(new Color(100, 150, 255, 40));
        g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        g.setColor(new Color(100, 150, 255, 150));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    private void drawTank(Graphics2D g, int x, int y) {
        if (currentTankImage != null) {
            g.drawImage(currentTankImage, x, y, GameWorld.TANK_SIZE, GameWorld.TANK_SIZE, null);
        }
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(x + 10, y + GameWorld.TANK_SIZE - 5, GameWorld.TANK_SIZE - 20, 8);
    }

    private void drawHUD(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(5, 5, 220, 140, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Позиция: [" + player.gridX + ", " + player.gridY + "]", 15, 25);
        g.drawString("❤ " + player.health + "/130", 15, 45);
        g.drawString("⭐ " + player.movePoints + "/" + player.maxMovePoints, 15, 65);
        g.drawString("Сила: " + player.strength + " | Ловк: " + player.agility, 15, 85);
        g.drawString("Стоимость шага: " + player.moveCost + " о.х.", 15, 105);
        if (player.isOnHill) g.drawString("✦ Бонус +" + (player.currentHillBonus * 10) + "%", 15, 125);

        // Подсказки
        g.setColor(new Color(200, 200, 200));
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString("WASD - движение | Клик по танку - голос | E - ход | F - огонь", 15, getHeight() - 10);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateMove();
        updateFriendlyMove();
        updateEnemyAnimations();
        updateProjectiles();

        // ===== ЦЕНТРИРУЕМ КАМЕРУ ТОЛЬКО НА ВИДИМОМ ВРАГЕ =====
        if (world.isEnemyTurn()) {
            if (currentActiveEnemy != null && currentActiveEnemy.isAlive) {
                // ДОПОЛНИТЕЛЬНАЯ ПРОВЕРКА ВИДИМОСТИ
                if (world.isEnemyVisibleByAnyone(currentActiveEnemy)) {
                    centerCameraOnEnemy(currentActiveEnemy);
                }
            }
        }
        // ==================================================

        repaint();

        if (needsDetectionCheck) {
            checkEnemyDetection();
            needsDetectionCheck = false;
        }
    }

    // В GamePanel.java, в методе updateProjectiles(), измените:

    // В GamePanel.java, в updateProjectiles, измените:

    private void updateProjectiles() {
        List<combat.Projectile> finishedProjectiles = new ArrayList<>();
        List<Enemy> killedEnemies = new ArrayList<>();
        Map<Enemy, FriendlyUnit> killedByFriendly = new HashMap<>();
        Map<Enemy, Enemy> killedByEnemy = new HashMap<>();  // ← НОВАЯ КАРТА для убийц-врагов

        for (combat.Projectile p : activeProjectiles) {
            boolean finished = p.update();
            if (finished) {
                Enemy killedEnemy = p.applyDamageAndGetKilledEnemy();
                if (killedEnemy != null) {
                    killedEnemies.add(killedEnemy);
                    if (!p.isPlayerShot() && p.getShooterFriendly() != null) {
                        // Убил союзник
                        killedByFriendly.put(killedEnemy, p.getShooterFriendly());
                        killedByEnemy.put(killedEnemy, null);
                    } else if (!p.isPlayerShot() && p.getShooterEnemy() != null) {
                        // Убил другой враг (межфракционная атака)
                        killedByEnemy.put(killedEnemy, p.getShooterEnemy());
                        killedByFriendly.put(killedEnemy, null);
                    } else {
                        // Убил игрок
                        killedByFriendly.put(killedEnemy, null);
                        killedByEnemy.put(killedEnemy, null);
                    }
                }
                finishedProjectiles.add(p);
            }
        }

        // Если есть активный снаряд от врага, центрируем камеру на стреляющем КАЖДЫЙ КАДР
        for (combat.Projectile p : activeProjectiles) {
            if (!p.isPlayerShot() && p.getShooterEnemy() != null) {
                Enemy shooter = p.getShooterEnemy();
                if (currentActiveEnemy == shooter && world.isEnemyVisibleByAnyone(shooter)) {
                    centerCameraOnEnemy(shooter);
                    break;
                }
            }
        }

        // Обрабатываем убитых врагов
        for (Enemy enemy : killedEnemies) {
            if (enemy != null && !enemy.isAlive) {
                System.out.println("=== ВРАГ УНИЧТОЖЕН! ===");

                // ===== ПРОВЕРЯЕМ, КТО УБИЛ =====
                Enemy killerEnemy = killedByEnemy.get(enemy);
                FriendlyUnit killerFriendly = killedByFriendly.get(enemy);

                // ЕСЛИ УБИЛ ДРУГОЙ ВРАГ - НЕ ДАЁМ НАГРАДУ!
                if (killerEnemy != null) {
                    System.out.println("⚠️ " + enemy.type + " убит другим врагом (" + killerEnemy.type +
                            " из фракции " + killerEnemy.getFaction().displayName + ")!");
                    System.out.println("❌ Награда (опыт и дроп) НЕ НАЧИСЛЯЕТСЯ команде!");
                    // НЕ вызываем world.dropLootFromEnemy()
                    // НЕ вызываем distributeExperience()
                    continue;
                }

                // Только если убил игрок или союзник - даём награду
                world.dropLootFromEnemy(enemy);
                distributeExperience(enemy, killerFriendly);

                // Воспроизводим звук
                if (killerFriendly == null) {
                    soundManager.playEnemyDestroyedSound();
                } else if ("M53".equals(killerFriendly.type)) {
                    soundManager.playM53EnemyDestroyedSound();
                } else if ("MS-1".equals(killerFriendly.type)) {
                    soundManager.playMS1EnemyDestroyedSound();
                } else if ("VK10001P".equals(killerFriendly.type)) {
                    soundManager.playVK10001PEnemyDestroyedSound();
                } else if ("T1".equals(killerFriendly.type)) {
                    soundManager.playT1EnemyDestroyedSound();
                } else {
                    soundManager.playEnemyDestroyedSound();
                }
            }
        }

        activeProjectiles.removeAll(finishedProjectiles);

        // Проверяем, не умер ли игрок от попадания снаряда
        if (player.health <= 0 && !gameOver) {
            System.out.println("💀 Leichttraktor уничтожен от снаряда! Игра окончена.");
            SwingUtilities.invokeLater(() -> showGameOverDialog());
            return;
        }

        if (!activeProjectiles.isEmpty()) {
            repaint();
        }
    }

    // ===== НОВЫЙ МЕТОД ДЛЯ РАСПРЕДЕЛЕНИЯ ОПЫТА =====
    private void distributeExperience(Enemy enemy, FriendlyUnit killer) {
        int totalExp = enemy.getBaseExperience();
        System.out.println("Уничтожен " + enemy.type + "! Опыт: " + totalExp);

        // Собираем всех живых членов команды (игрок + все нанятые союзники)
        List<Object> teamMembers = new ArrayList<>();

        // Добавляем игрока, если жив
        if (player.health > 0) {
            teamMembers.add(player);
        }

        // Добавляем всех нанятых живых союзников
        for (FriendlyUnit unit : world.getFriendlyUnits()) {
            if (unit.isAlive && unit.isRecruited) {
                teamMembers.add(unit);
            }
        }

        System.out.println("=== РАСПРЕДЕЛЕНИЕ ОПЫТА ===");
        System.out.println("Всего членов команды: " + teamMembers.size());
        System.out.println("Убийца: " + (killer == null ? "Leichttraktor" : killer.name));
        System.out.println("Общий опыт: " + totalExp);

        // ===== ЕСЛИ В КОМАНДЕ ТОЛЬКО 1 ЧЕЛОВЕК =====
        if (teamMembers.size() == 1) {
            Object soloMember = teamMembers.get(0);
            if (soloMember instanceof PlayerTank) {
                int oldLevel = ((PlayerTank) soloMember).getExperienceLevel();
                ((PlayerTank) soloMember).addExperience(totalExp);
                checkAndShowLevelUpDialogAfterExp(soloMember, ((PlayerTank) soloMember).getExperienceSystem(), oldLevel);
                System.out.println("✨ Leichttraktor получил " + totalExp + " опыта (один в команде)");
            } else if (soloMember instanceof FriendlyUnit) {
                int oldLevel = ((FriendlyUnit) soloMember).getExperienceLevel();
                ((FriendlyUnit) soloMember).addExperience(totalExp);
                checkAndShowLevelUpDialogAfterExp(soloMember, ((FriendlyUnit) soloMember).getExperienceSystem(), oldLevel);
                System.out.println("✨ " + ((FriendlyUnit) soloMember).name + " получил " + totalExp + " опыта (один в команде)");
            }
            System.out.println("=== РАСПРЕДЕЛЕНИЕ ЗАВЕРШЕНО ===\n");
            return;
        }

        // ===== ДЛЯ КОМАНДЫ ИЗ 2+ ЧЕЛОВЕК =====
        int killerExp = (int)(totalExp * 0.8);  // 80% убийце
        int remainingExp = totalExp - killerExp;  // 20% остальным

        System.out.println("Убийце (80%): " + killerExp);
        System.out.println("Остальным (20%): " + remainingExp);

        // Начисляем опыт убийце
        if (killer == null) {
            int oldLevel = player.getExperienceLevel();
            player.addExperience(killerExp);
            checkAndShowLevelUpDialogAfterExp(player, player.getExperienceSystem(), oldLevel);
            System.out.println("✨ Leichttraktor получил " + killerExp + " опыта (как убийца)");
        } else {
            int oldLevel = killer.getExperienceLevel();
            killer.addExperience(killerExp);
            checkAndShowLevelUpDialogAfterExp(killer, killer.getExperienceSystem(), oldLevel);
            System.out.println("✨ " + killer.name + " получил " + killerExp + " опыта (как убийца)");
        }

        // Распределяем оставшийся опыт между остальными членами команды
        int remainingMembersCount = teamMembers.size() - 1;  // минус убийца

        if (remainingMembersCount > 0 && remainingExp > 0) {
            // Удаляем убийцу из списка для распределения
            List<Object> otherMembers = new ArrayList<>();
            for (Object member : teamMembers) {
                if (killer == null && member == player) continue;
                if (killer != null && member == killer) continue;
                otherMembers.add(member);
            }

            if (!otherMembers.isEmpty()) {
                // Равномерно распределяем опыт
                double expPerMember = (double) remainingExp / otherMembers.size();
                int leftover = remainingExp;

                for (int i = 0; i < otherMembers.size(); i++) {
                    Object member = otherMembers.get(i);
                    int memberExp;

                    if (i == otherMembers.size() - 1) {
                        // Последнему отдаём остаток
                        memberExp = leftover;
                    } else {
                        memberExp = (int) Math.floor(expPerMember);
                        if (memberExp < 1) memberExp = 1;
                        leftover -= memberExp;
                    }

                    if (member instanceof PlayerTank) {
                        int oldLevel = ((PlayerTank) member).getExperienceLevel();
                        ((PlayerTank) member).addExperience(memberExp);
                        checkAndShowLevelUpDialogAfterExp(member, ((PlayerTank) member).getExperienceSystem(), oldLevel);
                        System.out.println("✨ Leichttraktor получил " + memberExp + " опыта (доля от команды)");
                    } else if (member instanceof FriendlyUnit) {
                        int oldLevel = ((FriendlyUnit) member).getExperienceLevel();
                        ((FriendlyUnit) member).addExperience(memberExp);
                        checkAndShowLevelUpDialogAfterExp(member, ((FriendlyUnit) member).getExperienceSystem(), oldLevel);
                        System.out.println("✨ " + ((FriendlyUnit) member).name + " получил " + memberExp + " опыта (доля от команды)");
                    }
                }
            }
        }

        System.out.println("=== РАСПРЕДЕЛЕНИЕ ЗАВЕРШЕНО ===\n");
    }

    // Добавьте этот метод для проверки и показа диалога после добавления опыта
    private void checkAndShowLevelUpDialogAfterExp(Object unit, ExperienceSystem expSystem, int oldLevel) {
        if (expSystem == null) return;

        int newLevel = expSystem.getLevel();

        System.out.println("=== checkAndShowLevelUpDialogAfterExp ===");
        System.out.println("unit: " + (unit instanceof PlayerTank ? "Player" : ((FriendlyUnit)unit).name));
        System.out.println("oldLevel: " + oldLevel + ", newLevel: " + newLevel);
        System.out.println("hasPendingBonuses: " + expSystem.hasPendingBonuses());

        // Если уровень повысился И есть ожидающие бонусы
        if (newLevel > oldLevel && expSystem.hasPendingBonuses()) {
            System.out.println("✅ ДИАЛОГ ДОЛЖЕН ПОКАЗАТЬСЯ!");
            SwingUtilities.invokeLater(() -> showLevelUpDialog(unit, expSystem));
        }
    }

    // В GamePanel.java добавьте метод:

    private void showLevelUpDialog(Object unit, ExperienceSystem expSystem) {
        if (!expSystem.hasPendingBonuses()) return;

        LevelUpBonus bonusSystem = expSystem.getPendingLevelUpBonus();
        String unitName = (unit instanceof PlayerTank) ? "Leichttraktor" : ((FriendlyUnit) unit).name;

        JDialog dialog = new JDialog(parentFrame, "🎉 ПОВЫШЕНИЕ УРОВНЯ - " + unitName, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(650, 750);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(20, 20, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Верхняя панель с информацией
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(20, 20, 30));

        JLabel titleLabel = new JLabel("🎉 " + unitName + " достиг " + expSystem.getLevel() + " уровня!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel bonusLabel = new JLabel("Осталось распределить бонусов: " + bonusSystem.getRemainingBonuses());
        bonusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        bonusLabel.setForeground(Color.CYAN);
        bonusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructionLabel = new JLabel("Выберите, какие характеристики улучшить:");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        instructionLabel.setForeground(Color.LIGHT_GRAY);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(titleLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        topPanel.add(bonusLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(instructionLabel);

        // Панель с кнопками бонусов (3 ряда по 3 кнопки)
        JPanel bonusesPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        bonusesPanel.setBackground(new Color(20, 20, 30));
        bonusesPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Текущие характеристики
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(new Color(30, 30, 40));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel statsTitle = new JLabel("ТЕКУЩИЕ ХАРАКТЕРИСТИКИ");
        statsTitle.setFont(new Font("Arial", Font.BOLD, 12));
        statsTitle.setForeground(new Color(255, 215, 0));
        statsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Создаём метки для всех характеристик
        JLabel healthStat, strengthStat, agilityStat, accuracyStat;
        JLabel armorStat, critStat, visionStat, reloadStat, dodgeStat;

        if (unit instanceof PlayerTank) {
            PlayerTank p = (PlayerTank) unit;
            healthStat = new JLabel("❤️ Здоровье: " + p.health + "/" + p.maxHealth);
            strengthStat = new JLabel("💪 Сила: " + p.strength);
            agilityStat = new JLabel("🦶 Ловкость: " + p.agility);
            accuracyStat = new JLabel("🎯 Точность оружия: " + p.weaponAccuracy);
            armorStat = new JLabel("🛡️ Броня: " + p.armor);
            critStat = new JLabel("⚡ Крит. шанс: +" + p.critBonus + "%");
            visionStat = new JLabel("👁️ Зрение: " + p.viewRadius + " кл.");
            reloadStat = new JLabel("🔄 Перезарядка: " + p.reloadCost + " о.х.");
            dodgeStat = new JLabel("💨 Уклонение: " + String.format("%.1f", p.dodgeChance) + "%");
        } else {
            FriendlyUnit f = (FriendlyUnit) unit;
            healthStat = new JLabel("❤️ Здоровье: " + f.health + "/" + f.maxHealth);
            strengthStat = new JLabel("💪 Сила: " + f.strength);
            agilityStat = new JLabel("🦶 Ловкость: " + f.agility);
            accuracyStat = new JLabel("🎯 Точность оружия: " + f.weaponAccuracy);
            armorStat = new JLabel("🛡️ Броня: " + f.armor);
            critStat = new JLabel("⚡ Крит. шанс: +" + f.critBonus + "%");
            visionStat = new JLabel("👁️ Зрение: " + f.viewRadius + " кл.");
            reloadStat = new JLabel("🔄 Перезарядка: " + f.reloadCost + " о.х.");
            dodgeStat = new JLabel("💨 Уклонение: " + String.format("%.1f", f.dodgeChance) + "%");
        }

        healthStat.setForeground(Color.WHITE);
        strengthStat.setForeground(Color.WHITE);
        agilityStat.setForeground(Color.WHITE);
        accuracyStat.setForeground(Color.WHITE);
        armorStat.setForeground(Color.WHITE);
        critStat.setForeground(Color.WHITE);
        visionStat.setForeground(Color.WHITE);
        reloadStat.setForeground(Color.WHITE);
        dodgeStat.setForeground(Color.WHITE);

        statsPanel.add(statsTitle);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        statsPanel.add(healthStat);
        statsPanel.add(strengthStat);
        statsPanel.add(agilityStat);
        statsPanel.add(accuracyStat);
        statsPanel.add(armorStat);
        statsPanel.add(critStat);
        statsPanel.add(visionStat);
        statsPanel.add(reloadStat);
        statsPanel.add(dodgeStat);

        // Создаём кнопки для ВСЕХ типов бонусов
        JButton healthButton = createBonusButton(LevelUpBonus.BonusType.HEALTH, bonusSystem, unit, bonusLabel,
                healthStat, strengthStat, agilityStat, accuracyStat, armorStat, critStat, visionStat, reloadStat, dodgeStat, dialog);
        JButton accuracyButton = createBonusButton(LevelUpBonus.BonusType.ACCURACY, bonusSystem, unit, bonusLabel,
                healthStat, strengthStat, agilityStat, accuracyStat, armorStat, critStat, visionStat, reloadStat, dodgeStat, dialog);
        JButton strengthButton = createBonusButton(LevelUpBonus.BonusType.STRENGTH, bonusSystem, unit, bonusLabel,
                healthStat, strengthStat, agilityStat, accuracyStat, armorStat, critStat, visionStat, reloadStat, dodgeStat, dialog);
        JButton agilityButton = createBonusButton(LevelUpBonus.BonusType.AGILITY, bonusSystem, unit, bonusLabel,
                healthStat, strengthStat, agilityStat, accuracyStat, armorStat, critStat, visionStat, reloadStat, dodgeStat, dialog);
        JButton armorButton = createBonusButton(LevelUpBonus.BonusType.ARMOR, bonusSystem, unit, bonusLabel,
                healthStat, strengthStat, agilityStat, accuracyStat, armorStat, critStat, visionStat, reloadStat, dodgeStat, dialog);
        JButton critButton = createBonusButton(LevelUpBonus.BonusType.CRITICAL, bonusSystem, unit, bonusLabel,
                healthStat, strengthStat, agilityStat, accuracyStat, armorStat, critStat, visionStat, reloadStat, dodgeStat, dialog);
        JButton visionButton = createBonusButton(LevelUpBonus.BonusType.VISION, bonusSystem, unit, bonusLabel,
                healthStat, strengthStat, agilityStat, accuracyStat, armorStat, critStat, visionStat, reloadStat, dodgeStat, dialog);
        JButton reloadButton = createBonusButton(LevelUpBonus.BonusType.RELOAD, bonusSystem, unit, bonusLabel,
                healthStat, strengthStat, agilityStat, accuracyStat, armorStat, critStat, visionStat, reloadStat, dodgeStat, dialog);
        JButton nimbleButton = createBonusButton(LevelUpBonus.BonusType.NIMBLE, bonusSystem, unit, bonusLabel,
                healthStat, strengthStat, agilityStat, accuracyStat, armorStat, critStat, visionStat, reloadStat, dodgeStat, dialog);

        bonusesPanel.add(healthButton);
        bonusesPanel.add(accuracyButton);
        bonusesPanel.add(strengthButton);
        bonusesPanel.add(agilityButton);
        bonusesPanel.add(armorButton);
        bonusesPanel.add(critButton);
        bonusesPanel.add(visionButton);
        bonusesPanel.add(reloadButton);
        bonusesPanel.add(nimbleButton);

        // Нижняя панель с кнопкой завершения
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(20, 20, 30));

        JButton finishButton = new JButton("✅ ЗАВЕРШИТЬ");
        finishButton.setFont(new Font("Arial", Font.BOLD, 12));
        finishButton.setBackground(new Color(0, 150, 0));
        finishButton.setForeground(Color.WHITE);
        finishButton.setFocusPainted(false);
        finishButton.addActionListener(e -> {
            if (bonusSystem.hasBonuses()) {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "У вас осталось " + bonusSystem.getRemainingBonuses() + " нераспределённых бонусов!\n" +
                                "Вы уверены, что хотите закрыть окно? Нераспределённые бонусы будут сохранены.",
                        "Предупреждение",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            expSystem.clearPendingBonuses();
            dialog.dispose();
        });

        bottomPanel.add(finishButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(bonusesPanel, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JButton createBonusButton(LevelUpBonus.BonusType type, LevelUpBonus bonusSystem,
                                      Object unit, JLabel bonusLabel,
                                      JLabel healthStat, JLabel strengthStat, JLabel agilityStat,
                                      JLabel accuracyStat, JLabel armorStat, JLabel critStat,
                                      JLabel visionStat, JLabel reloadStat, JLabel dodgeStat,
                                      JDialog dialog) {

        JButton button = new JButton("<html><center>" + type.name + "<br><font size='2'>" + type.description + "</font></center></html>");
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBackground(new Color(0, 100, 150));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(190, 70));

        button.addActionListener(e -> {
            if (bonusSystem.applyBonus(type, unit)) {
                // Обновляем отображение всех характеристик
                if (unit instanceof PlayerTank) {
                    PlayerTank p = (PlayerTank) unit;
                    healthStat.setText("❤️ Здоровье: " + p.health + "/" + p.maxHealth);
                    strengthStat.setText("💪 Сила: " + p.strength);
                    agilityStat.setText("🦶 Ловкость: " + p.agility);
                    accuracyStat.setText("🎯 Точность оружия: " + p.weaponAccuracy);
                    armorStat.setText("🛡️ Броня: " + p.armor);
                    critStat.setText("⚡ Крит. шанс: +" + p.critBonus + "%");
                    visionStat.setText("👁️ Зрение: " + p.viewRadius + " кл.");
                    reloadStat.setText("🔄 Перезарядка: " + p.reloadCost + " о.х.");
                    dodgeStat.setText("💨 Уклонение: " + String.format("%.1f", p.dodgeChance) + "%");
                } else {
                    FriendlyUnit f = (FriendlyUnit) unit;
                    healthStat.setText("❤️ Здоровье: " + f.health + "/" + f.maxHealth);
                    strengthStat.setText("💪 Сила: " + f.strength);
                    agilityStat.setText("🦶 Ловкость: " + f.agility);
                    accuracyStat.setText("🎯 Точность оружия: " + f.weaponAccuracy);
                    armorStat.setText("🛡️ Броня: " + f.armor);
                    critStat.setText("⚡ Крит. шанс: +" + f.critBonus + "%");
                    visionStat.setText("👁️ Зрение: " + f.viewRadius + " кл.");
                    reloadStat.setText("🔄 Перезарядка: " + f.reloadCost + " о.х.");
                    dodgeStat.setText("💨 Уклонение: " + String.format("%.1f", f.dodgeChance) + "%");
                }

                bonusLabel.setText("Осталось распределить бонусов: " + bonusSystem.getRemainingBonuses());

                // Если бонусов больше нет, закрываем диалог
                if (!bonusSystem.hasBonuses()) {
                    JOptionPane.showMessageDialog(dialog,
                            "🎉 Все бонусы за уровень распределены!",
                            "Поздравляем!",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                }
            }
        });

        return button;
    }

    private void checkAndShowLevelUpDialog(Object unit, ExperienceSystem expSystem) {
        if (expSystem != null && expSystem.hasPendingBonuses()) {
            SwingUtilities.invokeLater(() -> showLevelUpDialog(unit, expSystem));
        }
    }

    public void addProjectile(combat.Projectile projectile) {
        activeProjectiles.add(projectile);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Во время хода врага не обновляем подсветку
        if (world.isEnemyTurn()) {
            hoveredEnemy = null;
            setCursor(Cursor.getDefaultCursor());
            repaint();
            return;
        }

        // ===== ПОЛНАЯ БЛОКИРОВКА ВСЕХ ДЕЙСТВИЙ ВО ВРЕМЯ ХОДА ВРАГА =====
        if (world.isEnemyTurn()) {
            System.out.println("⏳ СЕЙЧАС ХОД ВРАГОВ! НЕЛЬЗЯ НИКУДА КЛИКАТЬ!");
            return;
        }


        if (gameOver) {
            System.out.println("Игра окончена! Действия заблокированы.");
            return;
        }

        if (player.health <= 0) {
            System.out.println("Игра окончена! Действия заблокированы.");
            return;
        }

        requestFocusInWindow();
        System.out.println("Mouse clicked, focus requested");

        // ===== ЗАПРЕТ ДЕЙСТВИЙ ВО ВРЕМЯ ДВИЖЕНИЯ =====
        if (isMoving || isAutoMoving) {
            System.out.println("⚠ Игрок двигается, подождите...");
            return;
        }

        if (isFriendlyMoving || isFriendlyAutoMoving) {
            System.out.println("⚠ Союзник двигается, подождите...");
            return;
        }

        if (world.isAnyEnemyMoving()) {
            System.out.println("⚠ Враги двигаются, подождите...");
            return;
        }
        // ===== КОНЕЦ ЗАПРЕТА =====

        if (isMoving || isAutoMoving) return;

        int mouseGridX = (e.getX() + cameraX) / GameWorld.CELL_SIZE;
        int mouseGridY = (e.getY() + cameraY) / GameWorld.CELL_SIZE;

        // Проверка на клик по NPC
        // В GamePanel.java, в методе mouseClicked, найдите блок проверки NPC и замените его:

// Проверка на клик по NPC
        for (QuestNPC npc : world.getQuestNPCs()) {
            if (npc.gridX == mouseGridX && npc.gridY == mouseGridY) {
                // ===== ИСПРАВЛЕНИЕ: проверяем расстояние от АКТИВНОГО юнита =====
                int unitX, unitY;
                if (isPlayerActive()) {
                    unitX = player.gridX;
                    unitY = player.gridY;
                } else {
                    FriendlyUnit active = getActiveFriendly();
                    if (active == null) return;
                    unitX = active.gridX;
                    unitY = active.gridY;
                }

                int dx = Math.abs(unitX - npc.gridX);
                int dy = Math.abs(unitY - npc.gridY);
                boolean isAdjacent = (dx == 1 && dy == 0) || (dx == 0 && dy == 1);

                if (isAdjacent) {
                    interactWithNPC(npc);
                } else {
                    String unitName = isPlayerActive() ? "Вы" : getActiveFriendly().name;
                    JOptionPane.showMessageDialog(this,
                            unitName + " слишком далеко от " + npc.name + "!\nПодойдите ближе, чтобы поговорить!",
                            "Слишком далеко",
                            JOptionPane.WARNING_MESSAGE);
                }
                return;
            }
        }

        // Проверка на клик по торговцу
        for (Trader trader : world.getTraders()) {
            if (trader.gridX == mouseGridX && trader.gridY == mouseGridY) {
                // Проверяем, рядом ли активный юнит
                int unitX, unitY;
                if (isPlayerActive()) {
                    unitX = player.gridX;
                    unitY = player.gridY;
                } else {
                    FriendlyUnit active = getActiveFriendly();
                    if (active == null) return;
                    unitX = active.gridX;
                    unitY = active.gridY;
                }

                int dx = Math.abs(unitX - mouseGridX);
                int dy = Math.abs(unitY - mouseGridY);
                boolean isAdjacent = (dx == 1 && dy == 0) || (dx == 0 && dy == 1);

                if (isAdjacent) {
                    // Загружаем портрет T34
                    // Загружаем портрет T34
                    BufferedImage traderPortrait = null;
                    try {
                        String portraitPath = "src/traders/T34.png";
                        File portraitFile = new File(portraitPath);
                        if (portraitFile.exists()) {
                            traderPortrait = ImageIO.read(portraitFile);
                        }
                    } catch (IOException ex) {  // ← ИСПРАВЛЕНО: ex вместо e
                        System.err.println("Ошибка загрузки портрета T34: " + ex.getMessage());
                    }

                    TradeDialog dialog = new TradeDialog(parentFrame,
                            isPlayerActive() ? player : getActiveFriendly(),  // ← активный юнит!
                            trader, traderPortrait, player);
                    dialog.setVisible(true);
                } else {
                    String unitName = isPlayerActive() ? "Вы" : getActiveFriendly().name;
                    JOptionPane.showMessageDialog(this,
                            unitName + " слишком далеко от торговца!\nПодойдите в соседнюю клетку.",
                            "Слишком далеко", JOptionPane.WARNING_MESSAGE);
                }
                return;
            }
        }

        // Проверка на клик по активному юниту (чтобы он говорил сам с собой)
        if (isPlayerActive() && mouseGridX == player.gridX && mouseGridY == player.gridY) {
            soundManager.playVoiceSound();
            return;
        } else if (!isPlayerActive()) {
            FriendlyUnit active = getActiveFriendly();
            if (active != null && mouseGridX == active.gridX && mouseGridY == active.gridY) {
                if ("M53".equals(active.type)) {
                    soundManager.playM53VoiceSound();
                } else if ("MS-1".equals(active.type)) {
                    soundManager.playMS1VoiceSound();
                } else if ("VK10001P".equals(active.type)) {
                    soundManager.playVK10001PVoiceSound();  // ← ДОБАВИТЬ
                } else if ("T1".equals(active.type)) {
                    soundManager.playT1VoiceSound();  // ← ДОБАВИТЬ
                }
                return;
            }
        }

        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isAlive && friendly.gridX == mouseGridX && friendly.gridY == mouseGridY) {
                // Определяем, от кого проверять расстояние
                int unitX, unitY;
                if (isPlayerActive()) {
                    unitX = player.gridX;
                    unitY = player.gridY;
                } else {
                    FriendlyUnit active = getActiveFriendly();
                    if (active == null) return;
                    unitX = active.gridX;
                    unitY = active.gridY;
                }

                int dx = Math.abs(unitX - friendly.gridX);
                int dy = Math.abs(unitY - friendly.gridY);
                boolean isAdjacent = (dx == 1 && dy == 0) || (dx == 0 && dy == 1) || (dx == 0 && dy == 0);
                // dx == 0 && dy == 0 - это если кликнули по самому себе (своему юниту)

                if (isAdjacent) {
                    interactWithFriendly(friendly);
                } else {
                    String unitName = isPlayerActive() ? "Вы" : getActiveFriendly().name;
                    JOptionPane.showMessageDialog(this,
                            unitName + " слишком далеко от " + friendly.name + "!\nПодойдите ближе, чтобы поговорить!",
                            "Слишком далеко",
                            JOptionPane.WARNING_MESSAGE);
                }
                return;
            }
        }

        if (mouseGridX == player.gridX && mouseGridY == player.gridY) {
            soundManager.playVoiceSound();
            return;
        }

        // Проверка на клик по тумбочке
        // Проверка на клик по тумбочке
        StorageChest chest = world.getStorageChestAt(mouseGridX, mouseGridY);
        if (chest != null && chest.isAlive) {
            // Определяем, какой юнит активен
            Object activeUnit;
            int unitX, unitY;

            if (isPlayerActive()) {
                activeUnit = player;
                unitX = player.gridX;
                unitY = player.gridY;
            } else {
                FriendlyUnit active = getActiveFriendly();
                if (active == null) return;
                activeUnit = active;
                unitX = active.gridX;
                unitY = active.gridY;
            }

            // Проверяем, рядом ли активный юнит
            int dx = Math.abs(unitX - mouseGridX);
            int dy = Math.abs(unitY - mouseGridY);
            boolean isAdjacent = (dx <= 2 && dy <= 1) ||
                    (chest.containsCell(unitX, unitY));

            if (isAdjacent) {
                openStorageChest(chest, activeUnit);
            } else {
                String unitName = isPlayerActive() ? "Вы" : getActiveFriendly().name;
                JOptionPane.showMessageDialog(this,
                        unitName + " слишком далеко от тумбочки!\nПодойдите ближе, чтобы открыть её!",
                        "Слишком далеко", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        // В mouseClicked, после проверки на StorageChest, добавьте:
        GarbageContainer garbage = world.getGarbageContainerAt(mouseGridX, mouseGridY);
        if (garbage != null && garbage.isAlive && !garbage.isLooted) {
            Object activeUnit;
            int unitX, unitY;

            if (isPlayerActive()) {
                activeUnit = player;
                unitX = player.gridX;
                unitY = player.gridY;
            } else {
                FriendlyUnit active = getActiveFriendly();
                if (active == null) return;
                activeUnit = active;
                unitX = active.gridX;
                unitY = active.gridY;
            }

            // Проверяем расстояние
            int dx = Math.abs(unitX - mouseGridX);
            int dy = Math.abs(unitY - mouseGridY);
            boolean isAdjacent = (dx <= 2 && dy <= 2);

            if (isAdjacent) {
                openGarbageContainer(garbage, activeUnit);
            } else {
                String unitName = isPlayerActive() ? "Вы" : getActiveFriendly().name;
                JOptionPane.showMessageDialog(this,
                        unitName + " слишком далеко от мусорного контейнера!\nПодойдите ближе (в радиусе 2 клеток).",
                        "Слишком далеко", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        // ===== РЕЖИМ ТЕЛЕПОРТАЦИИ =====
        if (isTeleportMode) {
            mouseGridX = (e.getX() + cameraX) / GameWorld.CELL_SIZE;
            mouseGridY = (e.getY() + cameraY) / GameWorld.CELL_SIZE;

            // Проверяем границы карты
            int gridWidth = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE;
            int gridHeight = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE;

            if (mouseGridX < 0 || mouseGridX >= gridWidth || mouseGridY < 0 || mouseGridY >= gridHeight) {
                JOptionPane.showMessageDialog(this,
                        "❌ Нельзя телепортироваться за пределы карты!\n" +
                                "Координаты: [" + mouseGridX + "," + mouseGridY + "]",
                        "Ошибка телепортации", JOptionPane.WARNING_MESSAGE);
            } else {
                // Телепортируем активного юнита
                boolean success = teleportTo(mouseGridX, mouseGridY);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "🌀 ТЕЛЕПОРТАЦИЯ УСПЕШНА! 🌀\n\n" +
                                    getActiveUnitName() + " перемещён в [" + mouseGridX + "," + mouseGridY + "]",
                            "Телепортация", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "❌ Не удалось телепортироваться в [" + mouseGridX + "," + mouseGridY + "]\n" +
                                    "Клетка занята другим юнитом!",
                            "Ошибка телепортации", JOptionPane.WARNING_MESSAGE);
                }
            }

            // Выходим из режима телепортации
            isTeleportMode = false;
            setCursor(Cursor.getDefaultCursor());
            return;
        }

        Door door = world.getDoorAt(mouseGridX, mouseGridY);
        if (door != null && door.isAlive && !door.isOpen) {
            // Проверяем, рядом ли активный юнит
            int unitX, unitY;
            Object activeUnit;
            if (isPlayerActive()) {
                unitX = player.gridX;
                unitY = player.gridY;
                activeUnit = player;
            } else {
                FriendlyUnit active = getActiveFriendly();
                if (active == null) return;
                unitX = active.gridX;
                unitY = active.gridY;
                activeUnit = active;
            }

            int dx = Math.abs(unitX - mouseGridX);
            int dy = Math.abs(unitY - mouseGridY);
            boolean isAdjacent = (dx == 1 && dy == 0) || (dx == 0 && dy == 1);

            if (isAdjacent) {
                tryOpenDoor(door, activeUnit);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Подойдите вплотную к двери, чтобы открыть её!",
                        "Слишком далеко",
                        JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        // Проверка на дроп
        LootDrop drop = world.getLootDropAt(mouseGridX, mouseGridY);
        if (drop != null && drop.isAlive && !drop.isEmpty()) {
            // Определяем, какой юнит активен
            Object activeUnit;
            int unitX, unitY;

            if (isPlayerActive()) {
                activeUnit = player;
                unitX = player.gridX;
                unitY = player.gridY;
            } else {
                FriendlyUnit active = getActiveFriendly();
                if (active == null) return;
                activeUnit = active;
                unitX = active.gridX;
                unitY = active.gridY;
            }

            // Проверяем, рядом ли активный юнит (1 клетка)
            int dx = Math.abs(unitX - mouseGridX);
            int dy = Math.abs(unitY - mouseGridY);
            boolean isAdjacent = (dx <= 1 && dy <= 1);

            if (isAdjacent) {
                openLootDrop(drop, activeUnit);
            } else {
                String unitName = isPlayerActive() ? "Вы" : getActiveFriendly().name;
                JOptionPane.showMessageDialog(this,
                        unitName + " слишком далеко от дропа!\nПодойдите ближе (в соседнюю клетку), чтобы подобрать предметы.",
                        "Слишком далеко", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        // Проверяем, кликнули ли по врагу
        boolean clickedOnEnemy = false;
        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isAlive && enemy.gridX == mouseGridX && enemy.gridY == mouseGridY) {
                // ===== ИСПРАВЛЕНИЕ: используем видимость от КОМАНДЫ для ВСЕХ юнитов =====
                boolean isVisible = world.isEnemyVisibleByTeam(enemy);
                if (isVisible) {
                    clickedOnEnemy = true;
                    break;
                }
            }
        }

        if (isAimingMode || clickedOnEnemy) {
            // Режим стрельбы - устанавливаем цель
            targetShotX = mouseGridX;
            targetShotY = mouseGridY;
            if (clickedOnEnemy) {
                System.out.println("🎯 Цель выбрана: [" + targetShotX + "," + targetShotY + "]");
                double hitChance = calculateHitProbability(targetShotX, targetShotY);
                System.out.println("Шанс попадания: " + (int)(hitChance * 100) + "%");
            }
            repaint();
        } else {
            // Движение
            if (isPlayerActive()) {
                setTarget(mouseGridX, mouseGridY);
            } else {
                FriendlyUnit active = getActiveFriendly();
                if (active != null) {
                    setFriendlyTarget(active, mouseGridX, mouseGridY);
                }
            }
        }
    }

    private void tryOpenDoor(Door door, Object activeUnit) {
        Inventory inv;
        if (activeUnit instanceof PlayerTank) {
            inv = ((PlayerTank) activeUnit).getInventory();
        } else if (activeUnit instanceof FriendlyUnit) {
            inv = ((FriendlyUnit) activeUnit).getInventory();
        } else {
            return;
        }

        // Ищем ключ нужного цвета в инвентаре
        boolean hasKey = false;
        for (int y = 0; y < Inventory.HEIGHT; y++) {
            for (int x = 0; x < Inventory.WIDTH; x++) {
                Item item = inv.getItem(x, y);
                if (item instanceof KeyItem) {
                    KeyItem key = (KeyItem) item;
                    if (key.getColor() == door.color && key.getCount() > 0) {
                        hasKey = true;
                        // Удаляем один ключ
                        inv.removeItem(x, y, 1);
                        System.out.println("🔑 Использован " + door.color.displayName + " ключ!");
                        break;
                    }
                }
            }
            if (hasKey) break;
        }

        if (!hasKey) {
            JOptionPane.showMessageDialog(this,
                    "❌ Нужен " + door.color.displayName + " ключ, чтобы открыть эту дверь!",
                    "Нет ключа",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Открываем дверь
        door.open();
        JOptionPane.showMessageDialog(this,
                "🔓 Дверь " + door.color.displayName + " открыта!",
                "Дверь открыта",
                JOptionPane.INFORMATION_MESSAGE);
        repaint();
    }

    // Получить имя активного юнита
    private String getActiveUnitName() {
        if (isPlayerActive()) {
            return "Leichttraktor";
        } else {
            FriendlyUnit active = getActiveFriendly();
            return active != null ? active.name : "Unknown";
        }
    }

    // Телепортация активного юнита в указанные координаты
    private boolean teleportTo(int targetX, int targetY) {
        // Проверяем, свободна ли клетка
        if (isPlayerActive()) {
            // Проверяем для игрока
            if (!world.canMoveTo(targetX, targetY) &&
                    !(player.gridX == targetX && player.gridY == targetY)) {
                return false;
            }

            // Сохраняем старые координаты для отката при ошибке
            int oldX = player.gridX;
            int oldY = player.gridY;

            // Телепортируем
            player.gridX = targetX;
            player.gridY = targetY;

            // Обновляем видимость и проверяем холмы
            world.updateVisibilityMap();
            world.checkHillOccupation();

            // Центрируем камеру на новой позиции
            centerCameraOnUnit(targetX, targetY);

            System.out.println("🌀 Leichttraktor телепортирован из [" + oldX + "," + oldY +
                    "] в [" + targetX + "," + targetY + "]");
        } else {
            FriendlyUnit active = getActiveFriendly();
            if (active == null) return false;

            // Проверяем для союзника
            if (!world.canMoveToForFriendly(active, targetX, targetY) &&
                    !(active.gridX == targetX && active.gridY == targetY)) {
                return false;
            }

            // Сохраняем старые координаты
            int oldX = active.gridX;
            int oldY = active.gridY;

            // Телепортируем
            active.gridX = targetX;
            active.gridY = targetY;

            // Обновляем видимость
            world.updateVisibilityForFriendly(active);

            // Центрируем камеру на новой позиции
            centerCameraOnUnit(targetX, targetY);

            System.out.println("🌀 " + active.name + " телепортирован из [" + oldX + "," + oldY +
                    "] в [" + targetX + "," + targetY + "]");
        }

        // Обновляем карту
        repaint();
        return true;
    }

    private void openLootDrop(LootDrop drop, Object activeUnit) {
        LootDialog dialog = new LootDialog(parentFrame, activeUnit, drop,
                heroPortrait, m53Portrait, ms1Portrait, vk10001pPortrait, amx40Portrait, t1Portrait);
        dialog.setVisible(true);

        if (drop.isEmpty()) {
            drop.isAlive = false;
            world.getLootDrops().remove(drop);
        }
        repaint();
    }

    private void openStorageChest(StorageChest chest, Object activeUnit) {
        StorageDialog dialog = new StorageDialog(parentFrame, activeUnit, chest,
                heroPortrait, m53Portrait, ms1Portrait, vk10001pPortrait, amx40Portrait, t1Portrait);
        dialog.setVisible(true);
        repaint();
    }

    private void openGarbageContainer(GarbageContainer container, Object activeUnit) {
        GarbageDialog dialog = new GarbageDialog(parentFrame, activeUnit, container,
                heroPortrait, m53Portrait, ms1Portrait, vk10001pPortrait, amx40Portrait, t1Portrait);
        dialog.setVisible(true);

        if (container.isEmpty()) {
            container.isLooted = true;
        }
        repaint();
    }

    // Добавьте метод взаимодействия:
    private void interactWithFriendly(FriendlyUnit friendly) {
        System.out.println("=== ВЗАИМОДЕЙСТВИЕ С СОЮЗНИКОМ ===");
        System.out.println("Имя: " + friendly.name + ", тип: " + friendly.type + ", нанят: " + friendly.isRecruited);

        // ===== ДОБАВЬТЕ ЭТУ ПРОВЕРКУ В САМОМ НАЧАЛЕ =====
        if (friendly.isUnavailable) {
            JOptionPane.showMessageDialog(this,
                    "Этот союзник больше не может присоединиться к команде.\n" +
                            "Его судьба теперь связана с другим местом...",
                    friendly.name,
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (friendly.isUnavailable) {
            JOptionPane.showMessageDialog(this, friendly.greetingText, friendly.name, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (friendly.isRecruited) {
            if ("M53".equals(friendly.type)) {
                soundManager.playM53VoiceSound();
            } else if ("MS-1".equals(friendly.type)) {
                soundManager.playMS1VoiceSound();
            } else if ("VK10001P".equals(friendly.type)) {
                soundManager.playVK10001PVoiceSound();
            } else if ("AMX40".equals(friendly.type)) {
                soundManager.playCustomClip(friendly.greetingSound);
            } else if ("T1".equals(friendly.type)) {
                soundManager.playT1VoiceSound();
            }
            return;
        }

        // Воспроизводим звук приветствия
        friendly.playGreetingSound();

        // ===== ПРАВИЛЬНЫЙ ПОРЯДОК ПРОВЕРКИ =====
        if ("M53".equals(friendly.type) || "VK10001P".equals(friendly.type) ||
                "MS-1".equals(friendly.type) || "AMX40".equals(friendly.type) || "T1".equals(friendly.type)) {
            showFriendlyDialog(friendly);
            return;
        }

        // Для остальных союзников - обычное окно
        int option = JOptionPane.showConfirmDialog(this,
                friendly.greetingText + "\n\nВзять в команду?",
                friendly.name,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        // ОСТАНАВЛИВАЕМ ЗВУК ПОСЛЕ ТОГО, КАК ДИАЛОГ ЗАКРЫТ
        soundManager.stopVoiceClip();

        if (option == JOptionPane.YES_OPTION) {
            world.recruitFriendly(friendly);
            JOptionPane.showMessageDialog(this, friendly.recruitedText, "Присоединился!", JOptionPane.INFORMATION_MESSAGE);
            syncRecruitedUnits();
            repaint();
        }
    }

    // Показ диалога с союзником (с портретом)
    private void showFriendlyDialog(FriendlyUnit friendly) {
        if (friendly.isUnavailable) {
            JOptionPane.showMessageDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                    friendly.greetingText,
                    friendly.name,
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String dialogText;
        BufferedImage portrait;

        // ===== ВАЖНО: проверяйте в правильном порядке! =====
        if ("M53".equals(friendly.type)) {
            dialogText = "Это ты? Тебя за мной подослали? Неважно. Если пришёл освободить — кончай смотреть по сторонам и вытаскивай меня отсюда!";
            portrait = m53Portrait;
        } else if ("VK10001P".equals(friendly.type)) {
            dialogText = friendly.greetingText;
            portrait = vk10001pPortrait;
        } else if ("MS-1".equals(friendly.type)) {
            dialogText = "Ты не стреляешь? Правда? Все вокруг сошли с ума, а ты? Ты нормальный... \n " +
                    "Возьми меня в команду! Я маленький, но смелый! Ну... иногда смелый. Обещаю стараться! \n" +
                    "Пожалуйста... Не хочу больше один бояться...";
            portrait = ms1Portrait;
        } else if ("AMX40".equals(friendly.type)) {
            dialogText = friendly.greetingText;
            portrait = amx40Portrait;
        } else if ("T1".equals(friendly.type)) {
            dialogText = friendly.greetingText;
            portrait = t1Portrait;
        } else {
            dialogText = "Возьмёшь в команду?";
            portrait = friendlyImage;
        }

        // Создаём диалог
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), friendly.name, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(550, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ОСТАНАВЛИВАЕМ ЗВУК ПРИ ЗАКРЫТИИ ДИАЛОГА
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                soundManager.stopVoiceClip();
            }
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                soundManager.stopVoiceClip();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(20, 20, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(20, 20, 30));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        if (portrait != null) {
            Image scaledImage = portrait.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            JLabel portraitLabel = new JLabel(new ImageIcon(scaledImage));
            portraitLabel.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
            topPanel.add(portraitLabel, BorderLayout.WEST);
        } else {
            JPanel placeholder = new JPanel();
            placeholder.setPreferredSize(new Dimension(120, 120));
            placeholder.setBackground(new Color(255, 215, 0));
            placeholder.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            JLabel emojiLabel = new JLabel("🚀", SwingConstants.CENTER);
            emojiLabel.setFont(new Font("Segoe UI", Font.PLAIN, 50));
            placeholder.setLayout(new BorderLayout());
            placeholder.add(emojiLabel, BorderLayout.CENTER);
            topPanel.add(placeholder, BorderLayout.WEST);
        }

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(20, 20, 30));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));

        JLabel nameLabel = new JLabel(friendly.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(new Color(255, 215, 0));

        JLabel typeLabel = new JLabel(friendly.type);
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeLabel.setForeground(Color.LIGHT_GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(typeLabel);

        topPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea();
        textArea.setText(dialogText);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(30, 30, 40));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100), 1));
        scrollPane.setBackground(new Color(30, 30, 40));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(new Color(20, 20, 30));

        JButton acceptButton = new JButton("✅ ВЗЯТЬ В КОМАНДУ");
        acceptButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        acceptButton.setBackground(new Color(0, 150, 0));
        acceptButton.setForeground(Color.WHITE);
        acceptButton.setFocusPainted(false);
        acceptButton.addActionListener(e -> {
            world.recruitFriendly(friendly);
            JOptionPane.showMessageDialog(dialog, friendly.recruitedText, "Присоединился!", JOptionPane.INFORMATION_MESSAGE);
            player.calculateMovePoints();
            syncRecruitedUnits();
            repaint();
            soundManager.stopVoiceClip();  // ОСТАНАВЛИВАЕМ ЗВУК ПРИ НАЙМЕ
            dialog.dispose();
        });

        JButton declineButton = new JButton("❌ ОТКАЗАТЬСЯ");
        declineButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        declineButton.setBackground(new Color(150, 0, 0));
        declineButton.setForeground(Color.WHITE);
        declineButton.setFocusPainted(false);
        declineButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Может быть, в другой раз...", "Отказ", JOptionPane.INFORMATION_MESSAGE);
            soundManager.stopVoiceClip();  // ОСТАНАВЛИВАЕМ ЗВУК ПРИ ОТКАЗЕ
            dialog.dispose();
        });

        bottomPanel.add(acceptButton);
        bottomPanel.add(declineButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // Получить текущий активный юнит (если не игрок)
    public FriendlyUnit getActiveFriendly() {
        List<FriendlyUnit> units = world.getControllableUnits();
        if (currentUnitIndex > 0 && currentUnitIndex - 1 < units.size()) {
            return units.get(currentUnitIndex - 1);
        }
        return null;
    }

    // Проверить, активен ли игрок
    public boolean isPlayerActive() {
        return currentUnitIndex == 0;
    }

    // Переключение между управляемыми юнитами
    // В файле GamePanel.java, метод switchActiveUnit()
    private void switchActiveUnit() {

        world.removeDeadControllableUnits();
        syncRecruitedUnits();

        // ===== ЗАПРЕТ ПЕРЕКЛЮЧЕНИЯ ВО ВРЕМЯ ДВИЖЕНИЯ =====
        if (isMoving || isAutoMoving) {
            System.out.println("⚠ Нельзя переключиться во время движения игрока!");
            return;
        }

        if (isFriendlyMoving || isFriendlyAutoMoving) {
            System.out.println("⚠ Нельзя переключиться во время движения союзника!");
            return;
        }

        if (world.isAnyEnemyMoving()) {
            System.out.println("⚠ Нельзя переключиться во время хода врагов!");
            return;
        }
        // ===== КОНЕЦ ЗАПРЕТА =====

        friendlyPath.clear();
        movingFriendly = null;
        isFriendlyAutoMoving = false;

        syncRecruitedUnits();

        List<FriendlyUnit> units = world.getControllableUnits();

        if (units.isEmpty()) {
            System.out.println("НЕТ УПРАВЛЯЕМЫХ ЮНИТОВ!");
            return;
        }

        int totalUnits = units.size() + 1;
        currentUnitIndex = (currentUnitIndex + 1) % totalUnits;

        if (currentUnitIndex == 0) {
            System.out.println("Активный юнит: Игрок (Leichttraktor)");
            System.out.println("  Очки хода: " + player.movePoints + "/" + player.maxMovePoints);
            centerCameraOnUnit(player.gridX, player.gridY);
            world.updateVisibilityMap();

            lastVisibleEnemyCount = 0;
            // В GamePanel.java, в switchActiveUnit, при переключении на союзника:
        } else {
            FriendlyUnit active = units.get(currentUnitIndex - 1);

            // ===== ОБНОВЛЯЕМ ВИДИМОСТЬ ДЛЯ ЭТОГО СОЮЗНИКА =====
            world.updateFriendlyVisibilityMap(active);

            if ("M53".equals(active.type)) {
                lastVisibleEnemyCount = 0;
                lastM53SpottedTime = 0;
            }
            System.out.println("Активный юнит: " + active.name);
            System.out.println("  Здоровье: " + active.health + "/" + active.maxHealth);
            System.out.println("  Очки хода: " + active.movePoints + "/" + active.maxMovePoints);
            centerCameraOnUnit(active.gridX, active.gridY);

            world.updateAllFriendlyVisibilityMaps();  // Обновляем карты всех союзников
            updateVisibleEnemies();  // Обновляем список видимых врагов

            // Обновляем видимость и список врагов
            updateVisibleEnemies();
            checkEnemyDetection();
        }

        repaint();
    }



    // Центрирование камеры на указанном юните
    // Центрирование камеры на указанном юните
    private void centerCameraOnUnit(int gridX, int gridY) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Вычисляем пиксельные координаты центра юнита
        int unitScreenX = gridX * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE / 2;
        int unitScreenY = gridY * GameWorld.CELL_SIZE + GameWorld.CELL_SIZE / 2;

        // Устанавливаем камеру так, чтобы юнит был в центре
        int newCameraX = unitScreenX - viewWidth / 2;
        int newCameraY = unitScreenY - viewHeight / 2;

        // Ограничиваем камеру границами карты
        cameraX = Math.max(0, Math.min(GameWorld.FIELD_SIZE - viewWidth, newCameraX));
        cameraY = Math.max(0, Math.min(GameWorld.FIELD_SIZE - viewHeight, newCameraY));

        System.out.println("Камера центрирована на [" + gridX + "," + gridY +
                "], cameraX=" + cameraX + ", cameraY=" + cameraY);

        // Важно: НЕ вызываем repaint() здесь, так как он будет вызван после
        // repaint уже есть в switchActiveUnit
    }

    // Добавьте метод взаимодействия с NPC:
    // В GamePanel.java, замените метод interactWithNPC:

    private void interactWithNPC(QuestNPC npc) {
        System.out.println("=== ВЗАИМОДЕЙСТВИЕ С NPC ===");

        // Определяем, с кем имеем дело
        if ("Sav m/43".equals(npc.name)) {
            interactWithSavM43(npc);
            return;
        }

        // ===== ДАЛЬШЕ ИДЁТ КОД ДЛЯ T18 =====

        // ===== ПОЛУЧАЕМ АКТИВНЫЙ ЮНИТ =====
        Object activeUnit;
        int unitX, unitY;
        String unitName;

        if (isPlayerActive()) {
            activeUnit = player;
            unitX = player.gridX;
            unitY = player.gridY;
            unitName = "Leichttraktor";
        } else {
            FriendlyUnit active = getActiveFriendly();
            if (active == null) return;
            activeUnit = active;
            unitX = active.gridX;
            unitY = active.gridY;
            unitName = active.name;
        }

        int dx = Math.abs(unitX - npc.gridX);
        int dy = Math.abs(unitY - npc.gridY);
        boolean isAdjacent = (dx == 1 && dy == 0) || (dx == 0 && dy == 1);

        if (!isAdjacent) {
            JOptionPane.showMessageDialog(this,
                    unitName + " слишком далеко от " + npc.name + "!\nПодойдите ближе, чтобы поговорить.",
                    "Слишком далеко",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ===== ЕСЛИ КВЕСТ ПОЛНОСТЬЮ ЗАВЕРШЁН - НИЧЕГО НЕ ГОВОРИМ =====
        if (npc.isQuestFinished) {
            System.out.println("Квест уже выполнен, диалог не показывается");
            return;
        }

        // ===== ПРОВЕРКА: M53 в радиусе 3 клеток от T18 =====
        boolean isM53Nearby = false;
        FriendlyUnit m53Unit = null;

        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isAlive && friendly.isRecruited && "M53".equals(friendly.type)) {
                int distToNpc = Math.abs(friendly.gridX - npc.gridX) + Math.abs(friendly.gridY - npc.gridY);
                if (distToNpc <= 3) {
                    isM53Nearby = true;
                    m53Unit = friendly;
                    System.out.println("🎯 M53 находится в радиусе 3 клеток от T18! Расстояние: " + distToNpc);
                    break;
                }
            }
        }

        // Если квест ещё не выполнен и M53 рядом - завершаем квест
        if (!npc.isQuestCompleted && isM53Nearby) {
            System.out.println("🎉 M53 рядом с T18! Квест считается выполненным!");
            npc.isQuestCompleted = true;
        }

        if (npc.isQuestCompleted) {
            // Выполненный квест
            npc.playQuestSound();

            String dialogText;
            FriendlyUnit m53ToRemove = null;

            // Находим M53 в команде
            for (FriendlyUnit friendly : world.getFriendlyUnits()) {
                if (friendly.isAlive && friendly.isRecruited && "M53".equals(friendly.type)) {
                    m53ToRemove = friendly;
                    break;
                }
            }

            if (isM53Nearby && m53Unit != null) {
                dialogText = "🏆 " + npc.name + ": \"Я вижу, ты привёл его! " + m53Unit.name + ", ты жив! Спасибо тебе, " + unitName + "!\n\n" +
                        "Ты не наш союзник... но ты поступил как настоящий друг. Спасибо тебе.\n\n" +
                        "Награда: " + npc.rewardDescription;
            } else {
                dialogText = "🏆 " + npc.name + ": \"Спасибо, герой! Получи награду!\n\n" +
                        "Награда: " + npc.rewardDescription;
            }

            giveReward(npc);

            // ===== M53 ОСТАЁТСЯ НА КАРТЕ, НО ПОКИДАЕТ КОМАНДУ =====
            if (m53ToRemove != null) {
                world.getControllableUnits().remove(m53ToRemove);
                m53ToRemove.isRecruited = false;
                m53ToRemove.isUnavailable = true;

                if (world.canMoveToForFriendly(m53ToRemove, npc.gridX + 1, npc.gridY)) {
                    m53ToRemove.gridX = npc.gridX + 1;
                    m53ToRemove.gridY = npc.gridY;
                } else if (world.canMoveToForFriendly(m53ToRemove, npc.gridX - 1, npc.gridY)) {
                    m53ToRemove.gridX = npc.gridX - 1;
                    m53ToRemove.gridY = npc.gridY;
                } else if (world.canMoveToForFriendly(m53ToRemove, npc.gridX, npc.gridY + 1)) {
                    m53ToRemove.gridX = npc.gridX;
                    m53ToRemove.gridY = npc.gridY + 1;
                } else if (world.canMoveToForFriendly(m53ToRemove, npc.gridX, npc.gridY - 1)) {
                    m53ToRemove.gridX = npc.gridX;
                    m53ToRemove.gridY = npc.gridY - 1;
                } else {
                    m53ToRemove.gridX = npc.gridX;
                    m53ToRemove.gridY = npc.gridY;
                }

                m53ToRemove.greetingText = "Спасибо, что привёл меня к отцу! Теперь я останусь здесь и помогу ему.";
                m53ToRemove.recruitedText = "Извини, но моё место теперь здесь, с отцом.";
                m53ToRemove.health = m53ToRemove.maxHealth;

                System.out.println("👨‍👦 " + m53ToRemove.name + " остаётся с отцом " + npc.name + " и покидает команду!");

                JOptionPane.showMessageDialog(this,
                        "👨‍👦 " + m53ToRemove.name + " остаётся с отцом " + npc.name + "!\n\n" +
                                "Они благодарят вас за помощь и желают удачи в дальнейшем пути.\n" +
                                m53ToRemove.name + " покидает вашу команду, но остаётся на карте.",
                        "Прощание с M53",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            // ===== ПОМЕЧАЕМ КВЕСТ КАК ПОЛНОСТЬЮ ЗАВЕРШЁННЫЙ =====
            npc.isQuestFinished = true;
            npc.isQuestCompleted = false;
            npc.hasTalked = true;

            syncRecruitedUnits();

            if (m53ToRemove != null && !isPlayerActive() && getActiveFriendly() == m53ToRemove) {
                switchActiveUnit();
            }

            QuestDialog questDialog = new QuestDialog(parentFrame, npc, npcImage, dialogText, soundManager);
            questDialog.setVisible(true);
            soundManager.stopVoiceClip();
            repaint();

        } else if (npc.hasReceivedQuest()) {
            // Квест принят, но не выполнен - показываем подсказку
            npc.playQuestSound();

            String hint = "";
            for (FriendlyUnit friendly : world.getFriendlyUnits()) {
                if (friendly.isAlive && friendly.isRecruited && "M53".equals(friendly.type)) {
                    int distToNpc = Math.abs(friendly.gridX - npc.gridX) + Math.abs(friendly.gridY - npc.gridY);
                    hint = "\n\n⚠ " + friendly.name + " находится в [" + friendly.gridX + "," + friendly.gridY +
                            "], расстояние до вас: " + distToNpc + " клеток. Приведите его к T18 (радиус 3 клеток)!";
                    break;
                }
            }

            String dialogText = "🤝 " + npc.name + ": \"" + "Я места себе не нахожу. Каждую ночь мне снится, как он зовёт меня. " +
                    "Пожалуйста, не останавливайся. Найди его, умоляю. Я не переживу, если с ним что-то случится.\"" + hint;

            QuestDialog questDialog = new QuestDialog(parentFrame, npc, npcImage, dialogText, soundManager);
            questDialog.setVisible(true);
            soundManager.stopVoiceClip();

        } else {
            // НОВЫЙ КВЕСТ
            npc.playQuestSound();

            String dialogText = "🤝 " + npc.name + ": \"" + "Стой, не стреляй! Ты не похож на остальных. Послушай. У меня беда - пропал мой сын. Его украли японские танки, держат где-то в плену." +
                    "\n\n   Я не знаю, кому верить. Всё перевернулось с ног на голову. Русские стреляют в немцев, немцы - во французов. Город, где мы жили бок о бок, стал полем боя. Танки ополчились друг против друга, будто забыли, кто им друг, а кто враг." +
                    "\n\n   Но в тебе я чувствую что-то родное. Ты не такой, как эти озверевшие машины. Помоги мне, " + unitName + ". Найди моего мальчика. Я знаю - ты сможешь.\"" +
                    "\n\n   Подсказка: Приведите M53 к T18 (в радиусе 3 клеток), чтобы завершить квест!";

            npc.setQuestReceived();
            npc.hasTalked = true;

            QuestDialog questDialog = new QuestDialog(parentFrame, npc, npcImage, dialogText, soundManager);
            questDialog.setVisible(true);
            soundManager.stopVoiceClip();
        }

        repaint();
    }

    // В GamePanel.java, в interactWithSavM43():
    private void interactWithSavM43(QuestNPC npc) {
        if (npc.isQuestFinished) {
            System.out.println("Квест уже завершён!");
            return;
        }

        npc.disableSound();
        soundManager.stopVoiceClip();

        // ===== ПРОВЕРЯЕМ, БЫЛ ЛИ ПОЛУЧЕН КВЕСТ =====
        if (!npc.hasReceivedQuest()) {
            // ===== ПЕРВОЕ ВЗАИМОДЕЙСТВИЕ - ПОЛУЧЕНИЕ КВЕСТА =====
            npc.setQuestReceived();
            soundManager.playSoundFromFile("src/QuestPersons/Дедушка-Швед/QuestReceived/Received.wav");
            String dialogText = "А! Ты не стреляешь? Умный ход...\n\n" +
                    "Смотри, этот район превратился в сумасшедший дом! Французы дерутся с итальянцами, и " +
                    "всем плевать, что я тут вообще случайно оказался. Я хочу спать спокойно, а не вздрагивать от каждого выстрела!\n" +
                    "Поможешь навести порядок? Зачистишь окрестности - я поделюсь с тобой кое-чем ценным! Шведское качество. Сам понимаешь.\n\n" +
                    "Идёт?";
            showQuestDialog(npc, dialogText, savM43Image);
            return;
        }

        // ===== КВЕСТ УЖЕ ПОЛУЧЕН - ПРОВЕРЯЕМ ВЫПОЛНЕНИЕ =====
        boolean completed = npc.isQuestCompletedForFactions(world.getEnemies());

        if (completed && !npc.isQuestCompleted) {
            npc.isQuestCompleted = true;
            System.out.println("✅ Sav m/43: квест ВЫПОЛНЕН при взаимодействии!");
        }

        String dialogText;
        if (npc.isQuestCompleted) {
            // Если квест уже выполнен, но ещё не завершён - выдаём награду
            giveSavM43Reward();
            npc.isQuestFinished = true;
            npc.isQuestCompleted = false;

            // Показываем трёхэтапный диалог
            showSavM43CompletionDialog(npc);
            return;
        } else {
            // Подсчитываем, сколько врагов осталось
            int remaining = 0;
            for (Enemy enemy : world.getEnemies()) {
                if (enemy.isAlive && (enemy.getFaction() == Faction.FRANCE ||
                        enemy.getFaction() == Faction.ITALY)) {
                    remaining++;
                }
            }

            soundManager.playSoundFromFile("src/QuestPersons/Дедушка-Швед/QuestNotCompleted/NotCompleted.wav");
            dialogText = "Эти негодяи всё ещё здесь колесят... Ты всё ждёшь? Позволишь им продолжать этот балаган? " +
                    "Не тяни, приятель! Моё терпение ломается, и быстрее, чем я думал... " +
                    "Заканчивай с ними, пока я совсем не вышел из себя! Осталось: " + remaining;
            showQuestDialog(npc, dialogText, savM43Image);
        }
    }

    // Добавьте новый метод для трехэтапного диалога:
    // Добавьте новый метод для трехэтапного диалога:
    private void showSavM43CompletionDialog(QuestNPC npc) {
        // Создаем диалог для трех этапов
        JDialog dialog = new JDialog(parentFrame, "🎉 Квест выполнен!", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(550, 450);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Панель для смены контента
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(new Color(20, 20, 30));

        // Создаем три панели для трех этапов
        JPanel panel1 = createSavM43DialogPanel(
                savM43Image,
                "Дедушка-Швед",
                "Слышишь эту тишину? Это твоя работа, и она правильная! Не потому, что кто-то победил... " +
                        "А потому, что кто-то перестал стрелять.  \n\n" +
                        "Спасибо, друг. Держи, что обещал. И помни - иногда самый сильный поступок - не выстрел, а " +
                        "умение остановиться. А ты это умеешь. Цени...\n\n");
        contentPanel.add(panel1, "STEP1");

        JPanel panel2 = createSavM43DialogPanel(
                heroPortrait,
                "Leichttraktor",
                "Что здесь происходит? Прошу тебя, расскажи всё, что знаешь! Каждое слово может быть важным!"
        );
        contentPanel.add(panel2, "STEP2");

        JPanel panel3 = createSavM43DialogPanel(
                savM43Image,
                "Дедушка-Швед",
                "Откуда мне знать? Я такой же пехотинец, как и ты. Однажды я проснулся, а все вокруг уже " +
                        "стреляют друг в друга. Но мне кажется, что кто-то за всем этим стоит. Кто-то, кому выгоден этот хаос.\n\n" +
                        "А теперь слушай сюда. Убирайтесь отсюда, пока не поздно. Этот балаган только начинается, и чем дальше вы " +
                        "уйдёте - тем лучше. Найдите того, кто это устроил... или просто выживите."
        );
        contentPanel.add(panel3, "STEP3");

        // Получаем CardLayout для управления
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();

        // Счетчик текущего этапа
        int[] currentStep = {1};

        // Кнопки навигации
        JButton nextButton = new JButton("✅ Далее");
        nextButton.setFont(new Font("Arial", Font.BOLD, 12));
        nextButton.setBackground(new Color(0, 100, 200));
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);

        JButton closeButton = new JButton("🏆 Завершить");
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setBackground(new Color(0, 150, 0));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setVisible(false);

        // Обработчик кнопки "Далее"
        nextButton.addActionListener(e -> {
            // ===== ОСТАНАВЛИВАЕМ ТЕКУЩИЙ ЗВУК ПЕРЕД ПЕРЕКЛЮЧЕНИЕМ =====
            soundManager.stopVoiceClip();

            // Переключаем на следующий этап
            cardLayout.next(contentPanel);
            currentStep[0]++;

            // Воспроизводим соответствующий звук
            if (currentStep[0] == 2) {
                // Второй этап - Leichttraktor спрашивает (Done1.wav)
                soundManager.playSoundFromFile("src/QuestPersons/Дедушка-Швед/QuestDone/Done1.wav");
            } else if (currentStep[0] == 3) {
                // Третий этап - Дедушка-Швед объясняет (Done2.wav)
                soundManager.playSoundFromFile("src/QuestPersons/Дедушка-Швед/QuestDone/Done2.wav");
                // Скрываем кнопку "Далее", показываем "Завершить"
                nextButton.setVisible(false);
                closeButton.setVisible(true);
            }
        });

        // Обработчик кнопки "Завершить"
        closeButton.addActionListener(e -> {
            soundManager.stopVoiceClip();
            dialog.dispose();
        });

        // Добавляем обработчик закрытия окна
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                soundManager.stopVoiceClip();
            }
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                soundManager.stopVoiceClip();
            }
        });

        // Нижняя панель с кнопками
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(20, 20, 30));
        bottomPanel.add(nextButton);
        bottomPanel.add(closeButton);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        // Воспроизводим первый звук (Done.wav)
        soundManager.playSoundFromFile("src/QuestPersons/Дедушка-Швед/QuestDone/Done.wav");

        dialog.setVisible(true);
    }

    // Вспомогательный метод для создания панели диалога с портретом (без buttonText)
    private JPanel createSavM43DialogPanel(BufferedImage portrait, String name, String text) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(20, 20, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Верхняя панель с портретом и именем
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(20, 20, 30));

        if (portrait != null) {
            Image scaledImage = portrait.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            JLabel portraitLabel = new JLabel(new ImageIcon(scaledImage));
            portraitLabel.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
            topPanel.add(portraitLabel, BorderLayout.WEST);
        } else {
            JPanel placeholder = new JPanel();
            placeholder.setPreferredSize(new Dimension(120, 120));
            placeholder.setBackground(new Color(100, 100, 150));
            placeholder.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            JLabel emojiLabel = new JLabel("🎖️", SwingConstants.CENTER);
            emojiLabel.setFont(new Font("Segoe UI", Font.PLAIN, 50));
            placeholder.setLayout(new BorderLayout());
            placeholder.add(emojiLabel, BorderLayout.CENTER);
            topPanel.add(placeholder, BorderLayout.WEST);
        }

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(new Color(255, 215, 0));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        topPanel.add(nameLabel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        // Текстовая область
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(30, 30, 40));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100), 1));
        scrollPane.setBackground(new Color(30, 30, 40));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Добавьте этот метод в GamePanel.java (перегрузка)
    private void showQuestDialog(QuestNPC npc, String dialogText, BufferedImage npcImage) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        QuestDialog dialog = new QuestDialog((JFrame) parent, npc, npcImage, dialogText, soundManager);

        // Добавляем обработчик закрытия
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                soundManager.stopVoiceClip();
            }
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                soundManager.stopVoiceClip();
            }
        });

        dialog.setVisible(true);
    }

    private void giveSavM43Reward() {
        int totalExp = 750;

        // Собираем всех живых членов команды
        List<Object> teamMembers = new ArrayList<>();
        if (player.health > 0) {
            teamMembers.add(player);
        }
        for (FriendlyUnit unit : world.getFriendlyUnits()) {
            if (unit.isAlive && unit.isRecruited) {
                teamMembers.add(unit);
            }
        }

        if (teamMembers.isEmpty()) return;

        // Равномерно распределяем опыт
        int expPerMember = totalExp / teamMembers.size();
        int remainder = totalExp % teamMembers.size();

        System.out.println("=== ВЫПОЛНЕНИЕ КВЕСТА SAV M/43 ===");
        System.out.println("🎉 Награда: " + totalExp + " опыта разделена на " + teamMembers.size() + " членов команды");

        for (int i = 0; i < teamMembers.size(); i++) {
            Object member = teamMembers.get(i);
            int memberExp = expPerMember + (i == 0 ? remainder : 0);

            if (member instanceof PlayerTank) {
                PlayerTank p = (PlayerTank) member;
                int oldLevel = p.getExperienceLevel();
                p.addExperience(memberExp);
                checkAndShowLevelUpDialogAfterExp(p, p.getExperienceSystem(), oldLevel);
                System.out.println("  + " + memberExp + " опыта Leichttraktor");
            } else if (member instanceof FriendlyUnit) {
                FriendlyUnit f = (FriendlyUnit) member;
                int oldLevel = f.getExperienceLevel();
                f.addExperience(memberExp);
                checkAndShowLevelUpDialogAfterExp(f, f.getExperienceSystem(), oldLevel);
                System.out.println("  + " + memberExp + " опыта " + f.name);
            }
        }

        // ===== НОВОЕ: ВЫДАЧА 37-ММ ШВЕДСКОГО ОРУДИЯ =====
        giveSwedishGunReward();

        player.calculateMovePoints();
        System.out.println("Награда получена!");
    }

    // ===== НОВЫЙ МЕТОД ДЛЯ ВЫДАЧИ ОРУДИЯ =====
    private void giveSwedishGunReward() {
        // Определяем, кому выдавать орудие (активный юнит или игрок)
        Object recipient;
        if (isPlayerActive()) {
            recipient = player;
        } else {
            recipient = getActiveFriendly();
            if (recipient == null) {
                // Если нет активного союзника - выдаём игроку
                recipient = player;
            }
        }

        // Создаём предмет орудия
        Item gunItem = new Item(Item.ItemType.WEAPON_37MM_SWEDEN, 1);

        // Создаём 37мм снаряды (40 штук)
        AmmoItem ammoItem = new AmmoItem(Caliber.CALIBER_37MM, 40);

        // Пытаемся добавить в инвентарь получателя
        boolean addedGun = false;
        boolean addedAmmo = false;

        if (recipient instanceof PlayerTank) {
            PlayerTank p = (PlayerTank) recipient;
            addedGun = p.getInventory().addItemToInventory(gunItem);
            if (addedGun) {
                System.out.println("🔫 Leichttraktor получил 37 mm kan m-38-49 strv в награду!");
            }
            // Добавляем снаряды
            addedAmmo = p.getInventory().addItemToInventory(ammoItem);
            if (addedAmmo) {
                System.out.println("📦 Leichttraktor получил 40 снарядов 37mm в награду!");
            }
        } else if (recipient instanceof FriendlyUnit) {
            FriendlyUnit f = (FriendlyUnit) recipient;
            addedGun = f.getInventory().addItemToInventory(gunItem);
            if (addedGun) {
                System.out.println("🔫 " + f.name + " получил 37 mm kan m-38-49 strv в награду!");
            }
            addedAmmo = f.getInventory().addItemToInventory(ammoItem);
            if (addedAmmo) {
                System.out.println("📦 " + f.name + " получил 40 снарядов 37mm в награду!");
            }
        }

        // Если не удалось добавить орудие или снаряды - создаём дроп на земле
        if (!addedGun || !addedAmmo) {
            System.out.println("⚠️ Нет места в инвентаре! Часть награды падает на землю.");

            int dropX, dropY;
            if (recipient instanceof PlayerTank) {
                dropX = ((PlayerTank) recipient).gridX;
                dropY = ((PlayerTank) recipient).gridY;
            } else if (recipient instanceof FriendlyUnit) {
                dropX = ((FriendlyUnit) recipient).gridX;
                dropY = ((FriendlyUnit) recipient).gridY;
            } else {
                dropX = player.gridX;
                dropY = player.gridY;
            }

            // Проверяем, есть ли уже дроп в этой клетке
            LootDrop existingDrop = world.getLootDropAt(dropX, dropY);
            LootDrop drop;

            if (existingDrop != null && existingDrop.isAlive) {
                drop = existingDrop;
                System.out.println("📦 Использую существующий дроп в клетке [" + dropX + "," + dropY + "]");
            } else {
                drop = new LootDrop(dropX, dropY);
                System.out.println("📦 Создаю новый дроп в клетке [" + dropX + "," + dropY + "]");
            }

            // Добавляем то, что не поместилось
            if (!addedGun) {
                drop.addItem(Item.ItemType.WEAPON_37MM_SWEDEN, 1);
                System.out.println("  ➕ Добавлено орудие в дроп");
            }
            if (!addedAmmo) {
                drop.addAmmo(Caliber.CALIBER_37MM, 40);
                System.out.println("  ➕ Добавлено 40 снарядов 37mm в дроп");
            }

            // Если это новый дроп - добавляем его на карту
            if (existingDrop == null || !existingDrop.isAlive) {
                world.createLootDrop(dropX, dropY, drop);
            }

            JOptionPane.showMessageDialog(this,
                    "⚠️ В инвентаре нет места!\n\n" +
                            "Часть награды упала на землю в клетке [" + dropX + "," + dropY + "]:\n" +
                            (!addedGun ? "  • 37 mm kan m-38-49 strv\n" : "") +
                            (!addedAmmo ? "  • 40 снарядов 37mm\n" : "") +
                            "\nПодойдите и подберите их.",
                    "Награда упала на землю",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            // Всё успешно добавлено - показываем сообщение
            String recipientName = (recipient instanceof PlayerTank) ? "Leichttraktor" : ((FriendlyUnit) recipient).name;
            JOptionPane.showMessageDialog(this,
                    "🎁 " + recipientName + " получил в награду:\n\n" +
                            "🔫 37 mm kan m-38-49 strv\n" +
                            "   (шведское 37-мм орудие)\n" +
                            "📦 40 снарядов 37mm\n\n" +
                            "📦 Проверьте инвентарь (клавиша I), чтобы экипировать и зарядить оружие!",
                    "Награда получена!",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        // Обновляем отображение
        repaint();
    }

    // Вспомогательный метод для отложенного показа диалога
    private void showQuestDialogDelayed(QuestNPC npc, String dialogText, int delayMs) {
        javax.swing.Timer timer = new javax.swing.Timer(delayMs, e -> {
            showQuestDialog(npc, dialogText);
        });
        timer.setRepeats(false);
        timer.start();
    }

    // Упрощённый метод без Clip параметра
    private void showQuestDialog(QuestNPC npc, String dialogText) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        // ВАЖНО: передаём soundManager в конструктор!
        QuestDialog dialog = new QuestDialog((JFrame) parent, npc, npcImage, dialogText, soundManager);
        dialog.setVisible(true);
    }

    // Добавьте метод выдачи награды:
    private void giveReward(QuestNPC npc) {
        int totalExp = 500;

        // Находим M53 в команде
        FriendlyUnit m53ToRemove = null;
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isAlive && friendly.isRecruited && "M53".equals(friendly.type)) {
                m53ToRemove = friendly;
                break;
            }
        }



        // Собираем всех живых членов команды, КРОМЕ M53
        List<Object> teamMembers = new ArrayList<>();
        if (player.health > 0) {
            teamMembers.add(player);
        }
        for (FriendlyUnit unit : world.getFriendlyUnits()) {
            if (unit.isAlive && unit.isRecruited) {
                // Исключаем M53 из получения опыта
                if (!"M53".equals(unit.type)) {
                    teamMembers.add(unit);
                } else {
                    System.out.println("⚠ " + unit.name + " исключён из получения опыта (покидает команду)");
                }
            }
        }

        if (teamMembers.isEmpty()) return;

        // Равномерно распределяем опыт между оставшимися
        int expPerMember = totalExp / teamMembers.size();
        int remainder = totalExp % teamMembers.size();

        System.out.println("=== ВЫПОЛНЕНИЕ КВЕСТА T18 ===");
        System.out.println("🎉 Награда: " + totalExp + " опыта разделена на " + teamMembers.size() + " членов команды");
        System.out.println("   (M53 исключён, так как покидает команду)");

        for (int i = 0; i < teamMembers.size(); i++) {
            Object member = teamMembers.get(i);
            int memberExp = expPerMember + (i == 0 ? remainder : 0);

            if (member instanceof PlayerTank) {
                PlayerTank p = (PlayerTank) member;
                int oldLevel = p.getExperienceLevel();
                p.addExperience(memberExp);
                // ===== ВАЖНО: проверяем повышение уровня =====
                checkAndShowLevelUpDialogAfterExp(p, p.getExperienceSystem(), oldLevel);
                System.out.println("  + " + memberExp + " опыта Leichttraktor");
            } else if (member instanceof FriendlyUnit) {
                FriendlyUnit f = (FriendlyUnit) member;
                int oldLevel = f.getExperienceLevel();
                f.addExperience(memberExp);
                // ===== ВАЖНО: проверяем повышение уровня =====
                checkAndShowLevelUpDialogAfterExp(f, f.getExperienceSystem(), oldLevel);
                System.out.println("  + " + memberExp + " опыта " + f.name);
            }
        }

        player.calculateMovePoints();

        System.out.println("Награда получена!");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // ===== ОБРАБОТКА КОМБИНАЦИЙ ДЛЯ ЧИТОВ =====
        checkCheatCombination(e);

        // ===== ПОЛНАЯ БЛОКИРОВКА ВО ВРЕМЯ ХОДА ВРАГА =====
        if (world.isEnemyTurn()) {
            System.out.println("⏳ СЕЙЧАС ХОД ВРАГОВ! НЕЛЬЗЯ НИЧЕГО ДЕЛАТЬ!");
            e.consume();
            return;
        }

        // Если игра окончена - блокируем все действия, кроме ESC для меню
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                GameMenu menu = new GameMenu(parentFrame, this, world, player);
                menu.setVisible(true);
            }
            return;
        }

        // Если игрок мёртв - тоже блокируем
        if (player.health <= 0) {
            System.out.println("Игра окончена! Нажмите ESC для меню.");
            return;
        }

        // ===== УПРАВЛЕНИЕ КАМЕРОЙ (СТРЕЛКИ) =====
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            cameraY = Math.max(0, cameraY - 50);
            repaint();
            e.consume();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            cameraY = Math.min(GameWorld.FIELD_SIZE - getHeight(), cameraY + 50);
            repaint();
            e.consume();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            cameraX = Math.max(0, cameraX - 50);
            repaint();
            e.consume();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            cameraX = Math.min(GameWorld.FIELD_SIZE - getWidth(), cameraX + 50);
            repaint();
            e.consume();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_M) {
            showMiniMap();
            e.consume();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_E) {
            if (isPlayerActive()) {
                if (!player.turnEnded) {
                    endPlayerTurnAndStartEnemyTurn();
                }
            } else {
                FriendlyUnit active = getActiveFriendly();
                if (active != null && !active.turnEnded) {
                    active.endTurn();
                    System.out.println(active.name + " завершил ход досрочно");
                    switchActiveUnit();
                }
            }
            e.consume();
            return;
        }

        // ===== КЛАВИША TAB - ПЕРЕКЛЮЧЕНИЕ ЮНИТОВ (САМАЯ ВАЖНАЯ!) =====
        if (e.getKeyCode() == KeyEvent.VK_X) {
            if (isMoving || isAutoMoving) {
                System.out.println("⚠ Нельзя переключиться во время движения игрока!");
                e.consume();
                return;
            }
            if (isFriendlyMoving || isFriendlyAutoMoving) {
                System.out.println("⚠ Нельзя переключиться во время движения союзника!");
                e.consume();
                return;
            }
            if (world.isAnyEnemyMoving()) {
                System.out.println("⚠ Нельзя переключиться во время хода врагов!");
                e.consume();
                return;
            }
            System.out.println("TAB НАЖАТ! Переключение юнитов...");
            switchActiveUnit();
            e.consume();
            return;
        }

        // ===== КЛАВИША V - ПЕРЕКЛЮЧЕНИЕ МЕЖДУ ВИДИМЫМИ ВРАГАМИ =====
        if (e.getKeyCode() == KeyEvent.VK_V) {
            cycleToNextVisibleEnemy();
            e.consume();
            return;
        }

        // ===== КЛАВИША B - ВОЗВРАТ КАМЕРЫ К АКТИВНОМУ ЮНИТУ =====
        if (e.getKeyCode() == KeyEvent.VK_B) {
            returnCameraToActiveUnit();
            e.consume();
            return;
        }

        // ===== КЛАВИША C - ЦЕНТРИРОВАНИЕ КАМЕРЫ =====
        if (e.getKeyCode() == KeyEvent.VK_C) {
            if (isPlayerActive()) {
                centerCameraOnUnit(player.gridX, player.gridY);
            } else {
                FriendlyUnit active = getActiveFriendly();
                if (active != null) {
                    centerCameraOnUnit(active.gridX, active.gridY);
                }
            }
            System.out.println("📷 Камера отцентрирована");
            e.consume();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (!isMoving && !world.isAnyEnemyMoving()) {
                GameMenu menu = new GameMenu(parentFrame, this, world, player);
                menu.setVisible(true);
            }
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_F) {
            isAimingMode = !isAimingMode;
            if (isAimingMode) {
                System.out.println("🔫 РЕЖИМ ПРИЦЕЛЬНОГО ОГНЯ");
            } else {
                System.out.println("💥 РЕЖИМ БЕГЛОГО ОГНЯ");
            }
            repaint();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_I) {
            openInventory();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_R) {
            targetShotX = -1;
            targetShotY = -1;
            isAimingMode = false;
            System.out.println("❌ Цель сброшена");
            repaint();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE && targetShotX != -1 && targetShotY != -1) {
            boolean enemyStillAlive = false;
            for (Enemy enemy : world.getEnemies()) {
                if (enemy.isAlive && enemy.gridX == targetShotX && enemy.gridY == targetShotY) {
                    enemyStillAlive = true;
                    break;
                }
            }

            if (!enemyStillAlive) {
                System.out.println("⚠ Цель уже уничтожена! Выберите нового врага.");
                targetShotX = -1;
                targetShotY = -1;
                repaint();
                return;
            }

            performShot(targetShotX, targetShotY);
            return;
        }

        // ===== ДВИЖЕНИЕ ЮНИТА (только если не зажат CTRL) =====
        if (isMoving || isAutoMoving) {
            System.out.println("⚠ Игрок двигается, подождите...");
            return;
        }

        if (isFriendlyMoving || isFriendlyAutoMoving) {
            System.out.println("⚠ Союзник двигается, подождите...");
            return;
        }

        if (world.isAnyEnemyMoving()) {
            System.out.println("⚠ Враги двигаются, подождите...");
            return;
        }

        if (player.turnEnded && isPlayerActive()) {
            System.out.println("Ход завершён! Нажмите E для нового хода.");
            return;
        }

        // Определяем направление и новые координаты для движения
        int newX = -1, newY = -1;
        PlayerTank.Direction dir = null;

        switch(e.getKeyCode()) {
            case KeyEvent.VK_W:
                dir = PlayerTank.Direction.UP;
                break;
            case KeyEvent.VK_S:
                dir = PlayerTank.Direction.DOWN;
                break;
            case KeyEvent.VK_A:
                dir = PlayerTank.Direction.LEFT;
                break;
            case KeyEvent.VK_D:
                dir = PlayerTank.Direction.RIGHT;
                break;
            default: return;
        }

        // В методе keyPressed, в блоке движения игрока:
        if (isPlayerActive()) {
            // Движение игрока
            newX = player.gridX;
            newY = player.gridY;
            switch(dir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            // ===== ПРОВЕРКА ПЕРЕГРУЗА =====
            if (player.isOverweight()) {
                JOptionPane.showMessageDialog(this,
                        "⚠ ПЕРЕГРУЗ! ДВИЖЕНИЕ НЕВОЗМОЖНО!\n\n" +
                                "Текущий вес: " + String.format("%.1f", player.getInventory().getTotalWeight()) + "/" +
                                String.format("%.1f", player.maxCarryWeight) + "\n" +
                                "Сбросьте лишний груз, чтобы восстановить подвижность.",
                        "Невозможно двигаться", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!player.canMove()) {
                System.out.println("⚠ Недостаточно очков хода у игрока! Нужно: " + player.moveCost);
                return;
            }

            if (world.canMoveTo(newX, newY)) {
                startMove(newX, newY, dir, false);
            } else {
                System.out.println("❌ Нельзя туда двигаться!");
            }
        } else {
            // Движение союзника
            FriendlyUnit active = getActiveFriendly();
            if (active == null) {
                System.out.println("❌ Нет активного союзника!");
                return;
            }

            // ===== ПРОВЕРКА ПЕРЕГРУЗА =====
            if (active.isOverweight()) {
                JOptionPane.showMessageDialog(this,
                        "⚠ ПЕРЕГРУЗ! " + active.name + " не может двигаться!\n\n" +
                                "Текущий вес: " + String.format("%.1f", active.getInventory().getTotalWeight()) + "/" +
                                String.format("%.1f", active.maxCarryWeight) + "\n" +
                                "Сбросьте лишний груз, чтобы восстановить подвижность.",
                        "Невозможно двигаться", JOptionPane.WARNING_MESSAGE);
                return;
            }

            newX = active.gridX;
            newY = active.gridY;
            switch(dir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            if (!active.canMove()) {
                System.out.println("⚠ Недостаточно очков хода у " + active.name + "! Нужно: " + active.moveCost);
                return;
            }

            newX = active.gridX;
            newY = active.gridY;
            switch(dir) {
                case UP: newY--; break;
                case DOWN: newY++; break;
                case LEFT: newX--; break;
                case RIGHT: newX++; break;
            }

            if (!active.canMove()) {
                System.out.println("⚠ Недостаточно очков хода у " + active.name + "! Нужно: " + active.moveCost);
                return;
            }

            if (world.canMoveToForFriendly(active, newX, newY)) {
                active.consumeMovePoints();
                switch(dir) {
                    case UP: active.currentDirection = FriendlyUnit.Direction.UP; break;
                    case DOWN: active.currentDirection = FriendlyUnit.Direction.DOWN; break;
                    case LEFT: active.currentDirection = FriendlyUnit.Direction.LEFT; break;
                    case RIGHT: active.currentDirection = FriendlyUnit.Direction.RIGHT; break;
                }
                active.gridX = newX;
                active.gridY = newY;

                world.updateVisibilityForFriendly(active);
                checkEnemyDetection();

                repaint();
                System.out.println(active.name + " переместился на [" + newX + "," + newY +
                        "], осталось очков: " + active.movePoints + "/" + active.maxMovePoints);
            } else {
                System.out.println("❌ Нельзя туда двигаться! (стена, враг или дерево)");
            }
        }
    }

    private void showMiniMap() {
        if (world.isEnemyTurn()) {
            System.out.println("⏳ Нельзя открыть карту во время хода врагов!");
            return;
        }

        MiniMapDialog miniMap = new MiniMapDialog(parentFrame, world, player);
        miniMap.setVisible(true);
    }

    // В GamePanel.java добавьте метод показа диалога победы:
    public void showVictoryDialog() {
        if (gameOver) return;
        gameOver = true;

        if (timer != null) {
            timer.stop();
        }

        String message = "🏆 ПОБЕДА! 🏆\n\n" +
                "Вы достигли победной зоны!\n" +
                "Уничтожено врагов: " + world.getTotalEnemiesKilled() + "\n\n" +
                "Хотите продолжить игру (убить оставшихся врагов) или выйти?";

        int option = JOptionPane.showConfirmDialog(this,
                message,
                "Победа!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            // Продолжаем игру (просто закрываем диалог)
            gameOver = false;
            timer.start();
        } else {
            // Выход в главное меню или перезапуск
            int restart = JOptionPane.showConfirmDialog(this,
                    "Начать новую игру или выйти?",
                    "Завершение игры",
                    JOptionPane.YES_NO_OPTION);
            if (restart == JOptionPane.YES_OPTION) {
                restartGame();
            } else {
                System.exit(0);
            }
        }
    }

    // В методе paintComponent, после отрисовки холмов, добавьте отрисовку победной зоны:
    private void drawWinZone(Graphics2D g) {
        Point winZone = world.getWinZone();
        if (winZone == null) return;

        boolean isVisible = world.isWinZoneVisible();

        for (int dx = 0; dx < 5; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                int x = (winZone.x + dx) * GameWorld.CELL_SIZE - cameraX;
                int y = (winZone.y + dy) * GameWorld.CELL_SIZE - cameraY;

                if (isVisible) {
                    // Видимая зона - яркая
                    g.setColor(new Color(0, 200, 0, 100));
                    g.fillRect(x, y, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);
                    g.setColor(new Color(0, 255, 0, 200));
                    g.setStroke(new BasicStroke(2));
                    g.drawRect(x, y, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);
                } else {
                    // Невидимая зона - тусклая (но всё равно видна на миникарте)
                    g.setColor(new Color(0, 100, 0, 50));
                    g.fillRect(x, y, GameWorld.CELL_SIZE, GameWorld.CELL_SIZE);
                }
            }
        }

        // Рисуем флаг в центре зоны
        int centerX = (winZone.x + 2) * GameWorld.CELL_SIZE - cameraX;
        int centerY = (winZone.y + 2) * GameWorld.CELL_SIZE - cameraY;

        g.setColor(new Color(255, 215, 0, 200));
        g.fillRect(centerX + 5, centerY - 15, 5, 25);
        g.setColor(new Color(255, 0, 0, 200));
        g.fillRect(centerX + 10, centerY - 15, 15, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("★", centerX + 12, centerY - 7);
    }

// В paintComponent добавьте вызов drawWinZone(g2d) после отрисовки холмов

    // Добавьте метод открытия инвентаря:
    private void openInventory() {
        if (isPlayerActive()) {
            InventoryDialog dialog = new InventoryDialog(parentFrame, player, heroPortrait, this);
            dialog.setVisible(true);
        } else {
            FriendlyUnit active = getActiveFriendly();
            if (active != null  && active.isAlive) {
                BufferedImage portrait = null;
                if ("M53".equals(active.type)) portrait = m53Portrait;
                else if ("MS-1".equals(active.type)) portrait = ms1Portrait;
                else if ("VK10001P".equals(active.type)) {  // ← ДОБАВЬТЕ
                    portrait = vk10001pPortrait;          // ← ДОБАВЬТЕ
                }
                else if ("AMX40".equals(active.type)) {
                    portrait = amx40Portrait;     // ← ДОБАВЬТЕ
                }
                else if ("T1".equals(active.type)) {
                    portrait = t1Portrait;     // ← ДОБАВЬТЕ
                }
                InventoryDialog dialog = new InventoryDialog(parentFrame, active, portrait, player, this);
                dialog.setVisible(true);
            }
        }
        repaint();
    }

    public boolean isGameOver() {
        return player.health <= 0;
    }

    // Исправьте метод showGameOverDialog:
    public void showGameOverDialog() {
        System.out.println("=== showGameOverDialog ВЫЗВАН ===");
        if (gameOver) {
            System.out.println("  gameOver уже true, выходим");
            return;
        }
        gameOver = true;
        System.out.println("  gameOver установлен в true");

        if (timer != null) {
            timer.stop();
            System.out.println("  Таймер остановлен");
        }

        String message = "💀 ИГРА ОКОНЧЕНА 💀\n\n" +
                "Ваш Leichttraktor уничтожен!\n" +
                "Уничтожено врагов: " + world.getTotalEnemiesKilled() + "\n\n" +
                "Хотите начать заново или выйти?";

        System.out.println("  Показываем диалог...");
        int option = JOptionPane.showConfirmDialog(this,
                message,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        System.out.println("  Результат диалога: " + option);

        if (option == JOptionPane.YES_OPTION) {
            System.out.println("  Перезапуск игры...");
            restartGame();
        } else {
            System.out.println("  Выход из игры...");
            System.exit(0);
        }
    }

    private void restartGame() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof GameFrame) {
            window.dispose();
        }

        SwingUtilities.invokeLater(() -> {
            GameFrame newFrame = new GameFrame();
            newFrame.setVisible(true);
        });
    }

    // Добавьте метод для обработки чит-комбинаций
    private boolean cheatMenuOpen = false;
    private void checkCheatCombination(KeyEvent e) {
        // Обновляем состояние клавиш
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlPressed = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed = true;
        }

        // Сброс флагов при отпускании любой из клавиш-модификаторов
        if (e.getKeyCode() == KeyEvent.VK_CONTROL && !e.isControlDown()) {
            ctrlPressed = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_SHIFT && !e.isShiftDown()) {
            shiftPressed = false;
        }

        // Ctrl + Shift + D (только если обе клавиши зажаты)
        if (ctrlPressed && shiftPressed && e.getKeyCode() == KeyEvent.VK_D) {
            if (!cheatMenuOpen) {
                cheatMenuOpen = true;
                showCheatMenu();
                cheatMenuOpen = false;

                // Сброс флагов после использования
                ctrlPressed = false;
                shiftPressed = false;
            }
            e.consume();
        }
    }

    private void loadHeroPortrait() {
        String portraitPath = player.getPortraitPath();
        File portraitFile = new File(portraitPath);

        try {
            if (portraitFile.exists()) {
                heroPortrait = ImageIO.read(portraitFile);
                portraitLoaded = true;
                System.out.println("✅ Загружен портрет: " + portraitFile.getName());
            } else {
                // Fallback на стандартный портрет
                String defaultPath = "src/PositiveHeroes/ImageOfHeroes/Leichttraktor.png";
                File defaultFile = new File(defaultPath);
                if (defaultFile.exists()) {
                    heroPortrait = ImageIO.read(defaultFile);
                    portraitLoaded = true;
                    System.out.println("⚠ Портрет не найден, загружен стандартный: " + defaultPath);
                } else {
                    System.err.println("❌ Портрет не найден: " + portraitPath);
                    portraitLoaded = false;
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки портрета: " + e.getMessage());
            portraitLoaded = false;
        }
    }

    public void updateHeroPortrait() {
        loadHeroPortrait();
        repaint();
    }

    // Показ меню читов
    // Показ меню читов (вертикальное расположение)
    private void showCheatMenu() {
        // Создаём панель с вертикальным расположением
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(20, 20, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("🐛 РЕЖИМ РАЗРАБОТЧИКА 🐛");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Кнопки читов
        String[] cheatNames = {
                "💪 SUPER STRENGTH - Сила 999",
                "❤️ MAX HEALTH - Полное здоровье",
                "⭐ MAX MOVE POINTS - Полные очки хода",
                "🔫 GOD WEAPON - Ультимативное оружие",
                "💰 ADD SILVER - +1000 серебра",
                "⚙️ ADD PARTS - +500 деталей",
                "📈 LEVEL UP - +1 уровень активному юниту",
                "✨ ALL BONUSES - Все бонусы опыта",
                "🔧 MAX UPGRADE - Максимальная модернизация",
                "🗡️ INSTANT KILL - Убить цель под курсором",
                "🔓 UNLOCK ALL - Открыть всех союзников",
                "🎯 SUPER ACCURACY - Точность 999",
                "🛡️ GOD ARMOR - Броня 999",
                "🌀 TELEPORT - Телепортация в выбранную клетку",
                "👁️ REVEAL MAP - Видимость всей карты"  // ← НОВЫЙ ЧИТ
        };

        JButton[] buttons = new JButton[cheatNames.length];

        for (int i = 0; i < cheatNames.length; i++) {
            buttons[i] = createCheatButton(cheatNames[i], i);
            buttons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(buttons[i]);
            panel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // Кнопка отмены
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        JButton cancelButton = new JButton("❌ ЗАКРЫТЬ");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton.setBackground(new Color(100, 100, 100));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(panel);
            if (w != null) w.dispose();
        });
        panel.add(cancelButton);

        // Создаём диалог
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                "Cheat Menu (Ctrl+Shift+D)", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(panel), BorderLayout.CENTER);
        dialog.setSize(400, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Вспомогательный метод для создания кнопки чита
    private JButton createCheatButton(String text, int cheatIndex) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBackground(new Color(60, 60, 80));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(350, 35));
        button.setPreferredSize(new Dimension(350, 35));

        // Цвета для разных категорий читов
        if (text.startsWith("💪") || text.startsWith("🛡️") || text.startsWith("🎯")) {
            button.setBackground(new Color(100, 50, 150)); // Фиолетовый - баффы
        } else if (text.startsWith("❤️") || text.startsWith("⭐")) {
            button.setBackground(new Color(0, 100, 150)); // Синий - восстановление
        } else if (text.startsWith("💰") || text.startsWith("⚙️")) {
            button.setBackground(new Color(0, 120, 0)); // Зелёный - ресурсы
        } else if (text.startsWith("🔫") || text.startsWith("🗡️")) {
            button.setBackground(new Color(150, 50, 50)); // Красный - оружие/убийство
        } else if (text.startsWith("📈") || text.startsWith("✨") || text.startsWith("🔧")) {
            button.setBackground(new Color(200, 100, 0)); // Оранжевый - прогрессия
        } else {
            button.setBackground(new Color(60, 60, 80)); // Серый - остальное
        }

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(brighten(button.getBackground()));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground());
            }
        });

        button.addActionListener(e -> {
            applyCheat(cheatIndex);
            Window w = SwingUtilities.getWindowAncestor(button);
            if (w != null) w.dispose();
        });

        return button;
    }

    // Вспомогательный метод для осветления цвета
    private Color brighten(Color color) {
        return new Color(
                Math.min(255, color.getRed() + 40),
                Math.min(255, color.getGreen() + 40),
                Math.min(255, color.getBlue() + 40)
        );
    }

    // Применение выбранного чита
    private void applyCheat(int cheatIndex) {
        switch(cheatIndex) {
            case 0: // SUPER STRENGTH
                applySuperStrength();
                break;
            case 1: // MAX HEALTH
                applyMaxHealth();
                break;
            case 2: // MAX MOVE POINTS
                applyMaxMovePoints();
                break;
            case 3: // GOD WEAPON
                applyGodWeapon();
                break;
            case 4: // ADD SILVER
                player.addSilver(1000);
                JOptionPane.showMessageDialog(this, "💰 Добавлено 1000 серебра! Всего: " + player.getSilver());
                break;
            case 5: // ADD PARTS
                player.addParts(500);
                JOptionPane.showMessageDialog(this, "⚙️ Добавлено 500 деталей! Всего: " + player.getParts());
                break;
            case 6: // LEVEL UP
                levelUpActiveUnit();
                break;
            case 7: // ALL BONUSES
                applyAllBonuses();
                break;
            case 8: // MAX UPGRADE
                applyMaxUpgrade();
                break;
            case 9: // INSTANT KILL
                instantKillTarget();
                break;
            case 10: // UNLOCK ALL
                unlockAllAllies();
                break;
            case 11: // SUPER ACCURACY
                applySuperAccuracy();
                break;
            case 12: // GOD ARMOR
                applyGodArmor();
                break;
            case 13: // TELEPORT
                teleportActiveUnit();
                break;
            case 14: // REVEAL MAP
                toggleFullMapReveal();
                break;
        }

        // Сбрасываем флаги после применения
        ctrlPressed = false;
        shiftPressed = false;

        repaint();
    }

    // Чит 0: SUPER STRENGTH (Сила 999 всем членам команды)
    private void applySuperStrength() {
        // Игрок
        player.strength = 999;
        player.calculateMovePoints();
        player.movePoints = player.maxMovePoints;

        // Все нанятые союзники
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isRecruited && friendly.isAlive) {
                friendly.strength = 999;
                friendly.calculateMovePoints();
                friendly.movePoints = friendly.maxMovePoints;
            }
        }

        JOptionPane.showMessageDialog(this,
                "💪 SUPER STRENGTH АКТИВИРОВАН!\n\n" +
                        "Leichttraktor и все союзники получили силу 999!\n" +
                        "⭐ Очки хода увеличены до максимума!");
    }

    // Чит 1: MAX HEALTH (Полное здоровье всем)
    private void applyMaxHealth() {
        // Игрок
        player.health = player.maxHealth;

        // Все союзники (включая ненаймлённых - для тестирования)
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isAlive) {
                friendly.health = friendly.maxHealth;
            }
        }

        // Все враги (опционально - для баланса)
        // for (Enemy enemy : world.getEnemies()) {
        //     if (enemy.isAlive) {
        //         enemy.health = enemy.maxHealth;
        //     }
        // }

        JOptionPane.showMessageDialog(this, "❤️ MAX HEALTH АКТИВИРОВАН!\n\nВсе союзники полностью исцелены!");
    }

    // Чит 2: MAX MOVE POINTS (Полные очки хода всем)
    private void applyMaxMovePoints() {
        // Игрок
        player.movePoints = player.maxMovePoints;
        player.turnEnded = false;

        // Все нанятые союзники
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isRecruited && friendly.isAlive) {
                friendly.movePoints = friendly.maxMovePoints;
                friendly.turnEnded = false;
            }
        }

        JOptionPane.showMessageDialog(this, "⭐ MAX MOVE POINTS АКТИВИРОВАН!\n\nВсе очки хода восстановлены!");
    }

    // Чит 3: GOD WEAPON (Ультимативное оружие)
    private void applyGodWeapon() {
        PlayerTank.WeaponData godWeapon = new PlayerTank.WeaponData(
                "💀 GOD KILLER 💀",           // имя
                "203mm",
                Caliber.CALIBER_203MM,        // калибр
                10,                           // burstSize - 10 снарядов за выстрел
                999,                          // weaponAccuracy - 999% точность
                1.0,                          // weaponCaliber - 1 метр!
                1,                            // shotCost - 1 очко хода за выстрел
                1,                            // aimedShotCost - 1 очко хода за прицельный
                0,                            // reloadCost - бесплатная перезарядка
                9999,                         // weaponDamage - 9999 урона
                1.0,                          // critChance - 100% крит
                "src/ObjectsOfInventory/Weapon/2 cm Breda (I).png",
                0,                            // requiredStrength
                0                             // weight
        );

        player.setEquippedWeapon("god", godWeapon);
        player.getInventory().restoreAmmo(99, 99, 0, 10);

        JOptionPane.showMessageDialog(this,
                "🔫 GOD WEAPON АКТИВИРОВАН!\n\n" +
                        "💀 Оружие: GOD KILLER\n" +
                        "💥 Урон: 9999\n" +
                        "⚡ Критический шанс: 100%\n" +
                        "🎯 Точность: 999%\n" +
                        "📦 Боезапас: 99 снарядов\n" +
                        "⭐ Стоимость выстрела: 1 ОХ");
    }

    // Чит 6: LEVEL UP (Повышение уровня активного юнита)
    // Чит 6: LEVEL UP (Повышение уровня активного юнита)
    private void levelUpActiveUnit() {
        Object unit = isPlayerActive() ? player : getActiveFriendly();
        if (unit == null) return;

        ExperienceSystem expSystem = null;
        String unitName = "";

        if (unit instanceof PlayerTank) {
            expSystem = ((PlayerTank) unit).getExperienceSystem();
            unitName = "Leichttraktor";
        } else if (unit instanceof FriendlyUnit) {
            expSystem = ((FriendlyUnit) unit).getExperienceSystem();
            unitName = ((FriendlyUnit) unit).name;
        }

        if (expSystem != null) {
            // Получаем текущий уровень ДО повышения
            int oldLevel = expSystem.getLevel();

            // Добавляем ровно столько опыта, сколько нужно для следующего уровня
            int neededExp = expSystem.getExperienceForNextLevel();
            if (neededExp == Integer.MAX_VALUE) {
                JOptionPane.showMessageDialog(this,
                        "⚠️ " + unitName + " уже достиг максимального уровня (10)!",
                        "Нельзя повысить", JOptionPane.WARNING_MESSAGE);
                return;
            }

            expSystem.addExperience(neededExp);

            // Проверяем, повысился ли уровень
            if (expSystem.getLevel() > oldLevel) {
                JOptionPane.showMessageDialog(this,
                        "📈 " + unitName + " повысил уровень до " + expSystem.getLevel() + "!\n\n" +
                                "🎁 Открыто окно выбора бонусов!",
                        "Уровень повышен!", JOptionPane.INFORMATION_MESSAGE);

                // Показываем диалог выбора бонусов
                checkAndShowLevelUpDialog(unit, expSystem);
            } else {
                JOptionPane.showMessageDialog(this,
                        "⚠️ Не удалось повысить уровень!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
        repaint();
    }

    // Чит 7: ALL BONUSES (Все бонусы опыта)
    private void applyAllBonuses() {
        Object unit = isPlayerActive() ? player : getActiveFriendly();
        if (unit == null) return;

        ExperienceSystem expSystem = null;
        if (unit instanceof PlayerTank) {
            expSystem = ((PlayerTank) unit).getExperienceSystem();
        } else if (unit instanceof FriendlyUnit) {
            expSystem = ((FriendlyUnit) unit).getExperienceSystem();
        }

        if (expSystem != null) {
            // Создаём бонусную систему, если её нет
            if (!expSystem.hasPendingBonuses()) {
                // Искусственно повышаем уровень для получения бонусов
                expSystem.addExperience(expSystem.getExperienceForNextLevel());
            }

            LevelUpBonus bonus = expSystem.getPendingLevelUpBonus();
            if (bonus != null) {
                // Применяем все бонусы
                for (LevelUpBonus.BonusType bt : LevelUpBonus.BonusType.values()) {
                    while (bonus.getRemainingBonuses() > 0) {
                        bonus.applyBonus(bt, unit);
                    }
                }
                expSystem.clearPendingBonuses();
            }

            String name = (unit instanceof PlayerTank) ? "Leichttraktor" : ((FriendlyUnit) unit).name;
            JOptionPane.showMessageDialog(this,
                    "✨ " + name + " получил все бонусы опыта!\n" +
                            "Все характеристики увеличены до максимума!");
        }
    }

    // Чит 8: MAX UPGRADE (Максимальная модернизация)
    private void applyMaxUpgrade() {
        Object unit = isPlayerActive() ? player : getActiveFriendly();
        if (unit == null) return;

        // Добавляем достаточно деталей
        player.addParts(2000);

        if (unit instanceof PlayerTank) {
            PlayerTank p = (PlayerTank) unit;
            while (p.getUpgradeLevel() < 4) {
                p.performUpgrade("TT", player.getParts());
                player.removeParts(p.getUpgradeCost());
            }
            updatePlayerTextures();
            JOptionPane.showMessageDialog(this, "🔧 Leichttraktor модернизирован до ТТ-4!");
        } else if (unit instanceof FriendlyUnit) {
            FriendlyUnit f = (FriendlyUnit) unit;
            while (f.getUpgradeLevel() < 4) {
                f.performUpgrade("TT", player.getParts());
                player.removeParts(f.getUpgradeCost());
            }
            JOptionPane.showMessageDialog(this, "🔧 " + f.name + " модернизирован до максимального уровня!");
        }
    }

    // Чит 9: INSTANT KILL (Мгновенное убийство цели под курсором)
    private void instantKillTarget() {
        Point mousePos = getMousePosition();
        if (mousePos == null) {
            JOptionPane.showMessageDialog(this, "Наведите курсор на врага и нажмите чит!");
            return;
        }

        int gridX = (mousePos.x + cameraX) / GameWorld.CELL_SIZE;
        int gridY = (mousePos.y + cameraY) / GameWorld.CELL_SIZE;

        for (Enemy enemy : world.getEnemies()) {
            if (enemy.isAlive && enemy.gridX == gridX && enemy.gridY == gridY) {
                // Мгновенное убийство
                enemy.health = 0;
                enemy.isAlive = false;
                world.dropLootFromEnemy(enemy);
                world.updateQuestProgress();
                JOptionPane.showMessageDialog(this, "🗡️ " + enemy.type + " мгновенно уничтожен!");
                repaint();
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "❌ Нет врага под курсором!");
    }

    // Чит 10: UNLOCK ALL (Открыть всех союзников)
    private void unlockAllAllies() {
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (!friendly.isRecruited && friendly.isAlive && !friendly.isUnavailable) {
                world.recruitFriendly(friendly);
            }
        }
        syncRecruitedUnits();
        JOptionPane.showMessageDialog(this, "🔓 Все союзники добавлены в команду!");
    }

    // Чит 11: SUPER ACCURACY
    private void applySuperAccuracy() {
        player.weaponAccuracy = 999;
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isRecruited && friendly.isAlive) {
                friendly.weaponAccuracy = 999;
            }
        }
        JOptionPane.showMessageDialog(this, "🎯 SUPER ACCURACY АКТИВИРОВАН!\n\nТочность всех юнитов: 999%");
    }

    // Чит 12: GOD ARMOR
    private void applyGodArmor() {
        player.armor = 999;
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isRecruited && friendly.isAlive) {
                friendly.armor = 999;
            }
        }
        JOptionPane.showMessageDialog(this, "🛡️ GOD ARMOR АКТИВИРОВАН!\n\nБроня всех юнитов: 999");
    }

    // Чит 13: TELEPORT (Телепортация активного юнита)
    private void teleportActiveUnit() {
        // Создаём панель для ввода координат
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 79, 1));
        JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 79, 1));

        // Текущие координаты активного юнита
        int currentX, currentY;
        if (isPlayerActive()) {
            currentX = player.gridX;
            currentY = player.gridY;
        } else {
            FriendlyUnit active = getActiveFriendly();
            currentX = active != null ? active.gridX : 0;
            currentY = active != null ? active.gridY : 0;
        }
        xSpinner.setValue(currentX);
        ySpinner.setValue(currentY);

        panel.add(new JLabel("Координата X (0-79):"));
        panel.add(xSpinner);
        panel.add(new JLabel("Координата Y (0-79):"));
        panel.add(ySpinner);
        panel.add(new JLabel("Текущая позиция:"));
        panel.add(new JLabel("[" + currentX + "," + currentY + "]"));

        int option = JOptionPane.showConfirmDialog(this, panel,
                "🌀 ТЕЛЕПОРТАЦИЯ 🌀",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            int targetX = (Integer) xSpinner.getValue();
            int targetY = (Integer) ySpinner.getValue();

            // Проверяем границы
            int gridWidth = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE;
            int gridHeight = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE;

            if (targetX < 0 || targetX >= gridWidth || targetY < 0 || targetY >= gridHeight) {
                JOptionPane.showMessageDialog(this,
                        "❌ Неправильные координаты! Допустимый диапазон: X: 0-" + (gridWidth-1) +
                                ", Y: 0-" + (gridHeight-1),
                        "Ошибка телепортации", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Телепортируем
            boolean success = teleportTo(targetX, targetY);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "🌀 ТЕЛЕПОРТАЦИЯ УСПЕШНА! 🌀\n\n" +
                                getActiveUnitName() + " перемещён из [" + currentX + "," + currentY +
                                "] в [" + targetX + "," + targetY + "]",
                        "Телепортация", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Не удалось телепортироваться в [" + targetX + "," + targetY + "]\n" +
                                "Клетка занята другим юнитом или препятствием!",
                        "Ошибка телепортации", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // Чит 14: REVEAL MAP - Видимость всей карты
    private void toggleFullMapReveal() {
        isFullMapRevealed = !isFullMapRevealed;

        if (isFullMapRevealed) {
            // Принудительно обновляем карту видимости
            world.revealFullMap();
            JOptionPane.showMessageDialog(this,
                    "👁️ FULL MAP REVEAL АКТИВИРОВАН!\n\n" +
                            "Вся карта теперь видна!\n" +
                            "Нажмите снова Ctrl+Shift+D и выберите REVEAL MAP, чтобы отключить.",
                    "Режим видимости", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Возвращаем нормальную видимость
            world.disableFullMapReveal();
            world.updateVisibilityMap();
            world.updateAllFriendlyVisibilityMaps();
            JOptionPane.showMessageDialog(this,
                    "👁️ FULL MAP REVEAL ОТКЛЮЧЁН!\n\n" +
                            "Нормальная видимость восстановлена.",
                    "Режим видимости", JOptionPane.INFORMATION_MESSAGE);
        }

        // Обновляем отображение
        repaint();
    }

    // Метод для перехода на следующий уровень
    public void loadNextLevel() {

        currentLevelIndex++;

        if (currentLevelIndex >= levelPaths.length) {
            // Игра пройдена!
            showGameCompleteDialog();
            return;
        }


        String nextLevelPath = levelPaths[currentLevelIndex];
        this.currentLevelPath = nextLevelPath;

        // Сохраняем текущее состояние перед загрузкой
        saveCurrentState();

        // Загружаем следующий уровень
        world.loadNextLevel(nextLevelPath, this);

        // Обновляем камеру
        centerCameraOnUnit(world.getPlayer().gridX, world.getPlayer().gridY);

        // Обновляем интерфейс
        syncRecruitedUnits();
        repaint();

        // Показываем сообщение о переходе
        JOptionPane.showMessageDialog(this,
                "🏆 УРОВЕНЬ " + currentLevelIndex + " ПРОЙДЕН! 🏆\n\n" +
                        "Переход на уровень " + (currentLevelIndex + 1) + "...\n" +
                        "Ваша команда сохранена!",
                "Новый уровень",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveCurrentState() {
        // Сохраняем состояние в отдельный файл для возможности продолжения
        // или просто передаём через world.loadNextLevel
    }

    private void showGameCompleteDialog() {
        String message = "🎉 ПОЗДРАВЛЯЕМ! 🎉\n\n" +
                "Вы прошли все уровни!\n" +
                "Уничтожено врагов: " + world.getTotalEnemiesKilled() + "\n\n" +
                "Спасибо за игру!";

        int option = JOptionPane.showConfirmDialog(this,
                message,
                "Игра пройдена!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            // Перезапуск игры или выход в меню
            restartGame();
        } else {
            System.exit(0);
        }
    }

    // Сброс модификаторов при отпускании клавиш
    private void resetCheatModifiers() {
        ctrlPressed = false;
        shiftPressed = false;
    }

    public GameWorld getWorld() {
        return world;
    }

    public BufferedImage getHeroPortrait() { return heroPortrait; }
    public BufferedImage getM53Portrait() { return m53Portrait; }
    public BufferedImage getMS1Portrait() { return ms1Portrait; }
    public BufferedImage getVK10001PPortrait() { return vk10001pPortrait; }
    public BufferedImage getAMX40Portrait() {  // ← ДОБАВЬТЕ ЭТОТ МЕТОД
        return amx40Portrait;
    }
    public BufferedImage getT1Portrait() { return t1Portrait; }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlPressed = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressed = false;
        }
    }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
    public SoundManager getSoundManager() {
        return soundManager;
    }


    public String getCurrentLevelPath() {
        return currentLevelPath;
    }

    public void setCurrentLevelPath(String path) {
        this.currentLevelPath = path;
        System.out.println("✅ Путь к уровню установлен: " + path);
    }
}