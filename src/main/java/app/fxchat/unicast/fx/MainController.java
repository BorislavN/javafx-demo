package app.fxchat.unicast.fx;

import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.nio.Constants;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

//TODO: introduce better exceptuion handling - check if the client is connected,
// when the server is closed we can disable the buttons, show an error popup...
// when the SenderTask fails show error (see if we can get exception message from the task)
public class MainController {
    @FXML
    private Label announcementMessage;
    @FXML
    private TextField messageInput;
    @FXML
    private Button backBtn, dmButton, sendBtn;
    @FXML
    private TextArea chatArea;
    private ChatContext context;

    public void setContext(ChatContext context) {
        this.context = context;
        this.context.setMessageListener(this.getChangeHandler());

        this.context.getChatHistory().get("public").forEach(this::appendToTextArea);
        this.setWelcomeMessage();
    }

    public void onSend(ActionEvent event) {
        event.consume();

        try {
            String message = this.messageInput.getText();

            ChatUtility.validateField("Message", message);

            message = this.context.wrapMessage(message);

            this.context.addToHistory("public", message);
            this.context.enqueueMessage(ChatUtility.newPublicMessage(message));

            this.appendToTextArea(message);

            this.setWelcomeMessage();
            this.messageInput.clear();

        } catch (IllegalArgumentException e) {
            this.setErrorMessage(e.getMessage());
        }
    }

    public void onEnter(ActionEvent event) {
        event.consume();

        this.sendBtn.fire();
    }

    public void onChangeName(ActionEvent event) {
        event.consume();

        Scene scene = Initializer.buildJoinScene(this.context);
        Stage stage = Initializer.getStage(this.backBtn);
        stage.setScene(scene);
    }

    public void onShowMessages(ActionEvent event) {
        event.consume();

        this.dmButton.setDisable(true);
        this.backBtn.setDisable(true);

        Stage window = Initializer.buildDMStage(Initializer.getStage(this.dmButton), this.context);

        window.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, this.enableButtons(window));
    }

    private EventHandler<WindowEvent> enableButtons(Stage stage) {
        return (e) -> {
            this.dmButton.setDisable(false);
            this.backBtn.setDisable(false);

            System.out.println(Thread.currentThread().getName());
            System.out.println(stage.getTitle());
        };
    }


    private void setWelcomeMessage() {
        this.announcementMessage.setStyle("");
        this.announcementMessage.setText(String.format("Welcome, %s!", this.context.getUsername()));
    }

    private void setErrorMessage(String errorMessage) {
        this.announcementMessage.setStyle("-fx-background-color: #eb4d42");
        this.announcementMessage.setText(errorMessage);
    }

    public ChangeListener<String> getChangeHandler() {
        return (observable, oldValue, newValue) -> {

            if (newValue != null && !newValue.isBlank()) {
                if (!newValue.startsWith(Constants.MEMBERS_COMMAND) && !newValue.startsWith(Constants.FROM_FLAG)) {
                    String value = this.context.extractUserMessage(newValue);

                    this.appendToTextArea(value);
                    this.context.addToHistory("public", value);
                }
            }
        };
    }

    private void appendToTextArea(String message) {
        this.chatArea.appendText(message);
        this.chatArea.appendText(System.lineSeparator());
    }
}