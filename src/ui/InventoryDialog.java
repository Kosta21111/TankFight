package ui;

import entities.FriendlyUnit;
import entities.PlayerTank;
import entities.Door;
import world.*;
import inventory.*;
import inventory.Item;
import inventory.MedicalItem;
import inventory.Caliber;

import combat.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

public class InventoryDialog extends JDialog {
    private Object unit;

    private JLabel weightLabel;
    private PlayerTank player;
    private Inventory inventory;
    private BufferedImage portrait;
    private String unitName;
    private int maxHealth;
    private int currentHealth;
    private int maxMovePoints;
    private int currentMovePoints;

    private static ImageIcon weaponIcon = null;

    private JPanel inventoryGrid;
    private JLabel healthLabel;
    private JLabel silverLabel;
    private JLabel infoLabel;
    private JLabel weaponNameLabel;
    private JLabel weaponImageLabel;
    private JLabel ammoLabel;
    private JButton reloadButton;
    private int selectedX = -1, selectedY = -1;
    private boolean isInitialized = false;

    // ===== НОВЫЕ ПОЛЯ ДЛЯ ОПЫТА =====
    private JLabel levelLabel;
    private JLabel expLabel;
    private JProgressBar expProgressBar;

    private static ImageIcon basicShellIcon = null;
    private static ImageIcon improvedShellIcon = null;
    private static ImageIcon thirtySevenMMShellIcon = null;
    private static ImageIcon thirtyMMShellIcon = null;
    private static ImageIcon twentyFiveMMShellIcon = null;
    private static ImageIcon fortyFiveMMShellIcon = null;
    private static ImageIcon fortySevenMMShellIcon = null;
    private static ImageIcon OneHundredFiveMMShellIcon = null;

    // Иконки для оружия

    private GamePanel gamePanel;
    private GameWorld world;  // если нет, добавьте

    private java.util.Map<String, JComponent> slotComponents = new java.util.HashMap<>();

    private Map<Door.DoorColor, ImageIcon> keyIcons = new HashMap<>();  // ← ДОБАВЬТЕ

    // Дополнительные метки для характеристик
    private JLabel strengthLabel, agilityLabel, accuracyLabel;
    private JLabel armorLabel, critLabel, visionLabel, reloadLabel, dodgeLabel;
    private JLabel movePointsLabel, moveCostLabel;

    // Конструктор для игрока
    public InventoryDialog(JFrame parent, PlayerTank player, BufferedImage portrait, GamePanel gamePanel) {
        super(parent, "Инвентарь - Leichttraktor", true);
        this.unit = player;
        this.player = player;
        this.inventory = player.getInventory();
        this.portrait = portrait;
        this.unitName = "Leichttraktor";
        this.maxHealth = player.maxHealth;
        this.currentHealth = player.health;
        this.maxMovePoints = player.maxMovePoints;
        this.currentMovePoints = player.movePoints;
        this.gamePanel = gamePanel;
        this.world = gamePanel.getWorld();  // добавьте геттер в GamePanel
        initUI();
    }

    public InventoryDialog(JFrame parent, FriendlyUnit friendly, BufferedImage portrait, PlayerTank player, GamePanel gamePanel) {
        super(parent, "Инвентарь - " + friendly.name, true);
        this.unit = friendly;
        this.player = player;
        this.inventory = friendly.getInventory();
        this.portrait = portrait;
        this.unitName = friendly.name;
        this.maxHealth = friendly.maxHealth;
        this.currentHealth = friendly.health;
        this.maxMovePoints = friendly.maxMovePoints;
        this.currentMovePoints = friendly.movePoints;
        this.gamePanel = gamePanel;
        this.world = gamePanel.getWorld();
        initUI();
    }

    private Item.ItemType getWeaponTypeFromEquipped(Object weaponData) {
        if (weaponData instanceof PlayerTank.WeaponData) {
            PlayerTank.WeaponData w = (PlayerTank.WeaponData) weaponData;
            if ("breda".equals(w.weaponId)) return Item.ItemType.WEAPON;
            if ("45mm".equals(w.weaponId)) return Item.ItemType.WEAPON_45MM;
            if ("25mm".equals(w.weaponId)) return Item.ItemType.WEAPON_25MM;
            if ("47mm_french".equals(w.weaponId)) return Item.ItemType.WEAPON_47MM_FRENCH;
            if ("47mm_italian".equals(w.weaponId)) return Item.ItemType.WEAPON_47MM_ITALIAN;
            if ("8mm".equals(w.weaponId)) return Item.ItemType.WEAPON_8MM;
            if ("13mm_japan".equals(w.weaponId)) return Item.ItemType.WEAPON_13MM_JAPAN;
            if ("13mm_french".equals(w.weaponId)) return Item.ItemType.WEAPON_13MM_FRENCH;
            if ("30mm".equals(w.weaponId)) return Item.ItemType.WEAPON_30MM;
            if ("37mm_italian".equals(w.weaponId)) return Item.ItemType.WEAPON_37MM_ITALIAN;
            if ("37mm_american".equals(w.weaponId)) return Item.ItemType.WEAPON_37MM_AMERICAN;
            if ("37mm_sweden".equals(w.weaponId)) return Item.ItemType.WEAPON_37MM_SWEDEN;
            if ("76mm".equals(w.weaponId)) return Item.ItemType.WEAPON_76MM;
            if ("105mm".equals(w.weaponId)) return Item.ItemType.WEAPON_105MM;
            if ("128mm".equals(w.weaponId)) return Item.ItemType.WEAPON_128MM;
        } else if (weaponData instanceof FriendlyUnit.WeaponData) {
            FriendlyUnit.WeaponData w = (FriendlyUnit.WeaponData) weaponData;
            if ("breda".equals(w.weaponId)) return Item.ItemType.WEAPON;
            if ("45mm".equals(w.weaponId)) return Item.ItemType.WEAPON_45MM;
            if ("25mm".equals(w.weaponId)) return Item.ItemType.WEAPON_25MM;
            if ("47mm_french".equals(w.weaponId)) return Item.ItemType.WEAPON_47MM_FRENCH;
            if ("47mm_italian".equals(w.weaponId)) return Item.ItemType.WEAPON_47MM_ITALIAN;
            if ("8mm".equals(w.weaponId)) return Item.ItemType.WEAPON_8MM;
            if ("13mm_japan".equals(w.weaponId)) return Item.ItemType.WEAPON_13MM_JAPAN;
            if ("13mm_french".equals(w.weaponId)) return Item.ItemType.WEAPON_13MM_FRENCH;
            if ("30mm".equals(w.weaponId)) return Item.ItemType.WEAPON_30MM;
            if ("37mm_italian".equals(w.weaponId)) return Item.ItemType.WEAPON_37MM_ITALIAN;
            if ("37mm_american".equals(w.weaponId)) return Item.ItemType.WEAPON_37MM_AMERICAN;
            if ("37mm_sweden".equals(w.weaponId)) return Item.ItemType.WEAPON_37MM_SWEDEN;
            if ("105mm".equals(w.weaponId)) return Item.ItemType.WEAPON_105MM;
            if ("76mm".equals(w.weaponId)) return Item.ItemType.WEAPON_76MM;
            if ("128mm".equals(w.weaponId)) return Item.ItemType.WEAPON_128MM;
        }
        return Item.ItemType.WEAPON;
    }

    private void initUI() {
        loadImages();
        setLayout(new BorderLayout(10, 10));
        setSize(800, 700);
        setLocationRelativeTo(getParent());

        add(createTopPanel(), BorderLayout.NORTH);
        add(createWeaponPanel(), BorderLayout.WEST);
        add(createInventoryPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> refreshWeaponPanel());
    }

