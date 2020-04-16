package com.timrface.watchfacelayout;

import android.graphics.Color;

public class ConfigurationBuilder {

    public static Configuration getDefaultConfiguration() {
        return new Configuration()
                .setShowBatteryLevel(true)
                .setSmoothScrolling(true)
                .setBackgroundColor(Color.parseColor("#FAFAFA"))
                .setArrowResourceId(R.drawable.indicator)
                .setInteractiveColor(Color.parseColor("#FF9800"))
                .setTextColor(Color.parseColor("#424242"))
                .setShowZeroDigit(true)
                .setAstronomicalClockFormat(true);
    }

}
