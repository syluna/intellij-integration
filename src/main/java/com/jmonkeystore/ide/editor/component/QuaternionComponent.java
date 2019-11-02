package com.jmonkeystore.ide.editor.component;

import com.jme3.math.Quaternion;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class QuaternionComponent extends Component {

    private JPanel contentPanel;
    private JFormattedTextField xTextField;
    private JFormattedTextField yTextField;
    private JFormattedTextField zTextField;
    private JLabel propertyNameLabel;

    public QuaternionComponent(Object parent, Method getter, Method setter) {
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

            Quaternion quaternion = (Quaternion) value;
            float[] angles = quaternion.toAngles(null);

            SwingUtilities.invokeLater(() -> {
                xTextField.setText("" + angles[0]);
                yTextField.setText("" + angles[1]);
                zTextField.setText("" + angles[2]);

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
        propertyNameLabel.setText("Quaternion: " + propertyName);
    }



    private void set() {

        Quaternion quaternion = (Quaternion) getReflectedProperty().getValue();
        float[] angles = quaternion.toAngles(null);

        String x = xTextField.getText().isEmpty() ? "" + angles[0] : xTextField.getText();
        String y = yTextField.getText().isEmpty() ? "" + angles[1] : yTextField.getText();
        String z = zTextField.getText().isEmpty() ? "" + angles[2] : zTextField.getText();

        float[] newValues = {
                Float.parseFloat(x),
                Float.parseFloat(y),
                Float.parseFloat(z)
        };

        Quaternion newQuaternion = new Quaternion().fromAngles(newValues);

        if (getPropertyChangedEvent() != null) {
            getPropertyChangedEvent().propertyChanged(newQuaternion);
        }

    }

    private final DocumentListener changeListener = new DocumentListener() {
        @Override public void insertUpdate(DocumentEvent e) { set(); }
        @Override public void removeUpdate(DocumentEvent e) { set(); }
        @Override public void changedUpdate(DocumentEvent e) { set(); }
    };

}
