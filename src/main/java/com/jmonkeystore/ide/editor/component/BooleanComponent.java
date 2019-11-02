package com.jmonkeystore.ide.editor.component;

import javax.swing.*;
import java.lang.reflect.Method;

public class BooleanComponent extends Component {

    private JCheckBox checkBox;
    private JPanel contentPanel;

    public BooleanComponent() {
        super(null, null, null);
    }

    public BooleanComponent(Object parent, Method getter, Method setter) {
        super(parent, getter, setter);
    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);

        if (!isBinded()) {
            boolean newVal = (boolean) value;

            SwingUtilities.invokeLater(() -> {
                this.checkBox.setSelected(newVal);
                bind();
            });
        }

    }

    @Override
    public void bind() {
        super.bind();

        checkBox.addActionListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            setValue(checkbox.isSelected());
        });
    }

    @Override
    public void setPropertyName(String propertyName) {
        super.setPropertyName(propertyName);
        checkBox.setText("Float: " + propertyName);
    }

}
