/*
 *  Copyright 2016 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.aau.herd.controller.VideoStreaming;

import static com.aau.herd.controller.ControllerApplication.getCameraInstance;

import org.webrtc.CapturerObserver;
import org.webrtc.NV12Buffer;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import dji.common.camera.ResolutionAndFrameRate;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;

public class DJIVideoCapturer implements VideoCapturer {
    private final static String TAG = "DJIStreamer";
    private static DJICodecManager codecManager;
    private static final ArrayList<CapturerObserver> observers = new ArrayList<CapturerObserver>();

    private final String droneDisplayName;
    private Context context;
    private CapturerObserver capturerObserver;
    private int height, width, framerate; // TODO: Figure out how to set the resolution - try the dji.Camera class

    public DJIVideoCapturer(String droneDisplayName){
        this.droneDisplayName = droneDisplayName;
    }

    // TODO: Move this logic into separate dedicated class and init it once from the constructor of DJIStreamer
    private void setupVideoListener(){
        if(codecManager != null)
            return;
        // Pass SurfaceTexture as null to force the Yuv callback - width and height for the surface does not matter
        codecManager = new DJICodecManager(context, (SurfaceTexture)null, 0, 0);
        codecManager.enabledYuvData(true);
        codecManager.setYuvDataCallback(new DJICodecManager.YuvDataCallback() {
            @Override
            public void onYuvDataReceived(MediaFormat mediaFormat, ByteBuffer videoBuffer, int dataSize, int width, int height) {
                if (videoBuffer != null){
                    try{
                        // We need to check which color format they are using by doing a lookup in our MediaFormat, otherwise we get green artifacts
                        // This can change with Android/device versions. The format might actually change, seemingly at random, according to community reports...
                        // Other possible buffers we might have to use: I420Buffer
                        //Log.d(TAG, "Received video! (" + width + "x" + height + ")");
                        int colorFormat = mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
                        long timestampNS = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
                        NV12Buffer buffer = new NV12Buffer(width,
                                                            height,
                                                            mediaFormat.getInteger(MediaFormat.KEY_STRIDE),
                                                            mediaFormat.getInteger(MediaFormat.KEY_SLICE_HEIGHT),
                                                            videoBuffer,
                                                            null);
                        VideoFrame videoFrame = new VideoFrame(buffer, 0, timestampNS);
                        // Feed the video frame to everyone
                        for (CapturerObserver obs : observers) {
                            obs.onFrameCaptured(videoFrame);
                        }
                        videoFrame.release();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        switch (this.droneDisplayName){
            // The Air 2S relies on the VideoDataListener to obtain the video feed
            case "DJI Air 2S":
                // The onReceive callback provides us the raw H264 (at least according to official documentation). To decode it we send it to our DJICodecManager
                // H264 or H265 encoding is done to compress and save bandwidth. (4K video might force a switch to H265 on DJI drones)
                VideoFeeder.VideoDataListener videoDataListener = new VideoFeeder.VideoDataListener() {
                    @Override
                    public void onReceive(byte[] bytes, int dataSize) {
                        // Pass the encoded data along to obtain the YUV-color data
                        //Log.d(TAG, "Video data received: " + dataSize);
                        codecManager.sendDataToDecoder(bytes, dataSize);
                    }
                };
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
                break;
        }
    }

    private void setCameraResolution(){
        Camera cam = getCameraInstance();
        if(cam != null){
            // Lets first figure out which resolutions and FPS combinations are available to this drone:
            ResolutionAndFrameRate[] availableResolutions = cam.getCapabilities().videoResolutionAndFrameRateRange();
            for (ResolutionAndFrameRate res : availableResolutions){
                Log.d(TAG, "Possible resolution: " + res.toString());
            }

            // Before setting the resolution we must change the mode to VIDEO
            cam.setMode(SettingsDefinitions.CameraMode.RECORD_VIDEO, null);

            cam.setVideoResolutionAndFrameRate(
                    new ResolutionAndFrameRate(
                            SettingsDefinitions.VideoResolution.RESOLUTION_1920x1080,
                            SettingsDefinitions.VideoFrameRate.FRAME_RATE_50_FPS
                    ),
                    new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            cam.getVideoResolutionAndFrameRate(new CommonCallbacks.CompletionCallbackWith<ResolutionAndFrameRate>() {
                                @Override
                                public void onSuccess(ResolutionAndFrameRate resolutionAndFrameRate) {
                                    //Log.d(TAG, "Resolution: " + resolutionAndFrameRate.toString());
                                }

                                @Override
                                public void onFailure(DJIError djiError) {

                                }
                            });
                        }
                    }
            );
        }
    }

    @Override
    public void initialize(SurfaceTextureHelper surfaceTextureHelper, Context applicationContext,
                           CapturerObserver capturerObserver) {
        this.context = applicationContext;
        this.capturerObserver = capturerObserver;

        observers.add(capturerObserver);
    }

    @Override
    public void startCapture(int width, int height, int framerate) {
        // TODO: We will need a HashMap to map the integer values to the DJI enums used for setting resolution
        this.height = height;
        this.width = width;
        this.framerate = framerate;

        //setCameraResolution();
        // Hook onto the DJI onYuvDataReceived event
        setupVideoListener();
    }

    @Override
    public void stopCapture() throws InterruptedException {
    }

    @Override
    public void changeCaptureFormat(int width, int height, int framerate) {
        // Empty on purpose
    }

    @Override
    public void dispose() {
        // Stop receiving frames on the callback from the decoder
        if (observers.contains(capturerObserver))
            observers.remove(capturerObserver);
    }

    @Override
    public boolean isScreencast() {
        return false;
    }
}