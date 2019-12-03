package com.jmonkeystore.ide.jme.impl;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.system.awt.AwtPanel;
import com.jme3.system.awt.PaintMode;

import java.awt.*;

public class JmePanel extends AwtPanel {

    public static final String ROOTNODE_PREFIX = "IJ ROOTNODE: ";

    private final String name;

    private Camera camera;
    private ViewPort viewPort;
    private Node rootNode;

    JmePanel(String name, RenderManager renderManager) {
        super(PaintMode.Accelerated);

        this.name = name;

        camera = new Camera(640, 480);

        float aspectRatio = (float)camera.getWidth() / (float)camera.getHeight();
        camera.setFrustumPerspective( 45, aspectRatio, 0.1f, 10000f);

        camera.lookAtDirection(new Vector3f(0, 0, -1), Vector3f.UNIT_Y);
        camera.setLocation(new Vector3f(0, 0, 15));
        viewPort = new ViewPort("Offscreen View", camera);
        viewPort.setClearFlags(true, true, true);

        rootNode = new Node(ROOTNODE_PREFIX + name);

        // rootNode.updateLogicalState(0);
        // rootNode.updateGeometricState();
        viewPort.attachScene(rootNode);

        setPreferredSize(new Dimension(camera.getWidth(), camera.getHeight()));

        attachTo(false, viewPort);
        initialize(renderManager, viewPort);
    }

    @Override
    public void setSize(Dimension dimension) {
        super.setSize(dimension);
        camera.resize(dimension.width, dimension.height, true);
    }

    @Override
    public void setPreferredSize(Dimension dimension) {
        super.setPreferredSize(dimension);
        camera.resize(dimension.width, dimension.height, true);
    }

    @Override
    public String getName() {
        return name;
    }

    public Camera getCamera() {
        return camera;
    }

    public ViewPort getViewPort() {
        return viewPort;
    }

    public Node getRootNode() {
        return rootNode;
    }

    @Override
    public void preFrame(float tpf) {
        rootNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
    }

    public void cleanup() {

        // viewPort.clearProcessors();

        while (rootNode.getLocalLightList().size() > 0) {
            rootNode.removeLight(rootNode.getLocalLightList().get(0));
        }

        rootNode.detachAllChildren();
    }

}
