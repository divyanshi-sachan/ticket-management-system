package com.ticketsystem.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Each status knows which statuses it is allowed to move to.
 * Keeping the rules here means no other class needs a big if/else chain.
 */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public boolean canTransitionTo(TicketStatus next) {
        switch (this) {
            case OPEN:
                return next == IN_PROGRESS;
            case IN_PROGRESS:
                return next == RESOLVED;
            case RESOLVED:
                return next == CLOSED || next == IN_PROGRESS; // close it, or reopen it
            case CLOSED:
            default:
                return false; // closed is final
        }
    }

    /** All statuses this one may move to, e.g. RESOLVED -> [IN_PROGRESS, CLOSED]. */
    public List<TicketStatus> nextStatuses() {
        List<TicketStatus> result = new ArrayList<>();
        for (TicketStatus status : values()) {
            if (canTransitionTo(status)) {
                result.add(status);
            }
        }
        return result;
    }
}
