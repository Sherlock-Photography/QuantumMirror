package com.obsidium.bettermanual.views;

import android.content.Context;
import android.util.AttributeSet;

import com.github.killerink.CameraWrapper;

/**
 * Created by KillerInk on 22.08.2017.
 */

public class EvView extends BaseTextView {

    @Override
    public void setIn_DecrementValue(int value) {
        onScrolled(value);
    }

    private class EvSetRunner implements Runnable
    {
        private int direction;
        public EvSetRunner(int direction)
        {
            this.direction = direction;
        }
        @Override
        public void run() {
            if (direction > 0)
                decrementExposureCompensation(true);
            else
                incrementExposureCompensation(true);
        }
    }

    private int             m_maxExposureCompensation;
    private int             m_minExposureCompensation;
    private int             m_curExposureCompensation;
    private float           m_exposureCompensationStep;
    private CameraWrapper wrapper;

    public EvView(Context context) {
        super(context);
    }

    public EvView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EvView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrolled(int distance) {
        cameraUiInterface.getActivityInterface().getBackHandler().post(new EvSetRunner(distance));
    }

    @Override
    public boolean onClick() {
        // Reset exposure compensation
        setExposureCompensation(0);
        return true;
    }

    public void init(CameraWrapper wrapper)
    {
        this.wrapper = wrapper;
        m_maxExposureCompensation = wrapper.getMaxExposureCompensation();
        m_minExposureCompensation = wrapper.getMinExposureCompensation();
        m_exposureCompensationStep = wrapper.getExposureCompensationStep();
        m_curExposureCompensation = wrapper.getExposureCompensation();
        updateExposureCompensation(false);
    }

    private void setExposureCompensation(int value)
    {
        wrapper.setExposureCompensation(value);
        m_curExposureCompensation = value;
        updateExposureCompensation(false);
    }

    private void decrementExposureCompensation(boolean notify)
    {
        if (m_curExposureCompensation > m_minExposureCompensation)
        {
            --m_curExposureCompensation;
            wrapper.setExposureCompensation(m_curExposureCompensation);
            updateExposureCompensation(notify);
        }
    }

    private void incrementExposureCompensation(boolean notify)
    {
        if (m_curExposureCompensation < m_maxExposureCompensation)
        {
            ++m_curExposureCompensation;
            wrapper.setExposureCompensation(m_curExposureCompensation);
            updateExposureCompensation(notify);
        }
    }

    private void updateExposureCompensation(final boolean notify)
    {
        cameraUiInterface.getActivityInterface().getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                final String text;
                if (m_curExposureCompensation == 0)
                    text = "\uEB18\u00B10.0";
                else if (m_curExposureCompensation > 0)
                    text = String.format("\uEB18+%.1f", m_curExposureCompensation * m_exposureCompensationStep);
                else
                    text = String.format("\uEB18%.1f", m_curExposureCompensation * m_exposureCompensationStep);
                setText(text);
                if (notify)
                    cameraUiInterface.showMessageDelayed(text);
            }
        });

    }
}
