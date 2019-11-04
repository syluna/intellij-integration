package com.jmonkeystore.ide.jme;

import com.jme3.math.ColorRGBA;

import java.awt.*;

public class ColorUtils {

    public static ColorRGBA toColorRGBA(Color color) {

        return new ColorRGBA(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        );
    }

    public static Color fromColorRGBA(ColorRGBA colorRGBA) {
        return new Color(
                colorRGBA.r,
                colorRGBA.g,
                colorRGBA.b,
                colorRGBA.a
        );
    }

}
