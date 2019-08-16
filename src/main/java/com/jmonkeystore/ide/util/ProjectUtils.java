package com.jmonkeystore.ide.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;

import java.awt.*;

public class ProjectUtils {

    public static Project getActiveProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                return project;
            }
        }

        return null;
    }

}
