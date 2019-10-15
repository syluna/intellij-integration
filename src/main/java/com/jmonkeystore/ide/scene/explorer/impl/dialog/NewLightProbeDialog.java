package com.jmonkeystore.ide.scene.explorer.impl.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.treeStructure.Tree;
import com.jme3.light.LightProbe;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jmonkeystore.ide.scene.explorer.SceneTreeRenderer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.List;

public class NewLightProbeDialog extends DialogWrapper {

    private JPanel contentPanel;

    private JComboBox<LightProbe.AreaType> areaTypeComboBox;
    private JSpinner radiusSpinner;
    private JScrollPane treeScrollPane;

    private Spatial selectedSpatial;
    private LightProbe.AreaType selectedAreaType = LightProbe.AreaType.values()[0];


    public NewLightProbeDialog(Component parent, Spatial scene) {
        super(parent, true);
        setTitle("New LightProbe...");
        init();

        radiusSpinner.setValue(10);
        createTree(scene);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPanel;
    }

    public Spatial getSelectedSpatial() {
        return selectedSpatial;
    }

    public LightProbe.AreaType getSelectedAreaType() {
        return selectedAreaType;
    }

    public int getRadius() {
        return (int) radiusSpinner.getValue();
    }

    private void createTree(Spatial scene) {

        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(scene);
        DefaultTreeModel treeModel = new DefaultTreeModel(treeRoot);
        Tree tree = new Tree(treeModel);
        tree.setCellRenderer(new SceneTreeRenderer());
        traverseScene(treeRoot, scene);

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) ((Tree) e.getSource()).getLastSelectedPathComponent();
            selectedSpatial = (Spatial) treeNode.getUserObject();
        });

        areaTypeComboBox.setModel(new DefaultComboBoxModel<>(LightProbe.AreaType.values()));

        areaTypeComboBox.addActionListener(e -> {
            selectedAreaType = (LightProbe.AreaType) areaTypeComboBox.getSelectedItem();
        });

        treeScrollPane.setViewportView(tree);
    }

    private void traverseScene(DefaultMutableTreeNode treeNode, Spatial spatial) {

        if (spatial instanceof Node) {

            List<Spatial> children = ((Node) spatial).getChildren();
            for (Spatial child : children) {

                if (child instanceof Node) {
                    DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(child);

                    treeNode.add(childTreeNode);
                    traverseScene(childTreeNode, child);
                }
            }
        }

    }

}
