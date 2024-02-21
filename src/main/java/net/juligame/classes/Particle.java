package net.juligame.classes;

import net.juligame.Window;
import net.juligame.classes.utils.Side;
import net.juligame.classes.utils.Vector2;

import java.awt.*;

public class Particle {
    public static int TILE_SIZE = 1;
//    public static int TILE_SIZE = 6;
    public Color color;
    public Vector2 velocity = new Vector2(0, 0);
    public float x, y;


    public Particle(){
        id = -1;
    }

    public Particle(Color color, float x, float y) {
        ChangeColor(color);
        this.x = x;
        this.y = y;

        if (x < 0 || x >= Window.tileMap.tiles.length || y < 0 || y >= Window.tileMap.tiles[0].length) {
            return;
        }

        Window.tileMap.AddParticleToAddQueue(this);
    }

    public byte r, g, b;
    public void ChangeColor(Color color) {
        this.color = color;
        r = (byte) color.getRed();
        g = (byte) color.getGreen();
        b = (byte) color.getBlue();
    }

    public void tick() {
        Particle down = getSide(Side.BOTTOM);
//        if (down == null) {
            velocity.y = Math.min(velocity.y + .1f, 5);
            velocity.y = Math.max(velocity.y, 1);
//            velocity.y = 1;
//        } else {
//            velocity.y = 0;
//        }

        Window.tileMap.MoveTile(this);
    }

    public Particle getSide(Side side) {
        return switch (side) {
            case TOP -> Window.tileMap.getTile((int) x, (int) y - 1);
            case BOTTOM -> Window.tileMap.getTile((int) x, (int) y + 1);
            case LEFT -> Window.tileMap.getTile((int) x - 1, (int) y);
            case RIGHT -> Window.tileMap.getTile((int) x + 1, (int) y);
        };
    }

    public void updatePosition(float x, float y) {
        if (x == this.x && y == this.y)
            return;

        tickNeighbours();

        Window.tileMap.tiles[(int) this.x][(int) this.y] = null;
        this.x = x;
        this.y = y;
        Window.tileMap.tiles[(int) x][(int) y] = this;

//        tickNeighbours();

        // duped
        Window.tileMap.AddParticleToTickQueue(this);
    }

    public void tickNeighbours() {
        Side.getSides().forEach(side -> {
            Particle tile = getSide(side);
            if (tile == null) {
//                System.out.println("Tile was null ticking neighbours");
                return;
            }
            if (tile == this) {
//                System.out.println("Tile is itself when ticking neighbours");
                return;
            }
            if (tile.getID() == -1)
                return;


            // aca se genera primero?
            Window.tileMap.AddParticleToTickQueue(tile);
//            tile.tick();
        });
    }

    public static int idCounter = 0;
    public int id;
    public int getID() {
        return id;
    }
}
