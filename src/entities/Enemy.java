package entities;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

import world.GameWorld;
import ui.GamePanel;

public class Enemy {
    public int gridX, gridY;
    public Direction direction;
    public int health;
    public int maxHealth;
    public boolean isAlive;

    private GameWorld gameWorld;

    private Faction faction = Faction.GERMANY;  // По умолчанию Германия

    public String type = "Leichttraktor";

    public int stuckInPatrolCounter = 0;  // Счётчик застревания в патруле
    private Point lastPosition = null;      // Последняя позиция для проверки

    private int shotsFiredThisTurn = 0;      // Сколько выстрелов сделано в этом ходу
    private int hitsThisTurn = 0;            // Сколько попаданий было в этом ходу
    private int turnsSinceLastAssault = 0;   // Сколько ходов прошло с последней штыковой атаки

    private Point assaultPoint = null;
    private boolean isAssaultMode = false;

    private Point lastHeardSoundPosition = null;  // Последняя услышанная позиция выстрела
    private int soundMemoryTurns = 0;             // Сколько ходов помнит звук

    public int noTargetTurns = 0;// Счётчик ходов без видимой цели
    private boolean isInvestigatingSound = false;  // Исследует ли звук


    private Point currentMovementTarget = null;  // Текущая цель движения
    private int stuckCounter = 0;  // Счётчик "застревания"

    private List<Point> cachedPath = null;
    private int cachedPathIndex = 0;

    // В Enemy.java, добавьте:
    private Point longTermTarget = null;  // Долгосрочная цель (не меняется, пока не достигнута)
    private int targetPriority = 0;       // Приоритет цели (0=патруль, 1=исследование, 2=атака)

    // Характеристики врага
    public int strength = 30;
    public int agility = 50;
    // Броня (уменьшает входящий урон по формуле: damage * (100 / (100 + armor)))
    public int armor = 0;

    // Проворность (уклонение) - шанс полностью избежать попадания
    public int nimble = 0;
    public double dodgeChance = 5.0;  // 5% по умолчанию
    public int movePoints = 0;
    public int maxMovePoints = 0;
    public int moveCost = 0;
    public boolean hasMovedThisTurn = false;
    public boolean hasAttackedThisTurn = false;
    public boolean isMovingHidden = false;  // Движение вне зоны видимости

    // Память ИИ
    public int lastSeenPlayerX = -1;
    public int lastSeenPlayerY = -1;
    public int memoryTurns = 0;

    // Добавьте в класс Enemy (файл Enemy.java)
    public int lastHeardHitX = -1;
    public int lastHeardHitY = -1;
    public int hitMemoryTurns = 0;

    // ДЛЯ АНИМАЦИИ ДВИЖЕНИЯ
    public boolean isMoving = false;
    public int moveProgress = 0;
    public int moveFromX, moveFromY;
    public int moveToX, moveToY;
    public Direction moveDirection;
    public int startGridX, startGridY;
    public int targetGridX, targetGridY;

    public int burstSize = 3;        // Количество снарядов за выстрел
    public int weaponDamage = 11;    // Урон одного снаряда
    public double critChance = 0.09; // Шанс крита
    public int weaponAccuracy = 30;   // Точность орудия
    public int vision = 20;           // Зрение
    public int viewRadius = 14;       // Радиус обзора (10 + vision/5)
    public double weaponCaliber = 0.02; // Калибр оружия
    public int aimedShotCost = 9;      // Стоимость прицельного выстрела
    public int critBonus = 0;  // Добавьте в начало класса

    public int shotCost = 5;  // Стоимость одного выстрела (как у игрока для беглого огня)
    public int reloadCost = 6;  // ← ДОБАВИТЬ ЭТУ СТРОКУ

    // В классе Enemy.java, добавьте:
    private boolean isRetreating = false;      // Отступает ли враг
    private int retreatTurns = 0;              // Сколько ходов отступать
    private boolean isSnipingMode = true;      // Снайпер в режиме стрельбы (или ищет прикрытие)
    private int alliesCount = 0;               // Количество союзников врага в радиусе видимости
    private int enemiesCount = 0;              // Количество врагов (игрок+союзники) в радиусе видимости

    private boolean hasShotThisTurn = false;

    private int maxEnemiesSeenThisTurn = 0;
    private java.util.List<Point> rememberedEnemyPositions = new ArrayList<>();  // Запомненные позиции врагов

