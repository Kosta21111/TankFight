package entities;

import inventory.*;
import inventory.Caliber;
import audio.*;
import combat.*;  // ← ДОБАВИТЬ
import java.util.Random;
import java.io.Serializable;  // ← ДОБАВЬТЕ ЭТУ СТРОКУ

public class PlayerTank {

    private int silver = 0;  // Общее серебро команды

    public int gridX, gridY;
    public int health = 130;
    public int maxHealth = 130;
    public int strength = 36;
    public int agility = 50;
    public int movePoints = 0;
    public int moveCost = 0;
    public int maxMovePoints = 0;

    public double maxCarryWeight = 0;  // Максимальный вес в кг
    public boolean isOnHill = false;
    public int currentHillBonus = 0;
    public boolean turnEnded = false;

    public int breadDebuffTurns = 0;
    public int breadDebuffRemainingTurns = 0;
    public boolean poisonedSoundPlayed = false;

    // ===== НОВЫЕ ПОЛЯ ДЛЯ ОРУЖИЯ =====
    private String equippedWeaponId;      // "breda", "45mm", "8mm"
    private WeaponData equippedWeaponData;
    private java.util.Map<String, WeaponData> availableWeapons = new java.util.HashMap<>();
    private java.util.Map<String, AmmoState> weaponAmmoStates = new java.util.HashMap<>();

    // Характеристики оружия (будут обновляться при смене)
    public int burstSize = 3;
    public int weaponDamage = 11;
    public double critChance = 0.09;
    public int weaponAccuracy = 30;
    public int baseAccuracy = 30;
    public int shotCost = 5;
    public int aimedShotCost = 11;
    public double weaponCaliber = 0.02;
    public int reloadCost = 6;

    private ExperienceSystem experienceSystem;

    private transient SoundManager soundManager;
    private Inventory inventory;
    private Caliber requiredCaliber = Caliber.CALIBER_20MM;

    // В PlayerTank.java добавьте новые поля:

    // Броня
    public int armor = 10;

    // Критический шанс (бонус в процентах)
    public int critBonus = 0;

    // Зрение
    public int vision = 20;
    public int viewRadius = 14;  // 10 базовых + 20/5 = 14

    // Бонус перезарядки (в процентах)
    public int reloadBonus = 0;
    public int originalReloadCost = 6;

    // Проворность (уклонение)
    public int nimble = 32;
    public double dodgeChance = 9.8;  // 5 + 32*0.15 = 9.8

    private int parts = 0;

    public int upgradeLevel = 1;          // 1..4
    public String upgradeClass = null;    // null для 1 уровня, затем "PT", "TT", "ST", "LT"

    public int weaponWeight = 0;  // Вес оружия

    // Геттер для originalReloadCost
    public int getOriginalReloadCost() { return originalReloadCost; }

    // Вложенные классы
    public static class WeaponData implements java.io.Serializable {
        public String name;
        public String weaponId;  // ← НОВОЕ ПОЛЕ: "breda", "45mm", "47mm_french", "47mm_italian" и т.д.
        public Caliber caliber;
        public int burstSize;
        public int weaponAccuracy;
        public double weaponCaliber;
        public int shotCost;
        public int aimedShotCost;
        public int reloadCost;
        public int weaponDamage;
        public double critChance;
        public String iconPath;
        public int requiredStrength;
        public int weight;

        public WeaponData(String name, String weaponId, Caliber caliber, int burstSize, int weaponAccuracy,
                          double weaponCaliber, int shotCost, int aimedShotCost, int reloadCost,
                          int weaponDamage, double critChance, String iconPath, int requiredStrength, int weight) {
            this.name = name;
            this.weaponId = weaponId;  // ← СОХРАНЯЕМ ID
            this.caliber = caliber;
            this.burstSize = burstSize;
            this.weaponAccuracy = weaponAccuracy;
            this.weaponCaliber = weaponCaliber;
            this.shotCost = shotCost;
            this.aimedShotCost = aimedShotCost;
            this.reloadCost = reloadCost;
            this.weaponDamage = weaponDamage;
            this.critChance = critChance;
            this.iconPath = iconPath;
            this.requiredStrength = requiredStrength;
            this.weight = weight;
        }
    }

    public static class AmmoState implements java.io.Serializable {
        public int currentAmmo;
        public int maxAmmo;
        public AmmoState(int currentAmmo, int maxAmmo) {
            this.currentAmmo = currentAmmo;
            this.maxAmmo = maxAmmo;
        }
    }

    public PlayerTank(int startX, int startY) {
        this.gridX = startX;
        this.gridY = startY;
        this.inventory = new Inventory();
        this.inventory.addStarterItems();

        // Инициализируем систему опыта
        this.experienceSystem = new ExperienceSystem("Leichttraktor");

        // Инициализируем стартовое оружие
        initDefaultWeapon();

        // ===== НОВЫЕ ХАРАКТЕРИСТИКИ =====
        this.armor = 10;
        this.critBonus = 0;
        this.vision = 20;
        this.viewRadius = 14;
        this.reloadBonus = 0;
        this.originalReloadCost = 6;
        this.nimble = 32;
        this.dodgeChance = 9.8;

        // Добавьте в начало класса, после других полей

// В конструкторе PlayerTank, после инициализации характеристик:
        this.maxCarryWeight = this.strength * 0.5;  // Половина от силы
        System.out.println("📦 Грузоподъёмность Leichttraktor: " + maxCarryWeight);

        calculateMovePoints();  // ← без параметров
        movePoints = maxMovePoints;
    }

    // Метод для проверки, можно ли добавить предмет
    // В PlayerTank.java
    public boolean canAddItem(Item item) {
        double currentWeight = inventory.getTotalWeight();
        double newWeight = currentWeight + item.getTotalWeight();
        return newWeight <= maxCarryWeight;
    }

