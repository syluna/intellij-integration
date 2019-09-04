package com.jmonkeystore.ide.viewer.impl;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EventDispatcher;
import com.jmonkeystore.ide.viewer.JmeMaterialViewer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public class JmeMaterialFileViewerImpl implements FileEditor {

    private static final String NAME = "JME Material Viewer";

    private JmeMaterialViewer materialViewer;
    private final EventDispatcher<PropertyChangeListener> myDispatcher = EventDispatcher.create(PropertyChangeListener.class);


    public JmeMaterialFileViewerImpl(@NotNull Project project, @NotNull VirtualFile file) {
        materialViewer = new JmeMaterialViewerImpl(project, file);
        Disposer.register(this, materialViewer);
    }

    public JmeMaterialViewer getMaterialViewer() {
        return materialViewer;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return materialViewer.getJComponent();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return materialViewer.getContentComponent();
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {
    }

    @Override
    public void deselectNotify() {
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
        myDispatcher.addListener(propertyChangeListener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
        myDispatcher.removeListener(propertyChangeListener);
    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {

    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T t) {

    }

}
