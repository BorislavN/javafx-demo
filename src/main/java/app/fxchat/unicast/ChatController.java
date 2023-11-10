package app.fxchat.unicast;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ChatController {
    @FXML
    public Label joinPageError;
    @FXML
    public TextField usernameInput;
    @FXML
    public Button joinBtn;

    public void onEnter(ActionEvent event) {
    }

    public void onJoin(ActionEvent event) {
        Stage stage = Initializer.getStage(this.joinBtn);
        try {
            SceneContext sceneContext = Initializer.buildScene(ChatApp.class, "chat-view.fxml");
            stage.setScene(sceneContext.getScene());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void showSettings(ActionEvent event) {
    }

    public void onSend(ActionEvent event) {
    }

    public void onChangeName(ActionEvent event) {
    }

    public void onClose(WindowEvent event, Stage stage) {
    }

    public void configureClient() {
    }
}
