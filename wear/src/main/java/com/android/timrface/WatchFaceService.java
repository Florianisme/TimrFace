package com.android.timrface;

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
import java.util.TimeZone;

public class WatchFaceService extends CanvasWatchFaceService {

    public static int mInteractiveBackgroundColor =
            WatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND;
    public static int mInteractiveMinuteDigitsColor =
            WatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_MINUTE_DIGITS;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    public class Engine extends CanvasWatchFaceService.Engine implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

        static final int MSG_UPDATE_TIME = 0;
        final Handler mUpdateTimeHandler = new Handler() {
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
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        private final Typeface TYPEFACE =
                Typeface.createFromAsset(getAssets(), "font.ttf");
        private final int HOUR_X = 35;
        private final int HOUR_Y = 170;
        private final int MINUTE_X = 160;
        private final int MINUTE_Y = 170;
        private final int DATE_Y = 60;
        private final int TIME_X = 230;
        private final int TIME_Y = 195;
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
        SimpleDateFormat format;
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(WatchFaceService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        private long INTERACTIVE_UPDATE_RATE_MS = 100;
        private Resources resources;

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
            scale = scale.createScaledBitmap(scale, 600, 50, true);

            mBackgroundPaint = new Paint();
            mTilePaint = new Paint();
            mScalePaint = new Paint();
            mArrowPaint = new Paint();
            mBorderPaint = new Paint();

            mScalePaint.setAntiAlias(false);

            mHourPaint = createTextPaint(resources.getColor(R.color.text), TYPEFACE);
            mMinutePaint = createTextPaint(mInteractiveBackgroundColor, TYPEFACE);
            mDatePaint = createTextPaint(resources.getColor(R.color.text), TYPEFACE);
            mTimePaint = createTextPaint(resources.getColor(R.color.text), TYPEFACE);
            mDatePaint.setTextAlign(Paint.Align.CENTER);

            mBackgroundPaint.setColor(resources.getColor(R.color.background));
            mArrowPaint.setColor(resources.getColor(R.color.background));
            mTilePaint.setColor(mInteractiveBackgroundColor);
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

        @Override  // GoogleApiClient.ConnectionCallbacks
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
        public void onDraw(Canvas canvas, Rect bounds) {
            seconds = getSeconds();
            minutes = getMinutes();
            hours = getHours();
            date = getDate();
            time = getAmPm();


            canvas.drawRect(0, bounds.centerY() + 45, bounds.width(), bounds.height(), mTilePaint);
            canvas.drawRect(0, 0, bounds.width(), bounds.centerY() + 45, mBackgroundPaint);

            canvas.save();
            canvas.rotate(45, bounds.centerX(), bounds.centerY());
            canvas.drawRect(bounds.centerX() + 15, bounds.centerY() + 15, bounds.centerX() + 45f, bounds.centerY() + 45f, mArrowPaint);
            canvas.restore();

            canvas.drawRect(bounds.centerX() - 30, bounds.centerY() + 15, bounds.centerX() + 30, bounds.centerY() + 45, mBorderPaint);
            canvas.drawBitmap(scale, seconds - 445, bounds.centerY() + 60, mScalePaint);
            canvas.drawBitmap(scale, seconds + 155, bounds.centerY() + 60, mScalePaint);
            canvas.drawBitmap(scale, seconds + 755, bounds.centerY() + 60, mScalePaint);

            canvas.drawText(hours, HOUR_X, HOUR_Y, mHourPaint);
            canvas.drawText(minutes, MINUTE_X, MINUTE_Y, mMinutePaint);
            canvas.drawText(date, bounds.centerX() - mDatePaint.getStrokeWidth() / 2, DATE_Y, mDatePaint);
            canvas.drawText(time, TIME_X, TIME_Y, mTimePaint);
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

        private float getSeconds() {
            mTime.set(System.currentTimeMillis());
            return (mTime.second + (System.currentTimeMillis() % 1000) / 1000f) * (-10);
        }

        private String getDate() {
            format = new SimpleDateFormat("EEEE, F. MMMM");
            return format.format(Calendar.getInstance().getTime());
        }

        private String getAmPm() {
            format = new SimpleDateFormat("a");
            return format.format(Calendar.getInstance().getTime());
        }

        private String getMinutes() {
            return (mTime.minute > 9
                    ? String.valueOf(mTime.minute) : "0" + String.valueOf(mTime.minute));
        }

        private String getHours() {
            return (String.valueOf(mTime.hour).length() > 1
                    ? String.valueOf(mTime.hour) : "0" + String.valueOf(mTime.hour));
        }

        private void setInteractiveBackgroundColor(int color) {
            mInteractiveBackgroundColor = color;
            mTilePaint.setColor(color);
        }


        private void setInteractiveMinuteDigitsColor(int color) {
            mInteractiveMinuteDigitsColor = color;
            mMinutePaint.setColor(color);
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

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
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

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible();
        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnectionSuspended(int cause) {
        }

        @Override  // GoogleApiClient.OnConnectionFailedListener
        public void onConnectionFailed(ConnectionResult result) {
        }

        private void updateConfigDataItemAndUiOnStartup() {
            WatchFaceUtil.fetchConfigDataMap(mGoogleApiClient,
                    new WatchFaceUtil.FetchConfigDataMapCallback() {
                        @Override
                        public void onConfigDataMapFetched(DataMap startupConfig) {
                            // If the DataItem hasn't been created yet or some keys are missing,
                            // use the default values.
                            setDefaultValuesForMissingConfigKeys(startupConfig);
                            WatchFaceUtil.putConfigDataItem(mGoogleApiClient, startupConfig);

                            updateUiForConfigDataMap(startupConfig);
                        }
                    }
            );
        }

        private void setDefaultValuesForMissingConfigKeys(DataMap config) {
            addIntKeyIfMissing(config, WatchFaceUtil.KEY_BACKGROUND_COLOR,
                    WatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND);
            addIntKeyIfMissing(config, WatchFaceUtil.KEY_MINUTES_COLOR,
                    WatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_MINUTE_DIGITS);
        }

        private void addIntKeyIfMissing(DataMap config, String key, int color) {
            if (!config.containsKey(key)) {
                config.putInt(key, color);
            }
        }

        @Override // DataApi.DataListener
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
                    System.out.println("Data Changed");
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
                //int color = config.getInt(configKey);
                String color = WatchFaceUtil.KEY_BACKGROUND_COLOR;
                System.out.println("Color: "+color);
                if (updateUiForKey(configKey, color)) {
                    uiUpdated = true;
                }
            }
            if (uiUpdated) {
                invalidate();
            }
        }

        /**
         * Updates the color of a UI item according to the given {@code configKey}. Does nothing if
         * {@code configKey} isn't recognized.
         *
         * @return whether UI has been updated
         */
        private boolean updateUiForKey(String configKey, String color) {
            System.out.println("Update");
            System.out.println(configKey);
            if (configKey.equals(WatchFaceUtil.KEY_BACKGROUND_COLOR) && !WatchFaceUtil.KEY_BACKGROUND_COLOR.equals("BACKGROUND_COLOR")) {
                setInteractiveBackgroundColor(Color.parseColor(color));
                setInteractiveMinuteDigitsColor(Color.parseColor(color));
            } else {
                return false;
            }
            return true;
        }

    }


}

