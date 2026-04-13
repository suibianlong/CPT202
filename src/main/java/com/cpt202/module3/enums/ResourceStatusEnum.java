package com.cpt202.module3.enums;

public enum ResourceStatusEnum {

    DRAFT("DRAFT"),
    PENDING_REVIEW("PENDING_REVIEW"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    ARCHIVED("ARCHIVED");

    private final String value;

    ResourceStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ResourceStatusEnum fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Resource status cannot be null");
        }

        switch (value.toUpperCase()) {
            case "DRAFT":
                return DRAFT;
            case "PENDING_REVIEW":
                return PENDING_REVIEW;
            case "APPROVED":
                return APPROVED;
            case "REJECTED":
                return REJECTED;
            case "ARCHIVED":
                return ARCHIVED;
            default:
                throw new IllegalArgumentException("Unknown resource status: " + value);
        }
    }
}