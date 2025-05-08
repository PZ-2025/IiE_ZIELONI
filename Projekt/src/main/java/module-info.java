module com.example.projektzielonifx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens com.example.projektzielonifx to javafx.fxml;
    exports com.example.projektzielonifx;
    exports com.example.projektzielonifx.database;
    opens com.example.projektzielonifx.database to javafx.fxml;
    exports com.example.projektzielonifx.auth;
    opens com.example.projektzielonifx.auth to javafx.fxml;
    exports com.example.projektzielonifx.home;
    opens com.example.projektzielonifx.home to javafx.fxml;
    exports com.example.projektzielonifx.userstab;
    opens com.example.projektzielonifx.userstab to javafx.fxml;
    opens com.example.projektzielonifx.models to javafx.base;
    opens com.example.projektzielonifx.tasks to javafx.fxml;
    exports com.example.projektzielonifx.ProjInfo;
    opens com.example.projektzielonifx.ProjInfo to javafx.fxml;
}