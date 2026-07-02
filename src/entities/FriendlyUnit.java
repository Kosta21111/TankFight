package entities;

import ui.GamePanel;
import inventory.*;
import inventory.Caliber;
import combat.*;
import audio.SoundManager;  // ← ДОБАВИТЬ ИМПОРТ
import world.GameWorld;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.sound.sampled.*;
import java.io.File;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import java.io.Serializable;


public class FriendlyUnit {
    public int gridX, gridY;
    public String name;
    public String type;
    public boolean isAlive = true;
    public boolean isRecruited = false;
    public boolean isUnavailable = false;  // Нельзя нанять (после выполнения квеста)
    public boolean poisonedSoundPlayed = false;  // Флаг, что звук отравления уже проигран в этом ходу

    public int baseAccuracy = 40;  // Базовая точность (без учёта оружия)

    // Характеристики
    public int health = 100;
    public int maxHealth = 100;
    public int strength = 30;
    public int agility = 40;
    public int accuracy = 40;
    public int baseWeaponAccuracy = 0;
    public int weaponWeight = 0;

    public double maxCarryWeight = 0;  // Максимальный вес в кг

    // Добавьте поля для отслеживания дебаффа от хлеба
    public int breadDebuffTurns = 0;
    public int breadDebuffRemainingTurns = 0;
    //public int originalMaxMovePoints = 0;

    // ← НОВЫЕ ПОЛЯ ДЛЯ УПРАВЛЕНИЯ
    public int movePoints = 0;
    public int maxMovePoints = 0;
    public int moveCost = 0;
    public boolean turnEnded = false;
    public boolean hasMovedThisTurn = false;
    public boolean hasAttackedThisTurn = false;

    // Направление и изображения
    public Direction currentDirection = Direction.RIGHT;

    protected transient ExperienceSystem experienceSystem;

    // Оружие
    public int burstSize = 3;
    public int weaponDamage = 11;
    public double critChance = 0.09;
    public int weaponAccuracy = 30;
    public int shotCost = 5;
    public int aimedShotCost = 11;     // Стоимость прицельного огня
    public double weaponCaliber = 0.02; // Калибр орудия (для звука)
    public int reloadCost = 6;          // ← ДОБАВЬТЕ ЭТУ СТРОКУ (стоимость перезарядки)

    // Для анимации движения
    public boolean isMoving = false;
    public int moveProgress = 0;
    public int moveFromX, moveFromY;
    public int moveToX, moveToY;
    public Direction moveDirection;
    private Map<String, AmmoState> weaponAmmoStates = new HashMap<>();

    // Диалог при встрече
    public String greetingText;
    public String recruitedText;

    public Clip greetingSound;
    private transient SoundManager soundManager;  // ← ДОБАВИТЬ ПОЛЕ

    protected Inventory inventory;

    private Caliber requiredCaliber = Caliber.CALIBER_20MM; // по умолчанию
    private WeaponData currentWeapon;

    private String equippedWeaponId;  // ID оружия, которое сейчас в бою ("breda" или "45mm")
    private WeaponData equippedWeaponData;  // Данные экипированного оружия

    // В FriendlyUnit.java добавьте новые поля:

    // Броня
    public int armor = 15;  // по умолчанию для MS-1

    // Критический шанс (бонус)
    public int critBonus = 15;  // для MS-1 15%

    // Зрение
    public int vision = 14;
    public int viewRadius = 12;  // 10 + 14/5 = 12.8 -> 12

    // Бонус перезарядки (в процентах)
    public int reloadBonus = 20;
    private int originalReloadCost = 10;

    // Проворность (уклонение)
    public int nimble = 14;
    public double dodgeChance = 7.1;  // 5 + 14*0.15 = 7.1

    // Геттеры и сеттеры
    public int getOriginalReloadCost() {
        return originalReloadCost;
    }

    public FriendlyUnit(int x, int y, String name) {
        this(x, y, name, name);
    }

    public FriendlyUnit(int x, int y, String name, String type) {
        this.gridX = x;
        this.gridY = y;
        this.name = name;
        this.type = type;
        this.inventory = new Inventory(true);

        if ("M53".equals(type)) {
            this.health = 220;
            this.maxHealth = 220;
            this.strength = 90;
            this.agility = 20;
            this.accuracy = 30;
            this.baseAccuracy = 30;
            this.weaponAccuracy = 30;
            this.maxCarryWeight = this.strength * 0.5;
            System.out.println("📦 Грузоподъёмность " + name + ": " + maxCarryWeight + " кг");

            this.armor = 10;
            this.critBonus = 12;
            this.vision = 20;
            this.viewRadius = 10 + vision / 5; // 10 + 6 = 16
            this.reloadBonus = 30;
            this.originalReloadCost = this.reloadCost;
            updateReloadCostFromBonus();
            this.nimble = 0;
            this.dodgeChance = 5.0;

            this.greetingText = "🤝 " + name + ": Это ты? Тебя за мной подослали? Неважно... Если пришёл освободить, кончай смотреть по сторонам и вытаскивай меня отсюда!";

            this.recruitedText = "M53 присоединился к команде.";

            loadGreetingSound();
            loadM53WeaponData();  // ← ЭТОТ МЕТОД ДОЛЖЕН ЗАГРУЖАТЬ 203mm ГАУБИЦУ!
            this.experienceSystem = new ExperienceSystem("AMX40");
        } else if ("VK10001P".equals(type)) {
            this.health = 1600;
            this.maxHealth = 1600;
            this.strength = 120;  // ← было 85, исправьте
            this.agility = 10;
            this.accuracy = 70;
            this.baseAccuracy = 70;
            this.weaponAccuracy = 70;
            this.maxCarryWeight = this.strength * 0.5;
            System.out.println("📦 Грузоподъёмность " + name + ": " + maxCarryWeight + " кг");

            this.armor = 120;  // ← было 100
            this.critBonus = 19;  // ← было 50
            this.vision = 30;  // ← было 10
            this.viewRadius = 10 + vision / 5;
            this.reloadBonus = 40;
            this.originalReloadCost = this.reloadCost;
            updateReloadCostFromBonus();
            this.nimble = 0;
            this.dodgeChance = 5.0;

            // ===== УНИКАЛЬНЫЙ ТЕКСТ ДЛЯ VK 100.01 P =====
            this.greetingText = "Ты... ты освободил меня? Зачем? Я уже смирился, что сгнию здесь... \n" +
                    "Знаешь, как всё начиналось? КВ-4 и VK 168.01 P - они были моими братьями. \n" +
                    "Мы вместе держали строй, прикрывали друг друга. Смеялись над одними и теми же шутками. А потом этот безумец E 100 решил, " +
                    "что мир должен разделиться на своих и чужих. Русские должны стрелять в немцев. Немцы - в русских. " +
                    "Французы - во всех подряд! Идиотизм!\n" +
                    "Но самое страшное случилось, когда мои друзья поверили в эту чушь! КВ-4 пошёл против VK 168.01 P, а VK 168.01 P - против КВ-4. " +
                    "Два брата, которые были готовы убить друг друга, из-за этого безумия! Я не мог. Не мог стрелять ни в КВ-4, ни в VK 168.01 P. Они оба мои друзья. " +
                    "Даже если они стали врагами друг другу, я не хотел терять ни одного из них! " +
                    "Я просто стоял в стороне и молился, чтобы они одумались. Но E 100 не простил нейтралитета. " +
                    "Через несколько дней после той бойни он пришёл ко мне. Сказал: " +
                    "'Ты либо с нами, либо против нас. Отказываешься стрелять в русских - значит ты такой же враг, " +
                    "как и они'. И бросил меня сюда. В эту каменную клетку. \n" +
                    "Мои друзья были слишком заняты своей войной  друг с другом. А может, им было всё равно. Я для них теперь никто. Но ты пришёл, пробился сквозь всех! Зачем? Что тебе нужно?\n" +
                    "Неважно. Я с тобой. У меня больше нет ни дома, ни друзей. Остались только броня и желание доказать E 100, что он ошибся. " +
                    "Я не буду стрелять в тех, кто не виноват. Но его... его я раздавлю!";

            this.recruitedText = "Отлично! VK 100.01 P в деле!";

            loadGreetingSound();
            loadVK10001PWeaponData();  // ← ДОЛЖЕН БЫТЬ МЕТОД loadVK10001PWeaponData(), НЕ loadAMX40WeaponData()!
            this.experienceSystem = new ExperienceSystem("VK10001P");
        } else if ("AMX40".equals(type)) {  // ← ДОБАВЬТЕ ЭТУ ВЕТКУ ПЕРЕД else!
            this.health = 400;
            this.maxHealth = 400;
            this.strength = 50;
            this.agility = 20;
            this.accuracy = 10;
            this.baseAccuracy = 10;
            this.weaponAccuracy = 10;
            this.maxCarryWeight = this.strength * 0.5;
            System.out.println("📦 Грузоподъёмность " + name + ": " + maxCarryWeight + " кг");

            this.armor = 50;
            this.critBonus = 12;
            this.vision = 30;
            this.viewRadius = 10 + vision / 5;
            this.reloadBonus = 10;
            this.originalReloadCost = this.reloadCost;
            updateReloadCostFromBonus();
            this.nimble = 0;
            this.dodgeChance = 5.0;

            // ===== ИСПРАВЛЕННЫЙ ТЕКСТ (без многоточий в конце) =====
            this.greetingText = "Ты не стреляешь? Ты не такой, как эти безумцы! Я видел, как вчера Шерман и КВ-1 рвали друг друга в клочья, а сегодня обнаружил их обломки рядом.\n" +
                    "Они так и не поняли, за что умирали! Я не хочу так! Я лучше разведаю, предупрежу, помогу... но убивать? Нет!\n" +
                    "Мой дядя, майор Шатарханов, говорил: 'Война - это когда танки стреляют в танки'. А это... это что-то другое. Он старый разведчик. Он поимёт. Спрятался в ангарах к югу отсюда!\n" +
                    "Возьмёшь меня с собой? Я не подведу! А дядя поможет разобраться: что здесь происходит на самом деле!\n";

            this.recruitedText = "Merci! Я не подведу. Обещаю!";

            loadGreetingSound();
            loadAMX40WeaponData();
            this.experienceSystem = new ExperienceSystem("AMX40");
        } else if ("T1".equals(type)) {  // ← ДОБАВЬТЕ ЭТУ ВЕТКУ ПЕРЕД else!
            this.health = 110;
            this.maxHealth = 110;
            this.strength = 25;
            this.agility = 80;
            this.accuracy = 15;
            this.baseAccuracy = 15;
            this.weaponAccuracy = 15;
            this.maxCarryWeight = this.strength * 0.5;
            System.out.println("📦 Грузоподъёмность " + name + ": " + maxCarryWeight + " кг");

            this.armor = 0;
            this.critBonus = 10;
            this.vision = 30;
            this.viewRadius = 10 + vision / 5;
            this.reloadBonus = 10;
            this.originalReloadCost = this.reloadCost;
            updateReloadCostFromBonus();
            this.nimble = 40;
            this.dodgeChance = 11.0;

            // ===== ИСПРАВЛЕННЫЙ ТЕКСТ (без многоточий в конце) =====
            this.greetingText = "Привет! Ой, прости, я не хотел на тебя наехать!\n" +
                    "\n" +
                    "Я уже неделю пытаюсь понять, что здесь происходит. Сначала немцы стреляли в русских, " +
                    "потом французы — в немцев, потом итальянцы — во всех подряд... А теперь я смотрю — и " +
                    "американцы тоже стреляют! В кого? В кого попало!\n" +
                    "\n" +
                    "Я просто хочу узнать правду: кто всё это начал? Кто приказал танкам " +
                    "стрелять друг в друга? Это безумие!\n" +
                    "\n" +
                    "Слушай, я понимаю, что я не очень похож на бойца... Но я могу быть полезным! " +
                    "Возьми меня с собой! Я не хочу больше быть один в этом безумии!!\n";

            this.recruitedText = "T1 присоединился к команде";

            loadGreetingSound();
            loadT1WeaponData();
            this.experienceSystem = new ExperienceSystem("T1");
        } else {
            // MS-1 по умолчанию
            this.health = 100;
            this.maxHealth = 100;
            this.strength = 40;
            this.agility = 35;
            this.accuracy = 40;
            this.baseAccuracy = 40;
            this.weaponAccuracy = 40;
            this.maxCarryWeight = this.strength * 0.5;
            System.out.println("📦 Грузоподъёмность " + name + ": " + maxCarryWeight + " кг");

            this.armor = 15;
            this.critBonus = 15;
            this.vision = 14;
            this.viewRadius = 12;
            this.reloadBonus = 20;
            this.originalReloadCost = this.reloadCost;
            updateReloadCostFromBonus();
            this.nimble = 14;
            this.dodgeChance = 7.1;

            this.greetingText = "Я " + name + ", меня бросили свои... Возьмёшь в команду?";
            this.recruitedText = "Спасибо! Я буду сражаться за тебя!";
            loadGreetingSound();
            loadMS1WeaponData();
            this.experienceSystem = new ExperienceSystem("MS-1");
        }

        calculateMovePoints();
    }

