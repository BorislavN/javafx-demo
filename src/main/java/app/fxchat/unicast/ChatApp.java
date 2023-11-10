package app.fxchat.unicast;

import javafx.application.Application;
import javafx.stage.Stage;

public class ChatApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        SceneContext sceneContext = Initializer.buildScene(getClass(), "join-view.fxml");

//        ChatController controller = sceneContext.getLoader().getController();
//
//        stage.setOnCloseRequest((event -> controller.onClose(event, stage)));
//        controller.configureClient();

        stage.setTitle("Chat Client");
        stage.setScene(sceneContext.getScene());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}