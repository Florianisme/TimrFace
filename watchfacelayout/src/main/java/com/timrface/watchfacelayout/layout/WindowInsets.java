package com.timrface.watchfacelayout.layout;

public class WindowInsets {

    private final float timeTextSize;
    private final float infoTextSize;
    private final float tickTextSize;

    public WindowInsets(float timeTextSize, float infoTextSize, float tickTextSize) {
        this.timeTextSize = timeTextSize;
        this.infoTextSize = infoTextSize;
        this.tickTextSize = tickTextSize;
    }

    public float getTimeTextSize() {
        return timeTextSize;
    }

    public float getInfoTextSize() {
        return infoTextSize;
    }

    public float getTickTextSize() {
        return tickTextSize;
    }
}
