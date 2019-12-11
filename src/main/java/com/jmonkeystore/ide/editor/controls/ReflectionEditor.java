package com.jmonkeystore.ide.editor.controls;

import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jmonkeystore.ide.api.JmeObject;
import com.jmonkeystore.ide.editor.component.Component;
import com.jmonkeystore.ide.reflection.ComponentBuilder;
import com.jmonkeystore.ide.reflection.UniqueProperties;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

public class ReflectionEditor implements JmeObject {

    private JPanel contentPanel;

    public ReflectionEditor(Object object) {

        this.contentPanel = new JPanel(new VerticalLayout());

        UniqueProperties uniqueProperties = new UniqueProperties(object);
        ComponentBuilder componentBuilder = new ComponentBuilder(uniqueProperties);

        // @todo: register external components for get/set return types that are not registered.
        // componentBuilder.registerComponent(MyReturnType.class, MyComponent.class);

        componentBuilder.build();

        for (Component component : componentBuilder.getComponents()) {
            contentPanel.add(component.getJComponent());
        }

        // if this is a Spatial it will have a getControl and getLightList
        if (object instanceof Spatial) {

            Spatial spatial = (Spatial) object;

            int controlsCount = spatial.getNumControls();

            if (controlsCount > 0) {

                for (int i = 0; i < controlsCount; i++) {

                    Control control = spatial.getControl(i);

                    UniqueProperties controlUniqueProperties = new UniqueProperties(control);
                    ComponentBuilder controlComponentBuilder = new ComponentBuilder(controlUniqueProperties);
                    controlComponentBuilder.build();

                    for (Component component : controlComponentBuilder.getComponents()) {
                        contentPanel.add(component.getJComponent());
                    }
                }

            }

        }
    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void cleanup() {

    }

}
