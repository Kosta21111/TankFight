package ui;

import entities.*;
import inventory.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Map;
import java.util.HashMap;

public class StorageDialog extends JDialog {
    private Object activeUnit;
    private StorageChest chest;
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
    private static ImageIcon eightMMShellIcon = null;

    private static ImageIcon thirteenMMShellIcon = null;
    private static ImageIcon improvedShellIcon = null;
    private static ImageIcon twentyFiveMMShellIcon = null;
    private static ImageIcon thirtySevenMMShellIcon = null;
    private static ImageIcon thirtyMMShellIcon = null;

    private static ImageIcon fortyFiveMMShellIcon = null;
    private static ImageIcon fortySevenMMShellIcon = null;
    private static ImageIcon SeventySixMMShellIcon = null;
    private static ImageIcon OneHundredFiveMMShellIcon = null;
    private static ImageIcon ThoHundredThreeMMShellIcon = null;
    private static ImageIcon medkitIcon = null;
    private static ImageIcon bandageIcon = null;
    private static ImageIcon repairKitIcon = null;
    private static ImageIcon energyDrinkIcon = null;
    private static ImageIcon grenadeIcon = null;
    private static ImageIcon breadIcon = null;
    private static ImageIcon extinguisherIcon = null;
    private static ImageIcon weaponIcon = null;
    private static ImageIcon weapon8mmIcon = null;
    private static ImageIcon weapon13mmJapanIcon = null;
    private static ImageIcon weapon13mmFrenchIcon = null;
    private static ImageIcon weapon30mmIcon = null;
    private static ImageIcon weapon37ItalianIcon = null;
    private static ImageIcon weapon37AmericanIcon = null;
    private static ImageIcon weapon37SwedenIcon = null;
    private static ImageIcon weapon45mmIcon = null;
    private static ImageIcon weapon25mmIcon = null;
    private static ImageIcon weapon47mmItalianIcon = null;
    private static ImageIcon weapon47mmFrenchIcon = null;
    private static ImageIcon weapon76mmIcon = null;
    private static ImageIcon weapon105mmIcon = null;
    private static ImageIcon weapon128mmIcon = null;
    private static ImageIcon weapon203mmIcon = null;
    private Map<Door.DoorColor, ImageIcon> keyIcons = new HashMap<>();

