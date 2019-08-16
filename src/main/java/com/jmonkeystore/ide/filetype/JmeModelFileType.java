package com.jmonkeystore.ide.filetype;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JmeModelFileType extends LanguageFileType {

    public static final JmeModelFileType INSTANCE = new JmeModelFileType();

    private JmeModelFileType() {
        super(JmeModelLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Jme Model";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Jme Model";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "j3o";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/Icons/jmonkey.png");

    }
}
