package com.jmonkeystore.ide.editor.objects;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jmonkeystore.ide.editor.controls.quaternion.QuaternionControl;
import com.jmonkeystore.ide.editor.controls.vector.Vector3fControl;
import com.jmonkeystore.ide.jme.JmeEngineService;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

@Deprecated
public class NodeEditor implements JmeObject {

    private JPanel content;

    private final Node node;

    public NodeEditor(Node node) {
        this.node = node;

        this.content = new JPanel(new VerticalLayout());

        /*
        StringControl nameControl = new StringControl("Name", node.getName()) {
            @Override
            public String getValue() {
                return node.getName();
            }

            @Override
            public void setValue(String value) {
                node.setName(value);
            }
        };
        content.add(nameControl.getJComponent());
         */

        Vector3fControl translationControl = new Vector3fControl("Local Translation", node.getLocalTranslation()) {
            @Override
            public void setValue(Vector3f value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> node.setLocalTranslation(value));
            }
        };
        content.add(translationControl.getJComponent());

        QuaternionControl rotationControl = new QuaternionControl("Local Rotation", node.getLocalRotation()) {
            @Override
            public void setValue(Quaternion value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> node.setLocalRotation(value));
            }
        };
        content.add(rotationControl.getJComponent());

        Vector3fControl scaleControl = new Vector3fControl("Local Scale", node.getLocalScale()) {
            @Override
            public void setValue(Vector3f value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> node.setLocalScale(value));
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
