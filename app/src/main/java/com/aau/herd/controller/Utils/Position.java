package com.aau.herd.controller.Utils;

public class Position {

    private double lat;
    private double lng;
    private double alt;
    private double speed;

    public Position(double lat, double lng, double alt){
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
    }

    public Position(double lat, double lng, double alt, double speed){
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
        this.speed = speed;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getAlt() {
        return alt;
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return "Position{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", alt=" + alt +
                ", speed=" + speed +
                '}';
    }
}
