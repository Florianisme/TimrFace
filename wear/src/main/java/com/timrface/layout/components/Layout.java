package com.timrface.layout.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.timrface.Configuration;

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

    public abstract void applyWindowInsets(float timeTextSize, float infoTextSize);

    public void updateConfiguration(Configuration configuration) {
        this.configuration = configuration;
        onConfigurationUpdated(configuration);
    }

    public void updateAmbientMode(boolean inAmbientMode) {
        this.ambientMode = inAmbientMode;
        onAmbientModeChanged(inAmbientMode);
    }

    abstract void onAmbientModeChanged(boolean inAmbientMode);

    protected Paint createTextPaint(int color, Typeface typeface) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setTypeface(typeface);
        paint.setAntiAlias(true);
        return paint;
    }

    protected void adjustPaintColorToCurrentMode(Paint paint, int interactiveColor,
                                                 int ambientColor, boolean isInAmbientMode) {
        paint.setColor(isInAmbientMode ? ambientColor : interactiveColor);
    }
}
