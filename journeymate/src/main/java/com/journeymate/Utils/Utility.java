package com.journeymate.Utils;

import android.content.res.Resources;

import com.google.android.gms.location.DetectedActivity;

import java.util.HashMap;

/**
 * @author Ankur Khandelwal on 22/04/18.
 */

public class Utility {
    public static HashMap<String, Object> getAuth() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(StringConstants.KEY_CONTENT_TYPE, StringConstants.CONTENT_TYPE);
        hashMap.put(StringConstants.KEY_AUTH, "Bearer " + getJwtToken());
        return hashMap;
    }

    public static String getJwtToken() {
        return PreferencesManager.getString(StringConstants.KEY_ACCESS_TOKEN , "");
    }

    public static void saveJwtToken(String token) {
        PreferencesManager.putString(StringConstants.KEY_ACCESS_TOKEN, token);
    }

    public static String getActivityString(int detectedActivityType) {
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on bicycle";
            case DetectedActivity.ON_FOOT:
                return "on foot";
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.UNKNOWN:
                return "unknown activity";
            case DetectedActivity.WALKING:
                return "walking";
            default:
                return "unknown activity";
        }
    }

    public static String getTransitionString(int detectedTransitionType){
        return (detectedTransitionType == 0 ? "enter" : "exit");
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


}
