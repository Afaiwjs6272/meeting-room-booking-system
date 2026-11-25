package ru.company.reference.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationException extends RuntimeException {

    private final String field;
    private final String message;

    public ValidationException(String field, String message) {
        super(String.format("Validation error for field '%s': %s", field, message));
        this.field = field;
        this.message = message;
    }

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.message = message;
    }
}