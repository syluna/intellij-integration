package com.jmonkeystore.ide.scene.explorer;

import com.jme3.scene.Spatial;

import javax.swing.*;

public interface SceneExplorerService {

   JComponent getWindowContent();
   void setScene(Spatial scene);

   void refreshScene();
}
