package com.jmonkeystore.ide;

import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.VetoableProjectManagerListener;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

public class JmeVetoableProjectListener implements VetoableProjectManagerListener {

    @Override
    public boolean canClose(@NotNull Project project) {
        return true;
    }

    @Override
    public void projectOpened(@NotNull Project project) {

        MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new ModelFileAdapter());
    }

    @Override
    public void projectClosed(@NotNull Project project) {
    }

}