    private void initDefaultWeapon() {
        WeaponData breda = new WeaponData(
                "2 cm Breda (I)", "breda", Caliber.CALIBER_20MM,
                3, 30, 0.02, 5, 11, 6, 11, 0.09,
                "src/ObjectsOfInventory/Weapon/2 cm Breda (I).png",
                15, 4  // ← ДОБАВИТЬ
        );
        availableWeapons.put("breda", breda);
        setEquippedWeapon("breda", breda);
    }

    public double getEquipmentWeight() {
        double weight = 0.0;
        // Вес экипированного оружия
        if (equippedWeaponData != null) {
            weight += equippedWeaponData.weight;
        }
        return weight;
    }

    public void setEquippedWeapon(String weaponId, WeaponData weaponData) {
        System.out.println("=== PlayerTank.setEquippedWeapon() ===");
        System.out.println("  weaponId: " + weaponId);
        System.out.println("  weaponData: " + (weaponData != null ? weaponData.name : "null"));

        // Сохраняем состояние текущего оружия ПЕРЕД сменой
        if (this.equippedWeaponId != null && this.equippedWeaponData != null && inventory != null) {
            AmmoState currentState = new AmmoState(inventory.getCurrentAmmoCount(), inventory.getMaxAmmo());
            weaponAmmoStates.put(this.equippedWeaponId, currentState);
            System.out.println("  Сохранено состояние для " + this.equippedWeaponId +
                    ": " + currentState.currentAmmo + "/" + currentState.maxAmmo);
        }

        this.equippedWeaponId = weaponId;
        this.equippedWeaponData = weaponData;

        if (weaponData != null) {
            // Обновляем характеристики оружия
            this.requiredCaliber = weaponData.caliber;
            this.burstSize = weaponData.burstSize;
            this.weaponAccuracy = weaponData.weaponAccuracy + this.baseAccuracy;
            this.weaponCaliber = weaponData.weaponCaliber;
            this.shotCost = weaponData.shotCost;
            this.aimedShotCost = weaponData.aimedShotCost;
            this.weaponWeight = weaponData.weight;

            // ===== ИСПРАВЛЕНИЕ: обновляем originalReloadCost =====
            this.originalReloadCost = weaponData.reloadCost;

            // Пересчитываем reloadCost с учётом бонуса перезарядки
            updateReloadCostFromBonus();

            this.weaponDamage = weaponData.weaponDamage;
            this.critChance = weaponData.critChance;

            // Определяем правильный maxAmmo для калибра
            int newMaxAmmo;
            if (weaponData.caliber == Caliber.CALIBER_20MM) {
                newMaxAmmo = 12;
            } else if (weaponData.caliber == Caliber.CALIBER_25MM) {
                newMaxAmmo = 10;
            } else if (weaponData.caliber == Caliber.CALIBER_13MM) {
                newMaxAmmo = 15;
            } else if (weaponData.caliber == Caliber.CALIBER_45MM) {
                newMaxAmmo = 7;
            } else if (weaponData.caliber == Caliber.CALIBER_47MM) {
                newMaxAmmo = 5;
            } else if (weaponData.caliber == Caliber.CALIBER_8MM) {
                newMaxAmmo = 40;
            } else if (weaponData.caliber == Caliber.CALIBER_30MM) {
                newMaxAmmo = 12;
            } else if (weaponData.caliber == Caliber.CALIBER_37MM) {
                newMaxAmmo = 5;
            } else if (weaponData.caliber == Caliber.CALIBER_76MM) {
                newMaxAmmo = 3;
            } else if (weaponData.caliber == Caliber.CALIBER_105MM) {
                newMaxAmmo = 3;
            } else if (weaponData.caliber == Caliber.CALIBER_128MM) {
                newMaxAmmo = 3;
            } else if (weaponData.caliber == Caliber.CALIBER_203MM) {
                newMaxAmmo = 1;
            }else {
                newMaxAmmo = 1;
            }

            // Восстанавливаем состояние оружия или создаём новое
            AmmoState savedState = weaponAmmoStates.get(weaponId);
            if (savedState != null && savedState.maxAmmo == newMaxAmmo) {
                if (inventory != null) {
                    inventory.restoreAmmo(savedState.currentAmmo, newMaxAmmo, reloadCost, burstSize);
                    System.out.println("  Восстановлено состояние для " + weaponId +
                            ": " + savedState.currentAmmo + "/" + newMaxAmmo);
                }
            } else {
                if (inventory != null) {
                    inventory.setPlayerWeapon(weaponData, newMaxAmmo);
                    System.out.println("  Новое оружие: заряжено " + newMaxAmmo + "/" + newMaxAmmo);
                }
            }

            calculateMovePoints();  // ← без параметров

            // ===== ПРОСТО: итоговая точность = базовая + точность оружия =====
            //this.weaponAccuracy = this.baseAccuracy + weaponData.weaponAccuracy;
            System.out.println("  Новая точность: " + this.weaponAccuracy +
                    " (база=" + this.baseAccuracy + " + оружие=" + weaponData.weaponAccuracy + ")");
        }


    }

