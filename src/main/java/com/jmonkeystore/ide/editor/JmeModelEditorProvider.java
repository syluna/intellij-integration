package com.jmonkeystore.ide.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jmonkeystore.ide.editor.impl.JmeModelFileEditorImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class JmeModelEditorProvider implements FileEditorProvider {

    @NonNls
    private static final String EXTENSION = "j3o";

    @NonNls
    private static final String EDITOR_TYPE_ID = "yumlmeDiagrams";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return Objects.requireNonNull(virtualFile.getExtension()).equalsIgnoreCase(EXTENSION);
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
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
