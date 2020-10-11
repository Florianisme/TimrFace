package com.timrface.watchfacelayout.config;

import android.graphics.Color;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

public class ConfigUpdater {

    public static void updateConfig(Configuration configuration, DataItem item) {

        if (item.getUri().getPath().startsWith(ConfigurationConstant.CONFIG_PATH.toString())) {
            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
            if (dataMap.containsKey(ConfigurationConstant.SMOOTH_SECONDS.toString())) {
                configuration.setSmoothScrolling(dataMap.getBoolean(ConfigurationConstant.SMOOTH_SECONDS.toString()));
            }
            if (dataMap.containsKey(ConfigurationConstant.BACKGROUND_COLOR.toString())) {
                int backgroundColor = Color.parseColor(getStringFromDataMap(dataMap, ConfigurationConstant.BACKGROUND_COLOR, "#FAFAFA"));
                boolean isBackgroundColorWhite = backgroundColor == Color.parseColor("#FAFAFA");

                configuration.setBackgroundColor(backgroundColor);
                configuration.setTextColor(isBackgroundColorWhite ? Color.parseColor("#424242") : Color.parseColor("#FAFAFA"));
            }
            if (dataMap.containsKey(ConfigurationConstant.INTERACTIVE_COLOR.toString())) {
                configuration.setInteractiveColor(Color.parseColor(getStringFromDataMap(dataMap, ConfigurationConstant.INTERACTIVE_COLOR, "#FF9800")));
            }
            if (dataMap.containsKey(ConfigurationConstant.BATTERY_INDICATOR.toString())) {
                configuration.setShowBatteryLevel(dataMap.getBoolean(ConfigurationConstant.BATTERY_INDICATOR.toString(), true));
            }
            if (dataMap.containsKey(ConfigurationConstant.ZERO_DIGIT.toString())) {
                configuration.setShowZeroDigit(dataMap.getBoolean(ConfigurationConstant.ZERO_DIGIT.toString(), true));
            }
            if (dataMap.containsKey(ConfigurationConstant.UNREAD_NOTIFICATIONS.toString())) {
                configuration.setShowUnreadNotificationsCounter(dataMap.getBoolean(ConfigurationConstant.UNREAD_NOTIFICATIONS.toString(), true));
            }
            if (dataMap.containsKey(ConfigurationConstant.STROKE_DIGITS.toString())) {
                configuration.setUseStrokeDigitsInAmbientMode(dataMap.getBoolean(ConfigurationConstant.STROKE_DIGITS.toString(), true));
            }
            if (dataMap.containsKey(ConfigurationConstant.AUTOMATIC_DARK_LIGHT.toString())) {
                configuration.setAutomaticDarkLightMode(dataMap.getBoolean(ConfigurationConstant.AUTOMATIC_DARK_LIGHT.toString(), false));
            }
        }
    }

    private static String getStringFromDataMap(DataMap dataMap, ConfigurationConstant configurationConstant, String defaultValue) {
        String value = dataMap.getString(configurationConstant.toString());
        return value == null ? defaultValue : value;
    }
}
