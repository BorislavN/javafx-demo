package app.fxchat.unicast.fx;

import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.nio.Constants;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

//TODO: fix improper wrapping of message (wrap before calling the chat utility) and displaying of the received message
public class MainController {
    @FXML
    private Label announcementMessage;
    @FXML
    private TextField messageInput;
    @FXML
    private Button backBtn, sendBtn;
    @FXML
    private TextArea chatArea;
    private ChatContext context;

    public void setContext(ChatContext context) {
        this.context = context;

        this.setWelcomeMessage();

        this.chatArea.setText(String.join(System.lineSeparator(), this.context.getChatHistory().get("public")));
    }

    public void onSend(ActionEvent event) {
        event.consume();

        try {
            String message = this.messageInput.getText();

            ChatUtility.validateField("Message", message);

            message = this.context.wrapMessage(message);

            this.context.enqueueMessage(ChatUtility.newPublicMessage(message));

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

        Stage stage = Initializer.buildDMScene(this.context);

        stage.show();
    }


    private void setWelcomeMessage() {
        this.announcementMessage.getStyleClass().remove("errorLabel");
        this.announcementMessage.setText(String.format("Welcome, %s!", this.context.getUsername()));
    }

    private void setErrorMessage(String errorMessage) {
        this.announcementMessage.getStyleClass().add("errorLabel");
        this.announcementMessage.setText(errorMessage);
    }

    public ChangeListener<String> getChangeHandler() {
        return (observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println(newValue);

                if (newValue.startsWith(Constants.PUBLIC_MESSAGE_COMMAND)) {
                    String value = this.context.extractUserMessage(newValue);

                    this.context.addToHistory("public", value);
                    this.chatArea.appendText(value);
                }

                //TODO: append to direct, signal that there are direct messages, use CSS animation on "direct" button???
            }
        };
    }
}