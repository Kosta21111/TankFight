package inventory;

import java.io.Serializable;
import entities.Door;

public class KeyItem extends Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private Door.DoorColor color;

    public KeyItem(Door.DoorColor color, int count) {
        super(ItemType.KEY, count); // <-- ИСПРАВЛЕНО: ItemType.KEY
        this.color = color;
    }

    public Door.DoorColor getColor() {
        return color;
    }

    @Override
    public String getDisplayName() {
        return "🔑 " + color.name() + " ключ";
    }

    // Метод getIconPath() есть в суперклассе (Item), мы его переопределяем
    @Override
    public String getIconPath() {
        // Можно использовать разные иконки для разных цветов
        return "src/ObjectsOfInventory/Keys/" + color.name() + "Key.png";
    }

    // Метод getTooltipText() НЕ СУЩЕСТВУЕТ в суперклассе (Item),
    // поэтому мы не можем использовать @Override, либо добавляем его как новый метод.
    // Убираем @Override
    public String getTooltipText() { // <-- УБРАЛИ @Override
        return "<html>🔑 " + color.name() + " ключ<br>Открывает двери " + color.name() + " цвета</html>";
    }
}