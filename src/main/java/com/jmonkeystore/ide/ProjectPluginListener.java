package com.jmonkeystore.ide;

import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import org.jetbrains.annotations.NotNull;

public class ProjectPluginListener implements VirtualFileListener {

    /**
     * Fired when the contents of a virtual file is changed.
     *
     * @param event the event object containing information about the change.
     */
    public void contentsChanged(@NotNull VirtualFileEvent event) {

        if (event.getFile().getExtension() != null && event.getFile().getExtension().equals("java")) {

            System.out.println(".java file changed.");

        }

    }

}
