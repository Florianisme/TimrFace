package com.timrface;

import android.graphics.Color;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.timrface.watchfacelayout.Configuration;

public class ConfigUpdater {

    public static void updateConfig(Configuration configuration, DataItem item) {

        if ("/watch_face_config".equals(item.getUri().getPath())) {
            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
            if (dataMap.containsKey("SMOOTH_SECONDS")) {
                configuration.setSmoothScrolling(dataMap.getBoolean("SMOOTH_SECONDS"));
            }
            if (dataMap.containsKey("BACKGROUND_COLOR")) {
                int backgroundColor = Color.parseColor(dataMap.getString("BACKGROUND_COLOR"));
                boolean isBackgroundColorWhite = backgroundColor == Color.parseColor("#FAFAFA");

                configuration.setBackgroundColor(backgroundColor);
                configuration.setTextColor(isBackgroundColorWhite ? Color.parseColor("#424242") : Color.parseColor("#FAFAFA"));
                configuration.setArrowResourceId(getArrowDrawableResourceIdByBackgroundColor(dataMap.getString("BACKGROUND_COLOR")));
            }
            if (dataMap.containsKey("COLOR")) {
                configuration.setInteractiveColor(Color.parseColor(dataMap.getString("COLOR")));
            }
            if (dataMap.containsKey("COLOR_MANUAL")) {
                configuration.setInteractiveColor(dataMap.getInt("COLOR_MANUAL"));
            }
            if (dataMap.containsKey("BATTERY_INDICATOR")) {
                configuration.setShowBatteryLevel(dataMap.getBoolean("BATTERY_INDICATOR", true));
            }
            if (dataMap.containsKey("ZERO_DIGIT")) {
                configuration.setShowZeroDigit(dataMap.getBoolean("ZERO_DIGIT", true));
            }
        }
    }


    private static int getArrowDrawableResourceIdByBackgroundColor(String color) {
        switch (color) {
            case "#424242":
                return R.drawable.indicator_grey;
            case "#000000":
                return R.drawable.indicator_black;
            default:
                return R.drawable.indicator;
        }
    }
}
