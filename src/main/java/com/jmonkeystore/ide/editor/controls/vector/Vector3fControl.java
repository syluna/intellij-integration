package com.jmonkeystore.ide.editor.controls.vector;

import com.jme3.math.Vector3f;
import com.jmonkeystore.ide.editor.controls.JmeEditorControl;
import com.jmonkeystore.ide.util.NumberUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class Vector3fControl implements JmeEditorControl {

    private JPanel contentPanel;
    private JLabel titleLabel;

    private JTextField textFieldX;
    private JTextField textFieldY;
    private JTextField textFieldZ;


    private final Vector3f vector3f;

    public Vector3fControl(String title, Vector3f vector3f) {
        this.vector3f = vector3f;

        // ((TitledBorder)contentPanel.getBorder()).setTitle("Vector3f: " + title);
        titleLabel.setText("Vector3f: " + title);
        refresh();

        textFieldX.getDocument().addDocumentListener(listenerX);
        textFieldY.getDocument().addDocumentListener(listenerY);
        textFieldZ.getDocument().addDocumentListener(listenerZ);
    }

    public abstract void setValue(Vector3f value);

    @Override
    public void refresh() {
        textFieldX.setText("" + vector3f.x);
        textFieldY.setText("" + vector3f.y);
        textFieldZ.setText("" + vector3f.z);
    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void cleanup() {

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
            float val = Float.parseFloat(textField.getText());

            switch (axis) {
                case 0: setValue(new Vector3f(val, vector3f.y, vector3f.z)); break;
                case 1: setValue(new Vector3f(vector3f.x, val, vector3f.z)); break;
                case 2: setValue(new Vector3f(vector3f.x, vector3f.y, val)); break;
            }
        }

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
