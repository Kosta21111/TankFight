package audio;

import javax.sound.sampled.*;
import java.io.File;

public class MusicManager {
    private Clip adventureClip;
    private Clip fightClip;

    private boolean isFightMusicPlaying = false;
    private boolean isAdventureMusicPlaying = false;
    private String currentMusicPath = null;
    private float targetVolume = 0.6f; // Целевая громкость
    private float currentVolume = 0.0f; // Текущая громкость (для плавного перехода)

    // Параметры плавного перехода
    private static final float FADE_SPEED = 0.02f; // Скорость изменения громкости за кадр
    private static final int FADE_INTERVAL_MS = 50; // Интервал обновления громкости

    private javax.swing.Timer fadeTimer;
    private boolean isFading = false;
    private Clip fadingOutClip = null;
    private Clip fadingInClip = null;
    private float fadeOutStartVolume = 0.0f;
    private float fadeInStartVolume = 0.0f;

    // Пути к музыкальным файлам
    private static final String ADVENTURE_MUSIC_PATH = "src/melody/adventure/Тихая миссия.wav";
    private static final String FIGHT_MUSIC_PATH = "src/melody/fight/Финальное сражение.wav";

    public MusicManager() {
        loadMusic();
        initFadeTimer();
    }

    private void initFadeTimer() {
        fadeTimer = new javax.swing.Timer(FADE_INTERVAL_MS, e -> updateFade());
        fadeTimer.setRepeats(true);
    }

    private void loadMusic() {
        // Загружаем фоновую музыку для исследования
        try {
            File adventureFile = new File(ADVENTURE_MUSIC_PATH);
            System.out.println("📁 Проверка файла: " + adventureFile.getAbsolutePath());
            System.out.println("📁 Файл существует: " + adventureFile.exists());
            if (adventureFile.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(adventureFile);
                adventureClip = AudioSystem.getClip();
                adventureClip.open(audioInputStream);
                // Начинаем с нулевой громкости
                setClipVolume(adventureClip, 0.0f);
                System.out.println("✅ Загружена музыка: Тихая миссия");
            } else {
                System.err.println("❌ Файл музыки не найден: " + ADVENTURE_MUSIC_PATH);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки музыки (Adventure): " + e.getMessage());
        }

        // Загружаем боевую музыку
        try {
            File fightFile = new File(FIGHT_MUSIC_PATH);
            if (fightFile.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fightFile);
                fightClip = AudioSystem.getClip();
                fightClip.open(audioInputStream);
                // Начинаем с нулевой громкости
                setClipVolume(fightClip, 0.0f);
                System.out.println("✅ Загружена музыка: Финальное сражение");
            } else {
                System.err.println("❌ Файл музыки не найден: " + FIGHT_MUSIC_PATH);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки музыки (Fight): " + e.getMessage());
        }
    }

    /**
     * Установка громкости для конкретного клипа (0.0 - 1.0)
     */
    private void setClipVolume(Clip clip, float volume) {
        if (clip == null || !clip.isOpen()) return;
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            // Преобразуем линейную громкость в dB
            float dB;
            if (volume <= 0.0f) {
                dB = min; // Полная тишина
            } else {
                dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                // Ограничиваем диапазоном контроллера
                dB = Math.max(min, Math.min(max, dB));
            }
            gainControl.setValue(dB);
        } catch (Exception e) {
            // Если управление громкостью недоступно, игнорируем
        }
    }

    /**
     * Установка громкости (0.0 - 1.0) - внешний интерфейс
     */
    public void setVolume(float volume) {
        this.targetVolume = Math.max(0.0f, Math.min(1.0f, volume));
        // Если не в режиме плавного перехода, применяем сразу
        if (!isFading) {
            applyVolumeToCurrentClip();
        }
    }

    private void applyVolumeToCurrentClip() {
        if (currentMusicPath == null) return;
        Clip clip = getCurrentClip();
        if (clip != null && clip.isOpen()) {
            setClipVolume(clip, currentVolume);
        }
    }

