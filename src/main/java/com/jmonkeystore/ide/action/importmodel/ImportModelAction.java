package com.jmonkeystore.ide.action.importmodel;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.jmonkeystore.ide.action.importmodel.importer.ModelImporter;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ImportModelAction extends AnAction {


    public ImportModelAction() {
        super();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        Project project = anActionEvent.getProject();

        if (project == null) {
            return;
        }

        ImportModelDialog importModelDialog = new ImportModelDialog();

        if(importModelDialog.showAndGet()) {
            if (importModelDialog.getSelectedFile() != null) {
                importSelectedModel(anActionEvent.getProject(), importModelDialog.getSelectedFile());
            }
        }

        importModelDialog.cleanupPanel();
    }

    private void importSelectedModel(Project project, File selectedFile) {
        ModelImporter importer = new ModelImporter();

        String projectPath = project.getBasePath();

        String srcRoot = selectedFile.getParent();
        String targetRoot = projectPath + "/src/main/resources";
        String targetAssetPath = "Models/" + selectedFile.getParentFile().getName();
        String modelPath = selectedFile.getAbsolutePath();

        importer.begin(srcRoot, targetRoot, targetAssetPath, modelPath);

        // Refresh the "project" tree. If we don't do this the folder tree doesn't get updated to show the new model.
        // It might be possible to refresh the actual directory instead of the whole project.
        LocalFileSystem.getInstance().refresh(true);
    }

}
