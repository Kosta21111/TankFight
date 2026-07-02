package ui;

import entities.*;
import inventory.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.*;

import javax.swing.border.TitledBorder;

public class TradeInventoryDialog extends JDialog {
    private Object leftUnit;
    private Object rightUnit;
    private PlayerTank player;
    private GamePanel gamePanel;
    private JPanel leftGrid;
    private JPanel rightGrid;
    private JLabel leftNameLabel;
    private JLabel rightNameLabel;
    private JLabel infoLabel;
    private int selectedLeftX = -1, selectedLeftY = -1;
    private int selectedRightX = -1, selectedRightY = -1;
    private boolean isSelectingFromLeft = true;

    private BufferedImage heroPortrait;
    private BufferedImage m53Portrait;
    private BufferedImage ms1Portrait;
    private BufferedImage vk10001pPortrait;
    private BufferedImage amx40Portrait;
    private BufferedImage t1Portrait;

    private static ImageIcon eightMMShellIcon = null;
    private static ImageIcon basicShellIcon = null;
    private static ImageIcon improvedShellIcon = null;
    private static ImageIcon fortyFiveMMShellIcon = null;
    private static ImageIcon fortySevenMMShellIcon = null;

    private static ImageIcon twentyFiveMMShellIcon = null;
    private static ImageIcon thirtySevenMMShellIcon = null;
    private static ImageIcon thirteenMMShellIcon = null;
    private static ImageIcon thirtyMMShellIcon = null;
    private static ImageIcon seventySixMMShellIcon = null;

    private static ImageIcon OneHundredFiveMMShellIcon = null;
    private static ImageIcon oneTwentyEightMMShellIcon = null;

    private static ImageIcon weaponIcon = null;
    private static ImageIcon weapon8mmIcon = null;
    private static ImageIcon weapon13mmJapanIcon = null;
    private static ImageIcon weapon13mmFrenchIcon = null;
    private static ImageIcon weapon25mmIcon = null;
    private static ImageIcon weapon30mmIcon = null;
    private static ImageIcon weapon37ItalianIcon = null;
    private static ImageIcon weapon37AmericanIcon = null;
    private static ImageIcon weapon37SwedenIcon = null;
    private static ImageIcon weapon45mmIcon = null;
    private static ImageIcon weapon47mmFrenchIcon = null;
    private static ImageIcon weapon47mmItalianIcon = null;
    private static ImageIcon weapon76mmIcon = null;
    private static ImageIcon weapon105mmIcon = null;
    private static ImageIcon weapon128mmIcon = null;
    private static ImageIcon weapon203mmIcon = null;
    private static ImageIcon medkitIcon = null;
    private static ImageIcon bandageIcon = null;
    private static ImageIcon repairKitIcon = null;
    private static ImageIcon energyDrinkIcon = null;
    private static ImageIcon grenadeIcon = null;
    private static ImageIcon breadIcon = null;
    private static ImageIcon extinguisherIcon = null;

    // Кэш для хранения ссылок на ячейки
    private Map<String, JPanel> leftSlotComponents = new HashMap<>();
    private Map<String, JPanel> rightSlotComponents = new HashMap<>();

    public TradeInventoryDialog(JFrame parent, Object unitA, Object unitB, PlayerTank player,
                                BufferedImage heroPortrait, BufferedImage m53Portrait,
                                BufferedImage ms1Portrait, BufferedImage vk10001pPortrait,
                                BufferedImage amx40Portrait, BufferedImage t1Portrait, GamePanel gamePanel) {
        super(parent, "Обмен предметами", true);
        this.leftUnit = unitA;
        this.rightUnit = unitB;
        this.player = player;
        this.gamePanel = gamePanel;
        this.heroPortrait = heroPortrait;
        this.m53Portrait = m53Portrait;
        this.ms1Portrait = ms1Portrait;
        this.vk10001pPortrait = vk10001pPortrait;
        this.amx40Portrait = amx40Portrait;
        this.t1Portrait = t1Portrait;

        loadItemImages();

        setLayout(new BorderLayout(10, 10));
        setSize(1000, 650);
        setLocationRelativeTo(parent);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        refreshGrids();
    }

