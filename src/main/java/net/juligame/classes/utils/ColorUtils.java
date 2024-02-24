package net.juligame.classes.utils;

import java.awt.*;

public class ColorUtils {

    public static Color GetRandomColor() {
        return new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
    }


    private static float hue = 0;
    public static Color GetRandomColorPretty() {
        hue += 0.001f;
//        float randBrightness = (float) Math.random() * 0.2f + 0.8f;
        float randSaturation = (float) Math.random() * 0.2f + 0.8f;
        return GetColor(hue, .8f, 1f);
    }

    public static Color GetColor(float hue, float saturation, float brightness) {
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public static Color Brighten(Color color, int amount) {
        int red = Math.min(255, color.getRed() + amount);
        int green = Math.min(255, color.getGreen() + amount);
        int blue = Math.min(255, color.getBlue() + amount);
        return new Color(red, green, blue);
    }

    public static Color Darken(Color color, int amount) {
        int red = Math.max(0, color.getRed() -amount);
        int green = Math.max(0, color.getGreen() -amount);
        int blue = Math.max(0, color.getBlue() -amount);
        return new Color(red, green, blue);
    }


    public static Color addColors(Color color1, Color color2) {
        int red = Math.min(255, color1.getRed() + color2.getRed());
        int green = Math.min(255, color1.getGreen() + color2.getGreen());
        int blue = Math.min(255, color1.getBlue() + color2.getBlue());
        return new Color(red, green, blue);
    }
}
