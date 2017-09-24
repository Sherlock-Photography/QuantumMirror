package com.obsidium.bettermanual.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.obsidium.bettermanual.R;
import com.sony.scalar.hardware.CameraEx;

/**
 * Created by KillerInk on 22.08.2017.
 */

public class ApertureView extends BaseTextView implements CameraEx.ApertureChangeListener {

    @Override
    public void setIn_DecrementValue(int value) {
        onScrolled(value);
    }

    @Override
    public String getNavigationString() {

        return getResources().getString(R.string.view_aperture_navigation);
    }

    // Aperture
    private boolean         m_notifyOnNextApertureChange;
    private boolean         m_haveApertureControl;

    public ApertureView(Context context) {
        super(context);
    }

    public ApertureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ApertureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrolled(int distance) {
        m_notifyOnNextApertureChange = true;
        if (distance > 0)
            cameraUiInterface.getActivityInterface().getCamera().decrementAperture();
        else
            cameraUiInterface.getActivityInterface().getCamera().incrementAperture();
    }


    @Override
    public boolean onClick() {
        return false;
    }

    public boolean haveApertureControl()
    {
        return m_haveApertureControl;
    }


    @Override
    public void onApertureChange(CameraEx.ApertureInfo apertureInfo, CameraEx cameraEx) {
        // Disable aperture control if not available
        m_haveApertureControl = apertureInfo.currentAperture != 0;
        final String text = String.format("f%.1f", (float)apertureInfo.currentAperture / 100.0f);
        this.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(m_haveApertureControl ? View.VISIBLE : View.GONE);
                /*
                log(String.format("currentAperture %d currentAvailableMin %d currentAvailableMax %d\n",
                        apertureInfo.currentAperture, apertureInfo.currentAvailableMin, apertureInfo.currentAvailableMax));
                */

                setText(text);
            }
        });

        if (m_notifyOnNextApertureChange)
        {
            m_notifyOnNextApertureChange = false;
            cameraUiInterface.showMessageDelayed(text);
        }
    }
}
