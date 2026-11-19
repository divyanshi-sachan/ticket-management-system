package com.ticketsystem.notification;

import com.ticketsystem.model.User;

/** One small method, so any channel (console, email, SMS) is easy to add. */
public interface NotificationService {
    void notify(User user, String message);
}
