module app.chat.fxchat {
    requires javafx.controls;
    requires javafx.fxml;

    opens app.chat.fxchat to javafx.fxml;

    exports app.chat.fxchat;
}