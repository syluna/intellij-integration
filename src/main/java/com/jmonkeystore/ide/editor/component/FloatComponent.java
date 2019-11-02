package com.jmonkeystore.ide.editor.component;

import com.jmonkeystore.ide.util.NumberUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.lang.reflect.Method;

public class FloatComponent extends Component {

    private JPanel contentPanel;
    private JTextField valueTextField;
    private JLabel propertyNameLabel;

    public FloatComponent() {
        super(null, null, null);
    }

    public FloatComponent(Object object, Method getter, Method setter) {
        super(object, getter, setter);
    }

    private void set() {
        if (!valueTextField.getText().trim().isEmpty() && NumberUtils.isFloat(valueTextField.getText())) {
            float val = Float.parseFloat(valueTextField.getText());
            setValue(val);
        }
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);

        if (!isBinded()) {
            float newVal = (float) value;

            SwingUtilities.invokeLater(() -> {
                this.valueTextField.setText("" + newVal);
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

        this.valueTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent documentEvent) { set(); }
            @Override public void removeUpdate(DocumentEvent documentEvent) { set(); }
            @Override public void changedUpdate(DocumentEvent documentEvent) { set(); }
        });
    }

    @Override
    public void setPropertyName(String propertyName) {
        super.setPropertyName(propertyName);
        propertyNameLabel.setText("Float: " + propertyName);
    }

}
