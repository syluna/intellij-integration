package com.jmonkeystore.ide.scene.explorer.impl.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;

public class NewSkyBoxDialog extends DialogWrapper {

    private JPanel contentPanel;
    private JScrollPane scrollPane;


    protected NewSkyBoxDialog(@NotNull Project project) {
        super(project, true);
        setTitle("New SkyBox...");
        init();

        createTree(project);
    }

    private void createTree(Project project) {

        VirtualFile[] paths = Arrays.stream(ProjectRootManager.getInstance(project).getContentSourceRoots())
                .filter(virtualFile -> virtualFile.getPath().endsWith("/resources"))
                .toArray(VirtualFile[]::new);

        for (VirtualFile path : paths) {

        }

    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPanel;
    }

}
