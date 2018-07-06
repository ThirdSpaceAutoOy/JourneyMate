package com.journeymate.Utils;


import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.journeymate.Interface.SensorChecker;


public class HardwareChecker implements SensorChecker {

    boolean gyroscopeIsAvailable = false;
    public HardwareChecker(SensorManager sensorManager) {
        if(sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE).size() > 0) {
            gyroscopeIsAvailable = true;
        }
    }

    @Override
    public boolean IsGyroscopeAvailable() {
        return gyroscopeIsAvailable;
    }


}
