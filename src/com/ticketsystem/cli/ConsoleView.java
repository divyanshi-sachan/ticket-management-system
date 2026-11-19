package com.ticketsystem.cli;

import com.ticketsystem.model.Agent;
import com.ticketsystem.model.Customer;
import com.ticketsystem.model.Priority;
import com.ticketsystem.model.Ticket;
import com.ticketsystem.model.TicketStatus;

import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Everything the user SEES: banner, tables, messages, colors.
 * ConsoleApp decides WHAT to show; this class decides HOW it looks (Single Responsibility).
 */
public class ConsoleView {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String DIM = "\033[2m";
    private static final String RED = "\033[31m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String BLUE = "\033[34m";
    private static final String CYAN = "\033[36m";

    private static final String TICKET_LINE =
            "+------+----------------------------+----------+-------------+------------+------------+----------+";
    private static final String USER_LINE =
            "+------+----------+--------------+--------------------------+--------+";

    private final boolean colorsEnabled;

    public ConsoleView(boolean colorsEnabled) {
        this.colorsEnabled = colorsEnabled;
    }

    /** Colors only in a real terminal; piped output stays plain text. Respects NO_COLOR. */
    public static boolean colorsSupported() {
        return System.console() != null && System.getenv("NO_COLOR") == null;
    }

    // ---------- general messages ----------

    public void printBanner() {
        System.out.println(paint("+==================================================+", CYAN));
        System.out.println(paint("|            TICKET MANAGEMENT SYSTEM              |", CYAN + BOLD));
        System.out.println(paint("|      simple LLD project: SOLID + patterns        |", CYAN));
        System.out.println(paint("+==================================================+", CYAN));
    }

    public void heading(String text) {
        System.out.println();
        System.out.println(paint("== " + text + " ==", BOLD));
    }

    public void success(String message) {
        System.out.println(paint("[OK] " + message, GREEN));
    }

    public void error(String message) {
        System.out.println(paint("[ERROR] " + message, RED));
    }

    public void info(String message) {
        System.out.println(paint("-> " + message, DIM));
    }

    // ---------- tickets ----------

    public void printTicketTable(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            info("No tickets found.");
            return;
        }
        System.out.println(TICKET_LINE);
        System.out.println(paint(String.format("| %-4s | %-26s | %-8s | %-11s | %-10s | %-10s | %-8s |",
                "ID", "Title", "Priority", "Status", "Customer", "Agent", "Updated"), BOLD));
        System.out.println(TICKET_LINE);
        for (Ticket ticket : tickets) {
            System.out.println(String.format("| %-4s | %-26s | %s | %s | %-10s | %-10s | %-8s |",
                    "#" + ticket.getId(),
                    cut(ticket.getTitle(), 26),
                    paint(pad(ticket.getPriority().name(), 8), colorOf(ticket.getPriority())),
                    paint(pad(ticket.getStatus().name(), 11), colorOf(ticket.getStatus())),
                    cut(ticket.getCustomer().getName(), 10),
                    cut(agentName(ticket), 10),
                    ticket.getUpdatedAt().format(TIME_FORMAT)));
        }
        System.out.println(TICKET_LINE);
        System.out.println(paint(summary(tickets), DIM));
    }

    public void printTicketDetails(Ticket ticket) {
        String line = "+--------------------------------------------------+";
        List<TicketStatus> next = ticket.getStatus().nextStatuses();

        System.out.println(line);
        System.out.println(paint("  Ticket #" + ticket.getId() + "  " + ticket.getTitle(), BOLD));
        System.out.println(line);
        System.out.println("  Status      : " + paint(ticket.getStatus().name(), colorOf(ticket.getStatus())));
        System.out.println("  Priority    : " + paint(ticket.getPriority().name(), colorOf(ticket.getPriority())));
        System.out.println("  Customer    : " + ticket.getCustomer().getName()
                + " (" + ticket.getCustomer().getEmail() + ")");
        System.out.println("  Agent       : " + agentName(ticket));
        System.out.println("  Created     : " + ticket.getCreatedAt().format(TIME_FORMAT));
        System.out.println("  Updated     : " + ticket.getUpdatedAt().format(TIME_FORMAT));
        System.out.println("  Description : " + (ticket.getDescription().isEmpty() ? "-" : ticket.getDescription()));
        System.out.println("  Can move to : " + (next.isEmpty() ? "nothing (ticket is final)" : next));
        System.out.println(line);
    }

    // ---------- users ----------

    public void printUsers(List<Customer> customers, List<Agent> agents) {
        System.out.println(USER_LINE);
        System.out.println(paint(String.format("| %-4s | %-8s | %-12s | %-24s | %-6s |",
                "ID", "Role", "Name", "Email", "Active"), BOLD));
        System.out.println(USER_LINE);
        for (Customer customer : customers) {
            System.out.println(String.format("| %-4s | %-8s | %-12s | %-24s | %-6s |",
                    customer.getId(), "Customer", cut(customer.getName(), 12), cut(customer.getEmail(), 24), "-"));
        }
        for (Agent agent : agents) {
            System.out.println(String.format("| %-4s | %s | %-12s | %-24s | %-6s |",
                    agent.getId(), paint(pad("Agent", 8), CYAN), cut(agent.getName(), 12),
                    cut(agent.getEmail(), 24), agent.getActiveTicketCount()));
        }
        System.out.println(USER_LINE);
    }

    // ---------- small helpers ----------

    private String summary(List<Ticket> tickets) {
        Map<TicketStatus, Integer> counts = new EnumMap<>(TicketStatus.class);
        for (TicketStatus status : TicketStatus.values()) {
            counts.put(status, 0);
        }
        for (Ticket ticket : tickets) {
            counts.merge(ticket.getStatus(), 1, Integer::sum);
        }
        StringBuilder text = new StringBuilder(tickets.size() + " ticket(s) |");
        counts.forEach((status, count) -> text.append(' ').append(status).append(": ").append(count));
        return text.toString();
    }

    private String agentName(Ticket ticket) {
        return ticket.getAssignedAgent() == null ? "unassigned" : ticket.getAssignedAgent().getName();
    }

    private String colorOf(TicketStatus status) {
        switch (status) {
            case OPEN:        return YELLOW;
            case IN_PROGRESS: return BLUE;
            case RESOLVED:    return GREEN;
            default:          return DIM;
        }
    }

    private String colorOf(Priority priority) {
        switch (priority) {
            case HIGH:   return RED;
            case MEDIUM: return YELLOW;
            default:     return GREEN;
        }
    }

    /** Color is added AFTER padding, so invisible color codes never break column widths. */
    private String paint(String text, String color) {
        return colorsEnabled ? color + text + RESET : text;
    }

    private String pad(String text, int width) {
        return String.format("%-" + width + "s", text);
    }

    private String cut(String text, int width) {
        return text.length() <= width ? text : text.substring(0, width - 3) + "...";
    }
}
