package net.juligame.classes.threading;

import net.juligame.classes.Particle;

import java.util.List;
import java.util.Queue;
public class WorkerThread implements Runnable {
    List<Particle> queue;
    public WorkerThread(List<Particle> s){
        this.queue=s;
    }
    public void run() {
        if (queue == null)
            return;

        while (!queue.isEmpty()) {
            Particle particle = queue.remove(0);
            if (particle == null)
                return;

            particle.tick();
        }
    }
}