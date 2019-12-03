package com.jmonkeystore.ide.filetype;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JmeMaterialFileType extends LanguageFileType {

    public static final JmeMaterialFileType INSTANCE = new JmeMaterialFileType();

    private JmeMaterialFileType() {
        super(JmeMaterialLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Jme Material";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Jme Material";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "j3m";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/Icons/jmonkey.png");

    }

}
