package com.jmonkeystore.ide.scene.explorer.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.LightProbeFactory;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.light.*;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.util.SkyFactory;
import com.jmonkeystore.ide.jme.JmeEngineService;
import com.jmonkeystore.ide.scene.explorer.SceneExplorerService;
import com.jmonkeystore.ide.scene.explorer.impl.dialog.NewLightProbeDialog;
import com.jmonkeystore.ide.scene.explorer.impl.dialog.NewSkyBoxDialog;
import com.jmonkeystore.ide.scene.explorer.impl.dialog.RenameSpatialDialog;
import com.jmonkeystore.ide.util.ProjectUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SceneTreeContextMenuListener implements MouseListener {

    private final Tree tree;
    private final DefaultMutableTreeNode treeRoot;

    // registered stuff we want to provide context menus for.
    // nodes
    private Set<Class<? extends Node>> registeredNodes = new HashSet<>();

    // private MenuElement[] spatialMenuItems;

    // private JPopupMenu lightPopupMenu;
    // private JPopupMenu nodePopupMenu;
    // private JPopupMenu geomPopupMenu;

    private DefaultMutableTreeNode clickedNode;

    SceneTreeContextMenuListener(Tree tree, DefaultMutableTreeNode treeRoot) {
        this.tree = tree;
        this.treeRoot = treeRoot;

        // this.spatialMenuItems = createSpatialMenuItems();

        // this.lightPopupMenu = createLightPopupMenu();
        // this.nodePopupMenu = createNodePopupMenu();
        // this.geomPopupMenu = createGeomPopupMenu();
    }

    public void registerNode(Class<? extends Node> nodeClass) {
        registeredNodes.add(nodeClass);
    }

    public void registerNodes(List<Class<? extends Node>> nodes) {
        registeredNodes.addAll(nodes);
    }

    public void clearRegisteredNodes() {
        registeredNodes.clear();
    }

    // creates menu items that all spatials have.
    private MenuElement[] createSpatialMenuItems() {

        JMenuItem renameItem = new JMenuItem("Rename...");
        renameItem.addActionListener(e -> {
            Spatial spatial = (Spatial) clickedNode.getUserObject();
            RenameSpatialDialog dialog = new RenameSpatialDialog(tree, spatial);

            if (dialog.showAndGet()) {
                spatial.setName(dialog.getChosenName());
                ServiceManager.getService(SceneExplorerService.class).refreshScene();
            }
        });

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {

            Spatial spatial = (Spatial) clickedNode.getUserObject();
            ServiceManager.getService(JmeEngineService.class).enqueue(() -> {
                spatial.removeFromParent();
                ServiceManager.getService(SceneExplorerService.class).refreshScene();
            });
        });

        // Lights
        JMenu lightsMenuItem = new JMenu("Light...");

        // ambient light
        JMenuItem ambLightMenuItem = new JMenuItem("Ambient Light");
        ambLightMenuItem.addActionListener(e -> {
            Spatial spatial = (Spatial) clickedNode.getUserObject();
            spatial.addLight(new AmbientLight());
            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        lightsMenuItem.add(ambLightMenuItem);

        // directional light
        JMenuItem dirLightMenuItem = new JMenuItem("Directional Light");
        dirLightMenuItem.addActionListener(e -> {
            Spatial spatial = (Spatial) clickedNode.getUserObject();
            spatial.addLight(new DirectionalLight());
            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        lightsMenuItem.add(dirLightMenuItem);

        // point light
        JMenuItem pointLightMenuItem = new JMenuItem("Point Light");
        pointLightMenuItem.addActionListener(e -> {
            Spatial spatial = (Spatial) clickedNode.getUserObject();
            spatial.addLight(new PointLight());
            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        lightsMenuItem.add(pointLightMenuItem);

        // spot light
        JMenuItem spotLightMenuItem = new JMenuItem("Spot Light");
        spotLightMenuItem.addActionListener(e -> {
            Spatial spatial = (Spatial) clickedNode.getUserObject();
            spatial.addLight(new SpotLight());
            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        lightsMenuItem.add(spotLightMenuItem);

        // light probe
        JMenuItem probeLightMenuItem = new JMenuItem("Light Probe");
        probeLightMenuItem.addActionListener(e -> {

            Spatial scene = (Spatial) treeRoot.getUserObject();
            NewLightProbeDialog dialog = new NewLightProbeDialog(tree, scene);

            boolean makeProbe = dialog.showAndGet();

            if (makeProbe && dialog.getSelectedSpatial() != null) {

                Spatial clickedSpatial = (Spatial) clickedNode.getUserObject();

                Spatial spatial = dialog.getSelectedSpatial();
                EnvironmentCamera environmentCamera = ServiceManager.getService(JmeEngineService.class).getStateManager().getState(EnvironmentCamera.class);

                LightProbeFactory.makeProbe(environmentCamera, spatial, new JobProgressAdapter<LightProbe>() {

                    @Override
                    public void progress(double value) {

                    }

                    @Override
                    public void done(LightProbe result) {
                        result.setAreaType(dialog.getSelectedAreaType());
                        result.getArea().setRadius(dialog.getRadius());

                        clickedSpatial.addLight(result);

                        EventQueue.invokeLater(() -> ServiceManager.getService(SceneExplorerService.class).refreshScene());

                    }
                });
            }

        });
        lightsMenuItem.add(probeLightMenuItem);

        return new MenuElement[] {
                renameItem,
                deleteItem,
                lightsMenuItem
        };
    }

    private void addElements(MenuElement[] elemts, JPopupMenu menu) {

        for (MenuElement menuElement : elemts) {

            if (menuElement instanceof JMenu) {
                menu.add((JMenu)menuElement);
            }
            if (menuElement instanceof JMenuItem) {
                menu.add((JMenuItem)menuElement);
            }
        }
    }

    private JPopupMenu createLightPopupMenu() {
        JPopupMenu menu = new JPopupMenu("Light Menu");

        JMenuItem removeItem = new JMenuItem("Remove");
        removeItem.addActionListener(e -> {
            Light light = (Light) clickedNode.getUserObject();

            // call getParent twice because the first parent is just a placeholder for all lights
            DefaultMutableTreeNode parentTree = (DefaultMutableTreeNode) clickedNode.getParent().getParent();
            Spatial parent = (Spatial) parentTree.getUserObject();
            parent.removeLight(light);

            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        menu.add(removeItem);

        return menu;
    }

    private JPopupMenu createGeomPopupMenu() {

        JPopupMenu menu = new JPopupMenu("Geometry Menu");
        addElements(createSpatialMenuItems(), menu);

        return menu;
    }

    private JPopupMenu createNodePopupMenu() {
        JPopupMenu menu = new JPopupMenu("Node Menu");

        addElements(createSpatialMenuItems(), menu);
        menu.add(new JSeparator());

        JMenuItem addNodeItem = new JMenuItem("Add Node");
        addNodeItem.addActionListener(e -> {
            Node node = (Node) clickedNode.getUserObject();
            node.attachChild(new Node());
            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        menu.add(addNodeItem);

        JMenuItem skyControlMenuItem = new JMenuItem("SkyBox");
        skyControlMenuItem.addActionListener(e -> {

            Project project = ProjectUtils.getActiveProject();

            NewSkyBoxDialog newSkyBoxDialog = new NewSkyBoxDialog(project);
            boolean proceed = newSkyBoxDialog.showAndGet();

            if (proceed && newSkyBoxDialog.getSelectedFile() != null) {
                Node node = (Node) clickedNode.getUserObject();

                VirtualFile selectedFile = newSkyBoxDialog.getSelectedFile();

                // this relies on the fact that we have a "./src/main/resources/" dir.. This may break.
                VirtualFile resourcePath = Arrays.stream(ProjectRootManager.getInstance(project).getContentSourceRoots())
                        .filter(virtualFile -> virtualFile.getPath().endsWith("src/main/resources"))
                        .findFirst()
                        .orElse(null);

                String image = selectedFile.getUrl().replace(resourcePath.getUrl(), "");
                AssetManager assetManager = ServiceManager.getService(JmeEngineService.class).getAssetManager();

                Node skyNode = new Node("Sky Node");
                Spatial sky = SkyFactory.createSky(assetManager, image, SkyFactory.EnvMapType.EquirectMap);
                skyNode.attachChild(sky);

                ServiceManager.getService(JmeEngineService.class).enqueue(() -> {
                    node.attachChild(skyNode);
                    EventQueue.invokeLater(() -> ServiceManager.getService(SceneExplorerService.class).refreshScene());
                });

            }

        });
        menu.add(skyControlMenuItem);

        JMenu geomsMenu = new JMenu("Geometry");
        JMenuItem boxMenuItem = new JMenuItem("Box");
        boxMenuItem.addActionListener(actionEvent -> {
            Node node = (Node) clickedNode.getUserObject();

            AssetManager assetManager = ServiceManager.getService(JmeEngineService.class).getAssetManager();

            Box box = new Box(1,1,1);
            Geometry geometry = new Geometry("Box", box);
            geometry.setMaterial(new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"));

            node.attachChild(geometry);
            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        geomsMenu.add(boxMenuItem);
        menu.add(geomsMenu);

        JMenuItem particleEmitterMenuItem = new JMenuItem("Particle Emitter");
        particleEmitterMenuItem.addActionListener(e -> {

            AssetManager assetManager = ServiceManager.getService(JmeEngineService.class).getAssetManager();

            ParticleEmitter particleEmitter = new ParticleEmitter("New ParticleEmitter", ParticleMesh.Type.Triangle, 30);

            Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
            mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));

            particleEmitter.setMaterial(mat_red);
            particleEmitter.setImagesX(2);
            particleEmitter.setImagesY(2); // 2x2 texture animation
            particleEmitter.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
            particleEmitter.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
            particleEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
            particleEmitter.setStartSize(1.5f);
            particleEmitter.setEndSize(0.1f);
            particleEmitter.setGravity(0, 0, 0);
            particleEmitter.setLowLife(1f);
            particleEmitter.setHighLife(3f);
            particleEmitter.getParticleInfluencer().setVelocityVariation(0.3f);

            Node node = (Node) clickedNode.getUserObject();
            node.attachChild(particleEmitter);
            ServiceManager.getService(SceneExplorerService.class).refreshScene();
        });
        menu.add(particleEmitterMenuItem);

        // add registered nodes
        for (Class<? extends Node> nodeClass : registeredNodes) {

            String name = nodeClass.getSimpleName();
            JMenuItem nodeItem = new JMenuItem(name);

            nodeItem.addActionListener(e -> {
                try {
                    Constructor<? extends Node> constructor = nodeClass.getConstructor();
                    Node newNode = constructor.newInstance();

                    Node node = (Node) clickedNode.getUserObject();
                    node.attachChild(newNode);
                    ServiceManager.getService(SceneExplorerService.class).refreshScene();

                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            });

            menu.add(nodeItem);
        }

        return menu;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {

            JTree tree = (JTree) e.getComponent();
            clickedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            JPopupMenu popupMenu = null;

            if (clickedNode.getUserObject() instanceof Node) {
                popupMenu = createNodePopupMenu();
            }
            else if (clickedNode.getUserObject() instanceof Light) {
                popupMenu = createLightPopupMenu();
            }
            else if (clickedNode.getUserObject() instanceof Geometry) {
                popupMenu = createGeomPopupMenu();
            }
            if (popupMenu != null) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }

        }
    }

    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
}
