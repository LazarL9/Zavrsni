module com.example.zavrsniraddropbox {
    requires javafx.controls;
    requires javafx.fxml;
    requires dropbox.core.sdk;


    opens com.zavrsniraddropbox to javafx.fxml;
    exports com.zavrsniraddropbox;
}