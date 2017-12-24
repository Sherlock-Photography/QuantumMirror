package com.obsidium.bettermanual;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.github.ma1co.pmcademo.app.BaseActivity;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.camera.CaptureSession;
import com.obsidium.bettermanual.layout.BaseLayout;
import com.obsidium.bettermanual.layout.CameraUiFragment;
import com.obsidium.bettermanual.layout.ImageFragment;
import com.obsidium.bettermanual.layout.MinShutterFragment;
import com.obsidium.bettermanual.layout.PreviewMagnificationFragment;
import com.sony.scalar.hardware.CameraEx;


/**
 * Created by KillerInk on 27.08.2017.
 */

public class MainActivity extends BaseActivity implements ActivityInterface, CameraInstance.CameraEvents, SurfaceHolder.Callback,CameraEx.ShutterListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private Preferences preferences;

    //private CameraInstance cameraInstance;

    public final static int FRAGMENT_CAMERA_UI = 0;
    public final static int FRAGMENT_MIN_SHUTTER = 1;
    public final static int FRAGMENT_PREVIEWMAGNIFICATION = 2;
    public final static int FRAGMENT_IMAGEVIEW = 3;
    public final static int FRAGMENT_WAITFORCAMERARDY = 4;

    private Handler   m_handler;
    private HandlerThread cameraThread;

    private SurfaceHolder m_surfaceHolder;
    SurfaceView surfaceView;

    LinearLayout layoutHolder;
    FrameLayout surfaceViewHolder;

    private boolean isBulbCapture = false;
    private boolean isCaptureInProgress = false;
    private CaptureSession.CaptureDoneEvent eventListner;

    private BaseLayout currentLayout;

    private AvIndexManager avIndexManager;

    //Caution caution;

    public void startBackgroundThread(){
        cameraThread = new HandlerThread("HandlerThread");
        cameraThread.start();
    }

    private void stopBackgroundThread()
    {
        Log.d(TAG,"stopBackgroundThread");
        if(cameraThread == null)
            return;

        cameraThread.quit();
        try {
            cameraThread.join();
            cameraThread = null;
            cameraThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
        startBackgroundThread();
        m_handler = new Handler();
        setContentView(R.layout.main_activity);
        CameraInstance.GET().initHandler(cameraThread.getLooper());

        surfaceViewHolder = (FrameLayout) findViewById(R.id.surfaceView);
        //surfaceView.setOnTouchListener(new CameraUiFragment.SurfaceSwipeTouchListener(getContext()));
        avIndexManager = new AvIndexManager(getContentResolver());

        layoutHolder = (LinearLayout)findViewById(R.id.fragment_holder);
        preferences = new Preferences(getApplicationContext());

    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();

        registerReceiver(avIndexManager, avIndexManager.AVAILABLE_SIZE_INTENTS);
        registerReceiver(avIndexManager, avIndexManager.MEDIA_INTENTS);
        avIndexManager.onResume(getApplicationContext());
        addSurfaceView();

        CameraInstance.GET().setCameraEventsListner(MainActivity.this);

    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        removeSurfaceView();
        super.onPause();
        unregisterReceiver(avIndexManager);
        avIndexManager.onPause(getApplicationContext());

        saveDefaults();
        CameraInstance.GET().closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBackgroundThread();
    }

    private void saveDefaults()
    {
        // Scene mode
        getPreferences().setSceneMode(CameraInstance.GET().getSceneMode());
        // Drive mode and burst speed
        getPreferences().setDriveMode(CameraInstance.GET().getDriveMode());
        getPreferences().setBurstDriveSpeed(CameraInstance.GET().getBurstDriveSpeed());
    }


    @Override
    public boolean hasTouchScreen() {
        return getDeviceInfo().getModel().compareTo("ILCE-5100") == 0;
    }

    @Override
    public Preferences getPreferences() {
        return preferences;
    }

    @Override
    public KeyEventHandler getDialHandler() {
        return keyEventHandler;
    }

    @Override
    public Handler getMainHandler() {
        return m_handler;
    }

    @Override
    public void closeApp() {
        // Exiting, make sure the app isn't restarted
        Intent intent = new Intent("com.android.server.DAConnectionManagerService.AppInfoReceive");
        intent.putExtra("package_name", getComponentName().getPackageName());
        intent.putExtra("class_name", getComponentName().getClassName());
        intent.putExtra("pullingback_key", new String[] {});
        intent.putExtra("resume_key", new String[] {});
        sendBroadcast(intent);
        onBackPressed();
    }

    @Override
    public void setColorDepth(boolean highQuality) {
        super.setColorDepth(highQuality);
    }

    @Override
    public void loadFragment(int fragment) {
        if (currentLayout != null) {
            currentLayout.Destroy();
            layoutHolder.removeAllViews();
        }
        switch (fragment)
        {
            case FRAGMENT_MIN_SHUTTER:
                MinShutterFragment msa = new MinShutterFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListner(msa);
                currentLayout = msa;
                layoutHolder.addView(msa);
                break;
            case FRAGMENT_PREVIEWMAGNIFICATION:
                PreviewMagnificationFragment pmf = new PreviewMagnificationFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListner(pmf);
                currentLayout = pmf;
                layoutHolder.addView(pmf);
                break;
            case FRAGMENT_IMAGEVIEW:
                CameraInstance.GET().closeCamera();
                removeSurfaceView();
                ImageFragment imageFragment = new ImageFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListner(imageFragment);
                currentLayout = imageFragment;
                layoutHolder.addView(imageFragment);
                break;
            case FRAGMENT_WAITFORCAMERARDY:
                addSurfaceView();
                break;
            case FRAGMENT_CAMERA_UI:
            default:
                CameraUiFragment cameraUiFragment = new CameraUiFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListner(cameraUiFragment);
                currentLayout = cameraUiFragment;
                layoutHolder.addView(cameraUiFragment);
                break;

        }
    }

    @Override
    public void setSurfaceViewOnTouchListner(View.OnTouchListener onTouchListner) {
        if (surfaceView != null)
            surfaceView.setOnTouchListener(onTouchListner);
    }

    @Override
    public String getResString(int id) {
        return getResources().getString(id);
    }

    public void setBulbCapture(boolean bulbCapture)
    {
        this.isBulbCapture = bulbCapture;
    }

    @Override
    public AvIndexManager getAvIndexManager() {
        return avIndexManager;
    }

    public void setCaptureDoneEventListner(CaptureSession.CaptureDoneEvent eventListner)
    {
        this.eventListner = eventListner;
    }

    public boolean isCaptureInProgress()
    {
        return isCaptureInProgress;
    }

    public boolean isBulbCapture()
    {
        return isBulbCapture;
    }

    public void cancelBulbCapture()
    {
        isBulbCapture = false;
        CameraInstance.GET().cancelCapture();
    }

    @Override
    public void onCameraOpen(boolean isOpen) {
        Log.d(TAG, "onCameraOpen");
        CameraInstance.GET().enableHwShutterButton();
        CameraInstance.GET().setShutterListener(this);
        CameraInstance.GET().setSurfaceHolder(m_surfaceHolder);
        CameraInstance.GET().startPreview();


        loadFragment(FRAGMENT_CAMERA_UI);
        loadDefaults();
    }

    private void loadDefaults()
    {
        final TimeLog timeLog = new TimeLog("loadDefaults");
        //Log.d(TAG,"Parameters: " + params.flatten());
        // Focus mode
        CameraInstance.GET().setFocusMode(CameraEx.ParametersModifier.FOCUS_MODE_MANUAL);
        // Scene mode
        final String sceneMode = getPreferences().getSceneMode();
        CameraInstance.GET().setSceneMode(sceneMode);
        // Drive mode and burst speed
        CameraInstance.GET().setDriveMode(getPreferences().getDriveMode());
        CameraInstance.GET().setBurstDriveSpeed(getPreferences().getBurstDriveSpeed());
        // Minimum shutter speed
        if(CameraInstance.GET().isAutoShutterSpeedLowLimitSupported()) {
            if (sceneMode.equals(CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE))
                CameraInstance.GET().setAutoShutterSpeedLowLimit(-1);
            else
                CameraInstance.GET().setAutoShutterSpeedLowLimit(getPreferences().getMinShutterSpeed());
        }
        // Disable self timer
        CameraInstance.GET().setSelfTimer(0);
        // Force aspect ratio to 3:2
        CameraInstance.GET().setImageAspectRatio(CameraEx.ParametersModifier.IMAGE_ASPECT_RATIO_3_2);
        CameraInstance.GET().setImageQuality(CameraEx.ParametersModifier.PICTURE_STORAGE_FMT_RAW);
        // View visibility

        /*if (getCamera().isLongExposureNoiseReductionSupported())
            getCamera().setLongExposureNoiseReduction(getP);*/
        timeLog.logTime();

    }

    private void addSurfaceView() {
        surfaceView = new SurfaceView(getApplicationContext());
        m_surfaceHolder = surfaceView.getHolder();
        m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        m_surfaceHolder.addCallback(this);
        surfaceViewHolder.addView(surfaceView);
    }

    private void removeSurfaceView()
    {
        surfaceView = null;
        surfaceViewHolder.removeAllViews();
    }

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                CameraInstance.GET().setSurfaceHolder(surfaceHolder);
                CameraInstance.GET().startCamera();
            }
        });

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    /**
     * Returned from camera when a capture is done
     * STATUS_CANCELED = 1;
     * STATUS_ERROR = 2;
     * STATUS_OK = 0;
     * @param i code
     * @param cameraEx2 did capture Image
     */
    @Override
    public void onShutter(int i, CameraEx cameraEx2) {
        Log.d(TAG, "onShutter:" + logCaptureCode(i)+ " isBulb:" + isBulbCapture);
        Log.d(TAG, "RunMainThread: " + (Thread.currentThread() == Looper.getMainLooper().getThread()));
        if (!isBulbCapture()) {

            CameraInstance.GET().cancelCapture();
            /*Caution.SetTrigger(131078, 1, false);
            avIndexHandler.update();
            Caution.SetMode(2, AvindexStore.getExternalMediaIds());
            String mediaStatus =  Environment.getExternalStorageState();
            Log.d(TAG,"MediaStatus:" + mediaStatus);*/
            isCaptureInProgress = false;
            if (eventListner != null)
                eventListner.onCaptureDone();
        }
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
