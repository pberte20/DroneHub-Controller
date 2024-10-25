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

    public static JSONObject droneStateMessage(DroneState drone) throws JSONException {
        // Create JSON coordinates
        JSONObject dronePosJSON = new JSONObject();
        dronePosJSON.put("lat", drone.getPos().getLat());
        dronePosJSON.put("lng", drone.getPos().getLng());

        // Create JSON object
        JSONObject droneInfo = new JSONObject();

        droneInfo.put("id", drone.getId());
        droneInfo.put("name", drone.getName());
        droneInfo.put("version", drone.getModel());
        droneInfo.put("battery", drone.getBattery());
        droneInfo.put("position", dronePosJSON);
        droneInfo.put("altitude", drone.getPos().getAlt());
        droneInfo.put("yaw", drone.getYaw());

        return droneInfo;
    }
}
