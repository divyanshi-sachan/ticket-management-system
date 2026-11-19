package com.ticketsystem.model;

public class Agent extends User {
    private int activeTicketCount;

    public Agent(int id, String name, String email) {
        super(id, name, email);
        this.activeTicketCount = 0;
    }

    public int getActiveTicketCount() {
        return activeTicketCount;
    }

    public void incrementActiveTickets() {
        activeTicketCount++;
    }

    public void decrementActiveTickets() {
        if (activeTicketCount > 0) {
            activeTicketCount--;
        }
    }

    @Override
    public String getRole() {
        return "Agent";
    }

    @Override
    public String toString() {
        return super.toString() + " | active tickets: " + activeTicketCount;
    }
}
