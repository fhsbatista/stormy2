package com.devandroid.fbatista.stormy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CurrentWeather {

    private String locationLabel;
    private String icon;
    private double temperature;
    private Long time;
    private double humidity;
    private double precipation;
    private String summary;
    private String timezone;
    private String formattedTime;

    public CurrentWeather() {

    }

    public CurrentWeather(String locationLabel, String icon,
                          double temperature, double humidity, double precipation, String summary, String timezone, Long time) {
        this.locationLabel = locationLabel;
        this.icon = icon;
        this.temperature = temperature;
        this.humidity = humidity;
        this.precipation = precipation;
        this.summary = summary;
        this.timezone = timezone;
        this.time = time;
    }

    public int getIconId(){

        //clear-day, clear-night, rain, snow, sleet, wind, fog, cloudy, partly-cloudy-day, or partly-cloudy-night
        int iconId = 0;

        switch (icon){

            case "clear-night":
                iconId = R.drawable.clear_night;
                break;

            case "clear-day":
                iconId = R.drawable.clear_day;
                break;

            case "rain":
                iconId = R.drawable.rain;
                break;
            case "snow":
                iconId = R.drawable.snow;
                break;
            case "sleet":
                iconId = R.drawable.sleet;
                break;
            case "wind":
                iconId = R.drawable.wind;
                break;
            case "fog":
                iconId = R.drawable.fog;
                break;
            case "cloudy":
                iconId = R.drawable.cloudy;
                break;
            case "partly-cloudy-day":
                iconId = R.drawable.partly_cloudy;
                break;
            case "partly-cloudy-night":
                iconId = R.drawable.cloudy_night;
                break;
        }


        return iconId;


    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLocationLabel() {
        return locationLabel;
    }

    public void setLocationLabel(String locationLabel) {
        this.locationLabel = locationLabel;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getPrecipation() {
        return precipation;
    }

    public void setPrecipation(double precipation) {
        this.precipation = precipation;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFormattedTime() {

        SimpleDateFormat formater = new SimpleDateFormat("hh:mm a");
        formater.setTimeZone(TimeZone.getTimeZone(timezone));
        Date date = new Date(time * 1000);


        return formater.format(date);
    }
}
