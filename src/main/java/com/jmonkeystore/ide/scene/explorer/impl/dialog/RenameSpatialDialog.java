package com.jmonkeystore.ide.scene.explorer.impl.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.jme3.scene.Spatial;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class RenameSpatialDialog extends DialogWrapper {

    private JPanel contentPanel;
    private JTextField nameTextField;

    public RenameSpatialDialog(Component parent, Spatial spatial) {
        super(parent, true);
        setTitle("Rename...");
        init();

        if (spatial.getName() != null) {
            nameTextField.setText(spatial.getName());
        }
    }

    public String getChosenName() {
        return nameTextField.getText();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPanel;
    }
}
