package com.ticketsystem;

import com.ticketsystem.cli.ConsoleApp;
import com.ticketsystem.notification.ConsoleNotificationService;
import com.ticketsystem.notification.NotificationService;
import com.ticketsystem.repository.InMemoryTicketRepository;
import com.ticketsystem.repository.InMemoryUserRepository;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.repository.UserRepository;
import com.ticketsystem.service.TicketService;
import com.ticketsystem.service.UserService;
import com.ticketsystem.strategy.AssignmentStrategy;
import com.ticketsystem.strategy.RoundRobinAssignmentStrategy;

/**
 * Composition root: the ONLY place that knows the concrete classes.
 * To change behaviour (DB, email, another assignment rule) you edit just this file.
 */
public class Main {
    public static void main(String[] args) {
        UserRepository userRepository = new InMemoryUserRepository();
        TicketRepository ticketRepository = new InMemoryTicketRepository();
        AssignmentStrategy assignmentStrategy = new RoundRobinAssignmentStrategy();
        NotificationService notificationService = new ConsoleNotificationService();

        UserService userService = new UserService(userRepository);
        TicketService ticketService = new TicketService(
                ticketRepository, userRepository, assignmentStrategy, notificationService);

        seedData(userService);

        new ConsoleApp(ticketService, userService).start();
    }

    private static void seedData(UserService userService) {
        userService.registerCustomer("Rahul", "rahul@mail.com");   // id 1
        userService.registerCustomer("Priya", "priya@mail.com");   // id 2
        userService.registerAgent("Amit", "amit@support.com");     // id 3
        userService.registerAgent("Neha", "neha@support.com");     // id 4
    }
}
