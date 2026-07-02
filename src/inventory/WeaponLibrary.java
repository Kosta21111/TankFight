package inventory;

import entities.FriendlyUnit;
import java.util.HashMap;
import java.util.Map;

public class WeaponLibrary {
    private static Map<String, FriendlyUnit.WeaponData> weapons = new HashMap<>();

    static {
        // 2 cm Breda (I) - сила 15
        // И обновите все вызовы:
        weapons.put("2 cm Breda (I)", new FriendlyUnit.WeaponData(
                "2 cm Breda (I)","breda", Caliber.CALIBER_20MM,
                3, 30, 0.02, 5, 11, 6, 11, 0.09,
                "src/ObjectsOfInventory/Weapon/2 cm Breda (I).png",
                15, 4  // requiredStrength, weight
        ));

        weapons.put("25mm Canon Raccourci mle. 1934", new FriendlyUnit.WeaponData(
                "25mm Canon Raccourci mle. 1934","25mm", Caliber.CALIBER_25MM,
                2, 45, 0.025, 4, 9, 14, 27, 0.11,
                "src/ObjectsOfInventory/Weapon/25-mm-Canon-Raccourci-mle.-1934.png",
                15, 4  // requiredStrength, weight
        ));

        weapons.put("45 мм обр. 1932 г.", new FriendlyUnit.WeaponData(
                "45 мм обр. 1932 г.", "45mm", Caliber.CALIBER_45MM,
                1, 15, 0.045, 11, 22, 10, 47, 0.12,
                "src/ObjectsOfInventory/Weapon/45 мм обр. 1932 г.png",
                19, 5
        ));

        weapons.put("47 mm SA35", new FriendlyUnit.WeaponData(
                "47 mm SA35", "47mm_french", Caliber.CALIBER_47MM,
                1, 21, 0.047, 9, 18, 10, 55, 0.09,
                "src/ObjectsOfInventory/Weapon/47 mm SA35.png",
                17, 5  // requiredStrength, weight
        ));

        weapons.put("Cannone da 47-32", new FriendlyUnit.WeaponData(
                "Cannone da 47-32", "47mm_italian", Caliber.CALIBER_47MM,
                1, 17, 0.047, 9, 18, 10, 52, 0.1,
                "src/ObjectsOfInventory/Weapon/Cannone da 47-32.png",
                16, 5  // requiredStrength, weight
        ));

        weapons.put("7,92 mm Mauser E.W. 141", new FriendlyUnit.WeaponData(
                "7,92 mm Mauser E.W. 141", "8mm", Caliber.CALIBER_8MM,
                8, 25, 0.008, 7, 14, 22, 8, 0.06,
                "src/ObjectsOfInventory/Weapon/7,92 mm Mauser E.W. 141.png",
                12, 3
        ));

        weapons.put("13 mm Autocannon Type Ho", new FriendlyUnit.WeaponData(
                "13 mm Autocannon Type Ho", "13mm_japan", Caliber.CALIBER_13MM,
                3, 17, 0.013, 4, 9, 12, 8, 0.07,
                "src/ObjectsOfInventory/Weapon/13 mm Autocannon Type Ho.png",
                12, 3
        ));

        weapons.put("13,2 mm Hotchkiss mle. 1930", new FriendlyUnit.WeaponData(
                "13,2 mm Hotchkiss mle. 1930", "13mm_french", Caliber.CALIBER_13MM,
                3, 13, 0.013, 4, 9, 9, 8, 0.05,
                "src/ObjectsOfInventory/Weapon/13,2 mm Hotchkiss mle. 1930.png",
                10, 3
        ));

        weapons.put("3 cm M.K. 103A", new FriendlyUnit.WeaponData(
                "3 cm M.K. 103A", "30mm", Caliber.CALIBER_30MM,
                3, 15, 0.03, 5, 11, 38, 30, 0.09,
                "src/ObjectsOfInventory/Weapon/3 cm M.K. 103A.png",
                45, 9
        ));

        weapons.put("Cannone da 37-40", new FriendlyUnit.WeaponData(
                "Cannone da 37-40", "37mm_italian", Caliber.CALIBER_37MM,
                1, 15, 0.037, 7, 14, 12, 40, 0.07,
                "src/ObjectsOfInventory/Weapon/Cannone da 37-40.png",
                13, 4  // requiredStrength, weight
        ));

        weapons.put("37 mm Semiautomatic Gun M1924", new FriendlyUnit.WeaponData(
                "37 mm Semiautomatic Gun M1924", "37mm_american", Caliber.CALIBER_37MM,
                5, 10, 0.037, 9, 18, 25, 30, 0.07,
                "src/ObjectsOfInventory/Weapon/37 mm Semiautomatic Gun M1924.png",
                20, 4  // requiredStrength, weight
        ));

        weapons.put("37 mm kan m-38-49 strv", new FriendlyUnit.WeaponData(
                "37 mm kan m-38-49 strv", "37mm_sweden", Caliber.CALIBER_37MM,
                1, 50, 0.037, 8, 16, 12, 40, 0.05,
                "src/ObjectsOfInventory/Weapon/37 mm kan m-38-49 strv.png",
                24, 5  // requiredStrength, weight
        ));

        weapons.put("8-inch Howitzer M47", new FriendlyUnit.WeaponData(
                "8-inch Howitzer M47", "203mm", Caliber.CALIBER_203MM,
                1, 20, 0.203, 32, 64, 75, 700, 0.3,
                "src/InfoAboutWeapon/8-inch Howitzer M47/8-inch Howitzer M47.png",
                90, 26
        ));

        weapons.put("76 мм Л-10С", new FriendlyUnit.WeaponData(
                "76 мм Л-10С", "76mm", Caliber.CALIBER_76MM,
                1, 20, 0.076, 12, 24, 15, 120, 0.15,
                "src/ObjectsOfInventory/Weapon/76 мм Л-10С.png",
                43, 12
        ));

        weapons.put("10,5 cm StuH 42 L28", new FriendlyUnit.WeaponData(
                "10,5 cm StuH 42 L28", "105mm", Caliber.CALIBER_30MM,
                1, 20, 0.12, 24, 48, 26, 350, 0.12,
                "src/ObjectsOfInventory/Weapon/10,5 cm StuH 42 L28.png",
                60, 14
        ));

        weapons.put("12,8 cm Kw.K. L50", new FriendlyUnit.WeaponData(
                "12,8 cm Kw.K. L50", "128mm", Caliber.CALIBER_128MM,
                1, 30, 0.128, 30, 60, 36, 440, 0.15,
                "src/ObjectsOfInventory/Weapon/12,8 cm Kw.K. L50.png",
                80, 19
        ));
    }

