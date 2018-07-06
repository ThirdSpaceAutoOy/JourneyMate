package com.journeymate.SensorHelper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.journeymate.Utils.LogUtils;
import com.journeymate.Utils.PreferencesManager;
import com.journeymate.Utils.StringConstants;
import com.journeymate.Utils.Utility;
import com.journeymate.network.APIClient;
import com.journeymate.network.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Ankur Khandelwal on 09/04/18.
 */

public class LocationHelper {
    private String TAG = LocationHelper.class.getSimpleName();
    private Context context;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10*1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private Location previousLocation;
    private Boolean mRequestingLocationUpdates = false;
    private Boolean isFirstTime = false;
    private String city= "";
    private String weather = "";
    public LocationHelper(Context context){
        this.context = context;
    }

    public void init(){
        LogUtils.printLog(TAG,"--- init location helper --- ");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mSettingsClient = LocationServices.getSettingsClient(context);
        isFirstTime = true;
        initializePreviousLocation();
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        startLocationUpdates();
    }

    private void initializePreviousLocation() {
        previousLocation = new Location("");
        Location l = new Location("");
        l.setLatitude(0.0);
        l.setLongitude(0.0);
        previousLocation.set(l);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    public Location getLocation(){
        return mCurrentLocation;
    }

    public String getCity(){
        return city;
    }

    public String getWeather(){
        return weather;
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                if(mCurrentLocation!=null) {
                    if(isFirstTime){
//                        sendLocationToServer(mCurrentLocation);
                        isFirstTime = false;
                    }else {
                        LogUtils.printLog(TAG, "current location = " + mCurrentLocation);
                        checkDistance(previousLocation, mCurrentLocation);
                    }
                    city = getCity(mCurrentLocation);
                    if(PreferencesManager.getString(StringConstants.KEY_WEATHER,"").equalsIgnoreCase("")) {
                        weather = getWeather(mCurrentLocation);
                    }else{
                        long now = System.currentTimeMillis();
                        long lastWeatherUpdate = PreferencesManager.getLong("lastWeatherUpdate",0);
                        //if weather updated 12hrs ago, call again
                        if(Math.abs(now-lastWeatherUpdate) >= 1000 * 60 * 60 * 12){
                            weather = getWeather(mCurrentLocation);
                        }
                    }
                }
            }
        };
    }

    private void checkDistance(Location previousLocation, Location mCurrentLocation) {
        float distanceInMeters =  mCurrentLocation.distanceTo(previousLocation);
        if(distanceInMeters > 200*1000){
            //if distance is more than 200km , get weather of location.
            getWeather(mCurrentLocation);
        }
    }

    private void sendLocationToServer(Location mCurrentLocation) {
        String userName = PreferencesManager.getString(StringConstants.KEY_USER_EMAIL,"");
//        String deviceId = PreferencesManager.getString("deviceId","");
        ApiInterface apiClientInterface = APIClient.createService(ApiInterface.class);

        long timestamp = System.currentTimeMillis();
        HashMap<String,Object> locationHashMap = new HashMap<>();
        locationHashMap.put("username",userName);
//        locationHashMap.put("deviceId",deviceId);
        locationHashMap.put("lat",mCurrentLocation.getLatitude());
        locationHashMap.put("lng",mCurrentLocation.getLongitude());
        locationHashMap.put("alt",mCurrentLocation.getAltitude());
        locationHashMap.put("timeStamp",timestamp);

        LogUtils.printLog(TAG,"sending location to server");

        Call<ResponseBody> locationResponse = apiClientInterface.sendLocationData(Utility.getAuth(),locationHashMap);
        locationResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private String getCity(Location mCurrentLocation) {
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault()); //it is Geocoder
        String city = "";
        try {
            if(mCurrentLocation!=null) {
                List<Address> address = geoCoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                if (address.size() > 0) {
                    city = address.get(0).getLocality();
                    this.city = city;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return city;
    }

    private String getWeather(Location mCurrentLocation){
        if(mCurrentLocation!=null && weather.equalsIgnoreCase("")){
            ApiInterface apiClientInterface = APIClient.createService(ApiInterface.class);
            Call<ResponseBody> weatherResponse = apiClientInterface.
                    getWeatherForecast(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(), StringConstants.KEY_OPEWEATHER_API);

            weatherResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if(response.body()!=null ) {
                            JSONObject object = new JSONObject(response.body().string());
                            if (object.has("weather")) {
                                JSONArray weatherArray = object.getJSONArray("weather");
                                JSONObject weatherObject = weatherArray.getJSONObject(0);
                                weather = weatherObject.getString("main").toLowerCase();
                                LogUtils.printLog(TAG, "weather - " + weather);
                                PreferencesManager.putString(StringConstants.KEY_WEATHER, weather);
                                PreferencesManager.putLong("lastWeatherUpdate",System.currentTimeMillis());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
        return weather;
    }

    public void stopLocationUpdates() {
        LogUtils.printLog(TAG,"- -- stopLocationUpdate --- ");
        if (!mRequestingLocationUpdates) {
            LogUtils.printLog(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    public void startLocationUpdates() {
        LogUtils.printLog(TAG,"- -- startLocationUpdate --- ");
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
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
//                                    ResolvableApiException rae = (ResolvableApiException) e;
//                                    rae.startResolutionForResult(context, REQUEST_CHECK_SETTINGS);
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
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

}
