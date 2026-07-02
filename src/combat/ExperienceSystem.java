package combat;

import java.io.Serializable;
import java.util.ArrayList;  // ← ДОБАВИТЬ
import java.util.List;       // ← ДОБАВИТЬ


public class ExperienceSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int level = 1;
    private int experience = 0;
    private int experienceForNextLevel = 400;  // ← ИЗМЕНЕНО с 100 на 400
    private int totalExperience = 0;  // Общий накопленный опыт

    private LevelUpBonus pendingLevelUpBonus = null;
    private int bonusesPerLevel = 0;

    public ExperienceSystem() {
        calculateNextLevelExp();
    }

    public ExperienceSystem(String unitType) {
        this();
        switch (unitType) {
            case "Leichttraktor":
                bonusesPerLevel = 8;
                break;
            case "MS-1":
                bonusesPerLevel = 8;
                break;
            case "M53":
                bonusesPerLevel = 3;
                break;
            case "VK10001P":
                bonusesPerLevel = 5;
                break;
            case "AMX40":
                bonusesPerLevel = 11;
                break;
            case "T1":
                bonusesPerLevel = 10;
                break;
            default:
                bonusesPerLevel = 5;
                break;
        }
    }

    // ===== НОВЫЙ МЕТОД С ТАБЛИЦЕЙ ОПЫТА =====
    private void calculateNextLevelExp() {
        if (level >= 10) {
            experienceForNextLevel = Integer.MAX_VALUE;
            return;
        }

        // Таблица с округлением до сотен
        switch(level) {
            case 1: experienceForNextLevel = 400; break;
            case 2: experienceForNextLevel = 600; break;
            case 3: experienceForNextLevel = 900; break;
            case 4: experienceForNextLevel = 1400; break;
            case 5: experienceForNextLevel = 2100; break;
            case 6: experienceForNextLevel = 3200; break;
            case 7: experienceForNextLevel = 4800; break;
            case 8: experienceForNextLevel = 7200; break;
            case 9: experienceForNextLevel = 10800; break;
            default: experienceForNextLevel = 100;
        }
    }

    public void addExperience(int amount) {
        if (level >= 10) {
            System.out.println("⚠️ Максимальный уровень (10) достигнут! Опыт не начисляется.");
            return;
        }

        experience += amount;
        totalExperience += amount;  // ← Добавить
        System.out.println("✨ Получено опыта: " + amount + " (Всего: " + experience + "/" + experienceForNextLevel + ")");

        while (experience >= experienceForNextLevel && level < 10) {
            levelUp();
        }
    }

    public void setExperienceForNextLevel(int expForNextLevel) {
        this.experienceForNextLevel = expForNextLevel;
    }

    // В ExperienceSystem.java, метод levelUp():
    private void levelUp() {
        if (level >= 10) return;

        experience -= experienceForNextLevel;
        level++;
        calculateNextLevelExp();

        pendingLevelUpBonus = new LevelUpBonus(bonusesPerLevel);

        // ===== ВАЖНО: НЕ ОЧИЩАЕМ старые бонусы, а создаём новые =====
        System.out.println("🎉 ПОВЫШЕНИЕ УРОВНЯ! Теперь уровень: " + level);
        System.out.println("   🎁 Получено бонусов для распределения: " + bonusesPerLevel);
    }

    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getExperienceForNextLevel() { return experienceForNextLevel; }

    public float getExperiencePercent() {
        if (experienceForNextLevel == Integer.MAX_VALUE) return 1.0f;
        return (float) experience / experienceForNextLevel;
    }

    public boolean hasPendingBonuses() {
        return pendingLevelUpBonus != null && pendingLevelUpBonus.hasBonuses();
    }

    public LevelUpBonus getPendingLevelUpBonus() {
        return pendingLevelUpBonus;
    }

    public void clearPendingBonuses() {
        pendingLevelUpBonus = null;
    }

    public int getBonusesPerLevel() {
        return bonusesPerLevel;
    }

    public int getPendingBonuses() {
        return pendingLevelUpBonus != null ? pendingLevelUpBonus.getRemainingBonuses() : 0;
    }

    public List<LevelUpBonus.BonusType> getSelectedBonuses() {
        return pendingLevelUpBonus != null ? pendingLevelUpBonus.getSelectedBonuses() : new ArrayList<>();
    }
}