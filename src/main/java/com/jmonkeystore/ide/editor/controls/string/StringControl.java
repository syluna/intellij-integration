package com.jmonkeystore.ide.editor.controls.string;

import com.jmonkeystore.ide.editor.controls.JmeEditorControl;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@Deprecated
public  abstract class StringControl implements JmeEditorControl {

    private JPanel contentPanel;
    private JTextField valueTextField;

    public StringControl(String title, String value) {

        ((TitledBorder)contentPanel.getBorder()).setTitle("String: " + title);
        refresh();

        this.valueTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { set(); }
            @Override public void removeUpdate(DocumentEvent e) { set(); }
            @Override public void changedUpdate(DocumentEvent e) { set(); }
        });

    }

    private void set() {
        setValue(valueTextField.getText());
    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    public abstract String getValue();
    public abstract void setValue(String value);

    @Override
    public void refresh() {
        valueTextField.setText(getValue());
    }

    @Override
    public void cleanup() {

    }
}
