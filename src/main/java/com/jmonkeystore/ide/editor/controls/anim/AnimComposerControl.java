package com.jmonkeystore.ide.editor.controls.anim;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jmonkeystore.ide.editor.controls.JmeEditorControl;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.util.AtomicFloat;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class AnimComposerControl implements JmeEditorControl {

    private AnimComposer animComposer;

    private JComboBox<AnimClip> animsComboBox;
    private JButton playButton;
    private JButton stopButton;
    private JSlider timeSlider;
    private JSlider speedSlider;
    private JPanel contentPanel;

    private final DefaultBoundedRangeModel animTimelineModel = new DefaultBoundedRangeModel(0, 1, 0, 200);
    private AnimClip animClip;

    private AtomicFloat animSpeed = new AtomicFloat(1.0f);

    public AnimComposerControl(AnimComposer animComposer) {
        this.animComposer = animComposer;

        timeSlider.setModel(animTimelineModel);

        Collection<AnimClip> animClipsCollection = animComposer.getAnimClips();
        AnimClip[] animClips = animClipsCollection.toArray(new AnimClip[0]);

        if (animClips.length > 0) {
            animClip = animClips[0];
        }

        EventQueue.invokeLater(() -> {
            animsComboBox.setModel(new DefaultComboBoxModel<>(animClips));
        });

        animsComboBox.addItemListener(e -> {
            AnimClip selectedClip = (AnimClip) animsComboBox.getSelectedItem();

            if (selectedClip != null) {
                animClip = selectedClip;

                int max = (int) (animClip.getLength() * 1000);
                timeSlider.getModel().setMaximum(max);
            }
        });

        timeSlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            float val = slider.getValue() / 1000f;
            ServiceManager.getService(JmeEngineService.class).enqueue(() -> {

            });

        });

        speedSlider.setModel(new DefaultBoundedRangeModel(0, 1, 0, 200));
        speedSlider.setValue((int) (animSpeed.get() * 100));
        speedSlider.addChangeListener(e -> {
            JSlider slider = (JSlider) e.getSource();
            animSpeed.set(slider.getValue() / 100f);
            // animChannel.setSpeed(animSpeed.get());
            animComposer.setGlobalSpeed(animSpeed.get());
        });

        playButton.addActionListener(e -> {
            animComposer.setCurrentAction(animClip.getName());
            // animChannel.setSpeed(animSpeed.get());
            // animChannel.setLoopMode(LoopMode.Loop);
        });

        stopButton.addActionListener(e -> {
            // animChannel.setSpeed(0);
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

    }

}
