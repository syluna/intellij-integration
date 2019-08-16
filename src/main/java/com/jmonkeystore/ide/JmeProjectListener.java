package com.jmonkeystore.ide;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectLifecycleListener;
import com.jme3.asset.plugins.FileLocator;
import com.jmonkeystore.ide.jme.JmeEngineService;
import org.jetbrains.annotations.NotNull;

/**
 * Adds a project resources folder to the JmeEngineService AssetManager so assets can be found.
 */
public class JmeProjectListener implements ProjectLifecycleListener {

    private String getProjectResourcesDir(Project project) {
        return project.getBasePath() + "/src/main/resources";
    }

    public void projectComponentsInitialized(@NotNull Project project) {

        ServiceManager.getService(JmeEngineService.class)
                .getAssetManager()
                .registerLocator(getProjectResourcesDir(project), FileLocator.class);
    }

    @Override
    public void afterProjectClosed(@NotNull Project project) {

        ServiceManager.getService(JmeEngineService.class)
                .getAssetManager()
                .unregisterLocator(getProjectResourcesDir(project), FileLocator.class);
    }

}
