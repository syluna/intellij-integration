package com.jmonkeystore.ide.scene.explorer.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.ui.treeStructure.Tree;
import com.jme3.light.*;
import com.jme3.scene.Spatial;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class SceneTreeContextMenuListener implements MouseListener {

    private final Tree tree;

    private JPopupMenu spatialPopupMenu;
    private JPopupMenu lightPopupMenu;

    private DefaultMutableTreeNode clickedNode;

    public SceneTreeContextMenuListener(Tree tree) {
        this.tree = tree;
        this.spatialPopupMenu = createSpatialPopupMenu();
        this.lightPopupMenu = createLightPopupMenu();
    }

    private JPopupMenu createLightPopupMenu() {
        JPopupMenu menu = new JPopupMenu("Light Menu");

        JMenuItem removeItem = new JMenuItem("Remove");
        removeItem.addActionListener(e -> {
            Light light = (Light) clickedNode.getUserObject();

            // call getParent twice because the first parent is just a placeholder for all lights
            DefaultMutableTreeNode parentTree = (DefaultMutableTreeNode) clickedNode.getParent().getParent();
            Spatial parent = (Spatial) parentTree.getUserObject();
            parent.removeLight(light);

            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        menu.add(removeItem);

        return menu;
    }

    private JPopupMenu createSpatialPopupMenu() {
        JPopupMenu menu = new JPopupMenu("Spatial Menu");
        //JMenuItem addLightMenu = new JMenuItem("Add Light");

        JMenuItem ambLightMenuItem = new JMenuItem("Ambient Light");
        ambLightMenuItem.addActionListener(e -> {
            Spatial spatial = (Spatial) clickedNode.getUserObject();
            spatial.addLight(new AmbientLight());
            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        menu.add(ambLightMenuItem);

        JMenuItem dirLightMenuItem = new JMenuItem("Directional Light");
        dirLightMenuItem.addActionListener(e -> {
            Spatial spatial = (Spatial) clickedNode.getUserObject();
            spatial.addLight(new DirectionalLight());
            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        menu.add(dirLightMenuItem);

        // pointlight
        JMenuItem pointLightMenuItem = new JMenuItem("Point Light");
        pointLightMenuItem.addActionListener(e -> {
            Spatial spatial = (Spatial) clickedNode.getUserObject();
            spatial.addLight(new PointLight());
            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        menu.add(pointLightMenuItem);

        // spotlight
        JMenuItem spotLightMenuItem = new JMenuItem("Spot Light");
        spotLightMenuItem.addActionListener(e -> {
            Spatial spatial = (Spatial) clickedNode.getUserObject();
            spatial.addLight(new SpotLight());
            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        menu.add(spotLightMenuItem);

        menu.add(new JSeparator());

        //menu.add(addLightMenu);

        return menu;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {

            JTree tree = (JTree) e.getComponent();
            clickedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (clickedNode.getUserObject() instanceof Spatial) {
                spatialPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
            else if (clickedNode.getUserObject() instanceof Light) {
                lightPopupMenu.show(e.getComponent(), e.getX(), e.getY());
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
