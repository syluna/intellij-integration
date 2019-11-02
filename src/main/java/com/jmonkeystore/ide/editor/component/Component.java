package com.jmonkeystore.ide.editor.component;

import com.jmonkeystore.ide.reflection.ReflectedProperty;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Component implements ValuedComponent, NamedComponent {

    private String propertyName = "";

    private ReflectedProperty reflectedProperty;
    private PropertyChangedEvent propertyChangedEvent;

    private final AtomicBoolean isBound = new AtomicBoolean(false);

    public Component(Object parent, Method getter, Method setter) {
        if (parent != null) {
            this.reflectedProperty = new ReflectedProperty(parent, getter, setter, this);
        }

    }

    public ReflectedProperty getReflectedProperty() {
        return reflectedProperty;
    }

    public void setReflectedProperty(ReflectedProperty reflectedProperty) {
        this.reflectedProperty = reflectedProperty;
    }

    protected boolean isBinded() {
        return isBound.get();
    }

    protected void setBound() {
        bind();

    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public void setValue(Object value) {
        if (this.propertyChangedEvent != null) {
            this.propertyChangedEvent.propertyChanged(value);
        }
    }

    @Override
    public PropertyChangedEvent getPropertyChangedEvent() {
        return this.propertyChangedEvent;
    }

    @Override
    public void setPropertyChangedEvent(PropertyChangedEvent event) {
        this.propertyChangedEvent = event;
    }

    public abstract JComponent getJComponent();

    /**
     * Internal use only.
     * Binds the input components to the object. Called AFTER the values have been set to avoid cyclic issues.
     */
    public void bind() {
        isBound.set(true);
    };
}
