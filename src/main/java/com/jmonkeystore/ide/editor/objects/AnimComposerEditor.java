package com.jmonkeystore.ide.editor.objects;

import com.jme3.anim.AnimComposer;
import com.jmonkeystore.ide.editor.controls.anim.AnimComposerControl;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

public class AnimComposerEditor implements JmeObject {

    private JPanel content;
    private final AnimComposerControl animComposerControl;

    public AnimComposerEditor(AnimComposer animComposer) {

        animComposerControl = new AnimComposerControl(animComposer);

        content = new JPanel(new VerticalLayout());
        content.add(animComposerControl.getJComponent());
    }

    @Override
    public JComponent getJComponent() {
        return content;
    }

    @Override
    public void cleanup() {

    }

}
