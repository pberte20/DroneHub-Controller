package com.aau.herd.controller.Listeners;

import com.aau.herd.controller.Socket.DroneState;

public interface DroneStateListener {
    void onDroneStateChanged (DroneState droneState);
}
