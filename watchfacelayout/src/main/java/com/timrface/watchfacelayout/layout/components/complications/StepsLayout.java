package com.timrface.watchfacelayout.layout.components.complications;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.wearable.complications.ComplicationData;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.timrface.watchfacelayout.config.ComplicationType;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.ColorConstants;
import com.timrface.watchfacelayout.layout.WindowInsets;

import java.util.Calendar;

public class StepsLayout extends Complication {

    private final Paint mStepPaint;
    private final VectorDrawableCompat shoeDrawable;
    private final Typeface robotoMedium;
    private final Typeface robotoLight;
    private String stepCount = "-";

    public StepsLayout(Configuration configuration, Context context, VectorDrawableCompat shoeDrawable, Typeface robotoMedium, Typeface robotoLight) {
        super(configuration);
        this.shoeDrawable = shoeDrawable;
        this.robotoMedium = robotoMedium;
        this.robotoLight = robotoLight;

        mStepPaint = createTextPaint(configuration.getTextColor(), robotoMedium);

        onAmbientModeChanged(isInAmbientMode());
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        float textX = getTextXPosition(centerX);
        float textY = getTextYPosition(centerY);
        shoeDrawable.setBounds(getIconPositionRect(textX, textY));

        canvas.drawText(stepCount, textX, textY, mStepPaint);
        shoeDrawable.draw(canvas);
    }

    @Override
    public void onConfigurationUpdated(Configuration configuration) {
        if (!isInAmbientMode()) {
            mStepPaint.setColor(configuration.getTextColor());
        }
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return true;
    }

    @Override
    public void applyWindowInsets(WindowInsets windowInsets) {
        mStepPaint.setTextSize(windowInsets.getInfoTextSize());
    }

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
        adjustPaintColorToCurrentMode(mStepPaint, configuration.getTextColor(), ColorConstants.AMBIENT_TEXT_COLOR, inAmbientMode);
        mStepPaint.setTypeface(inAmbientMode ? robotoLight : robotoMedium);

        if (inAmbientMode) {
            shoeDrawable.setTint(ColorConstants.AMBIENT_TEXT_COLOR);
        } else {
            shoeDrawable.setTint(configuration.getTextColor());
        }
    }

    @Override
    public void onComplicationDataUpdate(ComplicationData complicationData, Context context) {
        stepCount = getComplicationTextOrDefault(complicationData, "0", context);
        mStepPaint.getTextBounds(stepCount, 0, stepCount.length(), textRect);
    }

    @Override
    public ComplicationType getComplicationType() {
        return ComplicationType.STEPS;
    }
}
