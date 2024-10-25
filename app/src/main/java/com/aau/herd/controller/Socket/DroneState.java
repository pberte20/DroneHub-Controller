package com.aau.herd.controller.Socket;

import com.aau.herd.controller.Utils.Position;

public class DroneState {

    private String id;
    private String name;
    private String model;
    private String flightMode;
    private Position pos;
    private double yaw;
    private int battery;
    private boolean isReturning;
    private boolean isOnMission;
    private boolean isOnWaypointMission;

    public DroneState(String id) {
        this.id = id;
        this.name = "Search drone";
        this.model = "Unknown";
        this.flightMode = "Undefined";
        this.pos = new Position(0,0,0);
        this.yaw = 0;
        this.battery = 0;
        this.isOnMission = false;
        this.isReturning = false;
        this.isOnWaypointMission = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFlightMode() {
        return flightMode;
    }

    public void setFlightMode(String flightMode) {
        this.flightMode = flightMode;
    }

    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public boolean isReturning() {
        return isReturning;
    }

    public void setReturning(boolean returning) {
        isReturning = returning;
    }

    public boolean isOnMission() {
        return isOnMission;
    }

    public void setOnMission(boolean onMission) {
        isOnMission = onMission;
    }

    public boolean isOnWaypointMission() {
        return isOnWaypointMission;
    }

    public void setOnWaypointMission(boolean onWaypointMission) {
        isOnWaypointMission = onWaypointMission;
    }
}
