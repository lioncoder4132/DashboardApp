module com.example.dashboardapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.dashboardapp to javafx.fxml;
    exports com.example.dashboardapp;
}