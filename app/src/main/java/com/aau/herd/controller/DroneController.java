package com.aau.herd.controller;

import static com.aau.herd.controller.ControllerApplication.getProductInstance;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aau.herd.controller.Listeners.ColorStateListener;
import com.aau.herd.controller.Listeners.DroneStateListener;
import com.aau.herd.controller.Socket.DroneState;
import com.aau.herd.controller.Socket.Event;
import com.aau.herd.controller.Socket.SocketConnection;
import com.aau.herd.controller.Listeners.BatteryStateListener;
import com.aau.herd.controller.Utils.Constants;
import com.aau.herd.controller.Utils.JSONConverter;
import com.aau.herd.controller.Utils.Position;
import com.aau.herd.controller.Utils.Trigonometry;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Queue;

import dji.common.Stick;
import dji.common.error.DJIError;
import dji.common.flightcontroller.LEDsSettings;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.remotecontroller.HardwareState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.remotecontroller.RemoteController;

public class DroneController extends AppCompatActivity {
    private BatteryStateListener batteryStateListener;
    private ColorStateListener colorStateListener;
    private DroneStateListener droneStateListener;
    private Context context;
    private Handler handler = new Handler();
    // DJI Variables
    private FlightController flightController;
    private FlightControlData flightControlData;
    private Battery battery;
    private RemoteController remoteController;
    private Stick leftStick, rightStick;
    private HardwareState.Button goHomeButton;

    // Drone Variables
    private Position goToTarget;
    private Queue<Position> waypointTargets;
    private boolean lightsOn = false;

    private DroneState droneState;

    // Mission Variables
    private static final double ALLOWED_ALTITUDE_OFFSET = 0.5; // Meters
    private static final double ALLOWED_YAW_OFFSET = 5; // Degrees

