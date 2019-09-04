package com.jmonkeystore.ide.viewer.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.viewer.JmeMaterialViewer;
import com.jmonkeystore.ide.viewer.ui.JmeMaterialViewerUI;

import javax.swing.*;

public class JmeMaterialViewerImpl implements JmeMaterialViewer {

    private VirtualFile file;
    private Project project;

    private final JmeMaterialViewerUI editorUI;

    public JmeMaterialViewerImpl(Project project, VirtualFile file) {
        this.file = file;
        this.project = project;
        editorUI = new JmeMaterialViewerUI(this);
    }

    public VirtualFile getFile() {
        return file;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public void dispose() {
        ServiceManager.getService(JmeEngineService.class).removePanel(editorUI.getJmePanel());
        Disposer.dispose(this);
    }

    @Override
    public JComponent getJComponent() {
        return editorUI.getJmeComponent();
    }

    @Override
    public JComponent getContentComponent() {
        return editorUI.getJmeComponent();
    }

    @Override
    public JmeMaterialViewerUI getEditor() {
        return editorUI;
    }
}
