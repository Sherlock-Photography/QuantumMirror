package com.obsidium.bettermanual.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.Printer;

import com.obsidium.bettermanual.Preferences;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.sony.scalar.hardware.CameraEx;

public class BatteryObserverModel extends AbstractModel<String>
{

    private BatteryObserver batteryObserver;

    public BatteryObserverModel(CameraInstance camera) {
        super(camera);
        batteryObserver = new BatteryObserver(Preferences.GET().getContext());
    }


    public void start()
    {
        batteryObserver.startListing();
    }

    public void stop()
    {
        batteryObserver.stopListing();
    }

    @Override
    public void setValue(int i) {

    }

    @Override
    public String getValue() {
        return Math.round((batteryObserver.level * 1.0 / batteryObserver.max) * 100) + "%";
    }

    @Override
    public boolean isSupported() {
        return true;
    }

   private class BatteryObserver extends BroadcastReceiver
   {
       private  final String TAG = BatteryObserver.class.getSimpleName();
       private static final String _EXTRA_PRESENT = "present";
       private static final String _PLUGGED = "plugged";
       private static final String _STATUS = "status";

       public int max;
       public int level;

       private Context context;

       public BatteryObserver(Context context)
       {
           this.context = context;
       }

       public void startListing()
       {
           IntentFilter filter = new IntentFilter();
           filter.addAction("android.intent.action.BATTERY_CHANGED");
           Log.d(TAG, "Start BatteryObserver");
           Intent intent = context.registerReceiver(this, filter);
           if (intent != null) {
               onReceive(context, intent);
           }
       }

       public void stopListing()
       {
           context.unregisterReceiver(this);
       }


       @Override
       public void onReceive(Context context, Intent intent) {
           String action = intent.getAction();
           if (action.equals("android.intent.action.BATTERY_CHANGED")) {
               Bundle data = intent.getExtras();
               max = data.getInt("scale", 0);
               level = max == 0 ? 0 : (data.getInt("level", 0) * 100) / max;

               int plugged = intent.getIntExtra(_PLUGGED, 0);
               int status = intent.getIntExtra(_STATUS, 0);
               boolean present = intent.getBooleanExtra(_EXTRA_PRESENT, false);
               BatteryObserverModel.this.fireOnValueChanged();
           }
       }
   }

}
