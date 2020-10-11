package com.timrface.watchfacelayout.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.timrface.watchfacelayout.config.Configuration;

import java.util.Calendar;

public class DayNightBroadcastReceiver extends FilteredBroadcastReceiver {

    private Configuration configuration;
    private final ConfigurationChangeCallback callback;

    public DayNightBroadcastReceiver(Configuration configuration, ConfigurationChangeCallback callback) {
        this.configuration = configuration;
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
            updateDayNightBackgroundColor();
        }
    }

    private void updateDayNightBackgroundColor() {
        if (configuration.isAutomaticLightDarkMode()) {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hour > 19 || hour <= 6) {
                configuration.setBackgroundColor("#000000");
                configuration.setTextColor("#FAFAFA");
            } else {
                configuration.setBackgroundColor("#FAFAFA");
                configuration.setTextColor("#424242");
            }
            callback.onConfigurationChanged(configuration);
        }
    }

    @Override
    public void register(Context context) {
        context.registerReceiver(this, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void updateInternalConfigurationState(Configuration configuration) {
        this.configuration = configuration;
        updateDayNightBackgroundColor();
    }
}
