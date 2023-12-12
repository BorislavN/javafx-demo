package app.unicast.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneWrapper {
    private FXMLLoader loader;
    private Scene scene;
    private Stage stage;

    public SceneWrapper(FXMLLoader loader, Scene scene, Stage stage) {
        this.setLoader(loader);
        this.setScene(scene);
        this.setStage(stage);
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

    public Stage getStage() {
        return this.stage;
    }

    public void setStage(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("SceneContext error - stage is null!");
        }

        this.stage = stage;
    }
}
