package com.timrface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.LayoutProvider;

import java.util.Calendar;
import java.util.TimeZone;


public class TimeFormatChangedReceiver {

    private BroadcastReceiver broadcastReceiver;

    public void register(Context context, Configuration configuration, LayoutProvider layoutProvider, Calendar calendar) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);

        instantiateBroadcastReceiver(configuration, layoutProvider, calendar);

        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void unregister(Context context) {
        context.unregisterReceiver(broadcastReceiver);
    }

    private void instantiateBroadcastReceiver(Configuration configuration, LayoutProvider layoutProvider, Calendar calendar) {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                calendar.clear();
                calendar.setTimeZone(TimeZone.getDefault());
                configuration.setAstronomicalClockFormat(DateFormat.is24HourFormat(context));
                layoutProvider.onConfigurationChange(configuration);

            }
        };
    }


}
