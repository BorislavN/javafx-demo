package app.fxchat.unicast.fx;

import app.fxchat.unicast.nio.ChatUtility;
import app.fxchat.unicast.nio.Constants;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

//TODO: introduce exceptuion handling in the "DM" stage, when the connection is lost
//TODO: add the outgoing messages to the TextArea, only if they were sent successfully

//TODO: create a unseenDMs flag in the ChatContext, so the animation will continue playing after
// the user changes their username
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
    private FadeTransition unseenMessagesAnimation;

    public void setContext(ChatContext context) {
        this.context = context;
        this.context.setMessageListener(this.getChangeHandler());
        this.context.setReceiverServiceFailHandler(this.failureHandler());

        this.context.getChatHistory().get(Constants.DEFAULT_KEY).forEach(this::appendToTextArea);
        this.setWelcomeMessage();
    }

    public void onSend(ActionEvent event) {
        event.consume();

        try {
            String message = this.messageInput.getText();

            ChatUtility.validateField("Message", message);

            message = this.context.wrapMessage(message);

            this.context.addToHistory(Constants.DEFAULT_KEY, message);
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

        Initializer.buildJoinScene(Initializer.getStage(this.backBtn), this.context);
    }

    public void onShowMessages(ActionEvent event) {
        event.consume();

        if (this.unseenMessagesAnimation != null) {
            this.unseenMessagesAnimation.stop();
            this.dmButton.setOpacity(0.4);
        }

        this.dmButton.setDisable(true);
        this.backBtn.setDisable(true);

        Stage window = Initializer.buildDMStage(Initializer.getStage(this.dmButton), this.context);

        window.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, this.enableButtons());
    }

    public ChangeListener<String> getChangeHandler() {
        return (observable, oldValue, newValue) -> {

            if (newValue != null && !newValue.isBlank()) {
                if (!newValue.startsWith(Constants.MEMBERS_COMMAND)) {
                    String[] data = this.context.extractMessageData(newValue, "\\|");

                    String user = data[1];
                    String message = data[data.length - 1];

                    if (newValue.startsWith(Constants.FROM_FLAG)) {
                        this.context.addToHistory(user, message);

                        if (!this.dmButton.isDisabled()) {
                            if (this.unseenMessagesAnimation == null) {
                                this.unseenMessagesAnimation = Initializer.newButtonAnimation(this.dmButton);
                            }

                            this.unseenMessagesAnimation.playFromStart();
                        }

                        return;
                    }

                    if (newValue.startsWith(Constants.LEFT_FLAG)) {
                        this.context.removePrivateMessages(user);
                    }


                    this.context.addToHistory(Constants.DEFAULT_KEY, message);
                    this.appendToTextArea(message);
                }
            }
        };
    }

    private EventHandler<WindowEvent> enableButtons() {
        return (e) -> {
            if (this.context.isClientLive()) {
                this.dmButton.setDisable(false);
                this.dmButton.setOpacity(1);
            }
            this.backBtn.setDisable(false);
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

    private EventHandler<WorkerStateEvent> failureHandler() {
        return (event) -> {
            this.setErrorMessage("Connection lost!");
            this.context.addToHistory(Constants.DEFAULT_KEY, "Connection to server was lost!");

            this.sendBtn.setDisable(true);
            this.dmButton.setDisable(true);

            this.context.shutdown();
        };
    }

    private void appendToTextArea(String message) {
        this.chatArea.appendText(message);
        this.chatArea.appendText(System.lineSeparator());
    }
}