package app.unicast.fx;

import app.unicast.ChatApp;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class Initializer {
    public static void buildJoinScene( ChatContext context,Stage stage) {
        SceneWrapper sceneWrapper = Initializer.buildScene("join-view.fxml",stage);

        stage.setScene(sceneWrapper.getScene());

        JoinController controller = sceneWrapper.getLoader().getController();
        controller.setContext(context);

        if (!stage.isShowing()) {
            stage.show();
        }
    }

    public static Scene buildMainScene(ChatContext context,Stage stage) {
        SceneWrapper sceneWrapper = Initializer.buildScene("chat-view.fxml",stage);

        MainController controller = sceneWrapper.getLoader().getController();
        controller.setContext(context);

        return sceneWrapper.getScene();
    }

    public static ChatContext buildSettingsStage(ChatContext context) {
        Stage stage = Initializer.buildStage("Settings", Modality.APPLICATION_MODAL);
        SceneWrapper sceneWrapper = Initializer.buildScene("settings-view.fxml",stage);

        SettingsController controller = sceneWrapper.getLoader().getController();
        controller.setContext(context);

        stage.setX(600);
        stage.setY(250);

        stage.setScene(sceneWrapper.getScene());
        stage.showAndWait();

        return controller.getContext();
    }

    public static SceneWrapper buildDMStage(Stage parentStage, ChatContext context) {
        Stage stage = Initializer.buildStage("Direct Messages", Modality.NONE);
        SceneWrapper sceneWrapper = Initializer.buildScene("message-view.fxml",stage);

        sceneWrapper.getStage().initOwner(parentStage);
        sceneWrapper.getStage().setScene(sceneWrapper.getScene());

        sceneWrapper.getStage().setX(200);
        sceneWrapper.getStage().setY(200);

        MessageController controller = sceneWrapper.getLoader().getController();
        controller.setContext(stage, context);

        return sceneWrapper;
    }

    //Take care to provide a visible (not null) element
    public static Stage getStage(Node visibleElement) {
        return (Stage) visibleElement.getScene().getWindow();
    }

    private static SceneWrapper buildScene(String fxmlName,Stage stage) {
        FXMLLoader fxmlLoader = null;
        Scene scene = null;

        try {
            fxmlLoader = new FXMLLoader(ChatApp.class.getResource(fxmlName));
            scene = new Scene(fxmlLoader.load());

        } catch (IOException e) {
            System.err.println("Scene failed to load - " + e.getMessage());
        }

        return new SceneWrapper(fxmlLoader, scene,stage);
    }

    public static FadeTransition newButtonAnimation(Button button) {
        FadeTransition transition = new FadeTransition(Duration.seconds(2), button);
        transition.setFromValue(1);
        transition.setToValue(0.55);
        transition.setAutoReverse(true);
        transition.setCycleCount(Animation.INDEFINITE);

        return transition;
    }

    private static Stage buildStage(String title, Modality modality) {
        Stage stage = new Stage();
        stage.setTitle(title);

        stage.initModality(modality);

        return stage;
    }
}