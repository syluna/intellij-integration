package com.jmonkeystore.ide.editor.component;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnumComponent extends Component {

    private JPanel contentPanel;
    private JComboBox valueComboBox;
    private JLabel propertyNameLabel;

    public EnumComponent(Object parent, Method getter, Method setter) {
        super(parent, getter, setter);

        try {
            setValue(getter.invoke(parent));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setEnumValues(Class<? extends Enum> enumData) {
        valueComboBox.setModel(new DefaultComboBoxModel<>(enumData.getEnumConstants()));

    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);

        if (!isBinded()) {

            Enum enumValue = (Enum) value;

            SwingUtilities.invokeLater(() -> {
                valueComboBox.setSelectedItem(enumValue);
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

        valueComboBox.addActionListener(actionEvent -> {
            setValue(valueComboBox.getSelectedItem());

            if (!isBinded()) {
                bind();
            }
        });
    }

    @Override
    public void setPropertyName(String propertyName) {
        super.setPropertyName(propertyName);
        propertyNameLabel.setText("Enum: " + propertyName);
    }
}
