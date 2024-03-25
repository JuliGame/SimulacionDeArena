package net.juligame.classes;

import net.juligame.Main;
import net.juligame.Window;
import net.juligame.classes.threading.TileMapChanges;
import net.juligame.classes.utils.Side;
import net.juligame.classes.utils.Vector2;
import net.juligame.classes.utils.Vector2Int;

import java.awt.*;
import java.util.List;

public class Particle {
    public static int TILE_SIZE = 1;
//    public static int TILE_SIZE = 2;
    public Color color;
    public Color colorOverlay = Color.BLACK;
    public Vector2 velocity = new Vector2(0, 0);
    public int x;
    public int y;


    public Particle(){
        id = -1;
    }

    public Particle(Color color, int x, int y) {
        this.color = color;
        this.x = x;
        this.y = y;

        if (x < 0 || x >= Window.tileMap.width || y < 0 || y >= Window.tileMap.height) {
            return;
        }

        Window.tileMap.AddParticleToAddQueue(this);
    }


    private int burnAtTick = 0;
    private int burnTime = 1;
    public Vector2 burnVelocity = new Vector2(0, 0);
    public void SetVelocityWithTimeBurn(Vector2 velocity, int burnInTicks) {
        burnVelocity = velocity;
        this.burnAtTick = Window.tileMap.tick + burnInTicks;
        this.burnTime = burnInTicks;
    }

    public static Vector2 WindForce = new Vector2(0, 0);
    public static Vector2 Gravity = Main.config.gravity;
    public TileMapChanges.TileMapChange tick() {
        if (Window.tileMap.tick - burnAtTick < 0) {
            float burn = (float) (burnAtTick - Window.tileMap.tick) / burnTime;
            burnVelocity.Multiply(burn);
        } else {
            burnVelocity = new Vector2(0, 0);
        }

        velocity.y += Gravity.y;
        velocity.x += Gravity.x;

        velocity.x += WindForce.x;
        velocity.y += WindForce.y;

        velocity = velocity.Multiply(0.95f);

        return Window.tileMap.MoveTile(this);
    }

    public Particle getSide(Side side, Vector2Int pos) {
        switch (side) {
            case TOP:
                return Window.tileMap.getTile(pos.x, pos.y - 1);
            case BOTTOM:
                return Window.tileMap.getTile(pos.x, pos.y + 1);
            case LEFT:
                return Window.tileMap.getTile(pos.x - 1, pos.y);
            case RIGHT:
                return Window.tileMap.getTile(pos.x + 1, pos.y);
        }
        return null;
    }

    public Particle[] getNeighbours(Vector2Int pos, boolean includeThis) {
        Particle[] tiles = new Particle[]{
                Window.tileMap.getTile(pos.x, pos.y - 1),
                Window.tileMap.getTile(pos.x, pos.y - 1),
                Window.tileMap.getTile(pos.x - 1, pos.y),
                Window.tileMap.getTile(pos.x + 1, pos.y)
        };

        List<Particle> particles = new java.util.ArrayList<>();
        for (Particle tile : tiles) {
            if (tile == null || tile.getID() == -1)
                continue;

            particles.add(tile);
        }

        if (includeThis)
            particles.add(this);

        return particles.toArray(new Particle[0]);
    }

    public  Particle[] getAllNeighbours(Vector2Int pos, boolean includeThis) {
        Particle[] tiles = new Particle[]{
                Window.tileMap.getTile(pos.x, pos.y - 1),
                Window.tileMap.getTile(pos.x, pos.y - 1),
                Window.tileMap.getTile(pos.x - 1, pos.y),
                Window.tileMap.getTile(pos.x + 1, pos.y),
                Window.tileMap.getTile(pos.x - 1, pos.y - 1),
                Window.tileMap.getTile(pos.x + 1, pos.y - 1),
                Window.tileMap.getTile(pos.x - 1, pos.y + 1),
                Window.tileMap.getTile(pos.x + 1, pos.y + 1),
        };

        List<Particle> particles = new java.util.ArrayList<>();
        for (Particle tile : tiles) {
            if (tile == null || tile.getID() == -1)
                continue;

            particles.add(tile);
        }

        if (includeThis)
            particles.add(this);

        return particles.toArray(new Particle[0]);
    }
    public static int idCounter = 0;
    public int id;
    public int getID() {
        return id;
    }
}
