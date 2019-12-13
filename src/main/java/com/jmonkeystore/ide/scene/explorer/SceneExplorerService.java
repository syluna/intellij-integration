package com.jmonkeystore.ide.scene.explorer;

import com.intellij.openapi.vfs.VirtualFile;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jmonkeystore.ide.api.plugin.input.SceneInputListener;
import com.jmonkeystore.ide.editor.controls.JmeEditorControl;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public interface SceneExplorerService {

   JComponent getWindowContent();
   void setScene(Spatial scene, VirtualFile virtualFile);

   void refreshScene();

   void registerControlEditor(Class<? extends Control> controlClass, Class<? extends JmeEditorControl> editorClass);
   void registerGeometryEditor(Class<? extends Geometry> geomClass, Class<? extends JmeEditorControl> editorClass);
   void registerNodeEditor(Class<? extends Node> nodeClass, Class<? extends JmeEditorControl> editorClass);

   void registerNode(Class<? extends Node> nodeClass);
   void registerNodes(List<Class<? extends Node>> nodes);
   void clearRegisteredNodes();

   void registerSceneStateListener(Class<? extends Spatial> spatialClass, SceneInputListener inputListener);
   void registerSceneStateListeners(Map<Class<? extends Spatial>, SceneInputListener> sceneStateListeners);
   void clearRegisteredStateListeners();

   Class<? extends JmeEditorControl> getControlEditor(Class<? extends Control> controlClass);

   Map<Class<? extends Spatial>, SceneInputListener> getSceneInputListeners();

   SceneInputListener getActiveSceneInputListener();
   void setActiveSceneInputListener(SceneInputListener activeSceneInputListener);
   void setActiveSceneInputListener(Class<?> spatialClass);

}
