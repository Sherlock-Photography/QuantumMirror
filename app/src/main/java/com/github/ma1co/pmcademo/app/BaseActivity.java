package com.github.ma1co.pmcademo.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.github.ma1co.openmemories.framework.DeviceInfo;
import com.obsidium.bettermanual.KeyEventHandler;
import com.obsidium.bettermanual.KeyEvents;
import com.sony.scalar.hardware.avio.DisplayManager;
import com.sony.scalar.sysutil.didep.Gpelibrary;

public class BaseActivity extends Activity implements KeyEvents {

    private final String TAG = BaseActivity.class.getSimpleName();

    public static final String NOTIFICATION_DISPLAY_CHANGED = "NOTIFICATION_DISPLAY_CHANGED";

    public static final String KEY_ACCESSORY_APO = "KEY_ACCESSORY_APO";
    public static final String KEY_DEDICATED_APO = "KEY_DEDICATED_APO";
    public static final String KEY_LENS_APO = "KEY_LENS_APO";
    public static final String KEY_MEDIA_INOUT_APO = "KEY_MEDIA_INOUT_APO";
    public static final String KEY_PLAY_APO = "KEY_PLAY_APO";
    public static final String KEY_PLAY_PON = "KEY_PLAY_PON";
    public static final String KEY_POWER_APO = "KEY_POWER_APO";
    public static final String KEY_POWER_SLIDE_PON = "KEY_POWER_SLIDE_PON";
    public static final String KEY_RELEASE_APO = "KEY_RELEASE_APO";

    private DisplayManager displayManager;

    protected KeyEventHandler keyEventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        keyEventHandler = new KeyEventHandler(this);
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();

        setColorDepth(false);
        notifyAppInfo();

        displayManager = new DisplayManager();
        displayManager.setDisplayStatusListener(new DisplayManager.DisplayEventListener() {
            @Override
            public void onDeviceStatusChanged(int event) {
                if (event == DisplayManager.EVENT_SWITCH_DEVICE)
                    onDisplayChanged(displayManager.getActiveDevice());
                if (event == DisplayManager.EVENT_DETACH_DEVICE)
                    Log.d("BaseActivity", "Detach Device");
                if (event == DisplayManager.EVENT_ATTACH_DEVICE)
                    Log.d("BaseActivity", "Attach Device");
                if (event == DisplayManager.EVENT_CHANGE_VIEW_PATTERN)
                    Log.d("BaseActivity", "change view pattern");

            }
        });
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();

        displayManager.releaseDisplayStatusListener();
        displayManager.finish();
        displayManager = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyEventHandler.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
       return keyEventHandler.onKeyUp(keyCode,event);
    }

    public boolean onFnKeyDown() { return false; }
    public boolean onFnKeyUp() { return false; }
    public boolean onAelKeyDown() { return false; }
    public boolean onAelKeyUp() { return false; }
    public boolean onMenuKeyDown() { return false; }
    public boolean onMenuKeyUp() { return false; }
    public boolean onFocusKeyDown() { return false; }
    public boolean onFocusKeyUp() { return false; }
    public boolean onShutterKeyDown() { return false; }
    public boolean onShutterKeyUp() { return false; }
    public boolean onPlayKeyDown() { return false; }
    public boolean onPlayKeyUp() { return false; }
    public boolean onMovieKeyDown() { return false; }
    public boolean onMovieKeyUp() { return false; }
    public boolean onC1KeyDown() { return false; }
    public boolean onC1KeyUp() { return false; }
    public boolean onLensAttached() { return false; }
    public boolean onLensDetached() { return false; }
    public boolean onModeDialChanged(int value) { return false; }

    public boolean onZoomTeleKey(){return false;}
    public boolean onZoomWideKey(){return false;}
    public boolean onZoomOffKey(){return false;}


    public boolean onDeleteKeyDown() {
        return true;
    }
    public boolean onDeleteKeyUp() {
        onBackPressed();
        return true;
    }

    public void onDisplayChanged(String device) {
        AppNotificationManager.getInstance().notify(NOTIFICATION_DISPLAY_CHANGED);
    }

    protected void setAutoPowerOffMode(boolean enable) {
        String mode = enable ? "APO/NORMAL" : "APO/NO";// or "APO/SPECIAL" ?
        Intent intent = new Intent();
        intent.setAction("com.android.server.DAConnectionManagerService.apo");
        intent.putExtra("apo_info", mode);
        sendBroadcast(intent);
    }

    protected void setColorDepth(boolean highQuality) {
        /* Enabling highQuality makes A7R hang forever when shooting JPEGs (fine for RAW though!) */
        Gpelibrary.GS_FRAMEBUFFER_TYPE type = highQuality ? Gpelibrary.GS_FRAMEBUFFER_TYPE.ABGR8888 : Gpelibrary.GS_FRAMEBUFFER_TYPE.RGBA4444;
        Gpelibrary.changeFrameBufferPixel(type);
    }

    protected void notifyAppInfo() {
        Intent intent = new Intent("com.android.server.DAConnectionManagerService.AppInfoReceive");
        intent.putExtra("package_name", getComponentName().getPackageName());
        intent.putExtra("class_name", getComponentName().getClassName());
        //intent.putExtra("pkey", new String[] {});// either this or these two:
        //intent.putExtra("pullingback_key", new String[] {});
        // Exit app when plugging camera into USB
        intent.putExtra("pullingback_key", new String[] { "KEY_USB_CONNECT" });
        // Automatically resume app after power off etc.
        intent.putExtra("resume_key", new String[] { KEY_POWER_SLIDE_PON, KEY_RELEASE_APO, KEY_PLAY_APO,
                KEY_MEDIA_INOUT_APO, KEY_LENS_APO, KEY_ACCESSORY_APO, KEY_DEDICATED_APO, KEY_POWER_APO, KEY_PLAY_PON });
        sendBroadcast(intent);
    }

    public DisplayManager getDisplayManager() {
        return displayManager;
    }

    public DeviceInfo getDeviceInfo() {
        return DeviceInfo.getInstance();
    }

    @Override
    public boolean onUpperDialChanged(int value) {
        return false;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
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

    @Override
    public boolean onEnterKeyDown() {
        return false;
    }

    @Override
    public boolean onEnterKeyUp() {
        return false;
    }
}
