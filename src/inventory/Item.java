package inventory;

import java.io.Serializable;

public class Item implements Serializable {
    public enum ItemType {
        AMMO("🔫", "Снаряды", "Боеприпасы для основного орудия", 1, 1, true, 60, 0),
        MEDKIT("💊", "Аптечка", "Восстанавливает 70 HP", 1, 1, false, 1, 2.0),
        REPAIR_KIT("🔧", "Ремкомплект", "Восстанавливает 150 прочности", 1, 1, false, 1, 4.0),
        FIRE_EXTINGUISHER("🧯", "Огнетушитель", "Тушит пожар", 1, 1, false, 1, 3.0),
        GRENADE("💣", "Граната", "Наносит урон в радиусе 3 клеток", 1, 1, false, 1, 0.5),
        BANDAGE("🩹", "Бинт", "Восстанавливает 30 HP", 1, 1, false, 1, 0.5),
        ENERGY_DRINK("⚡", "Энергетик", "Восстанавливает 10 очков хода", 1, 1, true, 5, 0.3),
        KEY("🔑", "Ключ", "Открывает двери соответствующего цвета", 1, 1, false, 1, 0.1),
        SCOPE("🔍", "Прицел", "Увеличивает точность на 10%", 1, 1, false, 1, 0.5),
        BREAD("🍞", "Просроченный хлеб", "Восстанавливает 100 HP, но отнимает 80/50/30% очков хода на 3 хода", 1, 1, false, 1, 0.1),
        WEAPON("🔫", "2 cm Breda (I)", "Основное орудие Leichttraktor", 2, 2, false, 1, 4.0),
        WEAPON_25MM("🔫", "25mm Canon Raccourci mle. 1934", "25mm орудие Hotchkiss", 2, 2, false, 1, 4.0),
        WEAPON_37MM_ITALIAN("🔫", "Cannone da 37-40", "Итальянское 37мм орудие", 2, 2, false, 1, 4.0),
        WEAPON_37MM_AMERICAN("🔫", "37 mm Semiautomatic Gun M1924", "Американское 37мм орудие", 2, 2, false, 1, 4.0),
        WEAPON_37MM_SWEDEN("🔫", "37 mm kan m-38-49 strv", "Шведское 37мм орудие", 2, 2, false, 1, 5.0),
        WEAPON_45MM("🔫", "45 мм обр. 1932 г.", "Основное орудие MS-1", 2, 2, false, 1, 5.0),
        WEAPON_47MM_FRENCH("🔫", "47 mm SA35", "47мм французское орудие", 2, 2, false, 1, 5.0),
        WEAPON_47MM_ITALIAN("🔫", "Cannone da 47-32", "47мм итальянское орудие", 2, 2, false, 1, 5.0),
        WEAPON_8MM("🔫", "7,92 mm Mauser E.W. 141", "Пулемёт калибра 8mm", 2, 2, false, 1, 3.0),
        WEAPON_30MM("🔫", "3 cm M.K. 103A", "Автоматическая пушка калибра 30mm", 2, 2, false, 1, 9.0),
        WEAPON_13MM_JAPAN("🔫", "13 mm Autocannon Type Ho", "Автоматическая пушка калибра 13mm", 2, 2, false, 1, 3.0),
        WEAPON_13MM_FRENCH("🔫", "13,2 mm Hotchkiss mle. 1930", "Французский крупнокалиберный пулемёт", 2, 2, false, 1, 3.0),
        WEAPON_76MM("🔫", "76 мм Л-10С", "Мощное 76мм орудие советского производства", 2, 2, false, 1, 12.0),
        WEAPON_105MM("🔫", "10,5 cm StuH 42 L28", "Немецкая гаубица 105-го калибра", 2, 2, false, 1, 14.0),
        WEAPON_128MM("🔫", "12,8 cm Kw.K. L50", "Немецкое штурмовое орудие с отличным бронепробитием", 2, 2, false, 1, 19.0),
        WEAPON_203MM("🔫", "8-inch Howitzer M47", "Американская гаубица", 2, 2, false, 1, 26.0);

        public final String icon;
        public final String name;
        public final String description;
        public final int width;
        public final int height;
        public final boolean canStack;
        public final int maxStackSize;
        public final double weight;  // ← ДОБАВЛЕНО поле веса

        ItemType(String icon, String name, String description, int width, int height, boolean canStack, int maxStackSize, double weight) {
            this.icon = icon;
            this.name = name;
            this.description = description;
            this.width = width;
            this.height = height;
            this.canStack = canStack;
            this.maxStackSize = maxStackSize;
            this.weight = weight;
        }
    }

    protected ItemType type;
    protected int count;

    public Item(Item.ItemType type, int count) {
        this.type = type;
        this.count = Math.min(count, type.maxStackSize);
    }

    protected void setCount(int count) {
        this.count = Math.min(count, type.maxStackSize);
    }

    public ItemType getType() { return type; }
    public int getCount() { return count; }

    // Метод для получения общего веса предмета (вес одного * количество)
    public double getTotalWeight() {
        return type.weight * count;
    }

    // Метод для получения веса одного предмета
    public double getWeightPerUnit() {
        return type.weight;
    }

    public boolean addCount(int amount) {
        if (!type.canStack) return false;
        int newCount = count + amount;
        if (newCount <= type.maxStackSize) {
            count = newCount;
            return true;
        }
        return false;
    }

