package com.timrface;

import android.graphics.Color;

import helper.TeleportService;

public final class WatchFaceUtil extends TeleportService {

    public static final int AMBIENT_BACKGROUND = parseColor("#000000");
    public static final int AMBIENT_TEXT = parseColor("#FFFFFF");

    public static int KEY_BACKGROUND_COLOR = parseColor("#FF9800");
    public static int KEY_MAIN_COLOR = parseColor("#FAFAFA");
    public static int KEY_TEXT_COLOR = parseColor("#424242");
    public static boolean SMOOTH_SECONDS = true;

    public WatchFaceUtil() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    private static int parseColor(String colorName) {
        return Color.parseColor(colorName);
    }

    public static void overwriteKeys(String key) {
        if (key.equals("true") || key.equals("false")) {
            SMOOTH_SECONDS = Boolean.valueOf(key);
        } else if (key.equals("#FAFAFA") || key.equals("#424242") || key.equals("#000000")) {
            KEY_MAIN_COLOR = Color.parseColor(key);
            if (key.equals("#FAFAFA")) {
                KEY_TEXT_COLOR = Color.parseColor("#424242");
            } else {
                KEY_TEXT_COLOR = Color.parseColor("#FAFAFA");
            }
        } else {
            KEY_BACKGROUND_COLOR = Color.parseColor(key);
        }
    }
}