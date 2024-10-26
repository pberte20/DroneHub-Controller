package com.aau.herd.controller;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.aau.herd.controller.VideoStreaming.DJIStreamer;

import dji.common.product.Model;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;

public class MainActivity extends AppCompatActivity implements SurfaceTextureListener,OnClickListener{

    private static final String TAG = MainActivity.class.getName();
    private DroneController droneController;
    protected VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;
    protected DJICodecManager mCodecManager = null;
    protected TextureView mVideoSurface = null;
    protected ImageButton mBtnEvent;
    protected ImageButton mBtnVideo;
    private DJIStreamer djiStreamer;
    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        droneController = new DroneController(this, id);

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
        mBtnEvent = findViewById(R.id.btn_event);
        mBtnEvent.setOnClickListener(this);

        mBtnVideo = findViewById(R.id.btn_video_cast);
        mBtnVideo.setOnClickListener(this);

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
        if(v.getId() == R.id.btn_event) {
            String content = "A drone has detected an object, please investigate the site.";
            String type = "Detected Object";
            droneController.sendDroneEventMessage(content, type);
        }
        // Video Stream Button
        else if (v.getId() == R.id.btn_video_cast) {
            if(djiStreamer == null) {
                djiStreamer = new DJIStreamer(this);
                mBtnVideo.setColorFilter(Color.GREEN);

                showToast("Video Stream Enabled");
            }
            else {
                djiStreamer = null;
                mBtnVideo.setColorFilter(Color.WHITE);

                showToast("Video Stream Disabled");
            }
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
