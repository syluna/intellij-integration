package com.jmonkeystore.ide.newproject;

public class ResourceRoute {

    private final String resourcePath;
    private final String resourceName;

    private final String destPath;
    private final String destName;

    public ResourceRoute(String resourcePath, String resourceName, String destPath) {
        this(resourcePath, resourceName, destPath, resourceName);
    }

    public ResourceRoute(String resourcePath, String resourceName, String destPath, String destName) {
        this.resourcePath = resourcePath;
        this.resourceName = resourceName;
        this.destPath = destPath;
        this.destName = destName;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getDestPath() {
        return destPath;
    }

    public String getDestName() {
        return destName;
    }

    @Override
    public String toString() {
        return String.format("resource: [ %s | %s ] dest: [ %s | %s ]", resourcePath, resourceName, destPath, destName);
    }
}
