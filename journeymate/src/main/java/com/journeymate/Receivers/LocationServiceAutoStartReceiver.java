package com.journeymate.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.journeymate.Services.SensorLocationService;
import com.journeymate.Utils.LogUtils;


/**
 * @author Ankur Khandelwal on 13/04/18.
 */

public class LocationServiceAutoStartReceiver extends BroadcastReceiver {
    private String TAG = LocationServiceAutoStartReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.printLog(TAG, "Service Stops! Oops!!!!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, SensorLocationService.class));
        } else {
            context.startService(new Intent(context, SensorLocationService.class));
        }
    }
}
