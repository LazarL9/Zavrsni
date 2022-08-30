module com.example.zavrsniraddropbox {
    requires javafx.controls;
    requires javafx.fxml;
    requires dropbox.core.sdk;


    opens com.zavrsniraddropbox to javafx.fxml;
    exports com.zavrsniraddropbox;
    exports com.zavrsniraddropbox.service;
    opens com.zavrsniraddropbox.service to javafx.fxml;
    exports com.zavrsniraddropbox.controller;
    opens com.zavrsniraddropbox.controller to javafx.fxml;
}