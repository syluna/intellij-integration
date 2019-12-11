package com.jmonkeystore.ide.editor.objects.light;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jmonkeystore.ide.api.JmeObject;
import com.jmonkeystore.ide.editor.controls.color.ColorControl;
import com.jmonkeystore.ide.jme.JmeEngineService;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

@Deprecated
public class AmbientLightEditor implements JmeObject {

    private final AmbientLight ambientLight;
    private final JPanel contentPanel;

    public AmbientLightEditor(AmbientLight ambientLight) {
        this.ambientLight = ambientLight;

        this.contentPanel = new JPanel(new VerticalLayout());

        // color
        ColorControl colorControl = new ColorControl("Light Color", ambientLight.getColor()) {
            @Override
            public void setValue(ColorRGBA colorRGBA) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> ambientLight.setColor(colorRGBA));
            }
        };

        this.contentPanel.add(colorControl.getJComponent());
    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void cleanup() {

    }

}
