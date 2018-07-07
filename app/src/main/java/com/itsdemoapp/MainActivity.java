package com.itsdemoapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.itsdemoapp.Utils.LogUtils;
import com.journeymate.JourneyActivity;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private Context context;
    private Activity activity;
    private MarshMallowPermission marshMallowPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        activity = this;

        //check location permission for devices with SDK > marshmallow.
        marshMallowPermission = new MarshMallowPermission(activity);
        if(marshMallowPermission.checkPermissionForLocation()){
            //permission is given
            initJourneyMateService();

        }else{
            marshMallowPermission.requestPermissionForLocation();
        }
    }

    private void initJourneyMateService() {
        /**
         * Implement this code to start journeyMate implementation
         * before this make sure you ask location permission.
         */

        JourneyActivity journeyActivity = new JourneyActivity();
        journeyActivity.init(context,activity);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.printLog(TAG, "onRequestPermissionResult = " + requestCode);
        switch (requestCode) {
            case 11:
                LogUtils.printLog(TAG, "location permission ");
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Access location Permission Denied");
                } else {
                    LogUtils.printLog(TAG, "Access location permission granted");
                    initJourneyMateService();
                }
                break;
        }
    }
}
