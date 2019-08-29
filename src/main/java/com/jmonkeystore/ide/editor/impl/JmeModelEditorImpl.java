package com.jmonkeystore.ide.editor.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.jmonkeystore.ide.editor.JmeModelEditor;
import com.jmonkeystore.ide.editor.ui.JmeModelEditorUI;
import com.jmonkeystore.ide.jme.JmeEngineService;

import javax.swing.*;

public class JmeModelEditorImpl implements JmeModelEditor {

    private static final Logger LOG = Logger.getInstance(JmeModelEditorImpl.class);

    private VirtualFile file;
    private Project project;

    private final JmeModelEditorUI editorUI;

    public JmeModelEditorImpl(Project project, VirtualFile file) {
        this.file = file;
        this.project = project;
        editorUI = new JmeModelEditorUI(this);
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
    public JmeModelEditorUI getEditor() {
        return editorUI;
    }


}
