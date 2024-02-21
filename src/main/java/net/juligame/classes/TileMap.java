package net.juligame.classes;

import net.juligame.classes.utils.Side;
import net.juligame.classes.utils.Vector2;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.lwjgl.opengl.GL11.*;


public class TileMap {
    List<Particle> particles = new ArrayList<>();
    boolean queueReset = false;
    public void Reset() {
        queueReset = true;
    }


    int width, height;
    public TileMap(int width, int height) {
        tiles = new Particle[width][height];
        this.width = width;
        this.height = height;
        alreadyAddedToTickQueue = new boolean[width * height];
        alreadyAddedToAddQueue = new boolean[width * height];
    }

//    public void draw() {
////        Arrays.stream(tiles).forEach(tiles -> Arrays.stream(tiles).forEach(tile -> {
////            if (tile != null) {
////                tile.draw();
////            }
////        }));
//
//        for (int x = 0; x < particles.size(); x++) {
//            particles.get(x).draw();
//        }
//    }
    int tex;
    ByteBuffer b;
    public void initTextureAllocations(){
        b = ByteBuffer.allocateDirect(3 * width * height);
        tex = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, tex);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, b);
    }
    public void draw() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Particle particle = tiles[x][y];
                if (particle == null) {
                    b.put((byte) 0);
                    b.put((byte) 0);
                    b.put((byte) 0);
                    continue;
                }

                b.put(particle.r);
                b.put(particle.g);
                b.put(particle.b);
            }
        }

        b.flip();
//        glBindTexture(GL_TEXTURE_2D, tex);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, b);

//        glClear(GL_COLOR_BUFFER_BIT);
//        glPushMatrix();

        glBegin(GL_QUADS);
            glTexCoord2f(0, 0);
            glVertex2f(0, 0);
            glTexCoord2f(1, 0);
            glVertex2f(width * Particle.TILE_SIZE, 0);
            glTexCoord2f(1, 1);
            glVertex2f(width * Particle.TILE_SIZE, height * Particle.TILE_SIZE);
            glTexCoord2f(0, 1);
            glVertex2f(0, height * Particle.TILE_SIZE);
        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);

        glPopMatrix();
    }


    private final Queue<Particle> queuedParticlesToAdd = new java.util.LinkedList<>();
    private final Queue<Particle> queuedParticlesToTick = new java.util.LinkedList<>();
    public void Tick() {
        if (queueReset) {
            queueReset = false;
            particles.clear();
            tiles = new Particle[width][height];
            queuedParticlesToAdd.clear();
            queuedParticlesToTick.clear();
            Particle.idCounter = 0;
        }

//        particles.forEach(particle -> particle.tick());
//        System.out.println("Particles to tick: " + queuedParticlesToTick.size());
        while (!queuedParticlesToAdd.isEmpty()) {
            Particle particle = queuedParticlesToAdd.poll();
            if (particle == null) {
                System.out.println("queuedParticlesToAdd.size(): " + queuedParticlesToAdd.size());
                System.out.println("Particle is null");
                continue;
            }

            if (tiles[(int) particle.x][(int) particle.y] != null){
                tiles[(int) particle.x][(int) particle.y].ChangeColor(particle.color);
                continue;
            }

            particle.id = Particle.idCounter++;

            particles.add(particle);
            tiles[(int) particle.x][(int) particle.y] = particle;
            AddParticleToTickQueue(particle);
        }

        alreadyAddedToAddQueue = new boolean[width * height];


//        System.out.println("Particles to tick: " + queuedParticlesToTick.size());
        Queue<Particle> queuedParticlesToTick = new java.util.LinkedList<>(this.queuedParticlesToTick);
        this.queuedParticlesToTick.clear();
        alreadyAddedToTickQueue = new boolean[width * height];

        while (!queuedParticlesToTick.isEmpty()) {
            Particle particle = queuedParticlesToTick.poll();
            if (particle == null)
                continue;

            particle.tick();
        }
    }

    private boolean[] alreadyAddedToTickQueue;
    public void AddParticleToTickQueue(Particle particle) {
        if (alreadyAddedToTickQueue[particle.getID()]) {
            return;
        }

        queuedParticlesToTick.add(particle);
        alreadyAddedToTickQueue[particle.getID()] = true;
    }

    private boolean[] alreadyAddedToAddQueue;
    public void AddParticleToAddQueue(Particle particle) {
        if (particle == null) {
            System.out.println("Particle is null");
            return;
        }

        if (alreadyAddedToAddQueue[(int) (particle.x) + ((int) particle.y * width)]) {
            return;
        }

        queuedParticlesToAdd.add(particle);
        alreadyAddedToAddQueue[(int) (particle.x) + ((int) particle.y * width)] = true;
    }



    public Particle[][] tiles;

    private Particle voidParticle = new Particle();
    public Particle getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return voidParticle;
        }

        return tiles[x][y];
    }

    public void MoveTile(Particle particle) {
        // trace a line from the current position to the new position
        // if the line intersects with a tile, stop the movement
        Vector2 velocity = particle.velocity;
        if (velocity.x == 0 && velocity.y == 0) {
            return;
        }


        float x = particle.x;
        float y = particle.y;


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
//                System.out.println("Tile found");
                Side moveSide = velocity.x > 0 ? Side.LEFT : velocity.x < 0 ? Side.RIGHT : velocity.y > 0 ? Side.TOP : Side.BOTTOM;

                if (moveSide == Side.TOP || moveSide == Side.BOTTOM) {
//                    System.out.println("Bottom");
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
//                        System.out.println("No space to move");
                        break;
                    }
                }

//                System.out.println("Tile found");
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
