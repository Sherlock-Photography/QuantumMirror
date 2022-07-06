package com.obsidium.bettermanual.model;

import android.os.Handler;
import android.os.Looper;

import com.obsidium.bettermanual.camera.CameraInstance;



public abstract class AbstractModel<T> implements Model<T> {

    private Events eventsListener;
    protected boolean supported;
    protected T value;
    protected T[] values;
    protected int value_int;
    protected CameraInstance camera;
    private Handler handler = new Handler(Looper.getMainLooper());

    public AbstractModel(CameraInstance camera)
    {
        this.camera = camera;
    }

    @Override
    public void setListener(Events events) {
        this.eventsListener = events;
    }

    protected void fireOnValueChanged()
    {
        if (eventsListener != null)
            handler.post(()-> eventsListener.onValueChanged());
    }

    protected void fireOnSupportedChanged()
    {
        if (eventsListener != null)
            handler.post(()-> eventsListener.onIsSupportedChanged());
    }
}
