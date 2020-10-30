package com.timrface.watchfacelayout.layout.components.complications;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.wearable.complications.ComplicationData;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.timrface.watchfacelayout.config.ComplicationSide;
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

        this.shoeDrawable.setTint(configuration.getTextColor());
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Rect iconRect = getIconRect(width, height);
        shoeDrawable.setBounds(iconRect);
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        shoeDrawable.draw(canvas);
        if (configuration.isShowBatteryLevel()) {
            canvas.drawText(stepCount, getTextPosition(centerX), centerY + centerY / 3.5f, mStepPaint);
        }
    }

    @Override
    public void onConfigurationUpdated(Configuration configuration) {
        if (!isInAmbientMode()) {
            mStepPaint.setColor(configuration.getTextColor());
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
        return centerX / 2.8f;
    }

    private float getMiddleTextXPosition(float centerX) {
        return centerX * 1.02f;
    }

    private Rect getIconMiddleRect(int width, int height) {
        float centerY = height / 2f;
        int positionX = (int) (width * 0.45);
        int positionY = (int) (centerY + centerY / 5.3f);
        int boundsSize = (int) (width * 0.06);

        return new Rect(positionX, positionY, positionX + boundsSize, positionY + boundsSize);
    }

    private Rect getIconLeftRect(int width, int height) {
        float centerY = height / 2f;
        int positionX = (int) (width * 0.11);
        int positionY = (int) (centerY + centerY / 5.3f);
        int boundsSize = (int) (width * 0.06);

        return new Rect(positionX, positionY, positionX + boundsSize, positionY + boundsSize);
    }

    protected Rect getIconRect(int width, int height) {
        if (complicationSide == ComplicationSide.LEFT) {
            return getIconLeftRect(width, height);
        } else {
            return getIconMiddleRect(width, height);
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

        if (inAmbientMode) {
            shoeDrawable.setTint(ColorConstants.AMBIENT_TEXT_COLOR);
        } else {
            shoeDrawable.setTint(configuration.getTextColor());
        }
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
