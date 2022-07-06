package com.obsidium.bettermanual.layout;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.obsidium.bettermanual.ActivityInterface;
import com.obsidium.bettermanual.CameraUtil;
import com.obsidium.bettermanual.KeyEvents;
import com.obsidium.bettermanual.MainActivity;
import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.controller.ShutterController;
import com.sony.scalar.hardware.CameraEx;

public class MinShutterFragment extends BaseLayout implements SeekBar.OnSeekBarChangeListener, KeyEvents
{
    private SeekBar     m_sbShutter;
    private TextView    m_tvInfo;

    public MinShutterFragment(Context context, ActivityInterface activityInterface) {
        super(context, activityInterface);
        inflateLayout(R.layout.min_shutter_fragment);
        m_sbShutter = (SeekBar) findViewById(R.id.sbShutter);
        m_sbShutter.setOnSeekBarChangeListener(this);
        m_sbShutter.setMax(CameraUtil.SHUTTER_SPEED_VALUES.length - 1);

        m_tvInfo = (TextView) findViewById(R.id.tvInfo);

        Button btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                MinShutterFragment.this.activityInterface.loadFragment(MainActivity.FRAGMENT_CAMERA_UI);
            }
        });

        CameraEx.ShutterSpeedInfo info = ShutterController.GetInstance().getShutterSpeedInfo();
        if (info != null) {
            setShutterSpeedToUi(info, true);

        }
        ShutterController.GetInstance().setShutterSpeedEventListener(new ShutterController.ShutterSpeedEvent() {
            @Override
            public void onChanged() {
                setShutterSpeedToUi(ShutterController.GetInstance().getShutterSpeedInfo(),false);
            }
        });

    }

    @Override
    public void Destroy() {
        // Save minimum shutter speed
        Preferences.GET().setMinShutterSpeed(CameraInstance.GET().getAutoShutterSpeedLowLimit());
        ShutterController.GetInstance().setShutterSpeedEventListener(null);
    }

    private void setShutterSpeedToUi(CameraEx.ShutterSpeedInfo shutterSpeedInfo, boolean updateSeekbarProgress) {
        final int idx = CameraUtil.getShutterValueIndex(shutterSpeedInfo.currentAvailableMin_n, shutterSpeedInfo.currentAvailableMin_d);
        if (idx >= 0)
        {
            if (updateSeekbarProgress)
                m_sbShutter.setProgress(idx);
            m_tvInfo.setText(CameraUtil.formatShutterSpeed(shutterSpeedInfo.currentAvailableMin_n, shutterSpeedInfo.currentAvailableMin_d));

        }
    }



    @Override
    public boolean onEnterKeyDown()
    {
        return true;
    }

    @Override
    public boolean onEnterKeyUp() {
        activityInterface.loadFragment(MainActivity.FRAGMENT_CAMERA_UI);
        return false;
    }

    @Override
    public boolean onFnKeyDown() {
        return false;
    }

    @Override
    public boolean onFnKeyUp() {
        return false;
    }

    @Override
    public boolean onAelKeyDown() {
        return false;
    }

    @Override
    public boolean onAelKeyUp() {
        return false;
    }

    @Override
    public boolean onMenuKeyDown() {
        return false;
    }

    @Override
    public boolean onMenuKeyUp() {
        return false;
    }

    @Override
    public boolean onFocusKeyDown() {
        return false;
    }

    @Override
    public boolean onFocusKeyUp() {
        return false;
    }

    @Override
    public boolean onShutterKeyDown() {
        return false;
    }

    @Override
    public boolean onShutterKeyUp() {
        return false;
    }

    @Override
    public boolean onPlayKeyDown() {
        return false;
    }

    @Override
    public boolean onPlayKeyUp() {
        return false;
    }

    @Override
    public boolean onMovieKeyDown() {
        return false;
    }

    @Override
    public boolean onMovieKeyUp() {
        return false;
    }

    @Override
    public boolean onC1KeyDown() {
        return false;
    }

    @Override
    public boolean onC1KeyUp() {
        return false;
    }

    @Override
    public boolean onLensAttached() {
        return false;
    }

    @Override
    public boolean onLensDetached() {
        return false;
    }

    @Override
    public boolean onModeDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onZoomTeleKey() {
        return false;
    }

    @Override
    public boolean onZoomWideKey() {
        return false;
    }

    @Override
    public boolean onZoomOffKey() {
        return false;
    }

    @Override
    public boolean onDeleteKeyDown() {
        return false;
    }

    @Override
    public boolean onDeleteKeyUp() {
        return false;
    }

    @Override
    public boolean onUpperDialChanged(int value)
    {

        return true;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
        m_sbShutter.incrementProgressBy(value);
        int speed = CameraUtil.SHUTTER_SPEED_VALUES[m_sbShutter.getProgress()].getMillisecond();
        CameraInstance.GET().setAutoShutterSpeedLowLimit(speed);
        return false;
    }

    @Override
    public boolean onUpKeyDown() {
        return false;
    }

    @Override
    public boolean onUpKeyUp() {
        return false;
    }

    @Override
    public boolean onDownKeyDown() {
        return false;
    }

    @Override
    public boolean onDownKeyUp() {
        return false;
    }

    @Override
    public boolean onLeftKeyDown() {
        return false;
    }

    @Override
    public boolean onLeftKeyUp() {
        return false;
    }

    @Override
    public boolean onRightKeyDown() {
        return false;
    }

    @Override
    public boolean onRightKeyUp() {
        return false;
    }

    /* OnSeekBarChangeListener */
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (fromUser)
        {
            CameraInstance.GET().setAutoShutterSpeedLowLimit(CameraUtil.SHUTTER_SPEED_VALUES[progress].getMillisecond());
        }
    }
    public void onStartTrackingTouch(SeekBar var1)
    {
    }
    public void onStopTrackingTouch(SeekBar var1)
    {
    }


}
