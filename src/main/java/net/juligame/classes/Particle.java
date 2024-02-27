package net.juligame.classes;

import net.juligame.Main;
import net.juligame.Window;
import net.juligame.classes.threading.TileMapChanges;
import net.juligame.classes.utils.ColorUtils;
import net.juligame.classes.utils.Side;
import net.juligame.classes.utils.Vector2;

import java.awt.*;

public class Particle {
//    public static int TILE_SIZE = 1;
    public static int TILE_SIZE = 8;
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

    public void SendColorUpdate() {
        Window.tileMap.ChangeColor(x, y, ColorUtils.addColors(color, colorOverlay).getRGB());
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
    public static Vector2 Gravity = Main.config.gravity;
    public TileMapChanges.TileMapChange tick() {
        if (System.currentTimeMillis() - burnAtUnix < 0) {
            float burn = (float) (burnAtUnix - System.currentTimeMillis()) / burnTime;
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
