package net.juligame.classes.utils;

import net.juligame.Window;
import net.juligame.classes.Particle;

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

}
