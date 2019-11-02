package com.jmonkeystore.ide.editor.component;

import com.jme3.math.Vector4f;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Vector4fComponent extends Component {

    private JPanel contentPanel;
    private JLabel propertyNameLabel;

    private JFormattedTextField xTextField;
    private JFormattedTextField yTextField;
    private JFormattedTextField zTextField;
    private JFormattedTextField wTextField;


    public Vector4fComponent() {
        super(null, null, null);
    }

    public Vector4fComponent(Object parent, Method getter, Method setter) {
        super(parent, getter, setter);

        FloatFormatFactory floatFormatFactory = new FloatFormatFactory();

        xTextField.setFormatterFactory(floatFormatFactory);
        yTextField.setFormatterFactory(floatFormatFactory);
        zTextField.setFormatterFactory(floatFormatFactory);
        wTextField.setFormatterFactory(floatFormatFactory);

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

            Vector4f vector4f = (Vector4f) value;

            SwingUtilities.invokeLater(() -> {
                this.xTextField.setText("" + vector4f.x);
                this.yTextField.setText("" + vector4f.y);
                this.zTextField.setText("" + vector4f.z);
                this.wTextField.setText("" + vector4f.w);

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
        wTextField.getDocument().addDocumentListener(changeListener);
    }

    @Override
    public void setPropertyName(String propertyName) {
        super.setPropertyName(propertyName);
        propertyNameLabel.setText("Vector4f: " + propertyName);
    }

    private void set() {

        Vector4f value = (Vector4f) getReflectedProperty().getValue();

        String x = xTextField.getText().isEmpty() ? "" + value.x : xTextField.getText();
        String y = yTextField.getText().isEmpty() ? "" + value.y : yTextField.getText();
        String z = zTextField.getText().isEmpty() ? "" + value.z : zTextField.getText();
        String w = wTextField.getText().isEmpty() ? "" + value.w : wTextField.getText();

        Vector4f newValue = new Vector4f(
                Float.parseFloat(x),
                Float.parseFloat(y),
                Float.parseFloat(z),
                Float.parseFloat(w)
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
