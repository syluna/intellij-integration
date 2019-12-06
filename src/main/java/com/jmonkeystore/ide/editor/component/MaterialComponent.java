package com.jmonkeystore.ide.editor.component;

import com.intellij.openapi.components.ServiceManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Geometry;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture2D;
import com.jme3.util.ListMap;
import com.jmonkeystore.ide.jme.JmeEngineService;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class MaterialComponent extends Component {

    // private static final Logger log = Logger.getInstance(MaterialComponent.class);

    private JPanel contentPanel;

    public MaterialComponent(Object parent, Method getter, Method setter) {
        super(parent, getter, setter);

        contentPanel.setLayout(new VerticalLayout());

        // Material material = (Material) parent;
        Geometry geometry = (Geometry) parent;
        Material material = geometry.getMaterial();

        // a list of all possible params
        Collection<MatParam> params = material.getMaterialDef().getMaterialParams();
        List<MatParam> allParams = new ArrayList<>(params);

        allParams.sort(Comparator.comparing(MatParam::getName));

        // a list of params that have been set (either default or by the user).
        ListMap<String, MatParam> setParams = material.getParamsMap();

        for (MatParam matParam : allParams) {

            VarType varyType = matParam.getVarType();

            if (varyType == VarType.Float) {

                setParams.entrySet().stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase(matParam.getName()))
                        .findFirst()
                        .ifPresent(setParam -> {

                            FloatComponent floatComponent = new FloatComponent();
                            floatComponent.setPropertyName(matParam.getName());

                            float fVal = (float) setParam.getValue().getValue();
                            floatComponent.setValue(fVal);

                            floatComponent.setPropertyChangedEvent(value -> {
                                float val = (float)value;
                                material.setFloat(matParam.getName(), val);
                            });

                            contentPanel.add(floatComponent.getJComponent());
                        });

            }
            else if (varyType == VarType.Vector2) {



            }
            else if (varyType == VarType.Vector3) {

                setParams.entrySet().stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase(matParam.getName()))
                        .findFirst()
                        .ifPresent(setParam -> {

                            Vector3fComponent vector3fComponent = new Vector3fComponent();
                            vector3fComponent.setPropertyName(matParam.getName());

                            Vector3f fVal = (Vector3f) setParam.getValue().getValue();
                            vector3fComponent.setValue(fVal);

                            vector3fComponent.setPropertyChangedEvent(value -> {
                                Vector3f val = (Vector3f)value;
                                material.setVector3(matParam.getName(), val);
                            });

                            contentPanel.add(vector3fComponent.getJComponent());
                        });

            }
            else if (varyType == VarType.Vector4) {

                // a vector4 could also be a ColorRGBA.

                setParams.entrySet().stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase(matParam.getName()))
                        .findFirst()
                        .ifPresent(setParam -> {

                            if (setParam.getValue().getValue() instanceof Vector4f) {


                                Vector4fComponent vector4fComponent = new Vector4fComponent();
                                vector4fComponent.setPropertyName(matParam.getName());

                                Vector4f fVal = (Vector4f) setParam.getValue().getValue();
                                vector4fComponent.setValue(fVal);

                                vector4fComponent.setPropertyChangedEvent(value -> {
                                    Vector4f val = (Vector4f) value;
                                    material.setVector4(matParam.getName(), val);
                                });

                                contentPanel.add(vector4fComponent.getJComponent());
                            }

                            else if (setParam.getValue().getValue() instanceof ColorRGBA) {

                                ColorRGBAComponent colorRGBAComponent = new ColorRGBAComponent();
                                colorRGBAComponent.setPropertyName(matParam.getName());

                                ColorRGBA fVal = (ColorRGBA) setParam.getValue().getValue();
                                colorRGBAComponent.setValue(fVal);

                                colorRGBAComponent.setPropertyChangedEvent(value -> {
                                    ColorRGBA val = (ColorRGBA) value;
                                    material.setColor(matParam.getName(), val);
                                });

                                contentPanel.add(colorRGBAComponent.getJComponent());

                            }

                        });

            }
            else if (varyType == VarType.Boolean) {

                setParams.entrySet().stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase(matParam.getName()))
                        .findFirst()
                        .ifPresent(setParam -> {

                            BooleanComponent booleanComponent = new BooleanComponent();
                            booleanComponent.setPropertyName(matParam.getName());

                            boolean fVal = (boolean) setParam.getValue().getValue();
                            booleanComponent.setValue(fVal);

                            booleanComponent.setPropertyChangedEvent(value -> {
                                boolean val = (boolean)value;
                                material.setBoolean(matParam.getName(), val);
                            });

                            contentPanel.add(booleanComponent.getJComponent());
                        });

            }

            else if (varyType == VarType.Texture2D) {

                setParams.entrySet().stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase(matParam.getName()))
                        .findFirst()
                        .ifPresent(setParam -> {

                            Texture2DComponent texture2DComponent = new Texture2DComponent();
                            texture2DComponent.setPropertyName(matParam.getName());

                            Texture2D fVal = (Texture2D) setParam.getValue().getValue();
                            texture2DComponent.setValue(fVal);

                            texture2DComponent.setPropertyChangedEvent(value -> {
                                // Texture2D val = (Texture2D)value;

                                String val = (String) value;

                                // Texture2D texture2D = ServiceManager.getService(JmeEngineService.class).getExternalAssetLoader().load(val, Texture2D.class);
                                try {
                                    Texture2D texture2D = (Texture2D) ServiceManager.getService(JmeEngineService.class).getAssetManager().loadTexture(val);
                                    material.setTexture(matParam.getName(), texture2D);
                                }
                                catch (AssetNotFoundException ex) {
                                    // do nothing.
                                    System.out.println("Texture2D Not Found: " + val);
                                    // log.info("Texture2D Not Found: " + val);
                                }

                            });

                            contentPanel.add(texture2DComponent.getJComponent());
                        });

            }

        }

    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

}