    // Система патрулирования
    private List<Point> patrolPoints = new ArrayList<>();  // Точки патрулирования
    private int currentPatrolIndex = 0;                    // Индекс текущей точки
    private boolean isPatrolling = true;                   // Режим патрулирования
    private Point explorationTarget = null;                // Цель для исследования
    private int explorationRadius = 15;                    // Радиус исследования от стартовой точки
    private Point startPosition;                           // Стартовая позиция врага


    public boolean hasShotThisTurn() { return hasShotThisTurn; }
    public void setHasShotThisTurn(boolean shot) { this.hasShotThisTurn = shot; }

    public Enemy(int x, int y, Direction dir) {
        this.gridX = x;
        this.gridY = y;
        this.direction = dir;
        this.maxHealth = 130;
        this.health = this.maxHealth;
        this.isAlive = true;
        this.faction = Faction.GERMANY;  // По умолчанию

        // Новые характеристики по умолчанию
        this.armor = 10;      // базовая броня
        this.nimble = 10;     // базовая проворность

        this.startPosition = new Point(x, y);
        generatePatrolPoints();
        generateExplorationTarget();

        calculateDodgeChance();

        calculateMovePoints();
        loadWeaponData();
    }

    public void setR_OtsuStats() {
        this.type = "R_Otsu";  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
        this.maxHealth = 150;
        this.health = 150;
        this.strength = 40;
        this.agility = 20;
        this.armor = 30;
        this.nimble = 2;
        this.critBonus = 20;
        this.vision = 20;
        this.viewRadius = 10 + vision / 5; // 10 + 4 = 14

        this.burstSize = 3;      // 13mm Autocannon Type Ho
        this.weaponDamage = 8;
        this.critChance = 0.07;
        this.weaponAccuracy = 20;
        this.weaponCaliber = 0.013;
        this.shotCost = 7;
        this.aimedShotCost = 14;
        this.reloadCost = 15;

        calculateDodgeChance();
        calculateMovePoints();
    }

    public void setM14_41Stats() {
        this.type = "M14_41";
        this.maxHealth = 190;
        this.health = 190;
        this.strength = 40;
        this.agility = 20;
        this.armor = 35;
        this.nimble = 14;
        this.critBonus = 22;
        this.vision = 20;
        this.viewRadius = 10 + vision / 5; // 10 + 4 = 14

        // Итальянское орудие Cannone da 47-32
        this.burstSize = 1;
        this.weaponDamage = 52;
        this.critChance = 0.1;
        this.weaponAccuracy = 17;
        this.weaponCaliber = 0.047;
        this.shotCost = 14;
        this.aimedShotCost = 28;
        this.reloadCost = 10;

        calculateDodgeChance();
        calculateMovePoints();
    }

    public void setH35Stats() {
        this.type = "H35";
        this.maxHealth = 210;
        this.health = 210;
        this.strength = 40;
        this.agility = 10;
        this.armor = 45;
        this.nimble = 2;
        this.critBonus = 30;
        this.vision = 30;
        this.viewRadius = 10 + vision / 5; // 10 + 6 = 16

        // 25mm орудие
        this.burstSize = 2;
        this.weaponDamage = 27;
        this.critChance = 0.11;
        this.weaponAccuracy = 45;
        this.weaponCaliber = 0.025;
        this.shotCost = 11;
        this.aimedShotCost = 22;
        this.reloadCost = 14;

        calculateDodgeChance();
        calculateMovePoints();
    }

    // Добавьте этот метод в класс Enemy (после setH35Stats)
    public void setFTStats() {
        this.type = "FT";
        this.maxHealth = 110;
        this.health = 110;
        this.strength = 35;
        this.agility = 20;
        this.armor = 10;
        this.nimble = 12;
        this.critBonus = 15;
        this.vision = 20;
        this.viewRadius = 10 + vision / 5; // 10 + 4 = 14

        // Французское 13,2 mm Hotchkiss mle. 1930
        this.burstSize = 3;
        this.weaponDamage = 8;
        this.critChance = 0.05;
        this.weaponAccuracy = 13;
        this.weaponCaliber = 0.013;
        this.shotCost = 7;
        this.aimedShotCost = 14;
        this.reloadCost = 14;

        calculateDodgeChance();
        calculateMovePoints();
    }

