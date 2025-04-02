package com.example.vohoportunitysconect.models;

public enum UserType {
    ORGANIZATION("organization"),
    VOLUNTEER("volunteer");

    private final String value;

    UserType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserType fromValue(String value) {
        for (UserType type : UserType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return VOLUNTEER; // Default to VOLUNTEER if unknown value
    }
} 