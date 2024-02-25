package net.juligame.classes.utils;

public class Vector2Int {
    public int x;
    public int y;

    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2Int Normalize() {
        float length = (float) Math.sqrt(x * x + y * y);
        if (length == 0)
            return this;

        x /= (int) length;
        y /= (int) length;
        return this;
    }

    public Vector2Int Multiply(float value) {
        x *= (int) value;
        y *= (int) value;
        return this;
    }

    public Vector2Int Invert() {
        x = -x;
        y = -y;
        return this;
    }

    public Vector2Int clone() {
        return new Vector2Int(x, y);
    }

    @Override
    public String toString() {
        return "X: " + x + "   Y:" + y;
    }
}