    public DroneController(Context context, String id) {

        this.context = context;
        this.droneState = new DroneState(id);

        initBattery();
        initModel();
        initFlightController();
        initRemoteController();
        initVirtualStick();
    }
    private void initBattery() {
        BaseProduct product = getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                battery = product.getBattery();
            }
        }
        getBattery();
    }

    private void initModel() {
        BaseProduct product = getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                String model = product.getModel().name();
                droneState.setModel(model);

                product.getName(new CommonCallbacks.CompletionCallbackWith<String>() {
                        @Override
                        public void onSuccess(String name) {
                            droneState.setName(name);
                        }

                        @Override
                        public void onFailure(DJIError djiError) {
                            showToast(djiError.getDescription());
                        }
                    }
                );
            }
        }
    }

    private void initFlightController() {
        BaseProduct product = getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                flightController = ((Aircraft) product).getFlightController();
            }
        }
        getDroneStatus();
    }
    private void initRemoteController(){
        BaseProduct product = getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                remoteController = ((Aircraft) product).getRemoteController();
            }
        }

        if(remoteController != null){
            remoteController.setHardwareStateCallback(hardwareState -> {
                leftStick = hardwareState.getLeftStick();
                rightStick = hardwareState.getRightStick();
                goHomeButton = hardwareState.getGoHomeButton();
            });
        }
    }
    private void initVirtualStick(){
        if(flightController != null){
            flightController.setVirtualStickModeEnabled(true, completion -> {

                flightController.setVerticalControlMode(VerticalControlMode.POSITION);
                flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
                flightController.setYawControlMode(YawControlMode.ANGLE);
                flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

                resetFlightControlData();
            });
        }
    }
    private void resetFlightControlData(){
        flightControlData = new FlightControlData(0,0,0,0);
        flightControlData.setYaw((float) droneState.getYaw());
        flightControlData.setVerticalThrottle((float) droneState.getPos().getAlt());
    }
    public void getBattery(){
        if (battery != null) {
            battery.setStateCallback(batteryState -> {
                int currPercentage = batteryState.getChargeRemainingInPercent();
                droneState.setBattery(currPercentage);
                if (batteryStateListener != null) {
                    batteryStateListener.onBatteryStateChanged(currPercentage);
                }
            });
        }
    }
    private void getDroneStatus(){
        if (flightController != null) {
            flightController.setStateCallback(currentState -> {
                double lat = currentState.getAircraftLocation().getLatitude();
                double lng = currentState.getAircraftLocation().getLongitude();
                double alt = currentState.getAircraftLocation().getAltitude();

                Position currPos = new Position(lat, lng, alt);
                droneState.setPos(currPos);
                droneState.setYaw(currentState.getAttitude().yaw);
                droneState.setFlightMode(currentState.getFlightModeString());
                droneState.setReturning(currentState.isGoingHome());

                if(droneStateListener != null) {
                    droneStateListener.onDroneStateChanged(droneState);
                }

                updateDroneState(); //Continuously send the drone state to the server
            });
        }
    }

    private void updateDroneState() {
        // Waypoint mission
        if(SocketConnection.isWaypointMessageReceived() || droneState.isOnWaypointMission()){
            performWaypointMission();
            SocketConnection.setWaypointMessageReceived(false);
        }

        // Check if a stop message has been received
        if(SocketConnection.isStopMessageReceived()){
            performStopMission();
        }

        // Server has send a designated drone color
        if(SocketConnection.isColorMessageReceived()){
            try{
                String jsonString = SocketConnection.getColorMessage().toString();
                JSONObject jsonObject = new JSONObject(jsonString);

                SocketConnection.setColorMessageReceived(false);
                String color = jsonObject.getString("droneColor");

                if(colorStateListener != null) {
                    colorStateListener.onColorStateChanged(color);
                }

            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        // Get the state of the physical device buttons
        getStickState();

        SocketConnection.sendDroneStateMessage(droneState);
    }

    public void sendDroneEventMessage(String content, String type) {

        Event event = new Event(droneState, content, type);
        SocketConnection.sendEventMessage(event);

        showToast("Event sent to server");
    }

    private void performStopMission(){
        boolean stop = JSONConverter.convertStopMessage(droneState);
        if(stop){
            if(droneState.isOnWaypointMission()) stopWaypointMission();
        }
        SocketConnection.setStopMessageReceived(false);
    }
    private void performWaypointMission(){
        if(flightController.isVirtualStickControlModeAvailable()){
            if(droneState.isReturning()) stopRTHMission();
            startWaypointMission();
        } else {
            initVirtualStick();
            performWaypointMission();
        }
    }

    /** START COMMANDS */
    private void startWaypointMission(){
        // Init waypoint mission
        if(SocketConnection.isWaypointMessageReceived())
            waypointTargets = JSONConverter.convertWaypointMessage(droneState);

        SocketConnection.setWaypointMessageReceived(false);

        // Start mission
        if(waypointTargets != null && waypointTargets.size() > 0){
            goToTarget = waypointTargets.peek();
            configureMission();

            // Send virtual stick commands
            flightController.sendVirtualStickFlightControlData(flightControlData, completion -> {
                if(!droneState.isOnWaypointMission()) {

                    droneState.setOnWaypointMission(true);

                    showToast("Waypoint mission started");
                }
            });

            double dist = Trigonometry.calcDistanceBtwPoints(droneState.getPos(), goToTarget);
            if(dist < 2){
                waypointTargets.remove();
                // If the last point has been reached, stop the waypoint mission
                if(waypointTargets.isEmpty()){
                    stopWaypointMission();

                    showToast("Waypoint mission completed.");
                }
            }
        }
    }

    /** STOP COMMANDS*/
    private void stopRTHMission(){
        if(flightController != null){
            flightController.cancelGoHome(djiError -> {
                if(djiError == null){
                    showToast("RTH command cancelled");
                } else {
                    showToast("Could not stop the RTH Command. Please take manual control.");
                    SocketConnection.emit(Constants.ERROR, djiError.getDescription());
                }
            });
        }
    }
    private void stopWaypointMission(){
        flightController.setVirtualStickModeEnabled(false, completion -> {
            resetFlightControlData();
            goToTarget = null;
            waypointTargets = null;
            droneState.setOnWaypointMission(false);

            showToast("Waypoint mission stopped.");
        });
    }

    // Set the flight control data to the proper data
    private void configureMission(){
        double angle = Trigonometry.calcAngleBtwPoints(droneState.getPos(), goToTarget);
        double dist = Trigonometry.calcDistanceBtwPoints(droneState.getPos(), goToTarget);
        double dAlt = Math.abs(droneState.getPos().getAlt() - goToTarget.getAlt());
        double dYaw = Math.abs(droneState.getYaw() - angle);

        flightControlData.setYaw((float) angle);
        flightControlData.setVerticalThrottle((float) goToTarget.getAlt());

        // If the drone is at the right altitude and correct yaw, start flying
        if(dAlt <= ALLOWED_ALTITUDE_OFFSET && dYaw <= ALLOWED_YAW_OFFSET){
            // If the drone is close to the target, decrease speed
            // TODO: find a better equation to calculate overshoot and when to decrease speed
            if(dist < goToTarget.getSpeed()) flightControlData.setRoll((float) goToTarget.getSpeed());
            else flightControlData.setRoll((float) (goToTarget.getSpeed() * 0.5));
        } else {
            flightControlData.setRoll(0);
        }
    }
    private void getStickState(){
        if(remoteController != null && leftStick != null && rightStick != null && goHomeButton != null){
            int lHoz = Math.abs(leftStick.getHorizontalPosition());
            int lVer = Math.abs(leftStick.getVerticalPosition());
            int rHoz = Math.abs(rightStick.getHorizontalPosition());
            int rVer = Math.abs(rightStick.getVerticalPosition());

            int maxStickVal = 660;

            // If a go-to mission is on going, the user should say whether they want to cancel it or not.
            if(lHoz == maxStickVal || lVer == maxStickVal ||rHoz == maxStickVal || rVer == maxStickVal
                    || goHomeButton.isClicked()){

                if(droneState.isOnWaypointMission()) stopWaypointMission();
            }
        }
    }

    private Runnable flashLightsRunnable = new Runnable() {
        @Override
        public void run() {
            if(flightController != null){
                lightsOn = !lightsOn;

                LEDsSettings.Builder builder = new LEDsSettings.Builder();
                builder.statusIndicatorOn(lightsOn);

                LEDsSettings settings = builder.build();

                flightController.setLEDsEnabledSettings(settings, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            showToast(djiError.getDescription());
                        }
                    }
                });
            }
            // Schedule next toggle
            handler.postDelayed(flashLightsRunnable, 500);
        }
    };

    public void startFlashingLights() {
        handler.post(flashLightsRunnable);
    }

    public void stopFlashingLights() {
        handler.removeCallbacks(flashLightsRunnable);
        // Ensure lights are turned off
        if (flightController != null) {

            LEDsSettings.Builder builder = new LEDsSettings.Builder();
            builder.statusIndicatorOn(false);

            LEDsSettings settings = builder.build();

            flightController.setLEDsEnabledSettings(settings, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        showToast(djiError.getDescription());
                    }
                }
            });
        }
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setBatteryStateListener(BatteryStateListener listener) {
        this.batteryStateListener = listener;
    }

    public void setColorStateListenter(ColorStateListener listener) {
        this.colorStateListener = listener;
    }

    public void setDroneStateListener(DroneStateListener listener) {
        this.droneStateListener = listener;
    }
}
