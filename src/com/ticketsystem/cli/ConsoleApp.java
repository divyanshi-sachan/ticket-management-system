package com.ticketsystem.cli;

import com.ticketsystem.model.Priority;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketStatus;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.service.UserService;

import java.util.List;
import java.util.Scanner;

/** Only handles reading input and printing output. No business rules here. */
public class ConsoleApp {
    private final TicketService ticketService;
    private final UserService userService;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleApp(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    public void start() {
        System.out.println("=== Ticket Management System ===");
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1 -> createTicket();
                    case 2 -> updateTicketStatus();
                    case 3 -> printTickets(ticketService.getAllTickets());
                    case 4 -> viewAgentTickets();
                    case 5 -> listUsers();
                    case 6 -> addAgent();
                    case 7 -> addCustomer();
                    case 0 -> running = false;
                    default -> System.out.println("Invalid option, try again.");
                }
            } catch (RuntimeException e) {
                // every business error ends up here and is shown nicely
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Create ticket");
        System.out.println("2. Update ticket status");
        System.out.println("3. View all tickets");
        System.out.println("4. View tickets of an agent");
        System.out.println("5. List customers and agents");
        System.out.println("6. Add agent");
        System.out.println("7. Add customer");
        System.out.println("0. Exit");
    }

    private void createTicket() {
        int customerId = readInt("Customer id: ");
        String title = readLine("Title: ");
        String description = readLine("Description: ");
        Priority priority = readEnum("Priority (LOW/MEDIUM/HIGH): ", Priority.class);
        Ticket ticket = ticketService.createTicket(customerId, title, description, priority);
        System.out.println("Created: " + ticket);
    }

    private void updateTicketStatus() {
        int ticketId = readInt("Ticket id: ");
        Ticket ticket = ticketService.getTicket(ticketId);
        System.out.println("Current status: " + ticket.getStatus());
        TicketStatus newStatus = readEnum("New status (OPEN/IN_PROGRESS/RESOLVED/CLOSED): ", TicketStatus.class);
        System.out.println("Updated: " + ticketService.updateStatus(ticketId, newStatus));
    }

    private void viewAgentTickets() {
        int agentId = readInt("Agent id: ");
        printTickets(ticketService.getTicketsForAgent(agentId));
    }

    private void listUsers() {
        System.out.println("Customers:");
        userService.getAllCustomers().forEach(c -> System.out.println("  " + c));
        System.out.println("Agents:");
        userService.getAllAgents().forEach(a -> System.out.println("  " + a));
    }

    private void addAgent() {
        String name = readLine("Agent name: ");
        String email = readLine("Agent email: ");
        System.out.println("Added: " + userService.registerAgent(name, email));
    }

    private void addCustomer() {
        String name = readLine("Customer name: ");
        String email = readLine("Customer email: ");
        System.out.println("Added: " + userService.registerCustomer(name, email));
    }

    private void printTickets(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            System.out.println("No tickets found.");
            return;
        }
        tickets.forEach(System.out::println);
    }

    // ---------- small input helpers ----------

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            if (!scanner.hasNextLine()) {
                return 0; // input closed: behave like "exit"
            }
            String input = readLine(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private <E extends Enum<E>> E readEnum(String prompt, Class<E> type) {
        while (true) {
            String input = readLine(prompt).toUpperCase();
            try {
                return Enum.valueOf(type, input);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid value: " + input);
            }
        }
    }
}
