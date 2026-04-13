package com.cpt202.module3.enums;

public enum ResourceTypeEnum {

    IMAGE("IMAGE"),
    VIDEO("VIDEO"),
    AUDIO("AUDIO"),
    DOCUMENT("DOCUMENT");

    private final String value;

    ResourceTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ResourceTypeEnum fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Resource type cannot be null");
        }

        switch (value.toUpperCase()) {
            case "IMAGE":
                return IMAGE;
            case "VIDEO":
                return VIDEO;
            case "AUDIO":
                return AUDIO;
            case "DOCUMENT":
                return DOCUMENT;
            default:
                throw new IllegalArgumentException("Unknown resource type: " + value);
        }
    }
}