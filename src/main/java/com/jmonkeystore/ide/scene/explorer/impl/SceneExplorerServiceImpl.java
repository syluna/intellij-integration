package com.jmonkeystore.ide.scene.explorer.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.Light;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jmonkeystore.ide.editor.controls.ReflectionEditor;
import com.jmonkeystore.ide.editor.impl.JmeModelFileEditorImpl;
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

    // private final Map<Class<?>, Class<? extends JmeObject>> registeredEditors = new HashMap<>();

    private JBScrollPane windowContent;

    // tree
    private final Tree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode treeRoot;

    private JPanel toolBar;

    private final PropertyEditorService propertyEditorService;

    private Spatial scene;
    private VirtualFile virtualFile;

    public SceneExplorerServiceImpl() {

        //registerInternalEditors();

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
        refreshButton.addActionListener(e -> refreshScene());
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

                ReflectionEditor reflectionEditor = new ReflectionEditor(item);
                propertyEditorService.setWindowContent(reflectionEditor);



                // node/geometry/mesh highlighter
                if (modelEditor != null) {

                    if (item instanceof Node) {
                        Node node = (Node) item;
                        modelEditor.highlightWithBoundingBox(node);
                    }
                    else if (item instanceof Geometry) {
                        Geometry geometry = (Geometry) item;
                        modelEditor.highlightMesh(geometry);
                    }
                    else if (item instanceof Mesh) {

                        // this is going to get removed because there's no editpr control assigned, so highlights will be cleaned.
                        // I'll leave it here for now so it starts working when the code has better structure.
                        Geometry geometry = (Geometry) ((DefaultMutableTreeNode)treeNode.getParent()).getUserObject();
                        modelEditor.highlightMesh(geometry);
                    }
                }

                // normal viewer
                if (item instanceof Spatial) {
                    JmeEngineService engineService = ServiceManager.getService(JmeEngineService.class);
                    engineService.enqueue(() -> {
                        engineService.getStateManager()
                                .getState(NormalViewerState.class)
                                .focus((Spatial) item);
                    });
                }

                if (true) {
                    return;
                }

                /*

                boolean foundEditor = false;

                // iterate over all registered editors.
                for (Map.Entry<Class<?>, Class<? extends JmeObject>> entry : registeredEditors.entrySet()) {

                    Class<?> clazz = entry.getKey();

                    if (item.getClass().isAssignableFrom(clazz)) {
                        try {
                            Constructor constructor = entry.getValue().getConstructor(item.getClass());
                            JmeObject editor = (JmeObject) constructor.newInstance(item);
                            propertyEditorService.setWindowContent(editor);
                        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                            ex.printStackTrace();
                        }

                        // regardless of an exception, an editor was matched, so we should stop looking for one.
                        foundEditor = true;
                        break;
                    }

                }

                if (!foundEditor) {
                    propertyEditorService.clearWindowContent();

                    if (modelEditor != null) {
                        modelEditor.clearAllHighlights();
                    }
                }
                */
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

            // Spatial scene = (Spatial) treeRoot.getUserObject();
            // String filename = scene.getParent().getName();


            if (virtualFile != null && scene != null) {

                if (!virtualFile.getPath().startsWith("jar:")) {

                    File outfile = new File(virtualFile.getPath().replace("file://", ""));

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



        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.INSERT);
        SceneTreeDragListener dragListener = new SceneTreeDragListener(tree);

        tree.addMouseListener(new SceneTreeContextMenuListener(tree, treeRoot));

        jPanel.add(tree);

        windowContent = new JBScrollPane(jPanel);
    }

    /*
    public void registerEditor(Class<?> clazz, Class<? extends JmeObject> editorClass) {
        registeredEditors.put(clazz, editorClass);
    }

    private void registerInternalEditors() {

        // spatials
        registerEditor(Node.class, NodeEditor.class);
        registerEditor(Geometry.class, GeometryEditor.class);

        // controls
        registerEditor(AnimControl.class, AnimControlEditor.class);
        registerEditor(AnimComposer.class, AnimComposerEditor.class);

        // lights
        registerEditor(AmbientLight.class, AmbientLightEditor.class);
        registerEditor(DirectionalLight.class, DirectionalLightEditor.class);
        registerEditor(PointLight.class, PointLightEditor.class);
        registerEditor(SpotLight.class, SpotLightEditor.class);

    }

     */

    @Override
    public void refreshScene() {
        // Spatial scene = (Spatial) treeRoot.getUserObject();

        // if (scene != null) {
            setScene(scene, virtualFile);
        // }
    }

    @Override
    public JComponent getWindowContent() {
        return windowContent;
    }

    @Override
    public void setScene(Spatial scene, VirtualFile virtualFile) {
        this.scene = scene;
        this.virtualFile = virtualFile;

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

        if (spatial.getLocalLightList().size() > 0) {

            DefaultMutableTreeNode lightsTreeNode = new DefaultMutableTreeNode(new CollectionNode("Lights", "/Icons/SceneExplorer/light.png"));

            for (Light light : spatial.getLocalLightList()) {
                lightsTreeNode.add(new DefaultMutableTreeNode(light));
            }

            treeNode.add(lightsTreeNode);
        }

    }

    private void findControls(DefaultMutableTreeNode treeNode, Spatial spatial) {

        if (spatial.getNumControls() > 0) {

            DefaultMutableTreeNode controlsTreeNode = new DefaultMutableTreeNode(new CollectionNode("Controls", "/Icons/SceneExplorer/joystick.png"));

            for (int i = 0; i < spatial.getNumControls(); i++) {
                controlsTreeNode.add(new DefaultMutableTreeNode(spatial.getControl(i)));
            }

            treeNode.add(controlsTreeNode);
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
