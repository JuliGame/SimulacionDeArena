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
    public static void setTilemap(Particle[] particles, int WIDTH, int HEIGHT) {
        TileMapChanges.particles = particles.clone();
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
    private static final List<TileMapChange> changes = new LinkedList<>();
    public static void changeParticle(TileMapChange change) {
        changes.add(change);
    }

    public static Particle[] Resove() {
//        while (!particlesToRemove.isEmpty()) {
//            Vector2Int pos = particlesToRemove.poll();
//            particles[pos.x + pos.y * WIDTH] = null;
//            Window.tileMap.ChangeColor(pos.x, pos.y, Color.BLACK.getRGB());
//        }

        for (TileMapChange c : changes) {
            Vector2Int from = new Vector2Int(c.particle.x, c.particle.y);
            particles[from.x + from.y * WIDTH] = null;
            Window.tileMap.ChangeColor(from.x, from.y, Color.BLACK.getRGB());
        }

        // Esto de las ids es por un bug re de mierda.
        // Si yo no agrego las particulas al spawnearlas.

        // ----------------
        // t0:
        // no hay particulas.
        // al final se tickean las particulas spawneadas en t0

        // t1:
        // cuando ponga una particula, checkea con las de los tiles, que fueron calculadas en t0
        // no hay bug, excepto si una particula no se movio en el primer tick.
        // si no hubo movimiento, no se llama update pos, y si no se llama update pos.
        // nunca se llama ChangeTile, por lo cual no es agregada a la lista de cambios.
        // Y esto genera un bug donde mi lista del tilemap tiene la particula, pero el array de particulas no.

        // ----------------
        // Si nosotros añadimos un addParticle en t0.
        // Las particulas que si son tickeadas al inicio, se tickean 2 veces, en el addparticle, y en el chance.
        // lo que genera otro bug.

        // -- Solucion --
        // Añadir esta lista para chequear si la particula fue añadida en el tick actual.
        // Buscar una solucion mas elegante. Porque un contains asi de grande es nos laguea mucho.

        List<Integer> ids = new LinkedList<>();
        while (!particlesToAdd.isEmpty()) {
            Particle p = particlesToAdd.poll();
            Vector2Int to = new Vector2Int(p.x, p.y);
            Vector2Int found = findFreSpot(to, false);
            if (found == null){
                System.out.println("Failed to move particle");
                continue;
            }
            p.x = found.x;
            p.y = found.y;
            particles[p.x + p.y * WIDTH] = p;
            p.SendColorUpdate();
            ids.add(p.id);
        }

        for (TileMapChange c : changes) {
            Vector2Int to = c.to;
            Particle p = c.particle;
            if (ids.contains(p.id))
                continue;

            Vector2Int found = findFreSpot(to, false);
            if (found == null){
                System.out.println("Failed to move particle");
                continue;
            }
            p.x = found.x;
            p.y = found.y;
            particles[p.x + p.y * WIDTH] = p;
            p.SendColorUpdate();
        }

        if (!changes.isEmpty())
            System.out.println("Changes: " + changes.size());

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

    public static Vector2Int findFreSpot(Vector2Int start, boolean debug) {
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
                    Particle p = particles[newX + newY * WIDTH];
                    if (debug) {
                        debugColor = ColorUtils.Darken(debugColor, 1);
                        Window.tileMap.ChangeColor(newX, newY, debugColor.getRGB());
                    }
                    if (p == null) {
                        if (debug)
                            Window.tileMap.ChangeColor(newX, newY, Color.RED.getRGB());
                        return new Vector2Int(newX, newY);
                    }
                }
            }
        }
        return null;
    }

}