    public void restoreEquippedWeapon(String weaponId, int currentAmmo, int maxAmmo,
                                      int reloadCost, int burstSize) {
        WeaponData weapon;

        // Проверяем, не является ли это "god" оружием
        if ("god".equals(weaponId)) {
            weapon = new WeaponData(
                    "💀 GOD KILLER 💀", "god", Caliber.CALIBER_203MM,
                    10, 999, 1.0, 1, 1, 0, 9999, 1.0,
                    "src/ObjectsOfInventory/Weapon/2 cm Breda (I).png", 0, 0
            );
        } else {
            weapon = createPlayerWeaponFromId(weaponId);
            if (weapon == null) {
                System.err.println("❌ Не удалось восстановить оружие с ID: " + weaponId);
                // Fallback на Breda
                weapon = createPlayerWeaponFromId("breda");
                if (weapon == null) return;
            }
        }

        // Сохраняем состояние в кэш
        AmmoState state = new AmmoState(currentAmmo, maxAmmo);
        weaponAmmoStates.put(weaponId, state);

        // Устанавливаем оружие
        this.equippedWeaponId = weaponId;
        this.equippedWeaponData = weapon;

        // Обновляем все характеристики
        this.requiredCaliber = weapon.caliber;
        this.burstSize = weapon.burstSize;
        this.weaponWeight = weapon.weight;
        this.weaponCaliber = weapon.weaponCaliber;
        this.shotCost = weapon.shotCost;
        this.aimedShotCost = weapon.aimedShotCost;
        this.originalReloadCost = weapon.reloadCost;
        this.weaponDamage = weapon.weaponDamage;
        this.critChance = weapon.critChance;

        // Пересчитываем точность
        this.weaponAccuracy = this.baseAccuracy + weapon.weaponAccuracy;

        // Обновляем перезарядку
        updateReloadCostFromBonus();

        // Восстанавливаем боезапас
        this.getInventory().restoreAmmo(currentAmmo, maxAmmo, reloadCost, burstSize);

        // ===== ВАЖНО: устанавливаем тип снарядов =====
        AmmoItem ammoType = new AmmoItem(weapon.caliber, 0);
        this.getInventory().setCurrentAmmoType(ammoType);

        System.out.println("🔫 Восстановлено оружие: " + weapon.name +
                " (ID: " + weapon.weaponId +
                ", калибр: " + weapon.caliber.name() +
                ", вес: " + weapon.weight +
                ", снаряды: " + currentAmmo + "/" + maxAmmo + ")");
    }

    // В PlayerTank.java
    public void updateReloadCostFromBonus() {
        // reloadBonus хранится в процентах (0-100)
        // Максимальное уменьшение стоимости перезарядки - 50% (не 33%)
        double reductionPercent = Math.min(50, reloadBonus * 0.5);
        int reduction = (int)(originalReloadCost * reductionPercent / 100.0);
        reloadCost = originalReloadCost - reduction;
        if (reloadCost < 1) reloadCost = 1;

        System.out.println("🔄 Обновлена стоимость перезарядки: исходная=" + originalReloadCost +
                ", бонус=" + reloadBonus + "%, уменьшение=" + String.format("%.1f", reductionPercent) +
                "%, новая стоимость=" + reloadCost);
    }

    public void updateCurrentWeaponAmmo() {
        if (equippedWeaponId != null && inventory != null) {
            AmmoState state = new AmmoState(inventory.getCurrentAmmoCount(), inventory.getMaxAmmo());
            weaponAmmoStates.put(equippedWeaponId, state);
        }
    }

    // В PlayerTank.java добавьте этот метод
    public void takeDamage(int damage) {
        // Проверка на уклонение
        Random rand = new Random();
        if (rand.nextDouble() * 100 < dodgeChance) {
            System.out.println("💨 Уклонение! Leichttraktor избежал урона!");
            return;
        }

        // Расчёт с учётом брони
        int finalDamage = (int)(damage * (100.0 / (100.0 + armor)));
        health -= finalDamage;

        if (health <= 0) {
            health = 0;
            System.out.println("💀 Leichttraktor уничтожен!");
        }

        System.out.println("⚔️ Leichttraktor получил урон: " + finalDamage + " (было: " + (damage + health) + ", броня: " + armor + ")");
    }

    public boolean isPoisoned() {
        return breadDebuffRemainingTurns > 0;
    }

    public void setSoundManager(SoundManager sm) {
        this.soundManager = sm;
    }

    public Inventory getInventory() { return inventory; }

    // Лечение
    public boolean heal(int amount) {
        if (health >= maxHealth) return false;
        health = Math.min(maxHealth, health + amount);
        return true;
    }

    // Ремонт
    public boolean repair(int amount) {
        return true;
    }

    // Удалите метод calculateMovePoints(int weaponWeight) и оставьте только этот:

    public void calculateMovePoints() {
        // Базовая формула: сила * 1.5
        int basePoints = (int)(strength * 1.5);

        // Вычитаем вес оружия (1 вес = -1 ОХ)
        int pointsAfterWeapon = Math.max(1, basePoints - weaponWeight);

        // Вычитаем вес инвентаря (1 кг = -1 ОХ)
        double inventoryWeight = inventory.getTotalWeight();
        int totalWeightPoints = pointsAfterWeapon - (int)inventoryWeight;

        // Минимальное значение - 1 ОХ
        maxMovePoints = Math.max(1, totalWeightPoints);

        moveCost = calculateMoveCost(agility);

        System.out.println("📊 Расчёт ОХ: сила=" + strength +
                " -> база=" + basePoints +
                ", вес оружия=" + weaponWeight +
                ", вес инвентаря=" + inventoryWeight +
                ", итого ОХ=" + maxMovePoints);
    }

// Удалите дублирующийся метод calculateMovePoints(int weaponWeight)

    private int calculateMoveCost(int agility) {
        if (agility < 12) return 8;
        else if (agility <= 16) return 7;
        else if (agility <= 29) return 6;
        else if (agility <= 50) return 5;
        else if (agility <= 71) return 4;
        else if (agility <= 84) return 3;
        else if (agility <= 94) return 2;
        else return 1;
    }

    public boolean canMove() {
        if (turnEnded) return false;
        if (isOverweight()) {
            System.out.println("⚠ ПЕРЕГРУЗ! Невозможно двигаться! Вес: " +
                    String.format("%.1f", inventory.getTotalWeight()) + "/" +
                    String.format("%.1f", maxCarryWeight));
            return false;
        }
        return movePoints >= moveCost;
    }

    public void consumeMovePoints() {
        if (!turnEnded) {
            movePoints -= moveCost;
        }
    }

    public void endTurn() {
        turnEnded = true;
    }

