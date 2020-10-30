package com.timrface.watchfacelayout.layout.components.complications;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.wearable.complications.ComplicationData;

import com.timrface.watchfacelayout.config.ComplicationType;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.ColorConstants;
import com.timrface.watchfacelayout.layout.WindowInsets;

import java.util.Calendar;

public class StepsLayout extends Complication {

    private final Paint mStepPaint;
    private final Typeface robotoMedium;
    private final Typeface robotoLight;
    private String stepCount = "-";

    public StepsLayout(Configuration configuration, Context context, Typeface robotoMedium, Typeface robotoLight) {
        super(configuration);
        this.robotoMedium = robotoMedium;
        this.robotoLight = robotoLight;
        mStepPaint = createTextPaint(configuration.getTextColor(), robotoMedium);
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        if (configuration.isShowBatteryLevel()) {
            canvas.drawText(stepCount, centerX / 3.5f, centerY + centerY / 3.5f, mStepPaint);
        }
    }

    @Override
    public void onConfigurationUpdated(Configuration configuration) {
        if (!isInAmbientMode()) {
            mStepPaint.setColor(configuration.getTextColor());
        }
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return configuration.isShowBatteryLevel();
    }

    @Override
    public void applyWindowInsets(WindowInsets windowInsets) {
        mStepPaint.setTextSize(windowInsets.getInfoTextSize());
    }

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
        adjustPaintColorToCurrentMode(mStepPaint, configuration.getTextColor(), ColorConstants.AMBIENT_TEXT_COLOR, inAmbientMode);
        mStepPaint.setTypeface(inAmbientMode ? robotoLight : robotoMedium);
    }

    @Override
    public void onComplicationDataUpdate(ComplicationData complicationData, Context context) {
        stepCount = getComplicationTextOrDefault(complicationData, "-", context);
    }

    @Override
    public ComplicationType getComplicationType() {
        return ComplicationType.STEPS;
    }
}
