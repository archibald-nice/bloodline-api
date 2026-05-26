package com.bloodline.common.constants;

public enum RelationType {
    CALLS("CALLS"),
    HTTP_CALLS("HTTP_CALLS"),
    QUERIES("QUERIES"),
    REFERENCES("REFERENCES"),
    EXPOSES("EXPOSES");

    private final String value;

    RelationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
