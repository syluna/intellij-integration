/*
 * $Id$
 *
 * Copyright (c) 2019, Simsilica, LLC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package com.jmonkeystore.ide.action.importmodel.importer;

import com.google.common.io.Files;
import com.intellij.openapi.diagnostic.Logger;
import com.jme3.asset.AssetKey;
import com.jme3.asset.CloneableSmartAsset;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.material.Material;
import com.jme3.material.plugin.export.material.J3MExporter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 *  Writes an asset to a target directory structure, copying or
 *  saving its dependencies as required.
 *
 *  @author    Paul Speed
 */
public class AssetWriter implements ModelProcessor {

    private static final Logger log = Logger.getInstance(AssetWriter.class);

    private static J3MExporter j3mExporter = new J3MExporter();

    private File target;
    private String assetPath;

    public AssetWriter() {
    }

    public void setTarget( File target ) {
        this.target = target;
    }

    public void setAssetPath( String path ) {
        this.assetPath = path;
    }

    protected String toTargetPath( AssetKey key ) {
        if( assetPath != null ) {
            return assetPath + "/" + key.toString();
        }
        return key.toString();
    }

    protected String toTargetPath( String path ) {
        if( assetPath != null ) {
            return assetPath + "/" + path;
        }
        return path;
    }

    @Override
    public void apply( ModelInfo info ) {
        try {
            write(info);
        } catch( IOException e ) {
            throw new RuntimeException("Error writing model:" + info.getModelName(), e);
        }
    }

    public void write( ModelInfo info ) throws IOException {

        // Write the real file dependencies first, rehoming their keys as necessary.
        for( ModelInfo.Dependency dep : info.getDependencies() ) {
            if( dep.getSourceFile() == null ) {
                // It's a generated asset
                continue;
            }
            AssetKey key = dep.getKey();

            String path = toTargetPath(key);
            File f = new File(target, path);
            f.getParentFile().mkdirs();

            if( dep.getSourceFile() != null ) {
                log.info("Copying:" + dep.getSourceFile() + " to:" + f);
                Files.copy(dep.getSourceFile(), f);
            }

            // Set the new target to the dependency's key so that when
            // we write out the .j3o it will know about the new location.
            AssetKey newKey = rehome(path, key);
            //log.info("...setting key to:" + newKey);
            dep.setKey(newKey);
        }

        // Then generate the generated assets after writing the real file
        // assets... because the generated assets may need the updated keys from
        // the first loop.
        for( ModelInfo.Dependency dep : info.getDependencies() ) {
            if( dep.getSourceFile() != null ) {
                // It's a real file asset
                continue;
            }
            AssetKey key = dep.getKey();

            String path = toTargetPath(key);
            File f = new File(target, path);
            f.getParentFile().mkdirs();
            generateDependency(f, dep);

            // Set the new target to the dependency's key so that when
            // we write out the .j3o it will know about the new location.
            AssetKey newKey = rehome(path, key);
            //log.info("...setting key to:" + newKey);
            dep.setKey(newKey);
        }

        // Write the j3o
        File outFile = new File(target, toTargetPath(info.getModelName() + ".j3o"));
        log.info("Writing:" + outFile);
        BinaryExporter.getInstance().save(info.getModelRoot(), outFile);
        System.out.println(outFile.getAbsolutePath());
    }

    protected void generateDependency( File file, ModelInfo.Dependency dep ) throws IOException {
        CloneableSmartAsset asset = dep.getInstances().get(0); // should always be at least one
        if( asset instanceof Material ) {
            writeJ3m(file, dep, (Material)asset);
        } else {
            throw new UnsupportedOperationException("Type not supported for generation:" + asset);
        }
    }

    protected void writeJ3m( File file, ModelInfo.Dependency dep, Material material ) throws IOException {
        log.info("Writing material:" + file);
        j3mExporter.save(material, file);
    }

    private static AssetKey rehome( String newPath, AssetKey key ) {
        try {
            // Let the asset key class do the path logic for us
            AssetKey temp = new AssetKey(newPath);

            // But let clone copy the real class and all of its other fields
            AssetKey result = key.clone();

            // Then use reflection to copy the path parts from temp to the new
            // key.
            // just brute force unoptimized for now.
            Field field;
            field = AssetKey.class.getDeclaredField("folder");
            field.setAccessible(true);
            field.set(result, field.get(temp));

            field = AssetKey.class.getDeclaredField("name");
            field.setAccessible(true);
            field.set(result, field.get(temp));

            field = AssetKey.class.getDeclaredField("extension");
            field.setAccessible(true);
            field.set(result, field.get(temp));

            return result;
        } catch( Exception e ) {
            throw new RuntimeException("Error rehoming key:" + key, e);
        }
    }
}