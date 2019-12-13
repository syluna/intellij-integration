package com.jmonkeystore.ide.scene.explorer.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.ui.treeStructure.Tree;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.impl.JmePanel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SceneTreeClickListener extends MouseAdapter {

    @Override
    public void mousePressed(MouseEvent e) {

        if (e.getButton() == 1 && e.getClickCount() == 2) {

            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) ((Tree) e.getSource()).getLastSelectedPathComponent();

            if (treeNode != null) {

                if (treeNode.getUserObject() instanceof Spatial) {
                    Spatial spatial = (Spatial) treeNode.getUserObject();

                    JmeEngineService jmeEngineService = ServiceManager.getService(JmeEngineService.class);

                    JmePanel activePanel = jmeEngineService.getActivePanel();

                    if (activePanel != null) {
                        activePanel.getCamera().lookAt(spatial.getWorldTranslation(), Vector3f.UNIT_Y);
                    }

                }

            }

        }

    }

}
