package com.jmonkeystore.ide.editor.component;

public interface ValuedComponent {

    void setValue(Object value);

    PropertyChangedEvent getPropertyChangedEvent();
    void setPropertyChangedEvent(PropertyChangedEvent event);

}
