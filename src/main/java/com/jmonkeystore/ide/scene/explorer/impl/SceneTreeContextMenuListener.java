package com.jmonkeystore.ide.scene.explorer.impl;

import com.jme3.scene.Spatial;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class SceneTreeContextMenuListener implements MouseListener {

    private JPopupMenu spatialPopupMenu;

    public SceneTreeContextMenuListener() {
        this.spatialPopupMenu = createSpatialPopupMenu();
    }

    private JPopupMenu createSpatialPopupMenu() {
        JPopupMenu menu = new JPopupMenu("Spatial Menu");
        JMenuItem addLightMenu = new JMenuItem("Add Light");
        menu.add(addLightMenu);

        return menu;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {

            JTree tree = (JTree) e.getComponent();
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (treeNode.getUserObject() instanceof Spatial) {
                spatialPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }

        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
