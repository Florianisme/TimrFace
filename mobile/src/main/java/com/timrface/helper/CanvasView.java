package com.timrface.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.wearable.complications.ComplicationData;
import android.util.AttributeSet;
import android.view.View;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.config.ConfigurationBuilder;
import com.timrface.watchfacelayout.layout.LayoutProvider;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class CanvasView extends View {

    public long INTERACTIVE_UPDATE_RATE_MS = 30;
    String batteryLevel = "";
    private Calendar calendar;
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

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                layoutProvider.onSurfaceChanged(v.getWidth(), v.getHeight());
            }
        });
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
        layoutProvider.updateComplicationData(null, configuration.getLeftComplicationType(), getContext());
        layoutProvider.updateComplicationData(null, configuration.getMiddleComplicationType(), getContext());
    }

    public void updateTimezone(TimeZone timeZone) {
        this.calendar.clear();
        this.calendar.setTimeZone(timeZone);
    }
}
