package net.juligame;

import imgui.app.Application;
import imgui.app.Configuration;
import net.juligame.classes.logic.CreatingMenu;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class Main extends Application {
    public final int WIDTH = 1920;
    public final int HEIGHT = 1080;
    public static Main instance;
    public ArrayList<CreatingMenu> menus = new ArrayList<>();

    public static void main(String[] args) {
        instance = new Main();
        launch(instance);
    }
    @Override
    protected void configure(Configuration config) {
        config.setTitle("Sand Simulator");
        config.setWidth(WIDTH);
        config.setHeight(HEIGHT);
    }

    Window window;
    public static Config config = new Config();
    public static Debug debug = new Debug();
    @Override
    protected void preRun() {
        window = new Window(this);
        new CreatingMenu(config);
    }

    @Override
    public void process() {
        new ArrayList<>(menus).forEach(CreatingMenu::process);
        window.run();

        if (glfwGetKey(getHandle(), GLFW_KEY_C) == GLFW_PRESS)
            new CreatingMenu(config);

        if (glfwGetKey(getHandle(), GLFW_KEY_D) == GLFW_PRESS)
            new CreatingMenu(debug);

    }

}