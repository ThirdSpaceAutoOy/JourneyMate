package com.journeymate.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.journeymate.Services.SensorLocationService;
import com.journeymate.Utils.LogUtils;
import com.journeymate.Utils.PreferencesManager;
import com.journeymate.Utils.StringConstants;
import com.journeymate.Utils.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


/**
 * @author Ankur Khandelwal on 13/04/18.
 */

public class TransitionReceiver extends BroadcastReceiver {
    private String TAG = TransitionReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.printLog(TAG, "Got ActivityTransition Intent!!!!");
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        /**
         * here we will apply algo to find the most probable activity.
         * first remove UNKONWN_ACTIVITY and TILTING from the arrayList
         * then we keep first two most probable in another list a
         * if the first value is more than 65... i.e. activity is found
         * else if lastActivity is present in mostProbableList .. i.e. keep that as activity and update timestamp
         * else take the first one as activity.
         */
        if (result != null && result.getProbableActivities() != null) {
            ArrayList<DetectedActivity> resultdetectedActivities = (ArrayList) result.getProbableActivities();
            ArrayList<DetectedActivity> detectedActivities = new ArrayList<>();

            for (int i = 0; i < resultdetectedActivities.size(); i++) {
                if (resultdetectedActivities.get(i).getType() != DetectedActivity.UNKNOWN) {
                    if (resultdetectedActivities.get(i).getType() != DetectedActivity.TILTING) {
                        detectedActivities.add(resultdetectedActivities.get(i));
                    }
                }
            }

            if (detectedActivities.size() > 1) {
                ArrayList<DetectedActivity> mostProbableList = new ArrayList<>();
                mostProbableList.add(detectedActivities.get(0));
                mostProbableList.add(detectedActivities.get(1));

                int lastUpdatedActivityId = PreferencesManager.getInt(StringConstants.PREVIOUS_ACTIVITY_ID, -1);
                if (lastUpdatedActivityId == -1) {
                    //set activity as first one
                    setFoundActivity(detectedActivities.get(0), detectedActivities);
                } else {
                    if (detectedActivities.get(0).getConfidence() > 65) {
                        //first activity is the activity
                        setFoundActivity(detectedActivities.get(0), detectedActivities);
                    } else {
                        boolean isActivitypresent = false;
                        int index = -1;
                        for (int i = 0; i < mostProbableList.size(); i++) {
                            if (lastUpdatedActivityId == mostProbableList.get(i).getType()) {
                                //this means last activity present in mostProbable list... i.e. keep the last activity as final activity
                                isActivitypresent = true;
                                index = i;
                            }
                        }

                        if (isActivitypresent && index >= 0) {
                            setFoundActivity(mostProbableList.get(index), detectedActivities);
                        } else {
                            setFoundActivity(detectedActivities.get(0), detectedActivities);
                        }
                    }
                }
                mostProbableList.clear();
            } else if (detectedActivities.size() == 1) {
                //found the activity
                setFoundActivity(detectedActivities.get(0), detectedActivities);
            }
        }

        /**
         * try to start Service from here
         *
         */

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

    private void startService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, SensorLocationService.class));
        } else {
            context.startService(new Intent(context, SensorLocationService.class));
        }
    }

    private void setFoundActivity(DetectedActivity detectedActivity, ArrayList<DetectedActivity> detectedActivities) {
        String activity = Utility.getActivityString(detectedActivity.getType());
        long timestamp = System.currentTimeMillis();
        String previousActivity = PreferencesManager.getString(StringConstants.KEY_CURRENT_ACTIVITY, "");
        PreferencesManager.putString(StringConstants.KEY_CURRENT_ACTIVITY, activity.toLowerCase());
        PreferencesManager.putInt(StringConstants.PREVIOUS_ACTIVITY_ID, detectedActivity.getType());
        PreferencesManager.putLong("lastActivityUpdate", timestamp);
    }
}
