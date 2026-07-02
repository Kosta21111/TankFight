package save;

public class SaveSlotInfo {
    public int slotNumber;
    public String slotName;
    public SaveInfo saveInfo;
    public boolean isEmpty;

    public SaveSlotInfo(int slotNumber, String slotName, SaveInfo saveInfo) {
        this.slotNumber = slotNumber;
        this.slotName = slotName;
        this.saveInfo = saveInfo;
        this.isEmpty = (saveInfo == null);
    }
}