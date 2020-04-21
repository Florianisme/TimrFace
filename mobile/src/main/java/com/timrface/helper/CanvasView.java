package com.timrface.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.config.ConfigurationBuilder;
import com.timrface.watchfacelayout.layout.LayoutProvider;

import java.util.Calendar;


public class CanvasView extends View {

    public long INTERACTIVE_UPDATE_RATE_MS = 30;
    String batteryLevel = "";
    private final Calendar calendar;
    private final LayoutProvider layoutProvider;

    public CanvasView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        BroadcastReceiver updateBattery = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                int level = intent.getIntExtra("level", 0);
                batteryLevel = level + "%";
            }
        };
        getContext().registerReceiver(updateBattery,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        calendar = Calendar.getInstance();
        layoutProvider = new LayoutProvider().init(ConfigurationBuilder.getDefaultConfiguration(getContext()), getContext());
        layoutProvider.applyWindowInsets(getResources());
    }

    @Override
    public void onDraw(Canvas canvas) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        float centerX = canvas.getWidth() / 2f;
        float centerY = canvas.getHeight() / 2f;
        layoutProvider.update(canvas, centerX, centerY, calendar);
    }

    public void updateConfig(Configuration configuration) {
        layoutProvider.onConfigurationChange(configuration);
    }
}
