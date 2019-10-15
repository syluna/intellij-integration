package com.jmonkeystore.ide.action.newscene;

import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateInDirectoryActionBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.scene.Node;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class NewSceneAction extends CreateInDirectoryActionBase implements WriteActionAware {

    public NewSceneAction() {
        super("Empty Scene", "Creates a new Jme Scene", IconLoader.getIcon("/Icons/jmonkey.png"));
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        IdeView view = e.getData(LangDataKeys.IDE_VIEW);
        Project project = e.getProject();

        if (view == null || project == null) {
            return;
        }

        PsiDirectory dir = view.getOrChooseDirectory();

        if (dir == null) {
            return;
        }

        NewSceneDialog dialog = new NewSceneDialog(project);

        if (dialog.showAndGet()) {

            String name = dir.getVirtualFile().getPath();
            System.out.println("dir name: " + name);

            ApplicationManager.getApplication().runWriteAction(() -> {

                String filename = dialog.getChosenName();

                if (!filename.toLowerCase().endsWith(".j3o")) {
                    filename += ".j3o";
                }

                PsiFile psiFile = dir.createFile(filename);

                File newFile = new File(psiFile.getVirtualFile().getPath());

                try {
                    BinaryExporter.getInstance().save(new Node(), newFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            });
        }

    }

}
