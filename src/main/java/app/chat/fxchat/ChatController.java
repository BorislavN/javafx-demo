package app.chat.fxchat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

//TODO: Implement actual socket messages
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
    private Stage stage;

    public ChatController() {
        this.username = "";
        this.stage = null;
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
        } else if (!username.equals(this.username)){
            this.textArea.appendText(String.format("%s changed their name to %s%n", this.username, username));
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

        this.resize();
    }

    public void onClose(WindowEvent event, Stage stage) {
        event.consume();

        System.out.println("Close handler!");

        if (!"".equals(this.username)) {
            this.textArea.appendText(String.format("%s left the chat...%n", this.username));
        }

        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void resize() {
        if (this.stage != null) {
            stage.sizeToScene();
        }
    }
}