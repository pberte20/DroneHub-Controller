package com.aau.herd.controller;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.Toast;


public class UIUtils {

    public static void showToast(final Context context, final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
