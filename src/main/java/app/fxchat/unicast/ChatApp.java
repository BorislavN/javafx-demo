package app.fxchat.unicast;

import app.fxchat.unicast.fx.ChatContext;
import app.fxchat.unicast.fx.Initializer;
import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.nio.Constants;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

        Scene scene = Initializer.buildJoinScene(context);
        stage.setOnCloseRequest(this.cleanup(context, stage));

        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private EventHandler<WindowEvent> cleanup(ChatContext context, Stage stage) {
        return event -> {
            event.consume();

            if (context != null && context.getUsername() != null) {
                context.enqueueMessage(ChatUtility.newQuitRequest());

                context.getSenderService().setOnSucceeded((e) -> close(context, stage));
                context.getSenderService().setOnFailed((e) -> close(context, stage));
            }
        };
    }

    private void close(ChatContext context, Stage stage) {
        context.shutdown();
        stage.close();
    }
}