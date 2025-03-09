module com.abalone {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.abalone.controller to javafx.fxml;
    opens com.abalone.model to javafx.fxml;
    opens com.abalone.view to javafx.fxml;
    opens com.abalone.model.utils to javafx.fxml;
    opens com.abalone.model.utils.Players to javafx.fxml;


    exports com.abalone.controller;
    exports com.abalone.model;
    exports com.abalone.view;
    exports com.abalone.model.utils;
    exports com.abalone.model.utils.Players;
}