    private Clip getCurrentClip() {
        if (ADVENTURE_MUSIC_PATH.equals(currentMusicPath)) {
            return adventureClip;
        } else if (FIGHT_MUSIC_PATH.equals(currentMusicPath)) {
            return fightClip;
        }
        return null;
    }

    /**
     * Воспроизведение музыки для исследования (когда врагов не видно)
     */
    public void playAdventureMusic() {
        if (isAdventureMusicPlaying && !isFading) return;

        // Если уже играет боевая - начинаем плавный переход
        if (isFightMusicPlaying) {
            startCrossfade(FIGHT_MUSIC_PATH, ADVENTURE_MUSIC_PATH);
            return;
        }

        // Если ничего не играет - просто запускаем
        if (!isAdventureMusicPlaying && !isFightMusicPlaying) {
            startClip(adventureClip);
            isAdventureMusicPlaying = true;
            currentMusicPath = ADVENTURE_MUSIC_PATH;
            currentVolume = 0.0f;
            // Плавно увеличиваем громкость
            fadeIn(adventureClip, targetVolume);
        }
    }

    /**
     * Воспроизведение боевой музыки (когда обнаружен хотя бы один враг)
     */
    public void playFightMusic() {
        if (isFightMusicPlaying && !isFading) return;

        // Если уже играет приключенческая - начинаем плавный переход
        if (isAdventureMusicPlaying) {
            startCrossfade(ADVENTURE_MUSIC_PATH, FIGHT_MUSIC_PATH);
            return;
        }

        // Если ничего не играет - просто запускаем
        if (!isAdventureMusicPlaying && !isFightMusicPlaying) {
            startClip(fightClip);
            isFightMusicPlaying = true;
            currentMusicPath = FIGHT_MUSIC_PATH;
            currentVolume = 0.0f;
            fadeIn(fightClip, targetVolume);
        }
    }

    /**
     * Запуск клипа с нулевой громкости
     */
    private void startClip(Clip clip) {
        if (clip == null) return;
        try {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            setClipVolume(clip, 0.0f);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println("Ошибка запуска клипа: " + e.getMessage());
        }
    }

    /**
     * Плавное увеличение громкости
     */
    private void fadeIn(Clip clip, float targetVol) {
        if (clip == null) return;
        if (!clip.isRunning()) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }

