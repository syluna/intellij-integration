package com.jmonkeystore.ide.editor;

import com.intellij.openapi.Disposable;

import javax.swing.*;

public interface JmeModelEditor extends Disposable {

    JComponent getJComponent();
    JComponent getContentComponent();
}
