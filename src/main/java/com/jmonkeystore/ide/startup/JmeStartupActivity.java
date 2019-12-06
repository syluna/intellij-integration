package com.jmonkeystore.ide.startup;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.messages.MessageBus;
import com.jme3.system.JmeSystem;
import com.jmonkeystore.ide.JmeVetoableProjectListener;
import com.jmonkeystore.ide.ModelFileAdapter;
import com.jmonkeystore.ide.ProjectPluginListener;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.natives.SecondaryNativeLoader;
import com.jmonkeystore.ide.scene.editor.PropertyEditorService;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;
import org.jetbrains.annotations.NotNull;


public class JmeStartupActivity implements StartupActivity {

    private void loadNatives() {
        if (JmeSystem.isLowPermissions()) {
            return;
        }

        SecondaryNativeLoader.loadNativeLibrary("jinput", true);
        SecondaryNativeLoader.loadNativeLibrary("jinput-dx8", true);
        SecondaryNativeLoader.loadNativeLibrary("lwjgl", true);
    }

    @Override
    public void runActivity(@NotNull Project project) {

        loadNatives();
        ServiceManager.getService(SceneExplorerService.class);
        ServiceManager.getService(PropertyEditorService.class);
        ServiceManager.getService(JmeEngineService.class);

        MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new ModelFileAdapter());

        ProjectManager.getInstance().addProjectManagerListener(new JmeVetoableProjectListener());

        VirtualFileManager.getInstance().addVirtualFileListener(new ProjectPluginListener());
    }
}
