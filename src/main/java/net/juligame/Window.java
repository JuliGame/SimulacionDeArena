package net.juligame;

import net.juligame.classes.Particle;
import net.juligame.classes.TileMap;
import net.juligame.classes.utils.RandomColor;
import org.lwjgl.opengl.GL;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long window;
    public static TileMap tileMap;

    public Window(int width, int height, String title) {
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(window); // Make the OpenGL context current
        GL.createCapabilities();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);

        tileMap = new TileMap(width / Particle.TILE_SIZE, height / Particle.TILE_SIZE);
    }

    public static int mouseX;
    public static int mouseY;

    public long lastUnixTime = System.currentTimeMillis();
    public int TicksPerSecond = 60;
    public void run() {
        float timePerTick = 1000f / TicksPerSecond;
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear the framebuffer

            tileMap.draw();

            glfwSwapBuffers(window); // Swap the color buffers

            glfwPollEvents(); // Poll for window events


            glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
                mouseX = (int) Math.floor(xpos / Particle.TILE_SIZE);
                mouseY = (int) Math.floor((ypos + Particle.TILE_SIZE * 0.5f) / Particle.TILE_SIZE) + 1;
            });

            glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
                brushSize += yoffset;
                if (brushSize < 1) {
                    brushSize = 1;
                }
            });

            if (glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS){
                tileMap.Reset();
                System.out.println("Reset");
            }


            boolean isPressed = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;

            if (isPressed) {
                press(mouseX, mouseY);
            }

            if (System.currentTimeMillis() - lastUnixTime < timePerTick) {
                continue;
            }

            tileMap.Tick();
            lastUnixTime = System.currentTimeMillis();

        }
    }

    float brushSize = 6;

    void press(int x, int y) {
        Color color = RandomColor.GetRandomColorPretty();
        for (int i = (int) -brushSize; i < brushSize; i++) {
            for (int j = (int) -brushSize; j < brushSize; j++) {
                float distance = (float) Math.sqrt(i * i + j * j);
                float random = (float) Math.random() * brushSize;
                if (random > distance) {
                    new Particle(color, x + i, y + j);
                }
            }
        }
    }


}
