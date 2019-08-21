package com.jmonkeystore.ide.editor.objects;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jmonkeystore.ide.editor.controls.quaternion.QuaternionControl;
import com.jmonkeystore.ide.editor.controls.vector.Vector3fControl;
import com.jmonkeystore.ide.jme.JmeEngineService;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

public class GeometryEditor implements JmeObject {

    private final Geometry geometry;
    private final JPanel content;

    public GeometryEditor(Geometry geometry) {
        this.geometry = geometry;
        this.content = new JPanel(new VerticalLayout());

        Vector3fControl translationControl = new Vector3fControl("Local Translation", geometry.getLocalTranslation()) {
            @Override
            public void setValue(Vector3f value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> geometry.setLocalTranslation(value));
            }
        };
        content.add(translationControl.getJComponent());

        QuaternionControl rotationControl = new QuaternionControl("Local Rotation", geometry.getLocalRotation()) {
            @Override
            public void setValue(Quaternion value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> geometry.setLocalRotation(value));
            }
        };
        content.add(rotationControl.getJComponent());

        Vector3fControl scaleControl = new Vector3fControl("Local Scale", geometry.getLocalScale()) {
            @Override
            public void setValue(Vector3f value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> geometry.setLocalScale(value));
            }
        };
        content.add(scaleControl.getJComponent());

    }

    @Override
    public JComponent getJComponent() {
        return content;
    }

    @Override
    public void cleanup() {

    }

}
