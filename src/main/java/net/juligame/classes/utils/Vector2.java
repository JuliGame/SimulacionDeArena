package net.juligame.classes.utils;

public class Vector2 {
    public float x;
    public float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 Normalize() {
        float length = (float) Math.sqrt(x * x + y * y);
        if (length == 0)
            return this;

        x /= length;
        y /= length;
        return this;
    }

    public Vector2 Multiply(float value) {
        x *= value;
        y *= value;
        return this;
    }

    public Vector2 Invert() {
        x = -x;
        y = -y;
        return this;
    }

    public Vector2 clone() {
        return new Vector2(x, y);
    }

    @Override
    public String toString() {
        return "X: " + x + "   Y:" + y;
    }
}
