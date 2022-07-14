package com.obsidium.bettermanual.camera;

import android.hardware.Camera;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;

import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.controller.ApertureController;
import com.obsidium.bettermanual.controller.BatteryObserverController;
import com.obsidium.bettermanual.controller.ExposureCompensationController;
import com.obsidium.bettermanual.controller.ExposureHintController;
import com.obsidium.bettermanual.controller.ExposureModeController;
import com.obsidium.bettermanual.controller.FocusDriveController;
import com.obsidium.bettermanual.controller.HistogramController;
import com.obsidium.bettermanual.controller.ImageStabilisationController;
import com.obsidium.bettermanual.controller.IsoController;
import com.obsidium.bettermanual.controller.ShutterController;
import com.obsidium.bettermanual.model.ApertureModel;
import com.obsidium.bettermanual.model.BatteryObserverModel;
import com.obsidium.bettermanual.model.ExposureCompensationModel;
import com.obsidium.bettermanual.model.ExposureHintModel;
import com.obsidium.bettermanual.model.ExposureModeModel;
import com.obsidium.bettermanual.model.FocusDriveModel;
import com.obsidium.bettermanual.model.HistogramModel;
import com.obsidium.bettermanual.model.ImageStabilisationModel;
import com.obsidium.bettermanual.model.IsoModel;
import com.obsidium.bettermanual.model.ShutterModel;
import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.hardware.CameraSequence;
import com.sony.scalar.meta.FaceInfo;
import com.sony.scalar.provider.AvindexStore;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;


public class CameraInstance extends BaseCamera implements CameraSequence.ShutterSequenceCallback {
    private final String TAG = CameraInstance.class.getSimpleName();

//    public CameraSequence cameraSequence;

    final int SETSURFACE = 0;
    private static CameraInstance INSTANCE = new CameraInstance();
    private SurfaceHolder surfaceHolder;
    private ApertureModel apertureModel;
    private ShutterModel shutterModel;
    private IsoModel isoModel;
    private ExposureCompensationModel exposureCompensationModel;
    private ExposureHintModel exposureHintModel;
    private ExposureModeModel exposureModeModel;
    private ImageStabilisationModel imageStabilisationModel;
    private HistogramModel histogramModel;
    private FocusDriveModel focusDriveModel;
    private BatteryObserverModel batteryObserverModel;


    private CameraInstance() {
        super();
    }

    public static CameraInstance GET()
    {
        return INSTANCE;
    }

    public void startCamera() {
/*        CameraEx.OpenOptions options = new CameraEx.OpenOptions();
        options.setPreview(true);*/
        Log.d(TAG, "Open Cam");
        m_camera = CameraEx.open(0, null);
        cameraIsOpen = true;
        /*cameraSequence = CameraSequence.open(m_camera);
        setOptions(null);
        cameraSequence.setShutterSequenceCallback(this);*/
        Log.d(TAG, "Cam open");
        initCamera();
        fireOnCameraOpen(true);

    }

    public void initParameters()
    {
        apertureModel = new ApertureModel(this);
        m_camera.setApertureChangeListener(apertureModel);
        ApertureController.GetInstance().bindModel(apertureModel);

        shutterModel = new ShutterModel(this);
        m_camera.setShutterSpeedChangeListener(shutterModel);
        ShutterController.GetInstance().bindModel(shutterModel);

        isoModel = new IsoModel(this);
        m_camera.setAutoISOSensitivityListener(isoModel);
        IsoController.GetInstance().bindModel(isoModel);

        exposureCompensationModel = new ExposureCompensationModel(this);
        ExposureCompensationController.GetInstance().bindModel(exposureCompensationModel);

        exposureHintModel = new ExposureHintModel(this);
        m_camera.setProgramLineRangeOverListener(exposureHintModel);
        ExposureHintController.GetInstance().bindModel(exposureHintModel);

        exposureModeModel = new ExposureModeModel(this);
        ExposureModeController.GetInstance().bindModel(exposureModeModel);

        if (isImageStabSupported()) {
            imageStabilisationModel = new ImageStabilisationModel(this);
            ImageStabilisationController.GetInstance().bindModel(imageStabilisationModel);
        }

        focusDriveModel = new FocusDriveModel(this);
        m_camera.setFocusDriveListener(focusDriveModel);
        FocusDriveController.GetInstance().bindModel(focusDriveModel);

        histogramModel = new HistogramModel(this);
        m_camera.setPreviewAnalizeListener(histogramModel);
        HistogramController.GetInstance().bindModel(histogramModel);

        batteryObserverModel = new BatteryObserverModel(this);
        BatteryObserverController.GetInstance().bindModel(batteryObserverModel);
        batteryObserverModel.start();

        applySettings();
        //dumpParameter();
    }

    private void applySettings() {
        final String sceneMode = Preferences.GET().getSceneMode();
        setSceneMode(sceneMode);
        setDriveMode(CameraEx.ParametersModifier.DRIVE_MODE_SINGLE);
        // Minimum shutter speed
        if (isAutoShutterSpeedLowLimitSupported()) {
            if (sceneMode.equals(CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE))
                setAutoShutterSpeedLowLimit(-1);
            else
                setAutoShutterSpeedLowLimit(Preferences.GET().getMinShutterSpeed());
        }
        // Force aspect ratio to 3:2
        setImageAspectRatio(CameraEx.ParametersModifier.IMAGE_ASPECT_RATIO_3_2);
        setImageQuality(CameraEx.ParametersModifier.PICTURE_STORAGE_FMT_RAWJPEG);

        disableHwShutterButton();

        startFaceDetection(1);
    }

