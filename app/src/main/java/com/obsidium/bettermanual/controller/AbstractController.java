package com.obsidium.bettermanual.controller;

import android.view.View;

import com.obsidium.bettermanual.model.Model;

public abstract class AbstractController<V  extends View, M extends Model> implements Controller<V,M>, Model.Events {


    protected V view;
    protected M model;

    @Override
    public synchronized void bindView(V v) {
        this.view = v;
        onIsSupportedChanged();
    }

    @Override
    public synchronized void bindModel(M model) {
        this.model = model;
        if (model != null)
            model.setListener(this);
        onIsSupportedChanged();
    }

    @Override
    public void onIsSupportedChanged() {
        if (model != null){
            if (view != null)
                view.post(()->view.setVisibility(model.isSupported() ? View.VISIBLE : View.INVISIBLE));
        }
        else if (view != null)
            view.post(()->view.setVisibility(View.GONE));
    }

    @Override
    public void set_In_De_crase(int i) {
        if (model != null)
            model.setValue(i);
    }
}
