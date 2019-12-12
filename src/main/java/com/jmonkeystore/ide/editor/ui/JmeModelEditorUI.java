package com.jmonkeystore.ide.editor.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.debug.WireSphere;
import com.jmonkeystore.ide.editor.impl.JmeModelEditorImpl;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.impl.JmePanel;
import com.jmonkeystore.ide.jme.scene.NormalViewerState;
import com.jmonkeystore.ide.jme.scene.WireProcessor;
import com.jmonkeystore.ide.jme.sky.SkyLoader;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class JmeModelEditorUI implements Disposable {

    private JmePanel jmePanel;
    private JPanel formPanel;
    private JPanel outputPanel;
    private JLabel infoLabel;

    private final Spatial scene;

    private final DirectionalLight directionalLight;
    private final AmbientLight ambientLight;
    private final LightProbe lightProbe;

    private final SceneProcessor wireProcessor;
    private Spatial sky;
    private final Geometry grid;

    // bounding box for highlighted items
    private Geometry bbGeom; // used for highlighting bounding boxes
    private Geometry meshGeom; // used for highlighting meshes

    // Skybox menu items. Used to set checked/unchecked when we select one.
    private JCheckBoxMenuItem[] skyBoxMenuItems;

    public JmeModelEditorUI(JmeModelEditorImpl modelEditor) {
        super();

        VirtualFile[] roots = ProjectRootManager.getInstance(modelEditor.getProject()).getContentSourceRoots();
        System.out.println("ROOTS:");
        for (VirtualFile root : roots) {
            System.out.println(root.getPath());
        }

        JMenuBar menuBar = createMenuBar();
        outputPanel.add(menuBar, BorderLayout.PAGE_START);

        infoLabel = new JLabel();
        outputPanel.add(infoLabel, BorderLayout.SOUTH);

        JmeEngineService engineService = ServiceManager.getService(JmeEngineService.class);
        jmePanel = engineService.getOrCreatePanel(modelEditor.getFile().getUrl());

        outputPanel.addComponentListener(new JmeComponentListener());
        outputPanel.add(jmePanel);

        // create tools
        wireProcessor = new WireProcessor(engineService.getAssetManager());

        directionalLight = new DirectionalLight(new Vector3f(-1, -1, -1).normalizeLocal(), ColorRGBA.White.mult(0.7f));
        ambientLight = new AmbientLight(ColorRGBA.White.mult(0.5f));

        directionalLight.setEnabled(false);
        ambientLight.setEnabled(false);

        grid = createGrid(engineService.getAssetManager());

        // Spatial probeModel = engineService.getAssetManager().loadModel("Scenes/defaultProbe.j3o");
        Spatial probeModel = engineService.getAssetManager().loadModel("Models/lightprobe.j3o");
        lightProbe = (LightProbe) probeModel.getLocalLightList().get(0);
        lightProbe.setBounds(new BoundingSphere(500, new Vector3f(0, 0, 0)));

        sky = SkyLoader.LAGOON.load();
        skyBoxMenuItems[4].setSelected(true); // set the lagoon menuItem to selected.

        // animTimeSlider.setModel(animTimelineModel);

        scene = engineService.getExternalAssetLoader().load(modelEditor.getFile().getUrl(), Spatial.class);

        engineService.enqueue(() -> {
            jmePanel.getRootNode().addLight(directionalLight);
            jmePanel.getRootNode().addLight(ambientLight);

            jmePanel.getRootNode().attachChild(grid);
            // jmePanel.getRootNode().attachChild(sky);

            if (scene != null) {
                jmePanel.getRootNode().attachChild(scene);

                EventQueue.invokeLater(() -> {
                    ServiceManager.getService(SceneExplorerService.class).setScene(scene, modelEditor.getFile());
                });

                EventQueue.invokeLater(() -> {
                    infoLabel.setText(String.format("Vertices: %d / Triangles: %d", scene.getVertexCount(), scene.getTriangleCount()));
                });
            }

            jmePanel.getCamera().setLocation(new Vector3f(0, 5, 15));
            jmePanel.getCamera().lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);

        });

    }

    public void clearAllHighlights() {
        ServiceManager.getService(JmeEngineService.class).enqueue(() -> {
            clearBoundingBoxHighlight();
            clearMeshHighlight();
        });
    }

    public void clearBoundingBoxHighlight() {
        if (bbGeom != null) {
            bbGeom.removeFromParent();
            bbGeom = null;
        }
    }

    public void clearMeshHighlight() {
        if (meshGeom != null) {
            meshGeom.removeFromParent();
            meshGeom = null;
        }
    }

    public void highlightWithBoundingBox(Spatial spatial) {

        clearAllHighlights();

        if (spatial.getWorldBound() != null) {
            ServiceManager.getService(JmeEngineService.class).enqueue(() -> {

                if (spatial.getWorldBound() instanceof BoundingBox) {

                    this.bbGeom = WireBox.makeGeometry((BoundingBox) spatial.getWorldBound());

                    this.bbGeom.setMaterial(new Material(ServiceManager.getService(JmeEngineService.class).getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
                    this.bbGeom.getMaterial().getAdditionalRenderState().setLineWidth(2);
                    this.bbGeom.getMaterial().getAdditionalRenderState().setWireframe(true);
                    this.bbGeom.getMaterial().setColor("Color", ColorRGBA.Blue);

                    jmePanel.getRootNode().attachChild(bbGeom);

                }
                else if (spatial.getWorldBound() instanceof BoundingSphere) {

                    BoundingSphere boundingSphere = (BoundingSphere) spatial.getWorldBound();
                    WireSphere wireSphere = new WireSphere(boundingSphere.getRadius());

                    this.bbGeom = new Geometry("Bounding Sphere Geometry", wireSphere);
                    this.bbGeom.setMaterial(new Material(ServiceManager.getService(JmeEngineService.class).getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
                    this.bbGeom.getMaterial().getAdditionalRenderState().setLineWidth(2);
                    this.bbGeom.getMaterial().getAdditionalRenderState().setWireframe(true);
                    this.bbGeom.getMaterial().setColor("Color", ColorRGBA.Blue);

                    jmePanel.getRootNode().attachChild(bbGeom);

                }

            });
        }
    }

    public void highlightMesh(Geometry geometry) {

        clearAllHighlights();

        if (geometry != null) {
            ServiceManager.getService(JmeEngineService.class).enqueue(() -> {
                this.meshGeom = new Geometry("Mesh Highlight", geometry.getMesh());
                this.meshGeom.setLocalRotation(geometry.getWorldRotation());
                this.meshGeom.setLocalTranslation(geometry.getWorldTranslation());
                this.meshGeom.setLocalScale(geometry.getWorldScale());

                this.meshGeom.setMaterial(new Material(ServiceManager.getService(JmeEngineService.class).getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
                this.meshGeom.getMaterial().getAdditionalRenderState().setLineWidth(2);
                this.meshGeom.getMaterial().getAdditionalRenderState().setWireframe(true);
                this.meshGeom.getMaterial().setColor("Color", ColorRGBA.Blue);

                jmePanel.getRootNode().attachChild(this.meshGeom);
            });
        }
    }



    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // lighting
        JMenu lightsMenu = new JMenu("Lighting");
        lightsMenu.addMenuListener(redrawListener);

        JCheckBoxMenuItem dirLightMenuItem = new JCheckBoxMenuItem("Directional");
        dirLightMenuItem.addActionListener(e -> {
            JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) e.getSource();
            ServiceManager.getService(JmeEngineService.class).enqueue(() -> directionalLight.setEnabled(checkbox.isSelected()));
        });

        lightsMenu.add(dirLightMenuItem);

        JCheckBoxMenuItem ambLightMenuItem = new JCheckBoxMenuItem("Ambient");
        ambLightMenuItem.addActionListener(e -> {
            JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) e.getSource();
            ServiceManager.getService(JmeEngineService.class).enqueue(() -> ambientLight.setEnabled(checkbox.isSelected()));
        });
        lightsMenu.add(ambLightMenuItem);

        JCheckBoxMenuItem probeLightMenuItem = new JCheckBoxMenuItem("LightProbe");
        probeLightMenuItem.addActionListener(e -> {
            JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) e.getSource();
            if (checkbox.isSelected()) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> jmePanel.getRootNode().addLight(lightProbe));
            }
            else {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> jmePanel.getRootNode().removeLight(lightProbe));
            }
        });
        lightsMenu.add(probeLightMenuItem);

        menuBar.add(lightsMenu);

        // sky
        JMenu skyMenu = new JMenu("Sky");

        JCheckBoxMenuItem showSkyBoxMenuItem = new JCheckBoxMenuItem("Show SkyBox", false);
        showSkyBoxMenuItem.addActionListener(e -> {
            JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) e.getSource();
            if (checkbox.isSelected()) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> jmePanel.getRootNode().attachChild(sky));
            }
            else {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> sky.removeFromParent());
            }
        });
        skyMenu.add(showSkyBoxMenuItem);

        // skybox
        skyBoxMenuItems = new JCheckBoxMenuItem[SkyLoader.values().length];

        for (int i = 0; i < SkyLoader.values().length; i++) {
            SkyLoader skyLoader = SkyLoader.values()[i];
            skyBoxMenuItems[i] = new JCheckBoxMenuItem(skyLoader.getFriendlyName());
            skyBoxMenuItems[i].addActionListener(this::selectSkyBox);
            skyMenu.add(skyBoxMenuItems[i]);
        }

        menuBar.add(skyMenu);
        skyMenu.addMenuListener(redrawListener);

        // debug
        JMenu debugMenu = new JMenu("Debug");
        debugMenu.addMenuListener(redrawListener);

        JCheckBoxMenuItem wireframeMenuItem = new JCheckBoxMenuItem("Wireframe");
        wireframeMenuItem.addActionListener(e -> {
            JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) e.getSource();
            if (checkbox.isSelected()) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> jmePanel.getViewPort().addProcessor(wireProcessor));
            }
            else {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> jmePanel.getViewPort().removeProcessor(wireProcessor));
            }
        });
        debugMenu.add(wireframeMenuItem);

        JCheckBoxMenuItem normalsMenuItem = new JCheckBoxMenuItem("View Normals");
        normalsMenuItem.addActionListener(e -> {
            JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) e.getSource();

            ServiceManager.getService(JmeEngineService.class).enqueue(() -> {
                ServiceManager.getService(JmeEngineService.class).getStateManager().getState(NormalViewerState.class).setEnabled(checkbox.isSelected());
            });
        });
        debugMenu.add(normalsMenuItem);

        JCheckBoxMenuItem gridMenuItem = new JCheckBoxMenuItem("Grid", true);
        gridMenuItem.addActionListener(e -> {
            JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) e.getSource();
            if (checkbox.isSelected()) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> jmePanel.getRootNode().attachChild(grid));
            }
            else {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> grid.removeFromParent());
            }
        });
        debugMenu.add(gridMenuItem);

        menuBar.add(debugMenu);
        return menuBar;
    }

    private void selectSkyBox(ActionEvent e) {

        JCheckBoxMenuItem selectedItem = (JCheckBoxMenuItem) e.getSource();
        SkyLoader skyLoader = SkyLoader.fromFriendlyName(selectedItem.getText());

        for (JCheckBoxMenuItem menuItem : skyBoxMenuItems) {
            if (!menuItem.equals(selectedItem)) {
                menuItem.setSelected(false);
            }
        }

        ServiceManager.getService(JmeEngineService.class).enqueue(() -> {

            boolean isInScene = sky.getParent() != null;

            if (isInScene) {
                sky.removeFromParent();
            }

            sky = skyLoader.load();

            if (isInScene) {
                jmePanel.getRootNode().attachChild(sky);
            }

        });

    }

    private final MenuListener redrawListener = new MenuListener() {
        @Override
        public void menuSelected(MenuEvent e) {
            outputPanel.revalidate();
            outputPanel.repaint();
        }

        @Override public void menuDeselected(MenuEvent e) { }
        @Override public void menuCanceled(MenuEvent e) { }
    };

    public Spatial getScene() {
        return scene;
    }

    /*
    private void findAnimControl(Spatial model) {

        SceneGraphVisitor visitor = spatial -> {
            AnimControl control = spatial.getControl(AnimControl.class);
            if (control != null) {
                animControl = control;
            }
        };

        model.depthFirstTraversal(visitor);
    }

     */

    private Geometry createGrid(AssetManager assetManager) {
        Geometry geometry = new Geometry("grid", new Grid(20, 20, 1.0f));
        Material gridMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        gridMaterial.getAdditionalRenderState().setWireframe(true);
        gridMaterial.setColor("Color", ColorRGBA.Gray);
        geometry.setMaterial(gridMaterial);
        geometry.setLocalTranslation(-10, 0, -10);

        return geometry;
    }


    public JmePanel getJmePanel() {
        return jmePanel;
    }

    public JPanel getJmeComponent() {
        return formPanel;
    }


    public void resizeCanvas() {
        jmePanel.setPreferredSize(jmePanel.getParent().getSize());
    }

    @Override
    public void dispose() {
        Disposer.dispose(this);
    }

    private class JmeComponentListener extends ComponentAdapter {

        @Override
        public void componentMoved(ComponentEvent e) {
            resizeCanvas();
        }

        @Override
        public void componentShown(ComponentEvent e) {
            resizeCanvas();
        }

        @Override
        public void componentResized(ComponentEvent e) {
            resizeCanvas();
        }
    }

}
