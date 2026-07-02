package editor;

import entities.*;
import combat.ExperienceSystem;
import inventory.KeyItem;
import world.LevelLoader;
import inventory.Item;
import inventory.Caliber;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.awt.image.BufferedImage;

public class LevelEditor extends JFrame {
    private List<Wall> walls = new ArrayList<>();
    private List<QuestNPC> npcs = new ArrayList<>();
    private List<Tree> trees = new ArrayList<>();
    private List<Pavement> pavements = new ArrayList<>();  // ← ДОБАВИТЬ
    private List<OakPlanks> oakPlanks = new ArrayList<>();
    private List<IronFloor> ironFloors = new ArrayList<>();
    private List<InfernalLand> infernalLands = new ArrayList<>();

    private List<Water> waters = new ArrayList<>();
    private List<FriendlyUnit> friendlies = new ArrayList<>();
    private List<StorageChest> storageChests = new ArrayList<>();
    private List<GarbageContainer> garbageContainers = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Trader> traders = new ArrayList<>();

    private List<Door> doors = new ArrayList<>();

    private int gridWidth = 80;
    private int gridHeight = 80;
    private int cellSize = 30;
    private JPanel gridPanel;
    private Point selectedCell = null;
    private String currentLevelPath = null;

    private Point winZone = null;  // Победная зона (5x5)



    // Режим редактирования
    private enum DecorType { PAVEMENT, OAKPLANKS, INFERNALLAND, IRONFLOOR }
    private DecorType currentDecorType = DecorType.PAVEMENT;
    private enum EditMode {
        WALL, NPC, TREE, DECOR, FRIENDLY, STORAGE, ENEMY, START_POSITION, GARBAGE, TRADER, WINZONE, WATER, DOOR
    }
    private EditMode currentMode = EditMode.WALL;

    // Для массовой заливки
    private Point fillStartCell = null;
    private boolean isFillMode = false;

    private Point startPosition = null;

    // После других BufferedImage полей:
    private BufferedImage vk10001pImage;

    public LevelEditor() {
        setTitle("Редактор уровней - TankFight");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);

        gridPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Сетка
                g2d.setColor(Color.LIGHT_GRAY);
                for (int x = 0; x <= gridWidth; x++) {
                    g2d.drawLine(x * cellSize, 0, x * cellSize, gridHeight * cellSize);
                }
                for (int y = 0; y <= gridHeight; y++) {
                    g2d.drawLine(0, y * cellSize, gridWidth * cellSize, y * cellSize);
                }

