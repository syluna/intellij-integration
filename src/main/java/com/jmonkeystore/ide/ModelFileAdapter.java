package com.jmonkeystore.ide;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.jme3.scene.Spatial;
import com.jmonkeystore.ide.editor.impl.JmeModelFileEditorImpl;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Listens for .j3o files being opened and selected and sets the scene explorer root from the selected model.
 */
public class ModelFileAdapter implements FileEditorManagerListener {


    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {

        if (Objects.requireNonNull(file.getExtension()).equalsIgnoreCase("j3o")) {
            System.out.println("Opened j3o");
        }

    }

    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {

        if (Objects.requireNonNull(file.getExtension()).equalsIgnoreCase("j3o")) {
            System.out.println("Closed j3o");
            ServiceManager.getService(SceneExplorerService.class).setScene(null, null);
        }

    }

    public void selectionChanged(@NotNull FileEditorManagerEvent event) {

        if (event.getNewFile() != null && Objects.requireNonNull(event.getNewFile().getExtension()).equalsIgnoreCase("j3o")) {
            System.out.println("Changed to j3o");

            if (event.getNewEditor() instanceof JmeModelFileEditorImpl) {
                JmeModelFileEditorImpl impl = (JmeModelFileEditorImpl) event.getNewEditor();
                Spatial scene = impl.getModelEditor().getEditor().getScene();
                ServiceManager.getService(SceneExplorerService.class).setScene(scene, event.getNewFile());
            }
        }
        else {
            ServiceManager.getService(SceneExplorerService.class).setScene(null, null);
        }
    }

}
