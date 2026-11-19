package com.ticketsystem.service;

import com.ticketsystem.exception.TicketNotFoundException;
import com.ticketsystem.exception.UserNotFoundException;
import com.ticketsystem.model.Agent;
import com.ticketsystem.model.Customer;
import com.ticketsystem.model.Priority;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketStatus;
import com.ticketsystem.notification.NotificationService;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.strategy.AssignmentStrategy;

import java.util.List;

/**
 * All ticket business rules live here.
 * Every dependency is an interface, so this class never changes
 * when we swap storage, assignment rule or notification channel.
 */
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AssignmentStrategy assignmentStrategy;
    private final NotificationService notificationService;
    private int nextTicketId = 1;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         AssignmentStrategy assignmentStrategy,
                         NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.assignmentStrategy = assignmentStrategy;
        this.notificationService = notificationService;
    }

    public Ticket createTicket(int customerId, String title, String description, Priority priority) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Ticket title cannot be empty");
        }
        Customer customer = userRepository.findCustomerById(customerId)
                .orElseThrow(() -> new UserNotFoundException("Customer", customerId));

        Ticket ticket = new Ticket(nextTicketId++, title, description, priority, customer);

        Agent agent = assignmentStrategy.assign(userRepository.findAllAgents());
        ticket.assignTo(agent);
        agent.incrementActiveTickets();

        ticketRepository.save(ticket);
        notificationService.notify(customer,
                "Ticket #" + ticket.getId() + " created and assigned to " + agent.getName());
        return ticket;
    }

    public Ticket updateStatus(int ticketId, TicketStatus newStatus) {
        Ticket ticket = getTicket(ticketId);
        TicketStatus oldStatus = ticket.getStatus();

        ticket.updateStatus(newStatus); // throws if the move is not allowed
        updateAgentWorkload(ticket.getAssignedAgent(), oldStatus, newStatus);

        ticketRepository.save(ticket);
        notificationService.notify(ticket.getCustomer(),
                "Ticket #" + ticketId + " moved from " + oldStatus + " to " + newStatus);
        return ticket;
    }

    public Ticket getTicket(int ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsForAgent(int agentId) {
        userRepository.findAgentById(agentId)
                .orElseThrow(() -> new UserNotFoundException("Agent", agentId));
        return ticketRepository.findByAgentId(agentId);
    }

    /** A resolved ticket frees the agent; a reopened one takes their time again. */
    private void updateAgentWorkload(Agent agent, TicketStatus oldStatus, TicketStatus newStatus) {
        if (agent == null) {
            return;
        }
        if (newStatus == TicketStatus.RESOLVED) {
            agent.decrementActiveTickets();
        } else if (oldStatus == TicketStatus.RESOLVED && newStatus == TicketStatus.IN_PROGRESS) {
            agent.incrementActiveTickets();
        }
    }
}
