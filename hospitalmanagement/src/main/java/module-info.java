module com.cs320 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.cs320 to javafx.fxml;
    exports com.cs320;
}