    private void loadItemImages() {
        try {
            String shellsPath = "src/ObjectsOfInventory/Shells/";
            String treatmentPath = "src/ObjectsOfInventory/TreatmentAndRepair/";
            String weaponPath = "src/ObjectsOfInventory/Weapon/";

            // СНАРЯДЫ
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

            File shell37mmFile = new File(shellsPath + "37mmShell_based.png");
            if (shell37mmFile.exists()) {
                Image originalImage = ImageIO.read(shell37mmFile);
                thirtySevenMMShellIcon = new ImageIcon(originalImage);
            }

            File shell13mmFile = new File(shellsPath + "13mmShell_based.png");
            if (shell13mmFile.exists()) {
                Image originalImage = ImageIO.read(shell13mmFile);
                thirteenMMShellIcon = new ImageIcon(originalImage);
            }

            File shell30mmFile = new File(shellsPath + "30mmShell_based.png");
            if (shell30mmFile.exists()) {
                Image originalImage = ImageIO.read(shell30mmFile);
                thirtyMMShellIcon = new ImageIcon(originalImage);
            }

            File shell76mmFile = new File(shellsPath + "76mmShell_based.png");
            if (shell76mmFile.exists()) {
                Image originalImage = ImageIO.read(shell76mmFile);
                seventySixMMShellIcon = new ImageIcon(originalImage);
            }

            File shell105mmFile = new File(shellsPath + "105mmShell_based.png");
            if (shell105mmFile.exists()) {
                Image originalImage = ImageIO.read(shell105mmFile);
                OneHundredFiveMMShellIcon = new ImageIcon(originalImage);
            }

            File shell203mmFile = new File(shellsPath + "203mmShell_HESH.png");
            if (shell203mmFile.exists()) {
                Image originalImage = ImageIO.read(shell203mmFile);
                seventySixMMShellIcon = new ImageIcon(originalImage);
            }

            File shell128mmFile = new File(shellsPath + "128mmShell_based.png");
            if (shell128mmFile.exists()) {
                Image originalImage = ImageIO.read(shell128mmFile);
                oneTwentyEightMMShellIcon = new ImageIcon(originalImage);
            }

            File shell8mmFile = new File(shellsPath + "8mmShell_based.png");
            if (shell8mmFile.exists()) {
                Image originalImage = ImageIO.read(shell8mmFile);
                eightMMShellIcon = new ImageIcon(originalImage);
            }

            // ЛЕЧЕБНЫЕ ПРЕДМЕТЫ
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

            // ===== ОРУЖИЕ =====
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

            File weapon25mmFile = new File(weaponPath + "25-mm-Canon-Raccourci-mle.-1934.png");
            if (weapon25mmFile.exists()) {
                Image img = ImageIO.read(weapon25mmFile);
                weapon25mmIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
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

            File weapon47mmFrenchFile = new File(weaponPath + "47 mm SA35.png");
            if (weapon47mmFrenchFile.exists()) {
                Image img = ImageIO.read(weapon47mmFrenchFile);
                weapon47mmFrenchIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            }

            File weapon47mmItalianFile = new File(weaponPath + "Cannone da 47-32.png");
            if (weapon47mmItalianFile.exists()) {
                Image img = ImageIO.read(weapon47mmItalianFile);
                weapon47mmItalianIcon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
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

        } catch (Exception e) {
            System.err.println("Ошибка загрузки изображений предметов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(30, 30, 40));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(30, 30, 40));

        BufferedImage leftPortrait = getPortrait(leftUnit);
        JLabel leftPortraitLabel = new JLabel();
        if (leftPortrait != null) {
            Image scaled = leftPortrait.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            leftPortraitLabel.setIcon(new ImageIcon(scaled));
        } else {
            leftPortraitLabel.setText(getUnitName(leftUnit));
            leftPortraitLabel.setFont(new Font("Arial", Font.BOLD, 20));
        }

        leftNameLabel = new JLabel(getUnitName(leftUnit));
        leftNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        leftNameLabel.setForeground(new Color(255, 215, 0));
        leftNameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        leftPanel.add(leftPortraitLabel, BorderLayout.WEST);
        leftPanel.add(leftNameLabel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(30, 30, 40));

        BufferedImage rightPortrait = getPortrait(rightUnit);
        JLabel rightPortraitLabel = new JLabel();
        if (rightPortrait != null) {
            Image scaled = rightPortrait.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            rightPortraitLabel.setIcon(new ImageIcon(scaled));
        } else {
            rightPortraitLabel.setText(getUnitName(rightUnit));
            rightPortraitLabel.setFont(new Font("Arial", Font.BOLD, 20));
        }

        rightNameLabel = new JLabel(getUnitName(rightUnit));
        rightNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        rightNameLabel.setForeground(new Color(255, 215, 0));
        rightNameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        rightPanel.add(rightPortraitLabel, BorderLayout.WEST);
        rightPanel.add(rightNameLabel, BorderLayout.CENTER);

        panel.add(leftPanel);
        panel.add(rightPanel);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(20, 20, 30));

        JPanel leftInventoryPanel = createInventoryPanel(leftUnit, true);
        JPanel rightInventoryPanel = createInventoryPanel(rightUnit, false);

        mainPanel.add(leftInventoryPanel);
        mainPanel.add(rightInventoryPanel);

        return mainPanel;
    }

    private JPanel createInventoryPanel(Object unit, boolean isLeft) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 20, 30));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 150), 2),
                "📦 ИНВЕНТАРЬ - " + getUnitName(unit),
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12),
                new Color(255, 215, 0)
        ));

        JPanel gridPanel = new JPanel(null); // null layout для ручного позиционирования
        gridPanel.setBackground(new Color(20, 20, 30));
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        gridPanel.setPreferredSize(new Dimension(Inventory.WIDTH * 72, Inventory.HEIGHT * 72));

        if (isLeft) {
            leftGrid = gridPanel;
        } else {
            rightGrid = gridPanel;
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBackground(new Color(20, 20, 30));
        scrollPane.getViewport().setBackground(new Color(20, 20, 30));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(50);
        scrollPane.getVerticalScrollBar().setUnitIncrement(50);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshGrids() {
        refreshGrid(leftGrid, leftUnit, true);
        refreshGrid(rightGrid, rightUnit, false);
    }

    private void refreshGrid(JPanel grid, Object unit, boolean isLeft) {
        if (grid == null) return;

        grid.removeAll();
        Inventory inv = getInventory(unit);
        if (inv == null) return;

        Map<String, JPanel> slotComponents = isLeft ? leftSlotComponents : rightSlotComponents;
        slotComponents.clear();

        int cols = Inventory.WIDTH;
        int rows = Inventory.HEIGHT;
        int cellSize = 70;

        // Сначала создаём все базовые ячейки
        JPanel[][] cellPanels = new JPanel[cols][rows];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                JPanel cell = new JPanel(new BorderLayout());
                cell.setBackground(new Color(40, 40, 50));
                cell.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70), 1));
                cell.setBounds(x * cellSize, y * cellSize, cellSize, cellSize);

                final int finalX = x;
                final int finalY = y;
                final Object finalUnit = unit;
                final boolean finalIsLeft = isLeft;

                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        onSlotClick(finalUnit, finalX, finalY, finalIsLeft);
                    }
                });

                grid.add(cell);
                cellPanels[x][y] = cell;
                slotComponents.put(x + "," + y, cell);
            }
        }

        // Находим все большие предметы (оружие размером 2x2)
        boolean[][] occupied = new boolean[cols][rows];
        java.util.List<Item> bigItems = new java.util.ArrayList<>();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Item item = inv.getItem(x, y);
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
                        // Помечаем занятые клетки
                        for (int dy = 0; dy < item.getHeight(); dy++) {
                            for (int dx = 0; dx < item.getWidth(); dx++) {
                                if (x + dx < cols && y + dy < rows) {
                                    occupied[x + dx][y + dy] = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Отрисовываем большие предметы
        for (Item item : bigItems) {
            int itemX = -1, itemY = -1;
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    if (inv.getItem(x, y) == item) {
                        itemX = x;
                        itemY = y;
                        break;
                    }
                }
                if (itemX != -1) break;
            }

            if (itemX == -1) continue;

            // Удаляем ячейки, которые будут заняты большим предметом
            for (int dy = 0; dy < item.getHeight(); dy++) {
                for (int dx = 0; dx < item.getWidth(); dx++) {
                    int cx = itemX + dx;
                    int cy = itemY + dy;
                    if (cx < cols && cy < rows) {
                        JPanel oldCell = cellPanels[cx][cy];
                        if (oldCell != null) {
                            grid.remove(oldCell);
                            slotComponents.remove(cx + "," + cy);
                        }
                    }
                }
            }

            // Создаём панель для большого предмета
            JPanel bigItemPanel = new JPanel(new BorderLayout());
            int panelWidth = item.getWidth() * cellSize;
            int panelHeight = item.getHeight() * cellSize;
            bigItemPanel.setBounds(itemX * cellSize, itemY * cellSize, panelWidth, panelHeight);
            bigItemPanel.setBackground(new Color(50, 60, 70));
            bigItemPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));

            // Проверяем, выбран ли этот предмет
            boolean isSelected = false;
            if (isLeft && selectedLeftX == itemX && selectedLeftY == itemY) {
                isSelected = true;
            }
            if (!isLeft && selectedRightX == itemX && selectedRightY == itemY) {
                isSelected = true;
            }

            if (isSelected) {
                bigItemPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 0), 4));
            }

            // Загружаем иконку оружия
            ImageIcon weaponIcon = getItemIcon(item);
            if (weaponIcon != null) {
                Image scaledImage = weaponIcon.getImage().getScaledInstance(
                        panelWidth - 20, panelHeight - 40, Image.SCALE_SMOOTH);
                JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                bigItemPanel.add(iconLabel, BorderLayout.CENTER);
            } else {
                JLabel label = new JLabel(item.getType().icon, SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 48));
                bigItemPanel.add(label, BorderLayout.CENTER);
            }

            // Счётчик
            JLabel countLabel = new JLabel("x" + item.getCount(), SwingConstants.CENTER);
            countLabel.setFont(new Font("Arial", Font.BOLD, 12));
            countLabel.setForeground(new Color(255, 215, 0));
            bigItemPanel.add(countLabel, BorderLayout.SOUTH);

            // Добавляем обработчик клика
            final int finalItemX = itemX;
            final int finalItemY = itemY;
            final Object finalUnit = unit;
            final boolean finalIsLeft = isLeft;
            bigItemPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onSlotClick(finalUnit, finalItemX, finalItemY, finalIsLeft);
                }
            });

            bigItemPanel.setToolTipText(getItemTooltip(item));
            grid.add(bigItemPanel);
            slotComponents.put(itemX + "," + itemY, bigItemPanel);
        }

        // Заполняем оставшиеся ячейки (не занятые большими предметами)
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (occupied[x][y]) continue;

                JPanel cell = cellPanels[x][y];
                if (cell == null) continue;

                Item item = inv.getItem(x, y);
                if (item != null && item.getCount() > 0) {
                    cell.removeAll();
                    cell.setLayout(new BorderLayout());

                    // Проверяем, выбран ли этот предмет
                    if (isLeft && selectedLeftX == x && selectedLeftY == y) {
                        cell.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 0), 3));
                    } else if (!isLeft && selectedRightX == x && selectedRightY == y) {
                        cell.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 0), 3));
                    } else {
                        cell.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70), 1));
                    }

                    ImageIcon icon = getItemIcon(item);
                    JLabel iconLabel;
                    if (icon != null) {
                        iconLabel = new JLabel(icon);
                        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    } else {
                        iconLabel = new JLabel(item.getType().icon, SwingConstants.CENTER);
                        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 30));
                    }
                    cell.add(iconLabel, BorderLayout.CENTER);

                    JLabel countLabel = new JLabel("x" + item.getCount(), SwingConstants.CENTER);
                    countLabel.setFont(new Font("Arial", Font.PLAIN, 10));
                    countLabel.setForeground(new Color(255, 215, 0));
                    cell.add(countLabel, BorderLayout.SOUTH);

                    cell.setToolTipText(getItemTooltip(item));
                } else {
                    cell.removeAll();
                    cell.setLayout(new BorderLayout());
                    cell.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70), 1));
                    cell.setToolTipText("Пустой слот");
                }
            }
        }

        grid.revalidate();
        grid.repaint();
    }

    private void onSlotClick(Object unit, int x, int y, boolean isLeft) {
        // Находим корневой слот для большого предмета
        Inventory inv = getInventory(unit);
        if (inv == null) return;

        int realX = x;
        int realY = y;

        Item itemAtCell = inv.getItem(x, y);
        if (itemAtCell != null && itemAtCell.getCount() > 0 && (itemAtCell.getWidth() > 1 || itemAtCell.getHeight() > 1)) {
            // Ищем корневой слот
            for (int checkY = 0; checkY <= y; checkY++) {
                for (int checkX = 0; checkX <= x; checkX++) {
                    Item checkItem = inv.getItem(checkX, checkY);
                    if (checkItem != null && checkItem == itemAtCell) {
                        realX = checkX;
                        realY = checkY;
                        break;
                    }
                }
            }
        }

        if (isLeft) {
            if (selectedLeftX == -1 && selectedRightX == -1) {
                Inventory inventory = getInventory(unit);
                if (inventory != null && inventory.getItem(realX, realY) != null) {
                    selectedLeftX = realX;
                    selectedLeftY = realY;
                    isSelectingFromLeft = true;
                    refreshGrids();
                    infoLabel.setText("Выбран предмет из " + getUnitName(leftUnit) +
                            ". Кликните на слот в инвентаре " + getUnitName(rightUnit));
                }
            } else if (selectedLeftX != -1 && selectedRightX == -1) {
                selectedLeftX = -1;
                selectedLeftY = -1;
                refreshGrids();
                infoLabel.setText("Выделение снято");
            } else if (selectedLeftX == -1 && selectedRightX != -1) {
                moveItem(selectedRightX, selectedRightY, realX, realY, false);
                selectedRightX = -1;
                selectedRightY = -1;
                refreshGrids();
                infoLabel.setText("Предмет перемещён из " + getUnitName(rightUnit) + " в " + getUnitName(leftUnit));
            }
        } else {
            if (selectedLeftX == -1 && selectedRightX == -1) {
                Inventory inventory = getInventory(unit);
                if (inventory != null && inventory.getItem(realX, realY) != null) {
                    selectedRightX = realX;
                    selectedRightY = realY;
                    isSelectingFromLeft = false;
                    refreshGrids();
                    infoLabel.setText("Выбран предмет из " + getUnitName(rightUnit) +
                            ". Кликните на слот в инвентаре " + getUnitName(leftUnit));
                }
            } else if (selectedLeftX != -1 && selectedRightX == -1) {
                moveItem(selectedLeftX, selectedLeftY, realX, realY, true);
                selectedLeftX = -1;
                selectedLeftY = -1;
                refreshGrids();
                infoLabel.setText("Предмет перемещён из " + getUnitName(leftUnit) + " в " + getUnitName(rightUnit));
            } else if (selectedLeftX == -1 && selectedRightX != -1) {
                selectedRightX = -1;
                selectedRightY = -1;
                refreshGrids();
                infoLabel.setText("Выделение снято");
            }
        }
    }

    private void moveItem(int fromX, int fromY, int toX, int toY, boolean fromLeft) {
        Inventory sourceInv;
        Inventory targetInv;

        if (fromLeft) {
            sourceInv = getInventory(leftUnit);
            targetInv = getInventory(rightUnit);
        } else {
            sourceInv = getInventory(rightUnit);
            targetInv = getInventory(leftUnit);
        }

        if (sourceInv == null || targetInv == null) return;

        Item item = sourceInv.getItem(fromX, fromY);
        if (item == null || item.getCount() == 0) return;

        int count = item.getCount();

        Item itemToMove;
        if (item instanceof AmmoItem) {
            AmmoItem ammo = (AmmoItem) item;
            itemToMove = new AmmoItem(ammo.getCaliber(), count, ammo.isImproved());
        } else {
            itemToMove = new Item(item.getType(), count);
        }

        if (targetInv.addItemToInventory(itemToMove)) {
            sourceInv.removeItem(fromX, fromY, count);
            refreshGrids();
            if (gamePanel != null) {
                gamePanel.repaint();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Нет места в инвентаре получателя!",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
        }
    }

    private ImageIcon getItemIcon(Item item) {
        String shellsPath = "src/ObjectsOfInventory/Shells/";
        String weaponPath = "src/ObjectsOfInventory/Weapon/";

        switch (item.getType()) {
            case MEDKIT:
                return medkitIcon;
            case BANDAGE:
                return bandageIcon;
            case REPAIR_KIT:
                return repairKitIcon;
            case ENERGY_DRINK:
                return energyDrinkIcon;
            case GRENADE:
                return grenadeIcon;
            case BREAD:
                return breadIcon;
            case FIRE_EXTINGUISHER:
                return extinguisherIcon;

            case WEAPON:
                return loadWeaponIcon(weaponPath + "2 cm Breda (I).png");
            case WEAPON_8MM:
                return loadWeaponIcon(weaponPath + "7,92 mm Mauser E.W. 141.png");
            case WEAPON_13MM_JAPAN:
                return loadWeaponIcon(weaponPath + "13 mm Autocannon Type Ho.png");
            case WEAPON_13MM_FRENCH:
                return loadWeaponIcon(weaponPath + "13,2 mm Hotchkiss mle. 1930.png");
            case WEAPON_25MM:
                return loadWeaponIcon(weaponPath + "25-mm-Canon-Raccourci-mle.-1934.png");
            case WEAPON_30MM:
                return loadWeaponIcon(weaponPath + "3 cm M.K. 103A.png");
            case WEAPON_37MM_ITALIAN:
                return loadWeaponIcon(weaponPath + "Cannone da 37-40.png");
            case WEAPON_37MM_AMERICAN:
                return loadWeaponIcon(weaponPath + "37 mm Semiautomatic Gun M1924.png");
            case WEAPON_37MM_SWEDEN:
                return loadWeaponIcon(weaponPath + "37 mm kan m-38-49 strv.png");
            case WEAPON_45MM:
                return loadWeaponIcon(weaponPath + "45 мм обр. 1932 г.png");
            case WEAPON_47MM_FRENCH:
                return loadWeaponIcon(weaponPath + "47 mm SA35.png");
            case WEAPON_47MM_ITALIAN:
                return loadWeaponIcon(weaponPath + "Cannone da 47-32.png");
            case WEAPON_76MM:
                return loadWeaponIcon(weaponPath + "76 мм Л-10С.png");
            case WEAPON_105MM:
                return loadWeaponIcon(weaponPath + "10,5 cm StuH 42 L28.png");
            case WEAPON_128MM:
                return loadWeaponIcon(weaponPath + "12,8 cm Kw.K. L50.png");
            case WEAPON_203MM:
                return loadWeaponIcon(weaponPath + "8-inch Howitzer M47.png");

            case AMMO:
                if (item instanceof AmmoItem) {
                    AmmoItem ammoItem = (AmmoItem) item;
                    Caliber caliber = ammoItem.getCaliber();
                    boolean isImproved = ammoItem.isImproved();

                    if (caliber == Caliber.CALIBER_20MM) {
                        if (isImproved && improvedShellIcon != null) {
                            return new ImageIcon(improvedShellIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                        } else if (basicShellIcon != null) {
                            return new ImageIcon(basicShellIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                        }
                    } else if (caliber == Caliber.CALIBER_45MM) {
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
                    } else if (caliber == Caliber.CALIBER_25MM) {
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
                    } else if (caliber == Caliber.CALIBER_47MM) {
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
                    } else if (caliber == Caliber.CALIBER_13MM) {
                        File shell13mmFile = new File(shellsPath + "13mmShell_based.png");
                        if (shell13mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell13mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    } else if (caliber == Caliber.CALIBER_30MM) {
                        File shell30mmFile = new File(shellsPath + "30mmShell_based.png");
                        if (shell30mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell30mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    } else if (caliber == Caliber.CALIBER_37MM) {
                        File shell37mmFile = new File(shellsPath + "37mmShell_based.png");
                        if (shell37mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell37mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    } else if (caliber == Caliber.CALIBER_76MM) {
                        File shell76mmFile = new File(shellsPath + "76mmShell_based.png");
                        if (shell76mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell76mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    } else if (caliber == Caliber.CALIBER_105MM) {
                        File shell105mmFile = new File(shellsPath + "105mmShell_based.png");
                        if (shell105mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell105mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    } else if (caliber == Caliber.CALIBER_128MM) {
                        File shell128mmFile = new File(shellsPath + "128mmShell_based.png");
                        if (shell128mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell128mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    } else if (caliber == Caliber.CALIBER_8MM) {
                        File shell8mmFile = new File(shellsPath + "8mmShell_based.png");
                        if (shell8mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell8mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    } else if (caliber == Caliber.CALIBER_203MM) {
                        File shell203mmFile = new File(shellsPath + "203mmShell_HESH.png");
                        if (shell203mmFile.exists()) {
                            try {
                                Image img = ImageIO.read(shell203mmFile);
                                return new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                            } catch (Exception e) {}
                        }
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

    private ImageIcon loadWeaponIcon(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                Image img = ImageIO.read(file);
                if (img != null) {
                    return new ImageIcon(img);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки иконки оружия: " + path + " - " + e.getMessage());
        }
        return null;
    }

    private String getItemTooltip(Item item) {
        if (item instanceof MedicalItem) {
            MedicalItem medical = (MedicalItem) item;
            return medical.getTooltipText();
        }

        if (item instanceof AmmoItem) {
            AmmoItem ammo = (AmmoItem) item;
            return "<html>" + ammo.getCaliber().name + " снаряды<br>" +
                    "Количество: " + item.getCount() + "<br>" +
                    (ammo.isImproved() ? "✨ Улучшенные" : "📦 Базовые") + "</html>";
        }

        String displayName = getWeaponDisplayName(item.getType());
        if (displayName != null) {
            return "<html>🔫 " + displayName + "<br>" +
                    "Количество: " + item.getCount() + "<br>" +
                    "Занимает 2x2 клетки</html>";
        }

        return "<html>" + item.getType().name + "<br>" +
                "Количество: " + item.getCount() + "<br>" +
                item.getType().description + "</html>";
    }

    private String getWeaponDisplayName(Item.ItemType type) {
        switch (type) {
            case WEAPON: return "2 cm Breda (I)";
            case WEAPON_8MM: return "7,92 mm Mauser E.W. 141";
            case WEAPON_13MM_JAPAN: return "13 mm Autocannon Type Ho";
            case WEAPON_13MM_FRENCH: return "13,2 mm Hotchkiss mle. 1930";
            case WEAPON_25MM: return "25mm Canon Raccourci mle. 1934";
            case WEAPON_30MM: return "3 cm M.K. 103A";
            case WEAPON_37MM_ITALIAN: return "Cannone da 37-40";
            case WEAPON_37MM_AMERICAN: return "37 mm Semiautomatic Gun M1924";
            case WEAPON_37MM_SWEDEN: return "37 mm kan m-38-49 strv";
            case WEAPON_45MM: return "45 мм обр. 1932 г.";
            case WEAPON_47MM_FRENCH: return "47 mm SA35";
            case WEAPON_47MM_ITALIAN: return "Cannone da 47-32";
            case WEAPON_76MM: return "76 мм Л-10С";
            case WEAPON_105MM: return "10,5 cm StuH 42 L28";
            case WEAPON_128MM: return "12,8 cm Kw.K. L50";
            case WEAPON_203MM: return "8-inch Howitzer M47";
            default: return null;
        }
    }

    private Inventory getInventory(Object unit) {
        if (unit instanceof PlayerTank) {
            return ((PlayerTank) unit).getInventory();
        } else if (unit instanceof FriendlyUnit) {
            return ((FriendlyUnit) unit).getInventory();
        }
        return null;
    }

    private String getUnitName(Object unit) {
        if (unit instanceof PlayerTank) {
            return "Leichttraktor";
        } else if (unit instanceof FriendlyUnit) {
            return ((FriendlyUnit) unit).name;
        }
        return "Unknown";
    }

    private BufferedImage getPortrait(Object unit) {
        if (unit instanceof PlayerTank) {
            return heroPortrait;
        } else if (unit instanceof FriendlyUnit) {
            FriendlyUnit friendly = (FriendlyUnit) unit;
            if ("M53".equals(friendly.type)) return m53Portrait;
            if ("MS-1".equals(friendly.type)) return ms1Portrait;
            if ("VK10001P".equals(friendly.type)) return vk10001pPortrait;
            if ("AMX40".equals(friendly.type)) return amx40Portrait;
            if ("T1".equals(friendly.type)) return t1Portrait;
        }
        return null;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 40));

        infoLabel = new JLabel("Кликните на предмет в любом инвентаре, затем на слот в другом");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(Color.CYAN);

        JButton closeButton = new JButton("✖ ЗАКРЫТЬ");
        closeButton.addActionListener(e -> dispose());

        panel.add(infoLabel);
        panel.add(closeButton);

        return panel;
    }
}