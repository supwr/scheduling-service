package com.schedulingservice.api.domain.exception;

public class ValidationException extends DomainException {

    private final String field;
    private final Object rejectedValue;
    private final String reason;

    public ValidationException(final String field, final Object rejectedValue, final String reason) {
        super(reason);
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.reason = reason;
    }

    public String getField() {
        return field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public String getReason() {
        return reason;
    }
}
