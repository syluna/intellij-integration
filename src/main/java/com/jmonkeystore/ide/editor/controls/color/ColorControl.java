package com.jmonkeystore.ide.editor.controls.color;

import com.intellij.uiDesigner.core.GridConstraints;
import com.jme3.math.ColorRGBA;
import com.jmonkeystore.ide.editor.controls.JmeEditorControl;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

@Deprecated
public abstract  class ColorControl implements JmeEditorControl {

    private final ColorRGBA jmeColor;

    private JColorChooser jColorChooser;
    private JPanel contentPanel;

    public ColorControl(String title, ColorRGBA colorRGBA) {
        this.jmeColor = colorRGBA;
        ((TitledBorder)contentPanel.getBorder()).setTitle("ColorRGBA: " + title);
        this.jColorChooser = new JColorChooser(fromColorRGBA(colorRGBA));

        this.jColorChooser.getSelectionModel().addChangeListener(e -> {
            setValue(toColorRGBA(jColorChooser.getColor()));
        });

        this.contentPanel.add(jColorChooser, new GridConstraints());
    }

    public abstract void setValue(ColorRGBA colorRGBA);

    private ColorRGBA toColorRGBA(Color color) {

        return new ColorRGBA(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        );
    }

    private Color fromColorRGBA(ColorRGBA colorRGBA) {
        return new Color(
                colorRGBA.r,
                colorRGBA.g,
                colorRGBA.b,
                colorRGBA.a
        );
    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void refresh() {
        jColorChooser.setColor(fromColorRGBA(jmeColor));
    }

    @Override
    public void cleanup() {

    }

}
