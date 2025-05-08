package com.example.projektzielonifx.tasks;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

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
    private VBox taskRoot;

    /**
     * Etykieta wyświetlająca tytuł zadania.
     */
    @FXML
    private Label titleLabel;

    /**
     * Etykieta wyświetlająca opis zadania.
     */
    @FXML
    private Label descriptionLabel;

    /**
     * Etykieta wyświetlająca priorytet zadania.
     */
    @FXML
    private Label priorityLabel;

    /**
     * Etykieta wyświetlająca postęp zadania.
     */
    @FXML
    private Label progressLabel;

    /**
     * Przycisk umożliwiający edycję zadania.
     */
    @FXML
    private Button editButton;

    /**
     * Tworzy nową ramkę zadania z określonymi wartościami.
     * Ładuje układ z pliku FXML i ustawia wartości oraz style na podstawie przekazanych parametrów.
     *
     * @param title Tytuł zadania
     * @param description Opis zadania
     * @param priority Priorytet zadania (low, medium, high)
     * @param progress Status postępu zadania
     * @throws RuntimeException gdy nie udaje się załadować pliku FXML
     */
    public TaskFrame(String title, String description, String priority, String progress) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TaskFrame.fxml"));
        loader.setController(this);
        try {
            VBox root = loader.load();
            this.getChildren().add(taskRoot);

            // Set values
            titleLabel.setText(title);
            descriptionLabel.setText(description);
            priorityLabel.setText(priority);
            progressLabel.setText(progress);
            // Set style based on priority
            String backgroundColor = getPriorityColor(priority);
            taskRoot.setStyle(taskRoot.getStyle() +
                    "-fx-background-color: rgba(" + backgroundColor + ", 0.3); " +
                    "-fx-border-color: rgba(" + backgroundColor + ");");

            editButton.setOnAction(event -> openEditTask());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void openEditTask() {
        try {
            // Load the FXML for the edit task window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditTask.fxml"));

            // Create an instance of the controller
            EditTask editTaskController = new EditTask();
            loader.setController(editTaskController);

            // Load the FXML file
            Parent root = loader.load();

            // Create a new stage for the popup
            Stage popupStage = new Stage();
            popupStage.setTitle("Edit Task");
            popupStage.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
            popupStage.initOwner(editButton.getScene().getWindow()); // Set the parent window

            // Set the scene
            Scene scene = new Scene(root);
            popupStage.setScene(scene);

            // Show the popup
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Zwraca kod koloru RGB odpowiadający priorytetowi zadania.
     * Używany do wizualnego rozróżnienia zadań o różnych priorytetach w interfejsie.
     *
     * @param priority Priorytet zadania (low, medium, high)
     * @return Ciąg znaków reprezentujący wartości RGB odpowiadające priorytetowi
     */
    private String getPriorityColor(String priority) {
        switch(priority.toLowerCase()) {
            case "low": return "193, 255, 114";
            case "medium": return "255, 224, 102";
            case "high": return "255, 107, 107";
            default: return "240, 240, 240";
        }
    }
}
