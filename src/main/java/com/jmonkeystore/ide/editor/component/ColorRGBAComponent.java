package com.jmonkeystore.ide.editor.component;

import com.jme3.math.ColorRGBA;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

public class ColorRGBAComponent extends Component {

    private JPanel contentPanel;
    private JLabel propertyNameLabel;
    private JColorChooser jColorChooser;

    public ColorRGBAComponent() {
        super(null, null, null);
        initCustomLayout();
    }

    public ColorRGBAComponent(Object parent, Method getter, Method setter) {
        super(parent, getter, setter);
        initCustomLayout();
    }

    // intellij doesn't let us add a color chooser in the editor, so we have to set it up ourselves.
    private void initCustomLayout() {

        contentPanel.setLayout(new VerticalLayout());

        this.jColorChooser = new JColorChooser();
        this.contentPanel.add(jColorChooser);

        propertyNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);

        if (!isBinded()) {
            ColorRGBA newVal = (ColorRGBA) value;

            SwingUtilities.invokeLater(() -> {
                jColorChooser.setColor(fromColorRGBA(newVal));
                bind();
            });
        }

    }

    @Override
    public void bind() {
        super.bind();

        this.jColorChooser.getSelectionModel().addChangeListener(e -> {
            setValue(toColorRGBA(jColorChooser.getColor()));
        });
    }

    @Override
    public void setPropertyName(String propertyName) {
        super.setPropertyName(propertyName);
        propertyNameLabel.setText("ColorRGBA: " + propertyName);
    }

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

}
