package net.juligame.classes.threading;

import net.juligame.classes.Particle;

import java.util.List;

public class QueueUtils {
    
    

    public static List<Particle>[] splitQueue(List<Particle> queue, int size) {
        // We divide the queue into smaller queues of size "size"

        List<Particle>[] queues = new List[size];
        for (int i = 0; i < size; i++) {
            queues[i] = queue.subList(i * queue.size() / size, (i + 1) * queue.size() / size);
        }
        return queues;
    }

}
