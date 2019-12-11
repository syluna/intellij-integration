package com.jmonkeystore.ide.reflection;


import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.lang.UrlClassLoader;
import com.jmonkeystore.ide.api.plugin.PluginRegistrar;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassPathListener {

    // private ExternalClassLoader sdkClassLoader = new ExternalClassLoader(SceneExplorerService.class.getClassLoader());
    // private ExternalClassLoader jmeClassLoader = new ExternalClassLoader(SceneExplorerService.class.getClassLoader());
    //private ExternalClassLoader thisClassLoader;
    private UrlClassLoader urlClassLoader;

    private final Project project;

    public ClassPathListener(Project project) {
        this.project = project;
    }

    public void run() {

        /*
            So my thinking on this right now is injecting the .JAR file(s) into intellij and scan for
            the things we want:

            We need to be able to import
                - Nodes, Geometries and Meshes.
                - Control Editors
                    - e.g. an object that is not defined already (Vector3f, etc).

            We need to be able to identify the injected dependencies so we can remove them when something is updated.
                - Remove existing injected files
                - (Re)inject dependencies AND current built project.

         */

        // String output = CompilerModuleExtension.getInstance().getCompilerOutputUrl();




        System.out.println("Update Triggered.");

                /*
                    1) get any project dependencies.
                    2) get the project itself.
                    3) inject them into intellij and jmeEngineService(?)
                 */

        // get project dependencies.
        /*
        File buildFile = Paths.get(project.getBasePath(), "build.gradle").toFile();
        GradleBuildScriptEditor gradleBuildScriptEditor;

        try {
            gradleBuildScriptEditor = new GradleBuildScriptEditor(buildFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<GradleDependency> dependencies = gradleBuildScriptEditor.getAllDependencies();

        // jars that have been found that we want.
        // this isn't pulling transients, though. This may an issue...
        List<File> jarDependencies = new ArrayList<>();

        for (GradleDependency dependency : dependencies) {

            if (dependency.getName() == null || dependency.getGroup() == null || dependency.getVersion() == null) {
                continue;
            }

            // we don't want any jmonkey dependencies.
            if (dependency.getGroup().equalsIgnoreCase("org.jmonkeyengine")) {
                continue;
            }

            // %USERPROFILE%/.gradle/caches/modules-2/files-2.1 // windows
            // ~/.gradle/caches/modules-2/files-2.1 // linux
            // ~/.gradle/caches/modules-2/files-2.1 // mac

            Path path = Paths.get(System.getProperty("user.home"), ".gradle", "caches", "modules-2", "files-2.1",
                    dependency.getGroup(),
                    dependency.getName(),
                    dependency.getVersion());

            // from there are multiple randomly named directories. we need to find the dir with the jar:

            File[] dirs = path.toFile().listFiles(File::isDirectory);
            String filename = dependency.getName() + "-" + dependency.getVersion() + ".jar";
            String filenameSources = dependency.getName() + "-" + dependency.getVersion() + "-sources" + ".jar";

            if (dirs != null) {
                for (File dir : dirs) {

                    File[] files = dir.listFiles(file ->
                            file.isFile() &&
                                    (file.getName().equalsIgnoreCase(filename) || file.getName().equalsIgnoreCase(filenameSources))
                            );

                    if (files != null && files.length > 0) {
                        jarDependencies.add(files[0]);
                        break;
                    }

                }
            }

        }

        System.out.println("Found " + jarDependencies.size() + " local dependencies.");
         */

        // get the project itself.

        // this is the
        File libsDir = Paths.get(project.getBasePath(), "build", "libs").toFile();
        File[] buildLibs = libsDir.listFiles(file -> file.getName().endsWith(".jar"));
        List<URL> buildURLs = Arrays.stream(buildLibs)
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    return null;
                })
                .collect(Collectors.toList());

        // inject everything

        // remove everytthing first...
        ServiceManager.getService(SceneExplorerService.class).clearRegisteredNodes();
        ServiceManager.getService(SceneExplorerService.class).clearRegisteredStateListeners();

        // inject the classes into the SDK
        // sdkClassLoader = new ExternalClassLoader(SceneExplorerService.class.getClassLoader());

        if (urlClassLoader != null) {
            ServiceManager.getService(JmeEngineService.class).getAssetManager().removeClassLoader(urlClassLoader);
        }

        // ClassLoader cl = PluginManager.getPlugin(PluginId.getId("com.jmonkeystore.intellij-integration")).getPluginClassLoader();
        ClassLoader cl = ServiceManager.getService(JmeEngineService.class).getExternalAssetLoader().getClass().getClassLoader();
        urlClassLoader = UrlClassLoader.build()
                .parent(cl)
                .urls(buildURLs).get();

        // jmeClassLoader = new ExternalClassLoader(ServiceManager.getService(JmeEngineService.class).getExternalAssetLoader().getClass().getClassLoader());


        // ClassLoader cl = PluginManager.getPlugin(PluginId.getId("com.jmonkeystore.intellij-integration")).getPluginClassLoader();
        // ExternalClassLoader thisClassLoader = new ExternalClassLoader(cl);



        //thisClassLoader = new ExternalClassLoader(getClass().getClassLoader());

        /*
        for (File file : jarDependencies) {
            try {
                sdkClassLoader.addURL(file.toURI().toURL());
                jmeClassLoader.addURL(file.toURI().toURL());
                //thisClassLoader.addURL(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
         */

        /*
        if (buildLibs != null) {
            for (File file : buildLibs) {
                try {
                    // sdkClassLoader.addURL(file.toURI().toURL());
                    // jmeClassLoader.addURL(file.toURI().toURL());
                    // urlClassLoader.addURL(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

         */

        if (/* jarDependencies.isEmpty() && */  (buildLibs == null || buildLibs.length == 0) ) {
            System.out.println("Couldn't find built jar. Have you built the project?");
            return;
        }

        // urlClassLoader = urlBuilder.get();

        ServiceManager.getService(JmeEngineService.class).getAssetManager().addClassLoader(urlClassLoader);

        //UrlClassLoader urlClassLoader = UrlClassLoader.build().urls(sdkClassLoader.getURLs()).get();

        //OrderEnumerator.orderEntries(module).recursively().classes().getUrls()).

        ConfigurationBuilder builder = new ConfigurationBuilder()
                //.addClassLoader(jmeClassLoader)
                .addClassLoader(urlClassLoader)
                // .addClassLoader(urlClassLoader)
                // .addClassLoader(ServiceManager.getService(JmeEngineService.class).getClass().getClassLoader())
                // .addUrls(jmeClassLoader.getURLs())
                .addUrls(urlClassLoader.getUrls())
                // .addUrls(urlClassLoader.getUrls())
                .setScanners(new SubTypesScanner());

        Reflections reflections = new Reflections(builder);
        Set<Class<? extends PluginRegistrar>> classes = reflections.getSubTypesOf(PluginRegistrar.class);

        System.out.println("Found " + classes.size() + " registrar classes.");

        for (Class<? extends PluginRegistrar> registrarClass : classes) {

            try {

                Constructor<? extends PluginRegistrar> registrarConstructor = registrarClass.getConstructor();
                PluginRegistrar registrar = registrarConstructor.newInstance();

                SceneExplorerService explorerService = ServiceManager.getService(SceneExplorerService.class);

                explorerService.registerNodes(registrar.getRegisteredNodes());
                explorerService.registerSceneStateListeners(registrar.getRegisteredSceneStateListeners());


            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }

    }

}