    public void startNewTurn() {
        turnEnded = false;
        // Пересчитываем очки хода с учётом текущего веса
        calculateMovePoints();
        movePoints = maxMovePoints;
        poisonedSoundPlayed = false;
        System.out.println("=== НАЧАЛО ХОДА ИГРОКА ===");
        System.out.println("Очки хода восстановлены: " + movePoints + "/" + maxMovePoints);
        System.out.println("  🔫 ОРУЖИЕ: " + (equippedWeaponData != null ?
                equippedWeaponData.name : "нет оружия") + " (калибр " + weaponCaliber + " м)");
        System.out.println("  ⚖️ Вес инвентаря: " + inventory.getTotalWeight() + "/" + maxCarryWeight);
    }

    // В PlayerTank.java
    public int getReequipPenalty() {
        int fullPenalty = maxMovePoints;

        // Бонус перезарядки снижает штраф (каждый 1% бонуса = -0.5% штрафа, макс 50%)
        double reductionPercent = Math.min(50, reloadBonus * 0.5);
        int reducedPenalty = (int)(fullPenalty * (1.0 - reductionPercent / 100.0));

        System.out.println("⚙️ Штраф за переэкипировку: " + fullPenalty +
                " -> " + reducedPenalty + " (бонус перезарядки " + reloadBonus + "% даёт -" + reductionPercent + "%)");

        return Math.max(1, reducedPenalty);
    }

    public void resetMovePoints() {
        movePoints = maxMovePoints;
    }

    // Геттеры для оружия
    public WeaponData getEquippedWeaponData() { return equippedWeaponData; }
    public String getEquippedWeaponId() { return equippedWeaponId; }
    public Caliber getRequiredCaliber() { return requiredCaliber; }
    public double getWeaponCaliber() { return weaponCaliber; }
    public int getReloadCost() { return reloadCost; }
    public int getBurstSize() { return burstSize; }
    public int getWeaponDamage() { return weaponDamage; }
    public double getCritChance() { return critChance; }
    public int getStrength() { return strength; }
    public int getAgility() { return agility; }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    // Геттеры и сеттеры для серебра
    public int getSilver() { return silver; }
    public void setSilver(int silver) { this.silver = Math.max(0, silver); }
    public void addSilver(int amount) {
        this.silver += amount;
        System.out.println("💰 Добавлено " + amount + " серебра. Всего: " + silver);
    }
    public boolean removeSilver(int amount) {
        if (silver >= amount) {
            silver -= amount;
            System.out.println("💰 Потрачено " + amount + " серебра. Осталось: " + silver);
            return true;
        }
        System.out.println("💰 Недостаточно серебра! Нужно: " + amount + ", есть: " + silver);
        return false;
    }

    // Добавьте геттеры и методы:
    public ExperienceSystem getExperienceSystem() { return experienceSystem; }
    public void addExperience(int amount) {
        if (experienceSystem != null) {
            int oldLevel = experienceSystem.getLevel();
            experienceSystem.addExperience(amount);
            if (experienceSystem.getLevel() > oldLevel) {
                updateStatsFromExperience();
            }
        }
    }

    // Добавьте метод для получения старого уровня
    public int getExperienceLevel() {
        return experienceSystem != null ? experienceSystem.getLevel() : 1;
    }

    public void setExperienceSystem(ExperienceSystem exp) {
        this.experienceSystem = exp;
        updateStatsFromExperience();
    }

    public void updateStatsFromExperience() {

    }

    // Геттеры и сеттеры
    public int getParts() { return parts; }
    public void setParts(int parts) { this.parts = Math.max(0, parts); }
    public void addParts(int amount) {
        this.parts += amount;
        System.out.println("⚙️ Добавлено " + amount + " деталей. Всего: " + parts);
    }
    public boolean removeParts(int amount) {
        if (parts >= amount) {
            parts -= amount;
            System.out.println("⚙️ Потрачено " + amount + " деталей. Осталось: " + parts);
            return true;
        }
        System.out.println("⚙️ Недостаточно деталей! Нужно: " + amount + ", есть: " + parts);
        return false;
    }

    // Геттеры и сеттеры
    public int getUpgradeLevel() { return upgradeLevel; }
    public String getUpgradeClass() { return upgradeClass; }
    public void setUpgradeClass(String cls) {
        this.upgradeClass = cls;
        // НЕ ВЫЗЫВАЕМ applyUpgradeBonuses() здесь
    }
    public void setUpgradeLevel(int level) {
        this.upgradeLevel = level;
        // НЕ ВЫЗЫВАЕМ applyUpgradeBonuses() здесь - только при активной модернизации
    }

    // Стоимость улучшения до следующего уровня
    public int getUpgradeCost() {
        switch(upgradeLevel) {
            case 1: return 190;
            case 2: return 470;
            case 3: return 1150;
            default: return 0;
        }
    }

    // Проверка, можно ли улучшить (достаточно деталей и не максимальный уровень)
    public boolean canUpgrade(int teamParts) {
        return upgradeLevel < 4 && teamParts >= getUpgradeCost();
    }

    // Применить улучшение (выбор класса только при переходе с 1 на 2)
    public void performUpgrade(String chosenClass, int teamParts) {
        if (!canUpgrade(teamParts)) return;

        int cost = getUpgradeCost();
        // Списываем детали (это делает вызывающий код, но добавим для уверенности)
        // teamParts уже передаётся, но детали списываются снаружи

        if (upgradeLevel == 1 && chosenClass != null) {
            upgradeClass = chosenClass;
        }

        int oldLevel = upgradeLevel;
        upgradeLevel++;

        // ===== ВАЖНО: вызываем НОВЫЙ метод! =====
        applyUpgradeBonusesForNewLevel(oldLevel, upgradeLevel);
    }

