package com.jmonkeystore.ide.reflection;

import com.intellij.openapi.components.ServiceManager;
import com.jmonkeystore.ide.editor.component.ValuedComponent;
import com.jmonkeystore.ide.jme.JmeEngineService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectedProperty {

    private final Object parent;

    private final Method getter;
    private final Method setter;
    private final ValuedComponent component;

    public ReflectedProperty(Object parent, Method getter, Method setter, ValuedComponent component) {
        this.parent = parent;
        this.getter = getter;
        this.setter = setter;
        this.component = component;

        component.setPropertyChangedEvent(value -> {
            ServiceManager.getService(JmeEngineService.class).enqueue(() -> {
                try {
                    setter.invoke(parent, value);

                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        });

    }

    public void update() {
        try {
            Object value = getter.invoke(parent);
            this.component.setValue(value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setValue(Object value) {
        try {
            setter.invoke(parent, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Object getValue() {
        try {
            return getter.invoke(parent);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

}
