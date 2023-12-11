module app.unicast {
    requires javafx.controls;
    requires javafx.fxml;

    opens app.unicast to javafx.fxml;
    opens app.unicast.fx to javafx.fxml;
    opens app.unicast.service to javafx.fxml;
    opens app.unicast.nio to javafx.fxml;

    exports app.unicast;
    exports app.unicast.fx;
    exports app.unicast.service;
    exports app.unicast.nio;
}