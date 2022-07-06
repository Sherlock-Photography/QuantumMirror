package com.obsidium.bettermanual.controller;

import android.util.Log;
import android.view.View;

import com.obsidium.bettermanual.ActivityInterface;
import com.obsidium.bettermanual.MainActivity;
import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.model.ExposureModeModel;
import com.obsidium.bettermanual.model.ShutterModel;
import com.sony.scalar.hardware.CameraEx;

public class ShutterController extends TextViewController<ShutterModel> {

    private final String TAG = ShutterController.class.getSimpleName();

    public interface ShutterSpeedEvent
    {
        void onChanged();
    }

    private static ShutterController shutterController = new ShutterController();

    public static ShutterController GetInstance()
    {
        return shutterController;
    }

    private ShutterSpeedEvent shutterSpeedEventListener;
    private ActivityInterface activityInterface;

    public void bindActivityInterface(ActivityInterface activityInterface)
    {
        this.activityInterface = activityInterface;
    }


    @Override
    public void toggle() {
        if (ExposureModeController.GetInstance().getExposureMode() == ExposureModeModel.ExposureModes.aperture && !model.getValue().equals("BULB") && activityInterface != null)
            activityInterface.loadFragment(MainActivity.FRAGMENT_MIN_SHUTTER);
    }

    @Override
    public int getNavigationHelpID() {
        return R.string.view_drivemode_navigation;
    }

    public CameraEx.ShutterSpeedInfo getShutterSpeedInfo()
    {
        if (model != null)
            return model.getShutterSpeedInfo();
        return null;
    }

    public void setShutterSpeedEventListener(ShutterSpeedEvent eventListener)
    {
        this.shutterSpeedEventListener = eventListener;
    }

    @Override
    public void onValueChanged() {
        Log.d(TAG, "onValueChanged()");
        super.onValueChanged();
        if (shutterSpeedEventListener != null)
            shutterSpeedEventListener.onChanged();
        if (view != null && view.getVisibility() == View.GONE && model != null)
            view.setVisibility(View.VISIBLE);
    }
}
