package com.timrface.watchfacelayout.layout.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.timrface.watchfacelayout.config.Configuration;

import java.util.Calendar;

public class ChinLayout extends Layout {

    private final Paint mChinPaint;

    public ChinLayout(Configuration configuration) {
        super(configuration);
        mChinPaint = new Paint();
        mChinPaint.setColor(configuration.getInteractiveColor());
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        canvas.drawRect(0, centerY + centerY / 2.5f, centerX * 2, centerY * 2, mChinPaint);
    }

    @Override
    public void onConfigurationUpdated(Configuration configuration) {
        mChinPaint.setColor(configuration.getInteractiveColor());
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return false;
    }
}
