package com.ticketsystem.exception;

import com.ticketsystem.model.TicketStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super("Cannot move ticket from " + from + " to " + to);
    }
}
