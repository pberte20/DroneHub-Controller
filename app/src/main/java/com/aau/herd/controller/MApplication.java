package com.aau.herd.controller;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MApplication extends Application {

    private ControllerApplication controllerApplication;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if (controllerApplication == null) {
            controllerApplication = new ControllerApplication();
            controllerApplication.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        controllerApplication.onCreate();
    }
}
