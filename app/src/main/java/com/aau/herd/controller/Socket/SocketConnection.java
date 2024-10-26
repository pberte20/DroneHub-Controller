package com.aau.herd.controller.Socket;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import static io.socket.client.Socket.EVENT_CONNECT;

import com.aau.herd.controller.Utils.Constants;

public class SocketConnection {

    private static Socket socketConnection;

    private static boolean waypointMessageReceived = false;
    private static Object waypointMessage;

    private static boolean stopMessageReceived = false;
    private static Object stopMessage = false;

    public static void initSocket(String ip, String port) {
        try{
            String server = "http://" + ip + ":" + port;
            Log.d("TAG", "Instantiating socket using server: " + server);
            socketConnection = IO.socket(server);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void connect(){
        socketConnection.on(EVENT_CONNECT, args -> {
            System.out.println("Socket connected: " + socketConnection.connected());
        });
        socketConnection.connect();

        // Listen to the individual sockets
        SocketConnection.listen(Constants.DRONE_STOP);
        SocketConnection.listen(Constants.DRONE_WAYPOINT_MISSION);
    }

    public static void emit(String label, Object message){
        socketConnection.emit(label, message);
    }

    public static void listen(String event){

        socketConnection.on(event, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                switch (event) {
                    case Constants.DRONE_STOP:
                        stopMessageReceived = true;
                        stopMessage = args[0];
                        break;
                    case Constants.DRONE_WAYPOINT_MISSION:
                        waypointMessageReceived = true;
                        waypointMessage = args[0];
                        break;
                }
            }
        });
    }

    public static void sendDroneStateMessage(DroneState droneState){
        try {
            JSONObject droneStateJSON = SocketMessages.droneStateMessage(droneState);
            SocketConnection.emit(Constants.DRONE, droneStateJSON.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void sendEventMessage(Event event) {
        try {
            JSONObject eventJSON = SocketMessages.eventMessage(event);

            socketConnection.emit(Constants.EVENT, eventJSON.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static boolean isWaypointMessageReceived() {
        return waypointMessageReceived;
    }

    public static void setWaypointMessageReceived(boolean waypointMessageReceived) {
        SocketConnection.waypointMessageReceived = waypointMessageReceived;
    }

    public static boolean isStopMessageReceived() {
        return stopMessageReceived;
    }

    public static void setStopMessageReceived(boolean stopMessageReceived) {
        SocketConnection.stopMessageReceived = stopMessageReceived;
    }

    // Getters
    public static Object getWaypointMessage() {
        return waypointMessage;
    }
    public static Object getStopMessage() {
        return stopMessage;
    }

    public static Socket getInstance() {
        return socketConnection;
    }
}
