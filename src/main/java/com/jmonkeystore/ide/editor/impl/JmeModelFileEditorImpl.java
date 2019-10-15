package com.jmonkeystore.ide.editor.impl;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EventDispatcher;
import com.jmonkeystore.ide.editor.JmeModelEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public class JmeModelFileEditorImpl implements FileEditor {

    private static final String NAME = "JME Model Editor";

    private JmeModelEditor modelEditor;
    private final EventDispatcher<PropertyChangeListener> myDispatcher = EventDispatcher.create(PropertyChangeListener.class);


    public JmeModelFileEditorImpl(@NotNull Project project, @NotNull VirtualFile file) {

        modelEditor = new JmeModelEditorImpl(project, file);
        Disposer.register(this, modelEditor);
    }

    public JmeModelEditor getModelEditor() {
        return modelEditor;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return modelEditor.getJComponent();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return modelEditor.getContentComponent();
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
