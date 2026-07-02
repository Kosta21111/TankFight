package world;

import java.awt.Point;
import java.util.*;

public class PathFinder {
    private GameWorld world;

    public PathFinder(GameWorld world) {
        this.world = world;
    }

    public List<Point> findPath(int startX, int startY, int targetX, int targetY) {
        int gridWidth = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE;
        int gridHeight = GameWorld.FIELD_SIZE / GameWorld.CELL_SIZE;

        boolean[][] visited = new boolean[gridWidth][gridHeight];
        Point[][] previous = new Point[gridWidth][gridHeight];
        Queue<Point> queue = new LinkedList<>();

        queue.add(new Point(startX, startY));
        visited[startX][startY] = true;

        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.x == targetX && current.y == targetY) {
                List<Point> path = new ArrayList<>();
                Point step = new Point(targetX, targetY);
                while (!(step.x == startX && step.y == startY)) {
                    path.add(0, step);
                    step = previous[step.x][step.y];
                }
                return path;
            }

            for (int i = 0; i < 4; i++) {
                int newX = current.x + dx[i];
                int newY = current.y + dy[i];
                if (newX >= 0 && newX < gridWidth && newY >= 0 && newY < gridHeight &&
                        !visited[newX][newY] && world.canMoveTo(newX, newY)) {
                    visited[newX][newY] = true;
                    previous[newX][newY] = current;
                    queue.add(new Point(newX, newY));
                }
            }
        }
        return new ArrayList<>();
    }
}