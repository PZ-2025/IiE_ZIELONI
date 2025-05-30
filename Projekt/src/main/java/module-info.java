module com.example.projektzielonifx {
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires io;
    requires kernel;
    requires layout;
    requires java.prefs;
    requires jbcrypt;
    requires org.controlsfx.controls;
    requires RaportLibrary;


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
    exports com.example.projektzielonifx.reports;
    opens com.example.projektzielonifx.reports to javafx.fxml;
    exports com.example.projektzielonifx.notifications;
    opens com.example.projektzielonifx.notifications to javafx.fxml;
    exports com.example.projektzielonifx.settings;
    opens com.example.projektzielonifx.settings to javafx.fxml;
    exports com.example.projektzielonifx.newproject;
    opens com.example.projektzielonifx.newproject to javafx.fxml;
}