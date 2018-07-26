package com.journeymate.Utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.journeymate.BuildConfig;

import com.journeymate.JourneyActivity;
import com.journeymate.R;


public class NotificationBuilder {
    private static boolean hasNotificationArrived = false;

    public static void buildNotification(Context context, String notificationTitle, String message) {
        PendingIntent resultIntent;
        hasNotificationArrived = true;
        String title = "";
        Intent intent;
        if (isActivityRunning(context)) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("action_notification"));
            intent = new Intent(context, JourneyActivity.class);
            resultIntent = PendingIntent.getActivity(context, 0, intent, 0);
        } else {
            intent = new Intent(context, JourneyActivity.class);
            resultIntent = PendingIntent.getActivity(context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        }
        if (notificationTitle.isEmpty()) {
            title = context.getResources().getString(R.string.app_name);
        } else {
            title = notificationTitle;
        }
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).
                notify(0, new Builder(context).setSmallIcon(R.mipmap.ic_tsa).
                        setContentTitle(title).setAutoCancel(true).
                        setPriority(1).
                        setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}).
                        setSound(RingtoneManager.getDefaultUri(2)).setContentText(message).
                        setColor(ContextCompat.getColor(context, R.color.colorAccent)).
                        setStyle(new BigTextStyle().bigText(message)).setContentIntent(resultIntent).build());
    }

    public static boolean hasNotificationArrived() {
        return hasNotificationArrived;
    }

    private static boolean isActivityRunning(Context context) {
        for (RunningAppProcessInfo runningAppProcessInfo : ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses()) {
            if (runningAppProcessInfo.processName.equalsIgnoreCase(BuildConfig.APPLICATION_ID) && runningAppProcessInfo.importance == 100) {
                return true;
            }
        }
        return false;
    }
}
