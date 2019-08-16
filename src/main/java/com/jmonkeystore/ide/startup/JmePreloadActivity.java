package com.jmonkeystore.ide.startup;

import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.ProjectManager;
import com.jme3.system.JmeSystem;
import com.jmonkeystore.ide.JmeVetoableProjectListener;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.natives.SecondaryNativeLoader;
import org.jetbrains.annotations.NotNull;


public class JmePreloadActivity extends PreloadingActivity {

    @Override
    public void preload(@NotNull ProgressIndicator indicator) {

        System.out.println("Loading Native Libraries...");
        loadNatives();

        // call the service for the first time, which will create and start the JME engine.
        indicator.setText("Initializing Jmonkey Engine...");
        ServiceManager.getService(JmeEngineService.class);
        indicator.setText("JmonkeyEngine Initialized.");

        // registers any project that has been opened after the engine has loaded.
        /*
        Application app = ApplicationManager.getApplication();
        final MessageBusConnection connection = app.getMessageBus().connect();
        connection.subscribe(ProjectLifecycleListener.TOPIC, new JmeProjectListener());
         */

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
