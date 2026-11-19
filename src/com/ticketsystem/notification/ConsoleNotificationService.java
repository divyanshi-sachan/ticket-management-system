package com.ticketsystem.notification;

import com.ticketsystem.model.User;

public class ConsoleNotificationService implements NotificationService {

    @Override
    public void notify(User user, String message) {
        System.out.println("  [notify -> " + user.getEmail() + "] " + message);
    }
}
