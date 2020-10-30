package com.timrface.watchfacelayout.layout.components.complications;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationText;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

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

    private String complicationText = "-";

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
        complicationText = getComplicationTextOrDefault(complicationData, "-", context);
    }

    @Override
    public ComplicationType getComplicationType() {
        return ComplicationType.NOTIFICATIONS;
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        float centerY = height / 2f;
        int positionX = (int) (width * 0.45);
        int positionY = (int) (centerY + centerY / 5.3f);
        int boundsSize = (int) (width * 0.06);

        Rect iconRect = new Rect(positionX, positionY, positionX + boundsSize, positionY + boundsSize);
        unreadDrawable.setBounds(iconRect);
        unreadDrawableOutline.setBounds(iconRect);
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        if (configuration.isShowUnreadNotificationsCounter()) {
            if (isInAmbientMode()) {
                unreadDrawableOutline.draw(canvas);
            } else {
                unreadDrawable.draw(canvas);
            }
            canvas.drawText(complicationText, centerX * 1.02f, centerY + centerY / 3.5f, mCountPaint);
        }
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
