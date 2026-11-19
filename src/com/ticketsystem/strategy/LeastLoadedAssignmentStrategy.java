package com.ticketsystem.strategy;

import com.ticketsystem.model.Agent;

import java.util.List;

/**
 * Gives the new ticket to the agent with the fewest active tickets.
 * Proves Open/Closed: a brand-new rule, and TicketService is not touched at all.
 */
public class LeastLoadedAssignmentStrategy implements AssignmentStrategy {

    @Override
    public Agent assign(List<Agent> agents) {
        if (agents.isEmpty()) {
            throw new IllegalStateException("No agents available to assign the ticket");
        }

        // TODO(human): return the agent with the lowest getActiveTicketCount().
        // Decide what happens on a tie (two agents with the same count).
        throw new UnsupportedOperationException("LeastLoadedAssignmentStrategy not implemented yet");
    }
}
