package com.ticketsystem.repository;

import com.ticketsystem.model.Ticket;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryTicketRepository implements TicketRepository {
    // LinkedHashMap keeps tickets in the order they were created
    private final Map<Integer, Ticket> tickets = new LinkedHashMap<>();

    @Override
    public void save(Ticket ticket) {
        tickets.put(ticket.getId(), ticket);
    }

    @Override
    public Optional<Ticket> findById(int id) {
        return Optional.ofNullable(tickets.get(id));
    }

    @Override
    public List<Ticket> findAll() {
        return new ArrayList<>(tickets.values());
    }

    @Override
    public List<Ticket> findByAgentId(int agentId) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket.getAssignedAgent() != null && ticket.getAssignedAgent().getId() == agentId) {
                result.add(ticket);
            }
        }
        return result;
    }
}
