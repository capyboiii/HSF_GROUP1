package com.group1.recruitment.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carries field-level (or global) validation messages back to a controller so it
 * can re-render the form with inline errors. Key {@code "global"} is used for
 * form-wide messages.
 */
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public static ValidationException of(String field, String message) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(field, message);
        return new ValidationException(m);
    }

    public static ValidationException global(String message) {
        return of("global", message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
