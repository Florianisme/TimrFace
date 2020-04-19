package com.timrface.watchfacelayout.config;

import android.graphics.Color;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.timrface.watchfacelayout.R;

public class ConfigUpdater {

    public static void updateConfig(Configuration configuration, DataItem item) {

        if (ConfigurationConstant.CONFIG_PATH.toString().equals(item.getUri().getPath())) {
            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
            if (dataMap.containsKey(ConfigurationConstant.SMOOTH_SECONDS.toString())) {
                configuration.setSmoothScrolling(dataMap.getBoolean(ConfigurationConstant.SMOOTH_SECONDS.toString()));
            }
            if (dataMap.containsKey(ConfigurationConstant.BACKGROUND_COLOR.toString())) {
                int backgroundColor = Color.parseColor(getStringFromDataMap(dataMap, ConfigurationConstant.BACKGROUND_COLOR, "#FAFAFA"));
                boolean isBackgroundColorWhite = backgroundColor == Color.parseColor("#FAFAFA");

                configuration.setBackgroundColor(backgroundColor);
                configuration.setTextColor(isBackgroundColorWhite ? Color.parseColor("#424242") : Color.parseColor("#FAFAFA"));
                configuration.setArrowResourceId(getArrowDrawableResourceIdByBackgroundColor(getStringFromDataMap(dataMap, ConfigurationConstant.BACKGROUND_COLOR, "#FAFAFA")));
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
        }
    }

    private static String getStringFromDataMap(DataMap dataMap, ConfigurationConstant configurationConstant, String defaultValue) {
        String value = dataMap.getString(configurationConstant.toString());
        return value == null ? defaultValue : value;
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
