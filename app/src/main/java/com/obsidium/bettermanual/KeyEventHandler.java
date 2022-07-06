package com.obsidium.bettermanual;

import android.util.Log;
import android.view.KeyEvent;

import com.sony.scalar.sysutil.ScalarInput;

/**
 * Created by KillerInk on 24.08.2017.
 */

public class KeyEventHandler {

    private final  String TAG = KeyEventHandler.class.getSimpleName();

    private KeyEvents dialEventListener;
    private KeyEvents defaultListener;
    private final boolean log = false;

    public KeyEventHandler(KeyEvents defaultListener)
    {
        this.defaultListener = defaultListener;
    }

    public void setDefaultListener()
    {
        this.dialEventListener = defaultListener;
    }

    public void setDialEventListener(KeyEvents eventListener)
    {
        this.dialEventListener = eventListener;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (dialEventListener == null)
            return true;
        if (log)
            Log.d(TAG,"onKeyUp " + getKeyString(event.getScanCode()));
        if (dialEventListener != null) {
            switch (event.getScanCode()) {
                case ScalarInput.ISV_KEY_UP:
                    return dialEventListener.onUpKeyUp();
                case ScalarInput.ISV_KEY_DOWN:
                    return dialEventListener.onDownKeyUp();
                case ScalarInput.ISV_KEY_LEFT:
                    return dialEventListener.onLeftKeyUp();
                case ScalarInput.ISV_KEY_RIGHT:
                    return dialEventListener.onRightKeyUp();
                case ScalarInput.ISV_KEY_ENTER:
                    return dialEventListener.onEnterKeyUp();

                case ScalarInput.ISV_DIAL_1_CLOCKWISE:
                case ScalarInput.ISV_DIAL_1_COUNTERCW:
                case ScalarInput.ISV_DIAL_2_CLOCKWISE:
                case ScalarInput.ISV_DIAL_2_COUNTERCW:
                    return true;

                case ScalarInput.ISV_KEY_FN:
                    return dialEventListener.onFnKeyUp();
                case ScalarInput.ISV_KEY_AEL:
                    return dialEventListener.onAelKeyUp();
                case ScalarInput.ISV_KEY_MENU:
                case ScalarInput.ISV_KEY_SK1:
                    return dialEventListener.onMenuKeyUp();
                case ScalarInput.ISV_KEY_S1_1:
                    return dialEventListener.onFocusKeyUp();
                case ScalarInput.ISV_KEY_S1_2:
                    return true;
                case ScalarInput.ISV_KEY_S2:
                    return dialEventListener.onShutterKeyUp();
                case ScalarInput.ISV_KEY_PLAY:
                    return dialEventListener.onPlayKeyUp();
                case ScalarInput.ISV_KEY_STASTOP:
                    return dialEventListener.onMovieKeyUp();
                case ScalarInput.ISV_KEY_CUSTOM1:
                    return dialEventListener.onC1KeyUp();
                case ScalarInput.ISV_KEY_DELETE:
                case ScalarInput.ISV_KEY_SK2:
                    return dialEventListener.onDeleteKeyUp();
                case ScalarInput.ISV_KEY_LENS_ATTACH:
                    return dialEventListener.onLensDetached();
            }
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (dialEventListener == null)
            return true;
        if (log)
            Log.d(TAG,"onKeyDown " + getKeyString(event.getScanCode()));
        switch (event.getScanCode()) {
            case ScalarInput.ISV_KEY_UP:
                return dialEventListener.onUpKeyDown();
            case ScalarInput.ISV_KEY_DOWN:
                return dialEventListener.onDownKeyDown();
            case ScalarInput.ISV_KEY_LEFT:
                return dialEventListener.onLeftKeyDown();
            case ScalarInput.ISV_KEY_RIGHT:
                return dialEventListener.onRightKeyDown();
            case ScalarInput.ISV_KEY_ENTER:
                return dialEventListener.onEnterKeyDown();

            case ScalarInput.ISV_DIAL_1_CLOCKWISE:
                return dialEventListener.onUpperDialChanged(1);
            case ScalarInput.ISV_DIAL_1_COUNTERCW:
                return dialEventListener.onUpperDialChanged(-1);
            case ScalarInput.ISV_DIAL_2_CLOCKWISE:
                return dialEventListener.onLowerDialChanged(1);
            case ScalarInput.ISV_DIAL_2_COUNTERCW:
                return dialEventListener.onLowerDialChanged(-1);

            case ScalarInput.ISV_KEY_FN:
                return dialEventListener.onFnKeyDown();
            case ScalarInput.ISV_KEY_AEL:
                return dialEventListener.onAelKeyDown();
            case ScalarInput.ISV_KEY_MENU:
            case ScalarInput.ISV_KEY_SK1:
                return dialEventListener.onMenuKeyDown();
            case ScalarInput.ISV_KEY_S1_1:
                return dialEventListener.onFocusKeyDown();
            case ScalarInput.ISV_KEY_S1_2:
                return true;
            case ScalarInput.ISV_KEY_S2:
                return dialEventListener.onShutterKeyDown();
            case ScalarInput.ISV_KEY_PLAY:
                return dialEventListener.onPlayKeyDown();
            case ScalarInput.ISV_KEY_STASTOP:
                return dialEventListener.onMovieKeyDown();
            case ScalarInput.ISV_KEY_CUSTOM1:
                return dialEventListener.onC1KeyDown();
            case ScalarInput.ISV_KEY_DELETE:
            case ScalarInput.ISV_KEY_SK2:
                return dialEventListener.onDeleteKeyDown();
            case ScalarInput.ISV_KEY_LENS_ATTACH:
                return dialEventListener.onLensAttached();
            case ScalarInput.ISV_KEY_MODE_DIAL:
                return dialEventListener.onModeDialChanged(getDialStatus(ScalarInput.ISV_KEY_MODE_DIAL));
            case ScalarInput.ISV_KEY_ZOOM_OFF: // zoom not active
                return dialEventListener.onZoomOffKey();
            case ScalarInput.ISV_KEY_ZOOM_TELE: //zoom in
                return dialEventListener.onZoomTeleKey();
            case ScalarInput.ISV_KEY_IR_ZOOM_WIDE: //zoom out
                return dialEventListener.onZoomWideKey();
        }
        return true;
    }
    protected int getDialStatus(int key) {
        return ScalarInput.getKeyStatus(key).status;
    }

    public String getKeyString(int key)
    {
        switch (key)
        {
            case ScalarInput.ISV_KEY_UP:
                return "Up";
            case ScalarInput.ISV_KEY_DOWN:
                return "Down";
            case ScalarInput.ISV_KEY_LEFT:
                return "Left";
            case ScalarInput.ISV_KEY_RIGHT:
                return "Right";
            case ScalarInput.ISV_KEY_ENTER:
                return "Enter";

            case ScalarInput.ISV_DIAL_1_CLOCKWISE:
                return "UpperDialChanged_Clock";
            case ScalarInput.ISV_DIAL_1_COUNTERCW:
                return "UpperDialChanged_ CW";
            case ScalarInput.ISV_DIAL_2_CLOCKWISE:
                return "LowerDialChanged_Clock";
            case ScalarInput.ISV_DIAL_2_COUNTERCW:
                return "LowerDialChanged_CW";

            case ScalarInput.ISV_KEY_FN:
                return "FnKeyDown";
            case ScalarInput.ISV_KEY_AEL:
                return "AelKey";
            case ScalarInput.ISV_KEY_MENU:
            case ScalarInput.ISV_KEY_SK1:
                return "MenuKeyDown";
            case ScalarInput.ISV_KEY_S1_1:
                return "FocusKeyDown";
            case ScalarInput.ISV_KEY_S1_2:
                return "ISV_KEY_S1_2";
            case ScalarInput.ISV_KEY_S2:
                return "Shutter";
            case ScalarInput.ISV_KEY_PLAY:
                return "Play";
            case ScalarInput.ISV_KEY_STASTOP:
                return "Movie";
            case ScalarInput.ISV_KEY_CUSTOM1:
                return"C1";
            case ScalarInput.ISV_KEY_DELETE:
            case ScalarInput.ISV_KEY_SK2:
                return "Delete";
            case ScalarInput.ISV_KEY_LENS_ATTACH:
                return "LensAttached";
            case ScalarInput.ISV_KEY_MODE_DIAL:
                return "ModeDialChanged";
            case ScalarInput.ISV_KEY_ZOOM_OFF: // zoom not active
                return "ZoomOff";
            case ScalarInput.ISV_KEY_ZOOM_TELE: //zoom in
                return "ZoomTele";
            case ScalarInput.ISV_KEY_IR_ZOOM_WIDE: //zoom out
                return "ZoomWideKey";
        }
        return String.valueOf(key);
    }
}
