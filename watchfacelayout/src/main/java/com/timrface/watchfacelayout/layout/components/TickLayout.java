package com.timrface.watchfacelayout.layout.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.timrface.watchfacelayout.Configuration;
import com.timrface.watchfacelayout.layout.Constants;
import com.timrface.watchfacelayout.layout.WindowInsets;

import java.util.Calendar;

public class TickLayout extends Layout {

    private final Paint mTickPaint;
    private final Paint mThickTickPaint;
    private final Paint mTextTickPaint;
    private final int[] textsForIndixes = new int[]{50, 55, 0, 5, 10, 15, 20, 30, 35, 40, 45, 50, 55, 0, 5, 10};
    private final float distanceBetweenTicks = 16f;
    private final int visibleExtraTicksOnScreen = 12;

    public TickLayout(Configuration configuration, Typeface robotoLight) {
        super(configuration);
        mTickPaint = new Paint();
        mTickPaint.setColor(Constants.TICK_COLOR);
        mTickPaint.setStyle(Paint.Style.STROKE);
        mTickPaint.setStrokeWidth(2f);

        mThickTickPaint = new Paint(mTickPaint);
        mThickTickPaint.setStrokeWidth(4f);

        mTextTickPaint = createTextPaint(Constants.TICK_COLOR, robotoLight);
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        float seconds = getSeconds(calendar);
        float yStart = centerY + centerY / 2;
        for (int i = -visibleExtraTicksOnScreen; i < 60 + visibleExtraTicksOnScreen; i++) {
            if (i + visibleExtraTicksOnScreen < seconds || i - visibleExtraTicksOnScreen > seconds) {
                continue;
            }
            float xPosition = centerX + (distanceBetweenTicks * i) - (seconds * distanceBetweenTicks);
            if (i % 5 == 0) {
                String textForIndex = getTextForIndex(i);
                float textWidth = mTextTickPaint.measureText(textForIndex);
                canvas.drawLine(xPosition, yStart, xPosition, yStart + 36f, mThickTickPaint);
                canvas.drawText(textForIndex, xPosition - (textWidth / 2), yStart + 54f, mTextTickPaint);
            } else {
                canvas.drawLine(xPosition, yStart, xPosition, yStart + 24f, mTickPaint);
            }
        }
    }

    private String getTextForIndex(int i) {
        int index = (i + visibleExtraTicksOnScreen) / 5;
        return String.valueOf(textsForIndixes[index]);
    }

    private float getSeconds(Calendar calendar) {
        if (configuration.isSmoothScrolling()) {
            return (calendar.get(Calendar.SECOND) + (calendar.get(Calendar.MILLISECOND) / 1000f));
        }
        return calendar.get(Calendar.SECOND);
    }

    @Override
    void onConfigurationUpdated(Configuration configuration) {
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return false;
    }

    @Override
    public void applyWindowInsets(WindowInsets windowInsets) {
        mTextTickPaint.setTextSize(windowInsets.getTickTextSize());
    }

    @Override
    void onAmbientModeChanged(boolean inAmbientMode) {

    }
}
