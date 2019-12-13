package com.jmonkeystore.ide.scene.explorer.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jmonkeystore.ide.api.JmeObject;
import com.jmonkeystore.ide.editor.controls.JmeEditorControl;
import com.jmonkeystore.ide.editor.controls.ReflectionEditor;
import com.jmonkeystore.ide.editor.impl.JmeModelFileEditorImpl;
import com.jmonkeystore.ide.editor.ui.JmeModelEditorUI;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.jme.scene.NormalViewerState;
import com.jmonkeystore.ide.scene.editor.PropertyEditorService;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;
import com.jmonkeystore.ide.util.ProjectUtils;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SceneTreeSelectionListener implements TreeSelectionListener {

    @Override
    public void valueChanged(TreeSelectionEvent e) {

        // always clear the window content.
        // propertyEditorService.clearWindowContent();
        PropertyEditorService propertyEditorService = ServiceManager.getService(PropertyEditorService.class);

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) ((Tree) e.getSource()).getLastSelectedPathComponent();

        if (treeNode == null) {
            return;
        }

        SceneExplorerService sceneExplorerService = ServiceManager.getService(SceneExplorerService.class);
        JmeEngineService jmeEngineService = ServiceManager.getService(JmeEngineService.class);

        // disable the active listener
        if (sceneExplorerService.getActiveSceneInputListener() != null) {
            jmeEngineService.getStateManager().detach(sceneExplorerService.getActiveSceneInputListener());
        }

        sceneExplorerService.setActiveSceneInputListener(treeNode.getUserObject().getClass());

        // activeSceneInputListener = sceneStateListeners.get(treeNode.getUserObject().getClass());

        if (sceneExplorerService.getActiveSceneInputListener() != null) {
            Spatial spatial = (Spatial) treeNode.getUserObject();
            sceneExplorerService.getActiveSceneInputListener().setSpatial(spatial);
            jmeEngineService.getStateManager().attach(sceneExplorerService.getActiveSceneInputListener());
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

        //if (treeNode != null) {

            Object item = treeNode.getUserObject();

            JmeObject jmeObject = null;

            // search for all custom editors first.

            if (item instanceof Control) {

                Control control = (Control) item;

                Class<? extends JmeEditorControl> editorClass = sceneExplorerService.getControlEditor(control.getClass());

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

        //}
        //else {
            //propertyEditorService.clearWindowContent();

            //if (modelEditor != null) {
                //modelEditor.clearAllHighlights();
            //}
        //}

    }


}