    // Новый метод - применяет бонусы только за переход с oldLevel на newLevel
    // В PlayerTank.java, исправленный метод applyUpgradeBonusesForNewLevel:
// Новый метод - применяет бонусы только за переход с oldLevel на newLevel
    // Новый метод - применяет бонусы ОДИН РАЗ за новый уровень (без сброса старых)
    // Новый метод - применяет бонусы ОДИН РАЗ за новый уровень (без сброса старых)
    public void applyUpgradeBonusesForNewLevel(int oldLevel, int newLevel) {
        if (oldLevel >= newLevel) return;

        // ===== ВАЖНО: НЕ СБРАСЫВАЕМ СТАРЫЕ ХАРАКТЕРИСТИКИ! =====
        // Просто добавляем бонусы к текущим значениям

        // Применяем бонусы ОДИН РАЗ
        applyBonusesForSingleLevel();

        // Пересчитываем производные
        this.maxCarryWeight = this.strength * 0.5;
        calculateMovePoints();
        updateReloadCostFromBonus();

        System.out.println("🎉 Применены бонусы за переход на " + newLevel + " уровень!");
    }

    // В PlayerTank.java, после других методов

    public int getMaxStrengthForClass() {
        if (upgradeClass == null) return 999;
        switch (upgradeClass) {
            case "PT": return 160;
            case "TT": return 160;
            case "ST": return 100;
            case "LT": return 70;
            default: return 999;
        }
    }

    public int getMaxHealthForClass() {
        if (upgradeClass == null) return 999;
        switch (upgradeClass) {
            case "PT": return 1000;
            case "TT": return 2200;
            case "ST": return 1500;
            case "LT": return 700;
            default: return 999;
        }
    }

    public int getMaxAgilityForClass() {
        if (upgradeClass == null) return 999;
        switch (upgradeClass) {
            case "PT": return 50;
            case "TT": return 20;
            case "ST": return 80;
            case "LT": return 100;
            default: return 999;
        }
    }

    public int getMaxArmorForClass() {
        if (upgradeClass == null) return 999;
        switch (upgradeClass) {
            case "PT": return 160;
            case "TT": return 160;
            case "ST": return 70;
            case "LT": return 20;
            default: return 999;
        }
    }

    public int getMaxNimbleForClass() {
        if (upgradeClass == null) return 999;
        switch (upgradeClass) {
            case "PT": return 40;
            case "TT": return 20;
            case "ST": return 80;
            case "LT": return 100;
            default: return 999;
        }
    }

    public int getMaxReloadForClass() {
        if (upgradeClass == null) return 999;
        switch (upgradeClass) {
            case "PT": return 100;
            case "TT": return 50;
            case "ST": return 100;
            case "LT": return 20;
            default: return 999;
        }
    }
    // В PlayerTank.java, новый метод:
    private void applyBonusesForSingleLevel() {
        if (upgradeClass == null) return;

        switch(upgradeClass) {
            case "PT":
                // ===== ИСПРАВЛЕНИЕ: увеличиваем baseAccuracy, а не weaponAccuracy =====
                baseAccuracy += 10;  // ← было weaponAccuracy += 10
                reloadBonus += 20;
                maxHealth += 200;
                health += 200;
                agility -= 10;
                nimble -= 10;



                // ===== ОГРАНИЧЕНИЯ =====
                if (agility > 50) agility = 50;
                if (maxHealth > 1000) maxHealth = 1000;
                if (health > 1000) health = 1000;
                if (nimble > 40) nimble = 40;

                // Пересчитываем производные
                maxCarryWeight = strength * 0.5;
                dodgeChance = Math.min(20, 5 + nimble * 15 / 100);
                updateReloadCostFromBonus();

                // Пересчитываем итоговую точность
                if (equippedWeaponData != null) {
                    weaponAccuracy = baseAccuracy + equippedWeaponData.weaponAccuracy;
                } else {
                    weaponAccuracy = baseAccuracy;
                }

                break;

            case "TT":
                // ===== НОВЫЕ БОНУСЫ ДЛЯ ТТ =====
                // +10 сила, +20 броня, -20 ловкость, -10 проворность, +350 прочность
                strength += 10;
                armor += 20;
                agility -= 20;
                nimble -= 10;
                maxHealth += 350;
                health += 350;

                // ===== ОГРАНИЧЕНИЯ =====
                if (agility > 20) agility = 20;
                if (nimble > 20) nimble = 20;
                if (reloadBonus > 50) reloadBonus = 50;  // ← перезарядка не выше 50

                // Пересчитываем производные
                maxCarryWeight = strength * 0.5;
                dodgeChance = Math.min(20, 5 + nimble * 15 / 100);
                updateReloadCostFromBonus();
                break;

            case "ST":
                // СТ: +5 сила, +5 броня, +250 HP, +10 перезарядка
                strength += 5;
                armor += 5;
                maxHealth += 250;
                health += 250;
                reloadBonus += 10;

                // Ограничения
                if (strength > 100) strength = 100;
                if (armor > 70) armor = 70;
                if (maxHealth > 1500) maxHealth = 1500;
                if (health > 1500) health = 1500;

                maxCarryWeight = strength * 0.5;
                updateReloadCostFromBonus();
                break;

            case "LT":
                // ЛТ: +150 HP, +10 vision, +10 nimble, +10 agility, -5 strength
                maxHealth += 150;
                health += 150;
                vision += 10;
                nimble += 10;
                agility += 10;
                strength -= 5;

                // Ограничения
                if (maxHealth > 700) maxHealth = 700;
                if (health > 700) health = 700;
                if (strength < 1) strength = 1;
                if (strength > 70) strength = 70;
                if (armor > 20) armor = 20;
                break;
        }
    }

    // Применить бонусы за уровень модернизации


    private int getBaseMaxHealth() {
        return 130;  // Базовая для Leichttraktor
    }

    private int getBaseStrength() {
        return 36;
    }

    private int getBaseAgility() {
        return 50;
    }

    private int getBaseAccuracy() {
        return 30;  // Базовая точность оружия
    }

    private int getBaseArmor() {
        return 10;
    }

    private int getBaseVision() {
        return 20;
    }

