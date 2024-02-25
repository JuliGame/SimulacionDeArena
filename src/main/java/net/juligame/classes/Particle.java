package net.juligame.classes;

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

    public void tick() {
        if (velocity.y == 0)
            velocity.y = 1f;

        velocity.y = velocity.y + 0.2f;

        velocity = velocity.Multiply(0.9f);

        // reduce x velocity
//        if (velocity.x > 0) {
//            velocity.x -= 0.1f;
//        } else if (velocity.x < 0) {
//            velocity.x += 0.1f;
//        }

//        velocity.y = Math.min(velocity.y + .1f, 5);
//        velocity.y = Math.max(velocity.y, 1);

//        System.out.println("Velocity: " + velocity.x + ", " + velocity.y);
        Window.tileMap.MoveTile(this);
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

//        tickNeighbours();
        tickAllNeighbours();

        Window.tileMap.tiles[(int) this.x][(int) this.y] = null;
        Window.tileMap.ChangeColor((int) this.x, (int) this.y, 0);
        this.x = x;
        this.y = y;
        Window.tileMap.tiles[(int) x][(int) y] = this;

//        tickNeighbours();

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
                this
        };

        Side[] topSides = new Side[]{Side.TOP, Side.BOTTOM};
        for (Particle horTile : tiles) {
            if (horTile == null)
                continue;

            Arrays.asList(topSides).forEach(side -> {
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
    }
    public static int idCounter = 0;
    public int id;
    public int getID() {
        return id;
    }
}
