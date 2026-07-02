package audio;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

import audio.MusicManager;

import inventory.Caliber;

public class SoundManager {
    private Clip moveSoundClip;
    private Clip enemyMoveSoundClip;
    private Clip shootSoundClip;
    private Clip[] voiceClips = new Clip[3];
    private List<Clip> fewEnemiesClips = new ArrayList<>();
    private List<Clip> tooMuchEnemiesClips = new ArrayList<>();
    private List<Clip> enemyDestroyedClips = new ArrayList<>();
    private Random randomVoice = new Random();
    private boolean isVoicePlaying = false;

    private Map<String, Clip> enemyShootSoundCache = new HashMap<>();

    // Клипы для M53
    private Clip[] m53VoiceClips = new Clip[3];
    private Clip[] m53EnemyDestroyedClips = new Clip[3];
    private List<Clip> m53FewEnemiesClips = new ArrayList<>();
    private List<Clip> m53TooMuchEnemiesClips = new ArrayList<>();
    private List<Clip> poisonedClips = new ArrayList<>();

    private Clip[] ms1VoiceClips = new Clip[3];
    private Clip[] ms1EnemyDestroyedClips = new Clip[3];
    private List<Clip> ms1FewEnemiesClips = new ArrayList<>();
    private List<Clip> ms1TooMuchEnemiesClips = new ArrayList<>();
    private List<Clip> ms1PoisonedClips = new ArrayList<>();
    private List<Clip> m53PoisonedClips = new ArrayList<>();

    // В SoundManager.java, добавьте новые поля для VK 100.01 P:
    private Clip[] vk10001pVoiceClips = new Clip[3];
    private Clip[] vk10001pEnemyDestroyedClips = new Clip[3];
    private List<Clip> vk10001pFewEnemiesClips = new ArrayList<>();
    private List<Clip> vk10001pTooMuchEnemiesClips = new ArrayList<>();
    private List<Clip> vk10001pPoisonedClips = new ArrayList<>();

    private List<Clip> t1FewEnemiesClips = new ArrayList<>();
    private List<Clip> t1TooMuchEnemiesClips = new ArrayList<>();
    private Clip[] t1VoiceClips = new Clip[3];
    private List<Clip> t1PoisonedClips = new ArrayList<>();
    private Clip[] t1EnemyDestroyedClips = new Clip[3];

    private List<Clip> enemyShootClips = new ArrayList<>();
    private double currentCaliber = 0.02;

    // ===== ПОЛЯ ДЛЯ УПРАВЛЕНИЯ ВОСПРОИЗВЕДЕНИЕМ =====
    private Clip currentPlayingClip = null;
    private boolean shouldStopCurrentPlayback = false;
    private MusicManager musicManager;

    // ========== МЕТОДЫ ЗАГРУЗКИ (оставляем без изменений) ==========

    // ===== ДОБАВЬТЕ ЭТОТ КОНСТРУКТОР =====
    public SoundManager() {
        // Создаём MusicManager
        this.musicManager = new MusicManager();
        System.out.println("✅ SoundManager: MusicManager создан");
    }