    public void closeCamera() {
        cameraIsOpen = false;
        Log.d(TAG, "closeCamera");

        // m_camera.setJpegListener(null);

        if (exposureModeModel != null)
            Preferences.GET().setSceneMode(exposureModeModel.getStringValue());

        if (m_camera != null) {
            ApertureController.GetInstance().bindModel(null);
            m_camera.setApertureChangeListener(null);
            apertureModel = null;

            ShutterController.GetInstance().bindModel(null);
            m_camera.setShutterSpeedChangeListener(null);
            shutterModel = null;

            IsoController.GetInstance().bindModel(null);
            m_camera.setAutoISOSensitivityListener(null);
            isoModel = null;

            ExposureCompensationController.GetInstance().bindModel(null);
            exposureCompensationModel = null;

            ExposureHintController.GetInstance().bindModel(null);
            exposureHintModel = null;

            ExposureModeController.GetInstance().bindModel(null);
            exposureModeModel = null;

            HistogramController.GetInstance().bindModel(null);
            m_camera.setPreviewAnalizeListener(null);
            histogramModel = null;

            FocusDriveController.GetInstance().bindModel(null);
            m_camera.setFocusDriveListener(null);
            focusDriveModel = null;

            batteryObserverModel.stop();
            BatteryObserverController.GetInstance().bindModel(null);
            batteryObserverModel = null;

            /*cameraSequence.setShutterSequenceCallback(null);
            cameraSequence.release();*/
            m_camera.getNormalCamera().stopPreview();
            m_camera.getNormalCamera().release();
            m_camera.release();
            m_camera = null;
        }

        surfaceHolder = null;
    }

    public void setSurfaceHolder(SurfaceHolder surface) {
        this.surfaceHolder = surface;
    }

    public void startPreview() {
        Log.d(TAG,"startPreview");
        getCameraEx().getNormalCamera().startPreview();
    }

    public void stopPreview() {
        Log.d(TAG,"stopPreview");
        getCameraEx().getNormalCamera().stopPreview();
    }

    public void enableHwShutterButton() {
        Log.d(TAG,"enableHwShutterButton");
        m_camera.startDirectShutter();

    }

    public void disableHwShutterButton() {
        m_camera.stopDirectShutter(null);
    }

    public void cancelCapture()
    {
        Log.d(TAG,"cancelCapture");
        getCameraEx().cancelTakePicture();
    }

    public void takePicture()
    {
        //hw shutter button must get stopped else burstableTakePicture does not trigger
        getCameraEx().stopDirectShutter(new CameraEx.DirectShutterStoppedCallback() {
            @Override
            public void onShutterStopped(CameraEx cameraEx) {
                cameraEx.burstableTakePicture();
            }
        });
    }

    public void takePictureCallback(Camera.PictureCallback jpegCallback) {
        getCameraEx().getNormalCamera().takePicture(
            () -> {
                Log.d(TAG, "On shutter");

                getCameraEx().cancelTakePicture();
            },
            null, // rawCallback
            jpegCallback
        );
    }

    @Override
    public void onShutterSequence(CameraSequence.RawData rawData, CameraSequence cameraSequence) {
        Log.d(TAG,"onShutterSequence");
        m_camera.cancelTakePicture();
        //cameraSequence.setReleaseLock(true);
    }

  /*  @Override
    public void onSplitShutterSequence(CameraSequence.RawData rawData, CameraSequence.SplitExposureProgressInfo splitExposureProgressInfo, CameraSequence cameraSequence) {
        Log.d(TAG, "onSplitShutterSequence();");
        cameraSequence.setReleaseLock(false);
    }

    @Override
    public void onShutterSequence(CameraSequence.RawData rawData, CameraSequence cameraSequence) {
        Log.d(TAG,"onShutterSequence");
        cameraSequence.setReleaseLock(false);
    }*/


    public void initCamera()
    {
        autoPictureReviewControl = new CameraEx.AutoPictureReviewControl();
        m_camera.setAutoPictureReviewControl(getAutoPictureReviewControls());
        autoPictureReviewControl.setPictureReviewTime(0);
        autoPictureReviewControl.cancelAutoPictureReview();

        initParameters();

        //when false cameraparameters contains only the active parameters, but supported stuff is missing
        m_camera.withSupportedParameters(true);
    }

    /*public void setOptions(CameraSequence.Options paramOptions)
    {

            if (paramOptions == null) {
                paramOptions = new CameraSequence.Options();
            }
            paramOptions.setOption("AUTO_RELEASE_LOCK_ENABLED", true);
        cameraSequence.setReleaseLock(false);
    }*/

    private void dumpParameter() {
        StringTokenizer localStringTokenizer = new StringTokenizer(((Camera.Parameters)getParameters()).flatten(), ";");
        while (localStringTokenizer.hasMoreElements())
            Log.d(TAG, localStringTokenizer.nextToken());

        List<String> tmp = null;
        if (isImageStabSupported())
        {
            Log.d(TAG,"getSupportedImageStabModes");
            logList(getSupportedImageStabModes());
        }
        if (isLiveSlowShutterSupported()) {
            Log.d(TAG, "getSupportedLiveSlowShutterModes");
            logArray(getSupportedLiveSlowShutterModes());
        }
    }

    private void logList(List<String> list)
    {
        String st = new String();
        for (String s : list)
            st += s+",";
        Log.d(TAG,st);
    }
    private void logArray(String[] list)
    {
        String st = new String();
        for (String s : list)
            st += s+",";
        Log.d(TAG,st);
    }
}

