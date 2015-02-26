package com.android.timrface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class WatchFaceService extends CanvasWatchFaceService {
    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        Time mTime;

        private final Typeface TYPEFACE =
                Typeface.createFromAsset(getAssets(),"font.ttf");

        static final int MSG_UPDATE_TIME = 0;

        private final int HOUR_X = 35;
        private final int HOUR_Y = 170;
        private final int MINUTE_X = 160;
        private final int MINUTE_Y = 170;
        private final int DATE_Y= 60;
        private final int TIME_X = 230;
        private final int TIME_Y = 195;

        Paint mBackgroundPaint;
        Paint mTilePaint;
        Paint mScalePaint;
        Paint mHourPaint;
        Paint mMinutePaint;
        Paint mDatePaint;
        Paint mArrowPaint;
        Paint mTimePaint;
        Paint mBorderPaint;

        Bitmap scale;

        private boolean mRegisteredTimeZoneReceiver = false;
        private final long INTERACTIVE_UPDATE_RATE_MS = 16;
        private Resources resources;
        Date date;
        SimpleDateFormat format;

        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            resources = WatchFaceService.this.getResources();
            scale = BitmapFactory.decodeResource(resources, R.drawable.scale);
            scale = scale.createScaledBitmap(scale, 600, 50, false);

            mBackgroundPaint = new Paint();
            mTilePaint = new Paint();
            mScalePaint = new Paint();
            mArrowPaint = new Paint();
            mBorderPaint = new Paint();

            mHourPaint = createTextPaint(resources.getColor(R.color.text), TYPEFACE);
            mMinutePaint = createTextPaint(resources.getColor(R.color.tile), TYPEFACE);
            mDatePaint = createTextPaint(resources.getColor(R.color.text), TYPEFACE);
            mTimePaint = createTextPaint(resources.getColor(R.color.text), TYPEFACE);
            mDatePaint.setTextAlign(Paint.Align.CENTER);

            mBackgroundPaint.setColor(resources.getColor(R.color.background));
            mArrowPaint.setColor(resources.getColor(R.color.background));
            mTilePaint.setColor(resources.getColor(R.color.tile));
            mBorderPaint.setColor(resources.getColor(R.color.background));

            mBackgroundPaint.setShadowLayer(8.0f, 0.0f, 4.0f, resources.getColor(R.color.shadow));
            mArrowPaint.setShadowLayer(8.0f, 4.0f, 4.0f, resources.getColor(R.color.shadow));

            mTime = new Time();
        }

        private Paint createTextPaint(int color, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            /* get device features (burn-in, low-bit ambient) */
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            /* the wearable switched between modes */
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mTime.setToNow();

            float seconds = getSeconds();
            String minutes = getMinutes();
            String date = getDate();
            String time = getAmPm();

            canvas.drawRect(0, bounds.centerY() + 45, bounds.width(), bounds.height(), mTilePaint);
            canvas.drawRect(0, 0, bounds.width(), bounds.centerY() + 45, mBackgroundPaint);

            canvas.save();
            canvas.rotate(45, bounds.centerX(), bounds.centerY());
            canvas.drawRect(bounds.centerX() +15, bounds.centerY() +15, bounds.centerX() + 45f, bounds.centerY() + 45f, mArrowPaint);
            canvas.restore();

            canvas.drawRect(bounds.centerX() - 30, bounds.centerY() + 15, bounds.centerX() + 30, bounds.centerY() + 45, mBorderPaint);
            canvas.drawBitmap(scale, seconds - 445, bounds.centerY() + 60, mScalePaint);
            canvas.drawBitmap(scale, seconds + 155, bounds.centerY() + 60, mScalePaint);
            canvas.drawBitmap(scale, seconds + 755, bounds.centerY() + 60, mScalePaint);

            canvas.drawText(String.valueOf(mTime.hour), HOUR_X, HOUR_Y, mHourPaint);
            canvas.drawText(minutes, MINUTE_X, MINUTE_Y, mMinutePaint);
            canvas.drawText(date, bounds.centerX() - mDatePaint.getStrokeWidth() / 2, DATE_Y, mDatePaint);
            canvas.drawText(time, TIME_X, TIME_Y, mTimePaint);


        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                registerReceiver();

                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            updateTimer();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            Resources resources = WatchFaceService.this.getResources();
            boolean isRound = insets.isRound();
            float textSize = resources.getDimension(isRound
                    ? R.dimen.text_size_round : R.dimen.text_size);
            float amPmSize = resources.getDimension(isRound
                    ? R.dimen.am_pm_size_round : R.dimen.am_pm_size);
            float dateTextSize = resources.getDimension(isRound
                    ? R.dimen.date_size_round : R.dimen.date_size);
            float timeTextSize = resources.getDimension(isRound
                    ? R.dimen.time_size_round : R.dimen.time_size);

            mHourPaint.setTextSize(textSize);
            mMinutePaint.setTextSize(textSize);
            mDatePaint.setTextSize(dateTextSize);
            mTimePaint.setTextSize(timeTextSize);
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            WatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        private float getSeconds () {
            long now = System.currentTimeMillis();
            mTime.set(now);
            int milliseconds = (int) (now % 1000);
            float seconds = mTime.second + milliseconds / 1000f;
            return seconds * -10;
        }

        private String getDate() {
            date = Calendar.getInstance().getTime();
            format = new SimpleDateFormat("EEEE, F. MMMM");
            return format.format(date);
        }

        private String getAmPm() {
            date = Calendar.getInstance().getTime();
            format = new SimpleDateFormat("a");
            return format.format(date);
        }

        private String getMinutes() {
             boolean hours = (mTime.minute > 9);
             return (hours
                    ? String.valueOf(mTime.minute) : "0" + String.valueOf(mTime.minute));
        }

    }
}