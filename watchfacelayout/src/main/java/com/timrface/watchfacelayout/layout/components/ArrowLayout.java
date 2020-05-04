package com.timrface.watchfacelayout.layout.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.WindowInsets;

import java.util.Calendar;

public class ArrowLayout extends Layout {

    private final Paint mArrowPaint;

    public ArrowLayout(Configuration configuration) {
        super(configuration);
        mArrowPaint = new Paint();
        mArrowPaint.setColor(configuration.getArrowColor());
        mArrowPaint.setShadowLayer(10f, 0f, 5f, Color.BLACK);
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        float positionY = centerY + centerY / 2.5f;
        float arrowSize = centerX * 0.15f;

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(centerX - arrowSize, positionY);
        path.lineTo(centerX + arrowSize, positionY);
        path.lineTo(centerX, positionY + arrowSize);
        canvas.drawPath(path, mArrowPaint);
    }

    @Override
    void onConfigurationUpdated(Configuration configuration) {
        mArrowPaint.setColor(configuration.getArrowColor());
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
