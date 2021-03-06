package com.timrface.watchfacelayout.layout.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.ColorConstants;
import com.timrface.watchfacelayout.layout.WindowInsets;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeDigits extends Layout {

    private final Typeface robotoLight;
    private final Typeface robotoThin;
    private final Paint mHourPaint;
    private final Paint mMinutePaint;
    private float measuredTwoDigitTextSize = 0f;
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
        float hourWidthMeasured = hasTwoDigits(hours) ? measuredTwoDigitTextSize : measuredTwoDigitTextSize / 2;
        float minutesWidthMeasured = hasTwoDigits(minutes) ? measuredTwoDigitTextSize : measuredTwoDigitTextSize / 2;
        float yPosition = centerY + centerY / 15;

        canvas.drawText(hours, centerX - (hourWidthMeasured + (hourWidthMeasured / 20)), yPosition, mHourPaint);
        canvas.drawText(minutes, centerX + (minutesWidthMeasured / 20), yPosition, mMinutePaint);
    }

    private boolean hasTwoDigits(String hours) {
        return hours.length() == 2;
    }

    @Override
    public void onConfigurationUpdated(Configuration configuration) {
        if (!isInAmbientMode()) {
            mHourPaint.setColor(configuration.getTextColor());
            mMinutePaint.setColor(configuration.getInteractiveColor());
        }
        setTypefaceAndStyle(isInAmbientMode());

        hourFormat = new SimpleDateFormat();
        hourFormat.applyLocalizedPattern(configuration.isAstronomicalClockFormat() ? "H" : "h");
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return true;
    }

    @Override
    public void applyWindowInsets(WindowInsets windowInsets) {
        mHourPaint.setTextSize(windowInsets.getTimeTextSize());
        mMinutePaint.setTextSize(windowInsets.getTimeTextSize());
        measuredTwoDigitTextSize = mHourPaint.measureText("00");
    }

    @Override
    public void onAmbientModeChanged(boolean ambientEnabled) {
        adjustPaintColorToCurrentMode(mHourPaint, configuration.getTextColor(), ColorConstants.AMBIENT_TEXT_COLOR, ambientEnabled);
        adjustPaintColorToCurrentMode(mMinutePaint, configuration.getInteractiveColor(), ColorConstants.AMBIENT_TEXT_COLOR, ambientEnabled);

        setTypefaceAndStyle(ambientEnabled);
    }

    private void setTypefaceAndStyle(boolean ambientEnabled) {
        if (configuration.isUseStrokeDigitsInAmbientMode()) {
            mHourPaint.setTypeface(robotoLight);
            mMinutePaint.setTypeface(robotoLight);
            mHourPaint.setStyle(ambientEnabled ? Paint.Style.STROKE : Paint.Style.FILL);
            mMinutePaint.setStyle(ambientEnabled ? Paint.Style.STROKE : Paint.Style.FILL);
        } else {
            mHourPaint.setStyle(Paint.Style.FILL);
            mMinutePaint.setStyle(Paint.Style.FILL);
            mHourPaint.setTypeface(ambientEnabled ? robotoThin : robotoLight);
            mMinutePaint.setTypeface(ambientEnabled ? robotoThin : robotoLight);
        }
    }

    private String getHours(Calendar calendar) {
        return formatTwoDigits(hourFormat.format(calendar.getTime()));
    }

    private String formatTwoDigits(String number) {
        if (configuration.isShowZeroDigit()) {
            return number.length() < 2 ? "0" + number : "" + number;
        }
        return number;
    }

    private String getMinutes(Calendar calendar) {
        return formatTwoDigits(Integer.toString(calendar.get(Calendar.MINUTE)));
    }
}
