module com.example.projektz {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.projektz to javafx.fxml;
    exports com.example.projektz;
}