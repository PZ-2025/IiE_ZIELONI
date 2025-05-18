package com.example.projektzielonifx.notifications;

import com.example.projektzielonifx.InitializableWithId;
import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.Notification;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.collections.FXCollections;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.projektzielonifx.database.DBUtil.changeScene;

public class NotificationController implements InitializableWithId {
    @FXML
    private ListView<Notification> notificationsListView;

    @FXML
    private Button backButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button markAllReadButton;

    @FXML
    private ComboBox<String> filterComboBox;

    private int userId;
    private List<Notification> allNotifications;

    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;

        // Initialize filter combobox
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Today", "This Week", "Task Assigned", "Task Update", "Deadline", "Report Generated", "Other"
        ));
        filterComboBox.getSelectionModel().selectFirst();
        filterComboBox.setOnAction(event -> applyFilter());

        // Back button configuration
        backButton.setOnAction(event -> {
            changeScene(event, "/com/example/projektzielonifx/home/HomePage.fxml", "Home Page", userId, 700, 1000);
        });

        // Set up the custom cell factory
        notificationsListView.setCellFactory(param -> new NotificationListCell());

        // Load notifications
        loadNotifications();

        // Set up refresh button action
        refreshButton.setOnMouseClicked((MouseEvent event) -> loadNotifications());

        // Set up mark all as read button action
        markAllReadButton.setOnAction(event -> markAllAsRead());
    }

    private void loadNotifications() {
        allNotifications = DBUtil.getUnreadNotifications(userId);
        applyFilter();
    }

    private void applyFilter() {
        String filter = filterComboBox.getValue();
        List<Notification> filteredList;

        if (filter == null || filter.equals("All")) {
            filteredList = allNotifications;
        } else if (filter.equals("Today")) {
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            filteredList = allNotifications.stream()
                    .filter(n -> n.getCreatedAt().isAfter(today))
                    .collect(Collectors.toList());
        } else if (filter.equals("This Week")) {
            LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
            filteredList = allNotifications.stream()
                    .filter(n -> n.getCreatedAt().isAfter(weekStart))
                    .collect(Collectors.toList());
        } else {
            // Filter by type
            String type = getTypeFromDisplayName(filter);
            filteredList = allNotifications.stream()
                    .filter(n -> n.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        notificationsListView.getItems().setAll(filteredList);
    }

    private void markAllAsRead() {
        // Implement mark all as read functionality here
        // This would typically call a method to update the database
        // And then reload the notifications

        // TODO: Add database update call
        // DBUtil.markAllNotificationsAsRead(userId);

        // Reload notifications
        loadNotifications();
    }

    /**
     * Converts a display name like "Task Assigned" to the database type "zadanieprzypisane"
     * @param displayName The user-friendly display name
     * @return The corresponding database type value
     */
    private String getTypeFromDisplayName(String displayName) {
        if (displayName == null) return null;

        switch (displayName) {
            case "Task Assigned":
                return "zadanieprzypisane";
            case "Task Update":
                return "aktualizacjazadania";
            case "Deadline":
                return "deadline";
            case "Report Generated":
                return "generowanieraportu";
            case "Other":
                return "inne";
            default:
                return displayName;
        }
    }
}