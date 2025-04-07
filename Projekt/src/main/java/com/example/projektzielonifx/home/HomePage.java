package com.example.projektzielonifx.home;

import com.example.projektzielonifx.InitializableWithId;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Kontroler odpowiedzialny za obsługę głównej strony aplikacji.
 * Implementuje interfejsy InitializableWithId i Initializable, co umożliwia inicjalizację
 * zarówno z identyfikatorem użytkownika jak i standardową inicjalizację JavaFX.
 */
public class HomePage implements InitializableWithId, Initializable {

    /**
     * Identyfikator zalogowanego użytkownika.
     */
    private int userId;

    /**
     * Kontroler panelu nawigacyjnego.
     */
    @FXML
    private NavController navPanelController;

    /**
     * Kontroler głównej zawartości strony.
     */
    @FXML
    private MainContentController mainContentController;

    /**
     * Inicjalizuje kontroler z identyfikatorem użytkownika.
     * Metoda wywoływana po zmianie sceny z przekazaniem ID użytkownika.
     *
     * @param userId Identyfikator zalogowanego użytkownika
     */
    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;
        loadUserData(); // Load additional data as needed
    }

    /**
     * Inicjalizuje kontroler po całkowitym załadowaniu interfejsu.
     * Przekazuje dane do kontrolerów potomnych.
     *
     * @param location Lokalizacja używana do rozwiązywania ścieżek względnych, lub null jeśli lokalizacja jest nieznana
     * @param resources Zasoby używane do lokalizacji, lub null jeśli element root nie został zlokalizowany
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Pass data to child controllers
        navPanelController.initData(userId);
        mainContentController.initData(userId);
    }

    /**
     * Ładuje dane użytkownika na podstawie jego identyfikatora.
     * Metoda pomocnicza używana do pobrania i wyświetlenia danych specyficznych dla zalogowanego użytkownika.
     */
    private void loadUserData() {
        // Example: Load user-specific data using the ID
        // String username = DBUtil.getUsernameById(userId);
        // String role = DBUtil.getUserRoleById(userId);
        // Update UI accordingly
    }
}
