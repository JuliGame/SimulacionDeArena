package net.juligame.classes;

import imgui.ImGui;
import net.juligame.Window;
import net.juligame.classes.utils.ColorUtils;
import net.juligame.classes.utils.Side;
import net.juligame.classes.utils.Vector2;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

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
        colors = new int[width][height];

        // set all the colors to black
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                colors[x][y] = 0;
//            }
//        }
    }

    boolean paused;
    public void Pause(){
//        System.out.println("Paused");
        paused = !paused;
    }
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
                int color = colors[x][y];
                byte r = (byte) ((color >> 16) & 0xFF);
                byte g = (byte) ((color >> 8) & 0xFF);
                byte bl = (byte) (color & 0xFF);

                b.put(r);
                b.put(g);
                b.put(bl);
            }
        }
        b.flip();
//        glBindTexture(GL_TEXTURE_2D, tex);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, b);

        b.clear();
        glClear(GL_COLOR_BUFFER_BIT);
        glPushMatrix();

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


    boolean ctrlZ = false;
    public void SendCtrlZ() {
        ctrlZ = true;
    }

    public ArrayList<ArrayList<Particle>> history = new ArrayList<>();
    private final Queue<Particle> queuedParticlesToAdd = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final Queue<Particle> queuedParticlesToTick = new java.util.concurrent.ConcurrentLinkedQueue<>();
    public void Tick() {
        if (queueReset) {
            queueReset = false;
            particles.clear();
            tiles = new Particle[width][height];
            queuedParticlesToAdd.clear();
            queuedParticlesToTick.clear();
            Particle.idCounter = 0;
            colors = new int[width][height];
            history.clear();
        }

        if (paused)
            return;

        if (ctrlZ){
            ctrlZ = false;
            System.out.println("Historial size: " + history.size());
            if (!history.isEmpty()){
                ArrayList<Particle> h = history.get(history.size() - 1);
                history.remove(history.size() - 1);
                System.out.println("Historial size: " + h.size());
                for (Particle particle : h){
                    queuedParticlesToAdd.remove(particle);
                    queuedParticlesToTick.remove(particle);
                    particles.remove(particle);
                    tiles[(int) particle.x][(int) particle.y] = null;
                    ChangeColor((int) particle.x, (int) particle.y, 0);
                    particle.tickNeighbours();
                    Particle.idCounter--;
                }
            }
        }
        
//        particles.forEach(particle -> particle.tick());
//        System.out.println("Particles to tick: " + queuedParticlesToTick.size());
        ArrayList<Particle> historial = new ArrayList<>();
        while (!queuedParticlesToAdd.isEmpty()) {
            Particle particle = queuedParticlesToAdd.poll();

            if (tiles[(int) particle.x][(int) particle.y] != null){
                tiles[(int) particle.x][(int) particle.y].SendColorUpdate();
                continue;
            }

            particle.id = Particle.idCounter++;

            particles.add(particle);
            tiles[(int) particle.x][(int) particle.y] = particle;
            ChangeColor((int) particle.x, (int) particle.y, particle.color.getRGB());
            AddParticleToTickQueue(particle);
            historial.add(particle);
        }
        if (!historial.isEmpty())
            history.add(historial);

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

    private int[][] colors;
    public void ChangeColor(int x, int y, int color) {
        colors[x][y] = color;
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

    Random random = new Random();
    public void MoveTile(Particle particle) {
         Vector2 velocity = particle.velocity.clone();

//        Vector2 velocity = new Vector2(width / 2 - particle.x, height / 2 - particle.y).Normalize().Multiply(5);
//        System.out.println(velocity.toString());

        int velocityX = (int) Math.abs(velocity.x);
        float velocityXRemainder =  Math.abs(velocity.x) - velocityX;

        int velocityY = (int) Math.abs(velocity.y);
        float velocityYRemainder =  Math.abs(velocity.y) - velocityY;

        if (Math.random() < Math.abs(velocityXRemainder)) {
            velocity.x += velocity.x > 0 ? 1 : -1;
        } else {
            velocity.x += velocity.x > 0 ? velocityX : -velocityX;
        }


        if (Math.random() < Math.abs(velocityYRemainder)) {
            velocity.y += velocity.y > 0 ? 0 : -1;
        }else {
            velocity.y += velocity.y > 0 ? velocityY : -velocityY;
        }


//        System.out.println("Velocity: " + velocity.x + ", " + velocity.y);

        float x = particle.x;
        float y = particle.y;

        int toReachX = (int) (particle.x + velocity.x);
        int toReachY = (int) (particle.y + velocity.y);

        if (toReachX == particle.x && toReachY == particle.y) {
            if (particle.velocity.x == 0 && particle.velocity.y == 0) {
//                System.out.println("No velocity");
                return;
            }

            AddParticleToTickQueue(particle);
            return;
        }

        try {
//            ChangeColor(toReachX, toReachY, Color.WHITE.getRGB());
//            return;
        } catch (Exception e) {

        }

//        System.out.println("velocity2 : " + velocity.x + ", " + velocity.y);
        while (true) {
            Vector2 goTo =  new Vector2(toReachX - x, toReachY - y);
            int toTestX = (int) (x + (int) Math.signum(goTo.x));
            int toTestY = (int) (y + (int) Math.signum(goTo.y));


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
                        particle.color = random ? ColorUtils.Brighten(particle.color, 1) : ColorUtils.Darken(particle.color, 1);
                    } else if (second == null) {
                        x = random ? toTestX - 1 : toTestX + 1;
                        y = toTestY;
                        particle.color = random ? ColorUtils.Darken(particle.color, 2) : ColorUtils.Brighten(particle.color, 2);

                    } else {
//                        System.out.println("No space to move");
                        particle.velocity.x = 0;
                        particle.velocity.y = 0;
                        break;
                    }
                }

//                System.out.println("Tile found");
                particle.velocity.x = 0;
                particle.velocity.y = 1;
                Window.tileMap.AddParticleToTickQueue(particle);
                break;
            }

            x = toTestX;
            y = toTestY;

            if (x == toReachX && y == toReachY) {
//                System.out.println("Reached destination");
//                particle.velocity.y = 1;
                break;
            }
        }

        particle.updatePosition(x, y);
    }
}
