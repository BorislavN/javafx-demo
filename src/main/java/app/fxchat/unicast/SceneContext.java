package app.fxchat.unicast;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class SceneContext {
    private final FXMLLoader loader;
    private final Scene scene;

    public SceneContext(FXMLLoader loader, Scene scene) {
        this.loader = loader;
        this.scene = scene;
    }

    public FXMLLoader getLoader() {
        return this.loader;
    }

    public Scene getScene() {
        return this.scene;
    }
}
