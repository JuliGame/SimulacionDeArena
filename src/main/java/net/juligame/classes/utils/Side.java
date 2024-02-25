package net.juligame.classes.utils;

import net.juligame.Window;
import net.juligame.classes.Particle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum Side {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT;

    public static Side getOpposite(Side side) {
        switch (side) {
            case TOP:
                return BOTTOM;
            case BOTTOM:
                return TOP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            default:
                return null;
        }
    }

    private static Side[] sides = new Side[] {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    };
    public static List<Side> getSides() {
        return Arrays.asList(sides);
    }
    private static List<List<Side>> sidesArray = new ArrayList<>(sides.length * sides.length);
    public static void preComputeRandomSidesArray() {
        Random javaRandom = new Random();
        for (int i = 0; i < sides.length; i++) {
            List<Side> sides = Side.getSides();
            for (int j = 0; j < sides.size(); j++) {
                int randomIndex = javaRandom.nextInt(sides.size());
                Side temp = sides.get(j);
                sides.set(j, sides.get(randomIndex));
                sides.set(randomIndex, temp);
            }
            sidesArray.add(sides);
        }
    }
    public static List<Side> getSidesRandomized(int seed) {
        return sidesArray.get(seed % sidesArray.size());
    }

    public Vector2 getVector() {
        switch (this) {
            case TOP:
                return new Vector2(0, -1);
            case BOTTOM:
                return new Vector2(0, 1);
            case LEFT:
                return new Vector2(-1, 0);
            case RIGHT:
                return new Vector2(1, 0);
            default:
                return null;
        }
    }

}
