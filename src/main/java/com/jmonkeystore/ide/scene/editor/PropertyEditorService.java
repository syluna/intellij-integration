package com.jmonkeystore.ide.scene.editor;

import com.jmonkeystore.ide.editor.objects.JmeObject;

import javax.swing.*;

public interface PropertyEditorService {

    JComponent getWindowContent();
    void setWindowContent(JmeObject jmeObject);
    void clearWindowContent();
}
