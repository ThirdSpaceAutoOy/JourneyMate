package com.journeymate.Utils;

/**
 * @author Ankur Khandelwal on 02/04/18.
 */

public class Constants {
    public static final String BASE_URL =  "http://carpassport.eu-central-1.elasticbeanstalk.com";
    public static final String LOGIN = BASE_URL + "/auth/token/";
    public static final String REGISTER = BASE_URL + "/account/signup/";
    public static final String CALENDARDATA = BASE_URL +"/calendarData/";
    public static final String LOCATIONDATA = BASE_URL +"/car/location-data/";
    public static final String SENSORDATARAW = BASE_URL +"/car/sensor-data/";
    public static final String SENSORDATA = BASE_URL + "/car/smartphone-data/";
    public static final String AUDIO_URL = BASE_URL +"/process_audio/";
    public static final String SEND_FCM_TOKEN =  BASE_URL+ "/notification/register/";
    public static final String GET_WEATHER_FORECAST = "http://api.openweathermap.org/data/2.5/weather/";


    public static final String VW_BASE_URL =  "http://vwbusseva.eu-central-1.elasticbeanstalk.com";
    public static final String BUS_STATUS =  VW_BASE_URL + "/transit/bus-status";


}
