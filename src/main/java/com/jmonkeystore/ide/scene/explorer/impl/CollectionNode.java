package com.jmonkeystore.ide.scene.explorer.impl;

public class CollectionNode {

    private String name;
    private String icon;

    public CollectionNode(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

}
