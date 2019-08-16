package com.jmonkeystore.ide.filetype;

import com.intellij.lang.Language;

public class JmeModelLanguage extends Language {
    public static final JmeModelLanguage INSTANCE = new JmeModelLanguage();

    private JmeModelLanguage() {
        super("Jme.Model");
    }
}
