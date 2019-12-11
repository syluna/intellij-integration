package com.jmonkeystore.ide;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ExternalClassLoader extends URLClassLoader {

    private List<URL> addedUrls = new ArrayList<>();

    public ExternalClassLoader(ClassLoader parent) {
        this(new URL[0], parent);
    }

    public ExternalClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public void addURL(URL url) {
        super.addURL(url);
        addedUrls.add(url);

    }

}