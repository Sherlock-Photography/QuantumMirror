package com.obsidium.bettermanual;

import android.app.DAConnectionManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ma1co.pmcademo.app.BaseActivity;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.controller.BatteryObserverController;
import com.obsidium.bettermanual.controller.ShutterController;
import com.obsidium.bettermanual.layout.BaseLayout;
import com.obsidium.bettermanual.layout.CameraUiFragment;
import com.obsidium.bettermanual.layout.ImageFragment;
import com.obsidium.bettermanual.layout.MinShutterFragment;
import com.obsidium.bettermanual.layout.PreviewMagnificationFragment;
import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.meta.FaceInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.github.ma1co.openmemories.framework.DateTime;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.concurrent.*;

/**
 * Created by KillerInk on 27.08.2017.
 */

public class MainActivity extends BaseActivity implements ActivityInterface, CameraInstance.CameraEvents, SurfaceHolder.Callback,CameraEx.ShutterListener {

    private final String TAG = MainActivity.class.getSimpleName();

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

    private BaseLayout currentLayout;

    private AvIndexManager avIndexManager;

    private String m_endPointPrefix;
    private String m_bearerToken;
    private ExecutorService m_dalleExecutor = Executors.newSingleThreadExecutor();

    private boolean m_facesPresent = false;

    private WifiManager wifiManager;
    private BroadcastReceiver wifiStateReceiver;
    private BroadcastReceiver supplicantStateReceiver;
    private BroadcastReceiver networkStateReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");

        Preferences.CREATE(getApplicationContext());

        super.onCreate(savedInstanceState);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
        m_handler = new Handler();
        setContentView(R.layout.main_activity);
        ShutterController.GetInstance().bindActivityInterface(this);

        surfaceViewHolder = (FrameLayout) findViewById(R.id.surfaceView);
        //surfaceView.setOnTouchListener(new CameraUiFragment.SurfaceSwipeTouchListener(getContext()));
        avIndexManager = new AvIndexManager(getContentResolver(),getApplicationContext());

