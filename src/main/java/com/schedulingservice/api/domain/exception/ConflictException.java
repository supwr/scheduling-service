package com.schedulingservice.api.domain.exception;

public class ConflictException extends DomainException {

    private final String resource;
    private final String reason;

    public ConflictException(final String resource, final String reason) {
        super(reason);
        this.resource = resource;
        this.reason = reason;
    }

    public String getResource() {
        return resource;
    }

    public String getReason() {
        return reason;
    }
}
