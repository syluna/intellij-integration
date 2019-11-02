package com.jmonkeystore.ide.editor.component;

import com.jme3.math.Vector3f;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Vector3fComponent extends Component {

    private JPanel contentPanel;
    private JLabel propertyNameLabel;
    private JFormattedTextField xTextField;
    private JFormattedTextField yTextField;
    private JFormattedTextField zTextField;

    public Vector3fComponent(Object parent, Method getter, Method setter) {
        super(parent, getter, setter);

        FloatFormatFactory floatFormatFactory = new FloatFormatFactory();

        xTextField.setFormatterFactory(floatFormatFactory);
        yTextField.setFormatterFactory(floatFormatFactory);
        zTextField.setFormatterFactory(floatFormatFactory);

        try {
            setValue(getter.invoke(parent));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);

        if (!isBinded()) {

            Vector3f vector3f = (Vector3f) value;

            SwingUtilities.invokeLater(() -> {
                this.xTextField.setText("" + vector3f.x);
                this.yTextField.setText("" + vector3f.y);
                this.zTextField.setText("" + vector3f.z);

                bind();
            });
        }
    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void bind() {
        super.bind();
        xTextField.getDocument().addDocumentListener(changeListener);
        yTextField.getDocument().addDocumentListener(changeListener);
        zTextField.getDocument().addDocumentListener(changeListener);
    }

    @Override
    public void setPropertyName(String propertyName) {
        super.setPropertyName(propertyName);
        propertyNameLabel.setText("Vector3f: " + propertyName);
    }

    private void set() {

        Vector3f value = (Vector3f) getReflectedProperty().getValue();

        String x = xTextField.getText().isEmpty() ? "" + value.x : xTextField.getText();
        String y = yTextField.getText().isEmpty() ? "" + value.y : yTextField.getText();
        String z = zTextField.getText().isEmpty() ? "" + value.z : zTextField.getText();

        Vector3f newValue = new Vector3f(
                Float.parseFloat(x),
                Float.parseFloat(y),
                Float.parseFloat(z)
        );

        if (getPropertyChangedEvent() != null) {
            getPropertyChangedEvent().propertyChanged(newValue);
        }

    }

    private final DocumentListener changeListener = new DocumentListener() {

        @Override public void insertUpdate(DocumentEvent e) {
            set();
        }

        @Override public void removeUpdate(DocumentEvent e) {
            set();
        }

        @Override public void changedUpdate(DocumentEvent e) {
            set();
        }

    };

}
