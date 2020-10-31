package com.timrface.watchfacelayout.config;

import android.support.wearable.complications.SystemProviders;

public enum ComplicationType {
    BATTERY(0, SystemProviders.WATCH_BATTERY),
    NOTIFICATIONS(1, SystemProviders.UNREAD_NOTIFICATION_COUNT),
    STEPS(2, SystemProviders.STEP_COUNT),
    NONE(-1, -1);

    private final int id;
    private final int systemProvider;

    ComplicationType(int id, int systemProvider) {
        this.id = id;
        this.systemProvider = systemProvider;
    }

    public int getSystemProvider() {
        return systemProvider;
    }

    public int getId() {
        return id;
    }

    public static ComplicationType getComplicationForId(int id) {
        for (ComplicationType type : ComplicationType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("No Complication type found for id " + id);
    }
}
