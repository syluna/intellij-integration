package com.jmonkeystore.ide.editor.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.uiDesigner.core.GridConstraints;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingSphere;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Grid;
import com.jmonkeystore.ide.editor.impl.JmeModelEditorImpl;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.impl.JmePanel;
import com.jmonkeystore.ide.jme.scene.WireProcessor;
import com.jmonkeystore.ide.jme.sky.SkyLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class JmeModelEditorUI implements Disposable {

    private JmePanel jmePanel;
    private JPanel formPanel;
    private JCheckBox directionalLightCheckBox;
    private JCheckBox ambientLightCheckBox;
    private JPanel outputPanel;
    private JCheckBox wireframeCheckBox;
    private JCheckBox showSkyCheckBox;
    private JCheckBox showGridCheckBox;
    private JLabel vertsLabel;
    private JLabel trisLabel;
    private JCheckBox lightProbeCheckBox;
    private JComboBox<String> skyComboBox;
    private JComboBox<String> animsComboBox;
    private JButton playAnimButton;
    private JButton stopAnimButton;
    private JSlider animTimeSlider;
    private JSlider animSpeedSlider;

    private final DirectionalLight directionalLight;
    private final AmbientLight ambientLight;
    private final LightProbe lightProbe;

    private final SceneProcessor wireProcessor;
    private Spatial sky;
    private final Geometry grid;

    // animation
    private AnimControl animControl;
    private AnimChannel animChannel;
    private final DefaultBoundedRangeModel animTimelineModel = new DefaultBoundedRangeModel(0, 1, 0, 200);
    private float animSpeed = 1.0f;

    public JmeModelEditorUI(JmeModelEditorImpl modelEditor) {
        super();

        JmeEngineService engineService = ServiceManager.getService(JmeEngineService.class);
        jmePanel = engineService.getOrCreatePanel(modelEditor.getFile().getUrl());

        outputPanel.addComponentListener(new JmeComponentListener());
        outputPanel.add(jmePanel, new GridConstraints());

        // create tools
        wireProcessor = new WireProcessor(engineService.getAssetManager());

        directionalLight = new DirectionalLight(new Vector3f(-1, -1, -1).normalizeLocal(), ColorRGBA.White.mult(0.7f));
        ambientLight = new AmbientLight(ColorRGBA.White.mult(0.5f));

        grid = createGrid(engineService.getAssetManager());

        skyComboBox.setModel(new DefaultComboBoxModel<>(SkyLoader.getFriendlyNames()));

        Spatial probeModel = engineService.getAssetManager().loadModel("Scenes/defaultProbe.j3o");
        lightProbe = (LightProbe) probeModel.getLocalLightList().get(0);
        lightProbe.setBounds(new BoundingSphere(500, new Vector3f(0, 0, 0)));

        animTimeSlider.setModel(animTimelineModel);

        engineService.enqueue(() -> {
            jmePanel.getRootNode().addLight(directionalLight);
            jmePanel.getRootNode().addLight(ambientLight);

            // jmePanel.getRootNode().attachChild(sky);
            jmePanel.getRootNode().attachChild(grid);

            Spatial model = engineService.loadExternalModel(modelEditor.getFile().getUrl());
            jmePanel.getRootNode().attachChild(model);

            findAnimControl(model);

            // fill animations
            if (animControl != null && !animControl.getAnimationNames().isEmpty()) {
                String[] animNames = animControl.getAnimationNames().toArray(new String[0]);
                EventQueue.invokeLater(() -> animsComboBox.setModel(new DefaultComboBoxModel<>(animNames)) );

                animChannel = animControl.createChannel();
                animChannel.setAnim(animNames[0]);
                animChannel.setSpeed(0);

                int max = (int) (animChannel.getAnimMaxTime() * 1000);
                animTimelineModel.setMaximum(max);
            }
            else {
                playAnimButton.setEnabled(false);
                stopAnimButton.setEnabled(false);
                animsComboBox.setEnabled(false);
            }

            jmePanel.getCamera().setLocation(new Vector3f(0, 5, 15));
            jmePanel.getCamera().lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);

            EventQueue.invokeLater(() -> {
                vertsLabel.setText(String.format("Verts: %,d", model.getVertexCount()));
                trisLabel.setText(String.format("Tris: %,d", model.getTriangleCount()));
            });

        });


        // editor events

        // light
        directionalLightCheckBox.addItemListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            engineService.enqueue(() -> directionalLight.setEnabled(checkbox.isSelected()));
        });

        ambientLightCheckBox.addItemListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            engineService.enqueue(() -> ambientLight.setEnabled(checkbox.isSelected()));
        });

        lightProbeCheckBox.addItemListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            if (checkbox.isSelected()) {
                engineService.enqueue(() -> jmePanel.getRootNode().addLight(lightProbe));
            }
            else {
                engineService.enqueue(() -> jmePanel.getRootNode().removeLight(lightProbe));
            }
        });

        // sky
        skyComboBox.addItemListener(e -> {
            JComboBox comboBox = (JComboBox) e.getSource();
            int index = comboBox.getSelectedIndex();

            engineService.enqueue(() -> {

                if (sky != null) {
                    sky.removeFromParent();
                }

                sky = SkyLoader.values()[index].load();
                jmePanel.getRootNode().attachChild(sky);
            });
        });

        skyComboBox.setSelectedIndex(SkyLoader.LAGOON.ordinal());

        // debug
        wireframeCheckBox.addItemListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            if (checkbox.isSelected()) {
                engineService.enqueue(() -> jmePanel.getViewPort().addProcessor(wireProcessor));
            }
            else {
                engineService.enqueue(() -> jmePanel.getViewPort().removeProcessor(wireProcessor));
            }
        });

        showSkyCheckBox.addItemListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            if (checkbox.isSelected()) {
                engineService.enqueue(() -> jmePanel.getRootNode().attachChild(sky));
            }
            else {
                engineService.enqueue(() -> sky.removeFromParent());
            }
        });

        showGridCheckBox.addItemListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            if (checkbox.isSelected()) {
                engineService.enqueue(() -> jmePanel.getRootNode().attachChild(grid));
            }
            else {
                engineService.enqueue(() -> grid.removeFromParent());
            }
        });

        // animations
        animsComboBox.addItemListener(e -> {
            if (animControl != null) {
                String animName = (String) animsComboBox.getSelectedItem();
                if (animName != null) {
                    animChannel.setAnim(animName);

                    int max = (int) (animChannel.getAnimMaxTime() * 1000);
                    animTimeSlider.getModel().setMaximum(max);
                }
            }
        });

        playAnimButton.addActionListener(e -> {
            if (animChannel != null) {
                animChannel.setSpeed(animSpeed);
                animChannel.setLoopMode(LoopMode.Loop);
            }
        });

        stopAnimButton.addActionListener(e -> {
            if (animChannel != null) {
                animChannel.setSpeed(0);
            }
        });

        animTimeSlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            float val = slider.getValue() / 1000f;
            animChannel.setSpeed(0);
            animChannel.setTime(val);
        });

        animSpeedSlider.setModel(new DefaultBoundedRangeModel(0, 1, 0, 200));
        animSpeedSlider.setValue((int) (animSpeed * 100));
        animSpeedSlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            animSpeed = slider.getValue() / 100f;
            animChannel.setSpeed(animSpeed);
        });

    }

    private void findAnimControl(Spatial model) {

        SceneGraphVisitor visitor = spatial -> {
            AnimControl control = spatial.getControl(AnimControl.class);
            if (control != null) {
                animControl = control;
            }
        };

        model.depthFirstTraversal(visitor);
    }

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
