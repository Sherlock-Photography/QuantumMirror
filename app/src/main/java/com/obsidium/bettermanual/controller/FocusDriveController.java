package com.obsidium.bettermanual.controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.model.FocusDriveModel;
import com.obsidium.bettermanual.views.FocusScaleView;


public class FocusDriveController extends AbstractController<View,FocusDriveModel> {

    private static FocusDriveController focusDriveController = new FocusDriveController();

    public static FocusDriveController GetInstance() {
        return focusDriveController;
    }

    private FocusScaleView focusScaleView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private FocusPostionChangedEvent focusPostionChangedEvent;
    private String TAG = FocusDriveController.class.getSimpleName();

    public interface FocusPostionChangedEvent
    {
        void onFocusPostionChanged();
    }

    @Override
    public void bindView(View view) {
        super.bindView(view);
        if (view != null)
            focusScaleView =(FocusScaleView) view.findViewById(R.id.vFocusScale);
        else
            focusScaleView = null;
    }

    @Override
    public void toggle() {

    }

    @Override
    public void setColorToView(Integer color) {

    }

    @Override
    public int getNavigationHelpID() {
        return R.string.view_drivemode_navigation;
    }

    @Override
    public void onValueChanged() {

        Log.d(TAG,"OnFocusPositionChanged");
        view.setVisibility(View.VISIBLE);
        focusScaleView.setCurPosition(model.getValue());
        focusScaleView.setMaxPosition(model.getMaxPosition());
        handler.removeCallbacks(m_hideFocusScaleRunnable);
        handler.postDelayed(m_hideFocusScaleRunnable, 2000);
        if (focusPostionChangedEvent != null)
            focusPostionChangedEvent.onFocusPostionChanged();
    }

    public int getFocusPosition()
    {
        if (model == null)
            return 0;
        return model.getValue();
    }

    public int getFocusMaxPosition()
    {
        if (model == null)
            return 0;
        return model.getMaxPosition();
    }

    public void setFocusPostionChangedEventListener(FocusPostionChangedEvent eventListener)
    {
        this.focusPostionChangedEvent = eventListener;
    }

    private final Runnable m_hideFocusScaleRunnable = () -> view.setVisibility(View.GONE);
}
