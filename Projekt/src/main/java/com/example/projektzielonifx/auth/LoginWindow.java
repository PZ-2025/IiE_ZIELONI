package com.example.projektzielonifx.auth;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Główna klasa aplikacji odpowiedzialna za uruchomienie okna logowania.
 * Dziedziczy po klasie Application z JavaFX.
 */
public class LoginWindow extends Application {

    /**
     * Uruchamia aplikację JavaFX, ładując plik FXML dla okna logowania,
     * konfigurując scenę i wyświetlając ją użytkownikowi.
     *
     * @param stage Główna scena dla tej aplikacji.
     * @throws IOException Jeśli plik FXML nie może zostać załadowany.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                LoginWindow.class.getResource("/com/example/projektzielonifx/auth/LoginWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("GreenTask - Login");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Główny punkt wejścia do aplikacji.
     * Uruchamia środowisko JavaFX i okno logowania.
     *
     * @param args Argumenty wiersza poleceń przekazane do aplikacji.
     */
    public static void main(String[] args) {
        launch();
    }
}
