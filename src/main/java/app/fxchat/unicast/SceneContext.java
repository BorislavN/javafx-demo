package app.fxchat.unicast;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class SceneContext {
    private FXMLLoader loader;
    private Scene scene;

    public SceneContext(FXMLLoader loader, Scene scene) {
        this.setLoader(loader);
        this.setScene(scene);
    }

    public FXMLLoader getLoader() {
        return this.loader;
    }

    private void setLoader(FXMLLoader loader) throws IllegalArgumentException {
        if (loader == null) {
            throw new IllegalArgumentException("SceneContext error - loader is null!");
        }

        this.loader = loader;
    }

    public Scene getScene() {
        return this.scene;
    }

    private void setScene(Scene scene) throws IllegalArgumentException {
        if (scene == null) {
            throw new IllegalArgumentException("SceneContext error - scene is null!");
        }

        this.scene = scene;
    }
}
