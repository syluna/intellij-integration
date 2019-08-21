package com.jmonkeystore.ide.editor.objects;

import com.jme3.animation.AnimControl;
import com.jmonkeystore.ide.editor.controls.anim.AnimationControl;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

public class AnimControlEditor implements JmeObject {

    private JPanel content;
    private final AnimationControl animationControl;

    public AnimControlEditor(AnimControl animControl) {

        animationControl = new AnimationControl(animControl);

        content = new JPanel(new VerticalLayout());
        content.add(animationControl.getJComponent());
    }

    @Override
    public JComponent getJComponent() {
        return content;
    }

    @Override
    public void cleanup() {
        animationControl.cleanup();
    }

}
