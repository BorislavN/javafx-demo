package app.fxchat.unicast;

import app.fxchat.unicast.fx.ChatContext;
import app.fxchat.unicast.fx.Initializer;
import app.fxchat.unicast.fx.JoinController;
import app.fxchat.unicast.fx.SceneWrapper;
import javafx.application.Application;
import javafx.stage.Stage;

public class ChatApp extends Application {
    @Override
    public void start(Stage stage) {
        SceneWrapper sceneWrapper = Initializer.buildScene(getClass(), "join-view.fxml");

        JoinController controller = sceneWrapper.getLoader().getController();
        ChatContext context = new ChatContext();

        stage.setOnCloseRequest((event -> controller.onClose(event, stage)));
        controller.setContext(context);

        stage.setTitle("Chat Client");
        stage.setScene(sceneWrapper.getScene());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}