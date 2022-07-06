package com.obsidium.bettermanual;

import android.os.Handler;
import android.view.View;

import com.sony.scalar.hardware.avio.DisplayManager;

/**
 * Created by KillerInk on 27.08.2017.
 */

public interface ActivityInterface {
    boolean hasTouchScreen();
    KeyEventHandler getDialHandler();
    Handler getMainHandler();
    DisplayManager getDisplayManager();
    void closeApp();
    void setColorDepth(boolean highQuality);
    void loadFragment(int fragment);
    void setSurfaceViewOnTouchListener(View.OnTouchListener onTouchListener);
    String getResString(int id);
    AvIndexManager getAvIndexManager();

    void takePhoto();
}
