package save;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Date;

public class SaveInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public String saveName;
    public Date saveDate;
    public int enemiesKilled;
    public int playerHealth;
    public transient BufferedImage thumbnail;  // transient - не сериализуем

    public SaveInfo(String saveName, Date saveDate, int enemiesKilled, int playerHealth, BufferedImage thumbnail) {
        this.saveName = saveName;
        this.saveDate = saveDate;
        this.enemiesKilled = enemiesKilled;
        this.playerHealth = playerHealth;
        this.thumbnail = thumbnail;
    }
}