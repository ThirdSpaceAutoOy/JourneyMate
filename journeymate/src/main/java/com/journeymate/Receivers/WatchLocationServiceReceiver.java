package com.journeymate.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.journeymate.Services.SensorLocationService;
import com.journeymate.Utils.LogUtils;
import com.journeymate.Utils.PreferencesManager;


/**
 * @author Ankur Khandelwal on 13/04/18.
 */

public class WatchLocationServiceReceiver extends BroadcastReceiver {
    private String TAG = WatchLocationServiceReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentFilter = intent.getAction();
        LogUtils.printLog(TAG,"onReceive");
        if(intentFilter!=null && intentFilter.equals("com.journeyMate.Main")) {
            LogUtils.printLog(TAG, "Yayyy intent received!!!! & service running - " + PreferencesManager.getBoolean("isLocationServiceRunning", false));
            if (!PreferencesManager.getBoolean("isLocationServiceRunning", false)) {
                startService(context);
            }else{
                long lastUpdatedTime = PreferencesManager.getLong("lastServiceUpdateTime",0L);
                long now = System.currentTimeMillis();
                if (Math.abs(now - lastUpdatedTime) >= 1000*60*10){
                    LogUtils.printLog(TAG,"last service ran 10 mins before, so starting again");
                    startService(context);
                }
            }
        }
    }

    private void startService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, SensorLocationService.class));
        } else {
            context.startService(new Intent(context, SensorLocationService.class));
        }
    }
}
