package com.timrface.watchfacelayout.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;

import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.LayoutProvider;
import com.timrface.watchfacelayout.util.ConfigurationAwareBroadcastReceiver;
import com.timrface.watchfacelayout.util.ConfigurationChangeCallback;
import com.timrface.watchfacelayout.util.FilteredBroadcastReceiver;

import java.util.Calendar;
import java.util.TimeZone;


public class TimeFormatChangedReceiver extends FilteredBroadcastReceiver {

    private Configuration configuration;
    private final ConfigurationChangeCallback configurationChangeCallback;

    public TimeFormatChangedReceiver(Configuration configuration, ConfigurationChangeCallback configurationChangeCallback) {
        this.configuration = configuration;
        this.configurationChangeCallback = configurationChangeCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        configuration.setAstronomicalClockFormat(DateFormat.is24HourFormat(context));
        configurationChangeCallback.onConfigurationChanged(configuration);
    }

    @Override
    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);

        context.registerReceiver(this, intentFilter);
    }

    @Override
    public void updateInternalConfigurationState(Configuration configuration) {
        this.configuration = configuration;
    }
}
