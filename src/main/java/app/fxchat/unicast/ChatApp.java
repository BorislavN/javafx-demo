package app.fxchat.unicast;

import app.fxchat.unicast.fx.ChatContext;
import app.fxchat.unicast.fx.Initializer;
import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.nio.Constants;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApp extends Application {
    @Override
    public void start(Stage stage) {
        ChatContext context = null;

        try {
            context = new ChatContext(Constants.HOST, Constants.PORT);
        } catch (IOException e) {
            ChatUtility.printAsException("Context failed initialization!");
        }

        stage.setTitle("Chat Client");
        Initializer.buildJoinScene(stage, context);
    }

    public static void main(String[] args) {
        launch(args);
    }
}