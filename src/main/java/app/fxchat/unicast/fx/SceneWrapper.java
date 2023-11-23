package app.fxchat.unicast.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class SceneWrapper {
    private FXMLLoader loader;
    private Scene scene;

    public SceneWrapper(FXMLLoader loader, Scene scene) {
        this.setLoader(loader);
        this.setScene(scene);
    }

    public FXMLLoader getLoader() {
        return this.loader;
    }

    private void setLoader(FXMLLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException("SceneContext error - loader is null!");
        }

        this.loader = loader;
    }

    public Scene getScene() {
        return this.scene;
    }

    private void setScene(Scene scene) {
        if (scene == null) {
            throw new IllegalArgumentException("SceneContext error - scene is null!");
        }

        this.scene = scene;
    }
}
