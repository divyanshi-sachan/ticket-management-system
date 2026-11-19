package com.ticketsystem.repository;

import com.ticketsystem.model.Ticket;

import java.util.List;
import java.util.Optional;

/** Storage contract. The service depends on this, never on a concrete class. */
public interface TicketRepository {
    void save(Ticket ticket);

    Optional<Ticket> findById(int id);

    List<Ticket> findAll();

    List<Ticket> findByAgentId(int agentId);
}
