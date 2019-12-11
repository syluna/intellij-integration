package com.jmonkeystore.ide;

import com.intellij.openapi.vfs.*;
import com.jmonkeystore.ide.reflection.ClassPathListener;
import org.jetbrains.annotations.NotNull;

public class ReflectionFileListener implements VirtualFileListener {

    private final ClassPathListener classPathListener;

    public ReflectionFileListener(ClassPathListener classPathListener) {
        this.classPathListener = classPathListener;
    }

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
        classPathListener.run();
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        classPathListener.run();
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {
        classPathListener.run();
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
        classPathListener.run();
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent event) {
        classPathListener.run();
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent event) {
        classPathListener.run();
    }


}
