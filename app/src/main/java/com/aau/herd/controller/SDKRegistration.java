package com.aau.herd.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.thirdparty.afinal.core.AsyncTask;

public class SDKRegistration {

    private static final String TAG = ConnectionActivity.class.getName();

    public static void start(final ConnectionActivity activity, final SDKRegistrationCallback callback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                activity.showToast("registering, pls wait...");
                DJISDKManager.getInstance().registerApp(activity.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                    @Override
                    public void onRegister(DJIError djiError) {
                        callback.onRegister(djiError);
                    }

                    @Override
                    public void onProductDisconnect() {
                        callback.onProductDisconnect();
                    }

                    @Override
                    public void onProductConnect(BaseProduct baseProduct) {
                        callback.onProductConnect(baseProduct);
                    }

                    @Override
                    public void onProductChanged(BaseProduct baseProduct) {}

                    @Override
                    public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent, BaseComponent newComponent) {
                        if (newComponent != null) {
                            newComponent.setComponentListener(new BaseComponent.ComponentListener() {
                                @Override
                                public void onConnectivityChange(boolean isConnected) {
                                    Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                    activity.notifyStatusChange();
                                }
                            });
                        }
                        Log.d(TAG,
                                String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                        componentKey, oldComponent, newComponent));
                    }

                    @Override
                    public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {}

                    @Override
                    public void onDatabaseDownloadProgress(long l, long l1) {}
                });
            }
        });
    }
}
