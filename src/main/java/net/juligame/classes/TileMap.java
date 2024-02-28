package net.juligame.classes;

import net.juligame.Main;
import net.juligame.Window;
import net.juligame.classes.threading.TileMapChanges;
import net.juligame.classes.utils.ColorUtils;
import net.juligame.classes.utils.Side;
import net.juligame.classes.utils.Vector2;
import net.juligame.classes.utils.Vector2Int;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.lwjgl.opengl.GL11.*;


public class TileMap {
    public List<Particle> particles = new ArrayList<>();
    boolean queueReset = false;
    public void Reset() {
        queueReset = true;
    }
    public int width, height;
    public TileMap(int width, int height) {
        tiles = new Particle[width * height];
        this.width = width;
        this.height = height;
        Window.tileMap = this;
        InitializeThings();
        Side.preComputeRandomSidesArray();
    }

    private void InitializeThings(){
        particles.clear();
        tiles = new Particle[width * height];
        queuedParticlesToAdd.clear();
        queuedParticlesToTick.clear();
        Particle.idCounter = 0;
        history.clear();
        tick = 0;
        alreadyAddedToTickQueue = new boolean[width * height];
        for (int i = 0; i < randomFloatOfThisTick.length; i++)
            randomFloatOfThisTick[i] = randomClass.nextFloat();

        TileMapChanges.setTilemap(width, height);

        if (benchmark) {
            boolean[] occupied = new boolean[width * height];
            for (int i = 0; i < (width * height) / 20f;) {
                int x = randomClass.nextInt(width - 1);
                int y = randomClass.nextInt(height - 1);
                if (occupied[x + y * width])
                    continue;

                occupied[x + y * width] = true;
                Particle particle = new Particle(ColorUtils.GetRandomColorPretty(i * 0.0001f), x, y);
                AddParticleToAddQueue(particle);
                i++;
            }
        }
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
    private int lastRedrawTick = 0;
    public void reDrawTexture() {
        if (lastRedrawTick == tick)
            return;

        int[] colors = new int[width * height];
        for (Particle particle : new ArrayList<>(particles)) {
            if (particle.x < 0 || particle.x >= width || particle.y < 0 || particle.y >= height)
                continue;

            colors[particle.x + particle.y * width] = particle.color.getRGB();
        }

        for (int color : colors) {
            b.put((byte) (color >> 16 & 0xFF));
            b.put((byte) (color >> 8 & 0xFF));
            b.put((byte) (color & 0xFF));
        }

        b.flip();
        glBindTexture(GL_TEXTURE_2D, tex);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, b);

        b.clear();
        lastRedrawTick = tick;
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
    public List<Particle> queuedParticlesToTick = new ArrayList<>();
    public float[] randomFloatOfThisTick = new float[100];
    public int threads = 10;
    boolean[] alreadyAddedToTickQueue2;
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    boolean benchmark = true;
    public void Tick(boolean force) {
        Main.debug.isPaused = paused;

        if (queueReset) {
            queueReset = false;
            InitializeThings();
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
                    removeParticle(particle.x, particle.y);
//                    particle.tickNeighbours();
                    Particle.idCounter--;
                }
            }
        }

        ArrayList<Particle> historial = new ArrayList<>();

        if (!paused || force)
            alreadyAddedToTickQueue2 = new boolean[width * height];

        while (!queuedParticlesToAdd.isEmpty()) {
            Particle particle = queuedParticlesToAdd.poll();

            if (getTile(particle.x, particle.y) != null)
                continue;

            if (alreadyAddedToTickQueue2[particle.x + particle.y * width])
                continue;

            alreadyAddedToTickQueue2[particle.x + particle.y * width] = true;

            particle.id = Particle.idCounter++;
            particles.add(particle);
            AddParticleToTickQueue(particle);
            historial.add(particle);
        }


        if (!historial.isEmpty())
            history.add(historial);


        Main.debug.TickingParticles = queuedParticlesToTick.size();
        Main.debug.Particles = particles.size();

        if (paused && !force)
            return;

        tick++;
        CalculateWindForce();