    public void setFiat3000Stats() {
        this.type = "Fiat3000";
        this.maxHealth = 110;
        this.health = 110;
        this.strength = 40;
        this.agility = 20;
        this.armor = 10;
        this.nimble = 11;
        this.critBonus = 23;
        this.vision = 20;
        this.viewRadius = 10 + vision / 5; // 10 + 4 = 14

        // 37mm орудие Cannone da 37-40
        this.burstSize = 1;
        this.weaponDamage = 40;
        this.critChance = 0.07;
        this.weaponAccuracy = 15;
        this.weaponCaliber = 0.037;
        this.shotCost = 10;
        this.aimedShotCost = 20;
        this.reloadCost = 18;

        calculateDodgeChance();
        calculateMovePoints();
    }

    // Методы для генерации точек
    // В Enemy.java, замените метод generatePatrolPoints:

    private void generatePatrolPoints() {
        patrolPoints.clear();

        Random rand = new Random();
        int numPoints = 3 + rand.nextInt(3); // 3-5 точек

        int maxX = 79;
        int maxY = 79;
        int attempts = 0;
        int maxAttempts = numPoints * 30; // Максимум попыток

        while (patrolPoints.size() < numPoints && attempts < maxAttempts) {
            attempts++;

            // Разные радиусы для разных точек
            int radius;
            int type = rand.nextInt(3);
            switch(type) {
                case 0: radius = 5 + rand.nextInt(8); break;   // близкие
                case 1: radius = 12 + rand.nextInt(10); break; // средние
                default: radius = 20 + rand.nextInt(15); break; // дальние
            }

            double angle = rand.nextDouble() * 2 * Math.PI;
            int px = startPosition.x + (int)(Math.cos(angle) * radius);
            int py = startPosition.y + (int)(Math.sin(angle) * radius);

            // Ограничиваем границами с отступом
            px = Math.max(3, Math.min(maxX - 3, px));
            py = Math.max(3, Math.min(maxY - 3, py));

            // Не добавляем точки слишком близко к старту
            int distFromStart = Math.abs(px - startPosition.x) + Math.abs(py - startPosition.y);
            if (distFromStart < 5) continue;

            // ===== НОВАЯ ПРОВЕРКА: НЕ В СТЕНЕ =====
            // Пока у нас нет доступа к world, пропускаем эту проверку
            // Она будет выполнена позже, при попытке движения

            // Проверяем, не дублируем ли точку
            boolean duplicate = false;
            for (Point p : patrolPoints) {
                if (Math.abs(p.x - px) < 5 && Math.abs(p.y - py) < 5) {
                    duplicate = true;
                    break;
                }
            }

            if (!duplicate) {
                patrolPoints.add(new Point(px, py));
                System.out.println("  Точка патрулирования " + patrolPoints.size() + ": [" + px + "," + py +
                        "] (радиус " + radius + ")");
            }
        }

        // Добавляем стартовую позицию
        patrolPoints.add(0, new Point(startPosition.x, startPosition.y));

        System.out.println("Враг [" + gridX + "," + gridY + "] получил " +
                patrolPoints.size() + " точек патрулирования");
    }

    // Добавьте этот метод в класс Enemy - проверяет, находится ли точка в стене
    private boolean isPointInsideWall(int x, int y, GameWorld world) {
        if (world == null) return false;
        for (Wall wall : world.getWalls()) {
            if (wall.isAlive() && wall.gridX == x && wall.gridY == y) {
                return true;
            }
        }
        return false;
    }

    // Добавьте этот метод - проверяет, достижима ли точка
    private boolean isPointReachable(int x, int y, GameWorld world) {
        if (world == null) return true;
        // Проверяем, есть ли путь от текущей позиции до цели
        List<Point> path = world.findPathForEnemy(this.gridX, this.gridY, x, y);
        return !path.isEmpty();
    }

    private void generateExplorationTarget() {
        Random rand = new Random();
        // Исследуем в радиусе 10-20 клеток от старта
        int radius = 10 + rand.nextInt(11);
        double angle = rand.nextDouble() * 2 * Math.PI;
        int ex = startPosition.x + (int)(Math.cos(angle) * radius);
        int ey = startPosition.y + (int)(Math.sin(angle) * radius);

        // Ограничиваем границами
        ex = Math.max(0, Math.min(79, ex));
        ey = Math.max(0, Math.min(79, ey));

        explorationTarget = new Point(ex, ey);
    }

