package app.fxchat.unicast;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Initializer {
    //TODO: this class will provide static methods for constructing the popup windows for
    // "settings" and "direct messages" windows

    public static SceneContext buildScene(Class<? extends Application> appClass, String fxmlName) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(appClass.getResource(fxmlName));
        Scene scene = new Scene(fxmlLoader.load());

        return new SceneContext(fxmlLoader, scene);
    }

    public static Stage buildStage(String title, Modality modality) {
        Stage stage = new Stage();
        stage.setTitle(title);

        stage.initModality(modality);

        return stage;
    }

    public static Stage getStage(Node visibleElement) {
        return (Stage) visibleElement.getScene().getWindow();
    }
}