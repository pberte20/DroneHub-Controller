package com.aau.herd.controller.VideoStreaming;

import static com.aau.herd.controller.ControllerApplication.getProductInstance;
import static io.socket.client.Socket.EVENT_DISCONNECT;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.aau.herd.controller.Socket.SocketConnection;

import org.json.JSONObject;
import org.webrtc.VideoCapturer;

import java.util.Hashtable;


/**
 * The DJIStreamer class will manage all ongoing P2P connections with client,
 * who desire video feed.
 */
public class DJIStreamer {
    private static final String TAG = "DJIStreamer";

    private String droneDisplayName = "";
    private final Context context;
    private final Hashtable<String, WebRTCClient> ongoingConnections = new Hashtable<>();

    public DJIStreamer(Context context){
        this.droneDisplayName = getProductInstance().getModel().getDisplayName();
        this.context = context;

        setupSocketEvent();
    }

    private WebRTCClient getClient(String socketID){
        return ongoingConnections.getOrDefault(socketID, null);
    }

    private void removeClient(String socketID){
        // TODO: Any other cleanup necessary?.. Let the client stop the VideoCapturer though.
        ongoingConnections.remove(socketID);
    }

    private WebRTCClient addNewClient(String socketID){
        VideoCapturer videoCapturer = new DJIVideoCapturer(droneDisplayName);
        WebRTCClient client = new WebRTCClient(socketID, context, videoCapturer, new WebRTCMediaOptions());
        client.setConnectionChangedListener(new WebRTCClient.PeerConnectionChangedListener() {
            @Override
            public void onDisconnected() {
                removeClient(client.peerSocketID);
                Log.d(TAG, "DJIStreamer has removed connection from table. Remaining active sessions: " + ongoingConnections.size());
            }
        });
        ongoingConnections.put(socketID, client);
        return client;
    }

    private void setupSocketEvent(){
        SocketConnection.getInstance().on("webrtc_msg", args -> {

            Handler mainHandler = new Handler(context.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    String peerSocketID = (String)args[0]; // The web-client sending a message
                    Log.d(TAG, "Received WebRTCMessage: " + peerSocketID);

                    WebRTCClient client = getClient(peerSocketID);

                    if (client == null){
                        // A new client wants to establish a P2P
                        client = addNewClient(peerSocketID);
                    }

                    // Then just pass the message to the client
                    JSONObject message = (JSONObject) args[1];
                    client.handleWebRTCMessage(message);
                }
            };
            mainHandler.post(myRunnable);
        }).on(EVENT_DISCONNECT, args -> {
            Log.d(TAG, "connectToSignallingServer: disconnect");
        });
    }
}
