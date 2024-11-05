package com.aau.herd.controller;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.TextureView.SurfaceTextureListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.aau.herd.controller.Listeners.BatteryStateListener;
import com.aau.herd.controller.Listeners.ColorStateListener;
import com.aau.herd.controller.Listeners.DroneStateListener;
import com.aau.herd.controller.Socket.DroneState;
import com.aau.herd.controller.VideoStreaming.DJIStreamer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;

public class MainActivity extends AppCompatActivity implements SurfaceTextureListener,OnClickListener, BatteryStateListener, ColorStateListener, DroneStateListener {

    private static final String TAG = MainActivity.class.getName();
    private DroneController droneController;
    private VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;
    private DJICodecManager mCodecManager = null;
    private TextureView mVideoSurface = null;
    private ImageButton mBtnLocate;
    private ImageButton mBtnEvent;
    private ImageButton mBtnVideo;
    private TextView mTextBattery;
    private TextView mTextAltitude;
    private DJIStreamer djiStreamer;

    private boolean lightsFlashing = false;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        droneController = new DroneController(this, id);
        droneController.setBatteryStateListener(this);
        droneController.setColorStateListenter(this);
        droneController.setDroneStateListener(this);

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };
    }

    protected void onProductChange() {
        initPreviewer();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        onProductChange();

        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        uninitPreviewer();
        super.onDestroy();
    }

    private void initUI() {

        // Init buttons
        mBtnLocate = findViewById(R.id.btn_locate); // Flashes the light on the drone, controller and interface
        mBtnLocate.setOnClickListener(this);

        mBtnEvent = findViewById(R.id.btn_event); // Sends an event message to the server
        mBtnEvent.setOnClickListener(this);

        mBtnVideo = findViewById(R.id.btn_video_cast); // Enables the video feed to the client
        mBtnVideo.setOnClickListener(this);

        // Init Telemetry Text
        mTextBattery = findViewById(R.id.text_battery_level);
        mTextAltitude = findViewById(R.id.text_altitude);

        // init mVideoSurface
        mVideoSurface = findViewById(R.id.video_previewer_surface);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }
    }

    private void initPreviewer() {

        BaseProduct product = ControllerApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = ControllerApplication.getCameraInstance();
        if (camera != null){
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onClick(View v) {
        // Event Button
        if (v.getId() == R.id.btn_event) {
            String content = "A drone has detected an object, please investigate the site.";
            String type = "Detected Object";
            droneController.sendDroneEventMessage(content, type);

            showToast("Event sent to server");
        }
        // Video Stream Button
        else if (v.getId() == R.id.btn_video_cast) {
            if (djiStreamer == null) {
                djiStreamer = new DJIStreamer(this);
                mBtnVideo.setColorFilter(Color.GREEN);

                showToast("Video Stream Enabled");
            } else {
                djiStreamer = null;
                mBtnVideo.setColorFilter(Color.WHITE);

                showToast("Video Stream Disabled");
            }
        }
        else if (v.getId() == R.id.btn_locate){

            if(lightsFlashing) {
                droneController.stopFlashingLights();
                lightsFlashing = false;
                mBtnLocate.setColorFilter(Color.WHITE);

                showToast("Locating stopped");
            }
            else {
                droneController.startFlashingLights();
                lightsFlashing = true;
                mBtnLocate.setColorFilter(Color.GREEN);

                showToast("Locating Drone");
            }
        }
    }

    public void onBatteryStateChanged(int percentage) {
        // Update the TextView with the new battery percentage
        String batteryText = percentage + "%";
        runOnUiThread(() -> mTextBattery.setText(batteryText));
    }

    public void onColorStateChanged(String color) {
        runOnUiThread(() -> changeStatusBarColor(color));
    }

    @Override
    public void onDroneStateChanged(DroneState droneState) {
        runOnUiThread(() -> {

            // Altitude
            String altitudeText = "H " + Math.round(droneState.getPos().getAlt()) + " m";
            mTextAltitude.setText(altitudeText);
        });
    }

    private void changeStatusBarColor(String color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color));
        }
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
