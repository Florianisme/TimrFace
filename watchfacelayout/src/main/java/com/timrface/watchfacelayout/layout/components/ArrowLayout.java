package com.timrface.watchfacelayout.layout.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.timrface.watchfacelayout.Configuration;
import com.timrface.watchfacelayout.layout.WindowInsets;

import java.util.Calendar;

public class ArrowLayout extends Layout {

    private final Paint mArrowPaint;
    private final Context context;
    private Bitmap indicator;

    public ArrowLayout(Configuration configuration, Bitmap indicator, Context context) {
        super(configuration);
        this.indicator = indicator;
        this.context = context;
        mArrowPaint = new Paint();
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        canvas.drawBitmap(indicator, centerX - 25, centerY + centerY / 5 + centerY / 14, mArrowPaint);

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
    public void applyWindowInsets(WindowInsets windowInsets) {

    }

    @Override
    void onAmbientModeChanged(boolean inAmbientMode) {

    }
}
