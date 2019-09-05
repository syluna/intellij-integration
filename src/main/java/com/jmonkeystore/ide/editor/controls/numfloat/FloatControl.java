package com.jmonkeystore.ide.editor.controls.numfloat;

import com.jmonkeystore.ide.editor.controls.JmeEditorControl;
import com.jmonkeystore.ide.util.NumberUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class FloatControl implements JmeEditorControl {

    private float value;

    private JPanel contentPanel;
    private JTextField valueTextField;

    public FloatControl(String title, float value) {
        this.value = value;

        ((TitledBorder)contentPanel.getBorder()).setTitle("Float: " + title);
        refresh();

        this.valueTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { set(); }
            @Override public void removeUpdate(DocumentEvent e) { set(); }
            @Override public void changedUpdate(DocumentEvent e) { set(); }
        });

    }

    private void set() {
        if (!valueTextField.getText().trim().isEmpty() && NumberUtils.isFloat(valueTextField.getText())) {
            float val = Float.parseFloat(valueTextField.getText());
            setValue(val);
        }
    }


    public abstract float getValue();
    public abstract void setValue(float value);

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void refresh() {
        valueTextField.setText("" + getValue());
    }

    @Override
    public void cleanup() {

    }

}
