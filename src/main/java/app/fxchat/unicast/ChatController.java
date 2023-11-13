package app.fxchat.unicast;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ChatController {
    @FXML
    public Label joinPageError;
    @FXML
    public TextField usernameInput;
    @FXML
    public Button joinBtn, backBtn;

    public void onEnter(ActionEvent event) {
    }

    public void onJoin(ActionEvent event) {
        event.consume();

        SceneContext sceneContext = Initializer.buildScene(ChatApp.class, "chat-view.fxml");

        Stage stage = Initializer.getStage(this.joinBtn);
        stage.setScene(sceneContext.getScene());
    }

    public void showSettings(ActionEvent event) {
        event.consume();

        Stage stage = Initializer.buildStage("Settings", Modality.APPLICATION_MODAL);
        SceneContext sceneContext = Initializer.buildScene(ChatApp.class, "settings-view.fxml");

        stage.setX(600);
        stage.setY(250);

        stage.setScene(sceneContext.getScene());
        stage.showAndWait();
    }

    public void onSend(ActionEvent event) {
    }

    public void onChangeName(ActionEvent event) {
        event.consume();

        SceneContext sceneContext = Initializer.buildScene(ChatApp.class, "join-view.fxml");

        Stage stage = Initializer.getStage(this.backBtn);
        stage.setScene(sceneContext.getScene());
    }

    public void onClose(WindowEvent event, Stage stage) {
    }

    public void configureClient() {
    }

    public void onShowMessages(ActionEvent event) {
    }
}