                if (startPosition != null) {
                    int x = startPosition.x * cellSize;
                    int y = startPosition.y * cellSize;
                    int width = 5 * cellSize;
                    int height = 5 * cellSize;

                    g.setColor(new Color(0, 255, 0, 80));
                    g.fillRect(x, y, width, height);
                    g.setColor(new Color(0, 200, 0, 200));
                    g2d.setStroke(new BasicStroke(2));
                    g.drawRect(x, y, width, height);

                    // Рисуем иконку Leichttraktor в центре
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 14));
                    g.drawString("🚀 СТАРТ", x + width/2 - 20, y + height/2 + 5);
                }

                // АСФАЛЬТ (рисуем первым, чтобы был под всеми объектами)
                for (Pavement pavement : pavements) {
                    int x = pavement.gridX * cellSize;
                    int y = pavement.gridY * cellSize;
                    g2d.setColor(new Color(80, 80, 80));
                    g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                    g2d.setColor(new Color(60, 60, 60));
                    g2d.drawRect(x, y, cellSize - 1, cellSize - 1);
                    // Рисуем текстуру асфальта (небольшие точки)
                    g2d.setColor(new Color(50, 50, 50));
                    for (int i = 0; i < 5; i++) {
                        int dotX = x + 5 + (i * 5);
                        int dotY = y + 15;
                        g2d.fillRect(dotX, dotY, 2, 2);
                    }
                }

                for (OakPlanks oakPlank : oakPlanks) {
                    int x = oakPlank.gridX * cellSize;
                    int y = oakPlank.gridY * cellSize;
                    g2d.setColor(new Color(160, 120, 80));
                    g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                    g2d.setColor(new Color(120, 80, 40));
                    for (int i = 0; i < 3; i++) {
                        g2d.drawLine(x + 5, y + 10 + i * 10, x + cellSize - 5, y + 10 + i * 10);
                    }
                }

                for (IronFloor ironFloor : ironFloors) {
                    int x = ironFloor.gridX * cellSize;
                    int y = ironFloor.gridY * cellSize;
                    g2d.setColor(new Color(120, 120, 130));
                    g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                    g2d.setColor(new Color(100, 100, 110));
                    g2d.drawRect(x, y, cellSize - 1, cellSize - 1);
                    g2d.setColor(new Color(140, 140, 150));
                    g2d.drawLine(x + 10, y, x + 10, y + cellSize);
                    g2d.drawLine(x + cellSize - 10, y, x + cellSize - 10, y + cellSize);
                }

                // Стены
                for (Wall wall : walls) {
                    int x = wall.gridX * cellSize;
                    int y = wall.gridY * cellSize;
                    g2d.setColor(wall.isDestructible ? new Color(139, 69, 19) : Color.DARK_GRAY);
                    g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(String.valueOf(wall.health), x + 5, y + 15);
                }

                // Деревья
                for (Tree tree : trees) {
                    int x = tree.gridX * cellSize;
                    int y = tree.gridY * cellSize;
                    g2d.setColor(new Color(34, 139, 34, 200));
                    g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                    g2d.setColor(new Color(139, 69, 19));
                    g2d.fillRect(x + cellSize/2 - 3, y + cellSize/2 - 5, 6, 10);
                    g2d.setColor(new Color(0, 100, 0));
                    g2d.fillOval(x + cellSize/2 - 8, y + cellSize/2 - 10, 16, 12);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 8));
                    String shortType = tree.treeType.equals("Tree1") ? "🌲" : "🌳";
                    g2d.drawString(shortType, x + cellSize - 15, y + 12);
                }

                // Вода
                for (Water water : waters) {
                    int x = water.gridX * cellSize;
                    int y = water.gridY * cellSize;
                    g2d.setColor(new Color(30, 100, 180));
                    g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                    g2d.setColor(new Color(50, 150, 220));
                    g2d.drawRect(x, y, cellSize - 1, cellSize - 1);
                    // "Волны"
                    g2d.setColor(new Color(100, 180, 255, 150));
                    for (int i = 0; i < 3; i++) {
                        g2d.drawArc(x + 5 + i * 10, y + cellSize - 12, 8, 6, 0, 180);
                    }
                }



                // NPC
                // NPC
                for (QuestNPC npc : npcs) {
                    int x = npc.gridX * cellSize;
                    int y = npc.gridY * cellSize;

                    // Разные цвета для разных NPC
                    if ("Sav m/43".equals(npc.name)) {
                        g2d.setColor(new Color(0, 150, 100, 200));  // Зелёно-голубой для шведа
                    } else {
                        g2d.setColor(new Color(255, 215, 0, 200));  // Золотой для T18
                    }
                    g2d.fillRoundRect(x, y, cellSize - 1, cellSize - 1, 8, 8);
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.BOLD, 16));
                    g2d.drawString("!", x + cellSize/2 - 5, y + cellSize/2 + 5);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                    String shortName = npc.name.length() > 6 ? npc.name.substring(0, 6) : npc.name;
                    g2d.drawString(shortName, x + 5, y + 12);
                    g2d.drawString("Q:" + npc.questTarget, x + cellSize - 25, y + 20);
                }

                for (Trader trader : traders) {
                    int x = trader.gridX * cellSize;
                    int y = trader.gridY * cellSize;
                    // Зеленовато-золотой цвет для торговца
                    g2d.setColor(new Color(100, 150, 50, 200));
                    g2d.fillRoundRect(x, y, cellSize - 1, cellSize - 1, 8, 8);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 16));
                    g2d.drawString("💰", x + cellSize/2 - 5, y + cellSize/2 + 5);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                    String shortName = trader.name.length() > 6 ? trader.name.substring(0, 6) : trader.name;
                    g2d.drawString(shortName, x + 5, y + 12);
                    // У торговцев нет квеста, поэтому не рисуем "Q:"
                }

                for (FriendlyUnit friendly : friendlies) {
                    int x = friendly.gridX * cellSize;
                    int y = friendly.gridY * cellSize;
                    g2d.setColor(new Color(0, 100, 200, 200));
                    g2d.fillRoundRect(x, y, cellSize - 1, cellSize - 1, 8, 8);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 14));
                    g2d.drawString("F", x + cellSize/2 - 5, y + cellSize/2 + 5);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 8));
                    String shortName = friendly.name.length() > 6 ? friendly.name.substring(0, 6) : friendly.name;
                    g2d.drawString(shortName, x + 5, y + 12);
                }

                // Тумбочки
                // Тумбочки
                for (StorageChest chest : storageChests) {
                    int x = chest.gridX * cellSize;
                    int y = chest.gridY * cellSize;

                    // Коричневый цвет тумбочки (размер 1x1)
                    g2d.setColor(new Color(139, 69, 19));
                    g2d.fillRect(x, y, cellSize - 1, cellSize - 1);
                    g2d.setColor(new Color(101, 67, 33));
                    g2d.drawRect(x, y, cellSize - 1, cellSize - 1);

                    // Рисуем ручку
                    g2d.setColor(new Color(255, 215, 0));
                    g2d.fillOval(x + cellSize / 2 - 5, y + cellSize / 2 - 3, 10, 6);

                    // Если есть предметы - рисуем индикатор
                    if (!chest.isEmpty()) {
                        g2d.setColor(new Color(255, 200, 0, 200));
                        g2d.fillOval(x + cellSize - 15, y + 5, 12, 12);
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("Arial", Font.BOLD, 10));
                        g2d.drawString("📦", x + cellSize - 14, y + 14);
                    }
                }

                // После отрисовки тумбочек, добавьте:
                // После отрисовки тумбочек, добавьте:
                for (GarbageContainer container : garbageContainers) {
                    container.draw(g2d, 0, 0, cellSize, null); // cameraX=0, cameraY=0 в редакторе
                }

                for (Door door : doors) {
                    door.draw(g2d, 0, 0, cellSize, null);
                }

                // Внутри paintComponent грида, после отрисовки тумбочек:
                for (Enemy enemy : enemies) {
                    int x = enemy.gridX * cellSize;
                    int y = enemy.gridY * cellSize;

                    // Разные цвета для разных типов врагов
                    if ("R_Otsu".equals(enemy.type)) {
                        g2d.setColor(new Color(150, 0, 100, 200));  // Фиолетовый для R_Otsu
                    } else {
                        g2d.setColor(new Color(200, 0, 0, 200));    // Красный для обычных
                    }
                    g2d.fillRoundRect(x, y, cellSize - 1, cellSize - 1, 8, 8);

                    // Направление
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 14));
                    String arrow = "↓";
                    switch(enemy.direction) {
                        case UP: arrow = "↑"; break;
                        case DOWN: arrow = "↓"; break;
                        case LEFT: arrow = "←"; break;
                        case RIGHT: arrow = "→"; break;
                    }
                    g2d.drawString(arrow, x + cellSize/2 - 5, y + cellSize/2 + 5);

                    // Здоровье и тип
                    g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                    String shortType = "R_Otsu".equals(enemy.type) ? "R" : "L";
                    g2d.drawString(shortType + " HP:" + enemy.health, x + 5, y + 12);
                }

                // В paintComponent, после отрисовки стартовой позиции, добавьте отрисовку победной зоны:
                if (winZone != null) {
                    int x = winZone.x * cellSize;
                    int y = winZone.y * cellSize;
                    int width = 5 * cellSize;
                    int height = 5 * cellSize;

                    g.setColor(new Color(0, 200, 0, 80));
                    g.fillRect(x, y, width, height);
                    g.setColor(new Color(0, 255, 0, 200));
                    g2d.setStroke(new BasicStroke(2));
                    g.drawRect(x, y, width, height);

                    // Рисуем флаг в центре
                    int centerX = x + width / 2;
                    int centerY = y + height / 2;
                    g.setColor(new Color(255, 215, 0, 200));
                    g.fillRect(centerX - 15, centerY - 25, 5, 35);
                    g.setColor(new Color(255, 0, 0, 200));
                    g.fillRect(centerX - 10, centerY - 25, 20, 15);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    g.drawString("★", centerX, centerY - 18);
                }

                // Выделение выбранной клетки
                if (selectedCell != null) {
                    g2d.setColor(new Color(255, 255, 0, 100));
                    g2d.fillRect(selectedCell.x * cellSize, selectedCell.y * cellSize, cellSize, cellSize);

                    if (currentMode == EditMode.NPC) {
                        g2d.setColor(new Color(255, 215, 0, 200));
                        g2d.setStroke(new BasicStroke(3));
                        g2d.drawRect(selectedCell.x * cellSize, selectedCell.y * cellSize, cellSize, cellSize);
                    } else if (currentMode == EditMode.TREE) {
                        g2d.setColor(new Color(34, 139, 34, 200));
                        g2d.setStroke(new BasicStroke(3));
                        g2d.drawRect(selectedCell.x * cellSize, selectedCell.y * cellSize, cellSize, cellSize);
                    } else if (currentMode == EditMode.DECOR) {
                        // Рамка для декоративных элементов
                        g2d.setColor(new Color(100, 100, 100, 200));
                        g2d.setStroke(new BasicStroke(3));
                        g2d.drawRect(selectedCell.x * cellSize, selectedCell.y * cellSize, cellSize, cellSize);
                    }
                }

                // Отрисовка области для массовой заливки
                if (isFillMode && fillStartCell != null && selectedCell != null) {
                    g2d.setColor(new Color(0, 255, 0, 100));
                    int minX = Math.min(fillStartCell.x, selectedCell.x);
                    int maxX = Math.max(fillStartCell.x, selectedCell.x);
                    int minY = Math.min(fillStartCell.y, selectedCell.y);
                    int maxY = Math.max(fillStartCell.y, selectedCell.y);

                    int x1 = minX * cellSize;
                    int y1 = minY * cellSize;
                    int width = (maxX - minX + 1) * cellSize;
                    int height = (maxY - minY + 1) * cellSize;

                    g2d.fillRect(x1, y1, width, height);
                    g2d.setColor(new Color(0, 255, 0, 200));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRect(x1, y1, width, height);

                    String sizeText = "Область: " + (maxX - minX + 1) + "x" + (maxY - minY + 1);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    g2d.drawString(sizeText, x1 + 5, y1 + 20);
                }
            }
        };

        gridPanel.setPreferredSize(new Dimension(gridWidth * cellSize, gridHeight * cellSize));

        gridPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isFillMode) {
                    if (fillStartCell == null) {
                        fillStartCell = new Point(e.getX() / cellSize, e.getY() / cellSize);
                        selectedCell = fillStartCell;
                        gridPanel.repaint();
                        System.out.println("Выбрана начальная точка заливки: [" + fillStartCell.x + "," + fillStartCell.y + "]");
                        JOptionPane.showMessageDialog(LevelEditor.this,
                                "Выбрана начальная точка!\nТеперь кликните по конечной точке области.",
                                "Массовая заливка - шаг 1/2",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        Point endCell = new Point(e.getX() / cellSize, e.getY() / cellSize);
                        selectedCell = endCell;
                        gridPanel.repaint();
                        showFillDialog(fillStartCell, endCell);
                        fillStartCell = null;
                        isFillMode = false;
                        selectedCell = null;
                        gridPanel.repaint();
                    }
                } else {
                    selectedCell = new Point(e.getX() / cellSize, e.getY() / cellSize);
                    gridPanel.repaint();

                    if (currentMode == EditMode.WALL) {
                        showWallDialog();
                    } else if (currentMode == EditMode.NPC) {
                        showNPCDialog();
                    } else if (currentMode == EditMode.TREE) {
                        showTreeDialog();
                    } else if (currentMode == EditMode.DECOR) {
                        switch(currentDecorType) {
                            case PAVEMENT:
                                showPavementDialog();
                                break;
                            case OAKPLANKS:
                                showOakPlanksDialog();
                                break;
                            case INFERNALLAND:
                                showInfernalLandDialog();
                                break;
                            case IRONFLOOR:  // ← ДОБАВЬТЕ ЭТОТ БЛОК
                                showIronFloorDialog();
                                break;
                        }
                    } else if (currentMode == EditMode.FRIENDLY) {
                        showFriendlyDialog();
                    } else if (currentMode == EditMode.WATER) {
                        showWaterDialog();
                    } else if (currentMode == EditMode.DOOR) {
                        showDoorDialog();
                    } else if (currentMode == EditMode.STORAGE) {
                        showStorageDialog();
                    } else if (currentMode == EditMode.ENEMY) {
                        showEnemyDialog();
                    } else if (currentMode == EditMode.START_POSITION) {
                        showStartPositionDialog();
                    } else if (currentMode == EditMode.WINZONE) {
                        showWinZoneDialog();
                    } else if (currentMode == EditMode.GARBAGE) {
                        showGarbageDialog();
                    } else if (currentMode == EditMode.TRADER) {
                        showTraderDialog();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Панель управления
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS)); // Вертикальное расположение

// Первая строка кнопок
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
// Вторая строка кнопок
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
// Третья строка кнопок
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Кнопки режимов
        JButton wallModeButton = new JButton("Режим: Стены");
        JButton npcModeButton = new JButton("Режим: NPC");
        JButton treeModeButton = new JButton("Режим: Деревья");
        JButton decorModeButton = new JButton("Режим: Декор");
        JButton waterModeButton = new JButton("Режим: Вода");
        JButton doorModeButton = new JButton("Режим: Двери");
        JButton friendlyModeButton = new JButton("Режим: Союзники");
        JButton storageModeButton = new JButton("Режим: Тумбочки");
        JButton enemyModeButton = new JButton("Режим: Враги");
        JButton garbageModeButton = new JButton("Режим: Мусорный контейнер");
        JButton traderModeButton = new JButton("Режим: Торговцы");


        wallModeButton.setBackground(currentMode == EditMode.WALL ? new Color(100, 200, 100) : null);
        wallModeButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.WALL;
            wallModeButton.setBackground(new Color(100, 200, 100));
            npcModeButton.setBackground(null);
            treeModeButton.setBackground(null);
            decorModeButton.setBackground(null);
            doorModeButton.setBackground(null);
            setTitle("Редактор уровней - TankFight [РЕЖИМ: СТЕНЫ]");
        });
        controlPanel.add(wallModeButton);

        npcModeButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.NPC;
            npcModeButton.setBackground(new Color(100, 200, 100));
            wallModeButton.setBackground(null);
            treeModeButton.setBackground(null);
            decorModeButton.setBackground(null);
            doorModeButton.setBackground(null);
            setTitle("Редактор уровней - TankFight [РЕЖИМ: NPC]");
        });
        controlPanel.add(npcModeButton);

        traderModeButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.TRADER;
            traderModeButton.setBackground(new Color(100, 200, 100));
            // сбросить фон других кнопок...
            setTitle("Редактор уровней - TankFight [РЕЖИМ: ТОРГОВЦЫ]");
        });

        treeModeButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.TREE;
            treeModeButton.setBackground(new Color(100, 200, 100));
            wallModeButton.setBackground(null);
            npcModeButton.setBackground(null);
            decorModeButton.setBackground(null);
            doorModeButton.setBackground(null);
            setTitle("Редактор уровней - TankFight [РЕЖИМ: ДЕРЕВЬЯ]");
        });
        controlPanel.add(treeModeButton);

        decorModeButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            // Создаём выпадающее меню
            JPopupMenu decorMenu = new JPopupMenu();

            JMenuItem pavementItem = new JMenuItem("🛣️ Асфальт");
            pavementItem.addActionListener(ev -> {
                currentMode = EditMode.DECOR;
                currentDecorType = DecorType.PAVEMENT;
                decorModeButton.setText("Режим: Асфальт");  // ← меняем текст на кнопке
                setTitle("Редактор уровней - TankFight [РЕЖИМ: АСФАЛЬТ]");
            });

            JMenuItem oakPlanksItem = new JMenuItem("Дубовые доски");
            oakPlanksItem.addActionListener(ev -> {
                currentMode = EditMode.DECOR;
                currentDecorType = DecorType.OAKPLANKS;
                decorModeButton.setText("Режим: Дубовые доски");  // ← меняем текст на кнопке
                setTitle("Редактор уровней - TankFight [РЕЖИМ: ДУБОВЫЕ ДОСКИ]");
            });

            JMenuItem infernalLandItem = new JMenuItem("Адская земля");
            infernalLandItem.addActionListener(ev -> {
                currentMode = EditMode.DECOR;
                currentDecorType = DecorType.INFERNALLAND;
                decorModeButton.setText("🎨 Режим: Адская земля");  // ← меняем текст на кнопке
                setTitle("Редактор уровней - TankFight [РЕЖИМ: АДСКАЯ ЗЕМЛЯ]");
            });

            JMenuItem ironFloorItem = new JMenuItem("🔩 Железный пол");
            ironFloorItem.addActionListener(ev -> {
                currentMode = EditMode.DECOR;
                currentDecorType = DecorType.IRONFLOOR;
                decorModeButton.setText("🎨 Режим: Железный пол");
                setTitle("Редактор уровней - TankFight [РЕЖИМ: ЖЕЛЕЗНЫЙ ПОЛ]");
            });

            decorMenu.add(pavementItem);
            decorMenu.add(oakPlanksItem);
            decorMenu.add(infernalLandItem);
            decorMenu.add(ironFloorItem);

            decorMenu.show(decorModeButton, 0, decorModeButton.getHeight());
        });
        controlPanel.add(decorModeButton);

        waterModeButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.WATER;

            treeModeButton.setBackground(null);
            wallModeButton.setBackground(null);
            npcModeButton.setBackground(null);
            decorModeButton.setBackground(null);
            waterModeButton.setBackground(new Color(100, 200, 100));
            doorModeButton.setBackground(null);

            setTitle("Редактор уровней - TankFight [РЕЖИМ: ВОДА]");
        });
        controlPanel.add(waterModeButton);

        doorModeButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.DOOR;  // ← ЭТА СТРОКА БЫЛА ПРОПУЩЕНА!
            doorModeButton.setBackground(new Color(100, 200, 100));
            // Сбрасываем фон всех остальных кнопок
            wallModeButton.setBackground(null);
            npcModeButton.setBackground(null);
            treeModeButton.setBackground(null);
            decorModeButton.setBackground(null);
            waterModeButton.setBackground(null);
            friendlyModeButton.setBackground(null);
            storageModeButton.setBackground(null);
            enemyModeButton.setBackground(null);
            traderModeButton.setBackground(null);
            garbageModeButton.setBackground(null);
            setTitle("Редактор уровней - TankFight [РЕЖИМ: ДВЕРИ]");
        });
        controlPanel.add(doorModeButton);

        friendlyModeButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.FRIENDLY;
            friendlyModeButton.setBackground(new Color(100, 200, 100));
            wallModeButton.setBackground(null);
            npcModeButton.setBackground(null);
            treeModeButton.setBackground(null);
            decorModeButton.setBackground(null);
            setTitle("Редактор уровней - TankFight [РЕЖИМ: СОЮЗНИКИ]");
        });
        controlPanel.add(friendlyModeButton);

        storageModeButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(LevelEditor.this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.STORAGE;
            wallModeButton.setBackground(null);
            npcModeButton.setBackground(null);
            treeModeButton.setBackground(null);
            decorModeButton.setBackground(null);
            friendlyModeButton.setBackground(null);
            storageModeButton.setBackground(new Color(100, 200, 100));
            setTitle("Редактор уровней - TankFight [РЕЖИМ: ТУМБОЧКИ]");
        });
        controlPanel.add(storageModeButton);

        garbageModeButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.GARBAGE;
            garbageModeButton.setBackground(new Color(100, 200, 100));
            // сбросить фон других кнопок...
            setTitle("Редактор уровней - TankFight [РЕЖИМ: МУСОРНЫЙ КОНТЕЙНЕР]");
        });

        enemyModeButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.ENEMY;
            enemyModeButton.setBackground(new Color(100, 200, 100));
            wallModeButton.setBackground(null);
            npcModeButton.setBackground(null);
            treeModeButton.setBackground(null);
            decorModeButton.setBackground(null);
            friendlyModeButton.setBackground(null);
            storageModeButton.setBackground(null);
            setTitle("Редактор уровней - TankFight [РЕЖИМ: ВРАГИ]");
        });
        controlPanel.add(enemyModeButton);

        // После кнопки enemyModeButton добавьте:
        JButton startPosButton = new JButton("🚩 Режим: Стартовая позиция");
        startPosButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.START_POSITION;
            startPosButton.setBackground(new Color(100, 200, 100));
            wallModeButton.setBackground(null);
            npcModeButton.setBackground(null);
            treeModeButton.setBackground(null);
            decorModeButton.setBackground(null);
            friendlyModeButton.setBackground(null);
            storageModeButton.setBackground(null);
            enemyModeButton.setBackground(null);
            setTitle("Редактор уровней - TankFight [РЕЖИМ: СТАРТОВАЯ ПОЗИЦИЯ]");
        });

        JButton winZoneButton = new JButton("🏆 Режим: Победная зона");
        winZoneButton.addActionListener(e -> {
            if (isFillMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Выйти из режима массовой заливки?",
                        "Выход из режима",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    isFillMode = false;
                    fillStartCell = null;
                    selectedCell = null;
                    gridPanel.repaint();
                } else {
                    return;
                }
            }
            currentMode = EditMode.WINZONE;
            winZoneButton.setBackground(new Color(100, 200, 100));
            startPosButton.setBackground(null);
            wallModeButton.setBackground(null);
            npcModeButton.setBackground(null);
            treeModeButton.setBackground(null);
            decorModeButton.setBackground(null);
            friendlyModeButton.setBackground(null);
            storageModeButton.setBackground(null);
            enemyModeButton.setBackground(null);
            setTitle("Редактор уровней - TankFight [РЕЖИМ: ПОБЕДНАЯ ЗОНА]");
        });

        // КНОПКА МАССОВОЙ ЗАЛИВКИ
        JButton fillButton = new JButton("🟩 Массовая заливка (Fill)");
        fillButton.setBackground(new Color(0, 150, 0));
        fillButton.setForeground(Color.WHITE);
        fillButton.addActionListener(e -> {
            if (isFillMode) {
                isFillMode = false;
                fillStartCell = null;
                selectedCell = null;
                fillButton.setBackground(new Color(0, 150, 0));
                gridPanel.repaint();
                System.out.println("Режим массовой заливки отключён");
            } else {
                isFillMode = true;
                fillStartCell = null;
                fillButton.setBackground(new Color(255, 100, 0));
                System.out.println("=== РЕЖИМ МАССОВОЙ ЗАЛИВКИ ===");
                System.out.println("Кликните по начальной точке, затем по конечной");
                System.out.println("Область будет заполнена текущим типом объектов");
                JOptionPane.showMessageDialog(this,
                        "РЕЖИМ МАССОВОЙ ЗАЛИВКИ АКТИВИРОВАН!\n\n" +
                                "1. Кликните по начальной точке области\n" +
                                "2. Кликните по конечной точке области\n" +
                                "3. Выберите параметры заполнения\n\n" +
                                "Текущий режим: " + getCurrentModeName(),
                        "Массовая заливка",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        controlPanel.add(fillButton);

        // Кнопка очистки области
        JButton clearAreaButton = new JButton("🗑 Очистить область");
        clearAreaButton.addActionListener(e -> {
            if (isFillMode) {
                isFillMode = false;
                fillStartCell = null;
                selectedCell = null;
                fillButton.setBackground(new Color(0, 150, 0));
            }
            showClearAreaDialog();
        });
        controlPanel.add(clearAreaButton);

        JButton openButton = new JButton("📂 Открыть уровень");
        openButton.addActionListener(e -> openLevel());
        controlPanel.add(openButton);

        JButton saveButton = new JButton("💾 Сохранить");
        saveButton.addActionListener(e -> saveLevel());
        controlPanel.add(saveButton);

        JButton saveAsButton = new JButton("📁 Сохранить как...");
        saveAsButton.addActionListener(e -> saveLevelAs());
        controlPanel.add(saveAsButton);

        JButton clearWallsButton = new JButton("🧹 Очистить стены");
        clearWallsButton.addActionListener(e -> {
            walls.clear();
            gridPanel.repaint();
        });
        controlPanel.add(clearWallsButton);

        JButton clearNPCsButton = new JButton("🧹 Очистить NPC");
        clearNPCsButton.addActionListener(e -> {
            npcs.clear();
            gridPanel.repaint();
        });
        controlPanel.add(clearNPCsButton);

        JButton clearTreesButton = new JButton("🧹 Очистить деревья");
        clearTreesButton.addActionListener(e -> {
            trees.clear();
            gridPanel.repaint();
        });
        controlPanel.add(clearTreesButton);

        // ← НОВАЯ КНОПКА ОЧИСТКИ АСФАЛЬТА
        JButton clearPavementButton = new JButton("🧹 Очистить асфальт");
        clearPavementButton.addActionListener(e -> {
            pavements.clear();
            gridPanel.repaint();
        });
        controlPanel.add(clearPavementButton);

        JButton clearWaterButton = new JButton("🧹 Очистить воду");
        clearWaterButton.addActionListener(e -> {
            waters.clear();
            gridPanel.repaint();
        });
        row3.add(clearWaterButton);

        // ===== СТРОКА 1 =====

        row1.add(wallModeButton);
        row1.add(npcModeButton);
        row1.add(treeModeButton);
        row1.add(decorModeButton);
        row1.add(waterModeButton);
        row1.add(doorModeButton);
        row1.add(friendlyModeButton);
        row1.add(storageModeButton);
        row1.add(startPosButton);
        row1.add(winZoneButton);

// ===== СТРОКА 2 =====
        row2.add(fillButton);
        row2.add(clearAreaButton);
        row2.add(openButton);
        row2.add(saveButton);
        row2.add(saveAsButton);
        row2.add(traderModeButton);  // ← ДОБАВЬТЕ ЭТУ СТРОКУ
        row2.add(garbageModeButton);

// ===== СТРОКА 3 =====
        row3.add(clearWallsButton);
        row3.add(clearNPCsButton);
        row3.add(clearTreesButton);
        row3.add(clearPavementButton);
        row3.add(clearWaterButton);
// Если есть другие кнопки очистки, добавьте их сюда

// Добавляем строки на основную панель
        controlPanel.add(row1);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5))); // небольшой отступ
        controlPanel.add(row2);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        controlPanel.add(row3);

        add(controlPanel, BorderLayout.SOUTH);
        setLocationRelativeTo(null);

    }

    private void openLevel() {
        //JFileChooser chooser = new JFileChooser("C:\\Users\\Костя\\IdeaProjects\\TankFight 1.17\\levels\\");
        JFileChooser chooser = new JFileChooser("src/levels/");
        chooser.setFileFilter(new FileNameExtensionFilter("Level files (*.txt)", "txt"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            currentLevelPath = file.getAbsolutePath();

            walls = LevelLoader.loadLevel(currentLevelPath, gridWidth, gridHeight);
            npcs = LevelLoader.loadNPCs(currentLevelPath, gridWidth, gridHeight);
            trees = LevelLoader.loadTrees(currentLevelPath, gridWidth, gridHeight);
            pavements = LevelLoader.loadPavements(currentLevelPath, gridWidth, gridHeight);
            oakPlanks = LevelLoader.loadOakPlanks(currentLevelPath, gridWidth, gridHeight);
            infernalLands = LevelLoader.loadInfernalLands(currentLevelPath, gridWidth, gridHeight);
            ironFloors = LevelLoader.loadIronFloors(currentLevelPath, gridWidth, gridHeight);
            waters = LevelLoader.loadWaters(currentLevelPath, gridWidth, gridHeight);
            doors = LevelLoader.loadDoors(currentLevelPath, gridWidth, gridHeight);
            friendlies = LevelLoader.loadFriendlyUnits(currentLevelPath, gridWidth, gridHeight);
            storageChests = LevelLoader.loadStorageChests(currentLevelPath, gridWidth, gridHeight);
            garbageContainers = LevelLoader.loadGarbageContainers(currentLevelPath, gridWidth, gridHeight);
            enemies = LevelLoader.loadEnemies(currentLevelPath, gridWidth, gridHeight, null);

            // Загружаем стартовую позицию
            Point loadedStart = LevelLoader.loadStartPosition(currentLevelPath, gridWidth, gridHeight);
            if (loadedStart != null) {
                startPosition = loadedStart;
            }

// ===== ДОБАВЬТЕ ЗАГРУЗКУ ПОБЕДНОЙ ЗОНЫ =====
            Point loadedWinZone = LevelLoader.loadWinZone(currentLevelPath, gridWidth, gridHeight);
            if (loadedWinZone != null) {
                winZone = loadedWinZone;
            }
// ========================================

            gridPanel.repaint();

            setTitle("Редактор уровней - TankFight [" + file.getName() + "]");
            System.out.println("Загружено стен: " + walls.size() + ", NPC: " + npcs.size() +
                    ", деревьев: " + trees.size() + ", асфальта: " + pavements.size() +
                    ", врагов: " + enemies.size());
            JOptionPane.showMessageDialog(this, "Уровень загружен!\nСтен: " + walls.size() +
                    "\nNPC: " + npcs.size() + "\nДеревьев: " + trees.size() +
                    "\nАсфальта: " + pavements.size() + "\nВрагов: " + enemies.size());
        }
    }

    private void showTreeDialog() {
        if (selectedCell == null) return;

        Tree existingTree = null;
        for (Tree tree : trees) {
            if (tree.gridX == selectedCell.x && tree.gridY == selectedCell.y) {
                existingTree = tree;
                break;
            }
        }

        final Tree finalExistingTree = existingTree;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование дерева", true);
        dialog.setLayout(new GridLayout(4, 2, 5, 5));
        dialog.setSize(300, 180);

        JComboBox<String> treeTypeBox = new JComboBox<>(new String[]{"Tree1", "Tree2"});
        if (finalExistingTree != null) {
            treeTypeBox.setSelectedItem(finalExistingTree.treeType);
        }

        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel("Тип дерева:"));
        dialog.add(treeTypeBox);

        JButton addButton = new JButton(finalExistingTree != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            String treeType = (String) treeTypeBox.getSelectedItem();

            if (finalExistingTree != null) {
                trees.remove(finalExistingTree);
            }
            trees.add(new Tree(finalSelectedCell.x, finalSelectedCell.y, treeType));
            dialog.dispose();
            gridPanel.repaint();
            System.out.println("Добавлено дерево: " + treeType + " в [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
        });

        removeButton.addActionListener(e -> {
            if (finalExistingTree != null) {
                trees.remove(finalExistingTree);
                System.out.println("Удалено дерево из [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showPavementDialog() {
        if (selectedCell == null) return;

        Pavement existingPavement = null;
        for (Pavement p : pavements) {
            if (p.gridX == selectedCell.x && p.gridY == selectedCell.y) {
                existingPavement = p;
                break;
            }
        }

        final Pavement finalExistingPavement = existingPavement;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование асфальта", true);
        dialog.setLayout(new GridLayout(3, 2, 5, 5));
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel("Тип:"));
        dialog.add(new JLabel("Асфальт (декоративный)"));

        JButton addButton = new JButton(finalExistingPavement != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            if (finalExistingPavement != null) {
                pavements.remove(finalExistingPavement);
            }
            pavements.add(new Pavement(finalSelectedCell.x, finalSelectedCell.y));
            dialog.dispose();
            gridPanel.repaint();
            System.out.println("Добавлен асфальт в [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
        });

        removeButton.addActionListener(e -> {
            if (finalExistingPavement != null) {
                pavements.remove(finalExistingPavement);
                System.out.println("Удалён асфальт из [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void showOakPlanksDialog() {
        if (selectedCell == null) return;

        OakPlanks existingOakPlanks = null;
        for (OakPlanks op : oakPlanks) {
            if (op.gridX == selectedCell.x && op.gridY == selectedCell.y) {
                existingOakPlanks = op;
                break;
            }
        }

        final OakPlanks finalExisting = existingOakPlanks;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование дубовых досок", true);
        dialog.setLayout(new GridLayout(3, 2, 5, 5));
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel("Тип:"));
        dialog.add(new JLabel("Дубовые доски (декоративные)"));

        JButton addButton = new JButton(finalExisting != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            if (finalExisting != null) {
                oakPlanks.remove(finalExisting);
            }
            oakPlanks.add(new OakPlanks(finalSelectedCell.x, finalSelectedCell.y));
            dialog.dispose();
            gridPanel.repaint();
            System.out.println("Добавлены дубовые доски в [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
        });

        removeButton.addActionListener(e -> {
            if (finalExisting != null) {
                oakPlanks.remove(finalExisting);
                System.out.println("Удалены дубовые доски из [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void showIronFloorDialog() {
        if (selectedCell == null) return;

        IronFloor existingFloor = null;
        for (IronFloor floor : ironFloors) {
            if (floor.gridX == selectedCell.x && floor.gridY == selectedCell.y) {
                existingFloor = floor;
                break;
            }
        }

        final IronFloor finalExisting = existingFloor;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование железного пола", true);
        dialog.setLayout(new GridLayout(3, 2, 5, 5));
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel("Тип:"));
        dialog.add(new JLabel("Железный пол (декоративный)"));

        JButton addButton = new JButton(finalExisting != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            if (finalExisting != null) {
                ironFloors.remove(finalExisting);
            }
            ironFloors.add(new IronFloor(finalSelectedCell.x, finalSelectedCell.y));
            dialog.dispose();
            gridPanel.repaint();
            System.out.println("Добавлен железный пол в [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
        });

        removeButton.addActionListener(e -> {
            if (finalExisting != null) {
                ironFloors.remove(finalExisting);
                System.out.println("Удалён железный пол из [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void showWaterDialog() {
        if (selectedCell == null) return;

        Water existingWater = null;
        for (Water w : waters) {
            if (w.gridX == selectedCell.x && w.gridY == selectedCell.y) {
                existingWater = w;
                break;
            }
        }

        final Water finalExisting = existingWater;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование воды", true);
        dialog.setLayout(new GridLayout(3, 2, 5, 5));
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel("Тип:"));
        dialog.add(new JLabel("Вода (непроходимое препятствие)"));

        JButton addButton = new JButton(finalExisting != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            if (finalExisting != null) {
                waters.remove(finalExisting);
            }
            waters.add(new Water(finalSelectedCell.x, finalSelectedCell.y));
            dialog.dispose();
            gridPanel.repaint();
            System.out.println("Добавлена вода в [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
        });

        removeButton.addActionListener(e -> {
            if (finalExisting != null) {
                waters.remove(finalExisting);
                System.out.println("Удалена вода из [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void showDoorDialog() {
        if (selectedCell == null) return;

        Door existingDoor = null;
        for (Door door : doors) {
            if (door.gridX == selectedCell.x && door.gridY == selectedCell.y) {
                existingDoor = door;
                break;
            }
        }

        final Door finalExistingDoor = existingDoor;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование двери", true);
        dialog.setLayout(new GridLayout(4, 2, 5, 5));
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);

        // Выбор цвета двери
        JComboBox<Door.DoorColor> colorBox = new JComboBox<>(Door.DoorColor.values());
        if (finalExistingDoor != null) {
            colorBox.setSelectedItem(finalExistingDoor.color);
        }

        JCheckBox openCheckBox = new JCheckBox("Открыта",
                finalExistingDoor != null && finalExistingDoor.isOpen);

        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel("Цвет двери:"));
        dialog.add(colorBox);
        dialog.add(new JLabel("Состояние:"));
        dialog.add(openCheckBox);

        JButton addButton = new JButton(finalExistingDoor != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            Door.DoorColor color = (Door.DoorColor) colorBox.getSelectedItem();
            boolean isOpen = openCheckBox.isSelected();

            if (finalExistingDoor != null) {
                doors.remove(finalExistingDoor);
            }

            Door door = new Door(finalSelectedCell.x, finalSelectedCell.y, color);
            if (isOpen) {
                door.open();
            }
            doors.add(door);

            dialog.dispose();
            gridPanel.repaint();
            System.out.println("Добавлена дверь: " + color + " в [" +
                    finalSelectedCell.x + "," + finalSelectedCell.y + "], открыта: " + isOpen);
        });

        removeButton.addActionListener(e -> {
            if (finalExistingDoor != null) {
                doors.remove(finalExistingDoor);
                System.out.println("Удалена дверь из [" +
                        finalSelectedCell.x + "," + finalSelectedCell.y + "]");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void showFriendlyDialog() {
        if (selectedCell == null) return;

        FriendlyUnit existingFriendly = null;
        for (FriendlyUnit f : friendlies) {
            if (f.gridX == selectedCell.x && f.gridY == selectedCell.y) {
                existingFriendly = f;
                break;
            }
        }

        final FriendlyUnit finalExisting = existingFriendly;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование союзника", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Базовая информация
        JPanel basicPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        basicPanel.setBorder(BorderFactory.createTitledBorder("Основная информация"));

        JTextField nameField = new JTextField(finalExisting != null ? finalExisting.name : "MS-1");
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"MS-1", "M53", "VK10001P", "AMX40", "T1"});

        // ===== ИСПРАВЛЕННЫЙ БЛОК: правильно устанавливаем тип существующего союзника =====
        if (finalExisting != null) {
            if ("M53".equals(finalExisting.type)) {
                typeBox.setSelectedItem("M53");
            } else if ("VK10001P".equals(finalExisting.type)) {
                typeBox.setSelectedItem("VK10001P");
            } else if ("AMX40".equals(finalExisting.type)) {  // ← ДОБАВИТЬ ЭТУ СТРОКУ
                typeBox.setSelectedItem("AMX40");              // ← И ЭТУ
            } else if ("T1".equals(finalExisting.type)) {  // ← ДОБАВИТЬ ЭТУ СТРОКУ
                typeBox.setSelectedItem("T1");              // ← И ЭТУ
            }else {
                typeBox.setSelectedItem("MS-1");
            }
        }

        basicPanel.add(new JLabel("Имя союзника:"));
        basicPanel.add(nameField);
        basicPanel.add(new JLabel("Тип союзника:"));
        basicPanel.add(typeBox);

        mainPanel.add(basicPanel);

        // Панель модернизации (только для отображения/сохранения, не влияет на характеристики)
        JPanel upgradePanel = new JPanel(new GridLayout(3, 2, 5, 5));
        upgradePanel.setBorder(BorderFactory.createTitledBorder("Модернизация (только для информации)"));

        JComboBox<Integer> upgradeLevelBox = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        JComboBox<String> upgradeClassBox = new JComboBox<>(new String[]{"Нет", "ПТ", "ТТ", "СТ", "ЛТ"});

        if (finalExisting != null) {
            upgradeLevelBox.setSelectedItem(finalExisting.getUpgradeLevel());
            String cls = finalExisting.getUpgradeClass();
            if (cls != null) {
                switch(cls) {
                    case "PT": upgradeClassBox.setSelectedItem("ПТ"); break;
                    case "TT": upgradeClassBox.setSelectedItem("ТТ"); break;
                    case "ST": upgradeClassBox.setSelectedItem("СТ"); break;
                    case "LT": upgradeClassBox.setSelectedItem("ЛТ"); break;
                    default: upgradeClassBox.setSelectedItem("Нет");
                }
            }
        }

        upgradePanel.add(new JLabel("Уровень модернизации:"));
        upgradePanel.add(upgradeLevelBox);
        upgradePanel.add(new JLabel("Класс модернизации:"));
        upgradePanel.add(upgradeClassBox);

        mainPanel.add(upgradePanel);

        // Панель опыта (только для отображения/сохранения, не влияет на характеристики)
        JPanel expPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        expPanel.setBorder(BorderFactory.createTitledBorder("Опыт (только для информации)"));

        JSpinner levelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        JSpinner expSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 10));

        if (finalExisting != null && finalExisting.getExperienceSystem() != null) {
            levelSpinner.setValue(finalExisting.getExperienceSystem().getLevel());
            expSpinner.setValue(finalExisting.getExperienceSystem().getExperience());
        }

        expPanel.add(new JLabel("Уровень:"));
        expPanel.add(levelSpinner);
        expPanel.add(new JLabel("Опыт в текущем уровне:"));
        expPanel.add(expSpinner);

        mainPanel.add(expPanel);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addButton = new JButton(finalExisting != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Введите имя союзника!");
                return;
            }

            String type = (String) typeBox.getSelectedItem();
            int upgradeLvl = (Integer) upgradeLevelBox.getSelectedItem();
            String upgradeClassRaw = (String) upgradeClassBox.getSelectedItem();
            String upgradeClass = null;

            if (upgradeLvl >= 2 && !"Нет".equals(upgradeClassRaw)) {
                switch(upgradeClassRaw) {
                    case "ПТ": upgradeClass = "PT"; break;
                    case "ТТ": upgradeClass = "TT"; break;
                    case "СТ": upgradeClass = "ST"; break;
                    case "ЛТ": upgradeClass = "LT"; break;
                }
            }

            int level = (Integer) levelSpinner.getValue();
            int exp = (Integer) expSpinner.getValue();

            if (finalExisting != null) {
                friendlies.remove(finalExisting);
            }

            // Создаём союзника с БАЗОВЫМИ характеристиками (без применения бонусов!)
            FriendlyUnit friendly = new FriendlyUnit(finalSelectedCell.x, finalSelectedCell.y, name, type);

            // Просто сохраняем значения в поля (для информации, не влияют на характеристики)
            friendly.setUpgradeLevel(upgradeLvl);
            friendly.setUpgradeClass(upgradeClass);

            // Создаём систему опыта, но НЕ применяем бонусы к характеристикам
            if (level > 1 || exp > 0) {
                ExperienceSystem expSys = new ExperienceSystem(type);
                for (int i = 1; i < level; i++) {
                    expSys.addExperience(expSys.getExperienceForNextLevel());
                }
                if (exp > 0) {
                    expSys.addExperience(exp);
                }
                friendly.setExperienceSystem(expSys);
                // ВАЖНО: НЕ вызываем updateStatsFromExperience() - бонусы опыта НЕ применяются!
            }

            friendlies.add(friendly);
            dialog.dispose();
            gridPanel.repaint();

            System.out.println("Добавлен союзник: " + name + " (тип: " + type +
                    ") - характеристики БАЗОВЫЕ! Модернизация и опыт сохранены только для информации.");
        });

        removeButton.addActionListener(e -> {
            if (finalExisting != null) {
                friendlies.remove(finalExisting);
                System.out.println("Удалён союзник из [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel);

        dialog.add(new JScrollPane(mainPanel));
        dialog.setVisible(true);
    }

    // Вспомогательный метод для получения опыта до следующего уровня
    private int getExpForNextLevel(int level) {
        return 100 * level;
    }

    private void showStorageDialog() {
        if (selectedCell == null) return;

        // Находим существующую тумбочку в выбранной клетке
        StorageChest existing = null;
        for (StorageChest chest : storageChests) {
            if (chest.gridX == selectedCell.x && chest.gridY == selectedCell.y) {
                existing = chest;
                break;
            }
        }

        final StorageChest finalExisting = existing;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование тумбочки", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(550, 550);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель для добавления предметов
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addPanel.setBorder(BorderFactory.createTitledBorder("➕ Добавить предмет"));

        JComboBox<ItemTypeWrapper> itemTypeCombo = new JComboBox<>();

        // ===== ВСЕ ТИПЫ СНАРЯДОВ =====
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "20mm снаряды (базовые)", "Shells/20mmShell_based.png", Caliber.CALIBER_20MM, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "20mm снаряды (улучшенные)", "Shells/20mmShell_improved.png", Caliber.CALIBER_20MM, true));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "25mm снаряды", "Shells/25mmShell_based.png", Caliber.CALIBER_25MM, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "8mm снаряды (Mauser)", "Shells/8mmShell_based.png", Caliber.CALIBER_8MM, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "13mm снаряды", "Shells/13mmShell_based.png", Caliber.CALIBER_13MM, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "30mm снаряды", "Shells/30mmShell_based.png", Caliber.CALIBER_30MM, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "37mm снаряды", "Shells/37mmShell_based.png", Caliber.CALIBER_37MM, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "45mm снаряды", "Shells/45mmShell_based.png", Caliber.CALIBER_45MM, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "47mm снаряды", "Shells/47mmShell_based.png", Caliber.CALIBER_47MM, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "76mm снаряды", "Shells/76mmShell_based.png", Caliber.CALIBER_76MM, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "105mm снаряды", "Shells/105mmShell_based.png", Caliber.CALIBER_105MM, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "128mm снаряды", "Shells/128mmShell_based.png", Caliber.CALIBER_128MM, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "203mm снаряды (HESH)", "Shells/203mmShell_HESH.png", Caliber.CALIBER_203MM, false));

        // Ключи
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.KEY, "Красный ключ", null));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.KEY, "Оранжевый ключ", null));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.KEY, "Жёлтый ключ", null));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.KEY, "Зелёный ключ", null));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.KEY, "Синий ключ", null));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.KEY, "Фиолетовый ключ", null));

        // ===== ВСЁ ОСТАЛЬНОЕ ОРУЖИЕ =====
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON, "2 cm Breda (I)", "Weapon/2 cm Breda (I).png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_25MM, "25mm Canon Raccourci mle. 1934", "Weapon/25-mm-Canon-Raccourci-mle.-1934.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_8MM, "7,92 mm Mauser E.W. 141", "Weapon/7,92 mm Mauser E.W. 141.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_13MM_JAPAN, "13 mm Autocannon Type Ho", "Weapon/13 mm Autocannon Type Ho.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_13MM_FRENCH, "13,2 mm Hotchkiss mle. 1930", "Weapon/13,2 mm Hotchkiss mle. 1930.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_30MM, "3 cm M.K. 103A", "Weapon/3 cm M.K. 103A.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_37MM_ITALIAN, "Cannone da 37-40", "Weapon/Cannone da 37-40.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_37MM_AMERICAN, "37 mm Semiautomatic Gun M1924", "Weapon/37 mm Semiautomatic Gun M1924.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_37MM_SWEDEN, "37 mm kan m-38-49 strv", "Weapon/37 mm kan m-38-49 strv.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_45MM, "45 мм обр. 1932 г.", "Weapon/45 мм обр. 1932 г.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_47MM_FRENCH, "47 mm SA35", "Weapon/47 mm SA35.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_47MM_ITALIAN, "Cannone da 47-32", "Weapon/Cannone da 47-32.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_76MM, "76 мм Л-10С", "Weapon/76 мм Л-10С.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_105MM, "10,5 cm StuH 42 L28", "Weapon/10,5 cm StuH 42 L28.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_128MM, "12,8 cm Kw.K. L50", "Weapon/12,8 cm Kw.K. L50.png", null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.WEAPON_203MM, "8-inch Howitzer M47", "Weapon/8-inch Howitzer M47.png", null, false));

        // ===== ПРЕДМЕТЫ =====
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.MEDKIT, "💊 Аптечка", null, null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.REPAIR_KIT, "🔧 Ремкомплект", null, null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.FIRE_EXTINGUISHER, "🧯 Огнетушитель", null, null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.GRENADE, "💣 Граната", null, null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.BANDAGE, "🩹 Бинт", null, null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.ENERGY_DRINK, "⚡ Энергетик", null, null, false));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.BREAD, "🍞 Просроченный хлеб", null, null, false));

        JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        countSpinner.setPreferredSize(new Dimension(70, 25));

        final JTextArea itemsArea = new JTextArea(12, 40);
        itemsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        itemsArea.setBackground(new Color(40, 40, 50));
        itemsArea.setForeground(Color.WHITE);
        itemsArea.setMargin(new Insets(5, 5, 5, 5));

        if (finalExisting != null) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Item.ItemType, Integer> entry : finalExisting.getItems().entrySet()) {
                sb.append(entry.getKey().name()).append(" ").append(entry.getValue()).append("\n");
            }
            for (Map.Entry<Caliber, Integer> entry : finalExisting.getAmmo().entrySet()) {
                sb.append("AMMO_CALIBER_").append(entry.getKey().name().replace("CALIBER_", "")).append(" ").append(entry.getValue()).append("\n");
            }
            // ===== ИСПРАВЛЕНИЕ: ключи хранятся как Door.DoorColor, а НЕ KeyItem.ItemType =====
            for (Map.Entry<Door.DoorColor, Integer> entry : finalExisting.getKeys().entrySet()) {
                sb.append("KEY_").append(entry.getKey().name()).append(" ").append(entry.getValue()).append("\n");
            }
            itemsArea.setText(sb.toString());
        }

        JButton addButton = new JButton("➕ Добавить");
        // В showStorageDialog(), в addButton.addActionListener, замените блок для ключей:

        addButton.addActionListener(e -> {
            ItemTypeWrapper wrapper = (ItemTypeWrapper) itemTypeCombo.getSelectedItem();
            int count = (Integer) countSpinner.getValue();

            if (wrapper.caliber != null) {
                // Это снаряды
                itemsArea.append("AMMO_CALIBER_" + wrapper.caliber.name().replace("CALIBER_", "") + " " + count + "\n");
            } else if (wrapper.type == Item.ItemType.KEY) {
                // ===== ЭТО КЛЮЧИ - ИСПРАВЛЕНО =====
                String displayName = wrapper.displayName;
                String colorName = "";
                if (displayName.contains("Красный")) colorName = "RED";
                else if (displayName.contains("Оранжевый")) colorName = "ORANGE";
                else if (displayName.contains("Жёлтый")) colorName = "YELLOW";
                else if (displayName.contains("Зелёный")) colorName = "GREEN";
                else if (displayName.contains("Синий")) colorName = "BLUE";
                else if (displayName.contains("Фиолетовый")) colorName = "VIOLET";

                if (!colorName.isEmpty()) {
                    itemsArea.append("KEY_" + colorName + " " + count + "\n");
                } else {
                    itemsArea.append("KEY " + count + "\n");
                }
            } else {
                // Обычный предмет или оружие
                itemsArea.append(wrapper.type.name() + " " + count + "\n");
            }
        });

        addPanel.add(new JLabel("Предмет:"));
        addPanel.add(itemTypeCombo);
        addPanel.add(new JLabel("Кол-во:"));
        addPanel.add(countSpinner);
        addPanel.add(addButton);

        // Панель с содержимым
        JLabel infoLabel = new JLabel("📦 Содержимое тумбочки (каждая строка: ТИП КОЛИЧЕСТВО)");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(Color.LIGHT_GRAY);

        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setBorder(BorderFactory.createTitledBorder("📦 Содержимое"));
        contentPanel.add(infoLabel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(itemsArea), BorderLayout.CENTER);

        // Кнопки действий
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton saveButton = new JButton(finalExisting != null ? "💾 Сохранить изменения" : "➕ Добавить тумбочку");
        JButton removeButton = new JButton("🗑 Удалить тумбочку");
        JButton clearButton = new JButton("🧹 Очистить всё");
        JButton cancelButton = new JButton("❌ Отмена");

        saveButton.setBackground(new Color(0, 150, 0));
        saveButton.setForeground(Color.WHITE);
        removeButton.setBackground(new Color(150, 0, 0));
        removeButton.setForeground(Color.WHITE);
        clearButton.setBackground(new Color(200, 100, 0));
        clearButton.setForeground(Color.WHITE);

        saveButton.addActionListener(ev -> {
            if (finalExisting != null) {
                storageChests.remove(finalExisting);
            }

            StorageChest chest = new StorageChest(finalSelectedCell.x, finalSelectedCell.y);

            String[] lines = itemsArea.getText().split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        String typeStr = parts[0];
                        int count = Integer.parseInt(parts[1]);

                        // ===== ПРОВЕРКА НА КЛЮЧИ (ДОБАВЛЯЕМ ЭТОТ БЛОК ПЕРВЫМ!) =====
                        if (typeStr.startsWith("KEY_")) {
                            String colorName = typeStr.substring(4);
                            try {
                                Door.DoorColor color = Door.DoorColor.valueOf(colorName);
                                chest.addKey(color, count);
                                System.out.println("  Добавлен ключ: " + colorName + " x" + count);
                            } catch (IllegalArgumentException e) {
                                System.err.println("Неизвестный цвет ключа: " + colorName);
                            }
                        }
                        // ===== ПРОВЕРКА НА СНАРЯДЫ =====
                        else if (typeStr.startsWith("AMMO_CALIBER_")) {
                            String caliberName = typeStr.substring("AMMO_CALIBER_".length());
                            Caliber caliber = Caliber.valueOf("CALIBER_" + caliberName);
                            chest.addAmmo(caliber, count);
                            System.out.println("  Добавлены снаряды: " + caliberName + " x" + count);
                        } else {
                            Item.ItemType type = Item.ItemType.valueOf(typeStr);
                            chest.addItem(type, count);
                            System.out.println("  Добавлен предмет: " + typeStr + " x" + count);
                        }
                    } catch (Exception ex) {
                        System.err.println("Ошибка парсинга: " + line + " - " + ex.getMessage());
                    }
                }
            }

            if (canPlaceStorageChest(finalSelectedCell.x, finalSelectedCell.y)) {
                storageChests.add(chest);
                dialog.dispose();
                gridPanel.repaint();
                JOptionPane.showMessageDialog(this,
                        "✅ Тумбочка успешно " + (finalExisting != null ? "обновлена" : "добавлена") + "!",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "❌ Нельзя разместить тумбочку здесь!\nМесто занято стеной или другой тумбочкой.",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        removeButton.addActionListener(ev -> {
            if (finalExisting != null) {
                storageChests.remove(finalExisting);
                JOptionPane.showMessageDialog(dialog,
                        "✅ Тумбочка удалена!",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        clearButton.addActionListener(ev -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Вы уверены, что хотите очистить всё содержимое тумбочки?",
                    "Подтверждение",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                itemsArea.setText("");
            }
        });

        cancelButton.addActionListener(ev -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(addPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // Добавьте метод showInfernalLandDialog():
    private void showInfernalLandDialog() {
        if (selectedCell == null) return;

        InfernalLand existingLand = null;
        for (InfernalLand land : infernalLands) {
            if (land.gridX == selectedCell.x && land.gridY == selectedCell.y) {
                existingLand = land;
                break;
            }
        }

        final InfernalLand finalExisting = existingLand;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование адской земли", true);
        dialog.setLayout(new GridLayout(3, 2, 5, 5));
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel("Тип:"));
        dialog.add(new JLabel("Адская земля (декоративная)"));

        JButton addButton = new JButton(finalExisting != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            if (finalExisting != null) {
                infernalLands.remove(finalExisting);
            }
            infernalLands.add(new InfernalLand(finalSelectedCell.x, finalSelectedCell.y));
            dialog.dispose();
            gridPanel.repaint();
            System.out.println("Добавлена адская земля в [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
        });

        removeButton.addActionListener(e -> {
            if (finalExisting != null) {
                infernalLands.remove(finalExisting);
                System.out.println("Удалена адская земля из [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void showTraderDialog() {
        if (selectedCell == null) return;

        Trader existingTrader = null;
        for (Trader t : traders) {
            if (t.gridX == selectedCell.x && t.gridY == selectedCell.y) {
                existingTrader = t;
                break;
            }
        }

        final Trader finalExisting = existingTrader;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование торговца", true);
        dialog.setLayout(new GridLayout(4, 2, 5, 5));
        dialog.setSize(350, 180);
        dialog.setLocationRelativeTo(this);

        JTextField nameField = new JTextField(finalExisting != null ? finalExisting.name : "T34");

        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel("Имя торговца:"));
        dialog.add(nameField);

        JButton addButton = new JButton(finalExisting != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Введите имя торговца!");
                return;
            }

            if (finalExisting != null) {
                traders.remove(finalExisting);
            }

            traders.add(new Trader(finalSelectedCell.x, finalSelectedCell.y, name));
            dialog.dispose();
            gridPanel.repaint();
            System.out.println("Добавлен торговец: " + name + " в [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
        });

        removeButton.addActionListener(e -> {
            if (finalExisting != null) {
                traders.remove(finalExisting);
                System.out.println("Удалён торговец из [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void showEnemyDialog() {
        if (selectedCell == null) return;

        // Проверяем, есть ли уже враг в этой клетке
        Enemy existingEnemy = null;
        for (Enemy e : enemies) {
            if (e.gridX == selectedCell.x && e.gridY == selectedCell.y) {
                existingEnemy = e;
                break;
            }
        }

        final Enemy finalExistingEnemy = existingEnemy;
        final Point finalSelectedCell = selectedCell;

        JComboBox<String> factionBox = new JComboBox<>(new String[]{"GERMANY", "JAPAN", "FRANCE", "ITALY", "NEUTRAL"});
        if (finalExistingEnemy != null) {
            factionBox.setSelectedItem(finalExistingEnemy.getFaction().name());
        }

        JDialog dialog = new JDialog(this, "Редактирование врага", true);
        // Добавьте в диалог:
        dialog.add(new JLabel("Фракция:"));
        dialog.add(factionBox);
        dialog.setLayout(new GridLayout(7, 2, 5, 5));  // ← увеличили до 7 строк
        dialog.setSize(350, 300);
        dialog.setLocationRelativeTo(this);

        // ===== НОВОЕ: тип врага =====
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Leichttraktor", "R_Otsu", "M14_41", "H35", "FT", "Fiat3000"});
        // И в блоке проверки существующего врага:
        if (finalExistingEnemy != null) {
            if ("R_Otsu".equals(finalExistingEnemy.type)) {
                typeBox.setSelectedItem("R_Otsu");
            } else if ("M14_41".equals(finalExistingEnemy.type)) {
                typeBox.setSelectedItem("M14_41");
            } else if ("H35".equals(finalExistingEnemy.type)) {
                typeBox.setSelectedItem("H35");
            } else if ("FT".equals(finalExistingEnemy.type)) {  // ← ДОБАВИТЬ
                typeBox.setSelectedItem("FT");                   // ← ДОБАВИТЬ
            } else if ("Fiat3000".equals(finalExistingEnemy.type)) {  // ← ДОБАВИТЬ
                typeBox.setSelectedItem("Fiat3000");                   // ← ДОБАВИТЬ
            } else {
                typeBox.setSelectedItem("Leichttraktor");
            }
        }

        JTextField healthField = new JTextField(finalExistingEnemy != null ? String.valueOf(finalExistingEnemy.health) : "130");
        JComboBox<String> directionBox = new JComboBox<>(new String[]{"UP", "DOWN", "LEFT", "RIGHT"});
        if (finalExistingEnemy != null) {
            directionBox.setSelectedItem(finalExistingEnemy.direction.name());
        }

        // Характеристики
        JTextField strengthField = new JTextField(finalExistingEnemy != null ? String.valueOf(finalExistingEnemy.strength) : "30");
        JTextField agilityField = new JTextField(finalExistingEnemy != null ? String.valueOf(finalExistingEnemy.agility) : "50");
        JTextField armorField = new JTextField(finalExistingEnemy != null ? String.valueOf(finalExistingEnemy.armor) : "10");
        JTextField nimbleField = new JTextField(finalExistingEnemy != null ? String.valueOf(finalExistingEnemy.nimble) : "10");

        JComboBox<String> behaviorBox = new JComboBox<>(new String[]{"Агрессивный", "Тактический", "Снайпер"});
        if (finalExistingEnemy != null) {
            switch(finalExistingEnemy.getBehaviorType()) {
                case AGGRESSIVE: behaviorBox.setSelectedIndex(0); break;
                case TACTICAL: behaviorBox.setSelectedIndex(1); break;
                case SNIPER: behaviorBox.setSelectedIndex(2); break;
            }
        }

        dialog.add(new JLabel("Тип врага:"));        // ← НОВОЕ
        dialog.add(typeBox);                        // ← НОВОЕ
        dialog.add(new JLabel("Поведение:"));
        dialog.add(behaviorBox);
        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel("Здоровье:"));
        dialog.add(healthField);
        dialog.add(new JLabel("Направление:"));
        dialog.add(directionBox);
        dialog.add(new JLabel("Сила (очки хода):"));
        dialog.add(strengthField);
        dialog.add(new JLabel("Ловкость (стоимость шага):"));
        dialog.add(agilityField);
        dialog.add(new JLabel("Броня:"));
        dialog.add(armorField);
        dialog.add(new JLabel("Проворность (уклонение):"));
        dialog.add(nimbleField);

        JButton addButton = new JButton(finalExistingEnemy != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            try {
                int health = Integer.parseInt(healthField.getText());
                int strength = Integer.parseInt(strengthField.getText());
                int agility = Integer.parseInt(agilityField.getText());
                Enemy.Direction dir = Enemy.Direction.valueOf((String) directionBox.getSelectedItem());
                String enemyType = (String) typeBox.getSelectedItem();

                if (finalExistingEnemy != null) {
                    enemies.remove(finalExistingEnemy);
                }

                // ===== ИСПРАВЛЕНИЕ: создаём врага с правильным типом =====
                Enemy enemy = new Enemy(finalSelectedCell.x, finalSelectedCell.y, dir);
                enemy.setType(enemyType);  // ← УСТАНАВЛИВАЕМ ТИП!

                // Если это R_Otsu - применяем специальные статы
                if ("R_Otsu".equals(enemyType)) {
                    enemy.setR_OtsuStats();
                }

                // Переопределяем загруженные характеристики
                enemy.health = health;
                enemy.maxHealth = health;
                enemy.strength = strength;
                enemy.agility = agility;
                enemy.armor = Integer.parseInt(armorField.getText());
                enemy.nimble = Integer.parseInt(nimbleField.getText());
                enemy.calculateDodgeChance();
                enemy.calculateMovePoints();

                int behaviorIndex = behaviorBox.getSelectedIndex();
                switch(behaviorIndex) {
                    case 0: enemy.setBehaviorType(Enemy.BehaviorType.AGGRESSIVE); break;
                    case 1: enemy.setBehaviorType(Enemy.BehaviorType.TACTICAL); break;
                    case 2: enemy.setBehaviorType(Enemy.BehaviorType.SNIPER); break;
                }

                enemies.add(enemy);

                dialog.dispose();
                gridPanel.repaint();
                System.out.println("Добавлен враг: " + enemyType + " в [" + finalSelectedCell.x + "," + finalSelectedCell.y +
                        "], здоровье=" + health + ", сила=" + strength + ", ловкость=" + agility);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Введите корректные числа!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        removeButton.addActionListener(e -> {
            if (finalExistingEnemy != null) {
                enemies.remove(finalExistingEnemy);
                System.out.println("Удалён враг из [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void showStartPositionDialog() {
        if (selectedCell == null) return;

        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Установка стартовой позиции", true);
        dialog.setLayout(new GridLayout(5, 2, 5, 5));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        // Информация о размере (5x5)
        JLabel sizeLabel = new JLabel("Размер позиции: 5x5 клеток");
        sizeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        sizeLabel.setForeground(Color.BLUE);

        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(sizeLabel);
        dialog.add(new JLabel(""));

        // Проверка валидности позиции
        boolean isValid = isValidStartPosition(finalSelectedCell.x, finalSelectedCell.y);

        JLabel validLabel = new JLabel(isValid ? "✅ Позиция подходит" : "❌ Позиция пересекается с препятствиями!");
        validLabel.setForeground(isValid ? Color.GREEN : Color.RED);
        validLabel.setFont(new Font("Arial", Font.BOLD, 11));
        dialog.add(validLabel);
        dialog.add(new JLabel(""));

        JButton setButton = new JButton("Установить как стартовую");
        setButton.setEnabled(isValid);
        JButton removeButton = new JButton("Удалить стартовую позицию");
        JButton cancelButton = new JButton("Отмена");

        setButton.addActionListener(e -> {
            startPosition = new Point(finalSelectedCell.x, finalSelectedCell.y);
            dialog.dispose();
            gridPanel.repaint();
            JOptionPane.showMessageDialog(this,
                    "✅ Стартовая позиция установлена в [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]\n" +
                            "Размер области: 5x5 клеток",
                    "Стартовая позиция",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        removeButton.addActionListener(e -> {
            startPosition = null;
            dialog.dispose();
            gridPanel.repaint();
            JOptionPane.showMessageDialog(this,
                    "🗑 Стартовая позиция удалена",
                    "Стартовая позиция",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(setButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    // Метод показа диалога для победной зоны:
    private void showWinZoneDialog() {
        if (selectedCell == null) return;

        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Установка победной зоны", true);
        dialog.setLayout(new GridLayout(5, 2, 5, 5));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JLabel sizeLabel = new JLabel("Размер зоны: 5x5 клеток");
        sizeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        sizeLabel.setForeground(Color.GREEN);

        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(sizeLabel);
        dialog.add(new JLabel(""));

        // Проверка валидности позиции
        boolean isValid = isValidWinZone(finalSelectedCell.x, finalSelectedCell.y);

        JLabel validLabel = new JLabel(isValid ? "✅ Позиция подходит" : "❌ Позиция пересекается с препятствиями!");
        validLabel.setForeground(isValid ? Color.GREEN : Color.RED);
        validLabel.setFont(new Font("Arial", Font.BOLD, 11));
        dialog.add(validLabel);
        dialog.add(new JLabel(""));

        JButton setButton = new JButton("Установить как победную зону");
        setButton.setEnabled(isValid);
        JButton removeButton = new JButton("Удалить победную зону");
        JButton cancelButton = new JButton("Отмена");

        setButton.addActionListener(e -> {
            winZone = new Point(finalSelectedCell.x, finalSelectedCell.y);
            dialog.dispose();
            gridPanel.repaint();
            JOptionPane.showMessageDialog(this,
                    "✅ Победная зона установлена в [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]\n" +
                            "Размер области: 5x5 клеток",
                    "Победная зона",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        removeButton.addActionListener(e -> {
            winZone = null;
            dialog.dispose();
            gridPanel.repaint();
            JOptionPane.showMessageDialog(this,
                    "🗑 Победная зона удалена",
                    "Победная зона",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(setButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    // Метод проверки валидности победной зоны:
    private boolean isValidWinZone(int x, int y) {
        int size = 5;

        if (x < 0 || x + size > gridWidth || y < 0 || y + size > gridHeight) {
            return false;
        }

        // Проверка на пересечение со стенами
        for (Wall wall : walls) {
            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    if (wall.gridX == x + dx && wall.gridY == y + dy) {
                        return false;
                    }
                }
            }
        }

        // Проверка на пересечение с деревьями
        for (Tree tree : trees) {
            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    if (tree.gridX == x + dx && tree.gridY == y + dy) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void showGarbageDialog() {
        if (selectedCell == null) return;

        GarbageContainer existing = null;
        for (GarbageContainer container : garbageContainers) {
            if (container.containsCell(selectedCell.x, selectedCell.y)) {
                existing = container;
                break;
            }
        }

        final GarbageContainer finalExisting = existing;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование мусорного контейнера", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(550, 550);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Информация
        // В showGarbageDialog():
        JLabel infoLabel = new JLabel("📍 Размер контейнера: 2x1 клетки (горизонтальный)");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        infoLabel.setForeground(Color.ORANGE);
        mainPanel.add(infoLabel, BorderLayout.NORTH);

        // Панель для добавления предметов
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addPanel.setBorder(BorderFactory.createTitledBorder("➕ Добавить предмет"));

        JComboBox<ItemTypeWrapper> itemTypeCombo = new JComboBox<>();
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO, "🔫 Снаряды 20mm", null));
        // Временно убираем AMMO_203MM
        // itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.AMMO_203MM, "💥 Снаряды 203mm", null));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.MEDKIT, "💊 Аптечка", null));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.BANDAGE, "🩹 Бинт", null));
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.ENERGY_DRINK, "⚡ Энергетик", null));
        // Временно убираем BREAD
        itemTypeCombo.addItem(new ItemTypeWrapper(Item.ItemType.BREAD, "🍞 Просроченный хлеб", null));

        JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        countSpinner.setPreferredSize(new Dimension(70, 25));

        final JTextArea itemsArea = new JTextArea(12, 40);
        itemsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        itemsArea.setBackground(new Color(40, 40, 50));
        itemsArea.setForeground(Color.WHITE);
        itemsArea.setMargin(new Insets(5, 5, 5, 5));

        if (finalExisting != null) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Item.ItemType, Integer> entry : finalExisting.getItems().entrySet()) {
                sb.append(entry.getKey().name()).append(" ").append(entry.getValue()).append("\n");
            }
            itemsArea.setText(sb.toString());
        }

        JButton addButton = new JButton("➕ Добавить");
        addButton.addActionListener(ev -> {
            ItemTypeWrapper wrapper = (ItemTypeWrapper) itemTypeCombo.getSelectedItem();
            int count = (Integer) countSpinner.getValue();
            // Сохраняем правильное имя типа, а не displayName!
            itemsArea.append(wrapper.type.name() + " " + count + "\n");
            System.out.println("Добавлен предмет: " + wrapper.type.name() + " x" + count);
        });

        addPanel.add(new JLabel("Предмет:"));
        addPanel.add(itemTypeCombo);
        addPanel.add(new JLabel("Кол-во:"));
        addPanel.add(countSpinner);
        addPanel.add(addButton);

        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setBorder(BorderFactory.createTitledBorder("📦 Содержимое"));
        contentPanel.add(new JScrollPane(itemsArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton saveButton = new JButton(finalExisting != null ? "💾 Сохранить" : "➕ Добавить контейнер");
        JButton removeButton = new JButton("🗑 Удалить");
        JButton cancelButton = new JButton("❌ Отмена");

        saveButton.addActionListener(ev -> {
            if (finalExisting != null) {
                garbageContainers.remove(finalExisting);
            }

            GarbageContainer container = new GarbageContainer(finalSelectedCell.x, finalSelectedCell.y);

            String[] lines = itemsArea.getText().split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        // Проверяем, является ли это боеприпасами 203мм (если добавите позже)
                        if (parts[0].equals("AMMO_8MM")) {
                            container.addAmmo(Caliber.CALIBER_8MM, Integer.parseInt(parts[1]));
                        }
                        else if (parts[0].equals("AMMO_203MM")) {
                            container.addAmmo(Caliber.CALIBER_203MM, Integer.parseInt(parts[1]));
                        } else {
                            Item.ItemType type = Item.ItemType.valueOf(parts[0]);
                            int count = Integer.parseInt(parts[1]);
                            if (count > 0) {
                                container.addItem(type, count);
                                System.out.println("  Добавлен предмет: " + type.name() + " x" + count);
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Ошибка парсинга: " + line + " - " + ex.getMessage());
                    }
                }
            }

            garbageContainers.add(container);
            dialog.dispose();
            gridPanel.repaint();
            JOptionPane.showMessageDialog(LevelEditor.this, "✅ Мусорный контейнер добавлен!");
        });

        removeButton.addActionListener(ev -> {
            if (finalExisting != null) {
                garbageContainers.remove(finalExisting);
                JOptionPane.showMessageDialog(dialog, "✅ Контейнер удалён!");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(ev -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(addPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private boolean isValidStartPosition(int x, int y) {
        int size = 5; // Стартовая позиция 5x5

        // Проверка границ
        if (x < 0 || x + size > gridWidth || y < 0 || y + size > gridHeight) {
            System.out.println("❌ Выход за границы карты!");
            return false;
        }

        // Проверка на пересечение со стенами
        for (Wall wall : walls) {
            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    if (wall.gridX == x + dx && wall.gridY == y + dy) {
                        System.out.println("❌ Пересечение со стеной в [" + (x+dx) + "," + (y+dy) + "]");
                        return false;
                    }
                }
            }
        }

        // Проверка на пересечение с деревьями
        for (Tree tree : trees) {
            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    if (tree.gridX == x + dx && tree.gridY == y + dy) {
                        System.out.println("❌ Пересечение с деревом в [" + (x+dx) + "," + (y+dy) + "]");
                        return false;
                    }
                }
            }
        }

        // Проверка на пересечение с врагами
        for (Enemy enemy : enemies) {
            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    if (enemy.gridX == x + dx && enemy.gridY == y + dy) {
                        System.out.println("❌ Пересечение с врагом в [" + (x+dx) + "," + (y+dy) + "]");
                        return false;
                    }
                }
            }
        }

        // Проверка на пересечение с NPC
        for (QuestNPC npc : npcs) {
            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    if (npc.gridX == x + dx && npc.gridY == y + dy) {
                        System.out.println("❌ Пересечение с NPC в [" + (x+dx) + "," + (y+dy) + "]");
                        return false;
                    }
                }
            }
        }

        // Проверка на пересечение с союзниками
        for (FriendlyUnit friendly : friendlies) {
            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    if (friendly.gridX == x + dx && friendly.gridY == y + dy) {
                        System.out.println("❌ Пересечение с союзником в [" + (x+dx) + "," + (y+dy) + "]");
                        return false;
                    }
                }
            }
        }

        // Проверка на пересечение с тумбочками
        for (StorageChest chest : storageChests) {
            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    if (chest.gridX == x + dx && chest.gridY == y + dy) {
                        System.out.println("❌ Пересечение с тумбочкой в [" + (x+dx) + "," + (y+dy) + "]");
                        return false;
                    }
                }
            }
        }

        System.out.println("✅ Стартовая позиция валидна!");
        return true;
    }

    // Вспомогательный класс для комбобокса
    private class ItemTypeWrapper {
        public Item.ItemType type;
        public String displayName;
        public String iconPath;
        public Caliber caliber;      // ← ДОБАВИТЬ для снарядов
        public boolean isImproved;   // ← ДОБАВИТЬ для улучшенных снарядов

        public ItemTypeWrapper(Item.ItemType type, String displayName, String iconPath) {
            this(type, displayName, iconPath, null, false);
        }

        public ItemTypeWrapper(Item.ItemType type, String displayName, String iconPath, Caliber caliber, boolean isImproved) {
            this.type = type;
            this.displayName = displayName;
            this.iconPath = iconPath;
            this.caliber = caliber;
            this.isImproved = isImproved;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private boolean canPlaceStorageChest(int x, int y) {
        int width = 1;   // Тумбочка занимает 1 клетку
        int height = 1;  // Тумбочка занимает 1 клетку

        // Проверка границ
        if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
            System.out.println("❌ Тумбочка выходит за границы карты!");
            return false;
        }

        // ❌ НЕЛЬЗЯ ставить на стены
        for (Wall wall : walls) {
            if (wall.gridX == x && wall.gridY == y) {
                System.out.println("❌ Стена в клетке [" + x + "," + y + "]!");
                return false;
            }
        }

        // ❌ НЕЛЬЗЯ ставить на другие тумбочки
        for (StorageChest chest : storageChests) {
            if (chest.gridX == x && chest.gridY == y) {
                System.out.println("❌ Другая тумбочка в клетке [" + x + "," + y + "]!");
                return false;
            }
        }

        // ✅ МОЖНО ставить на деревья, асфальт, пустую землю
        System.out.println("✅ Место для тумбочки подходит!");
        return true;
    }

    private boolean isStorageAt(int x, int y) {
        for (StorageChest chest : storageChests) {
            if (chest.containsCell(x, y)) {
                return true;
            }
        }
        return false;
    }

    private void removeStorageAt(int x, int y) {
        storageChests.removeIf(chest -> chest.containsCell(x, y));
    }

    private void saveLevel() {
        if (currentLevelPath == null) {
            saveLevelAs();
        } else {
            saveAllToFile(currentLevelPath);
            JOptionPane.showMessageDialog(this, "Уровень сохранён!");
        }
    }

    private void saveLevelAs() {
        JFileChooser chooser = new JFileChooser("zlevels\\");
        chooser.setSelectedFile(new File("level1.txt"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if (!path.endsWith(".txt")) {
                path += ".txt";
            }
            currentLevelPath = path;
            saveAllToFile(currentLevelPath);
            setTitle("Редактор уровней - TankFight [" + file.getName() + "]");
            JOptionPane.showMessageDialog(this, "Уровень сохранён!");
        }
    }

    private void saveAllToFile(String path) {
        // Сначала сохраняем только стены (перезаписываем файл)
        LevelLoader.saveLevel(path, walls);

        // Затем ДОПИСЫВАЕМ остальные объекты
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(path, true))) {

            // ===== СТАРТОВАЯ ПОЗИЦИЯ =====
            if (startPosition != null) {
                writer.println("\n// ===== START POSITION =====");
                writer.printf("START %d %d\n", startPosition.x, startPosition.y);
            }

            // ===== NPC =====
            // В saveAllToFile(), в блоке // ===== NPC =====, замените на:
            if (!npcs.isEmpty()) {
                writer.println("\n// ===== NPC =====");
                for (QuestNPC npc : npcs) {
                    // Определяем тип NPC по имени
                    String npcType;
                    if ("Sav m/43".equals(npc.name)) {
                        npcType = "Sav m/43";
                    } else {
                        npcType = "T18";
                    }
                    writer.printf("NPC %d %d %s %s %d\n",
                            npc.gridX, npc.gridY, npc.name, npcType, npc.questTarget);
                }
            }

            // ===== TRADERS =====
            if (!traders.isEmpty()) {
                writer.println("\n// ===== TRADERS =====");
                for (Trader trader : traders) {
                    writer.printf("TRADER %d %d %s\n", trader.gridX, trader.gridY, trader.name);
                    System.out.println("Сохранён торговец: " + trader.name + " в [" + trader.gridX + "," + trader.gridY + "]");
                }
            }

            // ===== TREES =====
            if (!trees.isEmpty()) {
                writer.println("\n// ===== TREES =====");
                for (Tree tree : trees) {
                    writer.printf("TREE %d %d %s\n",
                            tree.gridX, tree.gridY, tree.treeType);
                }
            }

            // ===== PAVEMENT =====
            if (!pavements.isEmpty()) {
                writer.println("\n// ===== PAVEMENT =====");
                for (Pavement pavement : pavements) {
                    writer.printf("PAVEMENT %d %d\n",
                            pavement.gridX, pavement.gridY);
                }
            }

            // ===== OAKPLANKS =====
            if (!oakPlanks.isEmpty()) {
                writer.println("\n// ===== OAKPLANKS =====");
                for (OakPlanks op : oakPlanks) {
                    writer.printf("OAKPLANKS %d %d\n", op.gridX, op.gridY);
                }
            }

            if (!infernalLands.isEmpty()) {
                writer.println("\n// ===== INFERNAL LAND =====");
                for (InfernalLand land : infernalLands) {
                    writer.printf("INFERNALLAND %d %d\n", land.gridX, land.gridY);
                }
            }

            if (!ironFloors.isEmpty()) {
                writer.println("\n// ===== IRON FLOOR =====");
                for (IronFloor floor : ironFloors) {
                    writer.printf("IRONFLOOR %d %d\n", floor.gridX, floor.gridY);
                }
            }

            // ===== WATERS =====
            if (!waters.isEmpty()) {
                writer.println("\n// ===== WATERS =====");
                for (Water water : waters) {
                    writer.printf("WATER %d %d\n", water.gridX, water.gridY);
                }
            }

            if (!doors.isEmpty()) {
                writer.println("\n// ===== DOORS =====");
                for (Door door : doors) {
                    writer.printf("DOOR %d %d %s %s\n",
                            door.gridX, door.gridY,
                            door.color.name(),
                            door.isOpen ? "OPEN" : "CLOSED");
                }
            }

            // ===== FRIENDLY =====
            if (!friendlies.isEmpty()) {
                writer.println("\n// ===== FRIENDLY =====");
                for (FriendlyUnit friendly : friendlies) {
                    // Формат: FRIENDLY x y name type upgradeLevel upgradeClass expLevel expPoints
                    int upgradeLevel = friendly.getUpgradeLevel();
                    String upgradeClass = friendly.getUpgradeClass();
                    if (upgradeClass == null) upgradeClass = "NONE";

                    int expLevel = 1;
                    int expPoints = 0;
                    if (friendly.getExperienceSystem() != null) {
                        expLevel = friendly.getExperienceSystem().getLevel();
                        expPoints = friendly.getExperienceSystem().getExperience();
                    }

                    writer.printf("FRIENDLY %d %d %s %s %d %s %d %d\n",
                            friendly.gridX, friendly.gridY, friendly.name, friendly.type,
                            upgradeLevel, upgradeClass, expLevel, expPoints);
                }
            }

            // ===== STORAGE CHESTS =====
            if (!storageChests.isEmpty()) {
                writer.println("\n// ===== STORAGE CHESTS =====");
                for (StorageChest chest : storageChests) {
                    writer.printf("STORAGE %d %d", chest.gridX, chest.gridY);

                    // Сохраняем обычные предметы
                    for (Map.Entry<Item.ItemType, Integer> entry : chest.getItems().entrySet()) {
                        Item.ItemType type = entry.getKey();
                        int count = entry.getValue();

                        // ← ДОБАВЬТЕ ЭТИ ДВЕ СТРОКИ:
                        if (type == Item.ItemType.WEAPON_47MM_FRENCH) {
                            writer.printf(" WEAPON_47MM_FRENCH %d", count);
                        } else if (type == Item.ItemType.WEAPON_47MM_ITALIAN) {
                            writer.printf(" WEAPON_47MM_ITALIAN %d", count);
                        }
                        // ← ОСТАЛЬНЫЕ ТИПЫ ОСТАЮТСЯ БЕЗ ИЗМЕНЕНИЙ
                        else if (type == Item.ItemType.WEAPON_8MM) {
                            writer.printf(" WEAPON_8MM %d", count);
                        } else if (type == Item.ItemType.WEAPON_25MM) {
                            writer.printf(" WEAPON_25MM %d", count);
                        } else if (type == Item.ItemType.WEAPON_45MM) {
                            writer.printf(" WEAPON_45MM %d", count);
                        } else if (type == Item.ItemType.WEAPON_76MM) {
                            writer.printf(" WEAPON_76MM %d", count);
                        } else if (type == Item.ItemType.WEAPON_105MM) {
                            writer.printf(" WEAPON_105MM %d", count);
                        } else if (type == Item.ItemType.WEAPON_128MM) {
                            writer.printf(" WEAPON_128MM %d", count);
                        } else if (type == Item.ItemType.WEAPON_203MM) {
                            writer.printf(" WEAPON_203MM %d", count);
                        } else if (type == Item.ItemType.WEAPON_13MM_JAPAN) {
                            writer.printf(" WEAPON_13MM_JAPAN %d", count);
                        } else if (type == Item.ItemType.WEAPON_13MM_FRENCH) {
                            writer.printf(" WEAPON_13MM_FRENCH %d", count);
                        } else if (type == Item.ItemType.WEAPON_30MM) {
                            writer.printf(" WEAPON_30MM %d", count);
                        } else if (type == Item.ItemType.WEAPON_37MM_ITALIAN) {
                            writer.printf(" WEAPON_37MM_ITALIAN %d", count);
                        } else if (type == Item.ItemType.WEAPON_37MM_AMERICAN) {
                            writer.printf(" WEAPON_37MM_AMERICAN %d", count);
                        } else if (type == Item.ItemType.WEAPON_37MM_SWEDEN) {
                            writer.printf(" WEAPON_37MM_SWEDEN %d", count);
                        } else if (type == Item.ItemType.WEAPON) {
                            writer.printf(" WEAPON %d", count);
                        } else {
                            writer.printf(" %s %d", type.name(), count);
                        }
                    }

                    // Сохраняем снаряды с указанием калибра
                    for (Map.Entry<Caliber, Integer> entry : chest.getAmmo().entrySet()) {
                        Caliber caliber = entry.getKey();
                        int count = entry.getValue();
                        String caliberStr = caliber.name().replace("CALIBER_", "");
                        writer.printf(" AMMO_CALIBER_%s %d", caliberStr, count);
                    }

                    for (Map.Entry<Door.DoorColor, Integer> entry : chest.getKeys().entrySet()) {
                        Door.DoorColor color = entry.getKey();
                        int count = entry.getValue();
                        writer.printf(" KEY_%s %d", color.name(), count);
                    }

                    writer.println();
                }
            }

            // ===== ENEMIES =====
            if (!enemies.isEmpty()) {
                writer.println("\n// ===== ENEMIES =====");
                for (Enemy enemy : enemies) {
                    int behaviorValue = 0;
                    switch(enemy.getBehaviorType()) {
                        case AGGRESSIVE: behaviorValue = 0; break;
                        case TACTICAL: behaviorValue = 1; break;
                        case SNIPER: behaviorValue = 2; break;
                    }

                    // НОВЫЙ ФОРМАТ С ТИПОМ
                    writer.printf("ENEMY %d %d %s %s %d %s %d %d %d %d %d\n",
                            enemy.gridX, enemy.gridY,
                            enemy.type,
                            enemy.getFaction().name(),  // ← Фракция
                            enemy.health,
                            enemy.direction.name(),
                            enemy.strength, enemy.agility,
                            enemy.armor, enemy.nimble, behaviorValue);
                }
            }

            // ===== GARBAGE CONTAINERS =====
            if (!garbageContainers.isEmpty()) {
                writer.println("\n// ===== GARBAGE CONTAINERS =====");
                for (GarbageContainer container : garbageContainers) {
                    writer.printf("GARBAGE %d %d", container.gridX, container.gridY);

                    // Сохраняем обычные предметы
                    for (Map.Entry<Item.ItemType, Integer> entry : container.getItems().entrySet()) {
                        Item.ItemType type = entry.getKey();
                        int count = entry.getValue();
                        writer.printf(" %s %d", type.name(), count);
                    }

                    // Сохраняем снаряды с указанием калибра
                    for (Map.Entry<Caliber, Integer> entry : container.getAmmo().entrySet()) {
                        Caliber caliber = entry.getKey();
                        int count = entry.getValue();
                        String caliberStr = caliber.name().toUpperCase();
                        writer.printf(" AMMO_%s %d", caliberStr, count);
                    }

                    writer.println();
                }
            }

            if (winZone != null) {
                writer.println("\n// ===== WIN ZONE =====");
                writer.printf("WINZONE %d %d\n", winZone.x, winZone.y);
            }

            System.out.println("✅ Уровень сохранён: Деревьев=" + trees.size() +
                    ", NPC=" + npcs.size() + ", Врагов=" + enemies.size() +
                    ", Союзников=" + friendlies.size());

        } catch (java.io.IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showWallDialog() {
        if (selectedCell == null) return;

        Wall existingWall = null;
        for (Wall w : walls) {
            if (w.gridX == selectedCell.x && w.gridY == selectedCell.y) {
                existingWall = w;
                break;
            }
        }

        final Wall finalExistingWall = existingWall;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование стены", true);
        dialog.setLayout(new GridLayout(4, 2, 5, 5));
        dialog.setSize(300, 200);

        JTextField healthField = new JTextField(finalExistingWall != null ? String.valueOf(finalExistingWall.health) : "100");
        JCheckBox destructibleBox = new JCheckBox("Разрушаемая", finalExistingWall == null || finalExistingWall.isDestructible);

        // ===== НОВЫЙ КОМБОБОКС ДЛЯ ТИПА СТЕНЫ =====
        JComboBox<String> wallTypeBox = new JComboBox<>(new String[]{"Brick", "IronBlock"});
        if (finalExistingWall != null && finalExistingWall.wallType != null) {
            wallTypeBox.setSelectedItem(finalExistingWall.wallType);
        }

        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel("Прочность:"));
        dialog.add(healthField);
        dialog.add(new JLabel("Разрушаемая:"));
        dialog.add(destructibleBox);
        dialog.add(new JLabel("Тип стены:"));  // ← НОВОЕ
        dialog.add(wallTypeBox);              // ← НОВОЕ

        JButton addButton = new JButton(finalExistingWall != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            int health = Integer.parseInt(healthField.getText());
            boolean destructible = destructibleBox.isSelected();

            if (finalExistingWall != null) {
                walls.remove(finalExistingWall);
            }
            walls.add(new Wall(finalSelectedCell.x, finalSelectedCell.y, health, destructible));
            dialog.dispose();
            gridPanel.repaint();
        });

        removeButton.addActionListener(e -> {
            if (finalExistingWall != null) {
                walls.remove(finalExistingWall);
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // НОВЫЙ МЕТОД: Диалог для NPC
    private void showNPCDialog() {
        if (selectedCell == null) return;

        // Находим существующего NPC в выбранной клетке
        QuestNPC existingNPC = null;
        for (QuestNPC npc : npcs) {
            if (npc.gridX == selectedCell.x && npc.gridY == selectedCell.y) {
                existingNPC = npc;
                break;
            }
        }

        final QuestNPC finalExistingNPC = existingNPC;
        final Point finalSelectedCell = selectedCell;

        JDialog dialog = new JDialog(this, "Редактирование NPC", true);
        dialog.setLayout(new GridLayout(7, 2, 5, 5));  // ← увеличили до 7 строк
        dialog.setSize(400, 300);  // ← увеличили размер
        dialog.setLocationRelativeTo(this);

        // ===== КОМБОБОКС ДЛЯ ТИПА NPC =====
        JComboBox<String> npcTypeBox = new JComboBox<>(new String[]{"T18", "Sav m/43"});
        if (finalExistingNPC != null) {
            if ("Sav m/43".equals(finalExistingNPC.name)) {
                npcTypeBox.setSelectedItem("Sav m/43");
            } else {
                npcTypeBox.setSelectedItem("T18");
            }
        }

        JTextField nameField = new JTextField(finalExistingNPC != null ? finalExistingNPC.name : "T18");
        JTextField questTargetField = new JTextField(finalExistingNPC != null ? String.valueOf(finalExistingNPC.questTarget) : "5");

        // Тип квеста (зависит от NPC)
        JComboBox<String> questTypeBox = new JComboBox<>(new String[]{"Уничтожить врагов"});
        questTypeBox.setEnabled(false); // Пока только один тип

        // ===== ИНФОРМАЦИЯ О ВЫБРАННОМ NPC =====
        JLabel infoLabel = new JLabel("Изображение: T18.png");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        // Обновляем информацию при смене типа
        npcTypeBox.addActionListener(e -> {
            String selected = (String) npcTypeBox.getSelectedItem();
            if ("Sav m/43".equals(selected)) {
                nameField.setText("Sav m/43");
                questTargetField.setText("15");
                infoLabel.setText("Изображение: Дедушка-Швед.png");
            } else {
                nameField.setText("T18");
                questTargetField.setText("5");
                infoLabel.setText("Изображение: T18.png");
            }
        });

        dialog.add(new JLabel("Тип NPC:"));          // ← НОВОЕ
        dialog.add(npcTypeBox);                     // ← НОВОЕ
        dialog.add(new JLabel("X: " + finalSelectedCell.x + ", Y: " + finalSelectedCell.y));
        dialog.add(new JLabel(""));
        dialog.add(new JLabel("Имя NPC:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Тип квеста:"));
        dialog.add(questTypeBox);
        dialog.add(new JLabel("Цель (кол-во врагов):"));
        dialog.add(questTargetField);
        dialog.add(new JLabel("Изображение:"));
        dialog.add(infoLabel);

        JButton addButton = new JButton(finalExistingNPC != null ? "Обновить" : "Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton cancelButton = new JButton("Отмена");

        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Введите имя NPC!");
                return;
            }

            int questTarget;
            try {
                questTarget = Integer.parseInt(questTargetField.getText().trim());
                if (questTarget < 1) questTarget = 1;
            } catch (NumberFormatException ex) {
                questTarget = 5;
            }

            String npcType = (String) npcTypeBox.getSelectedItem();

            if (finalExistingNPC != null) {
                npcs.remove(finalExistingNPC);
            }

            QuestNPC newNPC;
            if ("Sav m/43".equals(npcType) || "Sav m/43".equals(name)) {
                // ===== СОЗДАЁМ SAV M/43 =====
                newNPC = QuestNPC.createSavM43(finalSelectedCell.x, finalSelectedCell.y);
                // Переопределяем цель, если пользователь изменил
                newNPC.questTarget = questTarget;
                newNPC.questText = "Зачисти район от " + questTarget + " врагов!";
                newNPC.questDescription = "Уничтожь " + questTarget + " противников в этом районе.";
            } else {
                // ===== СОЗДАЁМ ОБЫЧНОГО NPC (T18) =====
                newNPC = new QuestNPC(finalSelectedCell.x, finalSelectedCell.y, name);
                newNPC.questTarget = questTarget;
                newNPC.questText = "Уничтожь " + questTarget + " врагов!";
                newNPC.questDescription = "Уничтожь " + questTarget + " противников на поле боя.";
            }

            npcs.add(newNPC);

            dialog.dispose();
            gridPanel.repaint();
            System.out.println("Добавлен NPC: " + name + " (тип: " + npcType +
                    ") в [" + finalSelectedCell.x + "," + finalSelectedCell.y +
                    "], цель: " + questTarget + " врагов");
        });

        removeButton.addActionListener(e -> {
            if (finalExistingNPC != null) {
                npcs.remove(finalExistingNPC);
                System.out.println("Удалён NPC из [" + finalSelectedCell.x + "," + finalSelectedCell.y + "]");
            }
            dialog.dispose();
            gridPanel.repaint();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(addButton);
        dialog.add(removeButton);
        dialog.add(cancelButton);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    // МЕТОД ДЛЯ МАССОВОЙ ЗАЛИВКИ (ИСПРАВЛЕННАЯ ВЕРСИЯ С АСФАЛЬТОМ)
    private void showFillDialog(Point start, Point end) {
        int minX = Math.min(start.x, end.x);
        int maxX = Math.max(start.x, end.x);
        int minY = Math.min(start.y, end.y);
        int maxY = Math.max(start.y, end.y);

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int totalCells = width * height;

        JDialog dialog = new JDialog(this, "Массовая заливка", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 520);  // Чуть увеличил размер
        dialog.setLocationRelativeTo(this);

        // Панель информации
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(new JLabel("Область:"));
        infoPanel.add(new JLabel("(" + minX + "," + minY + ") → (" + maxX + "," + maxY + ")"));
        infoPanel.add(new JLabel("Размер:"));
        infoPanel.add(new JLabel(width + " x " + height + " = " + totalCells + " клеток"));
        infoPanel.add(new JLabel("Тип объектов:"));
        // Найдите строку с JComboBox типа объектов (примерно строка 1770-1780)
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Стены", "Деревья", "NPC", "Декор", "Союзники", "Вода", "Двери"});
        typeBox.setSelectedItem(getCurrentModeName());
        infoPanel.add(typeBox);

        // ========== ПАНЕЛЬ ДЛЯ СТЕН ==========
        JPanel wallPanel = new JPanel();
        wallPanel.setLayout(new BoxLayout(wallPanel, BoxLayout.Y_AXIS));
        wallPanel.setBorder(BorderFactory.createTitledBorder("Параметры стен"));

        JPanel paramsPanel = new JPanel(new GridLayout(3, 2, 5, 5));  // ← увеличили до 3 строк
        JTextField wallHealthField = new JTextField("100");
        JCheckBox destructibleBox = new JCheckBox("Разрушаемая", true);
        JComboBox<String> wallTypeBox = new JComboBox<>(new String[]{"Brick", "IronBlock"});  // ← НОВОЕ

        paramsPanel.add(new JLabel("Прочность:"));
        paramsPanel.add(wallHealthField);
        paramsPanel.add(new JLabel("Разрушаемая:"));
        paramsPanel.add(destructibleBox);
        paramsPanel.add(new JLabel("Тип стены:"));  // ← НОВОЕ
        paramsPanel.add(wallTypeBox);              // ← НОВОЕ

        wallPanel.add(paramsPanel);

        JPanel presetsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        presetsPanel.setBorder(BorderFactory.createTitledBorder("Быстрые пресеты"));

        JButton lightWallBtn = new JButton("Лёгкая (50)");
        lightWallBtn.addActionListener(e -> {
            wallHealthField.setText("50");
            destructibleBox.setSelected(true);
        });

        JButton mediumWallBtn = new JButton("Средняя (100)");
        mediumWallBtn.addActionListener(e -> {
            wallHealthField.setText("100");
            destructibleBox.setSelected(true);
        });

        JButton heavyWallBtn = new JButton("Тяжёлая (250)");
        heavyWallBtn.addActionListener(e -> {
            wallHealthField.setText("250");
            destructibleBox.setSelected(true);
        });

        JButton indestructibleBtn = new JButton("Неразрушаемая (999)");
        indestructibleBtn.addActionListener(e -> {
            wallHealthField.setText("999");
            destructibleBox.setSelected(false);
        });

        presetsPanel.add(lightWallBtn);
        presetsPanel.add(mediumWallBtn);
        presetsPanel.add(heavyWallBtn);
        presetsPanel.add(indestructibleBtn);
        wallPanel.add(presetsPanel);

        // ========== ПАНЕЛЬ ДЛЯ ДЕРЕВЬЕВ ==========
        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BoxLayout(treePanel, BoxLayout.Y_AXIS));
        treePanel.setBorder(BorderFactory.createTitledBorder("Параметры деревьев"));

        JPanel treeParamsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JComboBox<String> treeTypeBox = new JComboBox<>(new String[]{"Tree1", "Tree2"});
        treeParamsPanel.add(new JLabel("Тип дерева:"));
        treeParamsPanel.add(treeTypeBox);
        treeParamsPanel.add(new JLabel(""));
        treeParamsPanel.add(new JLabel(""));
        treePanel.add(treeParamsPanel);

        // ========== ПАНЕЛЬ ДЛЯ NPC ==========
        JPanel npcPanel = new JPanel();
        npcPanel.setLayout(new BoxLayout(npcPanel, BoxLayout.Y_AXIS));
        npcPanel.setBorder(BorderFactory.createTitledBorder("Параметры NPC"));

        JPanel npcParamsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField npcNameField = new JTextField("T18");
        JTextField questTargetField = new JTextField("5");
        npcParamsPanel.add(new JLabel("Имя NPC:"));
        npcParamsPanel.add(npcNameField);
        npcParamsPanel.add(new JLabel("Цель квеста (кол-во врагов):"));
        npcParamsPanel.add(questTargetField);
        npcParamsPanel.add(new JLabel(""));
        npcParamsPanel.add(new JLabel(""));
        npcPanel.add(npcParamsPanel);

        JPanel decorPanel = new JPanel();
        decorPanel.setLayout(new BoxLayout(decorPanel, BoxLayout.Y_AXIS));
        decorPanel.setBorder(BorderFactory.createTitledBorder("Параметры декоративных элементов"));

        JPanel decorSubPanel = new JPanel(new GridLayout(1, 3, 10, 5));
        JButton pavementPresetBtn = new JButton("Асфальт");
        JButton oakPlanksPresetBtn = new JButton("Дубовые доски");
        JButton infernalLandPresetBtn = new JButton("Адская земля");
        JButton ironFloorPresetBtn = new JButton("Железный пол");

        // По умолчанию выбираем асфальт
        pavementPresetBtn.setBackground(new Color(0, 150, 0));
        oakPlanksPresetBtn.setBackground(null);
        infernalLandPresetBtn.setBackground(null);

        final String[] selectedDecor = {"Асфальт", "Дубовые доски", "Адская земля", "Железный пол"};  // ← 4 элемента

        pavementPresetBtn.addActionListener(ev -> {
            selectedDecor[0] = "Асфальт";
            pavementPresetBtn.setBackground(new Color(0, 150, 0));
            oakPlanksPresetBtn.setBackground(null);
            infernalLandPresetBtn.setBackground(null);
            ironFloorPresetBtn.setBackground(null);
        });

        oakPlanksPresetBtn.addActionListener(ev -> {
            selectedDecor[0] = "Дубовые доски";
            pavementPresetBtn.setBackground(null);
            oakPlanksPresetBtn.setBackground(new Color(0, 150, 0));
            infernalLandPresetBtn.setBackground(null);
            ironFloorPresetBtn.setBackground(null);
        });

        infernalLandPresetBtn.addActionListener(ev -> {
            selectedDecor[0] = "Адская земля";
            pavementPresetBtn.setBackground(null);
            oakPlanksPresetBtn.setBackground(null);
            infernalLandPresetBtn.setBackground(new Color(0, 150, 0));
            ironFloorPresetBtn.setBackground(null);
        });

        ironFloorPresetBtn.addActionListener(ev -> {
            selectedDecor[0] = "Железный пол";
            pavementPresetBtn.setBackground(null);
            oakPlanksPresetBtn.setBackground(null);
            infernalLandPresetBtn.setBackground(null);
            ironFloorPresetBtn.setBackground(new Color(0, 150, 0));
        });

        decorSubPanel.add(pavementPresetBtn);
        decorSubPanel.add(oakPlanksPresetBtn);
        decorSubPanel.add(infernalLandPresetBtn);
        decorSubPanel.add(ironFloorPresetBtn);

        decorPanel.add(decorSubPanel);

        // ========== ПАНЕЛЬ ДЛЯ СОЮЗНИКОВ ==========
        JPanel friendlyPanel = new JPanel();
        friendlyPanel.setLayout(new BoxLayout(friendlyPanel, BoxLayout.Y_AXIS));
        friendlyPanel.setBorder(BorderFactory.createTitledBorder("Параметры союзников"));

        JPanel friendlyParamsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField friendlyNameField = new JTextField("MS-1");
        JComboBox<String> friendlyTypeBox = new JComboBox<>(new String[]{"MS-1", "M53", "VK10001P", "AMX40", "T1"});
        JLabel friendlyStatsLabel = new JLabel("Характеристики: HP:100, Сила:35, Ловк:40");
        friendlyStatsLabel.setFont(new Font("Arial", Font.PLAIN, 9));

        friendlyTypeBox.addActionListener(ev -> {
            if ("M53".equals(friendlyTypeBox.getSelectedItem())) {
                friendlyStatsLabel.setText("Характеристики: HP:220, Сила:60, Ловк:50");
                if (friendlyNameField.getText().equals("MS-1")) {
                    friendlyNameField.setText("M53");
                }
            } else {
                friendlyStatsLabel.setText("Характеристики: HP:100, Сила:35, Ловк:40");
                if (friendlyNameField.getText().equals("M53")) {
                    friendlyNameField.setText("MS-1");
                }
            }
        });

        friendlyParamsPanel.add(new JLabel("Имя союзника:"));
        friendlyParamsPanel.add(friendlyNameField);
        friendlyParamsPanel.add(new JLabel("Тип союзника:"));
        friendlyParamsPanel.add(friendlyTypeBox);
        friendlyParamsPanel.add(new JLabel(""));
        friendlyParamsPanel.add(friendlyStatsLabel);
        friendlyPanel.add(friendlyParamsPanel);

        // Панель с карточками
        JPanel centerPanel = new JPanel(new CardLayout());
        centerPanel.add(wallPanel, "Стены");
        centerPanel.add(treePanel, "Деревья");
        centerPanel.add(npcPanel, "NPC");
        centerPanel.add(decorPanel, "Декор");
        centerPanel.add(friendlyPanel, "Союзники");

        typeBox.addActionListener(e -> {
            CardLayout cl = (CardLayout) centerPanel.getLayout();
            cl.show(centerPanel, (String) typeBox.getSelectedItem());
        });

        // Опции
        JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Опции"));
        JCheckBox overwriteExisting = new JCheckBox("Перезаписывать существующие объекты", true);
        JCheckBox clearAreaFirst = new JCheckBox("Очистить область перед заливкой", false);
        optionsPanel.add(overwriteExisting);
        optionsPanel.add(clearAreaFirst);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(optionsPanel, BorderLayout.SOUTH);

        JButton fillButton = new JButton("Залить");
        JButton cancelButton = new JButton("Отмена");



        JPanel buttonPanel = new JPanel();
        buttonPanel.add(fillButton);
        buttonPanel.add(cancelButton);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        fillButton.addActionListener(e -> {
            String selectedType = (String) typeBox.getSelectedItem();

            // Валидация ввода
            if (selectedType.equals("Стены")) {
                try {
                    int health = Integer.parseInt(wallHealthField.getText().trim());
                    if (health <= 0) {
                        JOptionPane.showMessageDialog(dialog, "Прочность стены должна быть больше 0!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Введите корректное число для прочности стены!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (selectedType.equals("NPC")) {
                try {
                    int questTarget = Integer.parseInt(questTargetField.getText().trim());
                    if (questTarget <= 0) {
                        JOptionPane.showMessageDialog(dialog, "Цель квеста должна быть больше 0!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Введите корректное число для цели квеста!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (npcNameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Введите имя NPC!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Очистка области если нужно
            if (clearAreaFirst.isSelected()) {
                clearArea(minX, minY, maxX, maxY, selectedType);
            }

            int filled = 0;
            int skipped = 0;

            // Заполнение области
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    boolean shouldPlace = true;

                    if (!overwriteExisting.isSelected()) {
                        if (selectedType.equals("Стены") && isWallAt(x, y)) shouldPlace = false;
                        else if (selectedType.equals("Деревья") && isTreeAt(x, y)) shouldPlace = false;
                        else if (selectedType.equals("NPC") && isNPCAt(x, y)) shouldPlace = false;
                        else if (selectedType.equals("Асфальт") && isPavementAt(x, y)) shouldPlace = false;
                        else if (selectedType.equals("Дубовые доски") && isOakPlanksAt(x, y)) shouldPlace = false;
                        else if (selectedType.equals("Железный пол") && isIronFloorAt(x, y)) shouldPlace = false;
                        else if (selectedType.equals("Союзники") && isFriendlyAt(x, y)) shouldPlace = false;  // ← ДОБАВИТЬ
                        else if (selectedType.equals("Вода") && isWaterAt(x, y)) shouldPlace = false;  // ← ДОБАВИТЬ
                    } else {
                        if (selectedType.equals("Стены")) removeWallAt(x, y);
                        else if (selectedType.equals("Деревья")) removeTreeAt(x, y);
                        else if (selectedType.equals("NPC")) removeNPCAt(x, y);
                        else if (selectedType.equals("Асфальт")) removePavementAt(x, y);
                        else if (selectedType.equals("Дубовые доски")) removeOakPlanksAt(x, y);
                        else if (selectedType.equals("Железный пол")) removeIronFloorAt(x, y);
                        else if (selectedType.equals("Союзники")) removeFriendlyAt(x, y);  // ← ДОБАВИТЬ
                        else if (selectedType.equals("Вода")) removeWaterAt(x, y);  // ← ДОБАВИТЬ
                    }

                    if (shouldPlace) {
                        if (selectedType.equals("Стены")) {
                            int health = Integer.parseInt(wallHealthField.getText().trim());
                            boolean destructible = destructibleBox.isSelected();
                            String wallType = (String) wallTypeBox.getSelectedItem();  // ← НОВОЕ
                            walls.add(new Wall(x, y, health, destructible, wallType));
                            filled++;
                        } else if (selectedType.equals("Деревья")) {
                            String treeType = (String) treeTypeBox.getSelectedItem();
                            trees.add(new Tree(x, y, treeType));
                            filled++;
                        } else if (selectedType.equals("NPC")) {
                            String name = npcNameField.getText().trim();
                            int questTarget = Integer.parseInt(questTargetField.getText().trim());
                            QuestNPC npc = new QuestNPC(x, y, name);
                            npc.questTarget = questTarget;
                            npc.questText = "Уничтожь " + questTarget + " врагов!";
                            npc.questDescription = "Уничтожь " + questTarget + " противников на поле боя.";
                            npcs.add(npc);
                            filled++;
                        } else if (selectedType.equals("Декор")) {
                            if (selectedDecor[0].equals("Асфальт")) {
                                pavements.add(new Pavement(x, y));
                                filled++;
                            } else if (selectedDecor[0].equals("Дубовые доски")) {
                                oakPlanks.add(new OakPlanks(x, y));
                                filled++;
                            } else if (selectedDecor[0].equals("Адская земля")) {
                                infernalLands.add(new InfernalLand(x, y));
                                filled++;
                            } else if (selectedDecor[0].equals("Железный пол")) {
                                ironFloors.add(new IronFloor(x, y));
                                filled++;
                            }
                        } else if (selectedType.equals("Союзники")) {
                            String name = friendlyNameField.getText().trim();
                            String type = (String) friendlyTypeBox.getSelectedItem();
                            if (name.isEmpty()) name = (type.equals("M53") ? "M53" : "MS-1");
                            friendlies.add(new FriendlyUnit(x, y, name, type));
                            filled++;
                        } else if (selectedType.equals("Вода")) {
                            waters.add(new Water(x, y));
                            filled++;
                        } else if (selectedType.equals("Двери")) {
                            Door.DoorColor color = Door.DoorColor.RED; // по умолчанию
                            doors.add(new Door(x, y, color));
                            filled++;
                        }
                    } else {
                        skipped++;
                    }
                }
            }

            dialog.dispose();
            gridPanel.repaint();

            // Результат
            String resultMessage = String.format(
                    "✅ Заливка завершена!\n\n" +
                            "📦 Тип: %s\n" +
                            "📍 Область: (%d,%d) - (%d,%d)\n" +
                            "📐 Размер: %d x %d = %d клеток\n" +
                            "✨ Добавлено: %d\n" +
                            "⏭️ Пропущено: %d\n",
                    selectedType, minX, minY, maxX, maxY, width, height, totalCells, filled, skipped
            );

            if (selectedType.equals("Стены")) {
                resultMessage += String.format("\n📊 Параметры стен:\n  • Прочность: %s\n  • Разрушаемые: %s",
                        wallHealthField.getText(), destructibleBox.isSelected() ? "Да" : "Нет");
            } else if (selectedType.equals("Деревья")) {
                resultMessage += String.format("\n📊 Параметры деревьев:\n  • Тип: %s", treeTypeBox.getSelectedItem());
            } else if (selectedType.equals("NPC")) {
                resultMessage += String.format("\n📊 Параметры NPC:\n  • Имя: %s\n  • Цель: %s врагов",
                        npcNameField.getText(), questTargetField.getText());
            } else if (selectedType.equals("Асфальт")) {
                resultMessage += "\n📊 Параметры асфальта:\n  • Декоративный элемент";
            } else if (selectedType.equals("Дубовые доски")) {
                resultMessage += "\n📊 Параметры дубовых досок:\n  • Декоративный элемент";
            } else if (selectedType.equals("ЖЕлезный пол")) {
                resultMessage += "\n📊 Параметры железного пола:\n  • Декоративный элемент";
            }

            JOptionPane.showMessageDialog(this, resultMessage, "Результат", JOptionPane.INFORMATION_MESSAGE);
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private String getCurrentModeName() {
        switch(currentMode) {
            case WALL: return "Стены";
            case NPC: return "NPC";
            case TREE: return "Деревья";
            case DECOR:
                switch(currentDecorType) {
                    case PAVEMENT: return "Асфальт";
                    case OAKPLANKS: return "Дубовые доски";
                    case INFERNALLAND: return "Адская земля";
                    case IRONFLOOR: return "Железный пол";
                    default: return "Декор";
                }
            case FRIENDLY: return "Союзники";
            case STORAGE: return "Тумбочки";
            case TRADER: return "Торговцы";
            case DOOR: return "Двери";
            default: return "Неизвестно";
        }
    }

    private void showClearAreaDialog() {
        JDialog dialog = new JDialog(this, "Очистка области", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(350, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField x1Field = new JTextField("0");
        JTextField y1Field = new JTextField("0");
        JTextField x2Field = new JTextField(String.valueOf(gridWidth - 1));
        JTextField y2Field = new JTextField(String.valueOf(gridHeight - 1));

        panel.add(new JLabel("X1:"));
        panel.add(x1Field);
        panel.add(new JLabel("Y1:"));
        panel.add(y1Field);
        panel.add(new JLabel("X2:"));
        panel.add(x2Field);
        panel.add(new JLabel("Y2:"));
        panel.add(y2Field);

        JPanel typePanel = new JPanel(new GridLayout(2, 2, 5, 5));  // ← УВЕЛИЧИЛ
        typePanel.setBorder(BorderFactory.createTitledBorder("Очищать:"));
        JCheckBox wallsBox = new JCheckBox("Стены", true);
        JCheckBox treesBox = new JCheckBox("Деревья", true);
        JCheckBox npcsBox = new JCheckBox("NPC", true);
        JCheckBox decorBox = new JCheckBox("Декор (асфальт, доски, адская земля, железный пол)", true);
        JCheckBox friendlyBox = new JCheckBox("Союзники", true);
        JCheckBox storageBox = new JCheckBox("Тумбочки", true);
        JCheckBox doorBox = new JCheckBox("Двери", true);
        typePanel.add(wallsBox);
        typePanel.add(treesBox);
        typePanel.add(npcsBox);
        typePanel.add(decorBox);
        typePanel.add(friendlyBox);
        typePanel.add(storageBox);
        typePanel.add(doorBox);

        JButton clearButton = new JButton("Очистить");
        JButton cancelButton = new JButton("Отмена");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(clearButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.NORTH);
        dialog.add(typePanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        clearButton.addActionListener(e -> {
            try {
                int x1 = Integer.parseInt(x1Field.getText());
                int y1 = Integer.parseInt(y1Field.getText());
                int x2 = Integer.parseInt(x2Field.getText());
                int y2 = Integer.parseInt(y2Field.getText());

                int minX = Math.min(x1, x2);
                int maxX = Math.max(x1, x2);
                int minY = Math.min(y1, y2);
                int maxY = Math.max(y1, y2);

                int cleared = 0;

                if (wallsBox.isSelected()) {
                    cleared += clearArea(minX, minY, maxX, maxY, "Стены");
                }
                if (treesBox.isSelected()) {
                    cleared += clearArea(minX, minY, maxX, maxY, "Деревья");
                }
                if (npcsBox.isSelected()) {
                    cleared += clearArea(minX, minY, maxX, maxY, "NPC");
                }
                if (decorBox.isSelected()) {
                    cleared += clearArea(minX, minY, maxX, maxY, "Декор");
                }
                if (friendlyBox.isSelected()) {
                    cleared += clearArea(minX, minY, maxX, maxY, "Союзники");
                }
                if (storageBox.isSelected()) {
                    cleared += clearArea(minX, minY, maxX, maxY, "Тумбочки");
                }
                if (doorBox.isSelected()) {
                    cleared += clearArea(minX, minY, maxX, maxY, "Двери");
                }

                dialog.dispose();
                gridPanel.repaint();

                JOptionPane.showMessageDialog(this,
                        "Очищено объектов: " + cleared,
                        "Очистка области завершена",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Введите корректные числа!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void addWallPresets(JPanel wallPanel, JTextField healthField, JCheckBox destructibleBox) {
        JPanel presetsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        presetsPanel.setBorder(BorderFactory.createTitledBorder("Быстрые пресеты"));

        // Кнопки предустановок
        JButton lightWallBtn = new JButton("Лёгкая стена");
        lightWallBtn.setToolTipText("Прочность: 50, Разрушаемая");
        lightWallBtn.addActionListener(e -> {
            healthField.setText("50");
            destructibleBox.setSelected(true);
        });

        JButton mediumWallBtn = new JButton("Средняя стена");
        mediumWallBtn.setToolTipText("Прочность: 100, Разрушаемая");
        mediumWallBtn.addActionListener(e -> {
            healthField.setText("100");
            destructibleBox.setSelected(true);
        });

        JButton heavyWallBtn = new JButton("Тяжёлая стена");
        heavyWallBtn.setToolTipText("Прочность: 250, Разрушаемая");
        heavyWallBtn.addActionListener(e -> {
            healthField.setText("250");
            destructibleBox.setSelected(true);
        });

        JButton indestructibleBtn = new JButton("Неразрушаемая");
        indestructibleBtn.setToolTipText("Прочность: 999, Неразрушаемая");
        indestructibleBtn.addActionListener(e -> {
            healthField.setText("999");
            destructibleBox.setSelected(false);
        });

        presetsPanel.add(lightWallBtn);
        presetsPanel.add(mediumWallBtn);
        presetsPanel.add(heavyWallBtn);
        presetsPanel.add(indestructibleBtn);

        // Добавляем пресеты в панель стен (в методе showFillDialog)
        // Нужно изменить создание wallPanel, добавив пресеты
    }

    // Обновите методы clearArea и showClearAreaDialog для поддержки асфальта
    private int clearArea(int minX, int minY, int maxX, int maxY, String type) {
        int count = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (type.equals("Стены") && isWallAt(x, y)) {
                    removeWallAt(x, y);
                    count++;
                } else if (type.equals("Деревья") && isTreeAt(x, y)) {
                    removeTreeAt(x, y);
                    count++;
                } else if (type.equals("NPC") && isNPCAt(x, y)) {
                    removeNPCAt(x, y);
                    count++;
                } else if (type.equals("Декор")) {
                    if (isPavementAt(x, y)) {
                        removePavementAt(x, y);
                        count++;
                    } else if (isOakPlanksAt(x, y)) {
                        removeOakPlanksAt(x, y);
                        count++;
                    } else if (isInfernalLandAt(x, y)) {
                        removeInfernalLandAt(x, y);
                        count++;
                    } else if (isIronFloorAt(x, y)) {
                        removeIronFloorAt(x, y);
                        count++;
                    }
                } else if (type.equals("Союзники") && isFriendlyAt(x, y)) {
                    removeFriendlyAt(x, y);
                    count++;
                } else if (type.equals("Тумбочки") && isStorageAt(x, y)) {
                    removeStorageAt(x, y);
                    count++;
                } else if (type.equals("Вода") && isWaterAt(x, y)) {
                    removeWaterAt(x, y);
                    count++;
                } else if (type.equals("Двери") && isDoorAt(x, y)) {
                    removeDoorAt(x, y);
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isFriendlyAt(int x, int y) {
        return friendlies.stream().anyMatch(f -> f.gridX == x && f.gridY == y);
    }

    private void removeFriendlyAt(int x, int y) {
        friendlies.removeIf(f -> f.gridX == x && f.gridY == y);
    }

    private boolean isOakPlanksAt(int x, int y) {
        return oakPlanks.stream().anyMatch(op -> op.gridX == x && op.gridY == y);
    }

    private void removeOakPlanksAt(int x, int y) {
        oakPlanks.removeIf(op -> op.gridX == x && op.gridY == y);
    }

    private boolean isIronFloorAt(int x, int y) {
        return ironFloors.stream().anyMatch(f -> f.gridX == x && f.gridY == y);
    }

    private boolean isDoorAt(int x, int y) {
        return doors.stream().anyMatch(d -> d.gridX == x && d.gridY == y);
    }

    private void removeDoorAt(int x, int y) {
        doors.removeIf(d -> d.gridX == x && d.gridY == y);
    }

    private void removeIronFloorAt(int x, int y) {
        ironFloors.removeIf(f -> f.gridX == x && f.gridY == y);
    }

    // Добавьте методы для асфальта в вспомогательные методы
    private boolean isPavementAt(int x, int y) {
        return pavements.stream().anyMatch(p -> p.gridX == x && p.gridY == y);
    }

    private boolean isInfernalLandAt(int x, int y) {
        return infernalLands.stream().anyMatch(l -> l.gridX == x && l.gridY == y);
    }

    private void removePavementAt(int x, int y) {
        pavements.removeIf(p -> p.gridX == x && p.gridY == y);
    }

    private void removeInfernalLandAt(int x, int y) {
        infernalLands.removeIf(l -> l.gridX == x && l.gridY == y);
    }
    private boolean isWallAt(int x, int y) {
        return walls.stream().anyMatch(w -> w.gridX == x && w.gridY == y);
    }

    private boolean isWaterAt(int x, int y) {
        return waters.stream().anyMatch(w -> w.gridX == x && w.gridY == y);
    }

    private boolean isTreeAt(int x, int y) {
        return trees.stream().anyMatch(t -> t.gridX == x && t.gridY == y);
    }

    private boolean isNPCAt(int x, int y) {
        return npcs.stream().anyMatch(n -> n.gridX == x && n.gridY == y);
    }

    private void removeNPCAt(int x, int y) {
        npcs.removeIf(n -> n.gridX == x && n.gridY == y);
    }

    private void removeTreeAt(int x, int y) {
        trees.removeIf(t -> t.gridX == x && t.gridY == y);
    }

    private void removeWallAt(int x, int y) {
        walls.removeIf(w -> w.gridX == x && w.gridY == y);
    }

    private void removeWaterAt(int x, int y) {
        waters.removeIf(w -> w.gridX == x && w.gridY == y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LevelEditor().setVisible(true));
    }
}