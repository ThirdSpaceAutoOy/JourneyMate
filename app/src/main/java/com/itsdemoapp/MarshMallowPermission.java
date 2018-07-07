package com.itsdemoapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.itsdemoapp.Utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class MarshMallowPermission extends Activity {
    private String TAG = MarshMallowPermission.class.getSimpleName();
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 2;
    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1;
    public static final int READ_CALENDER_PERMISSION_REQUEST_CODE = 7;
    public static final int READ_CALL_LOG_PERMISSION_REQUEST_CODE = 6;
    public static final int READ_CONTACT_PERMISSION_REQUEST_CODE = 5;
    public static final int READ_PHONE_STATE_PERMISSION_REQUEST_CODE = 4;
    public static final int READ_SMS_PERMISSION_REQUEST_CODE = 3;
    public static final int RECEIVE_SMS_PERMISSION_REQUEST_CODE = 8;
    public static final int WRITE_CALENDER_PERMISSION_REQUEST_CODE = 9;
    public static final int WRITE_CONTACT_PERMISSION_REQUEST_CODE = 10;
    public static final int GET_LOCATION_PERMISSION_REQUEST_CODE = 11;
    public static final int RECORD_AUDIO = 12;
    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    Activity activity;

    public MarshMallowPermission(Activity activity) {
        this.activity = activity;
    }



    public boolean checkMultiplePermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,permission))
                return false;
        }
        return true;
    }

    public void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.RECORD_AUDIO))
            permissionsNeeded.add("Read Phone State");
        if(!addPermission(permissionsList,Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Access location");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                String message = "You need to grant permission to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                    showMessageOKCancel(message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(activity,permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                    }
                });
                return;
            }
            ActivityCompat.requestPermissions(activity,permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity).setMessage(message).setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null).create().show();
    }



    public boolean checkPermissionForExternalStorage() {
        if (ContextCompat.checkSelfPermission(this.activity, "android.permission.READ_EXTERNAL_STORAGE") == 0) {
            return true;
        }
        return false;
    }

    public boolean checkPermissionForCamera() {
        if (ContextCompat.checkSelfPermission(this.activity, "android.permission.CAMERA") == 0) {
            return true;
        }
        return false;
    }

    public boolean checkPermissionForReadSMS() {
        if (ContextCompat.checkSelfPermission(this.activity, "android.permission.READ_SMS") == 0) {
            return true;
        }
        return false;
    }

    public boolean checkPermissionForReceiveSMS() {
        if (ContextCompat.checkSelfPermission(this.activity, "android.permission.RECEIVE_SMS") == 0) {
            return true;
        }
        return false;
    }

    public boolean checkPermissionForReadPhoneState() {
        if (ContextCompat.checkSelfPermission(this.activity, "android.permission.READ_PHONE_STATE") == 0) {
            return true;
        }
        return false;
    }

    public boolean checkPermissionForReadContact() {
        if (ContextCompat.checkSelfPermission(this.activity, "android.permission.READ_CONTACTS") == 0) {
            return true;
        }
        return false;
    }

    public boolean checkPermissionForReadPhoneLogs() {
        if (ContextCompat.checkSelfPermission(this.activity, "android.permission.READ_CALL_LOG") == 0) {
            return true;
        }
        return false;
    }

    public boolean checkPermissionForReadCalendar() {
        if (ContextCompat.checkSelfPermission(this.activity, "android.permission.READ_CALENDAR") == 0) {
            return true;
        }
        return false;
    }

    public boolean checkPermissionForWriteCalendar() {
        if (ContextCompat.checkSelfPermission(this.activity, "android.permission.WRITE_CALENDAR") == 0) {
            return true;
        }
        return false;
    }

    public boolean checkPermissionForWriteContact() {
        if (ContextCompat.checkSelfPermission(this.activity, "android.permission.WRITE_CONTACTS") == 0) {
            return true;
        }
        return false;
    }

    public boolean checkPermissionForLocation() {
        if (ContextCompat.checkSelfPermission(this.activity, Manifest.permission.ACCESS_FINE_LOCATION) == 0) {
            return true;
        }
        return false;
    }

    public boolean checkPermissionForRecordAudio() {
        if (ContextCompat.checkSelfPermission(this.activity, "android.permission.RECORD_AUDIO") == 0) {
            return true;
        }
        return false;
    }


    public void requestPermissionForExternalStorage() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.READ_EXTERNAL_STORAGE")) {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
        }
    }

    public void requestPermissionForCamera() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.CAMERA")) {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.CAMERA"}, 2);
        }
    }

    public void requestPermissionForReadSMS() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.READ_SMS")) {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.READ_SMS"}, 3);
        }
    }

    public void requestPermissionForReceiveSMS() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.RECEIVE_SMS")) {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.RECEIVE_SMS"}, 8);
        }
    }

    public void requestPermissionForReadPhoneState() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.READ_PHONE_STATE")) {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.READ_PHONE_STATE"}, 4);
        }
    }

    public void requestPermissionForReadContact() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.READ_CONTACTS")) {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.READ_CONTACTS"}, 5);
        }
    }

    public void requestPermissionForReadPhoneLogs() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.READ_CALL_LOG")) {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.READ_CALL_LOG"}, 6);
        }
    }

    public void requestPermissionForReadCalender() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.READ_CALENDAR")) {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.READ_CALENDAR"}, 7);
        }
    }

    public void requestPermissionForWriteCalender() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.WRITE_CALENDAR")) {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.WRITE_CALENDAR"}, 9);
        }
    }
    public void requestPermissionForWriteContact() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.WRITE_CONTACTS")) {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.WRITE_CONTACTS"}, 10);
        }
    }

    public void requestPermissionForLocation() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this.activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 11);
        }
    }

    public void requestPermissionForRecordAudio() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.RECORD_AUDIO")) {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.RECORD_AUDIO"}, 12);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "External Storage Permission  Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "External Storage Permission granted");
                    return;
                }
            case 2:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Camera Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "Camera Permission granted");
                    return;
                }
            case 3:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Read Sms Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "Read SmsPermission granted");
                    return;
                }
            case 4:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Phone State Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "Phone State Permission granted");
                    return;
                }
            case 5:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Read Contact Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "Read Contact Permission granted");
                    return;
                }
            case 6:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Read Phone Call logs Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "Read Phone Call logs Permission granted");
                    return;
                }
            case 7:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Read CalendarData logs Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "Read CalendarData Permission granted");
                    return;
                }
            case 8:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Receive  Sms Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "Receive SmsPermission granted");
                    return;
                }
            case 9:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Write to Calendar Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "Write to Calendar granted");
                    return;
                }
            case 10:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Write to Contacts Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "Write to Contacts granted");
                    return;
                }

            case 11:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Access location Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "Access location permission granted");
                    return;
                }

            case 12:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    LogUtils.printLog(TAG, "Record Audio Permission Denied");
                    return;
                } else {
                    LogUtils.printLog(TAG, "Record Audio Permission Granted");
                    return;
                }
            default:
                return;
        }
    }
}
