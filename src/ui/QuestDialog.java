package ui;

import entities.QuestNPC;
import audio.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;

public class QuestDialog extends JDialog {

    private SoundManager soundManager;

    // Оригинальный конструктор (для обратной совместимости)
    public QuestDialog(JFrame parent, QuestNPC npc, BufferedImage npcImage, String dialogText) {
        this(parent, npc, npcImage, dialogText, null);
    }

    // Новый конструктор с SoundManager
    public QuestDialog(JFrame parent, QuestNPC npc, BufferedImage npcImage, String dialogText, SoundManager soundManager) {
        super(parent, getDisplayName(npc), true);
        this.soundManager = soundManager;

        setLayout(new BorderLayout());
        setSize(550, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Добавьте обработку Escape
        JRootPane rootPane = getRootPane();
        rootPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // В QuestDialog.java, конструктор уже содержит:

        // Убедитесь, что WindowListener правильно настроен
        // В конструкторе QuestDialog, после setDefaultCloseOperation, добавьте:
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.out.println("QuestDialog закрыт (windowClosed)");
                if (soundManager != null) {
                    soundManager.stopVoiceClip();
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("QuestDialog закрывается (windowClosing)");
                if (soundManager != null) {
                    soundManager.stopVoiceClip();
                }
            }


        });

        // Основная панель с отступами
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(20, 20, 30));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // === ВЕРХНЯЯ ПАНЕЛЬ С ПОРТРЕТОМ И ИМЕНЕМ ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(20, 20, 30));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Портрет
        if (npcImage != null) {
            // Масштабируем портрет до 120x120
            Image scaledImage = npcImage.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            JLabel portraitLabel = new JLabel(new ImageIcon(scaledImage));
            portraitLabel.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
            topPanel.add(portraitLabel, BorderLayout.WEST);
        } else {
            // Запасной вариант - цветной квадрат
            JPanel placeholder = new JPanel();
            placeholder.setPreferredSize(new Dimension(120, 120));
            placeholder.setBackground(new Color(255, 215, 0));
            placeholder.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            JLabel emojiLabel = new JLabel("🎖️", SwingConstants.CENTER);
            emojiLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
            emojiLabel.setForeground(Color.BLACK);
            placeholder.setLayout(new BorderLayout());
            placeholder.add(emojiLabel, BorderLayout.CENTER);
            topPanel.add(placeholder, BorderLayout.WEST);
        }

        // Имя NPC справа от портрета
        JLabel nameLabel = new JLabel(npc.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(new Color(255, 215, 0));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        topPanel.add(nameLabel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // === ЦЕНТРАЛЬНАЯ ПАНЕЛЬ С ТЕКСТОМ ===
        JTextArea textArea = new JTextArea();
        textArea.setText(dialogText);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(30, 30, 40));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100), 1));
        scrollPane.setBackground(new Color(30, 30, 40));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // === НИЖНЯЯ ПАНЕЛЬ С КНОПКОЙ ===
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(20, 20, 30));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton closeButton = new JButton("Закрыть");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.setBackground(new Color(80, 80, 100));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> {
            stopCurrentSound();
            dispose();
        });

        bottomPanel.add(closeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // ===== ДОБАВЬТЕ ЭТОТ МЕТОД =====
    private static String getDisplayName(QuestNPC npc) {
        if (npc == null) return "NPC";
        if (npc.name == null || npc.name.trim().isEmpty()) {
            return "T18";  // Значение по умолчанию
        }
        return npc.name;
    }


    private boolean isStopping = false;

    private void stopCurrentSound() {
        if (isStopping) return;
        isStopping = true;
        try {
            if (soundManager != null) {
                System.out.println("Останавливаем звук в QuestDialog");
                soundManager.stopVoiceClip();
            }
        } finally {
            isStopping = false;
        }
    }
}