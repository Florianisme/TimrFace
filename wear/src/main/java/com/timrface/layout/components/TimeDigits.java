package com.timrface.layout.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.timrface.Configuration;
import com.timrface.layout.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeDigits extends Layout {

    private final Typeface robotoLight;
    private final Typeface robotoThin;
    private final Paint mHourPaint;
    private final Paint mMinutePaint;
    SimpleDateFormat hourFormat;

    public TimeDigits(Configuration configuration, Typeface robotoLight, Typeface robotoThin) {
        super(configuration);
        this.robotoLight = robotoLight;
        this.robotoThin = robotoThin;
        mHourPaint = createTextPaint(configuration.getTextColor(), robotoLight);
        mMinutePaint = createTextPaint(configuration.getInteractiveColor(), robotoLight);

        hourFormat = new SimpleDateFormat();
        hourFormat.applyLocalizedPattern(configuration.isAstronomicalClockFormat() ? "H" : "h");
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        String hours = getHours(calendar);
        String minutes = getMinutes(calendar);
        float hourWidthMeasured = mHourPaint.measureText(getHours(calendar));
        float minutesWidthMeasured = mMinutePaint.measureText(getMinutes(calendar));
        float yPosition = centerY + centerY / 15;

        canvas.drawText(hours, centerX - (hourWidthMeasured + (hourWidthMeasured / 20)), yPosition, mHourPaint);
        canvas.drawText(minutes, centerX + (minutesWidthMeasured / 20), yPosition, mMinutePaint);
    }

    @Override
    public void onConfigurationUpdated(Configuration configuration) {
        mHourPaint.setColor(configuration.getTextColor());
        mMinutePaint.setColor(configuration.getInteractiveColor());
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return true;
    }

    @Override
    public void applyWindowInsets(float timeTextSize, float infoTextSize) {
        mHourPaint.setTextSize(timeTextSize);
        mMinutePaint.setTextSize(timeTextSize);
    }

    @Override
    public void onAmbientModeChanged(boolean ambientEnabled) {
        adjustPaintColorToCurrentMode(mHourPaint, configuration.getTextColor(), Constants.AMBIENT_TEXT_COLOR, ambientEnabled);
        adjustPaintColorToCurrentMode(mMinutePaint, configuration.getInteractiveColor(), Constants.AMBIENT_TEXT_COLOR, ambientEnabled);

        mHourPaint.setTypeface(ambientEnabled ? robotoThin : robotoLight);
        mMinutePaint.setTypeface(ambientEnabled ? robotoThin : robotoLight);
    }

    private String getHours(Calendar calendar) {
        return formatTwoDigits(Integer.parseInt(hourFormat.format(calendar.getTime())));
    }

    private String formatTwoDigits(int number) {
        if (configuration.isShowZeroDigit()) {
            return String.format(Locale.getDefault(), "%02d", number);
        }
        return String.valueOf(number);
    }

    private String getMinutes(Calendar calendar) {
        return formatTwoDigits(calendar.get(Calendar.MINUTE));
    }
}
