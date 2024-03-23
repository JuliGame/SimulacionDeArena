package net.juligame.classes.threading;

import net.juligame.classes.Particle;

import java.util.ArrayList;
import java.util.List;

public class ThreadResult {
    public List<TileMapChanges.TileMapChange> changes;
    public List<Particle> particlesToUpdate;

    public ThreadResult(List<TileMapChanges.TileMapChange> particles, ArrayList<Particle> particlesToTick) {
        this.changes = particles;
        this.particlesToUpdate = particlesToTick;
    }
}
