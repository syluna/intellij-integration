package com.jmonkeystore.ide.jme.impl;

import com.jme3.renderer.RenderManager;
import com.jme3.system.awt.AwtPanel;
import com.jme3.system.awt.AwtPanelsContext;

public class SwingCanvasContext extends AwtPanelsContext {

    private JmePanel activePanel;

    public void addPanel(JmePanel panel) {
        panels.add(panel);
    }

    public JmePanel getActivePanel() {
        return activePanel;
    }

    public void setActivePanel(JmePanel activePanel) {
        this.activePanel = activePanel;
    }

    public void removePanel(JmePanel panel) {
        panel.cleanup();
        panels.remove(panel);
    }

    public JmePanel getPanel(String name) {
        for (AwtPanel panel : panels) {

            JmePanel jmePanel = (JmePanel) panel;

            if (jmePanel.getName().equals(name)) {
                return jmePanel;
            }
        }

        return null;
    }

    public boolean containsPanel(JmePanel panel) {
        return panels.contains(panel);
    }

    public JmePanel getInputSource() {
        return (JmePanel) inputSource;
    }

    public int getPanelCount() {
        return panels.size();
    }

    void update(float tpf, RenderManager renderManager) {

        for (AwtPanel panel : panels) {

            JmePanel jmePanel = (JmePanel) panel;

            jmePanel.preFrame(tpf);
            renderManager.renderViewPort(jmePanel.getViewPort(), tpf);

            if (jmePanel.getParent() != null) {
                if (!jmePanel.getSize().equals(jmePanel.getParent().getSize())) {
                    jmePanel.setSize(jmePanel.getParent().getSize());
                }
            }

        }
    }

}
