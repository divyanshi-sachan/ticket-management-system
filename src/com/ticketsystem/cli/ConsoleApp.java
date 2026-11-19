package com.ticketsystem.cli;

import com.ticketsystem.model.Customer;
import com.ticketsystem.model.Priority;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketStatus;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.service.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controls the flow of the app: show menu, ask questions, call services.
 * No business rules here (services) and no formatting here (ConsoleView).
 */
public class ConsoleApp {

    private enum MenuOption {
        CREATE_TICKET("Create ticket"),
        UPDATE_STATUS("Update ticket status"),
        VIEW_ALL_TICKETS("View all tickets"),
        VIEW_TICKET("View ticket details"),
        VIEW_AGENT_TICKETS("View tickets of an agent"),
        LIST_USERS("List customers and agents"),
        ADD_AGENT("Add agent"),
        ADD_CUSTOMER("Add customer"),
        EXIT("Exit");

        private final String label;

        MenuOption(String label) {
            this.label = label;
        }
    }

    private final TicketService ticketService;
    private final UserService userService;
    private final InputReader input;
    private final MenuSelector menu;
    private final ConsoleView view;

    public ConsoleApp(TicketService ticketService, UserService userService,
                      InputReader input, MenuSelector menu, ConsoleView view) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.input = input;
        this.menu = menu;
        this.view = view;
    }

    public void start() {
        view.printBanner();
        boolean running = true;
        while (running) {
            try {
                System.out.println();
                MenuOption option = choose("MAIN MENU", Arrays.asList(MenuOption.values()), o -> o.label);
                switch (option) {
                    case CREATE_TICKET -> createTicket();
                    case UPDATE_STATUS -> updateTicketStatus();
                    case VIEW_ALL_TICKETS -> viewAllTickets();
                    case VIEW_TICKET -> viewTicket();
                    case VIEW_AGENT_TICKETS -> viewAgentTickets();
                    case LIST_USERS -> listUsers();
                    case ADD_AGENT -> addAgent();
                    case ADD_CUSTOMER -> addCustomer();
                    case EXIT -> running = false;
                }
            } catch (InputEndedException e) {
                running = false; // Ctrl+D or end of piped input
            } catch (RuntimeException e) {
                view.error(e.getMessage()); // every business error is shown here, in one place
            }
        }
        view.info("Goodbye!");
    }

    private void createTicket() {
        view.heading("Create ticket");
        int customerId = input.readInt("Customer id: ");
        Customer customer = userService.getCustomer(customerId); // fail early, before asking more
        view.info("Raising a ticket for " + customer.getName());

        String title = input.readRequiredLine("Title: ");
        String description = input.readLine("Description (optional): ");
        Priority priority = choose("Priority", Arrays.asList(Priority.values()), Enum::name);

        Ticket ticket = ticketService.createTicket(customerId, title, description, priority);
        view.success("Ticket #" + ticket.getId() + " created and assigned to " + ticket.getAssignedAgent().getName());
        view.printTicketDetails(ticket);
    }

    private void updateTicketStatus() {
        view.heading("Update ticket status");
        int ticketId = input.readInt("Ticket id: ");
        Ticket ticket = ticketService.getTicket(ticketId);

        // only offer moves the lifecycle allows, so the user can't pick an invalid one
        List<TicketStatus> allowed = ticket.getStatus().nextStatuses();
        if (allowed.isEmpty()) {
            view.info("Ticket #" + ticketId + " is " + ticket.getStatus() + " and cannot change any more.");
            return;
        }
        view.info("Ticket #" + ticketId + " is currently " + ticket.getStatus());
        TicketStatus newStatus = choose("Move ticket #" + ticketId + " to", allowed, Enum::name);

        ticketService.updateStatus(ticketId, newStatus);
        view.success("Ticket #" + ticketId + " moved to " + newStatus);
    }

    private void viewAllTickets() {
        view.heading("All tickets");
        view.printTicketTable(ticketService.getAllTickets());
    }

    private void viewTicket() {
        view.heading("Ticket details");
        int ticketId = input.readInt("Ticket id: ");
        view.printTicketDetails(ticketService.getTicket(ticketId));
    }

    private void viewAgentTickets() {
        view.heading("Tickets of an agent");
        int agentId = input.readInt("Agent id: ");
        view.printTicketTable(ticketService.getTicketsForAgent(agentId));
    }

    private void listUsers() {
        view.heading("Customers and agents");
        view.printUsers(userService.getAllCustomers(), userService.getAllAgents());
    }

    private void addAgent() {
        view.heading("Add agent");
        String name = input.readRequiredLine("Agent name: ");
        String email = input.readRequiredLine("Agent email: ");
        view.success("Added " + userService.registerAgent(name, email));
    }

    private void addCustomer() {
        view.heading("Add customer");
        String name = input.readRequiredLine("Customer name: ");
        String email = input.readRequiredLine("Customer email: ");
        view.success("Added " + userService.registerCustomer(name, email));
    }

    /** Turns any list of items into menu labels, asks the MenuSelector, returns the chosen item. */
    private <T> T choose(String title, List<T> items, java.util.function.Function<T, String> toLabel) {
        List<String> labels = new ArrayList<>();
        for (T item : items) {
            labels.add(toLabel.apply(item));
        }
        return items.get(menu.select(title, labels));
    }
}
