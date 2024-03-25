package net.juligame.classes;

import net.juligame.Window;
import net.juligame.classes.utils.Vector2Int;

import java.util.ArrayList;
import java.util.List;

public class SubTileMap {
    public List<Particle> particles = new ArrayList<>();
    public List<Particle> particlesToTick = new ArrayList<>();
    public Particle[] tiles = new Particle[width * height];
    public static int width = Window.tileMap.width / 10;
    public static int height = Window.tileMap.height / 10;

    public static final Particle voidParticle = new Particle();


    public SubTileMap(int x, int y) {

    }

    public Particle getTileFromGlobalPos(Vector2Int pos) {
        return getLocalTile(new Vector2Int(pos.x % width, pos.y % height));
    }
    public Particle getLocalTile(Vector2Int pos) {
        return tiles[pos.x + pos.y * width];
    }
    public void addParticle(Particle particle) {
        if (particle == null) {
            // throw stacktrace
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace)
                System.out.println(stackTraceElement);

            return;
        }

        particles.add(particle);
        tiles[particle.x % width + (particle.y % height) * width] = particle;
    }
    public void removeParticle(int x, int y) {
        Particle particle = getTileFromGlobalPos(new Vector2Int(x, y));
        particles.remove(particle);
        tiles[x % width + y % height * width] = null;
    }
}
