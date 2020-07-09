package com.timrface.watchfacelayout.config;

import android.graphics.Color;

public class Configuration {

    private boolean smoothScrolling;
    private boolean astronomicalClockFormat;
    private boolean showBatteryLevel;
    private boolean showZeroDigit;
    private boolean showUnreadNotificationsCounter;
    private boolean useStrokeDigitsInAmbientMode;
    private boolean automaticLightDarkMode;

    private int interactiveColor;
    private int backgroundColor;
    private int textColor;

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

    public Configuration setInteractiveColor(String interactiveColor) {
        this.interactiveColor = Color.parseColor(interactiveColor);
        return this;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public Configuration setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public Configuration setBackgroundColor(String backgroundColor) {
        this.backgroundColor = Color.parseColor(backgroundColor);
        return this;
    }

    public Configuration setAutomaticDarkLightMode(boolean darkLightMode) {
        this.automaticLightDarkMode = darkLightMode;
        return this;
    }

    public boolean isAstronomicalClockFormat() {
        return astronomicalClockFormat;
    }

    public Configuration setAstronomicalClockFormat(boolean astronomicalClockFormat) {
        this.astronomicalClockFormat = astronomicalClockFormat;
        return this;
    }

    public boolean isAutomaticLightDarkMode() {
        return automaticLightDarkMode;
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

    public Configuration setTextColor(String textColor) {
        this.textColor = Color.parseColor(textColor);
        return this;
    }

    public boolean isShowZeroDigit() {
        return showZeroDigit;
    }

    public Configuration setShowZeroDigit(boolean showZeroDigit) {
        this.showZeroDigit = showZeroDigit;
        return this;
    }

    public boolean isShowUnreadNotificationsCounter() {
        return showUnreadNotificationsCounter;
    }

    public Configuration setShowUnreadNotificationsCounter(boolean showUnreadNotificationsCounter) {
        this.showUnreadNotificationsCounter = showUnreadNotificationsCounter;
        return this;
    }

    public boolean isUseStrokeDigitsInAmbientMode() {
        return useStrokeDigitsInAmbientMode;
    }

    public Configuration setUseStrokeDigitsInAmbientMode(boolean useStrokeDigitsInAmbientMode) {
        this.useStrokeDigitsInAmbientMode = useStrokeDigitsInAmbientMode;
        return this;
    }
}
