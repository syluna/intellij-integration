package com.jmonkeystore.ide.editor.controls;

import com.jmonkeystore.ide.editor.component.Component;
import com.jmonkeystore.ide.editor.objects.JmeObject;
import com.jmonkeystore.ide.reflection.ReflectUtils;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

public class ReflectionEditor implements JmeObject {

    private JPanel contentPanel;

    private Object object;

    public ReflectionEditor(Object object) {
        this.object = object;

        this.contentPanel = new JPanel(new VerticalLayout());

        ReflectUtils.UniqueProperties uniqueProperties = new ReflectUtils.UniqueProperties(object);
        ReflectUtils.ComponentBuilder componentBuilder = new ReflectUtils.ComponentBuilder(uniqueProperties);

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
