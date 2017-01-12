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
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class WatchFaceService extends CanvasWatchFaceService {

    public static int AMBIENT_BACKGROUND = Color.parseColor("#000000");
    public static int AMBIENT_TEXT = Color.parseColor("#FFFFFF");
    public static int KEY_BACKGROUND_COLOR = Color.parseColor("#FF9800");
    public static int KEY_MAIN_COLOR = Color.parseColor("#FAFAFA");
    public static int KEY_TEXT_COLOR = Color.parseColor("#424242");
    public static boolean SMOOTH_SECONDS = true;
    public static boolean BATTERY_LEVEL = true;
    public static boolean ZERO_DIGIT = true;
    public static long INTERACTIVE_UPDATE_RATE_MS = 16;
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
    static boolean shouldReceive = true;
    boolean ambientMode = false;
    String batteryLevel = "";

    public static void updateUi(String color) {
        shouldReceive = false;
        setInteractiveBackgroundColor(Color.parseColor(color));
        if (KEY_MAIN_COLOR != Color.parseColor("#FAFAFA")) {
            setInteractiveTextColor(Color.parseColor("#FAFAFA"));
        } else {
            setInteractiveTextColor(Color.parseColor("#424242"));
        }
    }

    public static void setInteractiveBackgroundColor(int color) {
        KEY_BACKGROUND_COLOR = color;
        mTilePaint.setColor(color);
        mMinutePaint.setColor(color);
    }

    public static void setInteractiveMainColor(int color) {
        KEY_MAIN_COLOR = color;
        mBackgroundPaint.setColor(color);
        mArrowPaint.setColor(color);
    }

    public static void setInteractiveTextColor(int color) {
        KEY_TEXT_COLOR = color;
        mHourPaint.setColor(color);
        mDatePaint.setColor(color);
        mTimePaint.setColor(color);
        mBatteryPaint.setColor(color);
    }

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }


    public class Engine extends CanvasWatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        
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
        private final String TAG = "WatchFaceService";
        private final Typeface ROBOTO_LIGHT =
                Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        private final Typeface ROBOTO_THIN =
                Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
        Calendar mCalendar;
        Bitmap scale;
        Bitmap indicator;
        Bitmap shadow;
        float seconds;
        boolean mRegisteredTimeZoneReceiver = false;
        boolean is24Hour = false;
        SimpleDateFormat hourFormat;
        SimpleDateFormat amPmFormat;
        SimpleDateFormat dateFormat;
        Calendar cal;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.clear();
                mCalendar.setTimeZone(TimeZone.getDefault());
                initFormats();
                invalidate();
            }
        };
        Context context = getApplicationContext();
        boolean battery;
        float width;
        float height;
        private GoogleApiClient googleApiClient;
        private BroadcastReceiver updateBattery = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                int level = intent.getIntExtra("level", 0);
                batteryLevel = String.valueOf(level) + "%";
            }
        };
        private Resources resources;
        private final DataApi.DataListener onDataChangedListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEvents) {
                for (DataEvent event : dataEvents) {
                    if (event.getType() == DataEvent.TYPE_CHANGED) {
                        DataItem item = event.getDataItem();
                        processConfigurationFor(item);
                    }
                }

                dataEvents.release();
            }
        };
        private final ResultCallback<DataItemBuffer> onConnectedResultCallback = new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                if (shouldReceive) {
                    for (DataItem item : dataItems) {
                        processConfigurationFor(item);
                    }
                }
                dataItems.release();
                shouldReceive = true;
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_VISIBLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setHotwordIndicatorGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)
                    .setStatusBarGravity(Gravity.END | Gravity.TOP)
                    .setViewProtectionMode(WatchFaceStyle.PROTECT_STATUS_BAR)
                    .build());

            googleApiClient = new GoogleApiClient.Builder(WatchFaceService.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            resources = WatchFaceService.this.getResources();
            scale = BitmapFactory.decodeResource(resources, R.drawable.scale);
            scale = Bitmap.createScaledBitmap(scale, 2000, 55, true);

            shadow = BitmapFactory.decodeResource(resources, R.drawable.indicator_shadow);
            shadow = Bitmap.createScaledBitmap(shadow, 50, 25, true);

            indicator = BitmapFactory.decodeResource(resources, R.drawable.indicator);
            indicator = Bitmap.createScaledBitmap(indicator, 50, 25, true);

            createPaints();

            initFormats();

            registerReceiver(this.updateBattery,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            mCalendar = new GregorianCalendar(TimeZone.getDefault());
        }

        private Paint createTextPaint(int color, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            return paint;
        }

        private void initFormats() {
            dateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEE, dMMMM"));
            amPmFormat = new SimpleDateFormat("a", Locale.getDefault());
            hourFormat = new SimpleDateFormat();
            cal = Calendar.getInstance();
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
            is24Hour = DateFormat.is24HourFormat(context);
            cal.setTimeInMillis(System.currentTimeMillis());
            seconds = getSeconds();
            width = bounds.exactCenterX();
            height = bounds.exactCenterY();

            canvas.drawRect(0, height + height / 5 + height / 11, width * 2, height * 2, mTilePaint);
            if (!ambientMode)
                canvas.drawBitmap(shadow, width - 25, (height + height / 5 + height / 14) + 4, mShadowPaint);
            canvas.drawRect(0, 0, width * 2, height + height / 5 + height / 11, mBackgroundPaint);
            Log.d("Scale", "Pos: " + (seconds + width - 676.6f));
            if (!ambientMode) {
                canvas.drawBitmap(scale, seconds + width - 676.6f, height + height / 4 + height / 8, mScalePaint);
                canvas.drawBitmap(indicator, width - 25, height + height / 5 + height / 14, mArrowPaint);
            }

            canvas.drawText(getHours(), width - (mHourPaint.measureText(getHours()) + (mHourPaint.measureText(getHours()) / 20)), height + height / 15, mHourPaint);
            canvas.drawText(getMinutes(), width + (mMinutePaint.measureText(getMinutes()) / 20), height + height / 15, mMinutePaint);
            canvas.drawText(getDate(), width - mDatePaint.getStrokeWidth() / 2, height / 3 + height / 25, mDatePaint);
            canvas.drawText(getAmPm(), width * 2 - width / 2, height + height / 4, mTimePaint);
            if (battery) {
                canvas.drawText(batteryLevel, width / 2 - width / 3, height + height / 4, mBatteryPaint);
            }
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            ambientMode = inAmbientMode;
            adjustPaintColorToCurrentMode(mMinutePaint, KEY_BACKGROUND_COLOR,
                    AMBIENT_TEXT, ambientMode);

            adjustPaintColorToCurrentMode(mHourPaint, KEY_TEXT_COLOR,
                    AMBIENT_TEXT, ambientMode);
            adjustPaintColorToCurrentMode(mDatePaint, KEY_TEXT_COLOR,
                    AMBIENT_TEXT, ambientMode);
            adjustPaintColorToCurrentMode(mTimePaint, KEY_TEXT_COLOR,
                    AMBIENT_TEXT, ambientMode);

            adjustPaintColorToCurrentMode(mBackgroundPaint, KEY_MAIN_COLOR,
                    AMBIENT_BACKGROUND, ambientMode);
            adjustPaintColorToCurrentMode(mBatteryPaint, KEY_TEXT_COLOR,
                    AMBIENT_BACKGROUND, ambientMode);
            adjustPaintColorToCurrentMode(mTilePaint, KEY_BACKGROUND_COLOR,
                    AMBIENT_BACKGROUND, ambientMode);

            mHourPaint.setTypeface(ambientMode ? ROBOTO_THIN : ROBOTO_LIGHT);
            mMinutePaint.setTypeface(ambientMode ? ROBOTO_THIN : ROBOTO_LIGHT);

            invalidate();
            updateTimer();
        }

        private void adjustPaintColorToCurrentMode(Paint paint, int interactiveColor,
                                                   int ambientColor, boolean isInAmbientMode) {
            paint.setColor(isInAmbientMode ? ambientColor : interactiveColor);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            float infoTextSize = resources.getDimension(R.dimen.info_size);
            float timeTextSize = resources.getDimension(R.dimen.text_size);

            mHourPaint.setTextSize(timeTextSize);
            mMinutePaint.setTextSize(timeTextSize);
            mDatePaint.setTextSize(infoTextSize);
            mTimePaint.setTextSize(infoTextSize);
            mBatteryPaint.setTextSize(infoTextSize);
        }

        private float getSeconds() {
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            if (!SMOOTH_SECONDS)
                return mCalendar.get(Calendar.SECOND) * (-11.1f);
            return (mCalendar.get(Calendar.SECOND) + (mCalendar.get(Calendar.MILLISECOND) / 1000f)) * (-11.1f);
        }

        private String getDate() {
            return dateFormat.format(cal.getTime());
        }

        private String getMinutes() {
            return formatTwoDigits(mCalendar.get(Calendar.MINUTE), true);
        }

        private String getAmPm() {
            if (!is24Hour) {
                return amPmFormat.format(cal.getTime());
            } else {
                return "";
            }
        }

        private String formatTwoDigits(int number, boolean shouldFormat) {
            if (ZERO_DIGIT || shouldFormat)
                return String.format(Locale.getDefault(), "%02d", number);
            return String.valueOf(number);
        }

        private String getHours() {
            if (is24Hour) {
                hourFormat.applyLocalizedPattern("H");
                return formatTwoDigits(Integer.valueOf(hourFormat.format(cal.getTime())), false);
            } else {
                hourFormat.applyLocalizedPattern("h");
                return formatTwoDigits(Integer.valueOf(hourFormat.format(cal.getTime())), false);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerTimeReceiver();
                googleApiClient.connect();
                mCalendar.clear();
                mCalendar.setTimeZone(TimeZone.getDefault());
            } else {
                releaseGoogleApiClient();
                unregisterReceiver();
            }
            updateTimer();
        }

        private void registerTimeReceiver() {
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

        private void updateUi(int color, int color2, boolean key, boolean battery) {
            this.battery = battery;
            if (key) {
                INTERACTIVE_UPDATE_RATE_MS = 50;
            } else {
                INTERACTIVE_UPDATE_RATE_MS = 1000;
            }
            if (!ambientMode) {
                setInteractiveBackgroundColor(color);
                setInteractiveMainColor(color2);
                if (color2 != Color.parseColor("#FAFAFA")) {
                    setInteractiveTextColor(Color.parseColor("#FAFAFA"));
                } else {
                    setInteractiveTextColor(Color.parseColor("#424242"));
                }
            }
            invalidate();
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private void createPaints() {
            mBackgroundPaint = new Paint();
            mTilePaint = new Paint();
            mScalePaint = new Paint();
            mArrowPaint = new Paint();
            mShadowPaint = new Paint();

            mScalePaint.setAntiAlias(true);

            mHourPaint = createTextPaint(KEY_TEXT_COLOR, ROBOTO_LIGHT);
            mMinutePaint = createTextPaint(KEY_BACKGROUND_COLOR, ROBOTO_LIGHT);
            mDatePaint = createTextPaint(KEY_TEXT_COLOR, ROBOTO_LIGHT);
            mTimePaint = createTextPaint(KEY_TEXT_COLOR, ROBOTO_LIGHT);
            mBatteryPaint = createTextPaint(KEY_TEXT_COLOR, ROBOTO_LIGHT);
            mDatePaint.setTextAlign(Paint.Align.CENTER);

            mBackgroundPaint.setColor(KEY_MAIN_COLOR);
            mArrowPaint.setColor(KEY_MAIN_COLOR);
            mTilePaint.setColor(KEY_BACKGROUND_COLOR);

            mBackgroundPaint.setShadowLayer(8.0f, 0.0f, 8.0f, resources.getColor(R.color.shadow));

        }

        private void processConfigurationFor(DataItem item) {
            if ("/watch_face_config".equals(item.getUri().getPath())) {
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                if (dataMap.containsKey("SMOOTH_SECONDS"))
                    SMOOTH_SECONDS = dataMap.getBoolean("SMOOTH_SECONDS");
                if (dataMap.containsKey("BACKGROUND_COLOR"))
                    checkColors(dataMap.getString("BACKGROUND_COLOR"));
                if (dataMap.containsKey("COLOR"))
                    checkColors(dataMap.getString("COLOR"));
                if (dataMap.containsKey("COLOR_MANUAL"))
                    KEY_BACKGROUND_COLOR = dataMap.getInt("COLOR_MANUAL");
                if (dataMap.containsKey("BATTERY_INDICATOR"))
                    BATTERY_LEVEL = dataMap.getBoolean("BATTERY_INDICATOR", true);
                if (dataMap.containsKey("ZERO_DIGIT"))
                    ZERO_DIGIT = dataMap.getBoolean("ZERO_DIGIT", true);
                updateUi(KEY_BACKGROUND_COLOR, KEY_MAIN_COLOR, SMOOTH_SECONDS, BATTERY_LEVEL);
            }
        }

        void checkColors(String color) {
            if (color.equals("#FAFAFA") || color.equals("#424242") || color.equals("#000000")) {
                KEY_MAIN_COLOR = Color.parseColor(color);
                switch (color) {
                    case "#FAFAFA":
                        KEY_TEXT_COLOR = Color.parseColor("#424242");
                        indicator = BitmapFactory.decodeResource(resources, R.drawable.indicator);
                        break;
                    case "#424242":
                        indicator = BitmapFactory.decodeResource(resources, R.drawable.indicator_grey);
                        KEY_TEXT_COLOR = Color.parseColor("#FAFAFA");
                        break;
                    case "#000000":
                        indicator = BitmapFactory.decodeResource(resources, R.drawable.indicator_black);
                        KEY_TEXT_COLOR = Color.parseColor("#FAFAFA");
                        break;
                }
                indicator = Bitmap.createScaledBitmap(indicator, 50, 25, true);
            } else {
                KEY_BACKGROUND_COLOR = Color.parseColor(color);
            }
        }

        @Override
        public void onConnected(Bundle bundle) {
            Log.d(TAG, "connected GoogleAPI");
            Wearable.DataApi.addListener(googleApiClient, onDataChangedListener);
            Wearable.DataApi.getDataItems(googleApiClient).setResultCallback(onConnectedResultCallback);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e(TAG, "suspended GoogleAPI");
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.e(TAG, "connectionFailed GoogleAPI: " + connectionResult.getErrorMessage());
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            releaseGoogleApiClient();
            super.onDestroy();
        }

        private void releaseGoogleApiClient() {
            if (googleApiClient != null && googleApiClient.isConnected()) {
                Wearable.DataApi.removeListener(googleApiClient, onDataChangedListener);
                googleApiClient.disconnect();
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

    }
}