package com.timrface.watchfacelayout.layout.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.timrface.watchfacelayout.Configuration;
import com.timrface.watchfacelayout.layout.WindowInsets;

import java.util.Calendar;

public class ShadowLayout extends Layout {

    private final Paint mShadowPaint;
    private final Bitmap shadowBitmap;

    public ShadowLayout(Configuration configuration, Bitmap shadowBitmap) {
        super(configuration);
        this.shadowBitmap = shadowBitmap;
        mShadowPaint = new Paint();
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        canvas.drawBitmap(shadowBitmap, centerX - 25, centerY + centerY / 2.5f, mShadowPaint);
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

    }

    @Override
    void onAmbientModeChanged(boolean inAmbientMode) {

    }
}
