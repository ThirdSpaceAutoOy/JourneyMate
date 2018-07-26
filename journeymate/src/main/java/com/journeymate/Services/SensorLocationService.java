package com.journeymate.Services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.journeymate.R;

import com.journeymate.Receivers.LocationServiceAutoStartReceiver;
import com.journeymate.Receivers.TransitionReceiver;
import com.journeymate.SensorHelper.JourneySensorHelper;
import com.journeymate.SensorHelper.LocationHelper;
import com.journeymate.Utils.LogUtils;
import com.journeymate.Utils.MarshMallowPermission;
import com.journeymate.Utils.PreferencesManager;

import java.util.Calendar;
import java.util.List;

/**
 * @author Ankur Khandelwal on 01/04/18.
 */

public class SensorLocationService extends Service {
    private static final String TAG = SensorLocationService.class.getSimpleName();
    private static final long DETECTION_INTERVAL_IN_MILLISECONDS = 2000;
    private Context context;
    private boolean isGyroAvailable = true;
    private JourneySensorHelper journeySensorHelper;
    private LocationHelper locationHelper;
    private boolean isRunning = false;
    private PowerManager.WakeLock mWakeLock;
    private ActivityRecognitionClient mActivityRecognitionClient;

    @Override
    public IBinder onBind(Intent arg0) {
        Bundle extras = arg0.getExtras();
        LogUtils.printLog("service","onBind");

        if (extras != null) {
            LogUtils.printLog("service","onBind with extra");
        }
        return null;
    }


    @Override
    public void onCreate() {
        LogUtils.printLog(TAG,"service onCreate");
        context = this;
        this.isRunning = false;
        acquireWakeLock();
        super.onCreate();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            String channelId = createNotificationChannel();
            Notification notification = new NotificationCompat.Builder(this,channelId)
                    .setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_tsa)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();

            startForeground(191,notification);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(){
        String channelId = "sensor_service";
        String channelName = "JourneyMate Activity";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLACK);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager!=null) {
            manager.createNotificationChannel(channel);
        }
        return channelId;
    }

    public void acquireWakeLock() {
        final PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        releaseWakeLock();
        //Acquire new wake lock
        if(powerManager!=null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PARTIAL_WAKE_LOCK");
            mWakeLock.acquire();
        }
    }

    public void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        LogUtils.printLog(TAG , "====== onStartCommand =====");
        PreferencesManager.putBoolean("isLocationServiceRunning",true);
        if(!this.isRunning) {
            this.isRunning = true;
        }
        isGyroAvailable = intent.getBooleanExtra("isGyroAvailable",true);
        init();
        return START_STICKY;
    }

    private void init() {
        startSensorUpdate();
        startActivityRecognitionService();
    }

    private void startActivityRecognitionService() {
        mActivityRecognitionClient = ActivityRecognition.getClient(context);
        requestActivityRecognitionHandler();
    }

    private void requestActivityRecognitionHandler() {
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_IN_MILLISECONDS, getActivityDetectionPendingIntent());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                LogUtils.printLog(TAG, "task success ");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Task failed");
            }
        });
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(context,TransitionReceiver.class);
        return PendingIntent.getBroadcast(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void startSensorUpdate(){
        journeySensorHelper = new JourneySensorHelper(context,isGyroAvailable);
        journeySensorHelper.init();

        locationHelper = new LocationHelper(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.isRunning = false;
        LogUtils.printLog(TAG,"service OnDestroy");

        Intent broadcastIntent = new Intent("restartSensorService");
        sendBroadcast(broadcastIntent);
    }


    @Override public boolean stopService(Intent name) {
        LogUtils.printLog(TAG,"stop service");
        deInit();

        return super.stopService(name);
    }

    @Override public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        PreferencesManager.putBoolean("isLocationServiceRunning",false);
        LogUtils.printLog(TAG,"onTaskRemoved");

        /**
         * start service from broadcast receiver,that way it won't die.
         */
        sendBroadcast(new Intent(this,LocationServiceAutoStartReceiver.class));
    }

    private void deInit() {

        if(journeySensorHelper!=null){
            journeySensorHelper.unregisterSensor();
        }
        if(locationHelper!=null){
            locationHelper.stopLocationUpdates();
        }
    }
}