package com.jmonkeystore.ide.editor.controls;

import com.jmonkeystore.ide.editor.component.Component;
import com.jmonkeystore.ide.editor.objects.JmeObject;
import com.jmonkeystore.ide.reflection.ComponentBuilder;
import com.jmonkeystore.ide.reflection.UniqueProperties;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

public class ReflectionEditor implements JmeObject {

    private JPanel contentPanel;

    private Object object;

    public ReflectionEditor(Object object) {
        this.object = object;

        this.contentPanel = new JPanel(new VerticalLayout());

        // ReflectUtils.UniqueProperties uniqueProperties = new ReflectUtils.UniqueProperties(object);
        // ReflectUtils.ComponentBuilder componentBuilder = new ReflectUtils.ComponentBuilder(uniqueProperties);

        UniqueProperties uniqueProperties = new UniqueProperties(object);
        ComponentBuilder componentBuilder = new ComponentBuilder(uniqueProperties);

        // @todo: register external components for get/set return types that are not registered.
        // componentBuilder.registerComponent(MyReturnType.class, MyComponent.class);

        componentBuilder.build();

        for (Component component : componentBuilder.getComponents()) {
            contentPanel.add(component.getJComponent());
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
