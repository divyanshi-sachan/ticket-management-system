package com.ticketsystem.model;

import com.ticketsystem.exception.InvalidStatusTransitionException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final int id;
    private final String title;
    private final String description;
    private final Priority priority;
    private final Customer customer;
    private final LocalDateTime createdAt;

    private TicketStatus status;
    private Agent assignedAgent;
    private LocalDateTime updatedAt;

    public Ticket(int id, String title, String description, Priority priority, Customer customer) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.customer = customer;
        this.status = TicketStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    /** The only way to change status, so the rules can never be skipped. */
    public void updateStatus(TicketStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new InvalidStatusTransitionException(status, next);
        }
        this.status = next;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignTo(Agent agent) {
        this.assignedAgent = agent;
        this.updatedAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public Customer getCustomer() {
        return customer;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Agent getAssignedAgent() {
        return assignedAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        String agentName = (assignedAgent == null) ? "unassigned" : assignedAgent.getName();
        return String.format("#%-3d %-30s %-7s %-12s customer: %-10s agent: %-10s updated: %s",
                id, title, priority, status, customer.getName(), agentName,
                updatedAt.format(TIME_FORMAT));
    }
}
