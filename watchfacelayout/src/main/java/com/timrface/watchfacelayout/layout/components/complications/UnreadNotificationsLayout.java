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

public class UnreadNotificationsLayout extends Complication {

    private final VectorDrawableCompat unreadDrawable;
    private final VectorDrawableCompat unreadDrawableOutline;
    private final Paint mCountPaint;
    private final Typeface robotoMedium;
    private final Typeface robotoLight;

    private String complicationText = "0";

    public UnreadNotificationsLayout(Configuration configuration, VectorDrawableCompat unreadDrawable, VectorDrawableCompat unreadDrawableOutline, Typeface robotoMedium, Typeface robotoLight) {
        super(configuration);
        this.unreadDrawable = unreadDrawable;
        this.unreadDrawableOutline = unreadDrawableOutline;
        mCountPaint = createTextPaint(configuration.getTextColor(), robotoMedium);
        this.robotoMedium = robotoMedium;
        this.robotoLight = robotoLight;

        this.unreadDrawable.setTint(configuration.getTextColor());
        this.unreadDrawableOutline.setTint(ColorConstants.AMBIENT_TEXT_COLOR);
    }

    @Override
    public void onComplicationDataUpdate(ComplicationData complicationData, Context context) {
        complicationText = getComplicationTextOrDefault(complicationData, "0", context);
        mCountPaint.getTextBounds(complicationText, 0, complicationText.length(), textRect);
    }

    @Override
    public ComplicationType getComplicationType() {
        return ComplicationType.NOTIFICATIONS;
    }


    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        float textXPosition = getTextXPosition(centerX);
        float textYPosition = getTextYPosition(centerY);

        Rect iconPositionRect = getIconPositionRect(textXPosition, textYPosition);

        if (isInAmbientMode()) {
            unreadDrawableOutline.setBounds(iconPositionRect);
            unreadDrawableOutline.draw(canvas);
        } else {
            unreadDrawable.setBounds(iconPositionRect);
            unreadDrawable.draw(canvas);
        }
        canvas.drawText(complicationText, textXPosition, textYPosition, mCountPaint);
    }

    @Override
    public void onConfigurationUpdated(Configuration configuration) {
        if (!isInAmbientMode()) {
            mCountPaint.setColor(configuration.getTextColor());
            unreadDrawable.setTint(configuration.getTextColor());
        }
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return true;
    }

    @Override
    public void applyWindowInsets(WindowInsets windowInsets) {
        mCountPaint.setTextSize(windowInsets.getInfoTextSize());
    }

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
        adjustPaintColorToCurrentMode(mCountPaint, configuration.getTextColor(), ColorConstants.AMBIENT_TEXT_COLOR, inAmbientMode);
        mCountPaint.setTypeface(inAmbientMode ? robotoLight : robotoMedium);
    }
}
