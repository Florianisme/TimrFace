package com.timrface.watchfacelayout.layout.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.timrface.watchfacelayout.Configuration;
import com.timrface.watchfacelayout.layout.Constants;
import com.timrface.watchfacelayout.layout.WindowInsets;

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
            canvas.drawText(getAmPm(calendar), centerX * 2 - centerX / 2, centerY + centerY / 3, mAmPmLayout);
        }
    }

    private String getAmPm(Calendar calendar) {
        return amPmFormat.format(calendar.getTime());
    }

    @Override
    void onConfigurationUpdated(Configuration configuration) {
        if (!isInAmbientMode()) {
            mAmPmLayout.setColor(configuration.getTextColor());
        }
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return true;
    }

    @Override
    public void applyWindowInsets(WindowInsets windowInsets) {
        mAmPmLayout.setTextSize(windowInsets.getInfoTextSize());
    }

    @Override
    void onAmbientModeChanged(boolean inAmbientMode) {
        adjustPaintColorToCurrentMode(mAmPmLayout, configuration.getTextColor(), Constants.AMBIENT_TEXT_COLOR, inAmbientMode);
    }
}
