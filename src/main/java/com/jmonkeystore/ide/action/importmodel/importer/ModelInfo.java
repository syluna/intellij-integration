package com.jmonkeystore.ide.action.importmodel.importer;

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

import com.intellij.openapi.diagnostic.Logger;
import com.jme3.asset.AssetKey;
import com.jme3.asset.CloneableSmartAsset;
import com.jme3.asset.MaterialKey;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;

import java.io.File;
import java.util.*;

/**
 *  Inspected meta-data about a loaded model asset.
 *
 *  @author    Paul Speed
 */
public class ModelInfo {

    private static final Logger log = Logger.getInstance(ModelInfo.class);

    private File root;
    private String name;
    private Spatial model;
    private Map<CloneableSmartAsset, Dependency> dependencies = new HashMap<>();

    public ModelInfo( File root, String name, Spatial model ) {
        this.root = root;
        this.name = name;
        this.model = model;
        findDependencies(model);
    }

    /**
     *  Returns a collection containing all of the children (and their children)
     *  that match the specified name.  It uses a breadth first traversal
     *  such that items earlier in the results are higher in the tree.
     */
    public List<Spatial> findAll( String name ) {
        return findAll(name, Spatial.class);
    }

    /**
     *  Returns a collection containing all of the children (and their children)
     *  that match the specified name and type.  It uses a breadth first traversal
     *  such that items earlier in the results are higher in the tree.
     */
    public <T> List<T> findAll( final String name, final Class<T> type ) {
        // Check the type argument because from groovy scripts it's common
        // to pass the wrong node type
        if( !Spatial.class.isAssignableFrom(type) ) {
            throw new IllegalArgumentException("Type is not a Spatial compatible type:" + type);
        }
        final List<T> results = new ArrayList<>();
        model.breadthFirstTraversal(new SceneGraphVisitor() {
            public void visit( Spatial spatial ) {
                if( Objects.equals(name, spatial.getName()) && type.isInstance(spatial) ) {
                    results.add(type.cast(spatial));
                }
            }
        });
        return results;
    }

    /**
     *  Returns a collection containing all of the children (and their children)
     *  that match the type.  It uses a breadth first traversal
     *  such that items earlier in the results are higher in the tree.
     */
    public <T> List<T> findAll( final Class<T> type ) {
        // Check the type argument because from groovy scripts it's common
        // to pass the wrong node type
        if( !Spatial.class.isAssignableFrom(type) ) {
            throw new IllegalArgumentException("Type is not a Spatial compatible type:" + type);
        }
        final List<T> results = new ArrayList<>();
        model.breadthFirstTraversal(new SceneGraphVisitor() {
            public void visit( Spatial spatial ) {
                if( type.isInstance(spatial) ) {
                    results.add(type.cast(spatial));
                }
            }
        });
        return results;
    }

    /**
     *  Returns the first breadth-first-search result that matches the specified name.
     */
    public Spatial findFirst( String name ) {
        return findFirst(name, Spatial.class);
    }

    /**
     *  Returns the first breadth-first-search result that matches the specified name and type.
     */
    public <T> T findFirst( final String name, final Class<T> type ) {

        // Without a custom traverser, there is no way to early-out anyway...
        // so we'll just leverage the other method.
        List<T> results = findAll(name, type);
        if( results.isEmpty() ) {
            return null;
        }
        return results.get(0);
    }

    public Spatial getModelRoot() {
        return model;
    }

    public void setModelName( String name ) {
        this.name = name;
    }

    public String getModelName() {
        return name;
    }

    public Collection<Dependency> getDependencies() {
        return dependencies.values();
    }

    public Dependency getDependency( CloneableSmartAsset asset ) {
        return dependencies.get(asset);
    }

    public void generateMaterial(Material material, String assetName ) {
        log.debug("generateMaterial(" + material + ", " + assetName + ")");
        if( !assetName.toLowerCase().endsWith(".j3m") ) {
            assetName = assetName + ".j3m";
        }
        Dependency dep = addDependency(null, material);
        dep.setKey(new MaterialKey(assetName));
    }

    private void findDependencies( Spatial s ) {
        log.debug("findDependencies(" + s + ")");
        if( s instanceof Node) {
            Node n = (Node)s;
            for( Spatial child : n.getChildren() ) {
                findDependencies(child);
            }
        } else if( s instanceof Geometry) {
            findDependencies(((Geometry)s).getMaterial());
        }
    }

    private void findDependencies( Material m ) {
        log.debug("findDependencies(" + m + ")");
        if( m.getKey() != null ) {
            dependencies.put(m, new Dependency(root, m));
        }
        for( MatParam mp : m.getParams() ) {
            log.debug("Checking:" + mp);
            Object val = mp.getValue();
            if( !(val instanceof CloneableSmartAsset) ) {
                continue;
            }
            CloneableSmartAsset asset = (CloneableSmartAsset)val;
            log.debug("material asset:" + asset);
            if( asset.getKey() != null ) {
                addDependency(root, asset);
            }
        }
    }

    private Dependency addDependency( File root, CloneableSmartAsset asset ) {
        Dependency result = dependencies.get(asset);
        if( result == null ) {
            result = new Dependency(root, asset);
            dependencies.put(asset, result);
            return result;
        }

        // Else just add it to the existing
        result.instances.add(asset);
        return result;
    }

    public static class Dependency implements Comparable<Dependency> {
        private AssetKey key;
        private File file;
        private List<CloneableSmartAsset> instances = new ArrayList<>();

        public Dependency( File root, CloneableSmartAsset asset ) {
            instances.add(asset);
            this.key = asset.getKey();
            if( asset.getKey() != null ) {
                this.file = new File(root, asset.getKey().toString());
            }
        }

        public int compareTo( Dependency other ) {
            String s1 = key.toString();
            String s2 = other.getKey().toString();
            return s1.compareTo(s2);
        }

        public AssetKey getOriginalKey() {
            return key;
        }

        public void setKey( AssetKey key ) {
            for( CloneableSmartAsset asset : instances ) {
                asset.setKey(key);
            }
        }

        public AssetKey getKey() {
            return instances.get(0).getKey();
        }

        public File getSourceFile() {
            return file;
        }

        public List<CloneableSmartAsset> getInstances() {
            return instances;
        }

        @Override
        public String toString() {
            return "Dependency[file=" + file + ", key=" + key + "]";
        }
    }

}
