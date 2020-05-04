package com.timrface.watchfacelayout.layout.components;

import android.graphics.Canvas;
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
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        float positionY = centerY + centerY / 2.5f;

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(centerX - 25f, positionY);
        path.lineTo(centerX + 25, positionY);
        path.lineTo(centerX, positionY + 25);
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
