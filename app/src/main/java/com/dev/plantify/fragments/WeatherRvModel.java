// WeatherRvModel.java

package com.dev.plantify.fragments;

public class WeatherRvModel {
    private String time;
    private String temperature;
    private String icon;

    private String chanceRain;

    public WeatherRvModel(String time, String temperature, String icon, String chance_of_rain) {
        this.time = time;
        this.temperature = temperature+" °C";
        this.icon = icon;
        this.chanceRain = chanceRain;
    }

    public String getTime() {
        return time;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getIcon() {
        return icon;
    }

    public String getChanceRain() {
        return chanceRain;
    }
}
