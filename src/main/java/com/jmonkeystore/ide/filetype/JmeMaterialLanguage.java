package com.jmonkeystore.ide.filetype;

import com.intellij.lang.Language;

public class JmeMaterialLanguage extends Language {
    public static final JmeMaterialLanguage INSTANCE = new JmeMaterialLanguage();

    private JmeMaterialLanguage() {
        super("Jme.Material");
    }
}
