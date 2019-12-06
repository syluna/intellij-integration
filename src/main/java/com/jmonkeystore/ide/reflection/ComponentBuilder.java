package com.jmonkeystore.ide.reflection;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jmonkeystore.ide.editor.component.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentBuilder {

    private final Map<Class<?>, Class<? extends Component>> componentClasses = new HashMap<>();
    private final List<Component> components = new ArrayList<>();

    private final UniqueProperties props;

    public ComponentBuilder(UniqueProperties props) {

        this.props = props;

        // add the default supported components
        // these should be all the objects commonly used in jmonkey.

        componentClasses.put(boolean.class, BooleanComponent.class);
        componentClasses.put(ColorRGBA.class, ColorRGBAComponent.class);
        componentClasses.put(Enum.class, EnumComponent.class);
        componentClasses.put(float.class, FloatComponent.class);
        componentClasses.put(Quaternion.class, QuaternionComponent.class);
        componentClasses.put(Vector3f.class, Vector3fComponent.class);
        componentClasses.put(Vector4f.class, Vector4fComponent.class);

        componentClasses.put(Material.class, MaterialComponent.class);

    }

    public void registerComponent(Class<?> clazz, Class<? extends Component> component) {
        componentClasses.put(clazz, component);
    }

    public void build() {

        for (Method getter : props.getGetters()) {

            Map.Entry<Class<?>, Class<? extends Component>> entry = componentClasses.entrySet().stream()
                    .filter(c -> getter.getReturnType() == c.getKey() || ( getter.getReturnType().isEnum() && c.getKey() == Enum.class ) )
                    .findFirst()
                    .orElse(null);

            if (entry != null) {

                Method setter = props.getSetters().stream()
                        .filter(s -> s.getName().substring(3).equalsIgnoreCase(getter.getName().substring(3)))
                        .findFirst()
                        .orElse(null);

                try {

                    Class<? extends Component> componentClass = entry.getValue();
                    Constructor<? extends Component> constructor = componentClass.getConstructor(Object.class, Method.class, Method.class);
                    Component component = constructor.newInstance(props.getObject(), getter, setter);

                    component.setPropertyName(getter.getName().substring(3));

                    if (getter.getReturnType().isEnum()) {

                        Class<? extends Enum> values = (Class<? extends Enum>) getter.getReturnType();

                        EnumComponent enumComponent = (EnumComponent) component;
                        enumComponent.setEnumValues(values);
                    }


                    components.add(component);

                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                }

            }

        }

    }

    public List<Component> getComponents() {
        return components;
    }

}
