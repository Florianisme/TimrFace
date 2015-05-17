package com.timrface;

import android.content.Context;
import android.graphics.Color;

import helper.SharedPreferences;
import helper.TeleportService;

public final class WatchFaceUtil extends TeleportService {

    public static final int AMBIENT_BACKGROUND = parseColor("#000000");
    public static final int AMBIENT_TEXT = parseColor("#FFFFFF");

    public static int KEY_BACKGROUND_COLOR = parseColor("#FF9800");
    public static int KEY_MAIN_COLOR = parseColor("#FAFAFA");
    public static int KEY_TEXT_COLOR = parseColor("#424242");
    public static boolean SMOOTH_SECONDS = true;
    public static boolean BATTERY_LEVEL = true;

    public WatchFaceUtil() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private static int parseColor(String colorName) {
        return Color.parseColor(colorName);
    }

    public static void overwriteKeys(String key, Context context) {
        if (key.contains("seconds")) {
            boolean seconds = key.contains("true");
            SMOOTH_SECONDS = seconds;
            SharedPreferences.saveBoolean("smooth_seconds", seconds, context);
        }

        else if (key.contains("battery")) {
            boolean battery = key.contains("true");
            BATTERY_LEVEL = battery;
            SharedPreferences.saveBoolean("battery", battery, context);
        }

        else if (key.equals("#FAFAFA") || key.equals("#424242") || key.equals("#000000")) {
            KEY_MAIN_COLOR = parseColor(key);
            SharedPreferences.saveInteger("main_color", parseColor(key), context);

            if (key.equals("#FAFAFA")) {
                KEY_TEXT_COLOR = parseColor("#424242");
                SharedPreferences.saveInteger("text_color", parseColor("#424242"), context);

            } else {
                KEY_TEXT_COLOR = parseColor("#FAFAFA");
                SharedPreferences.saveInteger("text_color", parseColor("#FAFAFA"), context);

            }
        } else {
            KEY_BACKGROUND_COLOR = parseColor(key);
            SharedPreferences.saveInteger("background_color", parseColor(key), context);
        }
    }
}