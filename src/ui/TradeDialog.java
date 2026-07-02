package ui;

import entities.PlayerTank;
import entities.FriendlyUnit;
import entities.Trader;
import inventory.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class TradeDialog extends JDialog {
    private Object activeUnit;  // Может быть PlayerTank или FriendlyUnit
    private Trader trader;
    private PlayerTank player;  // Для доступа к общему серебру команды
    private BufferedImage portrait;
    private JPanel offersPanel;
    private JPanel sellPanel;
    private JLabel silverLabel;
    private JTabbedPane tabbedPane;

    public TradeDialog(JFrame parent, Object activeUnit, Trader trader,
                       BufferedImage portrait, PlayerTank player) {
        super(parent, "💰 Торговля - " + trader.name, true);
        this.activeUnit = activeUnit;
        this.trader = trader;
        this.portrait = portrait;
        this.player = player;

        setLayout(new BorderLayout(10, 10));
        setSize(600, 550);
        setLocationRelativeTo(parent);

        add(createTopPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("🛒 ПОКУПКА", createBuyPanel());
        tabbedPane.addTab("💰 ПРОДАЖА", createSellPanel());
        add(tabbedPane, BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);

        trader.isTalkedTo = true;
    }

    private String getActiveUnitName() {
        if (activeUnit instanceof PlayerTank) {
            return "Leichttraktor";
        } else if (activeUnit instanceof FriendlyUnit) {
            return ((FriendlyUnit) activeUnit).name;
        }
        return "Неизвестно";
    }

    private Inventory getActiveInventory() {
        if (activeUnit instanceof PlayerTank) {
            return ((PlayerTank) activeUnit).getInventory();
        } else if (activeUnit instanceof FriendlyUnit) {
            return ((FriendlyUnit) activeUnit).getInventory();
        }
        return null;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(30, 30, 40));

        JPanel portraitPanel = new JPanel(new BorderLayout());
        portraitPanel.setBackground(new Color(30, 30, 40));

        JLabel portraitLabel = new JLabel();
        if (portrait != null) {
            Image scaled = portrait.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            portraitLabel.setIcon(new ImageIcon(scaled));
        } else {
            portraitLabel.setText("💰");
            portraitLabel.setFont(new Font("Arial", Font.BOLD, 48));
        }
        portraitPanel.add(portraitLabel, BorderLayout.CENTER);
        panel.add(portraitPanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(30, 30, 40));

        JLabel titleLabel = new JLabel(getActiveUnitName() + " разговаривает с " + trader.name);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(255, 215, 0));

        silverLabel = new JLabel("💰 Серебро команды: " + player.getSilver());
        silverLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        silverLabel.setForeground(Color.CYAN);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(silverLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createBuyPanel() {
        offersPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        offersPanel.setBackground(new Color(20, 20, 30));
        offersPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (Trader.TradeOffer offer : trader.getOffers()) {
            // ===== ИСПРАВЛЕНИЕ: показываем только товары с quantity > 0 =====
            if (offer.quantity > 0) {
                offersPanel.add(createBuyCard(offer));
            }
        }

        // Если нет товаров - показываем сообщение
        if (offersPanel.getComponentCount() == 0) {
            JLabel emptyLabel = new JLabel("Товары временно отсутствуют", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            offersPanel.add(emptyLabel);
        }

        JScrollPane scrollPane = new JScrollPane(offersPanel);
        scrollPane.setBackground(new Color(20, 20, 30));
        scrollPane.getViewport().setBackground(new Color(20, 20, 30));

        return scrollPane;
    }

    private JScrollPane createSellPanel() {
        sellPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        sellPanel.setBackground(new Color(20, 20, 30));
        sellPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        refreshSellPanel();

        JScrollPane scrollPane = new JScrollPane(sellPanel);
        scrollPane.setBackground(new Color(20, 20, 30));
        scrollPane.getViewport().setBackground(new Color(20, 20, 30));

        return scrollPane;
    }

    private void refreshSellPanel() {
        sellPanel.removeAll();

        java.util.Map<Item.ItemType, Integer> sellPrices = new java.util.HashMap<>();
        sellPrices.put(Item.ItemType.MEDKIT, 25);
        sellPrices.put(Item.ItemType.BANDAGE, 15);
        sellPrices.put(Item.ItemType.REPAIR_KIT, 50);
        sellPrices.put(Item.ItemType.ENERGY_DRINK, 15);
        sellPrices.put(Item.ItemType.GRENADE, 40);
        sellPrices.put(Item.ItemType.WEAPON, 200);
        sellPrices.put(Item.ItemType.WEAPON_8MM, 300);
        sellPrices.put(Item.ItemType.WEAPON_13MM_JAPAN, 60);
        sellPrices.put(Item.ItemType.WEAPON_13MM_FRENCH, 55);
        sellPrices.put(Item.ItemType.WEAPON_25MM, 250);
        sellPrices.put(Item.ItemType.WEAPON_30MM, 600);
        sellPrices.put(Item.ItemType.WEAPON_37MM_ITALIAN, 75);
        sellPrices.put(Item.ItemType.WEAPON_37MM_AMERICAN, 75);
        sellPrices.put(Item.ItemType.WEAPON_37MM_SWEDEN, 230);
        sellPrices.put(Item.ItemType.WEAPON_47MM_FRENCH, 150);
        sellPrices.put(Item.ItemType.WEAPON_47MM_ITALIAN, 150);
        sellPrices.put(Item.ItemType.WEAPON_76MM, 400);
        sellPrices.put(Item.ItemType.WEAPON_105MM, 770);
        sellPrices.put(Item.ItemType.WEAPON_128MM, 800);
        sellPrices.put(Item.ItemType.WEAPON_203MM, 1500);

        Inventory inv = getActiveInventory();
        if (inv == null) return;

        java.util.Map<Item.ItemType, Integer> itemsInInventory = new java.util.HashMap<>();
        for (int y = 0; y < Inventory.HEIGHT; y++) {
            for (int x = 0; x < Inventory.WIDTH; x++) {
                Item item = inv.getItem(x, y);
                if (item != null && item.getCount() > 0) {
                    Item.ItemType type = item.getType();
                    if (type == Item.ItemType.AMMO) continue;

                    // Проверяем, не экипировано ли оружие
                    if (activeUnit instanceof PlayerTank) {
                        PlayerTank p = (PlayerTank) activeUnit;
                        if (p.getEquippedWeaponData() != null) {
                            if ((type == Item.ItemType.WEAPON && "breda".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_8MM && "8mm".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_30MM && "30mm".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_37MM_ITALIAN && "37mm_italian".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_37MM_AMERICAN && "37mm_american".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_37MM_SWEDEN && "37mm_sweden".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_13MM_JAPAN && "13mm_japan".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_13MM_FRENCH && "13mm_french".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_25MM && "25mm".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_45MM && "45mm".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_47MM_FRENCH && "47mm_french".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_47MM_ITALIAN && "47mm_italian".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_76MM && "76mm".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_105MM && "105mm".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_128MM && "128mm".equals(p.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_203MM && "203mm".equals(p.getEquippedWeaponId()))) {
                                continue;
                            }
                        }
                    } else if (activeUnit instanceof FriendlyUnit) {
                        FriendlyUnit f = (FriendlyUnit) activeUnit;
                        if (f.getEquippedWeaponData() != null) {
                            if ((type == Item.ItemType.WEAPON && "breda".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_8MM && "8mm".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_13MM_JAPAN && "13mm_japan".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_13MM_FRENCH && "13mm_french".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_25MM && "25mm".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_45MM && "45mm".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_47MM_FRENCH && "47mm_french".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_47MM_ITALIAN && "47mm_italian".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_30MM && "30mm".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_37MM_ITALIAN && "37mm_italian".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_37MM_AMERICAN && "37mm_american".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_37MM_SWEDEN && "37mm_sweden".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_76MM && "76mm".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_128MM && "128mm".equals(f.getEquippedWeaponId())) ||
                                    (type == Item.ItemType.WEAPON_203MM && "203mm".equals(f.getEquippedWeaponId()))) {
                                continue;
                            }
                        }
                    }
                    itemsInInventory.put(type, itemsInInventory.getOrDefault(type, 0) + item.getCount());
                }
            }
        }

        // Снаряды - только полными стеками
        boolean hasAnyAmmoToSell = false;
        for (Caliber caliber : Caliber.values()) {
            int ammoCount = inv.getAmmoCount(caliber);
            int maxStackSize = caliber.getMaxStackSize();

            if (ammoCount >= maxStackSize) {
                int fullStacks = ammoCount / maxStackSize;
                int pricePerStack = getSellPriceForAmmo(caliber);

                if (fullStacks > 0 && pricePerStack > 0) {
                    sellPanel.add(createSellAmmoStackCard(caliber, fullStacks, maxStackSize, pricePerStack));
                    hasAnyAmmoToSell = true;
                }
            }
        }

        for (Map.Entry<Item.ItemType, Integer> entry : itemsInInventory.entrySet()) {
            int price = sellPrices.getOrDefault(entry.getKey(), 10);
            sellPanel.add(createSellCard(entry.getKey(), entry.getValue(), price));
        }

        if (itemsInInventory.isEmpty() && !hasAnyAmmoToSell) {
            JLabel emptyLabel = new JLabel("Нет предметов для продажи", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            sellPanel.add(emptyLabel);
        }

        sellPanel.revalidate();
        sellPanel.repaint();
    }

    private JPanel createSellAmmoStackCard(Caliber caliber, int fullStacks, int stackSize, int pricePerStack) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel("🔫 " + caliber.name + " снаряды (стак " + stackSize + " шт.)");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);

        JLabel priceLabel = new JLabel("💰 Цена за стак: " + pricePerStack + " серебра");
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        priceLabel.setForeground(new Color(255, 215, 0));

        JLabel availableLabel = new JLabel("📦 Доступно стаков: " + fullStacks);
        availableLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        availableLabel.setForeground(Color.LIGHT_GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(priceLabel);
        infoPanel.add(availableLabel);

        JPanel sellControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        sellControlPanel.setOpaque(false);

        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, fullStacks, 1));
        amountSpinner.setPreferredSize(new Dimension(60, 25));

        JButton sellButton = new JButton("ПРОДАТЬ СТАК");
        sellButton.setFont(new Font("Arial", Font.BOLD, 10));
        sellButton.setBackground(new Color(200, 100, 0));
        sellButton.setForeground(Color.WHITE);
        sellButton.setFocusPainted(false);

        sellButton.addActionListener(e -> {
            int stacksToSell = (Integer) amountSpinner.getValue();
            int totalAmmo = stacksToSell * stackSize;
            int totalPrice = pricePerStack * stacksToSell;

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Продать " + stacksToSell + " стак(ов) (" + totalAmmo + " шт.) " + caliber.name + " снарядов за " + totalPrice + " серебра?\n\n" +
                            "⚠️ Продаются только полные стаки!",
                    "Подтверждение продажи",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (trader.sellFullAmmoStack(activeUnit, caliber, stacksToSell, stackSize, pricePerStack, player)) {
                    silverLabel.setText("💰 Серебро команды: " + player.getSilver());
                    refreshSellPanel();
                    JOptionPane.showMessageDialog(this,
                            "✅ Продажа совершена!\n" +
                                    "Вы продали " + stacksToSell + " стак(ов) (" + totalAmmo + " шт.) " + caliber.name + " снарядов\n" +
                                    "Получено: " + totalPrice + " серебра.",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка при продаже! Возможно, недостаточно полных стаков.",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        sellControlPanel.add(amountSpinner);
        sellControlPanel.add(sellButton);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(sellControlPanel, BorderLayout.EAST);

        return card;
    }

    private int getSellPriceForAmmo(Caliber caliber) {
        switch (caliber) {
            case CALIBER_20MM: return 40;
            case CALIBER_25MM: return 45;
            case CALIBER_30MM: return 50;
            case CALIBER_37MM: return 35;
            case CALIBER_8MM: return 15;
            case CALIBER_13MM: return 16;
            case CALIBER_45MM: return 30;
            case CALIBER_47MM: return 30;
            case CALIBER_76MM: return 60;
            case CALIBER_105MM: return 150;
            case CALIBER_128MM: return 250;
            case CALIBER_203MM: return 750;
            default: return 0;
        }
    }

    private JPanel createSellCard(Item.ItemType type, int available, int price) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String displayName = getDisplayName(type);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(type.icon + " " + displayName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);

        JLabel priceLabel = new JLabel("💰 Цена: " + price + " серебра за шт.");
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        priceLabel.setForeground(new Color(255, 215, 0));

        JLabel availableLabel = new JLabel("📦 В инвентаре: " + available + " шт.");
        availableLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        availableLabel.setForeground(Color.LIGHT_GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(priceLabel);
        infoPanel.add(availableLabel);

        JPanel sellControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        sellControlPanel.setOpaque(false);

        JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, available, 1));
        amountSpinner.setPreferredSize(new Dimension(60, 25));

        JButton sellButton = new JButton("ПРОДАТЬ");
        sellButton.setFont(new Font("Arial", Font.BOLD, 10));
        sellButton.setBackground(new Color(200, 100, 0));
        sellButton.setForeground(Color.WHITE);
        sellButton.setFocusPainted(false);

        sellButton.addActionListener(e -> {
            int amount = (Integer) amountSpinner.getValue();
            int totalPrice = price * amount;

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Продать " + amount + "x " + displayName + " за " + totalPrice + " серебра?",
                    "Подтверждение продажи",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (trader.sellItem(activeUnit, type, amount, price, player)) {
                    silverLabel.setText("💰 Серебро команды: " + player.getSilver());
                    refreshSellPanel();
                    JOptionPane.showMessageDialog(this,
                            "✅ Продажа совершена!\n" +
                                    "Вы получили " + totalPrice + " серебра.",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка при продаже!",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        sellControlPanel.add(amountSpinner);
        sellControlPanel.add(sellButton);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(sellControlPanel, BorderLayout.EAST);

        return card;
    }

    private String getDisplayName(Item.ItemType type) {
        if (type == Item.ItemType.WEAPON) return "2 cm Breda (I)";
        else if (type == Item.ItemType.WEAPON_45MM) return "45 мм обр. 1932 г.";
        else if (type == Item.ItemType.WEAPON_25MM) return "25mm Canon Raccourci mle. 1934";
        else if (type == Item.ItemType.WEAPON_47MM_FRENCH) return "47 mm SA35";
        else if (type == Item.ItemType.WEAPON_47MM_ITALIAN) return "Cannone da 47-32";
        else if (type == Item.ItemType.WEAPON_8MM) return "7,92 mm Mauser E.W. 141";
        else if (type == Item.ItemType.WEAPON_13MM_JAPAN) return "13 mm Autocannon Type Ho";
        else if (type == Item.ItemType.WEAPON_13MM_FRENCH) return "13,2 mm Hotchkiss mle. 1930";
        else if (type == Item.ItemType.WEAPON_30MM) return "3 cm M.K. 103A";
        else if (type == Item.ItemType.WEAPON_37MM_ITALIAN) return "Cannone da 37-40";
        else if (type == Item.ItemType.WEAPON_37MM_AMERICAN) return "37 mm Semiautomatic Gun M1924";
        else if (type == Item.ItemType.WEAPON_37MM_SWEDEN) return "37 mm kan m-38-49 strv";
        else if (type == Item.ItemType.WEAPON_76MM) return "76 мм Л-10С";
        else if (type == Item.ItemType.WEAPON_128MM) return "12,8 cm Kw.K. L50";
        else if (type == Item.ItemType.WEAPON_203MM) return "8-inch Howitzer M47";
        else return type.name;
    }

    private JPanel createBuyCard(Trader.TradeOffer offer) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        String displayName = offer.getDisplayName();
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);

        if (offer.itemType == Item.ItemType.AMMO && offer.isImproved) {
            nameLabel.setForeground(new Color(255, 215, 0));
        }

        String priceText;
        if (offer.itemType == Item.ItemType.AMMO) {
            priceText = "💰 Цена за стак (" + offer.stackSize + " шт.): " + offer.pricePerStack + " серебра";
        } else {
            priceText = "💰 Цена: " + offer.pricePerStack + " серебра";
        }
        JLabel priceLabel = new JLabel(priceText);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        priceLabel.setForeground(new Color(255, 215, 0));

        JLabel quantityLabel = new JLabel("📦 В наличии: " + offer.quantity + " стаков");
        quantityLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        quantityLabel.setForeground(Color.LIGHT_GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(priceLabel);
        infoPanel.add(quantityLabel);

        JPanel buyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buyPanel.setOpaque(false);

        // ===== ИСПРАВЛЕНИЕ: проверяем, что quantity > 0, иначе спиннер не создаём =====
        JSpinner amountSpinner;
        JButton buyButton;

        if (offer.quantity <= 0) {
            // Товара нет в наличии
            amountSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
            amountSpinner.setEnabled(false);
            buyButton = new JButton("НЕТ В НАЛИЧИИ");
            buyButton.setEnabled(false);
            buyButton.setBackground(new Color(100, 100, 100));
        } else {
            amountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, offer.quantity, 1));
            amountSpinner.setPreferredSize(new Dimension(60, 25));

            buyButton = new JButton("КУПИТЬ СТАК");
            buyButton.setFont(new Font("Arial", Font.BOLD, 10));
            buyButton.setBackground(new Color(0, 100, 200));
            buyButton.setForeground(Color.WHITE);
            buyButton.setFocusPainted(false);

            buyButton.addActionListener(e -> {
                int stacks = (Integer) amountSpinner.getValue();
                if (stacks > offer.quantity) {
                    JOptionPane.showMessageDialog(this, "Недостаточно товара в наличии!", "Ошибка", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int totalCost = offer.getTotalPrice(stacks);
                if (player.getSilver() < totalCost) {
                    JOptionPane.showMessageDialog(this,
                            "Недостаточно серебра в команде! Нужно: " + totalCost + ", есть: " + player.getSilver(),
                            "Ошибка", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String confirmText;
                if (offer.itemType == Item.ItemType.AMMO) {
                    int totalAmmo = offer.getTotalAmmoCount(stacks);
                    confirmText = "Купить " + stacks + " стак(ов) (" + totalAmmo + " шт.) " + offer.getDisplayName() +
                            " за " + totalCost + " серебра?\n\nПредметы получит: " + getActiveUnitName();
                } else {
                    confirmText = "Купить " + stacks + "x " + offer.getDisplayName() + " за " + totalCost + " серебра?\n\nПредметы получит: " + getActiveUnitName();
                }

                int confirm = JOptionPane.showConfirmDialog(this, confirmText, "Подтверждение покупки", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    if (trader.buyItem(offer, stacks, player, activeUnit)) {
                        silverLabel.setText("💰 Серебро команды: " + player.getSilver());
                        quantityLabel.setText("📦 В наличии: " + offer.quantity + " стаков");

                        // ===== ИСПРАВЛЕНИЕ: обновляем спиннер =====
                        if (offer.quantity > 0) {
                            amountSpinner.setModel(new SpinnerNumberModel(1, 1, offer.quantity, 1));
                        } else {
                            // Если товар закончился, отключаем кнопку и спиннер
                            amountSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 1));
                            amountSpinner.setEnabled(false);
                            buyButton.setEnabled(false);
                            buyButton.setText("РАСПРОДАНО");
                            buyButton.setBackground(new Color(100, 100, 100));
                        }

                        // Если товар закончился, удаляем карточку
                        if (offer.quantity == 0) {
                            offersPanel.remove(card);
                            offersPanel.revalidate();
                            offersPanel.repaint();
                        }

                        JOptionPane.showMessageDialog(this,
                                "✅ Покупка совершена!\n" +
                                        getActiveUnitName() + " получил " + stacks + " стак(ов) " + offer.getDisplayName(),
                                "Успех", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Ошибка при покупке!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }

        buyPanel.add(amountSpinner);
        buyPanel.add(buyButton);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buyPanel, BorderLayout.EAST);

        return card;
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