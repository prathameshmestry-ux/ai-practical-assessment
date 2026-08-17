package com.ttn.ai.core.services;

import static com.ttn.ai.core.constants.TicketConstants.STATUS_CANCELLED;
import static com.ttn.ai.core.constants.TicketConstants.STATUS_CLOSED;
import static com.ttn.ai.core.constants.TicketConstants.STATUS_IN_PROGRESS;
import static com.ttn.ai.core.constants.TicketConstants.STATUS_OPEN;
import static com.ttn.ai.core.constants.TicketConstants.STATUS_RESOLVED;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

/**
 * Validates ticket status transitions per FR-010.
 */
@Component(service = TicketStatusTransitionValidator.class)
public class TicketStatusTransitionValidator {

    private static final Map<String, Set<String>> ALLOWED = buildAllowed();

    private static Map<String, Set<String>> buildAllowed() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put(STATUS_OPEN, set(STATUS_IN_PROGRESS, STATUS_CANCELLED));
        map.put(STATUS_IN_PROGRESS, set(STATUS_RESOLVED, STATUS_CANCELLED));
        map.put(STATUS_RESOLVED, set(STATUS_CLOSED));
        map.put(STATUS_CLOSED, Collections.emptySet());
        map.put(STATUS_CANCELLED, Collections.emptySet());
        return map;
    }

    private static Set<String> set(String... values) {
        Set<String> set = new HashSet<>();
        for (String value : values) {
            set.add(value);
        }
        return set;
    }

    public void validateTransition(String currentStatus, String newStatus) throws InvalidStatusTransitionException {
        if (currentStatus == null || newStatus == null) {
            throw new InvalidStatusTransitionException(currentStatus, newStatus);
        }
        Set<String> allowed = ALLOWED.getOrDefault(currentStatus, Collections.emptySet());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, newStatus);
        }
    }

    public Set<String> getAllowedTargets(String currentStatus) {
        return ALLOWED.getOrDefault(currentStatus, Collections.emptySet());
    }

    public boolean isTerminal(String status) {
        return STATUS_CLOSED.equals(status) || STATUS_CANCELLED.equals(status);
    }
}
