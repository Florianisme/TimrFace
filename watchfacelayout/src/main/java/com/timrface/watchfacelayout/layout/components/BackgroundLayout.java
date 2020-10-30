package com.timrface.watchfacelayout.layout.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import com.timrface.watchfacelayout.config.Configuration;

import java.util.Calendar;

public class BackgroundLayout extends Layout {

    private final Paint mBackgroundPaint;
    private Path backgroundPath = new Path();

    public BackgroundLayout(Configuration configuration) {
        super(configuration);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(configuration.getBackgroundColor());
        mBackgroundPaint.setShadowLayer(8.0f, 0.0f, 8.0f, Color.parseColor("#20000000"));
    }

    @Override
    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        canvas.drawPath(backgroundPath, mBackgroundPaint);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        float centerX = width / 2f;
        float centerY = height / 2f;
        float positionY = centerY + centerY / 2.5f;
        float arrowSize = centerX * 0.15f;

        backgroundPath.setFillType(Path.FillType.EVEN_ODD);
        backgroundPath.moveTo(0f, positionY);
        backgroundPath.lineTo(centerX - arrowSize, positionY);
        backgroundPath.lineTo(centerX, positionY + arrowSize);
        backgroundPath.lineTo(centerX + arrowSize, positionY);
        backgroundPath.lineTo(width, positionY);
        backgroundPath.lineTo(width, 0);
        backgroundPath.lineTo(0, 0);
    }

    @Override
    public void onConfigurationUpdated(Configuration configuration) {
        mBackgroundPaint.setColor(configuration.getBackgroundColor());
    }

    @Override
    public boolean drawWhenInAmbientMode() {
        return false;
    }
}
