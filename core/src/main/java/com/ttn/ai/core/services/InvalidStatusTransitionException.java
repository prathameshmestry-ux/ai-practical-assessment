package com.ttn.ai.core.services;

/**
 * Thrown when a ticket status transition is not allowed.
 */
public class InvalidStatusTransitionException extends Exception {

    private final String fromStatus;
    private final String toStatus;

    public InvalidStatusTransitionException(String fromStatus, String toStatus) {
        super("Cannot transition from " + fromStatus + " to " + toStatus);
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }
}
