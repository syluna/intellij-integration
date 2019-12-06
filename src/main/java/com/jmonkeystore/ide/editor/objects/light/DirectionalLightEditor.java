package com.jmonkeystore.ide.editor.objects.light;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jmonkeystore.ide.editor.controls.color.ColorControl;
import com.jmonkeystore.ide.editor.controls.vector.Vector3fControl;
import com.jmonkeystore.ide.editor.objects.JmeObject;
import com.jmonkeystore.ide.jme.JmeEngineService;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

@Deprecated
public class DirectionalLightEditor implements JmeObject {

    private final DirectionalLight directionalLight;
    private final JPanel contentPanel;

    public DirectionalLightEditor(DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;

        this.contentPanel = new JPanel(new VerticalLayout());

        // color
        ColorControl colorControl = new ColorControl("Light Color", directionalLight.getColor()) {
            @Override
            public void setValue(ColorRGBA colorRGBA) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> directionalLight.setColor(colorRGBA));
            }
        };

        // direction
        Vector3fControl directionControl = new Vector3fControl("Direction", directionalLight.getDirection()) {
            @Override
            public void setValue(Vector3f value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> directionalLight.setDirection(value) );
            }
        };

        contentPanel.add(colorControl.getJComponent());
        contentPanel.add(directionControl.getJComponent());

    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void cleanup() {

    }

}
