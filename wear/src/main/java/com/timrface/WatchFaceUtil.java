package com.timrface;

import android.content.Context;
import android.graphics.Color;

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
        }

        else if (key.contains("battery")) {
            boolean battery = key.contains("true");
            BATTERY_LEVEL = battery;
        }

        else if (key.equals("#FAFAFA") || key.equals("#424242") || key.equals("#000000")) {
            KEY_MAIN_COLOR = parseColor(key);
            if (key.equals("#FAFAFA")) {
                KEY_TEXT_COLOR = parseColor("#424242");
            } else {
                KEY_TEXT_COLOR = parseColor("#FAFAFA");
            }
        } else {
            KEY_BACKGROUND_COLOR = parseColor(key);
        }
    }
}