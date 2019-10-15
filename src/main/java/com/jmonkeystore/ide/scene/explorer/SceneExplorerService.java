package com.jmonkeystore.ide.scene.explorer;

import com.intellij.openapi.vfs.VirtualFile;
import com.jme3.scene.Spatial;

import javax.swing.*;

public interface SceneExplorerService {

   JComponent getWindowContent();
   void setScene(Spatial scene, VirtualFile virtualFile);

   void refreshScene();
}
