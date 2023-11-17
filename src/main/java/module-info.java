module app.fxchat {
    requires javafx.controls;
    requires javafx.fxml;

    //Multicast
    opens app.fxchat.multicast to javafx.fxml;
    opens app.fxchat.multicast.client to javafx.fxml;
    opens app.fxchat.multicast.fx to javafx.fxml;
    opens app.fxchat.multicast.service to javafx.fxml;

    exports app.fxchat.multicast;
    exports app.fxchat.multicast.client;
    exports app.fxchat.multicast.fx;
    exports app.fxchat.multicast.service;


    //Unicast
    opens app.fxchat.unicast to javafx.fxml;
    opens app.fxchat.unicast.fx to javafx.fxml;
    opens app.fxchat.unicast.service to javafx.fxml;
    opens app.fxchat.unicast.nio to javafx.fxml;

    exports app.fxchat.unicast;
    exports app.fxchat.unicast.fx;
    exports app.fxchat.unicast.service;
    exports app.fxchat.unicast.nio;
}