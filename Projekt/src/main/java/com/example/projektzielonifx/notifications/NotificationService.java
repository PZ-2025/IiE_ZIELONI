package com.example.projektzielonifx.notifications;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.Notification;

import java.util.List;

public class NotificationService {
    private final NotificationManager notificationManager;

    public NotificationService() {
        this.notificationManager = new NotificationManager();
    }

    /**
     * Zwraca nieprzeczytane powiadomienia dla danego użytkownika.
     *
     * @param userId ID użytkownika
     * @return lista powiadomień
     */
    public List<Notification> getUnreadNotifications(int userId) {
        return DBUtil.getUnreadNotifications(userId);
    }
}
