package com.jmonkeystore.ide.editor.component;

import com.jme3.material.MatParam;
import com.jme3.material.Material;
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

            }
            else if (varyType == VarType.Vector4) {

            }
            else if (varyType == VarType.Boolean) {

            }


        }

    }

    @Override
    public JComponent getJComponent() {
        return contentPanel;
    }

}
