package com.jmonkeystore.ide.viewer.ui;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.impl.JmePanel;
import com.jmonkeystore.ide.viewer.impl.JmeMaterialViewerImpl;

import javax.swing.*;


public class JmeMaterialViewerUI {

    private JmePanel jmePanel;
    private JPanel outputPanel;

    public JmeMaterialViewerUI(JmeMaterialViewerImpl materialViewer) {

        JmeEngineService engineService = ServiceManager.getService(JmeEngineService.class);

        jmePanel = engineService.getOrCreatePanel(materialViewer.getFile().getUrl());
        outputPanel.add(jmePanel);

        engineService.enqueue(() -> {

            com.jme3.scene.shape.Box box = new com.jme3.scene.shape.Box(1, 1, 1);
            Geometry geom = new Geometry("box", box);

            DirectionalLight directionalLight = new DirectionalLight(new Vector3f(-1, -1, -1).normalizeLocal());
            jmePanel.getRootNode().addLight(directionalLight);

            AmbientLight ambientLight = new AmbientLight(ColorRGBA.White.mult(0.4f));
            jmePanel.getRootNode().addLight(ambientLight);

            Material mat = engineService.getExternalAssetLoader().load(materialViewer.getFile().getUrl(), Material.class);

            if (mat != null) {
                geom.setMaterial(mat);
                jmePanel.getRootNode().attachChild(geom);
            }

        });

    }

    public JmePanel getJmePanel() {
        return jmePanel;
    }

    public JPanel getJmeComponent() {
        return outputPanel;
    }

}
