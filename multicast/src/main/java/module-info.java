module app.multicast {
    requires javafx.controls;
    requires javafx.fxml;

    opens app.multicast to javafx.fxml;
    opens app.multicast.client to javafx.fxml;
    opens app.multicast.fx to javafx.fxml;
    opens app.multicast.service to javafx.fxml;

    exports app.multicast;
    exports app.multicast.client;
    exports app.multicast.fx;
    exports app.multicast.service;
}