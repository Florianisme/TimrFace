package com.timrface.watchfacelayout.layout.components.complications;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.wearable.complications.ComplicationData;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.timrface.watchfacelayout.config.ComplicationType;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.ColorConstants;
import com.timrface.watchfacelayout.layout.WindowInsets;

import java.util.Calendar;

public class BatteryLayout extends Complication {

    private final Paint mBatteryPaint;
    private final VectorDrawableCompat batteryDrawable;
    private final VectorDrawableCompat batteryOutlineDrawable;
    private final Typeface robotoMedium;
    private final Typeface robotoLight;
    private String batteryLevel = "-";

    public BatteryLayout(Configuration configuration, Context context, VectorDrawableCompat batteryDrawable, VectorDrawableCompat batteryOutlineDrawable, Typeface robotoMedium, Typeface robotoLight) {
        super(configuration);
        this.batteryDrawable = batteryDrawable;
        this.batteryOutlineDrawable = batteryOutlineDrawable;
        this.robotoMedium = robotoMedium;
        this.robotoLight = robotoLight;
        mBatteryPaint = createTextPaint(configuration.getTextColor(), robotoMedium);

        this.batteryDrawable.setTint(configuration.getTextColor());
        this.batteryOutlineDrawable.setTint(ColorConstants.AMBIENT_TEXT_COLOR);
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        float textYPosition = getTextYPosition(centerY);
        float textXPosition = getTextXPosition(centerX);

        Rect iconPositionRect = getIconPositionRect(textXPosition, textYPosition);

        if (isInAmbientMode()) {
            batteryOutlineDrawable.setBounds(iconPositionRect);
            batteryOutlineDrawable.draw(canvas);
        } else {
            batteryDrawable.setBounds(iconPositionRect);
            batteryDrawable.draw(canvas);
        }

        canvas.drawText(batteryLevel, textXPosition, textYPosition, mBatteryPaint);
    }

    @Override
    public void onConfigurationUpdated(Configuration configuration) {
        if (!isInAmbientMode()) {
            mBatteryPaint.setColor(configuration.getTextColor());
        }
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return true;
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
        mBatteryPaint.getTextBounds(batteryLevel, 0, batteryLevel.length(), textRect);
    }

    @Override
    public ComplicationType getComplicationType() {
        return ComplicationType.BATTERY;
    }
}
