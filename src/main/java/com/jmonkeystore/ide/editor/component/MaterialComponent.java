package com.jmonkeystore.ide.editor.component;

import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Geometry;
import com.jme3.shader.VarType;
import com.jme3.util.ListMap;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class MaterialComponent extends Component {

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

                            // Vector3fComponent vector3fComponent = new Vector3fComponent();
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


        }

    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

}
