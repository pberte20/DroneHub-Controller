package com.aau.herd.controller.Socket;

public class Event {

    private DroneState droneState;
    private String Content;
    private String type;

    public Event(DroneState droneState, String content, String type) {
        this.droneState = droneState;
        Content = content;
        this.type = type;

    }
    public DroneState getDroneState() {
        return droneState;
    }

    public String getContent() {
        return Content;
    }

    public String getType() {
        return type;
    }
}
