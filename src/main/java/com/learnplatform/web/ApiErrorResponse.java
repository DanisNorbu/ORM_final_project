package com.learnplatform.web;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationError> validationErrors
) {

    public record FieldValidationError(String field, String message) {
    }
}
