package entities;

import java.io.Serializable;
import java.awt.Color;

public enum Faction implements Serializable {
    GERMANY("Германия", new Color(100, 100, 100), new Color(200, 200, 200)),
    JAPAN("Япония", new Color(200, 100, 100), new Color(255, 150, 150)),
    FRANCE("Франция", new Color(100, 100, 200), new Color(150, 150, 255)),
    ITALY("Италия", new Color(100, 200, 100), new Color(150, 255, 150)),
    NEUTRAL("Нейтралы", new Color(150, 150, 150), new Color(200, 200, 200));

    public final String displayName;
    public final Color iconColor;      // Цвет для маркера на карте
    public final Color uiColor;        // Цвет для интерфейса

    Faction(String displayName, Color iconColor, Color uiColor) {
        this.displayName = displayName;
        this.iconColor = iconColor;
        this.uiColor = uiColor;
    }

    // Вспомогательный метод для получения фракции по типу врага
    public static Faction fromEnemyType(String enemyType) {
        switch (enemyType) {
            case "Leichttraktor": return GERMANY;
            case "R_Otsu": return JAPAN;
            case "H35": return FRANCE;
            case "M14_41": return ITALY;
            case "FT": return FRANCE;  // ← ДОБАВИТЬ
            case "Fiat3000": return ITALY;
            default: return NEUTRAL;
        }
    }

    // Проверка, враждебны ли две фракции
    public boolean isHostileTo(Faction other) {
        if (this == NEUTRAL || other == NEUTRAL) return false;
        return this != other;
    }
}