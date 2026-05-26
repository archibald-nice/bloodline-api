package com.bloodline.common.constants;

public enum TargetType {
    SERVICE("SERVICE"),
    TABLE("TABLE"),
    API_ENDPOINT("API_ENDPOINT"),
    DATABASE("DATABASE");

    private final String value;

    TargetType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
