package ui;

import entities.*;
import world.GameWorld;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class MiniMapDialog extends JDialog {
    private GameWorld world;
    private PlayerTank player;
    private List<FriendlyUnit> friendlyUnits;
    private List<Enemy> enemies;

    private static final int MINIMAP_SIZE = 400;
    private static final int CELL_SIZE = MINIMAP_SIZE / 80; // 400 / 80 = 5 пикселей на клетку

    public MiniMapDialog(JFrame parent, GameWorld world, PlayerTank player) {
        super(parent, "🗺️ КАРТА", false); // false - немодальное окно
        this.world = world;
        this.player = player;
        this.friendlyUnits = world.getFriendlyUnits();
        this.enemies = world.getEnemies();

        setSize(MINIMAP_SIZE + 20, MINIMAP_SIZE + 80);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Закрытие по клавише M или Escape
        JRootPane rootPane = getRootPane();
        rootPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_M, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Панель с картой
        JPanel mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMinimap(g);
            }
        };
        mapPanel.setPreferredSize(new Dimension(MINIMAP_SIZE, MINIMAP_SIZE));
        mapPanel.setBackground(new Color(30, 40, 30));
        mapPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));

        // Панель с легендой
        JPanel legendPanel = createLegendPanel();

        setLayout(new BorderLayout(5, 5));
        add(mapPanel, BorderLayout.CENTER);
        add(legendPanel, BorderLayout.SOUTH);

        // Таймер для обновления карты (каждые 500 мс)
        Timer refreshTimer = new Timer(500, e -> {
            this.friendlyUnits = world.getFriendlyUnits();
            this.enemies = world.getEnemies();
            mapPanel.repaint();
        });
        refreshTimer.start();

        // Остановить таймер при закрытии окна
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                refreshTimer.stop();
            }
        });
    }

    private void drawMinimap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Рисуем сетку
        g2d.setColor(new Color(50, 60, 50));
        for (int i = 0; i <= 80; i++) {
            int pos = i * CELL_SIZE;
            g2d.drawLine(pos, 0, pos, MINIMAP_SIZE);
            g2d.drawLine(0, pos, MINIMAP_SIZE, pos);
        }

        // ===== 1. КВЕСТОВЫЕ NPC (T18, Sav m/43) - ЗОЛОТЫЕ =====
        for (QuestNPC npc : world.getQuestNPCs()) {
            // Не показываем NPC, у которых квест полностью выполнен (isQuestFinished)
            if (npc.isQuestFinished) continue;

            int x = npc.gridX * CELL_SIZE;
            int y = npc.gridY * CELL_SIZE;

            // Золотой цвет для квестовых NPC
            g2d.setColor(new Color(255, 215, 0, 200));
            g2d.fillRect(x - 3, y - 3, 6, 6);
            g2d.setColor(new Color(255, 200, 0));
            g2d.fillRect(x - 2, y - 2, 4, 4);

            // Восклицательный знак для активных квестов
            if (!npc.isQuestCompleted && npc.hasReceivedQuest()) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 7));
                g2d.drawString("!", x, y - 2);
            } else if (!npc.hasReceivedQuest()) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 7));
                g2d.drawString("?", x, y - 2);
            }
        }

        // ===== 2. ТОРГОВЦЫ (T34 и др.) - ИЗУМРУДНО-ЗОЛОТЫЕ =====
        for (Trader trader : world.getTraders()) {
            int x = trader.gridX * CELL_SIZE;
            int y = trader.gridY * CELL_SIZE;

            g2d.setColor(new Color(100, 200, 50, 200));
            g2d.fillRect(x - 3, y - 3, 6, 6);
            g2d.setColor(new Color(150, 255, 100));
            g2d.fillRect(x - 2, y - 2, 4, 4);

            // Иконка монеты
            g2d.setColor(new Color(255, 215, 0));
            g2d.setFont(new Font("Arial", Font.BOLD, 6));
            g2d.drawString("💰", x - 1, y - 1);
        }

        // ===== 3. НЕНАНЯТЫЕ СОЮЗНИКИ (MS-1, M53, VK10001P, AMX40) - СИНИЕ =====
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (!friendly.isRecruited && friendly.isAlive && !friendly.isUnavailable) {
                int x = friendly.gridX * CELL_SIZE;
                int y = friendly.gridY * CELL_SIZE;

                // Синий цвет для ненаймлённых союзников
                g2d.setColor(new Color(50, 100, 200, 200));
                g2d.fillRect(x - 3, y - 3, 6, 6);
                g2d.setColor(new Color(100, 150, 255));
                g2d.fillRect(x - 2, y - 2, 4, 4);

                // Маленький "+" для обозначения возможности найма
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 6));
                g2d.drawString("+", x - 1, y - 1);
            }
        }

        // ===== 4. НАНЯТЫЕ СОЮЗНИКИ - ЗЕЛЁНЫЕ (уже есть, оставляем) =====
        for (FriendlyUnit friendly : world.getFriendlyUnits()) {
            if (friendly.isAlive && friendly.isRecruited) {
                int x = friendly.gridX * CELL_SIZE;
                int y = friendly.gridY * CELL_SIZE;
                g2d.setColor(new Color(0, 200, 0));
                g2d.fillOval(x - 3, y - 3, 6, 6);
                g2d.setColor(new Color(0, 255, 0));
                g2d.fillOval(x - 2, y - 2, 4, 4);
            }
        }

        // ===== 5. ВИДИМЫЕ ВРАГИ - КРАСНЫЕ (уже есть) =====
        for (Enemy enemy : enemies) {
            if (enemy.isAlive) {
                boolean isVisible = world.isEnemyVisibleByTeam(enemy);
                if (isVisible) {
                    int x = enemy.gridX * CELL_SIZE;
                    int y = enemy.gridY * CELL_SIZE;
                    g2d.setColor(new Color(200, 0, 0));
                    g2d.fillOval(x - 3, y - 3, 6, 6);
                    g2d.setColor(new Color(255, 0, 0));
                    g2d.fillOval(x - 2, y - 2, 4, 4);
                }
            }
        }

        // ===== 6. ИГРОК - СИНЯЯ ТОЧКА (уже есть) =====
        int playerX = player.gridX * CELL_SIZE;
        int playerY = player.gridY * CELL_SIZE;
        g2d.setColor(new Color(0, 100, 200));
        g2d.fillOval(playerX - 5, playerY - 5, 10, 10);
        g2d.setColor(new Color(0, 150, 255));
        g2d.fillOval(playerX - 3, playerY - 3, 6, 6);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(playerX - 1, playerY - 1, 2, 2);

        // ===== 7. ОБЛАСТЬ ОБЗОРА ИГРОКА (уже есть) =====
        int viewRadiusPixels = GameWorld.VIEW_RADIUS * CELL_SIZE;
        g2d.setColor(new Color(100, 150, 255, 60));
        g2d.fillOval(playerX - viewRadiusPixels, playerY - viewRadiusPixels,
                viewRadiusPixels * 2, viewRadiusPixels * 2);
        g2d.setColor(new Color(100, 150, 255, 120));
        g2d.drawOval(playerX - viewRadiusPixels, playerY - viewRadiusPixels,
                viewRadiusPixels * 2, viewRadiusPixels * 2);

        // Рамка для игрока
        g2d.setColor(new Color(255, 215, 0));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(playerX - 6, playerY - 6, 12, 12);
    }

    private JPanel createLegendPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Верхняя панель с иконками
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        iconsPanel.setOpaque(false);

        addLegendIcon(iconsPanel, "●", new Color(0, 100, 200), "Игрок");
        addLegendIcon(iconsPanel, "●", new Color(0, 200, 0), "Союзник (нанятый)");
        addLegendIcon(iconsPanel, "■", new Color(50, 100, 200), "Можно нанять");
        addLegendIcon(iconsPanel, "■", new Color(255, 215, 0), "Квестовый NPC");
        addLegendIcon(iconsPanel, "■", new Color(100, 200, 50), "Торговец");
        addLegendIcon(iconsPanel, "●", new Color(200, 0, 0), "Видимый враг");

        panel.add(iconsPanel);

        // ===== ПОДСКАЗКА =====
        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        hintPanel.setOpaque(false);

        JLabel hintLabel = new JLabel("💡 Наведи курсор на значок");
        hintLabel.setFont(new Font("Arial", Font.PLAIN, 8));
        hintLabel.setForeground(new Color(100, 200, 150));
        hintPanel.add(hintLabel);

        panel.add(hintPanel);

        // Кнопка закрытия
        JLabel closeHint = new JLabel("✖ M / ESC");
        closeHint.setFont(new Font("Arial", Font.PLAIN, 8));
        closeHint.setForeground(Color.GRAY);
        closeHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(closeHint);

        return panel;
    }

    private void addLegendIcon(JPanel panel, String shape, Color color, String tooltip) {
        JLabel label = new JLabel(shape);
        label.setForeground(color);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setToolTipText(tooltip);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(label);
    }

    private JPanel createLegendItem(Color color, String shape, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        JLabel shapeLabel = new JLabel(shape);
        shapeLabel.setForeground(color);
        shapeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(shapeLabel);

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        textLabel.setForeground(Color.WHITE);
        panel.add(textLabel);

        return panel;
    }
}