    private int getBaseNimble() {
        return 32;
    }

    private void resetToBaseStats() {
        this.maxHealth = getBaseMaxHealth();
        this.health = Math.min(health, maxHealth);
        this.strength = getBaseStrength();
        this.agility = getBaseAgility();
        this.weaponAccuracy = getBaseAccuracy();
        this.armor = getBaseArmor();
        this.critBonus = 0;
        this.vision = getBaseVision();
        this.viewRadius = 10 + this.vision / 5;
        this.reloadBonus = 0;
        updateReloadCostFromBonus();
        this.nimble = getBaseNimble();
        this.dodgeChance = Math.min(20, 5 + this.nimble * 15 / 100);
        this.maxCarryWeight = this.strength * 0.5;
        calculateMovePoints();
    }

    public String getTextureFolder() {
        if (upgradeLevel == 1) {
            return "Leichttraktor";
        }
        // Формируем название папки: "Leichttraktor (Модернизация КЛАСС-УРОВЕНЬ)"
        String className = "";
        switch (upgradeClass) {
            case "PT": className = "ПТ"; break;
            case "TT": className = "ТТ"; break;
            case "ST": className = "СТ"; break;
            case "LT": className = "ЛТ"; break;
            default: className = "ТТ";
        }
        return "Leichttraktor (Модернизация " + className + "-" + upgradeLevel + ")";
    }

    public String getPortraitPath() {
        if (upgradeLevel == 1) {
            return "src/PositiveHeroes/ImageOfHeroes/Leichttraktor.png";
        }

        // Формируем название класса для портрета
        String className = "";
        switch (upgradeClass) {
            case "PT": className = "ПТ"; break;
            case "TT": className = "ТТ"; break;
            case "ST": className = "СТ"; break;
            case "LT": className = "ЛТ"; break;
            default: className = "ТТ";
        }

        return "src/PositiveHeroes/ImageOfHeroes/Leichttraktor (Модернизация " + className + "-" + upgradeLevel + ").png";
    }

    public void applyBonusWithoutConsuming(LevelUpBonus.BonusType bonus) {
        switch (bonus) {
            case HEALTH:
                this.maxHealth += 10;
                this.health += 10;
                break;
            case ACCURACY:
                this.weaponAccuracy += 1;
                break;
            case STRENGTH:
                this.strength += 1;
                this.maxCarryWeight = this.strength * 0.5;
                calculateMovePoints();
                break;
            case AGILITY:
                this.agility += 1;
                calculateMovePoints();
                break;
            case ARMOR:
                this.armor += 1;
                break;
            case CRITICAL:
                this.critBonus += 1;
                break;
            case VISION:
                this.vision += 1;
                this.viewRadius = 10 + this.vision / 5;
                break;
            case RELOAD:
                if (this.reloadBonus < 33) {
                    this.reloadBonus += 1;
                    updateReloadCostFromBonus();
                }
                break;
            case NIMBLE:
                this.nimble += 1;
                this.dodgeChance = Math.min(20, 5 + this.nimble * 15 / 100);
                break;
        }
    }

    // В PlayerTank.java добавьте вложенный класс:

    public static class PlayerSaveData implements Serializable {
        private static final long serialVersionUID = 2L;  // ← УВЕЛИЧЬТЕ VERSION

        // Существующие поля
        public int health;
        public int maxHealth;
        public int strength;
        public int agility;
        public int armor;
        public int critBonus;
        public int vision;
        public int viewRadius;
        public double maxCarryWeight;
        public int reloadBonus;
        public int nimble;
        public double dodgeChance;
        public int silver;
        public int parts;
        public int upgradeLevel;
        public String upgradeClass;
        public int experienceLevel;
        public int experiencePoints;
        public int experienceForNextLevel;
        public String equippedWeaponId;

        // Состояние оружия
        public int currentAmmo;
        public int maxAmmo;
        public int burstSize;
        public int weaponAccuracy;
        public double weaponCaliber;
        public int shotCost;
        public int aimedShotCost;
        public int reloadCost;
        public int weaponDamage;
        public double critChance;

        // ===== ДОБАВЬТЕ НЕДОСТАЮЩИЕ ПОЛЯ =====
        public int baseAccuracy;  // ← ДОБАВИТЬ
        public int weaponWeight;   // ← ДОБАВИТЬ
        public int originalReloadCost;  // ← ДОБАВИТЬ
    }

    public PlayerSaveData saveData() {
        PlayerSaveData data = new PlayerSaveData();
        data.health = this.health;
        data.maxHealth = this.maxHealth;
        data.strength = this.strength;
        data.agility = this.agility;
        data.armor = this.armor;
        data.critBonus = this.critBonus;
        data.vision = this.vision;
        data.viewRadius = this.viewRadius;
        data.reloadBonus = this.reloadBonus;
        data.nimble = this.nimble;
        data.dodgeChance = this.dodgeChance;
        data.silver = this.silver;
        data.parts = this.parts;
        data.maxCarryWeight = this.maxCarryWeight;
        data.upgradeLevel = this.upgradeLevel;
        data.upgradeClass = this.upgradeClass;

        // ===== ДОБАВЬТЕ ЭТИ ПОЛЯ =====
        data.baseAccuracy = this.baseAccuracy;
        data.weaponWeight = this.weaponWeight;
        data.originalReloadCost = this.originalReloadCost;

        if (this.experienceSystem != null) {
            data.experienceLevel = this.experienceSystem.getLevel();
            data.experiencePoints = this.experienceSystem.getExperience();
            data.experienceForNextLevel = this.experienceSystem.getExperienceForNextLevel();
        }

        data.equippedWeaponId = this.equippedWeaponId;
        data.currentAmmo = this.getInventory().getCurrentAmmoCount();
        data.maxAmmo = this.getInventory().getMaxAmmo();
        data.burstSize = this.burstSize;
        data.weaponAccuracy = this.weaponAccuracy;
        data.weaponCaliber = this.weaponCaliber;
        data.shotCost = this.shotCost;
        data.aimedShotCost = this.aimedShotCost;
        data.reloadCost = this.reloadCost;
        data.weaponDamage = this.weaponDamage;
        data.critChance = this.critChance;

        return data;
    }

