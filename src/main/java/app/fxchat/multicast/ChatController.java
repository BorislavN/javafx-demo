package app.fxchat.multicast;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ChatController {
    @FXML
    private VBox usernamePage;
    @FXML
    private Label errorMessage;
    @FXML
    private TextField usernameInput;
    @FXML
    private Button joinBtn;
    @FXML
    private VBox mainPage;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField messageInput;
    private String username;
    private MulticastClient client;

    public ChatController() {
        this.username = "";
        this.client = null;
    }

    @FXML
    public void onJoinClick(ActionEvent actionEvent) {
        actionEvent.consume();

        String username = this.usernameInput.getText();

        if (username.isBlank() || username.length() > MulticastClient.USERNAME_LIMIT) {
            this.errorMessage.setVisible(true);
            return;
        }

        if ("".equals(this.username)) {
            this.client.sendMessage(String.format("%s joined the chat!", username));

            //Start listening for messages
            this.client.listenForMessages(this.textArea);

        } else if (!username.equals(this.username)) {
            this.client.sendMessage(String.format("%s changed their name to %s", this.username, username));
        }

        this.username = username;

        this.errorMessage.setVisible(false);
        this.usernamePage.setManaged(false);
        this.usernamePage.setVisible(false);

        this.mainPage.setManaged(true);
        this.mainPage.setVisible(true);

        this.resize();
    }

    @FXML
    public void onSend(ActionEvent actionEvent) {
        actionEvent.consume();

        String message = this.messageInput.getText();

        if (message.isBlank() || message.length() > MulticastClient.MESSAGE_LIMIT) {
            this.messageInput.setStyle("-fx-border-color: red");
            return;
        }

        this.client.sendMessage(this.username, message);

        this.messageInput.setStyle("");
        this.messageInput.clear();
    }

    @FXML
    public void onChangeName(ActionEvent actionEvent) {
        actionEvent.consume();

        this.mainPage.setManaged(false);
        this.mainPage.setVisible(false);

        this.errorMessage.setVisible(false);
        this.usernamePage.setManaged(true);
        this.usernamePage.setVisible(true);

        this.resize();
    }

    public void onClose(WindowEvent event, Stage stage) {
        event.consume();

        if (!"".equals(this.username)) {
            this.client.sendMessage(String.format("%s left the chat...", this.username));
        }

        this.client.closeChannel();
        stage.close();
    }

    public void configureClient() {
        try {
            this.client = new MulticastClient("lo");

        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            System.err.println("Client failed to initialize - " + e.getMessage());

            this.joinBtn.setDisable(true);
            this.errorMessage.setText("Client failed to initialize!");
            this.errorMessage.setVisible(true);
        }
    }

    //There may be better ways to get the stage
    //This code should avoid NullPointerException, because it is only called in button-click handlers
    private void resize() {
        this.usernamePage.getScene().getWindow().sizeToScene();
    }
}