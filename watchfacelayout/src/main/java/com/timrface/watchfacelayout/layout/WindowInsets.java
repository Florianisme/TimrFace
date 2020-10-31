package com.timrface.watchfacelayout.layout;

public class WindowInsets {

    private final float timeTextSize;
    private final float infoTextSize;
    private final float tickTextSize;

    private final float tickHorizontalDistance;
    private final float tickBottomDistance;
    private final float thinTickWidth;
    private final float tickWidth;
    private final float shortTickHeight;
    private final float tickHeight;

    public WindowInsets(float timeTextSize, float infoTextSize, float tickTextSize, float tickHorizontalDistance, float tickBottomDistance, float thinTickWidth, float tickWidth, float shortTickHeight, float tickHeight) {
        this.timeTextSize = timeTextSize;
        this.infoTextSize = infoTextSize;
        this.tickTextSize = tickTextSize;
        this.tickHorizontalDistance = tickHorizontalDistance;
        this.tickBottomDistance = tickBottomDistance;
        this.thinTickWidth = thinTickWidth;
        this.tickWidth = tickWidth;
        this.shortTickHeight = shortTickHeight;
        this.tickHeight = tickHeight;
    }

    public float getShortTickHeight() {
        return shortTickHeight;
    }

    public float getTickHeight() {
        return tickHeight;
    }

    public float getTickHorizontalDistance() {
        return tickHorizontalDistance;
    }

    public float getTickBottomDistance() {
        return tickBottomDistance;
    }

    public float getThinTickWidth() {
        return thinTickWidth;
    }

    public float getTickWidth() {
        return tickWidth;
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