    public void restoreData(PlayerSaveData data) {
        // Восстанавливаем базовые характеристики
        this.health = data.health;
        this.maxHealth = data.maxHealth;
        this.strength = data.strength;
        this.agility = data.agility;
        this.armor = data.armor;
        this.critBonus = data.critBonus;
        this.vision = data.vision;
        this.viewRadius = data.viewRadius;
        this.reloadBonus = data.reloadBonus;
        this.nimble = data.nimble;
        this.dodgeChance = data.dodgeChance;
        this.silver = data.silver;
        this.parts = data.parts;
        this.maxCarryWeight = data.maxCarryWeight;
        this.upgradeLevel = data.upgradeLevel;
        this.upgradeClass = data.upgradeClass;

        // ===== ВОССТАНАВЛИВАЕМ ПОЛЯ ОРУЖИЯ =====
        this.baseAccuracy = data.baseAccuracy;
        this.weaponWeight = data.weaponWeight;
        this.originalReloadCost = data.originalReloadCost;

        // ===== ВОССТАНАВЛИВАЕМ ХАРАКТЕРИСТИКИ ОРУЖИЯ ИЗ DATA (ВАЖНО!) =====
        this.burstSize = data.burstSize;
        this.weaponAccuracy = data.weaponAccuracy;
        this.weaponCaliber = data.weaponCaliber;
        this.shotCost = data.shotCost;
        this.aimedShotCost = data.aimedShotCost;
        this.reloadCost = data.reloadCost;
        this.weaponDamage = data.weaponDamage;
        this.critChance = data.critChance;

        // ===== ВОССТАНАВЛИВАЕМ КАЛИБР ИЗ DATA! =====
        // Нам нужно определить калибр из сохранённых данных
        Caliber savedCaliber = getCaliberFromWeaponId(data.equippedWeaponId);
        if (savedCaliber != null) {
            this.requiredCaliber = savedCaliber;
            System.out.println("🔫 Восстановлен калибр из сохранения: " + savedCaliber.name());
        } else {
            // Fallback
            this.requiredCaliber = Caliber.CALIBER_20MM;
        }

        // Пересчитываем производные
        this.maxCarryWeight = this.strength * 0.5;
        this.moveCost = calculateMoveCost(this.agility);
        this.maxMovePoints = (int)(this.strength * 1.5) - this.weaponWeight - (int)getInventory().getTotalWeight();
        if (this.maxMovePoints < 1) this.maxMovePoints = 1;
        if (this.movePoints > this.maxMovePoints) this.movePoints = this.maxMovePoints;

        // Восстанавливаем опыт
        ExperienceSystem expSys = new ExperienceSystem("Leichttraktor");
        if (data.experienceLevel > 1 || data.experiencePoints > 0) {
            for (int i = 1; i < data.experienceLevel; i++) {
                expSys.addExperience(expSys.getExperienceForNextLevel());
            }
            if (data.experiencePoints > 0) {
                expSys.addExperience(data.experiencePoints);
            }
            expSys.setExperienceForNextLevel(data.experienceForNextLevel);
        }
        this.experienceSystem = expSys;

        // ===== ВОССТАНАВЛИВАЕМ ЭКИПИРОВАННОЕ ОРУЖИЕ (только для ссылки) =====
        // Восстанавливаем оружие
        if (data.equippedWeaponId != null && !data.equippedWeaponId.isEmpty()) {
            restoreEquippedWeapon(data.equippedWeaponId, data.currentAmmo, data.maxAmmo,
                    data.reloadCost, data.burstSize);
        } else {
            this.equippedWeaponId = null;
            this.equippedWeaponData = null;
        }

        // ===== ВАЖНО: НЕ ПЕРЕСЧИТЫВАЕМ weaponAccuracy ИЗ EQUIPPED WEAPON! =====
        // Мы уже восстановили weaponAccuracy из data, оставляем как есть

        System.out.println("📊 Итог загрузки:");
        System.out.println("  Оружие: " + (this.equippedWeaponData != null ? this.equippedWeaponData.name : "нет"));
        System.out.println("  Калибр: " + this.requiredCaliber.name());
        System.out.println("  Точность: " + this.weaponAccuracy);
        System.out.println("  Урон: " + this.weaponDamage);
        System.out.println("  Стоимость выстрела: " + this.shotCost);
    }



    private Caliber getCaliberFromWeaponId(String weaponId) {
        if (weaponId == null) return null;
        switch (weaponId) {
            case "breda": return Caliber.CALIBER_20MM;
            case "25mm": return Caliber.CALIBER_25MM;
            case "45mm": return Caliber.CALIBER_45MM;
            case "47mm_french": return Caliber.CALIBER_47MM;
            case "47mm_italian": return Caliber.CALIBER_47MM;
            case "8mm": return Caliber.CALIBER_8MM;
            case "13mm_japan": return Caliber.CALIBER_13MM;
            case "13mm_french": return Caliber.CALIBER_13MM;
            case "30mm": return Caliber.CALIBER_30MM;
            case "37mm_italian": return Caliber.CALIBER_37MM;
            case "37mm_american": return Caliber.CALIBER_37MM;
            case "37mm_sweden": return Caliber.CALIBER_37MM;
            case "76mm": return Caliber.CALIBER_76MM;
            case "105mm": return Caliber.CALIBER_105MM;
            case "128mm": return Caliber.CALIBER_128MM;
            case "203mm": return Caliber.CALIBER_203MM;
            default: return null;
        }
    }

