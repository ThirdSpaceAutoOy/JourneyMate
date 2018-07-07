package com.journeymate.SensorHelper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import com.journeymate.Utils.LogUtils;
import com.journeymate.Utils.PreferencesManager;
import com.journeymate.Utils.StringConstants;
import com.journeymate.Utils.Utility;
import com.journeymate.network.APIClient;
import com.journeymate.network.ApiInterface;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.SENSOR_SERVICE;

public class JourneySensorHelper implements SensorEventListener {
    private String TAG = JourneySensorHelper.class.getSimpleName();
    private SensorManager sensorManager = null;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor gravity;
    private Sensor ambientTemperatureSensor;
    private Sensor magneticField;
    private Sensor linearAccelerationSensor;
    // angular speeds from gyro
    private float[] gyro = new float[3];
    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[9];
    // orientation angles from gyro matrix
    private float[] gyroOrientation = new float[3];
    // magnetic field vector
    private float[] magnet = new float[3];
    // accelerometer vector
    private float[] accel = new float[3];
    //counter for accelerometer reading
    float[] mMagneticField = new float[3];
    float[] mGravity = new float[3];

    float mPitch = 0f, mRoll = 0f, mYaw = 0f;
    // for accelerometer
    float xAccelerometer = 0f;
    float yAccelerometer = 0f;
    float zAccelerometer = 0f;
    private Timer fuseTimer = new Timer();
    Context context;
    private boolean isGyroAvailable = true;
    private boolean isLinearAccelerationAvailable = true;
    private Location mLocation;
    private LocationHelper locationHelper;
    private float[] gravityRaw = new float[3];
    private float[] linearAcceleration = new float[3];
    private float ambientTemperature = 0;

    public JourneySensorHelper(Context context, Boolean isGyroAvailable) {
        this.context = context;
        this.isGyroAvailable = isGyroAvailable;
    }

    public void init() {
        LogUtils.printLog(TAG, "--- init journey sensor helper --- ");
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        checkHardwareSensor();
        setSensors();
        registerSensor();
        setSensorData();
        locationHelper = new LocationHelper(context);
        locationHelper.init();
    }

    private void setSensors() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (isGyroAvailable) {
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        ambientTemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    private void registerSensor() {
        if (sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            if (isGyroAvailable) {
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            }
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, ambientTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            if (isLinearAccelerationAvailable) {
                sensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    public void unregisterSensor() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this, accelerometer);
            if (isGyroAvailable) {
                sensorManager.unregisterListener(this, gyroscope);
            }
            sensorManager.unregisterListener(this, gravity);
            sensorManager.unregisterListener(this, magneticField);
            sensorManager.unregisterListener(this, ambientTemperatureSensor);
            if (isLinearAccelerationAvailable) {
                sensorManager.unregisterListener(this, linearAccelerationSensor);
            }
        }
    }

    public void checkHardwareSensor() {
        if (sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION).size() > 0) {
            isLinearAccelerationAvailable = true;
        }
    }

    private void setSensorData() {
        // computing sensor values
        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f;
        gyroMatrix[1] = 0.0f;
        gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f;
        gyroMatrix[4] = 1.0f;
        gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f;
        gyroMatrix[7] = 0.0f;
        gyroMatrix[8] = 1.0f;

//        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(), 2*1000, TIME_CONSTANT);
        // analysing behavior period : 5 sec after 5 second delay
        fuseTimer.scheduleAtFixedRate(new BehaviorAnalysis(), 5 * 1000, 5 * 1000);
    }

