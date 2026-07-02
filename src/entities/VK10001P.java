package entities;

import inventory.Caliber;
import inventory.AmmoItem;
import inventory.WeaponLibrary;
import combat.ExperienceSystem;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class VK10001P extends FriendlyUnit {

    private Clip greetingSound;

    public VK10001P(int x, int y, String name) {
        super(x, y, name, "VK10001P");

        // Базовые характеристики (будут перезаписаны в resetToBaseStats)
        // Просто вызываем родительский конструктор и затем применяем upgrade

        // Устанавливаем уровень модернизации
        this.setUpgradeLevel(4);
        this.setUpgradeClass("TT");

        // Применяем бонусы модернизации (это вызовет resetToBaseStats + applyTTBonuses)
        //applyUpgradeBonuses();

        // Загружаем звук приветствия
        loadGreetingSound();

        // Загружаем оружие
        loadVKWeaponData();

        // Система опыта
        ExperienceSystem expSystem = new ExperienceSystem("VK10001P");
        this.setExperienceSystem(expSystem);
    }

    private void loadVKWeaponData() {
        // Очищаем инвентарь от оружия по умолчанию
        inventory.clearWeapon();

        // Загружаем орудие 12,8 cm Kw.K. L50
        FriendlyUnit.WeaponData weapon = WeaponLibrary.getWeapon("12,8 cm Kw.K. L50");

        if (weapon != null) {
            setEquippedWeapon("128mm", weapon);

            // Добавляем 7 обычных 128мм снарядов в инвентарь
            AmmoItem ammoItem = new AmmoItem(Caliber.CALIBER_128MM, 7, false);
            inventory.addAmmoItem(ammoItem);

            System.out.println("🔫 VK 100.01 P экипирован " + weapon.name + "!");
            System.out.println("📦 128мм снаряды: 7 шт.");
        } else {
            System.err.println("❌ Оружие 12,8 cm Kw.K. L50 не найдено в библиотеке!");
        }
    }

    private void loadGreetingSound() {
        try {
            String soundPath = "src/FriendlyPersons/VK-100-01-P/Union/VK-100-01-P.wav";
            File soundFile = new File(soundPath);
            if (soundFile.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                greetingSound = AudioSystem.getClip();
                greetingSound.open(audioInputStream);
                System.out.println("✅ Звук для VK 100.01 P загружен");
            } else {
                System.err.println("❌ Файл звука для VK 100.01 P не найден: " + soundPath);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки звука для VK 100.01 P: " + e.getMessage());
        }
    }

    // Переопределяем метод воспроизведения звука
    public void playGreetingSound() {
        if (greetingSound == null) return;
        try {
            if (greetingSound.isRunning()) greetingSound.stop();
            greetingSound.setFramePosition(0);
            greetingSound.start();
        } catch (Exception e) {
            System.err.println("Ошибка воспроизведения звука: " + e.getMessage());
        }
    }
}