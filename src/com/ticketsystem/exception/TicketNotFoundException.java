package com.ticketsystem.exception;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(int ticketId) {
        super("Ticket #" + ticketId + " not found");
    }
}