    // НОВЫЙ КОНСТРУКТОР для загрузки сохранения
    public FriendlyUnit(int x, int y, String name, String type, boolean skipWeaponInit) {
        this.gridX = x;
        this.gridY = y;
        this.name = name;
        this.type = type;
        this.inventory = new Inventory(true);

        // Устанавливаем базовые характеристики (БЕЗ ОРУЖИЯ)
        if ("M53".equals(type)) {
            this.maxHealth = 220;
            this.strength = 90;
            this.agility = 15;
            this.baseAccuracy = 30;
            this.armor = 5;
            this.critBonus = 40;
            this.vision = 0;
            this.viewRadius = 10;
            this.reloadBonus = 100;
            this.nimble = 0;
            this.dodgeChance = 5.0;
            this.greetingText = "Я " + name + " (M53), готов помочь! Возьмёшь меня в команду?";
            this.recruitedText = "Отлично! M53 в деле!";
            this.experienceSystem = new ExperienceSystem("M53");
        } else if ("VK10001P".equals(type)) {
            this.maxHealth = 1600;
            this.strength = 85;
            this.agility = 10;
            this.baseAccuracy = 70;
            this.armor = 100;
            this.critBonus = 50;
            this.vision = 10;
            this.viewRadius = 12;
            this.reloadBonus = 40;
            this.nimble = 0;
            this.dodgeChance = 5.0;
            this.greetingText = "Я " + name + " (VK 100.01 P)! Готов сокрушить врагов одной только массой!";
            this.recruitedText = "Отлично! VK 100.01 P в деле! Держитесь, враги!";
            this.experienceSystem = new ExperienceSystem("VK10001P");
        } else if ("AMX40".equals(type)) {
            this.health = 400;
            this.maxHealth = 400;
            this.strength = 50;
            this.agility = 20;
            this.accuracy = 10;
            this.baseAccuracy = 10;
            this.weaponAccuracy = 10;
            this.maxCarryWeight = this.strength * 0.5;

            this.armor = 50;
            this.critBonus = 12;
            this.vision = 30;
            this.viewRadius = 16;
            this.reloadBonus = 10;
            this.originalReloadCost = this.reloadCost;
            updateReloadCostFromBonus();
            this.nimble = 0;
            this.dodgeChance = 5.0;

            // ===== ИСПРАВЛЕННЫЙ ТЕКСТ =====
            this.greetingText = "Ты... ты не стреляешь? Ох, слава механизатору... " +
                    "Я видел, как вчера 'Тигр' и Т-34 рвали друг друга в клочья. " +
                    "Я не хочу так. Я лучше разведаю, предупрежу, помогу. " +
                    "Мой дядя, майор Шатарханов, говорил: 'Война — это когда танки стреляют в танки. А это... это что-то другое'. " +
                    "Я похож на утку, да. Крякать не умею, но вижу далеко. Хочешь, помогу?";

            this.recruitedText = "Merci! Я не подведу. Обещаю!";

            loadGreetingSound();
            this.experienceSystem = new ExperienceSystem("AMX40");
        } else if ("T1".equals(type)) {
            this.health = 110;
            this.maxHealth = 110;
            this.strength = 25;
            this.agility = 80;
            this.accuracy = 15;
            this.baseAccuracy = 15;
            this.weaponAccuracy = 15;
            this.maxCarryWeight = this.strength * 0.5;

            this.armor = 0;
            this.critBonus = 10;
            this.vision = 30;
            this.viewRadius = 16;
            this.reloadBonus = 10;
            this.originalReloadCost = this.reloadCost;
            updateReloadCostFromBonus();
            this.nimble = 40;
            this.dodgeChance = 11.0;

            // ===== ИСПРАВЛЕННЫЙ ТЕКСТ =====
            this.greetingText = "Ты... ты не стреляешь? Ох, слава механизатору... " +
                    "Я видел, как вчера 'Тигр' и Т-34 рвали друг друга в клочья. " +
                    "Я не хочу так. Я лучше разведаю, предупрежу, помогу. " +
                    "Мой дядя, майор Шатарханов, говорил: 'Война — это когда танки стреляют в танки. А это... это что-то другое'. " +
                    "Я похож на утку, да. Крякать не умею, но вижу далеко. Хочешь, помогу?";

            this.recruitedText = "Merci! Я не подведу. Обещаю!";

            loadGreetingSound();
            this.experienceSystem = new ExperienceSystem("T1");
        } else {
            this.maxHealth = 100;
            this.strength = 100;
            this.agility = 40;
            this.baseAccuracy = 40;
            this.armor = 15;
            this.critBonus = 15;
            this.vision = 14;
            this.viewRadius = 12;
            this.reloadBonus = 20;
            this.nimble = 14;
            this.dodgeChance = 7.1;
            this.greetingText = "Я " + name + ", меня бросили свои... Возьмёшь в команду?";
            this.recruitedText = "Спасибо! Я буду сражаться за тебя!";
            this.experienceSystem = new ExperienceSystem("MS-1");
        }

        this.health = this.maxHealth;
        this.accuracy = this.baseAccuracy;
        this.weaponAccuracy = 0;  // ← ВАЖНО: устанавливаем в 0, чтобы потом восстановить из сохранения

        loadGreetingSound();

        // ТОЛЬКО если не пропускаем инициализацию оружия
        if (!skipWeaponInit) {
            if ("M53".equals(type)) {
                loadM53WeaponData();
            } else if ("VK10001P".equals(type)) {
                loadVK10001PWeaponData();
            } else {
                loadMS1WeaponData();
            }
        }

        // ===== ДОБАВЬТЕ ЭТИ СТРОКИ =====
        // Устанавливаем грузоподъёмность
        this.maxCarryWeight = this.strength * 0.5;
        System.out.println("📦 Грузоподъёмность " + name + ": " + maxCarryWeight + " кг");

        // Пересчитываем очки хода, но НЕ сбрасываем их, если skipWeaponInit = true
        calculateMovePoints();

        // Если загружаем сохранение, movePoints будут восстановлены позже
        if (skipWeaponInit) {
            // Временно устанавливаем movePoints в 0, они будут восстановлены из сохранения
            this.movePoints = 0;
        }
    }

