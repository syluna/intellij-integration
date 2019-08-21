package com.jmonkeystore.ide.editor.controls;

import javax.swing.*;

public interface JmeEditorControl {

    JComponent getJComponent();
    void refresh();

    void cleanup();
}
