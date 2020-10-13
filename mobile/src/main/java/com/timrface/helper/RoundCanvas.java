package com.timrface.helper;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.config.ConfigurationBuilder;
import com.timrface.watchfacelayout.layout.LayoutProvider;

import java.util.Calendar;
import java.util.TimeZone;


public class RoundCanvas extends View {


    private final int backgroundColor = Color.parseColor("#FAFAFA");

    public RoundCanvas(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }


    @Override
    public void onDraw(Canvas canvas) {
        float centerX = canvas.getWidth() / 2f;
        float centerY = canvas.getHeight() / 2f;

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        canvas.drawRect(0, 0, centerX * 2f, centerY * 2f, backgroundPaint);

        backgroundPaint.setColor(Color.GREEN);
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas.drawCircle(centerX, centerY, centerX, backgroundPaint);

        Paint circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(Color.DKGRAY);
        circlePaint.setStrokeWidth(3);
        circlePaint.setAntiAlias(true);
        circlePaint.setShadowLayer(8.0f, 0.0f, 8.0f, Color.parseColor("#2C000000"));
        canvas.drawCircle(centerX, centerY, centerX - 2, circlePaint);

    }
}
