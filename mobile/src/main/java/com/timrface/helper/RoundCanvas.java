package com.timrface.helper;

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
    private final Paint shadowPaint;

    public RoundCanvas(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        shadowPaint = new Paint();
        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setStrokeWidth(2f);
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

        canvas.drawCircle(centerX, centerY, centerX, shadowPaint);

    }
}
