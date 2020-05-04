package com.timrface.watchfacelayout.layout.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.WindowInsets;

import java.util.Calendar;

public abstract class Layout {

    protected Configuration configuration;
    private boolean ambientMode;

    public Layout(Configuration configuration) {
        this.configuration = configuration;
    }

    public abstract void update(Canvas canvas, float centerX, float centerY, Calendar calendar);

    abstract void onConfigurationUpdated(Configuration configuration);

    public abstract boolean drawWhenInAmbientMode();

    public void applyWindowInsets(WindowInsets windowInsets) {

    }

    public void onSurfaceChanged(int width, int height) {

    }

    public void updateConfiguration(Configuration configuration) {
        this.configuration = configuration;
        onConfigurationUpdated(configuration);
    }

    public void updateAmbientMode(boolean inAmbientMode) {
        this.ambientMode = inAmbientMode;
        onAmbientModeChanged(inAmbientMode);
    }

    void onAmbientModeChanged(boolean inAmbientMode) {

    }

    protected Paint createTextPaint(int color, Typeface typeface) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setTypeface(typeface);
        paint.setAntiAlias(true);
        paint.setLinearText(true);
        return paint;
    }

    protected void adjustPaintColorToCurrentMode(Paint paint, int interactiveColor,
                                                 int ambientColor, boolean isInAmbientMode) {
        paint.setColor(isInAmbientMode ? ambientColor : interactiveColor);
        if (paint.isLinearText()) {
            paint.setAntiAlias(!isInAmbientMode);
        }
    }

    protected boolean isInAmbientMode() {
        return ambientMode;
    }
}
