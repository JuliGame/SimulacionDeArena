package net.juligame.classes.threading;

import net.juligame.classes.Particle;

import java.util.Queue;
public class WorkerThread implements Runnable {
    Queue<Particle> queue;
    public WorkerThread(Queue<Particle> s){
        this.queue=s;
    }
    public void run() {
        if (queue == null)
            return;

        while (!queue.isEmpty()) {
            Particle particle = queue.poll();
            if (particle == null)
                return;

            particle.tick();
        }
    }
}