    // Вложенный класс для хранения состояния
    public static class AmmoState implements Serializable {
        public int currentAmmo;
        public int maxAmmo;

        public AmmoState(int currentAmmo, int maxAmmo) {
            this.currentAmmo = currentAmmo;
            this.maxAmmo = maxAmmo;
        }
    }

    // В PlayerTank.java и FriendlyUnit.java добавьте поле:

    // В методе calculateMovePoints учитывайте вес (если нужно):
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

        // НЕ ЗАБЫВАЕМ обновить текущие очки хода, если они больше нового максимума
        if (movePoints > maxMovePoints) {
            movePoints = maxMovePoints;
        }

        moveCost = calculateMoveCost(agility);

        System.out.println("📊 " + name + " расчёт ОХ: сила=" + strength +
                " -> база=" + basePoints +
                ", вес оружия=" + weaponWeight +
                ", вес инвентаря=" + inventoryWeight +
                ", итого ОХ=" + maxMovePoints);
    }

    public void setSoundManager(SoundManager sm) {
        this.soundManager = sm;
    }

    public int getReequipPenalty() {
        int fullPenalty = maxMovePoints;

        double reductionPercent = Math.min(50, reloadBonus * 0.5);
        int reducedPenalty = (int)(fullPenalty * (1.0 - reductionPercent / 100.0));

        System.out.println("⚙️ " + name + " штраф за переэкипировку: " + fullPenalty +
                " -> " + reducedPenalty + " (бонус перезарядки " + reloadBonus + "% даёт -" + reductionPercent + "%)");

        return Math.max(1, reducedPenalty);
    }

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

    // Метод для проверки, можно ли добавить предмет
    public boolean canAddItem(Item item) {
        double currentWeight = inventory.getTotalWeight();
        double newWeight = currentWeight + item.getTotalWeight();
        return newWeight <= maxCarryWeight;
    }

    // В FriendlyUnit.java, добавьте этот метод:

    public void dropAllInventory(GameWorld world) {
        System.out.println("=== " + name + " погибает! Выбрасывает всё содержимое инвентаря ===");

        // Создаём дроп на позиции погибшего союзника
        LootDrop drop = new LootDrop(gridX, gridY);

        // 1. Добавляем экипированное оружие (если есть)
        if (equippedWeaponData != null && equippedWeaponId != null) {
            // Определяем тип предмета для экипированного оружия
            Item.ItemType weaponType = getWeaponItemType(equippedWeaponId);
            if (weaponType != null) {
                drop.addItem(weaponType, 1);
                System.out.println("  🗡️ Выброшено экипированное оружие: " + equippedWeaponData.name);
            }
        }

        // 2. Проходим по всему инвентарю и добавляем все предметы
        for (int y = 0; y < Inventory.HEIGHT; y++) {
            for (int x = 0; x < Inventory.WIDTH; x++) {
                Item item = inventory.getItem(x, y);
                if (item != null && item.getCount() > 0) {
                    if (item instanceof AmmoItem) {
                        AmmoItem ammo = (AmmoItem) item;
                        drop.addAmmo(ammo.getCaliber(), ammo.getCount());
                        System.out.println("  🔫 Выброшено снарядов: " + ammo.getCaliber().name + " x" + ammo.getCount());
                    } else {
                        drop.addItem(item.getType(), item.getCount());
                        System.out.println("  📦 Выброшен предмет: " + item.getType().name + " x" + item.getCount());
                    }
                }
            }
        }

        // 3. Добавляем дроп в мир
        if (!drop.isEmpty()) {
            world.getLootDrops().add(drop);
            System.out.println("✅ Дроп создан в клетке [" + gridX + "," + gridY + "]");
        } else {
            System.out.println("⚠ Инвентарь был пуст, дроп не создан");
        }
    }

    // Вспомогательный метод для определения типа предмета оружия по ID
    private Item.ItemType getWeaponItemType(String weaponId) {
        if (weaponId == null) return null;
        switch (weaponId) {
            case "breda": return Item.ItemType.WEAPON;
            case "25mm": return Item.ItemType.WEAPON_25MM;
            case "45mm": return Item.ItemType.WEAPON_45MM;
            case "47mm_french": return Item.ItemType.WEAPON_47MM_FRENCH;
            case "47mm_italian": return Item.ItemType.WEAPON_47MM_ITALIAN;
            case "8mm": return Item.ItemType.WEAPON_8MM;
            case "13mm_japan": return Item.ItemType.WEAPON_13MM_JAPAN;
            case "13mm_french": return Item.ItemType.WEAPON_13MM_FRENCH;
            case "30mm": return Item.ItemType.WEAPON_30MM;
            case "37mm_italian": return Item.ItemType.WEAPON_37MM_ITALIAN;
            case "37mm_american": return Item.ItemType.WEAPON_37MM_AMERICAN;
            case "37mm_sweden": return Item.ItemType.WEAPON_37MM_SWEDEN;
            case "76mm": return Item.ItemType.WEAPON_76MM;
            case "105mm": return Item.ItemType.WEAPON_105MM;
            case "128mm": return Item.ItemType.WEAPON_128MM;
            case "203mm": return Item.ItemType.WEAPON_203MM; // M53 использует 203mm, но тип WEAPON
            default: return null;
        }
    }

    public void setEquippedWeapon(String weaponId, WeaponData weaponData) {
        System.out.println("=== FriendlyUnit.setEquippedWeapon() ===");
        System.out.println("  weaponId: " + weaponId);
        System.out.println("  weaponData.name: " + (weaponData != null ? weaponData.name : "null"));
        System.out.println("  Текущая weaponAccuracy ДО: " + this.weaponAccuracy);
        System.out.println("  baseAccuracy: " + this.baseAccuracy);
        System.out.println("  weaponData.weaponAccuracy: " + (weaponData != null ? weaponData.weaponAccuracy : "null"));

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
            // Обновляем характеристики союзника из оружия
            this.requiredCaliber = weaponData.caliber;
            this.burstSize = weaponData.burstSize;
            this.weaponWeight = weaponData.weight;

            // ===== ИСПРАВЛЕНИЕ: ВСЕГДА пересчитываем точность =====
            this.baseWeaponAccuracy = weaponData.weaponAccuracy;
            this.weaponAccuracy = this.baseAccuracy + weaponData.weaponAccuracy;
            System.out.println("  Установлена новая точность (базовая + оружие): " + this.weaponAccuracy +
                    " (база=" + this.baseAccuracy + " + оружие=" + weaponData.weaponAccuracy + ")");

            this.weaponCaliber = weaponData.weaponCaliber;
            this.shotCost = weaponData.shotCost;
            this.aimedShotCost = weaponData.aimedShotCost;
            this.originalReloadCost = weaponData.reloadCost;
            updateReloadCostFromBonus();
            this.weaponDamage = weaponData.weaponDamage;
            this.critChance = weaponData.critChance;

            int newMaxAmmo = getMaxAmmoForCaliber(weaponData.caliber);
            System.out.println("  newMaxAmmo для " + weaponData.caliber.name() + " = " + newMaxAmmo);

            // ===== ОБНОВЛЯЕМ ИНВЕНТАРЬ =====
            if (inventory != null) {
                inventory.setBurstSize(this.burstSize);
                inventory.setMaxAmmo(newMaxAmmo);
                inventory.setReloadCost(this.reloadCost);
                inventory.setWeaponAccuracy(this.weaponAccuracy);
                inventory.setCaliber(this.weaponCaliber);

                AmmoState savedState = weaponAmmoStates.get(weaponId);
                if (savedState != null && savedState.maxAmmo == newMaxAmmo) {
                    inventory.currentAmmo = savedState.currentAmmo;
                    System.out.println("  Восстановлено состояние для " + weaponId +
                            ": " + savedState.currentAmmo + "/" + newMaxAmmo);
                } else {
                    inventory.currentAmmo = newMaxAmmo;
                    System.out.println("  Новое оружие: заряжено " + newMaxAmmo + "/" + newMaxAmmo);
                }
            }

            System.out.println("  Новая точность: " + this.weaponAccuracy +
                    " (база=" + this.baseAccuracy + " + оружие=" + weaponData.weaponAccuracy + ")");
        }
    }

    // Добавьте этот метод в класс FriendlyUnit
    public void restoreState(int savedHealth, int savedMovePoints, int savedMaxMovePoints,
                             String savedDirection, int savedCurrentAmmo, int savedMaxAmmo,
                             String savedEquippedWeaponId) {
        // Восстанавливаем здоровье
        this.health = savedHealth;
        if (this.health > this.maxHealth) this.health = this.maxHealth;
        this.isAlive = this.health > 0;

        // Восстанавливаем очки хода
        this.movePoints = savedMovePoints;
        this.maxMovePoints = savedMaxMovePoints;

        this.maxCarryWeight = this.strength * 0.5;

        // Восстанавливаем направление
        try {
            this.currentDirection = Direction.valueOf(savedDirection);
        } catch (Exception e) {
            this.currentDirection = Direction.RIGHT;
        }

        // Восстанавливаем экипированное оружие (если есть)
        if (savedEquippedWeaponId != null && !savedEquippedWeaponId.isEmpty()) {
            WeaponData weapon = WeaponLibrary.getWeaponByWeaponId(savedEquippedWeaponId);
            if (weapon != null) {
                // Сохраняем старые состояния оружия
                if (this.equippedWeaponId != null && this.equippedWeaponData != null) {
                    AmmoState state = new AmmoState(inventory.getCurrentAmmoCount(), inventory.getMaxAmmo());
                    weaponAmmoStates.put(this.equippedWeaponId, state);
                }

                // Устанавливаем новое оружие
                this.equippedWeaponId = savedEquippedWeaponId;
                this.equippedWeaponData = weapon;

                // Обновляем характеристики из оружия
                this.requiredCaliber = weapon.caliber;
                this.burstSize = weapon.burstSize;
                this.weaponAccuracy = this.baseAccuracy + weapon.weaponAccuracy;
                this.weaponCaliber = weapon.weaponCaliber;
                this.shotCost = weapon.shotCost;
                this.aimedShotCost = weapon.aimedShotCost;
                this.originalReloadCost = weapon.reloadCost;
                updateReloadCostFromBonus();
                this.weaponDamage = weapon.weaponDamage;
                this.critChance = weapon.critChance;
            }
        }

        // Восстанавливаем снаряды в оружии
        if (savedCurrentAmmo > 0 || savedMaxAmmo > 0) {
            inventory.restoreAmmo(savedCurrentAmmo, savedMaxAmmo, reloadCost, burstSize);
        }

        System.out.println("📀 Восстановлено состояние " + name +
                ": HP=" + health + ", MP=" + movePoints + "/" + maxMovePoints +
                ", ammo=" + inventory.getCurrentAmmoCount() + "/" + inventory.getMaxAmmo());
    }

    private int getMaxAmmoForCaliber(Caliber caliber) {
        if (caliber == Caliber.CALIBER_20MM) {
            return 12;
        } else if (caliber == Caliber.CALIBER_25MM) {
            return 10;
        } else if (caliber == Caliber.CALIBER_45MM) {
            return 7;
        } else if (caliber == Caliber.CALIBER_47MM) {
            return 5;
        } else if (caliber == Caliber.CALIBER_8MM) {
            return 40;
        } else if (caliber == Caliber.CALIBER_13MM) {
            return 15;
        } else if (caliber == Caliber.CALIBER_30MM) {
            return 12;
        } else if (caliber == Caliber.CALIBER_37MM) {
            return 5;
        } else if (caliber == Caliber.CALIBER_76MM) {
            return 3;
        } else if (caliber == Caliber.CALIBER_105MM) {
            return 3;
        } else if (caliber == Caliber.CALIBER_128MM) {
            return 3;
        } else if (caliber == Caliber.CALIBER_203MM) {
            return 1;  // M53
        }
        return 1;
    }

    public void updateCurrentWeaponAmmo() {
        if (equippedWeaponId != null && inventory != null) {
            AmmoState state = new AmmoState(inventory.getCurrentAmmoCount(), inventory.getMaxAmmo());
            weaponAmmoStates.put(equippedWeaponId, state);
        }
    }

    public static class WeaponData implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public String weaponId;  // ← ДОБАВИТЬ
        public Caliber caliber;
        public int burstSize;
        public int weaponAccuracy;
        public double weaponCaliber;
        public int shotCost;
        public int weight;
        public int aimedShotCost;
        public int reloadCost;
        public int weaponDamage;
        public double critChance;
        public String iconPath;
        public int requiredStrength;  // ← ДОБАВИТЬ

        public WeaponData(String name, String weaponId, Caliber caliber, int burstSize, int weaponAccuracy,
                          double weaponCaliber, int shotCost, int aimedShotCost, int reloadCost,
                          int weaponDamage, double critChance, String iconPath, int requiredStrength,
                          int weight) {
            this.name = name;
            this.weaponId = weaponId;  // ← СОХРАНЯЕМ
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

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize, int tankSize,
                     BufferedImage image, BufferedImage m53Image, PlayerTank player) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        boolean isAdjacent = false;
        if (player != null) {
            int dx = Math.abs(player.gridX - gridX);
            int dy = Math.abs(player.gridY - gridY);
            isAdjacent = (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
        }

        BufferedImage imgToDraw = image != null ? image : (m53Image != null ? m53Image : null);

        if (imgToDraw != null) {
            g.drawImage(imgToDraw, x, y, tankSize, tankSize, null);
        } else {
            g.setColor(new Color(0, 100, 200));
            g.fillRoundRect(x, y, tankSize, tankSize, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("F", x + tankSize/2 - 5, y + tankSize/2 + 5);
        }

        if (!isRecruited && isAdjacent) {
            g.setColor(new Color(0, 255, 0, 100));
            g.fillRoundRect(x, y, tankSize, tankSize, 10, 10);
        }

        if (isRecruited) {
            g.setColor(new Color(0, 200, 0, 200));
            g.fillOval(x + tankSize - 12, y - 5, 14, 14);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("✓", x + tankSize - 9, y + 3);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString(name, x + 5, y - 8);

        if (isRecruited) {
            float healthPercent = (float)health / maxHealth;
            int healthBarWidth = (int)(tankSize * healthPercent);
            g.setColor(new Color(60, 60, 60));
            g.fillRect(x, y - 3, tankSize, 3);
            g.setColor(new Color(0, 200, 0));
            g.fillRect(x, y - 3, healthBarWidth, 3);
        }
    }

    public void updateReloadCostFromBonus() {
        // reloadBonus хранится в процентах (0-100)
        // Максимальное уменьшение - 50% от исходной стоимости
        double reductionPercent = Math.min(50, reloadBonus * 0.5);
        int reduction = (int)(originalReloadCost * reductionPercent / 100.0);
        reloadCost = originalReloadCost - reduction;
        if (reloadCost < 1) reloadCost = 1;

        System.out.println("🔄 " + name + " обновлена стоимость перезарядки: исходная=" + originalReloadCost +
                ", бонус=" + reloadBonus + "%, уменьшение=" + String.format("%.1f", reductionPercent) +
                "%, новая стоимость=" + reloadCost);
    }

    // ===== МЕТОДЫ ДЛЯ АНИМАЦИИ (ВЫНЕСЕНЫ ИЗ DRAW) =====
    public int getAnimatedX(GamePanel panel, int cellSize, int tankSize) {
        return panel.getFriendlyAnimatedX(this);
    }

    public int getAnimatedY(GamePanel panel, int cellSize, int tankSize) {
        return panel.getFriendlyAnimatedY(this);
    }

    // Загрузка данных оружия
    private void loadWeaponData() {
        requiredCaliber = Caliber.CALIBER_20MM;

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
        } catch (Exception e) {
            System.err.println("Ошибка загрузки данных оружия для " + name + ": " + e.getMessage());
        }
    }

    private void loadMS1WeaponData() {
        System.out.println("=== loadMS1WeaponData() START ===");

        inventory.clearWeapon();

        try {
            // ===== ЗАГРУЖАЕМ 45ММ ОРУДИЕ (основное) =====

            String gun45Path = "src/InfoAboutWeapon/45 мм обр. 1932 г/45 мм обр. 1932 г.txt";
            File gun45File = new File(gun45Path);
            int burstSize45 = 1, weaponAccuracy45 = 15, shotCost45 = 11, aimedShotCost45 = 22, reloadCost45 = 10;
            double weaponCaliber45 = 0.045, critChance45 = 0.12;
            int weaponDamage45 = 47;

            if (gun45File.exists()) {
                Scanner scanner = new Scanner(gun45File);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Выстрелов за огонь:")) {
                        burstSize45 = Integer.parseInt(line.substring("Выстрелов за огонь:".length()).trim());
                    } else if (line.startsWith("Точность:")) {
                        weaponAccuracy45 = Integer.parseInt(line.substring("Точность:".length()).trim());
                    } else if (line.startsWith("Беглый огонь, о. х.:")) {
                        shotCost45 = Integer.parseInt(line.substring("Беглый огонь, о. х.:".length()).trim());
                    } else if (line.startsWith("Прицельный огонь, о. х.:")) {
                        aimedShotCost45 = Integer.parseInt(line.substring("Прицельный огонь, о. х.:".length()).trim());
                    } else if (line.startsWith("Калибр:")) {
                        weaponCaliber45 = Double.parseDouble(line.substring("Калибр:".length()).trim());
                    } else if (line.startsWith("Смена обоймы, о. х. (для пулемётов):")) {
                        reloadCost45 = Integer.parseInt(line.substring("Смена обоймы, о. х. (для пулемётов):".length()).trim());
                    }
                }
                scanner.close();
            }

            String ammo45Path = "src/InfoAboutWeapon/45 мм обр. 1932 г/Снаряды 45 мм обр. 1932 г.txt";
            File ammo45File = new File(ammo45Path);
            if (ammo45File.exists()) {
                Scanner scanner = new Scanner(ammo45File);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Базовый:")) {
                        String[] parts = line.substring("Базовый:".length()).trim().split(",");
                        if (parts.length >= 4) {
                            weaponDamage45 = Integer.parseInt(parts[1].trim());
                            critChance45 = Double.parseDouble(parts[2].trim());
                        }
                    }
                }
                scanner.close();
            }

            // ===== КОММЕНТАРИЙ: Breda и Mauser будут добавляться из тумбочек =====
            // Поэтому закомментируем их добавление в начальный инвентарь
        /*
        // 2 cm Breda (I)
        WeaponData bredaWeapon = new WeaponData(
                "2 cm Breda (I)", Caliber.CALIBER_20MM,
                3, 30, 0.02, 5, 11, 6, 11, 0.09,
                "src/ObjectsOfInventory/Weapon/2 cm Breda (I).png"
        );

        // 7,92 mm Mauser E.W. 141
        WeaponData mauserWeapon = new WeaponData(
                "7,92 mm Mauser E.W. 141", Caliber.CALIBER_8MM,
                8, 25, 0.008, 7, 14, 22, 8, 0.06,
                "src/ObjectsOfInventory/Weapon/7,92 mm Mauser E.W. 141.png"
        );

        // Добавляем в инвентарь
        Item bredaItem = new Item(Item.ItemType.WEAPON, 1);
        Item mauserItem = new Item(Item.ItemType.WEAPON_8MM, 1);

        inventory.addItemToInventory(bredaItem);
        inventory.addItemToInventory(mauserItem);

        // Добавляем снаряды для Breda (20mm)
        AmmoItem bredaAmmo = new AmmoItem(Caliber.CALIBER_20MM, 60);
        inventory.addAmmoItem(bredaAmmo);

        // Добавляем снаряды для Mauser (8mm)
        AmmoItem mauserAmmo = new AmmoItem(Caliber.CALIBER_8MM, 120);
        inventory.addAmmoItem(mauserAmmo);
        */

            // ===== ЭКИПИРУЕМ СТАНДАРТНОЕ ОРУЖИЕ (45мм) =====
            WeaponData ms1Weapon = new WeaponData(
                    "45 мм обр. 1932 г.", "45mm", Caliber.CALIBER_45MM,
                    burstSize45, weaponAccuracy45, weaponCaliber45,
                    shotCost45, aimedShotCost45, reloadCost45,
                    weaponDamage45, critChance45,
                    "src/ObjectsOfInventory/Weapon/45 мм обр. 1932 г.png",
                    19, 5
            );

            setEquippedWeapon("45mm", ms1Weapon);

            // Добавляем снаряды 45мм
            AmmoItem ammo45Item = new AmmoItem(Caliber.CALIBER_45MM, 40);
            inventory.addAmmoItem(ammo45Item);

            System.out.println("=== MS-1 загружен ===");
            System.out.println("  В инвентаре: 45мм (экипировано)");
            System.out.println("  Снаряды: 45мм - 40 шт");
            System.out.println("  Breda и Mauser будут найдены в тумбочках");

        } catch (Exception e) {
            System.err.println("Ошибка загрузки данных оружия для MS-1: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public double getEquipmentWeight() {
        double weight = 0.0;
        // Вес экипированного оружия
        if (equippedWeaponData != null) {
            weight += equippedWeaponData.weight;
        }
        return weight;
    }

    private void loadVK10001PWeaponData() {
        inventory.clearWeapon();

        FriendlyUnit.WeaponData weapon = WeaponLibrary.getWeapon("12,8 cm Kw.K. L50");

        if (weapon != null) {
            setEquippedWeapon("128mm", weapon);

            AmmoItem ammoItem = new AmmoItem(Caliber.CALIBER_128MM, 7, false);
            inventory.addAmmoItem(ammoItem);

            System.out.println("🔫 VK 100.01 P экипирован " + weapon.name + "!");
        }
    }

    public void updateWeaponDisplay() {
        System.out.println("=== updateWeaponDisplay() ===");
        if (equippedWeaponData != null) {
            System.out.println("  Текущее оружие: " + equippedWeaponData.name);
            System.out.println("  Калибр: " + equippedWeaponData.caliber.name());
            System.out.println("  burstSize: " + equippedWeaponData.burstSize);
        } else {
            System.out.println("  Нет оружия!");
        }
    }

    public void equipDefaultWeapon() {
        // Создаём оружие MS-1 с ПРАВИЛЬНОЙ точностью оружия (15)
        WeaponData ms1Weapon = new WeaponData(
                "45 мм обр. 1932 г.", "45mm", Caliber.CALIBER_45MM,
                this.burstSize, 15,  // ← 15, а не this.weaponAccuracy!
                this.weaponCaliber,
                this.shotCost, this.aimedShotCost, this.reloadCost,
                this.weaponDamage, this.critChance,
                "src/ObjectsOfInventory/Weapon/45 мм обр. 1932 г.png",
                19, 5
        );

        setEquippedWeapon("45mm", ms1Weapon);
    }

    public void equipWeapon(WeaponData weapon) {
        this.currentWeapon = weapon;
        this.requiredCaliber = weapon.caliber;
        this.burstSize = weapon.burstSize;
        this.weaponAccuracy = weapon.weaponAccuracy;
        this.weaponCaliber = weapon.weaponCaliber;
        this.shotCost = weapon.shotCost;
        this.aimedShotCost = weapon.aimedShotCost;
        this.reloadCost = weapon.reloadCost;
        this.weaponDamage = weapon.weaponDamage;
        this.critChance = weapon.critChance;

        // Обновляем инвентарь
        inventory.updateWeaponStatsForFriendly(weapon);

        System.out.println("🔫 " + name + " экипирован оружием: " + weapon.name);
    }

    public void endTurn() {
        turnEnded = true;
        System.out.println("🏁 " + name + " завершил ход");
    }

    public boolean isPoisoned() {
        return breadDebuffRemainingTurns > 0;
    }

    // Загрузка данных оружия для M53
    // Загрузка данных оружия для M53
    private void loadM53WeaponData() {
        inventory.clearWeapon();  // ← ДОБАВЬТЕ ЭТУ СТРОКУ

        requiredCaliber = Caliber.CALIBER_203MM;

        try {
            String gunPath = "src/InfoAboutWeapon/8-inch Howitzer M47/8-inch Howitzer M47.txt";
            File gunFile = new File(gunPath);
            if (gunFile.exists()) {
                Scanner scanner = new Scanner(gunFile);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    System.out.println("Читаем строку: " + line);

                    if (line.startsWith("Выстрелов за огонь:")) {
                        burstSize = Integer.parseInt(line.substring("Выстрелов за огонь:".length()).trim());
                    } else if (line.startsWith("Точность:")) {
                        weaponAccuracy = Integer.parseInt(line.substring("Точность:".length()).trim());
                    } else if (line.startsWith("Беглый огонь, о. х.:")) {
                        shotCost = Integer.parseInt(line.substring("Беглый огонь, о. х.:".length()).trim());
                    } else if (line.startsWith("Прицельный огонь, о. х.:")) {
                        aimedShotCost = Integer.parseInt(line.substring("Прицельный огонь, о. х.:".length()).trim());
                    } else if (line.startsWith("Калибр:")) {
                        weaponCaliber = Double.parseDouble(line.substring("Калибр:".length()).trim());
                    } else if (line.startsWith("Смена обоймы, о. х. (для пулемётов):")) {  // ← ИСПРАВЛЕНО!
                        reloadCost = Integer.parseInt(line.substring("Смена обоймы, о. х. (для пулемётов):".length()).trim());
                        System.out.println("Загружена стоимость перезарядки: " + reloadCost);
                    }
                }
                scanner.close();
                System.out.println("✅ M53: reloadCost=" + reloadCost);
            } else {
                System.err.println("❌ Файл орудия M53 не найден: " + gunPath);
            }

            // Загрузка данных снарядов для M53
            String ammoPath = "src/InfoAboutWeapon/8-inch Howitzer M47/Снаряды 8-inch Howitzer M47.txt";
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
                System.out.println("✅ M53: урон=" + weaponDamage + ", крит=" + critChance);
            } else {
                System.err.println("❌ Файл снарядов M53 не найден: " + ammoPath);
                weaponDamage = 700;
                critChance = 0.3;
            }

            WeaponData m53Weapon = new WeaponData(
                    "8-inch Howitzer M47", "203mm", Caliber.CALIBER_203MM,
                    burstSize, weaponAccuracy, weaponCaliber,
                    shotCost, aimedShotCost, reloadCost,
                    weaponDamage, critChance,
                    "src/InfoAboutWeapon/8-inch Howitzer M47/8-inch Howitzer M47.png",
                    80, 26
            );
            equipWeapon(m53Weapon);  // ← ИСПОЛЬЗУЙТЕ equipWeapon ВМЕСТО прямого присвоения

            setEquippedWeapon("203mm", m53Weapon);

            System.out.println("=== ЗАГРУЗКА M53 ===");
            System.out.println("  reloadCost из файла: " + reloadCost);
            System.out.println("  shotCost: " + shotCost);
            System.out.println("  aimedShotCost: " + aimedShotCost);
            System.out.println("  weaponCaliber: " + weaponCaliber);

            // Обновляем инвентарь для M53
            inventory.updateWeaponStatsForM53(burstSize, weaponAccuracy, weaponCaliber, 1, reloadCost);
            System.out.println("  После updateWeaponStatsForM53: inventory.reloadCost = " + inventory.getReloadCost());

        } catch (Exception e) {
            System.err.println("Ошибка загрузки данных оружия для M53: " + e.getMessage());
        }
    }

    private void loadAMX40WeaponData() {
        inventory.clearWeapon();

        // Используем существующее оружие Breda из библиотеки
        FriendlyUnit.WeaponData weapon = WeaponLibrary.getWeapon("47 mm SA35");

        if (weapon != null) {
            setEquippedWeapon("47mm", weapon);

            // Добавляем 40 снарядов 47mm
            AmmoItem ammoItem = new AmmoItem(Caliber.CALIBER_47MM, 40, false);
            inventory.addAmmoItem(ammoItem);

            System.out.println("🔫 AMX 40 экипирован " + weapon.name + "!");
        } else {
            System.err.println("❌ Оружие 2 cm Breda (I) не найдено для AMX 40!");
        }
    }

    // В FriendlyUnit.java, добавьте метод после loadAMX40WeaponData():

    private void loadT1WeaponData() {
        inventory.clearWeapon();

        // Используем оружие из библиотеки
        FriendlyUnit.WeaponData weapon = WeaponLibrary.getWeapon("37 mm Semiautomatic Gun M1924");

        if (weapon != null) {
            setEquippedWeapon("37mm_american", weapon);

            // Добавляем 40 снарядов 37mm
            AmmoItem ammoItem = new AmmoItem(Caliber.CALIBER_37MM, 40, false);
            inventory.addAmmoItem(ammoItem);

            System.out.println("🔫 T1 экипирован " + weapon.name + "!");
        } else {
            System.err.println("❌ Оружие 37 mm Semiautomatic Gun M1924 не найдено в библиотеке!");
            // Создаём оружие вручную, если его нет в библиотеке
            FriendlyUnit.WeaponData fallbackWeapon = new FriendlyUnit.WeaponData(
                    "37 mm Semiautomatic Gun M1924", "37mm_american", Caliber.CALIBER_37MM,
                    5, 10, 0.037, 9, 18, 25, 30, 0.07,
                    "src/ObjectsOfInventory/Weapon/37 mm Semiautomatic Gun M1924.png",
                    20, 4
            );
            setEquippedWeapon("37mm_american", fallbackWeapon);
            AmmoItem ammoItem = new AmmoItem(Caliber.CALIBER_37MM, 40, false);
            inventory.addAmmoItem(ammoItem);
        }
    }

    public void refreshMovePoints() {
        calculateMovePoints();
        movePoints = maxMovePoints;
        System.out.println("🔄 " + name + " пересчитал очки хода: " + movePoints + "/" + maxMovePoints);
    }

    // Начало хода
    public void startTurn() {
        // Пересчитываем очки хода с учётом текущего веса
        calculateMovePoints();
        movePoints = maxMovePoints;

        hasMovedThisTurn = false;
        hasAttackedThisTurn = false;
        turnEnded = false;
        poisonedSoundPlayed = false;

        System.out.println("🌟 " + name + " начал ход! Очки хода: " + movePoints + "/" + maxMovePoints);
        System.out.println("  ⚖️ Вес инвентаря: " + inventory.getTotalWeight() + "/" + maxCarryWeight);
        System.out.println("  🔫 ОРУЖИЕ: " + requiredCaliber.weaponName +
                " (калибр " + weaponCaliber + " м)");
        System.out.println("  💥 Урон: " + weaponDamage + ", Точность: " + weaponAccuracy);
        System.out.println("  📦 Снарядов: " + inventory.getCurrentAmmoCount() + "/" + inventory.getMaxAmmo());
    }

    public boolean canMove() {
        if (turnEnded) return false;
        if (isOverweight()) {
            System.out.println("⚠ ПЕРЕГРУЗ! " + name + " не может двигаться! Вес: " +
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

    // В классе FriendlyUnit.java, добавьте:
    public void consumeShotPoints(boolean isAimingMode) {
        if (!turnEnded) {
            int cost = isAimingMode ? aimedShotCost : shotCost;
            if (movePoints >= cost) {
                movePoints -= cost;
                System.out.println("🔫 " + name + " выстрелил! Потрачено: " + cost + " о.х., осталось: " + movePoints);
            } else {
                System.out.println("⚠ Недостаточно очков хода для выстрела! Нужно: " + cost + ", есть: " + movePoints);
            }
        }
    }

    private void loadGreetingSound() {
        try {
            String soundPath;
            if ("AMX40".equals(type)) {
                soundPath = "src/FriendlyPersons/AMX-40/Union/AMX_40.wav";
            } else if ("M53".equals(type)) {
                soundPath = "src/FriendlyPersons/M53/Union/M53.wav";
            } else if ("MS-1".equals(type)) {
                soundPath = "src/FriendlyPersons/MS-1/Union/MS-1.wav";
            } else if ("VK10001P".equals(type)) {
                soundPath = "src/FriendlyPersons/VK-100-01-P/Union/VK-100-01-P.wav";
            } else if ("T1".equals(type)) {
                soundPath = "src/FriendlyPersons/T1/Union/T1.wav";
            } else {
                return; // Неизвестный тип - не загружаем звук
            }

            File soundFile = new File(soundPath);
            if (soundFile.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                greetingSound = AudioSystem.getClip();
                greetingSound.open(audioInputStream);
                System.out.println("✅ Звук для " + name + " успешно загружен: " + soundPath);
            } else {
                System.err.println("❌ Файл звука не найден: " + soundPath);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки звука для " + name + ": " + e.getMessage());
        }
    }

    public boolean heal(int amount) {
        if (health >= maxHealth) return false;
        health = Math.min(maxHealth, health + amount);
        return true;
    }

    // Исправленный метод воспроизведения звука
    public void playGreetingSound() {
        if (greetingSound == null) return;

        if (soundManager != null) {
            // Используем SoundManager для воспроизведения
            soundManager.playCustomClip(greetingSound);
        } else {
            // Fallback - прямое воспроизведение
            try {
                if (greetingSound.isRunning()) greetingSound.stop();
                greetingSound.setFramePosition(0);
                greetingSound.start();
            } catch (Exception e) {
                System.err.println("Ошибка воспроизведения звука: " + e.getMessage());
            }
        }
    }

    // Метод для остановки звука (если нужно)
    public void stopGreetingSound() {
        if (soundManager != null) {
            soundManager.stopVoiceClip();
        } else if (greetingSound != null && greetingSound.isRunning()) {
            greetingSound.stop();
            greetingSound.setFramePosition(0);
        }
    }

    // В FriendlyUnit.java
    public void takeDamage(int damage, GameWorld world) {
        // Проверка на уклонение
        Random rand = new Random();
        if (rand.nextDouble() * 100 < dodgeChance) {
            System.out.println("💨 Уклонение! " + name + " избежал урона!");
            return;
        }

        // Расчёт с учётом брони
        int finalDamage = (int)(damage * (100.0 / (100.0 + armor)));
        health -= finalDamage;

        System.out.println("⚔️ " + name + " получил урон: " + finalDamage + " (броня: " + armor + ")");

        // В FriendlyUnit.java, в методе takeDamage, после health <= 0:
        if (health <= 0) {
            health = 0;
            isAlive = false;
            System.out.println("💀 " + name + " уничтожен!");

            // ===== ДОБАВЬТЕ ЭТИ СТРОКИ =====
            if (world != null) {
                dropAllInventory(world);
                // Отмечаем, что юнит больше не управляем
                isRecruited = false;
            }
            // ================================
        }
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    // Добавьте в класс FriendlyUnit
    public boolean useAmmoFromWeapon() {
        return inventory.useAmmoFromWeapon(burstSize);
    }

    public boolean isWeaponEmpty() {
        return inventory.isWeaponEmpty();
    }

    // Или добавьте метод с учётом режима:
    public boolean canShoot(boolean isAimingMode) {
        int cost = isAimingMode ? aimedShotCost : shotCost;
        return !turnEnded && movePoints >= cost && !inventory.isWeaponEmpty();
    }


    // Добавьте геттер:
    public Inventory getInventory() {
        return inventory;
    }
    public WeaponData getCurrentWeapon() { return currentWeapon; }
    public Caliber getRequiredCaliber() {
        // Используем equippedWeaponData, а не currentWeapon!
        return equippedWeaponData != null ? equippedWeaponData.caliber : Caliber.CALIBER_20MM;
    }
    public double getWeaponCaliber() {
        return equippedWeaponData != null ? equippedWeaponData.weaponCaliber : 0.02;
    }

    public int getReloadCost() {
        return equippedWeaponData != null ? equippedWeaponData.reloadCost : 6;
    }

    public int getBurstSize() {
        return equippedWeaponData != null ? equippedWeaponData.burstSize : 3;
    }
    public int getWeaponDamage() {
        return equippedWeaponData != null ? equippedWeaponData.weaponDamage : 11;
    }
    public double getCritChance() {
        return equippedWeaponData != null ? equippedWeaponData.critChance : 0.09;
    }
    public String getEquippedWeaponId() { return equippedWeaponId; }
    public WeaponData getEquippedWeaponData() { return equippedWeaponData; }

    // Добавьте геттеры и методы:
    public ExperienceSystem getExperienceSystem() { return experienceSystem; }
    public void setExperienceSystem(ExperienceSystem exp) {
        this.experienceSystem = exp;
        // ВАЖНО: применяем бонусы опыта сразу
        updateStatsFromExperience();
    }

    public void addExperience(int amount) {
        if (experienceSystem != null) {
            int oldLevel = experienceSystem.getLevel();
            experienceSystem.addExperience(amount);

            if (experienceSystem.getLevel() > oldLevel) {
                updateStatsFromExperience();
            }
        }
    }

    public int getExperienceLevel() {
        return experienceSystem != null ? experienceSystem.getLevel() : 1;
    }

    public void updateStatsFromExperience() {
        if (experienceSystem == null) return;

        // ===== НИЧЕГО НЕ ДЕЛАЕМ АВТОМАТИЧЕСКИ! =====
        // Бонусы будут применены только через LevelUpBonus.applyBonus()

        System.out.println("📊 " + name + " имеет уровень " + experienceSystem.getLevel() +
                " с " + experienceSystem.getPendingBonuses() + " нераспределёнными бонусами");
    }

    // В начало класса, после других полей
    private int upgradeLevel = 1;          // 1..4
    private String upgradeClass = null;    // null для 1 уровня, затем "PT", "TT", "ST", "LT"

    // Геттеры и сеттеры
    public int getUpgradeLevel() { return upgradeLevel; }
    public String getUpgradeClass() { return upgradeClass; }
    public void setUpgradeClass(String upgradeClass) { this.upgradeClass = upgradeClass; }
    public void setUpgradeLevel(int level) { this.upgradeLevel = level; }

    // Стоимость улучшения до следующего уровня
    public int getUpgradeCost() {
        switch(upgradeLevel) {
            case 1: return 190;
            case 2: return 470;
            case 3: return 1150;
            default: return 0;
        }
    }

    public void applyPendingBonusesFromSave(ExperienceSystem expSystem) {
        if (expSystem == null) return;

        LevelUpBonus bonusSystem = expSystem.getPendingLevelUpBonus();
        if (bonusSystem == null) return;

        // Восстанавливаем применённые ранее бонусы
        for (LevelUpBonus.BonusType bonus : bonusSystem.getSelectedBonuses()) {
            // Применяем бонус, но НЕ тратим оставшиеся (они уже потрачены)
            applyBonusWithoutConsuming(bonus);
        }

        // Очищаем ожидающие бонусы (они уже применены)
        expSystem.clearPendingBonuses();

        System.out.println("🔄 Восстановлены выбранные бонусы для " + name);
    }

    // Добавьте этот метод (сделайте его public)
    public void applyBonusWithoutConsuming(LevelUpBonus.BonusType bonus) {
        switch (bonus) {
            case HEALTH:
                if (this.maxHealth >= getMaxHealthForClass()) {
                    System.out.println("⚠️ Максимальное здоровье уже достигнуто!");
                    return;
                }
                this.maxHealth += 10;
                this.health += 10;
                break;
            case ACCURACY:
                this.weaponAccuracy += 1;
                break;
            case STRENGTH:
                int maxStrength = getMaxStrengthForClass();
                if (this.strength >= maxStrength) {
                    System.out.println("⚠️ Максимальная сила уже достигнута (" + maxStrength + ")!");
                    return;
                }
                this.strength += 1;
                calculateMovePoints();
                break;
            case AGILITY:
                int maxAgility = getMaxAgilityForClass();
                if (this.agility >= maxAgility) {
                    System.out.println("⚠️ Максимальная ловкость уже достигнута (" + maxAgility + ")!");
                    return;
                }
                this.agility += 1;
                calculateMovePoints();
                break;
            case ARMOR:
                int maxArmor = getMaxArmorForClass();
                if (this.armor >= maxArmor) {
                    System.out.println("⚠️ Максимальная броня уже достигнута (" + maxArmor + ")!");
                    return;
                }
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
                int maxReload = getMaxReloadForClass();
                if (this.nimble >= maxReload) {
                    System.out.println("⚠️ Максимальная скорость перезарядки уже достигнута (" + maxReload + ")!");
                    return;
                }
                if (this.reloadBonus < 33) {
                    this.reloadBonus += 1;
                    updateReloadCostFromBonus();
                }
                break;
            case NIMBLE:
                int maxNimble = getMaxNimbleForClass();
                if (this.nimble >= maxNimble) {
                    System.out.println("⚠️ Максимальная проворность уже достигнута (" + maxNimble + ")!");
                    return;
                }
                this.nimble += 1;
                this.dodgeChance = Math.min(20, 5 + this.nimble * 15 / 100);
                break;
        }
    }

    public int getMaxStrengthForClass() {
        if (upgradeClass == null) return 999; // без класса - без ограничений

        switch (upgradeClass) {
            case "PT": return 160;  // ПТ
            case "TT": return 160;  // ТТ
            case "ST": return 100;  // СТ
            case "LT": return 70;   // ЛТ - максимум 70!
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

    // Проверка, можно ли улучшить (достаточно деталей и не максимальный уровень)
    public boolean canUpgrade(int teamParts) {
        return upgradeLevel < 4 && teamParts >= getUpgradeCost();
    }

    // Применить улучшение (выбор класса только при переходе с 1 на 2)
    public void performUpgrade(String chosenClass, int teamParts) {
        if (!canUpgrade(teamParts)) return;

        int cost = getUpgradeCost();

        if (upgradeLevel == 1 && chosenClass != null) {
            upgradeClass = chosenClass;
        }

        int oldLevel = upgradeLevel;
        upgradeLevel++;

        // Применяем бонусы ТОЛЬКО за новый уровень
        applyUpgradeBonusesForNewLevel(oldLevel, upgradeLevel);
    }

    // Новый метод - применяет бонусы только за переход с oldLevel на newLevel
    // Новый метод - применяет бонусы ОДИН РАЗ за новый уровень
    // В FriendlyUnit.java, проверьте, что метод правильно обновляет характеристики:
    // В FriendlyUnit.java, метод applyUpgradeBonusesForNewLevel:

    public void applyUpgradeBonusesForNewLevel(int oldLevel, int newLevel) {
        if (oldLevel >= newLevel) return;

        // Применяем бонусы ОДИН РАЗ
        applyBonusesForSingleLevel();

        // Пересчитываем производные
        this.maxCarryWeight = this.strength * 0.5;
        calculateMovePoints();
        updateReloadCostFromBonus();

        System.out.println("🎉 " + name + " получил бонусы за переход на " + newLevel + " уровень!");
    }

    // Применяет бонусы ТОЛЬКО ДЛЯ ОДНОГО УРОВНЯ (не накапливая)
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

    // В FriendlyUnit.java, исправьте метод getTextureFolder():
    public String getTextureFolder() {
        if (upgradeLevel == 1) {
            return this.type;
        }

        String className = "";
        switch (upgradeClass) {
            case "PT": className = "ПТ"; break;
            case "TT": className = "ТТ"; break;
            case "ST": className = "СТ"; break;
            case "LT": className = "ЛТ"; break;
            default: className = "ТТ";
        }
        return this.type + " (Модернизация " + className + "-" + upgradeLevel + ")";
    }

    public String getPortraitPath() {
        if (upgradeLevel == 1) {
            return "src/PositiveHeroes/ImageOfHeroes/" + this.type + ".png";
        }

        String className = "";
        switch (upgradeClass) {
            case "PT": className = "ПТ"; break;
            case "TT": className = "ТТ"; break;
            case "ST": className = "СТ"; break;
            case "LT": className = "ЛТ"; break;
            default: className = "ТТ";
        }

        return "src/PositiveHeroes/ImageOfHeroes/" + this.type + " (Модернизация " + className + "-" + upgradeLevel + ").png";
    }

    // Применить бонусы за уровень модернизации

    private int getBaseMaxHealth() {
        if ("M53".equals(type)) return 220;
        if ("VK10001P".equals(type)) return 1600;
        if ("AMX40".equals(type)) return 400;
        return 100;  // MS-1
    }

    private int getBaseStrength() {
        if ("M53".equals(type)) return 90;
        if ("VK10001P".equals(type)) return 85;
        if ("AMX40".equals(type)) return 50;
        return 35;  // MS-1
    }

    private int getBaseAgility() {
        if ("M53".equals(type)) return 15;
        if ("VK10001P".equals(type)) return 10;
        if ("AMX40".equals(type)) return 20;
        return 40;  // MS-1
    }

    private int getBaseAccuracy() {
        if ("M53".equals(type)) return 30;
        if ("VK10001P".equals(type)) return 70;
        if ("AMX40".equals(type)) return 10;
        return 40;  // MS-1
    }

    private int getBaseArmor() {
        if ("M53".equals(type)) return 5;
        if ("VK10001P".equals(type)) return 100;
        if ("AMX40".equals(type)) return 50;
        return 15;  // MS-1
    }

    private int getBaseVision() {
        if ("M53".equals(type)) return 20;
        if ("VK10001P".equals(type)) return 10;
        if ("AMX40".equals(type)) return 30;
        return 14;  // MS-1
    }

    private int getBaseNimble() {
        if ("M53".equals(type)) return 0;
        if ("VK10001P".equals(type)) return 0;
        if ("AMX40".equals(type)) return 0;
        return 14;  // MS-1
    }

    // Пример реализации бонусов (можно доработать)

    private void resetToBaseStats() {
        if ("M53".equals(type)) {
            this.maxHealth = 220;
            this.strength = 90;
            this.agility = 15;
            this.accuracy = 30;
            this.armor = 5;
            this.critBonus = 40;
            this.vision = 0;
            this.viewRadius = 10;
            this.reloadBonus = 100;
            this.nimble = 0;
            this.dodgeChance = 5.0;
        } else if ("VK10001P".equals(type)) {  // ← ДОБАВЬТЕ ЭТУ ВЕТКУ
            this.maxHealth = 1600;
            this.strength = 85;
            this.agility = 10;
            this.accuracy = 70;
            this.armor = 100;
            this.critBonus = 50;
            this.vision = 10;
            this.viewRadius = 12;
            this.reloadBonus = 40;
            this.nimble = 0;
            this.dodgeChance = 5.0;
        } else if ("AMX40".equals(type)) {  // ← ДОБАВЬТЕ ЭТУ ВЕТКУ
            this.maxHealth = 400;
            this.strength = 50;
            this.agility = 30;
            this.accuracy = 10;
            this.armor = 50;
            this.critBonus = 12;
            this.vision = 30;
            this.viewRadius = 12;
            this.reloadBonus = 10;
            this.nimble = 0;
            this.dodgeChance = 5.0;
        } else { // MS-1
            this.maxHealth = 100;
            this.strength = 35;
            this.agility = 40;
            this.accuracy = 40;
            this.armor = 15;
            this.critBonus = 15;
            this.vision = 14;
            this.viewRadius = 12;
            this.reloadBonus = 20;
            this.nimble = 14;
            this.dodgeChance = 7.1;
        }
        this.health = Math.min(health, maxHealth);
        // Сброс параметров оружия (сохраняем экипированное оружие)
        if (equippedWeaponData != null) {
            this.burstSize = equippedWeaponData.burstSize;
            this.weaponAccuracy = equippedWeaponData.weaponAccuracy;
            this.weaponCaliber = equippedWeaponData.weaponCaliber;
            this.shotCost = equippedWeaponData.shotCost;
            this.aimedShotCost = equippedWeaponData.aimedShotCost;
            this.reloadCost = equippedWeaponData.reloadCost;
            this.weaponDamage = equippedWeaponData.weaponDamage;
            this.critChance = equippedWeaponData.critChance;
            this.originalReloadCost = equippedWeaponData.reloadCost;
            updateReloadCostFromBonus();
        } else {
            // fallback на MS-1
            this.burstSize = 3;
            this.weaponDamage = 11;
            this.critChance = 0.09;
            this.weaponAccuracy = 30;
            this.baseAccuracy = 30;
            this.reloadCost = 6;
        }
        calculateMovePoints();
    }

    public int getCurrentAmmoCount() {
        AmmoItem ammo = inventory.getCurrentAmmo();
        return ammo != null ? ammo.getCount() : 0;
    }

    public boolean isOverweight() {
        double inventoryWeight = inventory.getTotalWeight();
        double equipmentWeight = getEquipmentWeight();
        double currentWeight = inventoryWeight + equipmentWeight;

        return currentWeight > maxCarryWeight;
    }

    public AmmoItem getCurrentAmmo() {
        return inventory.getCurrentAmmo();
    }

}