package ui;

import entities.*;
import inventory.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class LootDialog extends JDialog {
    private Object activeUnit;
    private LootDrop drop;
    private JPanel itemsPanel;
    private JLabel infoLabel;
    private BufferedImage heroPortrait;
    private BufferedImage m53Portrait;
    private BufferedImage ms1Portrait;
    private BufferedImage vk10001pPortrait;
    private BufferedImage amx40Portrait;
    private BufferedImage t1Portrait;

    public LootDialog(JFrame parent, Object activeUnit, LootDrop drop,
                      BufferedImage heroPortrait, BufferedImage m53Portrait, BufferedImage ms1Portrait, BufferedImage vk10001pPortrait,
                      BufferedImage amx40Portrait, BufferedImage t1Portrait) {
        super(parent, "📦 Подобрать дроп", true);
        this.activeUnit = activeUnit;
        this.drop = drop;
        this.heroPortrait = heroPortrait;
        this.m53Portrait = m53Portrait;
        this.ms1Portrait = ms1Portrait;
        this.vk10001pPortrait = vk10001pPortrait;
        this.amx40Portrait = amx40Portrait;
        this.t1Portrait = t1Portrait;

        setLayout(new BorderLayout(10, 10));
        setSize(500, 400);
        setLocationRelativeTo(parent);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createItemsPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
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
            portraitLabel.setText("📦");
            portraitLabel.setFont(new Font("Arial", Font.BOLD, 36));
            portraitLabel.setForeground(Color.WHITE);
        }
        portraitPanel.add(portraitLabel, BorderLayout.CENTER);
        panel.add(portraitPanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(30, 30, 40));

        JLabel titleLabel = new JLabel(unitName + " нашёл дроп");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(255, 215, 0));

        JLabel locationLabel = new JLabel("📍 Клетка: [" + drop.gridX + ", " + drop.gridY + "]");
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
        itemsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        itemsPanel.setBackground(new Color(20, 20, 30));
        itemsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        refreshItemsPanel();

        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBackground(new Color(20, 20, 30));
        scrollPane.getViewport().setBackground(new Color(20, 20, 30));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(20, 20, 30));
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    private void refreshItemsPanel() {
        itemsPanel.removeAll();

        Map<Item.ItemType, Integer> items = drop.items;
        Map<Caliber, Integer> ammo = drop.ammo;

        boolean hasItems = !items.isEmpty() || !ammo.isEmpty();

        if (!hasItems) {
            JLabel emptyLabel = new JLabel("Дроп пуст", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            itemsPanel.add(emptyLabel);
        } else {
            // Показываем обычные предметы
            for (Map.Entry<Item.ItemType, Integer> entry : items.entrySet()) {
                Item.ItemType type = entry.getKey();
                int count = entry.getValue();
                JPanel itemCard = createItemCard(type, count);
                itemsPanel.add(itemCard);
            }

            // Показываем снаряды
            for (Map.Entry<Caliber, Integer> entry : ammo.entrySet()) {
                Caliber caliber = entry.getKey();
                int count = entry.getValue();
                JPanel ammoCard = createAmmoCard(caliber, count);
                itemsPanel.add(ammoCard);
            }
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createItemCard(Item.ItemType type, int count) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        card.setPreferredSize(new Dimension(200, 60));

        String displayName = type.name;
        if (type == Item.ItemType.WEAPON) displayName = "2 cm Breda (I)";
        else if (type == Item.ItemType.WEAPON_45MM) displayName = "45 мм обр. 1932 г.";
        else if (type == Item.ItemType.WEAPON_25MM) displayName = "25mm Canon Raccourci mle. 1934";
        else if (type == Item.ItemType.WEAPON_47MM_FRENCH) displayName = "47 mm SA35";
        else if (type == Item.ItemType.WEAPON_47MM_ITALIAN) displayName = "Cannone da 47-32";
        else if (type == Item.ItemType.WEAPON_8MM) displayName = "7,92 mm Mauser E.W. 141";
        else if (type == Item.ItemType.WEAPON_13MM_JAPAN) displayName = "13 mm Autocannon Type Ho";
        else if (type == Item.ItemType.WEAPON_13MM_FRENCH) displayName = "13,2 mm Hotchkiss mle. 1930C";
        else if (type == Item.ItemType.WEAPON_30MM) displayName = "3 cm M.K. 103A";
        else if (type == Item.ItemType.WEAPON_37MM_ITALIAN) displayName = "Cannone da 37-40";
        else if (type == Item.ItemType.WEAPON_37MM_AMERICAN) displayName = "37 mm Semiautomatic Gun M1924";
        else if (type == Item.ItemType.WEAPON_37MM_SWEDEN) displayName = "37 mm kan m-38-49 strv";
        else if (type == Item.ItemType.WEAPON_76MM) displayName = "76 мм Л-10С";
        else if (type == Item.ItemType.WEAPON_105MM) displayName = "10,5 cm StuH 42 L28";
        else if (type == Item.ItemType.WEAPON_128MM) displayName = "12,8 cm Kw.K. L50";
        else if (type == Item.ItemType.WEAPON_203MM) displayName = "8-inch Howitzer M47";

        JLabel iconLabel = new JLabel(type.icon + " " + displayName);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 14));
        iconLabel.setForeground(Color.WHITE);

        JLabel countLabel = new JLabel("x" + count);
        countLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        countLabel.setForeground(new Color(255, 215, 0));

        JButton takeButton = new JButton("ВЗЯТЬ");
        takeButton.setFont(new Font("Arial", Font.BOLD, 10));
        takeButton.setBackground(new Color(0, 100, 200));
        takeButton.setForeground(Color.WHITE);
        takeButton.setFocusPainted(false);
        takeButton.addActionListener(e -> takeItem(type, 1));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(iconLabel, BorderLayout.NORTH);
        textPanel.add(countLabel, BorderLayout.SOUTH);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(takeButton, BorderLayout.EAST);

        return card;
    }

    private JPanel createAmmoCard(Caliber caliber, int count) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        card.setPreferredSize(new Dimension(200, 60));

        String displayName = caliber.name + " снаряды";
        String icon = "🔫";

        JLabel iconLabel = new JLabel(icon + " " + displayName);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 14));
        iconLabel.setForeground(Color.WHITE);

        JLabel countLabel = new JLabel("x" + count);
        countLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        countLabel.setForeground(new Color(255, 215, 0));

        JButton takeButton = new JButton("ВЗЯТЬ");
        takeButton.setFont(new Font("Arial", Font.BOLD, 10));
        takeButton.setBackground(new Color(0, 100, 200));
        takeButton.setForeground(Color.WHITE);
        takeButton.setFocusPainted(false);
        takeButton.addActionListener(e -> takeAmmo(caliber, count));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(iconLabel, BorderLayout.NORTH);
        textPanel.add(countLabel, BorderLayout.SOUTH);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(takeButton, BorderLayout.EAST);

        return card;
    }

    private void takeItem(Item.ItemType type, int amount) {
        int taken = drop.takeItem(type, amount);

        if (taken > 0) {
            // Создаём временный предмет для расчёта веса
            Item tempItem = new Item(type, taken);
            double itemWeight = tempItem.getTotalWeight();
            int requiredPoints = (int)Math.ceil(itemWeight);

            // Получаем текущие очки хода
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
                drop.addItem(type, taken);  // возвращаем предмет обратно в дроп
                return;
            }

            // Проверка очков хода
            if (currentMovePoints < requiredPoints) {
                JOptionPane.showMessageDialog(this,
                        "❌ НЕДОСТАТОЧНО ОЧКОВ ХОДА!\n\n" +
                                "Для подбора предмета нужно: " + requiredPoints + " очков хода\n" +
                                "У вас осталось: " + currentMovePoints + " очков хода",
                        "Невозможно взять", JOptionPane.WARNING_MESSAGE);
                drop.addItem(type, taken);  // возвращаем предмет обратно в дроп
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
                added = ((PlayerTank) activeUnit).getInventory().addItemToInventory(tempItem);
            } else if (activeUnit instanceof FriendlyUnit) {
                added = ((FriendlyUnit) activeUnit).getInventory().addItemToInventory(tempItem);
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
                drop.addItem(type, taken);
                return;
            }

            String unitName = (activeUnit instanceof PlayerTank) ? "Leichttraktor" : ((FriendlyUnit) activeUnit).name;
            JOptionPane.showMessageDialog(this,
                    "✅ " + unitName + " взял " + taken + " " + type.name() + "!\n\n" +
                            "⚖️ Вес предмета: " + itemWeight + " кг\n" +
                            "⭐ Потрачено очков хода: " + requiredPoints + "\n" +
                            "Осталось очков хода: " + (currentMovePoints - requiredPoints),
                    "Предмет взят", JOptionPane.INFORMATION_MESSAGE);

            refreshItemsPanel();

            if (drop.isEmpty()) {
                dispose();
            }
        }
    }

    private void takeAmmo(Caliber caliber, int amount) {
        int taken = drop.takeAmmo(caliber, amount);
        if (taken > 0) {
            // Создаём временный предмет для расчёта веса
            AmmoItem tempAmmo = new AmmoItem(caliber, taken);
            double itemWeight = tempAmmo.getTotalWeight();
            int requiredPoints = (int)Math.ceil(itemWeight);

            // Получаем текущие очки хода
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
                drop.addAmmo(caliber, taken);
                return;
            }

            // Проверка очков хода
            if (currentMovePoints < requiredPoints) {
                JOptionPane.showMessageDialog(this,
                        "❌ НЕДОСТАТОЧНО ОЧКОВ ХОДА!\n\n" +
                                "Для подбора снарядов нужно: " + requiredPoints + " очков хода\n" +
                                "У вас осталось: " + currentMovePoints + " очков хода",
                        "Невозможно взять", JOptionPane.WARNING_MESSAGE);
                drop.addAmmo(caliber, taken);
                return;
            }

            // Тратим очки хода
            if (activeUnit instanceof PlayerTank) {
                ((PlayerTank) activeUnit).movePoints -= requiredPoints;
            } else if (activeUnit instanceof FriendlyUnit) {
                ((FriendlyUnit) activeUnit).movePoints -= requiredPoints;
            }

            // Добавляем снаряды в инвентарь (разбиваем на стаки)
            int maxStackSize = caliber.getMaxStackSize();
            int remaining = taken;

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

            if (drop.isEmpty()) {
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