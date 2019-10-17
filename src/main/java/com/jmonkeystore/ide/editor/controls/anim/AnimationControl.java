package com.jmonkeystore.ide.editor.controls.anim;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jmonkeystore.ide.editor.controls.JmeEditorControl;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.util.AtomicFloat;

import javax.swing.*;
import java.awt.*;

public class AnimationControl implements JmeEditorControl {

    private final AnimControl animControl;
    private JComboBox<String> animsComboBox;
    private JButton playButton;
    private JButton stopButton;
    private JSlider timeSlider;
    private JSlider speedSlider;
    private JPanel contentPanel;

    private final DefaultBoundedRangeModel animTimelineModel = new DefaultBoundedRangeModel(0, 1, 0, 200);
    private AnimChannel animChannel;

    // private float animSpeed = 1.0f;
    private AtomicFloat animSpeed = new AtomicFloat(1.0f);

    public AnimationControl(AnimControl animControl) {
        this.animControl = animControl;

        timeSlider.setModel(animTimelineModel);

        String[] animNames = animControl.getAnimationNames().toArray(new String[0]);
        animChannel = animControl.createChannel();

        if (animNames.length > 0) {
            animChannel.setAnim(animNames[0]);
        }

        EventQueue.invokeLater(() -> animsComboBox.setModel(new DefaultComboBoxModel<>(animNames)) );

        animsComboBox.addItemListener(e -> {
            String animName = (String) animsComboBox.getSelectedItem();
            if (animName != null) {
                animChannel.setAnim(animName);

                int max = (int) (animChannel.getAnimMaxTime() * 1000);
                timeSlider.getModel().setMaximum(max);
            }
        });

        timeSlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            float val = slider.getValue() / 1000f;
            ServiceManager.getService(JmeEngineService.class).enqueue(() -> {
                animChannel.setSpeed(0);
                animChannel.setTime(val);
            });

        });

        speedSlider.setModel(new DefaultBoundedRangeModel(0, 1, 0, 200));
        speedSlider.setValue((int) (animSpeed.get() * 100));
        speedSlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            animSpeed.set(slider.getValue() / 100f);
            animChannel.setSpeed(animSpeed.get());
        });

        playButton.addActionListener(e -> {
            animChannel.setSpeed(animSpeed.get());
            animChannel.setLoopMode(LoopMode.Loop);
        });

        stopButton.addActionListener(e -> {
            animChannel.setSpeed(0);
        });

    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

    @Override
    public void refresh() {

    }

    @Override
    public void cleanup() {
        animControl.clearChannels();
    }

}
