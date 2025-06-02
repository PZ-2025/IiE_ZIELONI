package com.example.projektzielonifx.tasks;

import com.example.projektzielonifx.database.DBUtil;
import com.example.projektzielonifx.models.TaskModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Klasa reprezentująca komponent interfejsu użytkownika dla pojedynczego zadania.
 * Wyświetla informacje o zadaniu, takie jak tytuł, opis, priorytet i postęp.
 * Dziedziczy po VBox, co pozwala na łatwe dodawanie do kontenerów w interfejsie.
 */
public class TaskFrame extends VBox {

    /**
     * Główny kontener przechowujący elementy interfejsu dla zadania.
     */
    @FXML
    protected VBox taskRoot;

    /**
     * Etykieta wyświetlająca tytuł zadania.
     */
    @FXML
    protected Label titleLabel;

    /**
     * Etykieta wyświetlająca opis zadania.
     */
    @FXML
    protected Label descriptionLabel;

    /**
     * Etykieta wyświetlająca priorytet zadania.
     */
    @FXML
    protected Label priorityLabel;

    /**
     * Etykieta wyświetlająca postęp zadania.
     */
    @FXML
    protected Label statusLabel;

    /**
     * Separator, którego kolor zależy od priorytetu
     */
    @FXML
    protected Separator prioritySeparator;

    /**
     * Przycisk umożliwiający edycję zadania.
     */
    @FXML
    protected Button editButton;

    @FXML
    protected Label dateLabel;
    @FXML
    protected Label assignedLabel;
    @FXML
    protected HBox assignedBox;
    protected Integer taskId;
    protected Integer userId;
    protected final Map<String, String> priorityDbToDisplay = new HashMap<>();
    protected final Map<String, String> statusDbToDisplay = new HashMap<>();
    /**
     * Tworzy nową ramkę zadania z określonymi wartościami.
     * Ładuje układ z pliku FXML i ustawia wartości oraz style na podstawie przekazanych parametrów.
     *
     * @param title       Tytuł zadania
     * @param description Opis zadania
     * @param priority    Priorytet zadania (low, medium, high)
     * @param status      Status postępu zadania
     * @param deadline
     * @throws RuntimeException gdy nie udaje się załadować pliku FXML
     */
    public TaskFrame(String id,String title, String description, String priority, String status, String deadline, String assignedTo, Integer userId) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TaskFrame.fxml"));
        loader.setController(this);
        taskId = Integer.valueOf(id);
        this.userId = userId;
        // Priority mappings (database to display)
        priorityDbToDisplay.put("niski", "Niski");
        priorityDbToDisplay.put("sredni", "Średni");
        priorityDbToDisplay.put("wysoki", "Wysoki");
        // Status mappings (database to display)
        statusDbToDisplay.put("doZrobienia", "Do zrobienia");
        statusDbToDisplay.put("wTrakcie", "W trakcie");
        statusDbToDisplay.put("zrobione", "Zakończone");
        statusDbToDisplay.put("anulowane", "Wstrzymane");

        try {
            VBox root = loader.load();
            this.getChildren().add(root);
            // Set values
            titleLabel.setText(title);
            descriptionLabel.setText(description);
            priorityLabel.setText(getPriorityDisplayValue(priority));
            statusLabel.setText(getStatusDisplayValue(status));
            dateLabel.setText(deadline);
            assignedLabel.setText(assignedTo);

            if(Objects.equals(assignedTo, "")) {
                assignedBox.setVisible(false);
            }
            // Apply priority-based styles
            String rgbColor = getPriorityRGB(priority);

            // Set title color based on priority
            titleLabel.setStyle("-fx-text-fill: rgba(" + rgbColor + ", 0.9);");

            // Set separator style based on priority
            prioritySeparator.setStyle("-fx-background: rgba(" +rgbColor + ",1);");

            // Set a subtle left border indicating priority
            taskRoot.setStyle(taskRoot.getStyle() +
                    " -fx-border-color: rgba(" + rgbColor + ", 0.7);" +
                    " -fx-border-width: 4;");

            editButton.setOnAction(event -> openEditTask());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Konwertuje wartość priorytetu z bazy danych na wartość wyświetlaną
     */
    public String getPriorityDisplayValue(String dbValue) {
        return priorityDbToDisplay.getOrDefault(dbValue, dbValue);
    }

    /**
     * Konwertuje wartość statusu z bazy danych na wartość wyświetlaną
     */
    public String getStatusDisplayValue(String dbValue) {
        return statusDbToDisplay.getOrDefault(dbValue, dbValue);
    }

    protected void openEditTask() {
        DBUtil.changeSceneWithTaskId(editButton, "/com/example/projektzielonifx/tasks/EditTask.fxml",
                "Edytuj Zadanie", userId, taskId, 700, 800);
    }

    /**
     * Zwraca kod koloru RGB odpowiadający priorytetowi zadania.
     * Używany do wizualnego rozróżnienia zadań o różnych priorytetach w interfejsie.
     *
     * @param priority Priorytet zadania (low, medium, high)
     * @return Ciąg znaków reprezentujący wartości RGB odpowiadające priorytetowi
     */
    protected String getPriorityRGB(String priority) {
        switch(priority.toLowerCase()) {
            case "niski": return "84, 209, 89";
            case "sredni": return "255, 224, 102";
            case "średni": return "255, 224, 102";
            case "wysoki": return "255, 107, 107";
            default: return "200, 200, 200";
        }
    }

}