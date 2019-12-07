package com.jmonkeystore.ide.reflection;

import org.reflections.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UniqueProperties {

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

        Set<Method> getters = ReflectionUtils.getAllMethods(object.getClass(),
                org.reflections.ReflectionUtils.withModifier(Modifier.PUBLIC), ReflectionUtils.withPrefix("get"));

        Set<Method> setters = ReflectionUtils.getAllMethods(object.getClass(),
                org.reflections.ReflectionUtils.withModifier(Modifier.PUBLIC), ReflectionUtils.withPrefix("set"));

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
