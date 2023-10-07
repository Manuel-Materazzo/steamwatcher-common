package com.steamwatcher.dto.enums;

public enum Severity {
    INFO("info"),
    WARNING("warning"),
    ERROR("error");

    public final String value;

    private Severity(String value) {
        this.value = value;
    }
}
