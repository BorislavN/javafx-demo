package app.fxchat.multicast;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

//TODO: the ip validation can be simplified
//TODO: finish implementing ip/port change
//TODO: cleanup the code
//Alternately can implement some "chat-spamming" check like in games
//To prevent sending messages faster than given threshold
public class ChatController {
    @FXML
    private VBox usernamePage, mainPage;
    @FXML
    private Label errorMessage, promptLabel;
    @FXML
    private TextField usernameInput;
    @FXML
    private Button joinBtn, showSettings, changeSettings;
    @FXML
    private Label announcement;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField messageInput;
    @FXML
    private Button sendBtn;
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

        if (username.isBlank()) {
            this.errorMessage.setText("Username cannot be blank!");
            this.errorMessage.setVisible(true);
            return;
        }

        if (username.length() > MulticastClient.USERNAME_LIMIT) {
            this.errorMessage.setText(String.format("Username too long, limit %d B", MulticastClient.USERNAME_LIMIT));
            this.errorMessage.setVisible(true);
            return;
        }

        if ("".equals(this.username)) {
            this.client.sendMessage(String.format("%s joined the chat!", username));

            //TODO: find an alternative
            //Start listening for messages
//            this.client.listenForMessages(this.textArea);

        } else if (!username.equals(this.username)) {
            this.client.sendMessage(String.format("%s changed their name to %s", this.username, username));
        }

        this.username = username;

        this.errorMessage.setVisible(false);
        this.usernamePage.setManaged(false);
        this.usernamePage.setVisible(false);

        this.announcement.setText(String.format("Welcome, %s!", this.username));
        this.announcement.setStyle("-fx-background-color: #515254");

        this.mainPage.setManaged(true);
        this.mainPage.setVisible(true);

        this.resize();
    }

    @FXML
    public void onSend(ActionEvent actionEvent) {
        actionEvent.consume();

        String message = this.messageInput.getText();

        if (message.isBlank()) {
            this.messageInput.setStyle("-fx-border-color: red");
            this.announcement.setText("Message cannot be blank!");
            this.announcement.setStyle("-fx-background-color: #eb4d42");
            return;
        }

        if (message.length() > MulticastClient.MESSAGE_LIMIT) {
            this.messageInput.setStyle("-fx-border-color: red");
            this.announcement.setText(String.format("Message too long, limit %d B", MulticastClient.MESSAGE_LIMIT));
            this.announcement.setStyle("-fx-background-color: #eb4d42");
            return;
        }

        //Commenting the actual UDP "send" request solves our freezing problem
//        this.client.sendMessage(this.username, message);
        this.textArea.appendText(String.format("%s: %s%n", this.username, message));


        this.announcement.setText(String.format("Welcome, %s!", this.username));
        this.announcement.setStyle("-fx-background-color: #515254");

        this.messageInput.setStyle("");
        this.messageInput.clear();
    }

    @FXML
    public void onEnter(ActionEvent actionEvent) {
        actionEvent.consume();

        String targetId = ((Node) actionEvent.getTarget()).getId();

        if ("usernameInput".equals(targetId)) {
            this.joinBtn.fire();
        }

        if ("messageInput".equals(targetId)) {
            this.sendBtn.fire();
        }
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

        if (this.client != null) {
            this.client.closeChannel();
        }

        stage.close();
    }

    public void onShowSettings(ActionEvent event) {
        event.consume();

        this.joinBtn.setDisable(true);

        this.promptLabel.setText("Enter group IP:");
        this.usernameInput.setText(this.client.getGroupIP());
        this.showSettings.setManaged(false);
        this.showSettings.setVisible(false);
        this.changeSettings.setManaged(true);
        this.changeSettings.setVisible(true);
    }

    public void onSaveSettings(ActionEvent event) {
        event.consume();

        this.client.changeGroup(this.usernameInput.getText(), this.errorMessage, this.textArea);

        this.promptLabel.setText("Enter an username:");
        this.usernameInput.setText(this.username);
        this.showSettings.setManaged(true);
        this.showSettings.setVisible(true);
        this.changeSettings.setManaged(false);
        this.changeSettings.setVisible(false);

        this.joinBtn.setDisable(false);
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