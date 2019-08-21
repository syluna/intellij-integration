package com.jmonkeystore.ide.scene.explorer.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.jme3.animation.AnimControl;
import com.jme3.light.Light;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jmonkeystore.ide.editor.impl.JmeModelFileEditorImpl;
import com.jmonkeystore.ide.editor.objects.AnimControlEditor;
import com.jmonkeystore.ide.editor.objects.GeometryEditor;
import com.jmonkeystore.ide.editor.objects.NodeEditor;
import com.jmonkeystore.ide.editor.ui.JmeModelEditorUI;
import com.jmonkeystore.ide.scene.editor.PropertyEditorService;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;
import com.jmonkeystore.ide.scene.explorer.SceneTreeRenderer;
import com.jmonkeystore.ide.util.ProjectUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.List;

public class SceneExplorerServiceImpl implements SceneExplorerService {

    private JBScrollPane windowContent;

    private final Tree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode treeRoot;

    private final PropertyEditorService propertyEditorService;

    public SceneExplorerServiceImpl() {

        treeRoot = new DefaultMutableTreeNode(new Node("Scene"));
        treeModel = new DefaultTreeModel(treeRoot);

        tree = new Tree(treeModel);
        tree.setCellRenderer(new SceneTreeRenderer());

        this.propertyEditorService = ServiceManager.getService(PropertyEditorService.class);



        tree.addTreeSelectionListener(e -> {

            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) ((Tree) e.getSource()).getLastSelectedPathComponent();

            Project project = ProjectUtils.getActiveProject();
            JmeModelEditorUI modelEditor = null;

            if (project != null) {
                FileEditor editor = FileEditorManager.getInstance(project).getSelectedEditor();

                if (editor instanceof JmeModelFileEditorImpl) {
                    JmeModelFileEditorImpl impl = (JmeModelFileEditorImpl) editor;
                    modelEditor = impl.getModelEditor().getEditor();
                }
            }

            if (treeNode != null) {
                Object item = treeNode.getUserObject();

                if (item instanceof Node) {
                    Node node = (Node) item;
                    propertyEditorService.setWindowContent(new NodeEditor(node));

                    if (modelEditor != null) {
                        modelEditor.highlightWithBoundingBox(node);
                    }
                }
                else if (item instanceof Geometry) {
                    Geometry geometry = (Geometry) item;
                    propertyEditorService.setWindowContent(new GeometryEditor(geometry));

                    if (modelEditor != null) {
                        modelEditor.highlightMesh(geometry);
                    }
                }
                else if (item instanceof AnimControl) {
                    AnimControl animControl = (AnimControl) item;
                    propertyEditorService.setWindowContent(new AnimControlEditor(animControl));

                    if (modelEditor != null) {
                        modelEditor.clearAllHighlights();
                    }
                }
                else {
                    propertyEditorService.clearWindowContent();

                    if (modelEditor != null) {
                        modelEditor.clearAllHighlights();
                    }
                }
            }
            else {
                propertyEditorService.clearWindowContent();

                if (modelEditor != null) {
                    modelEditor.clearAllHighlights();
                }
            }
        });

        windowContent = new JBScrollPane(tree);
    }

    @Override
    public JComponent getWindowContent() {
        return windowContent;
    }

    private boolean sceneActive = false;

    @Override
    public void setScene(Spatial scene) {
        treeRoot.removeAllChildren();
        treeModel.reload();

        tree.setVisible(scene != null);

        if (scene == null) {
            sceneActive = false;
        }
        else {
            treeRoot.setUserObject(scene);
            findLights(treeRoot, scene);
            findControls(treeRoot, scene);

            traverseScene(treeRoot, scene);

            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);
            }
        }
    }


    private void findLights(DefaultMutableTreeNode treeNode, Spatial spatial) {
        for (Light light : spatial.getLocalLightList()) {
            treeNode.add(new DefaultMutableTreeNode(light));
        }
    }

    private void findControls(DefaultMutableTreeNode treeNode, Spatial spatial) {
        for (int i = 0; i < spatial.getNumControls(); i++) {
            treeNode.add(new DefaultMutableTreeNode(spatial.getControl(i)));
        }
    }

    private void traverseScene(DefaultMutableTreeNode treeNode, Spatial spatial) {

        if (spatial instanceof Node) {

            List<Spatial> children = ((Node) spatial).getChildren();
            for (Spatial child : children) {
                DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(child);

                findLights(childTreeNode, child);
                findControls(childTreeNode, child);

                treeNode.add(childTreeNode);
                traverseScene(childTreeNode, child);
            }
        }
        else if (spatial instanceof Geometry) {
            Geometry geometry = (Geometry) spatial;
            DefaultMutableTreeNode geomNode = new DefaultMutableTreeNode(geometry.getMesh());
            treeNode.add(geomNode);
        }
    }

}