    private void stopDriverBehaviourTask() {
        LogUtils.printLog(TAG, " --- on Pause --- ");
        if (fuseTimer != null) {
            fuseTimer.cancel();
        }
        unregisterSensor();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values;
                System.arraycopy(event.values, 0, accel, 0, 3);
                xAccelerometer = accel[0];
                yAccelerometer = accel[1];
                zAccelerometer = accel[2];
                break;

            case Sensor.TYPE_GYROSCOPE:
                System.arraycopy(event.values, 0, gyro, 0, 3);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagneticField = event.values;
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;
            case Sensor.TYPE_GRAVITY:
                System.arraycopy(event.values, 0, gravityRaw, 0, 3);
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                ambientTemperature = event.values[0];
                break;

            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, linearAcceleration, 0, 3);
                break;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // analysis of user sensor and location, send at every 5 sec
    private class BehaviorAnalysis extends TimerTask {
        @Override
        public void run() {
            mLocation = locationHelper.getLocation();
            if (mLocation == null) {
                locationHelper.init();
                locationHelper.startLocationUpdates();
            } else {
                executeDriverTask();
            }
        }
    }

    private void executeDriverTask() {
        String userEmail = PreferencesManager.getString(StringConstants.KEY_USER_EMAIL, "");
        LogUtils.printLog(TAG, "Sending sensor data to server");

        final long currentTimestamp = System.currentTimeMillis();
        long lastUpdatedActivityTimeStamp = PreferencesManager.getLong("lastActivityUpdate", 0);
        long lastServiceUpdateTime = PreferencesManager.getLong("lastServiceUpdateTime", 0);
        LogUtils.printLog(TAG, "lastActivityTime =" + lastUpdatedActivityTimeStamp + " & current = " + currentTimestamp);
        LogUtils.printLog(TAG, "lastServiceTime =" + lastServiceUpdateTime + " & current = " + currentTimestamp);

        mLocation = locationHelper.getLocation();
        if (mLocation == null) {
            locationHelper.init();
        }

//        if (Math.abs(lastServiceUpdateTime - currentTimestamp) > 30 * 1000) {

        // if last updated activity time was 7 mins ago, change activity to still
        //user might be sleeping or phone is stationary...and stop service
        if (Math.abs(lastUpdatedActivityTimeStamp - currentTimestamp) > 1000 * 60 * 7) {
            LogUtils.printLog(TAG, "last timeStamp is more than 7 min");
            PreferencesManager.putString(StringConstants.KEY_CURRENT_ACTIVITY, "still");
            PreferencesManager.putInt(StringConstants.PREVIOUS_ACTIVITY_ID, 3);
            PreferencesManager.putLong("lastActivityUpdate", currentTimestamp);
        }

        String currentActivity = PreferencesManager.getString(StringConstants.KEY_CURRENT_ACTIVITY, "");
        String weather = PreferencesManager.getString(StringConstants.KEY_WEATHER, "");
        HashMap<String, Object> sensorHashMap = new HashMap<>();
        sensorHashMap.put("userEmail", userEmail);
        sensorHashMap.put("timeStamp", currentTimestamp);
        sensorHashMap.put("activity", currentActivity);
        sensorHashMap.put("weather_condition", weather);

        if (mLocation != null) {
            double lat = mLocation.getLatitude();
            double lng = mLocation.getLongitude();
            if (lat != 0.0 && lng != 0.0) {
                sensorHashMap.put("lat", lat);
                sensorHashMap.put("lng", lng);
                sensorHashMap.put("alt", mLocation.getAltitude());
                sensorHashMap.put("speed_of_vehicle", mLocation.getSpeed());
                sensorHashMap.put("city", locationHelper.getCity());


                ApiInterface apiClientInterface = APIClient.createService(ApiInterface.class);
                Call<ResponseBody> sensorResponse = apiClientInterface.sendSensorData(Utility.getAuth(), sensorHashMap);
                sensorResponse.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        LogUtils.printLog(TAG, "location-sensor data success!!!");
                        PreferencesManager.putLong("lastServiceUpdateTime", currentTimestamp);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        LogUtils.printLog(TAG, "location-sensor data failed!!!");
                        PreferencesManager.putLong("lastServiceUpdateTime", currentTimestamp);
                    }
                });
                sendRawSensorDataToServer(currentTimestamp);
            }
        } else {
            locationHelper.init();
        }
