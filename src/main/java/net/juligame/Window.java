package net.juligame;

import imgui.ImGui;
import net.juligame.classes.Particle;
import net.juligame.classes.TileMap;
import net.juligame.classes.tools.Explotion;
import net.juligame.classes.tools.Implotion;
import net.juligame.classes.utils.ColorUtils;
import net.juligame.classes.utils.Vector2;
import net.juligame.classes.utils.Vector2Int;
import org.lwjgl.opengl.GL;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long windowID;
    public static TileMap tileMap;
    public static int texture;

    public Window(Main application) {
        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        this.windowID = application.getHandle();
        if (windowID == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(windowID); // Make the OpenGL context current
        GL.createCapabilities();

        int width = application.WIDTH;
        int height = application.HEIGHT;

        InitOpenGL(width, height);
//        glLoadIdentity();

        tileMap = new TileMap(width / Particle.TILE_SIZE, height / Particle.TILE_SIZE);

        StartSimThread();

        glfwSetCursorPosCallback(windowID, (window, xpos, ypos) -> {
            mouseX = (int) Math.floor(xpos / Particle.TILE_SIZE);
            mouseY = (int) Math.ceil(ypos / Particle.TILE_SIZE);
        });

        glfwSetScrollCallback(windowID, (window, xoffset, yoffset) -> {
            // if presing shift, change the brush size
            if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS)
                Main.config.brushSize += (float) ((Main.config.brushSize * yoffset * .1f) + 1);
            else
                Main.config.brushSize += (float) yoffset;
            if (Main.config.brushSize < 1) {
                Main.config.brushSize = 1;
            }
        });

        LoadTextures();
        tileMap.initTextureAllocations();
    }

    private int loadTexture(String imagePath) {
        int tex;

        BufferedImage image = null;

        File file = new File(imagePath);
        if (!file.exists()) {
            System.err.println("The file does not exist");
            return 0;
        }

        try {
            image = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

        ByteBuffer b = ByteBuffer.allocateDirect(4 * width * height);
        tex = glGenTextures();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                b.put((byte) ((pixel >> 16) & 0xFF));
                b.put((byte) ((pixel >> 8) & 0xFF));
                b.put((byte) (pixel & 0xFF));
            }
        }

        b.flip();
        glBindTexture(GL_TEXTURE_2D, tex);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, b);

        glBindTexture(GL_TEXTURE_2D, 0);

        return tex;
    }

    Thread simThread;
    public void StartSimThread() {
        simThread = new Thread(this::simulation);
        simThread.setName("Simulation Thread");
        simThread.start();
    }

    public static int mouseX;
    public static int mouseY;

    public long lastUnixTime = System.nanoTime();
    public static int TicksPerSecond = 60;
    private float timePerTick = 1000000000f / TicksPerSecond;
    public void simulation() {
        while (true) {
            if (System.nanoTime() - lastUnixTime < timePerTick)
                continue;


            tileMap.Tick();
            Main.debug.TPS = Math.round(1000000000f / (System.nanoTime() - lastUnixTime));
            lastUnixTime = System.nanoTime();
        }
    }

    public void LoadTextures() {
//        texture = loadTexture("/home/julian/SimulacionDeArena/src/main/resources/wall.jpg");
        System.out.println("Texture: " + texture);
    }
    public void InitOpenGL(int width, int height) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glMatrixMode(GL_PROJECTION);
//        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glEnable(GL_TEXTURE_2D);
    }

    boolean spacePressed = false;
    int fps = 0;
    long lastFrameShowFPS = System.currentTimeMillis();
    public void run() {
        if (System.currentTimeMillis() - lastFrameShowFPS > 1000) {
            Main.debug.FPS = fps;
            lastFrameShowFPS = System.currentTimeMillis();
            fps = 0;
        }

        fps++;

        tileMap.reDrawTexture();
        tileMap.draw();

        if (glfwGetKey(windowID, GLFW_KEY_R) == GLFW_PRESS){
            tileMap.Reset();
            System.out.println("Reset");
        }

        //  || ImGui.isItemHovered() ||
        if (ImGui.isWindowFocused() || ImGui.isWindowHovered())
            return;

        boolean isPressed = glfwGetMouseButton(windowID, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
        if (isPressed)
            press(mouseX, mouseY);
        else
            lastMousePos = null;


        boolean isPressedRight = glfwGetMouseButton(windowID, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;
        if (isPressedRight)
            if (glfwGetKey(windowID, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS || glfwGetKey(windowID, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS)
                Implotion.Implode(mouseX, mouseY, Main.config.brushSize);
            else
                Explotion.Explode(mouseX, mouseY, Main.config.brushSize);


        if (glfwGetKey(windowID, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS && glfwGetKey(windowID, GLFW_KEY_Z) == GLFW_PRESS)
            tileMap.SendCtrlZ();

        if (glfwGetKey(windowID, GLFW_KEY_SPACE) == GLFW_PRESS) {
            if (!spacePressed)
                tileMap.Pause();

            spacePressed = true;
        } else {
            spacePressed = false;
        }


        if (!simThread.isAlive())
            StartSimThread();
    }
    Vector2Int lastMousePos = null;
    void press(int x, int y) {
        List<Vector2Int> points = new ArrayList<>();
        if (lastMousePos != null) {
            Vector2 direction = new Vector2(x - lastMousePos.x, y - lastMousePos.y);
            double distance = Math.sqrt(direction.x * direction.x + direction.y * direction.y);
            direction = direction.Normalize();

            int steps = (int) (Math.floor(distance / Main.config.brushSize) * 4);
            double lastDistance = 1000000f;
            for (int i = 0; i < steps; i++) {
                float x1 = lastMousePos.x + direction.x * i * Main.config.brushSize;
                float y1 = lastMousePos.y + direction.y * i * Main.config.brushSize;
                Vector2Int point = new Vector2Int((int) x1, (int) y1);
                double distanceToMouse = Math.sqrt((x1 - x) * (x1 - x) + (y1 - y) * (y1 - y));
                if (distanceToMouse > lastDistance)
                    continue;

                lastDistance = distanceToMouse;
                points.add(point);
            }
        }

        points.add(new Vector2Int(x, y));

        for (Vector2Int point : points) {
            Main.config.hue += 0.001f;
            float hue = Main.config.hue;
            float brushSize = Main.config.brushSize;

            Color color = ColorUtils.GetRandomColorPretty(hue);
            Color nextcolor = ColorUtils.GetRandomColorPretty(hue + 0.005f);

//        Color nextcolor = ColorUtils.okLCH(73.15f, 41.09f, hue);
//        Color color = ColorUtils.okLCH(73.15f, 41.09f, hue + 0.1f);
            for (int i = (int) -brushSize; i < brushSize; i++) {
                for (int j = (int) -brushSize; j < brushSize; j++) {

                    int x1 = (int) (point.x + i);
                    if (x1 < 0 || x1 >= tileMap.tiles.length)
                        continue;

                    int y1 = (int) (point.y + j);
                    if (y1 < 0 || y1 >= tileMap.tiles[0].length)
                        continue;

                    float distance = (float) Math.sqrt(i * i + j * j);
                    float random = tileMap.randomFloatOfThisTick[Math.abs((int) System.nanoTime()) % tileMap.randomFloatOfThisTick.length];

                    if (random * brushSize > distance) {
                        if (random < 0.8f)
                            new Particle(color, (x1), (y1));
                        else
                            new Particle(nextcolor, (x1), (y1));

                    }
                }
            }
        }

        lastMousePos = new Vector2Int(x, y);
    }

}
