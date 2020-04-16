package com.timrface.watchfacelayout.layout.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.timrface.watchfacelayout.Configuration;

import java.util.Calendar;

public class BackgroundLayout extends Layout {

    private final Paint mBackgroundPaint;

    public BackgroundLayout(Configuration configuration) {
        super(configuration);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(configuration.getBackgroundColor());
        mBackgroundPaint.setShadowLayer(8.0f, 0.0f, 8.0f, Color.parseColor("#20000000"));
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        canvas.drawRect(0, 0, centerX * 2, centerY + centerY / 5 + centerY / 11, mBackgroundPaint);
    }

    @Override
    void onConfigurationUpdated(Configuration configuration) {
        mBackgroundPaint.setColor(configuration.getBackgroundColor());
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return false;
    }

    @Override
    public void applyWindowInsets(float timeTextSize, float infoTextSize) {

    }

    @Override
    void onAmbientModeChanged(boolean inAmbientMode) {

    }
}
