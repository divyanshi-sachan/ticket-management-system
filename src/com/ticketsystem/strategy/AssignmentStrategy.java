package com.ticketsystem.strategy;

import com.ticketsystem.model.Agent;

import java.util.List;

/**
 * Strategy pattern: "how do we pick an agent?" is a rule that may change,
 * so it lives behind an interface. New rule = new class, no edits elsewhere.
 */
public interface AssignmentStrategy {
    Agent assign(List<Agent> agents);
}
