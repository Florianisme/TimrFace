package com.timrface;

import android.graphics.Canvas;
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
import androidx.annotation.NonNull;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.*;
import com.timrface.watchfacelayout.config.*;
import com.timrface.watchfacelayout.layout.LayoutProvider;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;

import static com.timrface.WatchFaceService.Engine.MSG_UPDATE_TIME;

public class WatchFaceService extends CanvasWatchFaceService {

    private LayoutProvider layoutProvider;

    boolean ambientMode = false;
    private Configuration configuration;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    /*
        Update Handler for refreshing the canvas
     */
    private static final class UpdateIntervalHandler extends Handler {

        private final WeakReference<Engine> mWeakReference;

        public UpdateIntervalHandler(WatchFaceService.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message message) {
            Engine engine = mWeakReference.get();
            if (engine != null) {
                if (message.what == MSG_UPDATE_TIME) {
                    engine.invalidate();
                    if (engine.shouldTimerBeRunning()) {
                        long timeMs = System.currentTimeMillis();
                        long delayMs = 16 - (timeMs % 16);
                        sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                    }
                }
            }
        }
    }

    public class Engine extends CanvasWatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataClient.OnDataChangedListener {


        static final int MSG_UPDATE_TIME = 0;
        final Handler mUpdateTimeHandler = new UpdateIntervalHandler(this);

        private final String TAG = "WatchFaceService";
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFaceService.this)
                    .setStatusBarGravity(Gravity.END | Gravity.TOP)
                    .setViewProtectionMode(WatchFaceStyle.PROTECT_STATUS_BAR)
                    .build());

            Wearable.getDataClient(WatchFaceService.this).addListener(this);

            configuration = ConfigurationBuilder.getDefaultConfiguration();
            layoutProvider = new LayoutProvider().init(configuration, getApplicationContext());

            NodeClient nodeClient = Wearable.getNodeClient(WatchFaceService.this);
            DataClient dataClient = Wearable.getDataClient(WatchFaceService.this);
            new StoredConfigurationFetcher().updateConfig(nodeClient, dataClient, configuration, new ConfigUpdateFinished() {
                @Override
                public void onUpdateFinished(Configuration configuration) {
                    layoutProvider.onConfigurationChange(configuration);
                }
            });
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
            ambientMode = inAmbientMode;
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

            updateTimer();
        }

        private void updateConfiguration() {
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
        public void onConnected(Bundle bundle) {
            Log.d(TAG, "connected GoogleAPI");
            Wearable.getDataClient(WatchFaceService.this).addListener(this);
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
            Wearable.getDataClient(getApplicationContext()).removeListener(this);
            super.onDestroy();
        }

        private boolean shouldTimerBeRunning() {
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
            updateConfiguration();
            dataEventBuffer.release();
        }
    }
}