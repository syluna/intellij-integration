package com.jmonkeystore.ide.util;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SimpleTextDialog extends DialogWrapper {

    // Strange setup. I "believe" that a new instance is created using a no-args constructor, then the args constructor
    // is called after creation and calling createCenterPanel() for some reason. weird.

    private JLabel label;

    public SimpleTextDialog(String title, String text) {
        super(true); // use current window as parent
        init();
        setTitle(title);

        label.setText(text);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());

        label = new JLabel();
        dialogPanel.add(label, BorderLayout.CENTER);

        return dialogPanel;
    }

}
