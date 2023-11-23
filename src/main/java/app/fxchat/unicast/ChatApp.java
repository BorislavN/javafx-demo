package app.fxchat.unicast;

import app.fxchat.unicast.fx.ChatContext;
import app.fxchat.unicast.fx.Initializer;
import app.fxchat.unicast.nio.ChatUtility;
import javafx.application.Application;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ChatApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        ChatContext context = new ChatContext();

        Scene scene = Initializer.buildJoinScene(context);
        stage.setOnCloseRequest((event -> this.onClose(event, stage, context)));

        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void onClose(WindowEvent event, Stage stage, ChatContext context) {
        if (context.getUsername() != null) {
            event.consume();

            context.enqueueMessage(ChatUtility.newQuitRequest());

            context.getSenderService().setOnSucceeded(close(context, stage));
            context.getSenderService().setOnFailed(close(context, stage));

            context.getSenderService().removeEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, this.close(context, stage));
            context.getSenderService().removeEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, this.close(context, stage));
        }
    }

    private EventHandler<WorkerStateEvent> close(ChatContext context, Stage stage) {
        return (e) -> {
            context.shutdown();
            stage.close();
        };
    }
}