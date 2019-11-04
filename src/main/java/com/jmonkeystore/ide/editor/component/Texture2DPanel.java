package com.jmonkeystore.ide.editor.component;

import com.intellij.util.ui.UIUtil;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Texture2DPanel extends JPanel {

    private BufferedImage img;
    private Image scaled;

    public Texture2DPanel() {
        super();

    }

    public void setTexture(Texture2D texture2D) {

        // this is slow.. but it works.

        int texWidth = texture2D.getImage().getWidth();
        int texHeight = texture2D.getImage().getHeight();

        // create a BufferedImage the same dimensions as the texture.
        img = UIUtil.createImage(
                texWidth,
                texHeight,
                BufferedImage.TYPE_INT_ARGB);

        WritableRaster writableRaster = img.getRaster();

        // write the texture to the BufferedImage.
        ImageRaster textureRaster = ImageRaster.create(texture2D.getImage());

        for (int x = 0; x < texture2D.getImage().getWidth(); x++) {
            for (int y = 0; y < texture2D.getImage().getHeight(); y++) {

                ColorRGBA pixel = textureRaster.getPixel(x, y);
                writableRaster.setPixel(x, y, new float[] {
                        pixel.r * 255,
                        pixel.g * 255,
                        pixel.b * 255,
                        pixel.a * 255 });
            }
        }

        img.setData(writableRaster);

        // define a max height and width, then determine the biggest from the texture.
        // the width of this won't work because the panel will be resized by it's parent.

        int maxHeight = 100;
        int newHeight = Math.min(texHeight, maxHeight);

        float widthRatio = (float)texWidth / (float)texHeight;

        int newWidth = (int) (newHeight * widthRatio);

        Dimension dim = new Dimension(newWidth, newHeight);

        setSize(dim);
        setMinimumSize(dim);
        setMaximumSize(dim);
        setPreferredSize(dim);
    }

    /*
    public ImagePanel(String img) {
        this(new ImageIcon(img).getImage());
    }

    public ImagePanel(Image img) {
        this.img = img;
    }

     */

    @Override
    public void invalidate() {
        super.invalidate();

        int width = getWidth();
        int height = getHeight();

        if (width > 0 && height > 0) {
            scaled = img.getScaledInstance(getWidth(), getHeight(),
                    Image.SCALE_SMOOTH);
        }


    }

    @Override
    public Dimension getPreferredSize() {
        /*
        return img == null ? new Dimension(200, 200) : new Dimension(
                img.getWidth(this), img.getHeight(this));

         */
        return super.getPreferredSize();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(scaled, 0, 0, null);
    }

}
