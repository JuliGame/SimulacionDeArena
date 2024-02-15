package net.juligame.classes.utils;

import java.awt.*;

public class RandomColor {

    public static Color GetRandomColor() {
        return new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
    }


    private static float hue = 0;
    public static Color GetRandomColorPretty() {
        hue += 0.001f;
        float randBrightness = (float) Math.random() * 0.2f + 0.8f;
        float randSaturation = (float) Math.random() * 0.2f + 0.8f;
        return GetColor(hue, .8f, randBrightness);
    }

    public static Color GetColor(float hue, float saturation, float brightness) {
        return Color.getHSBColor(hue, saturation, brightness);
    }
}
