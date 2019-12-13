package com.jmonkeystore.ide.jme.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.jme3.app.SimpleApplication;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.camera.EditorCameraState;
import com.jmonkeystore.ide.jme.scene.NormalViewerState;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class JmeEngineServiceImpl extends SimpleApplication implements JmeEngineService {

    private static final Logger LOG = Logger.getInstance(JmeEngineServiceImpl.class);

    private SwingCanvasContext canvasContext;
    private EditorCameraState editorCameraState;

    private ExternalAssetLoader externalAssetLoader;

    public JmeEngineServiceImpl() {
        super(new EditorCameraState(), new NormalViewerState(), new EnvironmentCamera());

        AppSettings settings = new AppSettings(true);
        settings.setCustomRenderer(SwingCanvasContext.class);
        // settings.setFrameRate(60);
        settings.setVSync(true);
        settings.setResizable(true);
        settings.setAudioRenderer(null);
        setSettings(settings);
        setPauseOnLostFocus(false);

        assetManager = JmeSystem.newAssetManager(getClass().getResource(
                "/AssetManager/IntellijAssetManager.cfg"));


        createCanvas();
        startCanvas(true);
        canvasContext = (SwingCanvasContext) getContext();
        canvasContext.setSystemListener(this);

        this.externalAssetLoader = new ExternalAssetLoader(assetManager);

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
                            canvasContext.setInputSource(jmePanel);
                            canvasContext.setActivePanel(jmePanel);
                            editorCameraState.setActiveCamera(jmePanel.getCamera());
                        }
                        else if (mouseEvent.getID() == MouseEvent.MOUSE_EXITED) {
                            editorCameraState.removeActiveCamera();

                            // don't set this to null.
                            // at least one panel will be active if it has been created.
                            // this allows us to manipilate the panel (camera, scene, etc) whilst the mouse
                            // is not directly over it.
                            // Used for situations such as looking at objects we click on in the scene explorer.
                            // canvasContext.setActivePanel(null);
                        }
                    }

                }
            }
        }
    }

    @Override
    public JmePanel getActivePanel() {
        return canvasContext.getActivePanel();
    }

    @Override
    public void simpleInitApp() {

        // sceneCameraState = getStateManager().getState(SceneCameraState.class);
        editorCameraState = getStateManager().getState(EditorCameraState.class);
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

    public ExternalAssetLoader getExternalAssetLoader() {
        return externalAssetLoader;
    }

}
