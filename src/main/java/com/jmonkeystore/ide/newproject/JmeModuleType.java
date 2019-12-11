package com.jmonkeystore.ide.newproject;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JmeModuleType extends ModuleType<JmeModuleBuilder> {

    public static final String ID = "jMonkeyEngine";

    protected JmeModuleType() {
        super(ID);
    }

    @NotNull
    @Override
    public JmeModuleBuilder createModuleBuilder() {
        return new JmeModuleBuilder();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @NotNull
    @Override
    public String getName() {
        return "Jmonkey Game";
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getDescription() {
        return "Create a game using jMonkeyEngine";
    }

    @Override
    public Icon getNodeIcon(boolean b) {
        return IconLoader.getIcon("/Icons/jmonkey.png");
    }

    @NotNull
    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull JmeModuleBuilder moduleBuilder, @NotNull ModulesProvider modulesProvider) {
        return super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider);
    }
}
