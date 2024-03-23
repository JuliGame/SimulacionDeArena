package net.juligame.classes.threading;

import net.juligame.Window;
import net.juligame.classes.Particle;
import net.juligame.classes.utils.ColorUtils;
import net.juligame.classes.utils.Vector2Int;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TileMapChanges {

    public static class TileMapChange {
        public Particle[] particlesToUpdate;
        public Particle particle;
        public Vector2Int to;
        public TileMapChange(Particle particle, Vector2Int to) {
            this.particle = particle;
            this.to = to;
        }
    }
    private static int WIDTH;
    private static int HEIGHT;
    private static Particle[] particles;
    public static void setTilemap(int WIDTH, int HEIGHT) {
        TileMapChanges.particles = new Particle[WIDTH * HEIGHT];
        TileMapChanges.WIDTH = WIDTH;
        TileMapChanges.HEIGHT = HEIGHT;
    }


    private static final Queue<Particle> particlesToAdd = new LinkedList<>();
    public static void addParticle(Particle particle) {
        particlesToAdd.add(particle);
    }
    private static final Queue<Vector2Int> particlesToRemove = new LinkedList<>();
    public static void removeParticle(Vector2Int pos) {
        particlesToRemove.add(pos);
    }
    public static final List<TileMapChange> changes = new LinkedList<>();
    public static void changeParticle(TileMapChange change) {
        changes.add(change);
    }


    public static void RemoveParticle(int x, int y){
        particles[x + y * WIDTH] = null;
    }
    public static Particle[] Resove() {
//        while (!particlesToRemove.isEmpty()) {
//            Vector2Int pos = particlesToRemove.poll();
//            particles[pos.x + pos.y * WIDTH] = null;
//            Window.tileMap.ChangeColor(pos.x, pos.y, Color.BLACK.getRGB());
//        }

//        boolean[] fromBools = new boolean[WIDTH * HEIGHT];

        for (TileMapChange c : changes) {
            Vector2Int from = new Vector2Int(c.particle.x, c.particle.y);
            particles[from.x + from.y * WIDTH] = null;
        }

        while (!particlesToAdd.isEmpty()) {
            Particle p = particlesToAdd.poll();
            Vector2Int to = new Vector2Int(p.x, p.y);
            Vector2Int found = findFreSpot(to);
            if (found == null){
                System.out.println("Failed to move particle");
                continue;
            }
            p.x = found.x;
            p.y = found.y;
            particles[p.x + p.y * WIDTH] = p;
        }

        for (TileMapChange c : changes) {
            Vector2Int to = c.to;
            Particle p = c.particle;

            Vector2Int found = findFreSpot(to);
            if (found == null){
                System.out.println("Failed to move particle");
                continue;
            }
            p.x = found.x;
            p.y = found.y;
            particles[p.x + p.y * WIDTH] = p;
//|| fromBools[to.x + to.y * WIDTH - 1] || fromBools[to.x + 1 + to.y * WIDTH] || fromBools[to.x - 1 + to.y * WIDTH
        }

//        if (!changes.isEmpty())
//            System.out.println("Changes: " + changes.size());

        changes.clear();

        return particles.clone();
    }

//    private static final Vector2Int[] directions = new Vector2Int[] {
//            new Vector2Int(0, 1),
//            new Vector2Int(0, -1),
//            new Vector2Int(1, 0),
//            new Vector2Int(-1, 0)
//    };
//
//    public static Vector2Int findFreSpot(Vector2Int start, List<Vector2Int> triedPositions, boolean debug) {
//        System.out.println("Finding free spot");
//        if (debug)
//            Window.tileMap.ChangeColor(start.x, start.y, Color.WHITE.getRGB());
//        Particle p = particles[start.x + start.y * WIDTH];
//        if (p == null) {
//            if (debug)
//                Window.tileMap.ChangeColor(start.x, start.y, Color.RED.getRGB());
//            return start;
//        }
//        for (Vector2Int dir : directions) {
//            Vector2Int newPos = start.clone().add(dir);
//            if (newPos.x < 0 || newPos.x >= WIDTH || newPos.y < 0 || newPos.y >= HEIGHT)
//                continue;
//            if (triedPositions.contains(newPos))
//                continue;
//            triedPositions.add(newPos);
//            Vector2Int found = findFreSpot(newPos, triedPositions, debug);
//            if (found != null)
//                return found;
//        }
//        System.out.println("Failed to find free spot");
//        return null;
//    }

    public static Vector2Int findFreSpot(Vector2Int start) {
        start = start.clone();

        Color debugColor = Color.WHITE;
        if (particles[start.x + start.y * WIDTH] == null)
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

                    if (particles[newX + newY * WIDTH] == null) {
                        return new Vector2Int(newX, newY);
                    }
                }
            }
        }
        return null;
    }

}