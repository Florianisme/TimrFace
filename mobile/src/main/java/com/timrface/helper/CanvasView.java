package com.timrface.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.*;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import com.timrface.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class CanvasView extends View {

    public static int KEY_BACKGROUND_COLOR;
    public static int KEY_MAIN_COLOR;
    public static int KEY_TEXT_COLOR;
    public static boolean SMOOTH_SECONDS;
    public static boolean BATTERY_LEVEL;
    public static boolean ZERO_DIGIT;
    static Paint mBackgroundPaint;
    static Paint mTilePaint;
    static Paint mScalePaint;
    static Paint mHourPaint;
    static Paint mMinutePaint;
    static Paint mDatePaint;
    static Paint mArrowPaint;
    static Paint mTimePaint;
    static Paint mBatteryPaint;
    static Paint mShadowPaint;
    public long INTERACTIVE_UPDATE_RATE_MS = 30;
    String batteryLevel = "";
    Bitmap scale;
    Bitmap indicator;
    Bitmap shadow;
    SimpleDateFormat format;
    float seconds;
    boolean is24Hour = false;
    DateFormat df;
    Calendar cal;
    boolean battery;
    float width;
    float height;
    Time mTime;
    private Typeface ROBOTO_LIGHT;
    private BroadcastReceiver updateBattery = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            batteryLevel = String.valueOf(level) + "%";
        }
    };

    public CanvasView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        KEY_BACKGROUND_COLOR = Color.parseColor("#FF9800");
        KEY_MAIN_COLOR = Color.parseColor("#FAFAFA");
        KEY_TEXT_COLOR = Color.parseColor("#424242");
        SMOOTH_SECONDS = true;
        BATTERY_LEVEL = true;

        ROBOTO_LIGHT = Typeface.createFromAsset(getContext().getAssets(), "Roboto-Light.ttf");

        Resources resources = getResources();
        scale = BitmapFactory.decodeResource(resources, R.drawable.scale);
        scale = Bitmap.createScaledBitmap(scale, 1800, 50, true);

        shadow = BitmapFactory.decodeResource(resources, R.drawable.indicator_shadow);
        shadow = Bitmap.createScaledBitmap(shadow, 50, 25, true);

        indicator = BitmapFactory.decodeResource(resources, R.drawable.indicator);
        indicator = Bitmap.createScaledBitmap(indicator, 50, 25, true);

        createPaints();
        getContext().registerReceiver(this.updateBattery,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        float infoTextSize = getResources().getDimension(R.dimen.info_size);
        float timeTextSize = getResources().getDimension(R.dimen.text_size);

        mHourPaint.setTextSize(timeTextSize);
        mMinutePaint.setTextSize(timeTextSize);
        mDatePaint.setTextSize(infoTextSize);
        mTimePaint.setTextSize(infoTextSize);
        mBatteryPaint.setTextSize(infoTextSize);

        mTime = new Time();

    }

    public static void setInteractiveBackgroundColor(int color) {
        KEY_BACKGROUND_COLOR = color;
        mTilePaint.setColor(color);
        mMinutePaint.setColor(color);
    }

    public static void setInteractiveMainColor(int color) {
        KEY_MAIN_COLOR = color;
        mBackgroundPaint.setColor(color);
    }

    public static void setInteractiveTextColor(int color) {
        KEY_TEXT_COLOR = color;
        mHourPaint.setColor(color);
        mDatePaint.setColor(color);
        mTimePaint.setColor(color);
        mBatteryPaint.setColor(color);
    }

    public void updateUi(int color, int color2, boolean seconds, boolean battery) {
        this.battery = battery;
        if (seconds) {
            INTERACTIVE_UPDATE_RATE_MS = 30;
        } else {
            INTERACTIVE_UPDATE_RATE_MS = 1000;
        }
        setInteractiveBackgroundColor(color);
        setInteractiveMainColor(color2);
        if (color2 != Color.parseColor("#FAFAFA")) {
            setInteractiveTextColor(Color.parseColor("#FAFAFA"));
        } else {
            setInteractiveTextColor(Color.parseColor("#424242"));
        }
        invalidate();
    }

    public void updateConfiguration(String item, Object value) {
        if (item.equals("SMOOTH_SECONDS"))
            SMOOTH_SECONDS = (Boolean) value;
        if (item.equals("BACKGROUND_COLOR"))
            checkColors((String) value);
        if (item.equals("COLOR"))
            checkColors((String) value);
        if (item.equals("COLOR_MANUAL"))
            KEY_BACKGROUND_COLOR = (Integer) value;
        if (item.equals("BATTERY_INDICATOR"))
            BATTERY_LEVEL = (Boolean) value;
        if (item.equals("ZERO_DIGIT"))
            ZERO_DIGIT = (Boolean) value;
        updateUi(KEY_BACKGROUND_COLOR, KEY_MAIN_COLOR, SMOOTH_SECONDS, BATTERY_LEVEL);
    }

    public void checkColors(String color) {
        if (color.equals("#FAFAFA") || color.equals("#424242") || color.equals("#000000")) {
            KEY_MAIN_COLOR = Color.parseColor(color);
            switch (color) {
                case "#FAFAFA":
                    KEY_TEXT_COLOR = Color.parseColor("#424242");
                    indicator = BitmapFactory.decodeResource(getResources(), R.drawable.indicator);
                    break;
                case "#424242":
                    indicator = BitmapFactory.decodeResource(getResources(), R.drawable.indicator_grey);
                    KEY_TEXT_COLOR = Color.parseColor("#FAFAFA");
                    break;
                case "#000000":
                    indicator = BitmapFactory.decodeResource(getResources(), R.drawable.indicator_black);
                    KEY_TEXT_COLOR = Color.parseColor("#FAFAFA");
                    break;
            }
            indicator = Bitmap.createScaledBitmap(indicator, 50, 25, true);
        } else {
            KEY_BACKGROUND_COLOR = Color.parseColor(color);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        is24Hour = df.is24HourFormat(getContext());
        seconds = getSeconds();
        cal = Calendar.getInstance();
        width = canvas.getWidth() / 2;
        height = canvas.getHeight() / 2;

        canvas.drawRect(0, height + height / 5 + height / 11, width * 2, height * 2, mTilePaint);
        canvas.drawBitmap(shadow, width - 25, (height + height / 5 + height / 14) + 4, mShadowPaint);
        canvas.drawRect(0, 0, width * 2, height + height / 5 + height / 11, mBackgroundPaint);

        canvas.drawBitmap(scale, seconds + width - 600, height + height / 4 + height / 10, mScalePaint);
        canvas.drawBitmap(indicator, width - 25, height + height / 5 + height / 14, mArrowPaint);


        canvas.drawText(getHours(), width - (mHourPaint.measureText(getHours()) + 10), height + height / 15, mHourPaint);
        canvas.drawText(getMinutes(), width + 10, height + height / 15, mMinutePaint);
        canvas.drawText(getDate(), width - mDatePaint.getStrokeWidth() / 2, height / 3 + height / 25, mDatePaint);
        canvas.drawText(getAmPm(), width * 2 - width / 2, height + height / 4, mTimePaint);
        if (battery) {
            canvas.drawText(batteryLevel, width / 2 - width / 3, height + height / 4, mBatteryPaint);
        }
    }

    private Paint createTextPaint(int color, Typeface typeface) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setTypeface(typeface);
        paint.setAntiAlias(true);
        return paint;
    }

    private float getSeconds() {
        if (SMOOTH_SECONDS) {
            mTime.setToNow();
        } else {
            mTime.set(System.currentTimeMillis());
        }
        return (mTime.second + (System.currentTimeMillis() % 1000) / 1000f) * (-10);
    }

    private String getDate() {
        format = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEE, dMMMM"));
        return format.format(cal.getTime());
    }

    private String getMinutes() {
        return formatTwoDigits(mTime.minute, true);
    }

    private String getAmPm() {
        if (!is24Hour) {
            format = new SimpleDateFormat("a", Locale.getDefault());
            return format.format(cal.getTime());
        } else {
            return "";
        }
    }

    private String formatTwoDigits(int number, boolean digits) {
        if (ZERO_DIGIT || digits)
            return String.format(Locale.getDefault(), "%02d", number);
        return String.valueOf(number);
    }

    private String getHours() {
        if (is24Hour) {
            format = new SimpleDateFormat("H");
            return formatTwoDigits(Integer.valueOf(format.format(cal.getTime())), false);
        } else {
            format = new SimpleDateFormat("h");
            return formatTwoDigits(Integer.valueOf(format.format(cal.getTime())), false);
        }
    }

    private void createPaints() {
        mBackgroundPaint = new Paint();
        mTilePaint = new Paint();
        mScalePaint = new Paint();
        mArrowPaint = new Paint();
        mShadowPaint = new Paint();

        mScalePaint.setAntiAlias(false);

        mHourPaint = createTextPaint(KEY_TEXT_COLOR, ROBOTO_LIGHT);
        mMinutePaint = createTextPaint(KEY_BACKGROUND_COLOR, ROBOTO_LIGHT);
        mDatePaint = createTextPaint(KEY_TEXT_COLOR, ROBOTO_LIGHT);
        mTimePaint = createTextPaint(KEY_TEXT_COLOR, ROBOTO_LIGHT);
        mBatteryPaint = createTextPaint(KEY_TEXT_COLOR, ROBOTO_LIGHT);
        mDatePaint.setTextAlign(Paint.Align.CENTER);

        mBackgroundPaint.setColor(KEY_MAIN_COLOR);
        mArrowPaint.setColor(KEY_MAIN_COLOR);
        mTilePaint.setColor(KEY_BACKGROUND_COLOR);

        mBackgroundPaint.setShadowLayer(8.0f, 0.0f, 8.0f, getResources().getColor(R.color.shadow));

    }
}
