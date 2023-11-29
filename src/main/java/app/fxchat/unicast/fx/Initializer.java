package app.fxchat.unicast.fx;

import app.fxchat.unicast.ChatApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Initializer {
    public static Scene buildJoinScene(ChatContext context) {
        SceneWrapper sceneWrapper = Initializer.buildScene("join-view.fxml");

        JoinController controller = sceneWrapper.getLoader().getController();
        controller.setContext(context);

        return sceneWrapper.getScene();
    }

    public static Scene buildMainScene(ChatContext context) {
        SceneWrapper sceneWrapper = Initializer.buildScene("chat-view.fxml");

        MainController controller = sceneWrapper.getLoader().getController();
        controller.setContext(context);

        return sceneWrapper.getScene();
    }

    public static ChatContext buildSettingsStage(ChatContext context) {
        Stage stage = Initializer.buildStage("Settings", Modality.APPLICATION_MODAL);
        SceneWrapper sceneWrapper = Initializer.buildScene("settings-view.fxml");

        SettingsController controller = sceneWrapper.getLoader().getController();
        controller.setContext(context);

        stage.setX(600);
        stage.setY(250);

        stage.setScene(sceneWrapper.getScene());
        stage.showAndWait();

        return controller.getContext();
    }

    public static void showDMStage(Stage parentStage, ChatContext context) {
        Stage stage = Initializer.buildStage("Direct Messages", Modality.NONE);
        SceneWrapper sceneWrapper = Initializer.buildScene("message-view.fxml");

        stage.initOwner(parentStage);
        stage.setScene(sceneWrapper.getScene());

        stage.setX(200);
        stage.setY(200);

        MessageController controller = sceneWrapper.getLoader().getController();
        controller.setContext(stage, context);
    }

    //Take care to provide a visible (ot null) element
    public static Stage getStage(Node visibleElement) {
        return (Stage) visibleElement.getScene().getWindow();
    }

    private static SceneWrapper buildScene(String fxmlName) {
        FXMLLoader fxmlLoader = null;
        Scene scene = null;

        try {
            fxmlLoader = new FXMLLoader(ChatApp.class.getResource(fxmlName));
            scene = new Scene(fxmlLoader.load());

        } catch (IOException e) {
            System.err.println("Scene failed to load - " + e.getMessage());
        }

        return new SceneWrapper(fxmlLoader, scene);
    }

    private static Stage buildStage(String title, Modality modality) {
        Stage stage = new Stage();
        stage.setTitle(title);

        stage.initModality(modality);

        return stage;
    }
}