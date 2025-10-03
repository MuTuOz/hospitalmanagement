module com.cs320 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.cs320 to javafx.fxml;

    exports com.cs320;
}
