package entities;

import audio.SoundManager;
import javax.sound.sampled.Clip;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuestNPC {
    public int gridX, gridY;
    public String name;
    public String questText;
    public String rewardDescription;
    public boolean isQuestCompleted = false;
    public boolean hasTalked = false;
    public boolean isQuestFinished = false;

    private boolean soundDisabled = false;

    // Звуки для NPC
    private Clip questReceivedSound;
    private Clip questNotCompletedSound;
    private Clip questDoneSound;

    private Clip questDone1Sound;  // Для Leichttraktor
    private Clip questDone2Sound;  // Для Дедушки-Шведа (второй)

    // Состояние взаимодействия
    public boolean hasReceivedQuest = false;

    // Добавляем SoundManager
    private transient SoundManager soundManager;

    public enum QuestType {
        KILL_ENEMIES,
        REACH_LOCATION,
        COLLECT_ITEM,
        CLEAR_AREA,
        CLEAR_SPECIFIC_FACTIONS
    }

    // ===== ИСПРАВЛЕНИЕ: используем java.util.List =====
    private List<Faction> targetFactions = new ArrayList<>();

    public QuestType questType;
    public int questTarget;
    public int questProgress;
    public String questDescription;

    public QuestNPC(int x, int y, String name) {
        this(x, y, name, null);
    }

    public QuestNPC(int x, int y, String name, SoundManager soundManager) {
        this.gridX = x;
        this.gridY = y;
        this.name = name;
        this.soundManager = soundManager;

        this.questType = QuestType.KILL_ENEMIES;
        this.questTarget = 5;
        this.questProgress = 0;
        this.questText = "Уничтожь " + questTarget + " врагов!";
        this.rewardDescription = "Опыт +50 и улучшенные снаряды!";
        this.questDescription = "Уничтожь " + questTarget + " противников на поле боя.";

        loadSounds();
    }

    public void setSoundManager(SoundManager sm) {
        this.soundManager = sm;
    }

    private void loadSounds() {
        String basePath = "src/QuestPersons/T18/";

        System.out.println("=== ЗАГРУЗКА ЗВУКОВ ДЛЯ T18 ===");
        System.out.println("Базовая папка: " + basePath);

        String receivedPath = basePath + "QuestReceived/Received.wav";
        questReceivedSound = loadSound(receivedPath);

        String notCompletedPath = basePath + "QuestNotCompleted/NotCompleted.wav";
        questNotCompletedSound = loadSound(notCompletedPath);

        String donePath = basePath + "QuestDone/Done.wav";
        questDoneSound = loadSound(donePath);

        System.out.println("Результат загрузки: Received=" + (questReceivedSound != null) +
                ", NotCompleted=" + (questNotCompletedSound != null) +
                ", Done=" + (questDoneSound != null));
    }

    private Clip loadSound(String fullPath) {
        try {
            File soundFile = new File(fullPath);
            System.out.println("  Файл существует: " + soundFile.exists() + " - " + fullPath);
            if (soundFile.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                System.out.println("  ✅ Звук загружен: " + soundFile.getName());
                return clip;
            } else {
                System.err.println("  ❌ Файл НЕ НАЙДЕН: " + fullPath);
            }
        } catch (Exception e) {
            System.err.println("  ❌ Ошибка загрузки звука: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void playQuestSound() {
        if (soundDisabled) {
            System.out.println("Звук отключён для " + name);
            return;
        }

        Clip soundToPlay = null;

        if (isQuestCompleted) {
            soundToPlay = questDoneSound;
        } else if (hasReceivedQuest) {
            soundToPlay = questNotCompletedSound;
        } else {
            soundToPlay = questReceivedSound;
        }

        if (soundToPlay != null) {
            if (soundManager != null) {
                soundManager.playCustomClip(soundToPlay);
            } else {
                try {
                    if (soundToPlay.isRunning()) soundToPlay.stop();
                    soundToPlay.setFramePosition(0);
                    soundToPlay.start();
                } catch (Exception e) {}
            }
        }
    }

    public void stopQuestSound() {
        if (soundManager != null) {
            soundManager.stopVoiceClip();
        } else {
            if (questReceivedSound != null && questReceivedSound.isRunning()) {
                questReceivedSound.stop();
                questReceivedSound.setFramePosition(0);
            }
            if (questNotCompletedSound != null && questNotCompletedSound.isRunning()) {
                questNotCompletedSound.stop();
                questNotCompletedSound.setFramePosition(0);
            }
            if (questDoneSound != null && questDoneSound.isRunning()) {
                questDoneSound.stop();
                questDoneSound.setFramePosition(0);
            }
        }
    }

    public void setQuestReceived() {
        this.hasReceivedQuest = true;
    }

    public boolean hasReceivedQuest() {
        return hasReceivedQuest;
    }

    public void updateProgress(int enemiesKilled) {
        if (!isQuestCompleted) {
            questProgress = Math.min(questTarget, enemiesKilled);
            if (questProgress >= questTarget) {
                isQuestCompleted = true;
                System.out.println(name + ": Квест ВЫПОЛНЕН!");
            }
        }
    }

    public String getStatusText() {
        if (isQuestCompleted) {
            return "✓ Квест выполнен! Подойди для награды!";
        }
        return "Прогресс: " + questProgress + "/" + questTarget;
    }

    public void draw(Graphics2D g, int cameraX, int cameraY, int cellSize, int tankSize,
                     BufferedImage npcImage, PlayerTank player) {
        int x = gridX * cellSize - cameraX;
        int y = gridY * cellSize - cameraY;

        boolean isAdjacent = false;
        if (player != null) {
            int dx = Math.abs(player.gridX - gridX);
            int dy = Math.abs(player.gridY - gridY);
            isAdjacent = (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
        }

        if (npcImage != null) {
            g.drawImage(npcImage, x, y, tankSize, tankSize, null);
        } else {
            g.setColor(new Color(255, 215, 0));
            g.fillRoundRect(x, y, tankSize, tankSize, 10, 10);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("!", x + tankSize/2 - 5, y + tankSize/2 + 5);
        }

        if (isAdjacent) {
            g.setColor(new Color(0, 255, 0, 100));
            g.fillRoundRect(x, y, tankSize, tankSize, 10, 10);
        }

        if (!isQuestCompleted && !hasReceivedQuest) {
            g.setColor(new Color(255, 100, 0, 220));
            g.fillOval(x + tankSize - 12, y - 5, 14, 14);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("!", x + tankSize - 8, y + 3);
        } else if (!isQuestCompleted && hasReceivedQuest) {
            g.setColor(new Color(255, 200, 0, 220));
            g.fillOval(x + tankSize - 12, y - 5, 14, 14);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("⏳", x + tankSize - 9, y + 3);
        } else if (isQuestCompleted) {
            g.setColor(new Color(0, 200, 0, 220));
            g.fillOval(x + tankSize - 12, y - 5, 14, 14);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("✓", x + tankSize - 9, y + 3);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString(name, x + 5, y - 8);
    }

    public static QuestNPC createSavM43(int x, int y, SoundManager soundManager) {
        QuestNPC npc = new QuestNPC(x, y, "Sav m/43");
        if (soundManager != null) {
            npc.setSoundManager(soundManager);
        }
        npc.questType = QuestType.CLEAR_SPECIFIC_FACTIONS;
        npc.questTarget = 0;
        npc.questProgress = 0;
        npc.questText = "Очисти этот район от французских и итальянских захватчиков!";
        npc.questDescription = "Уничтожь всех врагов из Франции и Италии.";
        npc.rewardDescription = "Шведский арсенал + 750 опыта!";
        npc.isQuestFinished = false;

        // ===== УСТАНАВЛИВАЕМ ЦЕЛЕВЫЕ ФРАКЦИИ =====
        npc.targetFactions.add(Faction.FRANCE);
        npc.targetFactions.add(Faction.ITALY);

        npc.loadSavM43Sounds();
        return npc;
    }

    public boolean isQuestCompletedForFactions(List<Enemy> enemies) {
        if (questType != QuestType.CLEAR_SPECIFIC_FACTIONS) {
            return isQuestCompleted;
        }

        for (Enemy enemy : enemies) {
            if (enemy.isAlive && targetFactions.contains(enemy.getFaction())) {
                return false;
            }
        }
        return true;
    }

    // В QuestNPC.java, добавьте метод:

    public void revalidateQuestStatus(List<Enemy> enemies) {
        if (!"Sav m/43".equals(this.name)) {
            return; // Только для Дедушки-Шведа
        }

        if (this.isQuestFinished) {
            return; // Квест уже полностью завершён
        }

        // Проверяем, есть ли живые враги из целевых фракций
        boolean completed = this.isQuestCompletedForFactions(enemies);

        if (completed) {
            this.isQuestCompleted = true;
            System.out.println("🔄 Sav m/43: квест перепроверен - ВЫПОЛНЕН!");
        } else {
            this.isQuestCompleted = false;
            // Подсчитываем, сколько врагов осталось
            int remaining = 0;
            for (Enemy enemy : enemies) {
                if (enemy.isAlive && targetFactions.contains(enemy.getFaction())) {
                    remaining++;
                }
            }
            System.out.println("🔄 Sav m/43: квест НЕ выполнен, осталось врагов: " + remaining);
        }
    }

    public static QuestNPC createSavM43(int x, int y) {
        return createSavM43(x, y, null);
    }

    private void loadSavM43Sounds() {
        String basePath = "src/QuestPersons/Дедушка-Швед/";

        System.out.println("=== ЗАГРУЗКА ЗВУКОВ ДЛЯ SAV M/43 ===");
        System.out.println("Базовая папка: " + basePath);

        String receivedPath = basePath + "QuestReceived/Received.wav";
        questReceivedSound = loadSound(receivedPath);

        String notCompletedPath = basePath + "QuestNotCompleted/NotCompleted.wav";
        questNotCompletedSound = loadSound(notCompletedPath);

        // ===== ИСПРАВЛЕНИЕ: было questNotCompletedSound, должно быть questDoneSound =====
        String donePath = basePath + "QuestDone/Done.wav";
        questDoneSound = loadSound(donePath);  // ← ИСПРАВЛЕНО!

        String done1Path = basePath + "QuestDone/Done1.wav";
        File done1File = new File(done1Path);
        if (done1File.exists()) {
            questDone1Sound = loadSound(done1Path);
            System.out.println("✅ Загружен звук Done1.wav для Leichttraktor");
        } else {
            System.err.println("❌ Файл Done1.wav не найден: " + done1Path);
        }

        // Загружаем Done2.wav (для Дедушки-Шведа второй)
        String done2Path = basePath + "QuestDone/Done2.wav";
        File done2File = new File(done2Path);
        if (done2File.exists()) {
            questDone2Sound = loadSound(done2Path);
            System.out.println("✅ Загружен звук Done2.wav для Дедушки-Шведа (второй)");
        } else {
            System.err.println("❌ Файл Done2.wav не найден: " + done2Path);
        }
    }

    public void playCustomSound(String soundFileName) {
        if (soundDisabled) return;

        try {
            String basePath = "src/QuestPersons/Дедушка-Швед/";
            String fullPath = basePath + soundFileName;
            File soundFile = new File(fullPath);

            if (soundFile.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);

                if (soundManager != null) {
                    soundManager.playCustomClip(clip);
                } else {
                    clip.start();
                }
                System.out.println("🔊 Воспроизведение звука: " + soundFileName);
            } else {
                System.err.println("❌ Файл звука не найден: " + fullPath);
            }
        } catch (Exception e) {
            System.err.println("Ошибка воспроизведения звука: " + e.getMessage());
        }
    }

    public void disableSound() {
        this.soundDisabled = true;
        if (soundManager != null) {
            soundManager.stopVoiceClip();
        }
    }

    public Clip getQuestReceivedSound() { return questReceivedSound; }
    public Clip getQuestNotCompletedSound() { return questNotCompletedSound; }
    public Clip getQuestDoneSound() { return questDoneSound; }

    public Clip getQuestDone1Sound() { return questDone1Sound; }
    public Clip getQuestDone2Sound() { return questDone2Sound; }
}