package com.rjs.recipesbuddy.util;

import java.util.List;

public class ValueValidator {
    
    public static boolean isVoid(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isVoid(Integer value) {
        return value == null || value == 0;
    }

    public static boolean isVoid(Double value) {
        return value == null || value == 0.0;
    }

    public static boolean isVoid(Long value) {
        return value == null || value == 0L;
    }

    public static boolean isVoid(List<?> value) {
        return value == null || value.isEmpty();
    }
}
