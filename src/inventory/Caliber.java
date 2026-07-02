package inventory;

import java.io.Serializable;

// В Caliber.java
public enum Caliber implements Serializable {
    CALIBER_20MM("20mm", 0.02, "2 cm Breda (I)", 60),
    CALIBER_25MM("25mm", 0.025, "25mm Canon Raccourci mle. 1934", 60),
    CALIBER_203MM("203mm", 0.203, "8-inch Howitzer M47", 3),
    CALIBER_8MM("8mm", 0.008, "7,92 mm Mauser E.W. 141", 120),
    CALIBER_37MM("37mm", 0.037, "Cannone da 37-40", 40),
    CALIBER_45MM("45mm", 0.045, "45 мм обр. 1932 г.", 40),
    CALIBER_47MM("47mm", 0.047, "47 mm SA35", 40),
    CALIBER_13MM("13mm", 0.013, "13 mm Autocannon Type Ho", 60),
    CALIBER_30MM("30mm", 0.03, "3 cm M.K. 103A", 60),
    CALIBER_76MM("76mm", 0.076, "76 мм Л-10С", 40),
    CALIBER_105MM("105mm", 0.105, "10,5 cm StuH 42 L28", 40),
    CALIBER_128MM("128mm", 0.128, "12,8 cm Kw.K. L50", 7);

    public final String name;
    public final double size;
    public final String weaponName;
    private final int maxStackSize;

    Caliber(String name, double size, String weaponName, int maxStackSize) {
        this.name = name;
        this.size = size;
        this.weaponName = weaponName;
        this.maxStackSize = maxStackSize;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }
}