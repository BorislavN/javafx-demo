package app.fxchat.unicast;

import app.fxchat.unicast.fx.ChatContext;
import app.fxchat.unicast.fx.Initializer;
import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.nio.Constants;
import javafx.application.Application;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ChatApp extends Application {
    @Override
    public void start(Stage stage) {
        ChatContext temp;

        try {
            temp = new ChatContext(Constants.HOST, Constants.PORT);
        } catch (IOException e) {
            temp = null;
        }

        ChatContext context = temp;
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
        if (context != null && context.getUsername() != null) {
            event.consume();

            context.enqueueMessage(ChatUtility.newQuitRequest());

            context.getSenderService().setOnSucceeded(this.close(context, stage));
            context.getSenderService().setOnFailed(this.close(context, stage));

            context.getSenderService().removeEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, this.close(context, stage));
            context.getSenderService().removeEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, this.close(context, stage));
        }
    }

    private EventHandler<WorkerStateEvent> close(ChatContext context, Stage stage) {
        return (e) -> {
            e.consume();

            if (context!=null){
                context.shutdown();
            }

            stage.close();
        };
    }
}