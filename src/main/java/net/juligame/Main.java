package net.juligame;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello world!");

        // Create a window with lwjgl
        Window window = new Window(1920, 1080, "Sand Simulator");
        window.run();
    }

}