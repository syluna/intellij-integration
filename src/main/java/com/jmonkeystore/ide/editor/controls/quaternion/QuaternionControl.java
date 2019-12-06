package com.jmonkeystore.ide.editor.controls.quaternion;

import com.jme3.math.Quaternion;
import com.jmonkeystore.ide.editor.controls.JmeEditorControl;
import com.jmonkeystore.ide.util.NumberUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@Deprecated
public abstract class QuaternionControl implements JmeEditorControl {

    private final Quaternion quaternion;
    private JPanel contentPanel;
    private JTextField textFieldX;
    private JTextField textFieldY;
    private JTextField textFieldZ;
    private JSlider sliderX;
    private JSlider sliderY;
    private JSlider sliderZ;
    private JLabel titleLabel;

    public QuaternionControl(String title, Quaternion quaternion) {
        this.quaternion = quaternion;

        // ((TitledBorder)contentPanel.getBorder()).setTitle("Quaternion: " + title);
        titleLabel.setText("Quaternion: " + title);
        refresh();

        textFieldX.getDocument().addDocumentListener(listenerX);
        textFieldY.getDocument().addDocumentListener(listenerY);
        textFieldZ.getDocument().addDocumentListener(listenerZ);

        float[] values = new float[3];
        quaternion.toAngles(values);

        sliderX.setValue((int) (values[0] * 1000000));
        sliderY.setValue((int) (values[1] * 1000000));
        sliderZ.setValue((int) (values[2] * 1000000));

        sliderX.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            float val = slider.getValue() / 1000000f;
            textFieldX.setText("" + val);
            set(0);
        });

        sliderY.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            float val = slider.getValue() / 1000000f;
            textFieldY.setText("" + val);
            set(1);
        });

        sliderZ.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            float val = slider.getValue() / 1000000f;
            textFieldZ.setText("" + val);
            set(2);
        });
    }

    public abstract void setValue(Quaternion value);

    @Override
    public void refresh() {
        float[] values = new float[3];
        quaternion.toAngles(values);

        textFieldX.setText("" + values[0]);
        textFieldY.setText("" + values[1]);
        textFieldZ.setText("" + values[2]);
    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    private void set(int axis) {

        JTextField textField;

        switch (axis) {
            case 0: textField = textFieldX; break;
            case 1: textField = textFieldY; break;
            case 2: textField = textFieldZ; break;
            default: throw new RuntimeException("Unknown axis: " + axis);
        }

        if (!textField.getText().trim().isEmpty() && NumberUtils.isFloat(textField.getText())) {

            float[] values = new float[3];
            quaternion.toAngles(values);

            float val = Float.parseFloat(textField.getText());

            values[axis] = val;
            setValue(new Quaternion().fromAngles(values));
        }

    }

    @Override
    public void cleanup() {

    }

    private final DocumentListener listenerX = new DocumentListener() {
        @Override public void insertUpdate(DocumentEvent e) { set(0); }
        @Override public void removeUpdate(DocumentEvent e) { set(0); }
        @Override public void changedUpdate(DocumentEvent e) { set(0); }
    };

    private final DocumentListener listenerY = new DocumentListener() {
        @Override public void insertUpdate(DocumentEvent e) { set(1); }
        @Override public void removeUpdate(DocumentEvent e) { set(1); }
        @Override public void changedUpdate(DocumentEvent e) { set(1); }
    };

    private final DocumentListener listenerZ = new DocumentListener() {
        @Override public void insertUpdate(DocumentEvent e) { set(2); }
        @Override public void removeUpdate(DocumentEvent e) { set(2); }
        @Override public void changedUpdate(DocumentEvent e) { set(2); }
    };

}