        isFading = true;
        fadingInClip = clip;
        fadeInStartVolume = currentVolume;
        isFading = true;
        fadeTimer.start();
    }

    /**
     * Плавный переход между двумя мелодиями (crossfade)
     */
    private void startCrossfade(String fromPath, String toPath) {
        // Останавливаем текущий fade, если он был
        stopFade();

        Clip fromClip = getClipByPath(fromPath);
        Clip toClip = getClipByPath(toPath);

        if (fromClip == null || toClip == null) return;

        // Запоминаем текущую громкость для затухания
        float currentVol = currentVolume;

        // Убеждаемся, что обе мелодии играют
        if (!fromClip.isRunning()) {
            fromClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        if (!toClip.isRunning()) {
            toClip.loop(Clip.LOOP_CONTINUOUSLY);
        }

        // Устанавливаем громкость целевой мелодии на 0
        setClipVolume(toClip, 0.0f);

        // Сохраняем состояние для плавного перехода
        fadingOutClip = fromClip;
        fadingInClip = toClip;
        fadeOutStartVolume = currentVol;
        fadeInStartVolume = 0.0f;

        // Обновляем текущий путь
        currentMusicPath = toPath;
        isFading = true;
        fadeTimer.start();

        System.out.println("🔄 Начинается плавный переход: " + fromPath + " -> " + toPath);
    }

    /**
     * Обновление плавного перехода (вызывается по таймеру)
     */
    private void updateFade() {
        if (!isFading) {
            fadeTimer.stop();
            return;
        }

        // Увеличиваем громкость входящей мелодии
        if (fadingInClip != null) {
            float newInVolume = Math.min(targetVolume, fadeInStartVolume + FADE_SPEED);
            setClipVolume(fadingInClip, newInVolume);
            fadeInStartVolume = newInVolume;
        }

        // Уменьшаем громкость исходящей мелодии
        if (fadingOutClip != null) {
            float newOutVolume = Math.max(0.0f, fadeOutStartVolume - FADE_SPEED);
            setClipVolume(fadingOutClip, newOutVolume);
            fadeOutStartVolume = newOutVolume;
        }

        // Обновляем текущую громкость
        currentVolume = fadeInStartVolume;

        // Проверяем, завершён ли переход
        boolean fadeOutComplete = (fadingOutClip == null || fadeOutStartVolume <= 0.0f);
        boolean fadeInComplete = (fadingInClip == null || fadeInStartVolume >= targetVolume);

        if (fadeOutComplete && fadeInComplete) {
            // Завершаем переход
            if (fadingOutClip != null && fadingOutClip.isRunning()) {
                fadingOutClip.stop();
                fadingOutClip.setFramePosition(0);
            }

            // Обновляем состояние
            isFading = false;
            isAdventureMusicPlaying = ADVENTURE_MUSIC_PATH.equals(currentMusicPath);
            isFightMusicPlaying = FIGHT_MUSIC_PATH.equals(currentMusicPath);
            currentVolume = targetVolume;

            fadingOutClip = null;
            fadingInClip = null;
            fadeTimer.stop();

            System.out.println("✅ Плавный переход завершён! Сейчас играет: " +
                    (isAdventureMusicPlaying ? "Тихая миссия" : "Финальное сражение"));
        }
    }

    /**
     * Остановка плавного перехода
     */
    private void stopFade() {
        if (isFading) {
            isFading = false;
            fadeTimer.stop();

            // Принудительно устанавливаем громкость
            if (fadingInClip != null) {
                setClipVolume(fadingInClip, targetVolume);
            }
            if (fadingOutClip != null) {
                setClipVolume(fadingOutClip, 0.0f);
                if (fadingOutClip.isRunning()) {
                    fadingOutClip.stop();
                    fadingOutClip.setFramePosition(0);
                }
            }

            fadingOutClip = null;
            fadingInClip = null;
            currentVolume = targetVolume;
        }
    }

    private Clip getClipByPath(String path) {
        if (ADVENTURE_MUSIC_PATH.equals(path)) return adventureClip;
        if (FIGHT_MUSIC_PATH.equals(path)) return fightClip;
        return null;
    }

    /**
     * Остановка всей музыки
     */
    public void stopAllMusic() {
        stopFade();

        if (adventureClip != null) {
            if (adventureClip.isRunning()) {
                adventureClip.stop();
            }
            setClipVolume(adventureClip, 0.0f);
        }
        if (fightClip != null) {
            if (fightClip.isRunning()) {
                fightClip.stop();
            }
            setClipVolume(fightClip, 0.0f);
        }

        isAdventureMusicPlaying = false;
        isFightMusicPlaying = false;
        currentMusicPath = null;
        currentVolume = 0.0f;
    }

    /**
     * Обновление музыки в зависимости от наличия видимых врагов
     */
    public void updateMusic(boolean hasVisibleEnemy) {
        System.out.println("🎵 MusicManager.updateMusic: hasVisibleEnemy = " + hasVisibleEnemy);

        if (hasVisibleEnemy) {
            playFightMusic();
        } else {
            playAdventureMusic();
        }
    }

    /**
     * Пауза воспроизведения
     */
    public void pause() {
        if (isFading) {
            stopFade();
        }
        Clip clip = getCurrentClip();
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    /**
     * Возобновление воспроизведения
     */
    public void resume() {
        Clip clip = getCurrentClip();
        if (clip != null && clip.isOpen()) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            if (currentVolume < targetVolume) {
                fadeIn(clip, targetVolume);
            }
        }
    }

    /**
     * Закрытие всех ресурсов
     */
    public void dispose() {
        stopAllMusic();
        if (fadeTimer != null) {
            fadeTimer.stop();
        }
        if (adventureClip != null) {
            adventureClip.close();
        }
        if (fightClip != null) {
            fightClip.close();
        }
    }

    public boolean isFightMusicPlaying() {
        return isFightMusicPlaying;
    }
}