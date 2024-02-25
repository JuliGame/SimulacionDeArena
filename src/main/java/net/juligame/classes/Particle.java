package net.juligame.classes;

import net.juligame.Main;
import net.juligame.Window;
import net.juligame.classes.utils.ColorUtils;
import net.juligame.classes.utils.Side;
import net.juligame.classes.utils.Vector2;

import java.awt.*;
import java.util.Arrays;

public class Particle {
//    public static int TILE_SIZE = 1;
    public static int TILE_SIZE = 4;
    public Color color;
    public Color colorOverlay = Color.BLACK;
    public Vector2 velocity = new Vector2(0, 0);
    public float x, y;


    public Particle(){
        id = -1;
    }

    public Particle(Color color, float x, float y) {
        this.color = color;
        this.x = x;
        this.y = y;

        if (x < 0 || x >= Window.tileMap.tiles.length || y < 0 || y >= Window.tileMap.tiles[0].length) {
            return;
        }

        velocity.y = 1f;
        Window.tileMap.AddParticleToAddQueue(this);
    }

    public void SendColorUpdate() {
        Window.tileMap.ChangeColor((int) x, (int) y, ColorUtils.addColors(color, colorOverlay).getRGB());
    }

    private long velocitySet = 0;
    private float burnPerSecond = 0;
    public void SetVelocityWithTimeBurn(Vector2 velocity, float burnPerSecond) {
        this.velocity = velocity;
        this.burnPerSecond = burnPerSecond;
        velocitySet = System.currentTimeMillis();
    }
    public void tick() {
        if (!ShouldTick())
            return;

        if (velocitySet != 0) {
            long time = System.currentTimeMillis() - velocitySet;
            float burn = Math.min(1, burnPerSecond * time / 1000);
            System.out.println("Mult: " +  (1 - burn));
            velocity = velocity.Multiply(1 - burn);
            velocitySet = 0;
        }

        if (velocity.y == 0)
            velocity.y = 1f;

        velocity.y = velocity.y + 0.14f;

        velocity = velocity.Multiply(0.95f);

        Window.tileMap.MoveTile(this);
    }

    public boolean ShouldTick() {
        if (burnPerSecond != 0)
            return true;

        Particle down = getSide(Side.BOTTOM);
        if (down == null)
            return true;

        Particle left = getSide(Side.LEFT);
        if (left == null)
            return true;

        Particle right = getSide(Side.RIGHT);
        if (right == null)
            return true;

        return false;
    }

    public Particle getSide(Side side) {
        switch (side) {
            case TOP:
                return Window.tileMap.getTile((int) x, (int) y - 1);
            case BOTTOM:
                return Window.tileMap.getTile((int) x, (int) y + 1);
            case LEFT:
                return Window.tileMap.getTile((int) x - 1, (int) y);
            case RIGHT:
                return Window.tileMap.getTile((int) x + 1, (int) y);
        }
        return null;
    }

    public void updatePosition(float x, float y) {
        if (x == this.x && y == this.y)
            return;

        tickAllNeighbours();

        Window.tileMap.tiles[(int) this.x][(int) this.y] = null;
        Window.tileMap.ChangeColor((int) this.x, (int) this.y, 0);
        this.x = x;
        this.y = y;
        Window.tileMap.tiles[(int) x][(int) y] = this;

        SendColorUpdate();
        Window.tileMap.AddParticleToTickQueue(this);
    }

    public void tickNeighbours() {
        Side.getSides().forEach(side -> {
            Particle tile = getSide(side);
            if (tile == null)
                return;

            if (tile == this)
                return;

            if (tile.getID() == -1)
                return;

            Window.tileMap.AddParticleToTickQueue(tile);
        });
    }

    public void tickAllNeighbours() {
        Particle tiles[] = new Particle[]{
                getSide(Side.LEFT),
                getSide(Side.RIGHT),
                getSide(Side.TOP),
                getSide(Side.BOTTOM),
                Window.tileMap.getTile((int) x - 1, (int) y - 1),
                Window.tileMap.getTile((int) x + 1, (int) y - 1),
                Window.tileMap.getTile((int) x - 1, (int) y + 1),
                Window.tileMap.getTile((int) x + 1, (int) y + 1),
        };

        for (Particle tile : tiles) {
            if (tile == null)
                continue;

            if (tile == this) {
                System.out.println("Tile is this ERROR, Something is really bad xD");
                continue;
            }

            if (tile.getID() == -1)
                continue;

            Window.tileMap.AddParticleToTickQueue(tile);
        }
    }
    public static int idCounter = 0;
    public int id;
    public int getID() {
        return id;
    }
}
