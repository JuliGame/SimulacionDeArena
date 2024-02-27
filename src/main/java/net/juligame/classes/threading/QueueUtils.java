package net.juligame.classes.threading;

import java.util.List;

public class QueueUtils {
    
    

    public static List[] splitQueue(List queue, int size) {
        List[] queues = new List[size];
        int i = 0;
        while (!queue.isEmpty()) {
            if (queues[i] == null) {
                queues[i] = new java.util.LinkedList<>();
            }
            queues[i].add(queue.remove(0));
            i++;
            if (i == size) {
                i = 0;
            }
        }
        return queues;
    }

}
