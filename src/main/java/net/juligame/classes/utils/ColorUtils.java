package net.juligame.classes.utils;

import java.awt.*;

public class ColorUtils {

    public static Color GetRandomColor() {
        return new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
    }


    public static Color GetRandomColorPretty(float hue) {

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




    public static Color okLCH(float L, float C, float H) {
        // Step 1: Convert LCH to Lab
        double a = C * Math.cos(H);
        double b = C * Math.sin(H);

        // Step 2: Convert Lab to XYZ
        double y = (L + 16) / 116;
        double x = a / 500 + y;
        double z = y - b / 200;

        double x3 = Math.pow(x, 3);
        double y3 = Math.pow(y, 3);
        double z3 = Math.pow(z, 3);

        x = ((x3 > 0.008856) ? x3 : (x - 16 / 116) / 7.787);
        y = ((y3 > 0.008856) ? y3 : (y - 16 / 116) / 7.787);
        z = ((z3 > 0.008856) ? z3 : (z - 16 / 116) / 7.787);

        // Observer= 2°, Illuminant= D65
        x = x * 95.047;
        y = y * 100.000;
        z = z * 108.883;

        // Step 3: Convert XYZ to linear RGB
        x = x / 100;
        y = y / 100;
        z = z / 100;

        double r = x * 3.2406 + y * -1.5372 + z * -0.4986;
        double g = x * -0.9689 + y * 1.8758 + z * 0.0415;
        double bl = x * 0.0557 + y * -0.2040 + z * 1.0570;

        // Step 4: Convert linear RGB to sRGB
        r = (r > 0.0031308) ? 1.055 * Math.pow(r, 1 / 2.4) - 0.055 : 12.92 * r;
        g = (g > 0.0031308) ? 1.055 * Math.pow(g, 1 / 2.4) - 0.055 : 12.92 * g;
        bl = (bl > 0.0031308) ? 1.055 * Math.pow(bl, 1 / 2.4) - 0.055 : 12.92 * bl;

        try {
            Color color = new Color((float) r, (float) g, (float) bl);
            lastColor = color;
            return new Color((float) r, (float) g, (float) bl);
        } catch (Exception e) {
            System.out.println("Error with color: " + r + " " + g + " " + bl);
            return lastColor;
        }
    }

    static Color lastColor = new Color(0, 0, 0);

}
