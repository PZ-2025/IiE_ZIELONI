package com.example.projektzielonifx.notifications;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.Notification;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import java.time.format.DateTimeFormatter;

public class NotificationListCell extends ListCell<Notification> {
    private final VBox content;
    private final Label titleLabel;
    private final Label messageLabel;
    private final Label dateLabel;
    private final Circle statusIndicator;
    private final Button markReadButton;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm");

    public NotificationListCell() {
        // Create container for content
        content = new VBox();
        content.setSpacing(4);
        content.setPadding(new Insets(10));
        content.getStyleClass().add("info-card");

        // Top row with title and date
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setSpacing(10);

        // Status indicator
        statusIndicator = new Circle(6);

        // Title label
        titleLabel = new Label();
        titleLabel.getStyleClass().add("info-label");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Date label
        dateLabel = new Label();
        dateLabel.getStyleClass().add("info-value");
        dateLabel.setStyle("-fx-opacity: 0.8; -fx-font-size: 12px;");

        topRow.getChildren().addAll(statusIndicator, titleLabel, spacer, dateLabel);

        // Message label
        messageLabel = new Label();
        messageLabel.getStyleClass().add("task-description");
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(5, 0, 5, 24));

        // Bottom row with action buttons
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER_RIGHT);
        bottomRow.setSpacing(10);

        markReadButton = new Button("Mark as Read");
        markReadButton.getStyleClass().add("edit-button");
        markReadButton.setOnAction(event -> {
            DBUtil.markAsRead(getItem().getId());
            getItem().setRead(true);
            updateItem(getItem(), false);
        });

        bottomRow.getChildren().add(markReadButton);

        // Add all components to the content VBox
        content.getChildren().addAll(topRow, messageLabel, bottomRow);
    }

    @Override
    protected void updateItem(Notification item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            // Update status indicator color based on notification type
            switch (item.getType().toLowerCase()) {
                case "zadanieprzypisane":
                    statusIndicator.setStyle("-fx-fill: #5468FF;"); // Blue
                    break;
                case "aktualizacjazadania":
                    statusIndicator.setStyle("-fx-fill: #54D36D;"); // Green
                    break;
                case "deadline":
                    statusIndicator.setStyle("-fx-fill: #FF5454;"); // Red
                    break;
                case "generowanieraportu":
                    statusIndicator.setStyle("-fx-fill: #8A54FF;"); // Purple
                    break;
                case "inne":
                default:
                    statusIndicator.setStyle("-fx-fill: #FFB954;"); // Orange/Yellow
                    break;
            }

            // Set the content of the labels
            titleLabel.setText(getFormattedType(item.getType()));
            messageLabel.setText(item.getMessage());
            dateLabel.setText(item.getCreatedAt().format(formatter));

            // Show/hide mark as read button based on read status
            markReadButton.setVisible(!item.isRead());

            // Set the graphic to our custom content
            setText(null);
            setGraphic(content);
        }
    }

    /**
     * Formats the notification type to a more user-friendly display format
     * @param type The raw notification type from the database
     * @return A formatted, readable type label
     */
    private String getFormattedType(String type) {
        if (type == null) return "Notification";

        switch (type.toLowerCase()) {
            case "zadanieprzypisane":
                return "Task Assigned";
            case "aktualizacjazadania":
                return "Task Update";
            case "deadline":
                return "Deadline";
            case "generowanieraportu":
                return "Report Generated";
            case "inne":
                return "Other";
            default:
                return type;
        }
    }
}