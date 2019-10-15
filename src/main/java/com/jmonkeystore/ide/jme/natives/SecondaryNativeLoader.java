package com.jmonkeystore.ide.jme.natives;

import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import com.jmonkeystore.ide.startup.JmeStartupActivity;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basically a carbon copy except we use a different classloader. Slightly annoying tbh.
 * lines: 193, 198, 371
 */
public class SecondaryNativeLoader {

    private static final Logger logger = Logger.getLogger(SecondaryNativeLoader.class.getName());

    private static File extractionFolderOverride = null;
    private static File extractionFolder = null;
    private static final byte[] buf = new byte[1024 * 100];

    private static final HashMap<NativeLibrary.Key, NativeLibrary> nativeLibraryMap = new HashMap<>();

    static {
        // LWJGL
        registerNativeLibrary("lwjgl", Platform.Windows32, "native/windows/lwjgl.dll");
        registerNativeLibrary("lwjgl", Platform.Windows64, "native/windows/lwjgl64.dll");
        registerNativeLibrary("lwjgl", Platform.Linux32,   "native/linux/liblwjgl.so");
        registerNativeLibrary("lwjgl", Platform.Linux64,   "native/linux/liblwjgl64.so");
        registerNativeLibrary("lwjgl", Platform.MacOSX32,  "native/macosx/liblwjgl.dylib");
        registerNativeLibrary("lwjgl", Platform.MacOSX64,  "native/macosx/liblwjgl.dylib");

        // OpenAL
        // For OSX: Need to add lib prefix when extracting
        registerNativeLibrary("openal", Platform.Windows32, "native/windows/OpenAL32.dll");
        registerNativeLibrary("openal", Platform.Windows64, "native/windows/OpenAL64.dll");
        registerNativeLibrary("openal", Platform.Linux32,   "native/linux/libopenal.so");
        registerNativeLibrary("openal", Platform.Linux64,   "native/linux/libopenal64.so");
        registerNativeLibrary("openal", Platform.MacOSX32,  "native/macosx/openal.dylib", "libopenal.dylib");
        registerNativeLibrary("openal", Platform.MacOSX64,  "native/macosx/openal.dylib", "libopenal.dylib");

        // LWJGL 3.x
        registerNativeLibrary("lwjgl3", Platform.Windows32, "native/windows/lwjgl32.dll");
        registerNativeLibrary("lwjgl3", Platform.Windows64, "native/windows/lwjgl.dll");
        registerNativeLibrary("lwjgl3", Platform.Linux32, "native/linux/liblwjgl32.so");
        registerNativeLibrary("lwjgl3", Platform.Linux64, "native/linux/liblwjgl.so");
        registerNativeLibrary("lwjgl3", Platform.MacOSX32, "native/macosx/liblwjgl.dylib");
        registerNativeLibrary("lwjgl3", Platform.MacOSX64, "native/macosx/liblwjgl.dylib");

        // GLFW for LWJGL 3.x
        registerNativeLibrary("glfw-lwjgl3", Platform.Windows32, "native/windows/glfw32.dll");
        registerNativeLibrary("glfw-lwjgl3", Platform.Windows64, "native/windows/glfw.dll");
        registerNativeLibrary("glfw-lwjgl3", Platform.Linux32, "native/linux/libglfw32.so");
        registerNativeLibrary("glfw-lwjgl3", Platform.Linux64, "native/linux/libglfw.so");
        registerNativeLibrary("glfw-lwjgl3", Platform.MacOSX32, "native/macosx/libglfw.dylib");
        registerNativeLibrary("glfw-lwjgl3", Platform.MacOSX64, "native/macosx/libglfw.dylib");

        // jemalloc for LWJGL 3.x
        registerNativeLibrary("jemalloc-lwjgl3", Platform.Windows32, "native/windows/jemalloc32.dll");
        registerNativeLibrary("jemalloc-lwjgl3", Platform.Windows64, "native/windows/jemalloc.dll");
        registerNativeLibrary("jemalloc-lwjgl3", Platform.Linux32, "native/linux/libjemalloc32.so");
        registerNativeLibrary("jemalloc-lwjgl3", Platform.Linux64, "native/linux/libjemalloc.so");
        registerNativeLibrary("jemalloc-lwjgl3", Platform.MacOSX32, "native/macosx/libjemalloc.dylib");
        registerNativeLibrary("jemalloc-lwjgl3", Platform.MacOSX64, "native/macosx/libjemalloc.dylib");

        // OpenAL for LWJGL 3.x
        // For OSX: Need to add lib prefix when extracting
        registerNativeLibrary("openal-lwjgl3", Platform.Windows32, "native/windows/OpenAL32.dll");
        registerNativeLibrary("openal-lwjgl3", Platform.Windows64, "native/windows/OpenAL.dll");
        registerNativeLibrary("openal-lwjgl3", Platform.Linux32, "native/linux/libopenal32.so");
        registerNativeLibrary("openal-lwjgl3", Platform.Linux64, "native/linux/libopenal.so");
        registerNativeLibrary("openal-lwjgl3", Platform.MacOSX32, "native/macosx/openal.dylib", "libopenal.dylib");
        registerNativeLibrary("openal-lwjgl3", Platform.MacOSX64, "native/macosx/openal.dylib", "libopenal.dylib");

        // BulletJme
        registerNativeLibrary("bulletjme", Platform.Windows32, "native/windows/x86/bulletjme.dll");
        registerNativeLibrary("bulletjme", Platform.Windows64, "native/windows/x86_64/bulletjme.dll");
        registerNativeLibrary("bulletjme", Platform.Linux32,   "native/linux/x86/libbulletjme.so");
        registerNativeLibrary("bulletjme", Platform.Linux64,   "native/linux/x86_64/libbulletjme.so");
        registerNativeLibrary("bulletjme", Platform.MacOSX32,  "native/osx/x86/libbulletjme.dylib");
        registerNativeLibrary("bulletjme", Platform.MacOSX64,  "native/osx/x86_64/libbulletjme.dylib");

        // JInput
        // For OSX: Need to rename extension jnilib -> dylib when extracting
        registerNativeLibrary("jinput", Platform.Windows32, "native/windows/jinput-raw.dll");
        registerNativeLibrary("jinput", Platform.Windows64, "native/windows/jinput-raw_64.dll");
        registerNativeLibrary("jinput", Platform.Linux32,   "native/windows/libjinput-linux.so");
        registerNativeLibrary("jinput", Platform.Linux64,   "native/windows/libjinput-linux64.so");
        registerNativeLibrary("jinput", Platform.MacOSX32,  "native/macosx/libjinput-osx.jnilib", "libjinput-osx.dylib");
        registerNativeLibrary("jinput", Platform.MacOSX64,  "native/macosx/libjinput-osx.jnilib", "libjinput-osx.dylib");

        // JInput Auxiliary (only required on Windows)
        registerNativeLibrary("jinput-dx8", Platform.Windows32, "native/windows/jinput-dx8.dll");
        registerNativeLibrary("jinput-dx8", Platform.Windows64, "native/windows/jinput-dx8_64.dll");
        registerNativeLibrary("jinput-dx8", Platform.Linux32,   null);
        registerNativeLibrary("jinput-dx8", Platform.Linux64,   null);
        registerNativeLibrary("jinput-dx8", Platform.MacOSX32,  null);
        registerNativeLibrary("jinput-dx8", Platform.MacOSX64,  null);
    }

