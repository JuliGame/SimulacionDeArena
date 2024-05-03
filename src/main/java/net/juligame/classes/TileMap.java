package net.juligame.classes;

import net.juligame.Main;
import net.juligame.Window;
import net.juligame.classes.threading.ThreadResult;
import net.juligame.classes.threading.TileMapChanges;
import net.juligame.classes.utils.*;
import org.javatuples.Pair;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.lwjgl.opengl.GL11.*;


public class TileMap {

    boolean queueReset = false;
    public void Reset() {
        queueReset = true;
    }
    public int width, height;
    public TileMap(int width, int height) {
        this.width = width;
        this.height = height;
        Window.tileMap = this;
        InitializeThings();
        Side.preComputeRandomSidesArray();
    }

    private void InitializeThings(){
        queuedParticlesToAdd.clear();
        Particle.idCounter = 0;
        history.clear();
        tick = 0;
        alreadyAddedToTickQueue = new boolean[width * height];
        for (int i = 0; i < randomFloatOfThisTick.length; i++)
            randomFloatOfThisTick[i] = randomClass.nextFloat();

        TileMapChanges.setTilemap(width, height);

        if (Main.config.benchmark) {
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

        subTileMaps.clear();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                subTileMaps.put(new Vector2Int(x, y), new SubTileMap(x, y));
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
    public static HashMap<Vector2Int, Pair<Integer, Integer>> debugDraws = new HashMap<>();
    public void reDrawTexture() {
        if (lastRedrawTick == tick)
            return;

        int[] colors = new int[width * height];
        for (Map.Entry<Vector2Int, SubTileMap> entry : new ArrayList<>(subTileMaps.entrySet())) {
            SubTileMap value = entry.getValue();
            for (Particle particle : new ArrayList<>(value.particles)) {
                if (particle == null)
                    continue;

                if (particle.x < 0 || particle.x >= width || particle.y < 0 || particle.y >= height)
                    continue;

                colors[particle.x + particle.y * width] = particle.color.getRGB();
            }
        }



        for (Map.Entry<Vector2Int, Pair<Integer, Integer>> entry : new ArrayList<>(debugDraws.entrySet())) {
            Vector2Int key = entry.getKey();
            Pair<Integer, Integer> value = entry.getValue();
            colors[key.x + key.y * width] = value.getValue0();
            if (value.getValue1() <= 1)
                debugDraws.remove(key);
            else
                debugDraws.put(key, Pair.with(value.getValue0(), value.getValue1() - 1));
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
//        DebugDraw.drawPoint(new Vector2Int(width / 2, height / 2), 10, 0xFF0000);

        Vector2Int forceRepresentation = new Vector2Int((int) ((Particle.WindForce.x + Particle.Gravity.x) * 50) , (int) ((Particle.WindForce.y + Particle.Gravity.y) * 50));
        DebugDraw.drawLine(new Vector2Int(width / 2, height / 2), new Vector2Int(width / 2 + forceRepresentation.x, height / 2 + forceRepresentation.y), 0, ColorUtils.GetColor(((float) tick / Window.TicksPerSecond) * .1f, 1f,.7f).getRGB(), 10);

        for (Map.Entry<Vector2Int, SubTileMap> entry : new ArrayList<>(subTileMaps.entrySet())) {
            Vector2Int key = entry.getKey();
            SubTileMap value = entry.getValue();
            DebugDraw.drawRect(new Vector2Int(key.x * SubTileMap.width, key.y * SubTileMap.height), new Vector2Int(key.x * SubTileMap.width + SubTileMap.width, key.y * SubTileMap.height + SubTileMap.height), 0, 0xFFFFFF, 0);
        }


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
    private final Queue<Runnable> tasksToRunOnThreadFinish = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final Queue<Particle> queuedParticlesToAdd = new java.util.concurrent.ConcurrentLinkedQueue<>();
    public float[] randomFloatOfThisTick = new float[100];
    public int threads = 10;
    boolean[] alreadyAddedToTickQueue2;
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    public void Tick(boolean force) {
        Main.debug.isPaused = paused;

        if (queueReset) {
            queueReset = false;
            InitializeThings();
        }


        if (!paused || force && !queuedParticlesToAdd.isEmpty())
            alreadyAddedToTickQueue2 = new boolean[width * height];

        if (!queuedParticlesToAdd.isEmpty()){
            while (!queuedParticlesToAdd.isEmpty()) {
                Particle particle = queuedParticlesToAdd.poll();

                if (getTile(particle.x, particle.y) != null)
                    continue;

                if (alreadyAddedToTickQueue2[particle.x + particle.y * width])
                    continue;

                alreadyAddedToTickQueue2[particle.x + particle.y * width] = true;

                particle.id = Particle.idCounter++;
                addParticle(particle);
                AddParticleToTickQueue(particle);
            }
        }


        if (paused && !force)
            return;

        long startTicking = System.currentTimeMillis();

        tick++;
        CalculateWindForce();

        List<Future<ThreadResult>> futures = new ArrayList<>(threads);

        // Este particles to tick se puede optimizar.
        // Manejar el tickeo de particulas directamente desde adentro del subtilemap

        int subTileMapsSize = subTileMaps.size();
        for (int threadID = 0; threadID < threads; threadID++) {

            int finalThreadID = threadID;
            Future<ThreadResult> future = executor.submit(() ->{
                List<TileMapChanges.TileMapChange> particles = new ArrayList<>(SubTileMap.width * 2 + SubTileMap.height * 2);
                int remainder = subTileMapsSize % threads;
                int ammount = (int) Math.floor((double) subTileMapsSize / threads);
                if (finalThreadID < remainder)
                    ammount++;

                boolean[] added = new boolean[width * height];
                ArrayList<Particle> particlesToTickMainThread = new ArrayList<>();


                for (int subNum = 0; subNum < ammount; subNum++) {
                    int index = subNum * threads + finalThreadID;
                    SubTileMap subTileMap = subTileMaps.entrySet().stream().skip(index).findFirst().get().getValue();
                    Particle[] tilesFinal = subTileMap.tiles.clone();

                    ArrayList<Particle> particlesToTickForSubTile = new ArrayList<>();
                    subTileMap.particlesToTick.forEach(particle -> {
                        int x = particle.x;
                        int y = particle.y;

                        TileMapChanges.TileMapChange change = particle.tick();


                        if (change.particlesToUpdate != null){
                            for (Particle particleToUpdate : change.particlesToUpdate) {
                                if (added[particleToUpdate.getID()])
                                    continue;

                                added[particleToUpdate.getID()] = true;
                                particlesToTickMainThread.add(particleToUpdate);
                            }
                        }

                        if (particle.x == change.to.x && particle.y == change.to.y) {
                            change.particle = null;
                            return;
                        }

                        Vector2Int subTileMapStart = new Vector2Int((int) Math.floor((float) x / SubTileMap.width), (int) Math.floor((float) y / SubTileMap.height));
                        Vector2Int subTileMapEnd = new Vector2Int((int) Math.floor((float) change.to.x / SubTileMap.width), (int) Math.floor((float) change.to.y / SubTileMap.height));

                        if (subTileMapStart.equals(subTileMapEnd)) {
                            if (tilesFinal[change.to.x % SubTileMap.width + (change.to.y % SubTileMap.height) * SubTileMap.width] != null)
                                return;



                            tilesFinal[x % SubTileMap.width + (y % SubTileMap.height) * SubTileMap.width] = null;
                            tilesFinal[change.to.x % SubTileMap.width + (change.to.y % SubTileMap.height) * SubTileMap.width] = particle;
                            particle.x = change.to.x;
                            particle.y = change.to.y;


                            return;
                        }

                        // Podemos optimizar sacando la particula directamente aca.
                        // Y enviar un add particle y no un change particle
                        particles.add(change);

                    });

                    subTileMap.tiles = tilesFinal;
                    subTileMap.particlesToTick = particlesToTickForSubTile;
                }

                return new ThreadResult(particles, particlesToTickMainThread);
            });

            futures.add(future);
        }

        alreadyAddedToTickQueue = new boolean[width * height];

        long startWaitMS = System.currentTimeMillis();
        int processedCount = 0;
        ArrayList<ThreadResult> results = new ArrayList<>();
        while (processedCount != threads) {
            for (Future<ThreadResult> future : new ArrayList<>(futures)) {
                if (!future.isDone())
                    continue;

                try {
                    ThreadResult result = future.get();
                    results.add(result);
                    futures.remove(future);
                    TileMapChanges.changes.addAll(result.changes);

                } catch (Exception e){ }
                processedCount++;
            }
        }
        Profiler.addValue("startWaitMS", (System.currentTimeMillis() - startWaitMS), 1000);

        long startResolve = System.currentTimeMillis();
        TileMapChanges.Resove();
        Profiler.addValue("resolvingMS", System.currentTimeMillis() - startResolve, 1000);



        int tickingParticlesNumber = 0;
        for (ThreadResult result : results) {
            for (Particle particleToUpdate : result.particlesToUpdate) {
                AddParticleToTickQueue(particleToUpdate);
                tickingParticlesNumber++;
            }
        }

        Main.debug.TickingParticles = tickingParticlesNumber;
        Main.debug.Particles = subTileMaps.values().stream().mapToInt(x -> x.particles.size()).sum();

        for (Runnable runnable : tasksToRunOnThreadFinish)
            runnable.run();

        tasksToRunOnThreadFinish.clear();


        Profiler.addValue("totalTick", System.currentTimeMillis() - startTicking, 1000);
        Profiler.addValue("crap", Profiler.getAverage("totalTick") - Profiler.getAverage("startWaitMS") - Profiler.getAverage("resolvingMS"), 1000);

        Main.debug.UnresolvedParticles = TileMapChanges.changes.size();
        Main.debug.MS =
                "\n · Wait Threads:          " + Profiler.getAverage("startWaitMS") +
                "\n · Main Thread Resolve:   " + Profiler.getAverage("resolvingMS")  +
                "\n · Crap:                  " + Profiler.getAverage("crap")  +
                "\n · Total Ticking:         " + Profiler.getAverage("totalTick");


    }

    private boolean[] alreadyAddedToTickQueue;
    public void AddParticleToTickQueue(Particle particle) {
        if (alreadyAddedToTickQueue[particle.getID()]) {
            return;
        }

        Vector2Int subTileMapPos = new Vector2Int((int) Math.floor((float) particle.x / SubTileMap.width), (int) Math.floor((float) particle.y / SubTileMap.height));
        SubTileMap subTileMap = subTileMaps.get(subTileMapPos);
        if (subTileMap == null)
            return;

        subTileMap.particlesToTick.add(particle);
        alreadyAddedToTickQueue[particle.getID()] = true;
    }

    public void AddParticleToAddQueue(Particle particle) {
        queuedParticlesToAdd.add(particle);
    }
    public void AddSyncTask(Runnable runnable) {
        tasksToRunOnThreadFinish.add(runnable);
    }
    // endregion
    public HashMap<Vector2Int, SubTileMap> subTileMaps = new HashMap<>();
    public Pair<Vector2Int, Vector2Int> getSubTileMapPos(Vector2Int pos) {
        Vector2Int subTileMapPos = new Vector2Int((int) Math.floor((float) pos.x / SubTileMap.width), (int) Math.floor((float) pos.y / SubTileMap.height));
        Vector2Int localPos = new Vector2Int(pos.x % SubTileMap.width, pos.y % SubTileMap.height);
        return new Pair<Vector2Int, Vector2Int>(subTileMapPos, localPos);
    }
    public Particle getTile(int x, int y) {
        Pair<Vector2Int, Vector2Int> subTileMapPos = getSubTileMapPos(new Vector2Int(x, y));

        SubTileMap subTileMap = subTileMaps.get(subTileMapPos.getValue0());
        if (subTileMap == null)
            return SubTileMap.voidParticle;

        return subTileMap.getLocalTile(subTileMapPos.getValue1());
    }

    public void addParticle(Particle particle) {
        Pair<Vector2Int, Vector2Int> subTileMapPos = getSubTileMapPos(new Vector2Int(particle.x, particle.y));

        SubTileMap subTileMap = subTileMaps.get(subTileMapPos.getValue0());
        if (subTileMap == null)
            return;

        subTileMap.addParticle(particle);
    }
    public void moveParticle(Particle particle, Vector2Int to) {
        Pair<Vector2Int, Vector2Int> subTileMapPosFrom = getSubTileMapPos(new Vector2Int(particle.x, particle.y));
        SubTileMap fromSubTileMap = subTileMaps.get(subTileMapPosFrom.getValue0());
        if (fromSubTileMap == null)
            return;

        Pair<Vector2Int, Vector2Int> subTileMapPosTo = getSubTileMapPos(new Vector2Int(to.x, to.y));
        SubTileMap toSubTileMap = subTileMaps.get(subTileMapPosTo.getValue0());
        if (toSubTileMap == null)
            return;

        fromSubTileMap.removeParticle(particle.x, particle.y);
        particle.x = to.x;
        particle.y = to.y;
        toSubTileMap.addParticle(particle);
    }
    public void removeParticle(int x, int y) {
        Pair<Vector2Int, Vector2Int> subTileMapPos = getSubTileMapPos(new Vector2Int(x, y));

        SubTileMap subTileMap = subTileMaps.getOrDefault(subTileMapPos.getValue0(), null);
        if (subTileMap == null)
            return;

        subTileMap.removeParticle(x, y);
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

        if (Main.config.benchmark){
            if (x == 0)
                x = width - 2;
            if (y == 0)
                y = height - 2;
            if (x == width - 1)
                x = 1;
            if (y == height - 1)
                y = 1;
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
