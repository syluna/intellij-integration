package com.jmonkeystore.ide.editor;

import com.intellij.openapi.Disposable;
import com.jmonkeystore.ide.editor.ui.JmeModelEditorUI;

import javax.swing.*;

public interface JmeModelEditor extends Disposable {

    JComponent getJComponent();
    JComponent getContentComponent();
    JmeModelEditorUI getEditor();
}
