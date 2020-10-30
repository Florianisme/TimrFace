package com.timrface.watchfacelayout.layout.components.complications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.wearable.complications.ComplicationData;

import com.timrface.watchfacelayout.config.ComplicationSide;
import com.timrface.watchfacelayout.config.ComplicationType;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.ColorConstants;
import com.timrface.watchfacelayout.layout.WindowInsets;
import com.timrface.watchfacelayout.layout.components.Layout;

import java.util.Calendar;

public class BatteryLayout extends Complication {

    private final Paint mBatteryPaint;
    private final Typeface robotoMedium;
    private final Typeface robotoLight;
    private String batteryLevel = "-";

    public BatteryLayout(Configuration configuration, Context context, Typeface robotoMedium, Typeface robotoLight) {
        super(configuration);
        this.robotoMedium = robotoMedium;
        this.robotoLight = robotoLight;
        mBatteryPaint = createTextPaint(configuration.getTextColor(), robotoMedium);
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        if (configuration.isShowBatteryLevel()) {
            canvas.drawText(batteryLevel, getTextPosition(centerX), centerY + centerY / 3.5f, mBatteryPaint);
        }
    }

    protected float getTextPosition(float centerX) {
        if (complicationSide == ComplicationSide.LEFT) {
            return getLeftTextXPosition(centerX);
        } else {
            return getMiddleTextXPosition(centerX);
        }
    }

    private float getLeftTextXPosition(float centerX) {
        return centerX / 3.5f;
    }

    private float getMiddleTextXPosition(float centerX) {
        return centerX * 0.90f;
    }

    @Override
    public void onConfigurationUpdated(Configuration configuration) {
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
    public void onAmbientModeChanged(boolean inAmbientMode) {
        adjustPaintColorToCurrentMode(mBatteryPaint, configuration.getTextColor(), ColorConstants.AMBIENT_TEXT_COLOR, inAmbientMode);
        mBatteryPaint.setTypeface(inAmbientMode ? robotoLight : robotoMedium);
    }

    @Override
    public void onComplicationDataUpdate(ComplicationData complicationData, Context context) {
        batteryLevel = getComplicationTextOrDefault(complicationData, "-%", context);
    }

    @Override
    public ComplicationType getComplicationType() {
        return ComplicationType.BATTERY;
    }
}
