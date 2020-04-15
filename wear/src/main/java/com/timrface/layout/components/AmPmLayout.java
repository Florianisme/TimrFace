package com.timrface.layout.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.timrface.Configuration;
import com.timrface.layout.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AmPmLayout extends Layout {

    private final Paint mAmPmLayout;
    private final SimpleDateFormat amPmFormat;

    public AmPmLayout(Configuration configuration, Typeface robotoLight) {
        super(configuration);
        mAmPmLayout = createTextPaint(configuration.getTextColor(), robotoLight);
        amPmFormat = new SimpleDateFormat("a", Locale.getDefault());
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        if (!configuration.isAstronomicalClockFormat()) {
            canvas.drawText(getAmPm(calendar), centerX * 2 - centerX / 2, centerY + centerY / 4, mAmPmLayout);
        }
    }

    private String getAmPm(Calendar calendar) {
        return amPmFormat.format(calendar.getTime());
    }

    @Override
    void onConfigurationUpdated(Configuration configuration) {
        mAmPmLayout.setColor(configuration.getTextColor());
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return true;
    }

    @Override
    public void applyWindowInsets(float timeTextSize, float infoTextSize) {
        mAmPmLayout.setTextSize(infoTextSize);
    }

    @Override
    void onAmbientModeChanged(boolean inAmbientMode) {
        adjustPaintColorToCurrentMode(mAmPmLayout, configuration.getTextColor(), Constants.AMBIENT_TEXT_COLOR, inAmbientMode);
    }
}