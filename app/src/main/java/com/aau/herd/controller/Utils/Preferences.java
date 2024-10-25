package com.aau.herd.controller.Utils;
import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String PREFS_NAME = "SettingsFile";

    public static void saveText(Context context, String name, String text) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(name, text);
        editor.apply();
    }

    public static String loadText(Context context, String name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(name, "");
    }
}