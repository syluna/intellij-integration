package com.jmonkeystore.ide.viewer;

import com.intellij.openapi.Disposable;
import com.jmonkeystore.ide.viewer.ui.JmeMaterialViewerUI;

import javax.swing.*;

public interface JmeMaterialViewer extends Disposable {
    JComponent getJComponent();
    JComponent getContentComponent();
    JmeMaterialViewerUI getEditor();
}
