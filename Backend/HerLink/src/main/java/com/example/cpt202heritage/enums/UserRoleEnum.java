package com.example.cpt202heritage.enums;

public enum UserRoleEnum {

    REGISTERED_VIEWER("REGISTERED_VIEWER"),
    ADMINISTRATOR("ADMINISTRATOR");

    private final String value;

    UserRoleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRoleEnum fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("User role cannot be null.");
        }

        for (UserRoleEnum roleEnum : values()) {
            if (roleEnum.value.equalsIgnoreCase(value)) {
                return roleEnum;
            }
        }

        throw new IllegalArgumentException("Unknown user role: " + value);
    }
}
