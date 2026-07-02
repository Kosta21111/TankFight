package ui;

import entities.FriendlyUnit;
import entities.PlayerTank;
import entities.GarbageContainer;
import inventory.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Map;

public class GarbageDialog extends JDialog {
    private Object activeUnit;
    private GarbageContainer container;
    private JPanel itemsPanel;
    private JLabel infoLabel;
    private BufferedImage heroPortrait;
    private BufferedImage m53Portrait;
    private BufferedImage ms1Portrait;
    private BufferedImage vk10001pPortrait;
    private BufferedImage amx40Portrait;
    private BufferedImage t1Portrait;


    // Иконки для предметов
    private static ImageIcon basicShellIcon = null;
    private static ImageIcon improvedShellIcon = null;
    private static ImageIcon fortyFiveMMShellIcon = null;
    private static ImageIcon twentyFiveMMShellIcon = null;
    private static ImageIcon fortySevenMMShellIcon = null;
    private static ImageIcon eightMMShellIcon = null;
    private static ImageIcon medkitIcon = null;
    private static ImageIcon bandageIcon = null;
    private static ImageIcon repairKitIcon = null;
    private static ImageIcon energyDrinkIcon = null;
    private static ImageIcon grenadeIcon = null;
    private static ImageIcon breadIcon = null;
    private static ImageIcon extinguisherIcon = null;
    private static ImageIcon weaponIcon = null;
    private static ImageIcon weapon8mmIcon = null;
    private static ImageIcon weapon30mmIcon = null;
    private static ImageIcon weapon13mmJapanIcon = null;
    private static ImageIcon weapon13mmFrenchIcon = null;
    private static ImageIcon weapon25mmIcon = null;
    private static ImageIcon weapon45mmIcon = null;
    private static ImageIcon weapon47mmIcon = null;
    private static ImageIcon weapon76mmIcon = null;
    private static ImageIcon weapon128mmIcon = null;