    /**
     * Register a new known JNI library.
     *
     * This simply registers a known library, the actual extraction and loading
     * is performed by calling {@link #loadNativeLibrary(java.lang.String, boolean) }.
     *
     * This method should be called several times for each library name,
     * each time specifying a different platform + path combination.
     *
     * @param name The name / ID of the library (not OS or architecture specific).
     * @param platform The platform for which the in-natives-jar path has
     * been specified for.
     * @param path The path inside the natives-jar or classpath
     * corresponding to this library. Must be compatible with the platform
     * argument.
     */
    public static void registerNativeLibrary(String name, Platform platform,
                                             String path) {
        registerNativeLibrary(name, platform, path, null);
    }

    /**
     * Register a new known library.
     *
     * This simply registers a known library, the actual extraction and loading
     * is performed by calling {@link #loadNativeLibrary(java.lang.String, boolean) }.
     *
     * @param name The name / ID of the library (not OS or architecture specific).
     * @param platform The platform for which the in-natives-jar path has
     * been specified for.
     * @param path The path inside the natives-jar or classpath
     * corresponding to this library. Must be compatible with the platform
     * argument.
     * @param extractAsName The filename that the library should be extracted as,
     * if null, use the same name as in the path.
     */
    public static void registerNativeLibrary(String name, Platform platform,
                                             String path, String extractAsName) {
        nativeLibraryMap.put(new NativeLibrary.Key(name, platform),
                new NativeLibrary(name, platform, path, extractAsName));
    }

