package com.jmonkeystore.ide.action.importmodel.importer;

import com.google.common.io.Files;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.jme3.scene.Spatial;
import com.jmonkeystore.ide.action.importmodel.importer.gltf.GltfExtrasLoader;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.util.SimpleTextDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModelImporter {

    private static final Logger log = Logger.getInstance(ModelImporter.class);

    private JmeEngineService engineService;

    private File sourceRoot;
    private File targetRoot;
    private String targetAssetPath;

    private AssetWriter writer;
    private Probe probe = null;
    private List<ModelProcessor> processors = new ArrayList<>();

    public ModelImporter() {
        this.engineService = ServiceManager.getService(JmeEngineService.class);
    }

    public void begin(String srcRoot, String targetRoot, String targetAssetPath, String modelPath) {

        setSourceRoot(new File(srcRoot)); // C:\\Downloads\\CoolModel
        setTargetRoot(new File(targetRoot)); // assets
        setTargetAssetPath(targetAssetPath);  // Models/CoolModel
        // setProbeOptions(it.next());

        if (!getSourceRoot().canRead()) {
            new SimpleTextDialog("Permission Error", "You do not have permission to read this directory.")
                    .showAndGet();
        }

        if (!getTargetRoot().canWrite()) {
            new SimpleTextDialog("Permission Error", "You do not have permission to write to this directory.")
                    .showAndGet();
        }

        convert(new File(modelPath)); // C:\\Downloads\\CoolModel\\AwesomeThing.gltf
    }

    public void setSourceRoot( File f ) {
        if( !f.exists() ) {
            log.error("Source root doesn't exist:" + f);
            return;
        }
        if( !f.isDirectory() ) {
            log.error("Source root is not a directory:" + f);
            return;
        }
        this.sourceRoot = f;
        // this.assets = new AssetReader(f);
    }

    public File getSourceRoot() {
        return sourceRoot;
    }

    public void setTargetRoot( File f ) {
        this.targetRoot = f;
        getAssetWriter().setTarget(f);
    }

    public File getTargetRoot() {
        return targetRoot;
    }

    public void setTargetAssetPath( String path ) {
        this.targetAssetPath = path;
        getAssetWriter().setAssetPath(path);
    }

    public String getTargetAssetPath() {
        return targetAssetPath;
    }

    protected AssetWriter getAssetWriter() {
        if( writer == null ) {
            writer = new AssetWriter();
            processors.add(writer);
        }
        return writer;
    }

    protected Probe getProbe() {
        if( probe == null ) {
            probe = new Probe();
            processors.add(0, probe);
        }
        return probe;
    }

    public void convert( File f ) {
        if( !f.exists() ) {
            log.error("File doesn't exist:" + f);
            return;
        }
        log.info("Convert:" + f);
        // Spatial s = assets.loadModel(f);
        // Spatial s = engineService.loadExternalModel(f.getAbsolutePath());

        Spatial s;

        String extension = Files.getFileExtension(f.getName());

        if( "gltf".equalsIgnoreCase(extension) ) {
            // We do special setup for GLTF
            s = engineService.getExternalAssetLoader().load(GltfExtrasLoader.createModelKey(f.getAbsolutePath()), Spatial.class);
        } else {
            s =  engineService.getExternalAssetLoader().load(f.getAbsolutePath(), Spatial.class);
        }

        if (s != null) {
            ModelInfo info = new ModelInfo(sourceRoot, f.getName(), s);
            runProcessors(info);
        }

    }

    public void runProcessors( ModelInfo info ) {
        if( processors.isEmpty() ) {
            log.warn("No output configured, probing instead.");
            getProbe(); // just let it use defaults
        }
        log.info("Processing:" + info.getModelName());
        for( ModelProcessor proc : processors ) {
            proc.apply(info);
        }
    }

}
