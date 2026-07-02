package combat;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Weapon {
    public int accuracy = 30;
    public int strength = 15;
    public int burstSize = 3;
    public int aimedShotCost = 11;
    public int burstShotCost = 5;
    public double caliber = 0.02;
    public int magazineSize = 12;      // Длина обоймы
    public int reloadCost = 6;         // Стоимость перезарядки

    public Ammo currentAmmo;

    public Weapon() {
        currentAmmo = new Ammo();  // ← Убедитесь, что эта строка есть
        loadGunData();
    }

    private void loadGunData() {
        try {
            String gunPath = "src/InfoAboutWeapon/2 cm Breda (I)/2 cm Breda (I).txt";
            File gunFile = new File(gunPath);
            if (gunFile.exists()) {
                Scanner scanner = new Scanner(gunFile);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Точность:")) {
                        accuracy = Integer.parseInt(line.substring("Точность:".length()).trim());
                    } else if (line.startsWith("Сила:")) {
                        strength = Integer.parseInt(line.substring("Сила:".length()).trim());
                    } else if (line.startsWith("Выстрелов за огонь:")) {
                        burstSize = Integer.parseInt(line.substring("Выстрелов за огонь:".length()).trim());
                    } else if (line.startsWith("Прицельный огонь, о. х.:")) {
                        aimedShotCost = Integer.parseInt(line.substring("Прицельный огонь, о. х.:".length()).trim());
                    } else if (line.startsWith("Беглый огонь, о. х.:")) {
                        burstShotCost = Integer.parseInt(line.substring("Беглый огонь, о. х.:".length()).trim());
                    } else if (line.startsWith("Калибр:")) {
                        caliber = Double.parseDouble(line.substring("Калибр:".length()).trim());
                    } else if (line.startsWith("Длина обоймы:")) {
                        magazineSize = Integer.parseInt(line.substring("Длина обоймы:".length()).trim());
                    } else if (line.startsWith("Смена обоймы, о. х.:")) {
                        reloadCost = Integer.parseInt(line.substring("Смена обоймы, о. х.:".length()).trim());
                    }
                }
                scanner.close();
                System.out.println("Данные орудия загружены:");
                System.out.println("  Точность: " + accuracy);
                System.out.println("  Выстрелов за огонь: " + burstSize);
                System.out.println("  Длина обоймы: " + magazineSize);
                System.out.println("  Стоимость перезарядки: " + reloadCost);
            } else {
                System.err.println("Файл орудия не найден: " + gunPath);
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки данных орудия: " + e.getMessage());
        }
    }
}