module com.abalone {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.abalone to javafx.fxml;
    exports com.abalone;
}
