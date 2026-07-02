package entities;

public class Hill {
    public int gridX, gridY;
    public int height;
    public boolean isOccupied;

    public Hill(int x, int y, int height) {
        this.gridX = x;
        this.gridY = y;
        this.height = height;
        this.isOccupied = false;
    }
}