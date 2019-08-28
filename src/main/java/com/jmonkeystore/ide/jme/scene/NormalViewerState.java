package com.jmonkeystore.ide.jme.scene;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.util.TangentBinormalGenerator;

import java.util.ArrayList;
import java.util.List;

public class NormalViewerState extends BaseAppState {

    private final List<Geometry> geometryList = new ArrayList<>();
    private final List<Geometry> normalList = new ArrayList<>();

    private Material normalMaterial;

    private Spatial focus;

    public NormalViewerState() {

    }

    public void focus(Spatial spatial) {
        clearLastFrame();
        this.focus = spatial;
    }

    @Override
    protected void initialize(Application app) {
        normalMaterial = app.getAssetManager().loadMaterial("Common/Materials/VertexColor.j3m");
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
        clearLastFrame();
    }

    private void clearLastFrame() {
        geometryList.clear();

        for (Geometry normalGeom : normalList) {
            normalGeom.removeFromParent();
        }

        normalList.clear();
    }

    private final SceneGraphVisitor visitor = spatial -> {
        if (spatial instanceof Geometry) {
            geometryList.add((Geometry) spatial);
        }
    };

    @Override
    public void update(float tpf) {

        if (focus == null) {
            return;
        }

        geometryList.clear();

        for (Geometry normalGeom : normalList) {
            normalGeom.removeFromParent();
        }

        normalList.clear();

        focus.breadthFirstTraversal(visitor);

        for (Geometry geom : geometryList) {

            Mesh normalMesh = TangentBinormalGenerator.genNormalLines(geom.getMesh(), 0.08f);

            Geometry normalGeom = new Geometry("Normal Debug", normalMesh);
            normalGeom.setMaterial(normalMaterial);
            normalGeom.setCullHint(Spatial.CullHint.Never);
            normalGeom.setLocalTranslation(geom.getLocalTranslation());
            normalGeom.setLocalRotation(geom.getLocalRotation());

            geom.getParent().attachChild(normalGeom);

            normalList.add(normalGeom);
        }

    }

}
