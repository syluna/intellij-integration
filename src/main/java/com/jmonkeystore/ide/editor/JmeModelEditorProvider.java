package com.jmonkeystore.ide.editor;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jmonkeystore.ide.editor.impl.JmeModelFileEditorImpl;
import com.jmonkeystore.ide.jme.JmeEngineService;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class JmeModelEditorProvider implements FileEditorProvider, DumbAware {

    @NonNls
    private static final String EXTENSION = "j3o";

    @NonNls
    private static final String EDITOR_TYPE_ID = "JmeSceneEditor";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return Objects.requireNonNull(virtualFile.getExtension()).equalsIgnoreCase(EXTENSION);
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {

        ServiceManager.getService(JmeEngineService.class)
                .getExternalAssetLoader()
                .registerRoot(project, virtualFile);

        return new JmeModelFileEditorImpl(project, virtualFile);
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