    public static FriendlyUnit.WeaponData getWeapon(String name) {
        FriendlyUnit.WeaponData weapon = weapons.get(name);
        if (weapon == null) {
            System.err.println("⚠️ Оружие не найдено в библиотеке: " + name);
        } else {
            System.out.println("✅ Найдено оружие: " + weapon.name + ", калибр: " + weapon.caliber.name());
        }
        return weapon;
    }

    public static FriendlyUnit.WeaponData getWeaponByItem(Item item) {
        System.out.println("=== getWeaponByItem ===");
        System.out.println("  item type: " + item.getType());

        if (item.getType() == Item.ItemType.WEAPON) {
            System.out.println("  Возвращаем Breda");
            return getWeapon("2 cm Breda (I)");
        } else if (item.getType() == Item.ItemType.WEAPON_45MM) {
            System.out.println("  Возвращаем 45mm");
            return getWeapon("45 мм обр. 1932 г.");
        } else if (item.getType() == Item.ItemType.WEAPON_25MM) {
            System.out.println("  Возвращаем 25mm");
            return getWeapon("25mm Canon Raccourci mle. 1934.");
        } else if (item.getType() == Item.ItemType.WEAPON_47MM_FRENCH) {
            System.out.println("  Возвращаем 47mm");
            return getWeapon("47 mm SA35");
        } else if (item.getType() == Item.ItemType.WEAPON_47MM_ITALIAN) {
            System.out.println("  Возвращаем 47mm");
            return getWeapon("Cannone da 47-32");
        } else if (item.getType() == Item.ItemType.WEAPON_8MM) {
            System.out.println("  Возвращаем Mauser");
            return getWeapon("7,92 mm Mauser E.W. 141");
        } else if (item.getType() == Item.ItemType.WEAPON_13MM_JAPAN) {
            System.out.println("  Возвращаем 13mm");
            return getWeapon("13 mm Autocannon Type Ho");
        } else if (item.getType() == Item.ItemType.WEAPON_13MM_FRENCH) {
            System.out.println("  Возвращаем 13mm");
            return getWeapon("13,2 mm Hotchkiss mle. 1930");
        } else if (item.getType() == Item.ItemType.WEAPON_30MM) {
            System.out.println("  Возвращаем 30mm");
            return getWeapon("3 cm M.K. 103A");
        } else if (item.getType() == Item.ItemType.WEAPON_37MM_ITALIAN) {
            System.out.println("  Возвращаем 37mm");
            return getWeapon("Cannone da 37-40");
        } else if (item.getType() == Item.ItemType.WEAPON_37MM_AMERICAN) {
            System.out.println("  Возвращаем 37mm");
            return getWeapon("37 mm Semiautomatic Gun M1924");
        } else if (item.getType() == Item.ItemType.WEAPON_37MM_SWEDEN) {
            System.out.println("  Возвращаем 37mm");
            return getWeapon("37 mm kan m-38-49 strv");
        } else if (item.getType() == Item.ItemType.WEAPON_76MM) {
            System.out.println("  Возвращаем 76mm");
            return getWeapon("76 мм Л-10С");
        } else if (item.getType() == Item.ItemType.WEAPON_105MM) {
            System.out.println("  Возвращаем 105mm");
            return getWeapon("10,5 cm StuH 42 L28");
        } else if (item.getType() == Item.ItemType.WEAPON_128MM) {
            System.out.println("  Возвращаем 128mm");
            return getWeapon("12,8 cm Kw.K. L50");
        } else if (item.getType() == Item.ItemType.WEAPON_203MM) {
            System.out.println("  Возвращаем 203mm");
            return getWeapon("8-inch Howitzer M47");
        }
        System.out.println("  Неизвестный тип, возвращаем null");
        return null;
    }