        layoutHolder = (LinearLayout)findViewById(R.id.fragment_holder);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            }
        };

        supplicantStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                networkStateChanged(WifiInfo.getDetailedStateOf((SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
            }
        };

        networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                networkStateChanged(((NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)).getDetailedState());
            }
        };

        try {
            loadBearerToken();
        } catch (Exception e) {
            Log.e(TAG, "Bad TOKEN.TXT file: " + e);
            showDallEProgress("Bad TOKEN.TXT file: " + e, false);
        }
    }

    private void loadBearerToken() throws IOException {
        File filename = new File(Environment.getExternalStorageDirectory(), "TOKEN.TXT");

        Log.d(TAG, "Loading bearer token from " + filename.toString());

        BufferedReader reader = new BufferedReader(new FileReader(filename));

        String line = reader.readLine().trim();
        if (!line.startsWith("http")) {
            throw new RuntimeException("Missing endpoint url");
        }

        if (!line.endsWith("/")) {
            line = line + "/";
        }

        m_endPointPrefix = line;

        line = reader.readLine().trim();

        if (line.length() < 16) {
            throw new RuntimeException("Missing bearer token");
        }

        m_bearerToken = line;

        reader.close();
    }

    protected void wifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                reportWiFiState("Enabling wifi");
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                reportWiFiState("Starting wifi");
                break;
        }
    }

    private void showMessageDelayed(String message) {
        Log.d(TAG, message);
        if (currentLayout instanceof CameraUiFragment) {
            ((CameraUiFragment) currentLayout).showMessageDelayed(message);
        }
    }

    private void showMessagePersistent(String message) {
        Log.d(TAG, message);
        if (currentLayout instanceof CameraUiFragment) {
            ((CameraUiFragment) currentLayout).showMessage(message);
        }
    }

    private void reportWiFiState(String message) {
        showMessagePersistent(message);
    }

    protected void networkStateChanged(NetworkInfo.DetailedState state) {
        String ssid = wifiManager.getConnectionInfo().getSSID();
        switch (state) {
            case CONNECTING:
                if (ssid != null)
                    reportWiFiState(ssid + ": Connecting");
                break;
            case AUTHENTICATING:
                reportWiFiState(ssid + ": Authenticating");
                break;
            case OBTAINING_IPADDR:
                reportWiFiState(ssid + ": Obtaining IP");
                break;
            case CONNECTED:
                wifiConnected();
                break;
            case DISCONNECTED:
                reportWiFiState("Wifi disconnected");
                break;
            case FAILED:
                reportWiFiState("Connection failed");
                break;
        }
    }

    protected void wifiConnected() {
        showMessageDelayed("Wifi connected");
    }


    private String readStreamAsString(InputStream in) throws IOException {
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];

        StringBuilder out = new StringBuilder();

        Reader reader = new InputStreamReader(in, "UTF-8");

        for (int numRead; (numRead = reader.read(buffer, 0, buffer.length)) > 0; ) {
            out.append(buffer, 0, numRead);
        }

        return out.toString();
    }

    private void logDallEToFile(String msg) {
        File file = new File(Environment.getExternalStorageDirectory(), "DALL-E.TXT");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

            writer.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(DateTime.getInstance().getCurrentTime().getTime()));
            writer.append(" - ");
            writer.append(msg);
            writer.newLine();
            writer.close();
        } catch (IOException e) {}
    }

    private JSONObject uploadDallEPicture(final byte[] bytes) throws IOException, JSONException {
        URL u = new URL(m_endPointPrefix + "submitImage");

        HttpURLConnection c = (HttpURLConnection) u.openConnection();

        c.setRequestMethod("POST");
        c.setRequestProperty("X-Authorization", "Bearer " + m_bearerToken);
        c.setRequestProperty("Content-Type", "application/binary");
        c.setDoOutput(true);
        c.setDoInput(true);

        OutputStream out = c.getOutputStream();

        // Maybe we can save memory by chunking our writes here:
        int chunkSize = 16384;
        int chunkStart = 0;

        while (chunkStart < bytes.length) {
            out.write(bytes, chunkStart, Math.min(bytes.length - chunkStart, chunkSize));
            chunkStart += chunkSize;
        }

        out.close();

        c.connect();

        if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error " + c.getResponseCode() + " (" + c.getResponseMessage() + ")");
        }

        InputStream in = c.getInputStream();

        return (JSONObject) new JSONTokener(readStreamAsString(in)).nextValue();
    }

    private JSONObject pollDallETask(final String taskID) throws IOException, JSONException {
        URL u = new URL(m_endPointPrefix + "pollTask/" + taskID);

        HttpURLConnection c = (HttpURLConnection) u.openConnection();

        c.setUseCaches(false);
        c.setRequestMethod("GET");
        c.setRequestProperty("X-Authorization", "Bearer " + m_bearerToken);
        c.setDoInput(true);

        c.connect();

        if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error " + c.getResponseCode() + " (" + c.getResponseMessage() + ")");
        }

        InputStream in = c.getInputStream();

        return (JSONObject) new JSONTokener(readStreamAsString(in)).nextValue();
    }

    private InputStream startDallEImageDownload(String url) throws IOException {
        URL u = new URL(url.replace("https://openailabsprodscus.blob.core.windows.net/", m_endPointPrefix + "getImage/"));

        HttpURLConnection c = (HttpURLConnection) u.openConnection();

        c.setUseCaches(false);
        c.setRequestMethod("GET");
        c.setDoInput(true);

        c.connect();

        if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error " + c.getResponseCode() + " (" + c.getResponseMessage() + ")");
        }

        return c.getInputStream();
    }

    /**
     * Send messages from the Dall-E threads to the main thread for display.
     */
    private void showDallEProgress(final String message, boolean delayedClear) {
        if (delayedClear) {
            m_handler.post(() -> showMessageDelayed(message));
        } else {
            m_handler.post(() -> showMessagePersistent(message));
        }
    }

    private void runDallE(final byte[] jpegBytes) {
        m_dalleExecutor.execute(() -> {
            showDallEProgress("Uploading to DALL-E...", false);

            try {
                JSONObject responseJSON = uploadDallEPicture(jpegBytes);
                String status = responseJSON.optString("status", "failed");
                String taskID;
                boolean success = false;

                if (!("success".equals(status) || "pending".equals(status))) {
                    // Check for a friendly-formatted error from OpenAI
                    if (responseJSON.getJSONObject("error") != null) {
                        String message = responseJSON.getJSONObject("error").getString("message");

                        Log.e(TAG, "Upload error: " + message);
                        logDallEToFile("Upload error: " + message);
                        showDallEProgress("Error: " + message, false);

                        return;
                    }

                    throw new RuntimeException("Task submit failed");
                }

                taskID = responseJSON.getString("id");

                logDallEToFile("Submitted: https://labs.openai.com/e/" + taskID.replace("task-", ""));

                for (int i = 0; i < 40; i++) {
                    Log.d(TAG, "DALL-e task status: " + status);

                    if ("pending".equals(status)) {
                        showDallEProgress("AI working...", false);

                        Thread.sleep(3000);
                        responseJSON = pollDallETask(taskID);

                        status = responseJSON.getString("status");
                    } else if ("succeeded".equals(status)) {
                        success = true;
                        break;
                    } else {
                        throw new RuntimeException("Task failed");
                    }
                }

                if (!success) {
                    throw new RuntimeException("Timed out waiting for AI");
                }

                showDallEProgress("Downloading images...", false);

                JSONArray generations = responseJSON.getJSONObject("generations").getJSONArray("data");

                CountDownLatch downloadCounter = new CountDownLatch(generations.length());

                for (int i = 0; i < generations.length(); i++) {
                    JSONObject generation = generations.getJSONObject(i);
                    final String imageURL = generation.getJSONObject("generation").getString("image_path");

                    m_handler.post(() -> {
                        try {
                            avIndexManager.importImage(startDallEImageDownload(imageURL));
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                        } finally {
                            downloadCounter.countDown();;
                        }
                    });
                }

                downloadCounter.await();

                m_handler.post(() -> {
                    showMessageDelayed("Complete!");
                });
            } catch (final Exception e) {
                Log.e(TAG, "Upload error: " + CustomExceptionHandler.stacktraceToString(e));
                logDallEToFile("Upload error: " + CustomExceptionHandler.stacktraceToString(e));
                showDallEProgress("Error: " + e, false);
            }
        });
    }

    public void takePhoto() {
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    CameraInstance.GET().takePictureCallback(new Camera.PictureCallback() {
                             @Override
                             public void onPictureTaken(byte[] bytes, Camera camera) {
                                 Log.d(TAG, "Captured jpeg, size: " + bytes.length);

                                 if (m_bearerToken == null) {
                                     showDallEProgress("No TOKEN.TXT file found!", true);
                                 } else if (m_facesPresent) {
                                     showDallEProgress("Not uploading due to faces", true);
                                 } else {
                                     runDallE(bytes);
                                 }
                             }
                         }
                    );
                } catch (Exception e) {
                    //takePicture throws an exception if the camera is busy, for example
                    showDallEProgress("Error: " + e, true);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        BatteryObserverController.GetInstance().bindView((TextView)findViewById(R.id.textView_battery));
        if (avIndexManager != null) {
            registerReceiver(avIndexManager, avIndexManager.AVAILABLE_SIZE_INTENTS);
            registerReceiver(avIndexManager, avIndexManager.MEDIA_INTENTS);

            avIndexManager.onResume(getApplicationContext());
        }
        addSurfaceView();

        CameraInstance.GET().setCameraEventsListener(MainActivity.this);

        registerReceiver(wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        registerReceiver(supplicantStateReceiver, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
        registerReceiver(networkStateReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        wifiManager.setWifiEnabled(true);
        setAutoPowerOffMode(false);

        /*
        try {
            avIndexManager.importImage(new FileInputStream(new File(Environment.getExternalStorageDirectory(), "image.jpg")));
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
         */
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        BatteryObserverController.GetInstance().bindView(null);
        if (avIndexManager != null) {
            unregisterReceiver(avIndexManager);
            avIndexManager.onPause(getApplicationContext());
        }

        CameraInstance.GET().closeCamera();
        removeSurfaceView();
        layoutHolder.removeAllViews();
        super.onPause();

        unregisterReceiver(wifiStateReceiver);
        unregisterReceiver(supplicantStateReceiver);
        unregisterReceiver(networkStateReceiver);
        wifiManager.setWifiEnabled(false);
        setAutoPowerOffMode(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Preferences.CLEAR();
        avIndexManager = null;
        ShutterController.GetInstance().bindActivityInterface(null);
    }



    @Override
    public boolean hasTouchScreen() {
        return getDeviceInfo().getModel().compareTo("ILCE-5100") == 0;
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
        new DAConnectionManager(this).finish();
        finish();
    }

    @Override
    public void setColorDepth(boolean highQuality) {
        super.setColorDepth(highQuality);
    }

    @Override
    public void loadFragment(int fragment) {
        if (fragment == FRAGMENT_IMAGEVIEW && avIndexManager == null)
            return;
        if (currentLayout != null) {
            currentLayout.Destroy();
            layoutHolder.removeAllViews();
        }
        switch (fragment)
        {
            case FRAGMENT_MIN_SHUTTER:
                MinShutterFragment msa = new MinShutterFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListener(msa);
                currentLayout = msa;
                layoutHolder.addView(msa);
                break;
            case FRAGMENT_PREVIEWMAGNIFICATION:
                PreviewMagnificationFragment pmf = new PreviewMagnificationFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListener(pmf);
                currentLayout = pmf;
                layoutHolder.addView(pmf);
                break;
            case FRAGMENT_IMAGEVIEW:
                CameraInstance.GET().closeCamera();
                removeSurfaceView();
                setColorDepth(true);
                ImageFragment imageFragment = new ImageFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListener(imageFragment);
                currentLayout = imageFragment;
                layoutHolder.addView(imageFragment);
                break;
            case FRAGMENT_WAITFORCAMERARDY:
                setColorDepth(false);
                addSurfaceView();
                break;
            case FRAGMENT_CAMERA_UI:
            default:
                setColorDepth(false);
                CameraUiFragment cameraUiFragment = new CameraUiFragment(getApplicationContext(),this);
                getDialHandler().setDialEventListener(cameraUiFragment);
                currentLayout = cameraUiFragment;
                layoutHolder.addView(cameraUiFragment);
                break;

        }
    }

    @Override
    public void setSurfaceViewOnTouchListener(View.OnTouchListener onTouchListener) {
        if (surfaceView != null)
            surfaceView.setOnTouchListener(onTouchListener);
    }

    @Override
    public String getResString(int id) {
        return getResources().getString(id);
    }

    @Override
    public AvIndexManager getAvIndexManager() {
        return avIndexManager;
    }

    @Override
    public void onCameraOpen(boolean isOpen) {
        Log.d(TAG, "onCameraOpen");
        // CameraInstance.GET().enableHwShutterButton();
        CameraInstance.GET().setShutterListener(this);
        CameraInstance.GET().setSurfaceHolder(m_surfaceHolder);
        CameraInstance.GET().startPreview();


        loadFragment(FRAGMENT_CAMERA_UI);
    }

    @Override
    public void onFaceDetected(FaceInfo[] faces) {
        m_facesPresent = faces != null && faces.length > 0;

        if (m_facesPresent) {
            showMessageDelayed("Warning: No faces allowed!");
        }
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
        m_surfaceHolder.removeCallback(this);
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
        // i: 0 = success, 1 = canceled, 2 = error
        Log.d(TAG, String.format("onShutter i: %d\n", i));
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
