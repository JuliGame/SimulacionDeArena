package net.juligame.classes.threading;

import net.juligame.Window;
import net.juligame.classes.Particle;
import net.juligame.classes.SubTileMap;
import net.juligame.classes.TileMap;
import net.juligame.classes.utils.Vector2Int;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class TileMapChanges {

    public static class TileMapChange {
        public Particle[] particlesToUpdate;
        public Particle particle;
        public Vector2Int to;
        public SubTileMap subTileMapFrom;
        public SubTileMap subTileMapTo;
        public TileMapChange(Particle particle, Vector2Int to) {
            this.particle = particle;
            this.to = to;
        }
    }
    private static int WIDTH;
    private static int HEIGHT;
    private static TileMap tileMap;
    public static void setTilemap(int WIDTH, int HEIGHT) {
        TileMapChanges.WIDTH = WIDTH;
        TileMapChanges.HEIGHT = HEIGHT;
        tileMap = Window.tileMap;
    }

    public static final List<TileMapChange> changes = new LinkedList<>();

    public static void Resove() {

//        boolean[] fromBools = new boolean[WIDTH * HEIGHT];

//        for (TileMapChange c : changes) {
//            Vector2Int from = new Vector2Int(c.particle.x, c.particle.y);
//            tileMap.changeParticle(c.particle, c.to);
//        }


        for (TileMapChange c : changes) {
            Vector2Int to = c.to;
            Particle p = c.particle;

            Vector2Int found = findFreSpot(to);
            if (found == null){
                System.out.println("Failed to move particle");
                continue;
            }
            tileMap.moveParticle(p, found);
        }

        changes.clear();
    }

    public static Vector2Int findFreSpot(Vector2Int start) {
        start = start.clone();

        Color debugColor = Color.WHITE;
        if (tileMap.getTile(start.x, start.y) == null)
            return start;

        int maxRange = 1000;

        for (int range = 1; range < maxRange; range++) {
            for (int i = -range; i <= range; i++) {
                int[] xValues = new int[]{i, i, -range, range};
                int[] yValues = new int[]{-range, range, i, i};
                for (int j = 0; j < 4; j++) {
                    int x = xValues[j];
                    int y = yValues[j];

                    int newX = start.x + x;
                    int newY = start.y + y;
                    if (newX < 0 || newX >= WIDTH || newY < 0 || newY >= HEIGHT)
                        continue;

                    if (tileMap.getTile(newX, newY) == null) {
                        return new Vector2Int(newX, newY);
                    }
                }
            }
        }
        return null;
    }

}