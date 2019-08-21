package com.jmonkeystore.ide.jme.sky;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Caps;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jmonkeystore.ide.jme.JmeEngineService;

public enum SkyLoader {

    BRIGHT("Bright"),
    CLEAR_BLUE("Clear Blue"),
    PATH("Path"),
    ST_PETERS("St Peters"),
    LAGOON("Lagoon");

    private final String friendlyName;
    SkyLoader(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public Spatial load() {

        JmeEngineService engineService = ServiceManager.getService(JmeEngineService.class);
        AssetManager assetManager = engineService.getAssetManager();

        switch (this) {
            case BRIGHT: return SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap);
            case CLEAR_BLUE: return SkyFactory.createSky(assetManager, "Textures/Sky/Bright/FullskiesBlueClear03.dds", SkyFactory.EnvMapType.CubeMap);
            case PATH: return SkyFactory.createSky(assetManager, "Textures/Sky/Path.hdr", SkyFactory.EnvMapType.EquirectMap);

            case ST_PETERS: {

                Texture envMap;

                if (engineService.getRenderer().getCaps().contains(Caps.FloatTexture)) {
                    envMap = assetManager.loadTexture("Textures/Sky/St Peters/StPeters.hdr");
                }
                else {
                    envMap = assetManager.loadTexture("Textures/Sky/St Peters/StPeters.jpg");
                }

                return SkyFactory.createSky(assetManager, envMap, new Vector3f(-1f, -1f, -1f), SkyFactory.EnvMapType.SphereMap);
            }

            case LAGOON: {
                Texture west = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_west.jpg");
                Texture east = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_east.jpg");
                Texture north = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_north.jpg");
                Texture south = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_south.jpg");
                Texture up = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_up.jpg");
                Texture down = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg");

                return SkyFactory.createSky(assetManager, west, east, north, south, up, down);
            }

            default: throw new AssetNotFoundException("Unsupported sky specified.");
        }
    }

    public static String[] getFriendlyNames() {
        String[] names = new String[values().length];

        for (int i = 0; i < values().length; i++) {
            names[i] = values()[i].friendlyName;
        }

        return names;
    }

    public static SkyLoader fromFriendlyName(String value) {
        for (int i = 0; i < values().length; i++) {
            if (values()[i].getFriendlyName().equalsIgnoreCase(value)) {
                return values()[i];
            }
        }

        return null;
    }

}