    // В Enemy.java, добавьте метод:

    public Point getNextPatrolPoint() {
        if (patrolPoints.isEmpty()) {
            return startPosition;
        }

        // Пробуем найти достижимую точку
        int attempts = 0;
        while (attempts < patrolPoints.size() * 2) {
            currentPatrolIndex = (currentPatrolIndex + 1) % patrolPoints.size();
            Point target = patrolPoints.get(currentPatrolIndex);

            // Проверяем, не внутри ли стены
            if (gameWorld != null && isPointInsideWall(target.x, target.y, gameWorld)) {
                attempts++;
                continue;
            }

            // Проверяем, достижима ли точка
            if (gameWorld != null && !isPointReachable(target.x, target.y, gameWorld)) {
                attempts++;
                continue;
            }

            System.out.println("  Переход к точке патрулирования #" + currentPatrolIndex +
                    ": [" + target.x + "," + target.y + "]");
            return target;
        }

        // Если все точки недостижимы - возвращаем стартовую позицию
        System.out.println("  ⚠ Все точки патрулирования недостижимы! Возвращаюсь к старту.");
        return startPosition;
    }

    public void regenerateExplorationTarget() {
        Random rand = new Random();
        int radius = 10 + rand.nextInt(21);
        double angle = rand.nextDouble() * 2 * Math.PI;
        int ex = startPosition.x + (int)(Math.cos(angle) * radius);
        int ey = startPosition.y + (int)(Math.sin(angle) * radius);

        ex = Math.max(0, Math.min(79, ex));
        ey = Math.max(0, Math.min(79, ey));

        explorationTarget = new Point(ex, ey);
    }

    // Добавьте метод загрузки характеристик оружия:
    private void loadWeaponData() {
        try {
            String gunPath = "src/InfoAboutWeapon/2 cm Breda (I)/2 cm Breda (I).txt";
            File gunFile = new File(gunPath);
            if (gunFile.exists()) {
                Scanner scanner = new Scanner(gunFile);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Выстрелов за огонь:")) {
                        burstSize = Integer.parseInt(line.substring("Выстрелов за огонь:".length()).trim());
                    } else if (line.startsWith("Точность:")) {
                        weaponAccuracy = Integer.parseInt(line.substring("Точность:".length()).trim());
                    }
                }
                scanner.close();
            }

