package com.aau.herd.controller;
import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String PREFS_NAME = "SettingsFile";
    private static final String KEY_SAVED_TEXT = "ipAddress";

    public static void saveText(Context context, String text) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SAVED_TEXT, text);
        editor.apply();
    }

    public static String loadText(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SAVED_TEXT, "");
    }
}