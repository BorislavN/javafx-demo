package app.fxchat.unicast;

import app.fxchat.unicast.fx.ChatController;
import app.fxchat.unicast.fx.Initializer;
import app.fxchat.unicast.fx.SceneContext;
import javafx.application.Application;
import javafx.stage.Stage;

public class ChatApp extends Application {
    @Override
    public void start(Stage stage)   {
        SceneContext sceneContext = Initializer.buildScene(getClass(), "join-view.fxml");

        ChatController controller = sceneContext.getLoader().getController();

        stage.setOnCloseRequest((event -> controller.onClose(event, stage)));
        controller.configureClient();

        stage.setTitle("Chat Client");
        stage.setScene(sceneContext.getScene());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}