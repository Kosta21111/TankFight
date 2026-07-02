package ui;

import javax.swing.*;

public class GameFrame extends JFrame {
    public GameFrame() {
        setTitle("Leichttraktor - Turn-Based Strategy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GamePanel panel = new GamePanel(this);
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Добавьте эти строки:
        addKeyListener(panel);
        panel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}