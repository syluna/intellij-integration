package com.jmonkeystore.ide.editor.component;

import com.jme3.math.ColorRGBA;
import com.jmonkeystore.ide.jme.ColorUtils;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;

public class ColorRGBAComponent extends Component {

    private JPanel contentPanel;
    private JLabel propertyNameLabel;
    private JLabel colorValueLabel;
    private JPanel colorPanel;

    public ColorRGBAComponent() {
        super(null, null, null);

        colorPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

                ColorDialog colorDialog = new ColorDialog(colorPanel.getBackground());

                if (colorDialog.showAndGet()) {
                    setValue(ColorUtils.toColorRGBA(colorDialog.getColor()));
                }

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    public ColorRGBAComponent(Object parent, Method getter, Method setter) {
        super(parent, getter, setter);
    }


    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);

        ColorRGBA newVal = (ColorRGBA) value;

        SwingUtilities.invokeLater(() -> {
            colorPanel.setBackground(ColorUtils.fromColorRGBA(newVal));
            colorValueLabel.setText(newVal.toString());
            bind();
        });

    }

    @Override
    public void bind() {
        super.bind();

        /*
        this.jColorChooser.getSelectionModel().addChangeListener(e -> {
            setValue(toColorRGBA(jColorChooser.getColor()));
        });

         */

    }

    @Override
    public void setPropertyName(String propertyName) {
        super.setPropertyName(propertyName);
        propertyNameLabel.setText("ColorRGBA: " + propertyName);
    }



}
