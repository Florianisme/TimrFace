package com.timrface.watchfacelayout.layout.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.ColorConstants;
import com.timrface.watchfacelayout.layout.WindowInsets;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateLayout extends Layout {

    private final Paint mDatePaint;

    private final SimpleDateFormat dateFormat;

    public DateLayout(Configuration configuration, Typeface robotoLight) {
        super(configuration);
        mDatePaint = createTextPaint(configuration.getTextColor(), robotoLight);
        mDatePaint.setTextAlign(Paint.Align.CENTER);
        dateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEE, dMMMM"));
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        canvas.drawText(getDate(calendar), centerX - mDatePaint.getStrokeWidth() / 2, centerY / 3 + centerY / 25, mDatePaint);
    }

    @Override
    void onConfigurationUpdated(Configuration configuration) {
        if (!isInAmbientMode()) {
            mDatePaint.setColor(configuration.getTextColor());
        }
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return true;
    }

    @Override
    public void applyWindowInsets(WindowInsets windowInsets) {
        mDatePaint.setTextSize(windowInsets.getInfoTextSize());
    }

    @Override
    void onAmbientModeChanged(boolean inAmbientMode) {
        adjustPaintColorToCurrentMode(mDatePaint, configuration.getTextColor(), ColorConstants.AMBIENT_TEXT_COLOR, inAmbientMode);
    }

    private String getDate(Calendar calendar) {
        return dateFormat.format(calendar.getTime());
    }
}
