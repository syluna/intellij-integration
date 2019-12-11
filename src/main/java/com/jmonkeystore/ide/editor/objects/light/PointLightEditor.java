package com.jmonkeystore.ide.editor.objects.light;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jmonkeystore.ide.api.JmeObject;
import com.jmonkeystore.ide.editor.controls.color.ColorControl;
import com.jmonkeystore.ide.editor.controls.numfloat.FloatControl;
import com.jmonkeystore.ide.editor.controls.vector.Vector3fControl;
import com.jmonkeystore.ide.jme.JmeEngineService;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

@Deprecated
public class PointLightEditor implements JmeObject {

    private final PointLight pointLight;
    private final JPanel contentPanel;

    public PointLightEditor(PointLight pointLight) {
        this.pointLight = pointLight;

        this.contentPanel = new JPanel(new VerticalLayout());

        // color
        ColorControl colorControl = new ColorControl("Light Color", pointLight.getColor()) {
            @Override
            public void setValue(ColorRGBA colorRGBA) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> pointLight.setColor(colorRGBA));
            }
        };
        this.contentPanel.add(colorControl.getJComponent());

        // position
        Vector3fControl positionControl = new Vector3fControl("Position", pointLight.getPosition()) {
            @Override
            public void setValue(Vector3f value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> pointLight.setPosition(value));
            }
        };
        this.contentPanel.add(positionControl.getJComponent());

        // radius
        FloatControl radiusControl = new FloatControl("Radius", pointLight.getRadius()) {
            @Override
            public float getValue() {
                return pointLight.getRadius();
            }

            @Override
            public void setValue(float value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> pointLight.setRadius(value) );
            }
        };

        this.contentPanel.add(radiusControl.getJComponent());
    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void cleanup() {

    }
}
