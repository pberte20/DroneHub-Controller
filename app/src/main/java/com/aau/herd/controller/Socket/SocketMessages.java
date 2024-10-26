package com.aau.herd.controller.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketMessages {
    public static JSONObject eventMessage(Event event) throws JSONException {
        JSONObject posJSON = new JSONObject();
        posJSON.put("lat", event.getDroneState().getPos().getLat());
        posJSON.put("lng", event.getDroneState().getPos().getLng());

        JSONObject eventJSON = new JSONObject();

        eventJSON.put("droneID", event.getDroneState().getId());
        eventJSON.put("position", posJSON);
        eventJSON.put("content", event.getContent());
        eventJSON.put("type", event.getType());

        return eventJSON;
    }

    public static JSONObject droneStateMessage(DroneState droneState) throws JSONException {
        // Create JSON coordinates
        JSONObject dronePosJSON = new JSONObject();
        dronePosJSON.put("lat", droneState.getPos().getLat());
        dronePosJSON.put("lng", droneState.getPos().getLng());

        // Create JSON object
        JSONObject droneInfo = new JSONObject();

        droneInfo.put("id", droneState.getId());
        droneInfo.put("name", droneState.getName());
        droneInfo.put("version", droneState.getModel());
        droneInfo.put("battery", droneState.getBattery());
        droneInfo.put("position", dronePosJSON);
        droneInfo.put("altitude", droneState.getPos().getAlt());
        droneInfo.put("yaw", droneState.getYaw());

        return droneInfo;
    }
}