    /**
     * First extracts the native library and then loads it.
     *
     * @param name The name of the library to load.
     * @param isRequired If true and the library fails to load, throw exception. If
     * false, do nothing if it fails to load.
     */
    public static void loadNativeLibrary(String name, boolean isRequired) {
        if (JmeSystem.isLowPermissions()) {
            throw new UnsupportedOperationException("JVM is running under "
                    + "reduced permissions. Cannot load native libraries.");
        }

        Platform platform = JmeSystem.getPlatform();
        NativeLibrary library = nativeLibraryMap.get(new NativeLibrary.Key(name, platform));

        if (library == null) {
            // No library exists for this platform.
            if (isRequired) {
                throw new UnsatisfiedLinkError(
                        "The required native library '" + name + "'"
                                + " is not available for your OS: " + platform);
            } else {
                logger.log(Level.FINE, "The optional native library ''{0}''" +
                                " is not available for your OS: {1}",
                        new Object[]{name, platform});
                return;
            }
        }

        final String pathInJar = library.getPathInNativesJar();

        if (pathInJar == null) {
            // This platform does not require the native library to be loaded.
            return;
        }

        final String fileNameInJar;

        if (pathInJar.contains("/")) {
            fileNameInJar = pathInJar.substring(pathInJar.lastIndexOf("/") + 1);
        } else {
            fileNameInJar = pathInJar;
        }

        // URL url = Thread.currentThread().getContextClassLoader().getResource(pathInJar);
        URL url = JmeStartupActivity.class.getClassLoader().getResource(pathInJar);

        if (url == null) {
            // Try the root of the classpath as well.
            // url = Thread.currentThread().getContextClassLoader().getResource(fileNameInJar);
            url = JmeStartupActivity.class.getClassLoader().getResource(fileNameInJar);
        }

        if (url == null) {
            // Attempt to load it as a system library.
            String unmappedName = unmapLibraryName(fileNameInJar);
            try {
                // XXX: HACK. Vary loading method based on library name..
                // lwjgl and jinput handle loading by themselves.
                if (!name.equals("lwjgl") && !name.equals("jinput")) {
                    // Need to unmap it from library specific parts.
                    System.loadLibrary(unmappedName);
                    logger.log(Level.FINE, "Loaded system installed "
                            + "version of native library: {0}", unmappedName);
                }
            } catch (UnsatisfiedLinkError e) {
                if (isRequired) {
                    throw new UnsatisfiedLinkError(
                            "The required native library '" + unmappedName + "'"
                                    + " was not found in the classpath via '" + pathInJar
                                    + "'. Error message: " + e.getMessage());
                } else {
                    logger.log(Level.FINE, "The optional native library ''{0}''" +
                                    " was not found in the classpath via ''{1}''" +
                                    ". Error message: {2}",
                            new Object[]{unmappedName, pathInJar, e.getMessage()});
                }
            }

            return;
        }

        // The library has been found and is ready to be extracted.
        // Determine what filename it should be extracted as.
        String loadedAsFileName;
        if (library.getExtractedAsName() != null) {
            loadedAsFileName = library.getExtractedAsName();
        } else {
            // Just use the original filename as it is in the JAR.
            loadedAsFileName = fileNameInJar;
        }

        File extactionDirectory = getExtractionFolder();
        URLConnection conn;
        InputStream in;

        try {
            conn = url.openConnection();
            in = conn.getInputStream();
        } catch (IOException ex) {
            // Maybe put more detail here? Not sure..
            throw new UnsatisfiedLinkError("Failed to open file: '" + url +
                    "'. Error: " + ex);
        }

        File targetFile = new File(extactionDirectory, loadedAsFileName);
        OutputStream out = null;
        try {
            if (targetFile.exists()) {
                // OK, compare last modified date of this file to
                // file in jar
                long targetLastModified = targetFile.lastModified();
                long sourceLastModified = conn.getLastModified();

                // Allow ~1 second range for OSes that only support low precision
                if (targetLastModified + 1000 > sourceLastModified) {
                    logger.log(Level.FINE, "Not copying library {0}. " +
                                    "Latest already extracted.",
                            loadedAsFileName);
                    return;
                }
            }

            out = new FileOutputStream(targetFile);
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            in = null;
            out.close();
            out = null;

            // NOTE: On OSes that support "Date Created" property,
            // this will cause the last modified date to be lower than
            // date created which makes no sense
            targetFile.setLastModified(conn.getLastModified());
        } catch (IOException ex) {
            if (ex.getMessage().contains("used by another process")) {
                return;
            } else {
                throw new UnsatisfiedLinkError("Failed to extract native "
                        + "library to: " + targetFile);
            }
        } finally {
            // XXX: HACK. Vary loading method based on library name..
            // lwjgl and jinput handle loading by themselves.
            if (name.equals("lwjgl") || name.equals("lwjgl3")) {
                System.setProperty("org.lwjgl.librarypath",
                        extactionDirectory.getAbsolutePath());
            } else if (name.equals("jinput")) {
                System.setProperty("net.java.games.input.librarypath",
                        extactionDirectory.getAbsolutePath());
            } else {
                // all other libraries (openal, bulletjme, custom)
                // will load directly in here.
                System.load(targetFile.getAbsolutePath());
            }

            if(in != null){
                try { in.close(); } catch (IOException ex) { }
            }
            if(out != null){
                try { out.close(); } catch (IOException ex) { }
            }
        }

        logger.log(Level.FINE, "Loaded native library from ''{0}'' into ''{1}''",
                new Object[]{url, targetFile});

    }

