module app.fxchat {
    requires javafx.controls;
    requires javafx.fxml;

    opens app.fxchat.multicast to javafx.fxml;
    opens app.fxchat.unicast to javafx.fxml;

    exports app.fxchat.multicast;
    exports app.fxchat.unicast;
}