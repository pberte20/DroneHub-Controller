package com.aau.herd.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class ConnectionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();
    protected TextView connectionStatusText;
    protected TextView deviceText;
    protected EditText ipAddressEdit;
    protected Button openAppBtn;
    protected Button saveIpButton;
    private static BaseProduct mProduct;
    private Handler mHandler;
    private final AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionHandler.checkAndRequestPermissions(this);
        }

        setContentView(R.layout.activity_connection);

        mHandler = new Handler(Looper.getMainLooper());

        initUI();

        // Register the broadcast receiver for receiving the device connection's changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    private void initUI() {
        connectionStatusText = findViewById(R.id.connection_status_text);
        deviceText = findViewById(R.id.device_text);

        openAppBtn = findViewById(R.id.open_app_btn);
        openAppBtn.setOnClickListener(this);
        openAppBtn.setEnabled(false);

        saveIpButton = findViewById(R.id.save_ip_btn);
        saveIpButton.setOnClickListener(this);

        ipAddressEdit = findViewById(R.id.edit_ip_address);


        // Load the saved text from SharedPreferences
        String savedText = Preferences.loadText(this);
        ipAddressEdit.setText(savedText);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.open_app_btn) {
            Log.d("ButtonClick", "Open App button clicked");
        }
        else if (id == R.id.save_ip_btn) {
            saveText();
        } else {
            Log.d("ButtonClick", "Unknown button clicked");
        }
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
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
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void saveText() {
        String textToSave = ipAddressEdit.getText().toString();
        Preferences.saveText(this, textToSave);
        showToast("IP Address Saved");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHandler.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    void showToast(final String toastMsg) {
        UIUtils.showToast(getApplicationContext(), toastMsg);
    }

    void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            SDKRegistration.start(this, new SDKRegistrationCallback() {
                @Override
                public void onRegister(DJIError djiError) {
                    if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                        showToast("Register Success");
                        DJISDKManager.getInstance().startConnectionToProduct();
                    } else {
                        showToast("Register sdk fails, please check the bundle id and network connection!");
                    }
                    Log.v(TAG, djiError.getDescription());
                }

                @Override
                public void onProductDisconnect() {
                    showToast("Product Disconnected");
                    notifyStatusChange();
                }

                @Override
                public void onProductConnect(BaseProduct baseProduct) {
                    showToast("Product Connected");
                    notifyStatusChange();
                }
            });
        }
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();
        }
    };

    private void refreshSDKRelativeUI() {
        BaseProduct mProduct = getProductInstance();

        if (null != mProduct && mProduct.isConnected()) {
            Log.v(TAG, "refreshSDK: True");
            openAppBtn.setEnabled(true);

            String str = mProduct instanceof Aircraft ? "DJIAircraft" : "DJIHandHeld";
            connectionStatusText.setText("Status: " + str + " connected");

            if (null != mProduct.getModel()) {
                deviceText.setText("Device: " + mProduct.getModel().getDisplayName());
            } else {
                deviceText.setText("Device: N/A");
            }
        } else {
            Log.v(TAG, "refreshSDK: False");
            openAppBtn.setEnabled(false);

            deviceText.setText("Device: N/A");
            connectionStatusText.setText("Status: No Device Connected");
        }
    }

    public static synchronized BaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }
}