    public GarbageDialog(JFrame parent, Object activeUnit, GarbageContainer container,
                         BufferedImage heroPortrait, BufferedImage m53Portrait,
                         BufferedImage ms1Portrait, BufferedImage vk10001pPortrait,
                         BufferedImage amx40Portrait, BufferedImage t1Portrait) {
        super(parent, "🗑 Мусорный контейнер", true);
        this.activeUnit = activeUnit;
        this.container = container;
        this.heroPortrait = heroPortrait;
        this.m53Portrait = m53Portrait;
        this.ms1Portrait = ms1Portrait;
        this.vk10001pPortrait = vk10001pPortrait;
        this.amx40Portrait = amx40Portrait;
        this.t1Portrait = t1Portrait;

        loadItemImages();

        setLayout(new BorderLayout(10, 10));
        setSize(550, 500);
        setLocationRelativeTo(parent);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createItemsPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private void loadItemImages() {
        try {
            String shellsPath = "src/ObjectsOfInventory/Shells/";
            String treatmentPath = "src/ObjectsOfInventory/TreatmentAndRepair/";
            String weaponPath = "src/ObjectsOfInventory/Weapon/";

            // Снаряды
            File basicFile = new File(shellsPath + "20mmShell_based.png");
            if (basicFile.exists()) {
                Image originalImage = ImageIO.read(basicFile);
                basicShellIcon = new ImageIcon(originalImage);
            }

            File improved20mmFile = new File(shellsPath + "20mmShell_improved.png");
            if (improved20mmFile.exists()) {
                Image originalImage = ImageIO.read(improved20mmFile);
                improvedShellIcon = new ImageIcon(originalImage);
            }

            File improved25mmFile = new File(shellsPath + "25mmShell_based.png");
            if (improved25mmFile.exists()) {
                Image originalImage = ImageIO.read(improved25mmFile);
                twentyFiveMMShellIcon = new ImageIcon(originalImage);
            }

            File shell45mmFile = new File(shellsPath + "45mmShell_based.png");
            if (shell45mmFile.exists()) {
                Image originalImage = ImageIO.read(shell45mmFile);
                fortyFiveMMShellIcon = new ImageIcon(originalImage);
            }

            File shell47mmFile = new File(shellsPath + "47mmShell_based.png");
            if (shell47mmFile.exists()) {
                Image originalImage = ImageIO.read(shell47mmFile);
                fortySevenMMShellIcon = new ImageIcon(originalImage);
            }

            // 8mm снаряды
            File shell8mmFile = new File(shellsPath + "8mmShell_based.png");
            if (shell8mmFile.exists()) {
                Image originalImage = ImageIO.read(shell8mmFile);
                eightMMShellIcon = new ImageIcon(originalImage);
            }

            // Лечебные предметы
            File medkitFile = new File(treatmentPath + "MedicineChest.png");
            if (medkitFile.exists()) {
                Image img = ImageIO.read(medkitFile);
                medkitIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File bandageFile = new File(treatmentPath + "Bandage.png");
            if (bandageFile.exists()) {
                Image img = ImageIO.read(bandageFile);
                bandageIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File repairFile = new File(treatmentPath + "RepairKit.png");
            if (repairFile.exists()) {
                Image img = ImageIO.read(repairFile);
                repairKitIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File energyFile = new File(treatmentPath + "EnergyDrink.png");
            if (energyFile.exists()) {
                Image img = ImageIO.read(energyFile);
                energyDrinkIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File grenadeFile = new File(treatmentPath + "Grenade.png");
            if (grenadeFile.exists()) {
                Image img = ImageIO.read(grenadeFile);
                grenadeIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File breadFile = new File(treatmentPath + "ExpiredBread.png");
            if (breadFile.exists()) {
                Image img = ImageIO.read(breadFile);
                breadIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File extinguisherFile = new File(treatmentPath + "extinguisher.png");
            if (extinguisherFile.exists()) {
                Image img = ImageIO.read(extinguisherFile);
                extinguisherIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            // Оружие
            File weaponFile = new File(weaponPath + "2 cm Breda (I).png");
            if (weaponFile.exists()) {
                Image img = ImageIO.read(weaponFile);
                weaponIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon8mmFile = new File(weaponPath + "7,92 mm Mauser E.W. 141.png");
            if (weapon8mmFile.exists()) {
                Image img = ImageIO.read(weapon8mmFile);
                weapon8mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon25mmFile = new File(weaponPath + "25-mm-Canon-Raccourci-mle.-1934.png");
            if (weapon25mmFile.exists()) {
                Image img = ImageIO.read(weapon25mmFile);
                weapon25mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon13mmJapanFile = new File(weaponPath + "13 mm Autocannon Type Ho.png");
            if (weapon13mmJapanFile.exists()) {
                Image img = ImageIO.read(weapon13mmJapanFile);
                weapon13mmJapanIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon13mmFrenchFile = new File(weaponPath + "13,2 mm Hotchkiss mle. 1930.png");
            if (weapon13mmFrenchFile.exists()) {
                Image img = ImageIO.read(weapon13mmFrenchFile);
                weapon13mmFrenchIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon30mmFile = new File(weaponPath + "3 cm M.K. 103A.png");
            if (weapon30mmFile.exists()) {
                Image img = ImageIO.read(weapon30mmFile);
                weapon30mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon45mmFile = new File(weaponPath + "45 мм обр. 1932 г.png");
            if (weapon45mmFile.exists()) {
                Image img = ImageIO.read(weapon45mmFile);
                weapon45mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon47mmFile = new File(weaponPath + "47 mm SA35.png");
            if (weapon47mmFile.exists()) {
                Image img = ImageIO.read(weapon47mmFile);
                weapon47mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon76mmFile = new File(weaponPath + "76 мм Л-10С.png");
            if (weapon76mmFile.exists()) {
                Image img = ImageIO.read(weapon76mmFile);
                weapon76mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon128mmFile = new File(weaponPath + "12,8 cm Kw.K. L50.png");
            if (weapon128mmFile.exists()) {
                Image img = ImageIO.read(weapon128mmFile);
                weapon128mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображений предметов: " + e.getMessage());
        }
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(30, 30, 40));

        JPanel portraitPanel = new JPanel(new BorderLayout());
        portraitPanel.setBackground(new Color(30, 30, 40));

        JLabel portraitLabel = new JLabel();
        BufferedImage portrait = null;
        String unitName = "";

        if (activeUnit instanceof PlayerTank) {
            portrait = heroPortrait;
            unitName = "Leichttraktor";
        } else if (activeUnit instanceof FriendlyUnit) {
            FriendlyUnit friendly = (FriendlyUnit) activeUnit;
            unitName = friendly.name;
            if ("M53".equals(friendly.type)) portrait = m53Portrait;
            else if ("MS-1".equals(friendly.type)) portrait = ms1Portrait;
            else if ("VK10001P".equals(friendly.type)) {
                portrait = vk10001pPortrait;
            } else if ("AMX40".equals(friendly.type)) {
                portrait = amx40Portrait;
            } else if ("T1".equals(friendly.type)) {
                portrait = t1Portrait;
            }
        }

        if (portrait != null) {
            Image scaled = portrait.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            portraitLabel.setIcon(new ImageIcon(scaled));
        } else {
            portraitLabel.setText("🗑");
            portraitLabel.setFont(new Font("Arial", Font.BOLD, 36));
            portraitLabel.setForeground(Color.WHITE);
        }
        portraitPanel.add(portraitLabel, BorderLayout.CENTER);
        panel.add(portraitPanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(30, 30, 40));

        JLabel titleLabel = new JLabel(unitName + " открывает мусорный контейнер");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(255, 215, 0));

        JLabel locationLabel = new JLabel("📍 Клетка: [" + container.gridX + ", " + container.gridY + "] (размер 2x1)");
        locationLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        locationLabel.setForeground(Color.LIGHT_GRAY);

        infoLabel = new JLabel("Кликните на предмет, чтобы взять");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(Color.CYAN);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(locationLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(infoLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createItemsPanel() {
        itemsPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        itemsPanel.setBackground(new Color(20, 20, 30));
        itemsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        refreshItemsPanel();

        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBackground(new Color(20, 20, 30));
        scrollPane.getViewport().setBackground(new Color(20, 20, 30));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(20, 20, 30));
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    private ImageIcon getItemIcon(Item.ItemType type, Item item) {
        if (item instanceof AmmoItem) {
            AmmoItem ammoItem = (AmmoItem) item;
            Caliber caliber = ammoItem.getCaliber();
            boolean isImproved = ammoItem.isImproved();

            if (caliber == Caliber.CALIBER_20MM) {
                if (isImproved && improvedShellIcon != null) {
                    return new ImageIcon(improvedShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
                } else if (basicShellIcon != null) {
                    return new ImageIcon(basicShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
                }
            } else if (caliber == Caliber.CALIBER_25MM && twentyFiveMMShellIcon != null) {
                return new ImageIcon(twentyFiveMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if (caliber == Caliber.CALIBER_45MM && fortyFiveMMShellIcon != null) {
                return new ImageIcon(fortyFiveMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if ((caliber == Caliber.CALIBER_47MM) && fortySevenMMShellIcon != null) {
                return new ImageIcon(fortySevenMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if (caliber == Caliber.CALIBER_8MM && eightMMShellIcon != null) {
                return new ImageIcon(eightMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            }
        }

        switch (type) {
            case MEDKIT: return medkitIcon;
            case BANDAGE: return bandageIcon;
            case REPAIR_KIT: return repairKitIcon;
            case ENERGY_DRINK: return energyDrinkIcon;
            case GRENADE: return grenadeIcon;
            case BREAD: return breadIcon;
            case FIRE_EXTINGUISHER: return extinguisherIcon;
            case WEAPON: return weaponIcon;
            case WEAPON_8MM: return weapon8mmIcon;
            case WEAPON_13MM_JAPAN: return weapon13mmJapanIcon;
            case WEAPON_13MM_FRENCH: return weapon13mmFrenchIcon;
            case WEAPON_25MM: return weapon25mmIcon;
            case WEAPON_30MM: return weapon30mmIcon;
            case WEAPON_45MM: return weapon45mmIcon;
            case WEAPON_47MM_FRENCH: return weapon47mmIcon;
            case WEAPON_47MM_ITALIAN: return weapon47mmIcon;
            case WEAPON_76MM: return weapon76mmIcon;
            case WEAPON_128MM: return weapon128mmIcon;
            default: return null;
        }
    }

    private JPanel createItemCard(Item.ItemType type, int count, Item item) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setPreferredSize(new Dimension(250, 200));

        String displayName;
        if (type == Item.ItemType.WEAPON) {
            displayName = "2 cm Breda (I)";
        } else if (type == Item.ItemType.WEAPON_45MM) {
            displayName = "45 мм обр. 1932 г.";
        } else if (type == Item.ItemType.WEAPON_25MM) {
            displayName = "25mm Canon Raccourci mle. 1934";
        } else if (type == Item.ItemType.WEAPON_47MM_FRENCH) {
            displayName = "47 mm SA35";
        } else if (type == Item.ItemType.WEAPON_47MM_ITALIAN) {
            displayName = "Cannone da 47-32";
        } else if (type == Item.ItemType.WEAPON_8MM) {
            displayName = "7,92 mm Mauser E.W. 141";
        } else if (type == Item.ItemType.WEAPON_76MM) {
            displayName = "76 мм Л-10С";
        } else if (type == Item.ItemType.WEAPON_13MM_JAPAN) {
            displayName = "13 mm Autocannon Type Ho";
        } else if (type == Item.ItemType.WEAPON_13MM_FRENCH) {
            displayName = "13,2 mm Hotchkiss mle. 1930";
        } else if (type == Item.ItemType.WEAPON_30MM) {
            displayName = "3 cm M.K. 103A";
        } else if (type == Item.ItemType.WEAPON_128MM) {
            displayName = "12,8 cm Kw.K. L50";
        } else {
            displayName = type.name;
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        ImageIcon icon = getItemIcon(type, item);
        JLabel iconLabel;
        if (icon != null) {
            Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            iconLabel = new JLabel(new ImageIcon(scaledImage));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            iconLabel = new JLabel(type.icon, SwingConstants.CENTER);
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        }
        topPanel.add(iconLabel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(displayName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel countLabel = new JLabel("Доступно: " + count + " шт.", SwingConstants.CENTER);
        countLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        countLabel.setForeground(new Color(255, 215, 0));
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(nameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        centerPanel.add(countLabel);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        bottomPanel.setOpaque(false);

        int maxAmount = (type == Item.ItemType.WEAPON ||
                type == Item.ItemType.WEAPON_8MM ||
                type == Item.ItemType.WEAPON_13MM_JAPAN ||
                type == Item.ItemType.WEAPON_13MM_FRENCH ||
                type == Item.ItemType.WEAPON_25MM ||
                type == Item.ItemType.WEAPON_30MM ||
                type == Item.ItemType.WEAPON_45MM ||
                type == Item.ItemType.WEAPON_47MM_FRENCH ||
                type == Item.ItemType.WEAPON_47MM_ITALIAN ||
                type == Item.ItemType.WEAPON_76MM ||
                type == Item.ItemType.WEAPON_128MM) ? 1 : count;

        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, maxAmount, 1));
        amountSpinner.setPreferredSize(new Dimension(70, 30));
        amountSpinner.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton takeButton = new JButton("ВЗЯТЬ");
        takeButton.setFont(new Font("Arial", Font.BOLD, 11));
        takeButton.setBackground(new Color(0, 100, 200));
        takeButton.setForeground(Color.WHITE);
        takeButton.setFocusPainted(false);
        takeButton.setPreferredSize(new Dimension(80, 30));
        takeButton.addActionListener(e -> {
            int amount = (Integer) amountSpinner.getValue();
            takeItem(type, amount);
        });

        bottomPanel.add(amountSpinner);
        bottomPanel.add(takeButton);

        if (maxAmount > 1) {
            JButton takeAllButton = new JButton("ВСЁ");
            takeAllButton.setFont(new Font("Arial", Font.BOLD, 11));
            takeAllButton.setBackground(new Color(200, 100, 0));
            takeAllButton.setForeground(Color.WHITE);
            takeAllButton.setFocusPainted(false);
            takeAllButton.setPreferredSize(new Dimension(80, 30));
            takeAllButton.addActionListener(e -> takeItem(type, count));
            bottomPanel.add(takeAllButton);
        }

        card.add(topPanel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }

    // Добавьте эти методы в класс StorageDialog:

    private double getCurrentWeight() {
        if (activeUnit instanceof PlayerTank) {
            return ((PlayerTank) activeUnit).getInventory().getTotalWeight();
        } else if (activeUnit instanceof FriendlyUnit) {
            return ((FriendlyUnit) activeUnit).getInventory().getTotalWeight();
        }
        return 0;
    }

    private double getMaxCarryWeight() {
        if (activeUnit instanceof PlayerTank) {
            return ((PlayerTank) activeUnit).maxCarryWeight;
        } else if (activeUnit instanceof FriendlyUnit) {
            return ((FriendlyUnit) activeUnit).maxCarryWeight;
        }
        return 500.0;
    }

    private void takeItem(Item.ItemType type, int amount) {
        int taken = container.takeItem(type, amount);
        if (taken > 0) {
            Item itemToAdd;

            // Создаём временный предмет для расчёта веса
            if (type == Item.ItemType.WEAPON || type == Item.ItemType.WEAPON_8MM ||
                    type == Item.ItemType.WEAPON_13MM_JAPAN || type == Item.ItemType.WEAPON_13MM_FRENCH ||
                    type == Item.ItemType.WEAPON_25MM ||type == Item.ItemType.WEAPON_30MM ||
                    type == Item.ItemType.WEAPON_45MM || type == Item.ItemType.WEAPON_47MM_FRENCH ||
                    type == Item.ItemType.WEAPON_47MM_ITALIAN ||
                    type == Item.ItemType.WEAPON_76MM || type == Item.ItemType.WEAPON_128MM) {
                itemToAdd = new Item(type, 1);
            } else {
                itemToAdd = new Item(type, taken);
            }

            double itemWeight = itemToAdd.getTotalWeight();
            int requiredPoints = (int)Math.ceil(itemWeight);

            // Получаем текущие очки хода и вес
            int currentMovePoints = 0;
            double currentWeight = 0;
            double maxCarryWeight = 0;

            if (activeUnit instanceof PlayerTank) {
                PlayerTank p = (PlayerTank) activeUnit;
                currentMovePoints = p.movePoints;
                currentWeight = p.getInventory().getTotalWeight() + p.getEquipmentWeight();
                maxCarryWeight = p.maxCarryWeight;
            } else if (activeUnit instanceof FriendlyUnit) {
                FriendlyUnit f = (FriendlyUnit) activeUnit;
                currentMovePoints = f.movePoints;
                currentWeight = f.getInventory().getTotalWeight() + f.getEquipmentWeight();
                maxCarryWeight = f.maxCarryWeight;
            }

            // Проверка грузоподъёмности
            if (currentWeight + itemWeight > maxCarryWeight) {
                JOptionPane.showMessageDialog(this,
                        "❌ СЛИШКОМ ТЯЖЁЛЫЙ ПРЕДМЕТ!\n\n" +
                                "Превышена максимальная грузоподъёмность!\n" +
                                "Текущий вес: " + String.format("%.1f", currentWeight) + "/" + maxCarryWeight +
                                "\nВес предмета: " + itemWeight,
                        "Невозможно взять", JOptionPane.WARNING_MESSAGE);
                container.addItem(type, taken);
                return;
            }

            // Проверка очков хода
            if (currentMovePoints < requiredPoints) {
                JOptionPane.showMessageDialog(this,
                        "❌ НЕДОСТАТОЧНО ОЧКОВ ХОДА!\n\n" +
                                "Для подбора предмета нужно: " + requiredPoints + " очков хода\n" +
                                "У вас осталось: " + currentMovePoints + " очков хода",
                        "Невозможно взять", JOptionPane.WARNING_MESSAGE);
                container.addItem(type, taken);
                return;
            }

            // Тратим очки хода
            if (activeUnit instanceof PlayerTank) {
                ((PlayerTank) activeUnit).movePoints -= requiredPoints;
            } else if (activeUnit instanceof FriendlyUnit) {
                ((FriendlyUnit) activeUnit).movePoints -= requiredPoints;
            }

            // Добавляем предмет в инвентарь
            boolean added = false;
            if (activeUnit instanceof PlayerTank) {
                added = ((PlayerTank) activeUnit).getInventory().addItemToInventory(itemToAdd);
            } else if (activeUnit instanceof FriendlyUnit) {
                added = ((FriendlyUnit) activeUnit).getInventory().addItemToInventory(itemToAdd);
            }

            if (!added) {
                // Возвращаем очки хода, если не удалось добавить
                if (activeUnit instanceof PlayerTank) {
                    ((PlayerTank) activeUnit).movePoints += requiredPoints;
                } else if (activeUnit instanceof FriendlyUnit) {
                    ((FriendlyUnit) activeUnit).movePoints += requiredPoints;
                }
                JOptionPane.showMessageDialog(this,
                        "❌ Нет места в инвентаре!",
                        "Невозможно взять", JOptionPane.WARNING_MESSAGE);
                container.addItem(type, taken);
                return;
            }

            String unitName = (activeUnit instanceof PlayerTank) ? "Leichttraktor" : ((FriendlyUnit) activeUnit).name;
            JOptionPane.showMessageDialog(this,
                    "✅ " + unitName + " взял " + taken + " " + type.name() + "!\n\n" +
                            "⚖️ Вес предмета: " + itemWeight + " кг\n" +
                            "⭐ Потрачено очков хода: " + requiredPoints + "\n" +
                            "Осталось очков хода: " + currentMovePoints,
                    "Предмет взят", JOptionPane.INFORMATION_MESSAGE);

            refreshItemsPanel();

            if (container.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Тумбочка опустела!", "Информация", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        }
    }

    private void refreshItemsPanel() {
        System.out.println("=== refreshItemsPanel() ===");
        itemsPanel.removeAll();

        Map<Item.ItemType, Integer> items = container.getItems();

        System.out.println("  Обычных предметов: " + items.size());

        if (items.isEmpty()) {
            JLabel emptyLabel = new JLabel("Мусорный контейнер пуст", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            itemsPanel.add(emptyLabel);
            System.out.println("  Контейнер пуст");
        } else {
            for (Map.Entry<Item.ItemType, Integer> entry : items.entrySet()) {
                Item.ItemType type = entry.getKey();
                int count = entry.getValue();
                Item tempItem = new Item(type, count);
                JPanel itemCard = createItemCard(type, count, tempItem);
                itemsPanel.add(itemCard);
            }
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
        System.out.println("=== refreshItemsPanel() завершён ===");
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 40));

        JButton closeButton = new JButton("✖ ЗАКРЫТЬ");
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton);

        return panel;
    }
}