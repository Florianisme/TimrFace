package com.timrface.watchfacelayout.layout.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.timrface.watchfacelayout.Configuration;
import com.timrface.watchfacelayout.layout.Constants;
import com.timrface.watchfacelayout.layout.WindowInsets;

import java.util.Calendar;

public class BatteryLayout extends Layout {

    private final Paint mBatteryPaint;
    private String batteryLevel;

    public BatteryLayout(Configuration configuration, Context context, Typeface robotoLight) {
        super(configuration);
        mBatteryPaint = createTextPaint(configuration.getTextColor(), robotoLight);

        BroadcastReceiver updateBattery = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra("level", 0);
                batteryLevel = level + "%";
            }
        };

        context.registerReceiver(updateBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        if (configuration.isShowBatteryLevel()) {
            canvas.drawText(batteryLevel, centerX / 2 - centerX / 3, centerY + centerY / 3, mBatteryPaint);
        }
    }

    @Override
    void onConfigurationUpdated(Configuration configuration) {
        if (!isInAmbientMode()) {
            mBatteryPaint.setColor(configuration.getTextColor());
        }
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return configuration.isShowBatteryLevel();
    }

    @Override
    public void applyWindowInsets(WindowInsets windowInsets) {
        mBatteryPaint.setTextSize(windowInsets.getInfoTextSize());
    }

    @Override
    void onAmbientModeChanged(boolean inAmbientMode) {
        adjustPaintColorToCurrentMode(mBatteryPaint, configuration.getTextColor(), Constants.AMBIENT_TEXT_COLOR, inAmbientMode);
    }
}
