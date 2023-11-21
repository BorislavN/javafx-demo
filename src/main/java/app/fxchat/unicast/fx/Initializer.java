package app.fxchat.unicast.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Initializer {
    public static SceneWrapper buildScene(Class<? extends Application> appClass, String fxmlName) {
        FXMLLoader fxmlLoader = null;
        Scene scene = null;

        try {
            fxmlLoader = new FXMLLoader(appClass.getResource(fxmlName));
            scene = new Scene(fxmlLoader.load());

        } catch (IOException e) {
            System.err.println("Scene failed to load - " + e.getMessage());
        }

        return new SceneWrapper(fxmlLoader, scene);
    }

    public static Stage buildStage(String title, Modality modality) {
        Stage stage = new Stage();
        stage.setTitle(title);

        stage.initModality(modality);

        return stage;
    }

    //Take care to provide a visible (ot null) element
    public static Stage getStage(Node visibleElement) {
        return (Stage) visibleElement.getScene().getWindow();
    }
}