module app.fxchat {
    requires javafx.controls;
    requires javafx.fxml;

    opens app.fxchat to javafx.fxml;
    opens app.fxchat.multicast to javafx.fxml;

    exports app.fxchat;
    exports app.fxchat.multicast;
}