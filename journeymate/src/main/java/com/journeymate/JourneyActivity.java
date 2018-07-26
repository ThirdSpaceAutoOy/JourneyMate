package com.journeymate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.journeymate.Receivers.WatchLocationServiceReceiver;
import com.journeymate.Services.DetectedActivitiesIntentService;
import com.journeymate.Services.SensorLocationService;
import com.journeymate.Utils.HardwareChecker;
import com.journeymate.Utils.LogUtils;
import com.journeymate.Utils.MarshMallowPermission;
import com.journeymate.Utils.PreferencesManager;
import com.journeymate.Utils.StringConstants;
import com.journeymate.Utils.Utility;
import com.journeymate.network.APIClient;
import com.journeymate.network.ApiInterface;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class JourneyActivity extends Activity {
    private String TAG = JourneyActivity.class.getSimpleName();
    private Context context;
    private Activity activity;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private MarshMallowPermission marshMallowPermission;
    private boolean isGyroAvailable = true;

    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    public void init(Context context, Activity activity,String email) {
        this.context = context;
        this.activity = activity;
        PreferencesManager.initialize(activity);
        marshMallowPermission = new MarshMallowPermission(activity);

        PreferencesManager.putString(StringConstants.KEY_USER_EMAIL,email);
        //start sensor service
        checkSensorHardware();
        checkLocationPermission();
    }



    private void initService() {
        LogUtils.printLog(TAG, " --- starting Service ---");
        LogUtils.printLog(TAG, "sdk version = " + Build.VERSION.SDK_INT);
        startAlarmManager();
        setActivityAlarm();

        Intent intent = new Intent(activity, SensorLocationService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("isGyroAvailable", isGyroAvailable);
        context.startService(intent);
    }

    private void startAlarmManager() {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long interval = 10 * 60 * 1000L;
        Intent alarmIntent = new Intent(activity, WatchLocationServiceReceiver.class);
        alarmIntent.setAction("com.journeyMate.Main");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, alarmIntent, 0);
        if (manager != null) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pendingIntent);
        }
    }

    private void setActivityAlarm() {
        LogUtils.printLog(TAG, "---Starting alarm service --- ");
        AlarmManager activityAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long activityInterval = 10 * 60 * 1000L;
        Intent actvityAlarmIntent = new Intent(activity, DetectedActivitiesIntentService.class);
        PendingIntent activityPendingIntent = PendingIntent.getService(activity, 109, actvityAlarmIntent, 0);
        if (activityAlarmManager != null) {
            activityAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), activityInterval, activityPendingIntent);
        }
    }

    private void checkSensorHardware() {
        SensorManager manager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if (manager != null) {
            HardwareChecker checker = new HardwareChecker(manager);
            if (!checker.IsGyroscopeAvailable()) {
                isGyroAvailable = false;
                Toast.makeText(getApplicationContext(), "Gyroscope is not available in device, data will not be updated for this device", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkLocationPermission() {
        if (marshMallowPermission.checkPermissionForLocation()) {
            mSettingsClient = LocationServices.getSettingsClient(context);
            initLocationRequest();
        } else {
            marshMallowPermission.requestPermissionForLocation();
        }
    }

    private void initLocationRequest() {
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationSettingPermission();
    }

    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }


    private void checkLocationSettingPermission() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            initService();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                                } catch (Exception sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
        initService();
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
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
                    initService();
                } else {
                    LogUtils.printLog(TAG, "Access location permission granted");
                    initLocationRequest();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }
}
