package com.timrface.watchfacelayout.config;

public enum ConfigurationConstant {

    CONFIG_PATH("/watch_face_config/"), BACKGROUND_COLOR("BACKGROUND_COLOR"), INTERACTIVE_COLOR("INTERACTIVE_COLOR"),
    SMOOTH_SECONDS("SMOOTH_SECONDS"), LEFT_COMPLICATION_ID("LEFT_COMPLICATION_ID"), ZERO_DIGIT("ZERO_DIGIT"),
    MIDDLE_COMPLICATION_ID("MIDDLE_COMPLICATION_ID"), STROKE_DIGITS("STROKE_DIGITS"), AUTOMATIC_DARK_LIGHT("AUTOMATIC_DARK_LIGHT");

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
