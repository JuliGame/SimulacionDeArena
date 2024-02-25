package net.juligame.classes;

import net.juligame.Main;
import net.juligame.Window;
import net.juligame.classes.utils.ColorUtils;
import net.juligame.classes.utils.Side;
import net.juligame.classes.utils.Vector2;

import java.awt.*;

public class Particle {
    public static int TILE_SIZE = 1;
//    public static int TILE_SIZE = 4;
    public Color color;
    public Color colorOverlay = Color.BLACK;
    public Vector2 velocity = new Vector2(0, 0);
    public int x, y;


    public Particle(){
        id = -1;
    }

    public Particle(Color color, int x, int y) {
        this.color = color;
        this.x = x;
        this.y = y;

        if (x < 0 || x >= Window.tileMap.tiles.length || y < 0 || y >= Window.tileMap.tiles[0].length) {
            return;
        }

        Window.tileMap.AddParticleToAddQueue(this);
    }

    public void SendColorUpdate() {
        Window.tileMap.ChangeColor((int) x, (int) y, ColorUtils.addColors(color, colorOverlay).getRGB());
    }

    private long burnAtUnix = 0;
    private int burnTime = 1;
    public Vector2 burnVelocity = new Vector2(0, 0);
    public void SetVelocityWithTimeBurn(Vector2 velocity, int burnIn) {
        burnVelocity = velocity;
        this.burnAtUnix = System.currentTimeMillis() + burnIn;
        this.burnTime = burnIn;
    }

    public static Vector2 WindForce = new Vector2(0, 0);
    public void tick() {
        if (!ShouldTick())
            return;

        if (System.currentTimeMillis() - burnAtUnix < 0) {
            float burn = (float) (burnAtUnix - System.currentTimeMillis()) / burnTime;
            burnVelocity.Multiply(burn);
        } else {
            burnVelocity = new Vector2(0, 0);
        }

        velocity.y += Main.config.gravity.y;
        velocity.x += Main.config.gravity.x;

        velocity.x += WindForce.x;
        velocity.y += WindForce.y;

        velocity = velocity.Multiply(0.95f);

        Window.tileMap.MoveTile(this);

    }

    public boolean ShouldTick() {
        if (System.currentTimeMillis() - burnAtUnix < 0)
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
                return Window.tileMap.getTile(x, y - 1);
            case BOTTOM:
                return Window.tileMap.getTile(x, y + 1);
            case LEFT:
                return Window.tileMap.getTile(x - 1, y);
            case RIGHT:
                return Window.tileMap.getTile(x + 1, y);
        }
        return null;
    }

    public void updatePosition(int x, int y) {
        if (x == this.x && y == this.y)
            return;

        tickAllNeighbours();

        Window.tileMap.tiles[this.x][this.y] = null;
        Window.tileMap.ChangeColor(this.x, this.y, 0);
        this.x = x;
        this.y = y;
        Window.tileMap.tiles[x][y] = this;

//        tickAllNeighbours();

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
        Particle[] tiles = new Particle[]{
                Window.tileMap.getTile(x, y - 1),
                Window.tileMap.getTile(x, y - 1),
                Window.tileMap.getTile(x - 1, y),
                Window.tileMap.getTile(x + 1, y),
                Window.tileMap.getTile(x - 1, y - 1),
                Window.tileMap.getTile(x + 1, y - 1),
                Window.tileMap.getTile(x - 1, y + 1),
                Window.tileMap.getTile(x + 1, y + 1),
        };

        for (Particle tile : tiles) {
            if (tile == null || tile.getID() == -1)
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
