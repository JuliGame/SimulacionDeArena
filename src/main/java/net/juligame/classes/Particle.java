package net.juligame.classes;

import net.juligame.Window;
import net.juligame.classes.utils.Side;
import net.juligame.classes.utils.Vector2;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class Particle {
    public static int TILE_SIZE = 16;
    public Color color;
    public Vector2 velocity = new Vector2(0, 0);
    public float x, y;


    public Particle(){}

    public Particle(Color color, float x, float y) {
        this.color = color;
        this.x = x;
        this.y = y;

        if (x < 0 || x >= Window.tileMap.tiles.length || y < 0 || y >= Window.tileMap.tiles[0].length) {
            return;
        }

        if (Window.tileMap.tiles[(int) x][(int) y] != null) {
            Window.tileMap.tiles[(int) x][(int) y].color = color;
            return;
        }

        Window.tileMap.particles.add(this);
        Window.tileMap.tiles[(int) x][(int) y] = this;
    }

    public void draw() {
        int x = (int) this.x;
        int y = (int) this.y;

        glColor3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        glBegin(GL_QUADS);
        glVertex2f(x * TILE_SIZE, y * TILE_SIZE);
        glVertex2f(x * TILE_SIZE + TILE_SIZE, y * TILE_SIZE);
        glVertex2f(x * TILE_SIZE + TILE_SIZE, y * TILE_SIZE + TILE_SIZE);
        glVertex2f(x * TILE_SIZE, y * TILE_SIZE + TILE_SIZE);
        glEnd();
    }

    public void tick() {
        velocity.y = Math.min(velocity.y + .1f, 5);
        velocity.y = Math.max(velocity.y, 1);
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
        Window.tileMap.tiles[(int) this.x][(int) this.y] = null;

        this.x = x;
        this.y = y;
        Window.tileMap.tiles[(int) x][(int) y] = this;
    }
}
