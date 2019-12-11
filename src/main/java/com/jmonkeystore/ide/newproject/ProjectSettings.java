package com.jmonkeystore.ide.newproject;

public class ProjectSettings {

    private String groupId;
    private String artifactId;
    private String version;

    private String engineVersion;
    private String lwjglVersion;
    // private boolean useEffectsDependency;
    // private boolean useBulletPhysicsDependency;
    private String bulletPhysicsDependencyType;
    // private boolean useOggDependency;
    // private boolean usePluginsDependency;
    private String templateType;

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getArtifactId() { return artifactId; }
    public void setArtifactId(String artifactId) { this.artifactId = artifactId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getEngineVersion() { return engineVersion; }
    public void setEngineVersion(String engineVersion) { this.engineVersion = engineVersion; }

    public String getLwjglVersion() { return lwjglVersion; }
    public void setLwjglVersion(String lwjglVersion) { this.lwjglVersion = lwjglVersion; }

    // public boolean isUseEffectsDependency() { return useEffectsDependency; }
    // public void setUseEffectsDependency(boolean useEffectsDependency) { this.useEffectsDependency = useEffectsDependency; }

    // public boolean isUseBulletPhysicsDependency() { return useBulletPhysicsDependency; }
    // public void setUseBulletPhysicsDependency(boolean useBulletPhysicsDependency) { this.useBulletPhysicsDependency = useBulletPhysicsDependency; }

    public String getBulletPhysicsDependencyType() { return bulletPhysicsDependencyType; }
    public void setBulletPhysicsDependencyType(String bulletPhysicsDependencyType) { this.bulletPhysicsDependencyType = bulletPhysicsDependencyType; }

    // public boolean isUseOggDependency() { return useOggDependency; }
    // public void setUseOggDependency(boolean useOggDependency) { this.useOggDependency = useOggDependency; }

    // public boolean isUsePluginsDependency() { return usePluginsDependency; }
    // public void setUsePluginsDependency(boolean usePluginsDependency) { this.usePluginsDependency = usePluginsDependency; }


    public String getTemplateType() { return templateType; }
    public void setTemplateType(String templateType) { this.templateType = templateType; }

}
