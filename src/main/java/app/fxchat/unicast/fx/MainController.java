package app.fxchat.unicast.fx;

import app.fxchat.unicast.nio.ChatUtility;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

//TODO: introduce better exceptuion handling - when the server is closed we can disable the buttons, show an error popup...
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

        this.context.setMessageListener(this.getChangeHandler());
    }

    public void onSend(ActionEvent event) {
        event.consume();

        try {
            String message = this.messageInput.getText();

            ChatUtility.validateField("Message", message);

            this.setWelcomeMessage();
            this.messageInput.clear();

            message = this.context.wrapMessage(message);

            this.appendToTextArea(message);

            this.context.addToHistory("public", message);
            this.context.enqueueMessage(ChatUtility.newPublicMessage(message));

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
        this.announcementMessage.setStyle("");
        this.announcementMessage.setText(String.format("Welcome, %s!", this.context.getUsername()));
    }

    private void setErrorMessage(String errorMessage) {
        this.announcementMessage.setStyle("-fx-background-color: #eb4d42");
        this.announcementMessage.setText(errorMessage);
    }

    public ChangeListener<String> getChangeHandler() {
        return (observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println(newValue);

                String value = this.context.extractUserMessage(newValue);

                this.appendToTextArea(value);
                this.context.addToHistory("public", value);

                //TODO: append to direct, signal that there are direct messages, use CSS animation on "direct" button???
                //TODO: the direct fumcunality will prove harder than expected
                //TODO: need to find a solution in which two ChangeHandlers are set at the same time to allow for group and private messages simultaneously
            }
        };
    }

    private void appendToTextArea(String message) {
        this.chatArea.appendText(System.lineSeparator());
        this.chatArea.appendText(message);
    }
}