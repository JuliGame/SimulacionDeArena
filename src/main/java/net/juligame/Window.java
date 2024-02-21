package net.juligame;

import net.juligame.classes.Particle;
import net.juligame.classes.TileMap;
import net.juligame.classes.utils.RandomColor;
import org.lwjgl.opengl.GL;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long window;
    public static TileMap tileMap;
    public static int texture;

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

        InitOpenGL(width, height);
//        glLoadIdentity();

        tileMap = new TileMap(width / Particle.TILE_SIZE, height / Particle.TILE_SIZE);

        StartSimThread();

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

    public long lastUnixTime = System.currentTimeMillis();
    public int TicksPerSecond = 20;


    private float timePerTick = 1000f / TicksPerSecond;
    public void simulation() {
        while (true) {
            if (System.currentTimeMillis() - lastUnixTime < timePerTick) {
                continue;
            }

            tileMap.Tick();
//            System.out.println("The simulation is performing at a ~TPS of " + (1000f / (System.currentTimeMillis() - lastUnixTime)));
//            System.out.println("Each tick should take: " + timePerTick + " but it took: " + (System.currentTimeMillis() - lastUnixTime));
            lastUnixTime = System.currentTimeMillis();
        }
    }

    public void LoadTextures() {
        texture = loadTexture("/home/julian/SimulacionDeArena/src/main/resources/wall.jpg");
        System.out.println("Texture: " + texture);
    }
    public void InitOpenGL(int width, int height) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glEnable(GL_TEXTURE_2D);
    }

    public void run() {
        LoadTextures();
        long lastFrame = System.currentTimeMillis();
        tileMap.initTextureAllocations();
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear the framebuffer

            tileMap.draw();

            glfwSwapBuffers(window); // Swap the color buffers

            glfwPollEvents(); // Poll for window events

            if (glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS){
                tileMap.Reset();
                System.out.println("Reset");
            }

            boolean isPressed = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;

            if (isPressed) {
                press(mouseX, mouseY);
            }

//            System.out.println("Rendered at a ~FPS of " + (1000f / (System.currentTimeMillis() - lastFrame)));
            lastFrame = System.currentTimeMillis();

            if (!simThread.isAlive())
                StartSimThread();
        }
        simThread.stop();
    }

    float brushSize = 1;

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
