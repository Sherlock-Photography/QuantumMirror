package com.obsidium.bettermanual.layout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.obsidium.bettermanual.ActivityInterface;
import com.obsidium.bettermanual.MainActivity;
import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.obsidium.bettermanual.controller.ApertureController;
import com.obsidium.bettermanual.controller.Controller;
import com.obsidium.bettermanual.controller.ExposureCompensationController;
import com.obsidium.bettermanual.controller.ExposureHintController;
import com.obsidium.bettermanual.controller.ExposureModeController;
import com.obsidium.bettermanual.controller.FocusDriveController;
import com.obsidium.bettermanual.controller.HistogramController;
import com.obsidium.bettermanual.controller.ImageStabilisationController;
import com.obsidium.bettermanual.controller.IsoController;
import com.obsidium.bettermanual.controller.ShutterController;
import com.obsidium.bettermanual.views.GridView;
import com.obsidium.bettermanual.views.HistogramView;

import java.util.ArrayList;
import java.util.List;


public class CameraUiFragment extends BaseLayout implements View.OnClickListener,
        CameraUiInterface
{



    private static final boolean LOGGING_ENABLED = false;
    private static final int MESSAGE_TIMEOUT = 1000;
    private final  String TAG  = CameraUiFragment.class.getSimpleName();

    private TextView        m_tvLog;
    private TextView        m_tvMsg;
    private HistogramView m_vHist;
    private GridView m_vGrid;
    private TextView        m_tvHint;
    private View            m_lFocusScale;

    private LinearLayout bottomHolder;
    private LinearLayout leftHolder;

    private List<Controller> dialViews;
    private int lastDialView;

    private final Runnable  m_hideMessageRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            m_tvMsg.setVisibility(View.GONE);
        }
    };

    private Runnable[] histogramViewRunners;

    private int             m_viewFlags;

    public CameraUiFragment(Context context, ActivityInterface activityInterface)
    {
        super(context,activityInterface);
        inflateLayout(R.layout.camera_ui_fragment);
        this.activityInterface = activityInterface;

        dialViews = new ArrayList();
        bottomHolder = (LinearLayout)findViewById(R.id.bottom_holder);
        leftHolder = (LinearLayout)findViewById(R.id.left_holder);

        m_tvLog = (TextView)findViewById(R.id.tvLog);
        m_tvLog.setVisibility(LOGGING_ENABLED ? View.VISIBLE : View.GONE);

        histogramViewRunners = new Runnable[2];
        histogramViewRunners[0] = () -> {
            Log.d(TAG, "Histo:false");
            m_vHist.setVisibility(GONE);
        };
        histogramViewRunners[1] = () -> {
            Log.d(TAG, "Histo:true");
            m_vHist.setVisibility(VISIBLE);
        };

        m_tvMsg = (TextView)findViewById(R.id.tvMsg);

        m_vGrid = (GridView)findViewById(R.id.vGrid);

        m_tvHint = (TextView)findViewById(R.id.tvHint);
        m_tvHint.setVisibility(View.GONE);

        FocusDriveController.GetInstance().bindView(findViewById(R.id.lFocusScale));

        //noinspection ResourceType
        ((ImageView)findViewById(R.id.ivFocusRight)).setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_rec_focuscontrol_far));
        //noinspection ResourceType
        ((ImageView)findViewById(R.id.ivFocusLeft)).setImageResource(getResources().getInteger(R.integer.p_16_dd_parts_rec_focuscontrol_near));

        ExposureModeController.GetInstance().bindView((ImageView) findViewById(R.id.iv_exposuremode));
        dialViews.add(ExposureModeController.GetInstance());

        ImageStabilisationController.GetInstance().bindView((ImageView) findViewById(R.id.iv_imagestab));
        dialViews.add(ImageStabilisationController.GetInstance());

        ShutterController.GetInstance().bindView((TextView)findViewById(R.id.shutter_txt));
        dialViews.add(ShutterController.GetInstance());

        ApertureController.GetInstance().bindView((TextView)findViewById(R.id.aperture_txt));
        dialViews.add(ApertureController.GetInstance());

        IsoController.GetInstance().bindView((TextView)findViewById(R.id.iso_txt));
        dialViews.add(IsoController.GetInstance());

        ExposureCompensationController.GetInstance().bindView((TextView)findViewById(R.id.evcopmensation_txt));
        dialViews.add(ExposureCompensationController.GetInstance());

        TextView m_tvExposure = (TextView) findViewById(R.id.evhint_txt);
        m_tvExposure.setCompoundDrawablesWithIntrinsicBounds(getResources().getInteger(R.integer.p_meteredmanualicon), 0, 0, 0);
        ExposureHintController.GetInstance().bindView(m_tvExposure);
        //dialViews.add(ExposureHintController.GetInstance());

                //then set the key event listener to avoid nullpointer
        activityInterface.getDialHandler().setDialEventListener(CameraUiFragment.this);

        m_vGrid.setVideoRect(activityInterface.getDisplayManager().getDisplayedVideoRect());

        // Preview/Histogram
        m_vHist = (HistogramView)findViewById(R.id.vHist);
        HistogramController.GetInstance().bindView(m_vHist);

        //returns when a capture is done, seems to replace the default android camera1 api CaptureCallback that get called with Camera.takePicture(shutter,raw, jpeg)
        //also it seems Camera.takePicture is nonfunctional/crash on a6000
        //activityInterface.getCamera().setShutterListener(this);

        m_viewFlags = Preferences.GET().getViewFlags(0);
        setDialMode(Preferences.GET().getDialMode(0));

        updateViewVisibility();

        Log.d(TAG,"initUiEnd");
    }

    public void Destroy()
    {
        Preferences.GET().setViewFlags(m_viewFlags);
        Preferences.GET().setDialMode(lastDialView);

        dialViews.clear();

        ApertureController.GetInstance().bindView(null);
        ShutterController.GetInstance().bindView(null);
        IsoController.GetInstance().bindView(null);
        ExposureCompensationController.GetInstance().bindView(null);
        ExposureHintController.GetInstance().bindView(null);
        ExposureModeController.GetInstance().bindView(null);
        ImageStabilisationController.GetInstance().bindView(null);
        HistogramController.GetInstance().bindView(null);
    }


    /* ##############################################################################
       ###### CameraUiInterface impl ###
       #################################  */

    @Override
    public void showMessageDelayed(String msg)
    {
        showMessage(msg);
        activityInterface.getMainHandler().removeCallbacks(m_hideMessageRunnable);
        activityInterface.getMainHandler().postDelayed(m_hideMessageRunnable, MESSAGE_TIMEOUT);
    }

    @Override
    public void showMessage(final String msg)
    {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_tvMsg.setText(msg);
                m_tvMsg.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void hideMessage()
    {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_tvMsg.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void showHintMessage(final String msg) {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_tvHint.setText(msg);
                if (m_tvHint.getVisibility() != VISIBLE)
                    m_tvHint.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void hideHintMessage() {
        activityInterface.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                m_tvHint.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public int getActiveViewsFlag() {
        return m_viewFlags;
    }

    @Override
    public void setActiveViewFlag(int viewsToShow) {
        m_viewFlags = viewsToShow;
    }


    @Override
    public ActivityInterface getActivityInterface() {
        return activityInterface;
    }

    @Override
    public void updateViewVisibility()
    {

    }


    @Override
    public void setLeftViewVisibility(boolean visible)
    {
        final int visibility = visible ? View.VISIBLE : View.GONE;
        leftHolder.setVisibility(visibility);
        bottomHolder.setVisibility(visibility);
    }

    private void changeHistogramGridViewVisibility(int val)
    {
        m_viewFlags += val;

        if (m_viewFlags > histogramViewRunners.length-1)
            m_viewFlags = 0;
        if (m_viewFlags < 0)
            m_viewFlags = histogramViewRunners.length-1;
        Log.d(TAG, "viewFLags:" + m_viewFlags);
        activityInterface.getMainHandler().post(histogramViewRunners[m_viewFlags]);
    }



    // OnClickListener
    public void onClick(View view)
    {
        /*if(view instanceof BaseImageView)
            ((BaseImageView) view).toggle();
        else
        if (view.equals(timelapse))
            timelapse.prepare();
        else if (view.equals(bracket))
            bracket.prepare();*/
    }

    private void setDialMode(final int mode)
    {
        Log.d(TAG , "setDialMode:" +mode);
        Controller lastView = dialViews.get(lastDialView);
        if (lastView == null)
            return;
        lastView.setColorToView(Color.WHITE);
        lastDialView = lastDialView + mode;
        if (lastDialView >= dialViews.size())
            lastDialView = 0;
        else if(lastDialView < 0)
            lastDialView = dialViews.size()-1;

        lastView = dialViews.get(lastDialView);
        lastView.setColorToView(Color.GREEN);
        try {
            if (lastView.getNavigationHelpID() != 0)
                showHintMessage(getResources().getString(lastView.getNavigationHelpID()));
        }
        catch (Resources.NotFoundException ex)
        {
            ex.printStackTrace();
        }

    }




    /*  ##################################################################
        ## Key events impl ##
        ##################### */

    @Override
    public boolean onUpperDialChanged(int value)
    {
        dialViews.get(lastDialView).set_In_De_crase(value);
        return true;
    }

    @Override
    public boolean onLowerDialChanged(int value) {
        dialViews.get(lastDialView).set_In_De_crase(value);
        return true;
    }

    @Override
    public boolean onEnterKeyUp()
    {
        Controller view = dialViews.get(lastDialView);
        view.toggle();
        Log.d(TAG,"onEnterKeyDown");
       /* if (view instanceof BaseImageView)
            ((BaseImageView) view).toggle();
        else if (view instanceof BaseTextView)
            ((BaseTextView) view).onClick();*/
       try {
           showHintMessage(getResources().getString(view.getNavigationHelpID()));
       }
       catch (Resources.NotFoundException ex)
       {
           ex.printStackTrace();
       }

        return true;
    }

    @Override
    public boolean onFnKeyDown() {
        return false;
    }

    @Override
    public boolean onFnKeyUp() {
        CameraInstance.GET().cancelCapture();

        return false;
    }

    @Override
    public boolean onAelKeyDown() {
        return false;
    }

    @Override
    public boolean onAelKeyUp() {
        activityInterface.loadFragment(MainActivity.FRAGMENT_PREVIEWMAGNIFICATION);
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
        hideMessage();
        return false;
    }

    @Override
    public boolean onFocusKeyUp() {
        return false;
    }

    @Override
    public boolean onEnterKeyDown()
    {
        return false;
    }

    @Override
    public boolean onUpKeyDown()
    {
        return true;
    }

    @Override
    public boolean onUpKeyUp()
    {
        setDialMode(-1);

        return true;
    }

    @Override
    public boolean onDownKeyDown()
    {
        return true;
    }

    @Override
    public boolean onDownKeyUp()
    {
        setDialMode(1);
        return true;
    }

    @Override
    public boolean onLeftKeyDown()
    {
        return true;
    }

    @Override
    public boolean onLeftKeyUp()
    {
        // Toggle visibility of some views
        changeHistogramGridViewVisibility(1);
        return false;
    }

    @Override
    public boolean onRightKeyDown()
    {
        return true;
    }

    @Override
    public boolean onRightKeyUp()
    {
        changeHistogramGridViewVisibility(-1);
        return false;
    }

    @Override
    public boolean onShutterKeyUp()
    {
        Log.d(TAG,"onShutterKeyUp");
        return true;
    }

    @Override
    public boolean onShutterKeyDown()
    {
        Log.d(TAG,"onShutterKeyDown");

        activityInterface.takePhoto();

        return true;
    }

    @Override
    public boolean onPlayKeyDown() {
        return false;
    }

    @Override
    public boolean onPlayKeyUp() {
        activityInterface.loadFragment(MainActivity.FRAGMENT_IMAGEVIEW);
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
    public boolean onDeleteKeyUp()
    {
        // Exiting, make sure the app isn't restarted
        activityInterface.closeApp();
        return true;
    }

}
