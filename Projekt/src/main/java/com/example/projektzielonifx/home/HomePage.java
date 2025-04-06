package com.example.projektzielonifx.home;
import com.example.projektzielonifx.InitializableWithId;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;


public class HomePage implements InitializableWithId, Initializable {
    private int userId;
    @FXML private NavController navPanelController;
    @FXML private MainContentController mainContentController;


    @Override
    public void initializeWithId(int userId) {
        this.userId = userId;
        loadUserData();  // Load additional data as needed
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Pass data to child controllers
        navPanelController.initData(userId);
        mainContentController.initData(userId);
    }






    private void loadUserData() {
        // Example: Load user-specific data using the ID
//        String username = DBUtil.getUsernameById(userId);
//        String role = DBUtil.getUserRoleById(userId);
        // Update UI accordingly
    }
}
