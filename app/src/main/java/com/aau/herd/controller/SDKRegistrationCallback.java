package com.aau.herd.controller;

import dji.common.error.DJIError;
import dji.sdk.base.BaseProduct;

public interface SDKRegistrationCallback {
    void onRegister(DJIError djiError);
    void onProductDisconnect();
    void onProductConnect(BaseProduct baseProduct);
}