        List<Future<List<TileMapChanges.TileMapChange>>> futures = new ArrayList<>(threads);
        for (int threadID = 0; threadID < threads; threadID++) {
            List<Particle> toTickClone = new ArrayList<>(this.queuedParticlesToTick);
            int finalThreadID = threadID;
            Future<List<TileMapChanges.TileMapChange>> future = executor.submit(() ->{
                List<TileMapChanges.TileMapChange> particles = new ArrayList<>();
                int remainder = toTickClone.size() % threads;
                int ammount = (int) Math.floor((double) toTickClone.size() / threads);
                if (finalThreadID < remainder)
                    ammount++;

                for (int j = 0; j < ammount; j++) {
                    int index = j * threads + finalThreadID;
                    particles.add(toTickClone.get(index).tick());
                }

                return particles;
            });

            futures.add(future);
        }

        alreadyAddedToTickQueue = new boolean[width * height];
        this.queuedParticlesToTick = new ArrayList<>(width * height);

        int processedCount = 0;
        while (processedCount != threads) {
            boolean[] alreadyAddedToTickQueue = new boolean[width * height];
            for (Future<List<TileMapChanges.TileMapChange>> future : new ArrayList<>(futures)) {
                if (!future.isDone())
                    continue;


                try {
                    List<TileMapChanges.TileMapChange> changes = future.get();

                    for (TileMapChanges.TileMapChange change : changes) {
                        TileMapChanges.changeParticle(change);

                        // optimizar esto, podemos hacer en el multi thread chequear que no este ya para tickear este.
                        if (change.particlesToUpdate == null)
                            continue;

                        for (Particle particleToUpdate : change.particlesToUpdate) {
                            if (alreadyAddedToTickQueue[particleToUpdate.getID()])
                                continue;

                            alreadyAddedToTickQueue[particleToUpdate.getID()] = true;
                            AddParticleToTickQueue(particleToUpdate);
                        }
                    }
                    futures.remove(future);
                    processedCount++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        tiles = TileMapChanges.Resove();

//        if (benchmark)
//            queuedParticlesToTick = new ArrayList<>(particles);

    }

    private boolean[] alreadyAddedToTickQueue;
    public void AddParticleToTickQueue(Particle particle) {
        if (alreadyAddedToTickQueue[particle.getID()]) {
            return;
        }

        queuedParticlesToTick.add(particle);
        alreadyAddedToTickQueue[particle.getID()] = true;
    }

    public void AddParticleToAddQueue(Particle particle) {
        queuedParticlesToAdd.add(particle);
    }
    // endregion


    public Particle[] tiles;
    private final Particle voidParticle = new Particle();
    public Particle getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return voidParticle;
        }

        return tiles[x + y * width];
    }
    public void addParticle(Particle particle) {
        TileMapChanges.addParticle(particle);
    }
    public void removeParticle(int x, int y) {
        TileMapChanges.removeParticle(new Vector2Int(x, y));
    }
    public void changeParticle(Particle particle, Vector2Int to) {
        TileMapChanges.changeParticle(new TileMapChanges.TileMapChange(particle, to));
    }

    // region physics
    Random randomClass = new Random();
    public TileMapChanges.TileMapChange MoveTile(Particle particle) {
         TileMapChanges.TileMapChange change = new TileMapChanges.TileMapChange(particle, new Vector2Int(particle.x, particle.y));

//         if (true)
//             return change;

         Vector2 velocity = particle.velocity.clone();

         velocity.y += particle.burnVelocity.y;
         velocity.x += particle.burnVelocity.x;

//        velocity = new Vector2(width / 2 - particle.x, height / 2 - particle.y).Normalize().Multiply(5);

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
                return change;
            }

            change.particlesToUpdate = new Particle[]{particle};
            return change;
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

        // if particle didn't move return
        if (x == particle.x && y == particle.y) {
                return change;
        }


        change.to = new Vector2Int(x, y);
        change.particlesToUpdate = particle.getNeighbours(new Vector2Int(particle.x, particle.y), true);
        return change;
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
