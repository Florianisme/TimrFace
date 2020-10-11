package com.timrface;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

final class UpdateIntervalHandler extends Handler {

    static final int MSG_UPDATE_TIME = 0;

    private final WeakReference<WatchFaceService.Engine> mWeakReference;

    public UpdateIntervalHandler(WatchFaceService.Engine reference) {
        mWeakReference = new WeakReference<>(reference);
    }

    @Override
    public void handleMessage(@NonNull Message message) {
        WatchFaceService.Engine engine = mWeakReference.get();
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