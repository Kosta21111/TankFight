package combat;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Ammo {
    public int penetration = 30;
    public int damage = 11;
    public double critChance = 0.09;
    public double accuracyBonus = 1.0;

    public Ammo() {
        loadAmmoData();
    }

    private void loadAmmoData() {
        try {
            String ammoPath = "src/PositiveHeroes/ListOfHeroes/Снаряды 2 cm Breda (I).txt";
            File ammoFile = new File(ammoPath);
            if (ammoFile.exists()) {
                Scanner scanner = new Scanner(ammoFile);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Базовый:")) {
                        String[] parts = line.substring("Базовый:".length()).trim().split(",");
                        if (parts.length >= 4) {
                            penetration = Integer.parseInt(parts[0].trim());
                            damage = Integer.parseInt(parts[1].trim());
                            critChance = Double.parseDouble(parts[2].trim());
                            accuracyBonus = Double.parseDouble(parts[3].trim());
                        }
                    }
                }
                scanner.close();
                System.out.println("Данные снарядов загружены");
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки данных снарядов: " + e.getMessage());
        }
    }
}