    public boolean removeCount(int amount) {
        if (count >= amount) {
            count -= amount;
            return true;
        }
        return false;
    }

    public boolean canStack() { return type.canStack; }
    public int getMaxStackSize() { return type.maxStackSize; }
    public boolean isFull() { return count >= type.maxStackSize; }
    public boolean isEmpty() { return count <= 0; }
    public boolean isMedicalItem() {
        return type == ItemType.MEDKIT ||
                type == ItemType.BANDAGE ||
                type == ItemType.REPAIR_KIT;
    }

    public String getDisplayName() {
        if (type == ItemType.WEAPON) {
            return "2 cm Breda (I)";
        } else if (type == ItemType.WEAPON_25MM) {
            return "25mm Canon Raccourci mle. 1934";
        } else if (type == Item.ItemType.WEAPON_37MM_ITALIAN) {
            return "Cannone da 37-40";
        } else if (type == ItemType.WEAPON_37MM_AMERICAN) {
            return "37 mm Semiautomatic Gun M1924";
        } else if (type == ItemType.WEAPON_37MM_SWEDEN) {
            return "37 mm kan m-38-49 strv";
        } else if (type == ItemType.WEAPON_47MM_FRENCH) {
            return "47 mm SA35";
        } else if (type == ItemType.WEAPON_47MM_ITALIAN) {
            return "Cannone da 47-32";
        } else if (type == ItemType.WEAPON_8MM) {
            return "7,92 mm Mauser E.W. 141";
        } else if (type == ItemType.WEAPON_13MM_JAPAN) {
            return "13 mm Autocannon Type Ho";
        } else if (type == ItemType.WEAPON_13MM_FRENCH) {
            return "13,2 mm Hotchkiss mle. 1930";
        } else if (type == ItemType.WEAPON_30MM) {
            return "3 cm M.K. 103A";
        } else if (type == ItemType.WEAPON_76MM) {
            return "76 мм Л-10С";
        } else if (type == ItemType.WEAPON_105MM) {
            return "10,5 cm StuH 42 L28";
        } else if (type == ItemType.WEAPON_128MM) {
            return "12,8 cm Kw.K. L50";
        } else if (type == ItemType.WEAPON_203MM) {
            return "8-inch Howitzer M47";
        }
        return type.icon + " " + type.name;
    }

    public int getWidth() { return type.width; }
    public int getHeight() { return type.height; }

    public String getIconPath() {
        if (type == ItemType.FIRE_EXTINGUISHER) {
            return "src/ObjectsOfInventory/TreatmentAndRepair/extinguisher.png";
        }
        if (type == ItemType.WEAPON) {
            return "src/ObjectsOfInventory/Weapon/2 cm Breda (I).png";
        }
        if (type == ItemType.WEAPON_25MM) {
            return "src/ObjectsOfInventory/Weapon/25-mm-Canon-Raccourci-mle.-1934.png";
        }
        if (type == Item.ItemType.WEAPON_37MM_ITALIAN) {
            return "src/ObjectsOfInventory/Weapon/Cannone da 37-40.png";
        }
        if (type == ItemType.WEAPON_37MM_AMERICAN) {
            return "src/ObjectsOfInventory/Weapon/37 mm Semiautomatic Gun M1924.png";
        }
        if (type == ItemType.WEAPON_37MM_SWEDEN) {
            return "src/ObjectsOfInventory/Weapon/37 mm kan m-38-49 strv.png";
        }
        if (type == ItemType.WEAPON_45MM) {
            return "src/ObjectsOfInventory/Weapon/45 мм обр. 1932 г.png";
        }
        if (type == ItemType.WEAPON_47MM_FRENCH) {
            return "src/ObjectsOfInventory/Weapon/47 mm SA35.png";
        }
        if (type == ItemType.WEAPON_47MM_ITALIAN) {
            return "src/ObjectsOfInventory/Weapon/Cannone da 47-32.png";
        }
        if (type == ItemType.WEAPON_8MM) {
            return "src/ObjectsOfInventory/Weapon/7,92 mm Mauser E.W. 141.png";
        }
        if (type == ItemType.WEAPON_13MM_JAPAN) {
            return "src/ObjectsOfInventory/Weapon/13 mm Autocannon Type Ho.png";
        }
        if (type == ItemType.WEAPON_13MM_FRENCH) {
            return "src/ObjectsOfInventory/Weapon/13,2 mm Hotchkiss mle. 1930.png";
        }
        if (type == ItemType.WEAPON_30MM) {
            return "src/ObjectsOfInventory/Weapon/3 cm M.K. 103A.png";
        }
        if (type == ItemType.WEAPON_76MM) {
            return "src/ObjectsOfInventory/Weapon/76 мм Л-10С.png";
        }
        if (type == ItemType.WEAPON_105MM) {
            return "src/ObjectsOfInventory/Weapon/10,5 cm StuH 42 L28.png";
        }
        if (type == ItemType.WEAPON_128MM) {
            return "src/ObjectsOfInventory/Weapon/12,8 cm Kw.K. L50.png";
        }
        if (type == ItemType.WEAPON_203MM) {
            return "src/ObjectsOfInventory/Weapon/8-inch Howitzer M47.png";
        }
        return null;
    }
}