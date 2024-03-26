package net.juligame.classes.utils;


import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Profiler {
    private static final HashMap<String, Profiler> dictionary = new HashMap<>();
    private final Queue<Long> times = new LinkedList<>();
    private final Queue<Double> values = new LinkedList<>();
    private double sum = 0;

    public static void addValue(String key, double value, int ms) {
        long now = System.currentTimeMillis();
        Profiler instance = dictionary.get(key);
        if (instance == null) {
            instance = new Profiler();
            dictionary.put(key, instance);
        }
        instance.times.add(now);
        instance.values.add(value);
        instance.sum += value;

        // Remove values older than 10 seconds
        while (now - instance.times.peek() > ms) {
            instance.sum -= instance.values.poll();
            instance.times.poll();
        }
    }
    static DecimalFormat value = new DecimalFormat("#.#");

    public static double getAverage(String key) {
        Profiler instance = dictionary.get(key);
        if (instance == null || instance.values.isEmpty()) {
            return 0; // or throw an exception
        }
        // return with only 1 decimal
        return Double.parseDouble(value.format(instance.sum / instance.values.size()));
    }
}
