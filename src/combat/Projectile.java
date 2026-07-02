package combat;

import entities.*;
import world.GameWorld;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Projectile {

    private Enemy shooterEnemy;  // Если не null - стрелял враг
    private Enemy killedEnemy;  // ← добавить
    private double x, y;           // Текущие координаты в пикселях
    private double targetX, targetY;
    private double startX, startY;
    private double progress = 0;    // 0 = старт, 1 = финиш
    private double speed = 0.02;    // Скорость полёта (чем больше, тем быстрее)

    private boolean isPlayerShot;   // true = игрок стрелял, false = враг
    private Enemy targetEnemy;      // Цель (если стрелял игрок)
    private PlayerTank targetPlayer; // Цель (если стрелял враг)
    private boolean hit = false;     // Попал ли снаряд
    private int damage;              // Урон снаряда
    private Random random = new Random();

    private double deviationAngle;   // Угол отклонения при промахе
    private double missTargetX, missTargetY; // Куда летит при промахе

    // Добавьте поле для хранения информации о попадании в стену:
    private boolean hitWall = false;
    private int wallHitX, wallHitY;

    // Добавьте ссылку на GameWorld для проверки стен
    private GameWorld gameWorld;
    private FriendlyUnit targetFriendly;

    private FriendlyUnit shooterFriendly;  // Если null - стрелял игрок

    public Projectile(double startX, double startY, Enemy targetEnemy, int damage, double hitChance, GameWorld gameWorld) {
        this.startX = startX;
        this.startY = startY;
        this.targetEnemy = targetEnemy;
        this.isPlayerShot = true;
        this.damage = damage;
        this.gameWorld = gameWorld;
        this.killedEnemy = targetEnemy;

        // Вычисляем центр цели
        this.targetX = targetEnemy.gridX * 50 + 20;
        this.targetY = targetEnemy.gridY * 50 + 20;

        // Определяем, попадём или нет
        this.hit = random.nextDouble() < hitChance;
        this.shooterFriendly = null;  // Стрелял игрок

        if (!this.hit) {
            // При промахе вычисляем случайное отклонение
            double angleToTarget = Math.atan2(targetY - startY, targetX - startX);
            // Отклонение в пределах ±30 градусов (0.52 радиана)
            deviationAngle = angleToTarget + (random.nextDouble() - 0.5) * Math.PI / 3;
            double distance = Math.hypot(targetX - startX, targetY - startY);
            // Летим примерно на ту же дистанцию, но в другом направлении
            missTargetX = startX + Math.cos(deviationAngle) * distance * (0.8 + random.nextDouble() * 0.4);
            missTargetY = startY + Math.sin(deviationAngle) * distance * (0.8 + random.nextDouble() * 0.4);
        }
    }

    // В Projectile.java, найдите конструктор для союзника и исправьте:

    public Projectile(double startX, double startY, Enemy targetEnemy, int damage, double hitChance,
                      GameWorld gameWorld, FriendlyUnit shooter) {
        this.startX = startX;
        this.startY = startY;
        this.targetEnemy = targetEnemy;      // ← цель-враг
        this.isPlayerShot = false;
        this.damage = damage;
        this.gameWorld = gameWorld;
        this.shooterFriendly = shooter;  // ← УЖЕ ЕСТЬ, ЭТО ПРАВИЛЬНО

        // НО НУЖНО ТАКЖЕ УСТАНОВИТЬ targetEnemy И ДРУГИЕ ПОЛЯ!
        this.targetX = targetEnemy.gridX * 50 + 20;
        this.targetY = targetEnemy.gridY * 50 + 20;

        this.hit = random.nextDouble() < hitChance;

        if (!this.hit) {
            double angleToTarget = Math.atan2(targetY - startY, targetX - startX);
            deviationAngle = angleToTarget + (random.nextDouble() - 0.5) * Math.PI / 3;
            double distance = Math.hypot(targetX - startX, targetY - startY);
            missTargetX = startX + Math.cos(deviationAngle) * distance * (0.8 + random.nextDouble() * 0.4);
            missTargetY = startY + Math.sin(deviationAngle) * distance * (0.8 + random.nextDouble() * 0.4);
        }
    }

    public Projectile(double startX, double startY, FriendlyUnit targetFriendly, int damage, double hitChance, GameWorld gameWorld) {
        this.startX = startX;
        this.startY = startY;
        this.targetFriendly = targetFriendly;
        this.isPlayerShot = false;
        this.damage = damage;
        this.gameWorld = gameWorld;
        this.shooterFriendly = null;

        this.targetX = targetFriendly.gridX * 50 + 20;
        this.targetY = targetFriendly.gridY * 50 + 20;

        this.hit = random.nextDouble() < hitChance;

        if (!this.hit) {
            double angleToTarget = Math.atan2(targetY - startY, targetX - startX);
            deviationAngle = angleToTarget + (random.nextDouble() - 0.5) * Math.PI / 3;
            double distance = Math.hypot(targetX - startX, targetY - startY);
            missTargetX = startX + Math.cos(deviationAngle) * distance * (0.8 + random.nextDouble() * 0.4);
            missTargetY = startY + Math.sin(deviationAngle) * distance * (0.8 + random.nextDouble() * 0.4);
        }
    }

    // Добавьте этот конструктор в Projectile.java (после существующих)
    public Projectile(double startX, double startY, PlayerTank targetPlayer, int damage, double hitChance, GameWorld gameWorld) {
        this(startX, startY, targetPlayer, damage, hitChance, gameWorld, null);
    }

    // Конструктор для врага, стреляющего в игрока
    public Projectile(double startX, double startY, PlayerTank targetPlayer, int damage, double hitChance,
                      GameWorld gameWorld, Enemy shooter) {
        this.startX = startX;
        this.startY = startY;
        this.targetPlayer = targetPlayer;
        this.isPlayerShot = false;
        this.damage = damage;
        this.gameWorld = gameWorld;
        this.shooterEnemy = shooter;  // ← запоминаем, кто стрелял (враг)
        this.shooterFriendly = null;

        this.targetX = targetPlayer.gridX * 50 + 20;
        this.targetY = targetPlayer.gridY * 50 + 20;

        this.hit = random.nextDouble() < hitChance;

        if (!this.hit) {
            double angleToTarget = Math.atan2(targetY - startY, targetX - startX);
            deviationAngle = angleToTarget + (random.nextDouble() - 0.5) * Math.PI / 3;
            double distance = Math.hypot(targetX - startX, targetY - startY);
            missTargetX = startX + Math.cos(deviationAngle) * distance * (0.8 + random.nextDouble() * 0.4);
            missTargetY = startY + Math.sin(deviationAngle) * distance * (0.8 + random.nextDouble() * 0.4);
        }
    }

    // Конструктор для врага, стреляющего в другого врага (межфракционные бои)
    public Projectile(double startX, double startY, Enemy targetEnemy, int damage, double hitChance,
                      GameWorld gameWorld, Enemy shooter) {
        this.startX = startX;
        this.startY = startY;
        this.targetEnemy = targetEnemy;
        this.isPlayerShot = false;
        this.damage = damage;
        this.gameWorld = gameWorld;
        this.shooterEnemy = shooter;  // ← запоминаем, кто стрелял (враг)
        this.shooterFriendly = null;

        this.targetX = targetEnemy.gridX * 50 + 20;
        this.targetY = targetEnemy.gridY * 50 + 20;

        this.hit = random.nextDouble() < hitChance;

        if (!this.hit) {
            double angleToTarget = Math.atan2(targetY - startY, targetX - startX);
            deviationAngle = angleToTarget + (random.nextDouble() - 0.5) * Math.PI / 3;
            double distance = Math.hypot(targetX - startX, targetY - startY);
            missTargetX = startX + Math.cos(deviationAngle) * distance * (0.8 + random.nextDouble() * 0.4);
            missTargetY = startY + Math.sin(deviationAngle) * distance * (0.8 + random.nextDouble() * 0.4);
        }
    }

    public Enemy applyDamageAndGetKilledEnemy() {
        if (hitWall) {
            if (gameWorld != null && gameWorld.isTreeAt(wallHitX, wallHitY)) {
                System.out.println("Снаряд попал в дерево! [" + wallHitX + "," + wallHitY + "]");
                gameWorld.damageTree(wallHitX, wallHitY, damage);
                return null;
            }
            System.out.println("Снаряд попал в стену! [" + wallHitX + "," + wallHitY + "]");
            if (gameWorld != null) {
                gameWorld.damageWall(wallHitX, wallHitY, damage);
            }
            return null;
        }

        if (hit) {
            // ===== ВРАГ СТРЕЛЯЕТ В ДРУГОГО ВРАГА (межфракционная атака) =====
            if (!isPlayerShot && targetEnemy != null && shooterEnemy != null && targetEnemy.isAlive) {
                boolean wasAlive = targetEnemy.isAlive;
                targetEnemy.takeDamage(damage);
                if (shooterEnemy != null) {
                    shooterEnemy.registerHit();
                }
                if (!targetEnemy.isAlive && wasAlive) {
                    return targetEnemy;  // ← ВОЗВРАЩАЕМ УБИТОГО ВРАГА
                }
            }
            // ===== СОЮЗНИК СТРЕЛЯЕТ ВО ВРАГА =====
            else if (!isPlayerShot && targetEnemy != null && shooterFriendly != null && targetEnemy.isAlive) {
                boolean wasAlive = targetEnemy.isAlive;
                targetEnemy.takeDamage(damage);
                if (!targetEnemy.isAlive && wasAlive) {
                    return targetEnemy;
                }
            }
            // Стрельба игрока во врага
            else if (isPlayerShot && targetEnemy != null && targetEnemy.isAlive) {
                boolean wasAlive = targetEnemy.isAlive;
                targetEnemy.takeDamage(damage);
                if (!targetEnemy.isAlive && wasAlive) {
                    return targetEnemy;
                }
            }
            // Враг стреляет в игрока
            else if (!isPlayerShot && targetPlayer != null && targetPlayer.health > 0) {
                targetPlayer.takeDamage(damage);
                if (shooterEnemy != null) {
                    shooterEnemy.registerHit();
                }
                return null;
            }
            // Враг стреляет в союзника
            else if (!isPlayerShot && targetFriendly != null && targetFriendly.isAlive) {
                targetFriendly.takeDamage(damage, gameWorld);
                return null;
            }
        } else {
            System.out.println("Промах!");
        }
        return null;
    }

    public Enemy getShooterEnemy() {
        return shooterEnemy;
    }

    public void setWallHit(int wallX, int wallY) {
        this.hitWall = true;
        this.wallHitX = wallX;
        this.wallHitY = wallY;
    }

    // НОВЫЙ МЕТОД: Проверка столкновения со стенами
    private boolean checkWallCollision(double currentX, double currentY, double prevX, double prevY) {
        if (gameWorld == null) return false;

        int gridX = (int)(currentX / GameWorld.CELL_SIZE);
        int gridY = (int)(currentY / GameWorld.CELL_SIZE);
        int prevGridX = (int)(prevX / GameWorld.CELL_SIZE);
        int prevGridY = (int)(prevY / GameWorld.CELL_SIZE);

        // ВАЖНО: Игнорируем клетку старта и клетку цели
        int startGridX = (int)(startX / GameWorld.CELL_SIZE);
        int startGridY = (int)(startY / GameWorld.CELL_SIZE);
        int targetGridX = (int)(targetX / GameWorld.CELL_SIZE);
        int targetGridY = (int)(targetY / GameWorld.CELL_SIZE);

        // Пропускаем стартовую и целевую клетки
        if ((gridX == startGridX && gridY == startGridY) ||
                (gridX == targetGridX && gridY == targetGridY)) {
            return false;
        }

        // Проверяем текущую клетку
        if (gameWorld.isWallAt(gridX, gridY)) {
            setWallHit(gridX, gridY);
            return true;
        }

        // Проверяем предыдущую клетку (если разные)
        if ((gridX != prevGridX || gridY != prevGridY) && gameWorld.isWallAt(prevGridX, prevGridY)) {
            // Пропускаем стартовую и целевую для предыдущей клетки
            if (!((prevGridX == startGridX && prevGridY == startGridY) ||
                    (prevGridX == targetGridX && prevGridY == targetGridY))) {
                setWallHit(prevGridX, prevGridY);
                return true;
            }
        }

        return false;
    }

    public boolean update() {
        double prevX = x;
        double prevY = y;

        progress += speed;

        if (progress >= 1.0) {
            return true; // Снаряд долетел
        }

        // Интерполяция позиции
        if (hit) {
            x = startX + (targetX - startX) * progress;
            y = startY + (targetY - startY) * progress;
        } else {
            x = startX + (missTargetX - startX) * progress;
            y = startY + (missTargetY - startY) * progress;
        }

        // ПРОВЕРКА НА СТОЛКНОВЕНИЕ СО СТЕНАМИ (только если не в целевой клетке)
        double remainingDistance = Math.hypot(targetX - x, targetY - y);
        if (remainingDistance > 10) { // Если ещё не близко к цели
            if (checkWallCollision(x, y, prevX, prevY)) {
                return true;
            }

            if (checkTreeCollision(x, y, prevX, prevY)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkTreeCollision(double currentX, double currentY, double prevX, double prevY) {
        if (gameWorld == null) return false;

        int gridX = (int)(currentX / 50);
        int gridY = (int)(currentY / 50);

        if (gameWorld.isTreeAt(gridX, gridY)) {
            setTreeHit(gridX, gridY);
            return true;
        }
        return false;
    }

    private void setTreeHit(int gridX, int gridY) {
        this.hitWall = true;  // переиспользуем флаг
        this.wallHitX = gridX;
        this.wallHitY = gridY;
    }

    // Обновите applyDamage:
    // В файле Projectile.java, измените метод applyDamage:

    public boolean applyDamage() {
        if (hitWall) {
            if (gameWorld != null && gameWorld.isTreeAt(wallHitX, wallHitY)) {
                System.out.println("Снаряд попал в дерево! [" + wallHitX + "," + wallHitY + "]");
                gameWorld.damageTree(wallHitX, wallHitY, damage);
                return false;
            }
            System.out.println("Снаряд попал в стену! [" + wallHitX + "," + wallHitY + "]");
            if (gameWorld != null) {
                gameWorld.damageWall(wallHitX, wallHitY, damage);
            }
            return false;
        }

        if (hit) {
            if (isPlayerShot && targetEnemy != null && targetEnemy.isAlive) {
                boolean wasAlive = targetEnemy.isAlive;
                targetEnemy.takeDamage(damage);
                boolean isDead = !targetEnemy.isAlive && wasAlive;
                System.out.println("Попадание по врагу! Урон: " + damage);
                return isDead;
            } else if (!isPlayerShot && targetPlayer != null && targetPlayer.health > 0) {
                targetPlayer.health -= damage;
                System.out.println("Попадание по игроку! Урон: " + damage);
                return false;
            } else if (!isPlayerShot && targetFriendly != null && targetFriendly.isAlive) {
                targetFriendly.takeDamage(damage, gameWorld);
                System.out.println("Попадание по союзнику " + targetFriendly.name + "! Урон: " + damage);
                return false;
            }
        } else {
            System.out.println("Промах!");
        }
        return false;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY) {
        int screenX = (int)x - cameraX;
        int screenY = (int)y - cameraY;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ===== ИСПРАВЛЯЕМ ЦВЕТА =====
        if (isPlayerShot) {
            // Игрок (Leichttraktor) - золотистый
            g2d.setColor(new Color(255, 200, 50, 200));
        } else {
            // Союзники - сине-голубой (вместо красного)
            g2d.setColor(new Color(50, 150, 255, 200));
        }

        int size = 6;
        g2d.fillOval(screenX - size/2, screenY - size/2, size, size);

        // Свечение
        if (isPlayerShot) {
            g2d.setColor(new Color(255, 200, 50, 100));
        } else {
            g2d.setColor(new Color(50, 150, 255, 100));
        }
        g2d.fillOval(screenX - size, screenY - size, size * 2, size * 2);

        g2d.dispose();
    }

    public boolean isHit() { return hit; }
    public boolean isPlayerShot() { return isPlayerShot; }
    public boolean isHitWall() { return hitWall; }

    // ===== НОВЫЙ ГЕТТЕР: кто стрелял =====
    public FriendlyUnit getShooterFriendly() { return shooterFriendly; }
}