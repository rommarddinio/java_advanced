package com.innowise.orderservice.enums;

import java.util.Arrays;

public enum Status {
    NEW,
    PAID,
    CANCELLED;

    public static boolean isValid(Status value) {
        return Arrays.asList(values()).contains(value);
    }

}
