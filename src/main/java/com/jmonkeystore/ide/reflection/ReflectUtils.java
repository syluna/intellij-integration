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
import java.lang.reflect.Modifier;
import java.util.*;

public class ReflectUtils {

    public static class UniqueProperties {

        private Object object;
        private List<Method> getters = new ArrayList<>();
        private List<Method> setters = new ArrayList<>();

        public UniqueProperties(Object object) {
            this.object = object;
            create();
        }


        public Object getObject() {
            return object;
        }

        public List<Method> getGetters() {
            return getters;
        }

        public List<Method> getSetters() {
            return setters;
        }

        private void create() {

            Set<Method> getters = org.reflections.ReflectionUtils.getAllMethods(object.getClass(),
                    org.reflections.ReflectionUtils.withModifier(Modifier.PUBLIC), org.reflections.ReflectionUtils.withPrefix("get"));

            Set<Method> setters = org.reflections.ReflectionUtils.getAllMethods(object.getClass(),
                    org.reflections.ReflectionUtils.withModifier(Modifier.PUBLIC), org.reflections.ReflectionUtils.withPrefix("set"));

            // remove all getters that have no matching setters
            getters.removeIf(getter -> {

                String suffix = getter.getName().substring(3).toLowerCase();

                Method setMethod = setters.stream().filter(m -> m.getName().substring(3).toLowerCase().equals(suffix))
                        .findFirst()
                        .orElse(null);

                return setMethod == null;

            });

            // remove all setters whose parameter is not the same as the getter return type.
            // e.g. getLocalScale returns Vector3f, setLocalScale must accept Vector3f as parameter.
            setters.removeIf(setter -> {

                String suffix = setter.getName().substring(3).toLowerCase();

                Method getter = getters.stream().filter(g -> g.getName().substring(3).toLowerCase().equals(suffix))
                        .findFirst()
                        .orElse(null);

                if (getter == null) {
                    return true;
                }

                Class<?>[] params = setter.getParameterTypes();

                if (setter.getParameterCount() > 1) {
                    return true;
                }

                boolean same =  params[0].isAssignableFrom(getter.getReturnType());
                return !same;

            });

            // AGAIN remove all getters that have no matching setters
            // we may have removed some setters that don't conform, so we need to remove those getters we can't deal with.
            getters.removeIf(getter -> {

                String suffix = getter.getName().substring(3).toLowerCase();

                Method setMethod = setters.stream().filter(m -> m.getName().substring(3).toLowerCase().equals(suffix))
                        .findFirst()
                        .orElse(null);

                return setMethod == null;

            });

            this.getters.addAll(getters);
            this.setters.addAll(setters);

            // this.getters.sort(Comparator.comparing(Method::getName));


        }

    }

    public static class ComponentBuilder {

        private List<Component> components = new ArrayList<>();

        public ComponentBuilder(UniqueProperties props) {

            Map<Class, Class<? extends Component>> componentClasses = new HashMap<>();
            componentClasses.put(boolean.class, BooleanComponent.class);
            componentClasses.put(ColorRGBA.class, ColorRGBAComponent.class);
            componentClasses.put(Enum.class, EnumComponent.class);
            componentClasses.put(float.class, FloatComponent.class);
            componentClasses.put(Quaternion.class, QuaternionComponent.class);
            componentClasses.put(Vector3f.class, Vector3fComponent.class);
            componentClasses.put(Vector4f.class, Vector4fComponent.class);
            componentClasses.put(Material.class, MaterialComponent.class);

            for (Method getter : props.getGetters()) {

                Map.Entry<Class, Class<? extends Component>> entry = componentClasses.entrySet().stream()
                        .filter(c -> getter.getReturnType() == c.getKey() || ( getter.getReturnType().isEnum() && c.getKey() == Enum.class ) )
                        .findFirst()
                        .orElse(null);

                if (entry != null) {

                    Method setter = props.getSetters().stream()
                            .filter(s -> s.getName().substring(3).equalsIgnoreCase(getter.getName().substring(3)))
                            .findFirst()
                            .orElse(null);

                    try {
                        Constructor constructor = entry.getValue().getConstructor(Object.class, Method.class, Method.class);
                        Component component = (Component) constructor.newInstance(props.getObject(), getter, setter);

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

}
