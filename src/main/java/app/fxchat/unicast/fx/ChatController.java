package app.fxchat.unicast.fx;

import app.fxchat.unicast.ChatApp;
import app.fxchat.unicast.nio.ChatClient;
import app.fxchat.unicast.nio.ChatUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ChatController {
    @FXML
    private Label joinPageError, announcementMessage;
    @FXML
    private TextField usernameInput, messageInput;
    @FXML
    private Button joinBtn, backBtn, sendBtn;
    @FXML
    private TextArea chatArea;
    private ChatClient client;

    public void onEnter(ActionEvent event) {
        event.consume();

        String targetId = ((Node) event.getTarget()).getId();

        if ("usernameInput".equals(targetId)) {
            this.joinBtn.fire();
        }

        if ("messageInput".equals(targetId)) {
            this.sendBtn.fire();
        }
    }

    public void onJoin(ActionEvent event) {
        event.consume();

        String username = this.usernameInput.getText();
        System.out.println(ChatUtility.newJoinRequest(username));
        this.client.sendMessage(ChatUtility.newJoinRequest(username));
        String response = this.client.receiveMessage();

        System.out.println(response);

//        SceneContext sceneContext = Initializer.buildScene(ChatApp.class, "chat-view.fxml");
//
//        Stage stage = Initializer.getStage(this.joinBtn);
//        stage.setScene(sceneContext.getScene());
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
        //TODO: ???
    }

    public void onChangeName(ActionEvent event) {
        event.consume();

        SceneContext sceneContext = Initializer.buildScene(ChatApp.class, "join-view.fxml");

        Stage stage = Initializer.getStage(this.backBtn);
        stage.setScene(sceneContext.getScene());
    }

    public void onShowMessages(ActionEvent event) {
        event.consume();

        Stage stage = Initializer.buildStage("Direct Messages", Modality.NONE);
        SceneContext sceneContext = Initializer.buildScene(ChatApp.class, "message-view.fxml");

        stage.setX(200);
        stage.setY(200);

        stage.setScene(sceneContext.getScene());
        stage.show();
    }

    public void onClose(WindowEvent event, Stage stage) {
        event.consume();

        if (this.client != null) {
            this.client.sendMessage(ChatUtility.newQuitRequest());
        }

        if (this.client != null) {
            this.client.shutdown();
        }

        stage.close();
    }

    public void configureClient() {
        try {
            this.client = new ChatClient();
        } catch (IOException e) {
           ChatClient.printException("Client initialization failed",e);
        }
    }
}