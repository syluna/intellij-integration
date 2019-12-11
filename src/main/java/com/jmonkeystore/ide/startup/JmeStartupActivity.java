package com.jmonkeystore.ide.startup;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.messages.MessageBus;
import com.jme3.system.JmeSystem;
import com.jmonkeystore.ide.JmeVetoableProjectListener;
import com.jmonkeystore.ide.ModelFileAdapter;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.natives.SecondaryNativeLoader;
import com.jmonkeystore.ide.reflection.ClassPathListener;
import com.jmonkeystore.ide.scene.editor.PropertyEditorService;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.UUID;


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
        ServiceManager.getService(ClassPathListener.class);

        MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new ModelFileAdapter());

        ProjectManager.getInstance().addProjectManagerListener(new JmeVetoableProjectListener());

        // VirtualFileManager.getInstance().addVirtualFileListener(new ReflectionFileListener(classPathListener));
        // watchForCompile(project);

    }

    private void watchForCompile(Project project) {

        ClassPathListener classPathListener = new ClassPathListener(project);



        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();

            Path path = Paths.get(project.getBasePath(), "build", "libs");

            WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            new Thread(() -> {

                WatchKey key;

                boolean runing = true;

                while(runing) {

                    try {

                        while ((key = watchService.take()) != null) {

                            System.out.println(".JAR CHANGED");
                            key.pollEvents();

                            classPathListener.run();
                            System.out.println("CLASSPATH UPDATED");

                            key.reset();

                        }

                        Thread.sleep(500);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static boolean testLockFile(File p_fi) {
        boolean bLocked = false;
        try (RandomAccessFile fis = new RandomAccessFile(p_fi, "rw")) {
            FileLock lck = fis.getChannel().lock();
            lck.release();
        } catch (Exception ex) {
            bLocked = true;
        }
        if (bLocked)
            return bLocked;
        // try further with rename
        String parent = p_fi.getParent();
        String rnd = UUID.randomUUID().toString();
        File newName = new File(parent + "/" + rnd);
        if (p_fi.renameTo(newName)) {
            newName.renameTo(p_fi);
        } else
            bLocked = true;
        return bLocked;
    }

}
