package com.journeymate.Services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.journeymate.Receivers.TransitionReceiver;
import com.journeymate.Utils.LogUtils;


/**
 * @author Ankur Khandelwal on 22/04/18.
 */

public class DetectedActivitiesIntentService extends IntentService {

    protected static final String TAG = "DetectedActivitiesIS";
    private ActivityRecognitionClient mActivityRecognitionClient;
    private static final long DETECTION_INTERVAL_IN_MILLISECONDS = 2000;

    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        LogUtils.printLog(TAG,"DetectedActivityOncreate");
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.printLog(TAG, "Got DetectedActivity Intent!!!!");
        LogUtils.printLog(TAG,"--------------------------------------------\n\n");

        mActivityRecognitionClient = ActivityRecognition.getClient(this);
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
        Intent intent = new Intent(this,TransitionReceiver.class);
        return PendingIntent.getBroadcast(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
