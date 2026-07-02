package inventory;

import ui.InventoryDialog;
import java.io.Serializable;

public class MedicalItem extends Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private int remainingUses;      // Осталось использований
    private int maxUses;            // Максимальное количество использований
    private int healAmount;         // Количество HP за одно использование

    // Добавьте этот конструктор в MedicalItem.java
    public MedicalItem(ItemType type, int remainingUses, int maxUses, int healAmount) {
        super(type, 1);
        this.remainingUses = remainingUses;
        this.maxUses = maxUses;
        this.healAmount = healAmount;
    }

    public MedicalItem(ItemType type, int count) {
        super(type, 1);  // Всегда 1 в слоте, но с несколькими использованиями
        this.maxUses = 4;
        this.remainingUses = 4;

        // Устанавливаем количество лечения в зависимости от типа
        switch (type) {
            case BANDAGE:
                this.healAmount = 30;
                break;
            case MEDKIT:
                this.healAmount = 70;
                break;
            case REPAIR_KIT:
                this.healAmount = 150;
                break;
            default:
                this.healAmount = 0;
        }
    }

    public int getRemainingUses() {
        return remainingUses;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getHealAmount() {
        return healAmount;
    }

    public boolean use() {
        if (remainingUses > 0) {
            remainingUses--;
            System.out.println("  MedicalItem использован, осталось: " + remainingUses + "/" + maxUses);
            return true;
        }
        System.out.println("  ⚠ MedicalItem уже истощён!");
        return false;
    }

    public boolean isDepleted() {
        return remainingUses <= 0;
    }

    @Override
    public boolean removeCount(int amount) {
        // Для MedicalItem удаляем только если предмет полностью использован
        if (isDepleted()) {
            return super.removeCount(1);
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return isDepleted() || super.isEmpty();
    }

    @Override
    public int getCount() {
        // Возвращаем количество использований (0 если истощён)
        return isDepleted() ? 0 : remainingUses;
    }

    @Override
    public String getDisplayName() {
        String baseName = super.getDisplayName();
        return baseName + " (" + remainingUses + "/" + maxUses + ")";
    }

    public String getTooltipText() {
        return "<html>" + type.icon + " " + type.name + "<br>" +
                "❤ Восстанавливает: " + healAmount + " HP<br>" +
                "📦 Осталось использований: " + remainingUses + "/" + maxUses + "<br>" +
                type.description + "</html>";
    }


}