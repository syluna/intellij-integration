package com.jmonkeystore.ide.scene.explorer.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.jme3.animation.AnimControl;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.Light;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jmonkeystore.ide.editor.impl.JmeModelFileEditorImpl;
import com.jmonkeystore.ide.editor.objects.AnimControlEditor;
import com.jmonkeystore.ide.editor.objects.GeometryEditor;
import com.jmonkeystore.ide.editor.objects.NodeEditor;
import com.jmonkeystore.ide.editor.ui.JmeModelEditorUI;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.scene.NormalViewerState;
import com.jmonkeystore.ide.scene.editor.PropertyEditorService;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;
import com.jmonkeystore.ide.scene.explorer.SceneTreeRenderer;
import com.jmonkeystore.ide.util.ProjectUtils;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SceneExplorerServiceImpl implements SceneExplorerService {

    private JBScrollPane windowContent;

    // tree
    private final Tree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode treeRoot;

    private JPanel toolBar;

    private final PropertyEditorService propertyEditorService;

    public SceneExplorerServiceImpl() {

        this.propertyEditorService = ServiceManager.getService(PropertyEditorService.class);

        JPanel jPanel = new JPanel(new VerticalLayout());

        toolBar = new JPanel(new HorizontalLayout());
        toolBar.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JButton saveButton = new JButton("Save Changes");
        saveButton.setToolTipText("Permanently save any changes you have made.");
        saveButton.setIcon(IconLoader.getIcon("/Icons/SceneExplorer/disk.png"));
        toolBar.add(saveButton);

        /*
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setToolTipText("Re-load the entire scene from storage.");
        refreshButton.setIcon(IconLoader.getIcon("/Icons/SceneExplorer/refresh.png"));
        toolBar.add(refreshButton, BorderLayout.EAST);
         */

        jPanel.add(toolBar);
        jPanel.add(new JSeparator());

        // create tree
        treeRoot = new DefaultMutableTreeNode(new Node("Scene"));
        treeModel = new DefaultTreeModel(treeRoot);

        tree = new Tree(treeModel);
        tree.setCellRenderer(new SceneTreeRenderer());

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

                // normal viewer
                if (item instanceof Spatial) {
                    JmeEngineService engineService = ServiceManager.getService(JmeEngineService.class);

                    engineService.enqueue(() -> {
                        engineService.getStateManager()
                                .getState(NormalViewerState.class)
                                .focus((Spatial) item);
                    });

                }

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

        // set the save action after the treeRoot has been initialized.
        saveButton.addActionListener(e -> {
            Spatial scene = (Spatial) treeRoot.getUserObject();

            String filename = scene.getParent().getName();

            if (filename != null) {

                if (filename.startsWith("file://")) {

                    File outfile = new File(filename.replace("file://", ""));

                    try {
                        BinaryExporter.getInstance().save(scene, outfile);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    System.out.println("Unable to save files inside jars.");
                }
            }
            else {
                System.out.println("filename is null!");
            }
        });

        jPanel.add(tree);

        windowContent = new JBScrollPane(jPanel);
    }

    @Override
    public JComponent getWindowContent() {
        return windowContent;
    }

    @Override
    public void setScene(Spatial scene) {
        treeRoot.removeAllChildren();
        treeModel.reload();

        tree.setVisible(scene != null);
        toolBar.setVisible(scene != null);

        if (scene != null) {

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
