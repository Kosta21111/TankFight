package inventory;

import java.io.Serializable;

public class AmmoItem extends Item implements Serializable {
    private Caliber caliber;
    private boolean isImproved;

    // Характеристики снарядов
    private int damage;
    private double critChance;
    private double accuracyMultiplier;

    public AmmoItem(Caliber caliber, int count) {
        this(caliber, count, false);
    }

    public AmmoItem(Caliber caliber, int count, boolean isImproved) {
        super(Item.ItemType.AMMO, count);
        this.caliber = caliber;
        this.isImproved = isImproved;

        // Загружаем характеристики в зависимости от калибра и улучшенности
        loadAmmoStats();
/*
        int maxStack = caliber.getMaxStackSize();
        if (count > maxStack) {
            setCount(maxStack);
            System.out.println("⚠ " + caliber.name + " снаряды ограничены до " + maxStack + " в стаке!");
        }

 */
    }

    private void loadAmmoStats() {
        // Характеристики для 20mm снарядов
        if (caliber == Caliber.CALIBER_20MM) {
            if (isImproved) {
                this.damage = 11;
                this.critChance = 0.14;
                this.accuracyMultiplier = 1.5;  // ← +50% точности для улучшенных
            } else {
                this.damage = 11;
                this.critChance = 0.09;
                this.accuracyMultiplier = 1.0;  // ← базовые без бонуса
            }
        }
        // 13mm снаряды
        else if (caliber == Caliber.CALIBER_13MM) {
            this.damage = 8;
            this.critChance = 0.07;
            this.accuracyMultiplier = 1.0;
            if (isImproved) {
                this.critChance = 0.1;
                this.accuracyMultiplier = 1.4;
            }
        }
        // 25mm снаряды
        else if (caliber == Caliber.CALIBER_25MM) {
            this.damage = 27;
            this.critChance = 0.11;
            this.accuracyMultiplier = 1.0;
            if (isImproved) {
                this.critChance = 0.16;
                this.accuracyMultiplier = 1.3;
            }
        }
        // 30mm снаряды
        else if (caliber == Caliber.CALIBER_30MM) {
            this.damage = 30;
            this.critChance = 0.09;
            this.accuracyMultiplier = 1.0;
            if (isImproved) {
                this.critChance = 0.14;
                this.accuracyMultiplier = 1.3;
            }
        }
        // 37mm снаряды
        else if (caliber == Caliber.CALIBER_37MM) {
            this.damage = 40;
            this.critChance = 0.07;
            this.accuracyMultiplier = 1.0;
            if (isImproved) {
                this.critChance = 0.1;
                this.accuracyMultiplier = 1.4;
            }
        }
        // 45mm снаряды
        else if (caliber == Caliber.CALIBER_45MM) {
            this.damage = 47;
            this.critChance = 0.12;
            this.accuracyMultiplier = 1.0;
            if (isImproved) {
                this.critChance = 0.18;
                this.accuracyMultiplier = 1.2;
            }
        }
        // 47mm снаряды
        else if (caliber == Caliber.CALIBER_47MM) {
            this.damage = 55;
            this.critChance = 0.09;
            this.accuracyMultiplier = 1.0;
            if (isImproved) {
                this.critChance = 0.12;
                this.accuracyMultiplier = 1.4;
            }
        }
        // 76mm снаряды
        else if (caliber == Caliber.CALIBER_76MM) {
            this.damage = 110;
            this.critChance = 0.1;
            this.accuracyMultiplier = 1.0;
            if (isImproved) {
                this.damage = 130;
                this.critChance = 0.15;
                this.accuracyMultiplier = 1.2;
            }
        }
        // 105mm снаряды
        else if (caliber == Caliber.CALIBER_105MM) {
            this.damage = 350;
            this.critChance = 0.12;
            this.accuracyMultiplier = 1.0;
            if (isImproved) {
                this.critChance = 0.19;
                this.accuracyMultiplier = 1.4;
            }
        }
        // 128mm снаряды
        else if (caliber == Caliber.CALIBER_128MM) {
            this.damage = 440;
            this.critChance = 0.15;
            this.accuracyMultiplier = 1.0;
            if (isImproved) {
                this.damage = 510;
                this.critChance = 0.19;
                this.accuracyMultiplier = 1.4;
            }
        }
        // 8mm снаряды (Mauser)
        else if (caliber == Caliber.CALIBER_8MM) {
            this.damage = 8;
            this.critChance = 0.06;
            this.accuracyMultiplier = 1.0;
            if (isImproved) {
                this.critChance = 0.1;
                this.accuracyMultiplier = 1.3;
            }
        }
        // 203mm снаряды (M53)
        else if (caliber == Caliber.CALIBER_203MM) {
            this.damage = 700;
            this.critChance = 0.3;
            this.accuracyMultiplier = 1.0;
            if (isImproved) {
                this.damage = 1200;
                this.critChance = 0.7;
                this.accuracyMultiplier = 1.2;
            }
        }
    }

    public Caliber getCaliber() {
        return caliber;
    }

    public boolean isImproved() {
        return isImproved;
    }

    public int getDamage() {
        return damage;
    }

    public double getCritChance() {
        return critChance;
    }

    public double getAccuracyMultiplier() {
        return accuracyMultiplier;
    }

    @Override
    public String getDisplayName() {
        String improvedText = isImproved ? " (улучшенные)" : "";
        return caliber.name + " снаряды" + improvedText;
    }

    public String getTooltipText() {
        return String.format("<html>%s снаряды%s<br>💥 Урон: %d<br>⚡ Крит: %.0f%%<br>🎯 Точность: x%.1f</html>",
                caliber.name, isImproved ? " (УЛУЧШЕННЫЕ)" : "",
                damage, critChance * 100, accuracyMultiplier);
    }

    @Override
    public int getMaxStackSize() {
        return caliber.getMaxStackSize();
    }

    @Override
    public boolean isFull() {
        return getCount() >= getMaxStackSize();
    }

    @Override
    public String getIconPath() {
        String basePath = "src/ObjectsOfInventory/Shells/";
        if (caliber == Caliber.CALIBER_20MM) {
            return isImproved ? basePath + "20mmShell_improved.png" : basePath + "20mmShell_based.png";
        } else if (caliber == Caliber.CALIBER_13MM) {
            return basePath + "13mmShell_based.png";
        } else if (caliber == Caliber.CALIBER_25MM) {
            return basePath + "25mmShell_based.png";
        } else if (caliber == Caliber.CALIBER_37MM) {
            return basePath + "37mmShell_based.png";
        } else if (caliber == Caliber.CALIBER_45MM) {
            return basePath + "45mmShell_based.png";
        } else if (caliber == Caliber.CALIBER_47MM) {
            return basePath + "47mmShell_based.png";
        } else if (caliber == Caliber.CALIBER_30MM) {
            return basePath + "30mmShell_based.png";
        } else if (caliber == Caliber.CALIBER_76MM) {
            return basePath + "76mmShell_based.png";
        } else if (caliber == Caliber.CALIBER_105MM) {
            return basePath + "105mmShell_based.png";
        } else if (caliber == Caliber.CALIBER_128MM) {
            return basePath + "128mmShell_based.png";
        } else if (caliber == Caliber.CALIBER_203MM) {
            return basePath + "203mmShell_HESH.png";
        } else if (caliber == Caliber.CALIBER_8MM) {
            return basePath + "8mmShell_based.png";
        }
        return null;
    }
}