//        }
    }


    private void sendRawSensorDataToServer(long timestamp) {
        String userEmail = PreferencesManager.getString(StringConstants.KEY_USER_EMAIL, "");

        ApiInterface apiClientInterface = APIClient.createService(ApiInterface.class);

        HashMap<String, Object> sensorHashMap = new HashMap<>();
        sensorHashMap.put("userEmail", userEmail);
        //accelerometer data
        sensorHashMap.put("xAccelerometer", xAccelerometer);
        sensorHashMap.put("yAccelerometer", yAccelerometer);
        sensorHashMap.put("zAccelerometer", zAccelerometer);
        if (accelerometer != null) {
            sensorHashMap.put("maxRangeAccelerometer", accelerometer.getMaximumRange());
            sensorHashMap.put("resolutionAccelerometer", accelerometer.getResolution());
        }
        //rotation data
        sensorHashMap.put("azimuth", mYaw);
        sensorHashMap.put("pitch", mPitch);
        sensorHashMap.put("roll", mRoll);
        //gyro data
        sensorHashMap.put("xGyro", gyro[0]);
        sensorHashMap.put("yGyro", gyro[1]);
        sensorHashMap.put("zGyro", gyro[2]);
        if (gyroscope != null) {
            if (isGyroAvailable) {
                sensorHashMap.put("maxRangeGyroscope", gyroscope.getMaximumRange());
                sensorHashMap.put("resolutionGyroscope", gyroscope.getResolution());
            }
        }
        //magnetic data
        sensorHashMap.put("xMagneticField", magnet[0]);
        sensorHashMap.put("yMagneticField", magnet[1]);
        sensorHashMap.put("zMagneticField", magnet[2]);
        if (magneticField != null) {
            sensorHashMap.put("maxRangeMagneticField", magneticField.getMaximumRange());
            sensorHashMap.put("resolutionMagneticField", magneticField.getResolution());
        }
        //Gravity data
        sensorHashMap.put("xGravity", gravityRaw[0]);
        sensorHashMap.put("yGravity", gravityRaw[1]);
        sensorHashMap.put("zGravity", gravityRaw[2]);
        if (gravity != null) {
            sensorHashMap.put("maxRangeGravity", gravity.getMaximumRange());
            sensorHashMap.put("resolutionGravity", gravity.getResolution());
        }

        //linearAcc data
        sensorHashMap.put("xLinearAcceleration", linearAcceleration[0]);
        sensorHashMap.put("yLinearAcceleration", linearAcceleration[1]);
        sensorHashMap.put("zLinearAcceleration", linearAcceleration[2]);
        if (linearAccelerationSensor != null) {
            sensorHashMap.put("maxRangeLinearAcc", linearAccelerationSensor.getMaximumRange());
            sensorHashMap.put("resolutionLinearAcc", linearAccelerationSensor.getResolution());
        }

        //ambiant temperature
        sensorHashMap.put("ambiantTemp", ambientTemperature);
        if (ambientTemperatureSensor != null) {
            sensorHashMap.put("maxRangeAmbientTemp", ambientTemperatureSensor.getMaximumRange());
            sensorHashMap.put("resolutionAmbientTemp", ambientTemperatureSensor.getResolution());
        }

//        String timestamp = String.valueOf(System.currentTimeMillis());
        sensorHashMap.put("timeStamp", timestamp);

        LogUtils.printLog(TAG, "Sending Raw sensor data to server");
        Call<ResponseBody> sensorResponse = apiClientInterface.sendRawSensorData(Utility.getAuth(), sensorHashMap);
        sensorResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                LogUtils.printLog(TAG, "Raw sensor data success");
                PreferencesManager.putLong("lastServiceUpdateTime", System.currentTimeMillis());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                LogUtils.printLog(TAG, "Raw sensor data failed!!!");
                PreferencesManager.putLong("lastServiceUpdateTime", System.currentTimeMillis());
            }
        });
    }
}
