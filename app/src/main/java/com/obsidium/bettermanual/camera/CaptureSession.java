package com.obsidium.bettermanual.camera;

import android.os.Looper;
import android.util.Log;

import com.sony.scalar.hardware.CameraEx;

/**
 * Created by KillerInk on 10.09.2017.
 */

public class CaptureSession implements Runnable, CameraEx.ShutterListener {

    private CameraInstance cameraInstance;
    private boolean isCaptureInProgress = false;
    private CaptureDoneEvent eventListener;
    private final String TAG = CaptureSession.class.getSimpleName();
    private CameraEx cameraEx;

    public interface CaptureDoneEvent
    {
        void onCaptureDone();
    }

    public CaptureSession(CameraInstance cameraInstance,CameraEx cameraEx )
    {
        this.cameraInstance = cameraInstance;
        this.cameraEx = cameraEx;
        //cameraInstance.setShutterListener(this);
    }

    @Override
    public void run() {
        if (isCaptureInProgress)
            return;
        //cameraEx.stopDirectShutter(null);
        cameraEx.burstableTakePicture();
        isCaptureInProgress = true;
    }

    /**
     * Returned from camera when a capture is done
     * STATUS_CANCELED = 1;
     * STATUS_ERROR = 2;
     * STATUS_OK = 0;
     * @param captureCode status
     * @param cameraEx2 did capture Image
     */
    @Override
    public void onShutter(int captureCode, CameraEx cameraEx2) {
        Log.d(TAG, "onShutter:" + logCaptureCode(captureCode));
        Log.d(TAG, "RunMainThread: " + (Thread.currentThread() == Looper.getMainLooper().getThread()));

        // cameraInstance.cancelCapture();
        //this.cameraEx.startDirectShutter();
        isCaptureInProgress = false;
        if (eventListener != null)
            eventListener.onCaptureDone();
    }

    private String logCaptureCode(int status)
    {
        switch (status)
        {
            case 1:
                return "Canceled";
            case 2:
                return "Error";
            default:
                return "OK";
        }
    }
}
