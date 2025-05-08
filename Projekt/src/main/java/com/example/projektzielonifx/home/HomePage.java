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
public class HomePage implements InitializableWithId {

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
        navPanelController.initData(userId);
        mainContentController.initData(userId);
    }
}