            // Загружаем данные снарядов
            String ammoPath = "src/PositiveHeroes/ListOfHeroes/Снаряды 2 cm Breda (I).txt";
            File ammoFile = new File(ammoPath);
            if (ammoFile.exists()) {
                Scanner scanner = new Scanner(ammoFile);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Базовый:")) {
                        String[] parts = line.substring("Базовый:".length()).trim().split(",");
                        if (parts.length >= 4) {
                            weaponDamage = Integer.parseInt(parts[1].trim());
                            critChance = Double.parseDouble(parts[2].trim());
                        }
                    }
                }
                scanner.close();
            }

            System.out.println("Враг загрузил оружие: burstSize=" + burstSize + ", damage=" + weaponDamage);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки данных оружия для врага: " + e.getMessage());
        }
    }

    // Добавьте метод для быстрого (мгновенного) перемещения
    public void teleportMove(int newGridX, int newGridY, Direction dir) {
        System.out.println("ТЕЛЕПОРТАЦИЯ: с [" + gridX + "," + gridY + "] на [" + newGridX + "," + newGridY + "]");
        this.gridX = newGridX;
        this.gridY = newGridY;
        this.direction = dir;
        this.isMoving = false;
        this.isMovingHidden = false;
        System.out.println("После телепортации: [" + gridX + "," + gridY + "]");
    }


    public void calculateMovePoints() {
        maxMovePoints = (int)(strength * 1.5);  // 30 * 1.5 = 45 очков хода
        movePoints = maxMovePoints;

        if (agility < 12) moveCost = 8;
        else if (agility <= 16) moveCost = 7;
        else if (agility <= 29) moveCost = 6;
        else if (agility <= 50) moveCost = 5;
        else if (agility <= 71) moveCost = 4;
        else if (agility <= 84) moveCost = 3;
        else if (agility <= 94) moveCost = 2;
        else moveCost = 1;
    }

    // В Enemy.java, в startTurn():
    public void startTurn() {
        movePoints = maxMovePoints;
        hasMovedThisTurn = false;
        hasAttackedThisTurn = false;
        hasShotThisTurn = false;

        // Уменьшаем память звука
        if (soundMemoryTurns > 0) {
            soundMemoryTurns--;
            if (soundMemoryTurns <= 0) {
                isInvestigatingSound = false;
            }
        }

        if (memoryTurns > 0) {
            memoryTurns--;
        }

        // ПРОСТО: уменьшаем счётчик отступления
        if (retreatTurns > 0) {
            retreatTurns--;
            if (retreatTurns <= 0) {
                isRetreating = false;
                System.out.println("Враг [" + gridX + "," + gridY + "] прекратил отступление (прошло " + (retreatTurns) + " ходов)");
                isPatrolling = true;
                noTargetTurns = 0;
            }
        }

        // Если позиция изменилась с прошлого хода, сбрасываем счётчик
        if (lastPosition != null && (lastPosition.x != gridX || lastPosition.y != gridY)) {
            stuckInPatrolCounter = 0;
        }
        lastPosition = new Point(gridX, gridY);

        resetTurnStats();  // ← ДОБАВИТЬ ЭТУ СТРОКУ
        if (turnsSinceLastAssault > 0) turnsSinceLastAssault++;
    }

    public void checkAndResetMaxEnemies(int currentVisibleEnemies) {
        // Если текущее количество видимых врагов меньше запомненного максимума
        // и прошло больше 2 ходов - сбрасываем
        if (currentVisibleEnemies < maxEnemiesSeenThisTurn && memoryTurns > 2) {
            maxEnemiesSeenThisTurn = currentVisibleEnemies;
            System.out.println("  [СБРОС] Врагов стало меньше (" + currentVisibleEnemies +
                    " < " + maxEnemiesSeenThisTurn + "), сбрасываю максимум!");
        }
    }

    // В конце хода сбрасываем счётчики
    public void resetTurnStats() {
        shotsFiredThisTurn = 0;
        hitsThisTurn = 0;
    }

    // Регистрируем попадание
    public void registerHit() {
        hitsThisTurn++;
    }

    // Регистрируем выстрел
    public void registerShot() {
        shotsFiredThisTurn++;
    }

    // Проверяем, нужно ли идти в штыковую
    public boolean shouldAssault() {
        if (shotsFiredThisTurn == 0) return false;
        double hitRate = (double) hitsThisTurn / shotsFiredThisTurn;
        return hitRate <= 0.20;  // 20% и меньше - идём в штыковую
    }

    // Сброс счётчика штыковых атак
    public void resetAssaultCooldown() {
        turnsSinceLastAssault = 0;
    }

    // Обновляем счётчик ходов
    public void incrementTurnsSinceLastAssault() {
        turnsSinceLastAssault++;
    }

    // Проверяем, можно ли штурмовать (не чаще чем раз в 2 хода)
    public boolean canAssault() {
        return turnsSinceLastAssault >= 2;
    }


    // В классе Enemy.java, добавьте метод оценки сил:
    public void evaluateSituation(java.util.List<Enemy> allEnemies, PlayerTank player, java.util.List<FriendlyUnit> friendlies, GameWorld world) {
        // Считаем союзников врага в радиусе видимости (10 клеток)
        alliesCount = 0;
        for (Enemy other : allEnemies) {
            if (other != this && other.isAlive) {
                int dx = Math.abs(this.gridX - other.gridX);
                int dy = Math.abs(this.gridY - other.gridY);
                if (dx*dx + dy*dy <= 100) { // Радиус 10 клеток
                    alliesCount++;
                }
            }
        }

        // Считаем противников (игрок + союзники) в радиусе видимости
        enemiesCount = 0;

        // Проверяем игрока
        if (player.health > 0) {
            int dx = Math.abs(this.gridX - player.gridX);
            int dy = Math.abs(this.gridY - player.gridY);
            if (dx*dx + dy*dy <= 100) {
                enemiesCount++;
            }
        }

        // Проверяем союзников игрока
        for (FriendlyUnit friendly : friendlies) {
            if (friendly.isAlive && friendly.isRecruited) {
                int dx = Math.abs(this.gridX - friendly.gridX);
                int dy = Math.abs(this.gridY - friendly.gridY);
                if (dx*dx + dy*dy <= 100) {
                    enemiesCount++;
                }
            }
        }

        System.out.println("  Ситуация: allies=" + alliesCount + ", enemies=" + enemiesCount);
    }
    public boolean canMove() {
        return movePoints >= moveCost;
    }

    public void consumeMovePoints() {
        movePoints -= moveCost;
    }

    // МЕТОД ДЛЯ НАЧАЛА АНИМИРОВАННОГО ДВИЖЕНИЯ
    // В классе Enemy.java, измените метод startAnimatedMove:

    // В классе Enemy.java, исправьте метод startAnimatedMove:

    // В Enemy.java, исправьте метод startAnimatedMove:

    // В методе startAnimatedMove в Enemy.java:

    public void startAnimatedMove(int newGridX, int newGridY, Direction dir,
                                  int cellSize, int tankSize, boolean isVisibleToPlayer) {
        if (isMoving) {
            return;
        }

        // ===== КЛЮЧЕВОЕ РЕШЕНИЕ =====
        // Анимируем движение ТОЛЬКО если враг виден игроку или союзнику
        if (!isVisibleToPlayer) {
            // Мгновенное перемещение (без анимации)
            this.gridX = newGridX;
            this.gridY = newGridY;
            this.direction = dir;
            this.isMoving = false;
            return;
        }

        // Видимый враг - плавная анимация
        int oldX = this.gridX;
        int oldY = this.gridY;

        this.gridX = newGridX;
        this.gridY = newGridY;
        this.direction = dir;

        this.isMoving = true;
        this.moveProgress = 0;
        this.moveFromX = oldX * cellSize + (cellSize - tankSize) / 2;
        this.moveFromY = oldY * cellSize + (cellSize - tankSize) / 2;
        this.moveToX = newGridX * cellSize + (cellSize - tankSize) / 2;
        this.moveToY = newGridY * cellSize + (cellSize - tankSize) / 2;
        this.moveDirection = dir;
    }

    // Исправьте метод updateAnimation:
    // В Enemy.java, в updateAnimation:
    public boolean updateAnimation(int cellSize, int tankSize) {
        if (!isMoving) return false;

        moveProgress += 25;  // было 16, больше = быстрее анимация

        if (moveProgress >= 100) {  // было 200, теперь 100 мс на шаг
            isMoving = false;
            return true;
        }
        return false;
    }

    // В Enemy.java
    public void takeDamage(int damage) {
        // Проверка на уклонение
        Random rand = new Random();
        if (rand.nextDouble() * 100 < dodgeChance) {
            System.out.println("💨 Враг уклонился от атаки!");
            return;
        }

        // Расчёт с учётом брони
        int finalDamage = (int)(damage * (100.0 / (100.0 + armor)));
        health -= finalDamage;

        System.out.println("⚔️ Враг получил урон: " + finalDamage + " (броня: " + armor + ")");

        if (health <= 0) {
            health = 0;
            isAlive = false;
            System.out.println("💀 Враг уничтожен!");
        }
    }

    // В классе Enemy.java добавьте этот метод
    public void generateAssaultPoint(int targetX, int targetY, GameWorld world) {
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        int bestX = -1, bestY = -1;
        int bestDist = Integer.MAX_VALUE;

        // Проверяем ВСЕ клетки вокруг цели (включая саму цель)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int checkX = targetX + dx;
                int checkY = targetY + dy;

                if (checkX < 0 || checkX >= 80 || checkY < 0 || checkY >= 80) continue;

                // КЛЮЧЕВОЕ: проверяем, МОЖНО ЛИ ВСТАТЬ на эту клетку
                if (world != null && !world.canMoveToForEnemy(this, checkX, checkY)) continue;

                // Проверяем, есть ли путь к этой клетке
                List<Point> path = world.findPathForEnemy(this.gridX, this.gridY, checkX, checkY);
                if (!path.isEmpty()) {
                    int dist = path.size();
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestX = checkX;
                        bestY = checkY;
                    }
                }
            }
        }

        if (bestX != -1) {
            assaultPoint = new Point(bestX, bestY);
            isAssaultMode = true;
            System.out.println("  🎯 ТОЧКА ШТУРМА: [" + bestX + "," + bestY +
                    "] (рядом с целью [" + targetX + "," + targetY + "])");
        } else {
            // НЕТ ДОСТУПНЫХ КЛЕТОК! Сбрасываем штурмовой режим
            assaultPoint = null;
            isAssaultMode = false;
            System.out.println("  ❌ НЕТ ДОСТУПНЫХ КЛЕТОК ДЛЯ ШТУРМА! Штурм отменён!");
        }
    }

    public boolean isMoving() {
        return isMoving;
    }

    public int getTargetGridX() {
        return targetGridX;
    }

    public int getTargetGridY() {
        return targetGridY;
    }

    // ПОЛУЧЕНИЕ ТЕКУЩЕЙ ПОЗИЦИИ ДЛЯ ОТРИСОВКИ (с учётом анимации)
    public int getAnimatedX(int cellSize, int tankSize) {
        if (!isMoving) {
            return gridX * cellSize + (cellSize - tankSize) / 2;
        }
        float t = (float)moveProgress / 200f;
        return (int)(moveFromX + (moveToX - moveFromX) * t);
    }

    public int getAnimatedY(int cellSize, int tankSize) {
        if (!isMoving) {
            return gridY * cellSize + (cellSize - tankSize) / 2;
        }
        float t = (float)moveProgress / 200f;
        return (int)(moveFromY + (moveToY - moveFromY) * t);
    }

    public Rectangle getScreenRect(int cameraX, int cameraY, int cellSize, int tankSize) {
        int x = getAnimatedX(cellSize, tankSize) - cameraX;
        int y = getAnimatedY(cellSize, tankSize) - cameraY;
        return new Rectangle(x, y, tankSize, tankSize);
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public void calculateDodgeChance() {
        // Уклонение = nimble * 0.15% (как у игрока), но максимум 15% для врагов
        dodgeChance = Math.min(20, nimble * 0.15);
    }

    // В классе Enemy.java, после поля direction, добавьте:
    public enum BehaviorType {
        AGGRESSIVE,   // Агрессивный - сразу атакует
        TACTICAL,     // Тактический - отступает при численном превосходстве
        SNIPER        // Снайпер - стреляет издалека, ищет прикрытие
    }

    private BehaviorType behaviorType = BehaviorType.TACTICAL; // По умолчанию агрессивный

    // Геттер и сеттер
    public BehaviorType getBehaviorType() { return behaviorType; }
    public void setBehaviorType(BehaviorType type) { this.behaviorType = type; }

    public boolean isRetreating() { return isRetreating; }
    public void setRetreating(boolean retreating) { isRetreating = retreating; }

    public int getRetreatTurns() { return retreatTurns; }
    public void setRetreatTurns(int turns) { retreatTurns = turns; }

    public boolean isSnipingMode() { return isSnipingMode; }
    public void setSnipingMode(boolean mode) { isSnipingMode = mode; }

    public int getAlliesCount() { return alliesCount; }
    public void setAlliesCount(int count) { alliesCount = count; }

    public int getEnemiesCount() { return enemiesCount; }
    public void setEnemiesCount(int count) { enemiesCount = count; }

    // В классе Enemy.java добавьте:
    private java.util.List<Point> lastSeenEnemyPositions = new ArrayList<>();

    public java.util.List<Point> getLastSeenEnemyPositions() { return lastSeenEnemyPositions; }
    public void setLastSeenEnemyPositions(java.util.List<Point> positions) {
        this.lastSeenEnemyPositions = positions;
    }


    // Геттеры и сеттеры
    public List<Point> getPatrolPoints() { return patrolPoints; }
    public void setPatrolPoints(List<Point> points) { this.patrolPoints = points; }
    public int getCurrentPatrolIndex() { return currentPatrolIndex; }
    public void setCurrentPatrolIndex(int index) { this.currentPatrolIndex = index; }
    public boolean isPatrolling() { return isPatrolling; }
    public void setPatrolling(boolean patrolling) { this.isPatrolling = patrolling; }
    public Point getExplorationTarget() { return explorationTarget; }
    public void setExplorationTarget(Point target) { this.explorationTarget = target; }
    public Point getStartPosition() { return startPosition; }
    public void setStartPosition(Point pos) { this.startPosition = pos; }

    // Геттеры и сеттеры
    public int getMaxEnemiesSeenThisTurn() { return maxEnemiesSeenThisTurn; }
    public void setMaxEnemiesSeenThisTurn(int count) { this.maxEnemiesSeenThisTurn = count; }
    public void updateMaxEnemiesSeen(int currentCount) {
        if (currentCount > maxEnemiesSeenThisTurn) {
            maxEnemiesSeenThisTurn = currentCount;
        }
    }
    public void resetMaxEnemiesSeenThisTurn() {
        maxEnemiesSeenThisTurn = 0;
        rememberedEnemyPositions.clear();
    }
    public void addRememberedEnemyPosition(int x, int y) {
        Point pos = new Point(x, y);
        if (!rememberedEnemyPositions.contains(pos)) {
            rememberedEnemyPositions.add(pos);
        }
    }
    public List<Point> getRememberedEnemyPositions() { return rememberedEnemyPositions; }

    // Геттеры/сеттеры
    public Point getCurrentMovementTarget() { return currentMovementTarget; }
    public void setCurrentMovementTarget(Point target) { this.currentMovementTarget = target; }
    public int getStuckCounter() { return stuckCounter; }
    public void setStuckCounter(int count) { this.stuckCounter = count; }
    public void incrementStuckCounter() { stuckCounter++; }
    public void resetStuckCounter() { stuckCounter = 0; }

    // Геттеры и сеттеры
    public Point getLongTermTarget() { return longTermTarget; }
    public void setLongTermTarget(Point target) { this.longTermTarget = target; }
    public int getTargetPriority() { return targetPriority; }
    public void setTargetPriority(int priority) { this.targetPriority = priority; }
    public void clearLongTermTarget() {
        this.targetPriority = 0;
    }
    public Point getLastPosition() { return lastPosition; }
    public void setLastPosition(Point pos) { this.lastPosition = pos; }


    public List<Point> getCachedPath() { return cachedPath; }
    public void setCachedPath(List<Point> path) { this.cachedPath = path; this.cachedPathIndex = 0; }
    public Point getNextCachedStep() {
        if (cachedPath != null && cachedPathIndex < cachedPath.size()) {
            return cachedPath.get(cachedPathIndex++);
        }
        return null;
    }
    public void clearCachedPath() { cachedPath = null; cachedPathIndex = 0; }

    // Геттеры/сеттеры
    public Point getLastHeardSoundPosition() { return lastHeardSoundPosition; }
    public void setLastHeardSoundPosition(Point pos) { this.lastHeardSoundPosition = pos; }
    public int getSoundMemoryTurns() { return soundMemoryTurns; }
    public void setSoundMemoryTurns(int turns) { this.soundMemoryTurns = turns; }
    public boolean isInvestigatingSound() { return isInvestigatingSound; }
    public void setInvestigatingSound(boolean investigating) { this.isInvestigatingSound = investigating; }
    public int getNoTargetTurns() { return noTargetTurns; }
    public void resetNoTargetTurns() { noTargetTurns = 0; }
    public void incrementNoTargetTurns() { noTargetTurns++; }


    // И геттеры/сеттеры
    public Point getAssaultPoint() { return assaultPoint; }
    public void setAssaultPoint(Point point) { this.assaultPoint = point; }
    public boolean isAssaultMode() { return isAssaultMode; }
    public void setAssaultMode(boolean mode) { isAssaultMode = mode; }
    public void clearAssaultPoint() { assaultPoint = null; isAssaultMode = false; }

    public Faction getFaction() { return faction; }
    public void setFaction(Faction faction) { this.faction = faction; }

    // В конструкторе или отдельном методе:
    public void setType(String type) {
        this.type = type;
        this.faction = Faction.fromEnemyType(type);
        if ("R_Otsu".equals(type)) {
            setR_OtsuStats();
        } else if ("M14_41".equals(type)) {
            setM14_41Stats();
        } else if ("H35".equals(type)) {
            setH35Stats();
        } else if ("FT".equals(type)) {  // ← ДОБАВИТЬ ЭТУ СТРОКУ
            setFTStats();                 // ← И ЭТУ
        } else if ("Fiat3000".equals(type)) {
            setFiat3000Stats();
        }
    }

    public int getBaseExperience() {
        if ("R_Otsu".equals(this.type)) {
            return 45;
        } else if ("M14_41".equals(this.type)) {
            return 70;
        } else if ("H35".equals(this.type)) {
            return 82;
        } else if ("FT".equals(this.type)) {  // ← ДОБАВИТЬ
            return 26;                         // ← ДОБАВИТЬ
        } else if ("Fiat3000".equals(this.type)) {  // ← ДОБАВИТЬ
            return 28;                         // ← ДОБАВИТЬ
        }
        return 32;
    }

    public void setGameWorld(GameWorld world) {
        this.gameWorld = world;
    }
}