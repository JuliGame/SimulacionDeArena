package net.juligame.classes;

import net.juligame.Window;
import net.juligame.classes.utils.Vector2Int;
import org.javatuples.Pair;

import static org.lwjgl.opengl.GL11.*;

public class DebugDraw {
    public static void drawPoint(Vector2Int point, int size, int color, int ticks) {
        for (int x = -size; x <= size; x++) {
            if (point.x + x < 0 || point.x + x >= Window.tileMap.width)
                continue;

            for (int y = -size; y <= size; y++) {
                if (point.y + y < 0 || point.y + y >= Window.tileMap.height)
                    continue;

                TileMap.debugDraws.put(new Vector2Int(point.x + x, point.y + y), new Pair<Integer, Integer> (color, ticks));
            }
        }
    }
    public static void drawLine(Vector2Int start, Vector2Int end, int size, int color, int ticks) {
        int dx = Math.abs(end.x - start.x);
        int dy = Math.abs(end.y - start.y);

        int sx = start.x < end.x ? 1 : -1;
        int sy = start.y < end.y ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true) {
            drawPoint(start, size, color, ticks);

            if (start.x == end.x && start.y == end.y)
                break;

            e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                start.x += sx;
            }
            if (e2 < dx) {
                err += dx;
                start.y += sy;
            }
        }
    }

    public static void drawRect(Vector2Int start, Vector2Int end, int size, int color, int ticks) {
        Vector2Int topLeft = new Vector2Int(Math.min(start.x, end.x), Math.min(start.y, end.y));
        Vector2Int bottomRight = new Vector2Int(Math.max(start.x, end.x), Math.max(start.y, end.y));

        drawLine(new Vector2Int(topLeft.x, topLeft.y), new Vector2Int(bottomRight.x, topLeft.y), size, color, ticks);
        drawLine(new Vector2Int(topLeft.x, topLeft.y), new Vector2Int(topLeft.x, bottomRight.y), size, color, ticks);
        drawLine(new Vector2Int(bottomRight.x, topLeft.y), new Vector2Int(bottomRight.x, bottomRight.y), size, color, ticks);
        drawLine(new Vector2Int(topLeft.x, bottomRight.y), new Vector2Int(bottomRight.x, bottomRight.y), size, color, ticks);
    }

}
