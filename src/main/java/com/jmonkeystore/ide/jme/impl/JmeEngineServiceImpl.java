package com.jmonkeystore.ide.jme.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.ModelKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jmonkeystore.ide.ModelFileAdapter;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.camera.SceneCameraState;
import com.jmonkeystore.ide.jme.scene.NormalViewerState;
import com.jmonkeystore.ide.util.ProjectUtils;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class JmeEngineServiceImpl extends SimpleApplication implements JmeEngineService {

    private static final Logger LOG = Logger.getInstance(JmeEngineServiceImpl.class);

    private SwingCanvasContext canvasContext;
    private SceneCameraState sceneCameraState;

    public JmeEngineServiceImpl() {
        super(new SceneCameraState(), new NormalViewerState());

        AppSettings settings = new AppSettings(true);
        settings.setCustomRenderer(SwingCanvasContext.class);
        settings.setWidth(640);
        settings.setHeight(480);
        settings.setFrameRate(60);
        settings.setResizable(true);
        settings.setAudioRenderer(null);
        setSettings(settings);
        setPauseOnLostFocus(false);

        assetManager = JmeSystem.newAssetManager(getClass().getResource(
                "/AssetManager/IntellijAssetManager.cfg"));

        Project project = ProjectUtils.getActiveProject();

        if (project != null) {
            String targetRoot = project.getBasePath() + "/src/main/resources";
            assetManager.registerLocator(targetRoot, FileLocator.class);

            MessageBus messageBus = project.getMessageBus();
            messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new ModelFileAdapter());
        }


        createCanvas();
        startCanvas(true);
        canvasContext = (SwingCanvasContext) getContext();
        canvasContext.setSystemListener(this);

        Toolkit.getDefaultToolkit().addAWTEventListener(new TargetedMouseListener(), AWTEvent.MOUSE_EVENT_MASK);
    }

    public JmePanel getOrCreatePanel(String name) {

        Callable<JmePanel> callable = () -> {
            JmePanel jmePanel = canvasContext.getPanel(name);

            if (jmePanel != null) {
                System.out.println("Retrieved Panel: " + name);
                return jmePanel;
            }

            return createPanel(name);
        };

        Future<JmePanel> future = enqueue(callable);

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private JmePanel createPanel(String name) {
        System.out.println("Creating Panel: " + name);
        JmePanel panel = new JmePanel(name, getRenderManager());
        canvasContext.addPanel(panel);
        System.out.println("Active Panels: " + canvasContext.getPanelCount());
        return panel;
    }

    public void removePanel(JmePanel jmePanel) {
        enqueue(() -> {
            System.out.println("Removing Panel: " + jmePanel.getName());
            canvasContext.removePanel(jmePanel);
            System.out.println("Active Panels: " + canvasContext.getPanelCount());
        });
    }




    /**
     * Activates the JME camera control for the panel that the mouse is over.
     */
    private class TargetedMouseListener implements AWTEventListener {

        @Override
        public void eventDispatched(AWTEvent event) {
            if (event instanceof MouseEvent) {

                MouseEvent mouseEvent = (MouseEvent)event;

                if (event.getSource() instanceof JmePanel) {

                    JmePanel jmePanel = (JmePanel) event.getSource();

                    if (canvasContext.containsPanel(jmePanel)) {
                        if (mouseEvent.getID() == MouseEvent.MOUSE_ENTERED) {
                            //if (canvasContext.getInputSource() != jmePanel) {
                                canvasContext.setInputSource(jmePanel);
                                // intellijFlyCamAppState.setCamera(jmePanel.getCamera());
                                sceneCameraState.setCamera(jmePanel.getCamera());
                            //}
                        }
                        else if (mouseEvent.getID() == MouseEvent.MOUSE_EXITED) {
                            // intellijFlyCamAppState.removeActiveCamera();
                            sceneCameraState.removeActiveCamera();
                        }
                    }

                }
            }
        }
    }

    @Override
    public void simpleInitApp() {
        sceneCameraState = getStateManager().getState(SceneCameraState.class);
        inputManager.setCursorVisible(true);

        getStateManager().getState(NormalViewerState.class).setEnabled(false);

        // @todo provide an options page to let the users decide what to use.
        // - anistropic filtering, FXAA, etc.
        /*
        assetManager.addAssetEventListener(new AssetEventListener() {
            @Override
            public void assetLoaded(AssetKey key) {
                if (key.getExtension().equalsIgnoreCase("png") || key.getExtension().equalsIgnoreCase("jpg") || key.getExtension().equalsIgnoreCase("dds") || key.getExtension().equalsIgnoreCase("jpeg")) {
                    System.out.println(key.getExtension());
                    TextureKey tkey = (TextureKey) key;
                    tkey.setAnisotropy(16);
                }
            }

            @Override
            public void assetRequested(AssetKey key) {

            }

            @Override
            public void assetDependencyNotFound(AssetKey parentKey, AssetKey dependentAssetKey) {

            }
        });
         */

    }

    public void close() {
        //stop();
    }

    @Override
    public void simpleUpdate(float tpf) {
        // im not sure why this is being called from another thread...
        enqueue(() -> canvasContext.update(tpf, getRenderManager()));

    }

    public Spatial loadExternalModel(String url) {
        return loadExternalModel(new ModelKey(url));
    }

    @Override
    public Spatial loadExternalModel(ModelKey modelKey) {

        // determine if this model is from a jar dependency or from the open project.

        String url = modelKey.getName();

        if (!url.startsWith("jar://")) {

            File path = new File(url);

            // @TODO: the file:// protocol could cause an issue...

            String dir = path.getParent().replace("file:\\", "");
            String name = path.getName();

            assetManager.registerLocator(dir, FileLocator.class);
            Spatial model = assetManager.loadModel(name);
            assetManager.unregisterLocator(dir, FileLocator.class);

            // we don't want to keep assets cached that are loaded from external sources.
            // it also causes issues when assets have the same name (e.g. sketchfab uses scene.gltf a lot)
            // this occurs because we register the path, so only the model name is the key.
            assetManager.deleteFromCache((ModelKey) model.getKey());

            return model;
        }
        else {

            String jarPart = "jar!/";
            int index = url.indexOf(jarPart);
            String modelPath = url.substring(index + jarPart.length());

            LOG.debug("Loading Model: " + modelPath);

            try {
                Spatial model = assetManager.loadModel(modelPath);
                return model;
            }
            catch (AssetNotFoundException ex) {

                // if the asset wasn't found, we need to add the jar to the classpath.
                // a try/catch might seem a bit heavyweight, but it means we don't have to keep a list
                // of jars that we've added. It also avoids a bunch of extra code to do a null check
                // which is what this process does anyway. I'm satisfied this is a suitable approach.

                String jarUrl = url.replace("jar://", "");
                jarUrl = jarUrl.substring(0, jarUrl.indexOf("!/"));

                LOG.debug("Adding jar to classpath: " + jarUrl);

                File jarFile = new File(jarUrl);

                addToClasspath(getClass().getClassLoader(), jarFile);

                Spatial model = assetManager.loadModel(modelPath);
                return model;
            }
        }

    }

    private void addToClasspath(ClassLoader classLoader, File file) {
        try {
            URL url = file.toURI().toURL();
            Method method = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

}
