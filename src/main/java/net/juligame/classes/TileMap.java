package net.juligame.classes;

import net.juligame.Main;
import net.juligame.classes.utils.Side;
import net.juligame.classes.utils.Vector2;

import java.util.ArrayList;
import java.util.List;


public class TileMap {

    List<Particle> particles = new ArrayList<>();

    public void Reset() {
        particles.clear();
        tiles = new Particle[width][height];
    }


    int width, height;
    public TileMap(int width, int height) {
        tiles = new Particle[width][height];
        this.width = width;
        this.height = height;
    }

    public void draw() {
        particles.forEach(Particle::draw);
    }


    public void Tick() {
        particles.forEach(Particle::tick);
    }

    public Particle[][] tiles;

    private Particle voidParticle = new Particle();
    public Particle getTile(int x, int y) {
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length) {
            return voidParticle;
        }

        return tiles[x][y];
    }

    public void MoveTile(Particle particle) {
        // trace a line from the current position to the new position
        // if the line intersects with a tile, stop the movement
        float x = particle.x;
        float y = particle.y;

        Vector2 velocity = particle.velocity;

        int toReachX = (int) (particle.x + velocity.x);
        int toReachY = (int) (particle.y + velocity.y);

//        if (toReachX < 0 || toReachX >= tiles.length || toReachY < 0 || toReachY >= tiles[0].length) {
//            return;
//        }


        while (true) {
            int toTestX = (int) (x + Math.signum(velocity.x));
            int toTestY = (int) (y + Math.signum(velocity.y));

            Particle pushedTile = getTile(toTestX, toTestY);
            if (pushedTile != null) {
                System.out.println("Tile found");
                Side moveSide = velocity.x > 0 ? Side.LEFT : velocity.x < 0 ? Side.RIGHT : velocity.y > 0 ? Side.TOP : Side.BOTTOM;

                if (moveSide == Side.TOP || moveSide == Side.BOTTOM) {
                    System.out.println("Bottom");
                    boolean random = Math.random() > 0.5;
                    Particle first;
                    Particle second;

                    if (random) {
                        first = getTile(toTestX + 1, toTestY);
                        second = getTile(toTestX - 1, toTestY);
                    } else {
                        first = getTile(toTestX - 1, toTestY);
                        second = getTile(toTestX + 1, toTestY);
                    }


                    if (first == null) {
                        x = random ? toTestX + 1 : toTestX - 1;
                        y = toTestY;
                    } else if (second == null) {
                        x = random ? toTestX - 1 : toTestX + 1;
                        y = toTestY;
                    } else {
                        System.out.println("No space to move");
                        break;
                    }
                }

                System.out.println("Tile found");
                break;
            }

            x = toTestX;
            y = toTestY;

            if (x == toReachX && y == toReachY) {
                break;
            }
        }

        particle.updatePosition(x, y);
    }
}
