package com.timrface;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.SystemProviders;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Wearable;
import com.timrface.watchfacelayout.config.ComplicationType;
import com.timrface.watchfacelayout.config.ConfigUpdater;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.config.ConfigurationBuilder;
import com.timrface.watchfacelayout.config.StoredConfigurationFetcher;
import com.timrface.watchfacelayout.layout.LayoutProvider;
import com.timrface.watchfacelayout.util.DayNightBroadcastReceiver;
import com.timrface.watchfacelayout.util.FilteredBroadcastReceiver;
import com.timrface.watchfacelayout.util.TimeZoneBroadcastReceiver;

import java.util.Calendar;
import java.util.TimeZone;

import static com.timrface.UpdateIntervalHandler.MSG_UPDATE_TIME;

public class WatchFaceService extends CanvasWatchFaceService {

    private LayoutProvider layoutProvider;
    private Configuration configuration;

    boolean ambientMode = false;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    public class Engine extends CanvasWatchFaceService.Engine implements DataClient.OnDataChangedListener {

        private final int UNREAD_NOTIFICATION_COMPLICATION_ID = 0;

        final Handler mUpdateTimeHandler = new UpdateIntervalHandler(this);

        private FilteredBroadcastReceiver timeFormatChangedReceiver;
        private FilteredBroadcastReceiver dayNightBroadcastReceiver;

        private final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        private DataClient dataClient;
        private NodeClient nodeClient;

        public Engine() {
            super();
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFaceService.this)
                    .setStatusBarGravity(Gravity.END | Gravity.TOP)
                    .setViewProtectionMode(WatchFaceStyle.PROTECT_STATUS_BAR)
                    .build());

            dataClient = Wearable.getDataClient(WatchFaceService.this);
            nodeClient = Wearable.getNodeClient(WatchFaceService.this);

            dataClient.addListener(this);

            configuration = ConfigurationBuilder.getDefaultConfiguration(WatchFaceService.this);
            layoutProvider = new LayoutProvider().init(configuration, WatchFaceService.this);

            timeFormatChangedReceiver = new TimeZoneBroadcastReceiver(configuration, this::updateTimezone);
            dayNightBroadcastReceiver = new DayNightBroadcastReceiver(configuration, this::updateUiToConfiguration);
            timeFormatChangedReceiver.register(WatchFaceService.this);
            dayNightBroadcastReceiver.register(WatchFaceService.this);

            new StoredConfigurationFetcher().updateConfig(nodeClient, dataClient, configuration,
                    configuration -> layoutProvider.onConfigurationChange(configuration));

            setActiveComplications(ComplicationType.BATTERY.getId(), ComplicationType.NOTIFICATIONS.getId());
            setDefaultSystemComplicationProvider(ComplicationType.BATTERY.getId(), ComplicationType.BATTERY.getSystemProvider(), ComplicationData.TYPE_SHORT_TEXT);
            setDefaultSystemComplicationProvider(ComplicationType.NOTIFICATIONS.getId(), ComplicationType.NOTIFICATIONS.getSystemProvider(), ComplicationData.TYPE_SHORT_TEXT);
        }


        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onComplicationDataUpdate(int watchFaceComplicationId, ComplicationData data) {
            ComplicationType complicationForId = ComplicationType.getComplicationForId(watchFaceComplicationId);
            layoutProvider.updateComplicationData(data, complicationForId, WatchFaceService.this);
            invalidate();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            layoutProvider.onSurfaceChanged(width, height);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            ambientMode = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            float centerX = bounds.exactCenterX();
            float centerY = bounds.exactCenterY();

            layoutProvider.update(canvas, centerX, centerY, calendar);
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            ambientMode = inAmbientMode;
            updateTimer();

            if (!inAmbientMode) {
                new StoredConfigurationFetcher().updateConfig(nodeClient, dataClient, configuration, this::updateConfiguration);
            }

            layoutProvider.onAmbientModeChanged(inAmbientMode);
            invalidate();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            layoutProvider.applyWindowInsets(getResources());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            updateTimer();
        }

        private void updateTimezone(Configuration configuration) {
            calendar.clear();
            calendar.setTimeZone(TimeZone.getDefault());

            updateConfiguration(configuration);
        }

        private void updateConfiguration(Configuration configuration) {
            dayNightBroadcastReceiver.updateInternalConfigurationState(configuration);
            timeFormatChangedReceiver.updateInternalConfigurationState(configuration);
            updateUiToConfiguration(configuration);
        }

        private void updateUiToConfiguration(Configuration configuration) {
            layoutProvider.onConfigurationChange(configuration);
            invalidate();
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            dataClient.removeListener(this);
            timeFormatChangedReceiver.unregister(WatchFaceService.this);
            dayNightBroadcastReceiver.unregister(WatchFaceService.this);
            super.onDestroy();
        }

        boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        @Override
        public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
            for (DataEvent event : dataEventBuffer) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    DataItem item = event.getDataItem();
                    ConfigUpdater.updateConfig(configuration, item);
                }
            }
            updateConfiguration(configuration);
            dataEventBuffer.release();
        }
    }
}