    public StorageDialog(JFrame parent, Object activeUnit, StorageChest chest,
                         BufferedImage heroPortrait, BufferedImage m53Portrait,
                         BufferedImage ms1Portrait, BufferedImage vk10001pPortrait,
                         BufferedImage amx40Portrait, BufferedImage t1Portrait) {
        super(parent, "📦 Складская тумбочка", true);
        this.activeUnit = activeUnit;
        this.chest = chest;
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
            String keysPath = "src/ObjectsOfInventory/Key/";  // ← ДОБАВЬТЕ

            // Снаряды
            File basicFile = new File(shellsPath + "20mmShell_based.png");
            if (basicFile.exists()) {
                Image originalImage = ImageIO.read(basicFile);
                basicShellIcon = new ImageIcon(originalImage);
            }

            // 8mm снаряды (Mauser)
            File shell8mmFile = new File(shellsPath + "8mmShell_based.png");
            if (shell8mmFile.exists()) {
                Image originalImage = ImageIO.read(shell8mmFile);
                eightMMShellIcon = new ImageIcon(originalImage);
                System.out.println("✅ Загружена иконка 8mm снарядов");
            } else {
                System.err.println("❌ Не найден файл: " + shellsPath + "8mmShell_based.png");
            }

            File improved20mmFile = new File(shellsPath + "20mmShell_improved.png");
            if (improved20mmFile.exists()) {
                Image originalImage = ImageIO.read(improved20mmFile);
                improvedShellIcon = new ImageIcon(originalImage);
            }

            File shell30mmFile = new File(shellsPath + "30mmShell_based.png");
            if (shell30mmFile.exists()) {
                Image originalImage = ImageIO.read(shell30mmFile);
                thirtyMMShellIcon = new ImageIcon(originalImage);
            }

            File shell37mmItalianFile = new File(shellsPath + "37mmShell_based.png");
            if (shell37mmItalianFile.exists()) {
                Image originalImage = ImageIO.read(shell37mmItalianFile);
                thirtySevenMMShellIcon = new ImageIcon(originalImage);
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

            File shell25mmFile = new File(shellsPath + "25mmShell_based.png");
            if (shell25mmFile.exists()) {
                Image originalImage = ImageIO.read(shell25mmFile);
                twentyFiveMMShellIcon = new ImageIcon(originalImage);
            }

            // Французское 47-мм орудие
            File weapon47mmFrenchFile = new File(weaponPath + "47 mm SA35.png");
            if (weapon47mmFrenchFile.exists()) {
                Image img = ImageIO.read(weapon47mmFrenchFile);
                weapon47mmFrenchIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            // Итальянское 47-мм орудие
            File weapon47mmItalianFile = new File(weaponPath + "Cannone da 47-32.png");
            if (weapon47mmItalianFile.exists()) {
                Image img = ImageIO.read(weapon47mmItalianFile);
                weapon47mmItalianIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon25mmFrenchFile = new File(weaponPath + "25-mm-Canon-Raccourci-mle.-1934.png");
            if (weapon25mmFrenchFile.exists()) {
                Image img = ImageIO.read(weapon25mmFrenchFile);
                weapon25mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File shell76mmFile = new File(shellsPath + "76mmShell_based.png");
            if (shell76mmFile.exists()) {
                Image originalImage = ImageIO.read(shell76mmFile);
                SeventySixMMShellIcon = new ImageIcon(originalImage);
            }

            File shell105mmFile = new File(shellsPath + "105mmShell_based.png");
            if (shell105mmFile.exists()) {
                Image originalImage = ImageIO.read(shell105mmFile);
                OneHundredFiveMMShellIcon = new ImageIcon(originalImage);
                System.out.println("✅ Загружена иконка 105mm снарядов");
            }

            File shell203mmFile = new File(shellsPath + "203mmShell_HESH.png");
            if (shell203mmFile.exists()) {
                Image originalImage = ImageIO.read(shell203mmFile);
                ThoHundredThreeMMShellIcon = new ImageIcon(originalImage);
            }

            // 13mm снаряды
            File shell13mmFile = new File(shellsPath + "13mmShell_based.png");
            if (shell13mmFile.exists()) {
                Image originalImage = ImageIO.read(shell13mmFile);
                thirteenMMShellIcon = new ImageIcon(originalImage);
                System.out.println("✅ Загружена иконка 13mm снарядов");
            } else {
                System.err.println("❌ Не найден файл: " + shellsPath + "13mmShell_based.png");
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

            File weapon37ItalianFile = new File(weaponPath + "Cannone da 37-40.png");
            if (weapon37ItalianFile.exists()) {
                Image img = ImageIO.read(weapon37ItalianFile);
                weapon37ItalianIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon37AmericanFile = new File(weaponPath + "37 mm Semiautomatic Gun M1924.png");
            if (weapon37AmericanFile.exists()) {
                Image img = ImageIO.read(weapon37AmericanFile);
                weapon37AmericanIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon37SwedenFile = new File(weaponPath + "37 mm kan m-38-49 strv.png");
            if (weapon37SwedenFile.exists()) {
                Image img = ImageIO.read(weapon37SwedenFile);
                weapon37SwedenIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon45mmFile = new File(weaponPath + "45 мм обр. 1932 г.png");
            if (weapon45mmFile.exists()) {
                Image img = ImageIO.read(weapon45mmFile);
                weapon45mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon76mmFile = new File(weaponPath + "76 мм Л-10С.png");
            if (weapon76mmFile.exists()) {
                Image img = ImageIO.read(weapon76mmFile);
                weapon76mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon105mmFile = new File(weaponPath + "10,5 cm StuH 42 L28.png");
            if (weapon105mmFile.exists()) {
                Image img = ImageIO.read(weapon105mmFile);
                weapon105mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon128mmFile = new File(weaponPath + "12,8 cm Kw.K. L50.png");
            if (weapon128mmFile.exists()) {
                Image img = ImageIO.read(weapon128mmFile);
                weapon128mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon203mmFile = new File(weaponPath + "8-inch Howitzer M47.png");
            if (weapon203mmFile.exists()) {
                Image img = ImageIO.read(weapon203mmFile);
                weapon203mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            for (Door.DoorColor color : Door.DoorColor.values()) {
                String fileName = keysPath + color.name() + "Key.png";
                File keyFile = new File(fileName);
                if (keyFile.exists()) {
                    Image img = ImageIO.read(keyFile);
                    // Сохраняем в Map для быстрого доступа
                    keyIcons.put(color, new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
                    System.out.println("✅ Загружена иконка ключа: " + color.name());
                } else {
                    System.err.println("❌ Не найден файл ключа: " + fileName);
                }
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
            if ("M53".equals(friendly.type)) {
                portrait = m53Portrait;
            } else if ("MS-1".equals(friendly.type)) {
                portrait = ms1Portrait;
            } else if ("VK10001P".equals(friendly.type)) {  // ← ДОБАВИТЬ
                portrait = vk10001pPortrait;
            } else if ("AMX40".equals(friendly.type)) {    // ← ДОБАВИТЬ
                portrait = amx40Portrait;
            } else if ("T1".equals(friendly.type)) {    // ← ДОБАВИТЬ
                portrait = t1Portrait;
            }
        }

        if (portrait != null) {
            Image scaled = portrait.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            portraitLabel.setIcon(new ImageIcon(scaled));
        } else {
            portraitLabel.setText("📦");
            portraitLabel.setFont(new Font("Arial", Font.BOLD, 36));
            portraitLabel.setForeground(Color.WHITE);
        }
        portraitPanel.add(portraitLabel, BorderLayout.CENTER);
        panel.add(portraitPanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(30, 30, 40));

        JLabel titleLabel = new JLabel(unitName + " открывает тумбочку");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(255, 215, 0));

        JLabel locationLabel = new JLabel("📍 Клетка: [" + chest.gridX + ", " + chest.gridY + "]");
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
            } else if (caliber == Caliber.CALIBER_45MM && fortyFiveMMShellIcon != null) {
                return new ImageIcon(fortyFiveMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if ((caliber == Caliber.CALIBER_47MM) && fortySevenMMShellIcon != null) {
                return new ImageIcon(fortySevenMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if (caliber == Caliber.CALIBER_8MM && eightMMShellIcon != null) {
                return new ImageIcon(eightMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if (caliber == Caliber.CALIBER_25MM && twentyFiveMMShellIcon != null) {
                return new ImageIcon(twentyFiveMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if (caliber == Caliber.CALIBER_37MM && thirtySevenMMShellIcon != null) {
                return new ImageIcon(thirtySevenMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if (caliber == Caliber.CALIBER_13MM && thirteenMMShellIcon != null) {  // ← ДОБАВИТЬ
                return new ImageIcon(thirteenMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if (caliber == Caliber.CALIBER_30MM) {
                return new ImageIcon(thirtyMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if (caliber == Caliber.CALIBER_76MM && SeventySixMMShellIcon != null) {
                return new ImageIcon(SeventySixMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if (caliber == Caliber.CALIBER_105MM  && OneHundredFiveMMShellIcon != null) {
                return new ImageIcon(OneHundredFiveMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            } else if (caliber == Caliber.CALIBER_128MM) {
                // можно добавить иконку для 128mm
            } else if (caliber == Caliber.CALIBER_203MM) {
                return new ImageIcon(ThoHundredThreeMMShellIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
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
            case WEAPON_37MM_ITALIAN: return weapon37ItalianIcon;
            case WEAPON_37MM_AMERICAN: return weapon37AmericanIcon;
            case WEAPON_37MM_SWEDEN: return weapon37SwedenIcon;
            case WEAPON_45MM: return weapon45mmIcon;
            case WEAPON_47MM_FRENCH: return weapon47mmFrenchIcon;
            case WEAPON_47MM_ITALIAN: return weapon47mmItalianIcon;
            case WEAPON_76MM: return weapon76mmIcon;
            case WEAPON_105MM: return weapon105mmIcon;
            case WEAPON_128MM: return weapon128mmIcon;
            case WEAPON_203MM: return weapon203mmIcon;
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
        } else if (type == Item.ItemType.WEAPON_13MM_JAPAN) {
            displayName = "13 mm Autocannon Type Ho";
        } else if (type == Item.ItemType.WEAPON_13MM_FRENCH) {
            displayName = "13,2 mm Hotchkiss mle. 1930";
        } else if (type == Item.ItemType.WEAPON_76MM) {
            displayName = "76 мм Л-10С";
        } else if (type == Item.ItemType.WEAPON_30MM) {
            displayName = "3 cm M.K. 103A";
        } else if (type == Item.ItemType.WEAPON_37MM_ITALIAN) {
            displayName = "Cannone da 37-40";
        } else if (type == Item.ItemType.WEAPON_37MM_AMERICAN) {
            displayName = "37 mm Semiautomatic Gun M1924";
        } else if (type == Item.ItemType.WEAPON_37MM_SWEDEN) {
            displayName = "37 mm kan m-38-49 strv";
        } else if (type == Item.ItemType.WEAPON_105MM) {
            displayName = "10,5 cm StuH 42 L28";
        } else if (type == Item.ItemType.WEAPON_128MM) {
            displayName = "12,8 cm Kw.K. L50";
        } else if (type == Item.ItemType.WEAPON_203MM) {
            displayName = "8-inch Howitzer M47";
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
                type == Item.ItemType.WEAPON_37MM_ITALIAN ||
                type == Item.ItemType.WEAPON_37MM_AMERICAN ||
                type == Item.ItemType.WEAPON_37MM_SWEDEN ||
                type == Item.ItemType.WEAPON_45MM ||
                type == Item.ItemType.WEAPON_47MM_FRENCH ||
                type == Item.ItemType.WEAPON_47MM_ITALIAN ||
                type == Item.ItemType.WEAPON_76MM ||
                type == Item.ItemType.WEAPON_105MM ||
                type == Item.ItemType.WEAPON_128MM ||
                type == Item.ItemType.WEAPON_203MM) ? 1 : count;

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

    private JPanel createAmmoCard(Caliber caliber, int count, AmmoItem ammoItem) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setPreferredSize(new Dimension(250, 200));

        String displayName;
        if (caliber == Caliber.CALIBER_20MM) {
            displayName = "20mm снаряды";
        } else if (caliber == Caliber.CALIBER_8MM) {
            displayName = "8mm снаряды Mauser";
        } else if (caliber == Caliber.CALIBER_13MM) {
            displayName = "13mm снаряды";
        } else if (caliber == Caliber.CALIBER_25MM) {
            displayName = "25mm снаряды";
        } else if (caliber == Caliber.CALIBER_45MM) {
            displayName = "45mm снаряды";
        } else if (caliber == Caliber.CALIBER_47MM) {
            displayName = "47mm снаряды";
        } else if (caliber == Caliber.CALIBER_30MM) {
            displayName = "30mm снаряды";
        } else if (caliber == Caliber.CALIBER_37MM) {
            displayName = "37mm снаряды";
        } else if (caliber == Caliber.CALIBER_76MM) {
            displayName = "76mm снаряды";
        } else if (caliber == Caliber.CALIBER_105MM) {
            displayName = "105mm снаряды";
        } else if (caliber == Caliber.CALIBER_128MM) {
            displayName = "128mm снаряды";
        } else if (caliber == Caliber.CALIBER_203MM) {
            displayName = "203mm снаряды";
        } else {
            displayName = caliber.name + " снаряды";
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        ImageIcon ammoIcon = getItemIcon(Item.ItemType.AMMO, ammoItem);
        JLabel iconLabel;
        if (ammoIcon != null) {
            Image scaledImage = ammoIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            iconLabel = new JLabel(new ImageIcon(scaledImage));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            String icon = caliber == Caliber.CALIBER_20MM ? "🔫" :
                    (caliber == Caliber.CALIBER_8MM ? "🔫" :
                            (caliber == Caliber.CALIBER_13MM ? "🔫" :  // ← ДОБАВИТЬ
                                    (caliber == Caliber.CALIBER_25MM ? "🔫" :
                                            (caliber == Caliber.CALIBER_37MM ? "🔫" :
                                        (caliber == Caliber.CALIBER_45MM ? "🔫" :
                                                (caliber == Caliber.CALIBER_47MM ? "🔫" :
                                                        (caliber == Caliber.CALIBER_30MM ? "🔫" :
                                                                (caliber == Caliber.CALIBER_76MM ? "💥" :
                                                                        (caliber == Caliber.CALIBER_105MM ? "🔫" :
                                                                        (caliber == Caliber.CALIBER_128MM ? "💥" :
                                                                                (caliber == Caliber.CALIBER_203MM ? "💥" :"💣")))))))))));
            iconLabel = new JLabel(icon, SwingConstants.CENTER);
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

        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, count, 1));
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
            takeAmmo(caliber, amount);
        });

        JButton takeAllButton = new JButton("ВСЁ");
        takeAllButton.setFont(new Font("Arial", Font.BOLD, 11));
        takeAllButton.setBackground(new Color(200, 100, 0));
        takeAllButton.setForeground(Color.WHITE);
        takeAllButton.setFocusPainted(false);
        takeAllButton.setPreferredSize(new Dimension(80, 30));
        takeAllButton.addActionListener(e -> takeAmmo(caliber, count));

        bottomPanel.add(amountSpinner);
        bottomPanel.add(takeButton);
        bottomPanel.add(takeAllButton);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createKeyCard(Door.DoorColor color, int count) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setPreferredSize(new Dimension(250, 200));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // ===== ИСПОЛЬЗУЕМ ЗАГРУЖЕННУЮ ИКОНКУ =====
        JLabel iconLabel;
        ImageIcon icon = keyIcons.get(color);
        if (icon != null) {
            Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            iconLabel = new JLabel(new ImageIcon(scaledImage));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            // Запасной вариант - эмодзи
            iconLabel = new JLabel("🔑", SwingConstants.CENTER);
            iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 60));
            iconLabel.setForeground(color.color);
        }
        topPanel.add(iconLabel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(color.displayName + " ключ", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
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

        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, count, 1));
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
            takeKey(color, amount);
        });

        JButton takeAllButton = new JButton("ВСЁ");
        takeAllButton.setFont(new Font("Arial", Font.BOLD, 11));
        takeAllButton.setBackground(new Color(200, 100, 0));
        takeAllButton.setForeground(Color.WHITE);
        takeAllButton.setFocusPainted(false);
        takeAllButton.setPreferredSize(new Dimension(80, 30));
        takeAllButton.addActionListener(e -> takeKey(color, count));

        bottomPanel.add(amountSpinner);
        bottomPanel.add(takeButton);
        bottomPanel.add(takeAllButton);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }

    private void takeItem(Item.ItemType type, int amount) {
        int taken = chest.takeItem(type, amount);
        if (taken > 0) {
            Item itemToAdd;

            // Создаём временный предмет для расчёта веса
            if (type == Item.ItemType.WEAPON || type == Item.ItemType.WEAPON_8MM ||
                    type == Item.ItemType.WEAPON_13MM_JAPAN || type == Item.ItemType.WEAPON_13MM_FRENCH ||
                    type == Item.ItemType.WEAPON_25MM ||
                    type == Item.ItemType.WEAPON_30MM ||
                    type == Item.ItemType.WEAPON_37MM_ITALIAN || type == Item.ItemType.WEAPON_37MM_AMERICAN ||
                    type == Item.ItemType.WEAPON_37MM_SWEDEN ||
                    type == Item.ItemType.WEAPON_45MM || type == Item.ItemType.WEAPON_47MM_FRENCH ||
                    type == Item.ItemType.WEAPON_47MM_ITALIAN ||
                    type == Item.ItemType.WEAPON_76MM ||
                    type == Item.ItemType.WEAPON_128MM || type == Item.ItemType.WEAPON_128MM ||
                    type == Item.ItemType.WEAPON_203MM) {
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
                chest.addItem(type, taken);
                return;
            }

            // Проверка очков хода
            if (currentMovePoints < requiredPoints) {
                JOptionPane.showMessageDialog(this,
                        "❌ НЕДОСТАТОЧНО ОЧКОВ ХОДА!\n\n" +
                                "Для подбора предмета нужно: " + requiredPoints + " очков хода\n" +
                                "У вас осталось: " + currentMovePoints + " очков хода",
                        "Невозможно взять", JOptionPane.WARNING_MESSAGE);
                chest.addItem(type, taken);
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
                chest.addItem(type, taken);
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

            if (chest.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Тумбочка опустела!", "Информация", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        }
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

    private void refreshItemsPanel() {
        System.out.println("=== refreshItemsPanel() ===");
        itemsPanel.removeAll();

        Map<Item.ItemType, Integer> items = chest.getItems();
        Map<Caliber, Integer> ammo = chest.getAmmo();
        Map<Door.DoorColor, Integer> keys = chest.getKeys();  // ← ДОБАВЬТЕ ЭТУ СТРОКУ

        System.out.println("  Обычных предметов: " + items.size());
        System.out.println("  Типов снарядов: " + ammo.size());
        System.out.println("  Ключей: " + keys.size());

        boolean hasItems = !items.isEmpty() || !ammo.isEmpty() || !keys.isEmpty();  // ← ИСПРАВЛЕНО

        if (!hasItems) {
            JLabel emptyLabel = new JLabel("Тумбочка пуста", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            itemsPanel.add(emptyLabel);
            System.out.println("  Тумбочка пуста");
        } else {
            // Обычные предметы
            for (Map.Entry<Item.ItemType, Integer> entry : items.entrySet()) {
                Item.ItemType type = entry.getKey();
                int count = entry.getValue();
                Item tempItem = new Item(type, count);
                JPanel itemCard = createItemCard(type, count, tempItem);
                itemsPanel.add(itemCard);
            }

            // Снаряды
            for (Map.Entry<Caliber, Integer> entry : ammo.entrySet()) {
                Caliber caliber = entry.getKey();
                int count = entry.getValue();
                AmmoItem tempAmmo = new AmmoItem(caliber, count);
                JPanel ammoCard = createAmmoCard(caliber, count, tempAmmo);
                itemsPanel.add(ammoCard);
            }

            // ===== КЛЮЧИ =====
            for (Map.Entry<Door.DoorColor, Integer> entry : keys.entrySet()) {
                Door.DoorColor color = entry.getKey();
                int count = entry.getValue();
                JPanel keyCard = createKeyCard(color, count);
                itemsPanel.add(keyCard);
            }
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
        System.out.println("=== refreshItemsPanel() завершён ===");
    }

    private void takeAmmo(Caliber caliber, int amount) {
        int taken = chest.takeAmmo(caliber, amount);
        if (taken > 0) {
            int maxStackSize = caliber.getMaxStackSize();
            int remaining = taken;

            // Создаём временный предмет для расчёта веса
            AmmoItem tempAmmo = new AmmoItem(caliber, taken);
            double itemWeight = tempAmmo.getTotalWeight();
            int requiredPoints = (int)Math.ceil(itemWeight);

            // Получаем текущие очки хода
            int currentMovePoints = 0;
            if (activeUnit instanceof PlayerTank) {
                currentMovePoints = ((PlayerTank) activeUnit).movePoints;
            } else if (activeUnit instanceof FriendlyUnit) {
                currentMovePoints = ((FriendlyUnit) activeUnit).movePoints;
            }

            // Проверка очков хода
            if (currentMovePoints < requiredPoints) {
                JOptionPane.showMessageDialog(this,
                        "❌ НЕДОСТАТОЧНО ОЧКОВ ХОДА!\n\n" +
                                "Для подбора снарядов нужно: " + requiredPoints + " очков хода\n" +
                                "У вас осталось: " + currentMovePoints + " очков хода",
                        "Невозможно взять", JOptionPane.WARNING_MESSAGE);
                chest.addAmmo(caliber, taken);
                return;
            }

            // Тратим очки хода
            if (activeUnit instanceof PlayerTank) {
                ((PlayerTank) activeUnit).movePoints -= requiredPoints;
            } else if (activeUnit instanceof FriendlyUnit) {
                ((FriendlyUnit) activeUnit).movePoints -= requiredPoints;
            }

            // Добавляем снаряды в инвентарь
            while (remaining > 0) {
                int stackSize = Math.min(remaining, maxStackSize);
                AmmoItem ammoItem = new AmmoItem(caliber, stackSize);

                if (activeUnit instanceof PlayerTank) {
                    ((PlayerTank) activeUnit).getInventory().addAmmoItem(ammoItem);
                } else if (activeUnit instanceof FriendlyUnit) {
                    ((FriendlyUnit) activeUnit).getInventory().addAmmoItem(ammoItem);
                }
                remaining -= stackSize;
            }

            String unitName = (activeUnit instanceof PlayerTank) ? "Leichttraktor" : ((FriendlyUnit) activeUnit).name;
            JOptionPane.showMessageDialog(this,
                    "✅ " + unitName + " взял " + taken + " " + caliber.name + " снарядов!\n\n" +
                            "⚖️ Вес: " + itemWeight + " кг\n" +
                            "⭐ Потрачено очков хода: " + requiredPoints,
                    "Предмет взят", JOptionPane.INFORMATION_MESSAGE);

            refreshItemsPanel();

            if (chest.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Тумбочка опустела!", "Информация", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        }
    }

    private void takeKey(Door.DoorColor color, int amount) {
        int taken = chest.takeKey(color, amount);
        if (taken > 0) {
            // Создаём ключ для добавления в инвентарь
            KeyItem keyItem = new KeyItem(color, taken);
            double itemWeight = keyItem.getTotalWeight();
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
                chest.addKey(color, taken);  // возвращаем ключ обратно
                return;
            }

            // Проверка очков хода
            if (currentMovePoints < requiredPoints) {
                JOptionPane.showMessageDialog(this,
                        "❌ НЕДОСТАТОЧНО ОЧКОВ ХОДА!\n\n" +
                                "Для подбора ключа нужно: " + requiredPoints + " очков хода\n" +
                                "У вас осталось: " + currentMovePoints + " очков хода",
                        "Невозможно взять", JOptionPane.WARNING_MESSAGE);
                chest.addKey(color, taken);
                return;
            }

            // Тратим очки хода
            if (activeUnit instanceof PlayerTank) {
                ((PlayerTank) activeUnit).movePoints -= requiredPoints;
            } else if (activeUnit instanceof FriendlyUnit) {
                ((FriendlyUnit) activeUnit).movePoints -= requiredPoints;
            }

            // Добавляем ключ в инвентарь
            boolean added = false;
            if (activeUnit instanceof PlayerTank) {
                added = ((PlayerTank) activeUnit).getInventory().addItemToInventory(keyItem);
            } else if (activeUnit instanceof FriendlyUnit) {
                added = ((FriendlyUnit) activeUnit).getInventory().addItemToInventory(keyItem);
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
                chest.addKey(color, taken);
                return;
            }

            String unitName = (activeUnit instanceof PlayerTank) ? "Leichttraktor" : ((FriendlyUnit) activeUnit).name;
            JOptionPane.showMessageDialog(this,
                    "✅ " + unitName + " взял " + taken + " " + color.displayName + " ключ!\n\n" +
                            "⚖️ Вес предмета: " + itemWeight + " кг\n" +
                            "⭐ Потрачено очков хода: " + requiredPoints,
                    "Ключ взят", JOptionPane.INFORMATION_MESSAGE);

            refreshItemsPanel();

            if (chest.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Тумбочка опустела!", "Информация", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        }
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