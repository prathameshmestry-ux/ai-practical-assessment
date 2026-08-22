package com.ttn.ai.core.services;

import static com.ttn.ai.core.constants.TicketConstants.STATUS_CANCELLED;
import static com.ttn.ai.core.constants.TicketConstants.STATUS_CLOSED;
import static com.ttn.ai.core.constants.TicketConstants.STATUS_IN_PROGRESS;
import static com.ttn.ai.core.constants.TicketConstants.STATUS_OPEN;
import static com.ttn.ai.core.constants.TicketConstants.STATUS_RESOLVED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketStatusTransitionValidatorTest {

    private TicketStatusTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TicketStatusTransitionValidator();
    }

    @Test
    void allowsValidTransitions() {
        assertDoesNotThrow(() -> validator.validateTransition(STATUS_OPEN, STATUS_IN_PROGRESS));
        assertDoesNotThrow(() -> validator.validateTransition(STATUS_OPEN, STATUS_CANCELLED));
        assertDoesNotThrow(() -> validator.validateTransition(STATUS_IN_PROGRESS, STATUS_RESOLVED));
        assertDoesNotThrow(() -> validator.validateTransition(STATUS_RESOLVED, STATUS_CLOSED));
    }

    @Test
    void rejectsInvalidTransitions() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> validator.validateTransition(STATUS_OPEN, STATUS_RESOLVED));
        assertThrows(InvalidStatusTransitionException.class,
                () -> validator.validateTransition(STATUS_CLOSED, STATUS_OPEN));
    }

    @Test
    void terminalStatusesHaveNoTargets() {
        assertTrue(validator.getAllowedTargets(STATUS_CLOSED).isEmpty());
        assertTrue(validator.getAllowedTargets(STATUS_CANCELLED).isEmpty());
        assertTrue(validator.isTerminal(STATUS_CLOSED));
    }
}
