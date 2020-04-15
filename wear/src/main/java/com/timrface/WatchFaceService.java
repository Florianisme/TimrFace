package com.timrface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.*;
import com.timrface.layout.LayoutProvider;

import java.util.Calendar;
import java.util.TimeZone;

public class WatchFaceService extends CanvasWatchFaceService {

    private LayoutProvider layoutProvider;

    static boolean shouldReceive = true;
    boolean ambientMode = false;
    private Configuration configuration;

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
                            long delayMs = 16 - (timeMs % 16);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };
        private final String TAG = "WatchFaceService";
        boolean mRegisteredTimeZoneReceiver = false;
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        /* final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 mCalendar.clear();
                 mCalendar.setTimeZone(TimeZone.getDefault());
                 initFormats();
                 invalidate();
             }
         };*/
        private GoogleApiClient googleApiClient;
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


            configuration = buildDefaultConfiguration();
            layoutProvider = new LayoutProvider().init(configuration, getApplicationContext());
        }

        private Configuration buildDefaultConfiguration() {
            return new Configuration()
                    .setShowBatteryLevel(true)
                    .setSmoothScrolling(true)
                    .setBackgroundColor(Color.parseColor("#FAFAFA"))
                    .setArrowResourceId(R.drawable.indicator)
                    .setInteractiveColor(Color.parseColor("#FF9800"))
                    .setTextColor(Color.parseColor("#424242"))
                    .setShowZeroDigit(true)
                    .setAstronomicalClockFormat(true);
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
            cal.setTimeInMillis(System.currentTimeMillis());
            float width = bounds.exactCenterX();
            float height = bounds.exactCenterY();

            layoutProvider.update(canvas, width, height, cal);
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            layoutProvider.onAmbientModeChanged(inAmbientMode);

            invalidate();
            updateTimer();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            layoutProvider.applyWindowInsets(getResources());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                //registerTimeReceiver();
                googleApiClient.connect();
            } else {
                releaseGoogleApiClient();
                //unregisterReceiver();
            }
            updateTimer();
        }

        private void updateConfiguration(int interactiveColor, int backgroundColor, int arrowResourceId,
                                         boolean smoothScrolling, boolean showBattery, boolean showZeroDigit) {
            boolean isBackgroundColorWhite = backgroundColor != Color.parseColor("#FAFAFA");
            configuration = new Configuration()
                    .setShowBatteryLevel(showBattery)
                    .setSmoothScrolling(smoothScrolling)
                    .setBackgroundColor(backgroundColor)
                    .setArrowResourceId(arrowResourceId)
                    .setInteractiveColor(interactiveColor)
                    .setTextColor(isBackgroundColorWhite ? Color.parseColor("#424242") : Color.parseColor("#FAFAFA"))
                    .setShowZeroDigit(showZeroDigit);

            layoutProvider.onConfigurationChange(configuration);
            invalidate();
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }


        private void processConfigurationFor(DataItem item) {
            boolean smoothSeconds = true;
            boolean showBatteryLevel = true;
            boolean showZeroDigit = true;

            int interactiveColor = Color.parseColor("#FF9800");
            int backgroundColor = Color.parseColor("#FAFAFA");
            int arrowResourceId = R.drawable.indicator;

            if ("/watch_face_config".equals(item.getUri().getPath())) {
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                if (dataMap.containsKey("SMOOTH_SECONDS"))
                    smoothSeconds = dataMap.getBoolean("SMOOTH_SECONDS");
                if (dataMap.containsKey("BACKGROUND_COLOR"))
                    backgroundColor = Color.parseColor(dataMap.getString("BACKGROUND_COLOR"));
                if (dataMap.containsKey("BACKGROUND_COLOR"))
                    arrowResourceId = getArrowDrawableResourceIdByBackgroundColor(dataMap.getString("BACKGROUND_COLOR"));
                if (dataMap.containsKey("COLOR"))
                    interactiveColor = Color.parseColor(dataMap.getString("COLOR"));
                if (dataMap.containsKey("COLOR_MANUAL"))
                    interactiveColor = dataMap.getInt("COLOR_MANUAL");
                if (dataMap.containsKey("BATTERY_INDICATOR"))
                    showBatteryLevel = dataMap.getBoolean("BATTERY_INDICATOR", true);
                if (dataMap.containsKey("ZERO_DIGIT"))
                    showZeroDigit = dataMap.getBoolean("ZERO_DIGIT", true);
                updateConfiguration(interactiveColor, backgroundColor, arrowResourceId, smoothSeconds, showBatteryLevel, showZeroDigit);
            }
        }

        private int getArrowDrawableResourceIdByBackgroundColor(String color) {
            switch (color) {
                case "#424242":
                    return R.drawable.indicator_grey;
                case "#000000":
                    return R.drawable.indicator_black;
                default:
                    return R.drawable.indicator;
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