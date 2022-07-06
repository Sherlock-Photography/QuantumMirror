package com.obsidium.bettermanual.model;

public interface Model<T> {
    void setValue(int i);
    T getValue();
    boolean isSupported();
    void setListener(Events events);

    public interface Events
    {
        void onValueChanged();
        void onIsSupportedChanged();
    }
}
