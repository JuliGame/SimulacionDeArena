package net.juligame.classes;

import net.juligame.Main;
import net.juligame.classes.threading.QueueUtils;
import net.juligame.classes.threading.WorkerThread;
import net.juligame.classes.utils.Side;
import net.juligame.classes.utils.Vector2;
import net.juligame.classes.utils.Vector2Int;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lwjgl.opengl.GL11.*;


public class TileMap {
    public List<Particle> particles = new ArrayList<>();
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
        for (int i = 0; i < randomFloatOfThisTick.length; i++) {
            randomFloatOfThisTick[i] = randomClass.nextFloat();
        }
        Side.preComputeRandomSidesArray();
    }
    // region Render
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
    public void reDrawTexture() {
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
        glBindTexture(GL_TEXTURE_2D, tex);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, b);

        b.clear();
    }

    public void draw(){
        glBindTexture(GL_TEXTURE_2D, tex);
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
    // endregion

    // region Ticking
    int tick = 0;
    boolean paused;
    public void Pause(){
        paused = !paused;
    }
    boolean ctrlZ = false;
    public void SendCtrlZ() {
        ctrlZ = true;
    }
    public ArrayList<ArrayList<Particle>> history = new ArrayList<>();
    private final Queue<Particle> queuedParticlesToAdd = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private Queue<Particle> queuedParticlesToTick = new java.util.concurrent.ConcurrentLinkedQueue<>();
    public float[] randomFloatOfThisTick = new float[100];
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
            tick = 0;

            for (int i = 0; i < randomFloatOfThisTick.length; i++) {
                randomFloatOfThisTick[i] = randomClass.nextFloat();
            }
        }

        if (ctrlZ){
            ctrlZ = false;
            if (!history.isEmpty()){
                ArrayList<Particle> h = history.get(history.size() - 1);
                history.remove(history.size() - 1);
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

        Main.debug.TickingParticles = queuedParticlesToTick.size();

        if (paused)
            return;

        tick++;
        CalculateWindForce();
        Queue<Particle> queuedParticlesToTick = new java.util.LinkedList<>(this.queuedParticlesToTick);
        this.queuedParticlesToTick = new java.util.concurrent.ConcurrentLinkedQueue<>();
        alreadyAddedToTickQueue = new boolean[width * height];

        int threads = 20;
        Queue<Particle>[] queuedParticlesList = QueueUtils.splitQueue(queuedParticlesToTick, threads * 5);
        ExecutorService executor = Executors.newFixedThreadPool(threads);//creating a pool of 5 threads
        for (int i = 0; i < threads * 5; i++) {
            Runnable worker = new WorkerThread(queuedParticlesList[i]);
            executor.execute(worker);//calling execute method of ExecutorService
        }
        executor.shutdown();
        while (!executor.isTerminated()) {   }

        System.out.println("Finished all threads");



//        while (!queuedParticlesToTick.isEmpty()) {
//            Particle particle = queuedParticlesToTick.poll();
//            if (particle == null)
//                continue;
//
//            particle.tick();
//        }
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
        if (alreadyAddedToAddQueue[(particle.x) + (particle.y * width)]) {
            return;
        }


        queuedParticlesToAdd.add(particle);
        alreadyAddedToAddQueue[(int) (particle.x) + ((int) particle.y * width)] = true;
    }
    // endregion


    public Particle[][] tiles;
    private final Particle voidParticle = new Particle();
    public Particle getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return voidParticle;
        }

        return tiles[x][y];
    }

    // region physics
    Random randomClass = new Random();
    public void MoveTile(Particle particle) {
         Vector2 velocity = particle.velocity.clone();

         velocity.y += particle.burnVelocity.y;
         velocity.x += particle.burnVelocity.x;

//        Vector2 velocity = new Vector2(width / 2 - particle.x, height / 2 - particle.y).Normalize().Multiply(5);

        int velocityX = (int) Math.floor(Math.abs(velocity.x));
        float velocityXRemainder =  Math.abs(velocity.x) - velocityX;

        int velocityY = (int) Math.abs(velocity.y);
        float velocityYRemainder =  Math.abs(velocity.y) - velocityY;

        double randomNum = randomFloatOfThisTick[(particle.getID() + tick) % randomFloatOfThisTick.length];
        if (randomNum < Math.abs(velocityXRemainder)) {
            velocity.x += velocity.x > 0 ? 1 : -1;
        } else {
            velocity.x = velocity.x > 0 ? velocityX : -velocityX;
        }

        if (randomNum < Math.abs(velocityYRemainder)) {
            velocity.y += velocity.y > 0 ? 1 : -1;
        }else {
            velocity.y = velocity.y > 0 ? velocityY : -velocityY;
        }


        int x = particle.x;
        int y = particle.y;

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
            Vector2Int goTo = new Vector2Int(toReachX - x, toReachY - y);
            int toTestX = x + (int) Math.signum(goTo.x);
            int toTestY = y + (int) Math.signum(goTo.y);

            Particle pushedTile = getTile(toTestX, toTestY);
            if (pushedTile != null) {
                if (pushedTile.id == -1)
                    break;


                List<Side> sides = Side.getSidesRandomized(tick + particle.getID());
                for (Side side : sides) {
                    Vector2 sideVector = side.getVector();
                    int nx = (pushedTile.x) + ((int) sideVector.x);
                    int ny = (pushedTile.y) + ((int) sideVector.y);

                    Particle pushedTileSide = getTile(nx, ny);
                    if (pushedTileSide != null)
                        continue;

                    x = nx;
                    y = ny;
                    break;
                }

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

    public void CalculateWindForce() {
        int depth = 10;
        Vector2 force = new Vector2(0, 0);
        for (int i = 1; i < depth; i++) {
            float influence = 1f / i;
            force.x += (float) (Math.sin((tick / 60f) * ((i * .9f + 2) / 5)) * influence);
            force.y += (float) (Math.sin((tick / 60f) * ((i * .9f + 3) / 5)) * influence);
        }
        force.x *= Main.config.windForce.x;
        force.y *= Main.config.windForce.y;

        Particle.WindForce = force;
    }

    // endregion
}
