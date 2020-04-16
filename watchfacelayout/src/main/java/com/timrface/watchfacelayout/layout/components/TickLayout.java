package com.timrface.watchfacelayout.layout.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.timrface.watchfacelayout.Configuration;

import java.util.Calendar;

public class TickLayout extends Layout {

    private final Paint mTickPaint;
    private final Bitmap scale;

    public TickLayout(Configuration configuration, Bitmap scale) {
        super(configuration);
        this.scale = scale;
        mTickPaint = new Paint();

        mTickPaint.setAntiAlias(true);
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {

        canvas.drawBitmap(scale, (getSeconds(calendar) * (-11.1f)) + centerX - 676.6f, centerY + centerY / 4 + centerY / 8, mTickPaint);
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
    public void applyWindowInsets(float timeTextSize, float infoTextSize) {

    }

    @Override
    void onAmbientModeChanged(boolean inAmbientMode) {

    }
}