    public void loadEnemyShootSound(double caliber) {
        String folderPath = getShootSoundPath(caliber);
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Папка со звуками выстрелов не найдена: " + folderPath);
            return;
        }
        File[] soundFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
        if (soundFiles != null && soundFiles.length > 0) {
            try {
                String cacheKey = folderPath;
                if (!enemyShootSoundCache.containsKey(cacheKey)) {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFiles[0]);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    enemyShootSoundCache.put(cacheKey, clip);
                    System.out.println("Загружен звук выстрела врага из: " + soundFiles[0].getName() +
                            " (категория: " + getCategoryName(caliber) + ")");
                }
            } catch (Exception e) {
                System.err.println("Ошибка загрузки звука выстрела врага: " + e.getMessage());
            }
        }
    }

    private String getShootSoundPath(double caliber) {
        String basePath = "src/SoundEffects/Shooting/";
        String category = getCategoryName(caliber);
        return basePath + category + "/";
    }

    private String getCategoryName(double caliber) {
        if (caliber <= 0.03) return "TinyCaliber";
        else if (caliber <= 0.08) return "SmallCaliber";
        else if (caliber <= 0.1) return "MediumCaliber";
        else if (caliber <= 0.14) return "BigCaliber";
        else return "LargeCaliber";
    }

    public void playEnemyShootSound(double caliber) {
        String folderPath = getShootSoundPath(caliber);
        Clip clip = enemyShootSoundCache.get(folderPath);
        if (clip == null) {
            loadEnemyShootSound(caliber);
            clip = enemyShootSoundCache.get(folderPath);
            if (clip == null) {
                System.err.println("Не удалось загрузить звук выстрела для калибра: " + caliber);
                return;
            }
        }
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void preloadEnemyShootSounds() {
        double[] calibers = {0.008, 0.013, 0.02, 0.03, 0.045, 0.076, 0.128, 0.203};
        for (double caliber : calibers) {
            loadEnemyShootSound(caliber);
        }
    }

    public void loadMoveSound() {
        try {
            String soundPath = "src/SoundEffects/Moving/";
            File soundFile = findSoundFile(soundPath);
            if (soundFile != null && soundFile.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                moveSoundClip = AudioSystem.getClip();
                moveSoundClip.open(audioInputStream);
                System.out.println("Звук движения успешно загружен: " + soundFile.getName());
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки звука движения: " + e.getMessage());
        }
    }

    public void loadEnemyMoveSound() {
        try {
            String enemySoundPath = "src/Enemies/Leichttraktor/MovementSound/";
            File soundFile = findSoundFile(enemySoundPath);
            if (soundFile != null && soundFile.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                enemyMoveSoundClip = AudioSystem.getClip();
                enemyMoveSoundClip.open(audioInputStream);
                System.out.println("Звук движения врага успешно загружен: " + soundFile.getName());
            } else {
                System.err.println("Звуковой файл движения врага не найден: " + enemySoundPath);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки звука движения врага: " + e.getMessage());
        }
    }

    public void loadM53Sounds() {
        String basePath = "src/SoundEffects/Speaking/M53/";
        String callingPath = basePath + "Calling/";
        for (int i = 1; i <= 3; i++) {
            File voiceFile = new File(callingPath + "Вариант " + i + ".wav");
            if (!voiceFile.exists()) voiceFile = new File(callingPath + "Вариант " + i + ".mp3");
            if (voiceFile.exists()) {
                try {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(voiceFile);
                    m53VoiceClips[i-1] = AudioSystem.getClip();
                    m53VoiceClips[i-1].open(audioInputStream);
                    System.out.println("✅ Загружен голос M53: Вариант " + i);
                } catch (Exception e) {
                    System.err.println("Ошибка загрузки голоса M53: " + e.getMessage());
                }
            }
        }
        String destroyedPath = basePath + "EnemyIsDestroyed/";
        loadM53SoundsFromFolder(destroyedPath, m53EnemyDestroyedClips);
        String fewEnemiesPath = basePath + "Spotting/FewEnemies/";
        loadM53SoundsToList(fewEnemiesPath, m53FewEnemiesClips);
        String tooMuchEnemiesPath = basePath + "Spotting/TooMuchEnemies/";
        loadM53SoundsToList(tooMuchEnemiesPath, m53TooMuchEnemiesClips);
        String poisonedPath = basePath + "Poisoned/";
        loadM53SoundsToList(poisonedPath, m53PoisonedClips);
    }

    private void loadM53SoundsFromFolder(String folderPath, Clip[] targetArray) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return;
        File[] soundFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
        if (soundFiles == null) return;
        for (int i = 0; i < soundFiles.length && i < targetArray.length; i++) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFiles[i]);
                targetArray[i] = AudioSystem.getClip();
                targetArray[i].open(audioInputStream);
            } catch (Exception e) {
                System.err.println("Ошибка загрузки " + soundFiles[i].getName() + ": " + e.getMessage());
            }
        }
    }

    private void loadM53SoundsToList(String folderPath, List<Clip> targetList) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return;
        File[] soundFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
        if (soundFiles == null) return;
        for (File soundFile : soundFiles) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                targetList.add(clip);
            } catch (Exception e) {
                System.err.println("Ошибка загрузки " + soundFile.getName() + ": " + e.getMessage());
            }
        }
    }

    public void loadMS1Sounds() {
        String basePath = "src/SoundEffects/Speaking/MS-1/";
        String callingPath = basePath + "Calling/";
        for (int i = 1; i <= 3; i++) {
            File voiceFile = new File(callingPath + "Вариант " + i + ".wav");
            if (!voiceFile.exists()) voiceFile = new File(callingPath + "Вариант " + i + ".mp3");
            if (voiceFile.exists()) {
                try {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(voiceFile);
                    ms1VoiceClips[i-1] = AudioSystem.getClip();
                    ms1VoiceClips[i-1].open(audioInputStream);
                    System.out.println("✅ Загружен голос MС-1: Вариант " + i);
                } catch (Exception e) {
                    System.err.println("Ошибка загрузки голоса MС-1: " + e.getMessage());
                }
            }
        }
        String destroyedPath = basePath + "EnemyIsDestroyed/";
        loadMS1SoundsFromFolder(destroyedPath, ms1EnemyDestroyedClips);
        String fewEnemiesPath = basePath + "Spotting/FewEnemies/";
        loadMS1SoundsToList(fewEnemiesPath, ms1FewEnemiesClips);
        String tooMuchEnemiesPath = basePath + "Spotting/TooMuchEnemies/";
        loadMS1SoundsToList(tooMuchEnemiesPath, ms1TooMuchEnemiesClips);
        String poisonedPath = basePath + "Poisoned/";
        loadMS1SoundsToList(poisonedPath, ms1PoisonedClips);
    }

    private void loadMS1SoundsFromFolder(String folderPath, Clip[] targetArray) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return;
        File[] soundFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
        if (soundFiles == null) return;
        for (int i = 0; i < soundFiles.length && i < targetArray.length; i++) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFiles[i]);
                targetArray[i] = AudioSystem.getClip();
                targetArray[i].open(audioInputStream);
            } catch (Exception e) {
                System.err.println("Ошибка загрузки " + soundFiles[i].getName() + ": " + e.getMessage());
            }
        }
    }

    private void loadMS1SoundsToList(String folderPath, List<Clip> targetList) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return;
        File[] soundFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
        if (soundFiles == null) return;
        for (File soundFile : soundFiles) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                targetList.add(clip);
            } catch (Exception e) {
                System.err.println("Ошибка загрузки " + soundFile.getName() + ": " + e.getMessage());
            }
        }
    }

    public void loadT1Sounds() {
        String basePath = "src/SoundEffects/Speaking/T1/";
        String callingPath = basePath + "Calling/";
        for (int i = 1; i <= 3; i++) {
            File voiceFile = new File(callingPath + "Вариант " + i + ".wav");
            if (!voiceFile.exists()) voiceFile = new File(callingPath + "Вариант " + i + ".mp3");
            if (voiceFile.exists()) {
                try {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(voiceFile);
                    t1VoiceClips[i-1] = AudioSystem.getClip();
                    t1VoiceClips[i-1].open(audioInputStream);
                    System.out.println("✅ Загружен голос T1: Вариант " + i);
                } catch (Exception e) {
                    System.err.println("Ошибка загрузки голоса T1: " + e.getMessage());
                }
            }
        }
        String destroyedPath = basePath + "EnemyIsDestroyed/";
        loadT1SoundsFromFolder(destroyedPath, t1EnemyDestroyedClips);
        String fewEnemiesPath = basePath + "Spotting/FewEnemies/";
        loadT1SoundsToList(fewEnemiesPath, t1FewEnemiesClips);
        String tooMuchEnemiesPath = basePath + "Spotting/TooMuchEnemies/";
        loadT1SoundsToList(tooMuchEnemiesPath, t1TooMuchEnemiesClips);
        String poisonedPath = basePath + "Poisoned/";
        loadT1SoundsToList(poisonedPath, t1PoisonedClips);
    }

    private void loadT1SoundsFromFolder(String folderPath, Clip[] targetArray) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return;
        File[] soundFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
        if (soundFiles == null) return;
        for (int i = 0; i < soundFiles.length && i < targetArray.length; i++) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFiles[i]);
                targetArray[i] = AudioSystem.getClip();
                targetArray[i].open(audioInputStream);
            } catch (Exception e) {
                System.err.println("Ошибка загрузки " + soundFiles[i].getName() + ": " + e.getMessage());
            }
        }
    }

    private void loadT1SoundsToList(String folderPath, List<Clip> targetList) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return;
        File[] soundFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
        if (soundFiles == null) return;
        for (File soundFile : soundFiles) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                targetList.add(clip);
            } catch (Exception e) {
                System.err.println("Ошибка загрузки " + soundFile.getName() + ": " + e.getMessage());
            }
        }
    }

    public void loadVK10001PSounds() {
        String basePath = "src/SoundEffects/Speaking/VK_100_01_P/";
        String callingPath = basePath + "Calling/";
        for (int i = 1; i <= 3; i++) {
            File voiceFile = new File(callingPath + "Вариант " + i + ".wav");
            if (!voiceFile.exists()) voiceFile = new File(callingPath + "Вариант " + i + ".mp3");
            if (voiceFile.exists()) {
                try {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(voiceFile);
                    vk10001pVoiceClips[i-1] = AudioSystem.getClip();
                    vk10001pVoiceClips[i-1].open(audioInputStream);
                    System.out.println("✅ Загружен голос VK 100.01 P: Вариант " + i);
                } catch (Exception e) {
                    System.err.println("Ошибка загрузки голоса VK 100.01 P: " + e.getMessage());
                }
            }
        }
        String destroyedPath = basePath + "EnemyIsDestroyed/";
        loadVK10001PSoundsFromFolder(destroyedPath, vk10001pEnemyDestroyedClips);
        String fewEnemiesPath = basePath + "Spotting/FewEnemies/";
        loadVK10001PSoundsToList(fewEnemiesPath, vk10001pFewEnemiesClips);
        String tooMuchEnemiesPath = basePath + "Spotting/TooMuchEnemies/";
        loadVK10001PSoundsToList(tooMuchEnemiesPath, vk10001pTooMuchEnemiesClips);
        String poisonedPath = basePath + "Poisoned/";
        loadVK10001PSoundsToList(poisonedPath, vk10001pPoisonedClips);
    }

    private void loadVK10001PSoundsFromFolder(String folderPath, Clip[] targetArray) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return;
        File[] soundFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
        if (soundFiles == null) return;
        for (int i = 0; i < soundFiles.length && i < targetArray.length; i++) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFiles[i]);
                targetArray[i] = AudioSystem.getClip();
                targetArray[i].open(audioInputStream);
            } catch (Exception e) {
                System.err.println("Ошибка загрузки " + soundFiles[i].getName() + ": " + e.getMessage());
            }
        }
    }

    private void loadVK10001PSoundsToList(String folderPath, List<Clip> targetList) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return;
        File[] soundFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
        if (soundFiles == null) return;
        for (File soundFile : soundFiles) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                targetList.add(clip);
            } catch (Exception e) {
                System.err.println("Ошибка загрузки " + soundFile.getName() + ": " + e.getMessage());
            }
        }
    }

    private void loadSoundsFromFolder(String folderPath, List<Clip> targetList) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return;
        File[] soundFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
        if (soundFiles == null) return;
        for (File soundFile : soundFiles) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                targetList.add(clip);
            } catch (Exception e) {
                System.err.println("Ошибка загрузки " + soundFile.getName() + ": " + e.getMessage());
            }
        }
    }

    private int countNonNull(Clip[] clips) {
        int count = 0;
        for (Clip clip : clips) {
            if (clip != null) count++;
        }
        return count;
    }

    public void loadEnemyDestroyedSounds() {
        String destroyedPath = "src/SoundEffects/Speaking/Leichttraktor/EnemyIsDestroyed/";
        loadSoundsFromFolder(destroyedPath, enemyDestroyedClips);
        System.out.println("Загружено звуков уничтожения врага: " + enemyDestroyedClips.size());
    }

    public void loadVoiceSound() {
        try {
            String voicePath = "src/SoundEffects/Speaking/Leichttraktor/Calling/";
            String[] voiceFiles = {"Вариант 1.wav", "Вариант 2.wav", "Вариант 3.wav"};
            for (int i = 0; i < voiceFiles.length; i++) {
                File voiceFile = new File(voicePath + voiceFiles[i]);
                if (!voiceFile.exists()) {
                    voiceFile = new File(voicePath + voiceFiles[i].replace(".wav", ".mp3"));
                }
                if (voiceFile.exists()) {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(voiceFile);
                    voiceClips[i] = AudioSystem.getClip();
                    voiceClips[i].open(audioInputStream);
                    System.out.println("Загружен голосовой файл: " + voiceFiles[i]);
                } else {
                    voiceClips[i] = null;
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки голосовых звуков: " + e.getMessage());
        }
    }

    public void loadSpottingSounds() {
        String fewEnemiesPath = "src/SoundEffects/Speaking/Leichttraktor/Spotting/FewEnemies/";
        loadSoundsFromFolder(fewEnemiesPath, fewEnemiesClips);
        String tooMuchEnemiesPath = "src/SoundEffects/Speaking/Leichttraktor/Spotting/TooMuchEnemies/";
        loadSoundsFromFolder(tooMuchEnemiesPath, tooMuchEnemiesClips);
        System.out.println("Загружено фраз обнаружения (1-2 врага): " + fewEnemiesClips.size());
        System.out.println("Загружено фраз обнаружения (3+ врагов): " + tooMuchEnemiesClips.size());
    }

    public void loadPoisonedSounds() {
        String poisonedPath = "src/SoundEffects/Speaking/Leichttraktor/Poisoned/";
        loadSoundsFromFolder(poisonedPath, poisonedClips);
        System.out.println("Загружено звуков отравления: " + poisonedClips.size());
    }

    // ========== ВОСПРОИЗВЕДЕНИЕ (исправленные методы) ==========

    // ГЛАВНЫЙ МЕТОД ВОСПРОИЗВЕДЕНИЯ
    private void playVoiceClip(Clip clip) {
        if (clip == null) return;

        // Останавливаем текущее воспроизведение
        stopVoiceClip();

        currentPlayingClip = clip;
        shouldStopCurrentPlayback = false;

        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);

        // Добавляем слушатель для отслеживания окончания
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                if (!shouldStopCurrentPlayback && currentPlayingClip == clip) {
                    isVoicePlaying = false;
                    currentPlayingClip = null;
                    System.out.println("✅ Воспроизведение завершено естественно");
                }
            }
        });

        clip.start();
        isVoicePlaying = true;
        System.out.println("🔊 Начато воспроизведение");
    }

    // В SoundManager.java, в метод stopVoiceClip(), добавьте:
    public void stopVoiceClip() {
        System.out.println("🔇 SoundManager.stopVoiceClip() вызван");

        shouldStopCurrentPlayback = true;

        // Останавливаем currentPlayingClip
        if (currentPlayingClip != null) {
            try {
                if (currentPlayingClip.isRunning()) {
                    currentPlayingClip.stop();
                    System.out.println("  Остановлен currentPlayingClip");
                }
                currentPlayingClip.setFramePosition(0);
                currentPlayingClip.close(); // Закрываем клип полностью
            } catch (Exception e) {
                System.err.println("Ошибка при остановке currentPlayingClip: " + e.getMessage());
            }
            currentPlayingClip = null;
        }

        // Принудительно останавливаем все клипы
        stopAllClips();

        isVoicePlaying = false;
        System.out.println("🔇 Все звуки остановлены, isVoicePlaying = false");
    }

    // Добавьте в SoundManager.java
    public void stopClip(Clip clip) {
        if (clip != null) {
            try {
                if (clip.isRunning()) {
                    clip.stop();
                    System.out.println("  Принудительно остановлен клип");
                }
                clip.setFramePosition(0);
            } catch (Exception e) {
                System.err.println("Ошибка при остановке клипа: " + e.getMessage());
            }
        }
    }

    private void stopAllClips() {
        stopClipIfRunning(moveSoundClip);
        stopClipIfRunning(enemyMoveSoundClip);
        stopClipIfRunning(shootSoundClip);

        for (Clip clip : voiceClips) stopClipIfRunning(clip);
        for (Clip clip : m53VoiceClips) stopClipIfRunning(clip);
        for (Clip clip : ms1VoiceClips) stopClipIfRunning(clip);
        for (Clip clip : vk10001pVoiceClips) stopClipIfRunning(clip);
        for (Clip clip : fewEnemiesClips) stopClipIfRunning(clip);
        for (Clip clip : tooMuchEnemiesClips) stopClipIfRunning(clip);
        for (Clip clip : enemyDestroyedClips) stopClipIfRunning(clip);
        for (Clip clip : m53FewEnemiesClips) stopClipIfRunning(clip);
        for (Clip clip : m53TooMuchEnemiesClips) stopClipIfRunning(clip);
        for (Clip clip : m53EnemyDestroyedClips) stopClipIfRunning(clip);
        for (Clip clip : m53PoisonedClips) stopClipIfRunning(clip);
        for (Clip clip : ms1FewEnemiesClips) stopClipIfRunning(clip);
        for (Clip clip : ms1TooMuchEnemiesClips) stopClipIfRunning(clip);
        for (Clip clip : ms1EnemyDestroyedClips) stopClipIfRunning(clip);
        for (Clip clip : ms1PoisonedClips) stopClipIfRunning(clip);
        for (Clip clip : vk10001pFewEnemiesClips) stopClipIfRunning(clip);
        for (Clip clip : vk10001pTooMuchEnemiesClips) stopClipIfRunning(clip);
        for (Clip clip : vk10001pEnemyDestroyedClips) stopClipIfRunning(clip);
        for (Clip clip : vk10001pPoisonedClips) stopClipIfRunning(clip);
        for (Clip clip : poisonedClips) stopClipIfRunning(clip);
    }

    private void stopClipIfRunning(Clip clip) {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    // ВСЕ МЕТОДЫ ВОСПРОИЗВЕДЕНИЯ (используют playVoiceClip)

    public void playM53SpottedSound(int visibleEnemyCount) {
        List<Clip> selectedClips = (visibleEnemyCount <= 2) ? m53FewEnemiesClips : m53TooMuchEnemiesClips;
        if (selectedClips.isEmpty()) return;
        Clip selectedClip = selectedClips.get(randomVoice.nextInt(selectedClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playMS1SpottedSound(int visibleEnemyCount) {
        List<Clip> selectedClips = (visibleEnemyCount <= 2) ? ms1FewEnemiesClips : ms1TooMuchEnemiesClips;
        if (selectedClips.isEmpty()) return;
        Clip selectedClip = selectedClips.get(randomVoice.nextInt(selectedClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playVK10001PSpottedSound(int visibleEnemyCount) {
        List<Clip> selectedClips = (visibleEnemyCount <= 2) ? vk10001pFewEnemiesClips : vk10001pTooMuchEnemiesClips;
        if (selectedClips.isEmpty()) return;
        Clip selectedClip = selectedClips.get(randomVoice.nextInt(selectedClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playT1SpottedSound(int visibleEnemyCount) {
        List<Clip> selectedClips = (visibleEnemyCount <= 2) ? t1FewEnemiesClips : t1TooMuchEnemiesClips;
        if (selectedClips.isEmpty()) return;
        Clip selectedClip = selectedClips.get(randomVoice.nextInt(selectedClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playM53VoiceSound() {
        List<Clip> availableClips = new ArrayList<>();
        for (Clip clip : m53VoiceClips) if (clip != null) availableClips.add(clip);
        if (availableClips.isEmpty()) return;
        Clip selectedClip = availableClips.get(randomVoice.nextInt(availableClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playMS1VoiceSound() {
        List<Clip> availableClips = new ArrayList<>();
        for (Clip clip : ms1VoiceClips) if (clip != null) availableClips.add(clip);
        if (availableClips.isEmpty()) return;
        Clip selectedClip = availableClips.get(randomVoice.nextInt(availableClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playVK10001PVoiceSound() {
        List<Clip> availableClips = new ArrayList<>();
        for (Clip clip : vk10001pVoiceClips) if (clip != null) availableClips.add(clip);
        if (availableClips.isEmpty()) return;
        Clip selectedClip = availableClips.get(randomVoice.nextInt(availableClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playT1VoiceSound() {
        List<Clip> availableClips = new ArrayList<>();
        for (Clip clip : t1VoiceClips) if (clip != null) availableClips.add(clip);
        if (availableClips.isEmpty()) return;
        Clip selectedClip = availableClips.get(randomVoice.nextInt(availableClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playM53EnemyDestroyedSound() {
        List<Clip> availableClips = new ArrayList<>();
        for (Clip clip : m53EnemyDestroyedClips) if (clip != null) availableClips.add(clip);
        if (availableClips.isEmpty()) return;
        Clip selectedClip = availableClips.get(randomVoice.nextInt(availableClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playMS1EnemyDestroyedSound() {
        List<Clip> availableClips = new ArrayList<>();
        for (Clip clip : ms1EnemyDestroyedClips) if (clip != null) availableClips.add(clip);
        if (availableClips.isEmpty()) return;
        Clip selectedClip = availableClips.get(randomVoice.nextInt(availableClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playVK10001PEnemyDestroyedSound() {
        List<Clip> availableClips = new ArrayList<>();
        for (Clip clip : vk10001pEnemyDestroyedClips) if (clip != null) availableClips.add(clip);
        if (availableClips.isEmpty()) return;
        Clip selectedClip = availableClips.get(randomVoice.nextInt(availableClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playT1EnemyDestroyedSound() {
        List<Clip> availableClips = new ArrayList<>();
        for (Clip clip : t1EnemyDestroyedClips) if (clip != null) availableClips.add(clip);
        if (availableClips.isEmpty()) return;
        Clip selectedClip = availableClips.get(randomVoice.nextInt(availableClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playEnemyDestroyedSound() {
        if (enemyDestroyedClips.isEmpty()) return;
        Clip selectedClip = enemyDestroyedClips.get(randomVoice.nextInt(enemyDestroyedClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playVoiceSound() {
        List<Clip> availableClips = new ArrayList<>();
        for (Clip clip : voiceClips) if (clip != null) availableClips.add(clip);
        if (availableClips.isEmpty()) return;
        Clip selectedClip = availableClips.get(randomVoice.nextInt(availableClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playEnemySpottedSound(int visibleEnemyCount) {
        List<Clip> selectedClips = (visibleEnemyCount <= 2) ? fewEnemiesClips : tooMuchEnemiesClips;
        if (selectedClips.isEmpty()) return;
        Clip selectedClip = selectedClips.get(randomVoice.nextInt(selectedClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playMS1PoisonedSound() {
        if (ms1PoisonedClips.isEmpty()) return;
        Clip selectedClip = ms1PoisonedClips.get(randomVoice.nextInt(ms1PoisonedClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playM53PoisonedSound() {
        if (m53PoisonedClips.isEmpty()) return;
        Clip selectedClip = m53PoisonedClips.get(randomVoice.nextInt(m53PoisonedClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playVK10001PPoisonedSound() {
        if (vk10001pPoisonedClips.isEmpty()) return;
        Clip selectedClip = vk10001pPoisonedClips.get(randomVoice.nextInt(vk10001pPoisonedClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playT1PoisonedSound() {
        if (t1PoisonedClips.isEmpty()) return;
        Clip selectedClip = t1PoisonedClips.get(randomVoice.nextInt(t1PoisonedClips.size()));
        playVoiceClip(selectedClip);
    }

    public void playPoisonedSound() {
        if (poisonedClips.isEmpty()) return;
        Clip selectedClip = poisonedClips.get(randomVoice.nextInt(poisonedClips.size()));
        playVoiceClip(selectedClip);
    }

    // ========== ОСТАЛЬНЫЕ МЕТОДЫ (без изменений) ==========

    public void loadShootSound(double caliber) {
        this.currentCaliber = caliber;
        String shootPath = getShootSoundPath(caliber);
        try {
            File soundFile = findSoundFile(shootPath);
            if (soundFile != null && soundFile.exists()) {
                if (shootSoundClip != null && shootSoundClip.isOpen()) shootSoundClip.close();
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                shootSoundClip = AudioSystem.getClip();
                shootSoundClip.open(audioInputStream);
                System.out.println("Звук выстрела загружен: " + soundFile.getName() + " (калибр: " + caliber + ")");
            } else {
                System.err.println("Звук выстрела не найден в папке: " + shootPath);
                loadDefaultShootSound();
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки звука выстрела: " + e.getMessage());
            loadDefaultShootSound();
        }
    }

    private void loadDefaultShootSound() {
        try {
            String defaultPath = "src/SoundEffects/Shooting/BigCaliber/";
            File soundFile = findSoundFile(defaultPath);
            if (soundFile != null && soundFile.exists()) {
                if (shootSoundClip != null && shootSoundClip.isOpen()) shootSoundClip.close();
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                shootSoundClip = AudioSystem.getClip();
                shootSoundClip.open(audioInputStream);
                System.out.println("Загружен дефолтный звук выстрела: " + soundFile.getName());
            }
        } catch (Exception e) {
            System.err.println("Не удалось загрузить дефолтный звук выстрела");
        }
    }

    public void playShootSound() {
        if (shootSoundClip == null) return;
        try {
            String folderPath = getShootSoundPath(currentCaliber);
            String fileName = getSoundFileName(folderPath);
            if (fileName.isEmpty()) {
                if (shootSoundClip.isRunning()) shootSoundClip.stop();
                shootSoundClip.setFramePosition(0);
                shootSoundClip.start();
                return;
            }
            File soundFile = new File(folderPath + fileName);
            if (!soundFile.exists()) {
                if (shootSoundClip.isRunning()) shootSoundClip.stop();
                shootSoundClip.setFramePosition(0);
                shootSoundClip.start();
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) clip.close();
            });
        } catch (Exception e) {
            System.err.println("Ошибка воспроизведения звука выстрела: " + e.getMessage());
            if (shootSoundClip != null) {
                if (shootSoundClip.isRunning()) shootSoundClip.stop();
                shootSoundClip.setFramePosition(0);
                shootSoundClip.start();
            }
        }
    }

    public void playMoveSound() {
        if (moveSoundClip == null) return;
        if (moveSoundClip.isRunning()) moveSoundClip.stop();
        moveSoundClip.setFramePosition(0);
        moveSoundClip.start();
    }

    public void playEnemyMoveSound() {
        if (enemyMoveSoundClip == null) return;
        if (enemyMoveSoundClip.isRunning()) enemyMoveSoundClip.stop();
        enemyMoveSoundClip.setFramePosition(0);
        enemyMoveSoundClip.start();
    }

    public void stopEnemyMoveSound() {
        if (enemyMoveSoundClip != null && enemyMoveSoundClip.isRunning()) {
            enemyMoveSoundClip.stop();
        }
    }

    public void stopMoveSound() {
        if (moveSoundClip != null && moveSoundClip.isRunning()) {
            moveSoundClip.stop();
        }
    }

    public void updateWeaponCaliber(double caliber) {
        this.currentCaliber = caliber;
        loadShootSound(caliber);
    }

    public void playSoundFromFile(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) {
                System.err.println("❌ Файл звука не найден: " + filePath);
                return;
            }

            // ===== ОСТАНАВЛИВАЕМ ПРЕДЫДУЩИЙ ЗВУК =====
            stopVoiceClip();

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            // Сохраняем как текущий воспроизводящийся клип
            currentPlayingClip = clip;
            shouldStopCurrentPlayback = false;

            // Добавляем слушатель для очистки после завершения
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    if (currentPlayingClip == clip) {
                        currentPlayingClip = null;
                        isVoicePlaying = false;
                    }
                    clip.close();
                }
            });

            clip.start();
            isVoicePlaying = true;
            System.out.println("🔊 Воспроизведение звука: " + soundFile.getName());

        } catch (Exception e) {
            System.err.println("❌ Ошибка воспроизведения звука: " + e.getMessage());
        }
    }

    public void playCustomClip(Clip clip) {
        if (clip != null) {
            // Останавливаем текущее воспроизведение
            stopVoiceClip();

            currentPlayingClip = clip;
            shouldStopCurrentPlayback = false;

            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    if (!shouldStopCurrentPlayback && currentPlayingClip == clip) {
                        isVoicePlaying = false;
                        currentPlayingClip = null;
                        System.out.println("✅ Custom clip завершён");
                    }
                }
            });

            clip.start();
            isVoicePlaying = true;
            System.out.println("🔊 Custom clip начат");
        } else {
            System.err.println("Custom clip is null");
        }
    }

    private String getSoundFileName(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
            if (files != null && files.length > 0) {
                return files[0].getName();
            }
        }
        return "";
    }

    private File findSoundFile(String soundPath) {
        File dir = new File(soundPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) ->
                    name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
            if (files != null && files.length > 0) {
                return files[0];
            }
        }
        return null;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }


}