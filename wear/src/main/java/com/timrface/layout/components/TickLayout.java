package com.timrface.layout.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.timrface.Configuration;

import java.util.Calendar;

public class TickLayout extends Layout {

    private final Paint mTickPaint;
    private final Paint mArrowPaint;
    private final Bitmap scale;
    private final Context context;
    private Bitmap indicator;

    public TickLayout(Configuration configuration, Bitmap scale, Bitmap indicator, Context context) {
        super(configuration);
        this.scale = scale;
        this.indicator = indicator;
        this.context = context;
        mTickPaint = new Paint();
        mArrowPaint = new Paint();

        mTickPaint.setAntiAlias(true);
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {

        canvas.drawBitmap(scale, (getSeconds(calendar) * (-11.1f)) + centerX - 676.6f, centerY + centerY / 4 + centerY / 8, mTickPaint);
        canvas.drawBitmap(indicator, centerX - 25, centerY + centerY / 5 + centerY / 14, mArrowPaint);
    }

    private float getSeconds(Calendar calendar) {
        if (configuration.isSmoothScrolling()) {
            return (calendar.get(Calendar.SECOND) + (calendar.get(Calendar.MILLISECOND) / 1000f));
        }
        return calendar.get(Calendar.SECOND);
    }

    @Override
    void onConfigurationUpdated(Configuration configuration) {
        indicator = BitmapFactory.decodeResource(context.getResources(), configuration.getArrowResourceId());
        indicator = Bitmap.createScaledBitmap(indicator, 50, 25, true);
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
