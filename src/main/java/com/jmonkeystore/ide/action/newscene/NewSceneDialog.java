package com.jmonkeystore.ide.action.newscene;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class NewSceneDialog extends DialogWrapper {

    private JPanel contentPanel;
    private JTextField nameTextField;


    public NewSceneDialog(Project project) {
        super(project);
        setTitle("Create New Scene");
        init();
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
