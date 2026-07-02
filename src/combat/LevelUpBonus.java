package combat;

import entities.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LevelUpBonus implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum BonusType {
        HEALTH("❤️ Здоровье", "+10 к максимальному здоровью"),
        ACCURACY("🎯 Точность", "+1 к точности оружия"),
        STRENGTH("💪 Сила", "+1 к силе (больше очков хода)"),
        AGILITY("🦶 Ловкость", "+1 к ловкости (меньше стоимость шага)"),
        ARMOR("🛡️ Броня", "+1 к броне (уменьшает входящий урон)"),
        CRITICAL("⚡ Критический шанс", "+1% к шансу критического удара (от базового)"),
        VISION("👁️ Зрение", "+1 к радиусу обзора"),
        RELOAD("🔄 Перезарядка", "-1 к стоимости перезарядки (макс -33%)"),
        NIMBLE("💨 Проворность", "+1 к уклонению (+0.15% шанс избежать попадания)");

        public final String name;
        public final String description;

        BonusType(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    private int remainingBonuses;
    private List<BonusType> selectedBonuses = new ArrayList<>();

    public LevelUpBonus(int bonusesPerLevel) {
        this.remainingBonuses = bonusesPerLevel;
    }

    public int getRemainingBonuses() {
        return remainingBonuses;
    }

    public boolean hasBonuses() {
        return remainingBonuses > 0;
    }

    public boolean applyBonus(BonusType bonus, Object unit) {
        if (remainingBonuses <= 0) return false;

        switch (bonus) {
            case HEALTH:
                if (unit instanceof FriendlyUnit) {
                    FriendlyUnit f = (FriendlyUnit) unit;
                    int maxHealth = f.getMaxHealthForClass();
                    if (f.maxHealth >= maxHealth) {
                        System.out.println("⚠️ Здоровье " + f.name + " уже на максимуме (" + maxHealth + ")!");
                        return false;
                    }
                    f.maxHealth += 10;
                    f.health += 10;
                    System.out.println("❤️ " + f.name + " увеличил здоровье на 10! Теперь: " + f.maxHealth +
                            " (макс: " + maxHealth + ")");
                } else if (unit instanceof PlayerTank) {
                    PlayerTank p = (PlayerTank) unit;
                    int maxHealth = p.getMaxHealthForClass();
                    if (p.maxHealth >= maxHealth) { // ✅
                        System.out.println("⚠️ Прочность Leichttraktor уже на максимуме (" + maxHealth + ")!");
                        return false;
                    }
                    p.maxHealth += 10; // ✅
                    p.health += 10;    // ✅
                    System.out.println("❤️ Leichttraktor увеличил прочность на 10! Теперь: " + p.maxHealth + // ✅ иконка "❤️"
                            " (макс: " + maxHealth + ")");
                }
                break;

            case ACCURACY:
                if (unit instanceof PlayerTank) {
                    PlayerTank p = (PlayerTank) unit;
                    // ===== ИСПРАВЛЕНИЕ: увеличиваем baseAccuracy =====
                    p.baseAccuracy += 1;
                    // Пересчитываем итоговую точность с учётом текущего оружия
                    if (p.getEquippedWeaponData() != null) {
                        p.weaponAccuracy = p.baseAccuracy + p.getEquippedWeaponData().weaponAccuracy;
                    } else {
                        p.weaponAccuracy = p.baseAccuracy;
                    }
                    System.out.println("🎯 " + getName(unit) + " увеличил базовую точность на 1! Теперь: " + p.baseAccuracy);
                } else if (unit instanceof FriendlyUnit) {
                    FriendlyUnit f = (FriendlyUnit) unit;
                    f.baseAccuracy += 1;
                    if (f.getEquippedWeaponData() != null) {
                        f.weaponAccuracy = f.baseAccuracy + f.getEquippedWeaponData().weaponAccuracy;
                    } else {
                        f.weaponAccuracy = f.baseAccuracy;
                    }
                    System.out.println("🎯 " + f.name + " увеличил базовую точность на 1! Теперь: " + f.baseAccuracy);
                }
                break;

            case STRENGTH:
                if (unit instanceof FriendlyUnit) {
                    FriendlyUnit f = (FriendlyUnit) unit;
                    int maxStrength = f.getMaxStrengthForClass();
                    if (f.strength >= maxStrength) {
                        System.out.println("⚠️ Сила " + f.name + " уже на максимуме (" + maxStrength + ")!");
                        return false;
                    }
                    f.strength += 1;
                    f.maxCarryWeight = f.strength * 0.5;
                    f.calculateMovePoints();
                    System.out.println("💪 " + f.name + " увеличил силу на 1! Теперь: " + f.strength +
                            " (макс: " + maxStrength + ")");
                } else if (unit instanceof PlayerTank) {
                    PlayerTank p = (PlayerTank) unit;
                    int maxStrength = p.getMaxStrengthForClass();
                    if (p.strength >= maxStrength) {
                        System.out.println("⚠️ Сила Leichttraktor уже на максимуме (" + maxStrength + ")!");
                        return false;
                    }
                    p.strength += 1;
                    p.maxCarryWeight = p.strength * 0.5;
                    p.calculateMovePoints();
                    System.out.println("💪 Leichttraktor увеличил силу на 1! Теперь: " + p.strength +
                            " (макс: " + maxStrength + ")");
                }
                break;

            case AGILITY:
                if (unit instanceof FriendlyUnit) {
                    FriendlyUnit f = (FriendlyUnit) unit;
                    int maxAgility = f.getMaxAgilityForClass();
                    if (f.agility >= maxAgility) {
                        System.out.println("⚠️ Ловкость " + f.name + " уже на максимуме (" + maxAgility + ")!");
                        return false;
                    }
                    f.agility += 1;
                    f.calculateMovePoints();
                    System.out.println("🦶 " + f.name + " увеличил ловкость на 1! Теперь: " + f.agility +
                            " (макс: " + maxAgility + ")");
                } else if (unit instanceof PlayerTank) {
                    PlayerTank p = (PlayerTank) unit;
                    int maxAgility = p.getMaxAgilityForClass();
                    if (p.agility >= maxAgility) {
                        System.out.println("⚠️ Ловкость Leichttraktor уже на максимуме (" + maxAgility + ")!"); // ✅
                        return false;
                    }
                    p.agility += 1;
                    p.calculateMovePoints(); // ✅ ВАЖНО: пересчитать очки хода!
                    System.out.println("🦶 Leichttraktor увеличил ловкость на 1! Теперь: " + p.agility + // ✅
                            " (макс: " + maxAgility + ")");
                }
                break;

            case ARMOR:
                if (unit instanceof FriendlyUnit) {
                    FriendlyUnit f = (FriendlyUnit) unit;
                    int maxArmor = f.getMaxArmorForClass();
                    if (f.armor >= maxArmor) {
                        System.out.println("⚠️ Броня " + f.name + " уже на максимуме (" + maxArmor + ")!");
                        return false;
                    }
                    f.armor += 1;
                    System.out.println("🛡️ " + f.name + " увеличил броню на 1! Теперь: " + f.armor +
                            " (макс: " + maxArmor + ")");
                } else if (unit instanceof PlayerTank) {
                    PlayerTank p = (PlayerTank) unit;
                    int maxArmor = p.getMaxArmorForClass();
                    if (p.armor >= maxArmor) {
                        System.out.println("⚠️ Броня Leichttraktor уже на максимуме (" + maxArmor + ")!");
                        return false;
                    }
                    p.armor += 1;
                    System.out.println("🛡️ Leichttraktor увеличил броню на 1! Теперь: " + p.armor +
                            " (макс: " + maxArmor + ")");
                }
                break;

            case CRITICAL:
                if (unit instanceof PlayerTank) {
                    PlayerTank p = (PlayerTank) unit;
                    p.critBonus += 1;  // +1% к базовому шансу крита
                    System.out.println("⚡ " + getName(unit) + " увеличил шанс крита на 1%! Теперь: " + p.critBonus + "%");
                } else if (unit instanceof FriendlyUnit) {
                    FriendlyUnit f = (FriendlyUnit) unit;
                    f.critBonus += 1;
                    System.out.println("⚡ " + f.name + " увеличил шанс крита на 1%! Теперь: " + f.critBonus + "%");
                }
                break;

            case VISION:
                if (unit instanceof PlayerTank) {
                    PlayerTank p = (PlayerTank) unit;
                    p.vision += 1;
                    // Пересчитываем радиус обзора: базовый 10 + vision/5
                    p.viewRadius = 10 + p.vision / 5;
                    System.out.println("👁️ " + getName(unit) + " увеличил зрение на 1! Теперь радиус обзора: " + p.viewRadius);
                } else if (unit instanceof FriendlyUnit) {
                    FriendlyUnit f = (FriendlyUnit) unit;
                    f.vision += 1;
                    f.viewRadius = 10 + f.vision / 5;
                    System.out.println("👁️ " + f.name + " увеличил зрение на 1! Теперь радиус обзора: " + f.viewRadius);
                }
                break;

            case RELOAD:
                if (unit instanceof PlayerTank) {
                    PlayerTank p = (PlayerTank) unit;
                    // Убираем ограничение 33, позволяем качать до 100
                    if (p.reloadBonus < 100) {
                        p.reloadBonus += 1;
                        if (p.reloadBonus > 100) p.reloadBonus = 100;

                        // Обновляем стоимость перезарядки
                        p.updateReloadCostFromBonus();

                        System.out.println("🔄 " + getName(unit) + " улучшил перезарядку! Теперь: " +
                                p.reloadCost + " о.х. (бонус: " + p.reloadBonus + "%)");
                    } else {
                        System.out.println("⚠ Максимальный бонус перезарядки (100%) уже достигнут!");
                    }
                } else if (unit instanceof FriendlyUnit) {
                    // Для союзников оставляем без изменений (у них reloadBonus может быть и так высоким)
                    FriendlyUnit f = (FriendlyUnit) unit;
                    if (f.reloadBonus < 100) {
                        f.reloadBonus += 1;
                        if (f.reloadBonus > 100) f.reloadBonus = 100;

                        int originalCost = f.getOriginalReloadCost();
                        double reductionPercent = Math.min(50, f.reloadBonus * 0.5);
                        int reduction = (int)(originalCost * reductionPercent / 100.0);
                        f.reloadCost = originalCost - reduction;
                        if (f.reloadCost < 1) f.reloadCost = 1;

                        System.out.println("🔄 " + f.name + " улучшил перезарядку! Бонус: " +
                                f.reloadBonus + "%, уменьшение: " + String.format("%.1f", reductionPercent) +
                                "%, стоимость: " + f.reloadCost + " о.х. (было: " + originalCost + ")");
                    }
                }
                break;

            case NIMBLE:
                if (unit instanceof FriendlyUnit) {
                    FriendlyUnit f = (FriendlyUnit) unit;
                    int maxNimble = f.getMaxNimbleForClass();
                    if (f.nimble >= maxNimble) {
                        System.out.println("⚠️ Проворность " + f.name + " уже на максимуме (" + maxNimble + ")!");
                        return false;
                    }
                    f.nimble += 1;
                    f.dodgeChance = Math.min(20, 5 + f.nimble * 15 / 100);
                    System.out.println("💨 " + f.name + " увеличил проворность на 1! Теперь: " + f.nimble +
                            " (макс: " + maxNimble + ")");
                } else if (unit instanceof PlayerTank) {
                    PlayerTank p = (PlayerTank) unit;
                    int maxNimble = p.getMaxNimbleForClass();
                    if (p.nimble >= maxNimble) {
                        System.out.println("⚠️ Проворность Leichttraktor уже на максимуме (" + maxNimble + ")!"); // ✅
                        return false;
                    }
                    p.nimble += 1;
                    p.dodgeChance = Math.min(20, 5 + p.nimble * 15 / 100);
                    System.out.println("💨 Leichttraktor увеличил проворность на 1! Теперь: " + p.nimble + // ✅
                            " (макс: " + maxNimble + ")");
                }
                break;
        }

        selectedBonuses.add(bonus);
        remainingBonuses--;

        System.out.println("✅ Применён бонус: " + bonus.name);
        System.out.println("   Осталось бонусов: " + remainingBonuses);

        return true;
    }

    private String getName(Object unit) {
        if (unit instanceof PlayerTank) {
            return "Leichttraktor";
        } else if (unit instanceof FriendlyUnit) {
            return ((FriendlyUnit) unit).name;
        }
        return "Unknown";
    }

    public List<BonusType> getSelectedBonuses() {
        return new ArrayList<>(selectedBonuses);
    }


}