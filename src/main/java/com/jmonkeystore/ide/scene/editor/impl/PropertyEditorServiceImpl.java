package com.jmonkeystore.ide.scene.editor.impl;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.jmonkeystore.ide.api.JmeObject;
import com.jmonkeystore.ide.scene.editor.PropertyEditorService;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

public class PropertyEditorServiceImpl implements PropertyEditorService {

    private JBScrollPane windowContent;
    private JPanel content;

    private JmeObject jmeObject;

    public PropertyEditorServiceImpl() {
        this.content = new JPanel(new VerticalLayout());
        this.content.setBorder(JBUI.Borders.empty(5));
        this.windowContent = new JBScrollPane(content);
    }

    public JComponent getWindowContent() {
        return windowContent;
    }

    @Override
    public void clearWindowContent() {
        if (jmeObject != null) {
            jmeObject.cleanup();
            jmeObject = null;
        }

        content.removeAll();
        windowContent.repaint();
        windowContent.revalidate();
    }

    @Override
    public void setWindowContent(JmeObject newObject) {

        if (jmeObject != null) {
            jmeObject.cleanup();
        }

        jmeObject = newObject;

        content.removeAll();
        content.add(jmeObject.getJComponent());

        windowContent.repaint();
        windowContent.revalidate();
    }

}
