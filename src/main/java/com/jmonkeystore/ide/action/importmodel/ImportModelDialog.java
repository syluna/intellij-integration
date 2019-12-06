package com.jmonkeystore.ide.action.importmodel;

import com.google.common.io.Files;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.jme3.bounding.BoundingSphere;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jmonkeystore.ide.action.importmodel.importer.gltf.GltfExtrasLoader;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.impl.JmePanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class ImportModelDialog extends DialogWrapper {

    private JTextField modelPathTextField;
    private JButton browseButton;
    private JPanel jPanel;
    private JPanel canvasPanel;

    private JmePanel jmePanel;
    private File selectedFile;

    public ImportModelDialog() {
        super(true);
        init();
        setOKButtonText("Import Model");
        setTitle("Import Model");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        modelPathTextField.setText("");

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            FileNameExtensionFilter filter = new FileNameExtensionFilter("Models",
                    "mesh.xml", "blend", "gltf", "glb"
            );
            fileChooser.setFileFilter(filter);

            fileChooser.setMultiSelectionEnabled(false);
            int result = fileChooser.showOpenDialog(jPanel);

            if (result == JFileChooser.APPROVE_OPTION) {
                // System.out.println("Chosen folder: " + fileChooser.getSelectedFile());
                selectedFile = fileChooser.getSelectedFile();
                modelPathTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());

                JmeEngineService engineService = ServiceManager.getService(JmeEngineService.class);

                jmePanel = engineService.getOrCreatePanel("Import Model Preview Panel");
                canvasPanel.add(jmePanel, new GridConstraints());

                engineService.enqueue(() -> {

                    while (jmePanel.getRootNode().getLocalLightList().size() > 0) {
                        jmePanel.getRootNode().removeLight(jmePanel.getRootNode().getLocalLightList().get(0));
                    }

                    jmePanel.getRootNode().detachAllChildren();
                    jmePanel.getViewPort().setBackgroundColor(ColorRGBA.DarkGray);

                    Spatial model;

                    jmePanel.getRootNode().addLight(new DirectionalLight(new Vector3f(-1, -1, -1).normalizeLocal(), ColorRGBA.White.mult(0.7f)));

                    if ("gltf".equalsIgnoreCase(Files.getFileExtension(fileChooser.getSelectedFile().getAbsolutePath())) ||
                        "glb".equalsIgnoreCase(Files.getFileExtension(fileChooser.getSelectedFile().getAbsolutePath()))) {

                        model = engineService.getExternalAssetLoader().load(GltfExtrasLoader.createModelKey(fileChooser.getSelectedFile().getAbsolutePath()), Spatial.class);

                        // add a light probe to GLTF models.
                        Spatial probeModel = engineService.getAssetManager().loadModel("Scenes/defaultProbe.j3o");
                        LightProbe lightProbe = (LightProbe) probeModel.getLocalLightList().get(0);
                        lightProbe.setBounds(new BoundingSphere(500, new Vector3f(0, 0, 0)));
                        jmePanel.getRootNode().addLight(lightProbe);

                    }
                    else {

                        model = engineService.getExternalAssetLoader().load(fileChooser.getSelectedFile().getAbsolutePath(), Spatial.class);

                        // add an ambient light to non-GLTF models.
                        jmePanel.getRootNode().addLight(new AmbientLight(ColorRGBA.White.mult(0.4f)));
                    }

                    if (model != null) {
                        jmePanel.getRootNode().attachChild(model);
                    }

                });

                // importButton.setEnabled(true);
            }

        });

        return jPanel;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void cleanupPanel() {

        if (jmePanel != null && jmePanel.getParent() != null) {
            jmePanel.getParent().remove(jmePanel);
            ServiceManager.getService(JmeEngineService.class).removePanel(jmePanel);
        }
    }

}
