module com.example.projektz {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.projektz to javafx.fxml;
    exports com.example.projektz;
}