    public WeaponData createPlayerWeaponFromId(String weaponId) {
        switch (weaponId) {
            case "breda":
                return new WeaponData("2 cm Breda (I)", "breda", Caliber.CALIBER_20MM,
                        3, 30, 0.02, 5, 11, 6, 11, 0.09,
                        "src/ObjectsOfInventory/Weapon/2 cm Breda (I).png", 15, 4);

            case "25mm":
                return new WeaponData("25mm Canon Raccourci mle. 1934", "25mm", Caliber.CALIBER_25MM,
                        2, 45, 0.025, 4, 9, 14, 27, 0.11,
                        "src/ObjectsOfInventory/Weapon/25-mm-Canon-Raccourci-mle.-1934.png", 15, 4);

            case "45mm":
                return new WeaponData("45 мм обр. 1932 г.", "45mm", Caliber.CALIBER_45MM,
                        1, 15, 0.045, 11, 22, 10, 47, 0.12,
                        "src/ObjectsOfInventory/Weapon/45 мм обр. 1932 г.png", 19, 5);

            case "47mm_french":
                return new WeaponData("47 mm SA35", "47mm_french", Caliber.CALIBER_47MM,
                        1, 21, 0.047, 9, 18, 10, 55, 0.09,
                        "src/ObjectsOfInventory/Weapon/47 mm SA35.png", 17, 5);

            case "47mm_italian":
                return new WeaponData("Cannone da 47-32", "47mm_italian", Caliber.CALIBER_47MM,
                        1, 17, 0.047, 9, 18, 10, 52, 0.1,
                        "src/ObjectsOfInventory/Weapon/Cannone da 47-32.png", 16, 5);

            case "8mm":
                return new WeaponData("7,92 mm Mauser E.W. 141", "8mm", Caliber.CALIBER_8MM,
                        8, 25, 0.008, 7, 14, 22, 8, 0.06,
                        "src/ObjectsOfInventory/Weapon/7,92 mm Mauser E.W. 141.png", 12, 3);

            case "13mm_japan":
                return new WeaponData("13 mm Autocannon Type Ho", "13mm_japan", Caliber.CALIBER_13MM,
                        3, 17, 0.013, 4, 9, 12, 8, 0.07,
                        "src/ObjectsOfInventory/Weapon/13 mm Autocannon Type Ho.png", 12, 3);
            case "13mm_french":
                return new WeaponData("13,2 mm Hotchkiss mle. 1930", "13mm_french", Caliber.CALIBER_13MM,
                        3, 13, 0.013, 4, 9, 9, 8, 0.05,
                        "src/ObjectsOfInventory/Weapon/13,2 mm Hotchkiss mle. 1930.png", 10, 3);

            case "30mm":
                return new WeaponData("3 cm M.K. 103A", "30mm", Caliber.CALIBER_30MM,
                        3, 15, 0.03, 5, 11, 38, 30, 0.09,
                        "src/ObjectsOfInventory/Weapon/3 cm M.K. 103A.png", 45, 9);

            case "37mm_italian":
                return new WeaponData("Cannone da 37-40", "37mm_italian", Caliber.CALIBER_37MM,
                        1, 15, 0.037, 7, 14, 12, 40, 0.07,
                        "src/ObjectsOfInventory/Weapon/Cannone da 37-40.png", 13, 3);

            case "37mm_american":
                return new WeaponData("37 mm Semiautomatic Gun M1924", "37mm_american", Caliber.CALIBER_37MM,
                        5, 10, 0.037, 9, 18, 25, 30, 0.07,
                        "src/ObjectsOfInventory/Weapon/Cannone da 37-40.png", 20, 3);

            case "37mm_sweden":
                return new WeaponData("37 mm kan m-38-49 strv", "37mm_sweden", Caliber.CALIBER_37MM,
                        1, 50, 0.037, 8, 16, 12, 40, 0.05,
                        "src/ObjectsOfInventory/Weapon/37 mm kan m-38-49 strv.png", 24, 5);

            case "76mm":
                return new WeaponData("76 мм Л-10С", "76mm", Caliber.CALIBER_76MM,
                        1, 5, 0.076, 18, 36, 16, 110, 0.1,
                        "src/ObjectsOfInventory/Weapon/76 мм Л-10С.png", 43, 12);

            case "105mm":
                return new WeaponData("10,5 cm StuH 42 L28", "105mm", Caliber.CALIBER_105MM,
                        1, 20, 0.105, 24, 48, 26, 350, 0.12,
                        "src/ObjectsOfInventory/Weapon/10,5 cm StuH 42 L28.png", 60, 14);

            case "128mm":
                return new WeaponData("12,8 cm Kw.K. L50", "128mm", Caliber.CALIBER_128MM,
                        1, 30, 0.128, 30, 60, 36, 440, 0.15,
                        "src/ObjectsOfInventory/Weapon/12,8 cm Kw.K. L50.png", 80, 19);

            case "203mm":
                return new WeaponData("8-inch Howitzer M47", "203mm", Caliber.CALIBER_203MM,
                        1, 20, 0.203, 32, 64, 75, 1200, 0.7,
                        "src/ObjectsOfInventory/Weapon/8-inch Howitzer M47.png", 90, 26);

            default:
                return null;
        }
    }

    public boolean isOverweight() {
        double inventoryWeight = inventory.getTotalWeight();
        double equipmentWeight = getEquipmentWeight();  // вес экипированного оружия
        double currentWeight = inventoryWeight + equipmentWeight;

        //System.out.println("DEBUG: inventoryWeight=" + inventoryWeight +
        //        ", equipmentWeight=" + equipmentWeight +
        //        ", total=" + currentWeight +
        //        ", maxCarryWeight=" + maxCarryWeight);

        return currentWeight > maxCarryWeight;
    }

    public int getCurrentAmmoCount() {
        AmmoItem ammo = inventory.getCurrentAmmo();
        return ammo != null ? ammo.getCount() : 0;
    }

    public AmmoItem getCurrentAmmo() {
        return inventory.getCurrentAmmo();
    }
}