package ui;

import save.GameSave;
import save.SaveInfo;
import save.SaveSlotInfo;
import world.GameWorld;
import entities.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class GameMenu extends JDialog {
    private GamePanel gamePanel;
    private GameWorld world;
    private PlayerTank player;
    private JFrame parentFrame;

    private JButton continueButton;
    private JButton saveButton;
    private JButton loadButton;
    private JButton questsButton;
    private JButton guideButton;      // ← НОВАЯ КНОПКА
    private JButton exitButton;

    public GameMenu(JFrame parent, GamePanel panel, GameWorld world, PlayerTank player) {
        super(parent, "Меню", true);
        this.parentFrame = parent;
        this.gamePanel = panel;
        this.world = world;
        this.player = player;

        setSize(550, 700);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setupUI();
        setupHotkeys();
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(20, 20, 30));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("LEICHTTRAKTOR");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        continueButton = createMenuButton("▶ ПРОДОЛЖИТЬ", new Color(0, 150, 0));
        saveButton = createMenuButton("💾 СОХРАНИТЬ ИГРУ", new Color(0, 100, 200));
        loadButton = createMenuButton("📂 ЗАГРУЗИТЬ ИГРУ", new Color(0, 100, 200));
        questsButton = createMenuButton("📜 СПИСОК КВЕСТОВ", new Color(200, 100, 0));
        guideButton = createMenuButton("📖 РУКОВОДСТВО", new Color(100, 100, 150));  // ← НОВАЯ КНОПКА
        exitButton = createMenuButton("✖ ВЫЙТИ ИЗ ИГРЫ", new Color(150, 0, 0));

        mainPanel.add(continueButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(saveButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(loadButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(questsButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(guideButton);   // ← НОВАЯ КНОПКА
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(exitButton);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel);

        add(mainPanel);
        setupButtonActions();
    }

    private JButton createMenuButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 50));
        button.setMinimumSize(new Dimension(300, 50));
        button.setPreferredSize(new Dimension(300, 50));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(brighten(bgColor));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private Color brighten(Color color) {
        return new Color(
                Math.min(255, color.getRed() + 50),
                Math.min(255, color.getGreen() + 50),
                Math.min(255, color.getBlue() + 50)
        );
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 40));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel statsLabel = new JLabel("СТАТИСТИКА");
        statsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statsLabel.setForeground(new Color(255, 215, 0));
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(statsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel healthLabel = new JLabel("Здоровье: " + player.health + "/" + player.maxHealth);
        healthLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        healthLabel.setForeground(Color.WHITE);
        healthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(healthLabel);

        JLabel killsLabel = new JLabel("Уничтожено врагов: " + world.getTotalEnemiesKilled());
        killsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        killsLabel.setForeground(Color.WHITE);
        killsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(killsLabel);

        JLabel positionLabel = new JLabel("Позиция: [" + player.gridX + ", " + player.gridY + "]");
        positionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        positionLabel.setForeground(Color.WHITE);
        positionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(positionLabel);

        return panel;
    }

    private void setupHotkeys() {
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void setupButtonActions() {
        continueButton.addActionListener(e -> {
            dispose();
            gamePanel.requestFocus();
        });

        saveButton.addActionListener(e -> showSaveDialog());
        loadButton.addActionListener(e -> showLoadDialog());
        questsButton.addActionListener(e -> showQuestsDialog());
        guideButton.addActionListener(e -> showGuideDialog());  // ← НОВЫЙ ОБРАБОТЧИК
        exitButton.addActionListener(e -> exitGame());
    }

    // ==================== НОВЫЙ МЕТОД - РУКОВОДСТВО ====================
    private void showGuideDialog() {
        JDialog guideDialog = new JDialog(this, "📖 РУКОВОДСТВО ПО ИГРЕ", true);
        guideDialog.setLayout(new BorderLayout(10, 10));
        guideDialog.setSize(750, 650);
        guideDialog.setLocationRelativeTo(this);

        // Основная панель с табами
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));

        // === ВКЛАДКА 1: УПРАВЛЕНИЕ ===
        JScrollPane controlsPanel = createControlsTab();
        tabbedPane.addTab("🎮 УПРАВЛЕНИЕ", controlsPanel);

        // === ВКЛАДКА 2: СМЕНА ЭКИПИРОВКИ ===
        JScrollPane equipmentPanel = createEquipmentTab();
        tabbedPane.addTab("🔧 СМЕНА ЭКИПИРОВКИ", equipmentPanel);

        // === ВКЛАДКА 3: СИСТЕМЫ ИГРЫ ===
        JScrollPane systemsPanel = createSystemsTab();
        tabbedPane.addTab("⚙️ СИСТЕМЫ", systemsPanel);

        // === ВКЛАДКА 4: СОЮЗНИКИ ===
        JScrollPane alliesPanel = createAlliesTab();
        tabbedPane.addTab("🤝 СОЮЗНИКИ", alliesPanel);

        guideDialog.add(tabbedPane, BorderLayout.CENTER);

        // Кнопка закрытия
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(30, 30, 40));
        JButton closeButton = new JButton("✖ ЗАКРЫТЬ");
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setBackground(new Color(100, 100, 100));
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> guideDialog.dispose());
        bottomPanel.add(closeButton);
        guideDialog.add(bottomPanel, BorderLayout.SOUTH);

        guideDialog.setVisible(true);
    }

    private JScrollPane createControlsTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 35));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Заголовок
        JLabel title = new JLabel("КЛАВИШИ УПРАВЛЕНИЯ");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(255, 215, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Таблица управления (4 колонки)
        JPanel gridPanel = new JPanel(new GridLayout(0, 4, 15, 10));
        gridPanel.setBackground(new Color(25, 25, 35));

        // Заголовки колонок
        gridPanel.add(createHeaderLabel("КЛАВИША"));
        gridPanel.add(createHeaderLabel("ДЕЙСТВИЕ"));
        gridPanel.add(createHeaderLabel("КЛАВИША"));
        gridPanel.add(createHeaderLabel("ДЕЙСТВИЕ"));

        // Данные
        String[][] controls = {
                {"W/A/S/D", "Движение", "ЛКМ", "Установить цель/движение"},
                {"ПРОБЕЛ", "Выстрел по цели", "ПКМ", "Информация об оружии"},
                {"F", "Переключить режим огня", "R", "Сбросить цель"},
                {"E", "Завершить ход", "X", "Переключить юнита"},
                {"I", "Открыть инвентарь", "V", "Следующий видимый враг"},
                {"B", "Вернуть камеру к юниту", "C", "Центрировать камеру"},
                {"ESC", "Открыть меню", "", ""},
        };

        for (String[] row : controls) {
            gridPanel.add(createKeyLabel(row[0]));
            gridPanel.add(createValueLabel(row[1]));
            gridPanel.add(createKeyLabel(row[2]));
            gridPanel.add(createValueLabel(row[3]));
        }

        panel.add(gridPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Дополнительные подсказки
        JPanel tipsPanel = new JPanel();
        tipsPanel.setLayout(new BoxLayout(tipsPanel, BoxLayout.Y_AXIS));
        tipsPanel.setBackground(new Color(35, 35, 45));
        tipsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel tipsTitle = new JLabel("💡 ПОЛЕЗНЫЕ СОВЕТЫ");
        tipsTitle.setFont(new Font("Arial", Font.BOLD, 12));
        tipsTitle.setForeground(new Color(100, 200, 255));
        tipsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        tipsPanel.add(tipsTitle);
        tipsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        String[] tips = {
                "• Прицельный огонь (F) имеет больший шанс попадания, но дороже",
                "• Вы можете управлять не только Leichttraktor, но и нанятыми союзниками (X)",
                "• Нажмите V, чтобы переключить камеру на видимых врагов",
                "• Следите за весом инвентаря — перегруз снижает очки хода",
                "• Разные типы снарядов имеют разную точность и урон",
                "• Используйте холмы для бонуса к точности",
                "• Просроченный хлеб лечит, но накладывает штраф на 3 хода"
        };

        for (String tip : tips) {
            JLabel tipLabel = new JLabel(tip);
            tipLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            tipLabel.setForeground(Color.LIGHT_GRAY);
            tipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            tipsPanel.add(tipLabel);
        }

        panel.add(tipsPanel);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(25, 25, 35));
        return scrollPane;
    }

    private JScrollPane createEquipmentTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 35));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ===== РАЗДЕЛ 1: КАК СМЕНИТЬ ОРУЖИЕ =====
        JLabel title1 = new JLabel("🔫 КАК СМЕНИТЬ ОРУЖИЕ");
        title1.setFont(new Font("Arial", Font.BOLD, 16));
        title1.setForeground(new Color(255, 215, 0));
        title1.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title1);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] steps = {
                "1. Нажмите I, чтобы открыть инвентарь",
                "2. Кликните левой кнопкой мыши по оружию в инвентаре (оно подсветится)",
                "3. Кликните по слоту 0,0 (левый верхний угол инвентаря)",
                "4. Подтвердите смену оружия",
                "",
                "⚠ ВАЖНО: Смена оружия требует ПОЛНОГО запаса очков хода!",
                "   После смены оружия будет применён штраф за переэкипировку."
        };

        for (String step : steps) {
            JLabel stepLabel = new JLabel(step);
            stepLabel.setFont(new Font("Arial", step.isEmpty() ? Font.PLAIN : Font.PLAIN, 12));
            stepLabel.setForeground(step.isEmpty() ? Color.BLACK : (step.contains("⚠") ? new Color(255, 150, 50) : Color.WHITE));
            stepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(stepLabel);
            if (!step.isEmpty()) panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // ===== РАЗДЕЛ 2: ДОСТУПНОЕ ОРУЖИЕ =====
        JLabel title2 = new JLabel("📦 ДОСТУПНОЕ ОРУЖИЕ");
        title2.setFont(new Font("Arial", Font.BOLD, 16));
        title2.setForeground(new Color(255, 215, 0));
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title2);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Таблица оружия
        String[][] weapons = {
                {"2 cm Breda (I)", "20mm", "3", "30", "5/11", "6", "15", "4"},
                {"7,92 mm Mauser E.W. 141", "8mm", "8", "25", "7/14", "22", "12", "3"},
                {"13 mm Autocannon Type Ho", "13mm_japan", "3", "17", "4/9", "12", "12", "3"},
                {"13,2 mm Hotchkiss mle. 1930", "13mm_french", "3", "13", "4/9", "9", "10", "3"},
                {"45 мм обр. 1932 г.", "45mm", "1", "15", "11/22", "10", "19", "5"},
                {"47 mm SA35", "47mm_french", "1", "21", "9/18", "10", "17", "5"},
                {"Cannone da 47-32", "47mm_italian", "1", "17", "9/18", "10", "16", "5"},
                {"25mm Canon Raccourci mle. 1934", "25mm", "2", "45", "4/9", "14", "15", "4"},
                {"3 cm M.K. 103A", "30mm", "3", "15", "5/11", "38", "45", "9"},
                {"Cannone da 37-40", "37mm_italian", "1", "15", "5/11", "38", "45", "9"},
                {"37 mm Semiautomatic Gun M1924", "37mm_american", "5", "10", "9/18", "25", "20", "4"},
                {"37 mm Semiautomatic Gun M1924", "37mm_american", "5", "10", "9/18", "25", "20", "4"},
                {"37 mm kan m-38-49 strv", "37mm_sweden", "1", "50", "8/16", "12", "24", "5"},
                {"76 мм Л-10С", "76mm", "1", "5", "18/36", "16", "43", "12"},
                {"10,5 cm StuH 42 L28", "105mm", "1", "20", "24/48", "26", "60", "14"},
                {"12,8 cm Kw.K. L50", "128mm", "1", "30", "30/60", "36", "80", "19"},
                {"8-inch Howitzer M47", "203mm", "1", "20", "32/64", "75", "90", "26"}
        };

        JPanel weaponTable = new JPanel(new GridLayout(weapons.length + 1, 8, 5, 2));
        weaponTable.setBackground(new Color(35, 35, 45));
        weaponTable.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100), 1));

        // Заголовки
        String[] headers = {"Название", "Калибр", "Выстрелов", "Точность", "Стоимость (Б/П)", "Перезарядка", "Сила", "Вес"};
        for (String h : headers) {
            JLabel header = new JLabel(h, SwingConstants.CENTER);
            header.setFont(new Font("Arial", Font.BOLD, 10));
            header.setForeground(new Color(255, 200, 100));
            header.setBackground(new Color(45, 45, 55));
            header.setOpaque(true);
            weaponTable.add(header);
        }

        for (String[] w : weapons) {
            for (String val : w) {
                JLabel cell = new JLabel(val, SwingConstants.CENTER);
                cell.setFont(new Font("Arial", Font.PLAIN, 10));
                cell.setForeground(Color.WHITE);
                weaponTable.add(cell);
            }
        }

        panel.add(weaponTable);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // ===== РАЗДЕЛ 3: ПЕРЕЗАРЯДКА =====
        JLabel title3 = new JLabel("🔄 ПЕРЕЗАРЯДКА");
        title3.setFont(new Font("Arial", Font.BOLD, 14));
        title3.setForeground(new Color(255, 215, 0));
        title3.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title3);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] reloadInfo = {
                "• Нажмите I, чтобы открыть инвентарь",
                "• В левой панели (оружие) нажмите кнопку 'ПЕРЕЗАРЯДИТЬ'",
                "• Если у вас есть снаряды нужного калибра в инвентаре, они будут заряжены",
                "• Если есть несколько типов снарядов (базовые/улучшенные) — появится выбор",
                "",
                "⚠ Перезарядка стоит очков хода (указано в характеристиках оружия)",
                "⚠ Разные снаряды дают разные бонусы: улучшенные имеют +50% точность!"
        };

        for (String info : reloadInfo) {
            JLabel infoLabel = new JLabel(info);
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            infoLabel.setForeground(info.contains("⚠") ? new Color(255, 150, 50) :
                    (info.isEmpty() ? Color.BLACK : Color.WHITE));
            infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(infoLabel);
            if (!info.isEmpty()) panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(25, 25, 35));
        return scrollPane;
    }

    private JScrollPane createSystemsTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 35));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ===== ОПЫТ И УРОВНИ =====
        JLabel title1 = new JLabel("⭐ ОПЫТ И УРОВНИ");
        title1.setFont(new Font("Arial", Font.BOLD, 16));
        title1.setForeground(new Color(255, 215, 0));
        title1.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title1);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] expInfo = {
                "• Опыт получают ВСЕ члены команды (игрок и союзники)",
                "• Убийца получает 80% опыта, остальные 20% делятся поровну",
                "• При повышении уровня вы получаете бонусы на выбор:",
                "   ❤️ +10 к макс. здоровью     🎯 +1 к точности оружия",
                "   💪 +1 к силе (больше ОХ)     🦶 +1 к ловкости (дешевле шаг)",
                "   🛡️ +1 к броне                ⚡ +1% к критическому шансу",
                "   👁️ +1 к зрению (больше обзор) 🔄 -1 к стоимости перезарядки",
                "   💨 +1 к проворности (уклонение)"
        };

        for (String info : expInfo) {
            JLabel infoLabel = new JLabel(info);
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            infoLabel.setForeground(info.startsWith("   ") ? new Color(150, 200, 255) : Color.WHITE);
            infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(infoLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 2)));
        }

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // ===== МОДЕРНИЗАЦИЯ =====
        JLabel title2 = new JLabel("🔧 МОДЕРНИЗАЦИЯ ТАНКА");
        title2.setFont(new Font("Arial", Font.BOLD, 16));
        title2.setForeground(new Color(255, 215, 0));
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title2);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] upgradeInfo = {
                "• Модернизация доступна в инвентаре (кнопка 'МОДЕРНИЗАЦИЯ')",
                "• Детали выпадают с убитых врагов",
                "",
                "📊 Классы модернизации:",
                "   ПТ (ПТ-2 → ПТ-3 → ПТ-4): +урон, +точность, -здоровье, -ловкость",
                "   ТТ (ТТ-2 → ТТ-3 → ТТ-4): +здоровье, +броня, -ловкость",
                "   СТ (СТ-2 → СТ-3 → СТ-4): сбалансированные бонусы",
                "   ЛТ (ЛТ-2 → ЛТ-3 → ЛТ-4): +ловкость, +обзор, +уклонение, -броня, -здоровье",
                "",
                "💰 Стоимость модернизации:",
                "   1→2 уровень: 190 деталей",
                "   2→3 уровень: 470 деталей",
                "   3→4 уровень: 1150 деталей"
        };

        for (String info : upgradeInfo) {
            JLabel infoLabel = new JLabel(info);
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            infoLabel.setForeground(info.startsWith("   ") ? new Color(200, 200, 150) :
                    (info.startsWith("💰") || info.startsWith("📊")) ? new Color(255, 200, 100) :
                            info.isEmpty() ? Color.BLACK : Color.WHITE);
            infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(infoLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 2)));
        }

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // ===== ВЕС И ОЧКИ ХОДА =====
        JLabel title3 = new JLabel("⚖️ ВЕС И ОЧКИ ХОДА");
        title3.setFont(new Font("Arial", Font.BOLD, 16));
        title3.setForeground(new Color(255, 215, 0));
        title3.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title3);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] weightInfo = {
                "• Максимальная грузоподъёмность = Сила × 0.5 (в кг)",
                "• Каждый предмет в инвентаре имеет вес",
                "• Очки хода = (Сила × 1.5) − вес оружия − вес инвентаря",
                "• При перегрузе (вес > грузоподъёмности) движение невозможно!",
                "",
                "💡 Совет: Выбрасывайте ненужные предметы через инвентарь (кнопка '🗑')"
        };

        for (String info : weightInfo) {
            JLabel infoLabel = new JLabel(info);
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            infoLabel.setForeground(info.contains("💡") ? new Color(100, 255, 100) : Color.WHITE);
            infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(infoLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 2)));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(25, 25, 35));
        return scrollPane;
    }

    private JScrollPane createAlliesTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 35));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("🤝 ДОСТУПНЫЕ СОЮЗНИКИ");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(new Color(255, 215, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Таблица союзников
        String[][] allies = {
                {"MS-1", "100", "35", "40", "40", "15%", "14", "20%", "7.1%", "45 мм обр. 1932 г."},
                {"M53", "220", "90", "15", "30", "12%", "20", "30%", "5.0%", "8-inch Howitzer M47"},
                {"VK 100.01 P", "1600", "85", "10", "70", "50%", "10", "40%", "5.0%", "12,8 cm Kw.K. L50"},
                {"AMX 40", "400", "50", "20", "10", "12%", "30", "10%", "5.0%", "47 mm SA35"}
        };

        String[] headers = {"Имя", "HP", "Сила", "Ловк", "Точн", "Крит", "Зрен", "Перез", "Укл", "Оружие"};

        JPanel allyTable = new JPanel(new GridLayout(allies.length + 1, headers.length, 5, 2));
        allyTable.setBackground(new Color(35, 35, 45));
        allyTable.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100), 1));

        for (String h : headers) {
            JLabel header = new JLabel(h, SwingConstants.CENTER);
            header.setFont(new Font("Arial", Font.BOLD, 10));
            header.setForeground(new Color(255, 200, 100));
            header.setBackground(new Color(45, 45, 55));
            header.setOpaque(true);
            allyTable.add(header);
        }

        for (String[] ally : allies) {
            for (String val : ally) {
                JLabel cell = new JLabel(val, SwingConstants.CENTER);
                cell.setFont(new Font("Arial", Font.PLAIN, 10));
                cell.setForeground(Color.WHITE);
                allyTable.add(cell);
            }
        }

        panel.add(allyTable);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Информация о найме
        JLabel hireTitle = new JLabel("📢 КАК НАНЯТЬ СОЮЗНИКА");
        hireTitle.setFont(new Font("Arial", Font.BOLD, 14));
        hireTitle.setForeground(new Color(255, 215, 0));
        hireTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(hireTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        String[] hireInfo = {
                "• Союзники отмечены синими значками на карте",
                "• Подойдите вплотную к союзнику и кликните на него ЛКМ",
                "• Некоторые союзники имеют уникальные задания (квесты)",
                "• После найма вы можете управлять союзником (нажмите X)"
        };

        for (String info : hireInfo) {
            JLabel infoLabel = new JLabel(info);
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            infoLabel.setForeground(Color.WHITE);
            infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(infoLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 3)));
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(25, 25, 35));
        return scrollPane;
    }

    // Вспомогательные методы для создания стилизованных меток
    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(new Color(255, 200, 100));
        label.setBackground(new Color(45, 45, 55));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100)));
        return label;
    }

    private JLabel createKeyLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(new Color(100, 200, 255));
        label.setBackground(new Color(40, 40, 50));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100)));
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        label.setForeground(Color.WHITE);
        label.setBackground(new Color(40, 40, 50));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100)));
        return label;
    }

    // ==================== ОСТАЛЬНЫЕ МЕТОДЫ (БЕЗ ИЗМЕНЕНИЙ) ====================

    private void showSaveDialog() {
        JDialog saveDialog = new JDialog(this, "Сохранить игру", true);
        saveDialog.setLayout(new BorderLayout());
        saveDialog.setSize(500, 550);
        saveDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(20, 20, 30));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("ВЫБЕРИТЕ СЛОТ ДЛЯ СОХРАНЕНИЯ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel slotsPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        slotsPanel.setBackground(new Color(20, 20, 30));
        slotsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        List<SaveSlotInfo> slots = GameSave.getAllSlots();

        for (SaveSlotInfo slot : slots) {
            JPanel slotCard = createSaveSlotCard(slot, true);
            final int slotNumber = slot.slotNumber;
            slotCard.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    handleSaveSlotClick(slotNumber, slot.isEmpty, saveDialog);
                }
            });
            slotsPanel.add(slotCard);
        }

        JScrollPane scrollPane = new JScrollPane(slotsPanel);
        scrollPane.setBackground(new Color(20, 20, 30));
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(20, 20, 30));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(20, 20, 30));
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> saveDialog.dispose());
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        saveDialog.add(mainPanel);
        saveDialog.setVisible(true);
    }

    private void handleSaveSlotClick(int slotNumber, boolean isEmpty, JDialog dialog) {
        if (!isEmpty) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Слот " + slotNumber + " уже содержит сохранение!\n\n" +
                            "Вы действительно хотите перезаписать это сохранение?",
                    "Перезапись сохранения",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        BufferedImage screenshot = GameSave.createScreenshot(gamePanel);
        // ИСПРАВЛЕНО: добавлен пятый аргумент - путь к текущему уровню
        GameSave.saveToSlot(world, player, slotNumber, screenshot, gamePanel.getCurrentLevelPath());
        dialog.dispose();

        JOptionPane.showMessageDialog(this,
                "Игра сохранена в слот " + slotNumber + "!",
                "Сохранение",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLoadDialog() {
        JDialog loadDialog = new JDialog(this, "Загрузить игру", true);
        loadDialog.setLayout(new BorderLayout());
        loadDialog.setSize(500, 550);
        loadDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(20, 20, 30));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("ВЫБЕРИТЕ СЛОТ ДЛЯ ЗАГРУЗКИ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel slotsPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        slotsPanel.setBackground(new Color(20, 20, 30));
        slotsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        List<SaveSlotInfo> slots = GameSave.getAllSlots();
        boolean hasSaves = false;

        for (SaveSlotInfo slot : slots) {
            if (!slot.isEmpty) hasSaves = true;
            JPanel slotCard = createSaveSlotCard(slot, false);
            final int slotNumber = slot.slotNumber;

            if (!slot.isEmpty) {
                slotCard.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        handleLoadSlotClick(slotNumber, loadDialog);
                    }
                });
            }
            slotsPanel.add(slotCard);
        }

        if (!hasSaves) {
            JLabel emptyLabel = new JLabel("Нет сохранённых игр", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            slotsPanel.add(emptyLabel);
        }

        JScrollPane scrollPane = new JScrollPane(slotsPanel);
        scrollPane.setBackground(new Color(20, 20, 30));
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(20, 20, 30));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(20, 20, 30));
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> loadDialog.dispose());
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        loadDialog.add(mainPanel);
        loadDialog.setVisible(true);
    }

    private void handleLoadSlotClick(int slotNumber, JDialog dialog) {
        SaveInfo info = GameSave.getSlotInfo(slotNumber);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Загрузить сохранение из слота " + slotNumber + "?\n\n" +
                        "Дата: " + (info != null && info.saveDate != null ? info.saveDate : "Неизвестно") + "\n" +
                        "Убито врагов: " + (info != null ? info.enemiesKilled : 0) + "\n" +
                        "Здоровье: " + (info != null ? info.playerHealth : 0) + "\n\n" +
                        "Несохранённый прогресс будет потерян!",
                "Загрузка игры",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            GameSave save = GameSave.loadFromSlot(slotNumber);
            if (save != null) {
                save.setGamePanel(gamePanel);
                save.restoreToWorld(world, player);

                world.getControllableUnits().clear();
                for (FriendlyUnit unit : world.getFriendlyUnits()) {
                    if (unit.isRecruited && unit.isAlive) {
                        world.addControllableUnit(unit);
                    }
                }

                gamePanel.syncRecruitedUnits();
                gamePanel.repaint();
                dialog.dispose();
                dispose();
                gamePanel.requestFocusInWindow();

                JOptionPane.showMessageDialog(this,
                        "Игра загружена!\nСлот: " + slotNumber,
                        "Загрузка",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка загрузки игры!",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createSaveSlotCard(SaveSlotInfo slot, boolean forSave) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(new Color(40, 40, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        card.setPreferredSize(new Dimension(140, 160));

        if (!slot.isEmpty && !forSave) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        JLabel numberLabel = new JLabel("СЛОТ " + slot.slotNumber);
        numberLabel.setFont(new Font("Arial", Font.BOLD, 12));
        numberLabel.setForeground(new Color(255, 215, 0));
        numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(numberLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(40, 40, 50));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        if (slot.isEmpty) {
            JLabel emptyLabel = new JLabel(forSave ? "ПУСТО" : "НЕТ ДАННЫХ");
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 12));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            centerPanel.add(emptyLabel, BorderLayout.CENTER);

            if (forSave) {
                JLabel clickLabel = new JLabel("▼ НАЖМИТЕ ▼");
                clickLabel.setFont(new Font("Arial", Font.PLAIN, 9));
                clickLabel.setForeground(new Color(0, 200, 0));
                clickLabel.setHorizontalAlignment(SwingConstants.CENTER);
                centerPanel.add(clickLabel, BorderLayout.SOUTH);
            }
        } else {
            if (slot.saveInfo != null && slot.saveInfo.thumbnail != null) {
                JLabel thumbnailLabel = new JLabel(new ImageIcon(slot.saveInfo.thumbnail));
                thumbnailLabel.setHorizontalAlignment(SwingConstants.CENTER);
                centerPanel.add(thumbnailLabel, BorderLayout.CENTER);
            } else {
                JPanel placeholder = new JPanel();
                placeholder.setBackground(new Color(60, 60, 70));
                placeholder.setPreferredSize(new Dimension(120, 90));
                JLabel noImageLabel = new JLabel("🎮");
                noImageLabel.setFont(new Font("Arial", Font.PLAIN, 40));
                noImageLabel.setForeground(Color.GRAY);
                placeholder.add(noImageLabel);
                centerPanel.add(placeholder, BorderLayout.CENTER);
            }
        }
        card.add(centerPanel, BorderLayout.CENTER);

        if (!slot.isEmpty && slot.saveInfo != null) {
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
            bottomPanel.setBackground(new Color(40, 40, 50));
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

            JLabel killsLabel = new JLabel("💀 " + slot.saveInfo.enemiesKilled);
            killsLabel.setFont(new Font("Arial", Font.PLAIN, 9));
            killsLabel.setForeground(Color.WHITE);
            killsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            bottomPanel.add(killsLabel);

            JLabel healthLabel = new JLabel("❤ " + slot.saveInfo.playerHealth);
            healthLabel.setFont(new Font("Arial", Font.PLAIN, 9));
            healthLabel.setForeground(Color.WHITE);
            healthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            bottomPanel.add(healthLabel);

            if (slot.saveInfo.saveDate != null) {
                String dateStr = String.format("%1$td.%1$tm.%1$tY", slot.saveInfo.saveDate);
                JLabel dateLabel = new JLabel(dateStr);
                dateLabel.setFont(new Font("Arial", Font.PLAIN, 8));
                dateLabel.setForeground(Color.LIGHT_GRAY);
                dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                bottomPanel.add(dateLabel);
            }

            card.add(bottomPanel, BorderLayout.SOUTH);
        }

        return card;
    }

    private void showQuestsDialog() {
        StringBuilder questsText = new StringBuilder();
        questsText.append("=== СПИСОК КВЕСТОВ ===\n\n");

        boolean hasQuests = false;

        for (QuestNPC npc : world.getQuestNPCs()) {
            hasQuests = true;
            questsText.append("📌 У моего старого знакомого дедушки T18 горе: он потерял сына и не может с этим смириться. \n Что случилось? " +
                    "Ведь ещё недавно японцы и американцы жили мирно и обсуждали между собой бытовые проблемы! \n" +
                    "Ладно... Похоже, придётся его спасать... Возможно, мне не удастся мирно договориться с захватчиками, но я не хочу, чтобы мирные танки страдали. \n");
            questsText.append("   Цель: необходимо освободить M53 из японского плена и живым привести его к T18. \n");
            questsText.append("   Награда: команда получит 500 опыта.");
        }

        if (!hasQuests) {
            questsText.append("Нет активных квестов.\n");
            questsText.append("Найдите NPC на карте, чтобы получить квесты!\n");
            questsText.append("NPC отмечены золотым значком на карте.");
        }

        JTextArea textArea = new JTextArea(questsText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(new Color(30, 30, 40));
        textArea.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this,
                scrollPane,
                "Список квестов",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void exitGame() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите выйти из игры?\nНесохранённый прогресс будет потерян!",
                "Выход из игры",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            Component[] components = ((JPanel)getContentPane().getComponent(0)).getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel infoPanel = (JPanel) comp;
                    Component[] infoComponents = infoPanel.getComponents();
                    for (Component infoComp : infoComponents) {
                        if (infoComp instanceof JLabel) {
                            JLabel label = (JLabel) infoComp;
                            if (label.getText().startsWith("❤")) {
                                label.setText("Здоровье: " + player.health + "/" + player.maxHealth);
                            } else if (label.getText().startsWith("💀")) {
                                label.setText("Уничтожено врагов: " + world.getTotalEnemiesKilled());
                            } else if (label.getText().startsWith("📍")) {
                                label.setText("Позиция: [" + player.gridX + ", " + player.gridY + "]");
                            }
                        }
                    }
                }
            }
        }
        super.setVisible(visible);
    }
}