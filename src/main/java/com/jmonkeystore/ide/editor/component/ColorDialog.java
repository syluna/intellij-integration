package com.jmonkeystore.ide.editor.component;

import com.intellij.openapi.ui.DialogWrapper;
import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ColorDialog extends DialogWrapper {

    private Color color;
    private JPanel contentPanel;

    protected ColorDialog(Color initialColor) {
        super(true);

        this.color = initialColor;

        init();
        setOKButtonText("Select Color");
        setTitle("Select Color");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        contentPanel = new JPanel();
        contentPanel.setLayout(new VerticalLayout());

        JColorChooser jColorChooser = new JColorChooser();

        jColorChooser.getSelectionModel().addChangeListener(e -> {
            color = jColorChooser.getColor();
        });

        contentPanel.add(jColorChooser);

        return contentPanel;
    }

    public Color getColor() {
        return color;
    }
}
