package com.ticketsystem.strategy;

import com.ticketsystem.model.Agent;

import java.util.List;

/** Hands tickets to agents in turn: A, B, C, A, B, C... */
public class RoundRobinAssignmentStrategy implements AssignmentStrategy {
    private int nextIndex = 0;

    @Override
    public Agent assign(List<Agent> agents) {
        if (agents.isEmpty()) {
            throw new IllegalStateException("No agents available to assign the ticket");
        }
        Agent chosen = agents.get(nextIndex % agents.size());
        nextIndex++;
        return chosen;
    }
}
