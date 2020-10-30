package com.timrface.watchfacelayout.config;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;

public class ConfigurationBuilder {

    public static Configuration getDefaultConfiguration(Context context) {
        return new Configuration()
                .setShowBatteryLevel(true)
                .setSmoothScrolling(true)
                .setBackgroundColor(Color.parseColor("#FAFAFA"))
                .setInteractiveColor(Color.parseColor("#FF9800"))
                .setTextColor(Color.parseColor("#424242"))
                .setShowZeroDigit(true)
                .setAstronomicalClockFormat(DateFormat.is24HourFormat(context))
                .setShowUnreadNotificationsCounter(true)
                .setUseStrokeDigitsInAmbientMode(false)
                .setAutomaticDarkLightMode(false)
                .setLeftComplicationType(ComplicationType.BATTERY)
                .setMiddleComplicationType(ComplicationType.NOTIFICATIONS);
    }

}