    private void loadImages() {
        try {
            String shellsPath = "src/ObjectsOfInventory/Shells/";
            String treatmentPath = "src/ObjectsOfInventory/TreatmentAndRepair/";
            String keysPath = "src/ObjectsOfInventory/Key/";  // ← ДОБАВЬТЕ

            // 20mm обычные
            File basicFile = new File(shellsPath + "20mmShell_based.png");
            if (basicFile.exists()) {
                Image originalImage = ImageIO.read(basicFile);
                basicShellIcon = new ImageIcon(originalImage);
            }

            // 20mm улучшенные
            File improved20mmFile = new File(shellsPath + "20mmShell_improved.png");
            if (improved20mmFile.exists()) {
                Image originalImage = ImageIO.read(improved20mmFile);
                improvedShellIcon = new ImageIcon(originalImage);
            }

            // 25mm снаряды
            File shell25mmFile = new File(shellsPath + "25mmShell_based.png");
            if (shell25mmFile.exists()) {
                Image originalImage = ImageIO.read(shell25mmFile);
                twentyFiveMMShellIcon = new ImageIcon(originalImage);
            }

            // 45mm снаряды
            File shell45mmFile = new File(shellsPath + "45mmShell_based.png");
            if (shell45mmFile.exists()) {
                Image originalImage = ImageIO.read(shell45mmFile);
                fortyFiveMMShellIcon = new ImageIcon(originalImage);
            }

            // 47mm снаряды
            File shell47mmFile = new File(shellsPath + "47mmShell_based.png");
            if (shell47mmFile.exists()) {
                Image originalImage = ImageIO.read(shell47mmFile);
                fortySevenMMShellIcon = new ImageIcon(originalImage);
            }

            File shell37mmFile = new File(shellsPath + "37mmShell_based.png");
            if (shell37mmFile.exists()) {
                Image originalImage = ImageIO.read(shell37mmFile);
                thirtySevenMMShellIcon = new ImageIcon(originalImage);
            }

            // 30mm снаряды (опционально)
            File shell30mmFile = new File(shellsPath + "30mmShell_based.png");
            if (shell30mmFile.exists()) {
                Image originalImage = ImageIO.read(shell30mmFile);
                thirtyMMShellIcon = new ImageIcon(originalImage);
            }

            // 76mm снаряды (опционально)
            File shell76mmFile = new File(shellsPath + "76mmShell_based.png");
            if (shell76mmFile.exists()) {
                // можно сохранить в отдельную переменную
            }

            // 105mm снаряды (опционально)
            File shell105mmFile = new File(shellsPath + "105mmShell_based.png");
            if (shell105mmFile.exists()) {
                Image originalImage = ImageIO.read(shell105mmFile);
                OneHundredFiveMMShellIcon = new ImageIcon(originalImage);
            }

            File shell128mmFile = new File(shellsPath + "128mmShell_based.png");
            if (shell128mmFile.exists()) {
                // можно сохранить в отдельную переменную
            }

            String weaponPath = "src/ObjectsOfInventory/Weapon/2 cm Breda (I).png";
            File weaponFile = new File(weaponPath);
            if (weaponFile.exists()) {
                Image originalImage = ImageIO.read(weaponFile);
                weaponIcon = new ImageIcon(originalImage);
            }

            // ===== КЛЮЧИ =====
            for (Door.DoorColor color : Door.DoorColor.values()) {
                String fileName = keysPath + color.name() + "Key.png";
                File keyFile = new File(fileName);
                if (keyFile.exists()) {
                    Image img = ImageIO.read(keyFile);
                    keyIcons.put(color, new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
                    System.out.println("✅ Загружена иконка ключа: " + color.name());
                }
            }

        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображений: " + e.getMessage());
        }
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(30, 30, 40));

        // Левая панель с портретом
        JPanel portraitPanel = new JPanel(new BorderLayout());
        portraitPanel.setBackground(new Color(30, 30, 40));

        JLabel portraitLabel = new JLabel();
        if (portrait != null) {
            Image scaled = portrait.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            portraitLabel.setIcon(new ImageIcon(scaled));
        } else {
            portraitLabel.setText("🚀");
            portraitLabel.setFont(new Font("Arial", Font.BOLD, 48));
            portraitLabel.setForeground(Color.WHITE);
        }
        portraitPanel.add(portraitLabel, BorderLayout.CENTER);
        panel.add(portraitPanel, BorderLayout.WEST);

        // Центральная панель с информацией (2 колонки для всех характеристик)
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        infoPanel.setBackground(new Color(30, 30, 40));
        infoPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        JLabel nameLabel = new JLabel(unitName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(new Color(255, 215, 0));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Добавляем имя на всю ширину
        JPanel namePanel = new JPanel(new GridLayout(1, 2));
        namePanel.setBackground(new Color(30, 30, 40));
        namePanel.add(nameLabel);
        namePanel.add(new JLabel("")); // пустышка для выравнивания

        healthLabel = new JLabel("❤️ Здоровье: " + currentHealth + "/" + maxHealth);
        healthLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        healthLabel.setForeground(Color.WHITE);

        // ===== ВСЕ ХАРАКТЕРИСТИКИ =====
        JLabel strengthLabel, agilityLabel, accuracyLabel;
        JLabel armorLabel, critLabel, visionLabel, reloadLabel, dodgeLabel;
        JLabel movePointsLabel, moveCostLabel;

        if (unit instanceof PlayerTank) {
            PlayerTank p = (PlayerTank) unit;
            strengthLabel = new JLabel("💪 Сила: " + p.strength);
            agilityLabel = new JLabel("🦶 Ловкость: " + p.agility);
            accuracyLabel = new JLabel("🎯 Точность: " + p.weaponAccuracy);
            armorLabel = new JLabel("🛡️ Броня: " + p.armor);
            critLabel = new JLabel("⚡ Крит. шанс: +" + p.critBonus + "%");
            visionLabel = new JLabel("👁️ Зрение: " + p.viewRadius + " кл.");
            reloadLabel = new JLabel("🔄 Перезарядка: " + p.reloadCost + " о.х.");
            dodgeLabel = new JLabel("💨 Уклонение: " + String.format("%.1f", p.dodgeChance) + "%");
            movePointsLabel = new JLabel("⭐ Очки хода: " + p.movePoints + "/" + p.maxMovePoints);
            moveCostLabel = new JLabel("🚶 Стоимость шага: " + p.moveCost + " о.х.");
        } else {
            FriendlyUnit f = (FriendlyUnit) unit;
            strengthLabel = new JLabel("💪 Сила: " + f.strength);
            agilityLabel = new JLabel("🦶 Ловкость: " + f.agility);
            accuracyLabel = new JLabel("🎯 Точность: " + f.weaponAccuracy);
            armorLabel = new JLabel("🛡️ Броня: " + f.armor);
            critLabel = new JLabel("⚡ Крит. шанс: +" + f.critBonus + "%");
            visionLabel = new JLabel("👁️ Зрение: " + f.viewRadius + " кл.");
            reloadLabel = new JLabel("🔄 Перезарядка: " + f.reloadCost + " о.х.");
            dodgeLabel = new JLabel("💨 Уклонение: " + String.format("%.1f", f.dodgeChance) + "%");
            movePointsLabel = new JLabel("⭐ Очки хода: " + f.movePoints + "/" + f.maxMovePoints);
            moveCostLabel = new JLabel("🚶 Стоимость шага: " + f.moveCost + " о.х.");
        }

        strengthLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        strengthLabel.setForeground(Color.WHITE);
        agilityLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        agilityLabel.setForeground(Color.WHITE);
        accuracyLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        accuracyLabel.setForeground(Color.WHITE);
        armorLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        armorLabel.setForeground(new Color(200, 200, 100));
        critLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        critLabel.setForeground(new Color(255, 150, 50));
        visionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        visionLabel.setForeground(new Color(100, 200, 255));
        reloadLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        reloadLabel.setForeground(new Color(100, 255, 200));
        dodgeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        dodgeLabel.setForeground(new Color(200, 200, 255));
        movePointsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        movePointsLabel.setForeground(new Color(255, 215, 0));
        moveCostLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        moveCostLabel.setForeground(Color.CYAN);

        // ===== УРОВЕНЬ И ОПЫТ =====
        ExperienceSystem expSystem = null;
        if (unit instanceof PlayerTank) {
            expSystem = ((PlayerTank) unit).getExperienceSystem();
        } else if (unit instanceof FriendlyUnit) {
            expSystem = ((FriendlyUnit) unit).getExperienceSystem();
        }

        JLabel levelLabel;
        JLabel expLabel;
        JProgressBar expProgressBar;

        if (expSystem != null) {
            levelLabel = new JLabel("📈 Уровень: " + expSystem.getLevel());
            levelLabel.setFont(new Font("Arial", Font.BOLD, 12));
            levelLabel.setForeground(new Color(100, 200, 255));

            expLabel = new JLabel("✨ Опыт: " + expSystem.getExperience() + "/" + expSystem.getExperienceForNextLevel());
            expLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            expLabel.setForeground(Color.LIGHT_GRAY);

            expProgressBar = new JProgressBar(0, expSystem.getExperienceForNextLevel());
            expProgressBar.setValue(expSystem.getExperience());
            expProgressBar.setStringPainted(true);
            expProgressBar.setForeground(new Color(100, 200, 255));
            expProgressBar.setBackground(new Color(60, 60, 70));
            expProgressBar.setPreferredSize(new Dimension(150, 14));
            expProgressBar.setFont(new Font("Arial", Font.PLAIN, 8));
        } else {
            levelLabel = new JLabel("📈 Уровень: 1");
            levelLabel.setFont(new Font("Arial", Font.BOLD, 12));
            levelLabel.setForeground(new Color(100, 200, 255));

            expLabel = new JLabel("✨ Опыт: 0/100");
            expLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            expLabel.setForeground(Color.LIGHT_GRAY);

            expProgressBar = new JProgressBar(0, 100);
            expProgressBar.setValue(0);
            expProgressBar.setStringPainted(true);
            expProgressBar.setForeground(new Color(100, 200, 255));
            expProgressBar.setBackground(new Color(60, 60, 70));
            expProgressBar.setPreferredSize(new Dimension(150, 14));
        }

        silverLabel = new JLabel("💰 Серебро: " + getTeamSilver());
        silverLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        silverLabel.setForeground(new Color(255, 215, 0));

        // Новый лейбл для деталей
        JLabel partsLabel = new JLabel("⚙️ Детали: " + getTeamParts());
        partsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        partsLabel.setForeground(new Color(200, 200, 255));

        JLabel upgradeLabel = new JLabel("🔧 Модернизация: " + getUpgradeText());
        upgradeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        upgradeLabel.setForeground(new Color(200, 200, 100));

        weightLabel = new JLabel("⚖️ Вес: " + String.format("%.1f", getCurrentWeight()) + "/" + getMaxCarryWeight());
        weightLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        weightLabel.setForeground(new Color(200, 200, 100));




        infoPanel.add(upgradeLabel);
        infoPanel.add(healthLabel);
        infoPanel.add(movePointsLabel);
        infoPanel.add(strengthLabel);
        infoPanel.add(agilityLabel);
        infoPanel.add(accuracyLabel);
        infoPanel.add(moveCostLabel);
        infoPanel.add(armorLabel);
        infoPanel.add(critLabel);
        infoPanel.add(visionLabel);
        infoPanel.add(reloadLabel);
        infoPanel.add(dodgeLabel);
        infoPanel.add(levelLabel);
        infoPanel.add(expLabel);
        infoPanel.add(silverLabel);
        infoPanel.add(partsLabel);
        infoPanel.add(weightLabel);

        // Полоска опыта на всю ширину
        JPanel expPanel = new JPanel(new BorderLayout());
        expPanel.setBackground(new Color(30, 30, 40));
        expPanel.add(expProgressBar, BorderLayout.CENTER);

        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(expPanel, BorderLayout.SOUTH);

        // Сохраняем ссылки на обновляемые метки
        this.healthLabel = healthLabel;
        this.silverLabel = silverLabel;

        // Сохраняем дополнительные метки как поля класса (нужно добавить в класс)
        this.strengthLabel = strengthLabel;
        this.agilityLabel = agilityLabel;
        this.accuracyLabel = accuracyLabel;
        this.armorLabel = armorLabel;
        this.critLabel = critLabel;
        this.visionLabel = visionLabel;
        this.reloadLabel = reloadLabel;
        this.dodgeLabel = dodgeLabel;
        this.movePointsLabel = movePointsLabel;
        this.moveCostLabel = moveCostLabel;
        this.levelLabel = levelLabel;
        this.expLabel = expLabel;
        this.expProgressBar = expProgressBar;

        return panel;
    }

    private double getMaxCarryWeight() {
        if (unit instanceof PlayerTank) {
            return ((PlayerTank) unit).maxCarryWeight;
        } else if (unit instanceof FriendlyUnit) {
            return ((FriendlyUnit) unit).maxCarryWeight;
        }
        return 500.0;
    }

    private String getUpgradeText() {
        if (unit instanceof PlayerTank) {
            PlayerTank p = (PlayerTank) unit;
            int lvl = p.getUpgradeLevel();
            String cls = p.getUpgradeClass();
            if (lvl == 1) return "1 уровень (бесклассовый)";
            else return lvl + " уровень (" + cls + ")";
        } else if (unit instanceof FriendlyUnit) {
            FriendlyUnit f = (FriendlyUnit) unit;
            int lvl = f.getUpgradeLevel();
            String cls = f.getUpgradeClass();
            // ===== ДОБАВЬТЕ ПРОВЕРКУ ДЛЯ VK10001P =====
            if ("VK10001P".equals(f.type) || "AMX40".equals(f.type)) {
                return lvl + " уровень (" + cls + ")";  // Уже ТТ-4
            }
            if (lvl == 1) return "1 уровень (бесклассовый)";
            else return lvl + " уровень (" + cls + ")";
        }
        return "1 уровень (бесклассовый)";
    }

    private void dropItem(Item.ItemType type, int amount) {
        // Проверяем, есть ли предмет в инвентаре
        int available = inventory.getItemCount(type);
        if (available < amount) {
            JOptionPane.showMessageDialog(this, "Недостаточно предметов для выброса!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Удаляем предмет из инвентаря
        int removed = 0;
        for (int i = 0; i < amount; i++) {
            if (inventory.useItem(type)) {
                removed++;
            }
        }

        if (removed > 0) {
            // Создаём предмет для выброса
            Item itemToDrop = new Item(type, removed);

            // Выбрасываем НА ТЕКУЩУЮ КЛЕТКУ
            int unitX, unitY;
            if (unit instanceof PlayerTank) {
                unitX = ((PlayerTank) unit).gridX;
                unitY = ((PlayerTank) unit).gridY;
            } else {
                unitX = ((FriendlyUnit) unit).gridX;
                unitY = ((FriendlyUnit) unit).gridY;
            }

            if (world.dropItemOnCurrentCell(unitX, unitY, itemToDrop)) {
                String unitName = (unit instanceof PlayerTank) ? "Leichttraktor" : ((FriendlyUnit) unit).name;
                JOptionPane.showMessageDialog(this,
                        "🗑 " + unitName + " выбросил " + removed + "x " + type.name() + "!\n" +
                                "Предмет упал на землю под ноги.",
                        "Предмет выброшен", JOptionPane.INFORMATION_MESSAGE);

                updateDisplay();
            } else {
                // Возвращаем предмет обратно в инвентарь
                for (int i = 0; i < removed; i++) {
                    inventory.addItem(type, 1);
                }
                JOptionPane.showMessageDialog(this,
                        "❌ Не удалось выбросить предмет!",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void dropAmmo(Caliber caliber, int amount) {
        // Проверяем, есть ли снаряды в инвентаре
        int available = inventory.getAmmoCount(caliber);
        if (available < amount) {
            JOptionPane.showMessageDialog(this, "Недостаточно снарядов для выброса!", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Создаём предмет для выброса
        AmmoItem ammoToDrop = new AmmoItem(caliber, amount);

        // Удаляем снаряды из инвентаря
        if (inventory.removeAmmoByCaliber(caliber, amount)) {
            // Выбрасываем НА ТЕКУЩУЮ КЛЕТКУ
            int unitX, unitY;
            if (unit instanceof PlayerTank) {
                unitX = ((PlayerTank) unit).gridX;
                unitY = ((PlayerTank) unit).gridY;
            } else {
                unitX = ((FriendlyUnit) unit).gridX;
                unitY = ((FriendlyUnit) unit).gridY;
            }

            if (world.dropItemOnCurrentCell(unitX, unitY, ammoToDrop)) {
                String unitName = (unit instanceof PlayerTank) ? "Leichttraktor" : ((FriendlyUnit) unit).name;
                JOptionPane.showMessageDialog(this,
                        "🗑 " + unitName + " выбросил " + amount + " " + caliber.name + " снарядов!\n" +
                                "Предмет упал на землю под ноги.",
                        "Предмет выброшен", JOptionPane.INFORMATION_MESSAGE);

                updateDisplay();
            } else {
                // Возвращаем снаряды обратно в инвентарь
                AmmoItem restoreAmmo = new AmmoItem(caliber, amount);
                inventory.addAmmoItem(restoreAmmo);
                JOptionPane.showMessageDialog(this,
                        "❌ Не удалось выбросить предмет!",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private JPanel createWeaponPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 20, 30));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                "⚔️ ОСНОВНОЕ ОРУЖИЕ",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12),
                new Color(255, 215, 0)
        ));
        panel.setPreferredSize(new Dimension(180, 0));

        JPanel weaponImagePanel = new JPanel(new BorderLayout());
        weaponImagePanel.setBackground(new Color(30, 30, 40));
        weaponImagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        weaponImageLabel = new JLabel();
        weaponImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel weaponInfoPanel = new JPanel();
        weaponInfoPanel.setLayout(new BoxLayout(weaponInfoPanel, BoxLayout.Y_AXIS));
        weaponInfoPanel.setBackground(new Color(30, 30, 40));
        weaponInfoPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        weaponNameLabel = new JLabel();
        weaponNameLabel.setForeground(new Color(255, 215, 0));
        weaponNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        weaponImagePanel.add(weaponImageLabel, BorderLayout.CENTER);

        ammoLabel = new JLabel("СНАРЯДЫ: " + inventory.getCurrentAmmoCount() + "/" + inventory.getMaxAmmo());
        ammoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        ammoLabel.setForeground(Color.WHITE);
        ammoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel reloadCostLabel = new JLabel("⚡ Стоимость перезарядки: " + inventory.getReloadCost() + " о.х.");
        reloadCostLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        reloadCostLabel.setForeground(Color.CYAN);
        reloadCostLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel ammoBarPanel = new JPanel();
        ammoBarPanel.setBackground(new Color(30, 30, 40));
        ammoBarPanel.setLayout(new BoxLayout(ammoBarPanel, BoxLayout.Y_AXIS));
        ammoBarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar ammoBar = new JProgressBar(0, inventory.getMaxAmmo());
        ammoBar.setValue(inventory.getCurrentAmmoCount());
        ammoBar.setStringPainted(true);
        ammoBar.setForeground(new Color(0, 150, 200));
        ammoBar.setBackground(new Color(60, 60, 70));
        ammoBar.setPreferredSize(new Dimension(140, 20));
        ammoBarPanel.add(ammoBar);

        reloadButton = new JButton("🔄 ПЕРЕЗАРЯДИТЬ");
        reloadButton.setFont(new Font("Arial", Font.BOLD, 11));
        reloadButton.setBackground(new Color(0, 100, 200));
        reloadButton.setForeground(Color.WHITE);
        reloadButton.setFocusPainted(false);
        reloadButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        updateReloadButtonState();
        reloadButton.addActionListener(e -> reloadWeapon(ammoBar));

        weaponInfoPanel.add(weaponNameLabel);
        weaponInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        weaponInfoPanel.add(ammoLabel);
        weaponInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        weaponInfoPanel.add(ammoBarPanel);
        weaponInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        weaponInfoPanel.add(reloadCostLabel);
        weaponInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        weaponInfoPanel.add(reloadButton);

        panel.add(weaponImagePanel, BorderLayout.NORTH);
        panel.add(weaponInfoPanel, BorderLayout.CENTER);

        return panel;
    }

    private void updateReloadButtonState() {
        if (inventory.isWeaponFull()) {
            reloadButton.setEnabled(false);
            reloadButton.setBackground(new Color(80, 80, 80));
            reloadButton.setToolTipText("Оружие полностью заряжено");
        } else {
            reloadButton.setEnabled(true);
            reloadButton.setBackground(new Color(0, 100, 200));
            reloadButton.setToolTipText("Перезарядить орудие");
        }
    }

    private void reloadWeapon(JProgressBar ammoBar) {
        int currentMovePoints = 0;
        int reloadCost = 0;
        Caliber requiredCaliber;

        if (unit instanceof PlayerTank) {
            PlayerTank playerTank = (PlayerTank) unit;
            currentMovePoints = playerTank.movePoints;
            requiredCaliber = playerTank.getRequiredCaliber();
            reloadCost = inventory.getReloadCost();
        } else {
            FriendlyUnit friendly = (FriendlyUnit) unit;
            currentMovePoints = friendly.movePoints;
            requiredCaliber = friendly.getRequiredCaliber();
            reloadCost = friendly.getReloadCost();
        }

        if (currentMovePoints < reloadCost) {
            JOptionPane.showMessageDialog(this,
                    "❌ НЕДОСТАТОЧНО ОЧКОВ ХОДА!\n\nНужно: " + reloadCost + " очков хода\n" +
                            "У вас: " + currentMovePoints + " очков хода",
                    "Перезарядка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ammoAvailable = inventory.getAmmoCount(requiredCaliber);
        if (ammoAvailable == 0) {
            JOptionPane.showMessageDialog(this,
                    "❌ НЕТ СНАРЯДОВ КАЛИБРА " + requiredCaliber.name.toUpperCase() + " В ИНВЕНТАРЕ!",
                    "Перезарядка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (unit instanceof PlayerTank) {
            ((PlayerTank) unit).movePoints -= reloadCost;
        } else {
            ((FriendlyUnit) unit).movePoints -= reloadCost;
        }

        int reloaded = inventory.reloadWeapon(requiredCaliber);
        if (reloaded > 0) {
            ammoLabel.setText("СНАРЯДЫ: " + inventory.getCurrentAmmoCount() + "/" + inventory.getMaxAmmo());
            ammoBar.setValue(inventory.getCurrentAmmoCount());

            if (inventory.getCurrentAmmoCount() == 0) {
                ammoBar.setForeground(new Color(200, 0, 0));
            } else if (inventory.getCurrentAmmoCount() <= inventory.getMaxAmmo() / 3) {
                ammoBar.setForeground(new Color(255, 100, 0));
            } else {
                ammoBar.setForeground(new Color(0, 150, 200));
            }

            updateReloadButtonState();
            updateDisplay();
            JOptionPane.showMessageDialog(this,
                    "✅ ОРУЖИЕ ПЕРЕЗАРЯЖЕНО!\n\nЗаряжено снарядов: " + reloaded + "\n" +
                            "Калибр: " + requiredCaliber.name + "\n" +
                            "Теперь в оружии: " + inventory.getCurrentAmmo() + "/" + inventory.getMaxAmmo(),
                    "Перезарядка", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createInventoryPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(20, 20, 30));
        mainPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 150), 2),
                "📦 ИНВЕНТАРЬ (8x5)",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(255, 215, 0)
        ));

        inventoryGrid = new JPanel(new GridBagLayout());
        inventoryGrid.setBackground(new Color(20, 20, 30));
        inventoryGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        mainPanel.add(inventoryGrid, BorderLayout.CENTER);

        infoLabel = new JLabel("Кликните по предмету, чтобы использовать");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(Color.LIGHT_GRAY);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.add(infoLabel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(new Color(30, 30, 40));

        JButton sortButton = new JButton("📦 Сортировать");
        sortButton.addActionListener(e -> updateDisplay());

        // ===== НОВАЯ КНОПКА ОБМЕНА =====
        JButton tradeButton = new JButton("🔄 ОБМЕН С СОЮЗНИКОМ");
        tradeButton.addActionListener(e -> openOtherInventory());

        JButton upgradeButton = new JButton("🔧 МОДЕРНИЗАЦИЯ");
        upgradeButton.addActionListener(e -> showUpgradeDialog());



        JButton closeButton = new JButton("✖ Закрыть");
        closeButton.addActionListener(e -> dispose());



        panel.add(tradeButton);
        panel.add(upgradeButton);
        panel.add(sortButton);
        panel.add(closeButton);

        return panel;
    }

    private void showUpgradeDialog() {
        int currentLevel = -1;
        String currentClass = null;
        boolean isPlayer = (unit instanceof PlayerTank);

        if (isPlayer) {
            PlayerTank p = (PlayerTank) unit;
            currentLevel = p.getUpgradeLevel();
            currentClass = p.getUpgradeClass();
        } else {
            FriendlyUnit f = (FriendlyUnit) unit;
            currentLevel = f.getUpgradeLevel();
            currentClass = f.getUpgradeClass();
        }

        if (currentLevel >= 4) {
            JOptionPane.showMessageDialog(this, "Максимальный уровень модернизации достигнут!", "Информация", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int cost = (currentLevel == 1) ? 190 : (currentLevel == 2) ? 470 : 1150;
        int teamParts = player.getParts();

        if (teamParts < cost) {
            JOptionPane.showMessageDialog(this, "Недостаточно деталей! Нужно: " + cost + ", есть: " + teamParts, "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Если текущий уровень 1 и класс не выбран – просим выбрать класс
        if (currentLevel == 1 && currentClass == null) {
            String[] classes = {"ПТ (противотанковая)", "ТТ (тяжёлый)", "СТ (средний)", "ЛТ (лёгкий)"};
            String classChoice = (String) JOptionPane.showInputDialog(this,
                    "Выберите направление модернизации для " + unitName + ":\n\n" +
                            "ПТ: высокий урон, низкая подвижность\n" +
                            "ТТ: высокая броня и здоровье\n" +
                            "СТ: сбалансированные характеристики\n" +
                            "ЛТ: высокая скорость и обзор",
                    "Выбор класса",
                    JOptionPane.QUESTION_MESSAGE,
                    null, classes, classes[0]);
            if (classChoice == null) return;

            String classCode = classChoice.substring(0, 2);
            String engClass;
            switch(classCode) {
                case "ПТ": engClass = "PT"; break;
                case "ТТ": engClass = "TT"; break;
                case "СТ": engClass = "ST"; break;
                case "ЛТ": engClass = "LT"; break;
                default: engClass = "ST";
            }

            if (isPlayer) {
                PlayerTank p = (PlayerTank) unit;
                p.performUpgrade(engClass, teamParts);
                player.removeParts(cost);

                // ===== ОБНОВЛЯЕМ ТЕКСТУРЫ И ПОРТРЕТ =====
                if (gamePanel != null) {
                    gamePanel.updatePlayerTextures();
                    gamePanel.updateHeroPortrait();
                }

                JOptionPane.showMessageDialog(this, unitName + " модернизирован до 2 уровня (" + classChoice + ")!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } else {
                FriendlyUnit f = (FriendlyUnit) unit;
                f.performUpgrade(engClass, teamParts);
                player.removeParts(cost);

                if (gamePanel != null) {
                    gamePanel.loadFriendlyTextures(f);
                    gamePanel.updateFriendlyPortrait(f);
                    gamePanel.syncRecruitedUnits();
                    gamePanel.repaint();
                }

                JOptionPane.showMessageDialog(this, unitName + " модернизирован до 2 уровня (" + classChoice + ")!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            }
            updateDisplay();
            return;
        }

        // Для уровней 2->3 или 3->4
        int confirm = JOptionPane.showConfirmDialog(this,
                "Модернизировать " + unitName + " до " + (currentLevel + 1) + " уровня?\nСтоимость: " + cost + " деталей",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (isPlayer) {
                PlayerTank p = (PlayerTank) unit;

                // ===== ВАЖНО: передаем null, но класс уже выбран =====
                p.performUpgrade(null, teamParts);
                player.removeParts(cost);

                // ===== ОБНОВЛЯЕМ ТЕКСТУРЫ И ПОРТРЕТ =====
                if (gamePanel != null) {
                    gamePanel.updatePlayerTextures();
                    gamePanel.updateHeroPortrait();
                }

                JOptionPane.showMessageDialog(this, unitName + " модернизирован до " + (currentLevel + 1) + " уровня!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } else {
                FriendlyUnit f = (FriendlyUnit) unit;
                f.performUpgrade(null, teamParts);
                player.removeParts(cost);

                if (gamePanel != null) {
                    gamePanel.loadFriendlyTextures(f);
                    gamePanel.updateFriendlyPortrait(f);
                    gamePanel.syncRecruitedUnits();
                    gamePanel.repaint();
                }

                JOptionPane.showMessageDialog(this, unitName + " модернизирован до " + (currentLevel + 1) + " уровня!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            }
            updateDisplay();
        }
    }

    public void updatePortrait(BufferedImage newPortrait) {
        this.portrait = newPortrait;
        // Обновляем отображение портрета в диалоге
        // (нужно найти JLabel с портретом и обновить его)
    }

    private void refreshWeaponPanel() {
        if (ammoLabel == null) return;

        if (unit instanceof FriendlyUnit) {
            FriendlyUnit friendly = (FriendlyUnit) unit;
            FriendlyUnit.WeaponData weapon = friendly.getEquippedWeaponData();

            if (weapon != null) {
                weaponNameLabel.setText(weapon.name);
                try {
                    File iconFile = new File(weapon.iconPath);
                    if (iconFile.exists()) {
                        Image originalImage = ImageIO.read(iconFile);
                        Image scaledImage = originalImage.getScaledInstance(120, 80, Image.SCALE_SMOOTH);
                        weaponImageLabel.setIcon(new ImageIcon(scaledImage));
                        weaponImageLabel.setText("");
                    } else {
                        weaponImageLabel.setText("🔫");
                    }
                } catch (Exception ex) {
                    weaponImageLabel.setText("🔫");
                }
                ammoLabel.setText("СНАРЯДЫ: " + inventory.getCurrentAmmoCount() + "/" + inventory.getMaxAmmo());
            } else {
                weaponNameLabel.setText("Нет оружия");
                weaponImageLabel.setText("❌");
                ammoLabel.setText("СНАРЯДЫ: 0/0");
            }
        }
    }

    private void updateDisplay() {
        healthLabel.setText("❤ Здоровье: " + currentHealth + "/" + maxHealth);
        silverLabel.setText("💰 Серебро: " + getTeamSilver());
        updateReloadButtonState();

        // Обновляем отображение опыта
        ExperienceSystem expSystem = null;
        // Обновление всех характеристик
        if (unit instanceof PlayerTank) {
            PlayerTank p = (PlayerTank) unit;
            if (strengthLabel != null) strengthLabel.setText("💪 Сила: " + p.strength);
            if (agilityLabel != null) agilityLabel.setText("🦶 Ловкость: " + p.agility);
            if (accuracyLabel != null) accuracyLabel.setText("🎯 Точность: " + p.weaponAccuracy);
            if (armorLabel != null) armorLabel.setText("🛡️ Броня: " + p.armor);
            if (critLabel != null) critLabel.setText("⚡ Крит. шанс: +" + p.critBonus + "%");
            if (visionLabel != null) visionLabel.setText("👁️ Зрение: " + p.viewRadius + " кл.");
            if (reloadLabel != null) reloadLabel.setText("🔄 Перезарядка: " + p.reloadCost + " о.х.");
            if (dodgeLabel != null) dodgeLabel.setText("💨 Уклонение: " + String.format("%.1f", p.dodgeChance) + "%");
            if (movePointsLabel != null) movePointsLabel.setText("⭐ Очки хода: " + p.movePoints + "/" + p.maxMovePoints);
            if (moveCostLabel != null) moveCostLabel.setText("🚶 Стоимость шага: " + p.moveCost + " о.х.");

            ExperienceSystem exp = p.getExperienceSystem();
            if (exp != null && levelLabel != null && expLabel != null && expProgressBar != null) {
                levelLabel.setText("📈 Уровень: " + exp.getLevel());
                expLabel.setText("✨ Опыт: " + exp.getExperience() + "/" + exp.getExperienceForNextLevel());
                expProgressBar.setMaximum(exp.getExperienceForNextLevel());
                expProgressBar.setValue(exp.getExperience());
            }
        } else if (unit instanceof FriendlyUnit) {
            FriendlyUnit f = (FriendlyUnit) unit;
            if (strengthLabel != null) strengthLabel.setText("💪 Сила: " + f.strength);
            if (agilityLabel != null) agilityLabel.setText("🦶 Ловкость: " + f.agility);
            if (accuracyLabel != null) accuracyLabel.setText("🎯 Точность: " + f.weaponAccuracy);
            if (armorLabel != null) armorLabel.setText("🛡️ Броня: " + f.armor);
            if (critLabel != null) critLabel.setText("⚡ Крит. шанс: +" + f.critBonus + "%");
            if (visionLabel != null) visionLabel.setText("👁️ Зрение: " + f.viewRadius + " кл.");
            if (reloadLabel != null) reloadLabel.setText("🔄 Перезарядка: " + f.reloadCost + " о.х.");
            if (dodgeLabel != null) dodgeLabel.setText("💨 Уклонение: " + String.format("%.1f", f.dodgeChance) + "%");
            if (movePointsLabel != null) movePointsLabel.setText("⭐ Очки хода: " + f.movePoints + "/" + f.maxMovePoints);
            if (moveCostLabel != null) moveCostLabel.setText("🚶 Стоимость шага: " + f.moveCost + " о.х.");

            ExperienceSystem exp = f.getExperienceSystem();
            if (exp != null && levelLabel != null && expLabel != null && expProgressBar != null) {
                levelLabel.setText("📈 Уровень: " + exp.getLevel());
                expLabel.setText("✨ Опыт: " + exp.getExperience() + "/" + exp.getExperienceForNextLevel());
                expProgressBar.setMaximum(exp.getExperienceForNextLevel());
                expProgressBar.setValue(exp.getExperience());
            }
        }

        if (expSystem != null && levelLabel != null && expLabel != null && expProgressBar != null) {
            levelLabel.setText("Уровень: " + expSystem.getLevel());
            expLabel.setText("Опыт: " + expSystem.getExperience() + "/" + expSystem.getExperienceForNextLevel());
            expProgressBar.setMaximum(expSystem.getExperienceForNextLevel());
            expProgressBar.setValue(expSystem.getExperience());
        }

        if (weightLabel != null) {
            weightLabel.setText("⚖️ Вес: " + String.format("%.1f", getCurrentWeight()) + "/" + getMaxCarryWeight());
        }

        // Перерисовка инвентаря
        inventoryGrid.removeAll();
        slotComponents.clear();

        inventoryGrid.setLayout(new GridBagLayout());
        JPanel[][] cellPanels = new JPanel[Inventory.WIDTH][Inventory.HEIGHT];
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        for (int y = 0; y < Inventory.HEIGHT; y++) {
            for (int x = 0; x < Inventory.WIDTH; x++) {
                JPanel cell = new JPanel(new BorderLayout());
                cell.setBackground(new Color(40, 40, 50));
                cell.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70), 1));
                cell.setPreferredSize(new Dimension(70, 70));

                final int finalX = x;
                final int finalY = y;
                final Object finalUnit = this.unit;  // ← ИСПРАВЛЕНО
                final boolean finalIsLeft = true;     // ← ИСПРАВЛЕНО (для левой панели)

                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            // Правый клик - показываем характеристики
                            Item item = inventory.getItem(finalX, finalY);
                            if (item != null && isWeaponType(item.getType())) {
                                String stats = getWeaponStatsText(item);
                                JOptionPane.showMessageDialog(inventoryGrid.getParent(),
                                        stats,
                                        "Характеристики оружия",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        } else {
                            // Левый клик - обычное действие
                            onSlotClick(finalX, finalY);
                        }
                    }
                });

                gbc.gridx = x;
                gbc.gridy = y;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                inventoryGrid.add(cell, gbc);
                cellPanels[x][y] = cell;
                slotComponents.put(x + "," + y, cell);
            }
        }

        boolean[][] occupied = new boolean[Inventory.WIDTH][Inventory.HEIGHT];
        java.util.List<Item> bigItems = new java.util.ArrayList<>();

        for (int y = 0; y < Inventory.HEIGHT; y++) {
            for (int x = 0; x < Inventory.WIDTH; x++) {
                Item item = inventory.getItem(x, y);
                if (item != null && item.getCount() > 0 && (item.getWidth() > 1 || item.getHeight() > 1)) {
                    boolean alreadyAdded = false;
                    for (Item added : bigItems) {
                        if (added == item) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded) {
                        bigItems.add(item);
                        for (int dy = 0; dy < item.getHeight(); dy++) {
                            for (int dx = 0; dx < item.getWidth(); dx++) {
                                if (x + dx < Inventory.WIDTH && y + dy < Inventory.HEIGHT) {
                                    occupied[x + dx][y + dy] = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Item item : bigItems) {
            int itemX = -1, itemY = -1;
            for (int y = 0; y < Inventory.HEIGHT; y++) {
                for (int x = 0; x < Inventory.WIDTH; x++) {
                    if (inventory.getItem(x, y) == item) {
                        itemX = x;
                        itemY = y;
                        break;
                    }
                }
                if (itemX != -1) break;
            }

            if (itemX == -1) continue;

            for (int dy = 0; dy < item.getHeight(); dy++) {
                for (int dx = 0; dx < item.getWidth(); dx++) {
                    int cx = itemX + dx;
                    int cy = itemY + dy;
                    if (cx < Inventory.WIDTH && cy < Inventory.HEIGHT) {
                        JPanel oldCell = cellPanels[cx][cy];
                        if (oldCell != null) {
                            inventoryGrid.remove(oldCell);
                            slotComponents.remove(cx + "," + cy);
                        }
                    }
                }
            }

            JPanel weaponPanel = new JPanel(new BorderLayout());
            weaponPanel.setBackground(new Color(40, 50, 60));
            weaponPanel.setPreferredSize(new Dimension(70 * item.getWidth(), 70 * item.getHeight()));

            // ===== НОВАЯ ПРОВЕРКА: можно ли экипировать =====
            boolean canEquip = canEquipItem(item, itemX, itemY);

            // Проверяем, экипировано ли это оружие
            boolean isEquipped = false;
            if (unit instanceof PlayerTank) {
                PlayerTank p = (PlayerTank) unit;
                // Получаем ID экипированного оружия и его тип
                String equippedId = p.getEquippedWeaponId();
                if (equippedId != null) {
                    // Сравниваем ID, а не тип!
                    if (("breda".equals(equippedId) && item.getType() == Item.ItemType.WEAPON) ||
                            ("45mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_45MM) ||
                            ("25mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_25MM) ||
                            ("47mm_french".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_47MM_FRENCH) ||
                            ("47mm_italian".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_47MM_ITALIAN) ||
                            ("8mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_8MM) ||
                            ("13mm_japan".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_13MM_JAPAN) ||
                            ("13mm_french".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_13MM_FRENCH) ||
                            ("30mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_30MM) ||
                            ("37mm_italian".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_37MM_ITALIAN) ||
                            ("37mm_american".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_37MM_AMERICAN) ||
                            ("37mm_sweden".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_37MM_SWEDEN) ||
                            ("76mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_76MM) ||
                            ("105mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_105MM) ||
                            ("128mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_128MM) ||
                            ("203mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_203MM)) {

                        // ДОПОЛНИТЕЛЬНАЯ ПРОВЕРКА: не экипировано ли это конкретное оружие?
                        // Для этого нужно хранить ссылку на экипированный предмет или его позицию
                        // Простой способ: если в инвентаре есть оружие того же типа в позиции 0,0 (слот экипировки) - значит оно экипировано
                        Item equippedItem = inventory.getItem(0, 0);
                        if (equippedItem != null && equippedItem == item) {
                            isEquipped = true;
                        }
                    }
                }
            } else if (unit instanceof FriendlyUnit) {
                FriendlyUnit f = (FriendlyUnit) unit;
                String equippedId = f.getEquippedWeaponId();
                if (equippedId != null) {
                    if (("breda".equals(equippedId) && item.getType() == Item.ItemType.WEAPON) ||
                            ("45mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_45MM) ||
                            ("25mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_25MM) ||
                            ("47mm_french".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_47MM_FRENCH) ||
                            ("47mm_italian".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_47MM_ITALIAN) ||
                            ("8mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_8MM) ||
                            ("13mm_japan".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_13MM_JAPAN) ||
                            ("13mm_french".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_13MM_FRENCH) ||
                            ("30mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_30MM) ||
                            ("37mm_italian".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_37MM_ITALIAN) ||
                            ("37mm_american".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_37MM_AMERICAN) ||
                            ("37mm_sweden".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_37MM_SWEDEN) ||
                            ("76mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_76MM) ||
                            ("105mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_105MM) ||
                            ("128mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_128MM) ||
                            ("203mm".equals(equippedId) && item.getType() == Item.ItemType.WEAPON_203MM)) {

                        Item equippedItem = f.getInventory().getItem(0, 0);
                        if (equippedItem != null && equippedItem == item) {
                            isEquipped = true;
                        }
                    }
                }
            }

            if (selectedX == itemX && selectedY == itemY) {
                weaponPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 0), 4));
            } else if (!canEquip) {
                weaponPanel.setBackground(new Color(80, 30, 30));
                weaponPanel.setBorder(BorderFactory.createLineBorder(new Color(150, 0, 0), 3));
            } else {
                weaponPanel.setBackground(new Color(40, 50, 60));
                weaponPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
            }

            // ===== ЗАГРУЗКА ИКОНКИ ОРУЖИЯ (ВОССТАНОВЛЕНО) =====
            String weaponPath;
            if (item.getType() == Item.ItemType.WEAPON) {
                weaponPath = "src/ObjectsOfInventory/Weapon/2 cm Breda (I).png";
            } else if (item.getType() == Item.ItemType.WEAPON_45MM) {
                weaponPath = "src/ObjectsOfInventory/Weapon/45 мм обр. 1932 г.png";
            } else if (item.getType() == Item.ItemType.WEAPON_25MM) {
                weaponPath = "src/ObjectsOfInventory/Weapon/25-mm-Canon-Raccourci-mle.-1934.png";
            } else if (item.getType() == Item.ItemType.WEAPON_47MM_FRENCH) {
                weaponPath = "src/ObjectsOfInventory/Weapon/47 mm SA35.png";
            } else if (item.getType() == Item.ItemType.WEAPON_47MM_ITALIAN) {
                weaponPath = "src/ObjectsOfInventory/Weapon/Cannone da 47-32.png";
            } else if (item.getType() == Item.ItemType.WEAPON_30MM) {
                weaponPath = "src/ObjectsOfInventory/Weapon/3 cm M.K. 103A.png";
            } else if (item.getType() == Item.ItemType.WEAPON_37MM_ITALIAN) {
                weaponPath = "src/ObjectsOfInventory/Weapon/Cannone da 37-40.png";
            } else if (item.getType() == Item.ItemType.WEAPON_37MM_AMERICAN) {
                weaponPath = "src/ObjectsOfInventory/Weapon/37 mm Semiautomatic Gun M1924.png";
            } else if (item.getType() == Item.ItemType.WEAPON_37MM_SWEDEN) {
                weaponPath = "src/ObjectsOfInventory/Weapon/37 mm kan m-38-49 strv.png";
            } else if (item.getType() == Item.ItemType.WEAPON_13MM_JAPAN) {
                weaponPath = "src/ObjectsOfInventory/Weapon/13 mm Autocannon Type Ho.png";
            } else if (item.getType() == Item.ItemType.WEAPON_13MM_FRENCH) {
                weaponPath = "src/ObjectsOfInventory/Weapon/13,2 mm Hotchkiss mle. 1930.png";
            } else if (item.getType() == Item.ItemType.WEAPON_76MM) {
                weaponPath = "src/ObjectsOfInventory/Weapon/76 мм Л-10С.png";
            } else if (item.getType() == Item.ItemType.WEAPON_105MM) {
                weaponPath = "src/ObjectsOfInventory/Weapon/10,5 cm StuH 42 L28.png";
            } else if (item.getType() == Item.ItemType.WEAPON_128MM) {
                weaponPath = "src/ObjectsOfInventory/Weapon/12,8 cm Kw.K. L50.png";
            } else if (item.getType() == Item.ItemType.WEAPON_203MM) {
                weaponPath = "src/ObjectsOfInventory/Weapon/8-inch Howitzer M47.png";
            } else {
                weaponPath = "src/ObjectsOfInventory/Weapon/7,92 mm Mauser E.W. 141.png";
            }

            ImageIcon originalIcon = null;
            try {
                File iconFile = new File(weaponPath);
                if (iconFile.exists()) {
                    originalIcon = new ImageIcon(weaponPath);
                    ImageIcon displayIcon = createTintedIcon(originalIcon, !canEquip);
                    Image scaledImage = displayIcon.getImage().getScaledInstance(
                            70 * item.getWidth() - 20,
                            70 * item.getHeight() - 40,
                            Image.SCALE_SMOOTH);
                    JLabel weaponIconLabel = new JLabel(new ImageIcon(scaledImage));
                    weaponIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    weaponPanel.add(weaponIconLabel, BorderLayout.CENTER);
                } else {
                    JLabel label = new JLabel("🔫", SwingConstants.CENTER);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 48));
                    if (!canEquip) {
                        label.setForeground(new Color(200, 50, 50));
                    }
                    weaponPanel.add(label, BorderLayout.CENTER);
                }
            } catch (Exception ex) {
                JLabel label = new JLabel("🔫", SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 48));
                if (!canEquip) {
                    label.setForeground(new Color(200, 50, 50));
                }
                weaponPanel.add(label, BorderLayout.CENTER);
            }

            JLabel countLabel = new JLabel("x" + item.getCount(), SwingConstants.CENTER);
            countLabel.setFont(new Font("Arial", Font.BOLD, 12));
            countLabel.setForeground(new Color(255, 215, 0));

            // Нижняя панель с кнопкой выброса
            JPanel bottomBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            bottomBtnPanel.setOpaque(false);

            // Кнопка выброса (только если оружие не экипировано)
            if (!isEquipped) {
                JButton dropWeaponBtn = new JButton("🗑 ВЫБРОСИТЬ");
                dropWeaponBtn.setFont(new Font("Arial", Font.BOLD, 9));
                dropWeaponBtn.setBackground(new Color(150, 50, 0));
                dropWeaponBtn.setForeground(Color.WHITE);
                dropWeaponBtn.setFocusPainted(false);
                dropWeaponBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(InventoryDialog.this,
                            "Выбросить " + item.getDisplayName() + "?",
                            "Подтверждение",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        dropItem(item.getType(), 1);
                    }
                });
                bottomBtnPanel.add(dropWeaponBtn);
            } else {
                // Заглушка для сохранения высоты
                JLabel emptyLabel = new JLabel(" ");
                emptyLabel.setFont(new Font("Arial", Font.PLAIN, 9));
                bottomBtnPanel.add(emptyLabel);
            }

            weaponPanel.add(countLabel, BorderLayout.SOUTH);
            weaponPanel.add(bottomBtnPanel, BorderLayout.NORTH);

            final int finalX = itemX;
            final int finalY = itemY;
            weaponPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        Item clickedItem = inventory.getItem(finalX, finalY);
                        if (clickedItem != null && isWeaponType(clickedItem.getType())) {
                            String stats = getWeaponStatsText(clickedItem);
                            JOptionPane.showMessageDialog(inventoryGrid.getParent(),
                                    stats,
                                    "Характеристики оружия",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        onSlotClick(finalX, finalY);
                    }
                }
            });

            gbc.gridx = itemX;
            gbc.gridy = itemY;
            gbc.gridwidth = item.getWidth();
            gbc.gridheight = item.getHeight();
            inventoryGrid.add(weaponPanel, gbc);
            slotComponents.put(itemX + "," + itemY, weaponPanel);
        }

        for (int y = 0; y < Inventory.HEIGHT; y++) {
            for (int x = 0; x < Inventory.WIDTH; x++) {
                if (occupied[x][y]) continue;

                JPanel cell = cellPanels[x][y];
                if (cell == null) continue;

                Item item = inventory.getItem(x, y);

                if (item != null && item.getCount() > 0) {
                    cell.removeAll();
                    cell.setLayout(new BorderLayout());

                    if (selectedX == x && selectedY == y) {
                        cell.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 0), 3));
                    } else {
                        cell.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70), 1));
                    }

                    // Верхняя панель с иконкой
                    JPanel topPanel = new JPanel(new BorderLayout());
                    topPanel.setOpaque(false);

                    ImageIcon icon = getItemIcon(item);
                    JLabel iconLabel;
                    if (icon != null) {
                        iconLabel = new JLabel(icon);
                        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    } else {
                        iconLabel = new JLabel(item.getType().icon, SwingConstants.CENTER);
                        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 30));
                    }
                    topPanel.add(iconLabel, BorderLayout.CENTER);
                    cell.add(topPanel, BorderLayout.CENTER);

                    // Нижняя панель с счётчиком и кнопкой выброса
                    JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 2));
                    bottomPanel.setOpaque(false);

                    JLabel countLabel = new JLabel("x" + item.getCount(), SwingConstants.CENTER);
                    countLabel.setFont(new Font("Arial", Font.PLAIN, 10));
                    countLabel.setForeground(new Color(255, 215, 0));
                    bottomPanel.add(countLabel);

                    // Кнопка выброса (только для не-оружия или если оружие не экипировано)
                    boolean isEquippedWeapon = false;
                    if (unit instanceof PlayerTank) {
                        PlayerTank p = (PlayerTank) unit;
                        if (p.getEquippedWeaponData() != null) {
                            Item.ItemType equippedType = getWeaponTypeFromEquipped(p.getEquippedWeaponData());
                            if (item.getType() == equippedType) {
                                isEquippedWeapon = true;
                            }
                        }
                    } else if (unit instanceof FriendlyUnit) {
                        FriendlyUnit f = (FriendlyUnit) unit;
                        if (f.getEquippedWeaponData() != null) {
                            Item.ItemType equippedType = getWeaponTypeFromEquipped(f.getEquippedWeaponData());
                            if (item.getType() == equippedType) {
                                isEquippedWeapon = true;
                            }
                        }
                    }

                    if (!isEquippedWeapon) {
                        JButton dropBtn = new JButton("🗑");
                        dropBtn.setFont(new Font("Arial", Font.BOLD, 8));
                        dropBtn.setBackground(new Color(150, 50, 0));
                        dropBtn.setForeground(Color.WHITE);
                        dropBtn.setFocusPainted(false);
                        dropBtn.setMargin(new Insets(0, 0, 0, 0));
                        dropBtn.addActionListener(e -> {
                            int toDrop = item.getCount();
                            int confirm = JOptionPane.showConfirmDialog(InventoryDialog.this,
                                    "Выбросить " + toDrop + "x " + item.getDisplayName() + "?",
                                    "Подтверждение",
                                    JOptionPane.YES_NO_OPTION);
                            if (confirm == JOptionPane.YES_OPTION) {
                                if (item instanceof AmmoItem) {
                                    dropAmmo(((AmmoItem) item).getCaliber(), toDrop);
                                } else {
                                    dropItem(item.getType(), toDrop);
                                }
                            }
                        });
                        bottomPanel.add(dropBtn);
                    } else {
                        // Заглушка для сохранения высоты
                        JLabel emptyLabel = new JLabel(" ");
                        emptyLabel.setFont(new Font("Arial", Font.PLAIN, 8));
                        bottomPanel.add(emptyLabel);
                    }

                    cell.add(bottomPanel, BorderLayout.SOUTH);

                    cell.setToolTipText("<html>" + item.getType().name + "<br>" +
                            "Количество: " + item.getCount() + "<br>" +
                            item.getType().description + "</html>");
                } else {
                    cell.removeAll();
                    cell.setLayout(new BorderLayout());
                    cell.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70), 1));
                    cell.setToolTipText("Пустой слот");
                }
            }
        }

        cleanupDepletedItems();

        inventoryGrid.revalidate();
        inventoryGrid.repaint();
    }

    private void cleanupDepletedItems() {
        boolean found = true;
        while (found) {
            found = false;
            for (int y = 0; y < Inventory.HEIGHT; y++) {
                for (int x = 0; x < Inventory.WIDTH; x++) {
                    Item item = inventory.getItem(x, y);
                    if (item instanceof MedicalItem && ((MedicalItem) item).isDepleted()) {
                        System.out.println("  🗑 Удаляем истощённый MedicalItem из [" + x + "," + y + "]");
                        inventory.removeItem(x, y, 1);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
        }
    }

    // Остальные методы (onSlotClick, useSelectedItem, healUnit, eatBread, getItemIcon, createPlayerWeapon, getTeamSilver и т.д.) остаются без изменений
    // ...

    private int getTeamSilver() {
        if (player != null) {
            return player.getSilver();
        }
        if (unit instanceof PlayerTank) {
            return ((PlayerTank) unit).getSilver();
        }
        return 0;
    }

    private int getTeamParts() {
        if (player != null) {
            return player.getParts();
        }
        if (unit instanceof PlayerTank) {
            return ((PlayerTank) unit).getParts();
        }
        return 0;
    }

    // ... остальные методы (onSlotClick, useSelectedItem, healUnit, eatBread, getItemIcon, createPlayerWeapon) ...

    @Override
    public void setVisible(boolean visible) {
        if (visible && !isInitialized) {
            SwingUtilities.invokeLater(() -> {
                updateDisplay();
                isInitialized = true;
            });
        }
        super.setVisible(visible);
    }

    // ===== МЕТОДЫ ДЛЯ РАБОТЫ С ИНВЕНТАРЕМ =====

    private void onSlotClick(int x, int y) {
        System.out.println("=== onSlotClick ===");
        System.out.println("  Клик по слоту: [" + x + "," + y + "]");

        // Находим корневой слот для большого предмета
        int realX = x;
        int realY = y;
        boolean found = false;

        Item itemAtCell = inventory.getItem(x, y);
        if (itemAtCell != null && itemAtCell.getCount() > 0 && (itemAtCell.getWidth() > 1 || itemAtCell.getHeight() > 1)) {
            for (int checkY = 0; checkY <= y && !found; checkY++) {
                for (int checkX = 0; checkX <= x && !found; checkX++) {
                    Item checkItem = inventory.getItem(checkX, checkY);
                    if (checkItem != null && checkItem == itemAtCell) {
                        realX = checkX;
                        realY = checkY;
                        found = true;
                        System.out.println("  Найден корневой слот: [" + realX + "," + realY + "]");
                    }
                }
            }
        }

        Item item = inventory.getItem(realX, realY);
        System.out.println("  Предмет: " + (item != null ? item.getType() : "null"));

        // ===== НОВАЯ ПРОВЕРКА: если предмет - MedicalItem и истощён, игнорируем =====
        if (item instanceof MedicalItem && ((MedicalItem) item).isDepleted()) {
            System.out.println("  ⚠ Предмет истощён! Удаляем его.");
            inventory.removeItem(realX, realY, 1);
            updateDisplay();
            infoLabel.setText("Предмет был истощён и удалён");
            return;
        }

        if (selectedX == -1 && selectedY == -1) {
            if (item != null && item.getCount() > 0) {
                selectedX = realX;
                selectedY = realY;
                updateDisplay();
                infoLabel.setText("Выбран: " + item.getDisplayName() + " (x" + item.getCount() +
                        ") | Кликните на другой слот для перемещения или на слот оружия (0,0) для экипировки");
            } else {
                infoLabel.setText("Пустой слот");
            }
        } else {
            // Экипировка в слот 0,0
            if (x == 0 && y == 0) {



                System.out.println("  >>> ЭКИПИРОВКА В СЛОТ 0,0 <<<");

                Item selectedItem = inventory.getItem(selectedX, selectedY);
                if (selectedItem == null) {
                    infoLabel.setText("Выделенный предмет не найден!");
                    selectedX = -1;
                    selectedY = -1;
                    updateDisplay();
                    return;
                }

                boolean isWeapon = (selectedItem.getType() == Item.ItemType.WEAPON ||
                        selectedItem.getType() == Item.ItemType.WEAPON_45MM ||
                        selectedItem.getType() == Item.ItemType.WEAPON_25MM ||
                        selectedItem.getType() == Item.ItemType.WEAPON_47MM_FRENCH ||
                        selectedItem.getType() == Item.ItemType.WEAPON_47MM_ITALIAN ||
                        selectedItem.getType() == Item.ItemType.WEAPON_13MM_JAPAN ||
                        selectedItem.getType() == Item.ItemType.WEAPON_13MM_FRENCH ||
                        selectedItem.getType() == Item.ItemType.WEAPON_30MM ||
                        selectedItem.getType() == Item.ItemType.WEAPON_37MM_ITALIAN ||
                        selectedItem.getType() == Item.ItemType.WEAPON_37MM_AMERICAN ||
                        selectedItem.getType() == Item.ItemType.WEAPON_37MM_SWEDEN ||
                        selectedItem.getType() == Item.ItemType.WEAPON_76MM ||
                        selectedItem.getType() == Item.ItemType.WEAPON_105MM ||
                        selectedItem.getType() == Item.ItemType.WEAPON_128MM ||
                        selectedItem.getType() == Item.ItemType.WEAPON_203MM ||
                        selectedItem.getType() == Item.ItemType.WEAPON_8MM);

                // В onSlotClick, при экипировке в слот 0,0:

// Проверяем, есть ли очки хода для экипировки
                int currentMovePoints = 0;
                int maxMovePoints = 0;
                if (unit instanceof PlayerTank) {
                    currentMovePoints = ((PlayerTank) unit).movePoints;
                    maxMovePoints = ((PlayerTank) unit).maxMovePoints;
                } else if (unit instanceof FriendlyUnit) {
                    currentMovePoints = ((FriendlyUnit) unit).movePoints;
                    maxMovePoints = ((FriendlyUnit) unit).maxMovePoints;
                }

// ===== НОВОЕ УСЛОВИЕ: можно менять оружие ТОЛЬКО если очки хода максимальны =====
                if (currentMovePoints != maxMovePoints) {
                    JOptionPane.showMessageDialog(this,
                            "❌ НЕЛЬЗЯ СМЕНИТЬ ОРУЖИЕ!\n\n" +
                                    "Смена оружия требует полного запаса очков хода.\n" +
                                    "У вас: " + currentMovePoints + "/" + maxMovePoints + " очков хода\n\n" +
                                    "Завершите ход и начните новый, чтобы сменить вооружение.",
                            "Нельзя экипировать", JOptionPane.WARNING_MESSAGE);
                    selectedX = -1;
                    selectedY = -1;
                    updateDisplay();
                    return;
                }

                if (!isWeapon) {
                    infoLabel.setText("Это не оружие! Выделите оружие для экипировки");
                    selectedX = -1;
                    selectedY = -1;
                    updateDisplay();
                    return;
                }

                // Для союзников
                if (unit instanceof FriendlyUnit) {
                    FriendlyUnit friendly = (FriendlyUnit) unit;

                    int newWeaponX = selectedX;
                    int newWeaponY = selectedY;
                    Item.ItemType newWeaponType = selectedItem.getType();

                    FriendlyUnit.WeaponData newWeapon = null;
                    String weaponId = null;

                    if (newWeaponType == Item.ItemType.WEAPON) {
                        newWeapon = WeaponLibrary.getWeapon("2 cm Breda (I)");
                        weaponId = "breda";
                    } else if (newWeaponType == Item.ItemType.WEAPON_45MM) {
                        newWeapon = WeaponLibrary.getWeapon("45 мм обр. 1932 г.");
                        weaponId = "45mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_25MM) {
                        newWeapon = WeaponLibrary.getWeapon("25mm Canon Raccourci mle. 1934");
                        weaponId = "25mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_37MM_ITALIAN) {
                        newWeapon = WeaponLibrary.getWeapon("Cannone da 37-40");
                        weaponId = "37mm_italian";
                    } else if (newWeaponType == Item.ItemType.WEAPON_37MM_AMERICAN) {
                        newWeapon = WeaponLibrary.getWeapon("37 mm Semiautomatic Gun M1924");
                        weaponId = "37mm_american";
                    } else if (newWeaponType == Item.ItemType.WEAPON_37MM_SWEDEN) {
                        newWeapon = WeaponLibrary.getWeapon("37 mm kan m-38-49 strv");
                        weaponId = "37mm_sweden";
                    } else if (newWeaponType == Item.ItemType.WEAPON_47MM_FRENCH) {
                        newWeapon = WeaponLibrary.getWeapon("47 mm SA35");
                        weaponId = "47mm_french";
                    } else if (newWeaponType == Item.ItemType.WEAPON_47MM_ITALIAN) {
                        newWeapon = WeaponLibrary.getWeapon("Cannone da 47-32");
                        weaponId = "47mm_italian";
                    } else if (newWeaponType == Item.ItemType.WEAPON_8MM) {
                        newWeapon = WeaponLibrary.getWeapon("7,92 mm Mauser E.W. 141");
                        weaponId = "8mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_13MM_JAPAN) {
                        newWeapon = WeaponLibrary.getWeapon("13 mm Autocannon Type Ho");
                        weaponId = "13mm_japan";
                    } else if (newWeaponType == Item.ItemType.WEAPON_13MM_FRENCH) {
                        newWeapon = WeaponLibrary.getWeapon("13,2 mm Hotchkiss mle. 1930");
                        weaponId = "13mm_french";
                    } else if (newWeaponType == Item.ItemType.WEAPON_76MM) {
                        newWeapon = WeaponLibrary.getWeapon("76 мм Л-10С");
                        weaponId = "76mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_203MM) {
                        newWeapon = WeaponLibrary.getWeapon("8-inch Howitzer M47");
                        weaponId = "203mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_30MM) {
                        boolean canEquip = false;
                        if ("LT".equals(friendly.getUpgradeClass()) && friendly.getUpgradeLevel() >= 2) {
                            canEquip = true;
                        }

                        if (!canEquip) {
                            JOptionPane.showMessageDialog(this,
                                    "❌ Это оружие требует модернизации ЛТ-2 и выше!\n" +
                                            "Выберите класс ЛТ при модернизации на 2 уровне.",
                                    "Нельзя экипировать",
                                    JOptionPane.WARNING_MESSAGE);
                            selectedX = -1;
                            selectedY = -1;
                            updateDisplay();
                            return;
                        }

                        // ИСПРАВЛЕНИЕ: используем WeaponLibrary.getWeapon() вместо createPlayerWeapon()
                        newWeapon = WeaponLibrary.getWeapon("3 cm M.K. 103A");
                        weaponId = "30mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_105MM) {
                        boolean canEquip = false;
                        if (("PT".equals(friendly.getUpgradeClass()) && friendly.getUpgradeLevel() >= 2) ||
                                ("TT".equals(friendly.getUpgradeClass()) && friendly.getUpgradeLevel() >= 3) ||
                                ("ST".equals(friendly.getUpgradeClass()) && friendly.getUpgradeLevel() >= 3)) {
                            canEquip = true;
                        }

                        if (!canEquip) {
                            JOptionPane.showMessageDialog(this,
                                    "❌ Это оружие требует модернизации ПТ-2/СТ-3/ТТ-3 и выше!\n" +
                                            "Выберите класс ПТ/СТ/ТТ при модернизации.",
                                    "Нельзя экипировать",
                                    JOptionPane.WARNING_MESSAGE);
                            selectedX = -1;
                            selectedY = -1;
                            updateDisplay();
                            return;
                        }

                        // ИСПРАВЛЕНИЕ: используем WeaponLibrary.getWeapon() вместо createPlayerWeapon()
                        newWeapon = WeaponLibrary.getWeapon("10,5 cm StuH 42 L28");
                        weaponId = "105mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_128MM) {
                        boolean canEquip = false;
                        if (("TT".equals(friendly.getUpgradeClass()) || "PT".equals(friendly.getUpgradeClass()))  && friendly.getUpgradeLevel() >= 4) {
                            canEquip = true;
                        }

                        if (!canEquip) {
                            JOptionPane.showMessageDialog(this,
                                    "❌ Это оружие требует модернизации ТТ-4/ПТ-4 и выше!\n",
                                    "Нельзя экипировать",
                                    JOptionPane.WARNING_MESSAGE);
                            selectedX = -1;
                            selectedY = -1;
                            updateDisplay();
                            return;
                        }

                        // ИСПРАВЛЕНИЕ: используем WeaponLibrary.getWeapon() вместо createPlayerWeapon()
                        newWeapon = WeaponLibrary.getWeapon("12,8 cm Kw.K. L50");
                        weaponId = "128mm";
                    }

                    if (newWeapon == null) {
                        JOptionPane.showMessageDialog(this, "Ошибка: данные оружия не найдены!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        selectedX = -1;
                        selectedY = -1;
                        updateDisplay();
                        return;
                    }

                    // ===== ДОБАВЬТЕ ПРОВЕРКУ СИЛЫ =====
                    if (friendly.strength < newWeapon.requiredStrength) {
                        JOptionPane.showMessageDialog(this,
                                "❌ Недостаточно силы для использования " + newWeapon.name + "!\n\n" +
                                        "Требуется сила: " + newWeapon.requiredStrength + "\n" +
                                        "Сила " + friendly.name + ": " + friendly.strength,
                                "Невозможно экипировать",
                                JOptionPane.WARNING_MESSAGE);
                        selectedX = -1;
                        selectedY = -1;
                        updateDisplay();
                        return;
                    }

                    FriendlyUnit.WeaponData currentWeapon = friendly.getEquippedWeaponData();
                    friendly.updateCurrentWeaponAmmo();
                    inventory.removeItem(newWeaponX, newWeaponY, 1);

                    if (currentWeapon != null) {
                        Item.ItemType currentItemType;
                        // Используем weaponId для определения типа!
                        if ("breda".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON;
                        } else if ("45mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_45MM;
                        } else if ("25mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_25MM;
                        }  else if ("47mm_french".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_47MM_FRENCH;
                        } else if ("47mm_italian".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_47MM_ITALIAN;
                        } else if ("8mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_8MM;
                        } else if ("13mm_japan".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_13MM_JAPAN;
                        } else if ("13mm_french".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_13MM_FRENCH;
                        } else if ("30mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_30MM;
                        } else if ("37mm_italian".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_37MM_ITALIAN;
                        } else if ("37mm_american".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_37MM_AMERICAN;
                        } else if ("37mm_sweden".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_37MM_SWEDEN;
                        } else if ("76mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_76MM;
                        } else if ("105mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_105MM;
                        } else if ("128mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_128MM;
                        } else if ("203mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_203MM;
                        } else {
                            currentItemType = Item.ItemType.WEAPON;
                        }
                        inventory.addItem(currentItemType, 1);
                    }

                    friendly.setEquippedWeapon(weaponId, newWeapon);

                    // ===== ПРИМЕНЯЕМ ШТРАФ ЗА ПЕРЕЭКИПИРОВКУ =====
                    int penalty = friendly.getReequipPenalty();
                    friendly.movePoints = Math.max(0, friendly.movePoints - penalty);

                    friendly.updateWeaponDisplay();
                    refreshWeaponPanel();

                    JOptionPane.showMessageDialog(this,
                            "✅ " + friendly.name + " экипировал " + newWeapon.name + "!",
                            "Оружие экипировано", JOptionPane.INFORMATION_MESSAGE);
                }
                // Для игрока
                else if (unit instanceof PlayerTank) {
                    PlayerTank playerTank = (PlayerTank) unit;

                    int newWeaponX = selectedX;
                    int newWeaponY = selectedY;
                    Item.ItemType newWeaponType = selectedItem.getType();

                    PlayerTank.WeaponData newWeapon = null;
                    String weaponId = null;

                    if (newWeaponType == Item.ItemType.WEAPON) {
                        newWeapon = createPlayerWeapon("2 cm Breda (I)", "breda", Caliber.CALIBER_20MM,
                                3, 30, 0.02, 5, 11, 6, 11, 0.09,
                                "src/ObjectsOfInventory/Weapon/2 cm Breda (I).png",
                                15, 4);
                        weaponId = "breda";
                    } else if (newWeaponType == Item.ItemType.WEAPON_45MM) {
                        newWeapon = createPlayerWeapon("45 мм обр. 1932 г.", "45mm", Caliber.CALIBER_45MM,
                                1, 15, 0.045, 11, 22, 10, 47, 0.12,
                                "src/ObjectsOfInventory/Weapon/45 мм обр. 1932 г.png",
                                19, 5);
                        weaponId = "45mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_25MM) {
                        newWeapon = createPlayerWeapon("25mm Canon Raccourci mle. 1934", "25mm", Caliber.CALIBER_25MM,
                                2, 45, 0.025, 4, 9, 14, 27, 0.11,
                                "src/ObjectsOfInventory/Weapon/25-mm-Canon-Raccourci-mle.-1934.png",
                                15, 5);
                        weaponId = "25mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_47MM_FRENCH) {
                        newWeapon = createPlayerWeapon("47 mm SA35", "47mm_french", Caliber.CALIBER_47MM,
                                1, 21, 0.047, 9, 18, 10, 55, 0.09,
                                "src/ObjectsOfInventory/Weapon/47 mm SA35.png",
                                17, 5);
                        weaponId = "47mm_french";
                    } else if (newWeaponType == Item.ItemType.WEAPON_47MM_ITALIAN) {
                        newWeapon = createPlayerWeapon("Cannone da 47-32", "47mm_italian", Caliber.CALIBER_47MM,
                                1, 17, 0.047, 9, 18, 10, 52, 0.1,
                                "src/ObjectsOfInventory/Weapon/Cannone da 47-32.png",
                                16, 5);
                        weaponId = "47mm_italian";
                    } else if (newWeaponType == Item.ItemType.WEAPON_8MM) {
                        newWeapon = createPlayerWeapon("7,92 mm Mauser E.W. 141", "8mm", Caliber.CALIBER_8MM,
                                8, 25, 0.008, 7, 14, 22, 8, 0.06,
                                "src/ObjectsOfInventory/Weapon/7,92 mm Mauser E.W. 141.png",
                                12, 3);
                        weaponId = "8mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_13MM_JAPAN) {
                        newWeapon = createPlayerWeapon("13 mm Autocannon Type Ho", "13mm_japan", Caliber.CALIBER_13MM,
                                3, 17, 0.013, 4, 9, 12, 8, 0.07,
                                "src/ObjectsOfInventory/Weapon/13 mm Autocannon Type Ho.png",
                                12, 3);
                        weaponId = "13mm_japan";
                    } else if (newWeaponType == Item.ItemType.WEAPON_13MM_FRENCH) {
                        newWeapon = createPlayerWeapon("13,2 mm Hotchkiss mle. 1930", "13mm_french", Caliber.CALIBER_13MM,
                                3, 13, 0.013, 4, 9, 9, 8, 0.05,
                                "src/ObjectsOfInventory/Weapon/13,2 mm Hotchkiss mle. 1930.png",
                                10, 3);
                        weaponId = "13mm_french";
                    } else if (newWeaponType == Item.ItemType.WEAPON_37MM_ITALIAN) {
                        newWeapon = createPlayerWeapon("Cannone da 37-40", "37mm_italian", Caliber.CALIBER_37MM,
                                1, 15, 0.037, 7, 14, 12, 40, 0.07,
                                "src/ObjectsOfInventory/Weapon/Cannone da 37-40.png", 13, 4);
                        weaponId = "37mm_italian";
                    } else if (newWeaponType == Item.ItemType.WEAPON_37MM_AMERICAN) {
                        newWeapon = createPlayerWeapon("37 mm Semiautomatic Gun M1924", "37mm_american", Caliber.CALIBER_37MM,
                                5, 10, 0.037, 9, 18, 25, 30, 0.07,
                                "src/ObjectsOfInventory/Weapon/37 mm Semiautomatic Gun M1924.png", 20, 4);
                        weaponId = "37mm_american";
                    } else if (newWeaponType == Item.ItemType.WEAPON_37MM_SWEDEN) {
                        newWeapon = createPlayerWeapon("37 mm kan m-38-49 strv", "37mm_sweden", Caliber.CALIBER_37MM,
                                1, 50, 0.037, 8, 16, 12, 40, 0.05,
                                "src/ObjectsOfInventory/Weapon/37 mm kan m-38-49 strv.png", 24, 5);
                        weaponId = "37mm_sweden";
                    } else if (newWeaponType == Item.ItemType.WEAPON_76MM) {
                        newWeapon = createPlayerWeapon("76 мм Л-10С", "76mm", Caliber.CALIBER_76MM,
                                1, 5, 0.076, 18, 36, 16, 110, 0.1,
                                "src/ObjectsOfInventory/Weapon/76 мм Л-10С.png", 43, 12);
                        weaponId = "76mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_203MM) {
                        newWeapon = createPlayerWeapon("8-inch Howitzer M47", "203mm", Caliber.CALIBER_203MM,
                                1, 20, 0.203, 32, 64, 75, 700, 0.3,
                                "src/ObjectsOfInventory/Weapon/8-inch Howitzer M47.png", 90, 26);
                        weaponId = "203mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_30MM) {  // ← ДОБАВЬТЕ ЭТОТ БЛОК
                        // Проверяем, может ли игрок экипировать 30-мм пушку
                        boolean canEquip = false;
                        if ("LT".equals(playerTank.getUpgradeClass()) && playerTank.getUpgradeLevel() >= 2) {
                            canEquip = true;
                        }

                        if (!canEquip) {
                            JOptionPane.showMessageDialog(this,
                                    "❌ 3 cm M.K. 103A требует модернизации ЛТ-2 и выше!\n" +
                                            "Выберите класс ЛТ при модернизации на 2 уровне.",
                                    "Нельзя экипировать",
                                    JOptionPane.WARNING_MESSAGE);
                            selectedX = -1;
                            selectedY = -1;
                            updateDisplay();
                            return;
                        }

                        newWeapon = createPlayerWeapon("3 cm M.K. 103A", "30mm", Caliber.CALIBER_30MM,
                                3, 15, 0.03, 5, 11, 38, 30, 0.09,
                                "src/ObjectsOfInventory/Weapon/3 cm M.K. 103A.png", 45, 9);
                        weaponId = "30mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_105MM) {  // ← ДОБАВЬТЕ ЭТОТ БЛОК
                        // Проверяем, может ли игрок экипировать 30-мм пушку
                        boolean canEquip = false;
                        if (("TT".equals(playerTank.getUpgradeClass()) && playerTank.getUpgradeLevel() >= 2) ||
                                ("PT".equals(playerTank.getUpgradeClass()) && playerTank.getUpgradeLevel() >= 3) ||
                                ("ST".equals(playerTank.getUpgradeClass()) && playerTank.getUpgradeLevel() >= 3)) {
                            canEquip = true;
                        }

                        if (!canEquip) {
                            JOptionPane.showMessageDialog(this,
                                    "❌ 10,5 cm StuH 42 L28 требует модернизации ПТ-2/СТ-3/ТТ-3 и выше!\n" +
                                            "Выберите класс ПТ/СТ/ТТ при модернизации.",
                                    "Нельзя экипировать",
                                    JOptionPane.WARNING_MESSAGE);
                            selectedX = -1;
                            selectedY = -1;
                            updateDisplay();
                            return;
                        }

                        newWeapon = createPlayerWeapon("10,5 cm StuH 42 L28", "105mm", Caliber.CALIBER_105MM,
                                1, 20, 0.105, 24, 48, 26, 350, 0.12,
                                "src/ObjectsOfInventory/Weapon/10,5 cm StuH 42 L28.png", 60, 14);
                        weaponId = "105mm";
                    } else if (newWeaponType == Item.ItemType.WEAPON_128MM) {  // ← ДОБАВЬТЕ ЭТОТ БЛОК
                        // Проверяем, может ли игрок экипировать 128-мм пушку
                        boolean canEquip = false;
                        if (("TT".equals(playerTank.getUpgradeClass()) || "PT".equals(playerTank.getUpgradeClass())) && playerTank.getUpgradeLevel() >= 4) {
                            canEquip = true;
                        }

                        if (!canEquip) {
                            JOptionPane.showMessageDialog(this,
                                    "❌ 12,8 cm Kw.K. L50 требует модернизации ТТ-4/ПТ-4 и выше!\n",
                                    "Нельзя экипировать",
                                    JOptionPane.WARNING_MESSAGE);
                            selectedX = -1;
                            selectedY = -1;
                            updateDisplay();
                            return;
                        }

                        newWeapon = createPlayerWeapon("12,8 cm Kw.K. L50", "128mm", Caliber.CALIBER_128MM,
                                1, 30, 0.128, 30, 60, 36, 440, 0.15,
                                "src/ObjectsOfInventory/Weapon/12,8 cm Kw.K. L50.png", 80, 19);
                        weaponId = "128mm";
                    }

                    if (newWeapon == null) {
                        JOptionPane.showMessageDialog(this, "Ошибка: данные оружия не найдены!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        selectedX = -1;
                        selectedY = -1;
                        updateDisplay();
                        return;
                    }

                    // ПРОВЕРКА СИЛЫ (здесь НЕ НАДО ОБЪЯВЛЯТЬ playerTank ЗАНОВО!)
                    if (playerTank.strength < newWeapon.requiredStrength) {
                        JOptionPane.showMessageDialog(this,
                                "❌ Недостаточно силы для использования " + newWeapon.name + "!\n\n" +
                                        "Требуется сила: " + newWeapon.requiredStrength + "\n" +
                                        "Ваша сила: " + playerTank.strength,
                                "Невозможно экипировать",
                                JOptionPane.WARNING_MESSAGE);
                        selectedX = -1;
                        selectedY = -1;
                        updateDisplay();
                        return;
                    }

                    PlayerTank.WeaponData currentWeapon = playerTank.getEquippedWeaponData();
                    playerTank.updateCurrentWeaponAmmo();
                    inventory.removeItem(newWeaponX, newWeaponY, 1);

                    if (currentWeapon != null) {
                        Item.ItemType currentItemType;
                        // Используем weaponId для определения типа!
                        if ("breda".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON;
                        } else if ("45mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_45MM;
                        } else if ("25mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_25MM;
                        } else if ("47mm_french".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_47MM_FRENCH;
                        } else if ("47mm_italian".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_47MM_ITALIAN;
                        } else if ("8mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_8MM;
                        } else if ("13mm_japan".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_13MM_JAPAN;
                        } else if ("13mm_french".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_13MM_FRENCH;
                        } else if ("30mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_30MM;
                        } else if ("37mm_italian".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_37MM_ITALIAN;
                        } else if ("37mm_american".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_37MM_AMERICAN;
                        } else if ("37mm_sweden".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_37MM_SWEDEN;
                        } else if ("76mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_76MM;
                        } else if ("105mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_105MM;
                        } else if ("128mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_128MM;
                        } else if ("203mm".equals(currentWeapon.weaponId)) {
                            currentItemType = Item.ItemType.WEAPON_203MM;
                        } else {
                            currentItemType = Item.ItemType.WEAPON;
                        }
                        inventory.addItem(currentItemType, 1);
                    }

                    playerTank.setEquippedWeapon(weaponId, newWeapon);

                    // ===== ПРИМЕНЯЕМ ШТРАФ ЗА ПЕРЕЭКИПИРОВКУ =====
                    int penalty = playerTank.getReequipPenalty();
                    playerTank.movePoints = Math.max(0, playerTank.movePoints - penalty);

                    refreshWeaponPanel();

                    JOptionPane.showMessageDialog(this,
                            "✅ Leichttraktor экипировал " + newWeapon.name + "!",
                            "Оружие экипировано", JOptionPane.INFORMATION_MESSAGE);
                }

                selectedX = -1;
                selectedY = -1;
                updateDisplay();
                return;
            }

            // Обычное перемещение
            if (selectedX == realX && selectedY == realY) {
                useSelectedItem();
            } else {
                Item targetItem = inventory.getItem(realX, realY);
                if (targetItem != null && (targetItem.getWidth() > 1 || targetItem.getHeight() > 1)) {
                    infoLabel.setText("Нельзя переместить предмет на место большого предмета!");
                } else {
                    boolean moved = inventory.moveItem(selectedX, selectedY, realX, realY);
                    if (!moved) {
                        infoLabel.setText("Нельзя переместить сюда!");
                    } else {
                        infoLabel.setText("Предмет перемещён");
                    }
                }
                selectedX = -1;
                selectedY = -1;
                updateDisplay();
            }
        }
    }

    private double getCurrentWeight() {
        double weight = inventory.getTotalWeight();
        if (unit instanceof PlayerTank) {
            weight += ((PlayerTank) unit).getEquipmentWeight();
        } else if (unit instanceof FriendlyUnit) {
            weight += ((FriendlyUnit) unit).getEquipmentWeight();
        }
        return weight;
    }

    private void useSelectedItem() {
        if (selectedX == -1 || selectedY == -1) return;

        Item item = inventory.getItem(selectedX, selectedY);
        if (item == null) return;

        // Определяем стоимость использования
        int actionCost = 0;
        int healAmount = 0;

        switch (item.getType()) {
            case BANDAGE:
                actionCost = 7;
                healAmount = 30;
                break;
            case MEDKIT:
                actionCost = 15;
                healAmount = 70;
                break;
            case REPAIR_KIT:
                actionCost = 30;
                healAmount = 150;
                break;
            case BREAD:
                actionCost = 20;
                healAmount = 100;
                break;
            default:
                infoLabel.setText(item.getType().description);
                return;
        }

        // Проверяем, достаточно ли очков хода
        int currentMovePoints = 0;
        if (unit instanceof PlayerTank) {
            currentMovePoints = ((PlayerTank) unit).movePoints;
        } else {
            currentMovePoints = ((FriendlyUnit) unit).movePoints;
        }

        if (currentMovePoints < actionCost) {
            String itemName = getItemDisplayName(item.getType());
            JOptionPane.showMessageDialog(this,
                    "❌ НЕДОСТАТОЧНО ОЧКОВ ХОДА!\n\n" +
                            "Для использования " + itemName + " нужно: " + actionCost + " очков хода\n" +
                            "У вас: " + currentMovePoints + " очков хода",
                    "Нельзя использовать", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Применяем эффект
        boolean used = false;
        switch (item.getType()) {
            case BANDAGE:
            case MEDKIT:
            case REPAIR_KIT:
                used = healUnit(healAmount);
                break;
            case BREAD:
                used = eatBread();
                break;
        }

        if (used) {
            // Тратим очки хода
            if (unit instanceof PlayerTank) {
                ((PlayerTank) unit).movePoints -= actionCost;
            } else {
                ((FriendlyUnit) unit).movePoints -= actionCost;
            }

            inventory.removeItem(selectedX, selectedY, 1);
            selectedX = -1;
            selectedY = -1;
            updateDisplay();

            String itemName = getItemDisplayName(item.getType());
            JOptionPane.showMessageDialog(this,
                    "✅ Использован " + itemName + "!\n" +
                            "❤ Восстановлено " + healAmount + " HP\n" +
                            "⭐ Потрачено " + actionCost + " очков хода\n" +
                            "Осталось: " + currentMovePoints + "/" + getMaxMovePoints(),
                    "Использование предмета", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Вспомогательный метод для отображения имени предмета
    private String getItemDisplayName(Item.ItemType type) {
        switch (type) {
            case BANDAGE: return "бинт";
            case MEDKIT: return "аптечку";
            case REPAIR_KIT: return "ремкомплект";
            case BREAD: return "просроченный хлеб";
            default: return type.name();
        }
    }

    // Вспомогательный метод для получения максимальных очков хода
    private int getMaxMovePoints() {
        if (unit instanceof PlayerTank) {
            return ((PlayerTank) unit).maxMovePoints;
        } else if (unit instanceof FriendlyUnit) {
            return ((FriendlyUnit) unit).maxMovePoints;
        }
        return 0;
    }

    private boolean healUnit(int amount) {
        if (unit instanceof PlayerTank) {
            PlayerTank p = (PlayerTank) unit;
            if (p.health >= p.maxHealth) {
                JOptionPane.showMessageDialog(this, "Здоровье уже максимальное!", "Нельзя использовать", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            p.health = Math.min(p.maxHealth, p.health + amount);
            currentHealth = p.health;
            healthLabel.setText("Здоровье: " + currentHealth + "/" + maxHealth);
        } else {
            FriendlyUnit f = (FriendlyUnit) unit;
            if (f.health >= f.maxHealth) {
                JOptionPane.showMessageDialog(this, "Здоровье уже максимальное!", "Нельзя использовать", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            f.health = Math.min(f.maxHealth, f.health + amount);
            currentHealth = f.health;
            healthLabel.setText("Здоровье: " + currentHealth + "/" + maxHealth);
        }
        JOptionPane.showMessageDialog(this, "Восстановлено " + amount + " HP!", "Лечение", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    private boolean eatBread() {
        boolean healed = healUnit(100);
        if (!healed) return false;

        if (unit instanceof PlayerTank) {
            PlayerTank playerTank = (PlayerTank) unit;
            if (playerTank.breadDebuffTurns == 0) {
                playerTank.breadDebuffTurns = 3;
                playerTank.breadDebuffRemainingTurns = 3;
            }
        } else if (unit instanceof FriendlyUnit) {
            FriendlyUnit friendly = (FriendlyUnit) unit;
            if (friendly.breadDebuffTurns == 0) {
                friendly.breadDebuffTurns = 3;
                friendly.breadDebuffRemainingTurns = 3;
            }
        }

        return true;
    }

    private ImageIcon getItemIcon(Item item) {

        if (item instanceof MedicalItem && ((MedicalItem) item).isDepleted()) {
            return null;  // Не показываем иконку для истощённого предмета
        }

        String treatmentPath = "src/ObjectsOfInventory/TreatmentAndRepair/";
        String shellsPath = "src/ObjectsOfInventory/Shells/";
        String weaponPath = "src/ObjectsOfInventory/Weapon/";
        String keysPath = "src/ObjectsOfInventory/Key/";  // ← ДОБАВЬТЕ

        // ===== КЛЮЧИ - ДОБАВЬТЕ ЭТОТ БЛОК ПЕРВЫМ =====
        if (item instanceof KeyItem) {
            KeyItem keyItem = (KeyItem) item;
            Door.DoorColor color = keyItem.getColor();
            ImageIcon icon = keyIcons.get(color);
            if (icon != null) {
                return new ImageIcon(icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }
            // Запасной вариант - используем цветной эмодзи
            JLabel tempLabel = new JLabel("🔑");
            tempLabel.setForeground(color.color);
            return null; // или создайте простую иконку
        }


        switch (item.getType()) {
            // ===== ЛЕЧЕБНЫЕ ПРЕДМЕТЫ =====
            case MEDKIT:
                File medkitFile = new File(treatmentPath + "MedicineChest.png");
                if (medkitFile.exists()) {
                    try {
                        Image img = ImageIO.read(medkitFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case BANDAGE:
                File bandageFile = new File(treatmentPath + "Bandage.png");
                if (bandageFile.exists()) {
                    try {
                        Image img = ImageIO.read(bandageFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case REPAIR_KIT:
                File repairFile = new File(treatmentPath + "RepairKit.png");
                if (repairFile.exists()) {
                    try {
                        Image img = ImageIO.read(repairFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case FIRE_EXTINGUISHER:
                File extinguisherFile = new File(treatmentPath + "extinguisher.png");
                if (extinguisherFile.exists()) {
                    try {
                        Image img = ImageIO.read(extinguisherFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case ENERGY_DRINK:
                File energyFile = new File(treatmentPath + "EnergyDrink.png");
                if (energyFile.exists()) {
                    try {
                        Image img = ImageIO.read(energyFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case GRENADE:
                File grenadeFile = new File(treatmentPath + "Grenade.png");
                if (grenadeFile.exists()) {
                    try {
                        Image img = ImageIO.read(grenadeFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case BREAD:
                File breadFile = new File(treatmentPath + "ExpiredBread.png");
                if (breadFile.exists()) {
                    try {
                        Image img = ImageIO.read(breadFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;

            // ===== ОРУЖИЕ =====
            case WEAPON:
                File weaponFile = new File(weaponPath + "2 cm Breda (I).png");
                if (weaponFile.exists()) {
                    try {
                        Image img = ImageIO.read(weaponFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_8MM:
                File weapon8mmFile = new File(weaponPath + "7,92 mm Mauser E.W. 141.png");
                if (weapon8mmFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon8mmFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_13MM_JAPAN:
                File weapon13mmJapanFile = new File(weaponPath + "13 mm Autocannon Type Ho.png");
                if (weapon13mmJapanFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon13mmJapanFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_13MM_FRENCH:
                File weapon13mmFrenchFile = new File(weaponPath + "13,2 mm Hotchkiss mle. 1930.png");
                if (weapon13mmFrenchFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon13mmFrenchFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_25MM:
                File weapon25mmFile = new File(weaponPath + "25-mm-Canon-Raccourci-mle.-1934.png");
                if (weapon25mmFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon25mmFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_30MM:
                File weapon30mmFile = new File(weaponPath + "3 cm M.K. 103A.png");
                if (weapon30mmFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon30mmFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_37MM_ITALIAN:
                File weapon37ItalianFile = new File(weaponPath + "Cannone da 37-40.png");
                if (weapon37ItalianFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon37ItalianFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_37MM_AMERICAN:
                File weapon37AmericanFile = new File(weaponPath + "37 mm Semiautomatic Gun M1924.png");
                if (weapon37AmericanFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon37AmericanFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_37MM_SWEDEN:
                File weapon37SwedenFile = new File(weaponPath + "37 mm kan m-38-49 strv.png");
                if (weapon37SwedenFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon37SwedenFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_45MM:
                File weapon45mmFile = new File(weaponPath + "45 мм обр. 1932 г.png");
                if (weapon45mmFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon45mmFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_47MM_FRENCH:
                File weapon47mmFrenchFile = new File(weaponPath + "47 mm SA35.png");
                if (weapon47mmFrenchFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon47mmFrenchFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_47MM_ITALIAN:
                File weapon47mmItalianFile = new File(weaponPath + "Cannone da 47-32.png");
                if (weapon47mmItalianFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon47mmItalianFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_76MM:
                File weapon76mmFile = new File(weaponPath + "76 мм Л-10С.png");
                if (weapon76mmFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon76mmFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_105MM:
                File weapon105mmFile = new File(weaponPath + "10,5 cm StuH 42 L28.png");
                if (weapon105mmFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon105mmFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_128MM:
                File weapon128mmFile = new File(weaponPath + "12,8 cm Kw.K. L50.png");
                if (weapon128mmFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon128mmFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;
            case WEAPON_203MM:
                File weapon203mmFile = new File(weaponPath + "8-inch Howitzer M47.png");
                if (weapon203mmFile.exists()) {
                    try {
                        Image img = ImageIO.read(weapon203mmFile);
                        return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    } catch (Exception e) {}
                }
                break;

            // ===== СНАРЯДЫ =====
            case AMMO:
                if (item instanceof AmmoItem) {
                    AmmoItem ammoItem = (AmmoItem) item;
                    Caliber caliber = ammoItem.getCaliber();
                    boolean isImproved = ammoItem.isImproved();

                    // 20mm снаряды
                    if (caliber == Caliber.CALIBER_20MM) {
                        if (isImproved && improvedShellIcon != null) {
                            return new ImageIcon(improvedShellIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                        } else if (basicShellIcon != null) {
                            return new ImageIcon(basicShellIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                        }
                    }
                    // 30мм снаряды
                    else if (caliber == Caliber.CALIBER_30MM) {
                        if (thirtyMMShellIcon != null) {
                            return new ImageIcon(thirtyMMShellIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                        }
                        File shell30mmFile = new File(shellsPath + "30mmShell_based.png");
                        if (shell30mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell30mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                    // 37мм снаряды
                    else if (caliber == Caliber.CALIBER_37MM) {
                        if (thirtySevenMMShellIcon != null) {
                            return new ImageIcon(thirtySevenMMShellIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                        }
                        File shell37mmFile = new File(shellsPath + "37mmShell_based.png");
                        if (shell37mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell37mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                    // 45mm снаряды
                    else if (caliber == Caliber.CALIBER_45MM) {
                        if (fortyFiveMMShellIcon != null) {
                            return new ImageIcon(fortyFiveMMShellIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                        }
                        File shell45mmFile = new File(shellsPath + "45mmShell_based.png");
                        if (shell45mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell45mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                    // 25mm снаряды
                    else if (caliber == Caliber.CALIBER_25MM) {
                        if (twentyFiveMMShellIcon != null) {
                            return new ImageIcon(twentyFiveMMShellIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                        }
                        File shell25mmFile = new File(shellsPath + "25mmShell_based.png");
                        if (shell25mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell25mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                    // 47mm снаряды
                    else if (caliber == Caliber.CALIBER_47MM) {
                        if (fortySevenMMShellIcon != null) {
                            return new ImageIcon(fortySevenMMShellIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                        }
                        File shell47mmFile = new File(shellsPath + "47mmShell_based.png");
                        if (shell47mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell47mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                    // 30mm снаряды
                    else if (caliber == Caliber.CALIBER_105MM) {
                        File shell105mmFile = new File(shellsPath + "105mmShell_based.png");
                        if (shell105mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell105mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                    // 13mm снаряды
                    else if (caliber == Caliber.CALIBER_13MM) {
                        File shell13mmFile = new File(shellsPath + "13mmShell_based.png");
                        if (shell13mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell13mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                    // 76mm снаряды
                    else if (caliber == Caliber.CALIBER_76MM) {
                        File shell76mmFile = new File(shellsPath + "76mmShell_based.png");
                        if (shell76mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell76mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                    // 128mm снаряды
                    else if (caliber == Caliber.CALIBER_128MM) {
                        File shell128mmFile = new File(shellsPath + "128mmShell_based.png");
                        if (shell128mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell128mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                    // 203mm снаряды (для M53)
                    else if (caliber == Caliber.CALIBER_203MM) {
                        File shell203mmFile = new File(shellsPath + "203mmShell_HESH.png");
                        if (shell203mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell203mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                    // 8mm снаряды (Mauser)
                    else if (caliber == Caliber.CALIBER_8MM) {
                        File shell8mmFile = new File(shellsPath + "8mmShell_based.png");
                        if (shell8mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell8mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                }
                break;

            default:
                break;
        }

        // Для медицинских предметов можно добавить специальную иконку с индикатором
        if (item instanceof MedicalItem) {
            MedicalItem medical = (MedicalItem) item;
            // Можно вернуть специальную иконку или стандартную
            // Здесь пока возвращаем стандартную
        }

        return null;
    }

    // В InventoryDialog.java добавьте метод для открытия инвентаря другого юнита
    // В InventoryDialog.java добавьте метод для открытия инвентаря другого юнита
    // В InventoryDialog.java добавьте метод для открытия инвентаря другого юнита
    private void openOtherInventory() {
        // Получаем координаты текущего юнита
        int unitX, unitY;
        if (unit instanceof PlayerTank) {
            unitX = ((PlayerTank) unit).gridX;
            unitY = ((PlayerTank) unit).gridY;
        } else {
            unitX = ((FriendlyUnit) unit).gridX;
            unitY = ((FriendlyUnit) unit).gridY;
        }

        // Собираем доступных союзников (только тех, кто в радиусе 1 клетки)
        java.util.List<Object> availableUnits = new java.util.ArrayList<>();

        // Добавляем союзников (не текущего)
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isAlive && friendly.isRecruited && friendly != unit) {
                int dx = Math.abs(unitX - friendly.gridX);
                int dy = Math.abs(unitY - friendly.gridY);
                // Обмен возможен только если расстояние <= 1 клетка
                if (dx <= 1 && dy <= 1) {
                    availableUnits.add(friendly);
                }
            }
        }

        // Добавляем игрока (если текущий юнит - не игрок)
        if (!(unit instanceof PlayerTank) && player != null && player.health > 0) {
            int dx = Math.abs(unitX - player.gridX);
            int dy = Math.abs(unitY - player.gridY);
            if (dx <= 1 && dy <= 1) {
                availableUnits.add(player);
            }
        }

        if (availableUnits.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Нет союзников рядом для обмена! Подойдите в соседнюю клетку.",
                    "Обмен", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Создаём диалог выбора цели
        String[] options = new String[availableUnits.size()];
        for (int i = 0; i < availableUnits.size(); i++) {
            Object u = availableUnits.get(i);
            if (u instanceof PlayerTank) {
                options[i] = "Leichttraktor";
            } else {
                options[i] = ((FriendlyUnit) u).name;
            }
        }

        int choice = JOptionPane.showOptionDialog(this,
                "Выберите союзника для обмена предметами (рядом):",
                "Обмен инвентарями",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice < 0) return;

        Object targetUnit = availableUnits.get(choice);

        // Получаем портреты
        BufferedImage heroPortrait = gamePanel != null ? gamePanel.getHeroPortrait() : null;
        BufferedImage m53Portrait = gamePanel != null ? gamePanel.getM53Portrait() : null;
        BufferedImage ms1Portrait = gamePanel != null ? gamePanel.getMS1Portrait() : null;
        BufferedImage vk10001pPortrait = gamePanel != null ? gamePanel.getVK10001PPortrait() : null;
        BufferedImage amx40Portrait = gamePanel != null ? gamePanel.getAMX40Portrait() : null;
        BufferedImage t1Portrait = gamePanel != null ? gamePanel.getT1Portrait() : null;

        // Открываем диалог обмена
        TradeInventoryDialog tradeDialog = new TradeInventoryDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                unit, targetUnit, player,
                heroPortrait, m53Portrait, ms1Portrait, vk10001pPortrait, amx40Portrait, t1Portrait, gamePanel
        );
        tradeDialog.setVisible(true);

        // Обновляем отображение после закрытия диалога
        updateDisplay();
    }

    private PlayerTank.WeaponData createPlayerWeapon(String name, String weaponId, Caliber caliber,
                                                     int burstSize, int weaponAccuracy, double weaponCaliber,
                                                     int shotCost, int aimedShotCost, int reloadCost,
                                                     int weaponDamage, double critChance, String iconPath,
                                                     int requiredStrength, int weight) {
        return new PlayerTank.WeaponData(name, weaponId, caliber, burstSize, weaponAccuracy,
                weaponCaliber, shotCost, aimedShotCost, reloadCost,
                weaponDamage, critChance, iconPath, requiredStrength, weight);
    }

    private boolean canEquipItem(Item item, int slotX, int slotY) {
        // Проверяем только оружие (размер 2x2)
        if (item.getWidth() < 2 || item.getHeight() < 2) return true;

        Item.ItemType type = item.getType();
        if (!isWeaponType(type)) return true;

        if (unit instanceof PlayerTank) {
            PlayerTank p = (PlayerTank) unit;

            // Проверка силы
            int requiredStrength = getRequiredStrengthForWeapon(type);
            if (requiredStrength > 0 && p.strength < requiredStrength) {
                return false;  // Недостаточно силы
            }

            // Проверка для 30mm орудия (ЛТ-2)
            if (type == Item.ItemType.WEAPON_30MM) {
                if (!("LT".equals(p.getUpgradeClass()) && p.getUpgradeLevel() >= 2)) {
                    return false;
                }
            }

            // Проверка для 105mm орудия
            if (type == Item.ItemType.WEAPON_105MM) {
                if (!("PT".equals(p.getUpgradeClass()) && p.getUpgradeLevel() >= 2) &&
                        !("ST".equals(p.getUpgradeClass()) && p.getUpgradeLevel() >= 3) &&
                        !("TT".equals(p.getUpgradeClass()) && p.getUpgradeLevel() >= 3)) {
                    return false;
                }
            }

            // Проверка для 128mm орудия
            if (type == Item.ItemType.WEAPON_128MM) {
                if (!(("TT".equals(p.getUpgradeClass()) || "PT".equals(p.getUpgradeClass())) && p.getUpgradeLevel() >= 4)) {
                    return false;
                }
            }

        } else if (unit instanceof FriendlyUnit) {
            FriendlyUnit f = (FriendlyUnit) unit;

            // Проверка силы
            int requiredStrength = getRequiredStrengthForWeapon(type);
            if (requiredStrength > 0 && f.strength < requiredStrength) {
                return false;  // Недостаточно силы
            }

            // Проверка для 30mm орудия (ЛТ-2)
            if (type == Item.ItemType.WEAPON_30MM) {
                if (!("LT".equals(f.getUpgradeClass()) && f.getUpgradeLevel() >= 2)) {
                    return false;
                }
            }

            // Проверка для 105mm орудия
            if (type == Item.ItemType.WEAPON_105MM) {
                if (!("PT".equals(f.getUpgradeClass()) && f.getUpgradeLevel() >= 2) &&
                        !("ST".equals(f.getUpgradeClass()) && f.getUpgradeLevel() >= 3) &&
                        !("TT".equals(f.getUpgradeClass()) && f.getUpgradeLevel() >= 3)) {
                    return false;
                }
            }

            // Проверка для 128mm орудия
            if (type == Item.ItemType.WEAPON_128MM) {
                if (!(("TT".equals(f.getUpgradeClass()) || "PT".equals(f.getUpgradeClass())) && f.getUpgradeLevel() >= 4)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isWeaponType(Item.ItemType type) {
        return type == Item.ItemType.WEAPON ||
                type == Item.ItemType.WEAPON_45MM ||
                type == Item.ItemType.WEAPON_25MM ||
                type == Item.ItemType.WEAPON_37MM_ITALIAN ||
                type == Item.ItemType.WEAPON_37MM_AMERICAN ||
                type == Item.ItemType.WEAPON_37MM_SWEDEN ||
                type == Item.ItemType.WEAPON_47MM_FRENCH ||
                type == Item.ItemType.WEAPON_47MM_ITALIAN ||
                type == Item.ItemType.WEAPON_8MM ||
                type == Item.ItemType.WEAPON_13MM_JAPAN ||
                type == Item.ItemType.WEAPON_13MM_FRENCH ||
                type == Item.ItemType.WEAPON_30MM ||
                type == Item.ItemType.WEAPON_76MM ||
                type == Item.ItemType.WEAPON_105MM ||
                type == Item.ItemType.WEAPON_128MM ||
                type == Item.ItemType.WEAPON_203MM;
    }

    private int getRequiredStrengthForWeapon(Item.ItemType type) {
        switch (type) {
            case WEAPON: return 15;      // 2 cm Breda (I)
            case WEAPON_45MM: return 19; // 45 мм обр. 1932 г.
            case WEAPON_25MM: return 15; // 45 мм обр. 1932 г.
            case WEAPON_37MM_ITALIAN: return 13;
            case WEAPON_37MM_AMERICAN: return 20;
            case WEAPON_37MM_SWEDEN: return 24;
            case WEAPON_47MM_FRENCH: return 17; // 47mm SA 35
            case WEAPON_47MM_ITALIAN: return 16; // 47mm SA 35
            case WEAPON_8MM: return 12;  // 7,92 mm Mauser
            case WEAPON_13MM_JAPAN: return 12; // 3 cm M.K. 103A
            case WEAPON_13MM_FRENCH: return 10; // 3 cm M.K. 103A
            case WEAPON_30MM: return 45; // 3 cm M.K. 103A
            case WEAPON_76MM: return 43; // 76 мм Л-10С
            case WEAPON_105MM: return 60; // 3 cm M.K. 103A
            case WEAPON_128MM: return 80; // 12,8 cm Kw.K. L50
            case WEAPON_203MM: return 90; // 8-inch Howitzer M47
            default: return 0;
        }
    }

    private ImageIcon createTintedIcon(ImageIcon originalIcon, boolean isRed) {
        if (!isRed || originalIcon == null) return originalIcon;

        BufferedImage originalImage = new BufferedImage(
                originalIcon.getIconWidth(),
                originalIcon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = originalImage.createGraphics();
        originalIcon.paintIcon(null, g, 0, 0);
        g.dispose();

        // Создаём красноватый оттенок
        BufferedImage tintedImage = new BufferedImage(
                originalIcon.getIconWidth(),
                originalIcon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tintedImage.createGraphics();
        g2.drawImage(originalImage, 0, 0, null);
        g2.setColor(new Color(200, 50, 50, 120));  // Красный с прозрачностью
        g2.fillRect(0, 0, tintedImage.getWidth(), tintedImage.getHeight());
        g2.dispose();

        return new ImageIcon(tintedImage);
    }

    // Добавьте этот метод в класс InventoryDialog (после других методов)

    private String getWeaponStatsText(Item item) {
        StringBuilder stats = new StringBuilder();

        if (unit instanceof PlayerTank) {
            PlayerTank.WeaponData weapon = null;
            String weaponId = null;

            // Определяем, какое это оружие
            if (item.getType() == Item.ItemType.WEAPON) {
                weapon = createPlayerWeapon("2 cm Breda (I)", "breda", Caliber.CALIBER_20MM,
                        3, 30, 0.02, 5, 11, 6, 11, 0.09,
                        "src/ObjectsOfInventory/Weapon/2 cm Breda (I).png", 15, 4);
                weaponId = "2 cm Breda (I)";
            } else if (item.getType() == Item.ItemType.WEAPON_8MM) {
                weapon = createPlayerWeapon("7,92 mm Mauser E.W. 141", "8mm", Caliber.CALIBER_8MM,
                        8, 25, 0.008, 7, 14, 22, 8, 0.06,
                        "src/ObjectsOfInventory/Weapon/7,92 mm Mauser E.W. 141.png", 12, 3);
                weaponId = "7,92 mm Mauser E.W. 141";
            } else if (item.getType() == Item.ItemType.WEAPON_13MM_JAPAN) {
                weapon = createPlayerWeapon("13 mm Autocannon Type Ho", "13mm_japan", Caliber.CALIBER_13MM,
                        3, 17, 0.013, 4, 9, 12, 8, 0.07,
                        "src/ObjectsOfInventory/Weapon/13 mm Autocannon Type Ho.png", 12, 3);
                weaponId = "13 mm Autocannon Type Ho";
            } else if (item.getType() == Item.ItemType.WEAPON_13MM_FRENCH) {
                weapon = createPlayerWeapon("13,2 mm Hotchkiss mle. 1930", "13mm_french", Caliber.CALIBER_13MM,
                        3, 13, 0.013, 4, 9, 9, 8, 0.05,
                        "src/ObjectsOfInventory/Weapon/13,2 mm Hotchkiss mle. 1930.png", 10, 3);
                weaponId = "13,2 mm Hotchkiss mle. 1930";
            } else if (item.getType() == Item.ItemType.WEAPON_30MM) {
                weapon = createPlayerWeapon("3 cm M.K. 103A", "30mm", Caliber.CALIBER_30MM,
                        3, 15, 0.03, 5, 11, 38, 30, 0.09,
                        "src/ObjectsOfInventory/Weapon/3 cm M.K. 103A.png", 45, 9);
                weaponId = "3 cm M.K. 103A";
            } else if (item.getType() == Item.ItemType.WEAPON_37MM_ITALIAN) {
                weapon = createPlayerWeapon("Cannone da 37-40", "37mm_italian", Caliber.CALIBER_37MM,
                        1, 15, 0.037, 7, 14, 12, 40, 0.07,
                        "src/ObjectsOfInventory/Weapon/Cannone da 37-40.png", 13, 4);
                weaponId = "Cannone da 37-40";
            } else if (item.getType() == Item.ItemType.WEAPON_37MM_AMERICAN) {
                weapon = createPlayerWeapon("37 mm Semiautomatic Gun M1924", "37mm_american", Caliber.CALIBER_37MM,
                        5, 10, 0.037, 9, 18, 25, 30, 0.07,
                        "src/ObjectsOfInventory/Weapon/37 mm Semiautomatic Gun M1924.png", 20, 4);
                weaponId = "37 mm Semiautomatic Gun M1924";
            } else if (item.getType() == Item.ItemType.WEAPON_37MM_SWEDEN) {
                weapon = createPlayerWeapon("37 mm kan m-38-49 strv", "37mm_sweden", Caliber.CALIBER_37MM,
                        1, 50, 0.037, 8, 16, 12, 40, 0.05,
                        "src/ObjectsOfInventory/Weapon/37 mm kan m-38-49 strv.png", 24, 5);
                weaponId = "37 mm kan m-38-49 strv";
            } else if (item.getType() == Item.ItemType.WEAPON_45MM) {
                weapon = createPlayerWeapon("45 мм обр. 1932 г.", "45mm", Caliber.CALIBER_45MM,
                        1, 15, 0.045, 11, 22, 10, 47, 0.12,
                        "src/ObjectsOfInventory/Weapon/45 мм обр. 1932 г.png", 19, 5);
                weaponId = "45 мм обр. 1932 г.";
            } else if (item.getType() == Item.ItemType.WEAPON_25MM) {
                weapon = createPlayerWeapon("25mm Canon Raccourci mle. 1934", "25mm", Caliber.CALIBER_25MM,
                        2, 45, 0.025, 4, 9, 14, 27, 0.11,
                        "src/ObjectsOfInventory/Weapon/25-mm-Canon-Raccourci-mle.-1934.png", 15, 5);
                weaponId = "45 мм обр. 1932 г.";
            } else if (item.getType() == Item.ItemType.WEAPON_47MM_FRENCH) {
                weapon = createPlayerWeapon("47 mm SA35", "47mm_french", Caliber.CALIBER_47MM,
                        1, 21, 0.047, 9, 18, 10, 55, 0.09,
                        "src/ObjectsOfInventory/Weapon/47 mm SA35.png", 17, 5);
                weaponId = "47 mm SA35";
            } else if (item.getType() == Item.ItemType.WEAPON_47MM_ITALIAN) {
                weapon = createPlayerWeapon("Cannone da 47-32", "47mm_italian", Caliber.CALIBER_47MM,
                        1, 17, 0.047, 9, 18, 10, 52, 0.1,
                        "src/ObjectsOfInventory/Weapon/Cannone da 47-32.png", 16, 5);
                weaponId = "Cannone da 47-32";
            } else if (item.getType() == Item.ItemType.WEAPON_76MM) {
                weapon = createPlayerWeapon("76 мм Л-10С", "76mm", Caliber.CALIBER_76MM,
                        1, 5, 0.076, 18, 36, 16, 110, 0.1,
                        "src/ObjectsOfInventory/Weapon/76 мм Л-10С.png", 43, 12);
                weaponId = "76 мм Л-10С";
            } else if (item.getType() == Item.ItemType.WEAPON_105MM) {
                weapon = createPlayerWeapon("10,5 cm StuH 42 L28", "105mm", Caliber.CALIBER_105MM,
                        1, 20, 0.12, 24, 48, 26, 350, 0.12,
                        "src/ObjectsOfInventory/Weapon/10,5 cm StuH 42 L28.png", 60, 14);
                weaponId = "3 cm M.K. 103A";
            } else if (item.getType() == Item.ItemType.WEAPON_128MM) {
                weapon = createPlayerWeapon("12,8 cm Kw.K. L50", "128mm", Caliber.CALIBER_128MM,
                        1, 30, 0.128, 30, 60, 36, 440, 0.15,
                        "src/ObjectsOfInventory/Weapon/12,8 cm Kw.K. L50.png", 80, 19);
                weaponId = "12,8 cm Kw.K. L50";
            } else if (item.getType() == Item.ItemType.WEAPON_203MM) {
                weapon = createPlayerWeapon("8-inch Howitzer M47", "203mm", Caliber.CALIBER_203MM,
                        1, 20, 0.203, 32, 64, 75, 700, 0.3,
                        "src/ObjectsOfInventory/Weapon/8-inch Howitzer M47.png", 90, 26);
                weaponId = "8-inch Howitzer M47";
            }

            if (weapon != null) {
                stats.append("=== ").append(weapon.name).append(" ===\n\n");
                stats.append("📌 Калибр: ").append(weapon.weaponCaliber).append(" м (").append(weapon.caliber.name).append(")\n");
                stats.append("💥 Количество выстрелов за огонь: ").append(weapon.burstSize).append("\n");
                stats.append("🎯 Точность оружия: ").append(weapon.weaponAccuracy).append("\n");
                stats.append("⚡ Стоимость беглого огня: ").append(weapon.shotCost).append(" о.х.\n");
                stats.append("🎯 Стоимость прицельного огня: ").append(weapon.aimedShotCost).append(" о.х.\n");
                stats.append("🔄 Стоимость перезарядки: ").append(weapon.reloadCost).append(" о.х.\n");
                stats.append("💢 Базовый урон: ").append(weapon.weaponDamage).append("\n");
                stats.append("⚡ Базовый шанс крита: ").append(String.format("%.0f", weapon.critChance * 100)).append("%\n");
                stats.append("💪 Требуемая сила: ").append(weapon.requiredStrength).append("\n");
                stats.append("⚖️ Вес оружия: ").append(weapon.weight).append("\n");

                // Определяем размер обоймы в зависимости от калибра
                int magazineSize = getMagazineSizeForCaliber(weapon.caliber);
                stats.append("📦 Размер обоймы: ").append(magazineSize).append(" снарядов\n");
            }
        } else if (unit instanceof FriendlyUnit) {
            FriendlyUnit.WeaponData weapon = WeaponLibrary.getWeaponByItem(item);
            if (weapon != null) {
                stats.append("=== ").append(weapon.name).append(" ===\n\n");
                stats.append("📌 Калибр: ").append(weapon.weaponCaliber).append(" м (").append(weapon.caliber.name).append(")\n");
                stats.append("💥 Количество выстрелов за огонь: ").append(weapon.burstSize).append("\n");
                stats.append("🎯 Точность оружия: ").append(weapon.weaponAccuracy).append("\n");
                stats.append("⚡ Стоимость беглого огня: ").append(weapon.shotCost).append(" о.х.\n");
                stats.append("🎯 Стоимость прицельного огня: ").append(weapon.aimedShotCost).append(" о.х.\n");
                stats.append("🔄 Стоимость перезарядки: ").append(weapon.reloadCost).append(" о.х.\n");
                stats.append("💢 Базовый урон: ").append(weapon.weaponDamage).append("\n");
                stats.append("⚡ Базовый шанс крита: ").append(String.format("%.0f", weapon.critChance * 100)).append("%\n");
                stats.append("💪 Требуемая сила: ").append(weapon.requiredStrength).append("\n");
                stats.append("⚖️ Вес оружия: ").append(weapon.weight).append("\n");

                int magazineSize = getMagazineSizeForCaliber(weapon.caliber);
                stats.append("📦 Размер обоймы: ").append(magazineSize).append(" снарядов\n");
            }
        }

        return stats.toString();
    }

    private int getMagazineSizeForCaliber(Caliber caliber) {
        if (caliber == Caliber.CALIBER_20MM) return 12;
        else if (caliber == Caliber.CALIBER_8MM) return 40;
        else if (caliber == Caliber.CALIBER_13MM) return 15;
        else if (caliber == Caliber.CALIBER_25MM) return 10;
        else if (caliber == Caliber.CALIBER_30MM) return 12;
        else if (caliber == Caliber.CALIBER_37MM) return 5;
        else if (caliber == Caliber.CALIBER_45MM) return 7;
        else if (caliber == Caliber.CALIBER_47MM) return 5;
        else if (caliber == Caliber.CALIBER_76MM) return 3;
        else if (caliber == Caliber.CALIBER_105MM) return 3;
        else if (caliber == Caliber.CALIBER_128MM) return 3;
        else if (caliber == Caliber.CALIBER_203MM) return 1;
        return 1;
    }
}