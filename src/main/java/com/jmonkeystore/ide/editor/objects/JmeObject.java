package com.jmonkeystore.ide.editor.objects;

import javax.swing.*;

public interface JmeObject {

    JComponent getJComponent();
    void cleanup();
}
