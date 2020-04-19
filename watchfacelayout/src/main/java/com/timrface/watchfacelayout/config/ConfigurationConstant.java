package com.timrface.watchfacelayout.config;

public enum ConfigurationConstant {

    CONFIG_PATH("/watch_face_config"), BACKGROUND_COLOR("BACKGROUND_COLOR"), INTERACTIVE_COLOR("INTERACTIVE_COLOR"),
    SMOOTH_SECONDS("SMOOTH_SECONDS"), BATTERY_INDICATOR("BATTERY_INDICATOR"), ZERO_DIGIT("ZERO_DIGIT");

    private final String content;

    ConfigurationConstant(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return getContent();
    }
}
