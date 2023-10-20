package app.chat.fxchat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

//TODO: Find out how to resize window when components are changed form visible to invisible
//TODO: Find out how to hook up to the close-event, so we can add "User left..." message
public class ChatController {
    @FXML
    private VBox usernamePage;
    @FXML
    private Label errorMessage;
    @FXML
    private TextField usernameInput;
    @FXML
    private VBox mainPage;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField messageInput;
    private String username;

    public ChatController() {
        this.username = "";
    }

    @FXML
    public void onJoinClick(ActionEvent actionEvent) {
        actionEvent.consume();

        String username = this.usernameInput.getText();

        if (username.isBlank() || username.length() > 20) {
            this.errorMessage.setVisible(true);
            return;
        }

        if ("".equals(this.username)) {
            this.textArea.appendText(String.format("%s joined the chat!%n", username));
        } else {
            this.textArea.appendText(String.format("%s changed their name to %s%n", this.username, username));
        }

        this.username = username;

        this.errorMessage.setVisible(false);
        this.usernamePage.setManaged(false);
        this.usernamePage.setVisible(false);

        this.mainPage.setManaged(true);
        this.mainPage.setVisible(true);
    }

    @FXML
    public void onSend(ActionEvent actionEvent) {
        actionEvent.consume();

        String message = this.messageInput.getText();

        if (message.isBlank() || message.length() > 50) {
            this.messageInput.setStyle("-fx-border-color: red");
            return;
        }

        this.textArea.appendText(String.format("%s: %s%n", this.username, message));

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
    }
}