    public static FriendlyUnit.WeaponData getWeaponByWeaponId(String weaponId) {
        System.out.println("=== getWeaponByWeaponId ===");
        System.out.println("  weaponId: " + weaponId);

        if (weaponId == null) {
            System.out.println("  weaponId is null");
            return null;
        }

        switch (weaponId) {
            case "breda":
                return getWeapon("2 cm Breda (I)");
            case "25mm":
                return getWeapon("25mm Canon Raccourci mle. 1934");
            case "37mm_italian":
                return getWeapon("Cannone da 37-40");
            case "37mm_american":
                return getWeapon("37 mm Semiautomatic Gun M1924");
            case "37mm_sweden":
                return getWeapon("37 mm kan m-38-49 strv");
            case "45mm":
                return getWeapon("45 мм обр. 1932 г.");
            case "47mm_french":
                return getWeapon("47 mm SA35");
            case "47mm_italian":
                return getWeapon("Cannone da 47-32");
            case "8mm":
                return getWeapon("7,92 mm Mauser E.W. 141");
            case "13mm_japan":
                return getWeapon("13 mm Autocannon Type Ho");
            case "13mm_french":
                return getWeapon("13,2 mm Hotchkiss mle. 1930");
            case "30mm":
                return getWeapon("3 cm M.K. 103A");
            case "76mm":
                return getWeapon("76 мм Л-10С");
            case "105mm":
                return getWeapon("10,5 cm StuH 42 L28");
            case "128mm":
                return getWeapon("12,8 cm Kw.K. L50");
            case "203mm":
                return getWeapon("8-inch Howitzer M47");
            default:
                System.out.println("  Неизвестный weaponId: " + weaponId);
                return null;
        }
    }
}