    /**
     * Determine jME3's cache folder for the user account based on the OS.
     *
     * If the OS cache folder is missing, the assumption is that this
     * particular version of the OS does not have a dedicated cache folder,
     * hence, we use the user's home folder instead as the root.
     *
     * The folder returned is as follows:<br>
     * <ul>
     * <li>Windows: ~\AppData\Local\jme3</li>
     * <li>Mac OS X: ~/Library/Caches/jme3</li>
     * <li>Linux: ~/.cache/jme3</li>
     * </ul>
     *
     * @return the user cache folder.
     */
    private static File getJmeUserCacheFolder() {
        File userHomeFolder = new File(System.getProperty("user.home"));
        File userCacheFolder = null;

        switch (JmeSystem.getPlatform()) {
            case Linux32:
            case Linux64:
                userCacheFolder = new File(userHomeFolder, ".cache");
                break;
            case MacOSX32:
            case MacOSX64:
            case MacOSX_PPC32:
            case MacOSX_PPC64:
                userCacheFolder = new File(new File(userHomeFolder, "Library"), "Caches");
                break;
            case Windows32:
            case Windows64:
                userCacheFolder = new File(new File(userHomeFolder, "AppData"), "Local");
                break;
        }

        if (userCacheFolder == null || !userCacheFolder.exists()) {
            // Fallback to home directory if cache folder is missing
            return new File(userHomeFolder, ".jme3");
        }

        return new File(userCacheFolder, "jme3");
    }

    private static int computeNativesHash() {
        URLConnection conn = null;
        try {
            String classpath = System.getProperty("java.class.path");
            // URL url = JmePreloadActivity.class.getClassLoader().getResource("com/jme3/system/NativeLibraryLoader.class");
            URL url = JmeStartupActivity.class.getClassLoader().getResource("com/jmonkeystore/ide/jme/natives/SecondaryNativeLoader.class");
            // com.jmonkeystore.ide.jme.natives

            StringBuilder sb = new StringBuilder(url.toString());
            if (sb.indexOf("jar:") == 0) {
                sb.delete(0, 4);
                sb.delete(sb.indexOf("!"), sb.length());
                sb.delete(sb.lastIndexOf("/") + 1, sb.length());
            }
            try {
                url = new URL(sb.toString());
            } catch (MalformedURLException ex) {
                throw new UnsupportedOperationException(ex);
            }

            conn = url.openConnection();
            int hash = classpath.hashCode() ^ (int) conn.getLastModified();
            return hash;
        } catch (IOException ex) {
            throw new UnsupportedOperationException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.getInputStream().close();
                    conn.getOutputStream().close();
                } catch (IOException ex) { }
            }
        }
    }

    private static void setExtractionFolderToUserCache() {
        File extractFolderInHome = getJmeUserCacheFolder();

        if (!extractFolderInHome.exists()) {
            extractFolderInHome.mkdir();
        }

        extractionFolder = new File(extractFolderInHome, "natives_" + Integer.toHexString(computeNativesHash()));

        if (!extractionFolder.exists()) {
            extractionFolder.mkdir();
        }

        logger.log(Level.WARNING, "Working directory is not writable. "
                        + "Natives will be extracted to:\n{0}",
                extractionFolder);
    }

    public static File getExtractionFolder() {
        if (extractionFolderOverride != null) {
            return extractionFolderOverride;
        }
        if (extractionFolder == null) {
            File workingFolder = new File("").getAbsoluteFile();
            if (!workingFolder.canWrite()) {
                setExtractionFolderToUserCache();
            } else {
                try {
                    File file = new File(workingFolder + File.separator + ".jmetestwrite");
                    file.createNewFile();
                    file.delete();
                    extractionFolder = workingFolder;
                } catch (Exception e) {
                    setExtractionFolderToUserCache();
                }
            }
        }
        return extractionFolder;
    }

    /**
     * Removes platform-specific portions of a library file name so
     * that it can be accepted by {@link System#loadLibrary(java.lang.String) }.
     * <p>
     * E.g.<br>
     * <ul>
     * <li>jinput-dx8_64.dll => jinput-dx8_64</li>
     * <li>liblwjgl64.so => lwjgl64</li>
     * <li>libopenal.so => openal</li>
     * </ul>
     *
     * @param filename The filename to strip platform-specific parts
     * @return The stripped library name
     */
    private static String unmapLibraryName(String filename) {
        StringBuilder sb = new StringBuilder(filename);
        if (sb.indexOf("lib") == 0 && !filename.toLowerCase().endsWith(".dll")) {
            sb.delete(0, 3);
        }
        int dot = sb.lastIndexOf(".");
        if (dot > 0) {
            sb.delete(dot, sb.length());
        }
        return sb.toString();
    }

}
