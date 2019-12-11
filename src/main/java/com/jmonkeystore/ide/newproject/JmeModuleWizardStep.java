package com.jmonkeystore.ide.newproject;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;

import javax.swing.*;

public class JmeModuleWizardStep extends ModuleWizardStep implements Disposable {

    private JComboBox<String> engineVersionCombo;
    private JPanel panel;
    private JTextField groupIdTextField;
    private JTextField artifactIdTextField;
    private JTextField versionTextField;
    private JComboBox<String> lwjglComboBox;
    private JComboBox<String> bulletPhysicsTypeComboBox;

    private JComboBox<String> templateTypeComboBox;

    private WizardContext context;
    private JmeModuleBuilder builder;

    private final String[] templates = {
            TemplateTypes.BASIC,
            TemplateTypes.FPS,
    };

    private final String[] versions = {

            "3.3.0-alpha5",
            "3.3.0-alpha4",
            "3.3.0-alpha3",
            "3.3.0-alpha2",
            "3.3.0-alpha1",

            "3.2.4-stable",
            "3.2.3-stable",
            "3.2.2-stable",
    };

    private final String[] lwjglVersions = {
            "LWJGL 3",
            "LWJGL 2"
    };

    private final String[] bulletTypes = {
        "Native",
        "jBullet"
    };

    public JmeModuleWizardStep(WizardContext context, JmeModuleBuilder builder) {
        this.context = context;
        this.builder = builder;

        templateTypeComboBox.setModel(new DefaultComboBoxModel(templates));
        engineVersionCombo.setModel(new DefaultComboBoxModel<>(versions));
        lwjglComboBox.setModel(new DefaultComboBoxModel<>(lwjglVersions));
        bulletPhysicsTypeComboBox.setModel(new DefaultComboBoxModel<>(bulletTypes));

        // hide bullet options if it's not selected.
        //setBulletTypeVisible(bulletPhysicsDependencyCheckBox.isSelected());
        //bulletPhysicsDependencyCheckBox.addActionListener(e -> {
            //JCheckBox cb = (JCheckBox) e.getSource();
            //setBulletTypeVisible(cb.isSelected());
        //});

    }



    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void updateDataModel() {

        builder.getProjectSettings().setGroupId(groupIdTextField.getText().trim());
        builder.getProjectSettings().setArtifactId(artifactIdTextField.getText().trim());
        builder.getProjectSettings().setVersion(versionTextField.getText().trim());

        builder.getProjectSettings().setEngineVersion(versions[engineVersionCombo.getSelectedIndex()]);
        builder.getProjectSettings().setLwjglVersion(lwjglVersions[lwjglComboBox.getSelectedIndex()]);

        // builder.getProjectSettings().setUseEffectsDependency(effectsDependencyComboBox.isSelected());
        // builder.getProjectSettings().setUseEffectsDependency(true);
        // builder.getProjectSettings().setUseBulletPhysicsDependency(true);
        builder.getProjectSettings().setBulletPhysicsDependencyType(bulletTypes[bulletPhysicsTypeComboBox.getSelectedIndex()]);
        // builder.getProjectSettings().setUseOggDependency(true);
        // builder.getProjectSettings().setUsePluginsDependency(true);
        builder.getProjectSettings().setTemplateType(templates[templateTypeComboBox.getSelectedIndex()]);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void disposeUIResources() {
        Disposer.dispose(this);
    }

}