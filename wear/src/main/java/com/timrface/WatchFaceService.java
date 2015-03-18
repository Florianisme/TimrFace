package com.timrface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class WatchFaceService extends CanvasWatchFaceService {

    public static int mInteractiveBackgroundColor =
            WatchFaceUtil.KEY_BACKGROUND_COLOR;

    public static int mInteractiveMainColor =
            WatchFaceUtil.KEY_MAIN_COLOR;

    public static int mInteractiveTextColor =
            WatchFaceUtil.KEY_TEXT_COLOR;

    public static long INTERACTIVE_UPDATE_RATE_MS = 120;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    public class Engine extends CanvasWatchFaceService.Engine implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

        static final int MSG_UPDATE_TIME = 0;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };                private final Typeface TYPEFACE =
                Typeface.createFromAsset(getAssets(), "font.ttf");final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs =
                                    INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };
        Time mTime;
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
        String hours;
        String minutes;
        float seconds;
        String time;
        String date;
        boolean mRegisteredTimeZoneReceiver = false;
        boolean is24Hour = false;
        boolean ambientMode = false;
        SimpleDateFormat format;
        DateFormat df;
        Calendar cal;
        Context context = getApplicationContext();
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(WatchFaceService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        private float HOUR_X;
        private float HOUR_MINUTE_Y;
        private float MINUTE_X;
        private float DATE_Y;
        private float TIME_X;
        private float TIME_Y;
        private Resources resources;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setViewProtection(WatchFaceStyle.PROTECT_STATUS_BAR)
                    .build());

            resources = WatchFaceService.this.getResources();
            scale = BitmapFactory.decodeResource(resources, R.drawable.scale);
            scale = scale.createScaledBitmap(scale, 600, 50, true);

            mBackgroundPaint = new Paint();
            mTilePaint = new Paint();
            mScalePaint = new Paint();
            mArrowPaint = new Paint();
            mBorderPaint = new Paint();

            mScalePaint.setAntiAlias(false);

            mHourPaint = createTextPaint(mInteractiveTextColor, TYPEFACE);
            mMinutePaint = createTextPaint(mInteractiveBackgroundColor, TYPEFACE);
            mDatePaint = createTextPaint(mInteractiveTextColor, TYPEFACE);
            mTimePaint = createTextPaint(mInteractiveTextColor, TYPEFACE);
            mDatePaint.setTextAlign(Paint.Align.CENTER);

            mBackgroundPaint.setColor(mInteractiveMainColor);
            mArrowPaint.setColor(mInteractiveMainColor);
            mTilePaint.setColor(mInteractiveBackgroundColor);
            mBorderPaint.setColor(mInteractiveMainColor);

            mBackgroundPaint.setShadowLayer(8.0f, 0.0f, 4.0f, resources.getColor(R.color.shadow));
            mArrowPaint.setShadowLayer(8.0f, 4.0f, 4.0f, resources.getColor(R.color.shadow));

            is24Hour = df.is24HourFormat(context);

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
        public void onConnected(Bundle connectionHint) {
            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);
            updateConfigDataItemAndUiOnStartup();
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            ambientMode = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            seconds = getSeconds();
            cal = Calendar.getInstance();

            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

            if (!ambientMode) {
                canvas.drawRect(0, 0, bounds.width(), bounds.centerY() + 45, mBackgroundPaint);
                canvas.drawRect(0, bounds.centerY() + 45, bounds.width(), bounds.height(), mTilePaint);
                if (seconds - 447 > -620) {
                    canvas.drawBitmap(scale, seconds - 447, bounds.centerY() + 60, mScalePaint);
                }
                canvas.drawBitmap(scale, seconds + 153, bounds.centerY() + 60, mScalePaint);
                if (seconds + 753 < 300) {
                    canvas.drawBitmap(scale, seconds + 753, bounds.centerY() + 60, mScalePaint);
                }
                canvas.save();
                canvas.rotate(45, bounds.centerX(), bounds.centerY());
                canvas.drawRect(bounds.centerX() + 15, bounds.centerY() + 15, bounds.centerX() + 45f, bounds.centerY() + 45f, mArrowPaint);
                canvas.restore();
                canvas.drawRect(bounds.centerX() - 30, bounds.centerY() + 15, bounds.centerX() + 30, bounds.centerY() + 45, mBorderPaint);
            }
            else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            }

            canvas.drawText(getHours(), HOUR_X, HOUR_MINUTE_Y, mHourPaint);
            canvas.drawText(getMinutes(), MINUTE_X, HOUR_MINUTE_Y, mMinutePaint);
            canvas.drawText(getDate(), bounds.centerX() - mDatePaint.getStrokeWidth() / 2, DATE_Y, mDatePaint);
            canvas.drawText(getAmPm(), TIME_X, TIME_Y, mTimePaint);
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            ambientMode = inAmbientMode;
            adjustPaintColorToCurrentMode(mMinutePaint, mInteractiveBackgroundColor,
                    WatchFaceUtil.AMBIENT_TEXT);

            adjustPaintColorToCurrentMode(mHourPaint, mInteractiveTextColor,
                    WatchFaceUtil.AMBIENT_TEXT);
            adjustPaintColorToCurrentMode(mDatePaint, mInteractiveTextColor,
                    WatchFaceUtil.AMBIENT_TEXT);
            adjustPaintColorToCurrentMode(mTimePaint, mInteractiveTextColor,
                    WatchFaceUtil.AMBIENT_TEXT);

            adjustPaintColorToCurrentMode(mBackgroundPaint, mInteractiveMainColor,
                    WatchFaceUtil.AMBIENT_BACKGROUND);
            adjustPaintColorToCurrentMode(mArrowPaint, mInteractiveMainColor,
                    WatchFaceUtil.AMBIENT_BACKGROUND);
            adjustPaintColorToCurrentMode(mBorderPaint, mInteractiveMainColor,
                    WatchFaceUtil.AMBIENT_BACKGROUND);

                mHourPaint.setAntiAlias(!ambientMode);
                mMinutePaint.setAntiAlias(!ambientMode);
                mTilePaint.setAntiAlias(!ambientMode);
                mDatePaint.setAntiAlias(!ambientMode);
                mTimePaint.setAntiAlias(!ambientMode);
                mBackgroundPaint.setAntiAlias(!ambientMode);
                mArrowPaint.setAntiAlias(!ambientMode);
                mBorderPaint.setAntiAlias(!ambientMode);

            invalidate();
            updateTimer();
        }

        private void adjustPaintColorToCurrentMode(Paint paint, int interactiveColor,
                                                   int ambientColor) {
            paint.setColor(isInAmbientMode() ? ambientColor : interactiveColor);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            Resources resources = WatchFaceService.this.getResources();
            float textSize = resources.getDimension(R.dimen.text_size);
            float dateTextSize = resources.getDimension(R.dimen.date_size);
            float timeTextSize = resources.getDimension(R.dimen.time_size);

            boolean isRound = insets.isRound();
            if (isRound) {
                HOUR_X = 35;
                HOUR_MINUTE_Y = 170;
                MINUTE_X = 160;
                DATE_Y = 60;
                TIME_X = 240;
                TIME_Y = 195;
            } else {
                HOUR_X = 15;
                HOUR_MINUTE_Y = 160;
                MINUTE_X = 140;
                DATE_Y = 40;
                TIME_X = 215;
                TIME_Y = 177;
            }

            mHourPaint.setTextSize(textSize);
            mMinutePaint.setTextSize(textSize);
            mDatePaint.setTextSize(dateTextSize);
            mTimePaint.setTextSize(timeTextSize);
        }

        private float getSeconds() {
            mTime.set(System.currentTimeMillis());
            return (mTime.second + (System.currentTimeMillis() % 1000) / 1000f) * (-10);
        }

        private String getDate() {
            if (is24Hour) {
                format = new SimpleDateFormat("EEEE, d. MMMM");
            } else {
                format = new SimpleDateFormat("EEEE, d MMMM");
            }
            return format.format(cal.getTime());
        }

        private String getMinutes() {
            return formatTwoDigits(mTime.minute);
        }

        private String getAmPm() {
            if (!is24Hour) {
                format = new SimpleDateFormat("a", Locale.getDefault());
                return format.format(cal.getTime());
            } else {
                return "";
            }
        }

        private String formatTwoDigits(int number) {
            return String.format("%02d", number);
        }

        private String getHours() {
            if (is24Hour) {
                format = new SimpleDateFormat("H");
                return formatTwoDigits(Integer.valueOf(format.format(cal.getTime())));
            } else {
                format = new SimpleDateFormat("K");
                return formatTwoDigits(Integer.valueOf(format.format(cal.getTime())));
            }
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();

                registerReceiver();

                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }
            updateTimer();
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

        @Override
        public void onConnectionSuspended(int cause) {
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
        }

        private void updateConfigDataItemAndUiOnStartup() {
            WatchFaceUtil.fetchConfigDataMap(mGoogleApiClient,
                    new WatchFaceUtil.FetchConfigDataMapCallback() {
                        @Override
                        public void onConfigDataMapFetched(DataMap startupConfig) {
                            WatchFaceUtil.putConfigDataItem(mGoogleApiClient, startupConfig);

                            updateUiForConfigDataMap(startupConfig);
                        }
                    }
            );
        }

        private void addIntKeyIfMissing(DataMap config, String key, int color) {
            if (!config.containsKey(key)) {
                config.putInt(key, color);
            }
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            try {
                for (DataEvent dataEvent : dataEvents) {
                    if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                        continue;
                    }

                    DataItem dataItem = dataEvent.getDataItem();
                    if (!dataItem.getUri().getPath().equals(
                            WatchFaceUtil.PATH_WITH_FEATURE)) {
                        continue;
                    }

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                    DataMap config = dataMapItem.getDataMap();
                    updateUiForConfigDataMap(config);
                }
            } finally {
                dataEvents.close();
            }
        }

        private void updateUiForConfigDataMap(final DataMap config) {
            boolean uiUpdated = false;
            for (String configKey : config.keySet()) {
                if (!config.containsKey(configKey)) {
                    continue;
                }
                if (updateUiForKey(WatchFaceUtil.KEY_BACKGROUND_COLOR, WatchFaceUtil.KEY_MAIN_COLOR, WatchFaceUtil.SMOOTH_SECONDS)) {
                    uiUpdated = true;
                }
            }
            if (uiUpdated) {
                invalidate();
            }
        }

        private boolean updateUiForKey(int color, int color2, boolean key) {
            if (key) {
                INTERACTIVE_UPDATE_RATE_MS = 100;
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
            return true;
        }

        private void setInteractiveBackgroundColor(int color) {
            mInteractiveBackgroundColor = color;
            mTilePaint.setColor(color);
            mMinutePaint.setColor(color);
        }

        private void setInteractiveMainColor(int color) {
            mInteractiveMainColor = color;
            mBackgroundPaint.setColor(color);
            mArrowPaint.setColor(color);
            mBorderPaint.setColor(color);
        }

        private void setInteractiveTextColor(int color) {
            mInteractiveTextColor = color;
            mHourPaint.setColor(color);
            mDatePaint.setColor(color);
            mTimePaint.setColor(color);
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





    }
}