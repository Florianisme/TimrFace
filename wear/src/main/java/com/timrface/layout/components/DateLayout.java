package com.timrface.layout.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import com.timrface.Configuration;
import com.timrface.layout.Constants;

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
        mDatePaint.setColor(configuration.getTextColor());
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return true;
    }

    @Override
    public void applyWindowInsets(float timeTextSize, float infoTextSize) {
        mDatePaint.setTextSize(infoTextSize);
    }

    @Override
    void onAmbientModeChanged(boolean inAmbientMode) {
        adjustPaintColorToCurrentMode(mDatePaint, configuration.getTextColor(), Constants.AMBIENT_TEXT_COLOR, inAmbientMode);
    }

    private String getDate(Calendar calendar) {
        return dateFormat.format(calendar.getTime());
    }
}
