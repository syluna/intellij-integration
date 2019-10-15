package com.jmonkeystore.ide.viewer;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.viewer.impl.JmeMaterialFileViewerImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class JmeMaterialViewerProvider implements FileEditorProvider {

    @NonNls
    private static final String EXTENSION = "j3m";

    @NonNls
    private static final String EDITOR_TYPE_ID = "JmeMaterialViewer";

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

        return new JmeMaterialFileViewerImpl(project, virtualFile);
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}
