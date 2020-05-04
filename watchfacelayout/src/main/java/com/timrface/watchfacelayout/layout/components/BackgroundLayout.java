package com.timrface.watchfacelayout.layout.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import com.timrface.watchfacelayout.config.Configuration;

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
        float positionY = centerY + centerY / 2.5f;
        float arrowSize = centerX * 0.15f;

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(0f, positionY);
        path.lineTo(centerX - arrowSize, positionY);
        path.lineTo(centerX, positionY + arrowSize);
        path.lineTo(centerX + arrowSize, positionY);
        path.lineTo(centerX * 2, positionY);
        path.lineTo(centerX * 2, 0);
        path.lineTo(0, 0);
        canvas.drawPath(path, mBackgroundPaint);
    }

    @Override
    void onConfigurationUpdated(Configuration configuration) {
        mBackgroundPaint.setColor(configuration.getBackgroundColor());
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return false;
    }
}
