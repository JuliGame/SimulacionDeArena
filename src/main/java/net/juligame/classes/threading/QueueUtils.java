package net.juligame.classes.threading;

import java.util.Queue;

public class QueueUtils {
    
    

    public static Queue[] splitQueue(Queue queue, int size) {
        Queue[] queues = new Queue[size];
        int i = 0;
        while (!queue.isEmpty()) {
            if (queues[i] == null) {
                queues[i] = new java.util.LinkedList<>();
            }
            queues[i].add(queue.remove());
            i++;
            if (i == size) {
                i = 0;
            }
        }
        return queues;
    }

}
