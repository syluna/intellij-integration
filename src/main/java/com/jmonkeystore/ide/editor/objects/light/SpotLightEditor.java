package com.jmonkeystore.ide.editor.objects.light;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jmonkeystore.ide.editor.controls.color.ColorControl;
import com.jmonkeystore.ide.editor.controls.numfloat.FloatControl;
import com.jmonkeystore.ide.editor.controls.vector.Vector3fControl;
import com.jmonkeystore.ide.editor.objects.JmeObject;
import com.jmonkeystore.ide.jme.JmeEngineService;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

public class SpotLightEditor implements JmeObject {

    private final SpotLight spotLight;
    private final JPanel contentPanel;

    public SpotLightEditor(SpotLight spotLight) {
        this.spotLight = spotLight;

        this.contentPanel = new JPanel(new VerticalLayout());

        // color
        ColorControl colorControl = new ColorControl("Light Color", spotLight.getColor()) {
            @Override
            public void setValue(ColorRGBA colorRGBA) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> spotLight.setColor(colorRGBA) );
            }
        };
        contentPanel.add(colorControl.getJComponent());

        // direction
        Vector3fControl directionControl = new Vector3fControl("Direction", spotLight.getDirection()) {
            @Override
            public void setValue(Vector3f value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> spotLight.setDirection(value) );
            }
        };
        contentPanel.add(directionControl.getJComponent());

        // position
        Vector3fControl positionControl = new Vector3fControl("Position", spotLight.getPosition()) {
            @Override
            public void setValue(Vector3f value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> spotLight.setPosition(value));
            }
        };
        this.contentPanel.add(positionControl.getJComponent());

        // angle: inner
        FloatControl innerAngleControl = new FloatControl("Angle: Inner", spotLight.getSpotInnerAngle()) {
            @Override
            public float getValue() {
                return spotLight.getSpotInnerAngle();
            }

            @Override
            public void setValue(float value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> spotLight.setSpotInnerAngle(value) );
            }
        };
        this.contentPanel.add(innerAngleControl.getJComponent());

        // angle: outer
        FloatControl outerAngleControl = new FloatControl("Angle: Outer", spotLight.getSpotOuterAngle()) {
            @Override
            public float getValue() {
                return spotLight.getSpotOuterAngle();
            }

            @Override
            public void setValue(float value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> spotLight.setSpotOuterAngle(value) );
            }
        };
        this.contentPanel.add(outerAngleControl.getJComponent());

        // range
        FloatControl rangeControl = new FloatControl("Range", spotLight.getSpotRange()) {
            @Override
            public float getValue() {
                return spotLight.getSpotRange();
            }

            @Override
            public void setValue(float value) {
                ServiceManager.getService(JmeEngineService.class).enqueue(() -> spotLight.setSpotRange(value) );
            }
        };
        this.contentPanel.add(rangeControl.getJComponent());
    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void cleanup() {

    }
}
