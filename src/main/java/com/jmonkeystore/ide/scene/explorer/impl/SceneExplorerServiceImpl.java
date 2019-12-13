package com.jmonkeystore.ide.scene.explorer.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.jme3.anim.AnimComposer;
import com.jme3.animation.AnimControl;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.light.Light;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jmonkeystore.ide.api.plugin.input.SceneInputListener;
import com.jmonkeystore.ide.editor.controls.JmeEditorControl;
import com.jmonkeystore.ide.editor.controls.anim.AnimComposerControl;
import com.jmonkeystore.ide.editor.controls.anim.AnimationControl;
import com.jmonkeystore.ide.scene.editor.PropertyEditorService;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;
import com.jmonkeystore.ide.scene.explorer.SceneTreeRenderer;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SceneExplorerServiceImpl implements SceneExplorerService {

    // editors for Control (AnimControl, AnimComposer, etc)
    private Map<Class<? extends Control>, Class<? extends JmeEditorControl>> controlEditors = new HashMap<>();
    // editors for Geometry (ParticleEmitter, etc)
    private Map<Class<? extends Geometry>, Class<? extends JmeEditorControl>> geometryEditors = new HashMap<>();
    // editors for Node (lemur labels, etc).
    private Map<Class<? extends Node>, Class<? extends JmeEditorControl>> nodeEditors = new HashMap<>();

    private Map<Class<? extends Spatial>, SceneInputListener> sceneInputListeners = new HashMap<>();
    private SceneInputListener activeSceneInputListener;

    private SceneTreeContextMenuListener contextMenuListener;

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
        tree.setToggleClickCount(0);
        tree.setCellRenderer(new SceneTreeRenderer());

        /*
        tree.addTreeSelectionListener(e -> {



            // always clear the window content.
            propertyEditorService.clearWindowContent();

            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) ((Tree) e.getSource()).getLastSelectedPathComponent();

            if (treeNode == null) {
                return;
            }

            // disable the active listener
            if (activeSceneInputListener != null) {
                ServiceManager.getService(JmeEngineService.class).getStateManager().detach(activeSceneInputListener);
            }

            activeSceneInputListener = sceneInputListeners.get(treeNode.getUserObject().getClass());

            if (activeSceneInputListener != null) {
                Spatial spatial = (Spatial) treeNode.getUserObject();
                activeSceneInputListener.setSpatial(spatial);

                ServiceManager.getService(JmeEngineService.class).getStateManager().attach(activeSceneInputListener);
            }


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

                JmeObject jmeObject = null;

                // search for all custom editors first.

                if (item instanceof Control) {

                    Control control = (Control) item;

                    Class<? extends JmeEditorControl> editorClass = controlEditors.get(control.getClass());

                    if (editorClass != null) {
                        try {
                            Constructor<? extends JmeEditorControl> constructor = editorClass.getConstructor(control.getClass());
                            jmeObject = constructor.newInstance(control);
                        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                            ex.printStackTrace();
                        }
                    }

                }

                // if a control has been found, use it, else reflect the properties.
                // actually we don't always want this. Geometry and Node editors (particles, etc) will want the default editor, too.

                if (jmeObject != null) {
                    propertyEditorService.setWindowContent(jmeObject);
                }
                else {
                    ReflectionEditor reflectionEditor = new ReflectionEditor(item);
                    propertyEditorService.setWindowContent(reflectionEditor);
                }

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
                    else {
                        modelEditor.clearAllHighlights();
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

            }
            else {
                propertyEditorService.clearWindowContent();

                if (modelEditor != null) {
                    modelEditor.clearAllHighlights();
                }
            }
        });

         */
        tree.addTreeSelectionListener(new SceneTreeSelectionListener());
        tree.addMouseListener(new SceneTreeClickListener());

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

        contextMenuListener = new SceneTreeContextMenuListener(tree, treeRoot);
        tree.addMouseListener(contextMenuListener);

        jPanel.add(tree);

        windowContent = new JBScrollPane(jPanel);

        registerInternalControlEditors();
    }

    @Override
    public void registerControlEditor(Class<? extends Control> controlClass, Class<? extends JmeEditorControl> editorClass) {
        controlEditors.put(controlClass, editorClass);
    }

    private void registerInternalControlEditors() {
        controlEditors.put(AnimControl.class, AnimationControl.class);
        controlEditors.put(AnimComposer.class, AnimComposerControl.class);
    }

    @Override
    public void registerGeometryEditor(Class<? extends Geometry> geomClass, Class<? extends JmeEditorControl> editorClass) {
        geometryEditors.put(geomClass, editorClass);
    }

    @Override
    public void registerNodeEditor(Class<? extends Node> nodeClass, Class<? extends JmeEditorControl> editorClass) {
        nodeEditors.put(nodeClass, editorClass);
    }

    @Override
    public void registerNode(Class<? extends Node> nodeClass) {
        contextMenuListener.registerNode(nodeClass);
    }

    @Override
    public void registerNodes(List<Class<? extends Node>> nodes) {
        contextMenuListener.registerNodes(nodes);
    }

    @Override
    public void clearRegisteredNodes() {
        contextMenuListener.clearRegisteredNodes();
    }

    @Override
    public void registerSceneStateListener(Class<? extends Spatial> spatialClass, SceneInputListener inputListener) {
        this.sceneInputListeners.put(spatialClass, inputListener);
    }

    @Override
    public void registerSceneStateListeners(Map<Class<? extends Spatial>, SceneInputListener> listeners) {
        this.sceneInputListeners.putAll(listeners);
    }

    @Override
    public void clearRegisteredStateListeners() {
        this.sceneInputListeners.clear();
    }

    @Override
    public Class<? extends JmeEditorControl> getControlEditor(Class<? extends Control> controlClass) {
        return controlEditors.get(controlClass);
    }

    @Override
    public Map<Class<? extends Spatial>, SceneInputListener> getSceneInputListeners() {
        return sceneInputListeners;
    }

    @Override
    public SceneInputListener getActiveSceneInputListener() {
        return activeSceneInputListener;
    }

    @Override
    public void setActiveSceneInputListener(SceneInputListener activeSceneInputListener) {
        this.activeSceneInputListener = activeSceneInputListener;
    }

    @Override
    public void setActiveSceneInputListener(Class<?> spatialClass) {
        this.activeSceneInputListener = sceneInputListeners.get(spatialClass);
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
