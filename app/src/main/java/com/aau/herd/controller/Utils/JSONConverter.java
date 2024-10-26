package com.aau.herd.controller.Utils;

import com.aau.herd.controller.Socket.DroneState;
import com.aau.herd.controller.Socket.SocketConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Queue;

public class JSONConverter {

    public static boolean convertStopMessage(DroneState drone) {
        boolean stop = false;

        try{
            String jsonString = SocketConnection.getStopMessage().toString();
            JSONObject jsonObject = new JSONObject(jsonString);

            if(jsonObject.getString("targetDroneId").equals(drone.getId())){
                stop = true;
            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return stop;
    }

    public static Queue<Position> convertWaypointMessage(DroneState drone){
        try{
            String jsonString = SocketConnection.getWaypointMessage().toString();
            JSONObject jsonObject = new JSONObject(jsonString);

            if(jsonObject.getString("targetDroneId").equals(drone.getId())){
                JSONArray jsonArray = jsonObject.getJSONArray("coordinates");
                double alt = (float) jsonObject.getDouble("altitude");
                double speed = (float) jsonObject.getDouble("speed");

                Queue<Position> positions = new LinkedList<>();

                for (int i = 0; i < jsonArray.length(); i++){
                    JSONObject pos = jsonArray.getJSONObject(i);

                    double lat = pos.getDouble("lat");
                    double lng = pos.getDouble("lng");

                    positions.add(new Position(lat, lng, alt, speed));
                }

                return positions;
            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return null;
    }
}
