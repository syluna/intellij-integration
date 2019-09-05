package com.jmonkeystore.ide.jme.impl;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.asset.ModelKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.jmonkeystore.ide.util.SimpleTextDialog;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;

public class ExternalAssetLoader {

    private final AssetManager assetManager;

    ExternalAssetLoader(AssetManager assetManager) {
        this.assetManager = assetManager;

    }

    @Nullable
    public <T> T load(String url, Class<T> clazz) {
        return load(new AssetKey(url), clazz);
    }

    @Nullable
    public <T> T load(AssetKey assetKey, Class<T> clazz) {

        ExternalFile ext = new ExternalFile(assetKey.getName());

        if (ext.isFile) {

            if (!ext.canRead) {
                new SimpleTextDialog(
                        "Permission Error",
                        "You do not have permission to read this directory."
                ).show();

                return null;
            }

            assetManager.registerLocator(ext.dir, FileLocator.class);

            // we don't want to keep assets cached that are loaded from external sources.
            // it also causes issues when assets have the same name (e.g. sketchfab uses scene.gltf a lot)
            // this occurs because we register the path, so only the model name is the key.

            if (clazz.isAssignableFrom(Spatial.class)) {

                Spatial model = assetManager.loadModel(ext.name);
                assetManager.unregisterLocator(ext.dir, FileLocator.class);
                assetManager.deleteFromCache((ModelKey) model.getKey());

                return (T) model;
            }
            else if (clazz.isAssignableFrom(Material.class)) {
                Material material = assetManager.loadMaterial(ext.name);
                assetManager.unregisterLocator(ext.dir, FileLocator.class);
                assetManager.deleteFromCache((ModelKey) material.getKey());

                return (T) material;
            }

        }
        else if (ext.isJar) {

            if (clazz.isAssignableFrom(Spatial.class)) {
                try {
                    Spatial model = assetManager.loadModel(ext.fullPath);
                    return (T) model;
                } catch (AssetNotFoundException ex) {
                    File jarFile = new File(ext.jarUrl);
                    addToClasspath(getClass().getClassLoader(), jarFile);

                    Spatial model = assetManager.loadModel(ext.fullPath);
                    return (T) model;
                }
            }
            else if (clazz.isAssignableFrom(Material.class)) {
                try {
                    Material material = assetManager.loadMaterial(ext.fullPath);
                    return (T) material;
                } catch (AssetNotFoundException ex) {
                    File jarFile = new File(ext.jarUrl);
                    addToClasspath(getClass().getClassLoader(), jarFile);

                    Material material = assetManager.loadMaterial(ext.fullPath);
                    return (T) material;
                }
            }

        }

        new SimpleTextDialog(
                "Load Model Error",
                "Unable to handle: " + assetKey.getName()
        ).show();

        return null;

    }

    private static class ExternalFile {

        private boolean canRead;

        private String name;
        private String dir;
        private String fullPath;

        private String jarUrl;

        private boolean isFile;
        private boolean isJar;

        private static final String fileDelimiter = "file";
        private static final String jarDelimiter = "jar";

        private String removeProtocolDirt(String input) {

            StringBuilder stringBuilder = new StringBuilder(input);

            while (!Character.isLetter(stringBuilder.charAt(0))) {
                stringBuilder.delete(0, 1);
            }

            return stringBuilder.toString();
        }

        ExternalFile(String input) {

            if (input.toLowerCase().startsWith(fileDelimiter)) {
                isFile = true;
                input = input.substring(fileDelimiter.length());
            }

            if (input.toLowerCase().startsWith(jarDelimiter)) {
                isJar = true;
                input = input.substring(jarDelimiter.length());
            }

            input = removeProtocolDirt(input);

            if (isFile) {

                File file = new File(input);
                this.canRead = file.canRead();

                dir = file.getParent();
                name = file.getName();
                fullPath = input;
            }
            else if (isJar) {

                String jarPart = "jar!/";
                int index = input.indexOf(jarPart);

                fullPath = input.substring(index + jarPart.length());

                File file = new File(fullPath);
                dir = file.getParent();
                name = file.getName();

                jarUrl = input.substring(0, input.indexOf("!/"));
            }

        }

    }

    private void addToClasspath(ClassLoader classLoader, File file) {
        try {
            URL url = file.toURI().toURL();
            Method method = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

}
