package com.timrface;

public class Configuration {

    private boolean smoothScrolling;
    private boolean astronomicalClockFormat;
    private boolean showBatteryLevel;
    private boolean showZeroDigit;

    private int interactiveColor;
    private int backgroundColor;
    private int textColor;
    private int arrowResourceId;

    public boolean isSmoothScrolling() {
        return smoothScrolling;
    }

    public Configuration setSmoothScrolling(boolean smoothScrolling) {
        this.smoothScrolling = smoothScrolling;
        return this;
    }

    public int getInteractiveColor() {
        return interactiveColor;
    }

    public Configuration setInteractiveColor(int interactiveColor) {
        this.interactiveColor = interactiveColor;
        return this;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public Configuration setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public boolean isAstronomicalClockFormat() {
        return astronomicalClockFormat;
    }

    public Configuration setAstronomicalClockFormat(boolean astronomicalClockFormat) {
        this.astronomicalClockFormat = astronomicalClockFormat;
        return this;
    }

    public boolean isShowBatteryLevel() {
        return showBatteryLevel;
    }

    public Configuration setShowBatteryLevel(boolean showBatteryLevel) {
        this.showBatteryLevel = showBatteryLevel;
        return this;
    }

    public int getTextColor() {
        return textColor;
    }

    public Configuration setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public boolean isShowZeroDigit() {
        return showZeroDigit;
    }

    public Configuration setShowZeroDigit(boolean showZeroDigit) {
        this.showZeroDigit = showZeroDigit;
        return this;
    }

    public int getArrowResourceId() {
        return arrowResourceId;
    }

    public Configuration setArrowResourceId(int arrowResourceId) {
        this.arrowResourceId = arrowResourceId;
        return this;
    }
}