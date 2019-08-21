package com.jmonkeystore.ide.startup;

import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.ProjectManager;
import com.jme3.system.JmeSystem;
import com.jmonkeystore.ide.JmeVetoableProjectListener;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.natives.SecondaryNativeLoader;
import com.jmonkeystore.ide.scene.editor.PropertyEditorService;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;
import org.jetbrains.annotations.NotNull;


public class JmePreloadActivity extends PreloadingActivity {

    @Override
    public void preload(@NotNull ProgressIndicator indicator) {

        indicator.setText("Loading native libraries...");
        loadNatives();

        indicator.setText("Initializing scene explorer...");
        ServiceManager.getService(SceneExplorerService.class);

        indicator.setText("Initializing property editor...");
        ServiceManager.getService(PropertyEditorService.class);

        // call the service for the first time, which will create and start the JME engine.
        indicator.setText("Initializing jmonkeyengine...");
        ServiceManager.getService(JmeEngineService.class);

        indicator.setText("JmonkeyEngine integration initialized.");

        // register/de-register projects as they open and close
        ProjectManager.getInstance().addProjectManagerListener(new JmeVetoableProjectListener());
    }

    private void loadNatives() {
        if (JmeSystem.isLowPermissions()) {
            return;
        }

        SecondaryNativeLoader.loadNativeLibrary("jinput", true);
        SecondaryNativeLoader.loadNativeLibrary("jinput-dx8", true);
        SecondaryNativeLoader.loadNativeLibrary("lwjgl", true);
    }

}
